/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import snap.util.*;

/**
 * Represents a file from a WebSite.
 */
public class WebFile implements Comparable <WebFile> {

    // The WebSite that provided this file
    WebSite           _site;
    
    // The file parent
    WebFile           _parent;
    
    // The file path
    String            _path;
    
    // Whether file is a directory
    boolean           _dir;
    
    // The file last modified time
    long              _modTime;
    
    // The file size
    long              _size;
    
    // Whether this file has been checked to see if it is saved at site
    boolean           _verified;
    
    // Whether this file has been saved at site
    boolean           _saved;
    
    // The file bytes
    byte              _bytes[];
    
    // The directory files
    List <WebFile>    _files;
    
    // The MIME type
    String            _mimeType;
    
    // A map of properties associated with file
    Map               _props = new HashMap();
    
    // The URL for this file
    WebURL            _url;
    
    // The PropChangeSupport
    PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    final public static String Bytes_Prop = "Bytes";
    final public static String File_Prop = "File";
    final public static String Files_Prop = "Files";
    final public static String ModTime_Prop = "ModTime";
    final public static String Saved_Prop = "Saved";
    final public static String Size_Prop = "Size";
    final public static String Updater_Prop = "Updater";
    final public static String Verified_Prop = "Verified";
    final public static String Loaded_Prop = "Loaded";
    
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
        if (_parent!=null || isRoot()) return _parent;

        // Get file for parent path from site
        String path = FilePathUtils.getParent(getPath());
        return _parent = getSite().createFile(path, true);
    }

    /**
     * Sets the file parent.
     */
    protected void setParent(WebFile aFile)  { _parent = aFile; }

    /**
     * Returns the file path.
     */
    public String getPath()  { return _path; }

    /**
     * Returns the resource name.
     */
    public String getName()  { return FilePathUtils.getFileName(getPath()); }

    /**
     * Returns the file simple name.
     */
    public String getSimpleName()  { return FilePathUtils.getFileNameSimple(getPath()); }

    /**
     * Returns the file type (extension without the '.').
     */
    public String getType()  { return FilePathUtils.getExtension(getPath()).toLowerCase(); }

    /**
     * Returns the path as a directory (with trailing separator).
     */
    public String getDirPath()
    {
        String path = getPath();
        return path.endsWith("/") ? path : path + '/';
    }

    /**
     * Returns the URL for this file.
     */
    public WebURL getURL()
    {
        // If already set, just return
        if (_url!=null) return _url;

        // Get path, site, URL and return
        String path = getPath(); WebSite site = getSite();
        return _url = site.getURL(path);
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
    public boolean isRoot()  { return getPath().equals("/"); }

    /**
     * Returns whether file status has been check at the site.
     */
    public boolean isVerified()  { return _verified; }

    /**
     * Sets whether file status has been check at the site.
     */
    protected void setVerified(boolean aValue)
    {
        // If already set, just return
        if (aValue==_verified) return;

        // If false, clear ModTime, Size, Saved
        if (!aValue) {
            _modTime = 0; _size = 0; _saved = false;
        }

        // Set new value, fire prop change
        firePropChange(Verified_Prop, _verified, _verified = aValue);
    }

    /**
     * Returns the file, ensuring that it's status has been checked with the site.
     */
    public WebFile getVerified()
    {
        if (!isVerified())
            getSite().getFile(getPath());
        return this;
    }

    /**
     * Returns whether this file has been saved at site..
     */
    public boolean isSaved()  { return _saved; }

    /**
     * Sets whether this file has been saved at site..
     */
    protected void setSaved(boolean aValue)
    {
        if (aValue==_saved) return;
        firePropChange(Saved_Prop, _saved, _saved = aValue);
    }

    /**
     * Conventional file method that simply wraps isSaved().
     */
    public boolean getExists()  { return isSaved(); }

    /**
     * Returns the file modification time.
     */
    public long getModTime()  { return _modTime; }

    /**
     * Sets the file modification time.
     */
    protected void setModTime(long aTime)
    {
        if (aTime==_modTime) return;
        firePropChange(ModTime_Prop, _modTime, _modTime = aTime);
    }

    /**
     * Sets the file modification time in file and in site internal storage.
     */
    public void setModTimeSaved(long aTime)
    {
        try { getSite().setModTimeSaved(this, aTime); }
        catch(Exception e) { System.err.println("WebFile.setModTimeSaved: " + e); }
        setModTime(aTime);
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
     * Returns the file size.
     */
    public long getSize()  { return _size; }

    /**
     * Sets the file size.
     */
    protected void setSize(long aSize)
    {
        if (aSize==_size) return;
        firePropChange(Size_Prop, _size, _size = aSize);
    }

    /**
     * Returns whether bytes/files have been set for this file/dir.
     */
    public boolean isLoaded()  { return _dir? (_files!=null) : (_bytes!=null); }

    /**
     * Sets whether bytes/files have been set for this file/dir.
     */
    protected void setLoaded(boolean aValue)
    {
        // If already set, just return
        if (aValue==isLoaded()) return;

        // If clearing, clear info
        if (!aValue) {
            _files = null; _bytes = null; _updater = null;
        }

        // Set value and fire prop change
        firePropChange(Loaded_Prop, !aValue, aValue);
    }

    /**
     * Returns the file bytes.
     */
    public synchronized byte[] getBytes()
    {
        // If already set, just return
        if (_bytes!=null) return _bytes;

        // Set request for bytes for URL
        WebURL url = getURL();
        WebResponse resp = url.getResponse();

        // Handle response
        if (resp.getCode()==WebResponse.OK)
            _saved = true;
        if (resp.getException()!=null)
            throw new ResponseException(resp);

        // Update file attributes
        _bytes = resp.getBytes();
        _modTime = resp.getModTime();
        _size = _bytes!=null? _bytes.length : 0;
        return _bytes;
    }

    /**
     * Sets the file bytes.
     */
    public void setBytes(byte theBytes[])
    {
        // If already set, just return
        if (ArrayUtils.equals(theBytes, _bytes)) return;

        // Set bytes and fire PropChange
        firePropChange(Bytes_Prop, _bytes, _bytes = theBytes);

        // Update size
        setSize(theBytes!=null ? theBytes.length : 0);
    }

    /**
     * Returns the number of files in this directory.
     */
    public int getFileCount()  { return getFiles()!=null? getFiles().size() : 0; }

    /**
     * Returns the individual file at given index.
     */
    public WebFile getFile(int anIndex)  { return getFiles().get(anIndex); }

    /**
     * Returns the directory files list.
     */
    public synchronized List <WebFile> getFiles()
    {
        // If already set, just return
        if (_files!=null) return _files;

        // Get response for files
        WebURL url = getURL();
        WebResponse resp = url.getResponse();

        // Handle Response.Code
        if (resp.getCode()==WebResponse.OK)
            _saved = true;
        if (resp.getException()!=null)
            throw new ResponseException(resp);

        // Get file headers
        WebSite site = getSite();
        List <FileHeader> fhdrs = resp.getFileHeaders();
        if (fhdrs==null) return Collections.EMPTY_LIST;

        // Get files
        List <WebFile> files = new ArrayList(fhdrs.size());
        for (FileHeader fhdr : fhdrs) {
            WebFile file = site.createFile(fhdr);
            file.setParent(this);
            file._saved = true;
            files.add(file);
        }

        // Sort files, set and return
        Collections.sort(files);
        _files = files;
        _modTime = resp.getModTime();
        return _files;
    }

    /**
     * Sets the directory files list.
     */
    protected void setFiles(List theFiles)
    {
        if (SnapUtils.equals(theFiles, _files)) return;
        firePropChange(Files_Prop, _files, _files = theFiles);
    }

    /**
     * Saves the file.
     */
    public WebResponse save() { return getSite().saveFile(this); }

    /**
     * Deletes the file.
     */
    public WebResponse delete() { return getSite().deleteFile(this); }

    /**
     * Resets the file to unverified state where nothing is known about size, mod-time, saved.
     */
    public void reset()
    {
        resetContent();
        setVerified(false);
    }

    /**
     * Resets the file to unloaded state so files/bytes are cleared (to reload when next called).
     */
    public void resetContent()  { setLoaded(false); }

    /**
     * Resets a file with check to have parent resetContent() if file is deleted.
     */
    public void reload()
    {
        // Reset content (just return if not verified)
        resetContent();
        if (!isVerified())
            return;

        // Reset and verify
        long modTime = getModTime();
        reset();
        getVerified();

        // If file was deleted, reset parent content and trigger Saved change
        if (!isSaved()) {
            getParent().resetContent();
            _saved = true; setSaved(false);
        }

        // If ModTime changed, trigger ModTime prop change
        else if (modTime!=0 && isFile() && modTime!=getModTime()) {
            modTime = getModTime(); _modTime = 0;
            setModTime(modTime);
        }
    }

    /**
     * Returns the file with the given name.
     */
    public WebFile getFile(String aName)
    {
        String path = aName.startsWith("/") ? aName : getDirPath() + aName;
        return getSite().getFile(path);
    }

    /**
     * Returns the list of files that match given regex.
     */
    public List <WebFile> getFiles(String aRegex)
    {
        List files = new ArrayList();
        for (WebFile file : getFiles())
            if (file.getName().matches(aRegex))
                files.add(file);
        return files;
    }

    /**
     * Returns the file keys.
     */
    public List <String> getFileNames()
    {
        List <String> names = new ArrayList<String>();
        for (WebFile file : getFiles()) names.add(file.getName());
        return names;
    }

    /**
     * Returns whether given file is contained in this directory.
     */
    public boolean contains(WebFile aFile)
    {
        return isDir() && getSite()==aFile.getSite() && aFile.getPath().startsWith(getDirPath());
    }

    /**
     * Returns the MIME type of the file.
     */
    public String getMiType()  { return _mimeType; }

    /**
     * Sets the MIME type for the file.
     */
    protected void setMIMEType(String aMIMEType)  { _mimeType = aMIMEType; }

    /**
     * Returns a file property for key.
     */
    public Object getProp(String aKey)  { return _props.get(aKey); }

    /**
     * Sets a property for a key.
     */
    public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

    /**
     * Returns whether update is set and has update.
     */
    public boolean isUpdateSet()  { return getUpdater()!=null; }

    /**
     * Returns the updater.
     */
    public Updater getUpdater()  { return _updater; } Updater _updater;

    /**
     * Sets the Updater.
     */
    public void setUpdater(Updater anUpdater)
    {
        if (anUpdater==_updater) return;
        firePropChange(Updater_Prop, _updater, _updater = anUpdater);
    }

    /**
     * An interface for classes that want to post modifications to files.
     */
    public interface Updater {

        /** Saves the file. */
        public void updateFile(WebFile aFile);
    }

    /**
     * Returns whether response is text (regardless of what the data type is).
     */
    public boolean isText()
    {
        byte bytes[] = getBytes(), junk = 0; if (bytes==null) return false;
        for (byte b : bytes) if((b & 0xFF) > 127) {
            junk++; if(junk>10) return false; }
        return true;
    }

    /**
     * Returns the file bytes as a string.
     */
    public String getText()
    {
        byte bytes[] = getBytes(); if (bytes==null) return null;
        return new String(bytes);
    }

    /**
     * Sets the file bytes as a string.
     */
    public void setText(String aString)  { setBytes(StringUtils.getBytes(aString)); }

    /**
     * Returns an input stream for file.
     */
    public InputStream getInputStream()  { return new ByteArrayInputStream(getBytes()); }

    /**
     * Returns a standard Java File (if available).
     */
    public File getJavaFile()  { return getURL().getJavaFile(); }

    /**
     * Returns a relative URL for the given file path.
     */
    public WebURL getURL(String aPath)
    {
        // If file, get from parent directory instead
        if (isFile())
            return getParent().getURL(aPath);

        // If path has protocol, do global eval, if root path, eval with site, otherwise create global URL and eval
        if (aPath.indexOf(':')>=0) return WebURL.getURL(aPath);
        if (aPath.startsWith("/")) getSite().getURL(aPath);
        String urls = PathUtils.getChild(getURL().getString(), aPath);
        return WebURL.getURL(urls);
    }

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
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        WebFile other = (WebFile)anObj; if (other==null) return false;
        return other.getURL().equals(getURL());
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()  { return getURL().hashCode(); }

    /**
     * Standard compareTo implementation.
     */
    public int compareTo(WebFile aFile)
    {
        int c = aFile.getParent()!=getParent() ? getPath().compareToIgnoreCase(aFile.getPath()) :
            getSimpleName().compareToIgnoreCase(aFile.getSimpleName());
        if (c==0) c = getName().compareToIgnoreCase(aFile.getName());
        return c;
    }

    /**
     * Returns a string representation of file.
     */
    public String toString()  { return getClass().getSimpleName() + ": " + getURL().getString() + (isDir()? "/" : ""); }

}