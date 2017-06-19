package snap.swing;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import snap.gfx.*;
import snap.view.*;
import snap.web.*;

/**
 * A ViewEnv subclass for Swing.
 */
public class SwingViewEnv extends ViewEnv {
    
    // The timer for runIntervals and runDelayed
    java.util.Timer           _timer = new java.util.Timer();
    
    // A map of timer tasks
    Map <Runnable,TimerTask>  _timerTasks = new HashMap();

    // A shared instance.
    static SwingViewEnv       _shared = new SwingViewEnv();

/**
 * Creates a new SwingViewEnv.
 */
public SwingViewEnv()
{
    // Start Font Loading
    AWTFontUtils.getFonts(); //new Thread(() -> AWTFontUtils.getFonts()).start();
    
    // Turn on dyamic layout
    Toolkit.getDefaultToolkit().setDynamicLayout(true);
}

/**
 * Returns whether current thread is event thread.
 */
public boolean isEventThread()  { return SwingUtilities.isEventDispatchThread(); }

/**
 * Run later.
 */
public void runLater(Runnable aRunnable)  { SwingUtilities.invokeLater(aRunnable); }

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
    runLater(() -> { synchronized(SwingViewEnv.this) { aRun.run(); SwingViewEnv.this.notify(); }});
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns a UI file for given class.
 */
public Object getUISource(Class aClass)
{
    WebURL durl = WebURL.getURL(aClass, null);
    WebFile dfile = durl.getFile().getParent();
    String sname = aClass.getSimpleName();
    WebFile file = dfile.getFile(sname + ".snp"); if(file!=null) return file;
    return aClass!=Object.class? getUISource(aClass.getSuperclass()) : null;
}

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return SwingClipboard.get(); }

/**
 * Returns the clipboard for drag and drop.
 */
public Clipboard getClipboardDrag()  { return SwingClipboard.getDrag(null); }

/**
 * Returns a FileChooser.
 */
public FileChooser getFileChooser()  { return new SwingFileChooser(); }

/**
 * Returns a property for given node.
 */
public Object getProp(Object anObj, String aKey)
{
    if(anObj instanceof JComponent) return ((JComponent)anObj).getClientProperty(aKey);
    return super.getProp(anObj, aKey);
}

/**
 * Sets a property for a given native.
 */
public void setProp(Object anObj, String aKey, Object aValue)
{
    if(anObj instanceof JComponent) ((JComponent)anObj).putClientProperty(aKey, aValue);
    else super.setProp(anObj, aKey, aValue);
}

/** Creates the top level properties map. */
protected Map createPropsMap()  { return new WeakHashMap(); }

/**
 * Returns a new ViewHelper for given native component.
 */
public ViewHelper createHelper(View aView)
{
    if(aView instanceof RootView) return new SWRootViewHpr();
    if(aView instanceof WindowView) return new SWWindowHpr();
    return null;
}

/**
 * Creates an event for a UI node.
 */
public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
{
    EventObject eobj = anEvent instanceof EventObject? (EventObject)anEvent : null;
    if(eobj==null && aType==null) aType = ViewEvent.Type.Action;
    
    // Create event, configure and send
    ViewEvent event = new SwingEvent(); event.setView(aView); event.setEvent(eobj); event.setType(aType);
    event.setName(aName!=null? aName : aView!=null? aView.getName() : null);
    return event;
}

/**
 * Returns the screen bounds inset to usable area.
 */
public Rect getScreenBoundsInset()
{
    Dimension ssize = Toolkit.getDefaultToolkit().getScreenSize();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    java.awt.Insets sins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    int sx = sins.left, sy = sins.top, sw = ssize.width - sx - sins.right, sh = ssize.height - sy - sins.bottom;
    return new Rect(sx,sy,sw,sh);
}
    
/**
 * Activates the App giving focus to given node.
 */
public void activateApp(View aView)
{
    // If already focused, just return
    WindowView aWin = aView.getWindow(); if(aWin.isFocused()) return;
    
    // Lets do this in app event thread (not sure I need this)
    if(!isEventThread()) { activateApp(aView); return; }
    
    // Get screen point to click on
    aWin.setAlwaysOnTop(true);
    snap.gfx.Point point2 = aWin.localToScreen(2,2); point2.x += 100; // bogus
    java.awt.Point point1 = java.awt.MouseInfo.getPointerInfo().getLocation();
    
    // Get robot and perform mouse press
    java.awt.Robot robot; try { robot = new java.awt.Robot(); } catch(Exception e) { throw new RuntimeException(e); }
    robot.mouseMove((int)point2.getX(), (int)point2.getY());
    robot.mousePress(java.awt.event.InputEvent.BUTTON1_MASK); robot.delay(20);
    robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_MASK);
    robot.mouseMove(point1.x, point1.y);
    aWin.setAlwaysOnTop(false); aWin.toFront();
}

/**
 * Returns a shared instance.
 */
public static SwingViewEnv get()  { return _shared; }

/**
 * Sets the Swing Node Env.
 */
public static void set()  { AWTEnv.set(); ViewEnv.setEnv(get()); }

/**
 * ViewHelper subclass for RootView/SWRootView.
 */
protected static class SWRootViewHpr <T extends SWRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new SWRootView(); }

    /** Override to set RootView in SWRootView. */
    public void setView(View aView)  { super.setView(aView); get().setRootView((RootView)aView); }
    
    /** Sets the cursor. */
    public void setCursor(snap.view.Cursor aCursor)  { get().setCursor(AWT.get(aCursor)); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { get().repaint(aRect); }
}

}