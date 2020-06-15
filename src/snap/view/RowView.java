/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.*;

/**
 * A View subclass to layout child views horizontally, from left to right.
 */
public class RowView extends ChildView {
    
    // The spacing between nodes
    private double  _spacing;
    
    // Whether to fill to height
    private boolean  _fillHeight;
    
    // Constants for properties
    public static final String FillHeight_Prop = "FillHeight";
    
    /**
     * Returns the spacing.
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Sets the spacing.
     */
    public void setSpacing(double aValue)
    {
        if (aValue==_spacing) return;
        firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
        relayout(); relayoutParent();
    }

    /**
     * Returns whether children will be resized to fill height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether children will be resized to fill height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue==_fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        relayout();
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return getPrefWidth(this, null, getSpacing(), aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return getPrefHeight(this, null, aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        layout(this, null, null, isFillHeight(), getSpacing());
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Spacing, FillHeight
        if (getSpacing()!=0) e.add("Spacing", getSpacing());
        if (isFillHeight()) e.add("FillHeight", true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Spacing, FillHeight
        setSpacing(anElement.getAttributeFloatValue("Spacing", 0));
        setFillHeight(anElement.getAttributeBoolValue("FillHeight", false));
    }

    // Whether testing new layout code
    private static boolean TEST_NEW_LAYOUT_CODE = false;

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, View theChildren[], double aSpacing, double aH)
    {
        if (TEST_NEW_LAYOUT_CODE)
            return getPrefWidth2(aPar, theChildren, aSpacing, aH);

        // Get insets and children (just return if empty)
        Insets ins = aPar.getInsetsAll();
        View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
        int ccount = children.length; if (ccount==0) return ins.getWidth();

        // Iterate over children and add spacing, margin-left (collapsable) and child width
        double pw = 0, spc = 0;
        for (View child : children) {
            Insets marg = child.getMargin();
            double cbw = child.getBestWidth(-1);
            pw += Math.max(spc, marg.left) + cbw;
            spc = Math.max(aSpacing, marg.right);
        }

        // Add margin for last child
        pw += children[ccount-1].getMargin().right;

        // Return preferred width + inset width (rounded)
        double pw2 = pw + ins.getWidth(); pw2 = Math.round(pw2);
        return pw2;
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    public static double getPrefHeight(ParentView aPar, View theChildren[], double aW)
    {
        if (TEST_NEW_LAYOUT_CODE)
            return getPrefHeight2(aPar, theChildren, aW);

        // Get insets and children (just return if empty)
        Insets ins = aPar.getInsetsAll();
        View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
        if (children.length==0) return ins.getHeight();

        // Get max best height of children (including margins)
        double ph = 0;
        for (View child : children) {
            double marH = child.getMargin().getHeight();
            double cbh = child.getBestHeight(-1);
            ph = Math.max(ph, cbh + marH);
        }

        // Return preferred height + inset height (rounded is best)
        double ph2 = ph + ins.getHeight(); ph2 = Math.round(ph2);
        return ph2;
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layout(ParentView aPar, View theChilds[], Insets theIns, boolean isFillHeight, double aSpacing)
    {
        // Get layout children (just return if none)
        View children[] = theChilds!=null? theChilds : aPar.getChildrenManaged(); if (children.length==0) return;

        // Get Parent ViewProxy with Children proxies
        ViewProxy par = ViewProxy.getProxyForParentAndChildren(aPar, children);
        if (theIns!=null) par.setInsets(theIns);
        par.setSpacing(aSpacing);

        // Do Proxy layout
        if (TEST_NEW_LAYOUT_CODE)
            layoutProxy2(par, false, isFillHeight);
        else layoutProxy(par, false, isFillHeight);

        // Push layout bounds back to real
        par.setBoundsInClient();
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layoutProxy(ViewProxy aPar, boolean isFillWidth, boolean isFillHeight)
    {
        // Get children (just return if empty)
        ViewProxy children[] = aPar.getChildren(); if (children.length==0) return;

        // Get parent bounds for insets
        Insets ins = aPar.getInsetsAll();
        double spacing = aPar.getSpacing();
        double px = ins.left, pw = aPar.getWidth() - ins.getWidth(); if (pw<0) pw = 0;
        double py = ins.top, ph = aPar.getHeight() - ins.getHeight(); if (ph<0) ph = 0;

        // Get child bounds
        double ay = aPar.getAlignYAsDouble();
        int grow = 0;

        // Layout children
        double cx = px, spc = 0;
        for (ViewProxy child : children) {

            // Get child margin
            Insets marg = child.getMargin();
            double marL = marg.left, marR = marg.right;

            // Get child height
            double maxH = Math.max(ph - marg.getHeight(), 0);
            double ch = isFillHeight || child.isGrowHeight()? maxH : Math.min(child.getBestHeight(-1), maxH);

            // Calc y accounting for margin and alignment
            double cy = py + marg.getTop();
            if (ch<maxH) {
                double ay2 = Math.max(ay, child.getLeanYAsDouble());
                cy = Math.max(cy, py + Math.round((ph-ch)*ay2));
            }

            // Get child width and update child x for spacing/margin-left
            double cw = child.getBestWidth(ch);
            cx += Math.max(spc, marL);

            // Set child bounds
            child.setBounds(cx, cy, cw, ch);

            // Update spacing, current child x and grow count
            spc = Math.max(spacing, marR); cx += cw;
            if (child.isGrowWidth()) grow++;
        }

        // Add margin for last child, calculate extra space and add to growers or alignment
        cx += children[children.length-1].getMargin().right;
        int extra = (int)Math.round(px + pw - cx);
        if (extra!=0)
            addExtraSpace(aPar, extra, grow, isFillWidth);
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpace(ViewProxy par, int extra, int grow, boolean fillW)
    {
        ViewProxy children[] = par.getChildren();

        // If grow shapes, add grow
        if (grow>0)
            addExtraSpaceToGrowers(children, extra, grow);

        // Otherwise, if FillWidth, extend last child
        else if (fillW) {
            ViewProxy child = children[children.length - 1];
            child.setWidth(child.getWidth() + extra);
        }

        // Otherwise, check for horizontal alignment/lean shift
        else if (extra>0)
            addExtraSpaceToAlign(par, extra);
    }

    /**
     * Adds extra space to growers.
     */
    private static void addExtraSpaceToGrowers(ViewProxy children[], int extra, int grow)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int each = extra/grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra%grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        for (int i=0, j=0, dx = 0,iMax=children.length; i<iMax; i++) {
            ViewProxy child = children[i];
            if (dx!=0)
                child.setX(child.getX() + dx);
            if (child.isGrowWidth()) {
                int each3 = j<count2? eachP1 : each;
                child.setWidth(child.getWidth() + each3);
                dx += each3; j++;
            }
        }
    }

    /**
     * Adds extra space to alignment.
     */
    private static void addExtraSpaceToAlign(ViewProxy par, double extra)
    {
        ViewProxy children[] = par.getChildren();
        double ax = par.getAlignXAsDouble();
        for (ViewProxy child : children) {
            ax = Math.max(ax, child.getLeanXAsDouble());
            double dx = extra*ax;
            if (dx>0)
                child.setX(child.getX() + extra*ax);
        }
    }

    /**
     * Returns preferred width of given parent using RowView layout.
     */
    public static double getPrefWidth2(View aPar, View theChildren[], double aSpacing, double aH)
    {
        // Get parent as proxy
        ViewProxy par = ViewProxy.getProxyForParentAndChildren(aPar, theChildren);
        par.setSize(-1, aH);
        par.setSpacing(aSpacing);

        // Layout proxy children and return last child max x with insets
        layoutProxy2(par, false, false);
        return par.getChildrenMaxXLastWithInsets();
    }

    /**
     * Returns preferred height of given parent using RowView layout.
     */
    public static double getPrefHeight2(View aPar, View theChildren[], double aW)
    {
        // Get parent as proxy
        ViewProxy par = ViewProxy.getProxyForParentAndChildren(aPar, theChildren);
        par.setSize(aW, -1);

        // Layout proxy children and return children max y with insets
        layoutProxy2(par, false, false);
        return par.getChildrenMaxYAllWithInsets();
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layoutProxy2(ViewProxy aPar, boolean isFillWidth, boolean isFillHeight)
    {
        // Get children (just return if empty) and create rects
        ViewProxy children[] = aPar.getChildren(); if (children.length==0) return;

        // Load layout rects and return
        layoutProxyX(aPar, isFillWidth);
        layoutProxyY(aPar, isFillHeight);
    }

    /**
     * Calculates RowView layout bounds (X & Width) for given Parent and sets in given rects.
     */
    private static void layoutProxyX(ViewProxy aPar, boolean isFillWidth)
    {
        // Get layout info and loop vars
        ViewProxy children[] = aPar.getChildren();
        Insets ins = aPar.getInsetsAll();
        double spacing = aPar.getSpacing();
        double px = ins.left;
        double cx = px;
        double spc = 0;
        int growersCount = 0;

        // Iterate over children to calculate bounds X and Width
        for (ViewProxy child : children) {

            // Get child width and update child x for spacing/margin-left
            Insets marg = child.getMargin();
            double cw = child.getBestWidth(-1);
            cx += Math.max(spc, marg.left);

            // Set child bounds X and Width
            child.setX(cx);
            child.setWidth(cw);

            // Update spacing, current child x and grow count
            spc = Math.max(spacing, marg.right);
            cx += cw;
            if (child.isGrowWidth()) growersCount++;
        }

        // If Parent.Width -1, just return rects
        double pw = aPar.getWidth();
        if (pw<0)
            return;
        pw = Math.max(pw - ins.getWidth(), 0);

        // Add margin for last child, calculate extra space and add to growers or alignment
        cx += children[children.length-1].getMargin().right;
        int extra = (int)Math.round(px + pw - cx);
        if (extra!=0)
            addExtraSpace(aPar, extra, growersCount, isFillWidth);
    }

    /**
     * Calculates RowView layout bounds (Y & Height) for given Parent and sets in given rects.
     */
    private static void layoutProxyY(ViewProxy aPar, boolean isFillHeight)
    {
        // Get layout info and loop vars
        ViewProxy children[] = aPar.getChildren();
        Insets ins = aPar.getInsetsAll();
        double ay = aPar.getAlignYAsDouble();
        double py = ins.top;
        double ph = aPar.getHeight(); if (ph>=0) ph = Math.max(ph - ins.getHeight(), 0);

        // Iterate over children to calculate bounds rects
        for (ViewProxy child : children) {

            // Calc y accounting for margin and alignment
            Insets marg = child.getMargin();
            double maxH = Math.max(ph - marg.getHeight(), 0);
            double cw = child.getWidth();
            double cy = py + marg.getTop();
            double ch;

            // If Parent.Height not set, set height to Child.PrefHeight
            if (ph<0) {
                ch = child.getBestHeight(cw); }

            // Otherwise, if Parent.FillHeight or Child.GrowHeight, set to max height
            else if (isFillHeight || child.isGrowHeight()) {
                ch = maxH; }

            // Otherwise, set height to Child.PrefHeight and adjust Y
            else {
                ch = child.getBestHeight(cw);
                ch = Math.min(ch, maxH);

                // Calc y accounting for margin and alignment
                if (ch < maxH) {
                    double ay2 = Math.max(ay, child.getLeanYAsDouble());
                    double dy = Math.round((ph - ch) * ay2);
                    cy = Math.max(cy, py + dy);
                }
            }

            // Set child rect Y and Height
            child.setY(cy);
            child.setHeight(ch);
        }
    }
}