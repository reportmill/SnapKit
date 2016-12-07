/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.*;
import snap.data.DataType;
import snap.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite extends SnapObject implements PropChangeListener {
    
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
    
    // The class loader for this WebSite
    WebClassLoader            _clsLdr;

/**
 * Returns the URL.
 */
public WebURL getURL()  { return _url; }

/**
 * Sets the URL.
 */
protected void setURL(WebURL aURL)  { _url = aURL; _url._asSite = this; }

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
        case HEAD: return doHead(aRequest);
        case GET: return doGet(aRequest);
        case POST: return doPost(aRequest);
        case PUT: return doPut(aRequest);
        case DELETE: return doDelete(aRequest);
    }
    return null;
}

/**
 * Handles a head request.
 */
protected WebResponse doHead(WebRequest aRequest)
{
    // Get URL and path and create basic response
    WebURL url = aRequest.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    WebResponse resp = new WebResponse(); resp.setRequest(aRequest);
    
    // Get file header for path
    FileHeader fhdr = null; try { fhdr = getFileHeader(path); }
    catch(AccessException e) { resp.setException(e); resp.setCode(WebResponse.UNAUTHORIZED); }
    catch(Exception e) { resp.setException(e); resp.setCode(WebResponse.NOT_FOUND); }
    
    // If found, set response code to ok
    if(fhdr!=null) {
        resp.setFileHeader(fhdr); resp.setCode(WebResponse.OK); }
        
    // Otherwise mark FILE_NOT_FOUND
    else resp.setCode(WebResponse.NOT_FOUND);
    
    // Return response
    return resp;
}

/**
 * Handle a get request.
 */
protected synchronized WebResponse doGet(WebRequest aRequest)
{
    // Get URL and path and create basic response
    WebURL url = aRequest.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    WebResponse resp = new WebResponse(); resp.setRequest(aRequest);
    
    // Get file header for path
    Object content = null; try { content = getFileContent(path); }
    catch(AccessException e) { resp.setException(e); resp.setCode(WebResponse.UNAUTHORIZED); }
    catch(Exception e) { resp.setException(e); resp.setCode(WebResponse.NOT_FOUND); }
    
    // Handle file
    if(content instanceof byte[]) { byte bytes[] = (byte[])content;
        resp.setBytes(bytes); resp.setCode(WebResponse.OK);
        FileHeader fhdr = new FileHeader(path, false); fhdr.setSize(bytes.length);
        resp.setFileHeader(fhdr);
    }
        
    // Handle directory
    else if(content instanceof List) { List <FileHeader> fhdrs = (List)content;
        resp.setFileHeaders(fhdrs); resp.setCode(WebResponse.OK);
        FileHeader fhdr = new FileHeader(path, true);
        resp.setFileHeader(fhdr);
    }
        
    // Handle FILE_NOT_FOUND
    else resp.setCode(WebResponse.NOT_FOUND);
    
    // Return response
    return resp;
}

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
 * Returns a new file at the given path, regardless of whether it exists in the data source.
 */
public WebFile createFile(String aPath, boolean isDir)  { return createFile(new FileHeader(aPath, isDir)); }

/**
 * Returns a new file for given file header, regardless of whether it exists in the data source.
 */
public synchronized WebFile createFile(FileHeader fileHdr)
{
    // Get standardized path
    String path = PathUtils.getNormalized(fileHdr.getPath());
    
    // Get cached file for path - if not found, create and put new file in cache and configure (synched get/put)
    WebFile file = _files.get(path);
    if(file==null) {
        file = new WebFile(); file._path = path; file._dir = fileHdr.isDir(); file._site = this;
        file._lastModTime = fileHdr.getLastModifiedTime(); file._size = fileHdr.getSize();
        _files.put(path, file);
        file.addPropChangeListener(this);
        file.setDataType(DataType.getPathDataType(path));
    }
    
    // Return file
    return file;
}

/**
 * Returns the individual file with the given path.
 */
public synchronized WebFile getFile(String aPath) throws ResponseException
{
    // Get file from files cache
    String path = PathUtils.getNormalized(aPath);
    WebFile file = _files.get(path);
    if(file!=null && file.getExists())
        return file;

    // Get URL, request and response for path
    WebURL url = getURL(aPath);
    WebRequest req = new WebRequest(url); req.setType(WebRequest.Type.HEAD);
    WebResponse resp = getResponse(req);
    
    // If response contains exception, throw it
    if(resp.getException()!=null)
        throw new ResponseException(resp);
        
    // If not found, return null
    if(resp.getCode()==WebResponse.NOT_FOUND)
        return null;
        
    // Get file header from response, create file and return
    FileHeader fhdr = resp.getFileHeader();
    if(fhdr==null) { System.err.println("WebSite.getFile: No Header for " + url); return null; } // Can't happen?
    file = createFile(fhdr); file._exists = true;
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
    if(parent!=null && !parent.getLoaded().getExists())
        parent.save();
    
    // Save file
    try { long mt = saveFileImpl(aFile); aFile.setLastModifiedTime(mt); }
    catch(Exception e) { WebResponse r = new WebResponse(); r.setException(e); throw new ResponseException(r); }
    
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
        WebResponse r = new WebResponse(); r.setException(e); new ResponseException(r);
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
    catch(Exception e) { WebResponse r = new WebResponse(); r.setException(e); throw new ResponseException(r); }
    
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
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws Exception  { throw notImpl("getFileHeader"); }

/**
 * Returns file content (bytes for file, FileHeaders for dir).
 */
protected abstract Object getFileContent(String aPath) throws Exception;

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
protected void setLastModifiedTime(WebFile aFile, long aTime) throws Exception {}//throw notImpl("setLastModifiedTime");

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
            if(!aFile.isRoot() && aFile.getParent().isFilesSet()) { WebFile par = aFile.getParent();
                par.addFile(aFile);
                par.save();
            }
            aFile._exists = null; aFile.setExists(true);  // Force firePropertyChange to fire
        }
        return;
    }
        
    // Fetch file copy - if raw file doesn't exist, reset real file and return
    FileHeader file = null; try { file = getFileHeader(aFile.getPath()); }
    catch(Exception e) { throw new RuntimeException(e); }
    if(file==null) {
        aFile.setExists(false);
        resetFile(aFile);
        if(!aFile.isRoot() && aFile.getParent().isFilesSet()) { WebFile par = aFile.getParent();
            par.removeFile(aFile);
            par.save();
        }
        return;
    }
    
    // If raw file has new modified time, reset file and set new modified time
    if(file.getLastModifiedTime()>aFile.getLastModifiedTime()) {
        aFile._size = file.getSize();
        aFile.setBytes(null); aFile.setFiles(null);
        aFile.setLastModifiedTime(file.getLastModifiedTime());
    }
}

/**
 * Resets a file.
 */
public synchronized void resetFile(WebFile aFile)
{
    aFile.removePropChangeListener(this);
    aFile.setFiles(null); aFile.setBytes(null); aFile._lastModTime = 0; aFile._size = 0; aFile._exists = null;
    aFile.addPropChangeListener(this);
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
 * Creates the data site remote site (database, directory file, etc.).
 */
public void createSite() throws Exception  { }

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
public WebSite getSandbox()  { return _sandbox!=null? _sandbox : (_sandbox=createSandbox()); }

/**
 * Sets a WebSite that can be used for storing persistent support files.
 */
public void setSandbox(WebSite aSandbox)  { _sandbox = aSandbox; }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected WebSite createSandbox()  { return createSandboxURL().getAsSite(); }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected WebURL createSandboxURL()  { return WebURL.getURL(createSandboxURLS()); }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected String createSandboxURLS()
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
 * Returns the DataClassLoader.
 */
public WebClassLoader getClassLoader() { return _clsLdr!=null? _clsLdr : (_clsLdr=new WebClassLoader(this));}

/**
 * Adds a deep (property) change listener to get notified when this WebSite sees changes (to files).
 */
public void addDeepChangeListener(DeepChangeListener aListener)
{
    addListener(DeepChangeListener.class, aListener);
}

/**
 * Removes a deep (property) change listener to get notified when this WebSite sees changes (to files).
 */
public void removeDeepChangeListener(DeepChangeListener aListener)
{
    removeListener(DeepChangeListener.class, aListener);
}

/**
 * Handle property changes on row objects by forwarding to listener.
 */
public void propertyChange(PropChange anEvent)
{
    // Forward to deep change listeners
    for(int i=0, iMax=getListenerCount(DeepChangeListener.class); i<iMax; i++)
        getListener(DeepChangeListener.class, i).deepChange(this, anEvent);
}

/**
 * Clears site Schema and ClassLoader.
 */
public synchronized void refresh()  { _clsLdr = null; }

/**
 * Flushes any unsaved changes to backing store.
 */
public void flush() throws Exception { }

/** Returns a "not implemented" exception for string (method name). */
private Exception notImpl(String aStr)  { return new Exception(getClass().getName() + ": Not implemented:" + aStr); }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ' ' + getURLString(); }

}