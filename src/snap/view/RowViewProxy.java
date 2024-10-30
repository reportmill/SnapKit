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
        return getLastChildMaxXWithInsets();
    }

    /**
     * Returns preferred height of given parent proxy using RowView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return getChildrenMaxYWithInsets();
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
        double parentH = getHeight();
        Insets parentPadding = getPadding();
        double parentSpacing = getSpacing();
        Insets borderInsets = getBorderInsets();

        // Loop vars
        double childX = borderInsets.left;
        ViewProxy<?> lastChild = null;
        double lastMargin = parentPadding.left;

        // Iterate over children to calculate bounds X and Width
        for (ViewProxy<?> child : children) {

            // Calculate spacing between lastChild and loop child
            Insets childMargin = child.getMargin();
            double childSpacing = Math.max(lastMargin, childMargin.left);
            if (lastChild != null)
                childSpacing = Math.max(childSpacing, parentSpacing);

            // If child pref height for PrefWidth calc
            double childInsetTop = Math.max(parentPadding.top, childMargin.top);
            double childInsetBottom = Math.max(parentPadding.bottom, childMargin.bottom);
            double childH = Math.max(parentH - childInsetTop - childInsetBottom, 0);

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
            lastMargin = childMargin.right;
        }

        // If Parent.Width -1, just return (laying out for PrefWidth)
        double viewW = getWidth();
        if (viewW < 0)
            return;

        // Calculate total layout width (last child MaxX + margin/padding)
        double rightSpacing = Math.max(lastMargin, parentPadding.right);
        double layoutW = childX + rightSpacing + borderInsets.right;

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
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();

        // Iterate over children to calculate/set child Y & Height
        for (ViewProxy<?> child : children) {

            // Initialize child Y to top margin
            Insets childMarg = child.getMargin();
            double childY = borderInsets.top + Math.max(parentPadding.top, childMarg.top);
            double childInsetBottom = borderInsets.bottom + Math.max(parentPadding.bottom, childMarg.bottom);

            // Initialize child Height to max height
            double childMaxH = Math.max(viewH - childY - childInsetBottom, 0);
            double childH = childMaxH;

            // If Parent.Height not set, just set height to Child.PrefHeight
            if (viewH < 0) {
                double childW = child.getWidth();
                childH = child.getBestHeight(childW);
            }

            // Otherwise, if not FillHeight, set height to Child.PrefHeight and align Y
            else if (!(isFillHeight || child.isGrowHeight())) {

                // Set child height to Child.PrefHeight
                double childW = child.getWidth();
                childH = child.getBestHeight(childW);

                // Constrain child height to max child height or if space available and align set, shift Y
                if (childH > childMaxH)
                    childH = childMaxH;
                else if (childH < childMaxH) {
                    double chldAlignY = Math.max(alignY, child.getLeanYAsDouble());
                    if (chldAlignY > 0)
                        childY += Math.round((childMaxH - childH) * chldAlignY);
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
        double shiftX = 0;

        for (ViewProxy<?> child : children) {

            // If child has lean, apply shift
            if (child.getLeanX() != null && extra > 0) {
                int childShiftX = (int) Math.round(extra * child.getLeanX().doubleValue());
                if (childShiftX > 0) {
                    shiftX += childShiftX;
                    extra -= childShiftX;
                }
            }

            // If parent has alignment, apply shift
            else if (alignX > 0 && extra > 0) {
                int childShiftX = (int) Math.round(extra * alignX);
                if (childShiftX > 0) {
                    shiftX += childShiftX;
                    extra -= childShiftX;
                    alignX = 0;
                }
            }

            // Apply shift
            if (shiftX > 0)
                child.setX(child.getX() + shiftX);
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