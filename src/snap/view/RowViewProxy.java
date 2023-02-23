/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.MathUtils;

/**
 * A ViewProxy subclass to layout child views horizontally, from left to right.
 */
public class RowViewProxy<T extends View> extends ParentViewProxy<T> {

    /**
     * Constructor for given parent view.
     */
    public RowViewProxy(View aParent)
    {
        super(aParent);

        if (aParent instanceof RowView) {
            RowView colView = (RowView) aParent;
            setFillHeight(colView.isFillHeight());
        }
    }

    /**
     * Returns preferred width of given parent proxy using RowView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return getChildrenMaxXLastWithInsets();
    }

    /**
     * Returns preferred height of given parent proxy using RowView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return getChildrenMaxYAllWithInsets();
    }

    /**
     * Performs layout for given ViewProxy.
     */
    public void layoutProxy()
    {
        // If no children, just return
        if (getChildCount() == 0) return;

        // If FillWidth and no children grow, make last child grow
        if (isFillWidth() && getGrowWidthCount() == 0) {
            ViewProxy<?> lastChild = getChildren()[getChildCount() - 1];
            lastChild.setGrowWidth(true);
            _growWidthCount++;
        }

        // Load layout rects and return
        layoutProxyX();
        layoutProxyY();
    }

    /**
     * Calculates RowView layout X & Width for given Parent proxy.
     */
    private void layoutProxyX()
    {
        // Get parent info
        ViewProxy<?>[] children = getChildren();
        Insets ins = getInsetsAll();
        double parentSpacing = getSpacing();
        boolean isFillHeight = isFillHeight() && getHeight() > 0;

        // Loop vars
        double childX = 0;
        ViewProxy<?> lastChild = null;
        double lastMargin = ins.left;

        // Iterate over children to calculate bounds X and Width
        for (ViewProxy<?> child : children) {

            // Calculate spacing between lastChild and loop child
            double loopMargin = child.getMargin().left;
            double childSpacing = Math.max(lastMargin, loopMargin);
            if (lastChild != null)
                childSpacing = Math.max(childSpacing, parentSpacing);

            // If child height is fixed because of FillHeight/GrowHeight, get child height value for PrefWidth calc
            double childH = -1;
            if (isFillHeight || child.isGrowHeight())
                childH = getChildFixedHeight(this, child);

            // Update ChildX with spacing, round and set
            childX += childSpacing;
            childX = Math.round(childX);
            child.setX(childX);

            // Calculate child width and set
            double childW = child.getBestWidth(childH);
            child.setWidth(childW);

            // Update child x loop var and last child
            childX += childW;
            lastChild = child;
            lastMargin = lastChild.getMargin().right;
        }

        // If Parent.Width -1, just return (laying out for PrefWidth)
        double viewW = getWidth();
        if (viewW < 0)
            return;

        // Calculate total layout width (last child MaxX + margin/padding)
        double rightSpacing = Math.max(lastMargin, ins.right);
        double layoutW = childX + rightSpacing;

        // Calculate extra space and add to growers or alignment
        int extraX = (int) Math.round(viewW - layoutW);
        if (extraX != 0)
            addExtraSpaceX(this, extraX);
    }

    /**
     * Calculates RowView layout Y & Height for given Parent proxy.
     */
    private void layoutProxyY()
    {
        // Get parent info
        ViewProxy<?>[] children = getChildren();
        double alignY = getAlignYAsDouble();
        boolean isFillHeight = isFillHeight();

        // Get view bounds, insets
        double viewH = getHeight();
        Insets ins = getInsetsAll();

        // Iterate over children to calculate/set child Y & Height
        for (ViewProxy<?> child : children) {

            // Calc Y accounting for margin and alignment
            Insets childMarg = child.getMargin();
            double topMarg = Math.max(ins.top, childMarg.top);
            double btmMarg = Math.max(ins.bottom, childMarg.bottom);
            double margH = topMarg + btmMarg;

            // Declare/init child Y and Height
            double childY = topMarg;
            double childH;

            // If Parent.Height not set, set height to Child.PrefHeight
            if (viewH < 0) {
                double childW = child.getWidth();
                childH = child.getBestHeight(childW);
            }

            // Otherwise, if Parent.FillHeight or Child.GrowHeight, set to max height
            else if (isFillHeight || child.isGrowHeight()) {
                childH = Math.max(viewH - margH, 0);
            }

            // Otherwise, set height to Child.PrefHeight and adjust Y
            else {
                double childMaxH = Math.max(viewH - margH, 0);
                double childW = child.getWidth();
                childH = child.getBestHeight(childW);
                childH = Math.min(childH, childMaxH);

                // Calc y accounting for margin and alignment
                if (childH < childMaxH) {
                    double alignY2 = Math.max(alignY, child.getLeanYAsDouble());
                    double shiftY = Math.round((viewH - childH) * alignY2);
                    childY = Math.max(childY, shiftY);
                }
            }

            // Set child rect Y and Height
            child.setY(childY);
            child.setHeight(childH);
        }
    }

    /**
     * Returns the child fixed width.
     */
    private static double getChildFixedHeight(ViewProxy<?> aParent, ViewProxy<?> aChild)
    {
        double parH = aParent.getHeight();
        Insets parPadding = aParent.getPadding();
        Insets childMargin = aChild.getMargin();
        double insTop = Math.max(parPadding.top, childMargin.top);
        double insBottom = Math.max(parPadding.bottom, childMargin.bottom);
        double fixedH = Math.max(parH - insTop - insBottom, 0);
        return fixedH;
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceX(ParentViewProxy<?> aPar, int extra)
    {
        // If there is child/children with GrowWidth, add extra width to child growers
        if (aPar.getGrowWidthCount() > 0)
            addExtraSpaceX_ToGrowers(aPar, extra);

        // If extra is positive, use for horizontal alignment/lean shift
        else if (extra > 0)
            addExtraSpaceX_ToAlign(aPar, extra);

        // If negative, try to trim last child back
        else removeExtraSpaceX_FromLastChild(aPar, extra);
    }

    /**
     * Adds extra space to children that GrowWidth.
     */
    private static void addExtraSpaceX_ToGrowers(ParentViewProxy<?> aPar, int extra)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int grow = aPar.getGrowWidthCount();
        int each = extra / grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra % grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        ViewProxy<?>[] children = aPar.getChildren();
        for (int i=0, j=0, shiftX = 0, iMax=children.length; i<iMax; i++) {
            ViewProxy<?> child = children[i];
            if (shiftX != 0)
                child.setX(child.getX() + shiftX);
            if (child.isGrowWidth()) {
                int each3 = j < count2 ? eachP1 : each;
                child.setWidth(child.getWidth() + each3);
                shiftX += each3; j++;
            }
        }
    }

    /**
     * Adds extra space to child alignment/lean.
     */
    private static void addExtraSpaceX_ToAlign(ViewProxy<?> aPar, int extra)
    {
        ViewProxy<?>[] children = aPar.getChildren();
        double alignX = aPar.getAlignXAsDouble();
        for (ViewProxy<?> child : children) {
            alignX = Math.max(alignX, child.getLeanXAsDouble());
            double shiftX = extra * alignX;
            if (shiftX > 0)
                child.setX(child.getX() + extra * alignX);
        }
    }

    /**
     * Remove extra space from last child.
     */
    private static void removeExtraSpaceX_FromLastChild(ViewProxy<?> aPar, int extra)
    {
        // Get last child
        ViewProxy<?>[] children = aPar.getChildren();
        ViewProxy<?> lastChild = children[children.length - 1];

        // Remove width from last child - probably should iterate to previous children if needed
        double childW = Math.max(lastChild.width + extra, 10);
        lastChild.setWidth(childW);
    }
}