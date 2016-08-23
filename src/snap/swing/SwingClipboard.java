package snap.swing;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.view.Clipboard;

/**
 * A custom class.
 */
public class SwingClipboard implements Clipboard {
    
    // The transferable
    Transferable        _trans;
    
    // Image flavor
    static DataFlavor   _imageFlavor = new DataFlavor("text/image", "Snap Image");
    static DataFlavor   _jpegFlavor = new DataFlavor("image/jpeg", "JPEG Image Data");
    
    // The shared clipboard
    static SwingClipboard      _shared = new SwingClipboard();

/**
 * Returns the clipboard content.
 */
public boolean hasContent(String aName)
{
    DataFlavor df = getDataFlavor(aName);
    Transferable trans = getTrans(); if(trans==null) return false;
    return trans.isDataFlavorSupported(df);
}

/**
 * Returns the clipboard content.
 */
public Object getContent(String aName)
{
    DataFlavor df = getDataFlavor(aName);
    Transferable trans = getTrans();
    try { return trans.getTransferData(df); }
    catch(Exception e) { throw new RuntimeException(e); }
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
            theContents = new Object[] { COLOR, theContents[0] };
    }
    
    // Create transferable and set
    GenericTransferable trans = new GenericTransferable(theContents);
    if(this==_shared)
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
    else _trans = trans;
}

/**
 * Returns whether clipboard has string.
 */
public boolean hasString()  { return getTrans().isDataFlavorSupported(DataFlavor.stringFlavor); }

/**
 * Returns whether clipboard has string.
 */
public boolean hasFiles()  { return getTrans().isDataFlavorSupported(DataFlavor.javaFileListFlavor); }

/**
 * Returns a string from given transferable.
 */
public String getString()  { return getString(getTrans()); }

/**
 * Returns a list of files from a given transferable.
 */
public List <File> getFiles()  { return getFiles(getTrans()); }

/**
 * Returns the current transferable.
 */
protected Transferable getTrans()
{
    if(this==_shared) {
        java.awt.datatransfer.Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        return cb.getContents(null);
    }
    
    else return _trans;
}

/**
 * Returns a dataflavor for a name.
 */
protected DataFlavor getDataFlavor(String aName)
{
    if(aName.equals(STRING)) return DataFlavor.stringFlavor;
    if(aName.equals(FILES)) return DataFlavor.javaFileListFlavor;
    if(aName.equals(IMAGE)) return _imageFlavor;
    //if(aName.equals("rm-xstring")) return new DataFlavor("text/rm-xstring", "ReportMill Text Data");
    String name = aName; if(name.indexOf('/')<0) name = "text/" + name;
    return new DataFlavor(name, aName);
}

/**
 * Returns a string from given transferable.
 */
protected static String getString(Transferable aTrans)
{
    // Handle StringFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.stringFlavor))
        try { return (String)aTrans.getTransferData(DataFlavor.stringFlavor); }
        catch(Exception e) { e.printStackTrace(); return null; }
    
    // Handle FileList
    List <File> files = getFiles(aTrans);
    if(files!=null && files.size()>0)
        return files.get(0).getAbsolutePath();
    
    // Otherwise return null
    return null;
}

/**
 * Returns a list of files from a given transferable.
 */
protected static List <File> getFiles(Transferable aTrans)
{
    // Handle JavaFileListFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        try { return (List)aTrans.getTransferData(DataFlavor.javaFileListFlavor); }
        catch(Exception e) { System.err.println(e); return null; }
    
    // Otherwise return null
    return null;
}

/**
 * Returns the shared SwingClipboard.
 */
public static SwingClipboard get()  { return _shared; }

/**
 * Transferable implementation for text editor and xstrings.
 */
private class GenericTransferable implements Transferable {
    
    // The list of content types and values
    List <DataFlavor>  _types = new ArrayList();
    List <Object>      _contents = new ArrayList();
    
    /** Creates a new editor clipboard for given xstring. */
    public GenericTransferable(Object ... theContents)
    {
        for(int i=0;i<theContents.length;) {
            String name = (String)theContents[i++];
            DataFlavor df = getDataFlavor(name);
            _types.add(df); _contents.add(theContents[i++]);
            if(df==_imageFlavor) {
                _types.add(DataFlavor.imageFlavor); _contents.add(theContents[i-1]);
                _types.add(_jpegFlavor); _contents.add(theContents[i-1]);
            }
        }
    }
    
    /** Returns the supported flavors: RMTextFlavor and stringFlavor. */
    public DataFlavor[] getTransferDataFlavors() { return _types.toArray(new DataFlavor[0]); }
    
    /** Returns whether given flavor is supported. */
    public boolean isDataFlavorSupported(DataFlavor aFlavor)  { return _types.contains(aFlavor); }
    
    /** Returns an inputstream with clipboard data for requested flavor. */
    public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
    {
        Object contents = null;
        for(int i=0;i<_types.size() && contents==null;i++)
            if(aFlavor.equals(_types.get(i)))
                contents = _contents.get(i);
        if(aFlavor==DataFlavor.imageFlavor && contents instanceof Image)
            contents = ((Image)contents).getNative();
        if(aFlavor==_jpegFlavor && contents instanceof Image)
            contents = ((Image)contents).getBytesJPEG();
        if(contents instanceof byte[])
            contents = new ByteArrayInputStream((byte[])contents);
        if(contents==null)
            throw new UnsupportedFlavorException(aFlavor);
        return contents;
    }
}

}