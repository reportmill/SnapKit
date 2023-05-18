/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;

/**
 * A ChildView subclass to show overlapping children.
 */
public class StackView extends ChildView {

    /**
     * Constructor.
     */
    public StackView()
    {
        super();
        _align = Pos.CENTER;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return getPrefWidth(this, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, aW); }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { layout(this); }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, double aH)
    {
        return ColView.getPrefWidth(aPar, aH);
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    public static double getPrefHeight(ParentView aPar, double aW)
    {
        return RowView.getPrefHeight(aPar, aW);
    }

    /**
     * Performs layout in content rect.
     */
    public static void layout(ParentView aPar)
    {
        // Get children (just return if empty)
        View[] children = aPar.getChildrenManaged(); if (children.length == 0) return;

        // Get parent bounds for insets
        Insets ins = aPar.getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = aPar.getWidth() - ins.getWidth(); if(areaW < 0) areaW = 0;
        double areaH = aPar.getHeight() - ins.getHeight(); if(areaH < 0) areaH = 0;

        // Get child bounds
        double alignX = ViewUtils.getAlignX(aPar);
        double alignY = ViewUtils.getAlignY(aPar);

        // Layout children
        for (View child : children) {

            // Get child margin
            Insets marg = child.getMargin();

            // Get child width
            double maxW = Math.max(areaW - marg.getWidth(), 0);
            double childW = child.isGrowWidth() ? maxW : Math.min(child.getBestWidth(-1), maxW);

            // Calc x accounting for margin and alignment
            double childX = areaX + marg.left;
            if (childW < maxW) {
                double alignX2 = Math.max(alignX, ViewUtils.getLeanX(child));
                childX = Math.max(childX, areaX + Math.round((areaW - childW) * alignX2));
            }

            // Get child height
            double maxH = Math.max(areaH - marg.getHeight(), 0);
            double childH = child.isGrowHeight() ? maxH : Math.min(child.getBestHeight(-1), maxH);

            // Calc y accounting for margin and alignment
            double childY = areaY + marg.top;
            if (childH < maxH) {
                double alignY2 = Math.max(alignY, ViewUtils.getLeanY(child));
                childY = Math.max(childY, areaY + Math.round((areaH - childH) * alignY2));
            }

            // Set child bounds
            child.setBounds(childX, childY, childW, childH);
        }
    }
}