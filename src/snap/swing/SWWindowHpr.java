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
public class SWWindowHpr extends WindowView.WindowHpr <Window> {
    
    // The snap Window
    WindowView        _win;
    
    // The Swing Window
    Window            _winNtv;
    
    // The snap RootView
    RootView          _rview;
    
    // The native RootView
    SWRootView        _rviewNtv;
    
    // The current cursor
    snap.view.Cursor  _cursor;
    
    // Runnable to update cursor
    Runnable          _cursRun, _cursRunShared = () -> { _rviewNtv.setCursor(AWT.get(_cursor)); _cursRun = null; };
    
/**
 * Returns the snap Window.
 */
public WindowView getWindow()  { return _win; }

/**
 * Sets the snap Window Swing.
 */
public void setWindow(WindowView aWin)
{
    // Set window
    _win = aWin;
    
    // Create native
    _winNtv = getNative();
    
    // Set RootView var and create native RootView
    _rview = aWin.getRootView();
    _rviewNtv = new SWRootView(); _rviewNtv.setRootView(_rview);
    
    // Add listener to update bounds
    _win.addPropChangeListener(pc -> snapWindowPropertyChanged(pc));
    
    // Add listener to update native cursor
    _win.addPropChangeListener(pc -> snapWindowActiveCursorChanged(), WindowView.ActiveCursor_Prop);
}
    
/**
 * Returns the native being helped.
 */
public Window getNative()
{
    // If already set, just return
    if(_winNtv!=null) return _winNtv;
    
    // Get Window type
    String type = _win.getType();
    
    // See if window has client/owner (was requested by/for view in another window)
    Window owner = getClientWindow(true);
    
    // Create frame or dialog
    if(type==WindowView.TYPE_MAIN) return new JFrame();
    return new JDialog(owner);
}

/**
 * Returns the native for the window content.
 */
public JComponent getContentNative()  { return _rviewNtv; }

/**
 * Initialze native window.
 */
public void initWindow()
{
    // Get native, window view and root view
    WindowView win = _win;
    Window winNtv = _winNtv;
    
    // Configure JFrame: Title, Resizable
    if(winNtv instanceof JFrame) { JFrame frame = (JFrame)winNtv;
        frame.setTitle(win.getTitle());
        frame.setResizable(win.isResizable());
    }
    
    // Configure JDialog: Title, Resizable, Modal
    else if(winNtv instanceof JDialog) { JDialog frame = (JDialog)winNtv;
        frame.setTitle(win.getTitle());
        frame.setModal(win.isModal());
        frame.setResizable(win.isResizable());
        if(win.getType()==WindowView.TYPE_UTILITY)
            frame.getRootPane().putClientProperty("Window.style", "small");
        else if(win.getType()==WindowView.TYPE_PLAIN) {
            frame.setUndecorated(true);
            frame.setFocusableWindowState(false);
        }
    }
    
    // Set common attributes
    winNtv.setAlwaysOnTop(win.isAlwaysOnTop());
    winNtv.setOpacity((float)win.getOpacity());

    // Install RootView Native as ContentPane
    RootPaneContainer rpc = (RootPaneContainer)winNtv;
    rpc.setContentPane(_rviewNtv);
    
    // Size window to root view
    winNtv.pack();
    winNtv.setLocation((int)win.getX(),(int)win.getY());
    
    // Set WindowView insets
    java.awt.Insets insAWT = winNtv.getInsets();
    Insets ins = new Insets(insAWT.top, insAWT.right, insAWT.bottom, insAWT.left);
    win.setPadding(ins);

    // Add component listener to sync win bounds changes with WindowView/RootView
    winNtv.addComponentListener(new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) { swingWindowBoundsChanged(); }
        public void componentResized(ComponentEvent e) { swingWindowBoundsChanged(); }
        public void componentShown(ComponentEvent e) { swingWindowShowingChanged(); }
        public void componentHidden(ComponentEvent e) { swingWindowShowingChanged(); }
    });
    
    // Add WindowListener to dispatch window events
    winNtv.addWindowListener(new WindowAdapter() {
        public void windowActivated(WindowEvent anEvent)  { swingWindowActiveChanged(); }
        public void windowDeactivated(WindowEvent anEvent)  { swingWindowActiveChanged(); }
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
    WindowView win = _win;
    Window winNtv = _winNtv;
    
    // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
    //if(wview.isAlwaysOnTop()) win.setAlwaysOnTop(true);
    
    // Get window x & y (if client view not in WindowView, shift for real window)
    int x = (int)Math.round(win.getX()), y = (int)Math.round(win.getY());
    
    // If ClientView exists, but not in WindowView (i.e., CV.RootView was installed in Swing), convert point to screen)
    if(!isClientViewInWindowView()) {
        RootView rview = win.getClientView().getRootView();
        WindowView rviewWin = rview.getWindow();
        SWWindowHpr rviewWinHpr = (SWWindowHpr)rviewWin.getHelper();
        JComponent rcomp = rviewWinHpr._rviewNtv;
        java.awt.Point pnt = new java.awt.Point(0,0); SwingUtilities.convertPointToScreen(pnt, rcomp);
        x += pnt.getX(); y += pnt.getY();
    }
    
    // Set window location, make visible and notify ShowingChanged
    winNtv.setLocation(x,y);
    winNtv.setVisible(true);
    swingWindowShowingChanged(); // So change is reflected immediately
    
    // If window is modal, just return
    if(win.isModal())
        return;
    
    // If window has frame save name, set listener
    if(win.getSaveName()!=null && win.getProp("FrameSaveListener")==null) {
        FrameSaveListener fsl = new FrameSaveListener(win);
        win.setProp("FrameSaveListener", fsl);
        winNtv.addComponentListener(fsl);
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
    _winNtv.setVisible(false);
    swingWindowShowingChanged(); // So change is reflected immediately
}

/**
 * Order window to front.
 */
public void toFront()  { _winNtv.toFront(); }

/**
 * Sets the title of the window.
 */
public void setTitle(String aTitle)
{
    if(_winNtv instanceof JFrame) ((JFrame)_winNtv).setTitle(aTitle);
    else if(_winNtv instanceof JDialog) ((JDialog)_winNtv).setTitle(aTitle);
    else System.err.println("WindowHpr.setTitle: Not supported " + _winNtv);
}

/**
 * Sets the title of the window.
 */
public void setResizable(boolean aValue)
{
    if(_winNtv instanceof JFrame) ((JFrame)_winNtv).setResizable(aValue);
    else if(_winNtv instanceof JDialog) ((JDialog)_winNtv).setResizable(aValue);
    else System.err.println("WindowHpr.setTitle: Not supported " + _winNtv);
}

/**
 * Sets the document file for the window title bar proxy icon.
 */
public void setDocURL(WebURL aURL)
{
    File file = aURL!=null? aURL.getJavaFile() : null;
    if(_winNtv instanceof RootPaneContainer) { RootPaneContainer rpc = (RootPaneContainer)_winNtv;
        JRootPane rpane = rpc.getRootPane(); if(rpane==null) return;
        rpane.putClientProperty("Window.documentFile", file);
    }
}

/** 
 * Sets the image property of given object to given string.
 */
public void setImage(Image anImage)  { _winNtv.setIconImage(AWT.get(anImage)); }

/**
 * Called when window is shown/hidden.
 */
protected void swingWindowShowingChanged()
{
    boolean showing = _winNtv.isShowing();
    ViewUtils.setShowing(_win, showing);
}
    
/**
 * Handles when Swing window bounds changed.
 */
protected void swingWindowBoundsChanged()
{
    _win.setBounds(_winNtv.getX(), _winNtv.getY(), _winNtv.getWidth(), _winNtv.getHeight());

    // If window deactivated and it has Popup, hide popup
    if(_win.getPopup()!=null)
        _win.getPopup().hide();
}

/**
 * Registers a view for repaint.
 */
public void requestPaint(Rect aRect)  { _rviewNtv.repaint(aRect); }

/**
 * Handles active changed.
 */
protected void swingWindowActiveChanged()
{
    boolean active = _winNtv.isActive();
    ViewUtils.setFocused(_win, active);
    ViewEvent.Type etype = active? ViewEvent.Type.WinActivate : ViewEvent.Type.WinDeactivate;
    if(_win.getEventAdapter().isEnabled(etype))
        sendWinEvent(null, etype);
    
    // If window deactivated and it has Popup, hide popup
    if(!active && _win.getPopup()!=null)
        _win.getPopup().hide();
}

/**
 * Called when WindowView changes to update native.
 */
protected void snapWindowPropertyChanged(PropChange aPC)
{
    String pname = aPC.getPropertyName();
    if(pname==View.X_Prop) setX((Double)aPC.getNewValue());
    else if(pname==View.Y_Prop) setY((Double)aPC.getNewValue());
    else if(pname==View.Width_Prop) setWidth((Double)aPC.getNewValue());
    else if(pname==View.Height_Prop) setHeight((Double)aPC.getNewValue());
    else if(pname==WindowView.AlwaysOnTop_Prop) _winNtv.setAlwaysOnTop(SnapUtils.booleanValue(aPC.getNewValue()));
    else if(pname==WindowView.Image_Prop) setImage((Image)aPC.getNewValue());
    else if(pname==WindowView.Title_Prop) setTitle((String)aPC.getNewValue());
    else if(pname==WindowView.Resizable_Prop) setResizable(SnapUtils.booleanValue(aPC.getNewValue()));
}

/** Sets bounds values. */
private void setX(double aValue)  { _winNtv.setLocation((int)aValue, _winNtv.getY()); }
private void setY(double aValue)  { _winNtv.setLocation(_winNtv.getX(), (int)aValue); }
private void setWidth(double aValue)  { _winNtv.setSize((int)aValue, _winNtv.getHeight()); }
private void setHeight(double aValue)  { _winNtv.setSize(_winNtv.getWidth(), (int)aValue); }

/**
 * Called when RootView CurrentCursor changes.
 */
void snapWindowActiveCursorChanged()
{
    _cursor = _win.getActiveCursor();
    if(_cursRun==null) SwingUtilities.invokeLater(_cursRun = _cursRunShared);
}
    
/**
 * Sends the given event.
 */
protected void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
{
    if(!_win.getEventAdapter().isEnabled(aType)) return;
    ViewEvent event = _win.getEnv().createEvent(_win, anEvent, aType, null);
    _win.fireEvent(event);
    if(aType==ViewEvent.Type.WinClose && _winNtv instanceof JFrame && event.isConsumed())
        ((JFrame)_winNtv).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
}

/** Returns the ClientView.RootView.Window.Native, with option to try ClientView.RootView.Native.Window instead. */
private Window getClientWindow(boolean doReal)
{
    View cview = _win.getClientView();
    RootView rview = cview!=null? cview.getRootView() : null;
    WindowView win = rview!=null && rview.isWindowSet()? rview.getWindow() : null;
    SWWindowHpr winHpr = win!=null? (SWWindowHpr)win.getHelper() : null;
    Window winNtv = winHpr!=null? winHpr._winNtv : null;
    
    // If ClientView found, but not in window that is showing, get win from RootView.Native instead
    if(cview!=null && rview!=null && (win==null || !win.isShowing()) && doReal) {
        JComponent rcomp = winHpr._rviewNtv;
        winNtv = SwingUtils.getParent(rcomp, Window.class);
    }
    
    // Return window
    return winNtv;
}

/** Returns whether ClientView.RootView is in Snap WindowView or explicitly installed in Swing component hierarch. */
private boolean isClientViewInWindowView()  { return getClientWindow(true)==getClientWindow(false); }

/** A component listener to save frame to preferences on move. */
private static class FrameSaveListener extends ComponentAdapter {
    public FrameSaveListener(WindowView aWN)  { _wview = aWN; } WindowView _wview;
    public void componentMoved(ComponentEvent e)  { setFrameString(e); }
    public void componentResized(ComponentEvent e)  { setFrameString(e); }
    private void setFrameString(ComponentEvent e)  { _wview.saveFrame(); }
}

}