/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View for scrolling other views.
 */
public class ScrollView extends ParentView implements PropChangeListener {
    
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
    
/**
 * Creates a new ScrollView.
 */
public ScrollView()
{
    _scroller = new Scroller(); _scroller.addPropChangeListener(this);
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
    _hbar = new ScrollBar(); _hbar.addPropChangeListener(this);
    return _hbar;
}

/**
 * Returns the vertical ScrollBar.
 */
public ScrollBar getVBar()
{
    if(_vbar!=null) return _vbar;
    _vbar = new ScrollBar(); _vbar.setVertical(true); _vbar.addPropChangeListener(this);
    return _vbar;
}

/**
 * Returns whether to show horizontal scroll bar (null means 'as-needed').
 */
public Boolean getShowHBar()  { return _showHBar; }

/**
 * Sets whether to show horizontal scroll bar (null means 'as-needed').
 */
public void setShowHBar(Boolean aValue)  { firePropChange("HBarPolicy", _showHBar, _showHBar=aValue); relayout(); }

/**
 * Returns whether to show vertical scroll bar (null means 'as-needed').
 */
public Boolean getShowVBar()  { return _showVBar; }

/**
 * Returns whether to show vertical scroll bar (null means 'as-needed').
 */
public void setShowVBar(Boolean aValue)  { firePropChange("VBarPolicy", _showVBar, _showVBar=aValue); relayout(); }

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
    Insets ins = getInsetsAll(); double cpw = _scroller.getPrefWidth();
    return ins.left + cpw + ins.right;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll(); double cph = _scroller.getPrefHeight();
    return ins.top + cph + ins.bottom;
}

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    Size cpsize = getContent()!=null? getContent().getBestSize() : new Size(1,1);
    double cpw = cpsize.getWidth(), cph = cpsize.getHeight(); int barSize = getBarSize();
    
    // Get whether to show scroll bars
    boolean asneedH = _showHBar==null, alwaysH = _showHBar==Boolean.TRUE;
    boolean asneedV = _showVBar==null, alwaysV = _showVBar==Boolean.TRUE;
    boolean showHBar = alwaysH || asneedH && cpw>w && !_scroller.isFillingWidth();
    boolean showVBar = alwaysV || asneedV && cph>h && !_scroller.isFillingHeight();
    
    // If horizontal scrollbar needed, add it
    setHBarShowing(showHBar);
    if(showHBar) { ScrollBar hbar = getHBar();
        double sbw = showVBar? w-barSize : w;
        hbar.setBounds(x,y+h-barSize,sbw,barSize);
        hbar.setThumbRatio(sbw/cpw);
    }
    
    // If vertical scrollbar needed, add it
    setVBarShowing(showVBar);
    if(showVBar) { ScrollBar vbar = getVBar();
        double sbh = showHBar? h-barSize : h;
        vbar.setBounds(x+w-barSize,y,barSize,sbh);
        vbar.setThumbRatio(sbh/cph);
    }
    
    // Set scroller
    if(showHBar) h -= barSize; if(showVBar) w -= barSize;
    _scroller.setBounds(x,y,w,h);
}

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return SCROLL_VIEW_BORDER; }

/**
 * Handle property changes.
 */
public void propertyChange(PropChange anEvent)
{
    String pname = anEvent.getPropertyName();
    if(pname==ScrollBar.Scroll_Prop) {
        if(anEvent.getSource()==_hbar) _scroller.setRatioH(SnapUtils.doubleValue(anEvent.getNewValue()));
        else _scroller.setRatioV(SnapUtils.doubleValue(anEvent.getNewValue()));
    }
    else if(pname==Scroller.ScrollV_Prop)
        getVBar().setScroll(_scroller.getRatioV()); //SnapUtils.doubleValue(anEvent.getNewValue()));
    else if(pname==Scroller.ScrollH_Prop)
        getHBar().setScroll(_scroller.getRatioH()); //SnapUtils.doubleValue(anEvent.getNewValue()));
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

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive content
    View child = getContent(); if(child==null) return;
    XMLElement cxml = anArchiver.toXML(child, this);
    anElement.add(cxml);
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive shapes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            setContent(view); break;
        }
    }
}

}