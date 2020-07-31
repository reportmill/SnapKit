/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A label subclass used to render items in Lists, Tables, Trees, Browsers.
 */
public class ListCell <T> extends Label {

    // The ListArea that created this cell
    protected ListArea<T>  _listArea;

    // The cell item
    protected T  _item;
    
    // The cell row/column
    protected int  _row, _col;
    
    // Whether cell is selected
    private boolean  _sel;
    
    /**
     * Creates a new ListCell.
     */
    public ListCell(ListArea<T> theList, T anItem, int aRow, int aCol, boolean isSel)
    {
        _listArea = theList;
        _item = anItem;
        _row = aRow; _col = aCol;
        _sel = isSel;
    }

    /**
     * Returns the ListArea.
     */
    public ListArea<T> getListArea()  { return _listArea; }

    /**
     * Returns the item.
     */
    public T getItem()  { return _item; }

    /**
     * Returns the row.
     */
    public int getRow()  { return _row; }

    /**
     * Returns the column.
     */
    public int getCol()  { return _col; }

    /**
     * Returns whether cell is selected.
     */
    public boolean isSelected()  { return _sel; }

    /**
     * Override to notify ListArea.
     */
    @Override
    public void setEditing(boolean aValue)
    {
        // If already set, just return, otherwise do normal
        if (aValue==isEditing()) return;
        super.setEditing(aValue);

        // Nofity ListArea
        if (_listArea!=null)
            _listArea.cellEditingChanged(this);
    }
}