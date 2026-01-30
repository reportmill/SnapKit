/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Shape;
import snap.geom.Size;
import snap.gfx.Border;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View for scrolling other views.
 */
public class ScrollView extends ParentView implements ViewHost {
    
    // The scroll view
    private Scroller  _scroller;
    
    // The scrollbars
    private ScrollBar  _hbar, _vbar;
    
    // Whether to show horizontal/vertical scroll bars (null means 'as-needed')
    private Boolean  _showHBar, _showVBar;
    
    // Whether horizontal/vertical scroll bars have shown recently
    private boolean  _showHBarHint, _showVBarHint;

    // The ScrollBar size
    private int  _barSize = 14;
    
    // Constants
    public static final String Content_Prop = Scroller.Content_Prop;
    public static final String FillWidth_Prop = Scroller.FillWidth_Prop;
    public static final String FillHeight_Prop = Scroller.FillHeight_Prop;
    public static final String ShowHBar_Prop = "ShowHBar";
    public static final String ShowVBar_Prop = "ShowVBar";
    public static final String BarSize_Prop = "BarSize";
    public static final String HBarShowing_Prop = "HBarShowing";
    public static final String VBarShowing_Prop = "VBarShowing";

    /**
     * Constructor.
     */
    public ScrollView()
    {
        super();

        // Create Scroller and add listeners for scroll changes
        _scroller = new Scroller();
        _scroller.addPropChangeListener(this::handleScrollerPropChange);
        addChild(_scroller);
    }

    /**
     * Constructor for given view.
     */
    public ScrollView(View aView)
    {
        this();
        setContent(aView);
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _scroller.getContent(); }

    /**
     * Sets the content.
     */
    public void setContent(View aView)  { _scroller.setContent(aView); }

    /**
     * Returns whether this ScrollView fits content to its width.
     */
    public boolean isFillWidth()  { return _scroller.isFillWidth(); }

    /**
     * Sets whether this ScrollView fits content to its width.
     */
    public void setFillWidth(boolean aValue)  { _scroller.setFillWidth(aValue); }

    /**
     * Returns whether this ScrollView fits content to its height.
     */
    public boolean isFillHeight()  { return _scroller.isFillHeight(); }

    /**
     * Sets whether this ScrollView fits content to its height.
     */
    public void setFillHeight(boolean aValue)  { _scroller.setFillHeight(aValue); }

    /**
     * ViewHost method: Override to send to Scroller.
     */
    public int getGuestCount()  { return _scroller.getGuestCount(); }

    /**
     * ViewHost method: Override to send to Scroller.
     */
    public View getGuest(int anIndex)  { return _scroller.getGuest(anIndex); }

    /**
     * ViewHost method: Override to send to Scroller.
     */
    public void addGuest(View aChild, int anIndex)  { _scroller.addGuest(aChild, anIndex); }

    /**
     * ViewHost method: Override to send to Scroller.
     */
    public View removeGuest(int anIndex)  { return _scroller.removeGuest(anIndex); }

    /**
     * Returns the view that handles scrolling.
     */
    public Scroller getScroller()  { return _scroller; }

    /**
     * Returns the Horizontal ScrollBar.
     */
    public ScrollBar getHBar()
    {
        if (_hbar != null) return _hbar;
        _hbar = new ScrollBar();
        _hbar.setBorderRadius(getBorderRadius());
        _hbar.addPropChangeListener(this::handleScrollBarPropChange, ScrollBar.Scroll_Prop);
        return _hbar;
    }

    /**
     * Returns the vertical ScrollBar.
     */
    public ScrollBar getVBar()
    {
        if (_vbar != null) return _vbar;
        _vbar = new ScrollBar();
        _vbar.setBorderRadius(getBorderRadius());
        _vbar.setVertical(true);
        _vbar.addPropChangeListener(this::handleScrollBarPropChange, ScrollBar.Scroll_Prop);
        return _vbar;
    }

    /**
     * Returns whether to show horizontal scroll bar (null means 'as-needed').
     */
    public Boolean getShowHBar()  { return _showHBar; }

    /**
     * Sets whether to show horizontal scroll bar (null means 'as-needed').
     */
    public void setShowHBar(Boolean aValue)
    {
        if (aValue == _showHBar) return;
        firePropChange(ShowHBar_Prop, _showHBar, _showHBar = aValue);
    }

    /**
     * Returns whether to show vertical scroll bar (null means 'as-needed').
     */
    public Boolean getShowVBar()  { return _showVBar; }

    /**
     * Returns whether to show vertical scroll bar (null means 'as-needed').
     */
    public void setShowVBar(Boolean aValue)
    {
        if (aValue == _showVBar) return;
        firePropChange(ShowVBar_Prop, _showVBar, _showVBar = aValue);
    }

    /**
     * Returns whether HBar is showing.
     */
    public boolean isHBarShowing()  { return getHBar().getParent() != null; }

    /**
     * Sets whether HBar is showing.
     */
    protected void setHBarShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue == isHBarShowing()) return;

        // If showing, add and update
        ScrollBar hbar = getHBar();
        if (aValue) {
            addChild(hbar);
            hbar.setScrollerSize(_scroller.getWidth());
            hbar.setContentSize(_scroller.getContentWidth());
            hbar.setScroll(_scroller.getScrollX());
            _showHBarHint = true;
        }

        // Otherwise, remove
        else removeChild(hbar);

        // Fire prop change
        firePropChange(HBarShowing_Prop, !aValue, aValue);
    }

    /**
     * Returns whether VBar is showing.
     */
    public boolean isVBarShowing()  { return getVBar().getParent() != null; }

    /**
     * Sets whether VBar is showing.
     */
    protected void setVBarShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue == isVBarShowing()) return;

        // If showing, add and update
        ScrollBar vbar = getVBar();
        if (aValue) {
            addChild(vbar);
            vbar.setScrollerSize(_scroller.getHeight());
            vbar.setContentSize(_scroller.getContentHeight());
            vbar.setScroll(_scroller.getScrollY());
            _showVBarHint = true;
        }

        // Otherwise, remove
        else removeChild(vbar);

        // Fire prop change
        firePropChange(VBarShowing_Prop, !aValue, aValue);
    }

    /**
     * Returns the scroll bar size.
     */
    public int getBarSize()  { return _barSize; }

    /**
     * Sets the scroll bar size.
     */
    public void setBarSize(int aValue)
    {
        if (aValue == _barSize) return;
        firePropChange(BarSize_Prop, _barSize, _barSize = aValue);
    }

    /**
     * Override to paint content border.
     */
    @Override
    protected void paintAll(Painter aPntr)
    {
        super.paintAll(aPntr);

        // If content is overflow scroll and has border, repaint content border
        View content = getContent();
        if (content != null && content.getOverflow() == Overflow.Scroll) {
            Border border = content.getBorder();
            if (border != null) {
                Shape boundsShape = getBoundsShape();
                border.paint(aPntr, boundsShape);
            }
        }
    }

    /**
     * Calculates the minimum width.
     */
    protected double getMinWidthImpl()
    {
        Insets ins = getInsetsAll();
        double minW = _scroller.getMinWidth();
        return minW + ins.getWidth();
    }

    /**
     * Calculates the minimum height.
     */
    protected double getMinHeightImpl()
    {
        Insets ins = getInsetsAll();
        double minH = _scroller.getMinHeight();
        return minH + ins.getHeight();
    }

    /**
     * Calculates the preferred width.
     */
    protected double computePrefWidth(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW = _scroller.getBestWidth(aH);
        if (_showVBarHint || _showVBar == Boolean.TRUE)
            prefW += getBarSize();
        return prefW + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double computePrefHeight(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH = _scroller.getBestHeight(aW);
        if (_showHBarHint || _showHBar == Boolean.TRUE)
            prefH += getBarSize();
        return prefH + ins.getHeight();
    }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()
    {
        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Account for ScrollBars
        boolean isVBarShowing = isVBarShowing();
        boolean isHBarShowing = isHBarShowing();
        int barSize = getBarSize();
        if (isVBarShowing)
            areaW = Math.max(areaW - barSize, 0);
        if (isHBarShowing)
            areaH = Math.max(areaH - barSize, 0);

        // Set Scroller bounds
        _scroller.setBounds(areaX, areaY, areaW, areaH);

        // Check whether either ScrollBar.Showing needs updating
        if (updateScrollBarsShowing()) {
            if (!_recursingLayout) {
                _recursingLayout = true;
                layoutImpl();
                _recursingLayout = false;
                return;
            }
            //else updateScrollBarsShowing();
            //else System.err.println("ScrollView: Confused about whether we get eliminate scrollbars");
        }

        // If horizontal scrollbar showing, set bounds
        if (isHBarShowing) {
            ScrollBar hbar = getHBar();
            hbar.setBounds(areaX, areaY + areaH, areaW, barSize);
        }

        // If vertical scrollbar showing, set bounds
        if (isVBarShowing) {
            ScrollBar vbar = getVBar();
            vbar.setBounds(areaX + areaW, areaY, barSize, areaH);
        }
    }

    // Whether recursing into layoutImpl
    private boolean _recursingLayout;

    /**
     * Called to update whether ScrollBars are showing. Returns true if any changes.
     */
    protected boolean updateScrollBarsShowing()
    {
        // Get Scroller Size
        double scrollerW = _scroller.getWidth();
        double scrollerH = _scroller.getHeight();
        if (scrollerW <= 30 || scrollerH <= 30)
            return false;

        // Get child size
        Size contentSize = _scroller.getContentPrefSize();
        double contentW = contentSize.width;
        double contentH = contentSize.height;

        // Get whether to show scroll bars
        boolean alwaysH = _showHBar == Boolean.TRUE;
        boolean alwaysV = _showVBar == Boolean.TRUE;
        boolean asneedH = _showHBar == null;
        boolean asneedV = _showVBar == null;
        boolean showHBar = alwaysH || asneedH && contentW > scrollerW;
        boolean showVBar = alwaysV || asneedV && contentH > scrollerH;

        // Get whether scroll bars are currently showing
        boolean isHBarShowing = isHBarShowing();
        boolean isVBarShowing = isVBarShowing();

        // If showing both ScrollBars, but only because both ScrollBars are showing, hide them and try again
        if (isVBarShowing && isHBarShowing && showVBar && showHBar && asneedH && asneedV) {
            boolean vbarNotReallyNeeded = contentW <= scrollerW + getBarSize();
            boolean hbarNotReallyNeeded = contentH <= scrollerH + getBarSize();
            if (vbarNotReallyNeeded && hbarNotReallyNeeded) {
                setVBarShowing(false);
                setHBarShowing(false);
                return true;
            }
        }

        // If either ScrollBar in wrong Showing state, set and try again
        if (showVBar != isVBarShowing || showHBar != isHBarShowing) {
            if (showVBar && !_showVBarHint || showHBar && !_showHBarHint)
                runLater(this::relayoutParent);
            _showVBarHint = showVBar;
            _showHBarHint = showHBar;
            setVBarShowing(showVBar);
            setHBarShowing(showHBar);
            return true;
        }

        // Return false since ScrollBar showing didn't change
        return false;
    }

    /**
     * Handle Scroller property changes.
     */
    protected void handleScrollerPropChange(PropChange aPC)
    {
        switch (aPC.getPropName()) {

            // Handle Scroller Content: Repost prop change and check for overflow scroll border update
            case Content_Prop:
                firePropChange(aPC.getPropName(), aPC.getOldValue(), aPC.getNewValue());

                // If content is overflow scroll, use its border
                View content = getContent();
                if (content != null && content.getOverflow() == Overflow.Scroll) {
                    setBorder(null);
                    setBorderRadius(content.getBorderRadius());
                }
                break;

            // Handle Scroller FillWidth, FillHeight: Repost prop change
            case FillWidth_Prop: case FillHeight_Prop:
                firePropChange(aPC.getPropName(), aPC.getOldValue(), aPC.getNewValue());
                relayout();
                break;

            // Handle Scroller ScrollX, ScrollY change
            case Scroller.ScrollX_Prop: getHBar().setScroll(_scroller.getScrollX()); break;
            case Scroller.ScrollY_Prop: getVBar().setScroll(_scroller.getScrollY()); break;

            // Handle Scroller.Width or Scroller.ScrollWidth change
            case Width_Prop: case Scroller.ContentWidth_Prop:
                getHBar().setScrollerSize(_scroller.getWidth());
                getHBar().setContentSize(_scroller.getContentWidth());
                getHBar().setScroll(_scroller.getScrollX());
                break;

            // Handle Scroller.Height or Scroller.ScrollHeight change
            case Height_Prop: case Scroller.ContentHeight_Prop:
                getVBar().setScrollerSize(_scroller.getHeight());
                getVBar().setContentSize(_scroller.getContentHeight());
                getVBar().setScroll(_scroller.getScrollY());
                break;
        }
    }

    /**
     * Handle ScrollBar property changes.
     */
    protected void handleScrollBarPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        if (propName == ScrollBar.Scroll_Prop) {
            ScrollBar scrollBar = (ScrollBar) aPC.getSource();
            double val = scrollBar.getScrollRatio();
            if (scrollBar == _hbar)
                _scroller.setScrollRatioX(val);
            else _scroller.setScrollRatioY(val);
        }
    }

    /**
     * Override to propagate to Scroller and ScrollBars.
     */
    @Override
    public void setBorderRadius(double aValue)
    {
        super.setBorderRadius(aValue);
        _scroller.setBorderRadius(aValue);
        if (_hbar != null)
            _hbar.setBorderRadius(aValue);
        if (_vbar != null)
            _vbar.setBorderRadius(aValue);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // FillWidth, FillHeight, ShowHBar, ShowVBar, BarSize
        aPropSet.addPropNamed(FillWidth_Prop, boolean.class, false);
        aPropSet.addPropNamed(FillHeight_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowHBar_Prop, Boolean.class, null);
        aPropSet.addPropNamed(ShowVBar_Prop, Boolean.class, null);
        aPropSet.addPropNamed(BarSize_Prop, int.class, 14);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // FillWidth, FillHeight, ShowHBar, ShowVBar, BarSize
            case FillWidth_Prop: return isFillWidth();
            case FillHeight_Prop: return isFillHeight();
            case ShowHBar_Prop: return getShowHBar();
            case ShowVBar_Prop: return getShowVBar();
            case BarSize_Prop: return getBarSize();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // FillWidth, FillHeight, ShowHBar, ShowVBar, BarSize
            case FillWidth_Prop: setFillWidth(Convert.boolValue(aValue)); break;
            case FillHeight_Prop: setFillHeight(Convert.boolValue(aValue)); break;
            case ShowHBar_Prop: setShowHBar(Convert.booleanValue(aValue)); break;
            case ShowVBar_Prop: setShowVBar(Convert.booleanValue(aValue)); break;
            case BarSize_Prop: setBarSize(Convert.intValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive FillWidth, FillHeight, ShowHBar, ShowVBar, BarSize
        if (!isPropDefault(FillWidth_Prop)) e.add(FillWidth_Prop, isFillWidth());
        if (!isPropDefault(FillHeight_Prop)) e.add(FillHeight_Prop, isFillWidth());
        if (!isPropDefault(ShowHBar_Prop)) e.add(ShowHBar_Prop, getShowHBar());
        if (!isPropDefault(ShowVBar_Prop)) e.add(ShowVBar_Prop, getShowVBar());
        if (!isPropDefault(BarSize_Prop)) e.add(BarSize_Prop, getBarSize());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive FillWidth, FillHeight, ShowHBar, ShowVBar, BarSize
        if (anElement.hasAttribute(FillWidth_Prop)) setFillWidth(anElement.getAttributeBoolValue(FillWidth_Prop));
        if (anElement.hasAttribute(FillHeight_Prop)) setFillHeight(anElement.getAttributeBoolValue(FillHeight_Prop));
        if (anElement.hasAttribute(ShowHBar_Prop)) setShowHBar(anElement.getAttributeBoolValue(ShowHBar_Prop));
        if (anElement.hasAttribute(ShowVBar_Prop)) setShowVBar(anElement.getAttributeBoolValue(ShowVBar_Prop));
        if (anElement.hasAttribute(BarSize_Prop)) setBarSize(anElement.getAttributeIntValue(BarSize_Prop));
    }
}