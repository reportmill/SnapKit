/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.javafx;
import javafx.application.Platform;
import javafx.event.*;
import javafx.geometry.Rectangle2D;
import javafx.stage.*;
import snap.gfx.Rect;
import snap.view.*;

/**
 * A ViewEnv subclass for JavaFX.
 */
public class JFXViewEnv extends ViewEnv {

    // A shared instance.
    static JFXViewEnv         _shared = new JFXViewEnv();
    
/**
 * Returns whether current thread is event dispatch thread.
 */
public boolean isEventThread()  { return Platform.isFxApplicationThread(); }

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return JFXClipboard.get(); }

/**
 * Runs the given runnable in the next event.
 */
public void runLater(Runnable aRun)  { Platform.runLater(aRun); }

/**
 * Creates an event for a UI node.
 */
public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
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
public ViewHelper createHelper(View aView)
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