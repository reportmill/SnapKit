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
public boolean getExists()  { WebFile f = getFile("/"); return f!=null && f.isSaved(); }

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
    if(file!=null && file.isVerified() && file.isSaved())
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
    file = createFile(fhdr); file._saved = true; file._url = url;
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
    file._modTime = fileHdr.getLastModTime(); file._size = fileHdr.getSize();
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
    WebFile par = aFile.getParent();
    if(par!=null && !par.getVerified().isSaved())
        par.save();
    
    // Save file
    try { long mt = saveFileImpl(aFile); aFile.setModTime(mt); }
    catch(Exception e) { WebResponse r = new WebResponse(null); r.setException(e); throw new ResponseException(r); }
    
    // If this is first save, have parent resetContent() so it will be added to parent files
    if(par!=null && !aFile.isSaved())
        par.resetContent();
    
    // Set File.Saved
    aFile.setSaved(true);
}

/**
 * Delete file.
 */
protected void deleteFile(WebFile aFile) throws ResponseException
{
    // If file doesn't exist, throw exception
    if(!aFile.isSaved()) {
        Exception e = new Exception("WebSite.deleteFile: File doesn't exist: " + aFile.getPath());
        WebResponse r = new WebResponse(null); r.setException(e); new ResponseException(r);
    }
    
    // If directory, delete child files
    if(aFile.isDir()) {
        for(WebFile file : aFile.getFiles())
            file.delete(); }

    // Delete file
    try { deleteFileImpl(aFile); }
    catch(Exception e) { WebResponse r = new WebResponse(null); r.setException(e); throw new ResponseException(r); }
    
    // If not root, have parent resetContent() so file will be removed from parent files
    if(!aFile.isRoot()) { WebFile par = aFile.getParent();
        par.resetContent(); }
    
    // Resets the file
    aFile.reset();
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
protected void setModTimeSaved(WebFile aFile, long aTime) throws Exception  { }

/**
 * Resets all loaded site files.
 */
public synchronized void resetFiles()  { for(WebFile file : _files.values()) file.reset(); }

/**
 * Returns a standard java.io.File, if available.
 */
protected File getJavaFile(WebURL aURL)
{
    Object src = aURL.getSource();
    if(src instanceof File) return (File)src;
    java.net.URL url = aURL.getJavaURL();
    return url!=null? FileUtils.getFile(url) : null;
}

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