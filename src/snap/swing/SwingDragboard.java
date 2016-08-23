package snap.swing;
import java.awt.Window;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import snap.gfx.Image;
import snap.gfx.Point;
import snap.view.*;

/**
 * A class to manage dragging.
 */
public class SwingDragboard extends SwingClipboard implements Dragboard, DragSourceListener, DragSourceMotionListener {

    // The item being dragged
    Object                _content;

    // The drag image
    Image                 _img;
    
    // The point that the drag image should be dragged by
    Point                 _imgOffset = new Point();
    
    // The view to initiate drag
    View                  _view;
    
    // The root pane for source of drag
    RootView              _rpane;
    
    // The ViewEvent that iniated the drag
    SwingEvent            _event;
    
    // The DragGestureEvent
    DragGestureEvent      _dge;
    
    // A window that is optionally used to simulate image dragging.
    JWindow               _dragWindow;

/**
 * Creates a new Dragboard for given view.
 */
public SwingDragboard(View aView, ViewEvent anEvent)
{
    // Set view and event
    _view = aView; _rpane = _view.getRootView();
    _event = (SwingEvent)anEvent;
    
    // If DragGesture, set DragGestureEvent
    if(_event.isDragGesture())
        setDragGestureEvent(_event.getEvent(DragGestureEvent.class));
    
    // If DragEvent, get transferable
    else if(_event.isDragEvent()) {
        DropTargetDragEvent dragEv = _event.getEvent(DropTargetDragEvent.class);
        if(dragEv!=null) _trans = dragEv.getTransferable();
        DropTargetDropEvent dropEv =_event. getEvent(DropTargetDropEvent.class);
        if(dropEv!=null) _trans = dropEv.getTransferable();
    }
    
    // Compain if passed a bogus event
    else System.err.println("SwingDragBoard.init: Invalid event type: " + anEvent);
}

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
 * Returns the DragGestureEvent.
 */
public DragGestureEvent getDragGestureEvent()  { return _dge; }

/**
 * Sets the DragGestureEvent.
 */
public void setDragGestureEvent(DragGestureEvent anEvent)  { _dge = anEvent; }

/**
 * Returns the component.
 */
protected JComponent getComponent()  { return (JComponent)_dge.getComponent(); }

/**
 * Returns the DragSource.
 */
public DragSource getDragSource()  { return _dge.getDragSource(); }

/**
 * Starts the drag.
 */
public void startDrag()
{
    // Get DragSource and start Listening to drag events drag source
    DragSource dragSource = getDragSource();
    dragSource.removeDragSourceListener(this); dragSource.removeDragSourceMotionListener(this);
    dragSource.addDragSourceListener(this); dragSource.addDragSourceMotionListener(this);
    
    // Check to see if image drag is supported by system. If not (ie, Windows), simulate image dragging with a window.
    if(getDragImage()!=null && !DragSource.isDragImageSupported())
        createDragWindow();

    // Start drag
    Transferable trans = getTrans();
    java.awt.Image aimg = getDragImage()!=null? (java.awt.Image)getDragImage().getNative() : null;
    double dx = aimg!=null? (-getDragImage().getWidth()/2 - getDragImageOffsetX()) : 0;
    double dy = aimg!=null? (-getDragImage().getHeight()/2 - getDragImageOffsetY()) : 0;
    java.awt.Point apnt = new java.awt.Point((int)dx, (int)dy);
    dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, aimg, apnt, trans, null);
    ViewUtils.setActiveDragboard(this);
}

/**
 * Creates new drag source listener.
 */
protected void createDragWindow()
{
    // Get drag image and the source window (if source is component)
    Image img = getDragImage();
    Window sourceWindow = SwingUtils.getWindow(getComponent());

    // Create window for drag image
    _dragWindow = new JWindow(sourceWindow);
    _dragWindow.setSize((int)img.getWidth(), (int)img.getHeight());
   
    // Create label for drag image and add to window
    _dragWindow.getContentPane().add(new JLabel(new ImageIcon((java.awt.Image)img.getNative())));
}

/**
 * DragSourceMotionListener method.
 */
public void dragMouseMoved(DragSourceDragEvent anEvent) 
{
    // Make the window follow the cursor, if using window-based image dragging
    // Note that the offset of the window is 1 pixel down and to the right of the cursor.  This is different
    // from how it appears if the system can handle image dragging, in which case the image is centered under the
    // cursor. If the dragWindow were centered at the cursor position, the dragWindow would become the destination
    // of all the system drag events, and we would never get meaningful dragEntered, dragExited, etc. events.
    // Clients can use translateRectToDropDestination() to get the proper image location across systems.
    if(_dragWindow!=null) {
        _dragWindow.setLocation(anEvent.getX()+1, anEvent.getY()+1);
        if(!_dragWindow.isVisible())
            _dragWindow.setVisible(true);
    }
}

/**
 * DragSourceListener method.
 */
public void dragDropEnd(DragSourceDropEvent anEvent)
{
    // Get rid of the window and its resources
    if(_dragWindow!=null) {
        _dragWindow.setVisible(false);
        _dragWindow.dispose(); _dragWindow = null;
    }
    
    // Stop listening to events
    dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnd);
    SwingUtilities.invokeLater(() -> ViewUtils.setActiveDragboard(null));
}

/** DragSourceListener methods. */
public void dragEnter(DragSourceDragEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnter); }
public void dragOver(DragSourceDragEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceOver); }
public void dropActionChanged(DragSourceDragEvent anEvent)  { }
public void dragExit(DragSourceEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceExit); }

/**
 * Sends an event to RootView.
 */
private void dispatchToRootView(Object anEvent, ViewEvent.Type aType)
{
    ViewEvent nevent = SwingViewEnv.get().createEvent(_rpane, anEvent, aType, null);
    _rpane.dispatchDragSourceEvent(nevent);
}

}