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
        return getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return getPrefHeight(this, aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        layout(this, isFillHeight());
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

    /**
     * Returns preferred width of given parent using RowView layout.
     */
    public static double getPrefWidth(View aPar, double aH)
    {
        ViewProxy par = ViewProxy.getProxy(aPar);
        return getPrefWidthProxy(par, aH);
    }

    /**
     * Returns preferred height of given parent using RowView layout.
     */
    public static double getPrefHeight(View aPar, double aW)
    {
        ViewProxy par = ViewProxy.getProxy(aPar);
        return getPrefHeightProxy(par, aW);
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layout(ParentView aPar, boolean isFillHeight)
    {
        // If no children, just return
        if (aPar.getChildrenManaged().length==0) return;

        // Get Parent ViewProxy with Children proxies
        ViewProxy par = ViewProxy.getProxy(aPar);

        // Do Proxy layout
        layoutProxy(par, isFillHeight);

        // Push layout bounds back to real views
        par.setBoundsInClient();
    }

    /**
     * Returns preferred width of given parent proxy using RowView layout.
     */
    public static double getPrefWidthProxy(ViewProxy aPar, double aH)
    {
        aPar.setSize(-1, aH);
        layoutProxy(aPar, false);
        return aPar.getChildrenMaxXLastWithInsets();
    }

    /**
     * Returns preferred height of given parent proxy using RowView layout.
     */
    public static double getPrefHeightProxy(ViewProxy aPar, double aW)
    {
        aPar.setSize(aW, -1);
        layoutProxy(aPar, false);
        return aPar.getChildrenMaxYAllWithInsets();
    }

    /**
     * Performs layout for given ViewProxy.
     */
    public static void layoutProxy(ViewProxy aPar, boolean isFillHeight)
    {
        // If no children, just return
        if (aPar.getChildCount()==0) return;

        // Load layout rects and return
        layoutProxyX(aPar);
        layoutProxyY(aPar, isFillHeight);
    }

    /**
     * Calculates RowView layout X & Width for given Parent proxy.
     */
    private static void layoutProxyX(ViewProxy aPar)
    {
        // Get parent info
        ViewProxy children[] = aPar.getChildren();
        double spacing = aPar.getSpacing();

        // Get area bounds
        double viewW = aPar.getWidth();
        Insets ins = aPar.getInsetsAll();
        double areaX = ins.left;
        double areaW = Math.max(viewW - ins.getWidth(), 0);

        // Loop vars
        double childX = 0;
        ViewProxy lastChild = null;

        // Iterate over children to calculate bounds X and Width
        for (ViewProxy child : children) {

            // Get child width
            double childW = child.getBestWidth(-1);

            // Update child x: advance for max of spacing and margins
            double lastMargin = lastChild!=null ? lastChild.getMargin().getRight() : ins.getLeft();
            double loopMargin = child.getMargin().getLeft();
            double maxMargin = Math.max(lastMargin, loopMargin);
            double childSpacing = lastChild!=null ? Math.max(spacing, maxMargin) : maxMargin;
            childX += childSpacing;

            // Set child bounds X and Width
            child.setX(childX);
            child.setWidth(childW);

            // Update child x loop var and last child
            childX += childW;
            lastChild = child;
        }

        // If Parent.Width -1, just return (laying out for PrefWidth)
        if (viewW<0)
            return;

        // Add margin for last child, calculate extra space and add to growers or alignment
        double childMaxX = childX + children[children.length-1].getMargin().right;
        int extraX = (int) Math.round(areaX + areaW - childMaxX);
        if (extraX!=0)
            addExtraSpaceX(aPar, extraX);
    }

    /**
     * Calculates RowView layout Y & Height for given Parent proxy.
     */
    private static void layoutProxyY(ViewProxy aPar, boolean isFillHeight)
    {
        // Get parent info
        ViewProxy children[] = aPar.getChildren();
        double alignY = aPar.getAlignYAsDouble();

        // Get area bounds
        double viewH = aPar.getHeight();
        Insets ins = aPar.getInsetsAll();
        double areaY = ins.top;
        double areaH = Math.max(viewH - ins.getHeight(), 0);

        // Iterate over children to calculate/set child Y & Height
        for (ViewProxy child : children) {

            // Calc Y accounting for margin and alignment
            Insets childMarg = child.getMargin();
            double childY = areaY + childMarg.getTop();
            double childH;

            // If Parent.Height not set, set height to Child.PrefHeight
            if (viewH<0) {
                double cw = child.getWidth();
                childH = child.getBestHeight(cw);
            }

            // Otherwise, if Parent.FillHeight or Child.GrowHeight, set to max height
            else if (isFillHeight || child.isGrowHeight()) {
                childH = Math.max(areaH - childMarg.getHeight(), 0);
            }

            // Otherwise, set height to Child.PrefHeight and adjust Y
            else {
                double childMaxH = Math.max(areaH - childMarg.getHeight(), 0);
                double childW = child.getWidth();
                childH = child.getBestHeight(childW);
                childH = Math.min(childH, childMaxH);

                // Calc y accounting for margin and alignment
                if (childH < childMaxH) {
                    double alignY2 = Math.max(alignY, child.getLeanYAsDouble());
                    double shiftY = Math.round((areaH - childH) * alignY2);
                    childY = Math.max(childY, areaY + shiftY);
                }
            }

            // Set child rect Y and Height
            child.setY(childY);
            child.setHeight(childH);
        }
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceX(ViewProxy aPar, int extra)
    {
        // If grow shapes, add grow
        if (aPar.getGrowWidthCount()>0)
            addExtraSpaceX_ToGrowers(aPar, extra);

        // Otherwise, if FillWidth, extend last child
        //else if (fillWidth) { ViewProxy ch = children[children.length - 1]; ch.setWidth(ch.getWidth() + extra); }

        // Otherwise, check for horizontal alignment/lean shift
        else if (extra>0)
            addExtraSpaceX_ToAlign(aPar, extra);
    }

    /**
     * Adds extra space X to children that GrowWidth.
     */
    private static void addExtraSpaceX_ToGrowers(ViewProxy aPar, int extra)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int grow = aPar.getGrowWidthCount();
        int each = extra/grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra%grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        ViewProxy children[] = aPar.getChildren();
        for (int i=0, j=0, shiftX = 0, iMax=children.length; i<iMax; i++) {
            ViewProxy child = children[i];
            if (shiftX!=0)
                child.setX(child.getX() + shiftX);
            if (child.isGrowWidth()) {
                int each3 = j<count2? eachP1 : each;
                child.setWidth(child.getWidth() + each3);
                shiftX += each3; j++;
            }
        }
    }

    /**
     * Adds extra space X to child alignment/lean.
     */
    private static void addExtraSpaceX_ToAlign(ViewProxy par, double extra)
    {
        ViewProxy children[] = par.getChildren();
        double alignX = par.getAlignXAsDouble();
        for (ViewProxy child : children) {
            alignX = Math.max(alignX, child.getLeanXAsDouble());
            double shiftX = extra * alignX;
            if (shiftX>0)
                child.setX(child.getX() + extra*alignX);
        }
    }
}