package snap.javafx;
import java.io.File;
import java.util.*;
import javafx.scene.input.DataFormat;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.view.Clipboard;

/**
 * A custom class.
 */
public class JFXClipboard implements Clipboard {
    
    // The shared clipboard
    static JFXClipboard      _shared = new JFXClipboard();
    
    // The map of formats
    static Map <String,DataFormat>  _dformats = new HashMap();

/**
 * Returns the clipboard content.
 */
public boolean hasContent(String aName)
{
    DataFormat df = getDataFormat(aName);
    return getClipboard().hasContent(df);
}

/**
 * Returns the clipboard content.
 */
public Object getContent(String aName)
{
    DataFormat df = getDataFormat(aName);
    Object content = getClipboard().getContent(df);
    if(aName.equals(IMAGE) && content instanceof javafx.scene.image.Image)
        content = Image.get(content);
    if(aName.equals(COLOR) && content instanceof String)
        content = Color.get(content);
    return content;
}

/**
 * Sets the clipboard content.
 */
public void setContent(Object ... theContents)
{
    // If contents only one object, map to key
    if(theContents.length==1) {
        if(theContents[0] instanceof String)
            theContents = new Object[] { STRING, theContents[0] };
        else if(theContents[0] instanceof File)
            theContents = new Object[] { FILES, Arrays.asList(theContents[0]) };
        else if(theContents[0] instanceof List)
            theContents = new Object[] { FILES, theContents[0] };
        else if(theContents[0] instanceof Image)
            theContents = new Object[] { IMAGE, theContents[0] };
        else if(theContents[0] instanceof Color)
            theContents = new Object[] { COLOR, ((Color)theContents[0]).toHexString() };
    }
    
    Map <DataFormat,Object> content = new HashMap();
    for(int i=0;i<theContents.length;i+=2)
        content.put(getDataFormat((String)theContents[i]), theContents[i+1]);
    
    // Create transferable and set
    //if(this==_shared)
    getClipboard().setContent(content);
}

/**
 * Returns whether clipboard has string.
 */
public boolean hasString()  { return getClipboard().hasString(); }

/**
 * Returns whether clipboard has string.
 */
public boolean hasFiles()  { return getClipboard().hasFiles(); }

/**
 * Returns a string from given transferable.
 */
public String getString()  { return getClipboard().getString(); }

/**
 * Returns a list of files from a given transferable.
 */
public List <File> getFiles()  { return getClipboard().getFiles(); }

/**
 * Returns a dataflavor for a name.
 */
protected DataFormat getDataFormat(String aName)
{
    DataFormat df = _dformats.get(aName);
    if(df==null) _dformats.put(aName, df = getDataFormatImpl(aName));
    return df;
}

/**
 * Returns a dataflavor for a name.
 */
protected DataFormat getDataFormatImpl(String aName)
{
    if(aName.equals(STRING)) return DataFormat.PLAIN_TEXT;
    if(aName.equals(FILES)) return DataFormat.FILES;
    if(aName.equals(IMAGE)) return DataFormat.IMAGE;
    //if(aName.equals("rm-xstring")) return new DataFlavor("text/rm-xstring", "ReportMill Text Data");
    String name = aName; if(name.indexOf('/')<0) name = "text/" + name;
    return new DataFormat(name, aName);
}

/**
 * Returns the JavaFX clipboard.
 */
protected javafx.scene.input.Clipboard getClipboard()  { return javafx.scene.input.Clipboard.getSystemClipboard(); }

/**
 * Returns the shared JFXClipboard.
 */
public static JFXClipboard get()  { return _shared; }

}