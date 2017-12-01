/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.*;

/**
 * A class to hold basic information for a generic file.
 */
public class FileHeader extends SnapObject {

    // The file path
    String            _path;
    
    // Whether file is a directory
    boolean           _dir;
    
    // The file modified time
    long              _lastModTime;
    
    // The file size
    long              _size;
    
    // The MIME type
    String            _mimeType;
    
    // Constants for properties
    final public static String LastModTime_Prop = "LastModTime";
    final public static String Size_Prop = "Size";
    final public static String MIMEType_Prop = "MIMEType";

/**
 * Creates a new FileInfo for path and directory.
 */
public FileHeader(String aPath, boolean isDir)  { _path = aPath; _dir = isDir; }

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
 * Returns the file modification time.
 */
public long getLastModTime()  { return _lastModTime; }

/**
 * Sets the file modification time.
 */
public void setLastModTime(long aTime)
{
    if(aTime==_lastModTime) return;
    firePropChange(LastModTime_Prop, _lastModTime, _lastModTime = aTime);
}

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
 * Returns the file MIME type.
 */
public String getMIMEType()
{
    if(_mimeType!=null) return _mimeType;
    return _mimeType = MIMEType.getType(getPath());
}

/**
 * Sets the file MIME type.
 */
public void setMIMEtype(String aMIMEType)
{
    if(SnapUtils.equals(aMIMEType, _mimeType)) return;
    firePropChange(MIMEType_Prop, _mimeType, _mimeType = aMIMEType);
}

/**
 * Returns a string representation of file.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getPath() + (isDir()? "/" : ""); }

}