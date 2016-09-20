/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A label subclass used to render items in Lists, Tables, Trees, Browsers.
 */
public class ListCell <T> extends Label {
    
    // The cell item
    T                  _item;
    
    // The cell parent node
    ListView           _view;
    
    // The cell row/column
    int                _row, _col;
    
    // Whether cell is selected
    boolean            _sel;
    
/**
 * Creates a new ListCell.
 */
public ListCell(ListView aView, T anItem, int aRow, int aCol, boolean isSel)
{
    _view = aView; _item = anItem; _row = aRow; _col = aCol; _sel = isSel;
}

/**
 * Returns the node this cell is associated with.
 */
public View getView()  { return _view; }

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

}