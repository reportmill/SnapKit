/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;

/**
 * A ViewProxy that can layout content in the manner of BoxView for any View.
 */
public class BoxViewProxy<T extends View> extends ParentViewProxy<T> {

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
     * Returns preferred width of layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        double prefW = getChildrenMaxXLastWithInsets();
        return prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        double prefH = getChildrenMaxYLastWithInsets();
        return prefH;
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    @Override
    public void layoutProxy()
    {
        // Get parent info
        double viewW = getWidth();
        double viewH = getHeight();
        boolean isFillWidth = isFillWidth();
        boolean isFillHeight = isFillHeight();

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
        double childW = areaW;
        boolean fitChildW = viewW >= 0 && (isFillWidth || child.isGrowWidth());
        if (!fitChildW)
            childW = child.getBestWidth(-1);

        // Get content height
        double childH = areaH;
        boolean fitChildH = viewH >= 0 && (isFillHeight || child.isGrowHeight());
        if (!fitChildH) {
            double fitW = fitChildW ? childW : -1;
            childH = child.getBestHeight(fitW);
        }

        // If Parent.Width -1, just return (laying out for PrefWidth/PrefHeight)
        if (viewW < 0 || viewH < 0) {
            child.setBounds(areaX, areaY, childW, childH);
            return;
        }

        // If child needs crop, make sure it fits in space
        childW = Math.min(childW, areaW);
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