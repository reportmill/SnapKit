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
 * A View to manage a ListView in a ScrollView.
 * 
 * To display custom text in list, simply call list.setItemTextFunction(itm -> itm.getName());
 * 
 * To custom configure list cell, simply call list.setCellConfigure(cell -> cell.setImage(img));
 */
public class ListViewScroll <T> extends ListView <T> implements View.Selectable <T> {
    
    // The ListView (real ListView functionality without scroll)
    ListView <T>          _listView;
    
    // The ScrollView
    ScrollView            _scroll;
    
    // The Preferred number of rows
    int                   _prefRowCount = -1;
    
    // The maximum number of rows
    int                   _maxRowCount = -1;

/**
 * Creates a new ListViewScroll.
 */
public ListViewScroll()
{
    // Reconfigure this to undo ListView stuff
    disableEvents(MousePress, MouseRelease, KeyPress);
    setFocusable(false); setFocusWhenPressed(false);
    setFill(null);

    // Create/configure ListView
    _listView = createListView();
    _listView.setGrowWidth(true); _listView.setGrowHeight(true);
    _listView.addEventHandler(e -> fireActionEvent(), Action);
    _listView.addPropChangeListener(pce -> listViewPropChange(pce));
    _listView._proxy = this;
    
    // Create/configure ScrollView
    _scroll = createScrollView();
    _scroll.setContent(_listView);
    addChild(_scroll);
}

/**
 * Returns the ListView.
 */
public ListView <T> getListView()  { return _listView; }

/**
 * Creates the ListView.
 */
protected ListView createListView()  { return new ListView(); }

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
public int getItemCount()  { return _listView.getItemCount(); }

/**
 * Returns the individual item at index.
 */
public T getItem(int anIndex)  { return _listView.getItem(anIndex); }

/**
 * Returns the items.
 */
public List <T> getItems()  { return _listView.getItems(); }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)  { _listView.setItems(theItems); }

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _listView.getSelectedIndex(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)  { _listView.setSelectedIndex(anIndex); }

/**
 * Returns the minimum selected index.
 */
public int getSelectedIndexMin()  { return _listView.getSelectedIndexMin(); }

/**
 * Returns the maximum selected index.
 */
public int getSelectedIndexMax()  { return _listView.getSelectedIndexMax(); }

/**
 * Returns the selected indices.
 */
public int[] getSelectedIndices()  { return _listView.getSelectedIndices(); }

/**
 * Sets the selection interval.
 */
public void setSelectionInterval(int aStart, int anEnd)  { _listView.setSelectionInterval(aStart,anEnd); }

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _listView.getSelectedItem(); }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)  { _listView.setSelectedItem(anItem); }

/**
 * Returns the row height.
 */
public double getRowHeight()  { return _listView.getRowHeight(); }

/**
 * Sets the row height.
 */
public void setRowHeight(double aValue)  { _listView.setRowHeight(aValue); }

/**
 * Returns the row at given Y location.
 */
public int getRowAt(double aY)  { return _listView.getRowAt(aY); }

/**
 * Returns function for deteriming text for an item.
 */
public Function <T,String> getItemTextFunction()  { return _listView.getItemTextFunction(); }

/**
 * Sets function for deteriming text for an item.
 */
public void setItemTextFunction(Function <T,String> aFunc)  { _listView.setItemTextFunction(aFunc); }

/**
 * Returns method to configure list cells.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _listView.getCellConfigure(); }

/**
 * Sets method to configure list cells.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _listView.setCellConfigure(aCC); }

/**
 * Returns the paint for alternating cells.
 */
public Paint getAltPaint()  { return _listView.getAltPaint(); }

/**
 * Sets the paint for alternating cells.
 */
public void setAltPaint(Paint aPaint)  { _listView.setAltPaint(aPaint); }

/**
 * Returns whether to fire action on mouse release instead of press.
 */
public boolean isFireActionOnRelease()  { return _listView.isFireActionOnRelease(); }

/**
 * Sets whether to fire action on mouse release instead of press.
 */
public void setFireActionOnRelease(boolean aValue)  { _listView.setFireActionOnRelease(aValue); }

/**
 * Returns whether list shows visual cue for item under the mouse.
 */
public boolean isTargeting()  { return _listView.isTargeting(); }

/**
 * Sets whether list shows visual cue for item under the mouse.
 */
public void setTargeting(boolean aValue)  { _listView.setTargeting(aValue); }

/**
 * Called to update items in list that have changed.
 */
public void updateItems(T ... theItems)  { _listView.updateItems(theItems); }

/**
 * Returns the cell at given index.
 */
public ListCell <T> getCell(int anIndex)  { return _listView.getCell(anIndex); }

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
 * Override to layout children with VBox layout.
 */
protected void layoutImpl()  { ViewLayout.layoutBasic(this, _scroll); }

/**
 * Returns text for item.
 */
public String getText(T anItem)  { return _listView.getText(anItem); }

/**
 * Override to return text for currently selected item.
 */
public String getText()  { return _listView.getText(); }

/**
 * Override to set the given text in the ListView by matching it to existing ListText item.
 */
public void setText(String aString)  { _listView.setText(aString); }

/**
 * Called to configure a cell.
 */
protected void configureCell(ListCell <T> aCell)  { }

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return getBinding("SelectedIndex")!=null? "SelectedIndex" : "SelectedItem"; }

/**
 * Catches property changes from ListView and redispatches for this ListViewScroll.
 */
void listViewPropChange(PropChange aPC)
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