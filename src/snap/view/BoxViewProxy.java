/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A ViewProxy that can layout content in the manner of BoxView for any View.
 */
public class BoxViewProxy<T extends View> extends ViewProxy<T> {

    // Whether child will crop to height if not enough space available
    private boolean  _cropHeight;

    /**
     * Constructor for given parent view.
     */
    public BoxViewProxy(View aParent)
    {
        super(aParent);

        if (aParent instanceof BoxView) {
            BoxView boxView = (BoxView) aParent;
            setFillWidth(boxView.isFillWidth());
            setFillHeight(boxView.isFillHeight());
            setCropHeight(boxView.isCropHeight());
        }
    }

    /**
     * Constructor for given parent view and FillWidth, FillHeight params.
     */
    public BoxViewProxy(View aParent, View aChild, boolean isFillWidth, boolean isFillHeight)
    {
        super(aParent);
        if (aChild != null)
            setContent(ViewProxy.getProxy(aChild));
        setFillWidth(isFillWidth);
        setFillHeight(isFillHeight);
    }

    /**
     * Returns whether child will crop to height if needed.
     */
    public boolean isCropHeight()  { return _cropHeight; }

    /**
     * Sets whether child will crop to height if needed.
     */
    public void setCropHeight(boolean aValue)
    {
        _cropHeight = aValue;
    }

    /**
     * Returns preferred width of layout.
     */
    public double getPrefWidth(double aH)
    {
        setSize(-1, aH);
        layoutProxy();
        double prefW = getChildrenMaxXLastWithInsets();
        return prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        setSize(aW, -1);
        layoutProxy();
        double prefH = getChildrenMaxYLastWithInsets();
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
     * Performs Box layout for given parent, child and fill width/height.
     */
    public void layoutProxy()
    {
        // Get parent info
        double viewW = getWidth();
        double viewH = getHeight();
        boolean isFillWidth = isFillWidth();
        boolean isFillHeight = isFillHeight();
        boolean isCropHeight = isCropHeight();

        // Get child
        ViewProxy<?> child = getContent(); if (child == null) return;

        // Get parent bounds for insets (just return if empty)
        Insets borderInsets = getBorderInsets();
        Insets pad = getPadding();
        Insets marg = child.getMargin();
        double areaX = borderInsets.left + Math.max(pad.left, marg.left);
        double areaY = borderInsets.top + Math.max(pad.top, marg.top);
        double areaW = Math.max(viewW - borderInsets.right - Math.max(pad.right, marg.right) - areaX, 0);
        double areaH = Math.max(viewH - borderInsets.bottom - Math.max(pad.bottom, marg.bottom) - areaY, 0);

        // Get content width
        double childW;
        if (viewW < 0)
            childW = child.getBestWidth(-1);
        else if (isFillWidth || child.isGrowWidth())
            childW = areaW;
        else childW = child.getBestWidth(-1);  // if (childW > areaW) childW = areaW;

        // Get content height
        double childH;
        if (viewH < 0)
            childH = child.getBestHeight(childW);
        else if (isFillHeight || child.isGrowHeight())
            childH = areaH;
        else childH = child.getBestHeight(childW);

        // If Parent.Width -1, just return (laying out for PrefWidth/PrefHeight)
        if (viewW < 0 || viewH < 0) {
            child.setBounds(areaX, areaY, childW, childH);
            return;
        }

        // If child needs crop, make sure it fits in space
        if (isCropHeight)
            childH = Math.min(childH, areaH);

        // Get content alignment as modifer/factor (0 = left, 1 = right)
        double alignX = child.getLeanX() != null ? child.getLeanXAsDouble() : getAlignXAsDouble();
        double alignY = child.getLeanY() != null ? child.getLeanYAsDouble() : getAlignYAsDouble();

        // Calc X/Y and set bounds
        double childX = areaX + Math.round((areaW - childW) * alignX);
        double childY = areaY + Math.round((areaH - childH) * alignY);
        child.setBounds(childX, childY, childW, childH);
    }
}