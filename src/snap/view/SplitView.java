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
    if(getItemCount()>0)        // Add divider, unless only item
        addChild(new Divider(), anIndex>0? anIndex*2-1 : 0);
    addChild(aView, anIndex*2);
    _items.add(aView);
}

/**
 * Override to remove unused dividers.
 */
public void removeItem(int anIndex)
{
    _items.remove(anIndex);
    removeChild(anIndex*2);
    if(getItemCount()>0)        // Remove divider, unless only item
        removeChild(anIndex>0? anIndex*2-1 : 0);
}

/**
 * Override to remove unused dividers.
 */
public int removeItem(View aView)
{
    int index = ListUtils.indexOfId(_items, aView);
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
 * Adds a child with animation.
 */
public void addItemWithAnim(View aView, double aSize)
{
    addItem(aView); getDivider(0).setRemainder(1);
    getDivider(0).getAnimCleared(500).setValue("Remainder", 1d, aSize).play();
}

/**
 * Removes a child with animation.
 */
public void removeItemWithAnim(View aView)
{
    double size = isVertical()? aView.getHeight() : aView.getWidth(); getDivider(0).setRemainder(size);
    getDivider(0).getAnimCleared(500).setValue("Remainder", size, 1d).setOnFinish(a -> removeItem(aView)).play();
}

/**
 * Returns the number of dividers.
 */
public int getDividerCount()  { return Math.max(getItemCount() - 1, 0); }

/**
 * Returns the individual divider at given index.
 */
public Divider getDivider(int anIndex)  { return (Divider)getChild(anIndex*2+1); }

/**
 * Returns the dividers.
 */
public Divider[] getDividers()
{
    int dc = getDividerCount();
    Divider divs[] = new Divider[dc]; for(int i=0;i<dc;i++) divs[i] = getDivider(i);
    return divs;
}

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
protected void layoutImpl()  { _layout.layoutChildren(); }

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