/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.MathUtils;

/**
 * A ViewLayout subclass to layout child views vertically, from top to bottom.
 */
public class ColViewLayout<T extends View> extends ParentViewLayout<T> {

    /**
     * Constructor for given parent view.
     */
    public ColViewLayout(View aParent)
    {
        super(aParent);
        if (aParent instanceof ColView colView) {
            setFillWidth(colView.isFillWidth());
            setHugging(colView.isHugging());
        }
    }

    /**
     * Constructor for given parent view.
     */
    public ColViewLayout(View aParent, boolean aFillWidth)
    {
        super(aParent);
        setFillWidth(aFillWidth);
    }

    /**
     * Returns preferred width of given parent proxy using ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)  { return getChildrenMaxXWithInsets(); }

    /**
     * Returns preferred height of given parent proxy using ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)  { return getLastChildMaxYWithInsets(); }

    /**
     * Performs layout.
     */
    @Override
    public void layoutViewLayout()
    {
        // If no children, just return
        if (getChildCount() == 0) return;

        // If FillHeight and no children grow, make last child grow
        if (isFillHeight() && getGrowHeightCount() == 0) {
            ViewLayout<?> lastChild = getChildren()[getChildCount() - 1];
            lastChild.setGrowHeight(true);
            _growHeightCount++;
        }

        // Load layout rects and return
        layoutProxyY();
        layoutProxyX();
    }

    /**
     * Calculates ColView layout X & Width for given Parent proxy.
     */
    private void layoutProxyX()
    {
        // Get parent info
        ViewLayout<?>[] children = getChildren();
        double alignX = getAlignXAsDouble();
        boolean isFillWidth = isFillWidth();

        // Get view bounds, insets
        double viewW = getWidth();
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();

        // Iterate over children to calculate/set child X & Width
        for (ViewLayout<?> child : children) {

            // Initialize child X to left margin
            Insets childMargin = child.getMargin();
            double childX = borderInsets.left + Math.max(parentPadding.left, childMargin.left);
            double childInsetRight = borderInsets.right + Math.max(parentPadding.right, childMargin.right);

            // Initialize child width to max width
            double childMaxW = Math.max(viewW - childX - childInsetRight, 0);
            double childW = childMaxW;

            // If Parent.Width not set, just set width to Child.PrefWidth
            if (viewW < 0) {
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);
            }

            // Otherwise, if not FillWidth, set width to Child.PrefWidth and align X
            else if (!(isFillWidth || child.isGrowWidth())) {

                // Set child width to Child.PrefWidth
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);

                // Constrain child width to max child width or if space available and align set, shift X
                if (childW > childMaxW)
                    childW = childMaxW;
                else if (childW < childMaxW) {
                    double childAlignX = Math.max(alignX, child.getLeanXAsDouble());
                    if (childAlignX > 0)
                        childX += Math.round((childMaxW - childW) * childAlignX);
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
    private void layoutProxyY()
    {
        // Get parent info
        ViewLayout<?>[] children = getChildren();
        double parentW = getWidth();
        Insets parentPadding = getPadding();
        double parentSpacing = getSpacing();
        Insets borderInsets = getBorderInsets();

        // Loop vars
        double childY = borderInsets.top;
        ViewLayout<?> lastChild = null;
        double lastMargin = parentPadding.top;

        // Iterate over children to calculate bounds Y and Height
        for (ViewLayout<?> child : children) {

            // Calculate spacing between lastChild and loop child
            Insets childMargin = child.getMargin();
            double childSpacing = Math.max(lastMargin, childMargin.top);
            if (lastChild != null)
                childSpacing = Math.max(childSpacing, parentSpacing);

            // Get child pref width
            double childInsetLeft = Math.max(parentPadding.left, childMargin.left);
            double childInsetRight = Math.max(parentPadding.right, childMargin.right);
            double childW = Math.max(parentW - childInsetLeft - childInsetRight, 0);

            // Update ChildY with spacing, round and set
            childY += childSpacing;
            childY = Math.round(childY);
            child.setY(childY);

            // Calculate child height and set
            double childH = child.getBestHeight(childW);
            child.setHeight(childH);

            // Update child Y loop var and last child
            childY += childH;
            lastChild = child;
            lastMargin = childMargin.bottom;
        }

        // If Parent.Height -1, just return (laying out for PrefHeight)
        double viewH = getHeight();
        if (viewH < 0)
            return;

        // Calculate total layout height (last child MaxY + margin/padding)
        double bottomSpacing = Math.max(lastMargin, parentPadding.bottom);
        double layoutH = childY + bottomSpacing + borderInsets.bottom;

        // Calculate extra space and add to growers or alignment
        int extraY = (int) Math.round(viewH - layoutH);
        if (extraY != 0)
            addExtraSpaceY(this, extraY);
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceY(ParentViewLayout<?> aPar, int extra)
    {
        // If there is child/children with GrowHeight, add extra height to child growers
        if (aPar.getGrowHeightCount() > 0)
            addExtraSpaceY_ToGrowers(aPar, extra);

        // If extra is positive, use for vertical alignment/lean shift
        else if (extra > 0)
            addExtraSpaceY_ToAlign(aPar, extra);

        // If negative, try to trim last child back
        else removeExtraSpaceY_FromLastChild(aPar, extra);
    }

    /**
     * Adds extra space Y to children that GrowWidth.
     */
    private static void addExtraSpaceY_ToGrowers(ParentViewLayout<?> aPar, int extra)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int grow = aPar.getGrowHeightCount();
        int each = extra / grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra % grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        ViewLayout<?>[] children = aPar.getChildren();
        for (int i = 0, j = 0, shiftY = 0, iMax = children.length; i < iMax; i++) {
            ViewLayout<?> child = children[i];
            if (shiftY != 0)
                child.setY(child.getY() + shiftY);
            if (child.isGrowHeight()) {
                int each3 = j < count2 ? eachP1 : each;
                double childH = Math.max(child.height + each3, 0);
                child.setHeight(childH);
                shiftY += each3; j++;
            }
        }
    }

    /**
     * Adds extra space Y to child alignment/lean.
     */
    private static void addExtraSpaceY_ToAlign(ViewLayout<?> aPar, int extra)
    {
        ViewLayout<?>[] children = aPar.getChildren();
        double alignY = aPar.getAlignYAsDouble();
        double shiftY = 0;

        for (ViewLayout<?> child : children) {

            // If child has lean, apply shift
            if (child.getLeanY() != null && extra > 0) {
                int childShiftY = (int) Math.round(extra * child.getLeanY().doubleValue());
                if (childShiftY > 0) {
                    shiftY += childShiftY;
                    extra -= childShiftY;
                }
            }

            // If parent has alignment, apply shift
            else if (alignY > 0 && extra > 0) {
                int childShiftY = (int) Math.round(extra * alignY);
                if (childShiftY > 0) {
                    shiftY += childShiftY;
                    extra -= childShiftY;
                    alignY = 0;
                }
            }

            // Apply shift
            if (shiftY > 0)
                child.setY(child.getY() + shiftY);
        }
    }

    /**
     * Remove extra space from last child.
     */
    private static void removeExtraSpaceY_FromLastChild(ViewLayout<?> aPar, int extra)
    {
        // Get last child
        ViewLayout<?>[] children = aPar.getChildren();
        ViewLayout<?> lastChild = children[children.length - 1];

        // Remove width from last child - probably should iterate to previous children if needed
        double childH = Math.max(lastChild.height + extra, 10);
        lastChild.setHeight(childH);
    }
}