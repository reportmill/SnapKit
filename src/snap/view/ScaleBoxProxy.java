/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;

/**
 * A ViewProxy that can layout content in the manner of ScaleBox for any View.
 */
public class ScaleBoxProxy extends BoxViewProxy<View> {

    // Whether to preserve natural aspect when scaling
    private boolean  _keepAspect;

    /**
     * Constructor for given parent view.
     */
    public ScaleBoxProxy(View aParent)
    {
        super(aParent);

        if (aParent instanceof ScaleBox) {
            ScaleBox scaleBox = (ScaleBox) aParent;
            setKeepAspect(scaleBox.isKeepAspect());
        }
    }

    /**
     * Creates a new ScaleBox for content with FillWidth, FillHeight params.
     */
    public ScaleBoxProxy(View aParent, View aChild, boolean isFillWidth, boolean isFillHeight)
    {
        this(aParent);
        setFillWidth(isFillWidth);
        setFillHeight(isFillHeight);
    }

    /**
     * Returns whether to preserve natural aspect of content when scaling.
     */
    public boolean isKeepAspect()  { return _keepAspect; }

    /**
     * Sets whether to preserve natural aspect of content when scaling.
     */
    public void setKeepAspect(boolean aValue)
    {
        _keepAspect = aValue;
    }

    /**
     * Returns the aspect of the content.
     */
    public double getAspect()
    {
        ViewProxy<?> child = getContent(); if (child == null) return 1;
        double bestW = child.getBestWidth(-1);
        double bestH = child.getBestHeight(-1);
        return bestW / bestH;
    }

    /**
     * Returns preferred width of layout.
     */
    @Override
    public double getPrefWidth(double aH)
    {
        // If scaling and value provided, return value by aspect
        if (aH >= 0) {
            if (isFillHeight() || aH < getPrefHeight(-1))
                return Math.ceil(aH * getAspect());
        }

        // Do normal version
        return super.getPrefWidth(aH);
    }

    /**
     * Returns preferred height of layout.
     */
    @Override
    public double getPrefHeight(double aW)
    {
        // If scaling and value provided, return value by aspect
        if (aW >= 0) {
            if (isFillWidth() || aW < getPrefWidth(-1))
                return Math.ceil(aW / getAspect());
        }

        // Do normal version
        return super.getPrefHeight(aW);
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public void layoutProxy()
    {
        // Get child (if null, just return)
        ViewProxy<?> child = getContent(); if (child == null) return;

        // Get layout info
        double viewW = getWidth();
        double viewH = getHeight();
        boolean isFillWidth = isFillWidth();
        boolean isFillHeight = isFillHeight();
        boolean isKeepAspect = isKeepAspect();

        // Get parent bounds for insets (just return if empty)
        Insets borderInsets = getBorderInsets();
        Insets pad = getPadding();
        Insets marg = child.getMargin();
        double areaX = borderInsets.left + Math.max(pad.left, marg.left);
        double areaY = borderInsets.top + Math.max(pad.top, marg.top);
        double areaW = Math.max(viewW - borderInsets.right - Math.max(pad.right, marg.right) - areaX, 0);
        double areaH = Math.max(viewH - borderInsets.bottom - Math.max(pad.bottom, marg.bottom) - areaY, 0);

        // Get content width
        double childW = child.getBestWidth(-1);
        double childH = child.getBestHeight(-1);

        // If Parent.Width -1, just return (laying out for PrefWidth/PrefHeight)
        if (viewW < 0 || viewH < 0) {
            child.setBounds(areaX, areaY, childW, childH);
            return;
        }

        // Get content alignment as modifier/factor (0 = left, 1 = right)
        double alignX = child.getLeanX() != null ? child.getLeanXAsDouble() : getAlignXAsDouble();
        double alignY = child.getLeanY() != null ? child.getLeanYAsDouble() : getAlignYAsDouble();

        // Handle ScaleToFit: Set content bounds centered, calculate scale and set
        if (isFillWidth || isFillHeight || childW > areaW || childH > areaH)  {

            // Get/set Child bounds, and calculate scale X/Y to fit
            double scaleX = isFillWidth || childW > areaW ? areaW/childW : 1;
            double scaleY = isFillHeight || childH > areaH ? areaH/childH : 1;

            // If KeepAspect (or both FillWidth/FillHeight), constrain scale X/Y to min
            if (isKeepAspect || (isFillWidth && isFillHeight))
                scaleX = scaleY = Math.min(scaleX,scaleY); // KeepAspect?

            // Set child scale
            View childView = child.getView();
            childView.setScaleX(scaleX);
            childView.setScaleY(scaleY);

            // Get/set child bounds
            double childX = Math.round(areaX + (areaW - childW * scaleX) * alignX + childW/2 * scaleX - childW/2);
            double childY = Math.round(areaY + (areaH - childH * scaleY) * alignY + childH/2 * scaleY - childH/2);
            child.setBounds(childX, childY, childW, childH);
            return;
        }

        // Handle normal layout
        double childX = Math.round(areaX + (areaW - childW) * alignX);
        double childY = Math.round(areaY + (areaH - childH) * alignY);
        child.setBounds(childX, childY, childW, childH);
    }
}