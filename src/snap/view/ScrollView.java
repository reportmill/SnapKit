/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Size;
import snap.gfx.*;
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
    
    // The ScrollBar size
    private int  _barSize = 16;
    
    // Constants
    static final Border SCROLL_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);
    public static final String ShowHBar_Prop = "ShowHBar";
    public static final String ShowVBar_Prop = "ShowVBar";
    public static final String HBarShowing_Prop = "HBarShowing";
    public static final String VBarShowing_Prop = "VBarShowing";
    public static final String BarSize_Prop = "BarSize";
    
    /**
     * Creates a new ScrollView.
     */
    public ScrollView()
    {
        // Configure ScrollView
        setBorder(SCROLL_VIEW_BORDER);

        // Create Scroller and add listeners for scroll changes
        _scroller = new Scroller();
        _scroller.addPropChangeListener(pc -> scrollerDidPropChange(pc), Width_Prop, Height_Prop,
            Scroller.ScrollX_Prop, Scroller.ScrollY_Prop, Scroller.ScrollWidth_Prop, Scroller.ScrollHeight_Prop);
        addChild(_scroller);
    }

    /**
     * Creates a new ScrollView.
     */
    public ScrollView(View aView)  { this(); setContent(aView); }

    /**
     * Returns the content.
     */
    public View getContent()  { return _scroller.getContent(); }

    /**
     * Sets the content.
     */
    public void setContent(View aView)  { _scroller.setContent(aView); }

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
        if (_hbar!=null) return _hbar;
        _hbar = new ScrollBar();
        _hbar.addPropChangeListener(pc -> scrollBarDidPropChange(pc), ScrollBar.Scroll_Prop);
        return _hbar;
    }

    /**
     * Returns the vertical ScrollBar.
     */
    public ScrollBar getVBar()
    {
        if (_vbar!=null) return _vbar;
        _vbar = new ScrollBar(); _vbar.setVertical(true);
        _vbar.addPropChangeListener(pc -> scrollBarDidPropChange(pc), ScrollBar.Scroll_Prop);
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
        if (aValue==_showHBar) return;
        firePropChange(ShowHBar_Prop, _showHBar, _showHBar=aValue);
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
        if (aValue==_showVBar) return;
        firePropChange(ShowVBar_Prop, _showVBar, _showVBar=aValue);
    }

    /**
     * Returns whether HBar is showing.
     */
    public boolean isHBarShowing()  { return getHBar().getParent()!=null; }

    /**
     * Sets whether HBar is showing.
     */
    protected void setHBarShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue==isHBarShowing()) return;

        // If showing, add and update
        ScrollBar hbar = getHBar();
        if (aValue) {
            addChild(hbar);
            hbar.setViewSize(_scroller.getWidth());
            hbar.setScrollSize(_scroller.getScrollWidth());
            hbar.setScroll(_scroller.getScrollX());
        }

        // Otherwise, remove
        else removeChild(hbar);

        // Fire prop change
        firePropChange(HBarShowing_Prop, !aValue, aValue);
    }

    /**
     * Returns whether VBar is showing.
     */
    public boolean isVBarShowing()  { return getVBar().getParent()!=null; }

    /**
     * Sets whether VBar is showing.
     */
    protected void setVBarShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue==isVBarShowing()) return;

        // If showing, add and update
        ScrollBar vbar = getVBar();
        if (aValue) {
            addChild(vbar);
            vbar.setViewSize(_scroller.getHeight());
            vbar.setScrollSize(_scroller.getScrollHeight());
            vbar.setScroll(_scroller.getScrollY());
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
        if (aValue==_barSize) return;
        firePropChange(BarSize_Prop, _barSize, _barSize = aValue);
    }

    /**
     * Returns whether this ScrollView fits content to its width.
     */
    public boolean isFillWidth()  { return _scroller.isFillWidth(); }

    /**
     * Sets whether this ScrollView fits content to its width.
     */
    public void setFillWidth(boolean aValue)
    {
        _scroller.setFillWidth(aValue);
        relayout();
    }

    /**
     * Returns whether this ScrollView fits content to its height.
     */
    public boolean isFillHeight()  { return _scroller.isFillHeight(); }

    /**
     * Sets whether this ScrollView fits content to its height.
     */
    public void setFillHeight(boolean aValue)
    {
        _scroller.setFillHeight(aValue);
        relayout();
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
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW = _scroller.getBestWidth(aH);
        if (_showVBar == Boolean.TRUE)
            prefW += getBarSize();
        return prefW + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH = _scroller.getBestHeight(aW);
        if (_showHBar == Boolean.TRUE)
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
            areaW -= barSize;
        if (isHBarShowing)
            areaH -= barSize;

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
            else {
                //updateScrollBarsShowing();
                System.err.println("ScrollView: Confused about whether we get eliminate scrollbars");
            }
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

        // Get child size
        Size contentSize = _scroller.getContentPrefSize();
        double contentW = contentSize.getWidth();
        double contentH = contentSize.getHeight();

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
            boolean vbarNotReallyNeeded = contentW <= scrollerW + getVBar().getWidth();
            boolean hbarNotReallyNeeded = contentH <= scrollerH + getHBar().getHeight();
            if (vbarNotReallyNeeded && hbarNotReallyNeeded) {
                setVBarShowing(false);
                setHBarShowing(false);
                return true;
            }
        }

        // If either ScrollBar in wrong Showing state, set and try again
        if (showVBar != isVBarShowing) {
            setVBarShowing(showVBar);
            return true;
        }

        if (showHBar != isHBarShowing) {
            setHBarShowing(showHBar);
            return true;
        }

        // Return false since ScrollBar showing didn't change
        return false;
    }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return SCROLL_VIEW_BORDER; }

    /**
     * Handle Scroller property changes.
     */
    protected void scrollerDidPropChange(PropChange anEvent)
    {
        // Get Property Name
        String pname = anEvent.getPropName();

        // Handle Scroller.ScrollX change
        if (pname==Scroller.ScrollX_Prop)
            getHBar().setScroll(_scroller.getScrollX());

        // Handle Scroller.ScrollY change
        else if (pname==Scroller.ScrollY_Prop)
            getVBar().setScroll(_scroller.getScrollY());

        // Handle Scroller.Width or Scroller.ScrollWidth change
        else if (pname==Width_Prop || pname==Scroller.ScrollWidth_Prop) {
            getHBar().setViewSize(_scroller.getWidth());
            getHBar().setScrollSize(_scroller.getScrollWidth());
            getHBar().setScroll(_scroller.getScrollX());
        }

        // Handle Scroller.Height or Scroller.ScrollHeight change
        else if (pname==Scroller.Height_Prop || pname==Scroller.ScrollHeight_Prop) {
            getVBar().setViewSize(_scroller.getHeight());
            getVBar().setScrollSize(_scroller.getScrollHeight());
            getVBar().setScroll(_scroller.getScrollY());
        }
    }

    /**
     * Handle ScrollBar property changes.
     */
    public void scrollBarDidPropChange(PropChange aPC)
    {
        String pname = aPC.getPropName();
        if (pname==ScrollBar.Scroll_Prop) {
            ScrollBar sbar = (ScrollBar)aPC.getSource();
            double val = sbar.getScrollRatio();
            if (sbar==_hbar)
                _scroller.setScrollXRatio(val);
            else _scroller.setScrollYRatio(val);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowHBar, ShowVBar, BarSize
        if (getShowHBar()!=null) e.add(ShowHBar_Prop, getShowHBar());
        if (getShowVBar()!=null) e.add(ShowVBar_Prop, getShowVBar());
        if (getBarSize()!=16) e.add(BarSize_Prop, getBarSize());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive ShowHBar, ShowVBar, BarSize
        if (anElement.hasAttribute(ShowHBar_Prop)) setShowHBar(anElement.getAttributeBoolValue(ShowHBar_Prop));
        if (anElement.hasAttribute(ShowVBar_Prop)) setShowVBar(anElement.getAttributeBoolValue(ShowVBar_Prop));
        if (anElement.hasAttribute(BarSize_Prop)) setBarSize(anElement.getAttributeIntValue(BarSize_Prop));
    }
}