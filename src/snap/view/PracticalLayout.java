/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.MathUtils;

/**
 * A base layout where subclasses only need to implement layoutViewLayout and pref sizes will be calculated from that.
 */
public abstract class PracticalLayout<T extends View> extends ViewLayout<T> {

    // The last calculated pref width and height
    private double _prefW = -1, _prefH = -1;

    /**
     * Constructor for given parent view.
     */
    public PracticalLayout(View aParent)
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
        if (_view.isPrefWidthSet())
            return _view.getPrefWidth(aH);

        // Perform layout with given height
        double oldW = width, oldH = height;
        double prefH = aH > 0 ? aH : _view.isPrefHeightSet() ? _view.getPrefHeight() : -1;
        setSize(-1, prefH);
        layoutViewLayout();
        width = oldW; height = oldH;

        // Get pref width/height and return
        _prefW = getPrefWidthImpl(aH);
        _prefH = getPrefHeightImpl(_prefW);
        return _prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        if (_prefH >= 0 && (aW < 0 || aW == _prefW))
            return _prefH;
        if (_view.isPrefHeightSet())
            return _view.getPrefHeight(aW);

        // Perform layout with given width
        double oldW = width, oldH = height;
        double prefW = aW > 0 ? aW : _view.isPrefWidthSet() ? _view.getPrefWidth() : -1;
        setSize(prefW, -1);
        layoutViewLayout();
        width = oldW; height = oldH;

        // Get pref height/width and return
        _prefH = getPrefHeightImpl(prefW);
        _prefW = getPrefWidthImpl(_prefH);
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
     * Returns preferred width of given parent with given children.
     */
    protected double getPrefWidthImpl(double aH)  { return getChildrenMaxXWithInsets(); }

    /**
     * Returns preferred height of given parent with given children.
     */
    protected double getPrefHeightImpl(double aW)  { return getChildrenMaxYWithInsets(); }

    /**
     * Performs layout of child layouts.
     */
    public abstract void layoutViewLayout();

    /**
     * Returns the MaxX of last child with insets.
     */
    public double getLastChildMaxXWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout<?> lastChild = getLastChild();
        Insets lastChildMargin = lastChild != null ? lastChild.getMargin() : Insets.EMPTY;

        // Return LastChildMaxX plus right margin plus border right
        double lastChildMaxX = lastChild != null ? lastChild.getMaxX() : 0;
        double rightMargin = Math.max(parentPadding.right, lastChildMargin.right);
        return Math.ceil(lastChildMaxX + rightMargin + borderInsets.right);
    }

    /**
     * Returns the MaxY of last child with insets.
     */
    public double getLastChildMaxYWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout<?> lastChild = getLastChild();
        Insets lastChildMargin = lastChild != null ? lastChild.getMargin() : Insets.EMPTY;

        // Return LastChildMaxY plus bottom margin plus border bottom
        double lastChildMaxY = lastChild != null ? lastChild.getMaxY() : 0;
        double bottomMargin = Math.max(parentPadding.bottom, lastChildMargin.bottom);
        return Math.ceil(lastChildMaxY + bottomMargin + borderInsets.bottom);
    }

    /**
     * Returns the MaxX of children with insets.
     */
    public double getChildrenMaxXWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout<?>[] children = getChildren();
        double childrenMaxX = parentPadding.getWidth() + borderInsets.getWidth();

        // Iterate over children to get MaxX
        for (ViewLayout<?> child : children) {
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
    public double getChildrenMaxYWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewLayout<?>[] children = getChildren();
        double childMaxYAll = parentPadding.getHeight() + borderInsets.getHeight();

        // Iterate over children to get MaxY
        for (ViewLayout<?> child : children) {
            double childMaxY = child.getMaxY();
            Insets childMargin = child.getMargin();
            double bottomMargin = Math.max(childMargin.bottom, parentPadding.bottom);
            childMaxYAll = Math.max(childMaxYAll, childMaxY + bottomMargin);
        }

        // Return (round up)
        return Math.ceil(childMaxYAll + borderInsets.bottom);
    }

    /**
     * Returns the best width for view - accounting for pref/min/max.
     */
    public double getBestWidth(double aH)
    {
        double prefW = getPrefWidth(aH);
        double minW = _view.getMinWidth();
        double maxW = _view.getMaxWidth();
        return MathUtils.clamp(prefW, minW, maxW);
    }

    /**
     * Returns the best height for view - accounting for pref/min/max.
     */
    public double getBestHeight(double aW)
    {
        double prefH = getPrefHeight(aW);
        double minH = _view.getMinHeight();
        double maxH = _view.getMaxHeight();
        return MathUtils.clamp(prefH, minH, maxH);
    }
}