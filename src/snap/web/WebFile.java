/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import snap.util.*;

/**
 * Represents a file from a WebSite.
 */
public class WebFile extends SnapObject implements Comparable<WebFile> {

    // The WebSite that provided this file
    WebSite           _site;
    
    // The file path
    String            _path;
    
    // Whether file is a directory
    boolean           _dir;
    
    // Whether file exists in data source (has been saved and, if so, not deleted)
    Boolean           _exists;

    // The file parent
    WebFile           _parent;
    
    // The file last modified time
    long              _lastModTime;
    
    // The file size
    long              _size;
    
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
    
    // Constants for properties
    final public static String ModifiedTime_Prop = "ModifiedTime";
    final public static String Bytes_Prop = "Bytes";
    final public static String Size_Prop = "Size";
    final public static String File_Prop = "File";
    final public static String Files_Prop = "Files";
    final public static String Exists_Prop = "Exists";
    final public static String Updater_Prop = "Updater";
    
/**
 * Returns the WebSite.
 */
public WebSite getSite()  { return _site; }

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
public String getDirPath()  { String path = getPath(); return path.endsWith("/")? path : path + '/'; }

/**
 * Returns the URL for this file.
 */
public WebURL getURL()
{
    // If already set, just return
    if(_url!=null) return _url;
    
    // Get path, site, URL and return
    String path = getPath();
    WebSite site = getSite();
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
 * Returns whether file exists in data source (has been saved and, if so, not deleted).
 */
public boolean getExists()  { return _exists!=null && _exists; }

/**
 * Sets whether file exists in data source (has been saved and, if so, not deleted).
 */
protected void setExists(boolean aFlag)
{
    if(_exists!=null && aFlag==_exists) return;
    firePropChange(Exists_Prop, _exists, _exists = aFlag);
}

/**
 * Returns whether file was formerly loaded or merely created.
 */
public boolean isLoaded()  { return _exists!=null; }

/**
 * Returns the file, ensuring that it has attempted to load.
 */
public WebFile getLoaded()
{
    if(!isLoaded()) getSite().getFile(getPath());
    return this;
}

/**
 * Returns the file parent directory.
 */
public WebFile getParent()
{
    // If parent not set, get from data source
    if(_parent==null && !isRoot() && getSite()!=null)
        _parent = getSite().createFile(FilePathUtils.getParent(getPath()), true);

    // Return parent
    return _parent;
}

/**
 * Sets the file parent.
 */
protected void setParent(WebFile aFile)  { _parent = aFile; }

/**
 * Returns the file modification time.
 */
public long getLastModTime()  { return _lastModTime; }

/**
 * Sets the file modification time.
 */
public void setLastModTime(long aTime)
{
    if(aTime==_lastModTime) return;
    firePropChange(ModifiedTime_Prop, _lastModTime, _lastModTime = aTime);
}

/**
 * Sets the file modification time in file and in site internal storage.
 */
public void setLastModTimeDeep(long aTime)
{
    try { getSite().setLastModTime(this, aTime); }
    catch(Exception e) { System.err.println("WebFile.setLastModTimeDeep: " + e); }
    setLastModTime(aTime);
}

/**
 * Returns the modified date.
 */
public Date getModifiedDate()  { return new Date(_lastModTime); }

/**
 * Returns the file size.
 */
public long getSize()  { return _size; }

/**
 * Sets the file size.
 */
public void setSize(long aSize)
{
    if(aSize==_size) return;
    firePropChange(Size_Prop, _size, _size = aSize);
}

/**
 * Returns whether bytes have been set/loaded for file.
 */
public boolean isBytesSet()  { return _bytes!=null; }

/**
 * Returns the file bytes.
 */
public synchronized byte[] getBytes()
{
    // If already set, just return
    if(_bytes!=null) return _bytes;
    
    // Set request for bytes for URL
    WebSite site = getSite(); WebURL url = getURL();
    WebResponse resp = site.getResponse(new WebRequest(url)); //getURL().getResponse();
    if(resp.getCode()==WebResponse.OK) _exists = true;
    if(resp.getException()!=null)
        throw new ResponseException(resp);
    return _bytes = resp.getBytes();
}

/**
 * Sets the file bytes.
 */
public void setBytes(byte theBytes[])
{
    if(ArrayUtils.equals(theBytes, _bytes)) return;
    firePropChange(Bytes_Prop, _bytes, _bytes = theBytes);
    setSize(theBytes!=null? theBytes.length : 0); // Update size
}

/**
 * Returns whether files have been set/loaded for directory.
 */
public boolean isFilesSet()  { return _files!=null; }

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
    if(_files!=null) return _files;
    
    // Get response for files
    WebURL url = getURL();
    WebResponse resp = url.getResponse();
    if(resp.getCode()==WebResponse.OK) _exists = true;
    if(resp.getException()!=null)
        throw new ResponseException(resp);
    
    // Get file headers
    WebSite site = getSite();
    List <FileHeader> fhdrs = resp.getFileHeaders(); if(fhdrs==null) return Collections.EMPTY_LIST;
    List <WebFile> files = new ArrayList(fhdrs.size());
    for(FileHeader fhdr : fhdrs) { WebFile file = site.createFile(fhdr);
        file.setParent(this); file._exists = true; files.add(file); }
        
    // Sort files, put in safe array and return
    Collections.sort(files);
    files = new CopyOnWriteArrayList(files);
    return _files = files;
}

/**
 * Sets the directory files list.
 */
protected void setFiles(List theFiles)
{
    if(SnapUtils.equals(theFiles, _files)) return;
    firePropChange(Files_Prop, _files, _files = theFiles);
}

/**
 * Adds a file.
 */
protected void addFile(WebFile aFile)
{
    // Get insert index, add file at index and set File.Parent to this file (just return if already in files)
    int index = -Collections.binarySearch(getFiles(), aFile) - 1; if(index<0) return;
    getFiles().add(index, aFile);
    aFile.setParent(this);
    
    // Fire property change
    firePropChange(File_Prop, null, aFile, index);
}

/**
 * Removes a file at given index.
 */
protected WebFile removeFile(int anIndex)
{
    // Remove file and clear file parent
    WebFile file = _files.remove(anIndex);
    file.setParent(null);
    
    // Fire property change and return file
    firePropChange(File_Prop, file, null, anIndex); return file;
}

/**
 * Removes given file.
 */
protected int removeFile(WebFile aFile)
{
    int index = ListUtils.indexOfId(getFiles(), aFile);
    if(index>=0) removeFile(index);
    return index;
}

/**
 * Saves the file.
 */
public void save() throws ResponseException  { getSite().saveFile(this); }

/**
 * Deletes the file.
 */
public void delete() throws ResponseException  { getSite().deleteFile(this); }

/**
 * Reloads a file from site.
 */
public void reload()  { getSite().reloadFile(this); }

/**
 * Returns the file with the given name.
 */
public WebFile getFile(String aName)
{
    String path = aName.startsWith("/")? aName : getDirPath() + aName;
    return getSite().getFile(path);
}

/**
 * Returns the list of files that match given regex.
 */
public List <WebFile> getFiles(String aRegex)
{
    List files = new ArrayList();
    for(WebFile file : getFiles())
        if(file.getName().matches(aRegex))
            files.add(file);
    return files;
}

/**
 * Returns the file keys.
 */
public List <String> getFileNames()
{
    List <String> names = new ArrayList<String>();
    for(WebFile file : getFiles()) names.add(file.getName());
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
    if(anUpdater==_updater) return;
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
    byte bytes[] = getBytes(), junk = 0; if(bytes==null) return false;
    for(byte b : bytes) if((b & 0xFF) > 127) { junk++; if(junk>10) return false; }
    return true;
}

/**
 * Returns the file bytes as a string.
 */
public String getText()
{
    byte bytes[] = getBytes(); if(bytes==null) return null;
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
 * Returns a standard java.io.File, if available.
 */
public File getStandardFile()  { return getSite().getStandardFile(this); }

/**
 * Returns a relative URL for the given file path.
 */
public WebURL getURL(String aPath)
{
    // If file, get from parent directory instead
    if(isFile())
        return getParent().getURL(aPath);

    // If path has protocol, do global eval, if root path, eval with site, otherwise create global URL and eval
    if(aPath.indexOf(':')>=0) return WebURL.getURL(aPath);
    if(aPath.startsWith("/")) getSite().getURL(aPath);
    String urls = PathUtils.getChild(getURL().getString(), aPath);
    return WebURL.getURL(urls);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    WebFile other = (WebFile)anObj; if(other==null) return false;
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
    int c = aFile.getParent()!=getParent()? getPath().compareToIgnoreCase(aFile.getPath()) :
        getSimpleName().compareToIgnoreCase(aFile.getSimpleName());
    if(c==0) c = getName().compareToIgnoreCase(aFile.getName());
    return c;
}

/**
 * Returns a string representation of file.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getURL().getString() + (isDir()? "/" : ""); }

}