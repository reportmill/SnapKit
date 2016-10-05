/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Color;

/**
 * A custom class.
 */
public class PopupWindow extends ParentView {
    
    // The root pane
    RootView      _rpane;
    
    // The mouse press trigger event and time
    ViewEvent     _mpress; long _mptime; double _nx, _ny;
    
/**
 * Returns the root pane.
 */
public RootView getRootView()
{
    if(_rpane!=null) return _rpane;
    _rpane = new RootView(); _rpane.setFill(Color.WHITE); _rpane.getHelper(); //_rpane._win = this;
    _rpane.setBorder(Color.LIGHTGRAY,1);
    addChild(_rpane);
    return _rpane;
}

/**
 * Returns the content associated with this window.
 */
public View getContent()  { return getRootView().getContent(); }

/**
 * Sets the content associated with this window.
 */
public void setContent(View aView)  { getRootView().setContent(aView); }

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Show node
    getHelper().show(aView, aX, aY);
    
    // Record time and node location and set this popup as Node.RootView.Popup to get trigger events
    _mptime = System.currentTimeMillis(); _nx = aX; _ny = aY;
    RootView rpane = aView!=null? aView.getRootView() : null;
    if(rpane!=null) rpane.setPopup(this);
}

/**
 * Hides the node.
 */
public void hide()  { getHelper().hide(); }

/**
 * Override to return showing, since it is eqivalent for window.
 */
public boolean isVisible()  { return isShowing(); }

/**
 * Override to call show/hide.
 */
public void setVisible(boolean aValue)  { if(!aValue) hide(); }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _rpane!=null? _rpane.getPrefWidth() : 0; }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _rpane!=null? _rpane.getPrefHeight() : 0; }

/**
 * Handles trigger event.
 */
protected void processTriggerEvent(ViewEvent anEvent)
{
    /*double mx = anEvent.getX(), my = anEvent.getY();
    if(anEvent.isMousePressed()) _mpress = anEvent;
    else if(_mpress==null || Math.abs(_mpress.getX()-mx)>2 || Math.abs(_mpress.getY()-my)>2 ||
        System.currentTimeMillis()-_mptime>500) {
        if(_mpress!=null) {
            double mpx = _mpress.getX(), mpy = _mpress.getY();
            ViewEvent event = _mpress.copyForNodePoint(getContent(), mpx-_nx, mpy-_ny, _mpress.getClickCount());
            getRootView().dispatchEvent(event);
            _mpress = null;
        }
        ViewEvent event = anEvent.copyForNodePoint(getContent(), mx-_nx, my-_ny, anEvent.getClickCount());
        getRootView().dispatchEvent(event);
    }*/
}

}