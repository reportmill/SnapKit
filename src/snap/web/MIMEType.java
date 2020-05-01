package snap.web;
import snap.util.FilePathUtils;

/**
 * A class to help work with MIME type strings.
 */
public class MIMEType {

    // List of MIME Types
    public static final String HTML = "text/html";
    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String GIF = "image/gif";
    public static final String JAVA = "text/java";
    public static final String MIDI = "audio/mid";
    public static final String PDF = "application/pdf";
    public static final String ReportFile = "application/rpt";
    public static final String TEXT = "text/plain";
    public static final String WAV = "audio/x-wav";
    public static final String MP3 = "audio/mpeg";
    public static final String SETTINGS = "application/settings";
    public static final String UKNOWN = "application/octet-stream";
    
    /**
     * Returns the extension for a MIME type.
     */
    public static String getExtension(String aMIMEType)
    {
        switch (aMIMEType) {
            case JPEG: return "jpg";
            case PNG: return "png";
            case GIF: return "gif";
            case HTML: return "html";
            case PDF: return "pdf";
            case TEXT: return "txt";
            default: return null;
        }
    }

    /**
     * Returns the MIME type for string path, extension, type.
     */
    public static String getType(String aStr)
    {
        // Get type for given string
        String type = FilePathUtils.getType(aStr);
        if (type.length()==0) type = aStr.toLowerCase();

        // Return type
        switch (type) {
            case "jpg": case "jpeg": return JPEG;
            case "png": return PNG;
            case "gif": return GIF;
            case "html": return HTML;
            case "pdf": return PDF;
            case "txt": return TEXT;
            default: return null;
        }
    }

    /**
     * Returns all known image types.
     */
    public static String[] getImageTypes()
    {
        return new String[] { PNG, JPEG, GIF };
    }
}