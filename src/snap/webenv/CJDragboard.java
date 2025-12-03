package snap.webenv;
import snap.gfx.Image;
import snap.view.ViewEvent;
import snap.webapi.*;

/**
 * A CJClipboard subclass to support drag and drop.
 */
public class CJDragboard extends CJClipboard {

    // The view event
    private ViewEvent  _viewEvent;

    // The shared clipboard for system drag/drop
    private static CJDragboard  _sharedDrag;

    /**
     * Constructor.
     */
    public CJDragboard()
    {
        super();
    }

    /**
     * Starts the drag.
     */
    @Override
    public void startDrag()
    {
        // Set Dragging true and consume event
        _viewEvent.consume();

        // Get drag image
        Image dragImage = getDragImage();
        if (dragImage == null)
            dragImage = Image.getImageForSize(1,1,true);

        // Get native HTML element for image (set style to hide when added on screen for drag)
        HTMLElement img = (HTMLElement) dragImage.getNative();
        double dx = getDragImageOffset().x;
        double dy = getDragImageOffset().y;
        img.getStyle().setProperty("position", "absolute");
        img.getStyle().setProperty("left", "-100%");

        // Start drag
        _dataTrans.startDrag(img, dx, dy);
    }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { }

    /**
     * Sets the current event.
     */
    protected void setEvent(ViewEvent anEvent)
    {
        _viewEvent = anEvent;

        // If DragGesture, create new DataTrans for client to configure
        if (anEvent.isDragGesture())
            _dataTrans = WebEnv.get().newDataTransfer();

        // If DragDrop etc., get DataTransfer from drag event
        else {
            DragEvent dragEvent = (DragEvent) anEvent.getEvent();
            _dataTrans = dragEvent.getDataTransfer();
        }
    }

    /**
     * Returns the shared Clipboard for drag and drop.
     */
    public static CJClipboard getDrag(ViewEvent anEvent)
    {
        // Create if missing
        if (_sharedDrag == null)
            _sharedDrag = new CJDragboard();

        // Set event
        if (anEvent != null)
            _sharedDrag.setEvent(anEvent);

        // Return
        return _sharedDrag;
    }
}
