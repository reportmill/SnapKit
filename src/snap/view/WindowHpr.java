/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Rect;
import snap.web.WebURL;

/**
 * A class to map snap Window functionality to native platform.
 */
public abstract class WindowHpr <T> {
    
    // The window and native
    WindowView         _win;
    T                  _ntv;
    
/** Returns the snap Window. */
public WindowView getWindow()  { return _win; }

/** Sets the snap Window. */
public void setWindow(WindowView aWin)
{
    _win = aWin;
    _ntv = createNative();
}

/** Returns the native being helped. */
public T get()  { return _ntv; }

/** Creates the native. */
protected abstract T createNative();

/** Registers a view for repaint. */
public abstract void requestPaint(Rect aRect);

/** Window method: initializes native window. */
public void initWindow()  { }

/** Window/Popup method: Shows the window. */
public abstract void show();

/** Window/Popup method: Hides the window. */
public abstract void hide();

/** Window/Popup method: Order window to front. */
public void toFront()  { }

/** Window/Popup method: Sets the document file url for the window title bar proxy icon. */
public void setDocURL(WebURL aURL)  { }

}