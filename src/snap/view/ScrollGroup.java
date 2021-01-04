package snap.view;
import snap.gfx.*;

/**
 * A view that encapsulates multiple scroll views (center, top, left) and keeps them in sync to provide scroll headers.
 */
public class ScrollGroup extends ParentView {
    
    // The primary scroll view
    private ScrollView  _scroll;

    // The view that holds the top view
    private Scroller  _topScroll;
    
    // The view that holds the top scroller, cornerNE and corner NW
    private RowView  _topScrollRow;
    
    // The view that holds the top scroll row and divider line
    private ColView  _topScrollCol;

    // The view that holds the left view
    private Scroller  _leftScroll;
    
    // The view that holds the left scroller and CornerSW
    private ColView  _leftScrollCol;

    // The view that holds the left scroll col and divider line
    private RowView  _leftScrollRow;
    
    // The view representing the NE corner (when TopView set and ScrollView.VBarShowing)
    private BoxView  _cornerNE;

    // The view representing the NW corner (when TopView set and LeftView set)
    private BoxView  _cornerNW;

    // The view representing the SW corner (when LeftView set and ScrollView.HBarShowing)
    private BoxView  _cornerSW;

    /**
     * Creates a ScrollGroup.
     */
    public ScrollGroup()
    {
        _scroll = new ScrollView();
        addChild(_scroll);
        setBorder(_scroll.getBorder());
        _scroll.setBorder(null);
    }

    /**
     * Creates a ScrollGroup.
     */
    public ScrollGroup(View aView)  { this(); setContent(aView); }

    /**
     * Returns the primary content.
     */
    public View getContent()  { return _scroll.getContent(); }

    /**
     * Sets the primary content.
     */
    public void setContent(View aView)  { _scroll.setContent(aView); }

    /**
     * Returns the primary scroll view.
     */
    public ScrollView getScrollView()  { return _scroll; }

    /**
     * Returns the top view.
     */
    public View getTopView()
    {
        return _topScroll!=null ? _topScroll.getContent() : null;
    }

    /**
     * Sets the top view.
     */
    public void setTopView(View aView)
    {
        getTopScroll().setContent(aView);
    }

    /**
     * Returns the top Scroller.
     */
    protected Scroller getTopScroll()
    {
        if (_topScroll==null)
            getTopScrollCol();
        return _topScroll;
    }

    /**
     * Returns the view that holds the TopScroll scroller view and corner view(s).
     */
    public RowView getTopScrollRow()
    {
        if (_topScrollRow==null)
            getTopScrollCol();
        return _topScrollRow;
    }

    /**
     * Returns the view that holds the TopScrollRow and a divider line view.
     */
    public ColView getTopScrollCol()
    {
        // If already set, just return it
        if (_topScrollCol!=null) return _topScrollCol;

        // Create TopScroll
        _topScroll = new Scroller(); _topScroll.setGrowWidth(true);

        // Create TopScrollRow and add
        _topScrollRow = new RowView(); _topScrollRow.setGrowHeight(true); _topScrollRow.setFillHeight(true);
        _topScrollRow.addChild(_topScroll);

        // Create TopScrollCol and add
        _topScrollCol = new ColView(); _topScrollCol.setFillWidth(true);
        LineView line = new LineView(0,.5,10,.5); line.setPrefHeight(1); line.setBorder(Color.LIGHTGRAY,1);
        _topScrollCol.setChildren(_topScrollRow, line);

        // Bind main ScrollView.Scroller.ScrollX to HeaderScroller (both ways)
        Scroller scroller = _scroll.getScroller();
        ViewUtils.bind(scroller, Scroller.ScrollX_Prop, _topScroll, true);

        // Bind ScrollView.VBarShowing to ShowCornerNE
        _scroll.addPropChangeListener(pc -> setShowCornerNE(_scroll.isVBarShowing()), ScrollView.VBarShowing_Prop);

        // Add TopScrollCol and return
        addChild(_topScrollCol);
        return _topScrollCol;
    }

    /**
     * Returns the left view.
     */
    public View getLeftView()
    {
        return _leftScroll!=null ? _leftScroll.getContent() : null;
    }

    /**
     * Sets the left view.
     */
    public void setLeftView(View aView)
    {
        getLeftScroll().setContent(aView);
        setShowCornerNW(aView!=null);
    }

    /**
     * Returns the left Scroller.
     */
    protected Scroller getLeftScroll()
    {
        if (_leftScroll==null)
            getLeftScrollRow();
        return _leftScroll;
    }

    /**
     * Returns the view that holds the LeftScroll scroller view and CornerSW.
     */
    public ColView getLeftScrollCol()
    {
        if (_leftScrollCol==null)
            getLeftScrollRow();
        return _leftScrollCol;
    }

    /**
     * Returns the view that holds the LeftScrollCol and divider line.
     */
    public RowView getLeftScrollRow()
    {
        // If already set, just return
        if (_leftScrollRow!=null) return _leftScrollRow;

        // Create LeftScroll
        _leftScroll = new Scroller(); _leftScroll.setGrowHeight(true);

        // Create LeftScrollCol and add
        _leftScrollCol = new ColView(); _leftScrollCol.setGrowWidth(true); _leftScrollCol.setFillWidth(true);
        _leftScrollCol.addChild(_leftScroll);

        // Create LeftScrollRow and add
        _leftScrollRow = new RowView(); _leftScrollRow.setFillHeight(true);
        LineView line = new LineView(.5,0,.5,10); line.setPrefWidth(1); line.setBorder(Color.LIGHTGRAY,1);
        _leftScrollRow.setChildren(_leftScrollCol, line);

        // Bind main ScrollView.Scroller.ScrollY to HeaderScroller (both ways)
        Scroller scroller = _scroll.getScroller();
        ViewUtils.bind(scroller, Scroller.ScrollY_Prop, _leftScroll, true);

        // Bind ScrollView.VBarShowing to ShowCornerNE
        _scroll.addPropChangeListener(pc -> setShowCornerSW(_scroll.isHBarShowing()), ScrollView.HBarShowing_Prop);

        // Add LeftScrollRow
        addChild(_leftScrollRow);
        return _leftScrollRow;
    }

    /**
     * Returns the Corner view (NE).
     */
    public BoxView getCornerNE()
    {
        if (_cornerNE!=null) return _cornerNE;
        _cornerNE = new BoxView();
        if (_scroll.isVBarShowing())
            _cornerNE.setPrefWidth(_scroll.getBarSize());
        else _cornerNE.setVisible(false);
        getTopScrollRow().addChild(_cornerNE);
        return _cornerNE;
    }

    /**
     * Returns whether NE Corner view is showing (true when TopView set and ScrollView.VBarShowing).
     */
    public boolean isShowCornerNE()  { return _cornerNE!=null && _cornerNE.isShowing(); }

    /**
     * Sets whether NE Corner view is showing (true when TopView set and ScrollView.VBarShowing).
     */
    protected void setShowCornerNE(boolean aValue)
    {
        getCornerNE().setVisible(aValue);
        if (aValue)
            getCornerNE().setPrefWidth(getScrollView().getBarSize());
    }

    /**
     * Returns the Corner view (NW).
     */
    public BoxView getCornerNW()
    {
        if (_cornerNW!=null) return _cornerNW;
        _cornerNW = new BoxView();
        if (getLeftView()!=null)
            _cornerNW.setPrefWidth(getLeftView().getPrefWidth());
        else _cornerNW.setVisible(false);
        getTopScrollRow().addChild(_cornerNW, 0);
        return _cornerNW;
    }

    /**
     * Returns whether NW Corner view is showing (true when TopView set and LeftView set).
     */
    public boolean isShowCornerNW()  { return _cornerNW!=null && _cornerNW.isShowing(); }

    /**
     * Sets whether NW Corner view is showing (true when TopView set and LeftView set).
     */
    protected void setShowCornerNW(boolean aValue)
    {
        getCornerNW().setVisible(aValue);
        if (aValue)
            getCornerNW().setPrefWidth(getLeftView().getPrefWidth());
    }

    /**
     * Returns the Corner view (SW).
     */
    public BoxView getCornerSW()
    {
        if (_cornerSW!=null) return _cornerSW;
        _cornerSW = new BoxView();
        if (getScrollView().isHBarShowing())
            _cornerSW.setPrefHeight(getScrollView().getBarSize());
        else _cornerSW.setVisible(false);
        getLeftScrollCol().addChild(_cornerSW);
        return _cornerSW;
    }

    /**
     * Returns whether SW Corner view is showing (true when LeftView set and ScrollView.HBarShowing).
     */
    public boolean isShowCornerSW()  { return _cornerSW!=null && _cornerSW.isShowing(); }

    /**
     * Sets whether NW Corner view is showing (true when LeftView set and ScrollView.HBarShowing).
     */
    protected void setShowCornerSW(boolean aValue)
    {
        getCornerSW().setVisible(aValue);
        if (aValue)
            getCornerSW().setPrefHeight(getScrollView().getBarSize());
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BorderView.getPrefWidth(this, _scroll, _topScrollCol, null, null, _leftScrollRow, aH);
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BorderView.getPrefHeight(this, _scroll, _topScrollCol, null, null, _leftScrollRow, aW);
    }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()
    {
        BorderView.layout(this, _scroll, _topScrollCol, null, null, _leftScrollRow);
        if (_leftScrollRow!=null && _topScrollRow!=null)
            getCornerNW().setPrefWidth(_leftScrollRow.getWidth());
    }
}