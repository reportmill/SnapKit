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

        // Load layout rects and return
        layoutProxyY();
        layoutProxyX();
    }

    /**
     * Calculates RowView layout X & Width for given Parent proxy.
     */
    private void layoutProxyX()
    {
        // Get parent info
        ViewProxy<?>[] children = getChildren();
        double alignX = getAlignXAsDouble();
        boolean isFillWidth = isFillWidth();

        // Get area bounds
        double viewW = getWidth();
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaW = Math.max(viewW - ins.getWidth(), 0);

        // Iterate over children to calculate/set child X & Width
        for (ViewProxy<?> child : children) {

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
    private void layoutProxyY()
    {
        // Get parent info
        ViewProxy<?>[] children = getChildren();
        Insets ins = getInsetsAll(); // Should really just use Padding
        double parentSpacing = getSpacing();

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
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceY(ViewProxy<?> aPar, int extra)
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
    private static void addExtraSpaceY_ToGrowers(ViewProxy<?> aPar, int extra)
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
    private static void addExtraSpaceY_ToAlign(ViewProxy<?> aPar, double extra)
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
}