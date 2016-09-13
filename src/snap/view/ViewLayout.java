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
 * Returns the children.
 */
public View[] getChildren()  { return _children!=null? _children : (_children=_parent.getChildrenManaged()); }

/**
 * Sets the children.
 */
public void setChildren(View theChildren[])  { _children = theChildren; }

/**
 * Returns the number of children.
 */
public int getChildCount()  { return _children!=null? _children.length : _parent.getChildCount(); }

/**
 * Returns the child at given index.
 */
public View getChild(int anIndex)  { return _children!=null? _children[anIndex] : _parent.getChild(anIndex); }

/**
 * Returns the node insets.
 */
public Insets getInsets()  { return _parent.getInsetsAll(); }

/**
 * Returns whether vertical or horizontal, based on parent.
 */
public boolean isHorizontal()  { return _parent.isHorizontal(); }
    
/**
 * Returns whether vertical or horizontal, based on parent.
 */
public boolean isVertical()  { return _parent.isVertical(); }
    
/**
 * Returns preferred width of layout.
 */
public abstract double getPrefWidth(double aH);

/**
 * Returns preferred height of layout.
 */
public abstract double getPrefHeight(double aW);

/**
 * Performs layout.
 */
public void layoutChildren()
{
    Insets ins = getInsets();
    double px = ins.left, py = ins.top;
    double pw = _parent.getWidth() - px - ins.right; if(pw<0) pw = 0;
    double ph = _parent.getHeight() - py - ins.bottom; if(ph<0) ph = 0;
    layoutChildren(px, py, pw, ph);
    _children = null;
}

/**
 * Performs layout.
 */
public void layoutChildren(double px, double py, double pw, double ph)  { }

/**
 * Returns the right size for a view.
 */
protected double getBestWidth(View aView, double aH)  { return Math.max(aView.getPrefWidth(aH), aView.getMinWidth()); }

/**
 * Returns the right size for a view.
 */
protected double getBestHeight(View aView, double aW) { return Math.max(aView.getPrefHeight(aW),aView.getMinHeight()); }
    
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
 * A centering layout.
 */
public static class BoxLayout extends ViewLayout {
    
    // The content
    View          _child;
    
    // Whether to fill width, height
    boolean       _fillWidth, _fillHeight;
    
    // Whether to scale to fix instead of sizing
    boolean       _scaleToFit;
    
    /** Creates a new BoxLayout for given parent. */
    public BoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the content. */
    public View getContent()  { return _child!=null? _child : _parent.getChildCount()>0? _parent.getChild(0) : null; }
    
    /** Sets the content. */
    public void setContent(View aView)  { _child = aView; }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns whether layout should scale instead of size. */
    public boolean isScaleToFit()  { return _scaleToFit; }
    
    /** Sets whether layout should scale instead of size. */
    public void setScaleToFit(boolean aValue)  { _scaleToFit = aValue; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        // If scaling and value provided, return value by aspect
        if(_scaleToFit && aH>=0 && (_fillWidth || aH<getPrefHeight(-1))) return aH*getAspect();
        
        // Otherwise, return pref size based on child
        Insets ins = getInsets(); View child = getContent();
        double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
        double w = child!=null? getBestWidth(child, h) : 0;
        return ins.left + w + ins.right;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        // If scaling and value provided, return value by aspect
        if(_scaleToFit && aW>=0 && (_fillWidth || aW<getPrefWidth(-1))) return aW/getAspect();
        
        // Otherwise, return pref size based on child
        Insets ins = getInsets(); View child = getContent();
        double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
        double h = child!=null? getBestHeight(child, w) : 0;
        return ins.top + h + ins.bottom;
    }
    
    /** Performs layout in content rect. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        // If scaling, return that layout
        if(_scaleToFit)  { layoutChildrenScale(px, py, pw, ph); return; }
        
        // Otherwise do normal layout
        View child = getContent();
        double cw = _fillWidth? pw : Math.min(getBestWidth(child, -1), pw);
        double ch = _fillHeight? ph : Math.min(getBestHeight(child, cw), ph);
        double dx = pw - cw, dy = ph - ch;
        double sx = child.getLeanX()!=null? getLeanX(child) : getAlignX(_parent);
        double sy = child.getLeanY()!=null? getLeanY(child) : getAlignY(_parent);
        child.setBounds(px+dx*sx, py+dy*sy, cw, ch);
    }
    
    /** Performs layout when ScaleToFit. */
    protected void layoutChildrenScale(double px, double py, double pw, double ph)
    {
        View child = getContent();
        double cw = child.getPrefWidth(), ch = child.getPrefHeight();
        if(pw<=0) pw = cw; if(ph<=0) ph = ch;
        
        // Set child bounds and scale
        child.setBounds((pw-cw)/2 + px, (ph-ch)/2 + py, cw, ch);
        double sx = pw/cw, sy = ph/ch, sc = Math.min(sx,sy); if(!_fillWidth) sc = Math.min(sc,1);
        child.setScaleX(sc); child.setScaleY(sc);
    }

    /** Returns the aspect of the content. */
    protected double getAspect()  { return getPrefWidth(-1)/getPrefHeight(-1); }
}

/**
 * A Horizontal box layout.
 */
public static class HBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill height
    boolean       _fillHeight;
    
    /** Creates a new HBox layout for given parent. */
    public HBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        Insets ins = getInsets(); View children[] = getChildren(); int ccount = children.length;
        double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
        double w = 0; for(View child : children) w += getBestWidth(child, h); if(ccount>1) w += (ccount-1)*_spacing;
        return w + ins.left + ins.right;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        Insets ins = getInsets(); View children[] = getChildren();
        double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
        double h = 0; for(View child : children) h = Math.max(h, getBestHeight(child, w));
        return h + ins.top + ins.bottom;
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
            double cw = getBestWidth(child, -1), cy = py;
            double ch = _fillHeight || child.isGrowHeight()? ph : Math.min(getBestHeight(child, cw), ph);
            if(ph>ch && !_fillHeight) { double ay2 = Math.max(ay,getLeanY(child)); cy += Math.round((ph-ch)*ay2); }
            cbnds[i] = new Rect(cx, cy, cw, ch); cx += cw + _spacing; if(child.isGrowWidth()) grow++;
        }
        
        // Calculate extra space (return if none)
        double extra = px + pw - (cx - _spacing);
        if(extra==0) { setBnds(children,cbnds); return; }
        
        // If grow shapes, add grow
        if(grow>0) { double dx = 0;
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
        setBnds(children, cbnds);
    }
}

/** Convenience to push bounds rects to nodes. */
private static final void setBnds(View c[], Rect r[])
{
    for(int i=0,iMax=c.length;i<iMax;i++) { View c2 = c[i]; Rect r2 = r[i]; c2.setBounds(r2); }
}

/**
 * A Vertical box layout.
 */
public static class VBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill width
    boolean       _fillWidth;
    
    /** Creates a new VBox layout for given parent. */
    public VBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        Insets ins = getInsets(); View children[] = getChildren();
        double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
        double w = 0; for(View child : children) w = Math.max(w, getBestWidth(child, h));
        return w + ins.left + ins.right;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        Insets ins = getInsets(); View children[] = getChildren(); int ccount = children.length;
        double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
        double h = 0; for(View child : children) h += getBestHeight(child, w);
        if(ccount>1) h += (ccount-1)*_spacing;
        return h + ins.top + ins.bottom;
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
            double cw = _fillWidth || child.isGrowWidth()? pw : Math.min(getBestWidth(child, -1), pw);
            double ch = getBestHeight(child, cw), cx = px;
            if(pw>cw) { double ax2 = Math.max(ax,getLeanX(child)); cx += Math.round((pw-cw)*ax2); }
            cbnds[i] = new Rect(cx,cy,cw,ch); cy += ch + _spacing; if(child.isGrowHeight()) grow++;
        }
        
        // Calculate extra space (return if none)
        double extra = py + ph - (cy - _spacing);
        if(extra==0) { setBnds(children, cbnds); return; }
        
        // If grow shapes, add grow
        if(grow>0) { double dy = 0;
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
        setBnds(children, cbnds);
    }
}

/**
 * A layout for HBox/VBox.
 */
public static class BoxesLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to with/height
    boolean       _fillOut;
    
    /** Creates a new HBox layout for given parent. */
    public BoxesLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill width/height. */
    public boolean isFillOut()  { return _fillOut; }
    
    /** Sets whether to fill width/height. */
    public void setFillOutt(boolean aValue)  { _fillOut = aValue; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        // Get insets, children, count
        Insets ins = getInsets(); View children[] = getChildren(); int ccount = children.length;
        
        // Handle vertical
        if(isVertical()) {
            double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
            double w = 0; for(View child : children) w = Math.max(w, getBestWidth(child, h));
            return w + ins.left + ins.right;
        }
        
        // Handle horizontal
        double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
        double w = 0; for(View child : children) w += getBestWidth(child, h); if(ccount>1) w += (ccount-1)*_spacing;
        return w + ins.left + ins.right;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        // Get insets, children, count
        Insets ins = getInsets(); View children[] = getChildren(); int ccount = children.length;
        
        // Handle horizontal
        if(!isVertical()) {
            double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
            double h = 0; for(View child : children) h = Math.max(h, getBestHeight(child, w));
            return h + ins.top + ins.bottom;
        }
        
        // Handle vertical
        double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
        double h = 0; for(View child : children) h += getBestHeight(child, w); if(ccount>1) h += (ccount-1)*_spacing;
        return h + ins.top + ins.bottom;
    }
        
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        if(isVertical()) layoutChildrenV(px,py,pw,ph);
        else layoutChildrenH(px,py,pw,ph);
    }
    
    /** Performs layout. */
    public void layoutChildrenH(double px, double py, double pw, double ph)
    {
        View children[] = getChildren(); int ccount = children.length; if(ccount==0) return;
        Rect cbnds[] = new Rect[children.length];
        double cx = px, ay = getAlignY(_parent);
        boolean dividers = false;
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double cw = getBestWidth(child, -1), cy = py;
            double ch = _fillOut || child.isGrowHeight()? ph : Math.min(getBestHeight(child, cw), ph);
            if(ph>ch && !_fillOut) { double ay2 = Math.max(ay,getLeanY(child)); cy += Math.round((ph-ch)*ay2); }
            cbnds[i] = new Rect(cx, cy, cw, ch); cx += cw + _spacing;
            if(child instanceof Divider) dividers = true; if(child.isGrowWidth()) grow++;
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
        
        // If dividers are present, adjust layouts
        if(dividers) for(View child : children) if(child instanceof Divider)
            ((Divider)child).adjustLayouts(children, cbnds);

        // Reset children bounds
        setBnds(children, cbnds);
    }
    
    /** Performs layout in content rect. */
    public void layoutChildrenV(double px, double py, double pw, double ph)
    {
        View children[] = getChildren(); int ccount = children.length; if(ccount==0) return;
        Rect cbnds[] = new Rect[children.length];
        double cy = py, ax = getAlignX(_parent);
        boolean dividers = false;
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double cw = _fillOut || child.isGrowWidth()? pw : Math.min(getBestWidth(child, -1), pw);
            double ch = getBestHeight(child, cw), cx = px;
            if(pw>cw && !_fillOut) { double ax2 = Math.max(ax,getLeanX(child)); cx += Math.round((pw-cw)*ax2); }
            cbnds[i] = new Rect(cx,cy,cw,ch); cy += ch + _spacing;
            if(child instanceof Divider) dividers = true; if(child.isGrowHeight()) grow++;
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

        // If dividers are present, adjust layouts
        if(dividers) for(View child : children) if(child instanceof Divider)
            ((Divider)child).adjustLayouts(children, cbnds);

        // Reset children bounds
        setBnds(children, cbnds);
    }
}

/**
 * A Border layout.
 */
public static class BorderLayout extends ViewLayout {
    
    // The panes
    View        _top, _center, _bottom, _left, _right;
    
    // Whether to fill center
    boolean     _fillCenter = true;
    
    // Proxy nodes for horizontal nodes and center node
    HBoxProxy     _hproxy = new HBoxProxy();
    CenterProxy   _cproxy = new CenterProxy();
    
    // Workers: for center node, horizontal nodes and vertical nodes
    HBoxLayout    _hlay = new HBoxLayout(_hproxy);
    VBoxLayout    _vlay = new VBoxLayout(null);
    BoxLayout     _clay = new BoxLayout(null);
    
    /** Creates a new Border layout for given parent. */
    public BorderLayout(ParentView aPar)
    {
        setParent(aPar); _hlay.setFillHeight(true); _vlay.setFillWidth(true);
    }
    
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
    public double getPrefWidth(double aH)  { return getVLay().getPrefWidth(aH); }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)  { return getVLay().getPrefHeight(aW); }
    
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        getVLay().layoutChildren(px, py, pw, ph);
        _hlay.layoutChildren(_hproxy.getX(), _hproxy.getY(), _hproxy.getWidth(), _hproxy.getHeight());
        
        if(_center==null) return;
        _clay.setContent(_center); _clay.setParent(getParent());
        _clay.setFillWidth(_fillCenter); _clay.setFillHeight(_fillCenter);
        _clay.layoutChildren(_cproxy.getX(), _cproxy.getY(), _cproxy.getWidth(), _cproxy.getHeight());
    }
    
    /** Returns a VBoxLayout with HBoxLayout to do real work. */
    public VBoxLayout getVLay()
    {
        View hkids[] = asArray(_left, _center!=null? _cproxy : null, _right);
        View vkids[] = asArray(_top, _hproxy, _bottom);
        _hlay.setChildren(hkids); _vlay.setChildren(vkids); _vlay.setParent(getParent());
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
        protected double getPrefWidthImpl(double aH)  { return _center.getPrefWidth(aH); }
        protected double getPrefHeightImpl(double aW)  { return _center.getPrefHeight(aW); }
    }
    
    /** Returns an array of non-null nodes from given nodes list. */
    private View[] asArray(View ... theNodes)
    {
        int i = 0, len = 0; for(View n : theNodes) if(n!=null) len++;
        View nodes[] = new View[len]; for(View n : theNodes) if(n!=null) nodes[i++] = n;
        return nodes;
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
    public double getPrefWidth(double aH)
    {
        Insets ins = getInsets(); View children[] = getChildren();
        double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
        double w = 0; for(View child : children) w = Math.max(w, getBestWidth(child, h));
        return ins.left + w + ins.right;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        Insets ins = getInsets(); View children[] = getChildren();
        double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
        double h = 0; for(View child : children) h = Math.max(h, getBestHeight(child, w));
        return ins.top + h + ins.bottom;
    }
    
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        double ay = getAlignY(_parent), ax = getAlignX(_parent);
        
        // Layout children
        View children[] = getChildren();
        for(View child : children) {
            double cw = _fillWidth || child.isGrowWidth()? pw : Math.min(getBestWidth(child, -1), pw);
            double ch = _fillHeight || child.isGrowHeight()? ph : Math.min(getBestHeight(child, -1), ph);
            double cx = px, cy = py;
            if(pw>cw) { double ax2 = child.getLeanX()!=null? getLeanX(child) : ax;
                cx += Math.round((pw-cw)*ax2); }
            if(ph>ch) { double ay2 = child.getLeanY()!=null? getLeanY(child) : ay;
                cy += Math.round((ph-ch)*ay2); }
            child.setBounds(cx, cy, cw, ch);
        }
    }
}

/**
 * A Spring layout.
 */
public static class SpringLayout extends ViewLayout {
    
    // The last set size
    double _ow, _oh;
    
    /** Creates a new SpringLayout for given parent. */
    public SpringLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns spring info for child. */
    protected SpringInfo getSpringInfo(View aChild)  { return (SpringInfo)aChild.getProp("SpringInfo"); }
    
    /** Adds spring info for child. */
    protected void addSpringInfo(View aChild)
    {
        double x = aChild.getX(), y = aChild.getY(), w = aChild.getWidth(), h = aChild.getHeight();
        SpringInfo sinfo = new SpringInfo(x,y,w,h,_parent.getWidth(),_parent.getHeight());
        aChild.setProp("SpringInfo", sinfo); _ow = _oh = 0;
    }
    
    /** Removes spring info for child. */
    protected void removeSpringInfo(View aChild)  { aChild.setProp("SpringInfo", null); _ow = _oh = 0; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)  { return _parent.getWidth(); }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)  { return _parent.getHeight(); }

    /** Override to perform layout. */
    public void layoutChildren()
    {
        View children[] = getChildren();
        double pw = _parent.getWidth(), ph = _parent.getHeight(); if(pw==_ow && ph==_oh) return;
        for(View child : children) layoutChild(child, pw, ph);
        _ow = pw; _oh = ph;
    }
    
    /** Returns the child rects for given parent height. */
    protected void layoutChild(View aChild, double newPW, double newPH)
    {
        SpringInfo sinfo = getSpringInfo(aChild);
        String asize = aChild.getAutosizing();
        double oldPW = sinfo.pwidth, oldPH = sinfo.pheight;
        boolean lms = asize.charAt(0)=='~', ws = asize.charAt(1)=='~', rms = asize.charAt(2)=='~';
        boolean tms = asize.charAt(4)=='~', hs = asize.charAt(5)=='~', bms = asize.charAt(6)=='~';
        double x1 = sinfo.x, y1 = sinfo.y, w1 = sinfo.width, h1 = sinfo.height;
        double sw = (lms? x1 : 0) + (ws? w1 : 0) + (rms? oldPW - (x1 + w1) : 0), dw = newPW - oldPW;
        double sh = (tms? y1 : 0) + (hs? h1 : 0) + (bms? oldPH - (y1 + h1) : 0), dh = newPH - oldPH;
        
        // Calculate new bounds and set
        double x2 = (!lms || sw==0)? x1 : (x1 + dw*x1/sw);
        double y2 = (!tms || sh==0)? y1 : (y1 + dh*y1/sh);
        double w2 = (!ws || sw==0)? w1 : (w1 + dw*w1/sw);
        double h2 = (!hs || sh==0)? h1 : (h1 + dh*h1/sh);
        aChild.setBounds(x2,y2,w2,h2);
    }
}
    
/**
 * A class to hold info for a spring child.
 */
public static class SpringInfo {
    
    // The bounds and original parent width/height
    double x, y, width, height, pwidth, pheight;
    
    /** Creates a SpringInfo. */
    public SpringInfo(double aX, double aY, double aW, double aH, double aPW, double aPH) {
        x = aX; y = aY; width = aW; height = aH; pwidth = aPW; pheight = aPH; }

    // Sets the rect
    public void setRect(double aX, double aY, double aW, double aH)  { x = aX; y = aY; width = aW; height = aH; }
}

}