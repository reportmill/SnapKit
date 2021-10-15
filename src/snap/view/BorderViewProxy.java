/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.Arrays;

/**
 * A ViewProxy subclass to layout views along edges (top, bottom, left, right) and center.
 */
public class BorderViewProxy extends ColViewProxy<View> {

    // The row proxy
    public RowViewProxy<?>  _rowProxy;

    /**
     * Constructor for given parent view and border views.
     */
    public BorderViewProxy(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft)
    {
        super(aPar);
        setFillWidth(true);

        // Create RowProxy
        _rowProxy = getRowViewProxy(aCtr, aLft, aRgt);

        // Create proxy child array and create/add proxies
        ViewProxy<?>[] colKids = new ViewProxy[3];
        int colKidCount = 0;
        if (aTop != null)
            colKids[colKidCount++] = new ViewProxy<>(aTop);
        colKids[colKidCount++] = _rowProxy;
        if (aBtm != null)
            colKids[colKidCount++] = new ViewProxy<>(aBtm);

        // Set trimmed children
        setChildren(Arrays.copyOf(colKids, colKidCount));
    }

    /**
     * Constructor for BorderView Center, Right, Left.
     */
    private static RowViewProxy<?> getRowViewProxy(View aCtr, View aLft, View aRgt)
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(null);
        viewProxy.setFillHeight(true);

        // Create proxy child array and create/add proxies
        ViewProxy<?>[] children = new ViewProxy[3];
        int count = 0;
        if (aLft != null)
            children[count++] = new ViewProxy<>(aLft);
        if (aCtr != null) {
            ViewProxy<?> ctrProxy = children[count++] = new ViewProxy<>(aCtr);
            ctrProxy.setGrowWidth(true);
            ctrProxy.setGrowHeight(true);
        }
        if (aRgt != null)
            children[count++] = new ViewProxy<>(aRgt);

        // Set trimmed children and GrowHeight
        viewProxy.setChildren(Arrays.copyOf(children, count));
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
    public void layoutProxy()
    {
        super.layoutProxy();
        _rowProxy.layoutProxy();
    }
}