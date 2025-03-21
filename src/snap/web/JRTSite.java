package snap.web;
import snap.util.SnapUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A WebSite implementation for JRT
 */
public class JRTSite extends WebSite {

    // The module name
    private String  _moduleName;

    // The FileSystem
    private FileSystem  _fileSystem;

    /**
     * Constructor.
     */
    public JRTSite()
    {
        super();
    }

    @Override
    public void setURL(WebURL aURL)
    {
        // Do normal version
        super.setURL(aURL);

        // Set module name
        String urlStr = aURL.getString();
        _moduleName = urlStr.replace("jrt:/", "");

        try {
            URI jrtURI = URI.create("jrt:/");
            String javaHome = System.getProperty("java.home");
            Map<String,String> javaHomeMap = Collections.singletonMap("java.home", javaHome);
            if (SnapUtils.isWebVM)
                javaHomeMap = Collections.emptyMap();
            _fileSystem = FileSystems.newFileSystem(jrtURI, javaHomeMap);
        }

        // Handle exceptions
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Implement.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get request file path
        String filePath = aReq.getFilePath();

        // Handle NOT_FOUND
        Path modulePath = getModulePathForUrlPath(filePath);
        if (!Files.exists(modulePath) || !Files.isReadable(modulePath)) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        boolean isDir = Files.isDirectory(modulePath);
        FileHeader fileHeader = new FileHeader(filePath, isDir);
        aResp.setFileHeader(fileHeader);
        if (isHead)
            return;

        // Handle Get file: read and set bytes
        if (fileHeader.isFile()) {
            try {
                byte[] bytes = Files.readAllBytes(modulePath);
                aResp.setBytes(bytes);
            }
            catch(IOException e) { aResp.setException(e); }
        }

        // Handle Get directory: configure directory info and return
        else {
            List<FileHeader> fileHeaders = getFileHeadersForUrlPath(filePath);
            aResp.setFileHeaders(fileHeaders.toArray(new FileHeader[0]));
        }
    }

    /**
     * Returns the file header for given module path.
     */
    protected FileHeader getFileHeaderForModulePath(Path modulePath)
    {
        String urlPath = getUrlPathForModulePath(modulePath);
        boolean isDir = Files.isDirectory(modulePath);
        return new FileHeader(urlPath, isDir);
    }

    /**
     * Returns the child file headers at given path.
     */
    protected List<FileHeader> getFileHeadersForUrlPath(String urlPath)
    {
        // Get module path
        Path modulePath = getModulePathForUrlPath(urlPath);

        // Get module path file paths
        try (Stream<Path> dirPathsStream = Files.list(modulePath)) {

            // Convert dir file paths to file headers and return
            Stream<FileHeader> fileHeadersStream = dirPathsStream.map(path -> getFileHeaderForModulePath(path));
            List<FileHeader> fileHeaders = fileHeadersStream.collect(Collectors.toList());
            return fileHeaders;
        }

        // If error, complain
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the module path for given URL path.
     */
    private Path getModulePathForUrlPath(String urlPath)
    {
        return _fileSystem.getPath("/modules", _moduleName, urlPath);
    }

    /**
     * Returns the module path for given URL path.
     */
    private String getUrlPathForModulePath(Path modulePath)
    {
        // If only one path component, return root dir path
        int nameCount = modulePath.getNameCount();
        if (nameCount <= 2)
            return "/";

        // Get subpath and append root dir
        Path urlPath = modulePath.subpath(2, nameCount);
        return '/' + urlPath.toString();
    }
}
