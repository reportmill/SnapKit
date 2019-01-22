/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * The top level View in a window.
 */
public class RootView extends ParentView {
    
    // The window used to show this root view on screen
    WindowView               _win;
    
    // The content
    View                     _content;
    
    // The focused view
    View                     _focusedView = this, _focusedViewLast;
    
    // A class to handle view updates (repaint, relayout, resetUI, animation)
    ViewUpdater              _updater;
    
    // Constants for properties
    public static final String Content_Prop = "Content";

/**
 * Creates a RootView.
 */
public RootView()
{
    enableEvents(KeyEvents); setFocusable(true); setFocusPainted(false);
    setFill(ViewUtils.getBackFill());
    _updater = new ViewUpdater(this);
}

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)
{
    View old = _content; if(aView==old) return;
    if(_content!=null) removeChild(_content); _content = aView;
    if(_content!=null) addChild(_content);
    firePropChange(Content_Prop, old, _content);
}

/**
 * Returns the Window to manage this ViewOwner's window.
 */
public boolean isWindowSet()  { return _win!=null; }

/**
 * Returns the window for this root view.
 */
public WindowView getWindow()
{
    if(_win!=null) return _win;
    WindowView win = new WindowView();
    win.setRootView(this);
    return _win = win;
}

/**
 * Returns the root view.
 */
public RootView getRootView()  { return this; }

/**
 * Returns the ViewUpdater.
 */
public ViewUpdater getUpdater()  { return _updater; }

/**
 * Override to handle when RootView is ordered out.
 */
protected void setShowing(boolean aVal)
{
    if(aVal==isShowing()) return; super.setShowing(aVal);
    
    // If no longer showing, dispatch mouse move outside bounds to trigger any mouse exit code
    if(!aVal) {
        ViewEvent event = getEnv().createEvent(this, null, MouseMove, null);
        event = event.copyForViewPoint(this, getWidth()+100, 0, 0);
        getWindow().dispatchEvent(event);
    }
}

/**
 * Returns the view that currently receives KeyEvents.
 */
public View getFocusedView()
{
    if(_focusedView!=null && !_focusedView.isFocused())
        _focusedView = null;
    return _focusedView;
}

/**
 * Returns the previous focus view.
 */
public View getFocusedViewLast()  { return _focusedViewLast; }

/**
 * Tries to makes the given view the view that receives KeyEvents.
 */
protected void requestFocus(View aView)
{
    // Make sure this happens on Event thread
    if(!getEnv().isEventThread()) { getEnv().runLater(() -> requestFocus(aView)); return; }
    
    // If already set, just return
    if(aView==_focusedView) return;
    
    // If existing FocusedView, clear View.Focused
    if(_focusedView!=null)
        _focusedView.setFocused(false);
    
    // Update FocusViewLast, FocusView
    _focusedViewLast = _focusedView; _focusedView = aView;
    
    // If new FocusedView, set View.Focused
    if(_focusedView!=null)
        _focusedView.setFocused(true);
}

/**
 * Override to register for layout.
 */
protected void setNeedsLayoutDeep(boolean aVal)
{
    if(aVal==isNeedsLayoutDeep()) return;
    super.setNeedsLayoutDeep(aVal);
    _updater.relayoutViews();
}

/**
 * Override to actually paint in this RootView.
 */
protected void repaintInParent(Rect aRect)  { repaint(aRect!=null? aRect : getBoundsLocal()); }

/** Returns the preferred width. */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _content, aH); }

/** Returns the preferred height. */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, _content, aW); }

/** Layout children. */
protected void layoutImpl()  { BoxView.layout(this, _content, null, true, true); }

}