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
    private Window _swingWindow;

    // The snap RootView
    private RootView _rootView;

    // The native RootView
    private SWRootView _rootViewNative;

    // The current cursor
    private snap.view.Cursor _cursor;

    // Runnable to update cursor
    private Runnable _setRootViewNativeCursorRun;

    /**
     * Constructor.
     */
    public SWWindowHpr()
    {
        super();
    }

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
        _swingWindow = createSwingWindow();

        // Set RootView var and create native RootView
        _rootView = aWin.getRootView();
        _rootViewNative = new SWRootView(_win, _rootView);

        // Add listener to handle window prop changes
        _win.addPropChangeListener(this::handleSnapWindowPropChange);
    }

    /**
     * Returns the native being helped.
     */
    @Override
    public Window getNative()  { return _swingWindow; }

    /**
     * Returns the native being helped.
     */
    private Window createSwingWindow()
    {
        String windowType = _win.getType();

        // If standard TYPE_MAIN window, return new JFrame
        if (windowType == WindowView.TYPE_MAIN)
            return new JFrame();

        // Get window client/owner (if requested by/for view in another window)
        Window owner = getClientWindow();
        return new JDialog(owner);
    }

    /**
     * Returns the native for the window content.
     */
    @Override
    public JComponent getContentNative()  { return _rootViewNative; }

    /**
     * Initialize native window.
     */
    @Override
    public void initWindow()
    {
        // Set title, resizeable
        setTitle(_win.getTitle());
        setResizable(_win.isResizable());

        // Configure JDialog: Title, Resizable, Modal
        if (_swingWindow instanceof JDialog jdialog) {
            jdialog.setModal(_win.isModal());
            switch (_win.getType()) {
                case WindowView.TYPE_UTILITY -> jdialog.getRootPane().putClientProperty("Window.style", "small");
                case WindowView.TYPE_PLAIN -> jdialog.setUndecorated(true);
            }
        }

        // Set common attributes
        _swingWindow.setFocusableWindowState(_win.isFocusable());
        _swingWindow.setAlwaysOnTop(_win.isAlwaysOnTop());
        _swingWindow.setOpacity((float) _win.getOpacity());

        // Install RootView Native as ContentPane
        RootPaneContainer rootPaneContainer = (RootPaneContainer) _swingWindow;
        rootPaneContainer.setContentPane(_rootViewNative);

        // Size window to root view
        _swingWindow.pack();

        // Set WindowView insets
        java.awt.Insets insAWT = _swingWindow.getInsets();
        _win.setPadding(new Insets(insAWT.top, insAWT.right, insAWT.bottom, insAWT.left));

        // Add component listener to sync win bounds changes with WindowView/RootView
        _swingWindow.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e)  { handleSwingWindowBoundsChange(); }
            public void componentResized(ComponentEvent e)  { handleSwingWindowBoundsChange(); }
            public void componentShown(ComponentEvent e)  { handleSwingWindowShowingChange(); }
            public void componentHidden(ComponentEvent e)  { handleSwingWindowShowingChange(); }
        });

        // Add WindowListener to dispatch window events
        _swingWindow.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent anEvent)  { handleSwingWindowActiveChange(anEvent); }
            public void windowDeactivated(WindowEvent anEvent)  { handleSwingWindowActiveChange(anEvent); }
            public void windowOpened(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinOpen); }
            public void windowClosing(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinClose); }
            public void windowClosed(WindowEvent anEvent)  { }
        });

        // Sync Window bounds to WindowView
        handleSwingWindowBoundsChange();
    }

    /**
     * Show window at given screen x and y.
     */
    @Override
    public void show()
    {
        // Set native window location, make visible and notify ShowingChanged (so change is reflected immediately)
        int winX = (int) Math.round(_win.getX());
        int winY = (int) Math.round(_win.getY());

        // Set WinNtv location and make visible
        _swingWindow.setLocation(winX, winY);
        _swingWindow.setVisible(true);
        handleSwingWindowShowingChange();

        // If window is modal, just return
        if (_win.isModal())
            return;

        // If window has frame save name, set listener
        if (_win.getSaveName() != null && _win.getProp("FrameSaveListener") == null) {
            FrameSaveListener fsl = new FrameSaveListener(_win);
            _win.setProp("FrameSaveListener", fsl);
            _swingWindow.addComponentListener(fsl);
        }
    }

    /**
     * Hides the window.
     */
    @Override
    public void hide()
    {
        _swingWindow.setVisible(false);
        handleSwingWindowShowingChange(); // So change is reflected immediately
    }

    /**
     * Order window to front.
     */
    @Override
    public void toFront()  { _swingWindow.toFront(); }

    /**
     * Override to correct for case of RootView not in Swing Window.
     */
    @Override
    public Point convertViewPointToScreen(View aView, double aX, double aY)
    {
        // Get point in RootView
        Point screenXY = aView.localToParent(aX, aY, _rootView);
        double screenX = screenXY.x;
        double screenY = screenXY.y;

        // Iterate up to screen, adding offsets
        for (java.awt.Component comp = _rootViewNative; comp != null; comp = comp.getParent()) {
            screenX += comp.getX();
            screenY += comp.getY();

            // If Window, go ahead and bail, in case this is dialog of parent window
            if (comp instanceof java.awt.Window)
                break;
        }

        // Return point
        return new Point(screenX, screenY);
    }

    /**
     * Sets the title of the window.
     */
    public void setTitle(String aTitle)
    {
        if (_swingWindow instanceof JFrame jframe)
            jframe.setTitle(aTitle);
        else if (_swingWindow instanceof JDialog jdialog)
            jdialog.setTitle(aTitle);
    }

    /**
     * Sets the title of the window.
     */
    public void setResizable(boolean aValue)
    {
        if (_swingWindow instanceof JFrame jframe)
            jframe.setResizable(aValue);
        else if (_swingWindow instanceof JDialog jdialog)
            jdialog.setResizable(aValue);
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
        if (_swingWindow instanceof RootPaneContainer rootPaneContainer) {
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
        _swingWindow.setIconImage(AWT.snapToAwtImage(anImage));
    }

    /**
     * Registers a view for repaint.
     */
    @Override
    public void requestPaint(Rect aRect)
    {
        _rootViewNative.repaint(aRect);
    }

    /**
     * Called when window is shown/hidden.
     */
    private void handleSwingWindowShowingChange()
    {
        boolean showing = _swingWindow.isShowing();
        ViewUtils.setShowing(_win, showing);
    }

    /**
     * Called when Swing window bounds changes.
     */
    private void handleSwingWindowBoundsChange()
    {
        int swingX = _swingWindow.getX();
        int swingY = _swingWindow.getY();
        int swingW = _swingWindow.getWidth();
        int swingH = _swingWindow.getHeight();
        _win.setBounds(swingX, swingY, swingW, swingH);

        // If window deactivated and it has Popup, hide popup
        if (_win.getPopup() != null)
            _win.getPopup().hide();
    }

    /**
     * Handles active changed.
     */
    private void handleSwingWindowActiveChange(WindowEvent anEvent)
    {
        // Update Window.Focused from SwingWindow.Active
        boolean active = _swingWindow.isActive();
        if (_win.isFocusable())
            ViewUtils.setFocused(_win, active);

        // If window deactivated and it has Popup and popup isn't new active window, hide popup
        if (!active && _win.getPopup() != null) {
            PopupWindow popupWindow = _win.getPopup();
            if (popupWindow.getNative() != anEvent.getOppositeWindow())
                _win.getPopup().hide();
        }
    }

    /**
     * Called when WindowView changes to update native.
     */
    private void handleSnapWindowPropChange(PropChange propChange)
    {
        switch (propChange.getPropName()) {

            // Handle bounds changes
            case View.X_Prop -> setX(Convert.doubleValue(propChange.getNewValue()));
            case View.Y_Prop -> setY(Convert.doubleValue(propChange.getNewValue()));
            case View.Width_Prop -> setWidth((Convert.doubleValue(propChange.getNewValue())));
            case View.Height_Prop -> setHeight(Convert.doubleValue(propChange.getNewValue()));

            // Handle AlwaysOnTop
            case WindowView.AlwaysOnTop_Prop -> _swingWindow.setAlwaysOnTop(Convert.boolValue(propChange.getNewValue()));

            // Handle Image, Title, Resizble
            case WindowView.Image_Prop -> setImage((Image) propChange.getNewValue());
            case WindowView.Title_Prop -> setTitle((String) propChange.getNewValue());
            case WindowView.Resizable_Prop -> setResizable(Convert.boolValue(propChange.getNewValue()));

            // Handle Cursor
            case WindowView.ActiveCursor_Prop -> handleSnapWindowActiveCursorChange();
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
        _rootViewNative.setCursor(cursor);
        _setRootViewNativeCursorRun = null;
    }

    /**
     * Sets bounds values.
     */
    private void setX(double aValue)  { _swingWindow.setLocation((int) aValue, _swingWindow.getY()); }
    private void setY(double aValue)  { _swingWindow.setLocation(_swingWindow.getX(), (int) aValue); }
    private void setWidth(double aValue)  { _swingWindow.setSize((int) aValue, _swingWindow.getHeight()); }
    private void setHeight(double aValue)  { _swingWindow.setSize(_swingWindow.getWidth(), (int) aValue); }

    /**
     * Sends the given event.
     */
    private void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
    {
        // If event type not used, just return
        if (!_win.getEventAdapter().isEnabled(aType)) return;

        // Create event and fire
        ViewEvent event = ViewEvent.createEvent(_win, anEvent, aType, null);
        _win.dispatchEventToWindow(event);

        // If Window Close, update JFrame.DefaultCloseOperation
        if (aType == ViewEvent.Type.WinClose && _swingWindow instanceof JFrame jframe && event.isConsumed())
            jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Returns the ClientView.Window.Native.
     */
    private Window getClientWindow()
    {
        // Get window ClientView.Window.Helper.Native (just return if ClientView is null)
        View clientView = _win.getClientView(); if (clientView == null) return null;
        WindowView clientWindow = clientView.getWindow(); if (clientWindow == null) return null; // Shouldn't be possible

        // If ClientView in window that's not showing, get win from RootView.Native instead
        // Might have been installed manually in JComponent hierarchy
        if (!clientWindow.isShowing()) {
            SWWindowHpr clientWindowHpr = (SWWindowHpr) clientWindow.getHelper();
            return SwingUtils.getParent(clientWindowHpr._rootViewNative, Window.class);
        }

        // Return
        return (Window) clientWindow.getNative();
    }

    /**
     * A component listener to save frame to preferences on move.
     */
    private static class FrameSaveListener extends ComponentAdapter {
        private WindowView _windowView;
        public FrameSaveListener(WindowView windowView)  { _windowView = windowView; }
        public void componentMoved(ComponentEvent e)  { _windowView.saveFrame(); }
        public void componentResized(ComponentEvent e)  { _windowView.saveFrame(); }
    }
}