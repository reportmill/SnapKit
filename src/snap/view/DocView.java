/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.*;

/**
 * A view to represent a traditional paper document.
 */
public class DocView extends ParentView {

    // The pages
    List <PageView>          _pages = new ArrayList();
    
    // The page margin
    Insets                   _pageMargin = new Insets(36);
    
    // The selected page index
    int                      _selIndex = -1;
    
    // Constants for properties
    public static final String PageMargin_Prop = "PageMargin";

/**
 * Creates a new DocNode.
 */
public DocView()
{
    setAlign(Pos.CENTER);
}

/**
 * Returns the number of pages.
 */
public int getPageCount()  { return _pages.size(); }

/**
 * Returns the individual page at given index.
 */
public PageView getPage(int anIndex)  { return _pages.get(anIndex); }

/**
 * Adds a given page.
 */
public void addPage(PageView aPage)  { addPage(aPage, getPageCount()); }

/**
 * Adds a given page at given index.
 */
public void addPage(PageView aPage, int anIndex)
{
    _pages.add(anIndex, aPage);
    
    if(_selIndex<=0)
        setSelIndex(0);
}

/**
 * Removes the page at given index.
 */
public PageView removePage(int anIndex)
{
    return _pages.remove(anIndex);
}

/**
 * Removes the given page.
 */
public int removePage(PageView aPage)
{
    int index = ListUtils.indexOfId(_pages, aPage);
    if(index>=0) removePage(index);
    return index;
}

/**
 * Returns the page margin.
 */
public Insets getPageMargin()  { return _pageMargin; }

/**
 * Sets the page margin.
 */
public void setPageMargin(Insets aIns)
{
    if(SnapUtils.equals(aIns, _pageMargin)) return;
    firePropChange(PageMargin_Prop, _pageMargin, _pageMargin = aIns);
}

/**
 * Returns the selected page index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Sets the selected page index.
 */
public void setSelIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    
    // Get/remove old page
    PageView opage = getSelPage();
    if(opage!=null) removeChild(opage);
    
    // Set new value
    _selIndex = anIndex;
    
    // Get new page
    PageView page = getSelPage();
    if(page!=null) addChild(page);
}

/**
 * Returns the currently selected page.
 */
public PageView getSelPage()  { return _selIndex>=0? getPage(_selIndex) : null; }

/**
 * Sets the currently selected page.
 */
public void setSelPage(PageView aPage)
{
    int index = ListUtils.indexOfId(_pages, aPage);
    if(index>=0) setSelIndex(index);
}

/**
 * Returns the page node.
 */
public PageView getPage()  { return getSelPage(); }

/**
 * Sets the page node.
 */
public void setPage(PageView aPage)
{
    if(getPage()!=null) removePage(getPage());
    addPage(aPage);
}

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, getPage(), -1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, getPage(), -1); }

/**
 * Layout children.
 */
protected void layoutImpl()  { BoxView.layout(this, getPage(), null, false, false); }

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    View child = getPage(); if(child==null) return;
    XMLElement cxml = anArchiver.toXML(child, this);
    cxml.removeAttribute("x"); cxml.removeAttribute("y"); cxml.removeAttribute("asize");
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
        if(childClass!=null && PageView.class.isAssignableFrom(childClass)) {
            PageView child = (PageView)anArchiver.fromXML(childXML, this);
            addPage(child);
        }
    }
    
    // Size document to page
    View page = getPage();
    if(page!=null && getWidth()==0 && getHeight()==0)
        setSize(page.getWidth(),page.getHeight());
}

}