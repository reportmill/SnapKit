package snap.view;
import snap.geom.Insets;
import snap.gfx.*;
import snap.props.PropChange;

/**
 * A view that encapsulates multiple scroll views (center, top, left) and keeps them in sync to provide scroll headers.
 */
public class ScrollGroup extends ParentView {
    
    // The primary scroll view
    private ScrollView _scrollView;

    // The view that holds the top view
    private Scroller _topScroller;
    
    // The view that holds the left view
    private Scroller _leftScroller;

    // The view that holds the top scroll row and divider line
    private ColView _topScrollerCol;

    /**
     * Creates a ScrollGroup.
     */
    public ScrollGroup()
    {
        _scrollView = new ScrollView();
        addChild(_scrollView);
        setBorder(_scrollView.getBorder());
        _scrollView.setBorder(null);

        // Listen for ScrollView scroll bar changes
        _scrollView.addPropChangeListener(this::handleScrollViewPropChange,
            ScrollView.HBarShowing_Prop, ScrollView.VBarShowing_Prop);
    }

    /**
     * Creates a ScrollGroup.
     */
    public ScrollGroup(View aView)
    {
        this();
        setContent(aView);
    }

    /**
     * Returns the primary content.
     */
    public View getContent()  { return _scrollView.getContent(); }

    /**
     * Sets the primary content.
     */
    public void setContent(View aView)  { _scrollView.setContent(aView); }

    /**
     * Returns the primary scroll view.
     */
    public ScrollView getScrollView()  { return _scrollView; }

    /**
     * Returns the top view.
     */
    public View getTopView()  { return _topScroller != null ? _topScroller.getContent() : null; }

    /**
     * Sets the top view.
     */
    public void setTopView(View aView)
    {
        if (aView == getTopView()) return;
        Scroller topScroller = getTopScroller();
        topScroller.setContent(aView);
    }

    /**
     * Returns the top Scroller.
     */
    protected Scroller getTopScroller()
    {
        if (_topScroller != null) return _topScroller;

        // Create top scroller
        _topScroller = new Scroller();
        _topScroller.setGrowWidth(true);
        _topScroller.setMargin(0, 0, 0, getLeftView() != null ? getLeftView().getWidth() : 0);

        // Bind main ScrollView.Scroller.ScrollX to HeaderScroller (both ways)
        Scroller scroller = _scrollView.getScroller();
        ViewUtils.bind(scroller, Scroller.ScrollX_Prop, _topScroller, true);

        // Create TopScrollCol and add
        _topScrollerCol = new ColView();
        _topScrollerCol.setFillWidth(true);
        LineView line = new LineView(0,.5,10,.5);
        line.setPrefHeight(1);
        line.setBorder(Color.LIGHTGRAY,1);
        _topScrollerCol.setChildren(_topScroller, line);
        addChild(_topScrollerCol);

        // Return
        return _topScroller;
    }

    /**
     * Returns the left view.
     */
    public View getLeftView()  { return _leftScroller != null ? _leftScroller.getContent() : null; }

    /**
     * Sets the left view.
     */
    public void setLeftView(View aView)
    {
        if (aView == getLeftView()) return;
        Scroller leftScroller = getLeftScroller();
        leftScroller.setContent(aView);
    }

    /**
     * Returns the left Scroller.
     */
    protected Scroller getLeftScroller()
    {
        if (_leftScroller !=null) return _leftScroller;

        // Create left scroller
        _leftScroller = new Scroller();
        _leftScroller.setGrowHeight(true);
        _leftScroller.addPropChangeListener(pc -> handleLeftScrollerWidthChange(), Width_Prop);

        // Bind main ScrollView.Scroller.ScrollY to HeaderScroller (both ways)
        Scroller scroller = _scrollView.getScroller();
        ViewUtils.bind(scroller, Scroller.ScrollY_Prop, _leftScroller, true);

        // Create LeftScrollRow and add
        addChild(_leftScroller);

        // Return
        return _leftScroller;
    }

    /**
     * Override to return border layout.
     */
    @Override
    protected ViewLayout<?> getViewLayoutImpl()
    {
        return new BorderViewLayout(this, _scrollView, _topScrollerCol, null, null, _leftScroller);
    }

    /**
     * Called when left scroller changes width to update top scroller.
     */
    private void handleLeftScrollerWidthChange()
    {
        if (_topScroller == null) return;
        Insets margin = _topScroller.getMargin();
        margin = new Insets(margin.top, margin.right, margin.bottom, _leftScroller.getWidth());
        _topScroller.setMargin(margin);
    }

    /**
     * Called when ScrollView property changes.
     */
    private void handleScrollViewPropChange(PropChange aPC)
    {
        switch (aPC.getPropName()) {

            // If horizontal scroll bar is shown/hidden, update left scroller margin
            case ScrollView.HBarShowing_Prop:
                if (_leftScroller != null) {
                    Insets margin = _leftScroller.getMargin();
                    margin = new Insets(margin.top, margin.right, _scrollView.isHBarShowing() ? _scrollView.getBarSize() : 0, margin.left);
                    _leftScroller.setMargin(margin);
                }
                break;

            // If vertical scroll bar is shown/hidden, update top scroller margin
            case ScrollView.VBarShowing_Prop:
                if (_topScroller != null) {
                    Insets margin = _topScroller.getMargin();
                    margin = new Insets(margin.top, _scrollView.isVBarShowing() ? _scrollView.getBarSize() : 0, margin.bottom, margin.left);
                    _topScroller.setMargin(margin);
                }
                break;
        }
    }
}