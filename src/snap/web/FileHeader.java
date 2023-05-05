/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropObject;
import snap.util.*;

import java.util.Objects;

/**
 * A class to hold basic information for a generic file.
 */
public class FileHeader extends PropObject {

    // The file path
    private String  _path;
    
    // Whether file is a directory
    private boolean  _dir;
    
    // The file modified time
    private long  _modTime;
    
    // The file size
    private long  _size;
    
    // The MIME type
    private String  _mimeType;
    
    // Constants for properties
    final public static String LastModTime_Prop = "LastModTime";
    final public static String Size_Prop = "Size";
    final public static String MIMEType_Prop = "MIMEType";

    /**
     * Constructor for path and directory.
     */
    public FileHeader(String aPath, boolean isDir)
    {
        _path = aPath;
        _dir = isDir;
    }

    /**
     * Constructor for file.
     */
    public FileHeader(WebFile aFile)
    {
        _path = aFile.getPath();
        _dir = aFile.isDir();
        _modTime = aFile.getModTime();
        _size = aFile.getSize();
    }

    /**
     * Returns the file path.
     */
    public String getPath()  { return _path; }

    /**
     * Sets the file path.
     */
    public void setPath(String aPath)  { _path = aPath; }

    /**
     * Returns the resource name.
     */
    public String getName()  { return FilePathUtils.getFilename(getPath()); }

    /**
     * Returns the file simple name.
     */
    public String getSimpleName()  { return FilePathUtils.getFilenameSimple(getPath()); }

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
    public long getModTime()  { return _modTime; }

    /**
     * Sets the file modification time.
     */
    public void setModTime(long aTime)
    {
        if (aTime == _modTime) return;
        firePropChange(LastModTime_Prop, _modTime, _modTime = aTime);
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
        if (aSize == _size) return;
        firePropChange(Size_Prop, _size, _size = aSize);
    }

    /**
     * Returns the file MIME type.
     */
    public String getMimeType()
    {
        if (_mimeType != null) return _mimeType;
        return _mimeType = MIMEType.getType(getPath());
    }

    /**
     * Sets the file MIME type.
     */
    public void setMimeType(String aMIMEType)
    {
        if (Objects.equals(aMIMEType, _mimeType)) return;
        firePropChange(MIMEType_Prop, _mimeType, _mimeType = aMIMEType);
    }

    /**
     * Returns a string representation of file.
     */
    @Override
    public String toStringProps()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Path=").append(isDir() ? getDirPath() : getPath());
        return sb.toString();
    }
}