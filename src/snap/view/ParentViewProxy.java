/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
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
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowWidth()) count++;
        return _growWidthCount = count;
    }

    /**
     * Returns the number of children that grow height.
     */
    public int getGrowHeightCount()
    {
        if (_growHeightCount >= 0) return _growHeightCount;
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowHeight()) count++;
        return _growHeightCount = count;
    }

    /**
     * Returns the MaxX of last child with insets.
     */
    public double getChildrenMaxXLastWithInsets()
    {
        // Get LastChildMaxX, LastChildMarginRight
        ViewProxy[] children = getChildren();
        ViewProxy lastChild = children.length > 0 ? children[children.length-1] : null;
        double childMaxX = lastChild != null ? lastChild.getMaxX() : 0;
        double lastChildMarginRight = lastChild != null ? lastChild.getMargin().right : 0;

        // Return LastChildMaxX plus padding right
        Insets ins = getInsetsAll();
        double rightInset = Math.max(ins.right, lastChildMarginRight);
        return Math.ceil(childMaxX + rightInset);
    }

    /**
     * Returns the MaxX of children with insets.
     */
    public double getChildrenMaxXAllWithInsets()
    {
        // Get children
        ViewProxy[] children = getChildren();
        Insets ins = getInsetsAll();
        double childMaxXAll = ins.getWidth();

        // Iterate over children to get MaxX
        for (ViewProxy child : children) {
            double childMaxX = child.getMaxX();
            childMaxX += Math.max(child.getMargin().right, ins.right);
            childMaxXAll = Math.max(childMaxXAll, childMaxX);
        }

        // Return (round up)
        return Math.ceil(childMaxXAll);
    }

    /**
     * Returns the MaxY of last child with insets.
     */
    public double getChildrenMaxYLastWithInsets()
    {
        // Get LastChildMaxY, LastChildMarginBottom
        ViewProxy[] children = getChildren();
        ViewProxy lastChild = children.length > 0 ? children[children.length-1] : null;
        double lastChildMaxY = lastChild != null ? lastChild.getMaxY() : 0;
        double lastChildMarginBottom = lastChild != null ? lastChild.getMargin().bottom : 0;

        // Return LastChildMaxY plus padding bottom
        Insets ins = getInsetsAll();
        double bottomInset = Math.max(ins.bottom, lastChildMarginBottom);
        return Math.ceil(lastChildMaxY + bottomInset);
    }

    /**
     * Returns the MaxY of children with insets.
     */
    public double getChildrenMaxYAllWithInsets()
    {
        // Get children
        ViewProxy[] children = getChildren();
        Insets ins = getInsetsAll();
        double childMaxYAll = ins.getHeight();

        // Iterate over children to get MaxY
        for (ViewProxy child : children) {
            double childMaxY = child.getMaxY();
            childMaxY += Math.max(child.getMargin().bottom, ins.bottom);
            childMaxYAll = Math.max(childMaxYAll, childMaxY);
        }

        // Return (round up)
        return Math.ceil(childMaxYAll);
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

    /**
     * Returns preferred width of layout.
     */
    public double getPrefWidth(double aH)
    {
        setSize(-1, aH);
        layoutProxy();
        double prefW = getPrefWidthImpl(aH);
        return prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        setSize(aW, -1);
        layoutProxy();
        double prefH = getPrefHeightImpl(aW);
        return prefH;
    }

    /**
     * Performs BoxView layout.
     */
    public void layoutView()
    {
        // Layout
        layoutProxy();

        // Apply bounds
        setBoundsInClient();
    }

    /**
     * Returns preferred width of layout.
     */
    protected abstract double getPrefWidthImpl(double aH);

    /**
     * Returns preferred height of layout.
     */
    protected abstract double getPrefHeightImpl(double aW);

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public abstract void layoutProxy();
}