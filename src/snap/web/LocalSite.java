/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;

/**
 * A FileSite subclass that stores files in a named directory relative to SnapCode home directory.
 */
public class LocalSite extends FileSite {

    // The file path to this data source
    String                 _filePath;
    
    /**
     * Returns the string identifying the prefix for URLs in this data source.
     */
    public String getURLScheme()  { return "local"; }

    /**
     * Returns the Java file for path.
     */
    protected File getJavaFile(String aPath)  { return new File(getPathInFileSystem() + aPath); }

    /**
     * Returns the path of this data source in file system.
     */
    protected String getPathInFileSystem()
    {
        // If not set or if name has changed, reset cached FilePath
        if (_filePath==null) {
            File jdir = snap.util.ClientUtils.getHomeDir(true);
            _filePath = new File(jdir, getPath()).getAbsolutePath();
        }

        // Return file path
        return _filePath;
    }

}