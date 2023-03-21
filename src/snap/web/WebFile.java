/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropObject;
import snap.util.ArrayUtils;
import snap.util.FilePathUtils;
import snap.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

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
    protected long  _modTime;

    // The file size
    protected long  _size;

    // Whether this file is known to exist at site
    protected Boolean  _exists;

    // The file bytes
    private byte[]  _bytes;

    // The directory files
    private WebFile[]  _files;

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
    public static final String ModTime_Prop = "ModTime";
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
        String parentPath = FilePathUtils.getParent(filePath);
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
        return FilePathUtils.getFileName(filePath);
    }

    /**
     * Returns the file simple name.
     */
    public String getSimpleName()
    {
        String filePath = getPath();
        return FilePathUtils.getFileNameSimple(filePath);
    }

    /**
     * Returns the file type (extension without the '.').
     */
    public String getType()
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
    public WebURL getURL()
    {
        // If already set, just return
        if (_url != null) return _url;

        // Get path, site, URL and return
        WebSite site = getSite();
        String filePath = getPath();
        return _url = site.getURL(filePath);
    }

    /**
     * Returns the URL string.
     */
    public String getUrlString()
    {
        WebURL url = getURL();
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
     * Returns whether file exists in site.
     */
    public boolean getExists()
    {
        // If already set, just return
        if (_exists != null) return _exists;

        // Explicitly fetch file
        WebSite site = getSite();
        String filePath = getPath();
        WebFile file = site.getFileForPath(filePath);
        _exists = file != null;

        // Return
        return _exists;
    }

    /**
     * Sets whether file is known to exist at site.
     */
    protected void setExists(Boolean aValue)
    {
        // If already set, just return
        if (aValue == _exists) return;

        // Set value
        Boolean oldExists = _exists;
        _exists = aValue;

        // Fire prop change if changing real value
        if (oldExists != null && _exists != null)
            firePropChange(Exists_Prop, oldExists, _exists);
    }

    /**
     * Returns the file modification time.
     */
    public long getModTime()  { return _modTime; }

    /**
     * Sets the file modification time.
     */
    protected void setModTime(long aValue)
    {
        // If already set, just return
        if (aValue == _modTime) return;

        // Set value and fire prop change
        firePropChange(ModTime_Prop, _modTime, _modTime = aValue);
    }

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

        // Get content bytes from site
        WebSite site = getSite();
        FileContents fileContents = site.getContentsForFile(this);
        if (fileContents != null) {
            setExists(true);
            _modTime = Math.max(_modTime, fileContents.getModTime());
            _bytes = fileContents.getBytes();
            _size = _bytes != null ? _bytes.length : 0;
        }

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
        WebFile[] files = getFiles();
        return files.length;
    }

    /**
     * Returns the directory files list.
     */
    public synchronized WebFile[] getFiles()
    {
        // If already set, just return
        if (_files != null) return _files;

        // Get content files from site
        WebSite site = getSite();
        FileContents fileContents = site.getContentsForFile(this);
        if (fileContents != null) {
            setExists(true);
            _modTime = Math.max(_modTime, fileContents.getModTime());
            WebFile[] files = fileContents.getFiles();
            for (WebFile file : files) {
                file.setParent(this);
                file.setExists(true);
            }
            _files = files;
        }

        // Return
        return _files;
    }

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
    public void reset()
    {
        resetContent();
        setExists(null);
        _modTime = 0;
        _size = 0;
    }

    /**
     * Resets the file to unloaded state so files/bytes are cleared (to reload when next called).
     */
    public void resetContent()
    {
        _bytes = null;
        _files = null;
        _updater = null;
        setModified(false);
    }

    /**
     * Resets a file with check to have parent resetContent() if file is deleted.
     */
    public void reload()
    {
        // If file hasn't really been loaded, just reset content and return
        if (!isVerified()) {
            resetContent();
            return;
        }

        // Cache current ModTime
        long oldModTime = getModTime();

        // Reset file
        reset();

        // If file was deleted, reset parent content and fire Saved prop change
        boolean isDeleted = !getExists();
        if (isDeleted) {
            WebFile parent = getParent();
            if (parent != null)
                parent.resetContent();
            setExists(false);
        }

        // If File and ModTime changed, fire ModTime prop change
        else if (isFile()) {
            long newModTime = getModTime();
            if (oldModTime != 0 && oldModTime != newModTime)
                firePropChange(ModTime_Prop, oldModTime, newModTime);
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
        WebURL url = getURL();
        long modTime = getModTime();
        long modTimeExternal = url.getLastModTime();
        return modTime < modTimeExternal;
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
        if (anUpdater != null)
            setModified(true);
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
     * Sets the file modification time in file and in site internal storage.
     */
    public void setModTimeSaved(long aValue)
    {
        // Set ModTime in site
        try {
            WebSite site = getSite();
            site.setModTimeForFile(this, aValue);
        }
        catch (Exception e) { System.err.println("WebFile.setModTimeSaved: " + e); }

        // Set ModTime in file
        setModTime(aValue);
    }

    /**
     * Returns the modified date.
     */
    public Date getModDate()  { return new Date(_modTime); }

    /**
     * Conventional file method that simply wraps getModTime().
     */
    public long getLastModTime()  { return _modTime; }

    /**
     * An interface for classes that want to post modifications to files.
     */
    public interface Updater {

        /**
         * Saves the file.
         */
        public void updateFile(WebFile aFile);
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
        WebURL url = getURL();
        return url.getJavaFile();
    }

    /**
     * Returns a relative URL for the given file path.
     */
    public WebURL getURL(String aPath)
    {
        // If plain file, get from parent directory instead
        if (isFile())
            return getParent().getURL(aPath);

        // If path has protocol, do global eval
        if (aPath.indexOf(':') >= 0)
            return WebURL.getURL(aPath);

        // If root path, eval with site
        if (aPath.startsWith("/"))
            return getSite().getURL(aPath);

        // Otherwise create global URL and eval
        String urlStr = PathUtils.getChild(getURL().getString(), aPath);
        return WebURL.getURL(urlStr);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        WebFile other = anObj instanceof WebFile ? (WebFile) anObj : null;
        if (other == null) return false;
        return other.getURL().equals(getURL());
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return getURL().hashCode();
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
        return "WebFile: " + getUrlString() + (isDir() ? "/" : "");
    }
}