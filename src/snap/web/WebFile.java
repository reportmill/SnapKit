/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropObject;
import snap.util.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a file from a WebSite.
 */
public class WebFile extends PropObject implements Comparable<WebFile> {

    // The WebSite that provided this file
    protected WebSite  _site;

    // The file parent
    private WebFile  _parent;

    // The file path
    protected String  _path;

    // Whether file is a directory
    protected boolean  _dir;

    // The file last modified time
    protected long _lastModTime;

    // The file size
    protected long  _size;

    // Whether this file is known to exist at site
    protected Boolean  _exists;

    // The file bytes
    private byte[]  _bytes;

    // The directory files
    private List<WebFile> _files;

    // The MIME type
    private String  _mimeType;

    // A map of properties associated with file
    private Map<String,Object>  _props = new HashMap<>();

    // The URL for this file
    protected WebURL  _url;

    // Whether this file has been modified locally
    protected boolean  _modified;

    // File updater
    private Updater  _updater;

    // The link, if this file really points to another
    private WebFile  _linkFile;

    // Constants for properties
    public static final String Bytes_Prop = "Bytes";
    public static final String Size_Prop = "Size";
    public static final String LastModTime_Prop = "LastModTime";
    public static final String Modified_Prop = "Modified";
    public static final String Updater_Prop = "Updater";
    public static final String Exists_Prop = "Exists";

    /**
     * Constructor.
     */
    public WebFile()
    {
        super();
    }

    /**
     * Returns the WebSite.
     */
    public WebSite getSite()  { return _site; }

    /**
     * Returns the file parent directory.
     */
    public WebFile getParent()
    {
        // If already set, just return
        if (_parent != null || isRoot()) return _parent;

        // Get file for parent path from site
        WebSite site = getSite();
        String filePath = getPath();
        String parentPath = FilePathUtils.getParentPath(filePath);
        WebFile parentDir = site.createFileForPath(parentPath, true);

        // Set/return
        return _parent = parentDir;
    }

    /**
     * Sets the file parent.
     */
    protected void setParent(WebFile aFile)
    {
        _parent = aFile;
    }

    /**
     * Returns the file path.
     */
    public String getPath()  { return _path; }

    /**
     * Returns the resource name.
     */
    public String getName()
    {
        String filePath = getPath();
        return FilePathUtils.getFilename(filePath);
    }

    /**
     * Returns the file simple name.
     */
    public String getSimpleName()
    {
        String filePath = getPath();
        return FilePathUtils.getFilenameSimple(filePath);
    }

    /**
     * Returns the file type (extension without the '.').
     */
    public String getFileType()
    {
        String filePath = getPath();
        return FilePathUtils.getExtension(filePath).toLowerCase();
    }

    /**
     * Returns the path as a directory (with trailing separator).
     */
    public String getDirPath()
    {
        String filePath = getPath();
        return filePath.endsWith("/") ? filePath : filePath + '/';
    }

    /**
     * Returns the URL for this file.
     */
    public WebURL getUrl()
    {
        // If already set, just return
        if (_url != null) return _url;

        // Get path, site, URL and return
        WebSite site = getSite();
        String filePath = getPath();
        return _url = site.getUrlForPath(filePath);
    }

    /**
     * Returns the URL address string.
     */
    public String getUrlAddress()
    {
        WebURL url = getUrl();
        return url.getString();
    }

    /**
     * Returns whether file is a directory.
     */
    public boolean isDir()  { return _dir; }

    /**
     * Returns whether file is a plain file.
     */
    public boolean isFile()  { return !_dir; }

    /**
     * Returns whether this file is root directory.
     */
    public boolean isRoot()
    {
        return getPath().equals("/");
    }

    /**
     * Returns whether Exists property has been explicitly set/checked.
     */
    public boolean isVerified()  { return _exists != null; }

    /**
     * Verifies the file.
     */
    private void verify()
    {
        // Explicitly fetch file
        WebSite site = getSite();
        String filePath = getPath();
        WebFile file = site.getFileForPath(filePath);
        _exists = file != null;
    }

    /**
     * Returns whether file exists in site.
     */
    public boolean getExists()
    {
        // If already set, just return
        if (_exists != null) return _exists;

        // Verify file
        verify();

        // Return
        return _exists;
    }

    /**
     * Sets whether file is known to exist at site.
     */
    protected void setExists(boolean aValue)
    {
        // If already set, just return
        if (_exists != null && aValue == _exists) return;

        // Set value
        Boolean oldExists = _exists;
        _exists = aValue;

        // Fire prop change if changing real value
        if (oldExists != null)
            firePropChange(Exists_Prop, oldExists, _exists);
    }

    /**
     * Returns the file last modified time.
     */
    public long getLastModTime()
    {
        if (_lastModTime > 0) return _lastModTime;

        // Verify file
        verify();

        // Return
        return _lastModTime;
    }

    /**
     * Sets the file last modified time.
     */
    protected void setLastModTime(long aValue)
    {
        // If already set, just return
        if (aValue == _lastModTime) return;

        // Set value and fire prop change
        firePropChange(LastModTime_Prop, _lastModTime, _lastModTime = aValue);
    }

    /**
     * Sets the file modification time in file and in site internal storage.
     */
    public void saveLastModTime(long aValue)
    {
        // Save LastModTime in site and set in file
        try {
            WebSite site = getSite();
            site.saveLastModTimeForFile(this, aValue);
            setLastModTime(aValue);
        }
        catch (Exception e) { System.err.println("WebFile.setModTimeSaved: " + e); }

        // Set ModTime in file
        setLastModTime(aValue);
    }

    /**
     * Returns the modified date.
     */
    public Date getLastModDate()  { return new Date(_lastModTime); }

    /**
     * Returns the file size.
     */
    public long getSize()  { return _size; }

    /**
     * Sets the file size.
     */
    protected void setSize(long aSize)
    {
        if (aSize == _size) return;
        firePropChange(Size_Prop, _size, _size = aSize);
    }

    /**
     * Returns the file bytes.
     */
    public synchronized byte[] getBytes()
    {
        // If already set, just return
        if (_bytes != null) return _bytes;

        // Get URL response
        WebURL url = getUrl();
        WebResponse resp = url.getResponse();

        // Handle error response
        int respCode = resp.getCode();
        if (respCode != WebResponse.OK) {
            if (resp.getException() != null)
                throw new ResponseException(resp);
            System.err.println("WebFile.getBytes: Response error: " + resp.getCodeString() + " (" + getUrlAddress() + ')');
            return null;
        }

        // Configure
        setExists(true);
        _lastModTime = Math.max(_lastModTime, resp.getLastModTime());
        _bytes = resp.getBytes();
        _size = _bytes != null ? _bytes.length : 0;

        // Return
        return _bytes;
    }

    /**
     * Sets the file bytes.
     */
    public void setBytes(byte[] theBytes)
    {
        // If already set, just return
        if (ArrayUtils.equals(theBytes, _bytes)) return;

        // Set bytes and fire PropChange
        firePropChange(Bytes_Prop, _bytes, _bytes = theBytes);

        // Update Size
        setSize(theBytes != null ? theBytes.length : 0);

        // Update Modified
        setModified(true);
    }

    /**
     * Returns the number of files in this directory.
     */
    public int getFileCount()
    {
        List<WebFile> files = getFiles();
        return files.size();
    }

    /**
     * Returns the directory files list.
     */
    public synchronized List<WebFile> getFiles()
    {
        // If already set, just return
        if (_files != null) return _files;

        // Get URL response
        WebURL url = getUrl();
        WebResponse resp = url.getResponse();

        // Handle error response
        int respCode = resp.getCode();
        if (respCode != WebResponse.OK) {
            if (resp.getException() != null)
                throw new ResponseException(resp);
            System.err.println("WebFile.getFiles: Response error: " + resp.getCodeString() + " (" + getUrlAddress() + ')');
            return _files = Collections.emptyList();
        }

        // Get content files from site
        setExists(true);
        _lastModTime = Math.max(_lastModTime, resp.getLastModTime());

        // Get file headers
        List<FileHeader> fileHeaders = resp.getFileHeaders();
        if (fileHeaders == null)
            fileHeaders = Collections.emptyList();

        // Get files sorted
        WebSite site = getSite();
        List<WebFile> files = fileHeaders.stream().map(fhdr -> site.getFileForFileHeader(fhdr))
                .sorted().collect(Collectors.toList());

        // Return
        return _files = files;
    }

    /**
     * Returns the directory files list.
     */
    public WebFile[] getFilesArray()  { return getFiles().toArray(new WebFile[0]); }

    /**
     * Saves the file.
     */
    public WebResponse save()
    {
        WebSite site = getSite();
        return site.saveFile(this);
    }

    /**
     * Deletes the file.
     */
    public WebResponse delete()
    {
        WebSite site = getSite();
        return site.deleteFile(this);
    }

    /**
     * Resets the file to unverified state where nothing is known about size, mod-time, saved.
     */
    public void reset()  { _site.resetFile(this); }

    /**
     * Resets the file to unverified state where nothing is known about size, mod-time, saved.
     */
    protected void resetImpl()
    {
        _bytes = null;
        _files = null;
        _updater = null;
        _exists = null;
        _lastModTime = 0;
        _size = 0;
        setModified(false);
    }

    /**
     * Resets a file with check to make sure Exists and LastModTime properties are updated (and prop changes fired) if appropriate.
     */
    public void resetAndVerify()
    {
        // If file hasn't really been loaded, just return
        if (!isVerified() || !getExists())
            return;

        // Cache current LastModTime
        long oldLastModTime = getLastModTime();
        int dirFileCount = _dir && _files != null ? _files.size() : -1;

        // Reset file
        reset();

        // If file was deleted, reset parent and fire Exists prop change
        if (!getExists()) {
            WebFile parent = getParent();
            if (parent != null)
                parent.resetAndVerify();
            firePropChange(Exists_Prop, true, false);
        }

        // If LastModTime changed, fire LastModTime prop change
        else {
            long newLastModTime = getLastModTime();
            if (oldLastModTime != newLastModTime)
                firePropChange(LastModTime_Prop, oldLastModTime, newLastModTime);

            // Hack for WebVM lack of support for directory LastModTime updating - hopefully remote soon !!!
            else if (SnapEnv.isWebVM && dirFileCount >= 0 && dirFileCount != getFileCount())
                firePropChange(LastModTime_Prop, oldLastModTime, newLastModTime);
        }
    }

    /**
     * Returns the file with the given name.
     */
    public WebFile getFileForName(String aName)
    {
        String path = aName.startsWith("/") ? aName : getDirPath() + aName;
        return getSite().getFileForPath(path);
    }

    /**
     * Returns whether file has been modified at site (outside this process).
     */
    public boolean isModifiedExternally()
    {
        WebURL url = getUrl();
        long lastModTime = getLastModTime();
        long lastModTimeExternal = url.getLastModTime();
        return lastModTime < lastModTimeExternal;
    }

    /**
     * Returns whether given file is contained in this directory.
     */
    public boolean containsFile(WebFile aFile)
    {
        return isDir() && getSite() == aFile.getSite() && aFile.getPath().startsWith(getDirPath());
    }

    /**
     * Returns the MIME type of the file.
     */
    public String getMimeType()  { return _mimeType; }

    /**
     * Sets the MIME type for the file.
     */
    protected void setMimeType(String aMIMEType)  { _mimeType = aMIMEType; }

    /**
     * Returns a file property for key.
     */
    public Object getProp(String aKey)  { return _props.get(aKey); }

    /**
     * Sets a property for a key.
     */
    public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

    /**
     * Returns whether this file has been modified locally.
     */
    public boolean isModified()  { return _modified; }

    /**
     * Sets whether this file has been modified locally.
     */
    protected void setModified(boolean aValue)
    {
        if (aValue == _modified) return;
        firePropChange(Modified_Prop, _modified, _modified = aValue);
    }

    /**
     * Returns whether update is set and has update.
     */
    public boolean isUpdateSet()  { return _updater != null; }

    /**
     * Returns the updater.
     */
    public Updater getUpdater()  { return _updater; }

    /**
     * Sets the Updater.
     */
    public void setUpdater(Updater anUpdater)
    {
        // If already set, just return
        if (anUpdater == _updater) return;

        // Set value and fire prop change
        firePropChange(Updater_Prop, _updater, _updater = anUpdater);

        // Update modified
        setModified(anUpdater != null);
    }

    /**
     * Returns the link file, if this file really points to another.
     */
    public WebFile getLinkFile()  { return _linkFile; }

    /**
     * Sets the link file, if this file really points to another.
     */
    protected void setLinkFile(WebFile aFile)
    {
        _linkFile = aFile;
    }

    /**
     * Returns the real file.
     */
    public WebFile getRealFile()  { return _linkFile != null ? _linkFile : this; }

    /**
     * An interface for classes that want to post modifications to files.
     */
    public interface Updater {

        /**
         * Saves the file.
         */
        void updateFile(WebFile aFile);
    }

    /**
     * Returns whether response is text (regardless of what the data type is).
     */
    public boolean isText()
    {
        byte[] bytes = getBytes();
        byte junk = 0;
        if (bytes == null)
            return false;
        for (byte b : bytes) {
            if ((b & 0xFF) > 127) {
                junk++;
                if (junk > 10)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the file bytes as a string.
     */
    public String getText()
    {
        byte[] bytes = getBytes();
        if (bytes == null)
            return null;
        return new String(bytes);
    }

    /**
     * Sets the file bytes as a string.
     */
    public void setText(String aString)
    {
        byte[] bytes = StringUtils.getBytes(aString);
        setBytes(bytes);
    }

    /**
     * Returns an input stream for file.
     */
    public InputStream getInputStream()
    {
        byte[] bytes = getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Returns a standard Java File (if available).
     */
    public File getJavaFile()
    {
        WebURL url = getUrl();
        return url.getJavaFile();
    }

    /**
     * Returns a relative URL for the given file path.
     */
    public WebURL getUrlForPath(String aPath)
    {
        // If plain file, get from parent directory instead
        if (isFile())
            return getParent().getUrlForPath(aPath);

        // If path has protocol, do global eval
        if (aPath.indexOf(':') >= 0)
            return WebURL.getUrl(aPath);

        // If root path, eval with site
        if (aPath.startsWith("/"))
            return getSite().getUrlForPath(aPath);

        // Otherwise create global URL and eval
        String urlStr = FilePathUtils.getChildPath(getUrl().getString(), aPath);
        return WebURL.getUrl(urlStr);
    }

    /**
     * Creates a child file for given relative file path.
     */
    public WebFile createChildFileForPath(String filePath, boolean isDir)
    {
        WebURL fileUrl = getUrl();
        WebURL childFileUrl = fileUrl.getChildUrlForPath(filePath);
        return childFileUrl.createFile(isDir);
    }

    /**
     * Returns the file as a site.
     */
    public WebSite getAsSite()  { return getUrl().getAsSite(); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        WebFile other = anObj instanceof WebFile ? (WebFile) anObj : null;
        if (other == null) return false;
        return other.getUrl().equals(getUrl());
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return getUrl().hashCode();
    }

    /**
     * Standard compareTo implementation.
     */
    public int compareTo(WebFile aFile)
    {
        int c = aFile.getParent() != getParent() ? getPath().compareToIgnoreCase(aFile.getPath()) :
                getSimpleName().compareToIgnoreCase(aFile.getSimpleName());
        if (c == 0)
            c = getName().compareToIgnoreCase(aFile.getName());
        return c;
    }

    /**
     * Returns a string representation of file.
     */
    public String toString()
    {
        return "WebFile: " + getUrlAddress() + (isDir() && !isRoot() ? "/" : "");
    }

    /**
     * Returns a WebFile for given file path.
     */
    public static WebFile getFileForPath(String filePath)
    {
        WebURL url = WebURL.getUrl(filePath);
        return url != null ? url.getFile() : null;
    }

    /**
     * Creates a WebFile for given file path.
     */
    public static WebFile createFileForPath(String filePath, boolean isDir)
    {
        WebURL url = WebURL.getUrl(filePath);
        return url != null ? url.createFile(isDir) : null;
    }

    /**
     * Create a temp WebFile for given file name.
     */
    public static WebFile createTempFileForName(String filename, boolean isDir)
    {
        String tempFilePath = FileUtils.getTempFile(filename).getPath();
        return createFileForPath(tempFilePath, isDir);
    }

    /**
     * Returns a WebFile for given Java file.
     */
    public static WebFile getFileForJavaFile(File aFile)
    {
        WebURL fileUrl = WebURL.getUrl(aFile);
        return fileUrl != null ? fileUrl.getFile() : null;
    }
}