/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Size;

/**
 * A Box subclass that scales it's content instead of resize.
 * FillWidth, FillHeight, KeepAspect, FillAlways default to true.
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
    public ScaleBox(View aContent)  { this(); setContent(aContent); }

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
        if (aValue==isKeepAspect()) return;
        firePropChange(KeepAspect_Prop, _keepAspect, _keepAspect = aValue);
        relayout();
    }

    /**
     * Returns the aspect of the content.
     */
    protected double getAspect()
    {
        View child = getContent(); if (child==null) return 1;
        Size bestSize = child.getBestSize();
        return bestSize.width / bestSize.height;
    }

    /**
     * Returns preferred width of layout.
     */
    protected double getPrefWidthImpl(double aH)
    {
        // If scaling and value provided, return value by aspect
        if (aH >= 0) {
            if (isFillHeight() || aH < getPrefHeight(-1))
                return Math.ceil(aH * getAspect());
        }

        // Do normal version
        return super.getPrefWidthImpl(aH);
    }

    /**
     * Returns preferred height of layout.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // If scaling and value provided, return value by aspect
        if (aW >= 0) {
            if (isFillWidth() || aW < getPrefWidth(-1))
                return Math.ceil(aW / getAspect());
        }

        // Do normal version
        return super.getPrefHeightImpl(aW);
    }

    /**
     * Performs layout.
     */
    protected void layoutImpl()
    {
        layout(this, getContent(), null, isFillWidth(), isFillHeight());
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layout(ParentView aPar, View aChild, Insets theIns, boolean isFillWidth, boolean isFillHeight)
    {
        // If no child, just return
        if (aChild==null) return;

        // Get area bounds
        Insets ins = theIns!=null ? theIns : aPar.getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = aPar.getWidth() - ins.getWidth();
        double areaH = aPar.getHeight() - ins.getHeight();

        // If area is empty, just return
        if (areaW<=0 || areaH<=0)
            return;

        // Get content width/height
        double childW = aChild.getBestWidth(-1);
        double childH = aChild.getBestHeight(-1);

        // Get content alignment as modifer/factor (0 = left, 1 = right)
        double alignX = aChild.getLeanX() != null ? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
        double alignY = aChild.getLeanY() != null ? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);

        // Handle ScaleToFit: Set content bounds centered, calculate scale and set
        if (isFillWidth || isFillHeight || childW > areaW || childH > areaH)  {

            // Get/set Child bounds, and calculate scale X/Y to fit
            double scaleX = isFillWidth || childW > areaW ? areaW/childW : 1;
            double scaleY = isFillHeight || childH > areaH ? areaH/childH : 1;

            // If KeepAspect (or both FillWidth/FillHeight), constrain scale X/Y to min
            boolean isKeepAspect = aPar instanceof ScaleBox && ((ScaleBox)aPar).isKeepAspect();
            if (isKeepAspect || isFillWidth && isFillHeight)
                scaleX = scaleY = Math.min(scaleX,scaleY); // KeepAspect?

            // Set child scale
            aChild.setScaleX(scaleX);
            aChild.setScaleY(scaleY);

            // Get/set child bounds
            double childX = Math.round(areaX + (areaW - childW * scaleX) * alignX + childW/2 * scaleX - childW/2);
            double childY = Math.round(areaY + (areaH - childH * scaleY) * alignY + childH/2 * scaleY - childH/2);
            aChild.setBounds(childX, childY, childW, childH);
            return;
        }

        // Handle normal layout
        double childX = Math.round(areaX + (areaW - childW) * alignX);
        double childY = Math.round(areaY + (areaH - childH) * alignY);
        aChild.setBounds(childX, childY, childW, childH);
    }
}