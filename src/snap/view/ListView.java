/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import snap.gfx.*;
import snap.util.*;

/**
 * A View to manage a list of items (ListCol) in a ScrollView.
 * 
 * To display custom text in list, simply call list.setItemTextFunction(itm -> itm.getName());
 * 
 * To custom configure list cell, simply call list.setCellConfigure(cell -> cell.setImage(img));
 */
public class ListView <T> extends ListCol <T> implements View.Selectable <T> {
    
    // The ListCol (real ListView functionality without scroll)
    ListCol <T>          _listCol;
    
    // The ScrollView
    ScrollView            _scroll;
    
    // The Preferred number of rows
    int                   _prefRowCount = -1;
    
    // The maximum number of rows
    int                   _maxRowCount = -1;

/**
 * Creates a new ListView.
 */
public ListView()
{
    // Reconfigure this to undo ListView stuff
    disableEvents(MousePress, MouseRelease, KeyPress);
    setFocusable(false); setFocusWhenPressed(false);
    setFill(null);

    // Create/configure ListCol
    _listCol = createCol();
    _listCol.setGrowWidth(true); _listCol.setGrowHeight(true);
    _listCol.addEventHandler(e -> fireActionEvent(), Action);
    _listCol.addPropChangeListener(pce -> listColPropChange(pce));
    _listCol._proxy = this;
    
    // Create/configure ScrollView
    _scroll = createScrollView();
    _scroll.setContent(_listCol);
    addChild(_scroll);
}

/**
 * Returns the ListCol.
 */
public ListCol <T> getCol()  { return _listCol; }

/**
 * Creates the ListCol.
 */
protected ListCol createCol()  { return new ListCol(); }

/**
 * Returns the ScrollView.
 */
public ScrollView getScrollView()  { return _scroll; }

/**
 * Creates the ScrollView.
 */
protected ScrollView createScrollView()  { return new ScrollView(); }

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
 * Returns the number of items.
 */
public int getItemCount()  { return _listCol.getItemCount(); }

/**
 * Returns the individual item at index.
 */
public T getItem(int anIndex)  { return _listCol.getItem(anIndex); }

/**
 * Returns the items.
 */
public List <T> getItems()  { return _listCol.getItems(); }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)  { _listCol.setItems(theItems); }

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _listCol.getSelectedIndex(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)  { _listCol.setSelectedIndex(anIndex); }

/**
 * Returns the minimum selected index.
 */
public int getSelectedIndexMin()  { return _listCol.getSelectedIndexMin(); }

/**
 * Returns the maximum selected index.
 */
public int getSelectedIndexMax()  { return _listCol.getSelectedIndexMax(); }

/**
 * Returns the selected indices.
 */
public int[] getSelectedIndices()  { return _listCol.getSelectedIndices(); }

/**
 * Sets the selection interval.
 */
public void setSelectionInterval(int aStart, int anEnd)  { _listCol.setSelectionInterval(aStart,anEnd); }

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _listCol.getSelectedItem(); }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)  { _listCol.setSelectedItem(anItem); }

/**
 * Returns the row height.
 */
public double getRowHeight()  { return _listCol.getRowHeight(); }

/**
 * Sets the row height.
 */
public void setRowHeight(double aValue)  { _listCol.setRowHeight(aValue); }

/**
 * Returns the row at given Y location.
 */
public int getRowAt(double aY)  { return _listCol.getRowAt(aY); }

/**
 * Returns function for deteriming text for an item.
 */
public Function <T,String> getItemTextFunction()  { return _listCol.getItemTextFunction(); }

/**
 * Sets function for deteriming text for an item.
 */
public void setItemTextFunction(Function <T,String> aFunc)  { _listCol.setItemTextFunction(aFunc); }

/**
 * Returns method to configure list cells.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _listCol.getCellConfigure(); }

/**
 * Sets method to configure list cells.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _listCol.setCellConfigure(aCC); }

/**
 * Returns the paint for alternating cells.
 */
public Paint getAltPaint()  { return _listCol.getAltPaint(); }

/**
 * Sets the paint for alternating cells.
 */
public void setAltPaint(Paint aPaint)  { _listCol.setAltPaint(aPaint); }

/**
 * Returns whether to fire action on mouse release instead of press.
 */
public boolean isFireActionOnRelease()  { return _listCol.isFireActionOnRelease(); }

/**
 * Sets whether to fire action on mouse release instead of press.
 */
public void setFireActionOnRelease(boolean aValue)  { _listCol.setFireActionOnRelease(aValue); }

/**
 * Returns whether list shows visual cue for item under the mouse.
 */
public boolean isTargeting()  { return _listCol.isTargeting(); }

/**
 * Sets whether list shows visual cue for item under the mouse.
 */
public void setTargeting(boolean aValue)  { _listCol.setTargeting(aValue); }

/**
 * Called to update items in list that have changed.
 */
public void updateItems(T ... theItems)  { _listCol.updateItems(theItems); }

/**
 * Returns the cell at given index.
 */
public ListCell <T> getCell(int anIndex)  { return _listCol.getCell(anIndex); }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ViewLayout.getPrefWidthBasic(this, _scroll, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    // If PrefRowCount set, return PrefRowCount*RowHeight
    if(getPrefRowCount()>0)
        return getPrefRowCount()*getRowHeight() + getInsetsAll().getHeight();
    
    // Return pref height of Scroll
    return ViewLayout.getPrefHeightBasic(this, _scroll, aW);
}

/**
 * Returns the maximum height.
 */
public double getMaxHeight()
{
    // If MaxRowCount set, return MaxRowCount*RowHeight
    if(getMaxRowCount()>0)
        return getMaxRowCount()*getRowHeight() + getInsetsAll().getHeight();
    
    // Return normal version
    return super.getMaxHeight();
}

/**
 * Override to layout ScrollView.
 */
protected void layoutImpl()  { ViewLayout.layoutBasic(this, _scroll); }

/**
 * Returns text for item.
 */
public String getText(T anItem)  { return _listCol.getText(anItem); }

/**
 * Override to return text for currently selected item.
 */
public String getText()  { return _listCol.getText(); }

/**
 * Override to forward to ListCol.
 */
public void setText(String aString)  { _listCol.setText(aString); }

/**
 * Called to configure a cell.
 */
protected void configureCell(ListCell <T> aCell)  { }

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return getBinding("SelectedIndex")!=null? "SelectedIndex" : "SelectedItem"; }

/**
 * Catches property changes from ListCol and redispatches for this ListView.
 */
void listColPropChange(PropChange aPC)
{
    if(aPC.getPropertyName()==SelectedIndex_Prop)
        firePropChange(SelectedIndex_Prop, aPC.getOldValue(), aPC.getNewValue());
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
}

}