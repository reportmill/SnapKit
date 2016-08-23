package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass for views with children.
 */
public class ParentView extends View {
    
    // The children
    List <View>    _children = Collections.EMPTY_LIST;
    
    // Whether node needs layout, or node has children that need layout
    boolean        _needsLayout, _needsLayoutDeep;

    // Whether this node is performing layout
    boolean        _inLayout;
    
    // Constants for properties
    public static final String Child_Prop = "Child";
    
/**
 * Returns the number of children associated with this node.
 */
public int getChildCount()  { return _children.size(); }

/**
 * Returns the child at the given index.
 */
public View getChild(int anIndex)  { return _children.get(anIndex); }

/**
 * Returns the list of children associated with this node.
 */
public List <View> getChildren()  { return Arrays.asList(getChildArray()); }

/**
 * Adds the given child to the end of this node's children list.
 */
protected void addChild(View aChild)  { addChild(aChild, getChildCount()); }

/**
 * Adds the given child to this node's children list at the given index.
 */
protected void addChild(View aChild, int anIndex)
{
    // If child already has parent, remove from parent
    if(aChild.getParent()!=null) aChild.getParent().removeChild(aChild);
    
    // Add child to children list and set child's parent to this node
    aChild.setParent(this);
    
    // Add child to Children list
    if(_children==Collections.EMPTY_LIST) _children = new ArrayList();
    _children.add(anIndex, aChild);
    relayout(); relayoutParent(); setNeedsLayoutDeep(true); repaint();
    aChild.setClipAll(null);
    
    // Fire property change
    firePropChange(Child_Prop, null, aChild, anIndex); //relayout(); repaint();
}

/**
 * Remove's the child at the given index from this node's children list.
 */
protected View removeChild(int anIndex)
{
    // Remove child from children list and clear parent
    View child = _children.remove(anIndex); child.setParent(null);
    relayout(); relayoutParent(); repaint();
    
    // Fire property change and return
    firePropChange(Child_Prop, child, null, anIndex);
    return child;
}

/**
 * Removes the given child from this node's children list.
 */
protected int removeChild(View aChild)
{
    int index = indexOfChild(aChild);
    if(index>=0) removeChild(index);
    return index;
}

/**
 * Removes all children from this node (in reverse order).
 */
protected void removeChildren()  { for(int i=getChildCount()-1; i>=0; i--) removeChild(i); }

/**
 * Sets children to given list.
 */
protected void setChildren(View ... theChildren)  { removeChildren(); for(View c : theChildren) addChild(c); }

/**
 * Returns a copy of the children as an array.
 */
public View[] getChildArray()
{
    View nodes[] = new View[getChildCount()];
    for(int i=0;i<nodes.length;i++) nodes[i] = getChild(i);
    return nodes;
}

/**
 * Returns the child with given name.
 */
public View getChild(String aName)
{
    for(View cnode : getChildren()) {
        if(aName.equals(cnode.getName())) return cnode;
        if(cnode instanceof ParentView && cnode.getOwner()==getOwner()) {
            View n = ((ParentView)cnode).getChild(aName); if(n!=null) return n; }
    }
    return null;
}

/**
 * Returns the index of the given child in this node's children list.
 */
public int indexOfChild(View aChild)
{
    for(int i=0,iMax=getChildCount();i<iMax;i++)
        if(aChild==getChild(i))
            return i;
    return -1;
}

/**
 * Returns the last child of this node.
 */
public View getChildLast()  { return getChildCount()>0? getChild(getChildCount()-1) : null; }

/**
 * Returns the managed children.
 */
public View[] getChildrenManaged()
{
    int ccount = getChildCount(); View nodes[] = new View[ccount];
    for(int i=0,j=0;i<ccount;i++) { View c = getChild(i);
        if(c.isManaged()) nodes[j++] = c; else nodes = Arrays.copyOf(nodes,nodes.length-1); }
    return nodes;
}

/**
 * Override to add call to paintChildren.
 */
protected void paintAll(Painter aPntr)
{
    super.paintAll(aPntr);
    paintChildren(aPntr);
    paintAbove(aPntr);
}

/**
 * Paint children.
 */
protected void paintChildren(Painter aPntr)
{
    // Get clip
    Shape clip = aPntr.getClip();
    
    // Iterate over children and paint any that intersect clip
    for(View child : getChildren()) {
        if(!child.isVisible()) continue;
        Shape clip2 = child.parentToLocal(clip);
        if(clip2.intersects(child.getBoundsInside())) {
            aPntr.save();
            aPntr.transform(child.getLocalToParent());
            if(child.getClip()!=null)
                aPntr.clip(child.getClip());
            child.paintAll(aPntr);
            aPntr.restore();
        }
    }
}

/**
 * Paints above children.
 */
protected void paintAbove(Painter aPntr)  { }

/**
 * Override to layout children.
 */
public void layout()
{
    if(_inLayout) return; _inLayout = true;
    layoutChildren(); _inLayout = false;
}

/**
 * Actual method to layout children.
 */
protected void layoutChildren()  { }

/**
 * Lays out children deep.
 */
public void layoutDeep()
{
    if(_needsLayout) layout();
    if(_needsLayoutDeep)
    for(View child : getChildren())
        if(child instanceof ParentView && (((ParentView)child)._needsLayout || ((ParentView)child)._needsLayoutDeep))
            ((ParentView)child).layoutDeep();
    _needsLayout = _needsLayoutDeep = false;
}

/**
 * Returns whether needs layout.
 */
public boolean isNeedsLayout()  { return _needsLayout; }

/**
 * Sets whether needs layout.
 */
protected void setNeedsLayout(boolean aVal)
{
    if(_needsLayout || _inLayout) return;
    _needsLayout = true;
    ParentView par = getParent(); if(par!=null) par.setNeedsLayoutDeep(true);
}

/**
 * Returns whether any children need layout.
 */
public boolean isNeedsLayoutDeep()  { return _needsLayoutDeep; }

/**
 * Sets whether any children need layout.
 */
protected void setNeedsLayoutDeep(boolean aVal)
{
    if(_needsLayoutDeep) return;
    _needsLayoutDeep = true;
    ParentView par = getParent(); if(par!=null) par.setNeedsLayoutDeep(true);
}

/**
 * Override Node version to really request layout from RootView.
 */
public void relayout()  { setNeedsLayout(true); }

/**
 * Override to request layout.
 */
public void setPadding(Insets theIns)
{
    if(theIns==null) theIns = getPaddingDefault();
    if(SnapUtils.equals(theIns,_padding)) return; super.setPadding(theIns);
    relayout();
}

/**
 * Override to propagate to children.
 */
protected void setClipAll(Shape aShape)
{
    if(SnapUtils.equals(aShape, _clipAll)) return;
    super.setClipAll(aShape);
    for(View child : getChildren())
        child.setClipAll(null);
}

/**
 * Override to break toXML into toXMLNode and toXMLChildren.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = toXMLView(anArchiver); // Archive shape
    toXMLChildren(anArchiver, e); // Archive children
    return e; // Return xml element
}

/**
 * XML Archival of basic node.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)  { return super.toXML(anArchiver); }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

/**
 * Override to break fromXML into fromXMLNode and fromXMLChildren.
 */
public View fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive shape and children and return
    fromXMLView(anArchiver, anElement); // Unarchive shape
    fromXMLChildren(anArchiver, anElement); // Unarchive children
    return this;
}

/**
 * XML unarchival of basic node.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)  { super.fromXML(anArchiver,anElement); }

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

}