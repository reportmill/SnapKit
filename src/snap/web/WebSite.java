/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.GFXEnv;
import snap.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite {
    
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
    
    // The PropChangeSupport for site listeners
    PropChangeSupport         _pcs = PropChangeSupport.EMPTY;

    // The PropChangeSupport for site file listeners
    PropChangeSupport         _filePCS = PropChangeSupport.EMPTY;

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
        if (_url!=null) throw new RuntimeException("WebSite.setURL: Can't set URL twice");

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
    public boolean getExists()
    {
        WebFile file = getFile("/");
        return file!=null && file.isSaved();
    }

    /**
     * Returns the root directory.
     */
    public WebFile getRootDir()
    {
        WebFile file = getFile("/");
        return file!=null ? file : createFile("/", true);
    }

    /**
     * Returns a response instance for a request.
     */
    public WebResponse getResponse(WebRequest aReq)
    {
        // Create response
        WebResponse resp = new WebResponse(aReq);

        // Send to property method
        switch (aReq.getType())  {
            case HEAD: doGetOrHead(aReq, resp, true); break;
            case GET: doGetOrHead(aReq, resp, false); break;
            case POST: doPost(aReq, resp); break;
            case PUT: doPut(aReq, resp); break;
            case DELETE: doDelete(aReq, resp); break;
        }

        // Return response
        return resp;
    }

    /**
     * Executes request and invokes callback with response.
     */
    public void getResponseAndCall(WebRequest aReq, Consumer <WebResponse> aCallback)
    {
        // If platform can handle this request, just return
        if (GFXEnv.getEnv().getResponseAndCall(aReq, aCallback))
            return;

        // Otherwise wrap in thread and start
        Runnable run = () -> {
            WebResponse resp = getResponse(aReq);
            aCallback.accept(resp);
        };
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * Handles a get or head request.
     */
    protected abstract void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead);

    /**
     * Handle a get request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)  { throw new RuntimeException("handlePost"); }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)  { throw new RuntimeException("handlePut"); }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp) { throw new RuntimeException("handleDelete"); }

    /**
     * Returns the individual file with the given path.
     */
    public synchronized WebFile getFile(String aPath) throws ResponseException
    {
        // Get file from cache (just return if found)
        String path = PathUtils.getNormalized(aPath);
        WebFile file = _files.get(path);
        if (file!=null && file.isVerified() && file.isSaved())
            return file;

        // Get path URL and Head response
        WebURL url = getURL(path);
        WebResponse resp = url.getHead();

        // If not found, return null
        if (resp.getCode()==WebResponse.NOT_FOUND)
            return null;

        // If response contains exception, throw it
        if (resp.getException()!=null)
            throw new ResponseException(resp);

        // If not found, return null
        if (resp.getCode()!=WebResponse.OK)
            return null;

        // Get file header from response, create file and return
        FileHeader fhdr = resp.getFileHeader();
        file = createFile(fhdr);
        file._verified = true;
        file._saved = true;
        file._modTime = fhdr.getModTime();
        file._size = fhdr.getSize();
        file._url = url;
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
        if (file!=null)
            return file;

        // Create/configure new file
        file = new WebFile();
        file._path = path;
        file._dir = fileHdr.isDir();
        file._site = this;
        file._modTime = fileHdr.getModTime();
        file._size = fileHdr.getSize();
        file.setMIMEType(fileHdr.getMIMEType());

        // Put in cache, start listening to file changes and return
        _files.put(path, file);
        file.addPropChangeListener(_fileLsnr);
        return file;
    }

    /**
     * Returns the file if it has been entered in the file cache.
     */
    protected synchronized WebFile getFileCacheFile(String aPath)
    {
        // Get file from cache (just return if found)
        String path = PathUtils.getNormalized(aPath);
        return _files.get(path);
    }

    /**
     * Save file.
     */
    protected WebResponse saveFile(WebFile aFile)
    {
        // If there is an updater, push update and clear
        WebFile.Updater updater = aFile.getUpdater();
        if (updater!=null) {
            updater.updateFile(aFile);
            aFile.setUpdater(null);
        }

        // If parent doesn't exist, save it (to make sure it exists)
        WebFile par = aFile.getParent();
        if (par!=null && !par.getVerified().isSaved())
            par.save();

        // Create web request
        WebRequest req = new WebRequest(aFile);
        req.setPutBytes(aFile.getBytes());

        // Get response
        WebResponse resp = getResponse(req); // Used to be saveFileImpl()
        if (resp.getCode()==WebResponse.OK) {
            long mt = resp.getModTime();
            if (mt==0) System.out.println("WebSite.saveFile: Unlikely saved mod time of 0");
            aFile.setModTime(mt);
        }

        // If this is first save, have parent resetContent() so it will be added to parent files
        if (par!=null && !aFile.isSaved())
            par.resetContent();

        // Set File.Saved
        aFile.setSaved(true);
        return resp;
    }

    /**
     * Delete file.
     */
    protected WebResponse deleteFile(WebFile aFile)
    {
        // If file doesn't exist, throw exception
        if (!aFile.isSaved()) {
            Exception e = new Exception("WebSite.deleteFile: File doesn't exist: " + aFile.getPath());
            WebResponse r = new WebResponse(null); r.setException(e); new ResponseException(r);
        }

        // If directory, delete child files
        if (aFile.isDir()) {
            for (WebFile file : aFile.getFiles())
                file.delete(); }

        // Create web request
        WebRequest req = new WebRequest(aFile);
        req.setType(WebRequest.Type.DELETE);

        // Get response
        WebResponse resp = getResponse(req); // Used to be deleteFileImpl()

        // If not root, have parent resetContent() so file will be removed from parent files
        if (!aFile.isRoot()) { WebFile par = aFile.getParent();
            par.resetContent(); }

        // Resets the file
        aFile.reset();
        return resp;
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    protected void setModTimeSaved(WebFile aFile, long aTime) throws Exception  { }

    /**
     * Resets all loaded site files.
     */
    public synchronized void resetFiles()
    {
        for (WebFile file : _files.values()) file.reset();
    }

    /**
     * Returns a standard java.io.File, if available.
     */
    protected File getJavaFile(WebURL aURL)
    {
        Object src = aURL.getSource();
        if (src instanceof File) return (File)src;
        java.net.URL url = aURL.getJavaURL();
        return url!=null? FileUtils.getFile(url) : null;
    }

    /**
     * Returns a URL for the given file path.
     */
    public WebURL getURL(String aPath)
    {
        if (aPath.indexOf(':')>=0) return WebURL.getURL(aPath);
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
        if (getFile("/")!=null)
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
        if (_sandbox!=null) return _sandbox;

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
        String scheme = url.getScheme(); if (!scheme.equals("local")) fname += scheme + '/';
        String host = url.getHost(); if (host!=null && host.length()>0) fname += host + '/';
        String path = url.getPath(); if (path!=null && path.length()>1) fname += path.substring(1);

        // If filename string ends with /bin or /, trim, then replace '/' & '.' separators with '_'
        if (fname.endsWith("/bin")) fname = fname.substring(0, fname.length()-4);
        else if (fname.endsWith("/")) fname = fname.substring(0, fname.length()-1);
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
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aLsnr);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr)
    {
        _pcs.removePropChangeListener(aLsnr);
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (!_pcs.hasListener(aProp)) return;
        PropChange pc = new PropChange(this, aProp, oldVal, newVal);
        _pcs.firePropChange(pc);
    }

    /**
     * Adds a PropChangeListener to listen for any site file PropChange.
     */
    public void addFileChangeListener(PropChangeListener aLsnr)
    {
        if (_filePCS==PropChangeSupport.EMPTY) _filePCS = new PropChangeSupport(this);
        _filePCS.addPropChangeListener(aLsnr);
    }

    /**
     * Removes a site file PropChangeListener.
     */
    public void removeFileChangeListener(PropChangeListener aLsnr)  { _filePCS.removePropChangeListener(aLsnr); }

    /**
     * Called when any site file changes.
     */
    protected void fileDidPropChange(PropChange aPC)
    {
        _filePCS.firePropChange(aPC);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return getClass().getSimpleName() + ' ' + getURLString(); }

    /** Returns a "not implemented" exception for string (method name). */
    private Exception notImpl(String aStr)  { return new Exception(getClass().getName() + ": Not implemented:" + aStr); }
}