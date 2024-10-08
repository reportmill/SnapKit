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
    private View _showView;

    // EventListener to listen to events from show view
    private EventListener _lsnr;

    /**
     * Constructor.
     */
    public PopupList()
    {
        super();
        setBorder(null);
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
     * Shows the node.
     */
    public void show(View aView, double aX, double aY)
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

        // Show popup window
        popupWindow.show(_showView = aView, aX, aY);
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
            getPopup().pack();
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
     * Called when owner View has KeyPress events.
     */
    protected void handleClientViewKeyPressEvent(ViewEvent anEvent)
    {
        if (anEvent.isUpArrow() || anEvent.isDownArrow() || anEvent.isEnterKey())
            processEvent(anEvent);
    }

    /**
     * Called when PopupWindow is shown/hidden.
     */
    protected void handlePopupWindowShowingChanged()
    {
        if (_showView == null) return;
        PopupWindow popup = getPopup();
        boolean showing = popup.isShowing();

        // If showing, add EventListener, otherwise, remove
        if (showing)
            _showView.addEventFilter(_lsnr = this::handleClientViewKeyPressEvent, KeyPress);

        // Otherwise remove listener
        else {
            _showView.removeEventFilter(_lsnr, KeyPress);
            _lsnr = null;
            _showView = null;
        }
    }
}