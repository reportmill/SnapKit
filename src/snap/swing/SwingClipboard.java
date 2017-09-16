package snap.swing;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.util.FileUtils;
import snap.util.SnapUtils;
import snap.view.*;
import snap.view.Clipboard;

/**
 * A Clipboard implementation for Swing.
 */
public class SwingClipboard extends Clipboard implements DragSourceListener, DragSourceMotionListener {
    
    // The transferable
    Transferable        _trans;
    
    // The view to initiate drag
    View                _view;
    
    // The DragGestureEvent
    DragGestureEvent    _dge;
    
    // A window that is optionally used to simulate image dragging.
    JWindow             _dragWindow;

    // Image flavor
    static DataFlavor   _imageFlavor = new DataFlavor("text/image", "Snap Image");
    static DataFlavor   _jpegFlavor = new DataFlavor("image/jpeg", "JPEG Image Data");
    
    // The shared clipboards for system and drag
    static SwingClipboard  _shared = new SwingClipboard();
    static SwingClipboard  _sharedDrag = new SwingClipboard();

/**
 * Sets the current event.
 */
public void setEvent(ViewEvent anEvent)
{
    // If DragGesture, set View and DragGestureEvent
    if(anEvent.isDragGesture()) {
        _view = anEvent.getView();
        _dge = anEvent.getEvent(DragGestureEvent.class);
    }
    
    // If DragEvent, get transferable
    else if(anEvent.isDragEvent()) {
        DropTargetDragEvent dragEv = anEvent.getEvent(DropTargetDragEvent.class);
        if(dragEv!=null) _trans = dragEv.getTransferable();
        DropTargetDropEvent dropEv = anEvent.getEvent(DropTargetDropEvent.class);
        if(dropEv!=null) _trans = dropEv.getTransferable();
    }
    
    // Compain if passed a bogus event
    else System.err.println("SwingDragBoard.init: Invalid event type: " + anEvent);
}

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
    // Handle STRING
    if(aName.equals(STRING))
        return getString();
        
    // Handle FILES
    if(aName.equals(FILES))
        return getFiles();
        
    // Handle anything else
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
 * Returns a string from given transferable.
 */
public String getString()
{
    // Get Transferable
    Transferable aTrans = getTrans();
    
    // Handle StringFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.stringFlavor))
        try { return (String)aTrans.getTransferData(DataFlavor.stringFlavor); }
        catch(Exception e) { e.printStackTrace(); return null; }
    
    // Handle FileList
    List <File> files = getJavaFiles();
    if(files!=null && files.size()>0)
        return files.get(0).getAbsolutePath();
    
    // Otherwise return null
    return null;
}

/**
 * Returns a list of files from a given transferable.
 */
public List <ClipboardFile> getFiles()
{
    // Return as ClipboardFiles
    List <File> jfiles = getJavaFiles(); if(jfiles==null) return null;
    List <ClipboardFile> cfiles = new ArrayList(jfiles.size());
    for(File file : jfiles) cfiles.add(new ClipboardFile(file));
    return cfiles;
}

/**
 * Returns a list of files from a given transferable.
 */
public List <File> getJavaFiles()
{
    Transferable trans = getTrans();
    if(trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        try { return (List)trans.getTransferData(DataFlavor.javaFileListFlavor); }
        catch(Exception e) { System.err.println(e); }
    return null;
}

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
 * Starts the drag.
 */
public void startDrag()
{
    // Get DragSource and start Listening to drag events drag source
    DragSource dragSource = _dge.getDragSource();
    dragSource.removeDragSourceListener(this); dragSource.removeDragSourceMotionListener(this);
    dragSource.addDragSourceListener(this); dragSource.addDragSourceMotionListener(this);
    
    // Check to see if image drag is supported by system. If not (ie, Windows), simulate image dragging with a window.
    if(getDragImage()!=null && !DragSource.isDragImageSupported())
        createDragWindow();

    // Get drag image and point (as AWT img/pnt)
    java.awt.Image img = getDragImage()!=null? (java.awt.Image)getDragImage().getNative() : null;
    double dx = img!=null? getDragImageOffset().x : 0;
    double dy = img!=null? getDragImageOffset().y : 0; if(SnapUtils.isMac) { dx = -dx; dy = -dy; } // Mac is flipped?
    java.awt.Point pnt = img!=null? new java.awt.Point((int)dx, (int)dy) : null;
    
    // Start drag
    Transferable trans = getTrans();
    dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, img, pnt, trans, null);
}

/** DragSourceListener methods. */
public void dragEnter(DragSourceDragEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnter); }
public void dragOver(DragSourceDragEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceOver); }
public void dropActionChanged(DragSourceDragEvent anEvent)  { }
public void dragExit(DragSourceEvent anEvent)  { dispatchToRootView(anEvent, ViewEvent.Type.DragSourceExit); }

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
    
    // Dispatch DragSourceEnd event
    dispatchToRootView(anEvent, ViewEvent.Type.DragSourceEnd);
    
    // Stop listening to DragSource events.
    DragSource dragSource = _dge.getDragSource();
    dragSource.removeDragSourceListener(this); dragSource.removeDragSourceMotionListener(this);
    _view = null; _dge = null;
}


/**
 * Sends an event to RootView.
 */
private void dispatchToRootView(Object anEvent, ViewEvent.Type aType)
{
    RootView rview = _view.getRootView();
    ViewEvent nevent = SwingViewEnv.get().createEvent(rview, anEvent, aType, null);
    rview.dispatchEvent(nevent);
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
    _dragWindow.getContentPane().add(new JLabel(new ImageIcon((java.awt.Image)img.getNative())));
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
    if(anEvent!=null) _sharedDrag.setEvent(anEvent);
    return _sharedDrag;
}

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
                _types.add(DataFlavor.javaFileListFlavor); _contents.add(theContents[i-1]);
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
        // Get contents for requested flavor
        Object contents = null;
        for(int i=0;i<_types.size() && contents==null;i++)
            if(aFlavor.equals(_types.get(i)))
                contents = _contents.get(i);
                
        // Handle ImageFlavor:
        if(aFlavor==DataFlavor.imageFlavor && contents instanceof Image)
            contents = ((Image)contents).getNative();
            
        // Handle JavaFileListFlavor + Image: Write to file and swap in for image
        if(aFlavor==DataFlavor.javaFileListFlavor && contents instanceof Image) { Image img = (Image)contents;
            byte bytes[] = img.getBytes()!=null? img.getBytes() : img.hasAlpha()? img.getBytesPNG():img.getBytesJPEG();
            File file = FileUtils.getTempFile(getImageName(img)); file.deleteOnExit();
            SnapUtils.writeBytes(bytes, file);
            contents = Arrays.asList(file);
        }
            
        // Handle JPEG Flavor
        if(aFlavor==_jpegFlavor && contents instanceof Image)
            contents = ((Image)contents).getBytesJPEG();
            
        // Handle content is bytes: Convert to ByteArrayInputStream
        if(contents instanceof byte[])
            contents = new ByteArrayInputStream((byte[])contents);
            
        // Handle no contents
        if(contents==null)
            throw new UnsupportedFlavorException(aFlavor);
        return contents;
    }
}

/**
 * Returns an image name.
 */
private String getImageName(Image anImage)
{
    if(anImage.getSourceURL()!=null)
        return anImage.getName();
    return "DragImage-" + (_dragCount++) + "." + anImage.getType();
} static int _dragCount;

}