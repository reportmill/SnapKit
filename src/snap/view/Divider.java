/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A View to represent a movable separation between views.
 */
public class Divider extends View {
    
    // The size
    double    _size = 8;
    
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
public double getLocation()
{
    ParentView par = getParent(); int index = par.indexOfChild(this);
    View peer0 = par.getChild(index-1);
    double loc = isVertical()? (getX() - peer0.getX()) : (getY() - peer0.getY());
    return loc;
}

/**
 * Sets the distance from the min x of preceeding View to min x of divider (or y if vertical).
 */
public void setLocation(double aValue)
{
    if(isVertical()) setLocationV(aValue);
    else setLocationH(aValue);
}

/** Implementation of setLocation for Vertical divider. */
protected void setLocationV(double aX)
{
    // Get parent and peer0
    ParentView par = getParent(); int index = par.indexOfChild(this), childCount = par.getChildCount();
    View peer0 = par.getChild(index-1);
    
    // Set pref size of peer0
    peer0.setPrefWidth(aX);
    double extra = peer0.getWidth() - aX;
    
    // Find out how many remaining items grow
    int growCount = 0; for(int i=index+1;i<childCount;i++) if(par.getChild(i).isGrowWidth()) growCount++;
    boolean parGrows = par.getParent() instanceof Scroller;
    
    // Handle somebody grows: Iterate over successive items and distribute extra size
    if(growCount>0) {
        for(int i=indexInParent()+1;i<par.getChildCount();i++) { View child = par.getChild(i);
            if(child.isGrowWidth())
                child.setPrefWidth(child.getWidth() + extra/growCount); }
    }
    
    // Handle nobody grows (compensate using last item)
    else if(growCount==0 && !parGrows) {
        View child = par.getChildLast();
        child.setPrefWidth(child.getWidth() + extra);
    }
}

/** Implementation of setLocation for Horizontal divider. */
protected void setLocationH(double aY)
{
    // Get parent and peer0
    ParentView par = getParent(); int index = par.indexOfChild(this), childCount = par.getChildCount();
    View peer0 = par.getChild(index-1);
    
    // Set pref size of peer0
    peer0.setPrefHeight(aY);
    double extra = peer0.getHeight() - aY;
    
    // Find out how many remaining items grow
    int growCount = 0; for(int i=index+1;i<childCount;i++) if(par.getChild(i).isGrowHeight()) growCount++;
    boolean parGrows = par.getParent() instanceof Scroller;
    
    // Handle somebody grows: Iterate over successive items and distribute extra size
    if(growCount>0) {
        for(int i=indexInParent()+1;i<par.getChildCount();i++) { View child = par.getChild(i);
            if(child.isGrowHeight())
                child.setPrefHeight(child.getHeight() + extra/growCount); }
    }
    
    // Handle nobody grows (compensate using last item)
    else if(growCount==0 && !parGrows) {
        View child = par.getChildLast();
        child.setPrefHeight(child.getHeight() + extra);
    }
    
}

/**
 * Returns the distance from the max x of successive View to max x of divider (or y if vertical).
 */
public double getRemainder()
{
    ParentView par = getParent(); int index = par.indexOfChild(this);
    View peer1 = par.getChild(index+1);
    double rem = isVertical()? (peer1.getMaxX() - getMaxX()) : (peer1.getMaxY() - getMaxY());
    return rem;
}

/**
 * Sets the distance from the max x of successive View to max x of divider (or y if vertical).
 */
public void setRemainder(double aValue)
{
    // Get parent and peers on ether side of divider
    ParentView par = getParent(); int index = par.indexOfChild(this);
    View peer0 = par.getChild(index-1), peer1 = par.getChild(index+1);
    
    // If Parent.NeedsLayout, do this (otherwise remainder could be bogus)
    if(par.isNeedsLayout())
        par.layout();
        
    double loc = isVertical()? (peer1.getMaxX() - aValue - getDividerSize() - peer0.getX()) :
        (peer1.getMaxY() - aValue - getDividerSize() - peer0.getY());
    setLocation(loc);
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
 * Handle MouseDrag event: Calcualte and set new location.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseDrag: Calculate new location and set
    if(anEvent.isMouseDrag()) {
        ParentView par = getParent(); int index = par.indexOfChild(this);
        Point pnt = localToParent(anEvent.getX(), anEvent.getY());
        View peer0 = par.getChild(index-1);
        double loc = isVertical()? (pnt.getX() - getWidth()/2 - peer0.getX()) :
            (pnt.getY() - getHeight()/2 - peer0.getY());
        setLocation(loc);
    }
}

}