/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Color;

/**
 * A WindowView subclass for popup windows.
 */
public class PopupWindow extends WindowView {

    // The parent window
    private WindowView _parentWindow;

    // The view given with last show
    private View _clientView;

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
     * Returns the parent window.
     */
    public WindowView getParentWindow()  { return _parentWindow; }

    /**
     * Returns the view last used for show.
     */
    public View getClientView()  { return _clientView; }

    /**
     * Shows this popup window at point XY in given view coords.
     */
    @Override
    public void show(View aView, double aX, double aY)
    {
        _clientView = aView;

        // Show node
        super.show(aView, aX, aY);

        // Set this popup as aView.Window.Popup
        _parentWindow = aView != null ? aView.getWindow() : null;
        if (_parentWindow != null)
            _parentWindow.setPopup(this);
        else System.err.println("PopupWindow.show: No Window found");
    }

    /**
     * Override to clear parent window.
     */
    @Override
    public void hide()
    {
        super.hide();
        _parentWindow = null;
        _clientView = null;
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