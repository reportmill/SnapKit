/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;

/**
 * A base layout where subclasses only need to implement the actual layout and pref sizes are calculated from that.
 */
public abstract class EmpiricalLayout extends ViewLayout {

    // The last calculated pref width and height
    private double _prefW = -1, _prefH = -1;

    /**
     * Constructor for given parent view.
     */
    public EmpiricalLayout(View aParent)
    {
        super(aParent);
    }

    /**
     * Returns preferred width of layout.
     */
    public double getPrefWidth(double aH)
    {
        if (_prefW >= 0 && (aH < 0 || aH == _prefH))
            return _prefW;

        // Perform layout with given height
        double prefH = aH > 0 ? aH : _view.isPrefHeightSet() ? _view.getPrefHeight() : -1;
        setSize(-1, prefH);
        layoutViewLayout();

        // Get pref width/height and return
        _prefW = getLayoutPrefWidth();
        _prefH = getLayoutPrefHeight();
        return _prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        if (_prefH >= 0 && (aW < 0 || aW == _prefW))
            return _prefH;

        // Perform layout with given width
        double prefW = aW > 0 ? aW : _view.isPrefWidthSet() ? _view.getPrefWidth() : -1;
        setSize(prefW, -1);
        layoutViewLayout();

        // Get pref height/width and return
        _prefH = getLayoutPrefHeight();
        _prefW = getLayoutPrefWidth();
        return _prefH;
    }

    /**
     * Performs layout of child views.
     */
    public void layoutView()
    {
        // Perform layout for current view size
        setSize(_view.getWidth(), _view.getHeight());
        layoutViewLayout();

        // Apply bounds
        setBoundsInClient();
    }

    /**
     * Returns pref width for parent view given current layout of children.
     */
    protected double getLayoutPrefWidth()  { return getChildrenMaxXWithInsets(); }

    /**
     * Returns pref height for parent view given current layout of children.
     */
    protected double getLayoutPrefHeight()  { return getChildrenMaxYWithInsets(); }

    /**
     * Performs layout of child layouts.
     */
    public abstract void layoutViewLayout();

    /**
     * Returns the MaxX of last child with insets.
     */
    protected double getLastChildMaxXWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout lastChild = getLastChild();
        Insets lastChildMargin = lastChild != null ? lastChild.getMargin() : Insets.EMPTY;

        // Return LastChildMaxX plus right margin plus border right
        double lastChildMaxX = lastChild != null ? lastChild.getMaxX() : 0;
        double rightMargin = Math.max(parentPadding.right, lastChildMargin.right);
        return Math.ceil(lastChildMaxX + rightMargin + borderInsets.right);
    }

    /**
     * Returns the MaxY of last child with insets.
     */
    protected double getLastChildMaxYWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout lastChild = getLastChild();
        Insets lastChildMargin = lastChild != null ? lastChild.getMargin() : Insets.EMPTY;

        // Return LastChildMaxY plus bottom margin plus border bottom
        double lastChildMaxY = lastChild != null ? lastChild.getMaxY() : 0;
        double bottomMargin = Math.max(parentPadding.bottom, lastChildMargin.bottom);
        return Math.ceil(lastChildMaxY + bottomMargin + borderInsets.bottom);
    }

    /**
     * Returns the MaxX of children with insets.
     */
    protected double getChildrenMaxXWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout[] children = getChildren();
        double childrenMaxX = parentPadding.getWidth() + borderInsets.getWidth();

        // Iterate over children to get MaxX
        for (ViewLayout child : children) {
            double childMaxX = child.getMaxX();
            Insets childMargin = child.getMargin();
            double rightMargin = Math.max(childMargin.right, parentPadding.right);
            childrenMaxX = Math.max(childrenMaxX, childMaxX + rightMargin);
        }

        // Return (round up)
        return Math.ceil(childrenMaxX + borderInsets.right);
    }

    /**
     * Returns the MaxY of children with insets.
     */
    protected double getChildrenMaxYWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout[] children = getChildren();
        double childMaxYAll = parentPadding.getHeight() + borderInsets.getHeight();

        // Iterate over children to get MaxY
        for (ViewLayout child : children) {
            double childMaxY = child.getMaxY();
            Insets childMargin = child.getMargin();
            double bottomMargin = Math.max(childMargin.bottom, parentPadding.bottom);
            childMaxYAll = Math.max(childMaxYAll, childMaxY + bottomMargin);
        }

        // Return (round up)
        return Math.ceil(childMaxYAll + borderInsets.bottom);
    }
}