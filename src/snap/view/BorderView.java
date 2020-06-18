/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.util.*;
import java.util.Arrays;

/**
 * A View subclass to manage subviews along edges (top, bottom, left, right) and center.
 */
public class BorderView extends ParentView {
    
    // The panes
    private View  _top, _center, _bottom, _left, _right;
    
    // Whether to fill center
    private boolean  _fillCenter = true;
    
    /**
     * Returns the center node.
     */
    public View getCenter()  { return _center; }

    /**
     * Sets the center node.
     */
    public void setCenter(View aView)
    {
        View old = getCenter(); if (aView==old) return;
        if (old!=null) removeChild(old);
        if (aView!=null) addChild(aView);
        _center = aView;
        firePropChange("Center", old, aView);
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
        View old = getTop(); if (aView==old) return;
        if(old!=null) removeChild(old);
        if (aView!=null) addChild(aView);
        _top = aView;
        firePropChange("Top", old, aView);
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
        View old = getBottom(); if (aView==old) return;
        if (old!=null) removeChild(old);
        if (aView!=null) addChild(aView);
        _bottom = aView;
        firePropChange("Bottom", old, aView);
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
        View old = getLeft(); if (aView==old) return;
        if (old!=null) removeChild(old);
        if (aView!=null) addChild(aView);
        _left = aView;
        firePropChange("Left", old, aView);
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
        View old = getRight(); if (aView==old) return;
        if (old!=null) removeChild(old);
        if (aView!=null) addChild(aView);
        _right = aView;
        firePropChange("Right", old, aView);
    }

    /**
     * Returns whether layout should fill center when bigger than pref size.
     */
    public boolean isFillCenter()  { return _fillCenter; }

    /**
     * Sets whether to fill center when bigger than pref size.
     */
    public void setFillCenter(boolean aValue)  { _fillCenter = aValue; }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return getPrefWidth(this, _center, _top, _right, _bottom, _left, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, _center, _top, _right, _bottom, _left, aW); }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { layout(this, _center, _top, _right, _bottom, _left); }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive Top, Left, Center, Bottom, Right
        if (getTop()!=null) { XMLElement top = new XMLElement("Top"); anElement.add(top);
            top.add(anArchiver.toXML(getTop(), this)); }
        if (getLeft()!=null) { XMLElement lft = new XMLElement("Left"); anElement.add(lft);
            lft.add(anArchiver.toXML(getLeft(), this)); }
        if (getCenter()!=null) { XMLElement ctr = new XMLElement("Center"); anElement.add(ctr);
            ctr.add(anArchiver.toXML(getCenter(), this)); }
        if (getBottom()!=null) { XMLElement btm = new XMLElement("Bottom"); anElement.add(btm);
            btm.add(anArchiver.toXML(getBottom(), this)); }
        if (getRight()!=null) { XMLElement rgt = new XMLElement("Right"); anElement.add(rgt);
            rgt.add(anArchiver.toXML(getRight(), this)); }
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Top
        XMLElement top = anElement.get("Top"); top = top!=null && top.getElementCount()>0? top.getElement(0) : null;
        Object topView = top!=null? anArchiver.fromXML(top, this) : null;
        if (topView instanceof View)
            setTop((View)topView);

        // Unarchive Left
        XMLElement lft = anElement.get("Left"); lft = lft!=null && lft.getElementCount()>0? lft.getElement(0) : null;
        Object lftView = lft!=null? anArchiver.fromXML(lft, this) : null;
        if (lftView instanceof View)
            setLeft((View)lftView);

        // Unarchive Center
        XMLElement ctr = anElement.get("Center"); ctr = ctr!=null && ctr.getElementCount()>0? ctr.getElement(0) : null;
        Object ctrView = ctr!=null? anArchiver.fromXML(ctr, this) : null;
        if (ctrView instanceof View)
            setCenter((View)ctrView);

        // Unarchive Bottom
        XMLElement btm = anElement.get("Bottom"); btm = btm!=null && btm.getElementCount()>0? btm.getElement(0) : null;
        Object btmView = btm!=null? anArchiver.fromXML(btm, this) : null;
        if (btmView instanceof View)
            setBottom((View)btmView);

        // Unarchive Right
        XMLElement rgt = anElement.get("Right"); rgt = rgt!=null && rgt.getElementCount()>0? rgt.getElement(0) : null;
        Object rgtView = rgt!=null? anArchiver.fromXML(rgt, this) : null;
        if (rgtView instanceof View)
            setRight((View)rgtView);
    }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft, double aH)
    {
        ViewProxy proxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        return proxy.getBestWidth(aH);
    }

    /**
     * Returns the preferred height.
     */
    public static double getPrefHeight(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft, double aW)
    {
        ViewProxy proxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        return proxy.getBestHeight(aW);
    }

    /**
     * Layout children.
     */
    public static void layout(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft)
    {
        BorderViewProxy proxy = new BorderViewProxy(aPar, aCtr, aTop, aRgt, aBtm, aLft);
        layoutProxy(proxy);
        proxy.setBoundsInClient();
    }

    /**
     * Layout BorderViewProxy.
     */
    private static void layoutProxy(BorderViewProxy aPar)
    {
        ColView.layoutProxy(aPar, true);
        RowView.layoutProxy(aPar.rowProxy, true);
    }

    /**
     * A special ViewProxy subclass for BorderView
     */
    private static class BorderViewProxy extends ViewProxy {

        // The row proxy
        public RowViewProxy rowProxy;

        // Constructor
        BorderViewProxy(ParentView aPar, View aCtr, View aTop, View aRgt, View aBtm, View aLft)
        {
            super(aPar);

            // Create RowProxy
            rowProxy = new RowViewProxy(aCtr, aLft, aRgt);

            // Create proxy child array and create/add proxies
            ViewProxy colKids[] = new ViewProxy[3];
            int colKidCount = 0;
            if (aTop!=null) colKids[colKidCount++] = new ViewProxy(aTop);
            colKids[colKidCount++] = rowProxy;
            if (aBtm!=null) colKids[colKidCount++] = new ViewProxy(aBtm);

            // Set trimmed children
            setChildren(Arrays.copyOf(colKids, colKidCount));
        }

        @Override
        public double getBestWidth(double aH)
        {
            return ColView.getPrefWidthProxy(this, aH);
        }

        @Override
        public double getBestHeight(double aW)
        {
            return ColView.getPrefHeightProxy(this, aW);
        }

        /**
         * Override to update RowProxy children y value before normal version.
         */
        @Override
        public void setBoundsInClient()
        {
            for (ViewProxy child : rowProxy.getChildren())
                child.setY(child.getY() + rowProxy.getY());
            super.setBoundsInClient();
        }
    }

    /**
     * A RowViewProxy for BorderViewProxy.
     */
    private static class RowViewProxy extends ViewProxy {

        /** Constructor for BorderView Center, Right, Left. */
        RowViewProxy(View aCtr, View aLft, View aRgt)
        {
            super(null);

            // Create proxy child array and create/add proxies
            ViewProxy children[] = new ViewProxy[3];
            int count = 0;
            if (aLft!=null) children[count++] = new ViewProxy(aLft);
            if (aCtr!=null) {
                ViewProxy ctrProxy = children[count++] = new ViewProxy(aCtr);
                ctrProxy.setGrowWidth(true);
                ctrProxy.setGrowHeight(true);
            }
            if (aRgt!=null) children[count++] = new ViewProxy(aRgt);

            // Set trimmed children and GrowHeight
            setChildren(Arrays.copyOf(children, count));
            setGrowHeight(true);
        }

        @Override
        public double getBestWidth(double aH)  { return RowView.getPrefWidthProxy(this, aH); }

        @Override
        public double getBestHeight(double aW)  { return RowView.getPrefHeightProxy(this, aW); }
    }
}