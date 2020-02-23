package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.MathUtils;

/**
 * This class represents a view for the purpose of layout.
 */
public class ViewProxy {

    // The original view (if available)
    private View        _view;

    // The size
    private double      _width = UNSET_DOUBLE, _height = UNSET_DOUBLE;

    // The children
    private ViewProxy   _children[];

    // The insets
    private Insets _insets;

    // The alignment
    private Pos _align;

    // The horizontal position this view would prefer to take when inside a pane
    private HPos _leanX;

    // The vertical position this view would prefer to take when inside a pane
    private VPos _leanY;

    // The margin
    private Insets       _margin;

    // Whether view should grow in X or Y
    private Boolean      _growX, _growY;

    // Spacing
    private double       _spacing = UNSET_DOUBLE;

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
        _align = _view!=null? _view.getAlign() : null;
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
     * Returns whether view grows height.
     */
    public boolean isGrowHeight()
    {
        if(_growY!=null) return _growY;
        return _growY = _view!=null && _view.isGrowHeight();
    }

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
     * Returns preferred width of given parent using RowView layout.
     */
    public static double getRowViewPrefWidth(View aPar, View theChildren[], double aSpacing, double aH)
    {
        // Get parent as proxy
        ViewProxy par = new ViewProxy(aPar);
        par.setSize(-1, aH);
        par.setSpacing(aSpacing);
        if(theChildren!=null)
            par._children = getProxies(theChildren);

        // Get child bounds rects and add bounds MaxX to insets right
        Rect bnds[] = getRowViewRects(par, false, false);
        Insets ins = par.getInsetsAll();
        double maxX = bnds.length>0 ? bnds[bnds.length-1].getMaxX() : 0;
        return maxX + ins.getRight();
    }

    /**
     * Returns preferred height of given parent using RowView layout.
     */
    public static double getRowViewPrefHeight(View aPar, View theChildren[], double aW)
    {
        // Get parent as proxy
        ViewProxy par = new ViewProxy(aPar);
        par.setSize(aW, -1);
        if(theChildren!=null)
            par._children = getProxies(theChildren);

        // Get child bounds rects and add bounds MaxX to insets right
        Rect bnds[] = getRowViewRects(par, false, false);
        Insets ins = par.getInsetsAll();
        double maxY = ins.getTop();
        for(Rect bnd : bnds) maxY = Math.max(maxY, bnd.getMaxY());
        double ph = maxY + ins.getBottom();
        ph = Math.round(ph);
        return ph;
    }

    /**
     * Performs layout for given parent View using RowView layout.
     */
    public static void layoutRowView(ParentView aPar, View theChildren[], Insets theIns, boolean isFillWidth,
         boolean isFillHeight, double aSpacing)
    {
        // Get Parent ViewProxy with Children proxies
        ViewProxy par = new ViewProxy(aPar);
        par.setSpacing(aSpacing);
        View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged(); if(children.length==0) return;
        par._children = getProxies(children);

        // Get layout rects and set back in children
        Rect cbnds[] = getRowViewRects(par, isFillWidth, isFillHeight);
        for(int i=0;i<children.length; i++) { View child = children[i]; Rect bnds = cbnds[i];
            child.setBounds(bnds);
        }
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static Rect[] getRowViewRects(ViewProxy aPar, boolean isFillWidth, boolean isFillHeight)
    {
        // Get children (just return if empty) and create rects
        ViewProxy children[] = aPar.getChildren(); if(children.length==0) return new Rect[0];
        Rect cbnds[] = new Rect[children.length];
        for(int i=0;i<cbnds.length;i++) cbnds[i] = new Rect();

        // Load layout rects and return
        getRowViewRectsX(aPar, isFillWidth, cbnds);
        getRowViewRectsY(aPar, isFillHeight, cbnds);
        return cbnds;
    }

    /**
     * Calculates RowView layout bounds (X & Width) for given Parent and sets in given rects.
     */
    private static void getRowViewRectsX(ViewProxy aPar, boolean isFillWidth, Rect cbnds[])
    {
        // Get layout info and loop vars
        ViewProxy children[] = aPar.getChildren();
        Insets ins = aPar.getInsetsAll();
        double spacing = aPar.getSpacing();
        double px = ins.left;
        double cx = px;
        double spc = 0;
        int growersCount = 0;

        // Iterate over children to calculate bounds X and Width
        for(int i=0,iMax=children.length;i<iMax;i++) { ViewProxy child = children[i];

            // Get child width and update child x for spacing/margin-left
            Insets marg = child.getMargin();
            double cw = child.getBestWidth(-1);
            cx += Math.max(spc, marg.left);

            // Set child bounds X and Width
            cbnds[i].x = cx; cbnds[i].width = cw;

            // Update spacing, current child x and grow count
            spc = Math.max(spacing, marg.right);
            cx += cw;
            if(child.isGrowWidth()) growersCount++;
        }

        // If Parent.Width -1, just return rects
        double pw = aPar.getWidth();
        if(pw<0)
            return;
        pw = Math.max(pw - ins.getWidth(), 0);

        // Add margin for last child, calculate extra space and add to growers or alignment
        cx += children[children.length-1].getMargin().right;
        int extra = (int)Math.round(px + pw - cx);
        if(extra!=0)
            addExtraSpaceX(aPar, cbnds, extra, growersCount, isFillWidth);
    }

    /**
     * Calculates RowView layout bounds (Y & Height) for given Parent and sets in given rects.
     */
    private static void getRowViewRectsY(ViewProxy aPar, boolean isFillHeight, Rect cbnds[])
    {
        // Get layout info and loop vars
        ViewProxy children[] = aPar.getChildren();
        Insets ins = aPar.getInsetsAll();
        double ay = getAlignY(aPar);
        double py = ins.top;
        double ph = aPar.getHeight(); if(ph>=0) ph = Math.max(ph - ins.getHeight(), 0);

        // Iterate over children to calculate bounds rects
        for(int i=0,iMax=children.length; i<iMax; i++) { ViewProxy child = children[i];

            // Calc y accounting for margin and alignment
            Insets marg = child.getMargin();
            double maxH = Math.max(ph - marg.getHeight(), 0);
            double cw = cbnds[i].width;
            double cy = py + marg.getTop();
            double ch;

            // If Parent.Height not set, set height to Child.PrefHeight
            if(ph<0) {
                ch = child.getBestHeight(cw);
            }

            // Otherwise, if Parent.FillHeight or Child.GrowHeight, set to max height
            else if(isFillHeight || child.isGrowHeight()) {
                ch = maxH;
            }

            // Otherwise, set height to Child.PrefHeight and adjust Y
            else {
                ch = child.getBestHeight(cw);
                ch = Math.min(ch, maxH);

                // Calc y accounting for margin and alignment
                if (ch < maxH) {
                    double ay2 = Math.max(ay, getLeanY(child));
                    double dy = Math.round((ph - ch) * ay2);
                    cy = Math.max(cy, py + dy);
                }
            }

            // Set child rect Y and Height
            cbnds[i].y = cy;
            cbnds[i].height = ch;
        }
    }

    /**
     * Adds extra space to growers or alignment.
     */
    private static void addExtraSpaceX(ViewProxy par, Rect cbnds[], int extra, int grow, boolean fillW)
    {
        ViewProxy children[] = par.getChildren();

        // If grow shapes, add grow
        if(grow>0)
            addExtraSpaceToGrowers(children, cbnds, extra, grow);

            // Otherwise, if FillWidth, extend last child
        else if(fillW)
            cbnds[children.length-1].width += extra;

            // Otherwise, check for horizontal alignment/lean shift
        else if(extra>0)
            addExtraSpaceToAlign(par, children, cbnds, extra);
    }

    /**
     * Adds extra space to growers.
     */
    private static void addExtraSpaceToGrowers(ViewProxy children[], Rect cbnds[], int extra, int grow)
    {
        // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
        int each = extra/grow;
        int eachP1 = each + MathUtils.sign(extra);
        int count2 = Math.abs(extra%grow);

        // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
        for(int i=0, j=0, dx = 0,iMax=children.length;i<iMax;i++) { ViewProxy child = children[i];
            Rect cbnd = cbnds[i];
            if(dx!=0)
                cbnd.setX(cbnd.x + dx);
            if(child.isGrowWidth()) {
                int each3 = j<count2? eachP1 : each;
                cbnd.setWidth(cbnd.width + each3);
                dx += each3; j++;
            }
        }
    }

    /**
     * Adds extra space to alignment.
     */
    private static void addExtraSpaceToAlign(ViewProxy par, ViewProxy children[], Rect cbnds[], double extra)
    {
        double ax = getAlignX(par);
        for(int i=0,iMax=children.length;i<iMax;i++) { ViewProxy child = children[i];
            Rect cbnd = cbnds[i];
            ax = Math.max(ax, getLeanX(child)); double dx = extra*ax;
            if(dx>0)
                cbnd.setX(cbnd.x + extra*ax);
        }
    }

    /**
     * Returns the align x factor.
     */
    public static double getAlignX(ViewProxy aView)
    {
        return ViewUtils.getAlignX(aView.getAlign());
    }

    /**
     * Returns the align y factor.
     */
    public static double getAlignY(ViewProxy aView)
    {
        return ViewUtils.getAlignY(aView.getAlign());
    }

    /**
     * Returns the lean x factor.
     */
    public static double getLeanX(ViewProxy aView)
    {
        return ViewUtils.getAlignX(aView.getLeanX());
    }

    /**
     * Returns the lean y factor.
     */
    public static double getLeanY(ViewProxy aView)
    {
        return ViewUtils.getAlignY(aView.getLeanY());
    }

    /**
     * Returns an array of proxies for given array of views.
     */
    public static ViewProxy[] getProxies(View theViews[])
    {
        ViewProxy proxies[] = new ViewProxy[theViews.length];
        for(int i=0;i<theViews.length;i++)
            proxies[i] = new ViewProxy(theViews[i]);
        return proxies;
    }
}
