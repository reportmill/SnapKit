/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Size;
import snap.gfx.Border;

/**
 * A BoxView subclass that scales the content instead of resize.
 */
public class ScaleBox extends BoxView {

    // Whether to preserve natural aspect when scaling
    private boolean  _keepAspect;

    // Constants for properties
    public static final String KeepAspect_Prop = "KeepAspect";

    /**
     * Creates a new ScaleBox.
     */
    public ScaleBox()  { }

    /**
     * Creates a new ScaleBox for content.
     */
    public ScaleBox(View aContent)
    {
        this();
        setContent(aContent);
    }

    /**
     * Creates a new ScaleBox for content with FillWidth, FillHeight params.
     */
    public ScaleBox(View aContent, boolean isFillWidth, boolean isFillHeight)
    {
        this(aContent);
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
        if (aValue == isKeepAspect()) return;
        firePropChange(KeepAspect_Prop, _keepAspect, _keepAspect = aValue);
        relayout();
    }

    /**
     * Returns the aspect of the content.
     */
    protected double getAspect()
    {
        View child = getContent(); if (child == null) return 1;
        Size bestSize = child.getBestSize();
        return bestSize.width / bestSize.height;
    }

    /**
     * Returns preferred width of layout.
     */
    @Override
    protected double computePrefWidth(double aH)
    {
        // If scaling and value provided, return value by aspect
        if (aH > 0) {
            if (isFillHeight() || aH < getPrefHeight(-1))
                return Math.ceil(aH * getAspect());
        }

        // Do normal version
        return super.computePrefWidth(aH);
    }

    /**
     * Returns preferred height of layout.
     */
    @Override
    protected double computePrefHeight(double aW)
    {
        // If scaling and value provided, return value by aspect
        if (aW > 0) {
            if (isFillWidth() || aW < getPrefWidth(-1))
                return Math.ceil(aW / getAspect());
        }

        // Do normal version
        return super.computePrefHeight(aW);
    }

    /**
     * Override to layout box and set scale.
     */
    @Override
    protected void layoutImpl()
    {
        // Get child (if null, just return)
        View child = getContent(); if (child == null) return;

        // Get layout info
        double viewW = getWidth();
        double viewH = getHeight();
        boolean isFillWidth = isFillWidth();
        boolean isFillHeight = isFillHeight();
        boolean isKeepAspect = isKeepAspect();

        // Get parent bounds for insets (just return if empty)
        Border border = getBorder();
        Insets borderInsets = border != null ? border.getInsets() : Insets.EMPTY;
        Insets padding = getPadding();
        Insets childMargin = child.getMargin();
        double areaX = borderInsets.left + Math.max(padding.left, childMargin.left);
        double areaY = borderInsets.top + Math.max(padding.top, childMargin.top);
        double areaW = Math.max(viewW - borderInsets.right - Math.max(padding.right, childMargin.right) - areaX, 0);
        double areaH = Math.max(viewH - borderInsets.bottom - Math.max(padding.bottom, childMargin.bottom) - areaY, 0);

        // Get content width
        double childW = child.getBestWidth(-1);
        double childH = child.getBestHeight(-1);

        // Get content alignment as modifier/factor (0 = left, 1 = right)
        double alignX = child.getLeanX() != null ? ViewUtils.getLeanX(child) : ViewUtils.getAlignX(this);
        double alignY = child.getLeanY() != null ? ViewUtils.getLeanY(child) : ViewUtils.getAlignY(this);

        // Handle ScaleToFit: Set content bounds centered, calculate scale and set
        if (isFillWidth || isFillHeight || childW > areaW || childH > areaH)  {

            // Get/set Child bounds, and calculate scale X/Y to fit
            double scaleX = isFillWidth || childW > areaW ? areaW/childW : 1;
            double scaleY = isFillHeight || childH > areaH ? areaH/childH : 1;

            // If KeepAspect (or both FillWidth/FillHeight), constrain scale X/Y to min
            if (isKeepAspect || (isFillWidth && isFillHeight))
                scaleX = scaleY = Math.min(scaleX,scaleY); // KeepAspect?

            // Set child scale
            child.setScaleX(scaleX);
            child.setScaleY(scaleY);

            // Get/set child bounds
            double childX = Math.round(areaX + (areaW - childW * scaleX) * alignX + childW/2 * scaleX - childW/2);
            double childY = Math.round(areaY + (areaH - childH * scaleY) * alignY + childH/2 * scaleY - childH/2);
            child.setBounds(childX, childY, childW, childH);
            return;
        }

        // Make sure scale is reset
        child.setScale(1);

        // Handle normal layout
        double childX = Math.round(areaX + (areaW - childW) * alignX);
        double childY = Math.round(areaY + (areaH - childH) * alignY);
        child.setBounds(childX, childY, childW, childH);
    }
}