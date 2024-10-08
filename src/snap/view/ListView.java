/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A View to manage a list of items (ListArea) in a ScrollView.
 */
public class ListView <T> extends ListArea<T> {
    
    // The Preferred number of rows
    private int  _prefRowCount = -1;
    
    // The maximum number of rows
    private int  _maxRowCount = -1;

    /**
     * Constructor.
     */
    public ListView()
    {
        super();
    }

    /**
     * Returns the ListArea.
     */
    public ListArea <T> getListArea()  { return this; }

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
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // If PrefRowCount set, return PrefRowCount*RowHeight
        if (getPrefRowCount() > 0)
            return getPrefRowCount() * getRowHeight() + getInsetsAll().getHeight();

        // Return pref height of Scroll
        return super.getPrefHeightImpl(aW);
    }

    /**
     * Returns the maximum height.
     */
    public double getMaxHeight()
    {
        // If MaxRowCount set, return MaxRowCount*RowHeight
        if (getMaxRowCount()>0)
            return getMaxRowCount()*getRowHeight() + getInsetsAll().getHeight();

        // Return normal version
        return super.getMaxHeight();
    }
}