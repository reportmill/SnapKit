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

    // The views
    View _topView, _bottomView;

    // The row layout
    public RowViewLayout<?> _rowLayout;

    /**
     * Constructor for given parent view and border views.
     */
    public BorderViewLayout(ParentView aPar, View centerView, View topView, View rightView, View bottomView, View leftView)
    {
        super(aPar);
        _topView = topView;
        _bottomView = bottomView;
        setFillWidth(true);

        // Create row layout
        _rowLayout = getRowViewLayout(centerView, leftView, rightView);

        // Create and add proxies for top/bottom views
        List<ViewLayout<?>> childProxies = new ArrayList<>(3);
        if (topView != null && topView.isVisible())
            childProxies.add(topView.getViewLayout());
        childProxies.add(_rowLayout);
        if (bottomView != null && bottomView.isVisible())
            childProxies.add(bottomView.getViewLayout());

        // Set trimmed children
        setChildren(childProxies.toArray(new ViewLayout[0]));
    }

    /**
     * Constructor for BorderView Center, Right, Left.
     */
    private static RowViewLayout<?> getRowViewLayout(View centerView, View leftView, View rightView)
    {
        RowViewLayout<?> viewLayout = new RowViewLayout<>(new View());
        viewLayout.setFillHeight(true);

        // Create and add proxies for left/center/right views
        List<ViewLayout<?>> childProxies = new ArrayList<>(3);
        if (leftView != null && leftView.isVisible())
            childProxies.add(leftView.getViewLayout());
        if (centerView != null && centerView.isVisible()) {
            ViewLayout<?> centerProxy = centerView.getViewLayout();
            centerProxy.setGrowWidth(true);
            centerProxy.setGrowHeight(true);
            childProxies.add(centerProxy);
        }
        if (rightView != null && rightView.isVisible())
            childProxies.add(rightView.getViewLayout());

        // Set trimmed children and GrowHeight
        viewLayout.setChildren(childProxies.toArray(new ViewLayout[0]));
        viewLayout.setGrowHeight(true);
        return viewLayout;
    }

    /**
     * Override to update row layout children y value before normal version.
     */
    @Override
    public void setBoundsInClient()
    {
        for (ViewLayout<?> child : _rowLayout.getChildren())
            child.setY(child.getY() + _rowLayout.getY());
        super.setBoundsInClient();
    }

    /**
     * Override to layout RowView.
     */
    @Override
    public void layoutView()
    {
        super.layoutView();

        double rowH = getHeight();
        ViewLayout<?> topViewLayout = getChildren()[0];
        ViewLayout<?> bottomViewLayout = getLastChild();
        if (topViewLayout != _rowLayout) rowH -= _topView.getHeight();
        if (bottomViewLayout != _rowLayout) rowH -= _bottomView.getHeight();
        _rowLayout.setSize(getWidth(), rowH);
        _rowLayout.layoutProxy();
        _rowLayout.layoutView();
    }
}