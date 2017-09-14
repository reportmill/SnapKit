package snap.javafx;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import snap.view.*;

/**
 * A ViewHelper for PopupWindow/JFX-PopupWindow.
 */
public class JFXPopupWindowHpr <T extends PopupWindow> extends ViewHelper <T> {

/** Creates the native. */
protected T createNative()  { return (T)new PopupControl(); }

/** Sets the x value. */
public void setX(double aValue)  { get().setX(aValue); }

/** Sets the y value. */
public void setY(double aValue)  { get().setY(aValue); }

/** Sets the width value. */
public void setWidth(double aValue)  { get().setWidth(aValue); }

/** Sets the height value. */
public void setHeight(double aValue)  { get().setHeight(aValue); }

/** Override to return node as PopupWindow. */
public snap.view.PopupWindow getView()  { return (snap.view.PopupWindow)super.getView(); }

/**
 * Ensure window has content.
 */
public void initWindow()
{
    // Get native, node and root pane
    PopupWindow win = get();
    snap.view.PopupWindow node = getView();
    RootView rview = node.getRootView();
    
    // Set content
    SnapRoot.setContent(win, rview);
    win.setOpacity(0); win.show(null); win.hide(); win.setOpacity(1);     // Make visible to force window to resolve
    
    // Sync window with node
    win.xProperty().addListener(e -> node.setX(win.getX()));
    win.yProperty().addListener(e -> node.setY(win.getY()));
    win.widthProperty().addListener(e -> node.setWidth(win.getWidth()));
    win.heightProperty().addListener(e -> node.setHeight(win.getHeight()));
    win.showingProperty().addListener(e -> showingChanged());
    node.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());
    
    // Add listeners to send focus events and window events
    win.focusedProperty().addListener((obs,oval,nval) -> focusChanged());
    win.addEventFilter(WindowEvent.ANY, e -> sendEvent(e, null));
}
    
/**
 * Shows the popup at given point relative to given node.
 */
public void show()
{
    // Configure popup node
    get().setAutoHide(true);
    get().setAutoFix(false);
    
    // Get PopupNode and RootView
    snap.view.PopupWindow pnode = getView();
    RootView rview = pnode.getRootView();
    
    // Set popup window SnapRoot content to RootView, and add drop shadow
    SnapRoot sroot = SnapRoot.get(get());
    sroot.setContent(rview);
    sroot.setEffect(new DropShadow(18,0,3,Color.DARKGRAY));
    
    // Show window
    //javafx.scene.Node node = aView.getNative(javafx.scene.Node.class);
    //Bounds bounds = node.localToScreen(node.getBoundsInLocal());
    //double sx = bounds.getMinX() + x, sy = bounds.getMinY() + y; get().show(node, sx, sy);
    //node.addEventFilter(MouseEvent.MOUSE_PRESSED, _realAutoHide);
    
    get().show((javafx.scene.Node)null, pnode.getX(), pnode.getY());
    javafx.scene.Node node = rview.getNative(javafx.scene.Node.class);
    node.addEventFilter(MouseEvent.MOUSE_PRESSED, _realAutoHide);
}

/**
 * Hides the popup.
 */
public void hide()  { get().hide(); }
    
// Bogus call to do AutoHide on clicks in source node
EventHandler <MouseEvent> _realAutoHide = e -> realAutoHide((Node)e.getSource());
private void realAutoHide(Node aNode)
{
    aNode.removeEventFilter(MouseEvent.MOUSE_PRESSED, _realAutoHide);
    get().hide();
}

/**
 * Called when window is shown/hidden.
 */
protected void showingChanged()
{
    PopupWindow win = get(); snap.view.PopupWindow node = getView();
    boolean showing = get().isShowing();
    ViewUtils.setShowing(node, showing);
    if(showing) node.getRootView().repaint();
}
    
/**
 * Handles focus changed.
 */
protected void focusChanged()
{
    PopupWindow win = get(); boolean focused = win.isFocused();
    snap.view.PopupWindow node = getView();
    ViewUtils.setFocused(node, focused);
    ViewEvent.Type etype = focused? ViewEvent.Type.WinActivate : ViewEvent.Type.WinDeactivate;
    if(node.getEventAdapter().isEnabled(etype))
        sendEvent(null, etype);
}

/**
 * Sends an event.
 */
private void sendEvent(Event anEvent, ViewEvent.Type aType)
{
    ViewEvent.Type type = aType!=null? aType : JFXEvent.getType(anEvent); if(type==null) return;
    snap.view.PopupWindow node = getView(); if(!node.getEventAdapter().isEnabled(type)) return;
    ViewEnv env = node.getEnv(); ViewEvent event = env.createEvent(node, anEvent, type, null);
    getView().fireEvent(event);
}

}