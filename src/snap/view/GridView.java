package snap.view;

/**
 * A View class to layout children in columns and rows.
 */
public class GridView extends HostView {

    // The number of columns and rows
    int               _colCount, _rowCount;

/**
 * Returns the number of columns.
 */
public int getColCount()  { return _colCount; }

/**
 * Returns the number of rows.
 */
public int getRowCount()  { return _rowCount; }

/**
 * Returns the cell at given col/row indexes.
 */
public Cell getCell(int aCol, int aRow)  { return null; }

/**
 * A View subclass to manage items in a cell.
 */
public class Cell extends View {
    
}

}