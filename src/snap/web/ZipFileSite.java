/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.*;
import snap.util.FilePathUtils;
import snap.util.SnapUtils;

/**
 * A WebSite subclass for Zip and Jar files.
 */
public class ZipFileSite extends WebSite {

    // The ZipFile
    ZipFile                    _zipFile;
    
    // A map of paths to ZipEntry
    Map <String,ZipEntry>      _entries;
    
    // A map of directory paths to List of child paths
    Map <String,List<String>>  _dirs;
    
    // Whether Zip is really a Jar
    boolean                    _jar;

    // Whether to trim entries via isInterestingPath (silly Jar feature)
    boolean                    _trim;

    /**
     * Returns the ZipFile.
     */
    protected ZipFile getZipFile()
    {
        // If already set, just return
        if(_zipFile!=null) return _zipFile;

        // If ZipFile
        if(!_jar) {
            File sfile = getJavaFile(); if(sfile==null) return null; // Get local file
            try { return _zipFile = new ZipFile(sfile); }
            catch(IOException e) { throw new RuntimeException(e); }
        }

        // If HTTP or .pack.gz, use "jar:" url
        if(getURL().getScheme().equals("http") || getURLString().endsWith(".pack.gz")) try {
            URL url = new URL("jar:" + getURLString() + "!/");
            JarURLConnection conn = (JarURLConnection)url.openConnection();
            return _zipFile = conn.getJarFile();
        }
        catch(IOException e) { System.err.println(e); }

        // Otherwise, get local file and create JarFile
        File sfile = getJavaFile(); if(sfile==null) return null; // Get local file
        try { return _zipFile = new JarFile(sfile); }
        catch(IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a map of ZipFile paths to ZipEntry(s).
     */
    protected synchronized Map <String,ZipEntry> getEntries()
    {
        // If already set, just return
        if(_entries!=null) return _entries;

        // Create maps
        _entries = new HashMap(); _dirs = new HashMap();

        // Get ZipFile
        ZipFile zipFile = getZipFile(); if(zipFile==null) return _entries;

        // Get ZipEntries and add
        Enumeration <? extends ZipEntry> zentries = zipFile.entries();
        while(zentries.hasMoreElements())
            addZipEntry(zentries.nextElement());

        // Close and return
        return _entries;
    }

    /**
     * Adds a ZipEntry to WebSite.
     */
    protected void addZipEntry(ZipEntry anEntry)
    {
        // If performing trim, check entry name
        if(_trim && !anEntry.isDirectory() && !isInterestingPath(anEntry.getName())) return;

        // Get path and add entry to entries and path to dirs lists
        String path = FilePathUtils.getStandardized('/' + anEntry.getName());
        _entries.put(path, anEntry);
        addDirListPath(path);
    }

    /**
     * Returns a dir list for a path.
     */
    protected List <String> getDirList(String aPath)
    {
        // Get parent path and return list for path
        String ppath =  FilePathUtils.getParent(aPath);
        List <String> dlist = _dirs.get(ppath); if(dlist!=null) return dlist;

        // If list not found, create, set and return
        _dirs.put(ppath, dlist=new ArrayList());
        addDirListPath(ppath);
        return dlist;
    }

    /**
     * Returns a dir list for a path.
     */
    protected void addDirListPath(String aPath)
    {
        if(aPath.length()<=1) return;
        String path = FilePathUtils.getStandardized(aPath);

        List <String> dlist = getDirList(path);
        if(!dlist.contains(path))
            dlist.add(path);
    }

    /**
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL and path
        WebURL url = aReq.getURL();
        String path = url.getPath(); if(path==null) path = "/";

        // Get FileHeader for path
        FileHeader fhdr = getFileHeaderForPath(path);

        // If not found, set Response.Code to NOT_FOUND and return
        if(fhdr==null) {
            aResp.setCode(WebResponse.NOT_FOUND); return; }

        // Otherwise configure response
        aResp.setCode(WebResponse.OK);
        aResp.setFileHeader(fhdr);

        // If Head, just return
        if(isHead)
            return;

        // If file, get/set file bytes
        if(aResp.isFile()) {
            try {
                ZipEntry zentry = getEntries().get(path);
                InputStream istream = _zipFile.getInputStream(zentry);
                byte bytes[] = SnapUtils.getBytesOrThrow(istream);
                aResp.setBytes(bytes);
            }
            catch(IOException e) { aResp.setException(e); }
        }

        // If directory, get/set dir FileHeaders
        else {
            List <String> dpaths = _dirs.get(path); if(dpaths==null) dpaths = Collections.EMPTY_LIST;
            List <FileHeader> fhdrs = dpaths.size()>0? new ArrayList() : Collections.EMPTY_LIST;
            for(String dpath : dpaths) {
                FileHeader fh = getFileHeaderForPath(dpath); if(fh==null) continue;
                fhdrs.add(fh);
            }
            aResp.setFileHeaders(fhdrs);
        }
    }

    /**
     * Returns a data source file for given path (if file exists).
     */
    private FileHeader getFileHeaderForPath(String aPath)
    {
        // Get ZipEntry for path - if not found and not directory, just return
        ZipEntry zentry = getEntries().get(aPath);
        if(zentry==null && _dirs.get(aPath)==null)
            return null;

        // Create FileHeader and return
        FileHeader file = new FileHeader(aPath, zentry==null || zentry.isDirectory());
        if(zentry!=null) file.setModTime(zentry.getTime());
        if(zentry!=null) file.setSize(zentry.getSize());
        return file;
    }

    /**
     * Returns a Java file for the zip file URL (copied to Sandbox if remote).
     */
    protected File getJavaFile()
    {
        WebURL url = getURL();
        WebFile file = url.getFile(); if(file==null) return null;
        WebFile localFile = file.getSite().getLocalFile(file, true); // Get local file in case file is over http
        return localFile.getJavaFile();
    }

    /**
     * Override to turn on file trimming from system jars.
     */
    public void setURL(WebURL aURL)
    {
        // Do normal version
        super.setURL(aURL);

        // Turn on file trimming if system jar
        String urls = aURL.getString().toLowerCase();
        _jar = urls.endsWith(".jar") || urls.endsWith(".jar.pack.gz");
        _trim = _jar && (urls.contains("/rt.jar") || urls.contains("/jfxrt.jar"));
    }

    /**
     * Adds an entry (override to ignore).
     */
    protected boolean isInterestingPath(String aPath)
    {
        // Bogus excludes
        if(aPath.startsWith("sun")) return false;
        if(aPath.startsWith("com/sun")) return false;
        if(aPath.startsWith("com/apple")) return false;
        if(aPath.startsWith("javax/swing/plaf")) return false;
        if(aPath.startsWith("org/omg")) return false;
        int dollar = aPath.endsWith(".class")? aPath.lastIndexOf('$') : -1;
        if(dollar>0 && Character.isDigit(aPath.charAt(dollar+1))) return false;
        return true;
    }

}