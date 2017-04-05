/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.ArrayUtils;

/**
 * A View to represent a movable separation between views.
 */
public class Divider extends View {
    
    // The size
    double    _size = 8;
    
    // The distance from the min x of preceeding View to min x of divider (or y if vertical)
    double    _loc = -1;
    
    // The remainder, if explicitly position relative to right side
    double    _rem = -1;
    
    // Constants for Divider Fill
    static final Color c1 = Color.get("#fbfbfb"), c2 = Color.get("#e3e3e3");
    static final Paint DIVIDER_FILL_HOR = new GradientPaint(c1, c2, 90);
    static final Paint DIVIDER_FILL_VER = new GradientPaint(c1, c2, 0);
    static final Border DIVIDER_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);
    
    // Constants for properties
    public static final String DividerSize_Prop = "DividerSize";
    
/**
 * Creates a new Divider.
 */
public Divider()
{
    enableEvents(MousePress, MouseDrag);
    setBorder(DIVIDER_BORDER);
}

/**
 * Returns the size of the divider.
 */
public double getDividerSize()  { return _size; }

/**
 * Sets the size of the divider.
 */
public void setDividerSize(double aValue)
{
    firePropChange(DividerSize_Prop, _size, _size = aValue);
}

/**
 * Returns the distance from the min x of preceeding View to min x of divider (or y if vertical).
 */
public double getLocation()  { return _loc; }

/**
 * Sets the distance from the min x of preceeding View to min x of divider (or y if vertical).
 */
public void setLocation(double aValue)
{
    _loc = aValue; _rem = -1;
    relayoutParent();
}

/**
 * Returns the distance from the max x of successive View to min x of divider (or y if vertical).
 */
public double getRemainder()  { return _rem; }

/**
 * Sets the distance from the max x of successive View to min x of divider (or y if vertical).
 */
public void setRemainder(double aValue)
{
    _rem = aValue; _loc = -1;
    relayoutParent();
}

/**
 * Returns the remainder as location.
 */
public double getRemainderAsLocation()
{
    ParentView par = getParent(); if(par==null) return 0;
    if(isHorizontal())
        return _rem - par.getHeight() - par.getInsetsAll().bottom;
    return _rem - par.getWidth() - par.getInsetsAll().right;
}

/**
 * Override.
 */
public boolean isVertical()  { return getParent()!=null? !getParent().isVertical() : super.isVertical(); }

/**
 * Override.
 */
protected void setParent(ParentView aPar)
{
    super.setParent(aPar);
    setCursor(isVertical()? Cursor.E_RESIZE:Cursor.N_RESIZE);
    setFill(isVertical()? DIVIDER_FILL_VER : DIVIDER_FILL_HOR);
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return isVertical()? _size : 0; }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return isVertical()? 0 : _size; }

/**
 * Called to adjust layouts of children.
 */
public void adjustLayouts(View children[], Rect bounds[])
{
    // Get location and remainder (just return if neither is set)
    double loc = getLocation(), rem = getRemainder(); if(loc<0 && rem<0) return;
    
    // Get parent and peers and peer bounds
    ParentView par = getParent(); int index = ArrayUtils.indexOf(children, this);
    View view0 = children[index-1], view1 = children[index+1];
    Rect bnds0 = bounds[index-1], bnds1 = bounds[index+1], bnds = bounds[index];
    
    // If horizontal, get delta for divider location and shift peers (using y components)
    if(isHorizontal()) {
        if(loc<0) loc = bnds1.getMaxY() - rem - bnds.height - bnds0.getY();
        double delta = loc - (bnds.getY() - bnds0.getY());
        bnds0.height += delta; bnds.y += delta; bnds1.y += delta; bnds1.height -= delta;
    }
    
    // If vertical, get delta for divider location and shift peers (using x components)
    else {
        if(loc<0) loc = bnds1.getMaxX() - rem - bnds.width - bnds0.getX();
        double delta = loc - (bnds.getX() - bnds0.getX());
        bnds0.width += delta; bnds.x += delta; bnds1.x += delta; bnds1.width -= delta;
    }
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseDragged: If divider pressed, set location or remainder
    if(anEvent.isMouseDrag()) {
        
        // Get parent, divider peers, whether peers grow and event in parent coords
        ParentView par = getParent(); int index = par.indexOfChild(this); boolean ver = isVertical();
        View peer0 = par.getChild(index-1), peer1 = par.getChild(index+1);
        boolean peer0Grows = ver? peer0.isGrowWidth() : peer0.isGrowHeight();
        boolean peer1Grows = ver? peer1.isGrowWidth() : peer1.isGrowHeight();
        Point pnt = localToParent(anEvent.getX(), anEvent.getY());
        
        // If peer0 grows, set remainder
        if(peer0Grows) {
            double rem = ver? peer1.getMaxX() - pnt.getX() - getWidth()/2 : peer1.getMaxY() - pnt.getY() -getHeight()/2;
            setRemainder(rem);
        }
        
        // Otherwise, set location
        else {
            double loc = ver? pnt.getX() - getWidth()/2 - peer0.getX() : pnt.getY() - getHeight()/2 - peer0.getY();
            setLocation(loc);
        }
    }
}

}