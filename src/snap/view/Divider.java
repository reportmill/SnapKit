/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.Convert;

/**
 * A View to represent a movable separation between views.
 */
public class Divider extends View {

    // The spacing between items (really the default span of the dividers)
    private double  _span;

    // The extra space beyond the divider bounds that should respond to resize
    private double  _reach;

    // Constants for properties
    public static final String PrefSpan_Prop = "PrefSpan";
    public static final String Reach_Prop = "Reach";
    public static final String Location_Prop = "Location";
    public static final String Remainder_Prop = "Remainder";

    // Constants for Divider Fill
    private static final Color c1 = Color.get("#fbfbfb"), c2 = Color.get("#e3e3e3");
    private static final Paint DIVIDER_FILL_HOR = new GradientPaint(c1, c2, 90);
    private static final Paint DIVIDER_FILL_VER = new GradientPaint(c1, c2, 0);
    public static final Border DIVIDER_BORDER = Border.createLineBorder(Color.LIGHTGRAY, 1);
    public static final int DEFAULT_SPAN = 8;

    /**
     * Creates a new Divider.
     */
    public Divider()
    {
        super();
        _span = DEFAULT_SPAN;
        setCursor(Cursor.N_RESIZE);
        setFill(DIVIDER_FILL_HOR);
        setBorder(DIVIDER_BORDER);
    }

    /**
     * Returns the size of the divider.
     */
    public double getSpan()
    {
        return isVertical() ? getWidth() : getHeight();
    }

    /**
     * Returns the preferred size of the divider.
     */
    public double getPrefSpan()  { return _span; }

    /**
     * Sets the size of the divider.
     */
    public void setPrefSpan(double aValue)
    {
        if (aValue == _span) return;
        firePropChange(PrefSpan_Prop, _span, _span = aValue);
    }

    /**
     * Returns the extra space beyond the span that divider should respond to.
     */
    public double getReach()  { return _reach; }

    /**
     * Sets the extra space beyond the span that divider should respond to.
     */
    public void setReach(double aValue)
    {
        if (aValue == _reach) return;
        firePropChange(Reach_Prop, _reach, _reach = aValue);
    }

    /**
     * Returns the distance from the min x of preceeding View to min x of divider (or y if vertical).
     */
    public double getLocation()
    {
        View peer0 = getViewBefore();
        double loc = isVertical() ? (getX() - peer0.getX()) : (getY() - peer0.getY());
        return loc;
    }

    /**
     * Sets the distance from the min x of preceeding View to min x of divider (or y if vertical).
     */
    public void setLocation(double aValue)
    {
        if (isVertical())
            setLocationV(aValue);
        else setLocationH(aValue);
    }

    /**
     * Implementation of setLocation for Vertical divider.
     */
    protected void setLocationV(double aX)
    {
        // Get parent and peer0
        ParentView par = getParent();
        int index = par.indexOfChild(this);
        int childCount = par.getChildCount();
        View peer0 = par.getChild(index - 1);

        // Set pref size of peer0
        peer0.setPrefWidth(aX);
        double extra = peer0.getWidth() - aX;

        // Find out how many remaining items grow
        int growCount = 0;
        for (int i = index + 1; i < childCount; i++) if (par.getChild(i).isGrowWidth()) growCount++;
        boolean parGrows = par.getParent() instanceof Scroller;

        // Handle somebody grows: Iterate over successive items and distribute extra size
        if (growCount > 0) {
            for (int i = index + 1; i < childCount; i++) {
                View child = par.getChild(i);
                if (child.isGrowWidth())
                    child.setPrefWidth(child.getWidth() + extra / growCount);
            }
        }

        // Handle nobody grows (compensate using last item)
        else if (growCount == 0 && !parGrows) {
            View child = par.getChildLast();
            child.setPrefWidth(child.getWidth() + extra);
        }
    }

    /**
     * Implementation of setLocation for Horizontal divider.
     */
    protected void setLocationH(double aY)
    {
        // Get parent and peer0
        ParentView par = getParent();
        int index = par.indexOfChild(this);
        int childCount = par.getChildCount();
        View peer0 = par.getChild(index - 1);

        // Set pref size of peer0
        peer0.setPrefHeight(aY);

        // Get extra size added/removed from view before
        double extra = peer0.getHeight() - aY;

        // Find out how many remaining items grow
        int growCount = 0;
        for (int i = index + 1; i < childCount; i++) if (par.getChild(i).isGrowHeight()) growCount++;
        boolean parGrows = par.getParent() instanceof Scroller;

        // Handle somebody grows: Iterate over successive items and distribute extra size
        if (growCount > 0) {
            for (int i = index + 1; i < childCount; i++) {
                View child = par.getChild(i);
                if (child.isGrowHeight())
                    child.setPrefHeight(child.getHeight() + extra / growCount);
            }
        }

        // Handle nobody grows (compensate using last item)
        else if (growCount == 0 && !parGrows) {
            View child = par.getChildLast();
            child.setPrefHeight(child.getHeight() + extra);
        }
    }

    /**
     * Returns the distance from the max x of successive View to max x of divider (or y if vertical).
     */
    public double getRemainder()
    {
        View peer1 = getViewAfter();
        double rem = isVertical() ? (peer1.getMaxX() - getMaxX()) : (peer1.getMaxY() - getMaxY());
        return rem;
    }

    /**
     * Sets the distance from the max x of successive View to max x of divider (or y if vertical).
     */
    public void setRemainder(double aValue)
    {
        // If Parent.NeedsLayout, do this (otherwise remainder could be bogus)
        ParentView par = getParent();
        if (par.isNeedsLayout())
            par.layout();

        // Get peers on ether side of divider
        int index = par.indexOfChild(this);
        View peer0 = par.getChild(index - 1);
        View peer1 = par.getChild(index + 1);

        // Get location for given remainder and set location
        double loc = isVertical() ? (peer1.getMaxX() - aValue - getSpan() - peer0.getX()) :
                (peer1.getMaxY() - aValue - getSpan() - peer0.getY());
        setLocation(loc);
    }

    /**
     * Returns the view before divider.
     */
    public View getViewBefore()
    {
        ParentView par = getParent();
        int index = par.indexOfChild(this);
        View peer0 = par.getChild(index - 1);
        return peer0;
    }

    /**
     * Returns the view after divider.
     */
    public View getViewAfter()
    {
        ParentView par = getParent();
        int index = par.indexOfChild(this);
        View peer1 = par.getChild(index + 1);
        return peer1;
    }

    /**
     * Override to configure attributes based on parent.Vertical.
     */
    @Override
    public void setVertical(boolean aValue)
    {
        // Do normal version
        if (aValue == isVertical()) return;
        super.setVertical(aValue);

        // Set Cursor and Fill based on Vertical
        setCursor(aValue ? Cursor.E_RESIZE : Cursor.N_RESIZE);
        if (getFill() == DIVIDER_FILL_VER)
            setFill(DIVIDER_FILL_HOR);
        else if (getFill() == DIVIDER_FILL_HOR)
            setFill(DIVIDER_FILL_VER);
    }

    /**
     * Override to return pref span.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return !isVertical() ? 0 : isDisabled() ? 1 : getPrefSpan();
    }

    /**
     * Override to return pref span.
     */
    @Override
    protected double getPrefHeightImpl(double aH)
    {
        return isVertical() ? 0 : isDisabled() ? 1 : getPrefSpan();
    }

    /**
     * Override to relayout parent since this can cause size change.
     */
    @Override
    public void setDisabled(boolean aValue)
    {
        // Do normal version
        if (aValue == isDisabled()) return;
        super.setDisabled(aValue);

        // If divider size and disabled size differ, trigger parent relayout
        if (getPrefSpan() != 1)
            relayoutParent();
    }

    /**
     * Override to handle extra props.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // PrefSpan, Reach
            case PrefSpan_Prop: return getPrefSpan();
            case Reach_Prop: return getReach();

            // Location, Remainder
            case Location_Prop: return getLocation();
            case Remainder_Prop: return getRemainder();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to handle extra props.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // PrefSpan, Reach
            case PrefSpan_Prop: setPrefSpan(Convert.doubleValue(aValue)); break;
            case Reach_Prop: setReach(Convert.doubleValue(aValue)); break;

            // Location, Remainder
            case Location_Prop: setLocation(Convert.doubleValue(aValue)); break;
            case Remainder_Prop: setRemainder(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}