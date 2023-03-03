/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;

/**
 * A View to manage a list of items (ListArea) in a ScrollView.
 */
public class ListView <T> extends ParentView implements Selectable<T> {
    
    // The ListArea (real ListView functionality without scroll)
    private ListArea<T>  _listArea;
    
    // The ScrollView
    private ScrollView  _scrollView;
    
    // The Preferred number of rows
    private int  _prefRowCount = -1;
    
    // The maximum number of rows
    private int  _maxRowCount = -1;

    // Constants for properties
    public static final String ItemKey_Prop = ListArea.ItemKey_Prop;
    public static final String Sel_Prop = ListArea.Sel_Prop;

    /**
     * Creates a new ListView.
     */
    public ListView()
    {
        super();
        setActionable(true);
        enableEvents(KeyPress);
        setBorder(ScrollView.DEFAULT_SCROLL_VIEW_BORDER);

        // Create/configure ListArea
        _listArea = createListArea();
        _listArea.addEventHandler(e -> listAreaDidFireActionEvent(e), Action);
        _listArea.addPropChangeListener(pce -> listAreaPropChange(pce));

        // Enable Focus
        setFocusable(true);
        setFocusWhenPressed(true);

        // Create/configure ScrollView
        _scrollView = createScrollView();
        _scrollView.setBorder(null);
        _scrollView.setContent(_listArea);
        addChild(_scrollView);
    }

    /**
     * Returns the ListArea.
     */
    public ListArea <T> getListArea()  { return _listArea; }

    /**
     * Creates the ListArea.
     */
    protected ListArea<T> createListArea()  { return new ListArea<>(); }

    /**
     * Returns the ScrollView.
     */
    public ScrollView getScrollView()  { return _scrollView; }

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
    public int getItemCount()  { return _listArea.getItemCount(); }

    /**
     * Returns the individual item at index.
     */
    public T getItem(int anIndex)  { return _listArea.getItem(anIndex); }

    /**
     * Returns the items.
     */
    public List <T> getItems()  { return _listArea.getItems(); }

    /**
     * Sets the items.
     */
    public void setItems(List <T> theItems)  { _listArea.setItems(theItems); }

    /**
     * Sets the items.
     */
    public void setItems(T ... theItems)  { _listArea.setItems(theItems); }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()  { return _listArea.getSelIndex(); }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)  { _listArea.setSelIndex(anIndex); }

    /**
     * Returns the selected item.
     */
    public T getSelItem()  { return _listArea.getSelItem(); }

    /**
     * Sets the selected index.
     */
    public void setSelItem(T anItem)  { _listArea.setSelItem(anItem); }

    /**
     * Selects up in the list.
     */
    public void selectUp()  { _listArea.selectUp(); }

    /**
     * Selects up in the list.
     */
    public void selectDown()  { _listArea.selectDown(); }

    /**
     * Returns the row height.
     */
    public double getRowHeight()  { return _listArea.getRowHeight(); }

    /**
     * Sets the row height.
     */
    public void setRowHeight(double aValue)  { _listArea.setRowHeight(aValue); }

    /**
     * Sets function for deteriming text for an item.
     */
    public void setItemTextFunction(Function <T,String> aFunc)  { _listArea.setItemTextFunction(aFunc); }

    /**
     * Returns method to configure list cells.
     */
    public Consumer<ListCell<T>> getCellConfigure()  { return _listArea.getCellConfigure(); }

    /**
     * Sets method to configure list cells.
     */
    public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _listArea.setCellConfigure(aCC); }

    /**
     * Returns the ItemKey (a simple alternate way to set ListArea item text using KeyChain).
     */
    public String getItemKey()  { return _listArea.getItemKey(); }

    /**
     * Sets the ItemKey (a simple alternate way to set ListArea item text using KeyChain).
     */
    public void setItemKey(String aKey)  { _listArea.setItemKey(aKey); }

    /**
     * Returns the paint for alternating cells.
     */
    public Paint getAltPaint()  { return _listArea.getAltPaint(); }

    /**
     * Sets the paint for alternating cells.
     */
    public void setAltPaint(Paint aPaint)  { _listArea.setAltPaint(aPaint); }

    /**
     * Returns whether list shows visual cue for item under the mouse.
     */
    public boolean isTargeting()  { return _listArea.isTargeting(); }

    /**
     * Sets whether list shows visual cue for item under the mouse.
     */
    public void setTargeting(boolean aValue)  { _listArea.setTargeting(aValue); }

    /**
     * Called to update items in list that have changed.
     */
    public void updateItems(T ... theItems)  { _listArea.updateItems(theItems); }

    /**
     * Returns the cell at given index.
     */
    public ListCell <T> getCell(int anIndex)  { return _listArea.getCell(anIndex); }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _scrollView, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // If PrefRowCount set, return PrefRowCount*RowHeight
        if (getPrefRowCount() > 0)
            return getPrefRowCount() * getRowHeight() + getInsetsAll().getHeight();

        // Return pref height of Scroll
        return BoxView.getPrefHeight(this, _scrollView, aW);
    }

    /**
     * Override to layout ScrollView.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _scrollView, true, true);
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

    /**
     * Returns text for item.
     */
    public String getText(T anItem)  { return _listArea.getText(anItem); }

    /**
     * Override to return text for currently selected item.
     */
    public String getText()  { return _listArea.getText(); }

    /**
     * Override to forward to ListArea.
     */
    public void setText(String aString)  { _listArea.setText(aString); }

    /**
     * Called to configure a cell.
     */
    protected void configureCell(ListCell <T> aCell)  { }

    /**
     * Process events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle KeyPress
        if (anEvent.isKeyPress()) {
            int keyCode = anEvent.getKeyCode();
            switch (keyCode) {
                case KeyCode.UP: selectUp(); anEvent.consume(); break;
                case KeyCode.DOWN: selectDown(); anEvent.consume(); break;
                case KeyCode.ENTER: _listArea.processEnterAction(anEvent); break;
            }
        }
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()
    {
        return getBinding(SelIndex_Prop)!=null ? SelIndex_Prop : SelItem_Prop;
    }

    /**
     * Catches ListArea Action event to resend from this ListView (and suppress original).
     */
    void listAreaDidFireActionEvent(ViewEvent anEvent)
    {
        fireActionEvent(anEvent);
        anEvent.consume();
    }

    /**
     * Catches property changes from ListArea and redispatches for this ListView.
     */
    void listAreaPropChange(PropChange aPC)
    {
        String pname = aPC.getPropName();
        if (pname==ListArea.Sel_Prop)
            firePropChange(Sel_Prop, aPC.getOldValue(), aPC.getNewValue());
    }

    /**
     * Override to sync ScrollView and ListArea.
     */
    @Override
    public void setBorderRadius(double aValue)
    {
        super.setBorderRadius(aValue);
        _scrollView.setBorderRadius(aValue);
        _listArea.setBorderRadius(aValue);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ItemKey
        if (getItemKey()!=null) e.add(ItemKey_Prop, getItemKey());

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

        // Unarchive ItemKey
        if (anElement.hasAttribute(ItemKey_Prop))
            setItemKey(anElement.getAttributeValue(ItemKey_Prop));
    }
}