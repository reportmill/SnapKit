package snap.javafx;
import java.util.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.*;
import snap.gfx.Rect;
import snap.view.*;
import snap.web.*;

/**
 * A custom class.
 */
public class JFXViewEnv extends ViewEnv {

    // The timer for runIntervals and runDelayed
    java.util.Timer           _timer = new java.util.Timer();
    
    // A map of timer tasks
    Map <Runnable,TimerTask>  _timerTasks = new HashMap();

    // A shared instance.
    static JFXViewEnv         _shared = new JFXViewEnv();
    
/**
 * Returns whether current thread is event dispatch thread.
 */
public boolean isEventThread()  { return Platform.isFxApplicationThread(); }

/**
 * Returns a UI source for given class.
 */
public Object getUISource(Class aClass)
{
    WebURL durl = WebURL.getURL(aClass, aClass.getSimpleName() + ".snp");
    return durl!=null || aClass==Object.class? durl : getUISource(aClass.getSuperclass());
}

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return JFXClipboard.get(); }

/**
 * Runs the given runnable in the next event.
 */
public void runLater(Runnable aRun)  { Platform.runLater(aRun); }

/**
 * Runs given runnable after delay.
 */
public void runDelayed(Runnable aRun, int aDelay, boolean inAppThread)
{
    TimerTask task = new TimerTask() { public void run() { if(inAppThread) runLater(aRun); else aRun.run(); }};
    _timer.schedule(task, aDelay);
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void runIntervals(Runnable aRun, int aPeriod, int aDelay, boolean doAll, boolean inAppThread)
{
    TimerTask task = new TimerTask() { public void run()  { if(inAppThread) runLaterAndWait(aRun); else aRun.run(); }};
    _timerTasks.put(aRun, task);
    if(doAll) _timer.scheduleAtFixedRate(task, aDelay, aPeriod);
    else _timer.schedule(task, aDelay, aPeriod);
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void stopIntervals(Runnable aRun)
{
    TimerTask task = _timerTasks.get(aRun);
    if(task!=null) task.cancel();
}

/** Runs an runnable later and waits for it to finish. */
private synchronized void runLaterAndWait(Runnable aRun)
{
    runLater(() -> { synchronized(JFXViewEnv.this) { aRun.run(); JFXViewEnv.this.notify(); }});
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Gets property for node.
 */
public Object getProp(Object anObj, String aKey)
{
    if(anObj instanceof Node) return ((Node)anObj).getProperties().get(aKey);
    return super.getProp(anObj, aKey);
}

/**
 * Sets property for node.
 */
public void setProp(Object anObj, String aKey, Object aValue)
{
    if(anObj instanceof Node) ((Node)anObj).getProperties().put(aKey, aValue);
    else super.setProp(anObj, aKey, aValue);
}

/** Creates the top level properties map. */
protected Map createPropsMap()  { return new WeakHashMap(); }

/**
 * Creates an event for a UI node.
 */
public ViewEvent createEvent(snap.view.View aView, Object anEvent, ViewEvent.Type aType, String aName)
{
    Object eobj = anEvent instanceof Event || anEvent instanceof java.util.EventObject? anEvent : null;
    if(eobj==null && aType==null) aType = ViewEvent.Type.Action;
    
    // Create event, configure and send
    ViewEvent event = new JFXEvent(); event.setView(aView); event.setEvent(eobj); event.setType(aType);
    event.setName(aName!=null? aName : aView!=null? aView.getName() : null);
    return event;
}

/**
 * Returns a new ViewHelper for given native component.
 */
public ViewHelper createHelper(snap.view.View aView)
{
    if(aView instanceof RootView) return new JFXRootViewHpr();
    if(aView instanceof snap.view.PopupWindow) return new JFXPopupWindowHpr();
    if(aView instanceof WindowView) return new StageHpr();
    return null;
}

/**
 * Returns the screen bounds inset to usable area.
 */
public Rect getScreenBoundsInset()
{
    Rectangle2D sbounds = Screen.getPrimary().getVisualBounds();
    return new Rect(sbounds.getMinX(), sbounds.getMinY(), sbounds.getWidth(), sbounds.getHeight());
}

/**
 * Returns a shared instance.
 */
public static JFXViewEnv get()  { return _shared; }

/**
 * Sets the JFXViewEnv as environment.
 */
public static void set()  { snap.gfx.GFXEnv.setEnv(JFXGfxEnv.get()); ViewEnv.setEnv(get()); }

}