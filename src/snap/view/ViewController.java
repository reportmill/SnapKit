/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.props.PropObject;
import snap.util.*;

/**
 * A base controller class that manages a UI View.
 * <br><br>
 * A standard ViewController works around five simple methods:
 * <ol>
 *     <li>{@link #createUI()}</li>
 *     <li>{@link #initUI()}</li>
 *     <li>{@link #resetUI()}</li>
 *     <li>{@link #respondUI(ViewEvent)}</li>
 * </ol>
 * <br>
 * <b>createUI()</b><br>
 * Called by SnapKit when the View is initialized, this method is responsible for the creation of the view, and all of
 * its components. The view can be created by hand, or loaded from a .snp file through the use of
 * {@link UILoader#loadViewForController(ViewController)}.
 * <br><br>
 * <b>initUI()</b><br>
 * Similar to {@code createUI()}, this method is also called during initialization, after {@code createUI()} has been
 * run. This method is responsible for any additional initialization that needs to take place after the view has been
 * created, such as setting settings.
 * <br><br>
 * <b>resetUI()</b><br>
 * resetUI() is called automatically by SnapKit whenever the user reacts with any UI component, but will not cause
 * accidental {@code respondUI(ViewEvent)} calls. It allows the user to reset or change aspects of the UI after an
 * interaction, such as might be required for an animation or image draw.
 * <br><br>
 * <b>respondUI(ViewEvent)</b><br>
 * Called automatically by SnapKit whenever a ViewEvent/user event has been detected. Use this method to provide
 * interactions and feedback to buttons and other user tools. In Swing, the listeners are attached to individual
 * components, but in SnapKit all of the controls are provided in a single place, allowing pieces to be swapped in
 * and out as necessary.
 * <br><br>
 */
public abstract class ViewController extends PropObject {

    // The UI View
    private View  _ui;
    
    // The Window
    private WindowView  _win;
    
    // Whether controller will fire events
    private boolean  _sendEventDisabled;
    
    // A map of named toggle groups
    private Map <String,ToggleGroup>  _toggleGroups;
    
    // The first UI view to focus when UI is made visible in window or dialog
    private Object  _firstFocus;
    
    // Map of key combos to action (names)
    private Map <KeyCombo,String>  _keyFilters = Collections.emptyMap();

    // Map of key combos to action (names)
    private Map <KeyCombo,String>  _keyHandlers = Collections.emptyMap();

    // Whether view controller UI is showing
    private boolean  _showing;

    // Whether initShowing has happened
    private boolean  _initShowingDone;

    // The view environment
    private ViewEnv  _env = ViewEnv.getEnv();
    
    // Whether UI needs to be reset when next shown
    private boolean  _resetLater;
    
    // Whether this view controller should suppress the next automatic reset that normally happens after respondUI
    private boolean  _cancelReset;

    // Constants for properties
    public static final String Showing_Prop = "Showing";
    
    // Convenience for common events
    public static final ViewEvent.Type Action = ViewEvent.Type.Action;
    public static final ViewEvent.Type KeyPress = ViewEvent.Type.KeyPress;
    public static final ViewEvent.Type KeyRelease = ViewEvent.Type.KeyRelease;
    public static final ViewEvent.Type KeyType = ViewEvent.Type.KeyType;
    public static final ViewEvent.Type MousePress = ViewEvent.Type.MousePress;
    public static final ViewEvent.Type MouseDrag = ViewEvent.Type.MouseDrag;
    public static final ViewEvent.Type MouseRelease = ViewEvent.Type.MouseRelease;
    public static final ViewEvent.Type MouseEnter = ViewEvent.Type.MouseEnter;
    public static final ViewEvent.Type MouseMove = ViewEvent.Type.MouseMove;
    public static final ViewEvent.Type MouseExit = ViewEvent.Type.MouseExit;
    public static final ViewEvent.Type DragEnter = ViewEvent.Type.DragEnter;
    public static final ViewEvent.Type DragOver = ViewEvent.Type.DragOver;
    public static final ViewEvent.Type DragExit = ViewEvent.Type.DragExit;
    public static final ViewEvent.Type DragDrop = ViewEvent.Type.DragDrop;
    public static final ViewEvent.Type DragGesture = ViewEvent.Type.DragGesture;
    public static final ViewEvent.Type WinClose = ViewEvent.Type.WinClose;
    public ViewEvent.Type[] KeyEvents = ViewEvent.Type.KeyEvents;
    public ViewEvent.Type[] MouseEvents = ViewEvent.Type.MouseEvents;
    public ViewEvent.Type[] DragEvents = ViewEvent.Type.DragEvents;

    /**
     * Constructor.
     */
    public ViewController()  { }

    /**
     * Constructor with given View for UI.
     */
    public ViewController(View aView)
    {
        _ui = aView;
        _ui.setController(this);
        setFirstFocus(aView);
        _ui.addPropChangeListener(pc -> setShowing(_ui.isShowing()), View.Showing_Prop);
    }

    /**
     * Returns whether UI has been set.
     */
    public boolean isUISet()  { return _ui != null; }

    /**
     * Returns top level view.
     */
    public synchronized View getUI()
    {
        // If already set, just return
        if (_ui != null) return _ui;

        // Create UI
        _ui = createUI();

        // Init UI
        setSendEventDisabled(true);
        initUI();
        _ui.setController(this);
        setSendEventDisabled(false);

        // Register for reset and bind UI.Showing to ViewController.Showing
        resetLater();
        _ui.addPropChangeListener(pc -> setShowing(_ui.isShowing()), View.Showing_Prop);

        // Return
        return _ui;
    }

    /**
     * Returns top level view as given class.
     */
    public <T extends View> T getUI(Class <T> aClass)  { return aClass.cast(getUI()); }

    /**
     * Creates the top level view for this controller.  
     * <br><br>
     * This method is called automatically by SnapKit at initialization, and does not need to be called inside an
     * implementation.
     * <br><br>
     * Implementation Note: This is where all components and members of the view should be composed and initialized. 
     * This can be done by hand, or through use of the {@link UILoader#loadViewForController(ViewController)} loader
     * method.
     * @see UILoader#loadViewForController(ViewController) 
     */
    abstract protected View createUI();

    /**
     * Initializes the UI panel. This method provides the ability to alter any settings or components of the View that
     * were not set by {@link #createUI()}.
     * <br><br>
     * This method is called automatically by SnapKit after the view has been initialized, and does not need to be
     * called inside of an implementation.
     * <br><br>
     * Implementation note: It is not always necessary to implement this method, especially if the {@code createUI()}
     * method was written by hand. It provides a way to add more initialization logic when the class has been loaded
     * from a .snp file.
     */
    abstract protected void initUI();

    /**
     * Returns the first focus UI view for when window/dialog is made visible.
     */
    public Object getFirstFocus()  { return _firstFocus; }

    /**
     * Sets the first focus UI view.
     */
    public void setFirstFocus(Object anObj)  { _firstFocus = anObj; }

    /**
     * Returns whether UI is showing.
     */
    public boolean isShowing()  { return _showing; }

    /**
     * Sets whether UI is showing.
     */
    protected void setShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue == _showing) return;

        // Set value
        _showing = aValue;

        // Handle Showing true
        if (aValue) {

            // Handle needs resetLater
            if (_resetLater) {
                resetLater();
                _resetLater = false;
            }

            // Handle FirstFocus and initShowing
            if (!_initShowingDone) {

                // Handle FirstFocus: If set, requestFocus and select text
                Object firstFocus = getFirstFocus();
                View firstFocusView = firstFocus != null ? getView(firstFocus) : null;
                if (firstFocusView != null) {
                    firstFocusView.requestFocus();
                    if (firstFocusView instanceof TextField)
                        ((TextField) firstFocusView).selectAll();
                }

                // Trigger initShowing
                initShowing();
                _initShowingDone = true;
            }
        }

        // Fire Prop Change
        firePropChange(Showing_Prop, !_showing, _showing);
    }

    /**
     * Called when ViewController is first shown.
     */
    protected void initShowing()  { }

    /**
     * Returns the specific child view for given object (name, event or view).
     */
    public View getView(Object anObj)
    {
        // If object is View, just return
        if (anObj instanceof View)
            return (View) anObj;

        // If object is String, try to find in UI hierarchy as name
        if (anObj instanceof String name) {

            // If given name is UI name, return UI
            View uiView = getUI();
            if (name.equals(uiView.getName()))
                return uiView;

            // If UI is ParentView, forward to ParentView.getChildForName()
            if (uiView instanceof ParentView) {
                View childView = ((ParentView) uiView).getChildForName(name);
                if (childView != null)
                    return childView;
            }
        }

        // Return not found
        return null;
    }

    /**
     * Returns the specific child view with the given name as the given class.
     */
    public <T> T getView(Object anObj, Class <T> aClass)
    {
        View view = getView(anObj);
        return ClassUtils.getInstance(view, aClass);
    }

    /**
     * Returns the object value for a given name or UI view.
     */
    public Object getViewValue(Object anObj)
    {
        View view = getView(anObj);
        if (view == null) {
            System.out.println("ViewController.getViewValue: Couldn't find view for: " + anObj);
            return null;
        }
        return view.getPropValue("Value");
    }

    /**
     * Sets the object value for a given name or UI view.
     */
    public void setViewValue(Object anObj, Object aValue)
    {
        boolean old = setSendEventDisabled(true);
        View view = getView(anObj);
        if (view != null)
            view.setPropValue("Value", aValue);
        else System.err.println("ViewController.setViewValue: Couldn't find view for: " + anObj);
        setSendEventDisabled(old);
    }

    /**
     * Returns the string value for a given name or UI view.
     */
    public String getViewStringValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return Convert.stringValue(value);
    }

    /**
     * Returns the boolean value for a given name or UI view.
     */
    public boolean getViewBoolValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return Convert.boolValue(value);
    }

    /**
     * Returns the int value for a given name or UI view.
     */
    public int getViewIntValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return Convert.intValue(value);
    }

    /**
     * Returns the float value for a given name or UI view.
     */
    public float getViewFloatValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return Convert.floatValue(value);
    }

    /**
     * Returns the text value for a given name or UI view.
     */
    public String getViewText(Object anObj)
    {
        View view = getView(anObj);
        return view.getText();
    }

    /**
     * Sets the object value for a given name or UI view.
     */
    public void setViewText(Object anObj, String aValue)
    {
        View view = getView(anObj);
        view.setText(aValue);
    }

    /**
     * Returns the items for a given name or UI view.
     */
    public List<?> getViewItems(Object anObj)
    {
        Selectable<?> selectable = getView(anObj, Selectable.class);
        return selectable.getItems();
    }

    /**
     * Sets the items for a given name or UI view.
     */
    public void setViewItems(Object anObj, List<?> theItems)
    {
        Selectable selectable = getView(anObj, Selectable.class);
        selectable.setItems(theItems);
    }

    /**
     * Sets the items for a given name or UI view.
     */
    public void setViewItems(Object anObj, Object[] theItems)
    {
        Selectable<Object> selectable = getView(anObj, Selectable.class);
        selectable.setItems(theItems);
    }

    /**
     * Returns the selected index for given name or UI view.
     */
    public int getViewSelIndex(Object anObj)
    {
        Selectable<?> selectable = getView(anObj, Selectable.class);
        return selectable.getSelIndex();
    }

    /**
     * Sets the selected index for given name or UI view.
     */
    public void setViewSelIndex(Object anObj, int aValue)
    {
        boolean old = setSendEventDisabled(true);
        Selectable<?> selectable = getView(anObj, Selectable.class);
        selectable.setSelIndex(aValue);
        setSendEventDisabled(old);
    }

    /**
     * Returns the selected item for given name or UI view.
     */
    public Object getViewSelItem(Object anObj)  { return getView(anObj, Selectable.class).getSelItem(); }

    /**
     * Sets the selected item for given name or UI view.
     */
    public void setViewSelItem(Object anObj, Object anItem)
    {
        boolean old = setSendEventDisabled(true);
        Selectable<Object> selectable = getView(anObj, Selectable.class);
        selectable.setSelItem(anItem);
        setSendEventDisabled(old);
    }

    /**
     * Returns whether given name or UI view is enabled.
     */
    public boolean isViewEnabled(Object anObj)
    {
        View view = getView(anObj);
        return view.isEnabled();
    }

    /**
     * Sets whether given name or UI view is enabled.
     */
    public void setViewEnabled(Object anObj, boolean aValue)
    {
        View view = getView(anObj);
        view.setDisabled(!aValue);
    }

    /**
     * Returns whether given name or UI view is disabled.
     */
    public boolean isViewDisabled(Object anObj)
    {
        View view = getView(anObj);
        return view.isDisabled();
    }

    /**
     * Sets whether given name or UI view is disabled.
     */
    public void setViewDisabled(Object anObj, boolean aValue)
    {
        View view = getView(anObj);
        view.setDisabled(aValue);
    }

    /**
     * Returns whether given name or UI view is visible.
     */
    public boolean isViewVisible(Object anObj)
    {
        View view = getView(anObj);
        return view.isVisible();
    }

    /**
     * Sets whether given name or UI view is visible.
     */
    public void setViewVisible(Object anObj, boolean aValue)
    {
        View view = getView(anObj);
        view.setVisible(aValue);
    }

    /**
     * Adds an event handler (for given types) to view for given name or view.
     */
    public void addViewEventHandler(Object anObj, EventListener aListener, ViewEvent.Type ... theTypes)
    {
        View view = getView(anObj);
        if (view != null)
            view.addEventHandler(aListener, theTypes);
    }

    /**
     * Returns an image with given name or path from this class.
     */
    public Image getImage(String aPath)
    {
        Class<?> cls = getClass();
        return Image.getImageForClassResource(cls, aPath);
    }

    /**
     * Return the toggle group for the given name (creating if needed).
     */
    public ToggleGroup getToggleGroup(String aName)
    {
        // Create ToggleGroups map if absent
        if (_toggleGroups == null)
            _toggleGroups = new HashMap<>();

        // Get cached toggleGroup - create/add if absent
        ToggleGroup toggleGroup = _toggleGroups.get(aName);
        if (toggleGroup == null)
            _toggleGroups.put(aName, toggleGroup = new ToggleGroup());

        // Return
        return toggleGroup;
    }

    /**
     * Called automatically by SnapKit after a user reacts with a UI component, this method allows the resetting of
     * the UI. It will not cause accidental {@code respondUI(ViewEvent)} calls. It allows the user to reset or change
     * aspects of the UI after an interaction, such as might be required for an animation or image draw.
     * <br> <br>
     * This method is overridable with no default implementation.
     */
    abstract protected void resetUI();

    /**
     * Called automatically by SnapKit when it detects a ViewEvent. This method should be overridden to respond to UI
     * controls, and provide feedback to user interactions.
     * <br>
     * If you are coming from a Swing environment, this class serves the same purposes as the action listeners attached
     * to each individual component. In this case, all of the events are funnelled into the same method, making it
     * easier to keep track of interactions. Everything is managed from the same location.
     */
    abstract protected void respondUI(ViewEvent anEvent);

    /**
     * Resets UI later.
     */
    public void resetLater()
    {
        // Get UI
        View ui = isUISet() ? getUI() : null;
        if (ui == null)
            return;

        // Get updater and forward
        ViewUpdater updater = ui.getUpdater();
        if (updater == null)
            _resetLater = true;
        else updater.resetLater(this);
    }

    /**
     * Whether this ViewController should suppress the next automatic reset that normally happens after respondUI.
     */
    protected void cancelReset()  { _cancelReset = true; }

    /**
     * A wrapper to call resetUI() properly so resulting view action events are ignored.
     */
    protected void invokeResetUI()
    {
        // Set SendEventDisabled to ignore action events
        boolean old = setSendEventDisabled(true);

        // Reset UI
        try {
            this.resetUI();
        }

        // Always reset SendEventDisabled
        finally {
            setSendEventDisabled(old);
        }
    }

    /**
     * A wrapper to call respondUI() properly so resulting view action events are ignored.
     */
    protected void invokeRespondUI(ViewEvent anEvent)
    {
        // If send event is disabled, just return
        if (isSendEventDisabled()) return;

        // Set SendEventDisabled to ignore action events
        setSendEventDisabled(true);

        // Call respondUI
        try {
            this.respondUI(anEvent);
        }

        // Always clear SendEventDisabled
        finally {
            setSendEventDisabled(false);
        }

        // Trigger UI reset
        if (!_cancelReset && getUI().isShowing())
            resetLater();
        _cancelReset = false;
    }

    /**
     * Sends an event to this ViewController through processEvent method.
     */
    public void dispatchEventToController(ViewEvent anEvent)
    {
        invokeRespondUI(anEvent);
    }

    /**
     * Triggers and action event for a UI view (name or view).
     */
    public void fireActionEventForObject(Object anObj, ViewEvent parentEvent)
    {
        // If view found for object, fire and return
        View view = getView(anObj);
        if (view != null && view.isEnabled()) {
            view.fireActionEvent(parentEvent);
            return;
        }

        // Otherwise, if object is string, create event for UI and fire
        if (anObj instanceof String name) {
            ViewEvent event = ViewEvent.createEvent(getUI(), null, View.Action, name);
            if (parentEvent != null)
                event.setParentEvent(parentEvent);
            dispatchEventToController(event);
        }
    }

    /**
     * Returns whether send event facility is disabled (so controls can be updated without triggering response).
     */
    public boolean isSendEventDisabled()  { return _sendEventDisabled; }

    /**
     * Sets whether send event facility is disabled (so controls can be updated without triggering response).
     */
    public boolean setSendEventDisabled(boolean aFlag)
    {
        boolean old = _sendEventDisabled;
        _sendEventDisabled = aFlag;
        return old;
    }

    /**
     * Registers an event filter to send event with given name to controller.respondUI() on key press for key description.
     * Key description is in Swing KeyStroke string format.
     */
    public void addKeyActionFilter(String aName, String aKey)
    {
        // Get KeyCombo
        KeyCombo keyCombo = KeyCombo.get(aKey);

        // If first, do init
        if (_keyFilters.isEmpty()) {
            View ui = getUI();
            ui.addEventFilter(e -> checkKeyActions(e, true), KeyPress);
            _keyFilters = new HashMap<>();
        }

        // Add KeyCombo
        _keyFilters.put(keyCombo, aName);
    }

    /**
     * Registers an event handler to send event with given name to controller.respondUI() on key press for key description.
     * Key description is in Swing KeyStroke string format.
     */
    public void addKeyActionHandler(String aName, String aKey)
    {
        // Get KeyCombo
        KeyCombo keyCombo = KeyCombo.get(aKey);

        // If first, do init
        if (_keyHandlers.isEmpty()) {
            View ui = getUI();
            ui.addEventHandler(e -> checkKeyActions(e, false), KeyPress);
            _keyHandlers = new HashMap<>();
        }

        // Add KeyCombo
        _keyHandlers.put(keyCombo, aName);
    }

    /**
     * Called to check for KeyAction filters/Handlers.
     */
    private void checkKeyActions(ViewEvent anEvent, boolean isFilter)
    {
        // Get name for action associated with given key event
        KeyCombo keyCombo = anEvent.getKeyCombo();
        String name = isFilter ? _keyFilters.get(keyCombo) : _keyHandlers.get(keyCombo);

        // If found, send action
        if (name != null) {
            fireActionEventForObject(name, anEvent);
            if (!isFilter)
                anEvent.consume();
        }
    }

    /**
     * Focuses given UI view (name or view).
     */
    public void requestFocus(Object anObj)
    {
        View view = getView(anObj);
        if (view != null)
            view.requestFocus();
    }

    /**
     * Returns whether Window has been created (happens when first accessed).
     */
    public boolean isWindowSet()  { return _win != null; }

    /**
     * Returns the Window to manage this ViewController's window.
     */
    public WindowView getWindow()
    {
        // If already set, just return
        if (_win != null) return _win;

        // If UI already has window, return it
        View ui = getUI();
        WindowView win = ui.getParent(WindowView.class);
        if (win != null)
            return win;

        // Create window and init
        _win = new WindowView();
        initWindow(_win);

        // Set content to UI and set controller to this ViewController
        _win.setContent(ui);
        _win.setController(this);

        // Return
        return _win;
    }

    /**
     * Initialize window.
     */
    protected void initWindow(WindowView aWindow)  { }

    /**
     * Returns whether window is visible.
     */
    public boolean isWindowVisible()
    {
        return isWindowSet() && getWindow().isVisible();
    }

    /**
     * Sets whether window is visible.
     */
    public void setWindowVisible(boolean aValue)
    {
        WindowView win = getWindow();
        if (aValue)
            win.showCentered(null);
        else win.hide();
    }

    /**
     * Returns whether current thread is event thread.
     */
    protected boolean isEventThread()  { return _env.isEventThread(); }

    /**
     * Runs the given runnable in the next event.
     */
    public void runLater(Runnable aRunnable)  { _env.runLater(aRunnable); }

    /**
     * Runs the runnable after the given delay in milliseconds.
     */
    public void runDelayed(Runnable aRunnable, int aDelay)  { _env.runDelayed(aRunnable, aDelay); }

    /**
     * Plays a beep.
     */
    public void beep()  { ViewUtils.beep(); }

    /**
     * Returns the ViewEnv for this controller.
     */
    public ViewEnv getEnv()  { return _env; }

    /**
     * Called when ViewTheme changes.
     */
    protected void handleThemeChange(ViewTheme oldTheme, ViewTheme newTheme)  { }
}