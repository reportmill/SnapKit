package snap.view;
import java.io.*;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.*;
import snap.web.*;

/**
 * ClipboardData represents a data entry from a copy/paste or drag/drop.
 */
public class ClipboardData {

    // The data source
    Object         _src;
    
    // A URL to the file contents
    WebURL         _srcURL;
    
    // The data name, if applicable
    String         _name;
    
    // The MIME type
    String         _mimeType;
    
    // The string
    String         _string;
    
    // The bytes
    byte           _bytes[];

/**
 * Creates a ClipboardData from source.
 */
public ClipboardData(Object aSource)
{
    _src = aSource;
    if(_src instanceof String) {
        _string = (String)_src; _mimeType = MIMEType.TEXT; }
}

/**
 * Creates a ClipboardData from source.
 */
public ClipboardData(String aMimeType, Object aSource)
{
    _src = aSource; _mimeType = aMimeType; if(_mimeType==null) _mimeType = MIMEType.UKNOWN;
    if(_src instanceof String)
        _string = (String)_src;
}

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
    if(_src instanceof byte[]) return null;
    return _srcURL = WebURL.getURL(_src);
}

/**
 * Returns whether data is String.
 */
public boolean isString()  { return _src instanceof String; }

/**
 * Returns whether data is File list.
 */
public boolean isFileList()  { return _mimeType==Clipboard.FILE_LIST; }

/**
 * Returns whether data is a File.
 */
public boolean isFile()  { return !isString() && !isFileList(); }

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
public String getExtension()
{
    if(getName()!=null && getName().indexOf('.')>0)
        return FilePathUtils.getExtension(getName());
    if(getMIMEType()!=null)
        return MIMEType.getExtension(getMIMEType());
    return null;
}

/**
 * Returns the file content type.
 */
public String getMIMEType()  { return _mimeType; }

/**
 * Returns the data as string.
 */
public String getString()
{
    // If already set, just return
    if(_string!=null) return _string;
    
    // Handle get string from bytes
    byte bytes[] = getBytes();
    if(bytes!=null)
        return _string = new String(bytes);
    
    // Complain and return null
    System.err.println("ClipboardData.getString: String not available for source " + _src);
    return null;
}

/**
 * Returns the data as byte array.
 */
public byte[] getBytes()
{
    // If already set, just return
    if(_bytes!=null) return _bytes;
    
    // If String set, return bytes
    if(_string!=null)
        return _bytes = _string.getBytes();
    
    // Handle get bytes from byte array or InputStream
    if(_src instanceof byte[] || _src instanceof InputStream)
        return _bytes = SnapUtils.getBytes(_src);
        
    // Handle get bytes from Source URL
    if(getSourceURL()!=null)
        return _bytes = getSourceURL().getBytes();
        
    // Complain and return null
    System.err.println("ClipboardData.getBytes: Bytes not available for source " + _src);
    return null;
}

/**
 * Returns the data as input stream.
 */
public InputStream getInputStream()  { return new ByteArrayInputStream(getBytes()); }

/**
 * Returns the data as a list of files.
 */
public List <ClipboardData> getFiles()
{
    List files = new ArrayList();
    if(_src instanceof List) { List list = (List)_src;
        for(Object file : list)
            files.add(ClipboardData.get(file)); }
    return files;
}

/**
 * Returns a conventional Java file, if available.
 */
public File getJavaFile()  { return _src instanceof File? (File)_src : null; }

/**
 * Returns the data as list of Java files.
 */
public List <File> getJavaFiles()
{
    return _src instanceof List? (List)_src : null;
}

/**
 * Returns a ClipboardData for given object.
 */
public static ClipboardData get(Object theData)
{
    // Handle ClipboardData
    if(theData instanceof ClipboardData)
        return (ClipboardData)theData;
        
    // Handle String
    if(theData instanceof String)
        return new ClipboardData(Clipboard.STRING, theData);
        
    // Handle File
    if(theData instanceof File) { File file = (File)theData;
        String mtype = MIMEType.getType(file.getPath());
        return new ClipboardData(mtype, file);
    }
        
    // Handle File List
    if(theData instanceof List) { List list = (List)theData; Object item0 = list.size()>0? list.get(0) : null;
        if(item0 instanceof File || item0 instanceof WebURL)
            return new ClipboardData(Clipboard.FILE_LIST, list);
    }
    
    // Handle File array
    if(theData instanceof File[]) { File files[] = (File[])theData;
        return new ClipboardData(Clipboard.FILE_LIST, Arrays.asList(files)); }
    
    // Handle Image
    if(theData instanceof Image) { Image img = (Image)theData;
        
        // Get image type and bytes
        byte bytes[] = img.getBytes();
        String mtype = MIMEType.getType(img.getType());
            
        // Add data for type
        return new ClipboardData(mtype, bytes);
    }
    
    // Handle Color
    if(theData instanceof Color) { Color color = (Color)theData;
        return new ClipboardData(Clipboard.COLOR, color.toHexString()); }
    
    // Complain
    System.err.println("ClipboardData.get: Unknown data type " + theData);
    return null;
}

}