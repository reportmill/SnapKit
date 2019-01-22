/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Color;

/**
 * A WindowView subclass for popup windows.
 */
public class PopupWindow extends WindowView {
    
/**
 * Creates a new PopupWindow.
 */
public PopupWindow()
{
    setType(TYPE_PLAIN);
    setAlwaysOnTop(true);
    setOpacity(.9);
}

/**
 * Returns the RootView.
 */
public RootView getRootView()
{
    if(_rview!=null) return _rview;
    RootView rview = new RootView(); rview.setFill(Color.WHITE); rview.setBorder(Color.LIGHTGRAY,1);
    setRootView(rview); _rview._win = this; _rview._updater._win = this;
    return _rview;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Show node
    super.show(aView, aX, aY);
    
    // Set this popup as aView.Window.Popup
    WindowView win = aView!=null? aView.getWindow() : null;
    if(win!=null) win.setPopup(this);
}

}