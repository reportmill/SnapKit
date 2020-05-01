/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

/**
 * Utility methods for Web classes.
 */
public class WebUtils {

    /**
     * Returns the common ancestor of this file and given file.
     */
    public static WebFile getCommonAncestor(WebFile aFile1, WebFile aFile2)
    {
        // Get this file directory and given file directory and return either if they are equal or root
        WebFile dir = aFile1.isDir() ? aFile1 : aFile1.getParent();
        WebFile fileDir = dir.isRoot() ? dir : aFile2.isDir()? aFile2 : aFile2.getParent();
        if (dir==fileDir)
            return dir;

        // Iterate up file's parents and return any equal to this file's directory
        for (WebFile file=fileDir.getParent(); !file.isRoot(); file=file.getParent())
            if (file==dir)
                return dir;

        // If not found, try again with directory parent
        return getCommonAncestor(dir.getParent(), fileDir);
    }

    /**
     * Copies a file to another file.
     */
    public static void copyFile(WebFile aSrcFile, WebFile aDstFile)
    {
        // Get Dest file (if directory, create new child file)
        WebFile dstFile = aDstFile;
        if (dstFile.isDir()) {
            String path = aDstFile.getDirPath() + aSrcFile.getName();
            dstFile = aDstFile.getSite().createFile(path, aSrcFile.isDir());
        }

        // If plain file, just load bytes into dest file and save
        if (aSrcFile.isFile()) {
            dstFile.setBytes(aSrcFile.getBytes());
            dstFile.save();
        }

        // If directory, iterate over children and recurse
        else {
            for (WebFile child : aSrcFile.getFiles())
                copyFile(child, dstFile);
            if (aSrcFile.getFileCount()==0)
                dstFile.save();
        }
    }
}