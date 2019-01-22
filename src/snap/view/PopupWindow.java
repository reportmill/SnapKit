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

    RootView rview = getRootView(); rview.setFill(Color.WHITE); rview.setBorder(Color.LIGHTGRAY,1);
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