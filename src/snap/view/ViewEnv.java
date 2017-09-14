/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.Rect;
import snap.web.WebURL;

/**
 * An interface for platform specific UI functionality.
 */
public abstract class ViewEnv {

    // Map of Run-Once runnables
    Set <Runnable>          _runOnceRuns = Collections.synchronizedSet(new HashSet());
    
    // Map of Run-Once names
    Set <String>            _runOnceNames = Collections.synchronizedSet(new HashSet());
    
    // The node environment
    static ViewEnv          _env;
    
/**
 * Returns the node environment.
 */
public static ViewEnv getEnv()
{
    if(_env==null) setSwingEnv();
    return _env;
}

/**
 * Pushes an environment.
 */
public static void setEnv(ViewEnv anEnv)  { _env = anEnv; }

/**
 * Sets the SwingEnv.
 */
public static void setSwingEnv()
{
    try {
        Class cls = Class.forName("snap.swing.SwingViewEnv");
        cls.getMethod("set").invoke(null);
    }
    catch(Exception e) { System.err.println("ViewEnv: No Environment set " + e); }
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
public Object getUISource(Class aClass)
{
    String rname = aClass.getSimpleName() + ".snp";
    WebURL durl = WebURL.getURL(aClass, rname); if(durl!=null) return durl;
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