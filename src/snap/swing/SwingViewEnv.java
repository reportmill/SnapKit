/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.swing;
import java.awt.*;
import java.util.EventObject;
import javax.swing.*;

import snap.geom.Rect;
import snap.view.*;

/**
 * A ViewEnv subclass for Swing.
 */
public class SwingViewEnv extends ViewEnv {
    
    // A shared instance.
    static SwingViewEnv       _shared;

/**
 * Creates a new SwingViewEnv.
 */
public SwingViewEnv()
{
    if (_env==null) {

        // Set vars
        _env = _shared = this;

        // Start Font Loading
        AWTFontUtils.getFonts(); //new Thread(() -> AWTFontUtils.getFonts()).start();

        // Turn on dyamic layout
        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        // Set Printer.Master to SwingPrinter
        SwingPrinter.set();
    }
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
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return SwingClipboard.get(); }

/**
 * Returns the clipboard for drag and drop.
 */
public Clipboard getClipboardDrag()  { return SwingClipboard.getDrag(null); }

/**
 * Returns a new ViewHelper for given native component.
 */
public WindowView.WindowHpr createHelper(View aView)  { return new SWWindowHpr(); }

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
    snap.geom.Point point2 = aWin.localToScreen(2,2); point2.x += 100; // bogus
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
public static SwingViewEnv get()
{
    if (_shared!=null) return _shared;
    return new SwingViewEnv();
}

/**
 * Sets the Swing Node Env.
 */
public static void set()  { AWTEnv.get(); get(); }

}