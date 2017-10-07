package snap.javafx;
import java.io.File;
import java.util.*;
import javafx.scene.input.*;
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
    if(aName.equals(FILE_LIST)) {
        List <File> jfiles = getClipboard().getFiles(); if(jfiles==null) return null;
        List <ClipboardData> cfiles = new ArrayList(jfiles.size());
        for(File jfile : jfiles) cfiles.add(ClipboardData.get(jfile));
        return cfiles;
    }
    
    DataFormat df = getDataFormat(aName);
    Object content = getClipboard().getContent(df);
    //if(aName.equals(IMAGE) && content instanceof javafx.scene.image.Image) content = Image.get(content);
    //if(aName.equals(COLOR) && content instanceof String) content = Color.get(content);
    return content;
}

/**
 * Sets the clipboard content.
 */
public void setContent(String aMIMEType, Object theData)
{
    Map <DataFormat,Object> content = new HashMap();
    content.put(getDataFormat(aMIMEType), theData);
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
    if(aName.equals(FILE_LIST)) return DataFormat.FILES;
    //if(aName.equals(IMAGE)) return DataFormat.IMAGE;
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
    
    // Set DragImage
    if(getDragImage()!=null) {
        Point offset = getDragImageOffset();
        db.setDragView((javafx.scene.image.Image)getDragImage().getNative(), -offset.x, offset.y);
    }
    
    // Start drag
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