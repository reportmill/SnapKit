package snap.javafx;
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
protected boolean hasDataImpl(String aMIMEType)
{
    DataFormat df = getDataFormat(aMIMEType);
    return getClipboard().hasContent(df);
}

/**
 * Returns the clipboard content.
 */
protected ClipboardData getDataImpl(String aMIMEType)
{
    DataFormat df = getDataFormat(aMIMEType);
    Object content = getClipboard().getContent(df);
    return new ClipboardData(aMIMEType, content);
}

/**
 * Sets the clipboard content.
 */
protected void addDataImpl(String aMIMEType, ClipboardData aData)
{
    // Do normal version
    super.addDataImpl(aMIMEType, aData);
    
    // Create map of content types for JavaFX
    Map <DataFormat,Object> content = new HashMap();
    for(String mtype : getClipboardDatas().keySet()) {
        DataFormat df = getDataFormat(mtype);
        ClipboardData cdata = getClipboardDatas().get(mtype);
        Object data = cdata.isString()? cdata.getString() : cdata.isFileList()? cdata.getJavaFiles() :
            cdata.getBytes();
        content.put(df, data);
    }
    
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
    // Map STRING and FILE_LIST to standard flavors
    if(aName.equals(STRING)) return DataFormat.PLAIN_TEXT;
    if(aName.equals(FILE_LIST)) return DataFormat.FILES;
    
    // For all others, create DataFormat
    return new DataFormat(aName, aName);
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