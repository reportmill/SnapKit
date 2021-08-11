/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.*;

/**
 * A View subclass to layout child views vertically, from top to bottom.
 */
public class ColView extends ChildView {

    // Whether to fill to with
    private boolean  _fillWidth;
    
    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";

    // Constants for property defaults
    private static final boolean DEFAULT_COL_VIEW_VERTICAL = true;
    
    /**
     * Returns whether children will be resized to fill width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether children will be resized to fill width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue == _fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
        relayout();
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

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
    protected void layoutImpl()  { layout(this, isFillWidth()); }

    /**
     * Override for custom defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Vertical
        if (aPropName == Vertical_Prop)
            return DEFAULT_COL_VIEW_VERTICAL;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive FillWidth
        if (isFillWidth())
            e.add(FillWidth_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive FillWidth
        if (anElement.hasAttribute(FillWidth_Prop))
            setFillWidth(anElement.getAttributeBoolValue(FillWidth_Prop));
    }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, double aH)
    {
        ViewProxy par = ViewProxy.getProxy(aPar);
        return getPrefWidthProxy(par, aH);
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    public static double getPrefHeight(ParentView aPar, double aW)
    {
        ViewProxy par = ViewProxy.getProxy(aPar);
        return getPrefHeightProxy(par, aW);
    }

    /**
     * Performs layout for given parent with option to fill width.
     */
    public static void layout(ParentView aPar, boolean isFillWidth)
    {
        // Get layout children (just return if none)
        if (aPar.getChildrenManaged().length == 0) return;

        // Get Parent ViewProxy with Children proxies
        ViewProxy par = ViewProxy.getProxy(aPar);
        par.setFillWidth(isFillWidth);

        // Do Proxy layout
        layoutProxy(par);

        // Push layout bounds back to real views
        par.setBoundsInClient();
    }

    /**
     * Returns preferred width of given parent proxy using ColView layout.
     */
    public static double getPrefWidthProxy(ViewProxy aPar, double aH)
    {
        aPar.setSize(-1, aH);
        layoutProxy(aPar);
        return aPar.getChildrenMaxXAllWithInsets();
    }

    /**
     * Returns preferred height of given parent proxy using ColView layout.
     */
    public static double getPrefHeightProxy(ViewProxy aPar, double aW)
    {
        aPar.setSize(aW, -1);
        layoutProxy(aPar);
        return aPar.getChildrenMaxYLastWithInsets();
    }

    /**
     * Performs layout for given ViewProxy.
     */
    public static void layoutProxy(ViewProxy aPar)
    {
        // If no children, just return
        if (aPar.getChildCount() == 0) return;

        // Load layout rects and return
        layoutProxyY(aPar);
        layoutProxyX(aPar);
    }

    /**
     * Calculates RowView layout X & Width for given Parent proxy.
     */
    private static void layoutProxyX(ViewProxy aPar)
    {
        // Get parent info
        ViewProxy[] children = aPar.getChildren();
        double alignX = aPar.getAlignXAsDouble();
        boolean isFillWidth = aPar.isFillWidth();

        // Get area bounds
        double viewW = aPar.getWidth();
        Insets ins = aPar.getInsetsAll();
        double areaX = ins.left;
        double areaW = Math.max(viewW - ins.getWidth(), 0);

        // Iterate over children to calculate/set child X & Width
        for (ViewProxy child : children) {

            // Calc X accounting for margin and alignment
            Insets childMarg = child.getMargin();
            double childX = areaX + childMarg.left;
            double childW;

            // If Parent.Width not set, set width to Child.PrefWidth
            if (viewW < 0) {
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);
            }

            // Otherwise, if Parent.FillWidth or Child.GrowWidth, set to max width
            else if (isFillWidth || child.isGrowWidth()) {
                childW = Math.max(areaW - childMarg.getWidth(), 0);
            }

            // Otherwise, set width to Child.PrefWidth and adjust X
            else {
                double childMaxW = Math.max(areaW - childMarg.getWidth(), 0);
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);
                childW = Math.min(childW, childMaxW);

                // Calc X accounting for margin and alignment
                if (childW < childMaxW) {
                    double alignX2 = Math.max(alignX, child.getLeanXAsDouble());
                    double shiftX = Math.round((areaW - childW) * alignX2);
                    childX = Math.max(childX, areaX + shiftX);
                }
            }

            // Set child rect X and Width
            child.setX(childX);
            child.setWidth(childW);
        }
    }

    /**
     * Calculates ColView layout Y & Height for given Parent proxy.
     */
    private static void layoutProxyY(ViewProxy aPar)
    {
        // Get parent info
        ViewProxy[] children = aPar.getChildren();
        Insets ins = aPar.getInsetsAll(); // Should really just use Padding
        double parentSpacing = aPar.getSpacing();

        // Loop vars
        double childY = 0;
        ViewProxy lastChild = null;
        double lastMargin = ins.top;

        // Iterate over children to calculate bounds Y and Height
        for (ViewProxy child : children) {

            // Calculate spacing between lastChild and loop child
            double loopMargin = child.getMargin().top;
            double childSpacing = Math.max(lastMargin, loopMargin);
            if (lastChild != null)
                childSpacing = Math.max(childSpacing, parentSpacing);

            // Update ChildY with spacing and calculate ChildH
            childY += childSpacing;
            double childH = child.getBestHeight(-1);

            // Set child bounds Y and Height
            child.setY(childY);
            child.setHeight(childH);

            // Update child Y loop var and last child
            childY += childH;
            lastChild = child;
            lastMargin = child.getMargin().bottom;
        }

        // If Parent.Height -1, just return (laying out for PrefHeight)
        double viewH = aPar.getHeight();
        if (viewH < 0)
            return;

        // Calculate total layout height (last child MaxY + margin/padding)
        double bottomSpacing = Math.max(lastMargin, ins.bottom);
        double layoutH = childY + bottomSpacing;

        // Calculate extra space and add to growers or alignment
        int extraY = (int) Math.round(viewH - layoutH);
        if (extraY != 0)
            addExtraSpaceY(aPar, extraY);
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceY(ViewProxy aPar, int extra)
    {
        // If grow shapes, add grow
        if (aPar.getGrowHeightCount() > 0)
            addExtraSpaceY_ToGrowers(aPar, extra);

        // Otherwise, if FillHeight, extend last child
        //else if (fillH) children[children.length-1].width += extra;

        // Otherwise, check for vertical alignment/lean shift
        else if (extra > 0)
            addExtraSpaceY_ToAlign(aPar, extra);
    }

    /**
     * Adds extra space Y to children that GrowWidth.
     */
    private static void addExtraSpaceY_ToGrowers(ViewProxy aPar, int extra)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int grow = aPar.getGrowHeightCount();
        int each = extra / grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra % grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        ViewProxy[] children = aPar.getChildren();
        for (int i=0, j=0, shiftY = 0, iMax=children.length; i<iMax; i++) {
            ViewProxy child = children[i];
            if (shiftY!=0)
                child.setY(child.getY() + shiftY);
            if (child.isGrowHeight()) {
                int each3 = j < count2 ? eachP1 : each;
                child.setHeight(child.getHeight() + each3);
                shiftY += each3; j++;
            }
        }
    }

    /**
     * Adds extra space Y to child alignment/lean.
     */
    private static void addExtraSpaceY_ToAlign(ViewProxy aPar, double extra)
    {
        ViewProxy[] children = aPar.getChildren();
        double alignY = aPar.getAlignYAsDouble();
        for (ViewProxy child : children) {
            alignY = Math.max(alignY, child.getLeanYAsDouble());
            double shiftY = extra * alignY;
            if (shiftY > 0)
                child.setY(child.getY() + extra * alignY);
        }
    }
}