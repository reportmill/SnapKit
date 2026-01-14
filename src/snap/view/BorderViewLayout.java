/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.ArrayList;
import java.util.List;

/**
 * A layout subclass to layout views along edges (top, bottom, left, right) and center.
 */
public class BorderViewLayout extends ColViewLayout<View> {

    // The top view
    private View _topView;

    // The row layout
    public RowViewLayout<?> _rowLayout;

    /**
     * Constructor for given parent view and border views.
     */
    public BorderViewLayout(ParentView aPar, View centerView, View topView, View rightView, View bottomView, View leftView)
    {
        super(aPar);
        _topView = topView;
        setFillWidth(true);

        // Create row layout
        _rowLayout = getRowViewLayout(centerView, leftView, rightView);

        // Create and add layouts for top/bottom views
        List<ViewLayout<?>> childLayouts = new ArrayList<>(3);
        if (topView != null && topView.isVisible())
            childLayouts.add(topView.getViewLayout());
        childLayouts.add(_rowLayout);
        if (bottomView != null && bottomView.isVisible())
            childLayouts.add(bottomView.getViewLayout());

        // Set trimmed children
        setChildren(childLayouts);
    }

    /**
     * Creates a row layout for left/center/right views.
     */
    private static RowViewLayout<?> getRowViewLayout(View centerView, View leftView, View rightView)
    {
        RowViewLayout<?> rowLayout = new RowViewLayout<>(new View());
        rowLayout.setFillHeight(true);

        // Create and add layouts for left/center/right views
        List<ViewLayout<?>> childLayouts = new ArrayList<>(3);
        if (leftView != null && leftView.isVisible())
            childLayouts.add(leftView.getViewLayout());
        if (centerView != null && centerView.isVisible()) {
            ViewLayout<?> centerLayout = centerView.getViewLayout();
            centerLayout.setGrowWidth(true);
            centerLayout.setGrowHeight(true);
            childLayouts.add(centerLayout);
        }
        if (rightView != null && rightView.isVisible())
            childLayouts.add(rightView.getViewLayout());

        // Set trimmed children and GrowHeight
        rowLayout.setChildren(childLayouts);
        rowLayout.setGrowHeight(true);
        return rowLayout;
    }

    /**
     * Override to layout row views.
     */
    @Override
    public void layoutView()
    {
        super.layoutView();

        // Layout row
        _rowLayout.layoutView();

        // If top view is present, adjust Y for row children
        if (_topView != null) {
            double rowY = _topView.getMaxY();
            for (ViewLayout<?> child : _rowLayout.getChildren())
                child._view.setY(rowY);
        }
    }
}