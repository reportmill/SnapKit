/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.FileUtils;
import java.io.File;

/**
 * A FileSite subclass that stores files in a named directory relative to SnapCode home directory.
 */
public class LocalSite extends FileSite {

    // The file path to this data source
    private String _filePath;

    /**
     * Constructor.
     */
    public LocalSite()
    {
        super();
    }

    /**
     * Returns the string identifying the prefix for URLs in this data source.
     */
    public String getURLScheme()  { return "local"; }

    /**
     * Override to prepend site path.
     */
    @Override
    protected String getJavaFilePathForPath(String filePath)
    {
        return getPathInFileSystem() + filePath;
    }

    /**
     * Returns the path of this data source in file system.
     */
    private String getPathInFileSystem()
    {
        if (_filePath != null) return _filePath;

        // If not set or if name has changed, reset cached FilePath
        File localSiteDir = FileUtils.getUserHomeDir("SnapCode", true);
        String filePath = new File(localSiteDir, getPath()).getAbsolutePath();

        // Return file path
        return _filePath = filePath;
    }
}