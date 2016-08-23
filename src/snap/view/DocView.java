package snap.view;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A custom class.
 */
public class DocView extends ParentView {

    // The layout
    ViewLayout.BorderLayout  _layout = new ViewLayout.BorderLayout(this);
    
    // The page
    View               _page;

/**
 * Creates a new DocNode.
 */
public DocView()
{
    setAlignment(Pos.CENTER);
    _layout.setFillCenter(false);
}

/**
 * Returns the page node.
 */
public View getPage()  { return _layout.getCenter(); }

/**
 * Sets the page node.
 */
public void setPage(View aView)
{
    View old = getPage(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setCenter(aView);
    firePropChange("Page", old, aView);
}

/**
 * Returns the default alignment.
 */    
public Pos getAlignmentDefault()  { return Pos.CENTER_LEFT; }

/**
 * Returns the preferred width.
 */
public double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
public double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

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
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View child = (View)anArchiver.fromXML(childXML, this);
            if(getPage()==null) setPage(child);
            else System.err.println("DocNode: Skipping child " + child);
        }
    }
    
    // Size document to page
    View page = getPage();
    if(page!=null && getWidth()==0 && getHeight()==0)
        setSize(page.getWidth(),page.getHeight());
}

}