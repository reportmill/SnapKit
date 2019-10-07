/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View for scrolling other views.
 */
public class ScrollView extends ParentView implements ViewHost {
    
    // The scroll view
    Scroller        _scroller;
    
    // The scrollbars
    ScrollBar       _hbar, _vbar;
    
    // Whether to show horizontal/vertical scroll bars (null means 'as-needed')
    Boolean         _showHBar, _showVBar;
    
    // The ScrollBar size
    int             _barSize = 16;
    
    // Constants
    static final Border SCROLL_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);
    public static final String ShowHBar_Prop = "ShowHBar";
    public static final String ShowVBar_Prop = "ShowVBar";
    public static final String HBarShowing_Prop = "HBarShowing";
    public static final String VBarShowing_Prop = "VBarShowing";
    
/**
 * Creates a new ScrollView.
 */
public ScrollView()
{
    _scroller = new Scroller();
    _scroller.addPropChangeListener(pc -> scrollerDidPropChange(pc), Scroller.ScrollH_Prop, Scroller.ScrollV_Prop);
    addChild(_scroller);
    setBorder(SCROLL_VIEW_BORDER);
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
 * Returns the horizontal scroll.
 */
public double getScrollH()  { return getHBar().getScroll(); }

/**
 * Returns the vertical scroll.
 */
public double getScrollV()  { return getVBar().getScroll(); }

/**
 * Returns the Horizontal ScrollBar.
 */
public ScrollBar getHBar()
{
    if(_hbar!=null) return _hbar;
    _hbar = new ScrollBar();
    _hbar.addPropChangeListener(pc -> scrollBarDidPropChange(pc), ScrollBar.Scroll_Prop);
    return _hbar;
}

/**
 * Returns the vertical ScrollBar.
 */
public ScrollBar getVBar()
{
    if(_vbar!=null) return _vbar;
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
public void setShowHBar(Boolean aValue)  { firePropChange(ShowHBar_Prop, _showHBar, _showHBar=aValue); relayout(); }

/**
 * Returns whether to show vertical scroll bar (null means 'as-needed').
 */
public Boolean getShowVBar()  { return _showVBar; }

/**
 * Returns whether to show vertical scroll bar (null means 'as-needed').
 */
public void setShowVBar(Boolean aValue)  { firePropChange(ShowVBar_Prop, _showVBar, _showVBar=aValue); relayout(); }

/**
 * Returns whether HBar is showing.
 */
public boolean isHBarShowing()  { return getHBar().getParent()!=null; }

/**
 * Sets whether HBar is showing.
 */
protected void setHBarShowing(boolean aValue)
{
    if(aValue==isHBarShowing()) return;
    ScrollBar hbar = getHBar();
    if(aValue) addChild(hbar);
    else removeChild(hbar);
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
    if(aValue==isVBarShowing()) return;
    ScrollBar vbar = getVBar();
    if(aValue) addChild(vbar);
    else removeChild(vbar);
    firePropChange(VBarShowing_Prop, !aValue, aValue);
}

/**
 * Returns the scroll bar size.
 */
public int getBarSize()  { return _barSize; }

/**
 * Sets the scroll bar size.
 */
public void setBarSize(int aValue)  { _barSize = aValue; }

/**
 * Returns whether this ScrollView fits content to its width.
 */
public boolean isFillWidth()  { return _scroller.isFillWidth(); }

/**
 * Sets whether this ScrollView fits content to its width.
 */
public void setFillWidth(boolean aValue)  { _scroller.setFillWidth(aValue); relayout(); }

/**
 * Returns whether this ScrollView fits content to its height.
 */
public boolean isFillHeight()  { return _scroller.isFillHeight(); }

/**
 * Sets whether this ScrollView fits content to its height.
 */
public void setFillHeight(boolean aValue)  { _scroller.setFillHeight(aValue); relayout(); }

/**
 * Calculates the minimum width.
 */
protected double getMinWidthImpl()
{
    Insets ins = getInsetsAll(); double cmw = _scroller.getMinWidth();
    return ins.left + cmw + ins.right;
}

/**
 * Calculates the minimum height.
 */
protected double getMinHeightImpl()
{
    Insets ins = getInsetsAll(); double cmh = _scroller.getMinHeight();
    return ins.top + cmh + ins.bottom;
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    double pw = _scroller.getBestWidth(aH); if(_showVBar==Boolean.TRUE) pw += getBarSize();
    return ins.left + pw + ins.right;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    double ph = _scroller.getBestHeight(aW); if(_showHBar==Boolean.TRUE) ph += getBarSize();
    return ins.top + ph + ins.bottom;
}

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    // Get Scroller size (minus insets)
    Insets ins = getInsetsAll(); int barSize = getBarSize();
    double x = ins.left, w = getWidth() - x - ins.right;
    double y = ins.top, h = getHeight() - y - ins.bottom;
    
    // Account for ScrollBars
    if(isVBarShowing()) w -= barSize;
    if(isHBarShowing()) h -= barSize;
    
    // Set Scroller bounds
    _scroller.setBounds(x,y,w,h);
    
    // Get content size
    Size cpsize = _scroller.getContentSize();
    double cpw = cpsize.getWidth(), cph = cpsize.getHeight();
    
    // Get whether to show scroll bars
    boolean asneedH = _showHBar==null, alwaysH = _showHBar==Boolean.TRUE, showHBar = alwaysH || asneedH && cpw>w;
    boolean asneedV = _showVBar==null, alwaysV = _showVBar==Boolean.TRUE, showVBar = alwaysV || asneedV && cph>h;
    
    // If showing both ScrollBars, but only because both ScrollBars are showing, hide them and try again
    if(isVBarShowing() && isHBarShowing() && showVBar && showHBar && asneedH && asneedV &&
        cpw<=w+getVBar().getWidth() && cph<=h+getHBar().getHeight()) {
            setVBarShowing(false); setHBarShowing(false); layoutImpl(); return; }
    
    // If either ScrollBar in wrong Showing state, set and try again
    if(showVBar!=isVBarShowing()) { setVBarShowing(showVBar); layoutImpl(); return; }
    if(showHBar!=isHBarShowing()) { setHBarShowing(showHBar); layoutImpl(); return; }
    
    // If horizontal scrollbar showing, update it
    if(showHBar) { ScrollBar hbar = getHBar();
        hbar.setBounds(x,y+h,w,barSize);
        hbar.setThumbRatio(w/cpw);
        hbar.setScroll(_scroller.getRatioH());
    }
    
    // If vertical scrollbar needed, add it
    if(showVBar) { ScrollBar vbar = getVBar();
        vbar.setBounds(x+w,y,barSize,h);
        vbar.setThumbRatio(h/cph);
        vbar.setScroll(_scroller.getRatioV());
    }
}

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return SCROLL_VIEW_BORDER; }

/**
 * Handle Scroller property changes.
 */
public void scrollerDidPropChange(PropChange anEvent)
{
    String pname = anEvent.getPropertyName();
    if(pname==Scroller.ScrollV_Prop)
        getVBar().setScroll(_scroller.getRatioV());
    else if(pname==Scroller.ScrollH_Prop)
        getHBar().setScroll(_scroller.getRatioH());
}

/**
 * Handle ScrollBar property changes.
 */
public void scrollBarDidPropChange(PropChange anEvent)
{
    String pname = anEvent.getPropertyName();
    if(pname==ScrollBar.Scroll_Prop) {
        if(anEvent.getSource()==_hbar) _scroller.setRatioH(SnapUtils.doubleValue(anEvent.getNewValue()));
        else _scroller.setRatioV(SnapUtils.doubleValue(anEvent.getNewValue()));
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
    if(getShowHBar()!=null) e.add("ShowHBar", getShowHBar());
    if(getShowVBar()!=null) e.add("ShowVBar", getShowVBar());
    if(getBarSize()!=16) e.add("BarSize", getBarSize());
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
    if(anElement.hasAttribute("ShowHBar")) setShowHBar(anElement.getAttributeBoolValue("ShowHBar"));
    if(anElement.hasAttribute("ShowVBar")) setShowVBar(anElement.getAttributeBoolValue("ShowVBar"));
    if(anElement.hasAttribute("BarSize")) setBarSize(anElement.getAttributeIntValue("BarSize"));
}

}