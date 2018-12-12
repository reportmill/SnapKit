package snap.view;
import snap.gfx.*;

/**
 * A view that encapsulates multiple scroll views (center, top, left) and keeps them in sync to provide scroll headers.
 */
public class ScrollGroup extends ParentView {
    
    // The primary scroll view
    ScrollView          _scroll;

    // The view that holds the top view
    Scroller            _topScroll;
    
    // The view that holds the top scroller and corners
    RowView             _topScrollRow;
    
    // The view that holds the top scroll row and divider line
    ColView             _topScrollCol;

    // The view that holds the left view
    Scroller            _leftScroll;
    
    // The view that holds the left scroller
    ColView             _leftScrollBox;
    
    // The view representing the NE corner (when TopView set and ScrollView.VBarShowing)
    BoxView             _cornerNE;

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
public View getTopView()  { return _topScroll!=null? _topScroll.getContent() : null; }

/**
 * Sets the top view.
 */
public void setTopView(View aView)
{
    getTopScroll().setContent(aView);
}

/**
 * Returns the Header ScrollView.
 */
protected Scroller getTopScroll()  { if(_topScroll==null) getTopScrollCol(); return _topScroll; }

/**
 * Returns the view that holds the TopScroll scroller view and corner view(s).
 */
public RowView getTopScrollRow()  { if(_topScrollRow==null) getTopScrollCol(); return _topScrollRow; }

/**
 * Returns the view that holds the TopScrollRow and a divider line view.
 */
public ColView getTopScrollCol()
{
    // If already set, just return it
    if(_topScrollCol!=null) return _topScrollCol;
    
    // Create TopScroll
    _topScroll = new Scroller(); _topScroll.setGrowWidth(true);
    
    // Create TopScrollRow and add
    _topScrollRow = new RowView(); _topScrollRow.setGrowHeight(true); _topScrollRow.setFillHeight(true);
    _topScrollRow.addChild(_topScroll);
    
    // Create TopScrollCol and add
    _topScrollCol = new ColView(); _topScrollCol.setFillWidth(true);
    LineView line = new LineView(0,.5,10,.5); line.setPrefHeight(1); line.setBorder(Color.LIGHTGRAY,1);
    _topScrollCol.setChildren(_topScrollRow, line);
    
    // Bind main ScrollView.Scroller.ScrollH to HeaderScroller (both ways)
    Scroller scroller = _scroll.getScroller();
    ViewUtils.bind(scroller, Scroller.ScrollH_Prop, _topScroll, true);
    
    // Bind ScrollView.
    _scroll.addPropChangeListener(pc -> scrollViewVBarShowingChanged(), ScrollView.VBarShowing_Prop);
    
    // Add TopScrollCol and return
    addChild(_topScrollCol);
    return _topScrollCol;
}

/**
 * Called when ScrollView changes ShowVBar.
 */
void scrollViewVBarShowingChanged()
{
    setShowCornerNE(_scroll.isVBarShowing());
}

/**
 * Returns the Corner view (NE).
 */
public BoxView getCornerNE()
{
    if(_cornerNE!=null) return _cornerNE;
    _cornerNE = new BoxView();
    if(_scroll.isVBarShowing()) _cornerNE.setPrefWidth(_scroll.getBarSize());
    else _cornerNE.setVisible(false);
    getTopScrollRow().addChild(_cornerNE);
    return _cornerNE;
}

/**
 * Returns whether NE Corner view is showing.
 */
public boolean isShowCornerNE()  { return _cornerNE!=null && _cornerNE.isShowing(); }

/**
 * Sets whether NE Corner view is showing.
 */
protected void setShowCornerNE(boolean aValue)
{
    getCornerNE().setVisible(aValue);
    if(aValue) getCornerNE().setPrefWidth(_scroll.getBarSize());
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    return BorderView.getPrefWidth(this, _scroll, _topScrollCol, null, null, _leftScrollBox, aH);
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    return BorderView.getPrefHeight(this, _scroll, _topScrollCol, null, null, _leftScrollBox, aW);
}

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    BorderView.layout(this, _scroll, _topScrollCol, null, null, _leftScrollBox);
}

}