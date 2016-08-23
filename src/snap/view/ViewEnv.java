package snap.view;
import java.util.*;
import snap.gfx.Rect;

/**
 * An interface for platform specific UI functionality.
 */
public abstract class ViewEnv {

    // Map of RunOne runnables
    Map <String,Runnable>   _runOnceMap = new HashMap();
    
    // The node environment
    static ViewEnv          _env;
    
    // Weak map of properties
    static Map<Object,Map>  _props = new WeakHashMap();

/**
 * Returns the node environment.
 */
public static ViewEnv getEnv()  { return _env!=null? _env : (_env=snap.swing.SwingViewEnv.get()); }

/**
 * Pushes an environment.
 */
public static void setEnv(ViewEnv anEnv)  { _env = anEnv; }

/**
 * Returns whether current thread is event thread.
 */
public abstract boolean isEventThread();

/**
 * Run given runnable on event thread.
 */
public abstract void runLater(Runnable aRun);

/**
 * Invokes the given runnable for name once (cancels unexecuted previous runLater registered with same name).
 */
public void runLaterOnce(String aName, Runnable aRun)
{
    synchronized (_runOnceMap) {
        RunLaterRunnable runnable = (RunLaterRunnable)_runOnceMap.get(aName);
        if(runnable==null) {
            _runOnceMap.put(aName, runnable = new RunLaterRunnable(aName, aRun));
            runLater(runnable);
        }
        else runnable._run = aRun;
    }
}

/**
 * A wrapper Runnable for RunLaterOnce. 
 */
private class RunLaterRunnable implements Runnable {
    String _name; Runnable _run;
    RunLaterRunnable(String aName, Runnable aRun)  { _name = aName; _run = aRun; }
    public void run()
    {
        Runnable run;
        synchronized (_runOnceMap) { _runOnceMap.remove(_name); run = _run; }
        if(run!=null) run.run();
    }
}

/**
 * Runs given runnable after delay.
 */
public abstract void runDelayed(Runnable aRun, int aDelay, boolean inAppThread);

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public abstract void runIntervals(Runnable aRun, int aPeriod, int aDelay, boolean doAll, boolean inAppThread);

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public abstract void stopIntervals(Runnable aRun);

/**
 * Returns a UI source for given class.
 */
public Object getUISource(Class aClass)  { throw notImpl("getUIFile"); }

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { throw notImpl("getClipboard"); }

/**
 * Returns a FileChooser.
 */
public FileChooser getFileChooser()  { throw notImpl("getFileChooser"); }

/**
 * Returns a View for given object (View or native).
 */
public View getView(Object anObj)
{
    if(anObj instanceof View) return (View)anObj;
    return (View)getProp(anObj, "View");
}

/**
 * Returns a ViewHelper for given native component.
 */
public ViewHelper createHelper(View aView)  { return null; }

/**
 * Returns a property for given node.
 */
public Object getProp(Object anObj, String aKey)  { return getProps(anObj).get(aKey); }

/**
 * Sets a property for a given native.
 */
public void setProp(Object anObj, String aKey, Object aValue)  { getProps(anObj).put(aKey, aValue); }

/**
 * Returns the properties for given object.
 */
protected Map getProps(Object anObj)
{
    Map props = _props.get(anObj);
    if(props==null) _props.put(anObj, props=new HashMap());
    return props;
}

/**
 * Sends an event for a UI node.
 */
public abstract ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName);

/**
 * Returns the screen bounds inset to usable area.
 */
public Rect getScreenBoundsInset()  { complain("getScreenBoundsInset"); return null; }

/**
 * Activates the App giving focus to given node.
 */
public void activateApp(View aView)  { complain("activateWindow"); }

/** Returns a "not implemented" exception for string (method name). */
RuntimeException notImpl(String s)  { return new RuntimeException(getMsg(s)); }
protected void complain(String s)  { s = getMsg(s); if(!_cmpln.contains(s)) System.err.println(s); _cmpln.add(s); }
String getMsg(String s) { return getClass().getName() + ": Not implemented:" + s; }
static Set _cmpln = new HashSet();

}