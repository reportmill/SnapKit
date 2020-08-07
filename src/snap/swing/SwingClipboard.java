package snap.swing;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import snap.gfx.Image;
import snap.util.SnapUtils;
import snap.view.*;
import snap.view.Clipboard;

/**
 * A Clipboard implementation for Swing.
 */
public class SwingClipboard extends Clipboard implements DragSourceListener, DragSourceMotionListener {
    
    // The transferable
    private Transferable  _trans;
    
    // The view to initiate drag
    private View  _view;
    
    // The DragGestureEvent
    private DragGestureEvent  _dge;
    
    // A window that is optionally used to simulate image dragging.
    private JWindow  _dragWindow;

    // The shared clipboards for system and drag
    private static SwingClipboard  _shared = new SwingClipboard();
    private static SwingClipboard  _sharedDrag = new SwingClipboard();

    /**
     * Override to map to Swing clipboard.
     */
    protected boolean hasDataImpl(String aMIMEType)
    {
        // Get DataFlavor for MIME type
        DataFlavor df = getDataFlavor(aMIMEType);

        // Return whether transferable supports DataFlavor
        Transferable trans = getTrans();
        return trans!=null && trans.isDataFlavorSupported(df);
    }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMIMEType)
    {
        // Get DataFlavor for MIME type
        DataFlavor df = getDataFlavor(aMIMEType);

        // Get raw data for DataFlavor from transferable
        Transferable trans = getTrans();
        Object data;
        try { data = trans.getTransferData(df); }
        catch(Exception e) { throw new RuntimeException(e); }

        // Return ClipboardData for data
        return new ClipboardData(aMIMEType, data);
    }

    /**
     * Override to support DataFlavor.imageFlavor.
     */
    public boolean hasImage()
    {
        // Try normal version
        boolean hasImg = super.hasImage(); if (hasImg) return true;

        // Return whether transferable supports DataFlavor.imageFlavor
        Transferable trans = getTrans();
        return trans!=null && trans.isDataFlavorSupported(DataFlavor.imageFlavor);
    }

    /**
     * Override to support DataFlavor.imageFlavor.
     */
    public Image getImage()
    {
        // Try normal version
        Image img = super.getImage(); if (img!=null) return img;

        // Get awt image for DataFlavor.imageFlavor from transferable
        Transferable trans = getTrans();
        java.awt.Image img2;
        try { img2 = (java.awt.Image)trans.getTransferData(DataFlavor.imageFlavor); }
        catch(Exception e) { throw new RuntimeException(e); }
        return Image.get(img2);
    }

    /**
     * Override to support DataFlavor.imageFlavor.
     */
    public ClipboardData getImageData()
    {
        // Try normal version
        ClipboardData cd = super.getImageData(); if (cd!=null) return cd;

        // Get awt image for DataFlavor.imageFlavor from transferable
        Image img = getImage(); if (img==null) return null;
        return new ClipboardData(img.getBytesPNG());
    }

    /**
     * Sets the clipboard content.
     */
    protected void addDataImpl(String aMIMEType, ClipboardData aData)
    {
        // Do normal implementation to populate ClipboardDatas map
        super.addDataImpl(aMIMEType, aData);

        // Create transferable and set
        SnapTransferable trans = new SnapTransferable();
        if (this==_shared) {
            java.awt.datatransfer.Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(trans, null);
        }
        else _trans = trans;
    }

    /**
     * Returns the current transferable.
     */
    protected Transferable getTrans()
    {
        // If this Clipboard is System Clipboard, get transferable from System Clipboard
        if (this==_shared) {
            java.awt.datatransfer.Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            return cb.getContents(null);
        }

        // Otherwise, return transferable (assumed to be set by last addData call)
        return _trans;
    }

    /**
     * Returns a dataflavor for a name.
     */
    protected DataFlavor getDataFlavor(String aName)
    {
        // Map STRING and FILE_LIST to standard flavors
        if (aName.equals(STRING))
            return DataFlavor.stringFlavor;
        if (aName.equals(FILE_LIST))
            return DataFlavor.javaFileListFlavor;
        if (aName.equals(IMAGE))
            return DataFlavor.imageFlavor;

        // For all others, create flavor
        return new DataFlavor(aName, aName);
    }

    /**
     * Returns a dataflavor for a name.
     */
    protected String getMIMEType(DataFlavor aFlavor)
    {
        // Map StringFlavor and JavaFileListFlavor to STRING/FILE_LIST
        if (aFlavor.equals(DataFlavor.stringFlavor))
            return STRING;
        if (aFlavor.equals(DataFlavor.javaFileListFlavor))
            return FILE_LIST;
        if (aFlavor.equals(DataFlavor.imageFlavor))
            return IMAGE;

        // Otherwise get Mimetype and return it
        String mtype = aFlavor.getMimeType();
        if (mtype.indexOf(';')>0)
            mtype = mtype.substring(0,mtype.indexOf(';'));
        return mtype;
    }

    /**
     * Starts the drag.
     */
    public void startDrag()
    {
        // Get DragSource and start Listening to drag events drag source
        DragSource dragSource = _dge.getDragSource();
        dragSource.removeDragSourceListener(this);
        dragSource.removeDragSourceMotionListener(this);
        dragSource.addDragSourceListener(this);
        dragSource.addDragSourceMotionListener(this);

        // Check to see if image drag is supported by system. If not (ie, Windows), simulate image dragging with a window.
        if (getDragImage()!=null && !DragSource.isDragImageSupported())
            createDragWindow();

        // Get drag image and point (as AWT img/pnt)
        java.awt.Image img = getDragImage()!=null ? (java.awt.Image)getDragImage().getNative() : null;
        double dx = img!=null ? getDragImageOffset().x : 0;
        double dy = img!=null ? getDragImageOffset().y : 0;
        if (SnapUtils.isMac) { dx = -dx; dy = -dy; } // Mac is flipped?
        java.awt.Point pnt = img!=null ? new java.awt.Point((int)dx, (int)dy) : null;

        // Start drag
        Transferable trans = getTrans();
        dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, img, pnt, trans, null);
    }

    /**
     * Returns the drag source view that started drag.
     */
    public View getDragSourceView()  { return _view; }

    /** DragSourceListener methods. */
    public void dragEnter(DragSourceDragEvent anEvent)
    {
        dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnter);
    }
    public void dragOver(DragSourceDragEvent anEvent)
    {
        dispatchToRootView(anEvent, ViewEvent.Type.DragSourceOver);
    }
    public void dragExit(DragSourceEvent anEvent)
    {
        dispatchToRootView(anEvent, ViewEvent.Type.DragSourceExit);
    }
    public void dropActionChanged(DragSourceDragEvent anEvent)  { }

    /**
     * DragSourceMotionListener method.
     */
    public void dragMouseMoved(DragSourceDragEvent anEvent)
    {
        // Make window follow cursor, if using window-based image dragging
        // Note that offset of window is 1 pixel down and to right of cursor.  This is different from how it appears if
        // system can handle image dragging, in which case image is centered under the cursor. If dragWindow were centered
        // at cursor position, the dragWindow would become the destination of all system drag events, and we never get
        // meaningful dragEntered, dragExited, etc. events.
        // Clients can use translateRectToDropDestination() to get the proper image location across systems.
        if (_dragWindow!=null) {
            _dragWindow.setLocation(anEvent.getX()+1, anEvent.getY()+1);
            if (!_dragWindow.isVisible())
                _dragWindow.setVisible(true);
        }
    }

    /**
     * DragSourceListener method.
     */
    public void dragDropEnd(DragSourceDropEvent anEvent)
    {
        // Get rid of the window and its resources
        if (_dragWindow!=null) {
            _dragWindow.setVisible(false);
            _dragWindow.dispose(); _dragWindow = null;
        }

        // Dispatch DragSourceEnd event
        dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnd);

        // Stop listening to DragSource events.
        DragSource dragSource = _dge.getDragSource();
        dragSource.removeDragSourceListener(this);
        dragSource.removeDragSourceMotionListener(this);
        _view = null; _dge = null;
    }

    /**
     * Sends an event to RootView.
     */
    private void dispatchToRootView(Object anEvent, ViewEvent.Type aType)
    {
        RootView rview = _view.getRootView();
        ViewEvent nevent = ViewEvent.createEvent(rview, anEvent, aType, null);
        rview.getWindow().dispatchEvent(nevent);
    }

    /**
     * Creates a window to represent interactive drag (if not DragImageSupported).
     */
    protected void createDragWindow()
    {
        // Get drag image and the source window (if source is component)
        Image img = getDragImage();
        Window sourceWindow = SwingUtils.getWindow(_dge.getComponent());

        // Create window for drag image
        _dragWindow = new JWindow(sourceWindow);
        _dragWindow.setSize((int)img.getWidth(), (int)img.getHeight());

        // Create label for drag image and add to window
        JLabel label = new JLabel(new ImageIcon((java.awt.Image)img.getNative()));
        _dragWindow.getContentPane().add(label);
    }

    /**
     * Returns the shared SwingClipboard.
     */
    public static SwingClipboard get()  { return _shared; }

    /**
     * Returns the shared SwingClipboard for drag and drop.
     */
    public static SwingClipboard getDrag(ViewEvent anEvent)
    {
        if (anEvent!=null)
            _sharedDrag.setEvent(anEvent);
        return _sharedDrag;
    }

    /** Sets the current event. */
    void setEvent(ViewEvent anEvent)
    {
        // If DragGesture, set View and DragGestureEvent
        if (anEvent.isDragGesture()) {
            _view = anEvent.getView();
            _dge = anEvent.getEvent(DragGestureEvent.class);
        }

        // If DragEvent, get transferable
        else if (anEvent.isDragEvent()) {
            DropTargetDragEvent dragEv = anEvent.getEvent(DropTargetDragEvent.class);
            if (dragEv!=null)
                _trans = dragEv.getTransferable();
            DropTargetDropEvent dropEv = anEvent.getEvent(DropTargetDropEvent.class);
            if (dropEv!=null)
                _trans = dropEv.getTransferable();
        }

        // Compain if passed a bogus event
        else System.err.println("SwingDragBoard.init: Invalid event type: " + anEvent);
    }

    /**
     * Transferable implementation to vend ClipboardData objects to/from Swing.
     */
    private class SnapTransferable implements Transferable {

        /** Returns the supported flavors. */
        public DataFlavor[] getTransferDataFlavors()
        {
            // Get list of DataFlavors from ClipboardDatas
            List <DataFlavor> flavors = new ArrayList();
            for (String mtype : getClipboardDatas().keySet()) {
                DataFlavor flavor = getDataFlavor(mtype);
                if (flavor!=null)
                    flavors.add(flavor);
            }

            // Return as array
            return flavors.toArray(new DataFlavor[0]);
        }

        /** Returns whether given flavor is supported. */
        public boolean isDataFlavorSupported(DataFlavor aFlavor)
        {
            String mtype = getMIMEType(aFlavor);
            return mtype!=null && getClipboardDatas().containsKey(mtype);
        }

        /** Returns an inputstream with clipboard data for requested flavor. */
        public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
        {
            // Get ClipboardData for flavor
            String mtype = getMIMEType(aFlavor);
            ClipboardData data = mtype!=null ? getClipboardDatas().get(mtype) : null;
            if (data==null)
                throw new UnsupportedFlavorException(aFlavor);

            // Handle String
            if (data.isString())
                return data.getString();

            // Handle File list
            if (data.isFileList())
                return data.getJavaFiles();

            // Handle image
            if (data.isImage()) {
                Image image = data.getImage();
                return image.getNative();
            }

            // Otherwise, return input stream
            return data.getInputStream();
        }
    }
}