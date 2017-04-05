/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.util.*;

/**
 * A View subclass to show a table of items.
 */
public class TableView <T> extends ParentView implements View.Selectable <T> {

    // The items
    List <T>                _items = new ArrayList();
    
    // The selected index
    int                     _selIndex = -1;
    
    // The selected column
    int                     _selCol;
    
    // Whether to show table header
    boolean                 _showHeader = true;
    
    // Whether to show horziontal/vertical lines
    boolean                 _showLinesH, _showLinesV;
    
    // Grid color
    Color                   _gridColor;
    
    // Row height
    int                     _rowHeight = 24;

    // The Cell Configure method
    Consumer <ListCell<T>>  _cellConf;
    
    // The SplitView to hold columns
    SplitView               _split = new SplitView();
    
    // The ScrollView to hold SplitView+Columns
    ScrollView              _scroll = new ScrollView(_split);
    
    // Constants
    static final Paint DIVIDER_FILL = new Color("#EEEEEE");

    // Constants for properties
    public static final String Items_Prop = "Items";
    public static final String SelectedItem_Prop = "SelectedItem";
    public static final String SelectedIndex_Prop = "SelectedIndex";
    
/**
 * Creates a new TableView.
 */
public TableView()
{
    enableEvents(Action);
    
    _split.setGrowWidth(true);
    addChild(_scroll);
}

/**
 * Returns the items.
 */
public List <T> getItems()  { return _items; }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)
{
    if(ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items)) return;
    _items.clear();
    if(theItems!=null) _items.addAll(theItems);
    for(TableCol tcol : getCols()) tcol.setItems(theItems);
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Tell table to update given items (none means all).
 */
public void updateItems(T ... theItems)
{
    for(TableCol tcol : getCols()) tcol.updateItems(theItems);
}

/**
 * Returns the number of columns.
 */
public int getColCount()  { return _split.getItemCount(); }

/**
 * Returns the column at given index.
 */
public TableCol getCol(int anIndex)  { return (TableCol)_split.getItem(anIndex); }

/**
 * Returns the column at given index.
 */
public TableCol[] getCols()  { return _split.getItems().toArray(new TableCol[0]); }

/**
 * Adds a TableCol.
 */
public void addCol(TableCol aCol)
{
    _split.addItem(aCol);
    
    // Reset split DividerSize and Fill
    for(Divider div : _split.getDividers()) { div.setDividerSize(2); div.setFill(DIVIDER_FILL); div.setBorder(null); }
}

/**
 * Remove's the TableCol at the given index from this Table's children list.
 */
public TableCol removeCol(int anIndex)  { return (TableCol)_split.removeChild(anIndex); }

/**
 * Removes the given TableCol from this table's children list.
 */
public int removeCol(TableCol aCol)  { return _split.removeItem(aCol); }

/**
 * Returns the number of rows.
 */
public int getRowCount()  { return getItems().size(); }

/**
 * Returns whether to show header.
 */
public boolean getShowHeader()  { return _showHeader; }

/**
 * Sets whether to show header.
 */
public void setShowHeader(boolean aValue)
{
    firePropChange("ShowHeader", _showHeader, _showHeader = aValue);
}

/** Replace these with AllowMultipleChoice, AllowIntervals. */
//public int getSelectionMode()  { return _selMode; } int _selMode = SELECT_SINGLE;
//public void setSelectionMode(int aValue)  { firePropertyChange("SelectionMode", _selMode, _selMode = aValue, -1); }

/**
 * Returns whether to show horizontal lines.
 */
public boolean isShowLinesX()  { return _showLinesH; }

/**
 * Sets whether to show horizontal lines.
 */
public void setShowLinesX(boolean aValue)
{
    firePropChange("ShowHorizontalLines", _showLinesH, _showLinesH = aValue);
}

/**
 * Returns whether to show vertical lines.
 */
public boolean isShowLinesY()  { return _showLinesV; }

/**
 * Sets whether to show vertical lines.
 */
public void setShowLinesY(boolean aValue)
{
    firePropChange("ShowVerticalLines", _showLinesV, _showLinesV = aValue);
}

/**
 * Returns grid color.
 */
public Color getGridColor()  { return _gridColor; }

/**
 * Sets grid color.
 */
public void setGridColor(Color aValue)
{
    firePropChange("GridColor", _gridColor, _gridColor = aValue);
}

/**
 * Returns the row height.
 */
public int getRowHeight()  { return _rowHeight; }

/**
 * Sets the row height.
 */
public void setRowHeight(int aValue)
{
    firePropChange("RowHeight", _rowHeight, _rowHeight = aValue);
}

/**
 * Called to set method for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

/**
 * Called to set method for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    firePropChange("SelectedIndex", _selIndex, _selIndex = anIndex);
    for(TableCol tcol : getCols()) tcol.setSelectedIndex(anIndex);
    fireActionEvent();
}

/**
 * Returns the minimum selected index.
 */
public int getSelectedIndexMin()
{
    int indexes[] = getSelectedIndices();
    int min = Integer.MAX_VALUE; for(int i : indexes) min = Math.min(min, i);
    return min!=Integer.MAX_VALUE? min : -1;
}

/**
 * Returns the maximum selected index.
 */
public int getSelectedIndexMax()
{
    int indexes[] = getSelectedIndices();
    int max = -1; for(int i : indexes) max = Math.max(max, i);
    return max;
}

/**
 * Returns the selected indices.
 */
public int[] getSelectedIndices()  { return _selIndex>=0? new int[] { _selIndex } : new int[0]; }

/**
 * Sets the selection interval.
 */
public void setSelectionInterval(int aStart, int anEnd)
{
    int min = Math.min(aStart,anEnd), max = Math.max(aStart,anEnd), len = max-min+1;
    int indexes[] = new int[len]; for(int i=0;i<len;i++) indexes[i] = i + min;
    setSelectedIndex(min);
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _selIndex>=0? _items.get(_selIndex) : null; }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)
{
    int index = _items.indexOf(anItem);
    setSelectedIndex(index);
}
/**
 * Returns the selected row.
 */
public int getSelectedRow()  { return getSelectedIndex(); }

/**
 * Returns the selected column.
 */
public int getSelectedCol()  { return _selCol; }

/**
 * Sets the selected column.
 */
public void setSelectedCol(int anIndex)  { _selCol = anIndex; }

/**
 * Returns the row index at given point.
 */
public int getRowAt(double aX, double aY)  { return (int)(aY/getRowHeight()); }

/**
 * Override to reset cells.
 */
public void setY(double aValue)
{
    if(aValue==getY()) return; super.setY(aValue);
    for(TableCol tcol : getCols()) tcol.relayout();
}

/**
 * Override to reset cells.
 */
public void setHeight(double aValue)
{
    if(aValue==getHeight()) return; super.setHeight(aValue);
    for(TableCol tcol : getCols()) tcol.relayout();
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    //double pw = 0; for(TableCol tcol : getCols()) pw += tcol.getPrefWidth(); return pw;
    return _scroll.getPrefWidth(aH);
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getRowHeight()*getItems().size(); }

/**
 * Override to layout children with VBox layout.
 */
protected void layoutChildren()
{
    //ViewLayout.HBoxLayout layout = new ViewLayout.HBoxLayout(this); layout.setFillHeight(true);
    //layout.layoutChildren();
    _scroll.setSize(getWidth(),getHeight());
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "SelectedItem"; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive ShowHeader
    if(!getShowHeader()) e.add("ShowHeader", false);
    
    // Archive GridColor, ShowLinesX, ShowLinesY
    if(getGridColor()!=null) e.add("GridColor", '#' + getGridColor().toHexString());
    if(isShowLinesX()) e.add("ShowLinesX", true);
    if(isShowLinesY()) e.add("ShowLinesY", true);
    
    // Archive RowHeight
    if(getRowHeight()!=24) e.add("RowHeight", getRowHeight());
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive TableHeader
    if(anElement.hasAttribute("ShowHeader"))
        setShowHeader(anElement.getAttributeBooleanValue("ShowHeader"));
    
    // Unarchive GridColor, ShowLinesX, ShowLinesY
    if(anElement.hasAttribute("GridColor")) setGridColor(new Color(anElement.getAttributeValue("GridColor")));
    setShowLinesX(anElement.getAttributeBoolValue("ShowLinesX", false));
    setShowLinesY(anElement.getAttributeBoolValue("ShowLinesY", false));
    
    // Unarchive RowHeight
    setRowHeight(anElement.getAttributeIntValue("RowHeight", 24));
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getColCount(); i<iMax; i++) { TableCol child = getCol(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child views
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && TableCol.class.isAssignableFrom(cls)) {
            TableCol col = (TableCol)anArchiver.fromXML(childXML, this);
            addCol(col);
        }
    }
}

}