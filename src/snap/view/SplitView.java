/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show children with user adjustable divider.
 */
public class SplitView extends ParentView {

    // The list of items
    List <View>             _items = new ArrayList();
    
    // The list of dividers
    List <Divider>          _divs = new ArrayList();
    
    // The layout
    ViewLayout.BoxesLayout  _layout = new ViewLayout.BoxesLayout(this);
    
    // The default border
    static final Border SPLIT_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);

/**
 * Creates a new SplitView.
 */
public SplitView()
{
    setBorder(SPLIT_VIEW_BORDER);
    _layout.setFillOut(true);
}

/**
 * Returns the number of items.
 */
public int getItemCount()  { return _items.size(); }

/**
 * Returns the individual item at given index.
 */
public View getItem(int anIndex)  { return _items.get(anIndex); }

/**
 * Override to make sure dividers are in place.
 */
public void addItem(View aView)  { addItem(aView, getItemCount()); }

/**
 * Returns the SplitView items.
 */
public List <View> getItems()  { return _items; }

/**
 * Override to make sure dividers are in place.
 */
public void addItem(View aView, int anIndex)
{
    // Add View item
    _items.add(anIndex,aView);
    
    // If more than one item, add divider
    if(getItemCount()>1) {
        Divider div = new Divider();
        addDivider(div, anIndex>0? (anIndex-1) : 0);
        addChild(div, anIndex>0? (anIndex*2-1) : 0);
    }
        
    // Add view as child
    addChild(aView, anIndex*2);
}

/**
 * Override to remove unused dividers.
 */
public View removeItem(int anIndex)
{
    // Remove item and child
    View view = _items.remove(anIndex);
    removeChild(view);
    
    // If at least one item left, remove extra divider
    if(getItemCount()>0)
        removeDivider(anIndex>0? (anIndex-1) : 0);
    return view;
}

/**
 * Override to remove unused dividers.
 */
public int removeItem(View aView)
{
    int index = indexOfItem(aView);
    if(index>=0) removeItem(index);
    return index;
}

/**
 * Sets the item at index.
 */
public void setItem(View aView, int anIndex)
{
    View old = anIndex<getItemCount()? _items.get(anIndex) : null;
    int index = old!=null? removeItem(old) : -1;
    addItem(aView, index>=0? index : getItemCount());
}

/**
 * Sets the splitview items to given views
 */
public void setItems(View ... theViews)
{
    removeItems();
    for(View view : theViews) addItem(view);
}

/**
 * Sets the splitview items to given views
 */
public void removeItems()  { for(View view : getItems().toArray(new View[0])) removeItem(view); }

/**
 * Returns the index of given item.
 */
public int indexOfItem(View anItem)  { return ListUtils.indexOfId(_items, anItem); }

/**
 * Adds a child with animation.
 */
public void addItemWithAnim(View aView, double aSize)  { addItemWithAnim(aView, aSize, getItemCount()); }

/**
 * Adds a child with animation.
 */
public void addItemWithAnim(View aView, double aSize, int anIndex)
{
    addItem(aView, anIndex);
    Divider div = anIndex==0? getDivider(0) : getDivider(anIndex-1);
    
    if(anIndex==0) {
        div.setLocation(0);
        div.getAnimCleared(500).setValue("Location", 1d, aSize).play();
    }
    
    else {
        div.setRemainder(1);
        div.getAnimCleared(500).setValue("Remainder", 1d, aSize).play();
    }
}

/**
 * Removes a child with animation.
 */
public void removeItemWithAnim(View aView)
{
    int index = indexOfItem(aView);
    Divider div = index==0? getDivider(0) : getDivider(index-1);
    double size = isVertical()? aView.getHeight() : aView.getWidth();
    
    if(index==0) {
        div.setLocation(size);
        div.getAnimCleared(500).setValue("Location", size, 1d).setOnFinish(a -> removeItem(aView)).play();
    }
    
    else {
        div.setRemainder(size);
        div.getAnimCleared(500).setValue("Remainder", size, 1d).setOnFinish(a -> removeItem(aView)).play();
    }
}

/**
 * Returns the number of dividers.
 */
public int getDividerCount()  { return _divs.size(); }

/**
 * Returns the individual divider at given index.
 */
public Divider getDivider(int anIndex)  { return _divs.get(anIndex); }

/**
 * Adds a new divider.
 */
protected void addDivider(Divider aDiv, int anIndex)  { _divs.add(anIndex, aDiv); }

/**
 * Removes a divider.
 */
protected Divider removeDivider(int anIndex)
{
    Divider div = _divs.remove(anIndex);
    removeChild(div);
    return div;
}

/**
 * Returns the dividers.
 */
public Divider[] getDividers()  { return _divs.toArray(new Divider[_divs.size()]); }

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    // Do normal layout
    _layout.layoutChildren();
    
    // If children don't fill main axis, grow last child
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    View child = getChildLast(); if(child==null) return;
    if(isHorizontal() && MathUtils.lt(child.getMaxX(),w))
        child.setWidth(w - child.getX());
    else if(isVertical() && MathUtils.lt(child.getMaxY(),h))
        child.setHeight(h - child.getY());
}

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return SPLIT_VIEW_BORDER; }

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive items
    for(View item : getItems()) {
        XMLElement cxml = anArchiver.toXML(item, this);
        anElement.add(cxml);
    }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child nodes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && View.class.isAssignableFrom(cls)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            addItem(view);
        }
    }
}

}