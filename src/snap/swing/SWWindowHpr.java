package snap.swing;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * A WindowHpr map snap Window functionality to Swing.
 */
public class SWWindowHpr extends WindowView.WindowHpr {

    // The snap Window
    private WindowView _win;

    // The Swing Window
    private Window _winNtv;

    // The snap RootView
    private RootView _rview;

    // The native RootView
    private SWRootView _rviewNtv;

    // The listener for catching Swing window bounds changes
    private ComponentAdapter _ntvWinBndsLsnr;

    // The current cursor
    private snap.view.Cursor _cursor;

    // Runnable to update cursor
    private Runnable _setRootViewNativeCursorRun;

    /**
     * Returns the snap Window.
     */
    @Override
    public WindowView getWindow()  { return _win; }

    /**
     * Sets the snap Window Swing.
     */
    @Override
    public void setWindow(WindowView aWin)
    {
        // Set window
        _win = aWin;

        // Create native
        _winNtv = getNative();

        // Set RootView var and create native RootView
        _rview = aWin.getRootView();
        _rviewNtv = new SWRootView(_win, _rview);

        // Add listener to handle window prop changes
        _win.addPropChangeListener(this::handleSnapWindowPropertyChange);
    }

    /**
     * Returns the native being helped.
     */
    @Override
    public Window getNative()
    {
        // If already set, just return
        if (_winNtv != null) return _winNtv;

        // Get Window type
        String type = _win.getType();

        // If standard TYPE_MAIN window, just return new JFrame
        if (type == WindowView.TYPE_MAIN)
            return new JFrame();

        // Get window client/owner (if requested by/for view in another window)
        Window owner = getClientWindow();
        return new JDialog(owner);
    }

    /**
     * Returns the native for the window content.
     */
    @Override
    public JComponent getContentNative()  { return _rviewNtv; }

    /**
     * Initialize native window.
     */
    @Override
    public void initWindow()
    {
        // Get native, window view and root view
        WindowView win = _win;
        Window winNtv = _winNtv;

        // Configure JFrame: Title, Resizable
        if (winNtv instanceof JFrame) {
            JFrame frame = (JFrame) winNtv;
            frame.setTitle(win.getTitle());
            frame.setResizable(win.isResizable());
        }

        // Configure JDialog: Title, Resizable, Modal
        else if (winNtv instanceof JDialog) {
            JDialog frame = (JDialog) winNtv;
            frame.setTitle(win.getTitle());
            frame.setModal(win.isModal());
            frame.setResizable(win.isResizable());
            if (win.getType() == WindowView.TYPE_UTILITY)
                frame.getRootPane().putClientProperty("Window.style", "small");
            else if (win.getType() == WindowView.TYPE_PLAIN)
                frame.setUndecorated(true);
        }

        // Set common attributes
        winNtv.setFocusableWindowState(win.isFocusable());
        winNtv.setAlwaysOnTop(win.isAlwaysOnTop());
        winNtv.setOpacity((float) win.getOpacity());

        // Install RootView Native as ContentPane
        RootPaneContainer rpc = (RootPaneContainer) winNtv;
        rpc.setContentPane(_rviewNtv);

        // Size window to root view
        winNtv.pack(); //winNtv.setLocation((int)win.getX(),(int)win.getY());

        // Set WindowView insets
        java.awt.Insets insAWT = winNtv.getInsets();
        Insets ins = new Insets(insAWT.top, insAWT.right, insAWT.bottom, insAWT.left);
        win.setPadding(ins);

        // Add component listener to sync win bounds changes with WindowView/RootView
        winNtv.addComponentListener(getNativeWindowBoundsListener());

        // Add WindowListener to dispatch window events
        winNtv.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent anEvent)  { handleSwingWindowActiveChange(anEvent); }
            public void windowDeactivated(WindowEvent anEvent)  { handleSwingWindowActiveChange(anEvent); }
            public void windowOpened(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinOpen); }
            public void windowClosing(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinClose); }
            public void windowClosed(WindowEvent anEvent)  { }
        });

        // Sync Window bounds to WindowView
        handleSwingWindowBoundsChanged();
    }

    /**
     * Show window at given screen x and y.
     */
    @Override
    public void show()
    {
        // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
        //if (wview.isAlwaysOnTop()) win.setAlwaysOnTop(true);

        // Set native window location, make visible and notify ShowingChanged (so change is reflected immediately)
        int x = (int) Math.round(_win.getX());
        int y = (int) Math.round(_win.getY());

        // Set WinNtv location and make visible
        _winNtv.setLocation(x, y);
        _winNtv.setVisible(true);
        handleSwingWindowShowingChange();

        // If window is modal, just return
        if (_win.isModal()) return;

        // If window has frame save name, set listener
        if (_win.getSaveName() != null && _win.getProp("FrameSaveListener") == null) {
            FrameSaveListener fsl = new FrameSaveListener(_win);
            _win.setProp("FrameSaveListener", fsl);
            _winNtv.addComponentListener(fsl);
        }

        // If window is always-on-top or does hide-on-deactivate, add listener to handle app deactivate stuff
        //if (wview.isAlwaysOnTop() || wview.isHideOnDeactivate()) {
        //if (_actvAdptr==null) _actvAdptr = new ActivationAdapter(win,wview.isHideOnDeactivate()));
        //_actvAdptr.setEnabled(true); }
    }

    /**
     * Hides the window.
     */
    @Override
    public void hide()
    {
        _winNtv.setVisible(false);
        handleSwingWindowShowingChange(); // So change is reflected immediately
    }

    /**
     * Order window to front.
     */
    @Override
    public void toFront()  { _winNtv.toFront(); }

    /**
     * Override to correct for case of RootView not in Swing Window.
     */
    @Override
    public Point convertViewPointToScreen(View aView, double aX, double aY)
    {
        // Make sure we have right window
        WindowView win = aView.getWindow();
        if (win != _win) {
            WindowView.WindowHpr hpr = win.getHelper();
            return hpr.convertViewPointToScreen(aView, aX, aY);
        }

        // Get point in RootView
        Point point = aView.localToParent(aX, aY, _rview);
        double pointX = point.x;
        double pointY = point.y;

        // Iterate up to screen, adding offsets
        for (java.awt.Component comp = _rviewNtv; comp != null; comp = comp.getParent()) {
            pointX += comp.getX();
            pointY += comp.getY();

            // If Window, go ahead and bail, in case this is dialog of parent window
            if (comp instanceof java.awt.Window)
                break;
        }

        // Return point
        return new Point(pointX, pointY);
    }

    /**
     * Sets the title of the window.
     */
    public void setTitle(String aTitle)
    {
        if (_winNtv instanceof JFrame)
            ((JFrame) _winNtv).setTitle(aTitle);
        else if (_winNtv instanceof JDialog)
            ((JDialog) _winNtv).setTitle(aTitle);
        else System.err.println("WindowHpr.setTitle: Not supported " + _winNtv);
    }

    /**
     * Sets the title of the window.
     */
    public void setResizable(boolean aValue)
    {
        if (_winNtv instanceof JFrame)
            ((JFrame) _winNtv).setResizable(aValue);
        else if (_winNtv instanceof JDialog)
            ((JDialog) _winNtv).setResizable(aValue);
        else System.err.println("WindowHpr.setTitle: Not supported " + _winNtv);
    }

    /**
     * Sets the document file for the window title bar proxy icon.
     */
    @Override
    public void setDocURL(WebURL aURL)
    {
        // If not local, just bail
        if (aURL == null || !aURL.getScheme().equalsIgnoreCase("file"))
            return;

        // Get Java file
        File file = aURL.getJavaFile();

        // Install in RootPane
        if (_winNtv instanceof RootPaneContainer) {
            RootPaneContainer rootPaneContainer = (RootPaneContainer) _winNtv;
            JRootPane rootPane = rootPaneContainer.getRootPane();
            if (rootPane == null)
                return;
            rootPane.putClientProperty("Window.documentFile", file);
        }
    }

    /**
     * Sets the image property of given object to given string.
     */
    public void setImage(Image anImage)
    {
        _winNtv.setIconImage(AWT.snapToAwtImage(anImage));
    }

    /**
     * Registers a view for repaint.
     */
    @Override
    public void requestPaint(Rect aRect)
    {
        _rviewNtv.repaint(aRect);
    }

    /**
     * Called when window is shown/hidden.
     */
    protected void handleSwingWindowShowingChange()
    {
        boolean showing = _winNtv.isShowing();
        ViewUtils.setShowing(_win, showing);
    }

    /**
     * Handles when Swing window bounds changed.
     */
    protected void handleSwingWindowBoundsChanged()
    {
        int x = _winNtv.getX(), y = _winNtv.getY();
        int w = _winNtv.getWidth(), h = _winNtv.getHeight();
        _win.setBounds(x, y, w, h);

        // If window deactivated and it has Popup, hide popup
        if (_win.getPopup() != null)
            _win.getPopup().hide();
    }

    /**
     * Handles active changed.
     */
    protected void handleSwingWindowActiveChange(WindowEvent anEvent)
    {
        // Update Window.Focused from SwingWindow.Active
        boolean active = _winNtv.isActive();
        if (_win.isFocusable())
            ViewUtils.setFocused(_win, active);

        // If window deactivated and it has Popup and popup isn't new active window, hide popup
        if (!active && _win.getPopup() != null) {
            PopupWindow popupWindow = _win.getPopup();
            if (popupWindow.getHelper().getNative() != anEvent.getOppositeWindow())
                _win.getPopup().hide();
        }
    }

    /**
     * Called when WindowView changes to update native.
     */
    protected void handleSnapWindowPropertyChange(PropChange aPC)
    {
        switch (aPC.getPropName()) {

            // Handle bounds changes
            case View.X_Prop: setX((Double) aPC.getNewValue()); break;
            case View.Y_Prop: setY((Double) aPC.getNewValue()); break;
            case View.Width_Prop: setWidth((Double) aPC.getNewValue()); break;
            case View.Height_Prop: setHeight((Double) aPC.getNewValue()); break;

            // Handle AlwaysOnTop
            case WindowView.AlwaysOnTop_Prop:
                _winNtv.setAlwaysOnTop(Convert.booleanValue(aPC.getNewValue()));
                break;

            // Handle Image, Title, Resizble
            case WindowView.Image_Prop: setImage((Image) aPC.getNewValue()); break;
            case WindowView.Title_Prop: setTitle((String) aPC.getNewValue()); break;
            case WindowView.Resizable_Prop: setResizable(Convert.booleanValue(aPC.getNewValue())); break;

            // Handle Cursor
            case WindowView.ActiveCursor_Prop: handleSnapWindowActiveCursorChange(); break;
        }
    }

    /**
     * Called when RootView CurrentCursor changes.
     */
    private void handleSnapWindowActiveCursorChange()
    {
        _cursor = _win.getActiveCursor();
        if (_setRootViewNativeCursorRun == null)
            SwingUtilities.invokeLater(_setRootViewNativeCursorRun = this::setRootViewNativeCursor);
    }

    /**
     * Called to set cursor immediately.
     */
    private void setRootViewNativeCursor()
    {
        java.awt.Cursor cursor = AWT.get(_cursor);
        _rviewNtv.setCursor(cursor);
        _setRootViewNativeCursorRun = null;
    }

    /**
     * Sets bounds values.
     */
    private void setX(double aValue)
    {
        _winNtv.setLocation((int) aValue, _winNtv.getY());
    }

    private void setY(double aValue)
    {
        _winNtv.setLocation(_winNtv.getX(), (int) aValue);
    }

    private void setWidth(double aValue)
    {
        _winNtv.setSize((int) aValue, _winNtv.getHeight());
    }

    private void setHeight(double aValue)
    {
        _winNtv.setSize(_winNtv.getWidth(), (int) aValue);
    }

    /**
     * Returns the listener that listens to Swing window move/resize/show/hide.
     */
    protected ComponentAdapter getNativeWindowBoundsListener()
    {
        // If already set, just return
        if (_ntvWinBndsLsnr != null) return _ntvWinBndsLsnr;

        // Create listener
        ComponentAdapter lsnr = new ComponentAdapter() {
            public void componentMoved(ComponentEvent e)  { handleSwingWindowBoundsChanged(); }
            public void componentResized(ComponentEvent e)  { handleSwingWindowBoundsChanged(); }
            public void componentShown(ComponentEvent e)  { handleSwingWindowShowingChange(); }
            public void componentHidden(ComponentEvent e)  { handleSwingWindowShowingChange(); }
        };

        // Set and return
        return _ntvWinBndsLsnr = lsnr;
    }

    /**
     * Sends the given event.
     */
    protected void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
    {
        // If event type not used, just return
        if (!_win.getEventAdapter().isEnabled(aType)) return;

        // Create event and fire
        ViewEvent event = ViewEvent.createEvent(_win, anEvent, aType, null);
        _win.dispatchEventToWindow(event);

        // If Window Close, update JFrame.DefaultCloseOperation
        if (aType == ViewEvent.Type.WinClose && _winNtv instanceof JFrame && event.isConsumed())
            ((JFrame) _winNtv).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Returns the ClientView.Window.Native. If ClientView (or parent) is not in Snap created window (installed
     * manually in JComponent hierarchy), try ClientView.RootView.Native.Window instead.
     */
    private Window getClientWindow()
    {
        // Get window ClientView.Window.Helper.Native (just return if ClientView is null)
        View cview = _win.getClientView();
        if (cview == null) return null;
        WindowView cwin = cview.getWindow();
        if (cwin == null) return null; // Shouldn't be possible
        SWWindowHpr cwinHpr = (SWWindowHpr) cwin.getHelper();
        Window cwinNtv = cwinHpr._winNtv;

        // If ClientView in window that's not showing, get win from RootView.Native instead
        if (!cwin.isShowing())
            cwinNtv = SwingUtils.getParent(cwinHpr._rviewNtv, Window.class);

        // Return window
        return cwinNtv;
    }

    /**
     * A component listener to save frame to preferences on move.
     */
    private static class FrameSaveListener extends ComponentAdapter {
        public FrameSaveListener(WindowView aWN)
        {
            _wview = aWN;
        }

        WindowView _wview;

        public void componentMoved(ComponentEvent e)  { setFrameString(e); }
        public void componentResized(ComponentEvent e)  { setFrameString(e); }
        private void setFrameString(ComponentEvent e)  { _wview.saveFrame(); }
    }
}