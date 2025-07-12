/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;

/**
 * A View subclass to manage subviews along edges (top, bottom, left, right) and center.
 */
public class BorderView extends ParentView {

    // The panes
    private View _top, _center, _bottom, _left, _right;

    // Constants for properties
    public static final String Center_Prop = "Center";
    public static final String Top_Prop = "Top";
    public static final String Bottom_Prop = "Bottom";
    public static final String Left_Prop = "Left";
    public static final String Right_Prop = "Right";

    /**
     * Constructor.
     */
    public BorderView()
    {
        super();
        _align = Pos.CENTER;
    }

    /**
     * Returns the center node.
     */
    public View getCenter()  { return _center; }

    /**
     * Sets the center node.
     */
    public void setCenter(View aView)
    {
        if (aView == _center) return;

        batchPropChanges();
        if (_center != null)
            removeChild(_center);
        firePropChange(Center_Prop, _center, _center = aView);
        if (_center != null)
            addChild(_center);

        fireBatchPropChanges();
    }

    /**
     * Returns the top node.
     */
    public View getTop()  { return _top; }

    /**
     * Sets the top node.
     */
    public void setTop(View aView)
    {
        if (aView == _top) return;

        batchPropChanges();
        if (_top != null)
            removeChild(_top);
        firePropChange(Top_Prop, _top, _top = aView);
        if (_top != null)
            addChild(_top);

        fireBatchPropChanges();
    }

    /**
     * Returns the bottom node.
     */
    public View getBottom()  { return _bottom; }

    /**
     * Sets the bottom node.
     */
    public void setBottom(View aView)
    {
        if (aView == _bottom) return;

        batchPropChanges();
        if (_bottom != null)
            removeChild(_bottom);
        firePropChange(Bottom_Prop, _bottom, _bottom = aView);
        if (_bottom != null)
            addChild(_bottom);

        fireBatchPropChanges();
    }

    /**
     * Returns the left node.
     */
    public View getLeft()  { return _left; }

    /**
     * Sets the left node.
     */
    public void setLeft(View aView)
    {
        if (aView == _left) return;

        batchPropChanges();
        if (_left != null)
            removeChild(_left);
        firePropChange(Left_Prop, _left, _left = aView);
        if (_left != null)
            addChild(_left);

        fireBatchPropChanges();
    }

    /**
     * Returns the right node.
     */
    public View getRight()  { return _right; }

    /**
     * Sets the right node.
     */
    public void setRight(View aView)
    {
        if (aView == _right) return;

        batchPropChanges();
        if (_right != null)
            removeChild(_right);
        firePropChange(Right_Prop, _right, _right = aView);
        if (_right != null)
            addChild(_right);

        fireBatchPropChanges();
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        BorderViewProxy viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        BorderViewProxy viewProxy = getViewProxy();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        BorderViewProxy viewProxy = getViewProxy();
        viewProxy.layoutView();
    }

    /**
     * Override to return ColViewProxy.
     */
    @Override
    protected BorderViewProxy getViewProxy()
    {
        return new BorderViewProxy(this, _center, _top, _right, _bottom, _left);
    }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft, double aH)
    {
        ViewProxy<?> proxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        return proxy.getBestWidth(aH);
    }

    /**
     * Returns the preferred height.
     */
    public static double getPrefHeight(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft, double aW)
    {
        ViewProxy<?> proxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        return proxy.getBestHeight(aW);
    }

    /**
     * Layout children.
     */
    public static void layout(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft)
    {
        BorderViewProxy viewProxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        viewProxy.layoutView();
    }
}