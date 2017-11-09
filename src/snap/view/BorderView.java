/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Insets;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A custom class.
 */
public class BorderView extends ParentView {
    
    // The layout
    BorderLayout _layout = new BorderLayout(this);

/**
 * Returns the center node.
 */
public View getCenter()  { return _layout.getCenter(); }

/**
 * Sets the center node.
 */
public void setCenter(View aView)
{
    View old = getCenter(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setCenter(aView);
    firePropChange("Center", old, aView);
}

/**
 * Returns the top node.
 */
public View getTop()  { return _layout.getTop(); }

/**
 * Sets the top node.
 */
public void setTop(View aView)
{
    View old = getTop(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setTop(aView);
    firePropChange("Top", old, aView);
}

/**
 * Returns the bottom node.
 */
public View getBottom()  { return _layout.getBottom(); }

/**
 * Sets the bottom node.
 */
public void setBottom(View aView)
{
    View old = getBottom(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setBottom(aView);
    firePropChange("Bottom", old, aView);
}

/**
 * Returns the left node.
 */
public View getLeft()  { return _layout.getLeft(); }

/**
 * Sets the left node.
 */
public void setLeft(View aView)
{
    View old = getLeft(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setLeft(aView);
    firePropChange("Left", old, aView);
}

/**
 * Returns the right node.
 */
public View getRight()  { return _layout.getRight(); }

/**
 * Sets the right node.
 */
public void setRight(View aView)
{
    View old = getRight(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setRight(aView);
    firePropChange("Right", old, aView);
}

/**
 * Returns whether layout should fill center when bigger than pref size.
 */
public boolean isFillCenter()  { return _layout.isFillCenter(); }

/**
 * Sets whether to fill center when bigger than pref size.
 */
public void setFillCenter(boolean aValue)  { _layout.setFillCenter(aValue); }
    
/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive Top, Left, Center, Bottom, Right
    if(getTop()!=null) { XMLElement top = new XMLElement("Top"); anElement.add(top);
        top.add(anArchiver.toXML(getTop(), this)); }
    if(getLeft()!=null) { XMLElement lft = new XMLElement("Left"); anElement.add(lft);
        lft.add(anArchiver.toXML(getLeft(), this)); }
    if(getCenter()!=null) { XMLElement ctr = new XMLElement("Center"); anElement.add(ctr);
        ctr.add(anArchiver.toXML(getCenter(), this)); }
    if(getBottom()!=null) { XMLElement btm = new XMLElement("Bottom"); anElement.add(btm);
        btm.add(anArchiver.toXML(getBottom(), this)); }
    if(getRight()!=null) { XMLElement rgt = new XMLElement("Right"); anElement.add(rgt);
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
    if(topView instanceof View) setTop((View)topView);
    
    // Unarchive Left
    XMLElement lft = anElement.get("Left"); lft = lft!=null && lft.getElementCount()>0? lft.getElement(0) : null;
    Object lftView = lft!=null? anArchiver.fromXML(lft, this) : null;
    if(lftView instanceof View) setLeft((View)lftView);
    
    // Unarchive Center
    XMLElement ctr = anElement.get("Center"); ctr = ctr!=null && ctr.getElementCount()>0? ctr.getElement(0) : null;
    Object ctrView = ctr!=null? anArchiver.fromXML(ctr, this) : null;
    if(ctrView instanceof View) setCenter((View)ctrView);
    
    // Unarchive Bottom
    XMLElement btm = anElement.get("Bottom"); btm = btm!=null && btm.getElementCount()>0? btm.getElement(0) : null;
    Object btmView = btm!=null? anArchiver.fromXML(btm, this) : null;
    if(btmView instanceof View) setBottom((View)btmView);
    
    // Unarchive Right
    XMLElement rgt = anElement.get("Right"); rgt = rgt!=null && rgt.getElementCount()>0? rgt.getElement(0) : null;
    Object rgtView = rgt!=null? anArchiver.fromXML(rgt, this) : null;
    if(rgtView instanceof View) setRight((View)rgtView);
}

/**
 * A Border layout.
 */
public static class BorderLayout extends ViewLayout {
    
    // The panes
    View              _top, _center, _bottom, _left, _right;
    
    // Whether to fill center
    boolean          _fillCenter = true;
    
    // Proxy nodes for horizontal nodes and center node
    HBoxProxy        _hproxy = new HBoxProxy();
    CenterProxy      _cproxy = new CenterProxy();
    
    // Workers: for center node, horizontal nodes and vertical nodes
    HBox.HBoxLayout  _hlay = new HBox.HBoxLayout(_hproxy);
    VBox.VBoxLayout  _vlay = new VBox.VBoxLayout(null);
    
    /** Creates a new Border layout for given parent. */
    public BorderLayout(ParentView aPar)  { setParent(aPar); _hlay.setFillHeight(true); _vlay.setFillWidth(true); }
    
    /** Returns the top. */
    public View getTop()  { return _top; }
    
    /** Sets the top. */
    public void setTop(View aView)  { _top = aView; }
    
    /** Returns the Center. */
    public View getCenter()  { return _center; }
    
    /** Sets the Center. */
    public void setCenter(View aView)  { _center = aView; }
    
    /** Returns the Bottom. */
    public View getBottom()  { return _bottom; }
    
    /** Sets the Bottom. */
    public void setBottom(View aView)  { _bottom = aView; }
    
    /** Returns the Left. */
    public View getLeft()  { return _left; }
    
    /** Sets the Left. */
    public void setLeft(View aView)  { _left = aView; }
    
    /** Returns the Right. */
    public View getRight()  { return _right; }
    
    /** Sets the Bottom. */
    public void setRight(View aView)  { _right = aView; }
    
    /** Returns whether layout should fill center when bigger than pref size. */
    public boolean isFillCenter()  { return _fillCenter; }
    
    /** Sets whether to fill center when bigger than pref size. */
    public void setFillCenter(boolean aValue)  { _fillCenter = aValue; }
        
    /** Returns preferred width of layout. */
    public double getPrefWidthImpl(double aH)  { return getVLay().getPrefWidth(aH); }
    
    /** Returns preferred height of layout. */
    public double getPrefHeightImpl(double aW)  { return getVLay().getPrefHeight(aW); }
    
    /** Performs layout. */
    public void layoutChildren()
    {
        // Do vertical layout (top, horiz-proxy, bottom)
        View par = getParent();
        View vkids[] = getVLay().getChildren();
        VBox.VBoxLayout.layout(par, vkids, null, true, 0);
        
        // Do horizontal layout (left, center-proxy, bottom)
        Insets hins = getInsets(par, _hproxy);
        HBox.HBoxLayout.layout(par, _hlay.getChildren(), hins, true, 0);
        
        // Do center layout
        Insets cins = getInsets(par, _cproxy);
        Box.BoxLayout.layout(par, _center, cins, _fillCenter, _fillCenter);
    }
    
    /** Returns a VBoxLayout with HBoxLayout to do real work. */
    public VBox.VBoxLayout getVLay()
    {
        _cproxy.relayoutParent(); _hproxy.relayoutParent();
        _hlay.setChildren(asArray(_left, _center!=null? _cproxy : null, _right));
        _vlay.setChildren(asArray(_top, _hproxy, _bottom)); _vlay.setParent(getParent());
        return _vlay;
    }
    
    /** HBoxProxy to model left, center, right of BorderView. */
    private class HBoxProxy extends ParentView {
        public HBoxProxy() { setGrowWidth(true); setGrowHeight(true); }
        protected double getPrefWidthImpl(double aH)  { return _hlay.getPrefWidth(aH); }
        protected double getPrefHeightImpl(double aW)  { return _hlay.getPrefHeight(aW); }
    }
    
    /** CenterProxy to model center as always grow width/height. */
    private class CenterProxy extends ParentView {
        public CenterProxy() { setGrowWidth(true); setGrowHeight(true); }
        protected double getPrefWidthImpl(double aH)  { return _center.getBestWidth(aH); }
        protected double getPrefHeightImpl(double aW)  { return _center.getBestHeight(aW); }
    }
    
    /** Returns array of non-null views from given view args. */
    private View[] asArray(View ... theViews)
    {
        int i = 0, len = 0; for(View n : theViews) if(n!=null) len++;
        View views[] = new View[len]; for(View n : theViews) if(n!=null) views[i++] = n;
        return views;
    }
    
    /** Returns insets of given view in given parent. */
    private Insets getInsets(View aPar, View aChild)
    {
        double right = aPar.getWidth() - aChild.getX() - aChild.getWidth();
        double bottom = aPar.getHeight() - aChild.getY() - aChild.getHeight();
        return new Insets(aChild.getY(), right, bottom, aChild.getX());
    }
}
    
}