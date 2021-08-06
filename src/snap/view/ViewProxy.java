package snap.view;
import snap.geom.*;
import snap.gfx.Border;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a view for the purpose of layout.
 */
public class ViewProxy<T extends View> extends Rect {

    // The original view (if available)
    private T  _view;

    // The children
    private ViewProxy<?>[]  _children;

    // The border
    private Border  _border;

    // The Padding
    private Insets  _padding;

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

    // Whether this proxy should fillWidth, fillHeight (common attributes for ParentView)
    private boolean  _fillWidth, _fillHeight;

    // The number of children that grow width/height
    private int _growWidthCount = -1, _growHeightCount = -1;

    // Constants for unset vars
    private static double UNSET_DOUBLE = -Float.MIN_VALUE;

    /**
     * Creates a new ViewProxy for given View.
     */
    public ViewProxy(View aView)
    {
        _view = (T) aView;
        width = height = UNSET_DOUBLE;
    }

    /**
     * Returns the view.
     */
    public T getView()  { return _view; }

    /**
     * Returns the width.
     */
    public double getWidth()
    {
        if (width != UNSET_DOUBLE) return width;
        width = _view != null ? _view.getWidth() : 0;
        return width;
    }

    /**
     * Returns the height.
     */
    public double getHeight()
    {
        if (height != UNSET_DOUBLE) return height;
        height = _view != null ? _view.getHeight() : 0;
        return height;
    }

    /**
     * Sets the bounds.
     */
    public void setBounds(Rect aRect)
    {
        setBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

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
            for (ViewProxy<?> child : _children)
                child.setBoundsInClient();
        else if (_view!=null)
            _view.setBounds(x, y, width, height);
    }

    /**
     * Returns the content.
     */
    public ViewProxy<?> getContent()
    {
        ViewProxy<?>[] children = getChildren();
        return children.length > 0 ? children[0] : null;
    }

    /**
     * Sets the content.
     */
    public void setContent(ViewProxy<?> aViewProxy)
    {
        ViewProxy<?>[] children = aViewProxy != null ? new ViewProxy[] { aViewProxy } : new ViewProxy[0];
        setChildren(children);
    }

    /**
     * Returns the number of children.
     */
    public int getChildCount()  { return getChildren().length; }

    /**
     * Returns the children.
     */
    public ViewProxy<?>[] getChildren()
    {
        if (_children != null || _view == null) return _children;
        ParentView par = (ParentView) _view;
        View[] children = par.getChildrenManaged();
        return _children = getProxies(children);
    }

    /**
     * Returns the children.
     */
    public void setChildren(ViewProxy<?>[] theProxies)
    {
        _children = theProxies;
    }

    /**
     * Returns the child for given component class.
     */
    public <E extends View> ViewProxy<E> getChildForClass(Class<E> aClass)
    {
        for (ViewProxy<?> proxy : getChildren())
            if (aClass.isInstance(proxy.getView()))
                return (ViewProxy<E>) proxy;
        return null;
    }

    /**
     * Returns the children for given component class.
     */
    public <E extends View> ViewProxy<E>[] getChildrenForClass(Class<E> aClass)
    {
        List<ViewProxy<E>> children = new ArrayList<>();
        for (ViewProxy<?> proxy : getChildren())
            if (aClass.isInstance(proxy.getView()))
                children.add((ViewProxy<E>) proxy);
        return children.toArray(new ViewProxy[0]);
    }

    /**
     * Returns the border.
     */
    public Border getBorder()
    {
        if (_border != null) return _border;
        return _border = _view != null ? _view.getBorder() : null;
    }

    /**
     * Sets the border.
     */
    public void setBorder(Border aBorder)
    {
        _border = aBorder;
    }

    /**
     * Returns the border insets.
     */
    public Insets getBorderInsets()
    {
        Border border = getBorder();
        return border != null ? border.getInsets() : Insets.EMPTY;
    }

    /**
     * Returns the padding.
     */
    public Insets getPadding()
    {
        if (_padding != null) return _padding;
        return _padding = _view != null ? _view.getPadding() : Insets.EMPTY;
    }

    /**
     * Sets the padding.
     */
    public void setPadding(Insets theIns)
    {
        _padding = theIns;
    }

    /**
     * Returns the insets.
     */
    public Insets getInsetsAll()
    {
        Insets ins = getPadding();
        Border border = getBorder();
        if (border != null)
            ins = Insets.add(ins, border.getInsets());
        return ins;
    }

    /**
     * Returns whether proxy is visible.
     */
    public boolean isVisible()
    {
        return _view!=null && _view.isVisible();
    }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()
    {
        if (_align != null) return _align;
        _align = _view != null ? _view.getAlign() : Pos.TOP_LEFT;
        return _align;
    }

    /**
     * Sets the alignment.
     */
    public void setAlign(Pos aPos)
    {
        _align = aPos;
    }

    /**
     * Returns the LeanX.
     */
    public HPos getLeanX()
    {
        if (_leanX != null) return _leanX;
        _leanX = _view != null ? _view.getLeanX() : null;
        return _leanX;
    }

    /**
     * Returns the LeanY.
     */
    public VPos getLeanY()
    {
        if (_leanY != null) return _leanY;
        _leanY = _view != null ? _view.getLeanY() : null;
        return _leanY;
    }

    /**
     * Returns the margin.
     */
    public Insets getMargin()
    {
        if (_margin != null) return _margin;
        _margin = _view != null ? _view.getMargin() : Insets.EMPTY;
        return _margin;
    }

    /**
     * Returns whether view grows width.
     */
    public boolean isGrowWidth()
    {
        if (_growX != null) return _growX;
        return _growX = _view != null && _view.isGrowWidth();
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
        if (_growY != null) return _growY;
        return _growY = _view != null && _view.isGrowHeight();
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
        if (_spacing != UNSET_DOUBLE) return _spacing;
        return _spacing = _view != null ? _view.getSpacing() : 0;
    }

    /**
     * Sets spacing.
     */
    public void setSpacing(double aValue)
    {
        _spacing = aValue;
    }

    /**
     * Returns whether view fills width.
     */
    public boolean isFillWidth()
    {
        return _fillWidth;
    }

    /**
     * Sets whether view fills width.
     */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }

    /**
     * Returns whether view fills height.
     */
    public boolean isFillHeight()
    {
        return _fillHeight;
    }

    /**
     * Sets whether view fills height.
     */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }

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
        if (_growWidthCount >= 0) return _growWidthCount;
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowWidth()) count++;
        return _growWidthCount = count;
    }

    /**
     * Returns the number of children that grow height.
     */
    public int getGrowHeightCount()
    {
        if (_growHeightCount >= 0) return _growHeightCount;
        int count = 0; for (ViewProxy c : getChildren()) if (c.isGrowHeight()) count++;
        return _growHeightCount = count;
    }

    /**
     * Returns the MaxX of last child with insets.
     */
    public double getChildrenMaxXLastWithInsets()
    {
        // Get LastChildMaxX, LastChildMarginRight
        ViewProxy[] children = getChildren();
        ViewProxy lastChild = children.length > 0 ? children[children.length-1] : null;
        double childMaxX = lastChild != null ? lastChild.getMaxX() : 0;
        double lastChildMarginRight = lastChild != null ? lastChild.getMargin().right : 0;

        // Return LastChildMaxX plus padding right
        Insets ins = getInsetsAll();
        double rightInset = Math.max(ins.right, lastChildMarginRight);
        return Math.ceil(childMaxX + rightInset);
    }

    /**
     * Returns the MaxX of children with insets.
     */
    public double getChildrenMaxXAllWithInsets()
    {
        ViewProxy[] children = getChildren();
        Insets ins = getInsetsAll();
        double childMaxX = ins.getLeft();
        for (ViewProxy child : children)
            childMaxX = Math.max(childMaxX, child.getMaxX());
        return Math.ceil(childMaxX + ins.right);
    }

    /**
     * Returns the MaxY of last child with insets.
     */
    public double getChildrenMaxYLastWithInsets()
    {
        // Get LastChildMaxY, LastChildMarginBottom
        ViewProxy[] children = getChildren();
        ViewProxy lastChild = children.length > 0 ? children[children.length-1] : null;
        double lastChildMaxY = lastChild != null ? lastChild.getMaxY() : 0;
        double lastChildMarginBottom = lastChild != null ? lastChild.getMargin().bottom : 0;

        // Return LastChildMaxY plus padding bottom
        Insets ins = getInsetsAll();
        double bottomInset = Math.max(ins.bottom, lastChildMarginBottom);
        return Math.ceil(lastChildMaxY + bottomInset);
    }

    /**
     * Returns the MaxY of children with insets.
     */
    public double getChildrenMaxYAllWithInsets()
    {
        ViewProxy[] children = getChildren();
        Insets ins = getInsetsAll();
        double childMaxY = ins.top;
        for (ViewProxy child : children)
            childMaxY = Math.max(childMaxY, child.getMaxY());
        return Math.ceil(childMaxY + ins.bottom);
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
        if (_view != null)
            return "ViewProxy : " + _view;
        return "ViewProxy { Bounds=" + getBounds() + " }";
    }

    /**
     * Returns a proxy for given view.
     */
    public static ViewProxy<?> getProxy(View aView)
    {
        return aView != null ? new ViewProxy<>(aView) : null;
    }

    /**
     * Returns an array of proxies for given array of views.
     */
    public static ViewProxy<?>[] getProxies(View ... theViews)
    {
        ViewProxy<?>[] proxies = new ViewProxy[theViews.length];
        for(int i = 0; i < theViews.length; i++)
            proxies[i] = new ViewProxy<>(theViews[i]);
        return proxies;
    }
}
