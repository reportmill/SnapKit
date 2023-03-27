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
    public BrowserCol(BrowserView<T> aBrsr)
    {
        // Set browser
        _browser = aBrsr;
        setGrowWidth(true);

        // Update some attributes
        setFocusable(false);
        setFocusWhenPressed(false);
        setRowHeight(_browser.getRowHeight());

        // Configure ScrollView
        ScrollView scrollView = getScrollView();
        scrollView.setShowHBar(false);
        scrollView.setShowVBar(true);
        scrollView.setBarSize(14);

        // Configure ListArea to use Browser.configureBrowserCell
        ListArea<T> listArea = getListArea();
        listArea.setCellConfigure(lc -> _browser.configureBrowserCell(this, lc));

        // Add listener for ListArea.MouseRelease to update Browser.SelCol
        listArea.addEventFilter(e -> listAreaMouseReleased(), MouseRelease);
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
     * Called before ListArea.MousePress.
     */
    protected void listAreaMouseReleased()
    {
        _browser.setSelColIndex(_index);
        _browser.scrollSelToVisible();
    }

    /**
     * Override to suppress ListArea and fire Browser.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        _browser.fireActionEvent(anEvent);
    }

    /**
     * Override to return Browser.PrefColWidth.
     */
    protected double getPrefWidthImpl(double aH)  { return _browser.getPrefColWidth(); }
}