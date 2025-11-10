/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropChangeSupport;
import snap.util.FilePathUtils;
import snap.util.FileUtils;
import snap.util.SnapEnv;
import java.io.File;
import java.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite {

    // The URL describing this WebSite
    private WebURL _url;

    // The user name for authentication purposes
    private String _userName;

    // The password for authentication purposes
    private String _password;

    // The map of files previously vended by this data source
    private Map<String,WebFile> _files = new HashMap<>();

    // A directory that can be used for writing persistent support files
    private WebFile _sandboxDir;

    // A map to hold additional data about this site
    private Map<String,Object> _metadata = new HashMap<>();

    // PropChangeListener for file changes
    private PropChangeListener _filePropChangeLsnr = this::handleFilePropChange;

    // The PropChangeSupport for site file listeners
    private PropChangeSupport _filePCS = PropChangeSupport.EMPTY;

    /**
     * Constructor.
     */
    public WebSite()
    {
        super();
    }

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
     * Returns the site URL address string.
     */
    public String getUrlAddress()  { return _url.getString(); }

    /**
     * Returns the name for this data source.
     */
    public String getName()  { return !_url.getPath().isEmpty() ? _url.getFilename() : getHostName(); }

    /**
     * Returns the host name.
     */
    public String getHostName()  { return _url.getHost(); }

    /**
     * Returns the data source name-space and name in standard path form.
     */
    public String getPath()  { return _url.getPath(); }

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
        _userName = aName;
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
        _password = aPassword;
    }

    /**
     * Returns whether data source exists.
     */
    public boolean getExists()
    {
        WebFile rootDir = getFileForPath("/");
        return rootDir != null;
    }

    /**
     * Returns the root directory.
     */
    public WebFile getRootDir()
    {
        return createFileForPath("/", true);
    }

    /**
     * Returns the unique file instance with the given path (or null if it doesn't exist).
     */
    public synchronized WebFile getFileForPath(String aPath) throws ResponseException
    {
        // Get file from cache (just return if found and previously verified and exists)
        String filePath = FilePathUtils.getNormalizedPath(aPath);
        WebFile file = _files.get(filePath);
        if (file != null && file.isVerified() && file.getExists())
            return file;

        // Get file and return
        return getFileForPathImpl(filePath);
    }

    /**
     * Returns the file for given path (null if not found).
     */
    protected WebFile getFileForPathImpl(String filePath) throws ResponseException
    {
        // Get path URL and Head response
        WebURL url = getUrlForPath(filePath);
        WebResponse resp = url.getHead();

        // If not found, return null
        if (resp.getCode() == WebResponse.NOT_FOUND)
            return null;

        // If response contains exception, throw it
        //if (resp.getException() != null)
        //    throw new ResponseException(resp);

        // If not found, return null
        if (resp.getCode() != WebResponse.OK)
            return null;

        // Get file header from response, create file
        FileHeader fileHeader = resp.getFileHeader();

        // Create file (might as well set URL)
        WebFile file = getFileForFileHeader(fileHeader);
        file._url = url;

        // Return
        return file;
    }

    /**
     * Returns the file for given file header.
     */
    protected WebFile getFileForFileHeader(FileHeader fileHeader)
    {
        // Get file from cache
        String filePath = FilePathUtils.getNormalizedPath(fileHeader.getPath());
        boolean isDir = fileHeader.isDir();
        WebFile file = createFileForPath(filePath, isDir);

        // Update file properties
        file._lastModTime = fileHeader.getLastModTime();
        if (file._lastModTime == 0) {
            file._lastModTime = System.currentTimeMillis();
            //System.out.println("WebSite.createFileForFileHeader: Zero LastModTime provided for file: " + filePath);
        }
        file._size = fileHeader.getSize();
        file._mimeType = fileHeader.getMimeType();

        // Set link
        WebURL linkUrl = fileHeader.getLinkUrl();
        if (linkUrl != null)
            file._linkFile = linkUrl.createFile(isDir);

        // Set exists
        file.setExists(true);

        // Return
        return file;
    }

    /**
     * Creates a file for given path, regardless of whether it is known to actually exist in site.
     */
    public synchronized WebFile createFileForPath(String aPath, boolean isDir)
    {
        // Get file from cache - just return if found
        String filePath = FilePathUtils.getNormalizedPath(aPath);
        WebFile file = _files.get(filePath);
        if (file != null) {
            if (!file.isVerified() || !file.getExists())
                file._dir = isDir;
            return file;
        }

        // Create and configure new file
        file = new WebFile();
        file._path = filePath;
        file._dir = isDir;
        file._site = this;

        // Add to cache and start listening to file changes
        _files.put(filePath, file);
        file.addPropChangeListener(_filePropChangeLsnr);

        // Return
        return file;
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

        // Get whether file is being created by this save
        boolean fileBeingCreatedBySave = !aFile.getExists();

        // If parent dir doesn't exist, save it (to make sure it exists)
        WebFile parentDir = aFile.getParent();
        if (parentDir != null && !parentDir.getExists()) {
            WebResponse resp = parentDir.save();
            if (resp.getCode() != WebResponse.OK)
                return resp;
        }

        // Create PUT request for file
        WebRequest putRequest = new WebRequest(aFile);
        byte[] fileBytes = aFile.isFile() ? aFile.getBytes() : null;
        if (fileBytes == null)
            fileBytes = new byte[0];
        putRequest.setPutBytes(fileBytes);

        // Send request and get response
        WebResponse putResponse = getResponse(putRequest);

        // Just return if failed
        int respCode = putResponse.getCode();
        if (respCode != WebResponse.OK)
            return putResponse;

        // Set File.Exists and clear File.Modified since save succeeded
        aFile.setExists(true);
        aFile.setModified(false);

        // Update LastModTime
        long lastModTime = putResponse.getLastModTime();
        if (lastModTime == 0) {
            if (!SnapEnv.isWebVM)
                System.out.println("WebSite.saveFile: Unlikely saved mod time of 0 for " + aFile.getUrlAddress());
            lastModTime = System.currentTimeMillis();
        }
        aFile.setLastModTime(lastModTime);

        // If file created by save, reset parent
        if (fileBeingCreatedBySave && parentDir != null)
            parentDir.resetAndVerify();

        // Return
        return putResponse;
    }

    /**
     * Delete file.
     */
    protected WebResponse deleteFile(WebFile aFile)
    {
        // If file doesn't exist, throw exception
        if (!aFile.getExists()) {
            Exception exception = new Exception("WebSite.deleteFile: File doesn't exist: " + aFile.getPath());
            WebResponse errorResponse = new WebResponse(null);
            errorResponse.setException(exception);
            throw new ResponseException(errorResponse);
        }

        // If directory, delete child files
        if (aFile.isDir()) {
            List<WebFile> childFiles = aFile.getFiles();
            for (WebFile file : childFiles)
                file.delete();
        }

        // Create web request
        WebRequest req = new WebRequest(aFile);
        req.setType(WebRequest.Type.DELETE);

        // Get response
        WebResponse resp = getResponse(req);

        // Just return if failed
        int respCode = resp.getCode();
        if (respCode != WebResponse.OK)
            return resp;

        // Reset the file
        aFile.setExists(false);
        aFile.reset();

        // If not root, have parent reset
        WebFile parentDir = aFile.getParent();
        if (parentDir != null)
            parentDir.resetAndVerify();

        // Return
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
        switch (aReq.getType()) {
            case HEAD -> doGetOrHead(aReq, resp, true);
            case GET -> doGetOrHead(aReq, resp, false);
            case POST -> doPost(aReq, resp);
            case PUT -> doPut(aReq, resp);
            case DELETE -> doDelete(aReq, resp);
        }

        // Return response
        return resp;
    }

    /**
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        if (isHead)
            doHead(aReq, aResp);
        else doGet(aReq, aResp);
    }

    /**
     * Handles a head request.
     */
    protected void doHead(WebRequest aReq, WebResponse aResp)  { }

    /**
     * Handles a get request.
     */
    protected void doGet(WebRequest aReq, WebResponse aResp)  { }

    /**
     * Handle a get request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)
    {
        doPut(aReq, aResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        System.err.println(getClass().getSimpleName() + ".doPut: Not supported (" + aReq.getURL().getSite() + ')');
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        System.err.println(getClass().getSimpleName() + ".doDelete: Not supported (" + aReq.getURL().getSite() + ')');
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    public static void setLastModTimeForFile(WebFile aFile, long aTime) throws Exception
    {
        WebSite site = aFile.getSite();
        site.setLastModTimeForFileImpl(aFile, aTime);
    }

    /**
     * Sets the modified time for a file to underlying file system.
     */
    protected void setLastModTimeForFileImpl(WebFile aFile, long aTime) throws Exception
    {
        // Set in file - subclasses should save to real file
        aFile.setLastModTime(aTime);
    }

    /**
     * Resets all loaded site files.
     */
    public synchronized void resetFiles()
    {
        for (WebFile file : _files.values())
            file.reset();
    }

    /**
     * Returns a standard java.io.File, if available.
     */
    protected File getJavaFileForUrl(WebURL aURL)
    {
        // If URL.Source happens to be File, just return it
        Object src = aURL.getSource();
        if (src instanceof File)
            return (File) src;

        // If native URL if possible and try to get file
        java.net.URL url = aURL.getJavaUrl();
        return url != null ? FileUtils.getFile(url) : null;
    }

    /**
     * Returns a URL for the given file path.
     */
    public WebURL getUrlForPath(String aFilePath)
    {
        // If given path is already full URL string, return URL for it
        if (aFilePath.indexOf(':') >= 0)
            return WebURL.getUrl(aFilePath);

        // Get file path
        String filePath = FilePathUtils.getNormalizedPath(aFilePath);
        WebURL siteURL = getURL();
        String siteUrlString = siteURL.getString();
        if (!siteURL.getPath().isEmpty())
            siteUrlString += '!';

        // Get full URL string and return URL for it
        String fullUrlString = siteUrlString + filePath;
        return WebURL.getUrl(fullUrlString);
    }

    /**
     * Deletes this data site, assuming it corresponds to something that can be deleted, like a database.
     */
    public void deleteSite()
    {
        WebFile rootDir = getFileForPath("/");
        if (rootDir != null)
            rootDir.delete();
        _files.clear();
    }

    /**
     * Returns a metadata value for given key.
     */
    public Object getMetadataForKey(String aKey)  { return _metadata.get(aKey); }

    /**
     * Sets a metadata value for given key.
     */
    public void setMetadataForKey(String aKey, Object aValue)  { _metadata.put(aKey, aValue); }

    /**
     * Returns a WebSite that can be used for storing persistent support files.
     */
    public WebFile getSandboxDir()
    {
        // If already set, just return
        if (_sandboxDir != null) return _sandboxDir;

        // Get sandbox file: ~/Snapcode/Sandboxes/<site_name>
        String sandboxName = getSandboxSiteName();
        File snapCodeDir = FileUtils.getUserHomeDir("SnapCode", false);
        File sandboxesDir = new File(snapCodeDir, "Sandboxes");
        File sandboxDir = new File(sandboxesDir, sandboxName);

        // Get sandbox dir
        WebURL sandboxDirUrl = WebURL.getUrl(sandboxDir); assert sandboxDirUrl != null;
        return _sandboxDir = sandboxDirUrl.createFile(true);
    }

    /**
     * Returns a unique name for the Sandbox site.
     */
    protected String getSandboxSiteName()
    {
        // Get site URL and construct filename string from scheme/host/path
        String sandboxName = "";

        // Add URL.Scheme
        WebURL url = getURL();
        String scheme = url.getScheme();
        if (!scheme.equals("file"))
            sandboxName += scheme + '/';

        // Add URL.Host
        String hostname = url.getHost();
        if (hostname != null && !hostname.isEmpty())
            sandboxName += hostname + '/';

        // Add URL.Path
        String path = url.getPath();
        if (path.length() > 1)
            sandboxName += path.substring(1);

        // If filename string ends with /, trim
        if (sandboxName.endsWith("/"))
            sandboxName = sandboxName.substring(0, sandboxName.length() - 1);

        // Replace '/', '.' and ':' separators with '_'
        sandboxName = sandboxName.replace('.', '_').replace('/', '_').replace(':', '_');

        // Return
        return sandboxName;
    }

    /**
     * Returns a local file for given file (with option to cache for future use).
     */
    public WebFile getLocalFileForFile(WebFile aFile)
    {
        return aFile;
    }

    /**
     * Flushes any unsaved changes to backing store.
     */
    public void flush() throws Exception  { }

    /**
     * Resets the given file (provides a hook for subclasses).
     */
    protected void resetFile(WebFile aFile)  { aFile.resetImpl(); }

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
    protected void handleFilePropChange(PropChange aPC)
    {
        _filePCS.firePropChange(aPC);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String urlAddr = _url != null ? _url.getString() : "No site URL";
        return className + ": " + urlAddr;
    }
}