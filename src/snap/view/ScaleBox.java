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
     * Returns the aspect of the content.
     */
    protected double getAspect()
    {
        View child = getContent(); if (child==null) return 1;
        Size bestSize = child.getBestSize();
        return child.isHorizontal() ? bestSize.width/bestSize.height : bestSize.height/bestSize.width;
    }

    /**
     * Returns preferred width of layout.
     */
    protected double getPrefWidthImpl(double aH)
    {
        // If scaling and value provided, return value by aspect
        if (aH>=0 && (isFillHeight() || aH<getPrefHeight(-1)))
            return aH*getAspect();
        return super.getPrefWidthImpl(aH);
    }

    /**
     * Returns preferred height of layout.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // If scaling and value provided, return value by aspect
        if (aW>=0 && (isFillWidth() || aW<getPrefWidth(-1)))
            return aW/getAspect();
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

        // Get parent bounds for insets (just return if empty)
        Insets ins = theIns!=null ? theIns : aPar.getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = aPar.getWidth() - areaX - ins.right; if (areaW<0) areaW = 0; if (areaW<=0) return;
        double areaH = aPar.getHeight() - areaY - ins.bottom; if (areaH<0) areaH = 0; if (areaH<=0) return;

        // Get content width/height
        double childW = aChild.getBestWidth(-1);
        double childH = aChild.getBestHeight(childW);

        // Handle ScaleToFit: Set content bounds centered, calculate scale and set
        if (isFillWidth || isFillHeight || childW>areaW || childH>areaH)  {
            double childX = areaX + (areaW-childW)/2;
            double childY = areaY + (areaH-childH)/2;
            aChild.setBounds(childX, childY, childW, childH);
            double scaleX = isFillWidth || childW>areaW ? areaW/childW : 1;
            double scaleY = isFillHeight || childH>areaH ? areaH/childH : 1;
            if (isFillWidth && isFillHeight)
                scaleX = scaleY = Math.min(scaleX,scaleY); // KeepAspect?
            aChild.setScaleX(scaleX);
            aChild.setScaleY(scaleY);
            return;
        }

        // Handle normal layout
        double dx = areaW - childW;
        double dy = areaH - childH;
        double sx = aChild.getLeanX()!=null ? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
        double sy = aChild.getLeanY()!=null ? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);
        aChild.setBounds(areaX+dx*sx, areaY+dy*sy, childW, childH);
    }
}