package snap.web;
import java.util.List;

/**
 * This WebSite subclass implements some conveniences.
 */
public class WebSiteX extends WebSite {

    /**
     * Constructor.
     */
    public WebSiteX()
    {
        super();
    }

    /**
     * Handles a get or head request.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get request file path
        String filePath = aReq.getFilePath();

        // Get file header for file path
        FileHeader fileHeader = getFileHeaderForPath(filePath);

        // If not found, return not found
        if (fileHeader == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Configure response
        aResp.setCode(WebResponse.OK);
        aResp.setFileHeader(fileHeader);

        // If Head, just return
        if (isHead)
            return;

        // Handle plain file contents
        if (fileHeader.isFile()) {
            byte[] bytes = getFileBytesForGet(aReq, aResp);
            aResp.setBytes(bytes);
        }

        // Handle directory file contents
        else {
            List<FileHeader> fileHeaders = getFileHeadersForPath(filePath);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns the file header for given path.
     */
    protected FileHeader getFileHeaderForPath(String filePath)
    {
        throw new RuntimeException("WebSite.getFileHeaderForPath: Not implemented");
    }

    /**
     * Returns FileHeaders for dir file path.
     */
    protected List<FileHeader> getFileHeadersForPath(String filePath)
    {
        throw new RuntimeException("WebSite.getFileHeaderForPath: Not implemented");
    }

    /**
     * Returns bytes for Get call and given request/response.
     */
    protected byte[] getFileBytesForGet(WebRequest aReq, WebResponse aResp)
    {
        throw new RuntimeException("WebSite.getFileBytesForGet: Not implemented");
    }
}
