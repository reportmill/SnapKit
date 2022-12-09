/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.props.PropObject;
import snap.util.*;
import snap.web.WebURL;

/**
 * A base controller class that manages a UI View (usually loaded from a snp UI file).
 */
public class ViewOwner extends PropObject {

    // The UI View
    private View  _ui;
    
    // The Window
    private WindowView  _win;
    
    // Whether owner will fire events 
    private boolean  _sendEventDisabled;
    
    // A map of named toggle groups
    private Map <String,ToggleGroup>  _toggleGroups;
    
    // The first UI view to focus when UI is made visible in window or dialog
    private Object  _firstFocus;
    
    // Map of key combos to action (names)
    private Map <KeyCombo,String>  _keyFilters = Collections.emptyMap();

    // Map of key combos to action (names)
    private Map <KeyCombo,String>  _keyHandlers = Collections.emptyMap();

    // Whether initShowing has happened
    private boolean  _initShowingDone;

    // Map of RunOne runnables
    private final Map<String,Runnable> _runOnceMap = new HashMap<>();
    
    // The view environment
    private ViewEnv  _env = ViewEnv.getEnv();
    
    // Whether UI needs to be reset when next shown
    private boolean  _resetLater;
    
    // Whether this ViewOwner should suppress the next automatic reset that normally happens after respondUI
    private boolean  _cancelReset;

    // The EventListener to listen to view events
    private EventListener  _viewEventListener;
    
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
     * Creates a new ViewOwner.
     */
    public ViewOwner()  { }

    /**
     * Creates a new ViewOwner with given View for UI.
     */
    public ViewOwner(View aView)  { _ui = aView; _ui.setOwner(this); }

    /**
     * Returns whether UI has been set.
     */
    public boolean isUISet()  { return _ui!=null; }

    /**
     * Returns top level view.
     */
    public synchronized View getUI()
    {
        // If UI not present, create, init and set
        if (_ui != null) return _ui;

        // Create UI
        _ui = createUI();

        // Init UI
        setSendEventDisabled(true);
        initUI();
        _ui.setOwner(this);
        setSendEventDisabled(false);

        // Register for reset and showingChanged() and return
        resetLater();
        _ui.addPropChangeListener(pce -> showingChanged(), View.Showing_Prop);
        return _ui;
    }

    /**
     * Returns top level view as given class.
     */
    public <T extends View> T getUI(Class <T> aClass)
    {
        return (T) getUI();
    }

    /**
     * Creates the top level view for this class.
     */
    protected View createUI()
    {
        Object src = getUISource();
        if (src == null) {
            System.err.println("ViewOwner.createUI: Couldn't find source for class: " + getClass().getName());
            throw new RuntimeException("ViewOwner.createUI: Couldn't find source for class: " + getClass().getName());
        }
        return createUIForSource(src);
    }

    /**
     * Creates the top level view for given class.
     */
    protected View createUIForClass(Class<?> aClass)
    {
        WebURL src = _env.getUISource(aClass);
        return createUIForSource(src);
    }

    /**
     * Creates the top level view for given class.
     */
    protected View createUIForSource(Object aSource)
    {
        // Complain if bogus source
        if (aSource == null) {
            System.err.println("ViewOwner.createUI: Can't load from Null Source!");
            return null;
        }

        // Create archiver and return view
        ViewArchiver archiver = new ViewArchiver();
        archiver.setOwner(this);
        return archiver.getView(aSource);
    }

    /**
     * Returns the UI source.
     */
    protected Object getUISource()
    {
        Class<?> cls = getClass();
        return _env.getUISource(cls);
    }

    /**
     * Initializes the UI panel.
     */
    protected void initUI()  { }

    /**
     * Returns the first focus UI view for when window/dialog is made visible.
     */
    public Object getFirstFocus()  { return _firstFocus; }

    /**
     * Sets the first focus UI view.
     */
    public void setFirstFocus(Object anObj)  { _firstFocus = anObj; }

    /**
     * Returns whether ViewOwner UI is showing.
     */
    public boolean isShowing()
    {
        boolean showing = isUISet() && getUI().isShowing();
        return showing;
    }

    /**
     * Called when UI showing has changed.
     */
    protected void showingChanged()
    {
        if (_resetLater) {
            resetLater(); _resetLater = false; }

        if (isShowing() && !_initShowingDone) {
            _initShowingDone = true;

            // Handle First focus
            Object firstFoc = getFirstFocus();
            View view = firstFoc != null ? getView(firstFoc) : null;
            if (view != null) {
                view.requestFocus();
                if (view instanceof TextField)
                    ((TextField) view).selectAll();
            }

            // Trigger initShowing
            initShowing();
        }
    }

    /**
     * Called when ViewOwner is first shown.
     */
    protected void initShowing()  { }

    /**
     * Returns the native object for the UI (JComponent).
     */
    public Object getNative()
    {
        WindowView win = getWindow();
        return win.getContentNative();
    }

    /**
     * Returns the specific child view for given object (name, event or view).
     */
    public View getView(Object anObj)
    {
        // If object is View, just return
        if (anObj instanceof View)
            return (View) anObj;

        // If object is String, try to find in UI hierarchy as name
        if (anObj instanceof String) { String name = (String) anObj;

            // Look for view in UI hierarchy
            View view = getUI();
            if (name.equals(view.getName()))
                return view;
            View childView = view instanceof ParentView ? ((ParentView) view).getChildForName(name) : null;
            if (childView != null)
                return childView;
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
            System.out.println("ViewOwner.getViewValue: Couldn't find view for: " + anObj);
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
        else System.err.println("ViewOwner.setViewValue: Couldn't find view for: " + anObj);
        setSendEventDisabled(old);
    }

    /**
     * Returns the string value for a given name or UI view.
     */
    public String getViewStringValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return SnapUtils.stringValue(value);
    }

    /**
     * Returns the boolean value for a given name or UI view.
     */
    public boolean getViewBoolValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return SnapUtils.boolValue(value);
    }

    /**
     * Returns the int value for a given name or UI view.
     */
    public int getViewIntValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return SnapUtils.intValue(value);
    }

    /**
     * Returns the float value for a given name or UI view.
     */
    public float getViewFloatValue(Object anObj)
    {
        Object value = getViewValue(anObj);
        return SnapUtils.floatValue(value);
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
    public List getViewItems(Object anObj)
    {
        Selectable selectable = getView(anObj, Selectable.class);
        return selectable.getItems();
    }

    /**
     * Sets the items for a given name or UI view.
     */
    public void setViewItems(Object anObj, List theItems)
    {
        Selectable selectable = getView(anObj, Selectable.class);
        selectable.setItems(theItems);
    }

    /**
     * Sets the items for a given name or UI view.
     */
    public void setViewItems(Object anObj, Object[] theItems)
    {
        Selectable selectable = getView(anObj, Selectable.class);
        selectable.setItems(theItems);
    }

    /**
     * Returns the selected index for given name or UI view.
     */
    public int getViewSelIndex(Object anObj)
    {
        Selectable selectable = getView(anObj, Selectable.class);
        return selectable.getSelIndex();
    }

    /**
     * Sets the selected index for given name or UI view.
     */
    public void setViewSelIndex(Object anObj, int aValue)
    {
        boolean old = setSendEventDisabled(true);
        Selectable selectable = getView(anObj, Selectable.class);
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
        Selectable selectable = getView(anObj, Selectable.class);
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
     * Returns an image with given name or path from this class.
     */
    public Image getImage(String aPath)
    {
        Class<?> cls = getClass();
        return Image.get(cls, aPath);
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
     * Reset UI controls.
     */
    protected void resetUI()  { }

    /**
     * Respond to UI controls.
     */
    protected void respondUI(ViewEvent anEvent)  { }

    /**
     * Resets UI later.
     */
    public void resetLater()
    {
        View ui = isUISet() ? getUI() : null; if (ui == null) return;
        ViewUpdater updater = ui.getUpdater();
        if (updater == null)
            _resetLater = true;
        else updater.resetLater(this);
    }

    /**
     * Whether this ViewOwner should suppress the next automatic reset that normally happens after respondUI.
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
     * A hook to allow subclasses to wrap respondUI invocation easier.
     */
    protected void invokeRespondUI(ViewEvent anEvent)
    {
        this.respondUI(anEvent);
    }

    /**
     * Sends an event to this ViewOwner through processEvent method.
     */
    public void dispatchEventToOwner(ViewEvent anEvent)
    {
        // If send event is disabled, just return
        if (isSendEventDisabled()) return;

        // Set SendEventDisabled to ignore action events
        setSendEventDisabled(true);

        // Call respondUI
        try {
            invokeRespondUI(anEvent);
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
     * Enables events on given object.
     */
    public void enableEvents(Object anObj, ViewEvent.Type ... theTypes)
    {
        // Get View and view EventListener
        View view = getView(anObj);
        if (_viewEventListener == null)
            _viewEventListener = e -> dispatchEventToOwner(e);

        // Add EventHandler
        view.addEventHandler(_viewEventListener, theTypes);
    }

    /**
     * Enables events on given object.
     */
    public void disableEvents(Object anObj, ViewEvent.Type ... theTypes)
    {
        View view = getView(anObj);
        view.removeEventHandler(_viewEventListener, theTypes);
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
        if (anObj instanceof String) {
            String name = (String) anObj;
            ViewEvent event = ViewEvent.createEvent(getUI(), null, View.Action, name);
            if (parentEvent != null)
                event.setParentEvent(parentEvent);
            dispatchEventToOwner(event);
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
     * Registers an event filter to send event with given name to owner.respondUI() on key press for key description.
     * Key description is in Swing KeyStroke string format.
     */
    public void addKeyActionFilter(String aName, String aKey)
    {
        // Get KeyCombo
        KeyCombo keyCombo = KeyCombo.get(aKey);
        if (keyCombo == null)
            return;

        // If first, do init
        if (_keyFilters.size() == 0) {
            View ui = getUI();
            ui.addEventFilter(e -> checkKeyActions(e, true), KeyPress);
            _keyFilters = new HashMap();
        }

        // Add KeyCombo
        _keyFilters.put(keyCombo, aName);
    }

    /**
     * Registers an event handler to send event with given name to owner.respondUI() on key press for key description.
     * Key description is in Swing KeyStroke string format.
     */
    public void addKeyActionHandler(String aName, String aKey)
    {
        // Get KeyCombo
        KeyCombo keyCombo = KeyCombo.get(aKey);
        if (keyCombo == null)
            return;

        // If first, do init
        if (_keyHandlers.size() == 0) {
            View ui = getUI();
            ui.addEventHandler(e -> checkKeyActions(e, false), KeyPress);
            _keyHandlers = new HashMap();
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
     * Returns the Window to manage this ViewOwner's window.
     */
    public WindowView getWindow()
    {
        // If already set, just return
        if (_win != null) return _win;

        // Create window, set content to UI, set owner to this ViewOwner and return
        _win = new WindowView();
        View ui = getUI();
        _win.setContent(ui);
        _win.setOwner(this);
        return _win;
    }

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
    protected boolean isEventThread()
    {
        return _env.isEventThread();
    }

    /**
     * Runs the given runnable in the next event.
     */
    public void runLater(Runnable aRunnable)
    {
        _env.runLater(aRunnable);
    }

    /**
     * Runs the runnable after the given delay in milliseconds.
     */
    public void runLaterDelayed(int aDelay, Runnable aRunnable)
    {
        _env.runDelayed(aRunnable, aDelay, true);
    }

    /**
     * Invokes the given runnable for name once (cancels unexecuted previous runLater registered with same name).
     */
    public void runLaterOnce(String aName, Runnable aRunnable)
    {
        synchronized (_runOnceMap) {
            RunLaterRunnable runnable = (RunLaterRunnable) _runOnceMap.get(aName);
            if (runnable == null) {
                _runOnceMap.put(aName, runnable = new RunLaterRunnable(aName, aRunnable));
                runLater(runnable);
            }
            else runnable._runnable = aRunnable;
        }
    }

    /**
     * A wrapper Runnable for RunLaterOnce.
     */
    private class RunLaterRunnable implements Runnable {

        // Ivars
        String _name;
        Runnable _runnable;

        /** Constructor. */
        RunLaterRunnable(String aName, Runnable aRunnable)  { _name = aName; _runnable = aRunnable; }

        /** Run. */
        public void run()
        {
            Runnable runnable;
            synchronized (_runOnceMap) {
                _runOnceMap.remove(_name);
                runnable = _runnable;
            }
            if (runnable != null)
                runnable.run();
        }
    }

    /**
     * Plays a beep.
     */
    public void beep()  { ViewUtils.beep(); }

    /**
     * Returns the ViewEnv for this owner.
     */
    public ViewEnv getEnv()  { return _env; }
}