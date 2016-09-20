/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListNode subclass to act as a browser column.
 */
public class BrowserCol <T> extends ListView <T> {
    
    // The Browser
    BrowserView <T>  _browser;

    // The index of this browser column
    int              _index;
    
/**
 * Creates a new BrowserCol for given item.
 */
public BrowserCol(BrowserView aBrsr)  { _browser = aBrsr; setGrowHeight(true); setFocusWhenPressed(false); }

/**
 * Returns the browser.
 */
public BrowserView <T> getBrowser()  { return _browser; }

/**
 * Returns the column ScrollView.
 */
public ScrollView getScrollView()  { return getParent(ScrollView.class); }

/**
 * Override to return browser row height.
 */
public double getRowHeight()  { return getBrowser().getRowHeight(); }

/**
 * Override to forward to browser.
 */
protected void configureCell(ListCell <T> aCell)
{
    //Consumer cconf = getCellConfigure(); if(cconf!=null) cconf.accept(aCell); else aCell.configure();
    super.configureCell(aCell);
    _browser.configureBrowserCell(this, aCell);
}

/**
 * Override to suppress.
 */
public void fireActionEvent()
{
    while(_index+1<_browser.getColCount())
        _browser.removeChild(_index+1); //getParent().revalidate(); getParent().repaint();
    T item = getSelectedItem(); if(item==null) return;
    if(_browser.isParent(item)) {
        BrowserCol bcol = _browser.addCol();
        getEnv().runLater(() -> _browser.scrollToVisible(bcol.getScrollView().getBounds()));
    }
}

}