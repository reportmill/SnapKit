/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.Rect;
import snap.util.SnapUtils;
import snap.web.WebURL;

/**
 * An interface for platform specific UI functionality.
 */
public abstract class ViewEnv {

    // Map of Run-Once runnables
    Set <Runnable>            _runOnceRuns = Collections.synchronizedSet(new HashSet());
    
    // Map of Run-Once names
    Set <String>              _runOnceNames = Collections.synchronizedSet(new HashSet());
    
    // The timer for runIntervals and runDelayed
    java.util.Timer           _timer = new java.util.Timer();
    
    // A map of timer tasks
    Map <Runnable,TimerTask>  _timerTasks = new HashMap();
    
    // The node environment
    static ViewEnv            _env;
    
/**
 * Returns the node environment.
 */
public static ViewEnv getEnv()
{
    if(_env==null) setDefaultEnv();
    return _env;
}

/**
 * Pushes an environment.
 */
public static void setEnv(ViewEnv anEnv)  { _env = anEnv; }

/**
 * Sets the SwingEnv.
 */
public static void setDefaultEnv()
{
    // If Cheerp, try to install Cheerp
    if(SnapUtils.getPlatform()==SnapUtils.Platform.CHEERP) {
        try { Class.forName("snapcj.CJViewEnv").getMethod("set").invoke(null); return; }
        catch(Exception e) { System.err.println("ViewEnv.setDefaultEnv: Can't set CJViewEnv: " + e); }
    }
    
    // Try Swing
    try { Class.forName("snap.swing.SwingViewEnv").getMethod("set").invoke(null); }
    catch(Exception e) { System.err.println("ViewEnv.setDefaultEnv: Can't set SwingViewEnv: " + e); }
}

/**
 * Returns whether current thread is event thread.
 */
public abstract boolean isEventThread();

/**
 * Run given runnable on event thread.
 */
public abstract void runLater(Runnable aRun);

/**
 * Runs the given runnable once.
 */
public void runLaterOnce(Runnable aRun)
{
    // If runnable already queued, just return
    if(_runOnceRuns.contains(aRun)) return;

    // Queue runnable    
    _runOnceRuns.add(aRun);
    runLater(() -> { aRun.run(); _runOnceRuns.remove(aRun); });
}

/**
 * Runs the given runnable for name once.
 */
public void runLaterOnce(String aName, Runnable aRun)
{
    // If runnable already queued, just return
    if(_runOnceNames.contains(aName)) return;

    // Queue name and runnable    
    _runOnceNames.add(aName);
    runLater(() -> { aRun.run(); _runOnceNames.remove(aName); });
}

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
    // Create task
    TimerTask task = new TimerTask() { public void run()  {
        if(inAppThread) {
            if(doAll) runLater(aRun);
            else runLaterAndWait(aRun);
        }
        else aRun.run();
    }};
    
    // Add task and schedule
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

/**
 * Runs an runnable later and waits for it to finish.
 */
public synchronized void runLaterAndWait(Runnable aRun)
{
    // If isEventThread, just run and return
    if(isEventThread()) { aRun.run(); return; }
    
    // Register for runLater() to run and notify
    runLater(() -> runAndNotify(aRun));
    
    // Wait for runAndNotify
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/** Run and notify - used to resume thread that called runLaterAndWait. */
private synchronized void runAndNotify(Runnable aRun)
{
    aRun.run();
    notify();
}

/**
 * Returns a UI source for given class.
 */
public Object getUISource(Class aClass)
{
    String name = aClass.getSimpleName() + ".snp";
    WebURL url = WebURL.getURL(aClass, name); if(url!=null) return url;
    return aClass!=Object.class? getUISource(aClass.getSuperclass()) : null;
}

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { throw notImpl("getClipboard"); }

/**
 * Returns the clipboard for dragging.
 */
public Clipboard getClipboardDrag()  { throw notImpl("getClipboardDrag"); }

/**
 * Returns a FileChooser.
 */
public FileChooser getFileChooser()  { throw notImpl("getFileChooser"); }

/**
 * Returns a ViewHelper for given native component.
 */
public ViewHelper createHelper(View aView)  { return null; }

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