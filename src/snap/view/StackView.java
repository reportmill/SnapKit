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
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }

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
        View children[] = aPar.getChildrenManaged(); if (children.length==0) return;

        // Get parent bounds for insets
        Insets ins = aPar.getInsetsAll();
        double px = ins.left, pw = aPar.getWidth() - ins.getWidth(); if(pw<0) pw = 0;
        double py = ins.top, ph = aPar.getHeight() - ins.getHeight(); if(ph<0) ph = 0;

        // Get child bounds
        double ax = ViewUtils.getAlignX(aPar);
        double ay = ViewUtils.getAlignY(aPar);

        // Layout children
        for (View child : children) {

            // Get child margin
            Insets marg = child.getMargin();

            // Get child width
            double maxW = Math.max(pw - marg.getWidth(), 0);
            double cw = child.isGrowWidth() ? maxW : Math.min(child.getBestWidth(-1), maxW);

            // Calc x accounting for margin and alignment
            double cx = px + marg.left;
            if (cw<maxW) {
                double ax2 = Math.max(ax,ViewUtils.getLeanX(child));
                cx = Math.max(cx, px + Math.round((pw-cw)*ax2));
            }

            // Get child height
            double maxH = Math.max(ph - marg.getHeight(), 0);
            double ch = child.isGrowHeight() ? maxH : Math.min(child.getBestHeight(-1), maxH);

            // Calc y accounting for margin and alignment
            double cy = py + marg.top;
            if (ch<maxH) {
                double ay2 = Math.max(ay,ViewUtils.getLeanY(child));
                cy = Math.max(cy, py + Math.round((ph-ch)*ay2));
            }

            // Set child bounds
            child.setBounds(cx, cy, cw, ch);
        }
    }
}