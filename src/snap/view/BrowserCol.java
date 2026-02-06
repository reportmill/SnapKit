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
     * Constructor.
     */
    public BrowserCol(BrowserView<T> aBrsr)
    {
        // Set browser
        _browser = aBrsr;
        setBorder(null);
        setGrowWidth(true);
        setOverflow(Overflow.Visible);

        // Update some attributes
        setFocusable(false);
        setFocusWhenPressed(false);
        setRowHeight(_browser.getRowHeight());

        // Configure ListView to use Browser.configureBrowserCell
        setCellConfigure(_browser::configureBrowserCell);
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
     * Override to have browser select this column on MouseRelease.
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseRelease: Select browser column
        if (anEvent.isMouseRelease())
            _browser.setSelColIndex(_index);

        // Do normal version
        super.processEvent(anEvent);
    }

    /**
     * Override to suppress ListView and fire Browser.
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        _browser.fireActionEvent(anEvent);
    }

    /**
     * Override to return Browser.PrefColWidth.
     */
    protected double computePrefWidth(double aH)  { return _browser.getPrefColWidth(); }
}