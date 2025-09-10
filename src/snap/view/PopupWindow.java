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
     * Constructor.
     */
    public PopupWindow()
    {
        super();
        setFocusable(false);
        setType(TYPE_PLAIN);
        setAlwaysOnTop(true);
        //setOpacity(.9);

        // Configure RootView
        RootView rootView = getRootView();
        rootView.setFill(Color.WHITE);
        rootView.setBorder(Color.LIGHTGRAY, 1);
        rootView.addEventHandler(this::handleRootViewKeyPressEvent, ViewEvent.Type.KeyPress);
    }

    /**
     * Shows this popup window at point XY in given view coords.
     */
    @Override
    public void show(View aView, double aX, double aY)
    {
        // Show node
        super.show(aView, aX, aY);

        // Set this popup as aView.Window.Popup
        WindowView parentWindow = aView != null ? aView.getWindow() : null;
        if (parentWindow != null)
            parentWindow.setPopup(this);
        else System.err.println("PopupWindow.show: No Window found");
    }

    /**
     * Called when RootView gets a KeyPress.
     */
    private void handleRootViewKeyPressEvent(ViewEvent anEvent)
    {
        // Handle Escape key: Hide window
        if (anEvent.isEscapeKey()) {
            hide();
            anEvent.consume();
        }
    }
}