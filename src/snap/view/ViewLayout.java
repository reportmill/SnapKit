package snap.view;
import snap.geom.*;
import snap.gfx.Border;
import snap.util.ArrayUtils;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a view for the purpose of layout.
 */
public abstract class ViewLayout<T extends View> extends Rect {

    // The original view (if available)
    protected T _view;

    // The children
    private ViewLayout<?>[]  _children;

    // The border
    private Border  _border;

    // The margin
    private Insets  _margin;

    // The Padding
    private Insets  _padding;

    // The alignment
    private Pos  _align;

    // The horizontal position this view would prefer to take when inside a pane
    private HPos  _leanX;

    // The vertical position this view would prefer to take when inside a pane
    private VPos  _leanY;

    // Whether view should grow in X or Y
    private Boolean  _growX, _growY;

    // Spacing
    private double  _spacing = UNSET_DOUBLE;

    // Whether this proxy should fillWidth, fillHeight, hugging (common attributes for ParentView)
    private boolean  _fillWidth, _fillHeight, _hugging;

    // The last calculated pref width and height
    private double _prefW = -1, _prefH = -1;

    // Constants for unset vars
    private static double UNSET_DOUBLE = -Float.MIN_VALUE;

    /**
     * Constructor for given View.
     */
    public ViewLayout(View aView)
    {
        _view = (T) aView; assert _view != null;
        x = y = width = height = UNSET_DOUBLE;
    }

    /**
     * Returns the view.
     */
    public T getView()  { return _view; }

    /**
     * Returns the x.
     */
    public double getX()
    {
        if (x != UNSET_DOUBLE) return x;
        return x = _view.getX();
    }

    /**
     * Returns the y.
     */
    public double getY()
    {
        if (y != UNSET_DOUBLE) return y;
        return y = _view.getY();
    }

    /**
     * Returns the width.
     */
    public double getWidth()
    {
        if (width != UNSET_DOUBLE) return width;
        return width = _view.getWidth();
    }

    /**
     * Returns the height.
     */
    public double getHeight()
    {
        if (height != UNSET_DOUBLE) return height;
        return height = _view.getHeight();
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
        if (_children != null)
            for (ViewLayout<?> child : _children)
                child.setBoundsInView();
        else setBoundsInView();
    }

    /**
     * Sets the bounds back in the view.
     */
    private void setBoundsInView()
    {
        _view.setBounds(x, y, width, height);
    }

    /**
     * Returns the content.
     */
    public ViewLayout<?> getContent()
    {
        ViewLayout<?>[] children = getChildren();
        return children.length > 0 ? children[0] : null;
    }

    /**
     * Sets the content.
     */
    public void setContent(ViewLayout<?> aViewLayout)
    {
        setChildren(aViewLayout != null ? List.of(aViewLayout) : Collections.emptyList());
    }

    /**
     * Returns the number of children.
     */
    public int getChildCount()  { return getChildren().length; }

    /**
     * Returns the children.
     */
    public ViewLayout<?>[] getChildren()
    {
        if (_children != null) return _children;
        ParentView par = (ParentView) _view;
        View[] children = par.getChildrenManaged();
        return _children = ArrayUtils.map(children, child -> child.getViewLayout(), ViewLayout.class);
    }

    /**
     * Returns the children.
     */
    public void setChildren(ViewLayout<?>[] theProxies)
    {
        _children = theProxies;
    }

    /**
     * Returns the children.
     */
    public void setChildren(List<ViewLayout<?>> theProxies)
    {
        _children = theProxies.toArray(new ViewLayout[0]);
    }

    /**
     * Clears the children.
     */
    public void clearChildren()  { _children = null; }

    /**
     * Returns the last child.
     */
    public ViewLayout<?> getLastChild()
    {
        ViewLayout<?>[] children = getChildren();
        return children.length > 0 ? children[children.length-1] : null;
    }

    /**
     * Returns the child for given component class.
     */
    public <E extends View> ViewLayout<E> getChildForClass(Class<E> aClass)
    {
        ViewLayout<?>[] children = getChildren();
        return (ViewLayout<E>) ArrayUtils.findMatch(children, child -> aClass.isInstance(child.getView()));
    }

    /**
     * Returns the children for given component class.
     */
    public <E extends View> ViewLayout<E>[] getChildrenForClass(Class<E> aClass)
    {
        ViewLayout<?>[] children = getChildren();
        return (ViewLayout<E>[]) ArrayUtils.filter(children, child -> aClass.isInstance(child.getView()));
    }

    /**
     * Returns the border.
     */
    public Border getBorder()
    {
        if (_border != null) return _border;
        return _border = _view.getBorder();
    }

    /**
     * Sets the border.
     */
    public void setBorder(Border aBorder)  { _border = aBorder; }

    /**
     * Returns the border insets.
     */
    public Insets getBorderInsets()
    {
        Border border = getBorder();
        return border != null ? border.getInsets() : Insets.EMPTY;
    }

    /**
     * Returns the margin.
     */
    public Insets getMargin()
    {
        if (_margin != null) return _margin;
        return _margin = _view.getMargin();
    }

    /**
     * Sets the margin.
     */
    public void setMargin(Insets theIns)  { _margin = theIns; }

    /**
     * Returns the padding.
     */
    public Insets getPadding()
    {
        if (_padding != null) return _padding;
        return _padding = _view.getPadding();
    }

    /**
     * Sets the padding.
     */
    public void setPadding(Insets theIns)  { _padding = theIns; }

    /**
     * Returns whether proxy is visible.
     */
    public boolean isVisible()  { return _view.isVisible(); }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()
    {
        if (_align != null) return _align;
        return _align = _view.getAlign();
    }

    /**
     * Sets the alignment.
     */
    public void setAlign(Pos aPos)  { _align = aPos; }

    /**
     * Returns the LeanX.
     */
    public HPos getLeanX()
    {
        if (_leanX != null) return _leanX;
        return _leanX = _view.getLeanX();
    }

    /**
     * Returns the LeanY.
     */
    public VPos getLeanY()
    {
        if (_leanY != null) return _leanY;
        return _leanY = _view.getLeanY();
    }

    /**
     * Returns whether view grows width.
     */
    public boolean isGrowWidth()
    {
        if (_growX != null) return _growX;
        return _growX = _view.isGrowWidth();
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
        return _growY = _view.isGrowHeight();
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
        return _spacing = _view.getSpacing();
    }

    /**
     * Sets spacing.
     */
    public void setSpacing(double aValue)  { _spacing = aValue; }

    /**
     * Returns whether view fills width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether view fills width.
     */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }

    /**
     * Returns whether view fills height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether view fills height.
     */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }

    /**
     * Returns whether to wrap closely around children and project their margins.
     */
    public boolean isHugging()  { return _hugging; }

    /**
     * Sets whether to wrap closely around children and project their margins.
     */
    public void setHugging(boolean aValue)  { _hugging = aValue; }

    /**
     * Returns the best width.
     */
    public double getBestWidth(double aH)  { return _view.getBestWidth(aH); }

    /**
     * Returns the best height.
     */
    public double getBestHeight(double aW)  { return _view.getBestHeight(aW); }

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

    /**
     * Returns preferred width of layout.
     */
    public double getPrefWidth(double aH)
    {
        if (_prefW >=0 && (aH < 0 || aH == _prefH))
            return _prefW;
        if (_view.isPrefWidthSet())
            return _view.getPrefWidth(aH);

        // Perform layout with given height
        double oldW = width, oldH = height;
        double prefH = aH > 0 ? aH : _view.isPrefHeightSet() ? _view.getPrefHeight() : -1;
        setSize(-1, prefH);
        layoutViewLayout();
        width = oldW; height = oldH;

        // Get pref width/height and return
        _prefW = getPrefWidthImpl(aH);
        _prefH = getPrefHeightImpl(_prefW);
        return _prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        if (_prefH >= 0 && (aW < 0 || aW == _prefW))
            return _prefH;
        if (_view.isPrefHeightSet())
            return _view.getPrefHeight(aW);

        // Perform layout with given width
        double oldW = width, oldH = height;
        double prefW = aW > 0 ? aW : _view.isPrefWidthSet() ? _view.getPrefWidth() : -1;
        setSize(prefW, -1);
        layoutViewLayout();
        width = oldW; height = oldH;

        // Get pref height/width and return
        _prefH = getPrefHeightImpl(prefW);
        _prefW = getPrefWidthImpl(_prefH);
        return _prefH;
    }

    /**
     * Performs layout of child views.
     */
    public void layoutView()
    {
        // Perform layout for current view size
        setSize(_view.getWidth(), _view.getHeight());
        layoutViewLayout();

        // Apply bounds
        setBoundsInClient();
    }

    /**
     * Returns preferred width of layout.
     */
    protected abstract double getPrefWidthImpl(double aH);

    /**
     * Returns preferred height of layout.
     */
    protected abstract double getPrefHeightImpl(double aW);

    /**
     * Performs layout of child layouts.
     */
    public abstract void layoutViewLayout();

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()  { return getClass().getSimpleName() + ": " + _view; }

    /**
     * Returns the view layout for given view.
     */
    public static ViewLayout<?> getViewLayout(View aView)  { return aView != null ? aView.getViewLayout() : null; }
}
