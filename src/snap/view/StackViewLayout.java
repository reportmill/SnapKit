package snap.view;
import snap.geom.Insets;

/**
 * A layout class to layout children stacked on top of each other.
 */
public class StackViewLayout<T extends View> extends ParentViewLayout<T> {

    /**
     * Constructor.
     */
    public StackViewLayout(View aView)
    {
        super(aView);
    }

    /**
     * Returns preferred width of given parent with given children.
     */
    @Override
    public double getPrefWidthImpl(double aH)
    {
        ViewLayout<?> viewLayout = new ColViewLayout<>(getView());
        return viewLayout.getPrefWidth(aH);
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    @Override
    public double getPrefHeightImpl(double aW)
    {
        ViewLayout<?> viewLayout = new RowViewLayout<>(getView());
        return viewLayout.getPrefHeight(aW);
    }

    /**
     * Performs layout in content rect.
     */
    @Override
    public void layoutProxy()
    {
        // Get children (just return if empty)
        ViewLayout<?>[] children = getChildren();
        if (children.length == 0) return;

        // Get parent bounds for insets
        Insets ins = getPadding(); if (getBorder() != null) ins = Insets.add(ins, getBorderInsets());
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        if (areaW < 0) areaW = 0;
        double areaH = getHeight() - ins.getHeight();
        if (areaH < 0) areaH = 0;

        // Get child bounds
        double alignX = getAlignXAsDouble();
        double alignY = getAlignYAsDouble();

        // Layout children
        for (ViewLayout<?> child : children) {

            // Get child margin
            Insets marg = child.getMargin();

            // Get child width
            double maxW = Math.max(areaW - marg.getWidth(), 0);
            double childW = child.isGrowWidth() ? maxW : Math.min(child.getBestWidth(-1), maxW);

            // Calc x accounting for margin and alignment
            double childX = areaX + marg.left;
            if (childW < maxW) {
                double alignX2 = Math.max(alignX, child.getLeanXAsDouble());
                childX = Math.max(childX, areaX + Math.round((areaW - childW) * alignX2));
            }

            // Get child height
            double maxH = Math.max(areaH - marg.getHeight(), 0);
            double childH = child.isGrowHeight() ? maxH : Math.min(child.getBestHeight(-1), maxH);

            // Calc y accounting for margin and alignment
            double childY = areaY + marg.top;
            if (childH < maxH) {
                double alignY2 = Math.max(alignY, child.getLeanYAsDouble());
                childY = Math.max(childY, areaY + Math.round((areaH - childH) * alignY2));
            }

            // Set child bounds
            child.setBounds(childX, childY, childW, childH);
        }
    }
}
