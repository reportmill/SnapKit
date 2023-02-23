/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.MathUtils;

/**
 * A ViewProxy subclass to layout child views vertically, from top to bottom.
 */
public class ColViewProxy<T extends View> extends ParentViewProxy<T> {

    /**
     * Constructor for given parent view.
     */
    public ColViewProxy(View aParent)
    {
        super(aParent);

        if (aParent instanceof ColView) {
            ColView colView = (ColView) aParent;
            setFillWidth(colView.isFillWidth());
        }
    }

    /**
     * Returns preferred width of given parent proxy using ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        double prefW = getChildrenMaxXAllWithInsets();
        return prefW;
    }

    /**
     * Returns preferred height of given parent proxy using ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        double prefH = getChildrenMaxYLastWithInsets();
        return prefH;
    }

    /**
     * Performs layout for given ViewProxy.
     */
    @Override
    public void layoutProxy()
    {
        // If no children, just return
        if (getChildCount() == 0) return;

        // If FillHeight and no children grow, make last child grow
        if (isFillHeight() && getGrowHeightCount() == 0) {
            ViewProxy<?> lastChild = getChildren()[getChildCount() - 1];
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
        ViewProxy<?>[] children = getChildren();
        double alignX = getAlignXAsDouble();
        boolean isFillWidth = isFillWidth();

        // Get view bounds, insets
        double viewW = getWidth();
        Insets ins = getInsetsAll();

        // Iterate over children to calculate/set child X & Width
        for (ViewProxy<?> child : children) {

            // Calc X accounting for margin and alignment
            Insets childMarg = child.getMargin();
            double leftMarg = Math.max(ins.left, childMarg.left);
            double rightMarg = Math.max(ins.right, childMarg.right);
            double margW = leftMarg + rightMarg;

            // Declare/init child X and Width
            double childX = leftMarg;
            double childW;

            // If Parent.Width not set, set width to Child.PrefWidth
            if (viewW < 0) {
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);
            }

            // Otherwise, if Parent.FillWidth or Child.GrowWidth, set to max width
            else if (isFillWidth || child.isGrowWidth()) {
                childW = Math.max(viewW - margW, 0);
            }

            // Otherwise, set width to Child.PrefWidth and adjust X
            else {
                double childMaxW = Math.max(viewW - margW, 0);
                double childH = child.getHeight();
                childW = child.getBestWidth(childH);
                childW = Math.min(childW, childMaxW);

                // Calc X accounting for margin and alignment
                if (childW < childMaxW) {
                    double alignX2 = Math.max(alignX, child.getLeanXAsDouble());
                    double shiftX = Math.round((viewW - childW) * alignX2);
                    childX = Math.max(childX, shiftX);
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
        ViewProxy<?>[] children = getChildren();
        Insets ins = getInsetsAll(); // Should really just use Padding
        double parentSpacing = getSpacing();
        boolean isFillWidth = isFillWidth() && getWidth() > 0;

        // Loop vars
        double childY = 0;
        ViewProxy<?> lastChild = null;
        double lastMargin = ins.top;

        // Iterate over children to calculate bounds Y and Height
        for (ViewProxy<?> child : children) {

            // Calculate spacing between lastChild and loop child
            double loopMargin = child.getMargin().top;
            double childSpacing = Math.max(lastMargin, loopMargin);
            if (lastChild != null)
                childSpacing = Math.max(childSpacing, parentSpacing);

            // If child width is fixed because of FillWidth or GrowWidth, get child width value for PrefHeight calc
            double childW = -1;
            if (isFillWidth || child.isGrowWidth())
                childW = getChildFixedWidth(this, child);

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
            lastMargin = child.getMargin().bottom;
        }

        // If Parent.Height -1, just return (laying out for PrefHeight)
        double viewH = getHeight();
        if (viewH < 0)
            return;

        // Calculate total layout height (last child MaxY + margin/padding)
        double bottomSpacing = Math.max(lastMargin, ins.bottom);
        double layoutH = childY + bottomSpacing;

        // Calculate extra space and add to growers or alignment
        int extraY = (int) Math.round(viewH - layoutH);
        if (extraY != 0)
            addExtraSpaceY(this, extraY);
    }

    /**
     * Returns the child fixed width.
     */
    private static double getChildFixedWidth(ViewProxy<?> aParent, ViewProxy<?> aChild)
    {
        double parW = aParent.getWidth();
        Insets parPadding = aParent.getPadding();
        Insets childMargin = aChild.getMargin();
        double insLeft = Math.max(parPadding.left, childMargin.left);
        double insRight = Math.max(parPadding.right, childMargin.right);
        double fixedW = Math.max(parW - insLeft - insRight, 0);
        return fixedW;
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceY(ParentViewProxy<?> aPar, int extra)
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
    private static void addExtraSpaceY_ToGrowers(ParentViewProxy<?> aPar, int extra)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int grow = aPar.getGrowHeightCount();
        int each = extra / grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra % grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        ViewProxy<?>[] children = aPar.getChildren();
        for (int i = 0, j = 0, shiftY = 0, iMax = children.length; i < iMax; i++) {
            ViewProxy<?> child = children[i];
            if (shiftY != 0)
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
    private static void addExtraSpaceY_ToAlign(ViewProxy<?> aPar, int extra)
    {
        ViewProxy<?>[] children = aPar.getChildren();
        double alignY = aPar.getAlignYAsDouble();
        for (ViewProxy<?> child : children) {
            alignY = Math.max(alignY, child.getLeanYAsDouble());
            double shiftY = extra * alignY;
            if (shiftY > 0)
                child.setY(child.getY() + extra * alignY);
        }
    }

    /**
     * Remove extra space from last child.
     */
    private static void removeExtraSpaceY_FromLastChild(ViewProxy<?> aPar, int extra)
    {
        // Get last child
        ViewProxy<?>[] children = aPar.getChildren();
        ViewProxy<?> lastChild = children[children.length - 1];

        // Remove width from last child - probably should iterate to previous children if needed
        double childH = Math.max(lastChild.height + extra, 10);
        lastChild.setHeight(childH);
    }
}