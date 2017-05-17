package snap.swing;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A ViewHelper for WindowView/Swing-Window.
 */
public class SWWindowHpr <T extends Window> extends ViewHelper <T> {
    
/** Creates the native. */
protected T createNative()
{
    if(getView(WindowView.class).getType().equals(WindowView.TYPE_MAIN)) return (T)new JFrame();
    return (T)new JDialog();
}

/** Sets the x value. */
public void setX(double aValue)  { get().setLocation((int)aValue, get().getY()); }

/** Sets the y value. */
public void setY(double aValue)  { get().setLocation(get().getX(), (int)aValue); }

/** Sets the width value. */
public void setWidth(double aValue)  { get().setSize((int)aValue, get().getHeight()); }

/** Sets the height value. */
public void setHeight(double aValue)  { get().setSize(get().getWidth(), (int)aValue); }

/** Override to get view as WindowView. */
public WindowView getView()  { return (WindowView)super.getView(); }
    
/**
 * Configure component.
 */
protected void init()
{
    // Get native, window view and root view
    Window win = get();
    WindowView wview = getView();
    
    // Configure JFrame
    if(win instanceof JFrame) { JFrame frame = (JFrame)get();
        
        // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
        frame.setTitle(wview.getTitle());
        frame.setAlwaysOnTop(wview.isAlwaysOnTop()); //setIconImage(getIconImage());
        frame.setResizable(wview.isResizable());
    }
    
    // Configure JDialog
    else if(win instanceof JDialog) { JDialog frame = (JDialog)get();
    
        // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
        frame.setTitle(wview.getTitle());
        frame.setAlwaysOnTop(wview.isAlwaysOnTop());
        frame.setModal(wview.isModal());
        frame.setResizable(wview.isResizable());
        if(wview.getType()==WindowView.TYPE_UTILITY)
            frame.getRootPane().putClientProperty("Window.style", "small");
        else if(wview.getType()==WindowView.TYPE_PLAIN)
            frame.setUndecorated(true);
    }
    
    // Install RootView Native as ContentPane
    RootView rview = wview.getRootView();
    JComponent rviewNtv = rview.getNative(JComponent.class);
    RootPaneContainer rpc = (RootPaneContainer)win;
    rpc.setContentPane(rviewNtv);
    
    // Size window to root view
    win.pack();
    
    // Set WindowView insets
    java.awt.Insets insAWT = get().getInsets();
    Insets ins = new Insets(insAWT.top, insAWT.right, insAWT.bottom, insAWT.left);
    wview.setPadding(ins);

    // Add component listener to sync win bounds changes with WindowView/RootView
    win.addComponentListener(new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) { boundsChanged(); }
        public void componentResized(ComponentEvent e) { boundsChanged(); }
        public void componentShown(ComponentEvent e) { showingChanged(); }
        public void componentHidden(ComponentEvent e) { showingChanged(); }
    });
    
    // Add WindowListener to dispatch window events
    win.addWindowListener(new WindowAdapter() {
        public void windowActivated(WindowEvent anEvent)  { activeChanged(); }
        public void windowDeactivated(WindowEvent anEvent)  { activeChanged(); }
        public void windowOpened(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinOpen); }
        public void windowClosing(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinClose); }
        public void windowClosed(WindowEvent anEvent)  { }
    });
    
    // Sync Window bounds to WindowView
    boundsChanged();
}

/**
 * Checks whether window has been initialized.
 */
public void checkInit()  { if(!_initChecked) { _initChecked = true; init(); } } boolean _initChecked;

/**
 * Show window at given screen x and y.
 */
public void show(View aView, double aSX, double aSY)
{
    // On first show, configure window
    checkInit();
        
    // Get native window and window view
    Window win = get();
    WindowView wview = getView();
    
    // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
    //if(wview.isAlwaysOnTop()) win.setAlwaysOnTop(true);

    // Set window visible
    win.setLocation((int)Math.round(aSX),(int)Math.round(aSY));
    win.setVisible(true);
    
    // If window is modal, just return
    if(wview.isModal())
        return;
    
    // If window has frame save name, set listener
    if(wview.getSaveName()!=null && wview.getProp("FrameSaveListener")==null) {
        FrameSaveListener fsl = new FrameSaveListener(wview);
        wview.setProp("FrameSaveListener", fsl);
        win.addComponentListener(fsl);
    }
    
    // If window is always-on-top or does hide-on-deactivate, add listener to handle app deactivate stuff
    //if(wview.isAlwaysOnTop() || wview.isHideOnDeactivate()) {
        //if(_actvAdptr==null) _actvAdptr = new ActivationAdapter(win,wview.isHideOnDeactivate()));
        //_actvAdptr.setEnabled(true); }
}

/**
 * Hides the window.
 */
public void hide()
{
    get().setVisible(false);
    showingChanged();
}

/**
 * Order window to front.
 */
public void toFront()  { get().toFront(); }

/**
 * Sets the window size to preferred size.
 */
public void setPrefSize()  { get().pack(); }

/**
 * Sets the title of the window.
 */
public void setTitle(String aTitle)
{
    Window win = get();
    if(win instanceof JFrame)
        ((JFrame)win).setTitle(aTitle);
    else if(win instanceof JDialog)
        ((JDialog)win).setTitle(aTitle);
    else System.err.println("WindowHpr.setTitle: Not supported " + win.getClass());
}

/**
 * Sets the title of the window.
 */
public void setResizable(boolean aValue)
{
    Window win = get();
    if(win instanceof JFrame) ((JFrame)win).setResizable(aValue);
    else if(win instanceof JDialog) ((JDialog)win).setResizable(aValue);
    else System.err.println("WindowHpr.setTitle: Not supported " + win.getClass());
}

/**
 * Sets the document file for the window title bar proxy icon.
 */
public void setDocFile(File aFile)
{
    if(get() instanceof RootPaneContainer) { RootPaneContainer rpc = (RootPaneContainer)get();
        JRootPane rpane = rpc.getRootPane(); if(rpane==null) return;
        rpane.putClientProperty("Window.documentFile", aFile);
    }
}

/** 
 * Sets the image property of given object to given string.
 */
public void setImage(Image anImage)  { get().setIconImage(AWT.get(anImage)); }

/**
 * Called when window is shown/hidden.
 */
protected void showingChanged()
{
    Window win = get(); WindowView wview = getView();
    boolean showing = get().isShowing();
    ViewUtils.setShowing(wview, showing);
}
    
/**
 * Handles bounds changed.
 */
protected void boundsChanged()
{
    Window win = get(); WindowView wview = getView();
    wview.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());
}

/**
 * Handles active changed.
 */
protected void activeChanged()
{
    Window win = get(); boolean active = win.isActive();
    WindowView wview = getView();
    ViewUtils.setFocused(wview, active);
    ViewEvent.Type etype = active? ViewEvent.Type.WinActivate : ViewEvent.Type.WinDeactivate;
    if(wview.getEventAdapter().isEnabled(etype))
        sendWinEvent(null, etype);
}

/**
 * Called when WindowView changes to update native.
 */
public void propertyChange(PropChange aPC)
{
    super.propertyChange(aPC);
    String pname = aPC.getPropertyName();
    if(pname==WindowView.AlwaysOnTop_Prop) get().setAlwaysOnTop(SnapUtils.booleanValue(aPC.getNewValue()));
    else if(pname==WindowView.Image_Prop) setImage((Image)aPC.getNewValue());
    else if(pname==WindowView.Title_Prop) setTitle((String)aPC.getNewValue());
    else if(pname==WindowView.Resizable_Prop) setResizable(SnapUtils.booleanValue(aPC.getNewValue()));
}

/**
 * Sends the given event.
 */
protected void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
{
    View wview = getView(); if(!wview.getEventAdapter().isEnabled(aType)) return;
    ViewEvent event = wview.getEnv().createEvent(wview, anEvent, aType, null);
    wview.fireEvent(event);
    if(aType==ViewEvent.Type.WinClose && get() instanceof JFrame && event.isConsumed())
        ((JFrame)get()).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
}

/** A component listener to save frame to preferences on move. */
private static class FrameSaveListener extends ComponentAdapter {
    public FrameSaveListener(WindowView aWN)  { _wview = aWN; } WindowView _wview;
    public void componentMoved(ComponentEvent e)  { setFrameString(e); }
    public void componentResized(ComponentEvent e)  { setFrameString(e); }
    private void setFrameString(ComponentEvent e)  { _wview.saveFrame(); }
}

}