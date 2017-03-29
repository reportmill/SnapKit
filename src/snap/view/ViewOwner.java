/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.text.DateFormat;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A base controller class class that manages a SwingPanel usually loaded from a rib file.
 */
public class ViewOwner implements EventListener {

    // The UI View
    View                      _ui;
    
    // The RootView
    RootView                  _rview;
    
    // Whether owner will fire events 
    boolean                   _sendEventDisabled;
    
    // A map of named toggle groups
    Map <String,ToggleGroup>  _toggleGroups;
    
    // The first UI view to focus when UI is made visible in window or dialog
    Object                    _firstFocus;
    
    // Map of key combos to action (names)
    Map <KeyCombo,String>     _keyFilters = Collections.EMPTY_MAP, _keyHandlers = _keyFilters;
    
    // A map of binding values not explicitly defined in model
    Map                       _modelValues = new HashMap();
    
    // A map of maps that perform value conversions
    Map <String, Map>         _conversionMaps = new HashMap();
    
    // Map of RunOne runnables
    Map <String,Runnable>     _runOnceMap = new HashMap();
    
    // The view environment
    ViewEnv                   _env = ViewEnv.getEnv();
    
    // Whether UI needs to be reset when next shown
    boolean                   _resetLater;
    
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
    public static final ViewEvent.Type WinActivate = ViewEvent.Type.WinActivate;
    public static final ViewEvent.Type WinDeactivate = ViewEvent.Type.WinDeactivate;
    public static final ViewEvent.Type WinOpen = ViewEvent.Type.WinOpen;
    public static final ViewEvent.Type WinClose = ViewEvent.Type.WinClose;
    public ViewEvent.Type KeyEvents[] = { KeyPress, KeyRelease, KeyType };
    public ViewEvent.Type MouseEvents[] = { MousePress, MouseDrag, MouseRelease,
        MouseEnter, MouseMove, MouseExit };
    public ViewEvent.Type DragEvents[] = { DragEnter, DragExit, DragOver, DragDrop };
    
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
    if(_ui!=null) return _ui;
    _ui = createUI();
    setSendEventDisabled(true);
    initUI();
    _ui.setOwner(this);
    setSendEventDisabled(false);
    resetLater();
    _ui.addPropChangeListener(pce -> uiShowingChanged(), View.Showing_Prop);
    return _ui;
}

/**
 * Returns top level view as given class.
 */
public <T extends View> T getUI(Class <T> aClass)  { return (T)getUI(); }

/**
 * Creates the top level view for this class.
 */
protected View createUI()  { return createUI(getUISource()); }

/**
 * Creates the top level view for given class.
 */
protected View createUI(Class aClass)  { return createUI(_env.getUISource(aClass)); }

/**
 * Creates the top level view for given class.
 */
protected View createUI(Object aSource)
{
    if(aSource==null) return null;
    ViewArchiver arch = new ViewArchiver(); arch.setOwner(this);
    return arch.getView(aSource, null);
}

/**
 * Returns the UI source.
 */
protected Object getUISource()  { return _env.getUISource(getClass()); }

/**
 * Initializes the UI panel.
 */
protected void initUI()  { }

/**
 * Called when UI showing has changed.
 */
protected void uiShowingChanged()
{
    if(_resetLater) {
        resetLater(); _resetLater = false; }
        
    if(getUI().isShowing()) {
        if(getFirstFocus()!=null)
            getView(getFirstFocus()).requestFocus();
    }
}

/**
 * Returns the specific child view for given object (name, event or view).
 */
public View getView(Object anObj)
{
    // If object is View, just return
    if(anObj instanceof View) return (View)anObj;
    
    // If object is String, try to find in UI hierarchy as name
    if(anObj instanceof String) { String name = (String)anObj;
    
        // Look for view in UI hierarchy
        View view = getUI();
        if(name.equals(view.getName())) return view;
        View cview = view instanceof ParentView? ((ParentView)view).getChild(name) : null;
        if(cview!=null) return cview;
        
        // If view not found and RootView.MenuBar is set, look in MenuBar
        cview = isRootViewSet() && getRootView().getMenuBar()!=null? getRootView().getMenuBar().getChild(name) : null;
        return cview;
    }

    // If object is native, have environment get/create view for native
    return null; //_env.getView(anObj);
}

/**
 * Returns the specific child view with the given name as the given class.
 */
public <T> T getView(Object anObj, Class <T> aClass)
{
    return ClassUtils.getInstance(getView(anObj), aClass);
}

/**
 * Returns the object value for a given name or UI view.
 */
public Object getViewValue(Object anObj)  { return getView(anObj).getValue("Value"); }

/**
 * Sets the object value for a given name or UI view.
 */
public void setViewValue(Object anObj, Object aValue)
{
    boolean old = setSendEventDisabled(true);
    getView(anObj).setValue("Value", aValue);
    setSendEventDisabled(old);
}

/**
 * Returns the string value for a given name or UI view.
 */
public String getViewStringValue(Object anObj)  { return SnapUtils.stringValue(getViewValue(anObj)); }

/**
 * Returns the boolean value for a given name or UI view.
 */
public boolean getViewBoolValue(Object anObj)  { return SnapUtils.boolValue(getViewValue(anObj)); }

/**
 * Returns the int value for a given name or UI view.
 */
public int getViewIntValue(Object anObj)  { return SnapUtils.intValue(getViewValue(anObj)); }

/**
 * Returns the float value for a given name or UI view.
 */
public float getViewFloatValue(Object anObj)  { return SnapUtils.floatValue(getViewValue(anObj)); }

/**
 * Returns the text value for a given name or UI view.
 */
public String getViewText(Object anObj)  { return getView(anObj).getText(); }

/**
 * Sets the object value for a given name or UI view.
 */
public void setViewText(Object anObj, String aValue)  { getView(anObj).setText(aValue); }

/**
 * Returns the items for a given name or UI view.
 */
public List getViewItems(Object anObj)  { return getView(anObj, View.Selectable.class).getItems(); }

/**
 * Sets the items for a given name or UI view.
 */
public void setViewItems(Object anObj, List theItems)  { getView(anObj, View.Selectable.class).setItems(theItems); }

/**
 * Sets the items for a given name or UI view.
 */
public void setViewItems(Object anObj, Object theItems[])  { getView(anObj, View.Selectable.class).setItems(theItems); }

/**
 * Returns the selected index for given name or UI view.
 */
public int getViewSelectedIndex(Object anObj)  { return getView(anObj, View.Selectable.class).getSelectedIndex(); }

/**
 * Sets the selected index for given name or UI view.
 */
public void setViewSelectedIndex(Object anObj, int aValue)
{
    boolean old = setSendEventDisabled(true);
    getView(anObj, View.Selectable.class).setSelectedIndex(aValue);
    setSendEventDisabled(old);
}

/**
 * Returns the selected item for given name or UI view.
 */
public Object getViewSelectedItem(Object anObj)  { return getView(anObj, View.Selectable.class).getSelectedItem(); }

/**
 * Sets the selected item for given name or UI view.
 */
public void setViewSelectedItem(Object anObj, Object anItem)
{
    boolean old = setSendEventDisabled(true);
    getView(anObj, View.Selectable.class).setSelectedItem(anItem);
    setSendEventDisabled(old);
}

/**
 * Returns whether given name or UI view is enabled.
 */
public boolean isViewEnabled(Object anObj)  { return getView(anObj).isEnabled(); }

/**
 * Sets whether given name or UI view is enabled.
 */
public void setViewEnabled(Object anObj, boolean aValue)  { getView(anObj).setDisabled(!aValue); }

/**
 * Returns whether given name or UI view is disabled.
 */
public boolean isViewDisabled(Object anObj)  { return getView(anObj).isDisabled(); }

/**
 * Sets whether given name or UI view is disabled.
 */
public void setViewDisabled(Object anObj, boolean aValue)  { getView(anObj).setDisabled(aValue); }

/**
 * Returns an image with given name or path from this class.
 */
public Image getImage(String aPath)  { return Image.get(getClass(), aPath); }

/**
 * Return the toggle group for the given name (creating if needed).
 */
public ToggleGroup getToggleGroup(String aName)
{
    if(_toggleGroups==null) _toggleGroups = new HashMap();
    ToggleGroup tg = _toggleGroups.get(aName);
    if(tg==null) _toggleGroups.put(aName, tg=new ToggleGroup());
    return tg;
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
    View ui = isUISet()? getUI() : null; if(ui==null) return;
    RootView rview = ui.getRootView();
    if(rview==null) _resetLater = true;
    else rview.resetLater(this);
}

/**
 * Called to reset bindings and resetUI().
 */
protected void processResetUI()
{
    boolean old = setSendEventDisabled(true);
    try {
        resetViewBindings(getUI()); // Reset bindings
        this.resetUI();
    }
    finally { setSendEventDisabled(old); }
}

/**
 * Called to invoke respondUI().
 */
protected void processEvent(ViewEvent anEvent)
{
    // Get binding for property name and have it retrieve value
    View view = anEvent.getView();
    Binding binding = anEvent.isActionEvent()? view.getBinding("Value") : null;
    if(binding!=null)
        setBindingModelValue(binding);

    // Call main Owner.respondUI method
    this.respondUI(anEvent);
}

/**
 * Sends an event for a UI view.
 */
public void fireEvent(ViewEvent anEvent)
{
    // If send event is disabled, just return
    if(isSendEventDisabled()) return;
    
    // If no special callback, just call respondUI
    setSendEventDisabled(true);
    try { processEvent(anEvent); }
    finally { setSendEventDisabled(false); }
    
    // Trigger UI reset
    if(anEvent.getTriggersReset() && getUI().isShowing())
        resetLater();
}

/**
 * Adds a binding to a UI view.
 */
public void addViewBinding(Object anObj, String aPropName, String aKeyPath)
{
    View view = getView(anObj);
    view.addBinding(aPropName, aKeyPath);
}

/**
 * Reset bindings for UI view (recurses for children).
 */
protected void resetViewBindings(View aView)
{
    // If Owner of view doesn't match, just return
    //ViewHelper helper = aView.getHelper(); if(helper.isValueAdjusting()) return;
    Object owner = aView.getOwner(); if(owner!=this) return;
    
    // Iterate over view bindings and reset
    for(Binding binding : aView.getBindings())
        setBindingViewValue(binding);
    
    // Iterate over view children and recurse
    if(aView instanceof ParentView) { ParentView pview = (ParentView)aView;
        for(View child : pview.getChildren())
            resetViewBindings(child); }
}

/**
 * Returns the UI view value for the given binding.
 */
protected Object getBindingViewValue(Binding aBinding)
{
    // Get value from UI view
    View view = aBinding.getView(View.class); if(view==null) return null;
    Object value = view.getValue(aBinding.getPropertyName());
    
    // If conversion key is present, do conversion
    String convKey = aBinding.getConversionKey();
    if(convKey!=null)
        value = getConversionMapKey(convKey, value);
    
    // If binding format is available, try to parse
    //if(aBinding.getFormat()!=null && value instanceof String)
    //    try { value = aBinding.getFormat().parseObject((String)value); } catch(Exception e) { }
    
    // Return value
    return value;
}

/**
 * Sets the view value for the given binding from the key value.
 */
protected void setBindingViewValue(Binding aBinding)
{
    View view = aBinding.getView(View.class); if(view==null) return;
    String pname = aBinding.getPropertyName();
    Object value = getBindingModelValue(aBinding);
    view.setValue(pname, value);
}

/**
 * Returns the key value for a given binding.
 */
protected Object getBindingModelValue(Binding aBinding)
{
    // Get binding key and value
    String key = aBinding.getKey();
    Object value = getModelValue(key);
    
    // If conversion key is present, do conversion
    String convKey = aBinding.getConversionKey();
    if(convKey!=null)
        value = getConversionMapValue(convKey, value);
    
    // If format is present, format value
    //if(aBinding.getFormat()!=null && value!=null)
    //    try { value = aBinding.getFormat().format(value); }
    //    catch(Exception e) { System.err.println("ViewOwner.getBindingKeyValue: " + e); }
    
    // This is probably the wrong thing to do - maybe should be in JTextComponentHpr somehow
    if(value instanceof Date)
        value = DateFormat.getDateInstance(DateFormat.MEDIUM).format(value);
    
    // Return value
    return value;
}

/**
 * Sets the key value for the given binding from the UI view.
 */
protected void setBindingModelValue(Binding aBinding)
{
    Object value = getBindingViewValue(aBinding); // Get value from view
    setModelValue(aBinding.getKey(), value); // Set value in model
}

/**
 * Returns the first focus UI view for when window/dialog is made visible.
 */
public Object getFirstFocus()  { return _firstFocus; }

/**
 * Sets the first focus UI view.
 */
public void setFirstFocus(Object anObj)  { _firstFocus = anObj; }

/**
 * Focuses given UI view (name or view).
 */
public void requestFocus(Object anObj)  { View view = getView(anObj); if(view!=null) view.requestFocus(); }

/**
 * Returns whether RootView has been created.
 */
public boolean isRootViewSet()  { return _rview!=null; }

/**
 * Returns the root view (creates, if needed).
 */
public RootView getRootView()
{
    if(_rview!=null) return _rview;
    View ui = getUI();
    RootView rview = createRootView();
    rview.setContent(ui); rview.setOwner(this);
    return _rview = rview;
}

/**
 * Creates the RootView.
 */
protected RootView createRootView()  { return new RootView(); }

/**
 * Returns the Window to manage this ViewOwner's window.
 */
public boolean isWindowSet()  { return isRootViewSet() && getRootView().isWindowSet(); }

/**
 * Returns the Window to manage this ViewOwner's window.
 */
public WindowView getWindow()  { return getRootView().getWindow(); }

/**
 * Returns whether window is visible.
 */
public boolean isWindowVisible()  { return isWindowSet() && getWindow().isVisible(); }

/**
 * Sets whether window is visible.
 */
public void setWindowVisible(boolean aValue)
{
    if(aValue) getWindow().show();
    else getWindow().hide();
}

/**
 * Returns the map of maps, each of which is used to perform value conversions.
 */
public Map <String,Map> getConversionMaps()  { return _conversionMaps; }

/**
 * Returns a named map to perform value conversions.
 */
public Map <String,String> getConversionMap(String aName)
{
    Map map = _conversionMaps.get(aName);
    if(map==null)
        _conversionMaps.put(aName, map = new HashMap());
    return map;
}

/**
 * Converts a UI view value to binder object value using conversion key map.
 */
protected Object getConversionMapKey(String aConversionMapName, Object aValue)
{
    // Get conversion map (just return original object if null)
    Map <String, String> map = getConversionMap(aConversionMapName); if(map==null) return aValue;
    for(Map.Entry entry : map.entrySet()) // Return key for value (just return original object if null)
        if(entry.getValue().equals(aValue.toString()))
            return entry.getKey();
    return aValue.toString(); // Return original object, since value not found in conversion map
}

/**
 * Converts a binder object value to UI view using conversion key map.
 */
public Object getConversionMapValue(String aConversionMapName, Object aKey)
{
    Map map = getConversionMap(aConversionMapName); if(map==null) return aKey;
    String value = (String)map.get(aKey.toString());
    return value!=null? value : aKey.toString();
}

/**
 * Enables events on given object.
 */
public void enableEvents(Object anObj, ViewEvent.Type ... theTypes)
{
    View view = getView(anObj);
    view.addEventHandler(this, theTypes);
}

/**
 * Enables events on given object.
 */
public void disableEvents(Object anObj, ViewEvent.Type ... theTypes)
{
    View view = getView(anObj);
    view.removeEventHandler(this, theTypes);
}

/**
 * Sends an event for a UI view (name or view).
 */
public void sendEvent(Object anObj)
{
    // If view found for object, fire and return
    View view = getView(anObj);
    if(view!=null) {
        view.fireActionEvent(); return; }
    
    // Otherwise, if object is string, create event for UI and fire
    if(anObj instanceof String) { String name = (String)anObj;
        ViewEvent event = _env.createEvent(getUI(), null, null, name);
        fireEvent(event);
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
    boolean old = _sendEventDisabled; _sendEventDisabled = aFlag; return old;
}

/**
 * Registers an event filter to send event with given name to owner.respondUI() on key press for key description.
 * Key description is in Swing KeyStroke string format.
 */
public void addKeyActionFilter(String aName, String aKey)
{
    KeyCombo kcombo = KeyCombo.get(aKey); if(kcombo==null) return;
    if(_keyFilters.size()==0) {
        getUI().addEventFilter(e -> checkKeyActions(e, true), KeyPress); _keyFilters = new HashMap(); }
    _keyFilters.put(kcombo, aName);
}

/**
 * Registers an event handler to send event with given name to owner.respondUI() on key press for key description.
 * Key description is in Swing KeyStroke string format.
 */
public void addKeyActionHandler(String aName, String aKey)
{
    KeyCombo kcombo = KeyCombo.get(aKey); if(kcombo==null) return;
    if(_keyHandlers.size()==0) {
        getUI().addEventHandler(e -> checkKeyActions(e, false), KeyPress); _keyHandlers = new HashMap(); }
    _keyHandlers.put(kcombo, aName);
}

/**
 * Called to check for KeyAction filters/Handlers.
 */
private void checkKeyActions(ViewEvent anEvent, boolean isFilter)
{
    KeyCombo kcombo = anEvent.getKeyCombo();
    String name = isFilter? _keyFilters.get(kcombo) : _keyHandlers.get(kcombo);
    if(name!=null) {
        sendEvent(name); anEvent.consume(); }
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
public void runLaterDelayed(int aDelay, Runnable aRunnable)  { _env.runDelayed(aRunnable, aDelay, true); }

/**
 * Invokes the given runnable for name once (cancels unexecuted previous runLater registered with same name).
 */
public void runLaterOnce(String aName, Runnable aRunnable)
{
    synchronized (_runOnceMap) {
        RunLaterRunnable runnable = (RunLaterRunnable)_runOnceMap.get(aName);
        if(runnable==null) {
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
    String _name; Runnable _runnable;
    RunLaterRunnable(String aName, Runnable aRunnable)  { _name = aName; _runnable = aRunnable; }
    public void run()
    {
        Runnable runnable;
        synchronized (_runOnceMap) { _runOnceMap.remove(_name); runnable = _runnable; }
        if(runnable!=null) runnable.run();
    }
}

/**
 * Returns the model value for given key expression from this ViewOwner.
 */
public Object getModelValue(String aKey)
{
    Object value = GFXEnv.getEnv().getKeyChainValue(this, aKey);
    if(value==null) value = GFXEnv.getEnv().getKeyChainValue(_modelValues, aKey);
    return value;
}

/**
 * Sets the model value for given key expression and value for this ViewOwner.
 */
public void setModelValue(String aKey, Object aValue)
{
    try { GFXEnv.getEnv().setKeyChainValue(this, aKey, aValue); }
    catch(Exception e) { GFXEnv.getEnv().setKeyChainValue(_modelValues, aKey, aValue); }
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