/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.List;

/**
 * This class is a WebSite implementation for a FileSystems.
 */
public class FileSystemSite extends WebSite {

    // The FileSystem
    private FileSystem _fileSystem;

    /**
     * Constructor.
     */
    public FileSystemSite()
    {
        super();
    }

    /**
     * Override to set drive letter path on Windows.
     */
    @Override
    public void setURL(WebURL aURL)
    {
        super.setURL(aURL);

        // If Windows, get drive letter path
        try { _fileSystem = FileSystems.getFileSystem(aURL.getJavaUrl().toURI()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Handle a head request.
     */
    @Override
    protected void doHead(WebRequest aReq, WebResponse aResp)
    {
        // Get file header
        String filePath = aReq.getFilePath();
        Path javaPath = getJavaPathForLocalPath(filePath);
        FileHeader fileHeader = getFileHeaderForJavaPath(javaPath);
        if (fileHeader == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // If case doesn't match, return not found - case-insensitive file systems could be supported, but it could get tricky
        //String filePathReal = getLocalPathForJavaFile(javaFile);
        //if (!filePath.equals(filePathReal)) { aResp.setCode(WebResponse.NOT_FOUND); return; }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        aResp.setFileHeader(fileHeader);
    }

    /**
     * Handle a get request.
     */
    @Override
    protected void doGet(WebRequest aReq, WebResponse aResp)
    {
        String filePath = aReq.getFilePath();
        Path javaPath = getJavaPathForLocalPath(filePath);
        boolean isFile = aReq.getFile().isFile();

        // If file, just set bytes
        if (isFile) {
            try {
                byte[] bytes = Files.readAllBytes(javaPath);
                aResp.setBytes(bytes);
            }
            catch(IOException e) { aResp.setException(e); }
        }

        // If directory, configure directory info and return
        else {
            List<FileHeader> fileHeaders = getFileHeadersForJavaPath(javaPath);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns the file header for given path and java file.
     */
    protected FileHeader getFileHeaderForJavaPath(Path javaPath)
    {
        // Get file attributes
        BasicFileAttributes fileAttrs = getFileAttributesForJavaPath(javaPath);
        if (fileAttrs == null)
            return null;

        // Get file path
        String filePath = javaPath.toAbsolutePath().toString();
        if (!filePath.startsWith("/"))
            filePath = "/" + filePath;

        // Create and initialize FileHeader and return
        boolean isDir = fileAttrs.isDirectory();
        FileHeader fileHeader = new FileHeader(filePath, isDir);
        if (!isDir) {
            fileHeader.setLastModTime(fileAttrs.lastModifiedTime().toMillis());
            fileHeader.setSize(fileAttrs.size());
        }

        // Return
        return fileHeader;
    }

    /**
     * Returns the file attributes for given path.
     */
    private BasicFileAttributes getFileAttributesForJavaPath(Path javaPath)
    {
        if (javaPath.toString().startsWith("/"))
            javaPath = _fileSystem.getPath(javaPath.toString().substring(1));

        // Try the plain file
        try { return Files.readAttributes(javaPath, BasicFileAttributes.class); }
        catch (NoSuchFileException ignored) { }
        catch (Exception e) { e.printStackTrace(); }

        // Try as directory
        Path dirPath = _fileSystem.getPath(javaPath.toString() + '/');
        try { return Files.readAttributes(dirPath, BasicFileAttributes.class); }
        catch (NoSuchFileException ignored) { }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Returns the child file headers at given path.
     */
    protected List<FileHeader> getFileHeadersForJavaPath(Path parentFile)
    {
        // Get java file children (if null, just return)
        try (var pathsStream = Files.list(parentFile)) {
            List<Path> dirFiles = pathsStream.toList();
            return ListUtils.mapNonNull(dirFiles, path -> getFileHeaderForJavaPath(path));
        }
        catch (IOException e) {
            System.err.println("FileSite.getFileHeaders: error from list files for file: " + parentFile);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Handle a PUT request.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get java file
        String filePath = aReq.getFilePath();
        Path javaPath = getJavaPathForLocalPath(filePath);

        // If directory and missing, create directory
        if (aReq.isFileDir()) {
            Path dirPath = _fileSystem.getPath(javaPath.toString() + '/');
            if (!Files.exists(dirPath)) {
//                try { Files.createDirectory(dirPath); }
//                catch (IOException e) {
//                    aResp.setException(new RuntimeException("FileSystemSite.doPut: Error creating dir: " + javaPath, e));
//                    return;
//                }
            }
        }

        // Otherwise, write bytes
        else {
            byte[] fileBytes = aReq.getSendBytes();
            if (fileBytes != null) {
                try { Files.write(javaPath, fileBytes); }
                catch(IOException e) { aResp.setException(e); return; }
            }
        }

        // Get last modified time from java file and set in response
        try {
            long lastModTime = Files.getLastModifiedTime(javaPath).toMillis();
            aResp.setLastModTime(lastModTime);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Handle a DELETE request.
     */
    @Override
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get java file
        String filePath = aReq.getFilePath();
        Path javaPath = getJavaPathForLocalPath(filePath);
        try { Files.delete(javaPath); }
        catch (IOException e) { aResp.setException(e); }
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    @Override
    protected void saveLastModTimeForFile(WebFile aFile, long aTime) throws Exception
    {
        // Set in file
        aFile.setLastModTime(aTime);

        // Get java file and set last modified time
        Path javaPath = getJavaPathForLocalPath(aFile.getPath());
        try { Files.setLastModifiedTime(javaPath, FileTime.fromMillis(aTime)); }
        catch (IOException e) { System.err.println("FileSite.setModTimeForFile: Error setting mod time for file: " + javaPath); }

        // Do normal version
        super.saveLastModTimeForFile(aFile, aTime);
    }

    /**
     * Returns the Java file for a WebURL.
     */
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        String filePath = aURL.getPath();
        if (filePath.isEmpty()) filePath = "/";
        return _fileSystem.getPath(filePath).toFile();
    }

    /**
     * Returns the Java path for given local file path.
     */
    private Path getJavaPathForLocalPath(String filePath)  { return _fileSystem.getPath(filePath); }

    /**
     * Test.
     */
    public static void main(String[] args) throws Exception
    {
        configS3();
        testSite();
    }

    /**
     * Configures system properties for the AWS SDK client that the NIO provider uses.
     */
    public static void configS3()
    {
        System.setProperty("aws.endpointUrl", "");
        System.setProperty("aws.accessKeyId", "");
        System.setProperty("aws.secretAccessKey", "");
        System.setProperty("aws.region", "auto");
    }

    private static void testSite()
    {
        WebURL siteUrl = WebURL.createUrl("s3://snapcode");
        WebSite site = siteUrl.getAsSite();

        // Get file system
        var path1 = site.getFileForPath("test/test.txt"); // fileSystem.getPath("test/test.txt");
        System.out.println(path1.getText());

        var file2 = site.createFileForPath("test/hello.txt", false);
        file2.setText("Hello to/from S3 NIO!  " + System.currentTimeMillis());
        file2.save();

        System.out.println(file2.getUrl().getText());
    }

    private static void testFiles() throws Exception
    {
        String bucketName = "snapcode";

        // Get S3 file system
        FileSystem fileSystem = FileSystems.getFileSystem(URI.create("s3://" + bucketName));
        FileSystemProvider fileSystemProvider = fileSystem.provider();
        System.out.println("Using FileSystem: " + fileSystem);
        System.out.println("Using FileSystem provider: " + fileSystemProvider);

        // Get file system
        URI uri1 = new URI("s3://" + bucketName + "/" + "test/test.txt");
        var path1 = Paths.get(uri1); // fileSystem.getPath("test/test.txt");
        System.out.println(Files.readString(path1));

        URI uri2 = new URI("s3://" + bucketName + "/" + "test/hello.txt");
        var path2 = Paths.get(uri2);
        Files.writeString(path2, "Hello to/from S3 NIO!", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println(Files.readString(path2));
    }
}