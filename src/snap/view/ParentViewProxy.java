/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.ArrayUtils;
import snap.util.MathUtils;

/**
 * A ViewProxy that can layout content in the manner of BoxView for any View.
 */
public abstract class ParentViewProxy<T extends View> extends ViewProxy<T> {

    // The number of children that grow width/height
    protected int _growWidthCount = -1, _growHeightCount = -1;

    /**
     * Constructor for given parent view.
     */
    public ParentViewProxy(View aParent)
    {
        super(aParent);
    }

    /**
     * Returns the number of children that grow width.
     */
    public int getGrowWidthCount()
    {
        if (_growWidthCount >= 0) return _growWidthCount;
        return _growWidthCount = ArrayUtils.count(getChildren(), child -> child.isGrowWidth());
    }

    /**
     * Returns the number of children that grow height.
     */
    public int getGrowHeightCount()
    {
        if (_growHeightCount >= 0) return _growHeightCount;
        return _growHeightCount = ArrayUtils.count(getChildren(), child -> child.isGrowHeight());
    }

    /**
     * Returns the MaxX of last child with insets.
     */
    public double getLastChildMaxXWithInsets()
    {
        // Get info
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();
        ViewProxy<?> lastChild = getLastChild();
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
        ViewProxy<?> lastChild = getLastChild();
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
        ViewProxy<?>[] children = getChildren();
        double childrenMaxX = parentPadding.getWidth() + borderInsets.getWidth();

        // Iterate over children to get MaxX
        for (ViewProxy<?> child : children) {
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
        ViewProxy<?>[] children = getChildren();
        double childMaxYAll = parentPadding.getHeight() + borderInsets.getHeight();

        // Iterate over children to get MaxY
        for (ViewProxy<?> child : children) {
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

        View view = getView();
        if (view != null) {
            double minW = view.getMinWidth();
            double maxW = view.getMaxWidth();
            prefW = MathUtils.clamp(prefW, minW, maxW);
        }

        return prefW;
    }

    /**
     * Returns the best height for view - accounting for pref/min/max.
     */
    public double getBestHeight(double aW)
    {
        double prefH = getPrefHeight(aW);

        View view = getView();
        if (view != null) {
            double minH = view.getMinHeight();
            double maxH = view.getMaxHeight();
            prefH = MathUtils.clamp(prefH, minH, maxH);
        }

        return prefH;
    }
}