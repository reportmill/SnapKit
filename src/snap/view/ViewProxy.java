package snap.view;
import snap.geom.*;

/**
 * This class represents a view for the purpose of layout.
 */
public class ViewProxy {

    // The original view (if available)
    private View  _view;

    // The X/Y
    private double  _x, _y;

    // The size
    private double  _width = UNSET_DOUBLE, _height = UNSET_DOUBLE;

    // The children
    private ViewProxy  _children[];

    // The insets
    private Insets  _insets;

    // The alignment
    private Pos  _align;

    // The horizontal position this view would prefer to take when inside a pane
    private HPos  _leanX;

    // The vertical position this view would prefer to take when inside a pane
    private VPos  _leanY;

    // The margin
    private Insets  _margin;

    // Whether view should grow in X or Y
    private Boolean  _growX, _growY;

    // Spacing
    private double  _spacing = UNSET_DOUBLE;

    // The number of children that grow width/height
    private int _growWidthCount = -1, _growHeightCount = -1;

    // Constants for unset vars
    private static double UNSET_DOUBLE = -Float.MIN_VALUE;

    /**
     * Creates a new ViewProxy for given View.
     */
    public ViewProxy(View aView)
    {
        _view = aView;
    }

    /**
     * Returns the X.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X.
     */
    public void setX(double aValue)
    {
        _x = aValue;
    }

    /**
     * Returns the Y.
     */
    public double getY()  { return _y; }

    /**
     * Sets the Y.
     */
    public void setY(double aValue)
    {
        _y = aValue;
    }

    /**
     * Returns the width.
     */
    public double getWidth()
    {
        if(_width!=UNSET_DOUBLE) return _width;
        _width = _view!=null ? _view.getWidth() : 0;
        return _width;
    }

    /**
     * Sets the width.
     */
    public void setWidth(double aValue)
    {
        _width = aValue;
    }

    /**
     * Returns the height.
     */
    public double getHeight()
    {
        if(_height!=UNSET_DOUBLE) return _height;
        _height = _view!=null ? _view.getHeight() : 0;
        return _height;
    }

    /**
     * Sets the height.
     */
    public void setHeight(double aValue)
    {
        _height = aValue;
    }

    /**
     * Sets the size.
     */
    public void setSize(double aW, double aH)
    {
        setWidth(aW);
        setHeight(aH);
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()  { return new Rect(getX(), getY(), getWidth(), getHeight()); }

    /**
     * Sets the bounds.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX); setY(aY); setWidth(aW); setHeight(aH);
    }

    /**
     * Returns the Max X.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the Max Y.
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Sets the bounds back in the client.
     */
    public void setBoundsInClient()
    {
        if (_children!=null)
            for (ViewProxy child : _children)
                child.setBoundsInClient();
        else if (_view!=null)
            _view.setBounds(_x, _y, _width, _height);
    }

    /**
     * Returns the number of children.
     */
    public int getChildCount()  { return getChildren().length; }

    /**
     * Returns the children.
     */
    public ViewProxy[] getChildren()
    {
        if(_children!=null || _view==null) return _children;
        ParentView par = (ParentView)_view;
        View children[] = par.getChildrenManaged();
        return _children = getProxies(children);
    }

    /**
     * Returns the children.
     */
    public void setChildren(ViewProxy theProxies[])
    {
        _children = theProxies;
    }

    /**
     * Sets the insets.
     */
    public void setInsets(Insets theIns)
    {
        _insets = theIns;
    }

    /**
     * Returns the insets.
     */
    public Insets getInsetsAll()
    {
        if(_insets!=null) return _insets;
        _insets = _view!=null? _view.getInsetsAll() : Insets.EMPTY;
        return _insets;
    }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()
    {
        if(_align!=null) return _align;
        _align = _view!=null? _view.getAlign() : Pos.TOP_LEFT;
        return _align;
    }

    /**
     * Returns the LeanX.
     */
    public HPos getLeanX()
    {
        if(_leanX!=null) return _leanX;
        _leanX = _view!=null ? _view.getLeanX() : null;
        return _leanX;
    }

    /**
     * Returns the LeanY.
     */
    public VPos getLeanY()
    {
        if(_leanY!=null) return _leanY;
        _leanY = _view!=null ? _view.getLeanY() : null;
        return _leanY;
    }

    /**
     * Returns the margin.
     */
    public Insets getMargin()
    {
        if(_margin!=null) return _margin;
        _margin = _view!=null? _view.getMargin() : Insets.EMPTY;
        return _margin;
    }

    /**
     * Returns whether view grows width.
     */
    public boolean isGrowWidth()
    {
        if(_growX!=null) return _growX;
        return _growX = _view!=null && _view.isGrowWidth();
    }

    /**
     * Sets whether view grows width.
     */
    public void setGrowWidth(boolean aValue)  { _growX = aValue; }

    /**
     * Returns whether view grows height.
     */
    public boolean isGrowHeight()
    {
        if(_growY!=null) return _growY;
        return _growY = _view!=null && _view.isGrowHeight();
    }

    /**
     * Sets whether view grows height.
     */
    public void setGrowHeight(boolean aValue)  { _growY = aValue; }

    /**
     * Returns spacing.
     */
    public double getSpacing()
    {
        if(_spacing!=UNSET_DOUBLE) return _spacing;
        return _spacing = _view!=null ? _view.getSpacing() : 0;
    }

    /**
     * Sets spacing.
     */
    public void setSpacing(double aValue)
    {
        _spacing = aValue;
    }

    /**
     * Returns the best width.
     */
    public double getBestWidth(double aH)
    {
        return _view.getBestWidth(aH);
    }

    /**
     * Returns the best height.
     */
    public double getBestHeight(double aW)
    {
        return _view.getBestHeight(aW);
    }

    /**
     * Returns the number of children that grow width.
     */
    public int getGrowWidthCount()
    {
        if (_growWidthCount>=0) return _growWidthCount;
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowWidth()) count++;
        return _growWidthCount = count;
    }

    /**
     * Returns the number of children that grow height.
     */
    public int getGrowHeightCount()
    {
        if (_growHeightCount>=0) return _growHeightCount;
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowHeight()) count++;
        return _growHeightCount = count;
    }

    /**
     * Returns the MaxX of last child with insets.
     */
    public double getChildrenMaxXLastWithInsets()
    {
        ViewProxy children[] = getChildren();
        Insets ins = getInsetsAll();
        double maxX = children.length>0 ? children[children.length-1].getMaxX() : 0;
        return maxX + ins.getRight();
    }

    /**
     * Returns the MaxX of children with insets.
     */
    public double getChildrenMaxXAllWithInsets()
    {
        ViewProxy children[] = getChildren();
        Insets ins = getInsetsAll();
        double maxX = ins.getLeft();
        for (ViewProxy child : children) maxX = Math.max(maxX, child.getMaxX());
        double pw = maxX + ins.getRight();
        pw = Math.round(pw);
        return pw;
    }

    /**
     * Returns the MaxY of last child with insets.
     */
    public double getChildrenMaxYLastWithInsets()
    {
        ViewProxy children[] = getChildren();
        Insets ins = getInsetsAll();
        double maxY = children.length>0 ? children[children.length-1].getMaxY() : 0;
        return maxY + ins.getBottom();
    }

    /**
     * Returns the MaxY of children with insets.
     */
    public double getChildrenMaxYAllWithInsets()
    {
        ViewProxy children[] = getChildren();
        Insets ins = getInsetsAll();
        double maxY = ins.getTop();
        for (ViewProxy child : children) maxY = Math.max(maxY, child.getMaxY());
        double ph = maxY + ins.getBottom();
        ph = Math.round(ph);
        return ph;
    }

    /**
     * Returns the align x factor.
     */
    public double getAlignXAsDouble()  { return ViewUtils.getAlignX(getAlign()); }

    /**
     * Returns the align y factor.
     */
    public double getAlignYAsDouble()  { return ViewUtils.getAlignY(getAlign()); }

    /**
     * Returns the lean x factor.
     */
    public double getLeanXAsDouble()  { return ViewUtils.getAlignX(getLeanX()); }

    /**
     * Returns the lean y factor.
     */
    public double getLeanYAsDouble()  { return ViewUtils.getAlignY(getLeanY()); }

    @Override
    public String toString()
    {
        if (_view!=null)
            return "ViewProxy : " + _view.toString();
        return "ViewProxy { Bounds=" + getBounds() + " }";
    }

    /**
     * Returns a proxy for given view.
     */
    public static ViewProxy getProxy(View aView)  { return new ViewProxy(aView); }

    /**
     * Returns an array of proxies for given array of views.
     */
    public static ViewProxy[] getProxies(View ... theViews)
    {
        ViewProxy proxies[] = new ViewProxy[theViews.length];
        for(int i=0;i<theViews.length;i++)
            proxies[i] = new ViewProxy(theViews[i]);
        return proxies;
    }
}
