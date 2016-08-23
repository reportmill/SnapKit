/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.View;

/**
 * A class to provide utility methods for UI components.
 */
public class ViewHelper <T> implements PropChangeListener {
    
    // The view
    View               _view;
    
    // The native
    T                  _ntv;
    
/**
 * Returns the view.
 */
public View getView()  { return _view; }

/**
 * Sets the view.
 */
public void setView(View aView)
{
    _view = aView; _ntv = (T)aView.getNative();
    _view.addPropChangeListener(this);
}

/**
 * Returns the view as given class.
 */
public <N extends View> N getView(Class <N> aClass)  { return ClassUtils.getInstance(getView(), aClass); }

/**
 * Returns the native being helped.
 */
public T get()
{
    if(_ntv!=null) return _ntv;
    setNative(createNative());
    return _ntv;
}

/**
 * Sets the native being helped.
 */
public void setNative(Object anObj)
{
    _ntv = (T)anObj;
    _view.getEnv().setProp(_ntv, "View", _view);
}

/**
 * Creates the native.
 */
protected T createNative()  { return null; }

/**
 * Called when View changes to update native.
 */
public void propertyChange(PropChange aPC)
{
    String pname = aPC.getPropertyName();

    if(pname==RootView.CurrentCursor_Prop) setCursor((Cursor)aPC.getNewValue());
    
    // Handle bounds change
    if(pname==View.X_Prop) setX(SnapUtils.doubleValue(aPC.getNewValue()));
    if(pname==View.Y_Prop) setY(SnapUtils.doubleValue(aPC.getNewValue()));
    if(pname==View.Width_Prop) setWidth(SnapUtils.doubleValue(aPC.getNewValue()));
    if(pname==View.Height_Prop) setHeight(SnapUtils.doubleValue(aPC.getNewValue()));
}

/** Sets the x value. */
public void setX(double aValue)  { }

/** Sets the y value. */
public void setY(double aValue)  { }

/** Sets the width value. */
public void setWidth(double aValue)  { }

/** Sets the x value. */
public void setHeight(double aValue)  { }

/** Sets the cursor. */
public void setCursor(Cursor aCursor)  { }

/** Registers a view for repaint. */
public void requestPaint(Rect aRect)  { complain("repaint"); }

/** Window method: initializes window. */
public void checkInit()  { }

/** Window/Popup method: Shows the window at given point relative to given view. */
public void show(View aView, double aX, double aY)  { complain("show"); }

/** Window/Popup method: Hides the window. */
public void hide()  { complain("hide"); }

/** Window/Popup method: Sets the window size to preferred size. */
public void setPrefSize()  { complain("setPrefSize"); }

/** Window/Popup method: Order window to front. */
public void toFront()  { complain("toFront"); }

/** Window/Popup method: Sets the document file for the window title bar proxy icon. */
public void setDocFile(java.io.File aFile)  { complain("setDocFile"); }

/** Prints "not implemented" for string (method name). */
public void complain(String s)  { String msg = getClass().getSimpleName() + ": Not implemented:" + s;
    if(!_cmpln.contains(msg)) System.err.println(msg); _cmpln.add(msg); } static Set _cmpln = new HashSet();

/**
 * Standard to string implementation.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getView(); }

}