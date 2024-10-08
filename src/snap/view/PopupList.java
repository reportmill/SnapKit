/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListView subclass that displays in a PopupWindow.
 */
public class PopupList<T> extends ListView<T> {

    // The Preferred number of rows
    private int _prefRowCount = -1;

    // The maximum number of rows
    private int _maxRowCount = -1;

    // The PopupWindow
    private PopupWindow _popup;

    // The view given with last show
    private View _clientView;

    // EventListener to listen to key press events from client view
    private EventListener _clientViewKeyPressLsnr;

    /**
     * Constructor.
     */
    public PopupList()
    {
        super();
        setBorder(null);
        _clientViewKeyPressLsnr = this::handleClientViewKeyPressEvent;
    }

    /**
     * Returns the preferred number of rows.
     */
    public int getPrefRowCount()  { return _prefRowCount; }

    /**
     * Sets the preferred number of rows.
     */
    public void setPrefRowCount(int aValue)  { _prefRowCount = aValue; relayoutParent(); }

    /**
     * Returns the maximum number of rows.
     */
    public int getMaxRowCount()  { return _maxRowCount; }

    /**
     * Sets the maximum number of rows.
     */
    public void setMaxRowCount(int aValue)  { _maxRowCount = aValue; relayoutParent(); }

    /**
     * Returns the popup.
     */
    public PopupWindow getPopup()
    {
        // If already set, just return
        if (_popup != null) return _popup;

        // Create ScrollView
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBorder(null);

        // Create/configure PopupWindow
        PopupWindow popup = new PopupWindow();
        popup.setContent(scrollView);
        popup.addPropChangeListener(pc -> handlePopupWindowShowingChanged(), Showing_Prop);

        // Set and return
        return _popup = popup;
    }

    /**
     * Shows this popup list in a popup window at given XY relative to view.
     */
    public void show(View aView, double aX, double aY)
    {
        // Resize popup window
        resizePopupWindow();

        // Show popup window
        PopupWindow popupWindow = getPopup();
        popupWindow.show(_clientView = aView, aX, aY);
    }

    /**
     * Hides the node.
     */
    public void hide()
    {
        getPopup().hide();
    }

    /**
     * Override to resize if showing.
     */
    public void setItemsList(java.util.List<T> theItems)
    {
        super.setItemsList(theItems);
        if (isShowing())
            resizePopupWindow();
    }

    /**
     * Resizes the popup window.
     */
    private void resizePopupWindow()
    {
        // Get popup window and set best size
        PopupWindow popupWindow = getPopup();

        // Configure PrefHeight
        int prefRowCount = getPrefRowCount();
        double prefH = prefRowCount > 0 ? prefRowCount * getRowHeight() + getInsetsAll().getHeight() : -1;
        popupWindow.setPrefHeight(prefH);

        // Configure MaxHeight
        int maxRowCount = getMaxRowCount();
        double maxH = maxRowCount > 0 ? maxRowCount * getRowHeight() + getInsetsAll().getHeight() : -1;
        popupWindow.setMaxHeight(maxH);

        // Size popup window
        popupWindow.pack();
    }

    /**
     * Override to hide popup window.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        super.fireActionEvent(anEvent);
        hide();
    }

    /**
     * Called when PopupWindow is shown/hidden.
     */
    protected void handlePopupWindowShowingChanged()
    {
        if (_clientView == null) return;
        PopupWindow popupWindow = getPopup();
        boolean showing = popupWindow.isShowing();

        // If showing, add EventListener, otherwise, remove
        if (showing)
            _clientView.addEventFilter(_clientViewKeyPressLsnr, KeyPress);

        // Otherwise remove listener
        else {
            _clientView.removeEventFilter(_clientViewKeyPressLsnr, KeyPress);
            _clientView = null;
        }
    }

    /**
     * Called when owner View has KeyPress events.
     */
    protected void handleClientViewKeyPressEvent(ViewEvent anEvent)
    {
        if (anEvent.isUpArrow() || anEvent.isDownArrow() || anEvent.isEnterKey())
            processEvent(anEvent);
    }
}