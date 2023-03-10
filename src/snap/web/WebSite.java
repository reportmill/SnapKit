/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropChangeSupport;
import snap.util.ArrayUtils;
import snap.util.FileUtils;
import java.io.File;
import java.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite {

    // The URL describing this WebSite
    private WebURL  _url;

    // The user name for authentication purposes
    private String  _userName;

    // The password for authentication purposes
    private String  _password;

    // The map of files previously vended by this data source
    private Map<String,WebFile>  _files = new HashMap<>();

    // A WebSite that can be used for writing persistent support files
    private WebSite  _sandbox;

    // A map of properties associated with file
    private Map<String,Object>  _props = new HashMap<>();

    // PropChangeListener for file changes
    private PropChangeListener  _fileLsnr = pc -> fileDidPropChange(pc);

    // The PropChangeSupport for site listeners
    private PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // The PropChangeSupport for site file listeners
    private PropChangeSupport  _filePCS = PropChangeSupport.EMPTY;

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
        if (_url != null) throw new RuntimeException("WebSite.setURL: Can't set URL twice");

        // Set URL
        _url = aURL;
        _url._asSite = this;

        // Set in known sites
        WebGetter.setSite(aURL, this);
    }

    /**
     * Returns the URL root.
     */
    public String getURLString()
    {
        WebURL url = getURL();
        return url.getString();
    }

    /**
     * Returns the name for this data source.
     */
    public String getName()
    {
        WebURL url = getURL();
        return url.getPath() != null ? url.getPathName() : url.getHost();
    }

    /**
     * Returns the host name.
     */
    public String getHostName()
    {
        WebURL url = getURL();
        return url.getHost();
    }

    /**
     * Returns the data source name-space and name in standard path form.
     */
    public String getPath()
    {
        WebURL url = getURL();
        return url.getPath();
    }

    /**
     * Returns the user name.
     */
    public String getUserName()  { return _userName; }

    /**
     * Sets the user name.
     */
    public void setUserName(String aName)
    {
        if (Objects.equals(aName, _userName)) return;
        firePropChange("UserName", _userName, _userName = aName);
    }

    /**
     * Returns the password.
     */
    public String getPassword()  { return _password; }

    /**
     * Sets the password.
     */
    public void setPassword(String aPassword)
    {
        if (Objects.equals(aPassword, _password)) return;
        firePropChange("Password", _password, _password = aPassword);
    }

    /**
     * Returns whether data source exists.
     */
    public boolean getExists()
    {
        WebFile file = getFileForPath("/");
        return file != null && file.isSaved();
    }

    /**
     * Returns the root directory.
     */
    public WebFile getRootDir()
    {
        WebFile file = getFileForPath("/");
        if (file == null)
            file = createFileForPath("/", true);
        return file;
    }

    /**
     * Returns the unique file instance with the given path (or null if it doesn't exist).
     */
    public synchronized WebFile getFileForPath(String aPath) throws ResponseException
    {
        // Get file from cache (just return if found)
        String filePath = PathUtils.getNormalized(aPath);
        WebFile file = _files.get(filePath);
        if (file != null && file.isVerified() && file.isSaved())
            return file;

        // Return
        return getFileForPathImpl(filePath);
    }

    /**
     * Returns the individual file with the given path.
     */
    protected WebFile getFileForPathImpl(String filePath) throws ResponseException
    {
        // Get path URL and Head response
        WebURL url = getURL(filePath);
        WebResponse resp = url.getHead();

        // If not found, return null
        if (resp.getCode() == WebResponse.NOT_FOUND)
            return null;

        // If response contains exception, throw it
        if (resp.getException() != null)
            throw new ResponseException(resp);

        // If not found, return null
        if (resp.getCode() != WebResponse.OK)
            return null;

        // Get file header from response, create file
        FileHeader fileHeader = resp.getFileHeader();
        WebFile file = createFile(fileHeader);
        file._verified = true;
        file._saved = true;
        file._url = url;

        // Return
        return file;
    }

    /**
     * Returns a new file for given path, regardless of whether it exists on site.
     */
    public WebFile createFileForPath(String aPath, boolean isDir)
    {
        FileHeader fileHeader = new FileHeader(aPath, isDir);
        return createFile(fileHeader);
    }

    /**
     * Returns a new file for given file header, regardless of whether it exists on site.
     */
    protected synchronized WebFile createFile(FileHeader fileHdr)
    {
        // Get file from cache
        String path = PathUtils.getNormalized(fileHdr.getPath());
        WebFile file = _files.get(path);

        // If not found, create and add to cache
        if (file == null) {

            // Create/config
            file = new WebFile();
            file._path = path;
            file._dir = fileHdr.isDir();
            file._site = this;

            // Put in cache, start listening to file changes
            _files.put(path, file);
            file.addPropChangeListener(_fileLsnr);
        }

        // Update properties file
        file._modTime = fileHdr.getModTime();
        file._size = fileHdr.getSize();
        file.setMimeType(fileHdr.getMimeType());

        // Return
        return file;
    }

    /**
     * Returns the contents for given file.
     */
    protected FileContents getContentsForFile(WebFile aFile)
    {
        // Get request/response for file URL
        WebURL url = aFile.getURL();
        WebResponse resp = url.getResponse();
        int respCode = resp.getCode();
        long modTime = resp.getModTime();

        // Handle response
        if (respCode != WebResponse.OK) {
            if (resp.getException() != null)
                throw new ResponseException(resp);
            System.err.println("WebSite.getContentsForFile: Response error: " + resp.getCodeString() + ", for file: " + aFile.getPath());
            if (aFile.isDir())
                return new FileContents(new WebFile[0], 0);
            return null;
        }

        // Handle plain file
        if (aFile.isFile()) {
            byte[] bytes = resp.getBytes();
            return new FileContents(bytes, modTime);
        }

        // Get file headers
        FileHeader[] fileHeaders = resp.getFileHeaders();
        if (fileHeaders == null)
            return new FileContents(new WebFile[0], 0);

        // Get files
        WebFile[] files = new WebFile[fileHeaders.length];
        for (int i = 0; i < fileHeaders.length; i++) {
            FileHeader fileHeader = fileHeaders[i];
            WebFile file = files[i] = createFile(fileHeader);
            file._saved = true;
        }

        // Return
        return new FileContents(files, modTime);
    }

    /**
     * Save file.
     */
    protected WebResponse saveFile(WebFile aFile)
    {
        // If there is an updater, push update and clear
        WebFile.Updater updater = aFile.getUpdater();
        if (updater != null) {
            updater.updateFile(aFile);
            aFile.setUpdater(null);
        }

        // If parent doesn't exist, save it (to make sure it exists)
        WebFile par = aFile.getParent();
        if (par != null && !par.getVerified().isSaved())
            par.save();

        // Create web request
        WebRequest req = new WebRequest(aFile);
        byte[] fileBytes = aFile.getBytes();
        req.setPutBytes(fileBytes);

        // Get response
        WebResponse resp = getResponse(req); // Used to be saveFileImpl()
        int respCode = resp.getCode();
        if (respCode == WebResponse.OK) {
            long modTime = resp.getModTime();
            if (modTime == 0)
                System.out.println("WebSite.saveFile: Unlikely saved mod time of 0");
            aFile.setModTime(modTime);
        }

        // If this is first save, have parent resetContent() so it will be added to parent files
        if (par != null && !aFile.isSaved())
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
            WebResponse r = new WebResponse(null);
            r.setException(e);
            throw new ResponseException(r);
        }

        // If directory, delete child files
        if (aFile.isDir()) {
            WebFile[] childFiles = aFile.getFiles();
            for (WebFile file : childFiles)
                file.delete();
        }

        // Create web request
        WebRequest req = new WebRequest(aFile);
        req.setType(WebRequest.Type.DELETE);

        // Get response
        WebResponse resp = getResponse(req); // Used to be deleteFileImpl()

        // If not root, have parent resetContent() so file will be removed from parent files
        if (!aFile.isRoot()) {
            WebFile par = aFile.getParent();
            par.resetContent();
        }

        // Resets the file
        aFile.reset();
        return resp;
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
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get file for Request.URL.Path
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();
        WebFile file = getFileForPathImpl(filePath);

        // If not found, return not found
        if (file == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Create FileHeader for file and set in Response
        FileHeader fileHeader = new FileHeader(file);
        aResp.setFileHeader(fileHeader);

        // If Head, just return
        if (isHead)
            return;

        // Handle plain file contents
        if (file.isFile()) {
            byte[] bytes = file.getBytes();
            aResp.setBytes(bytes);
        }

        // Handle directory file contents
        else {
            WebFile[] dirFiles = file.getFiles();
            FileHeader[] fileHeaders = ArrayUtils.map(dirFiles, dirFile -> new FileHeader(dirFile), FileHeader.class);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Handle a get request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)
    {
        throw new RuntimeException("handlePost");
    }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        throw new RuntimeException("handlePut");
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        throw new RuntimeException("handleDelete");
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    protected void setModTimeSaved(WebFile aFile, long aTime) throws Exception
    {
    }

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
        if (src instanceof File)
            return (File) src;
        java.net.URL url = aURL.getJavaURL();
        return url != null ? FileUtils.getFile(url) : null;
    }

    /**
     * Returns a URL for the given file path.
     */
    public WebURL getURL(String aPath)
    {
        if (aPath.indexOf(':') >= 0)
            return WebURL.getURL(aPath);
        String path = PathUtils.getNormalized(aPath);
        WebURL url = getURL();
        String urls = url.getString();
        if (url.getPath() != null)
            urls += '!';
        return WebURL.getURL(urls + path);
    }

    /**
     * Deletes this data site, assuming it corresponds to something that can be deleted, like a database.
     */
    public void deleteSite() throws Exception
    {
        WebFile rootDir = getFileForPath("/");
        if (rootDir != null)
            rootDir.delete();
    }

    /**
     * Returns a file property for key.
     */
    public Object getProp(String aKey)
    {
        return _props.get(aKey);
    }

    /**
     * Sets a property for a key.
     */
    public void setProp(String aKey, Object aValue)
    {
        _props.put(aKey, aValue);
    }

    /**
     * Returns a WebSite that can be used for storing persistent support files.
     */
    public WebSite getSandbox()
    {
        // If already set, just return
        if (_sandbox != null) return _sandbox;

        // Get sandbox url
        String sandboxName = getSandboxName();
        String sandboxUrlStr = "local:/Sandboxes/" + sandboxName;
        WebURL sandboxURL = WebURL.getURL(sandboxUrlStr);

        // Get, set, return
        WebSite sandboxSite = sandboxURL.getAsSite();
        return _sandbox = sandboxSite;
    }

    /**
     * Creates a WebSite that can be used for storing persistent support files.
     */
    protected String getSandboxName()
    {
        // Get site URL and construct filename string from scheme/host/path
        String sandboxName = "";

        // Add URL.Scheme
        WebURL url = getURL();
        String scheme = url.getScheme();
        if (!scheme.equals("local"))
            sandboxName += scheme + '/';

        // Add URL.Host
        String host = url.getHost();
        if (host != null && host.length() > 0)
            sandboxName += host + '/';

        // Add URL.Path
        String path = url.getPath();
        if (path != null && path.length() > 1)
            sandboxName += path.substring(1);

        // If filename string ends with /bin or /, trim
        if (sandboxName.endsWith("/bin"))
            sandboxName = sandboxName.substring(0, sandboxName.length() - 4);
        else if (sandboxName.endsWith("/"))
            sandboxName = sandboxName.substring(0, sandboxName.length() - 1);

        // Replace '/' & '.' separators with '_'
        sandboxName = sandboxName.replace('.', '_').replace('/', '_');

        // Return
        return sandboxName;
    }

    /**
     * Returns a local file for given file (with option to cache for future use).
     */
    public WebFile getLocalFile(WebFile aFile, boolean doCache)
    {
        return aFile;
    }

    /**
     * Clears site caches.
     */
    public synchronized void refresh()  { }

    /**
     * Flushes any unsaved changes to backing store.
     */
    public void flush() throws Exception  { }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs == PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
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
        if (_filePCS == PropChangeSupport.EMPTY)
            _filePCS = new PropChangeSupport(this);
        _filePCS.addPropChangeListener(aLsnr);
    }

    /**
     * Removes a site file PropChangeListener.
     */
    public void removeFileChangeListener(PropChangeListener aLsnr)
    {
        _filePCS.removePropChangeListener(aLsnr);
    }

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
    public String toString()
    {
        String className = getClass().getSimpleName();
        return className + ' ' + getURLString();
    }
}