/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.*;

/**
 * A ChildView is a ParentNode where add/removeChild methods are public.
 */
public class ChildView extends ParentView {

/**
 * Adds the given child to the end of this node's children list.
 */
public void addChild(View aChild)  { super.addChild(aChild); }

/**
 * Adds the given child to this node's children list at the given index.
 */
public void addChild(View aChild, int anIndex)  { super.addChild(aChild, anIndex); }

/**
 * Remove's the child at the given index from this node's children list.
 */
public View removeChild(int anIndex)  { return super.removeChild(anIndex); }

/**
 * Removes the given child from this node's children list.
 */
public int removeChild(View aChild)  { return super.removeChild(aChild); }

/**
 * Removes all children from this node (in reverse order).
 */
public void removeChildren()  { super.removeChildren(); }

/**
 * Sets children to given list.
 */
public void setChildren(View ... theChildren)  { super.setChildren(theChildren); }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { View child = getChild(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child nodes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && View.class.isAssignableFrom(cls)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            addChild(view);
        }
    }
}

}