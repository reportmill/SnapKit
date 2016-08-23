/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.net.*;
import java.util.jar.JarFile;
import java.util.zip.*;

/**
 * A WebSite subclass for Jar files.
 */
public class JarFileSite extends ZipFileSite {

    // Whether to trim entries via isInterestingPath
    boolean       _trim;

/**
 * Override to turn on file trimming from system jars. 
 */
protected void setURL(WebURL aURL)
{
    // Do normal version
    super.setURL(aURL);
    
    // Turn on file trimming if system jar
    String urls = aURL.getString();
    _trim = urls.contains("/rt.jar") || urls.contains("/jfxrt.jar");
}

/**
 * Override to do weird (Jar)URLConnection thing if URL not local.
 */
protected ZipFile createZipFile() throws Exception
{
    // If HTTP or .pack.gz, use "jar:" url
    if(getURL().getScheme().equals("http") || getURLString().endsWith(".pack.gz")) try {
        URL url = new URL("jar:" + getURLString() + "!/");
        JarURLConnection conn = (JarURLConnection)url.openConnection();
        return conn.getJarFile();
    }
    catch(Exception e) { System.err.println(e); }
    
    // Otherwise, get local file and create JarFile
    File sfile = getStandardFile(); if(sfile==null) return null; // Get local file
    return new JarFile(sfile); // Create/return ZipFile
}

/**
 * Override to ignore certain Jar paths.
 */
protected void addZipEntry(ZipEntry anEntry)
{
    if(!anEntry.isDirectory() && _trim && !isInterestingPath(anEntry.getName())) return;
    super.addZipEntry(anEntry);
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