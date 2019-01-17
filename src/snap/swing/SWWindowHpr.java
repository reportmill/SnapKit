package snap.swing;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * A WindowHpr map snap Window functionality to Swing.
 */
public class SWWindowHpr <T extends Window> extends WindowHpr <T> {
    
    // The snap Window
    WindowView     _win;
    
    // The snap RootView
    RootView       _rview;
    
    // The native RootView
    SWRootView     _rviewNtv;
    
/**
 * Override to configure stuff for Swing.
 */
public void setWindow(WindowView aWin)
{
    // Do normal version
    _win = aWin;
    super.setWindow(aWin);
    
    // Set RootView var and create native RootView
    _rview = aWin.getRootView();
    _rviewNtv = new SWRootView(); _rviewNtv.setRootView(_rview);
    
    // Add listener to update bounds
    _win.addPropChangeListener(pc -> snapWindowPropertyChanged(pc));
}
    
/** Creates the native. */
protected T createNative()
{
    // Get Window type
    String type = _win.getType();
    
    // See if window has client/owner (was requested by/for view in another window)
    Window owner = getClientWindow(true);
    
    // Create frame or dialog
    if(type==WindowView.TYPE_MAIN) return (T)new JFrame();
    return (T)new JDialog(owner);
}

/**
 * Initialze native window.
 */
public void initWindow()
{
    // Get native, window view and root view
    Window win = get();
    WindowView wview = _win;
    
    // Configure JFrame: Title, Resizable
    if(win instanceof JFrame) { JFrame frame = (JFrame)win;
        frame.setTitle(wview.getTitle());
        frame.setResizable(wview.isResizable());
    }
    
    // Configure JDialog: Title, Resizable, Modal
    else if(win instanceof JDialog) { JDialog frame = (JDialog)win;
        frame.setTitle(wview.getTitle());
        frame.setModal(wview.isModal());
        frame.setResizable(wview.isResizable());
        if(wview.getType()==WindowView.TYPE_UTILITY)
            frame.getRootPane().putClientProperty("Window.style", "small");
        else if(wview.getType()==WindowView.TYPE_PLAIN) {
            frame.setUndecorated(true);
            frame.setFocusableWindowState(false);
        }
    }
    
    // Set common attributes
    win.setAlwaysOnTop(wview.isAlwaysOnTop());
    win.setOpacity((float)wview.getOpacity());

    // Install RootView Native as ContentPane
    RootPaneContainer rpc = (RootPaneContainer)win;
    rpc.setContentPane(_rviewNtv);
    
    // Size window to root view
    win.pack();
    win.setLocation((int)wview.getX(),(int)wview.getY());
    
    // Set WindowView insets
    java.awt.Insets insAWT = win.getInsets();
    Insets ins = new Insets(insAWT.top, insAWT.right, insAWT.bottom, insAWT.left);
    wview.setPadding(ins);

    // Add component listener to sync win bounds changes with WindowView/RootView
    win.addComponentListener(new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) { swingWindowBoundsChanged(); }
        public void componentResized(ComponentEvent e) { swingWindowBoundsChanged(); }
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
    swingWindowBoundsChanged();
}

/**
 * Show window at given screen x and y.
 */
public void show()
{
    // Get native window and window view
    Window win = get();
    WindowView wview = _win;
    
    // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
    //if(wview.isAlwaysOnTop()) win.setAlwaysOnTop(true);
    
    // Get window x & y (if client view not in WindowView, shift for real window)
    int x = (int)Math.round(wview.getX()), y = (int)Math.round(wview.getY());
    
    // If ClientView exists, but not in WindowView (i.e., CV.RootView was installed in Swing), convert point to screen)
    if(!isClientViewInWindowView()) {
        RootView rview = wview.getClientView().getRootView();
        WindowView rviewWin = rview.getWindow();
        SWWindowHpr rviewWinHpr = (SWWindowHpr)rviewWin.getHelper();
        JComponent rcomp = rviewWinHpr._rviewNtv;
        java.awt.Point pnt = new java.awt.Point(0,0); SwingUtilities.convertPointToScreen(pnt, rcomp);
        x += pnt.getX(); y += pnt.getY();
    }
    
    // Set window location, make visible and notify ShowingChanged
    win.setLocation(x,y);
    win.setVisible(true);
    showingChanged(); // So change is reflected immediately
    
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
    showingChanged(); // So change is reflected immediately
}

/**
 * Order window to front.
 */
public void toFront()
{
    get().toFront();
}

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
public void setDocURL(WebURL aURL)
{
    File file = aURL!=null? aURL.getJavaFile() : null;
    if(get() instanceof RootPaneContainer) { RootPaneContainer rpc = (RootPaneContainer)get();
        JRootPane rpane = rpc.getRootPane(); if(rpane==null) return;
        rpane.putClientProperty("Window.documentFile", file);
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
    Window win = get();
    boolean showing = get().isShowing();
    ViewUtils.setShowing(_win, showing);
}
    
/**
 * Handles when Swing window bounds changed.
 */
protected void swingWindowBoundsChanged()
{
    Window win = get();
    _win.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());

    // If window deactivated and it has Popup, hide popup
    if(_rview.getPopup()!=null)
        _rview.getPopup().hide();
}

/**
 * Registers a view for repaint.
 */
public void requestPaint(Rect aRect)  { _rviewNtv.repaint(aRect); }

/**
 * Handles active changed.
 */
protected void activeChanged()
{
    Window win = get(); boolean active = win.isActive();
    ViewUtils.setFocused(_win, active);
    ViewEvent.Type etype = active? ViewEvent.Type.WinActivate : ViewEvent.Type.WinDeactivate;
    if(_win.getEventAdapter().isEnabled(etype))
        sendWinEvent(null, etype);
    
    // If window deactivated and it has Popup, hide popup
    if(!active && _rview.getPopup()!=null)
        _rview.getPopup().hide();
}

/**
 * Called when WindowView changes to update native.
 */
public void snapWindowPropertyChanged(PropChange aPC)
{
    String pname = aPC.getPropertyName();
    if(pname==View.X_Prop) setX((Double)aPC.getNewValue());
    else if(pname==View.Y_Prop) setY((Double)aPC.getNewValue());
    else if(pname==View.Width_Prop) setWidth((Double)aPC.getNewValue());
    else if(pname==View.Height_Prop) setHeight((Double)aPC.getNewValue());
    else if(pname==WindowView.AlwaysOnTop_Prop) get().setAlwaysOnTop(SnapUtils.booleanValue(aPC.getNewValue()));
    else if(pname==WindowView.Image_Prop) setImage((Image)aPC.getNewValue());
    else if(pname==WindowView.Title_Prop) setTitle((String)aPC.getNewValue());
    else if(pname==WindowView.Resizable_Prop) setResizable(SnapUtils.booleanValue(aPC.getNewValue()));
}

/** Sets the x value. */
public void setX(double aValue)  { get().setLocation((int)aValue, get().getY()); }

/** Sets the y value. */
public void setY(double aValue)  { get().setLocation(get().getX(), (int)aValue); }

/** Sets the width value. */
public void setWidth(double aValue)  { get().setSize((int)aValue, get().getHeight()); }

/** Sets the height value. */
public void setHeight(double aValue)  { get().setSize(get().getWidth(), (int)aValue); }

/**
 * Sends the given event.
 */
protected void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
{
    if(!_win.getEventAdapter().isEnabled(aType)) return;
    ViewEvent event = _win.getEnv().createEvent(_win, anEvent, aType, null);
    _win.fireEvent(event);
    if(aType==ViewEvent.Type.WinClose && get() instanceof JFrame && event.isConsumed())
        ((JFrame)get()).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
}

/** Returns the ClientView.RootView.Window.Native, with option to try ClientView.RootView.Native.Window instead. */
public Window getClientWindow(boolean doReal)
{
    View cview = _win.getClientView();
    RootView rview = cview!=null? cview.getRootView() : null;
    WindowView wview = rview!=null && rview.isWindowSet()? rview.getWindow() : null;
    SWWindowHpr wviewHpr = wview!=null? (SWWindowHpr)wview.getHelper() : null;
    Window win = wviewHpr!=null? (Window)wviewHpr.get() : null;
    
    // If ClientView found, but not in window that is showing, get win from RootView.Native instead
    if(cview!=null && rview!=null && (wview==null || !wview.isShowing()) && doReal) {
        JComponent rcomp = wviewHpr._rviewNtv;
        win = SwingUtils.getParent(rcomp, Window.class);
    }
    
    // Return window
    return win;
}

/** Returns whether ClientView.RootView is in Snap WindowView or explicitly installed in Swing component hierarch. */
public boolean isClientViewInWindowView()  { return getClientWindow(true)==getClientWindow(false); }

/** A component listener to save frame to preferences on move. */
private static class FrameSaveListener extends ComponentAdapter {
    public FrameSaveListener(WindowView aWN)  { _wview = aWN; } WindowView _wview;
    public void componentMoved(ComponentEvent e)  { setFrameString(e); }
    public void componentResized(ComponentEvent e)  { setFrameString(e); }
    private void setFrameString(ComponentEvent e)  { _wview.saveFrame(); }
}

}