/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListView subclass to act as a BrowserView column.
 */
public class BrowserCol <T> extends ListView <T> {
    
    // The Browser
    private BrowserView <T>  _browser;

    // The index of this browser column
    protected int  _index;
    
    /**
     * Creates new BrowserCol for given BrowserView.
     */
    public BrowserCol(BrowserView aBrsr)
    {
        // Set browser
        _browser = aBrsr;

        // Update some attribues
        setRowHeight(_browser.getRowHeight());

        // Configure ScrollView
        ScrollView scroll = getScrollView();
        scroll.setShowHBar(false);
        scroll.setShowVBar(true);
        scroll.setBarSize(12);

        // Add listener for ListArea.MousePress to update Browser.SelCol
        getListArea().addEventFilter(e -> listAreaMousePressed(e), MousePress);
    }

    /**
     * Returns the browser.
     */
    public BrowserView <T> getBrowser()  { return _browser; }

    /**
     * Returns the column index.
     */
    public int getIndex()  { return _index; }

    /**
     * Override to forward to browser.
     */
    protected void configureCell(ListCell <T> aCell)
    {
        _browser.configureBrowserCell(this, aCell);
    }

    /**
     * Called before ListArea.MousePress.
     */
    protected void listAreaMousePressed(ViewEvent anEvent)
    {
        getUpdater().runBeforeUpdate(() -> {
            _browser.setSelColIndex(_index);
            _browser.scrollSelToVisible();
        });
    }

    /**
     * Override to suppress ListArea and fire Browser.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        _browser.fireActionEvent(anEvent);
    }

    /**
     * Override to request size of Browser/VisColCount (Should really be set in BrowserView.setWidth).
     */
    protected double getPrefWidthImpl(double aH)
    {
        double prefWidth0 = super.getPrefWidthImpl(aH);
        double width = _browser.getScrollView().getScroller().getWidth();
        int colCount = Math.min(_browser.getColCount(), _browser.getPrefColCount());
        double minWidth = width/colCount;
        double pw = Math.max(prefWidth0, minWidth);
        return pw;
    }
}