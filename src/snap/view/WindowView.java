/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.lang.reflect.Array;
import java.util.*;

import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.Size;
import snap.gfx.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * A class to manage a Window.
 */
public class WindowView extends ParentView {
    
    // The panel's window title
    private String  _title;
    
    // The type
    private String  _type = TYPE_MAIN;
    
    // The root view
    private RootView  _rootView;

    // The Active cursor
    private Cursor  _activeCursor = Cursor.DEFAULT;

    // Whether the panel's window is always on top
    private boolean  _alwaysOnTop;
    
    // The document file
    private WebURL  _docURL;
    
    // The window image
    private Image  _image;
    
    // Whether window hides on deativate
    private boolean  _hideOnDeactivate;
    
    // Whether window is modal
    private boolean  _modal = false;
    
    // Whether window is resizable
    private boolean  _resizable = true;
    
    // Whether window is sized to maximum screen size
    private boolean  _maximized;
    
    // The bounds for maximized window
    private Rect  _maxBounds, _unmaxBounds;
    
    // The Frame save name
    private String  _saveName;
    
    // Save frame size
    private boolean  _saveSize;
    
    // The focused view
    private View  _focusedView;
    
    // The previous focused view
    private View _lastFocusedView;

    // The helper to map window functionality to native platform
    protected WindowHpr _helper;
    
    // A class to handle view updates (repaint, relayout, resetUI, animation)
    private ViewUpdater  _updater;
    
    // The EventDispatcher
    private EventDispatcher  _eventDispatcher = new EventDispatcher(this);
    
    // The View that referenced on last show
    private View  _clientView;
    
    // A list of all open windows
    private static List <WindowView>  _openWins = new ArrayList<>();
    
    // Constants for Type
    public static final String TYPE_MAIN = "MAIN";
    public static final String TYPE_UTILITY = "UTILITY";
    public static final String TYPE_PLAIN = "PLAIN";
    
    // Constants for style
    public enum Style { Small }
    
    // Constants for properties
    public static final String ActiveCursor_Prop = "ActiveCursor";
    public static final String AlwaysOnTop_Prop = "AlwaysOnTop";
    public static final String Image_Prop = "Image";
    public static final String Maximized_Prop = "Maximized";
    public static final String Resizable_Prop = "Resizable";
    public static final String Title_Prop = "Title";
    public static final String FocusView_Prop = "FocusView";

    /**
     * Creates a WindowView.
     */
    public WindowView()
    {
        // Create and add RootView
        _rootView = new RootView();
        addChild(_rootView);
    }

    /**
     * Returns the title of the window.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the title of the window.
     */
    public void setTitle(String aValue)
    {
        if (Objects.equals(aValue, _title)) return;
        firePropChange(Title_Prop, _title, _title = aValue);
    }

    /**
     * Returns the window type.
     */
    public String getType()  { return _type; }

    /**
     * Sets the window type.
     */
    public void setType(String aType)  { _type = aType; }

    /**
     * Returns whether the window is resizable.
     */
    public boolean isResizable()  { return _resizable; }

    /**
     * Sets whether the window is resizable (default to true).
     */
    public void setResizable(boolean aValue)
    {
        if (aValue == _resizable) return;
        firePropChange(Resizable_Prop, _resizable, _resizable = aValue);
    }

    /**
     * Returns whether the window is sized to maximum screen size.
     */
    public boolean isMaximized()  { return _maximized; }

    /**
     * Sets whether the window is sized to maximum screen size.
     */
    public void setMaximized(boolean aValue)
    {
        // If already set, just return
        if (aValue == _maximized) return;
        _maximized = aValue;

        // If Maximizing
        if (aValue) {
            _unmaxBounds = getBounds();
            Rect maxBounds = getMaximizedBounds();
            setBounds(maxBounds);
        }

        // If Un-Maximizing, reset bounds
        else {
            if (_unmaxBounds != null && !_unmaxBounds.isEmpty())
                setBounds(_unmaxBounds);
            else {
                Size prefSize = getPrefSize();
                Rect screenRect = GFXEnv.getEnv().getScreenBoundsInset();
                Rect centeredBounds = screenRect.getRectCenteredInside(prefSize.width, prefSize.height);
                setBounds(centeredBounds);
            }
        }

        // Get screen size
        firePropChange(Maximized_Prop, !_maximized, _maximized);
    }

    /**
     * Returns the window max size.
     */
    public Rect getMaximizedBounds()
    {
        if (_maxBounds != null) return _maxBounds;

        // Get screen bounds
        Rect maxBounds = GFXEnv.getEnv().getScreenBoundsInset();

        // Set and return
        return _maxBounds = maxBounds;
    }

    /**
     * Sets the bounds rect to use when window is maximized (uses ViewEnv ScreenBoundsInset by default).
     */
    public void setMaximizedBounds(Rect aRect)  { _maxBounds = aRect; }

    /**
     * Returns the save name.
     */
    public String getSaveName()  { return _saveName; }

    /**
     * Sets the save name.
     */
    public void setSaveName(String aName)  { _saveName = aName; }

    /**
     * Returns whether to save size
     */
    public boolean isSaveSize()  { return _saveSize; }

    /**
     * Sets whether to save size.
     */
    public void setSaveSize(boolean aValue)  { _saveSize = aValue; }

    /**
     * Save frame.
     */
    public void saveFrame()
    {
        int winX = (int) getX();
        int winY = (int) getY();
        int winW = (int) getWidth();
        int winH = (int) getHeight();
        StringBuilder sb = new StringBuilder().append(winX).append(' ').append(winY);
        if (_saveSize)
            sb.append(' ').append(winW).append(' ').append(winH);
        Prefs.getDefaultPrefs().setValue(_saveName + "Loc", sb.toString());
    }

    /**
     * Returns the root view.
     */
    public RootView getRootView()  { return _rootView; }

    /**
     * Returns the content associated with this window.
     */
    public View getContent()
    {
        return getRootView().getContent();
    }

    /**
     * Sets the content associated with this window.
     */
    public void setContent(View aView)
    {
        getRootView().setContent(aView);
    }

    /**
     * Returns the native for the window content.
     */
    public Object getContentNative()
    {
        return getHelper().getContentNative();
    }

    /**
     * Returns whether the window is always on top.
     */
    public boolean isAlwaysOnTop()  { return _alwaysOnTop; }

    /**
     * Sets whether the window is always on top.
     */
    public void setAlwaysOnTop(boolean aValue)
    {
        if (aValue == _alwaysOnTop) return;
        firePropChange(AlwaysOnTop_Prop, _alwaysOnTop, _alwaysOnTop = aValue);
    }

    /**
     * Returns the document file URL for the window title bar proxy icon.
     */
    public WebURL getDocURL()  { return _docURL; }

    /**
     * Returns the document file URL for the window title bar proxy icon.
     */
    public void setDocURL(WebURL aURL)
    {
        // If already set, just return
        if (Objects.equals(aURL, getDocURL())) return;

        // Set
        _docURL = aURL;
        getHelper().setDocURL(aURL);
    }

    /**
     * Returns the icon image for the window.
     */
    public Image getImage()  { return _image; }

    /**
     * Sets the icon image for the window.
     */
    public void setImage(Image anImage)
    {
        firePropChange(Image_Prop, _image, _image = anImage);
    }

    /**
     * Returns whether the window will hide on deactivate.
     */
    public boolean isHideOnDeactivate()  { return _hideOnDeactivate; }

    /**
     * Sets whether the window will hide on deacativate.
     */
    public void setHideOnDeactivate(boolean aValue)  { _hideOnDeactivate = aValue; }

    /**
     * Returns the modal mode of the window.
     */
    public boolean isModal()  { return _modal; }

    /**
     * Sets the modal mode of the window (defaults to false).
     */
    public void setModal(boolean aValue)  { _modal = aValue; }

    /**
     * Returns the view that currently receives KeyEvents.
     */
    public View getFocusedView()
    {
        if (_focusedView != null && !_focusedView.isFocused())
            _focusedView = null;
        return _focusedView;
    }

    /**
     * Sets the view that currently receives KeyEvents.
     */
    protected void setFocusView(View aView)
    {
        if (aView == getFocusedView()) return;

        // If existing FocusedView, clear View.Focused
        if (_focusedView != null)
            _focusedView.setFocused(false);

        // Update FocusViewLast, FocusView
        _lastFocusedView = _focusedView;
        _focusedView = aView;

        // If new FocusedView, set View.Focused
        if (_focusedView != null)
            _focusedView.setFocused(true);

        // Tell WindowHpr
        WindowHpr helper = getHelper();
        helper.focusDidChange(_focusedView);

        // Fire prop change
        firePropChange(FocusView_Prop, _focusedView, _lastFocusedView);
    }

    /**
     * Tries to makes the given view the view that receives KeyEvents.
     */
    protected void requestFocus(View aView)
    {
        // Make sure this happens on Event thread
        if (!getEnv().isEventThread()) {
            getEnv().runLater(() -> requestFocus(aView));
            return;
        }

        // Get view - if null, try to find something better
        View newFocusView = aView;
        if (newFocusView == null) {
            newFocusView = getFocusViewForNull();
            if (newFocusView == getFocusedView())
                newFocusView = null;
        }

        // Sets the focus view
        setFocusView(newFocusView);

        // If resigning focus, reset last focus view, too
        if (aView == null)
            _lastFocusedView = null;
    }

    /**
     * Returns the previous focus view.
     */
    public View getLastFocusedView()  { return _lastFocusedView; }

    /**
     * Returns a view to use when focus is null.
     */
    private View getFocusViewForNull()
    {
        if (getLastFocusedView() != null)
            return getLastFocusedView();
        for (View view = getFocusedView(); view != null; view = view.getParent())
            if (view.isFocusable())
                return view;
        return null;
    }

    /**
     * Returns the view helper.
     */
    public WindowHpr getHelper()
    {
        if (_helper != null) return _helper;
        _helper = getEnv().createHelper(this);
        _helper.setWindow(this);
        _updater = new ViewUpdater(this); repaint();
        return _helper;
    }

    /**
     * Initializes the native window.
     */
    protected void initNativeWindow()
    {
        getHelper().initWindow();
        pack();
    }

    /** Initializes the native window once. */
    void initNativeWindowOnce()
    {
        if (_initWin) return;
        _initWin = true;
        initNativeWindow();
    }
    boolean _initWin;

    /**
     * Shows window in center of given view.
     */
    public void showCentered(View aView)
    {
        // Make sure this happens in event thread
        if (!ViewUtils.isEventThread()) { ViewUtils.runLater(() -> showCentered(aView)); return; }

        // Calculate screen location for view center and show
        Point screenLoc = getScreenLocation(aView, Pos.CENTER, 0, 0);
        show(null, screenLoc.x, screenLoc.y);
    }

    /**
     * Show the window relative to given view and View X/Y (screen x/y if view is null).
     */
    public void show(View aView, double aX, double aY)
    {
        // Make sure this happens in event thread
        if (!ViewUtils.isEventThread()) { ViewUtils.runLater(() -> show(aView, aX, aY)); return; }

        // Set ClientView
        _clientView = aView;

        // Make sure window is initialized
        initNativeWindowOnce();

        // Get window XY coords: If aView provided, convert point from view to screen coords
        double winX = aX;
        double winY = aY;
        if (aView != null) {
            Point pointInScreenCoords = aView.localToScreen(aX, aY);
            winX = pointInScreenCoords.x;
            winY = pointInScreenCoords.y;
        }

        // Handle Maximized
        if (isMaximized()) {
            Rect maximizedBounds = getMaximizedBounds();
            setSize(maximizedBounds.width, maximizedBounds.height);
            winX = maximizedBounds.x;
            winY = maximizedBounds.y;
        }

        // If FrameSaveName provided, set Location from defaults and register to store future window moves
        else if (getSaveName() != null) {

            // Get location string
            String saveName = getSaveName();
            String locString = Prefs.getDefaultPrefs().getString(saveName + "Loc");
            if (locString != null) {
                String[] strings = locString.split(" ");
                winX = StringUtils.intValue(strings[0]);
                winY = StringUtils.intValue(strings[1]);

                // Handle SaveSize
                if (isSaveSize()) {
                    int winW = strings.length > 2 ? StringUtils.intValue(strings[2]) : 0;
                    int winH = strings.length > 3 ? StringUtils.intValue(strings[3]) : 0;
                    if (winW > 0 && winH > 0)
                        setSize(winW, winH);
                }
            }
        }

        // Set window location
        setXY(winX, winY);

        // Have helper show window
        show();
    }

    /**
     * Shows window in center of screen.
     */
    public void show()
    {
        // Make sure this happens in event thread
        if (!ViewUtils.isEventThread()) { ViewUtils.runLater(this::show); return; }

        // If outside bounds, scoot in
        Rect screenBounds = GFXEnv.getEnv().getScreenBoundsInset();
        if (screenBounds.contains(getX(), getY())) {
            if (getMaxX() > screenBounds.getMaxX())
                setX(screenBounds.getMaxX() - getWidth() - 5);
            if (getMaxY() > screenBounds.getMaxY())
                setY(screenBounds.getMaxY() - getHeight() - 5);
        }

        // Forward to helper
        getHelper().show();
    }

    /**
     * Hide the window.
     */
    public void hide()
    {
        if (getPopup() != null)
            getPopup().hide();
        if (isShowing())
            getHelper().hide();
    }

    /**
     * Returns the view associated with the last show() call.
     */
    public View getClientView()  { return _clientView; }

    /**
     * Packs the window.
     */
    public void pack()
    {
        Size bestSize = getBestSize();
        setSize(bestSize);
    }

    /**
     * Order window to front.
     */
    public void toFront()
    {
        getHelper().toFront();
    }

    /**
     * Returns the screen location for given view, position and offsets.
     */
    public Point getScreenLocation(View aView, Pos aPos, double aDX, double aDY)
    {
        // Set ClientView
        _clientView = aView;

        // Make window is initialized
        initNativeWindowOnce();

        // If no view, just use screen
        if (aView == null) {
            Rect screenBounds = GFXEnv.getEnv().getScreenBoundsInset();
            return getRectLocation(screenBounds, aPos, aDX, aDY);
        }

        // Get View bounds in screen coords
        Rect rect1 = aView.getBoundsLocal();
        Rect rect2 = aView.localToParent(rect1, null).getBounds();

        // Return location for rect, position and offset
        Point point = getRectLocation(rect2, aPos, aDX, aDY);
        return point;
    }

    /**
     * Override to return this window.
     */
    public WindowView getWindow()  { return this; }

    /**
     * Override to do layout immediately.
     */
    public void relayout()  { layout(); }

    /**
     * Override to change to root view.
     */
    public void repaint(double aX, double aY, double aW, double aH)
    {
        RootView rootView = getRootView();
        double rootX = aX - rootView.getX();
        double rootY = aY - rootView.getY();
        rootView.repaint(rootX, rootY, aW, aH);
    }

    /**
     * Override to return showing, since it is eqivalent for window.
     */
    public boolean isVisible()  { return isShowing(); }

    /**
     * Override to call show/hide.
     */
    public void setVisible(boolean aValue)
    {
        if (aValue)
            showCentered(null);
        else hide();
    }

    /**
     * Override to update global windows list.
     */
    protected void setShowing(boolean aValue)
    {
        // Do normal version (or return if already set)
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // Handle Show: Update OpenWins list
        if (aValue) {
            ListUtils.moveToFront(_openWins, this);
        }

        // Handle Hide: Remove from OpenWins list and dispatch mouse move outside bounds to trigger any mouse exit code
        else {
            _openWins.remove(this);
            _eventDispatcher.dispatchMouseMoveOutsideWindow();

            // Register to check later to exit if still no open windows
            if (_openWins.isEmpty())
                ViewUtils.runDelayed(WindowView::checkForExitWhenAllWindowsClosed, 250);
        }
    }

    /**
     * Override to move window to front of all windows list.
     */
    protected void setFocused(boolean aValue)
    {
        // Do normal version
        if (aValue == isFocused()) return;
        super.setFocused(aValue);

        // Handle Focus Gained: Move window to front
        if (aValue)
            ListUtils.moveToFront(_openWins, this);
    }

    /**
     * Returns the active cursor.
     */
    public Cursor getActiveCursor()  { return _activeCursor; }

    /**
     * Sets the current cursor.
     */
    public void setActiveCursor(Cursor aCursor)
    {
        if (aCursor == _activeCursor) return;
        firePropChange(ActiveCursor_Prop, _activeCursor, _activeCursor = aCursor);
    }

    /**
     * Resets the active cursor.
     */
    public void resetActiveCursor()
    {
        View mouseOverView = _eventDispatcher._mouseOverView;
        Cursor cursor = mouseOverView != null ? mouseOverView.getCursor() : Cursor.DEFAULT;
        if (cursor != getActiveCursor())
            setActiveCursor(cursor);
    }

    /**
     * Override to try to get tool tip from mouse over stack.
     */
    public String getToolTip(ViewEvent anEvent)
    {
        List<View> mouseOvers = _eventDispatcher._mouseOvers;
        for (int i = mouseOvers.size() - 1; i >= 0; i--) {
            View view = mouseOvers.get(i);
            String text = view.isToolTipEnabled() ? view.getToolTip(anEvent.copyForView(view)) : view.getToolTip();
            if (text != null)
                return text;
        }
        return null;
    }

    /**
     * Returns the EventDispatcher.
     */
    public EventDispatcher getDispatcher()  { return _eventDispatcher; }

    /**
     * Dispatch event.
     */
    public void dispatchEventToWindow(ViewEvent anEvent)
    {
        _eventDispatcher.dispatchEvent(anEvent);
    }

    /**
     * Returns the Updater.
     */
    public ViewUpdater getUpdater()  { return _updater; }

    /**
     * Returns the popup window, if one was added to root view during last event.
     */
    public PopupWindow getPopup()  { return _eventDispatcher.getPopup(); }

    /**
     * Sets the popup window, if one added to this root view during last event.
     */
    protected void setPopup(PopupWindow aPopup)  { _eventDispatcher.setPopup(aPopup); }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, getRootView(), aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, getRootView(), aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, getRootView(), true, true);
    }

    /**
     * Returns an array of all open windows.
     */
    public static WindowView[] getOpenWindows()  { return _openWins.toArray(new WindowView[0]); }

    /**
     * Returns an array of all open windows.
     */
    public static <T extends ViewOwner> T getOpenWindowOwner(Class <T> aClass)
    {
        for (WindowView window : _openWins) {
            ViewOwner owner = window.getOwner();
            if (owner != null && (aClass == null || aClass.isAssignableFrom(owner.getClass())))
                return (T) owner;
        }

        // Return not found
        return null;
    }

    /**
     * Returns an array of all open windows.
     */
    public static <T extends ViewOwner> T[] getOpenWindowOwners(Class <T> aClass)
    {
        List <T> viewOwners = new ArrayList<>();
        for (WindowView window : _openWins) {
            ViewOwner owner = window.getOwner();
            if (owner != null && (aClass == null || aClass.isAssignableFrom(owner.getClass())))
                viewOwners.add((T) owner);
        }

        // Return array
        return viewOwners.toArray((T[]) Array.newInstance(aClass, viewOwners.size()));
    }

    /**
     * Returns the location for given rect, position and offsets.
     */
    private Point getRectLocation(Rect aRect, Pos aPos, double aDX, double aDY)
    {
        // Get rect for given node and point for given offsets
        double x = aDX, y = aDY;

        // Modify x for given HPos
        switch(aPos.getHPos()) {
            case LEFT: x += aRect.x; break;
            case CENTER: x += Math.round(aRect.x + (aRect.width - getWidth()) / 2); break;
            case RIGHT: x += aRect.getMaxX() - getWidth(); break;
        }

        // Modify y for given VPos
        switch(aPos.getVPos()) {
            case TOP: y += aRect.y; break;
            case CENTER: y += Math.round(aRect.y + (aRect.height - getHeight()) / 2); break;
            case BOTTOM: y += aRect.getMaxY() - getHeight(); break;
        }

        // Return point
        return new Point(x,y);
    }

    /**
     * Called when all windows closed to exit if still no windows after half second.
     */
    private static void checkForExitWhenAllWindowsClosed()
    {
        if (_openWins.isEmpty())
            System.exit(0);
    }

    /**
     * A class to map snap Window functionality to native platform.
     */
    public abstract static class WindowHpr {

        /** Returns the snap Window. */
        public abstract WindowView getWindow();

        /** Sets the snap Window. */
        public abstract void setWindow(WindowView aWin);

        /** Returns the native being helped. */
        public abstract Object getNative();

        /** Returns the native for the window content. */
        public Object getContentNative()  { return null; }

        /** Registers a view for repaint. */
        public abstract void requestPaint(Rect aRect);

        /** Window method: initializes native window. */
        public abstract void initWindow();

        /** Window/Popup method: Shows the window. */
        public abstract void show();

        /** Window/Popup method: Hides the window. */
        public abstract void hide();

        /** Window/Popup method: Order window to front. */
        public abstract void toFront();

        /** Convert given point x/y from given view to screen. */
        public Point convertViewPointToScreen(View aView, double aX, double aY)
        {
            return aView.localToParent(aX, aY, null);
        }

        /** Window/Popup method: Sets the document file url for the window title bar proxy icon. */
        public void setDocURL(WebURL aURL)  { }

        /** Notifies that focus changed. */
        public void focusDidChange(View aView) { }
    }
}