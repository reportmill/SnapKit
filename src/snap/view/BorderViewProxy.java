/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.ArrayList;
import java.util.List;

/**
 * A ViewProxy subclass to layout views along edges (top, bottom, left, right) and center.
 */
public class BorderViewProxy extends ColViewProxy<View> {

    // The views
    View _topView, _bottomView;

    // The row proxy
    public RowViewProxy<?> _rowProxy;

    /**
     * Constructor for given parent view and border views.
     */
    public BorderViewProxy(ParentView aPar, View centerView, View topView, View rightView, View bottomView, View leftView)
    {
        super(aPar);
        _topView = topView;
        _bottomView = bottomView;
        setFillWidth(true);

        // Create RowProxy
        _rowProxy = getRowViewProxy(centerView, leftView, rightView);

        // Create and add proxies for top/bottom views
        List<ViewProxy<?>> childProxies = new ArrayList<>(3);
        if (topView != null && topView.isVisible())
            childProxies.add(topView.getViewProxy());
        childProxies.add(_rowProxy);
        if (bottomView != null && bottomView.isVisible())
            childProxies.add(bottomView.getViewProxy());

        // Set trimmed children
        setChildren(childProxies.toArray(new ViewProxy[0]));
    }

    /**
     * Constructor for BorderView Center, Right, Left.
     */
    private static RowViewProxy<?> getRowViewProxy(View centerView, View leftView, View rightView)
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(null);
        viewProxy.setFillHeight(true);

        // Create and add proxies for left/center/right views
        List<ViewProxy<?>> childProxies = new ArrayList<>(3);
        if (leftView != null && leftView.isVisible())
            childProxies.add(leftView.getViewProxy());
        if (centerView != null && centerView.isVisible()) {
            ViewProxy<?> centerProxy = centerView.getViewProxy();
            centerProxy.setGrowWidth(true);
            centerProxy.setGrowHeight(true);
            childProxies.add(centerProxy);
        }
        if (rightView != null && rightView.isVisible())
            childProxies.add(rightView.getViewProxy());

        // Set trimmed children and GrowHeight
        viewProxy.setChildren(childProxies.toArray(new ViewProxy[0]));
        viewProxy.setGrowHeight(true);
        return viewProxy;
    }

    /**
     * Override to update RowProxy children y value before normal version.
     */
    @Override
    public void setBoundsInClient()
    {
        for (ViewProxy<?> child : _rowProxy.getChildren())
            child.setY(child.getY() + _rowProxy.getY());
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
        ViewProxy<?> topViewProxy = getChildren()[0];
        ViewProxy<?> bottomViewProxy = getLastChild();
        if (topViewProxy != _rowProxy) rowH -= _topView.getHeight();
        if (bottomViewProxy != _rowProxy) rowH -= _bottomView.getHeight();
        _rowProxy.setSize(getWidth(), rowH);
        _rowProxy.layoutProxy();
        _rowProxy.layoutView();
    }
}