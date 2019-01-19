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
    
    // The spacing between items (really the default span of the dividers)
    double                  _spacing = 8;
    
    // The default divider
    Divider                 _divider;
    
    // The divider currently being dragged (and the offset from center of drag start)
    Divider                 _dragDiv; double _dragOff;

    // A listener to watch for when item.Visible changes
    PropChangeListener      _visLsnr = pc -> itemVisibleChanged(pc);
    
    // The default border
    static final Border SPLIT_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);

/**
 * Creates a new SplitView.
 */
public SplitView()
{
    setBorder(SPLIT_VIEW_BORDER);
    addEventFilter(e -> processDividerEvent(e), MouseMove, MousePress, MouseDrag, MouseRelease);
}

/**
 * Returns the default width of the dividers.
 */
public double getSpacing()  { return _spacing; }

/**
 * Sets the default width of the dividers.
 */
public void setSpacing(double aValue)
{
    if(aValue==_spacing) return;
    for(Divider div : _divs) div.setPrefSpan(aValue);
    firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
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
        Divider div = createDivider();
        addDivider(div, anIndex>0? (anIndex-1) : 0);
        addChild(div, anIndex>0? (anIndex*2-1) : 0);
        
        // See if divider should be not-visible
        boolean vis = aView.isVisible(); if(anIndex==1) vis &= getItem(0).isVisible();
        div.setVisible(vis);
    }
        
    // Add view as child
    addChild(aView, anIndex*2);
    aView.addPropChangeListener(_visLsnr, Visible_Prop);
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
        div.getAnimCleared(500).setValue(Divider.Location_Prop, 1d, aSize).play();
    }
    
    else {
        div.setRemainder(1);
        div.getAnimCleared(500).setValue(Divider.Remainder_Prop, 1d, aSize).play();
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
        div.getAnimCleared(500).setValue(Divider.Location_Prop, size, 1d).setOnFinish(a -> removeItem(aView)).play();
    }
    
    else {
        div.setRemainder(size);
        div.getAnimCleared(500).setValue(Divider.Remainder_Prop, size, 1d).setOnFinish(a -> removeItem(aView)).play();
    }
}

/**
 * Sets a child visible with animation.
 */
public void setItemVisibleWithAnim(View aView, boolean aValue)
{
    // If already set, just return
    if(aValue==aView.isVisible()) return;
    
    // Get index, divider and size
    int index = indexOfItem(aView), time = 500;
    Divider div = index==0? getDivider(0) : getDivider(index-1);
    double size = isVertical()? aView.getHeight() : aView.getWidth();
    
    // Clear running anims
    aView.getAnimCleared(0); div.getAnimCleared(0);
    
    // Handle visible true
    if(aValue) {
        double dsize = div.getSpan();
        if(index==0) { div.setLocation(0); div.getAnim(time).setValue(Divider.Location_Prop, dsize, size).play(); }
        else { div.setRemainder(1); div.getAnim(time).setValue(Divider.Remainder_Prop, dsize, size).play(); }
        aView.setVisible(true); aView.setOpacity(0); div.setOpacity(0);
        aView.getAnim(time).setOpacity(1).play();
        div.getAnim(time).setOpacity(1).play();
    }
    
    // Handle visible false
    else {
        if(index==0) { div.setLocation(size); div.getAnim(time).setValue(Divider.Location_Prop, size, 1d).play(); }
        else { div.setRemainder(size); div.getAnim(time).setValue(Divider.Remainder_Prop, size, 1d).play(); }
        aView.setOpacity(1); div.setOpacity(1);
        div.getAnim(time).setOpacity(0).play();
        aView.getAnim(time).setOpacity(0).setOnFinish(a -> {
            aView.setVisible(false); aView.setOpacity(1); div.setOpacity(1);
            if(isVertical()) aView.setHeight(size); else aView.setWidth(size);
        }).play();
    }
}

/**
 * Called when an item changes the value of visible property.
 */
void itemVisibleChanged(PropChange aPC)
{
    if(getItemCount()<2) return;

    View view = (View)aPC.getSource();
    int ind = getItems().indexOf(view);
    Divider div = getDivider(ind>0? ind-1 : 0);
    boolean vis = view.isVisible(); if(ind==1) vis &= getItem(0).isVisible();
    div.setVisible(vis);
}

/**
 * Returns the default divider.
 */
public Divider getDivider()
{
    // If already set, just return
    if(_divider!=null) return _divider;
    
    // Create and return
    Divider div = new Divider();
    div.setVertical(!isVertical()); div.setBorder(Divider.DIVIDER_BORDER);
    div.addPropChangeListener(pc -> dividerPropChange(pc), Fill_Prop, Border_Prop);
    return _divider = div;
}

/**
 * Creates a new divider.
 */
protected Divider createDivider()
{
    Divider div0 = getDivider(), div = new Divider();
    div.setVertical(!isVertical());
    div.setFill(div0.getFill()); div.setBorder(div0.getBorder()); div.setReach(div0.getReach());
    div.setPrefSpan(getSpacing());
    return div;
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
 * Returns the divider at given point.
 */
public Divider getDividerAt(double aX, double aY)
{
    // Handle vertical
    if(isVertical()) { for(Divider div : _divs) { if(!div.isVisible()) continue;
        double min = div.getY() - div.getReach(), max = div.getMaxY() + div.getReach();
        if(aY>=min && aY<=max) return div;
    }}
    
    // Handle horizontal
    else { for(Divider div : _divs) { if(!div.isVisible()) continue;
        double min = div.getX() - div.getReach(), max = div.getMaxX() + div.getReach();
        if(aX>=min && aX<=max) return div;
    }}
    return null;
}

/**
 * Called when divider has prop change.
 */
void dividerPropChange(PropChange aPC)
{
    String pname = aPC.getPropName();
    if(pname==Fill_Prop) for(Divider d : _divs) d.setFill(_divider.getFill());
    else if(pname==Border_Prop) for(Divider d : _divs) d.setBorder(_divider.getBorder());
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    if(isHorizontal())
        return RowView.getPrefWidth(this, null, 0, aH);
    return ColView.getPrefWidth(this, null, -1);    
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    if(isHorizontal())
        return RowView.getPrefHeight(this, null, aW);
    return ColView.getPrefHeight(this, null, 0, -1);
}

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    // Do normal layout
    if(isHorizontal())
        RowView.layout(this, null, null, true, 0);
    else ColView.layout(this, null, null, true, 0);
    
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
 * Handle MouseDrag event: Calcualte and set new location.
 */
protected void processDividerEvent(ViewEvent anEvent)
{
    // Handle MouseMove
    if(anEvent.isMouseMove()) {
        Divider div = getDividerAt(anEvent.getX(), anEvent.getY());
        if(div!=null) { WindowView win = getWindow(); if(win!=null) win.setActiveCursor(div.getCursor()); }
    }
    
    // Handle MouseDrag: Calculate new location and set
    else if(anEvent.isMouseDrag()) { if(_dragDiv==null) return;
        
        // Get view before divider and adjust divider location
        Divider div = _dragDiv; double x = anEvent.getX(), y = anEvent.getY();
        int index = indexOfChild(div); View peer0 = getChild(index-1);
        double loc = div.isVertical()? (x - div.getWidth()/2 - peer0.getX()) : (y - div.getHeight()/2 - peer0.getY());
        div.setLocation(loc + _dragOff);
        anEvent.consume();
    }
    
    // Handle MousePress: Check for divider hit
    else if(anEvent.isMousePress()) {
        _dragDiv = getDividerAt(anEvent.getX(), anEvent.getY());
        if(_dragDiv!=null) { anEvent.consume();
            _dragOff = isVertical()? _dragDiv.getMidY() - anEvent.getY() : _dragDiv.getMidX() - anEvent.getX(); }
    }
        
    // Handle MousePress: Clear DragDiv
    else if(anEvent.isMouseRelease()) {
        if(_dragDiv!=null) { _dragDiv = null; anEvent.consume(); }
    }
}

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return SPLIT_VIEW_BORDER; }

/**
 * Override to forward to dividers.
 */
public void setVertical(boolean aValue)
{
    if(aValue==isVertical()) return; super.setVertical(aValue);
    if(_divider!=null) _divider.setVertical(!aValue);
    for(Divider d : _divs) d.setVertical(!aValue);
}

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

/**
 * Replaces the given view with a SplitView.
 */
public static SplitView makeSplitView(View aView)
{
    SplitView split = new SplitView();
    split.setVertical(aView.isVertical());
    split.setLeanX(aView.getLeanX()); split.setLeanY(aView.getLeanY());
    split.setGrowWidth(aView.isGrowWidth()); split.setGrowHeight(aView.isGrowHeight());
    
    // Handle HostView
    if(aView instanceof HostView) { HostView hview = (HostView)aView;
        split.setItems(hview.getGuests());
    }
    
    // Replace
    if(aView.getParent()!=null) ViewUtils.replaceView(aView, split);
    return split;
}

}