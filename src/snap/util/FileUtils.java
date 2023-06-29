/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.net.URL;
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
        if (aSource instanceof File)
            return (File)aSource;

        // for a file URL, pull out the path and fall through to the string case
        if (aSource instanceof URL) {
            URL url = (URL) aSource;
            if (url.getProtocol().equals("file"))
                return new File(url.getPath());
        }

        // If source is string, see if it represents a file
        if (aSource instanceof String) {
            String string = (String) aSource;
            string = string.trim();

            // If starts with a common file separator try file
            if (string.startsWith("/") || string.startsWith("\\") ||
                (string.length()>3 && Character.isLetter(string.charAt(0)) && string.charAt(1)==':'))
                return new File(string);

            // If string starts with file protocol, try that
            else if (StringUtils.startsWithIC(string, "file:"))
                return getFile(string.substring(5));
        }

        // Return not found
        return null;
    }

    /**
     * Returns a directory for given source (path, url, string path) with option to create if missing.
     */
    public static File getDirForSource(Object aSource, boolean doCreate)
    {
        // Get File
        File file = getFile(aSource);
        if (file == null) {
            System.err.println("FileUtils.mkdirs: Couldn't create file for source: " + aSource);
            return null;
        }

        // Create dir
        if (doCreate && !file.exists())
            file.mkdirs();
        return file;
    }

    /**
     * Tries to open the given file name with the platform reader.
     */
    public static void openFile(File aFile)
    {
        snap.gfx.GFXEnv.getEnv().openFile(aFile);
    }

    /**
     * Tries to open the given file name with the platform reader.
     */
    public static void openFile(String aName)
    {
        openFile(new File(aName));
    }

    /**
     * Returns bytes for a file.
     */
    public static byte[] getBytes(File aFile)
    {
        // Return null if file is null, doesn't exist, isn't readable or is directory
        if (aFile == null)
            return null;
        if (!aFile.exists())
            return null;
        if (!aFile.canRead())
            return null;
        if (aFile.isDirectory())
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
        int length = (int) aFile.length();
        byte[] bytes = new byte[length];
        InputStream stream = new FileInputStream(aFile);
        stream.read(bytes, 0, length);
        stream.close();
        return bytes;
    }

    /**
     * Writes the given bytes (within the specified range) to the given file.
     */
    public static void writeBytes(File aFile, byte[] theBytes) throws IOException
    {
        // If null bytes, delete file
        if (theBytes == null) {
            aFile.delete();
            return;
        }

        // Write bytes
        FileOutputStream fileStream = new FileOutputStream(aFile);
        fileStream.write(theBytes);
        fileStream.close();
    }

    /**
     * Writes the given bytes (within the specified range) to the given file, with an option for doing it "safely".
     */
    public static void writeBytesSafely(File aFile, byte[] theBytes) throws IOException
    {
        // If null bytes, delete file
        if (theBytes == null) {
            aFile.delete();
            return;
        }

        // If file doesn't exist, just write bytes
        if (!aFile.exists()) {
            writeBytes(aFile, theBytes);
            return;
        }

        // Otherwise, copy to backup file, write new bytes, delete backup
        File backupfile = new File(aFile.getPath() + ".rmbak");
        copyFile(aFile, backupfile);
        writeBytes(aFile, theBytes);
        backupfile.delete();
    }

    /**
     * Returns the temp directory.
     */
    public static File getTempDir()
    {
        // Hack for TeaVM, WebVM
        if (SnapUtils.isTeaVM) return new File("/");
        if (SnapUtils.isWebVM) return new File("/files/snaptmp/");

        String tmpDirStr = System.getProperty("java.io.tmpdir");
        return new File(tmpDirStr);
    }

    /**
     * Creates a file in the temp directory.
     */
    public static File getTempFile(String aName)
    {
        File tempDir = getTempDir();
        return new File(tempDir, aName);
    }

    /**
     * Returns a file for named directory in the user's home directory (with option to create).
     */
    public static File getUserHomeDir(String aName, boolean doCreate)
    {
        // Get user home - if name provided, add it
        String dir = System.getProperty("user.home");
        if (aName != null)
            dir += File.separator + aName;

        // Create file, actual directory (if requested) and return
        File dfile = new File(dir);
        if (doCreate && aName != null)
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
        if (SnapUtils.isWindows)
            dir += File.separator + "AppData" + File.separator + "Local";
        else if (SnapUtils.isMac)
            dir += File.separator + "Library" + File.separator + "Application Support";
        if (aName!=null)
            dir += File.separator + aName;

        // Create file, actual directory (if requested)
        File dfile = new File(dir);
        if (doCreate && aName != null)
            dfile.mkdirs();

        // Return
        return dfile;
    }

    /**
     * Copies a file from one location to another.
     */
    public static File copyFile(File aSource, File aDest) throws IOException
    {
        // Get input stream, output file and output stream
        FileInputStream fis = new FileInputStream(aSource);
        File out = aDest.isDirectory() ? new File(aDest, aSource.getName()) : aDest;
        FileOutputStream fos = new FileOutputStream(out);

        // Iterate over read/write until all bytes written
        byte[] buf = new byte[8192];
        for (int i=fis.read(buf); i!=-1; i=fis.read(buf))
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
        for (int i = 1; i <= 10; i++) {
            if (aSource.renameTo(aDest))
                return true;
            try { Thread.sleep(i*20); }
            catch(Exception ignore) { }
        }
        throw new IOException("FileUtils move timeout for " + aSource.getAbsolutePath() + " to " + aDest.getAbsolutePath());
    }

    /**
     * Deletes a directory.
     */
    public static boolean deleteDeep(File aFile)
    {
        // Handle directory
        if (aFile.isDirectory()) {
            File[] files = aFile.listFiles();
            if (files != null) {
                for (File file : files)
                    deleteDeep(file);
            }
        }

        // Delete file
        return aFile.delete();
    }

    /**
     * Unzips the given file into the destination file.
     */
    public static void unzipFile(File aFile) throws IOException
    {
        // Get new file input stream for file
        FileInputStream fileInputStream = new FileInputStream(aFile);

        // Get zip input stream
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

        // Create stream buffer
        byte[] buffer = new byte[8192];

        // Iterate over zip entries
        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {

            // Get entry output file
            File outputFile = new File(aFile.getParent(), entry.getName());

            // If directory, just create and skip
            if (entry.isDirectory()) {
                outputFile.mkdir();
                continue;
            }

            // Write the files to the disk
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            BufferedOutputStream dest = new BufferedOutputStream(fileOutputStream, 8192);
            for (int count = zipInputStream.read(buffer, 0, 8192); count != -1; count = zipInputStream.read(buffer, 0, 8192))
                dest.write(buffer, 0, count);

            // Flush and close output stream
            dest.flush();
            dest.close();
        }

        // Close zip
        zipInputStream.close();
    }
}