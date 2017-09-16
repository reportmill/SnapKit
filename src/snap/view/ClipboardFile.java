package snap.view;
import java.io.File;
import java.io.InputStream;
import snap.util.FilePathUtils;
import snap.util.SnapUtils;
import snap.web.WebURL;

/**
 * ClipboardFile represents a file from a copy/paste or drag/drop where the actual file might not be accessible.
 */
public class ClipboardFile {

    // The file source
    Object         _src;
    
    // A URL to the file contents
    WebURL         _srcURL;
    
    // The file name
    String         _name;
    
    // The MIME type
    String         _mimeType;
    
    // The bytes
    byte           _bytes[];

/**
 * Creates a ClipboardFile from source.
 */
public ClipboardFile(Object aSource)  { _src = aSource; }

/**
 * The source of the file.
 */
public Object getSource()  { return _src; }

/**
 * Returns the URL to the file.
 */
public WebURL getSourceURL()
{
    if(_srcURL!=null) return _srcURL;
    return _srcURL = WebURL.getURL(_src);
}

/**
 * Returns the file name.
 */
public String getName()
{
    if(_name!=null) return _name;
    if(getSourceURL()!=null)
        return _name = getSourceURL().getPathName();
    return _name;
}

/**
 * Returns the file extension.
 */
public String getExtension()  { return FilePathUtils.getExtension(getName()); }

/**
 * Returns the file content type.
 */
public String getMIMEType()  { return _mimeType; }

/**
 * Returns the file bytes.
 */
public byte[] getBytes()
{
    if(_bytes!=null) return _bytes;
    if(_src instanceof byte[] || _src instanceof InputStream)
        return _bytes = SnapUtils.getBytes(_src);
    if(getSourceURL()!=null)
        return _bytes = getSourceURL().getBytes();
    return null;
}

/**
 * Returns a conventional file, if available.
 */
public File getFile()  { return _src instanceof File? (File)_src : null; }

}