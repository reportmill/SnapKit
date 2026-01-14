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
     * Performs layout in content rect.
     */
    @Override
    public void layoutViewImpl()
    {
        // Get children (just return if empty)
        ViewLayout<?>[] children = getChildren();
        if (children.length == 0) return;

        // Get parent bounds and insets
        double viewW = getWidth();
        double viewH = getHeight();
        Insets parentPadding = getPadding();
        Insets borderInsets = getBorderInsets();

        // Layout children
        for (ViewLayout<?> child : children) {

            // Get child X
            Insets childMargin = child.getMargin();
            double childMarginLeft = Math.max(parentPadding.left, childMargin.left);
            double childMarginRight = Math.max(parentPadding.right, childMargin.right);
            double childX = borderInsets.left + childMarginLeft;

            // Get child width
            double maxChildW = viewW >= 0 ? Math.max(viewW - childMarginLeft - childMarginRight, 0) : -1;
            double childW = viewW >= 0 && child.isGrowWidth() ? maxChildW : child.getBestWidth(-1);

            // If parent width provided, update child X accounting alignment
            if (viewW >= 0 && childW < maxChildW) {
                double childAlignX = child.getLeanX() != null ? child.getLeanXAsDouble() : getAlignXAsDouble();
                childX += Math.round((maxChildW - childW) * childAlignX);
            }

            // Get child Y
            double childMarginTop = Math.max(parentPadding.top, childMargin.top);
            double childMarginBottom = Math.max(parentPadding.bottom, childMargin.bottom);
            double childY = borderInsets.top + childMarginTop;

            // Get child height
            double maxChildH = viewH >= 0 ? Math.max(viewH - childMarginTop - childMarginBottom, 0) : -1;
            double childH = viewH >= 0 && child.isGrowHeight() ? maxChildH : child.getBestHeight(-1);

            // If parent height provided, update child Y accounting alignment
            if (viewH >= 0 && childH < maxChildH) {
                double childAlignY = child.getLeanY() != null ? child.getLeanYAsDouble() : getAlignYAsDouble();
                childY += Math.round((maxChildH - childH) * childAlignY);
            }

            // Set child bounds
            child.setBounds(childX, childY, childW, childH);
        }
    }
}
