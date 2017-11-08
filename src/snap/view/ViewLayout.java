/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A custom class.
 */
public abstract class ViewLayout {

    // The parent
    ParentView       _parent;
    
    // The children
    View             _children[];
    
/**
 * Returns the parent.
 */
public ParentView getParent()  { return _parent; }

/**
 * Sets the parent.
 */
public void setParent(ParentView aPar)  { _parent = aPar; }

/**
 * Returns the (first) child.
 */
public View getChild()  { View chdrn[] = getChildren(); return chdrn!=null && chdrn.length>0? chdrn[0] : null; }

/**
 * Sets the child.
 */
public void setChild(View theChild)  { setChildren(new View[] { theChild }); }

/**
 * Returns the children.
 */
public View[] getChildren()  { return _children!=null? _children : _parent.getChildrenManaged(); }

/**
 * Sets the children.
 */
public void setChildren(View theChildren[])  { _children = theChildren; }

/**
 * Returns the number of children.
 */
public int getChildCount()  { return getChildren().length; }

/**
 * Returns the node insets.
 */
public Insets getInsets()  { return _parent.getInsetsAll(); }

/**
 * Returns whether vertical or horizontal, based on parent.
 */
public boolean isVertical()  { return _parent.isVertical(); }
    
/**
 * Returns preferred width of layout, including insets.
 */
public double getPrefWidth(double aH)
{
    Insets ins = getInsets(); if(getChildCount()==0) return ins.left + ins.right;
    double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
    double w = getPrefWidthImpl(h);
    return ins.left + w + ins.right;
}

/**
 * Returns preferred width of layout, excluding insets.
 */
protected double getPrefWidthImpl(double aH)  { return 0; }

/**
 * Returns preferred height of layout, including insets.
 */
public double getPrefHeight(double aW)
{
    Insets ins = getInsets(); if(getChildCount()==0) return ins.top + ins.bottom;
    double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
    double h = getPrefHeightImpl(w);
    return ins.top + h + ins.bottom;
}

/**
 * Returns preferred height of layout, excluding insets.
 */
protected double getPrefHeightImpl(double aW)  { return 0; }

/**
 * Performs layout.
 */
public void layoutChildren()
{
    if(getChildCount()==0) return;
    Insets ins = getInsets();
    double px = ins.left, py = ins.top;
    double pw = _parent.getWidth() - px - ins.right; if(pw<0) pw = 0;
    double ph = _parent.getHeight() - py - ins.bottom; if(ph<0) ph = 0;
    if(pw>0 && ph>0)
        layoutChildren(px, py, pw, ph);
}

/**
 * Performs layout.
 */
public void layoutChildren(double px, double py, double pw, double ph)  { }

/**
 * Returns the align x factor.
 */
protected double getAlignX(View aView)
{
    HPos hp = aView.getAlign().getHPos(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the align y factor.
 */
protected double getAlignY(View aView)
{
    VPos vp = aView.getAlign().getVPos(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean x factor.
 */
protected double getLeanX(View aView)
{
    HPos hp = aView.getLeanX(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean y factor.
 */
protected double getLeanY(View aView)
{
    VPos vp = aView.getLeanY(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}
    
/**
 * Convenience to push bounds rects to nodes.
 */
private static final void setBounds(View c[], Rect r[])
{
    for(int i=0,iMax=c.length;i<iMax;i++) { View c2 = c[i]; Rect r2 = r[i]; c2.setBounds(r2); }
}

/**
 * A layout for HBox/VBox.
 */
public static class BoxesLayout extends ViewLayout {
    
    // The real layouts
    HBoxLayout       _hbox = new HBoxLayout(null);
    VBoxLayout       _vbox = new VBoxLayout(null);
    
    /** Creates a new HBox layout for given parent. */
    public BoxesLayout(ParentView aPar)  { setParent(aPar); _hbox.setParent(aPar); _vbox.setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _hbox.getSpacing(); }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _hbox.setSpacing(aValue); _vbox.setSpacing(aValue); }
    
    /** Returns whether layout should fill width/height. */
    public boolean isFillOut()  { return _hbox.isFillHeight(); }
    
    /** Sets whether to fill width/height. */
    public void setFillOut(boolean aValue)  { _hbox.setFillHeight(aValue); _vbox.setFillWidth(aValue); }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        if(isVertical()) return _vbox.getPrefWidth(aH);
        return _hbox.getPrefWidth(aH);
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        if(isVertical()) return _vbox.getPrefHeight(aW);
        return _hbox.getPrefHeight(aW);
    }
        
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        if(isVertical()) _vbox.layoutChildren(px,py,pw,ph);
        else _hbox.layoutChildren(px,py,pw,ph);
    }
}

/**
 * A Horizontal box layout.
 */
public static class HBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to with/height
    boolean       _fillOut;
    
    /** Creates a new HBox layout for given parent. */
    public HBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillOut; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillOut = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren(); int ccount = children.length;
        double w = 0; for(View child : children) w += child.getBestWidth(aH); if(ccount>1) w += (ccount-1)*_spacing;
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren();
        double h = 0; for(View child : children) h = Math.max(h, child.getBestHeight(aW));
        return h;
    }
        
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        View children[] = getChildren(); int ccount = children.length; if(ccount==0) return;
        Rect cbnds[] = new Rect[children.length];
        double cx = px, ay = getAlignY(_parent);
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double ch = _fillOut || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
            double cw = child.getBestWidth(ch), cy = py;
            if(ph>ch && !_fillOut) { double ay2 = Math.max(ay,getLeanY(child)); cy += Math.round((ph-ch)*ay2); }
            cbnds[i] = new Rect(cx, cy, cw, ch); cx += cw + _spacing;
            if(child.isGrowWidth()) grow++;
        }
        
        // Calculate extra space
        double extra = px + pw - (cx - _spacing);
        
        // If grow shapes, add grow
        if(extra!=0 && grow>0) { double dx = 0;
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                if(dx!=0) cbnd.setX(cbnd.getX() + dx);
                if(child.isGrowWidth()) { cbnd.setWidth(cbnd.getWidth()+extra/grow); dx += extra/grow; }
            }
        }
        
        // Otherwise, check for horizontal alignment/lean shift
        else if(extra>0) {
            double ax = getAlignX(_parent);
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                ax = Math.max(ax, getLeanX(child)); double dx = extra*ax;
                if(dx>0) cbnd.setX(cbnd.getX() + extra*ax);
            }
        }
        
        // Reset children bounds
        setBounds(children, cbnds);
    }
}

/**
 * A Vertical box layout.
 */
public static class VBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to with/height
    boolean       _fillOut;
    
    /** Creates a new VBox layout for given parent. */
    public VBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillOut; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillOut = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren();
        double w = 0; for(View child : children) w = Math.max(w, child.getBestWidth(aH));
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren(); int ccount = children.length;
        double h = 0; for(View child : children) h += child.getBestHeight(aW); if(ccount>1) h += (ccount-1)*_spacing;
        return h;
    }
        
    /** Performs layout in content rect. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        View children[] = getChildren(); int ccount = children.length; if(ccount==0) return;
        Rect cbnds[] = new Rect[children.length];
        double cy = py, ax = getAlignX(_parent);
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double cw = _fillOut || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
            double ch = child.getBestHeight(cw), cx = px;
            if(pw>cw && !_fillOut) { double ax2 = Math.max(ax,getLeanX(child)); cx += Math.round((pw-cw)*ax2); }
            cbnds[i] = new Rect(cx,cy,cw,ch); cy += ch + _spacing;
            if(child.isGrowHeight()) grow++;
        }
        
        // Calculate extra space (return if none)
        double extra = py + ph - (cy - _spacing);
        
        // If grow shapes, add grow
        if(extra!=0 && grow>0) { double dy = 0;
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                if(dy!=0) cbnd.setY(cbnd.getY() + dy);
                if(child.isGrowHeight()) { cbnd.setHeight(cbnd.getHeight()+extra/grow); dy += extra/grow; }
            }
        }
        
        // Otherwise, check for vertical alignment/lean shift
        else if(extra>0) {
            double ay = getAlignY(_parent);
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                ay = Math.max(ay, getLeanY(child)); double dy = extra*ay;
                if(dy>0) cbnd.setY(cbnd.getY() + extra*ay);
            }
        }

        // Reset children bounds
        setBounds(children, cbnds);
    }
}

/**
 * A Stack layout.
 */
public static class StackLayout extends ViewLayout {
    
    // Whether to fill width/height
    boolean       _fillWidth, _fillHeight;
    
    /** Creates a new StackLayout for given parent. */
    public StackLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren();
        double w = 0; for(View child : children) w = Math.max(w, child.getBestWidth(aH));
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren();
        double h = 0; for(View child : children) h = Math.max(h, child.getBestHeight(aW));
        return h;
    }
    
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        double ay = getAlignY(_parent), ax = getAlignX(_parent);
        
        // Layout children
        View children[] = getChildren();
        for(View child : children) {
            double cw = _fillWidth || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
            double ch = _fillHeight || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
            double cx = px, cy = py;
            if(pw>cw) { double ax2 = child.getLeanX()!=null? getLeanX(child) : ax;
                cx += Math.round((pw-cw)*ax2); }
            if(ph>ch) { double ay2 = child.getLeanY()!=null? getLeanY(child) : ay;
                cy += Math.round((ph-ch)*ay2); }
            child.setBounds(cx, cy, cw, ch);
        }
    }
}

}