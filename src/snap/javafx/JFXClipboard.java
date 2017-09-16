package snap.javafx;
import java.io.File;
import java.util.*;
import javafx.scene.input.*;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.gfx.Point;
import snap.view.*;
import snap.view.Clipboard;

/**
 * A custom class.
 */
public class JFXClipboard extends Clipboard {
    
    // The view
    View                          _view;
    
    // The view event
    JFXEvent                      _event;
    
    // The JavaFX Dragboard
    javafx.scene.input.Dragboard  _dboard;
    
    // The shared clipboard
    static JFXClipboard      _shared = new JFXClipboard();
    
    // The map of formats
    static Map <String,DataFormat>  _dformats = new HashMap();

/**
 * Creates a new JFXClipboard.
 */
public JFXClipboard()  { }

/**
 * Creates a new JFXClipboard for given view.
 */
public JFXClipboard(View aView, ViewEvent anEvent)  { _view = aView; _event = (JFXEvent)anEvent; }

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
    // Handle FILES
    if(aName.equals(FILES)) {
        List <File> jfiles = getClipboard().getFiles(); if(jfiles==null) return null;
        List <ClipboardFile> cfiles = new ArrayList(jfiles.size());
        for(File jfile : jfiles) cfiles.add(new ClipboardFile(jfile));
        return cfiles;
    }
    
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
 * Starts the drag.
 */
public void startDrag()
{
    // Get DragSource and start Listening to drag events drag source
    Dragboard db = getDragboard();
    //ClipboardContent content = getContent(); //if(getDragImage()!=null) db.setDragView(getDragImage());
    //db.setContent(content); dragSource.addDragSourceListener(this); dragSource.addDragSourceMotionListener(this);
    
    // Set DragImage
    if(getDragImage()!=null) {
        Point offset = getDragImageOffset();
        db.setDragView((javafx.scene.image.Image)getDragImage().getNative(), -offset.x, offset.y);
    }
    
    // Start drag
    //_dragger = this; Transferable trans = getTransferable();
    //dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, getDragImage(), getDragImageOffset(), trans, _dsl);
    _event.consume();
}

/**
 * Returns the JavaFX clipboard.
 */
protected javafx.scene.input.Clipboard getClipboard()  { return javafx.scene.input.Clipboard.getSystemClipboard(); }

/**
 * Returns the clipboard.
 */
public javafx.scene.input.Dragboard getDragboard()
{
    if(_dboard!=null) return _dboard;
    if(_event.isDragEvent())
        return _dboard = _event.getDragEvent().getDragboard();
    return _dboard = _view.getNative(javafx.scene.Node.class).startDragAndDrop(TransferMode.ANY);
}

/**
 * Returns the shared JFXClipboard.
 */
public static JFXClipboard get()  { return _shared; }

}