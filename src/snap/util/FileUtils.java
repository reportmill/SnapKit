/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.net.URL;
import java.util.jar.*;
import java.util.zip.*;

/**
 * Utility methods for file.
 */
public class FileUtils {

/**
 * Returns a File object from a source, if one can be divined.
 */
public static File getFile(Object aSource)
{
    // If source is already file, just return it
    if(aSource instanceof File)
        return (File)aSource;
    
    // for a file URL, pull out the path and fall through to the string case
    if(aSource instanceof URL && ((URL)aSource).getProtocol().equals("file")) { URL url = (URL)aSource;
        return new File(url.getPath()); }
    
    // If source is string, see if it represents a file
    if(aSource instanceof String) { String string = (String)aSource; string.trim();
        
        // If starts with a common file separator try file
        if(string.startsWith("/") || string.startsWith("\\") ||
            (string.length()>3 && Character.isLetter(string.charAt(0)) && string.charAt(1)==':'))
            return new File(string);

        // If string starts with file protocol, try that
        else if(StringUtils.startsWithIC(string, "file:"))
            return getFile(string.substring(5));
    }
    
    // Return null if file not found
    return null;
}

/**
 * Returns the path for a file.
 */
public static String getPath(File aFile)  { return aFile.getAbsolutePath(); }

/**
 * Returns the file name for a file.
 */
public static String getFileName(File aFile)  { return FilePathUtils.getFileName(getPath(aFile)); }

/**
 * Returns the file name for a file.
 */
public static String getFileNameSimple(File aFile)  { return FilePathUtils.getFileNameSimple(getPath(aFile)); }

/**
 * Returns the file name for a file.
 */
public static String getFileExtension(File aFile)  { return FilePathUtils.getExtension(getPath(aFile)); }

/**
 * Returns whether file is given type.
 */
public static boolean isFileType(File aFile, String ... theTypes)
{
    for(String type : theTypes)
        if(StringUtils.endsWithIC(getPath(aFile), type))
            return true;
    return false;
}

/**
 * Returns a child directory for a parent directory, creating if necessary.
 */
public static File getDirectory(File aParent, String aChild, boolean create)
{
    File file = new File(aParent, aChild);
    if(create && !file.exists())
        file.mkdirs();
    return file;
}

/**
 * Tries to open the given file name with the platform reader.
 */
public static void openFile(File aFile)  { snap.gfx.GFXEnv.getEnv().openFile(aFile); }

/**
 * Tries to open the given file name with the platform reader.
 */
public static void openFile(String aName)  { openFile(new File(aName)); }

/**
 * Returns bytes for a file.
 */
public static byte[] getBytes(File aFile)
{
    // Return null if file is null, doesn't exist, isn't readable or is directory
    if(aFile==null)
        return null;
    if(!aFile.exists())
        return null;
    if(!aFile.canRead())
        return null;
    if(aFile.isDirectory())
        return null;
        
    // Get file length, byte buffer, file stream, read bytes into buffer and close stream
    try { return getBytesOrThrow(aFile); }
    catch(IOException e) { throw new RuntimeException(e); }
}

/**
 * Returns bytes for a file.
 */
public static byte[] getBytesOrThrow(File aFile) throws IOException
{
    int length = (int)aFile.length();
    byte bytes[] = new byte[length];
    InputStream stream = new FileInputStream(aFile);
    stream.read(bytes, 0, length);
    stream.close();
    return bytes;
}

/**
 * Writes the given bytes (within the specified range) to the given file.
 */
public static void writeBytes(File aFile, byte theBytes[]) throws IOException
{
    if(theBytes==null) { aFile.delete(); return; }
    FileOutputStream fileStream = new FileOutputStream(aFile);
    fileStream.write(theBytes);
    fileStream.close();
}

/**
 * Writes the given bytes (within the specified range) to the given file, with an option for doing it "safely".
 */
public static void writeBytesSafely(File aFile, byte theBytes[]) throws IOException
{
    if(theBytes==null) { aFile.delete(); return; }
    if(!aFile.exists()) { writeBytes(aFile, theBytes); return; }
    File bfile = new File(aFile.getPath() + ".rmbak");
    copyFile(aFile, bfile);
    writeBytes(aFile, theBytes);
    bfile.delete();
}

/**
 * Returns the temp directory.
 */
public static File getTempDir()  { return new File(System.getProperty("java.io.tmpdir")); }

/**
 * Creates a file in the temp directory.
 */
public static File getTempFile(String aName)  { return new File(getTempDir(), aName); }

/**
 * Returns a file for named directory in the user's home directory (with option to create).
 */
public static File getUserHomeDir(String aName, boolean doCreate)
{
    // Get user home - if name provided, add it
    String dir = System.getProperty("user.home");
    if(aName!=null)
        dir += File.separator + aName;

    // Create file, actual directory (if requested) and return
    File dfile = new File(dir);
    if(doCreate && aName!=null)
        dfile.mkdirs();
    return dfile;
}

/**
 * Returns the AppData or Application Support directory file.
 */
public static File getAppDataDir(String aName, boolean doCreate)
{
    // Get user home + AppDataDir (platform specific) + name (if provided)
    String dir = System.getProperty("user.home");
    if(SnapUtils.isWindows)
        dir += File.separator + "AppData" + File.separator + "Local";
    else if(SnapUtils.isMac)
        dir += File.separator + "Library" + File.separator + "Application Support";
    if(aName!=null)
        dir += File.separator + aName;

    // Create file, actual directory (if requested) and return
    File dfile = new File(dir);
    if(doCreate && aName!=null)
        dfile.mkdirs();
    return dfile;
}

/**
 * Copies a file from one location to another.
 */
public static File copyFile(File aSource, File aDest) throws IOException
{
    // Get input stream, output file and output stream
    FileInputStream fis = new FileInputStream(aSource);
    File out = aDest.isDirectory()? new File(aDest, aSource.getName()) : aDest;
    FileOutputStream fos = new FileOutputStream(out);
    
    // Iterate over read/write until all bytes written
    byte[] buf = new byte[8192];
    for(int i=fis.read(buf); i!=-1; i=fis.read(buf))
        fos.write(buf, 0, i);
    
    // Close in/out streams and return out file
    fis.close();
    fos.close();
    return out;
}

/**
 * Copies a file from one location to another with exception suppression.
 */
public static File copyFileSafe(File in, File out)
{
    try { return copyFile(in, out); }
    catch(IOException e) { System.err.println("Couldn't copy " + in + " to " + out + " (" + e + ")"); return null; }
}

/**
 * Moves a file.
 */
public static boolean move(File aSource, File aDest) throws IOException
{
    for(int i=1; i<=10; i++) {
        if(aSource.renameTo(aDest)) return true;
        try { Thread.sleep(i*20); } catch(Exception e) { }
    }
    throw new IOException("FileUtils move timeout for " + aSource.getAbsolutePath() + " to " + aDest.getAbsolutePath());
}

/**
 * Deletes a directory.
 */
public static boolean deleteDeep(File aFile)
{
    // Handle directory
    if(aFile.isDirectory())
        for(File file : aFile.listFiles())
            deleteDeep(file);
    
    // Delete file
    return aFile.delete();
}

/**
 * Unzips the given file into the destination file.
 */
public static void unzip(File aFile)
{
    // Catch exceptions
    try {
        
    // Get new file input stream for file
    FileInputStream fis = new FileInputStream(aFile);
    
    // Get zip input stream
    ZipInputStream zis = new ZipInputStream(fis);
    
    // Create stream buffer
    byte buffer[] = new byte[8192];    
    
    // Iterate over zip entries
    for(ZipEntry entry=zis.getNextEntry(); entry!=null; entry=zis.getNextEntry()) {
        
        // Get entry output file
        File outputFile = new File(aFile.getParent(), entry.getName());
        
        // If directory, just create and skip
        if(entry.isDirectory()) {
            outputFile.mkdir();
            continue;
        }

        // Write the files to the disk
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
        for(int count=zis.read(buffer, 0, 8192); count!=-1; count=zis.read(buffer, 0, 8192))
            dest.write(buffer, 0, count);

        // Flush and close output stream
        dest.flush();
        dest.close();
    }
    
    // Close zip
    zis.close();
    
    // Catch exceptions
    } catch(IOException e) { e.printStackTrace(); }
}

/**
 * Returns whether given file is packed with pack200.
 */
public static boolean isPacked(File aFile)  { return aFile.getName().endsWith(".pack.gz"); }

/**
 * Unpacks the given file into the destination file.
 */
public static File unpack(File aFile, File aDestFile)
{
    // Get dest file
    File destFile = getUnpackDestination(aFile, aDestFile);
    
    // If already unpacked, return
    if(destFile.exists() && destFile.lastModified()>aFile.lastModified())
        return destFile;
    
    // Handle exceptions
    try {
        
    // Create new Gzip input stream
    FileInputStream fileInput = new FileInputStream(aFile);
    GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
        
    // Create new FileOutput stream JarOutputStream
    FileOutputStream fileOut = new FileOutputStream(destFile);
    JarOutputStream jarOut = new JarOutputStream(fileOut);
    
    // Unpack file
    Pack200.newUnpacker().unpack(gzipInput, jarOut);
    
    // Close streams
    fileInput.close();
    gzipInput.close();
    jarOut.close();
    fileOut.close();
    
    // Catch exceptions
    } catch(IOException e) {
        
        // This is lame, but Windows seems to return .pack.gz files unpacked, so just copy
        if(copyFileSafe(aFile, destFile)!=null)
            return destFile;
        
        // Otherwise, do normal
        e.printStackTrace();
        return null;
    }
    
    // Return destination file
    return destFile;
}

/**
 * Returns the file that given packed file would be saved to using the unpack method.
 */
public static File getUnpackDestination(File aFile, File aDestFile)
{
    // Get dest file
    File destFile = aDestFile;

    // If dest file is null, create from packed file minus .pack.gz
    if(destFile==null)
        destFile = new File(StringUtils.delete(aFile.getPath(), ".pack.gz"));

    // If dest file is directory, change to file inside with packed file minus .pack.gz
    else if(destFile.isDirectory())
        destFile = new File(destFile, StringUtils.delete(aFile.getName(), ".pack.gz"));

    // Return destination file
    return destFile;
}

/**
 * Returns a unique non-existent file for given file.
 */
public static File getUniqueFile(File aFile)
{
    File file = aFile;
    while(file.exists()) {
        String fileName = file.getName();
        int value = Math.abs(SnapUtils.intValue(FilePathUtils.getFileNameSimple(file.getPath()))) +1;
        fileName = fileName.replaceFirst("-?\\d*\\.", "-" + value + ".");
        file = new File(file.getParentFile(), fileName);
    }
    return file;
}

}