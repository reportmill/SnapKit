/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.*;
import snap.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite extends SnapObject {
    
    // The URL describing this WebSite
    WebURL                    _url;
    
    // The user name for authentication purposes
    String                    _userName;
    
    // The password for authentication purposes
    String                    _password;
    
    // The map of files previously vended by this data source
    Map <String,WebFile>      _files = new HashMap();
    
    // A WebSite that can be used for writing persistent support files
    WebSite                   _sandbox;
    
    // A map of properties associated with file
    Map                       _props = new HashMap();
    
    // PropChangeListener for file changes
    PropChangeListener        _fileLsnr = pc -> fileDidPropChange(pc);
    
/**
 * Returns the URL.
 */
public WebURL getURL()  { return _url; }

/**
 * Sets the URL.
 */
public void setURL(WebURL aURL)
{
    // WebSite URL can't be set twice
    if(_url!=null) throw new RuntimeException("WebSite.setURL: Can't set URL twice");
    
    // Set URL
    _url = aURL; _url._asSite = this;
    
    // Set in known sites
    WebGetter.setSite(aURL, this);
}

/**
 * Returns the URL root.
 */
public String getURLString()  { return getURL().getString(); }

/**
 * Returns the name for this data source.
 */
public String getName()  { return getURL().getPath()!=null? getURL().getPathName() : getURL().getHost(); }

/**
 * Returns the host name.
 */
public String getHostName()  { return getURL().getHost(); }

/**
 * Returns the data source name-space and name in standard path form.
 */
public String getPath()  { return getURL().getPath(); }

/**
 * Returns the user name.
 */
public String getUserName()  { return _userName; }

/**
 * Sets the user name.
 */
public void setUserName(String aName)  { firePropChange("UserName", _userName, _userName = aName); }

/**
 * Returns the password.
 */
public String getPassword()  { return _password; }

/**
 * Sets the password.
 */
public void setPassword(String aPassword)  { firePropChange("Password", _password, _password = aPassword); }

/**
 * Returns whether data source exists.
 */
public boolean getExists()  { WebFile f = getFile("/"); return f!=null && f.getExists(); }

/**
 * Returns the root directory.
 */
public WebFile getRootDir()  { WebFile f = getFile("/"); return f!=null? f : createFile("/", true); }

/**
 * Returns a response instance for a request.
 */
public WebResponse getResponse(WebRequest aRequest)
{
    switch(aRequest.getType())  {
        case HEAD: return doGetOrHead(aRequest, true);
        case GET: return doGetOrHead(aRequest, false);
        case POST: return doPost(aRequest);
        case PUT: return doPut(aRequest);
        case DELETE: return doDelete(aRequest);
    }
    return null;
}

/**
 * Handles a get or head request.
 */
protected abstract WebResponse doGetOrHead(WebRequest aReq, boolean isHead);

/**
 * Handle a get request.
 */
protected WebResponse doPost(WebRequest aRequest)  { throw new RuntimeException("handlePost"); }

/**
 * Handle a PUT request.
 */
protected WebResponse doPut(WebRequest aRequest)  { throw new RuntimeException("handlePut"); }

/**
 * Handle a DELETE request.
 */
protected WebResponse doDelete(WebRequest aRequest) { throw new RuntimeException("handleDelete"); }

/**
 * Returns the individual file with the given path.
 */
public synchronized WebFile getFile(String aPath) throws ResponseException
{
    // Get file from cache (just return if found)
    String path = PathUtils.getNormalized(aPath);
    WebFile file = _files.get(path);
    if(file!=null && file.getExists())
        return file;

    // Get path URL and Head response
    WebURL url = getURL(path);
    WebResponse resp = url.getHead();
    
    // If not found, return null
    if(resp.getCode()==WebResponse.NOT_FOUND)
        return null;
        
    // If response contains exception, throw it
    if(resp.getException()!=null)
        throw new ResponseException(resp);
        
    // Get file header from response, create file and return
    FileHeader fhdr = resp.getFileHeader();
    file = createFile(fhdr); file._exists = true; file._url = url;
    return file;
}

/**
 * Returns a new file for given path, regardless of whether it exists on site.
 */
public WebFile createFile(String aPath, boolean isDir)  { return createFile(new FileHeader(aPath, isDir)); }

/**
 * Returns a new file for given file header, regardless of whether it exists on site.
 */
protected synchronized WebFile createFile(FileHeader fileHdr)
{
    // Get file from cache (just return if found)
    String path = PathUtils.getNormalized(fileHdr.getPath());
    WebFile file = _files.get(path);
    if(file!=null)
        return file;
    
    // Create/configure new file
    file = new WebFile(); file._path = path; file._dir = fileHdr.isDir(); file._site = this;
    file._lastModTime = fileHdr.getLastModTime(); file._size = fileHdr.getSize();
    file.setMIMEType(fileHdr.getMIMEType());
    
    // Put in cache, start listening to file changes and return
    _files.put(path, file);
    file.addPropChangeListener(_fileLsnr);
    return file;
}

/**
 * Save file.
 */
protected void saveFile(WebFile aFile) throws ResponseException
{
    // If there is an updater, push update and clear
    WebFile.Updater updater = aFile.getUpdater();
    if(updater!=null) {
        updater.updateFile(aFile); aFile.setUpdater(null); }

    // If parent doesn't exist, save it (to make sure it exists)
    WebFile parent = aFile.getParent();
    if(parent!=null && !parent.getVerified().getExists())
        parent.save();
    
    // Save file
    try { long mt = saveFileImpl(aFile); aFile.setLastModTime(mt); }
    catch(Exception e) { WebResponse r = new WebResponse(null); r.setException(e); throw new ResponseException(r); }
    
    // If file needs to be added to parent, add and save
    if(parent!=null && !aFile.getExists()) {
        parent.addFile(aFile);
        parent.save();
    }
    
    // Set File.Exists
    aFile.setExists(true);
}

/**
 * Delete file.
 */
protected void deleteFile(WebFile aFile) throws ResponseException
{
    // If file doesn't exist, throw exception
    if(!aFile.getExists()) {
        Exception e = new Exception("WebSite.deleteFile: File doesn't exist: " + aFile.getPath());
        WebResponse r = new WebResponse(null); r.setException(e); new ResponseException(r);
    }
    
    // If directory, delete child files
    if(aFile.isDir()) {
        aFile._exists = false;
        for(WebFile file : aFile.getFiles())
            file.delete();
        aFile._exists = true;
    }

    // Delete file
    try { deleteFileImpl(aFile); }
    catch(Exception e) { WebResponse r = new WebResponse(null); r.setException(e); throw new ResponseException(r); }
    
    // If not root, remove file from parent, and if parent still exists, save
    if(!aFile.isRoot()) { WebFile parent = aFile.getParent();
        parent.removeFile(aFile);
        if(parent.getExists())
            parent.save();
    }
    
    // Resets the file
    aFile.setExists(false);
    resetFile(aFile);
}

/**
 * Saves a file.
 */
protected long saveFileImpl(WebFile aFile) throws Exception { throw notImpl("saveFileImpl"); }

/**
 * Deletes a file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception { throw notImpl("deleteFileImpl"); }

/**
 * Saves the modified time for a file to underlying file system.
 */
protected void setLastModTime(WebFile aFile, long aTime) throws Exception  { }

/**
 * Reloads a file from site.
 */
protected void reloadFile(WebFile aFile)
{
    // Clear updater (if present)
    aFile.setUpdater(null);

    // If file doesn't exist, try a fetch, if exists, reload parent and return
    if(!aFile.getExists()) {
        getFile(aFile.getPath());
        if(aFile.getExists()) {
            aFile.setBytes(null); aFile.setFiles(null);
            if(!aFile.isRoot() && aFile.getParent().isLoaded()) { WebFile par = aFile.getParent();
                par.addFile(aFile);
                par.save();
            }
            aFile._exists = null; aFile.setExists(true);  // Force firePropertyChange to fire
        }
        return;
    }
        
    // Get updated HEAD response
    WebResponse resp = aFile.getURL().getHead();
    
    // Handle NOT_FOUND: Update File.Exists, reset and remove from parent
    if(resp.getCode()==WebResponse.NOT_FOUND) {
        aFile.setExists(false);
        resetFile(aFile);
        if(!aFile.isRoot() && aFile.getParent().isLoaded()) { WebFile par = aFile.getParent();
            par.removeFile(aFile);
            par.save();
        }
        return;
    }
    
    // Handle new mod time: reset file and set new modified time
    if(resp.getLastModTime()>aFile.getLastModTime()) {
        aFile._size = resp.getSize();
        aFile.setBytes(null); aFile.setFiles(null);
        aFile.setLastModTime(resp.getLastModTime());
    }
}

/**
 * Resets a file.
 */
protected synchronized void resetFile(WebFile aFile)
{
    aFile.removePropChangeListener(_fileLsnr);
    aFile.setFiles(null); aFile.setBytes(null); aFile._lastModTime = 0; aFile._size = 0; aFile._exists = null;
    aFile.addPropChangeListener(_fileLsnr);
}

/**
 * Resets all loaded site files.
 */
public synchronized void resetFiles()  { for(WebFile file : _files.values()) resetFile(file); }

/**
 * Returns a standard java.io.File, if available.
 */
protected File getStandardFile(WebFile aFile)  { return null; }

/**
 * Returns a URL for the given file path.
 */
public WebURL getURL(String aPath)
{
    if(aPath.indexOf(':')>=0) return WebURL.getURL(aPath);
    String path = PathUtils.getNormalized(aPath);
    WebURL url = getURL();
    String urls = url.getString(); if(url.getPath()!=null) urls += '!';
    return WebURL.getURL(urls + path);
}

/**
 * Deletes this data site, assuming it corresponds to something that can be deleted, like a database.
 */
public void deleteSite() throws Exception
{
    if(getFile("/")!=null)
        getFile("/").delete();
}

/**
 * Returns a file property for key.
 */
public Object getProp(String aKey)  { return _props.get(aKey); }

/**
 * Sets a property for a key.
 */
public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

/**
 * Returns a WebSite that can be used for storing persistent support files.
 */
public WebSite getSandbox()
{
    // If already set, just return
    if(_sandbox!=null) return _sandbox;
    
    // Create and return
    WebURL sboxURL = WebURL.getURL(getSandboxURLS());
    return _sandbox = sboxURL.getAsSite();
}

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected String getSandboxURLS()
{
    // Get site URL and construct filename string from scheme/host/path
    WebURL url = getURL(); String fname = "";
    String scheme = url.getScheme(); if(!scheme.equals("local")) fname += scheme + '/';
    String host = url.getHost(); if(host!=null && host.length()>0) fname += host + '/';
    String path = url.getPath(); if(path!=null && path.length()>1) fname += path.substring(1);
    
    // If filename string ends with /bin or /, trim, then replace '/' & '.' separators with '_'
    if(fname.endsWith("/bin")) fname = fname.substring(0, fname.length()-4);
    else if(fname.endsWith("/")) fname = fname.substring(0, fname.length()-1);
    fname = fname.replace('.', '_').replace('/', '_');
    
    // Return URL string for filename in local Sandboxes directory
    return "local:/Sandboxes/" + fname;
}

/**
 * Returns a local file for given file (with option to cache for future use).
 */
public WebFile getLocalFile(WebFile aFile, boolean doCache)  { return aFile; }

/**
 * Clears site caches.
 */
public synchronized void refresh()  { }

/**
 * Flushes any unsaved changes to backing store.
 */
public void flush() throws Exception { }

/**
 * Property change listener implementation to forward changes on to deep listeners.
 */
public void fileDidPropChange(PropChange aPCE)  { _pcs.fireDeepChange(this, aPCE); }

/** Returns a "not implemented" exception for string (method name). */
private Exception notImpl(String aStr)  { return new Exception(getClass().getName() + ": Not implemented:" + aStr); }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ' ' + getURLString(); }

}