package snap.javafx;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class StageHpr <T extends Stage> extends ViewHelper <T> {
    
/** Creates the native. */
protected T createNative()  { return (T)new Stage(); }

/** Sets the x value. */
public void setX(double aValue)  { get().setX(aValue); }

/** Sets the y value. */
public void setY(double aValue)  { get().setY(aValue); }

/** Sets the width value. */
public void setWidth(double aValue)  { get().setWidth(aValue); }

/** Sets the height value. */
public void setHeight(double aValue)  { get().setHeight(aValue); }

/** Override to get node as WindowView. */
public WindowView getView()  { return (WindowView)super.getView(); }
    
/**
 * Initialze native window.
 */
protected void init()
{
    // Get window and window node
    Stage win = get();
    WindowView node = getView();
    
    // Set style
    if(node.getType()==WindowView.TYPE_UTILITY)
        get().initStyle(StageStyle.UTILITY);
    else if(node.getType()==WindowView.TYPE_PLAIN)
        get().initStyle(StageStyle.UNDECORATED);
    else get().initStyle(StageStyle.DECORATED); // Don't need this

    // Install ContentPane
    RootView rpane = node.getRootView();
    SnapRoot.setContent(win, rpane);
    win.setOpacity(0); win.show(); win.hide(); win.setOpacity(1);     // Make visible to force window to resolve
    
    // Sync window with node
    win.xProperty().addListener(e -> boundsChanged()); //node.setX(win.getX())
    win.yProperty().addListener(e -> boundsChanged()); //node.setY(win.getY()
    win.widthProperty().addListener(e -> boundsChanged()); //node.setWidth(win.getWidth())
    win.heightProperty().addListener(e -> boundsChanged()); //node.setHeight(win.getHeight())
    win.showingProperty().addListener(e -> showingChanged());
    node.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());
    
    // Add listeners to send focus events and window events
    win.focusedProperty().addListener((obs,oval,nval) -> focusChanged());
    win.addEventFilter(WindowEvent.ANY, e -> sendEvent(e, null));
    
    // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
    win.setTitle(node.getTitle());
    win.setAlwaysOnTop(node.isAlwaysOnTop()); //setIconImage(getIconImage());
    win.setResizable(node.isResizable()); //if(_windowListener!=null) addWindowListener(_windowListener);
}

/**
 * Checks to see if window has been initialized.
 */
public void checkInit()  { if(!_initChecked) { _initChecked = true; init(); } } boolean _initChecked;

/**
 * Show window at given screen x and y.
 */
public void show(View aView, double aX, double aY)
{
    // On first show, configure window
    checkInit();
        
    // Get native window
    WindowView view = getView();
    Stage win = get();
    
    // Set window location
    win.setX(aX); win.setY(aY);
    
    // Set window visible
    if(view.isModal()) win.showAndWait();
    else win.show();
    
    // If window has frame save name, set listener
    if(view.getSaveName()!=null && view.getProp("FrameSaveListener")==null) {
        ChangeListener fsl = (a,b,c) -> view.saveFrame();
        win.xProperty().addListener(fsl);
        win.yProperty().addListener(fsl);
        win.widthProperty().addListener(fsl);
        win.heightProperty().addListener(fsl);
        view.setProp("FrameSaveListener", fsl);
    }
}

/**
 * Hides the window.
 */
public void hide()  { get().hide(); }

/**
 * Order window to front.
 */
public void toFront()  { get().toFront(); }

/**
 * Sets the window size to preferred size.
 */
public void setPrefSize()
{
    checkInit();
    get().sizeToScene();
}

/**
 * Called when window is shown/hidden.
 */
protected void showingChanged()
{
    WindowView node = getView(); boolean showing = get().isShowing();
    ViewUtils.setShowing(node, showing);
    if(showing) node.getRootView().repaint();
}
    
/**
 * Handles focus changed.
 */
protected void focusChanged()
{
    Stage win = get(); boolean focused = win.isFocused();
    WindowView node = getView();
    ViewUtils.setFocused(node, focused);
    ViewEvent.Type etype = focused? ViewEvent.Type.WinActivate : ViewEvent.Type.WinDeactivate;
    if(node.getEventAdapter().isEnabled(etype))
        sendEvent(null, etype);
}

/**
 * Handles bounds changed.
 */
protected void boundsChanged()
{
    Stage win = get();
    WindowView node = getView();
    RootView rpane = node.getRootView();
    javafx.scene.Node rpaneNtv = rpane.getNative(javafx.scene.Node.class);
    node.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());
    Scene scn = win.getScene(); double sx = scn.getX(), sy = scn.getY(), sw = scn.getWidth(), sh = scn.getHeight();
    rpane.setBounds(sx, sy, sw, sh);
}

/**
 * Called when Node changes to update native.
 */
public void propertyChange(PropChange aPC)
{
    super.propertyChange(aPC);
    String pname = aPC.getPropertyName();
    if(pname==WindowView.Image_Prop) setImage((Image)aPC.getNewValue());
    else if(pname==WindowView.Title_Prop) get().setTitle((String)aPC.getNewValue());
    else if(pname==WindowView.Resizable_Prop) get().setResizable(SnapUtils.booleanValue(aPC.getNewValue()));
}

/** 
 * Sets the image property of given object to given string.
 */
public void setImage(Image anImage)
{
    if(anImage!=null) get().getIcons().setAll(JFX.get(anImage));
    else get().getIcons().clear();
}

/**
 * Sends an event.
 */
private void sendEvent(Event anEvent, ViewEvent.Type aType)
{
    ViewEvent.Type type = aType!=null? aType : JFXEvent.getType(anEvent); if(type==null) return;
    View node = getView(); if(!node.getEventAdapter().isEnabled(type)) return;
    ViewEnv env = node.getEnv(); ViewEvent event = env.createEvent(node, anEvent, type, null);
    getView().fireEvent(event);
}

}