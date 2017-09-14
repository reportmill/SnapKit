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
    setRootView(rview);
    return _rview;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Show node
    super.show(aView, aX, aY);
    
    // Set this popup as aView.RootView.Popup
    RootView rview = aView!=null? aView.getRootView() : null;
    if(rview!=null) rview.setPopup(this);
}

}