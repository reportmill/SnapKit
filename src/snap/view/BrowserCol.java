/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.StringUtils;

/**
 * A ListNode subclass to act as a BrowserView columns.
 */
public class BrowserCol <T> extends ListView <T> {
    
    // The Browser
    BrowserView <T>  _browser;

    // The index of this browser column
    int              _index;
    
/**
 * Creates new BrowserCol for given BrowserView.
 */
public BrowserCol(BrowserView aBrsr)
{
    _browser = aBrsr;
    setFireActionOnRelease(true);
    setFocusWhenPressed(false);
}

/**
 * Returns the browser.
 */
public BrowserView <T> getBrowser()  { return _browser; }

/**
 * Returns the column ScrollView.
 */
public ScrollView getScrollView()  { return getParent(ScrollView.class); }

/**
 * Returns the column index.
 */
public int getIndex()  { return _index; }

/**
 * Returns the selected item text.
 */
public String getSelectedItemText()  { T si = getSelectedItem(); return getText(si); }

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
    _browser.setSelColIndex(_index);
    _browser.scrollSelToVisible();
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return StringUtils.toString(this, "Index", "SelectedItemText").toString();
}
    
}