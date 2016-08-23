package snap.javafx;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import snap.gfx.Image;
import snap.gfx.Point;
import snap.view.*;

/**
 * A custom class.
 */
public class JFXDragboard extends JFXClipboard implements snap.view.Dragboard {

    // The drag image
    Image                         _img;
    
    // The point that the drag image should be dragged by
    Point                         _imgOffset = new Point();
    
    // The view
    View                          _view;
    
    // The view event
    JFXEvent                      _event;
    
    // The JavaFX Dragboard
    javafx.scene.input.Dragboard  _dboard;
    

/**
 * Creates a new Dragboard for given view.
 */
public JFXDragboard(View aView, ViewEvent anEvent)  { _view = aView; _event = (JFXEvent)anEvent; }

/**
 * Returns the image to be dragged.
 */
public Image getDragImage()  { return _img; }

/**
 * Sets the image to be dragged.
 */
public void setDragImage(Image anImage)  { _img = anImage; }

/**
 * Returns the point that the drag image should be dragged by.
 */
public Point getDragImageOffset()  { return _imgOffset; }

/**
 * Returns the point that the drag image should be dragged by.
 */
public void setDragImageOffset(Point aPoint)  { _imgOffset = aPoint; }

/**
 * Returns the clipboard.
 */
public javafx.scene.input.Dragboard getClipboard()
{
    if(_dboard!=null) return _dboard;
    if(_event.isDragEvent())
        return _dboard = _event.getDragEvent().getDragboard();
    return _dboard = _view.getNative(javafx.scene.Node.class).startDragAndDrop(TransferMode.ANY);
}

/**
 * Starts the drag.
 */
public void startDrag()
{
    // Get DragSource and start Listening to drag events drag source
    Dragboard db = getClipboard();
    //ClipboardContent content = getContent(); //if(getDragImage()!=null) db.setDragView(getDragImage());
    //db.setContent(content); dragSource.addDragSourceListener(this); dragSource.addDragSourceMotionListener(this);
    
    // Set DragImage
    if(getDragImage()!=null)
        db.setDragView((javafx.scene.image.Image)getDragImage().getNative(), -_imgOffset.getX(), _imgOffset.getY());
    
    // Start drag
    //_dragger = this;
    //Transferable trans = getTransferable();
    //dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, getDragImage(), getDragImageOffset(), trans, _dsl);
    _event.consume();
}

}