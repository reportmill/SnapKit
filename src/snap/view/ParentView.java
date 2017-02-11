/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass for views with children.
 */
public class ParentView extends View {
    
    // The children
    View           _children[] = EMPTY_VIEWS, _managed[] = EMPTY_VIEWS;
    
    // Whether node needs layout, or node has children that need layout
    boolean        _needsLayout, _needsLayoutDeep;

    // Whether view should clip children to bounds
    boolean        _clipToBounds;
    
    // Whether this node is performing layout
    boolean        _inLayout;
    
    // Constants for properties
    public static final String Child_Prop = "Child";
    
    // Shared empty view array
    private static View[] EMPTY_VIEWS = new View[0];
    
    // Constants for properties
    public static final String ClipToBounds_Prop = "ClipToBounds";
    
/**
 * Returns the number of children associated with this node.
 */
public int getChildCount()  { return _children.length; }

/**
 * Returns the child at the given index.
 */
public View getChild(int anIndex)  { return _children[anIndex]; }

/**
 * Returns the list of children associated with this node.
 */
public View[] getChildren()  { return _children; }

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
    _children = ArrayUtils.add(_children, aChild, anIndex); _managed = null;
    relayout(); relayoutParent(); setNeedsLayoutDeep(true); repaint();
    
    // Fire property change
    firePropChange(Child_Prop, null, aChild, anIndex); //relayout(); repaint();
}

/**
 * Remove's the child at the given index from this node's children list.
 */
protected View removeChild(int anIndex)
{
    // Remove child from children list and clear parent
    View child = _children[anIndex];
    _children = ArrayUtils.remove(_children, anIndex); _managed = null;
    child.setParent(null);
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
 * Returns the child at given point.
 */
public View getChildAt(Point aPnt)  { return getChildAt(aPnt.x, aPnt.y); }

/**
 * Returns the child at given point.
 */
public View getChildAt(double aX, double aY)
{
    View children[] = getChildren();
    for(int i=children.length-1; i>=0; i--) { View child = children[i]; if(!child.isPickable()) continue;
        Point p = child.parentToLocal(aX, aY);
        if(child.contains(p.x,p.y))
            return child;
    }
    return null;
}

/**
 * Returns the child at given point.
 */
public List <View> getChildrenAt(Shape aShape)
{
    View children[] = getChildren(); List <View> hit = new ArrayList();
    for(int i=children.length-1; i>=0; i--) { View child = children[i]; if(!child.isPickable()) continue;
        Shape shp = child.parentToLocal(aShape);
        if(child.intersects(shp))
            hit.add(child);
    }
    return hit;
}

/**
 * Returns the managed children.
 */
public View[] getChildrenManaged()
{
    if(_managed!=null) return _managed;
    int cc = getChildCount(), mc = 0; for(View child : getChildren()) if(child.isManaged()) mc++;
    if(mc==cc) return _managed = _children;
    View mngd[] = new View[mc]; for(int i=0,j=0;i<cc;i++) { View c = getChild(i); if(c.isManaged()) mngd[j++] = c; }
    return _managed = mngd;
}

/**
 * Returns whether view should clip to bounds.
 */
public boolean isClipToBounds()  { return _clipToBounds; }

/**
 * Sets whether view should clip to bounds.
 */
public void setClipToBounds(boolean aValue)
{
    if(aValue==_clipToBounds) return;
    firePropChange(ClipToBounds_Prop, _clipToBounds, _clipToBounds = aValue);
    repaint();
}

/**
 * Clips to bounds.
 */
protected void clipToBounds(Painter aPntr)
{
    Shape shp = getBoundsShape();
    if(shp instanceof RectBase) {
        Insets ins = getInsetsAll(); double w = getWidth(), h = getHeight();
        shp = ((RectBase)shp).copyFor(new Rect(ins.left, ins.right, w-ins.left-ins.right, h-ins.top-ins.bottom));
    }
    aPntr.clip(shp);
}

/**
 * Returns the next focus View after given view (null to return first).
 */
protected View getFocusNext(View aChild)
{
    int ind = aChild!=null? indexOfChild(aChild) : -1;
    for(int i=ind+1,iMax=getChildCount();i<iMax;i++) { View child = getChild(i);
        if(child.isFocusable()) return child;
        ParentView par = child instanceof ParentView? (ParentView)child : null; if(par==null) continue;
        if(par.getFocusNext(null)!=null) return par.getFocusNext(null);
    }
    
    return getFocusNext();
}

/**
 * Returns the next focus View after given view (null to return last).
 */
protected View getFocusPrev(View aChild)
{
    int ind = aChild!=null? indexOfChild(aChild) : getChildCount();
    for(int i=ind-1;i>=0;i--) { View child = getChild(i);
        if(child.isFocusable()) return child;
        ParentView par = child instanceof ParentView? (ParentView)child : null; if(par==null) continue;
        if(par.getFocusPrev(null)!=null) return par.getFocusPrev(null);
    }
    
    return getFocusPrev();
}

/**
 * Override to propagate to children.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==_showing) return; super.setShowing(aValue);
    for(View child : getChildren())
        child.setShowing(aValue && child.isVisible());
}

/**
 * Override to propogate to children.
 */
public void setOwner(ViewOwner anOwner)
{
    if(getOwner()!=null) return; super.setOwner(anOwner);
    for(View child : getChildren())
        child.setOwner(anOwner);
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
    // If clip to bounds, do it
    if(isClipToBounds()) {
        aPntr.save();
        clipToBounds(aPntr);
    }

    // Get clip
    Shape clip = aPntr.getClip();
    
    // Iterate over children and paint any that intersect clip
    for(View child : getChildren()) {
        if(!child.isVisible()) continue;
        Shape clip2 = child.parentToLocal(clip);
        if(clip2.intersects(child.getBoundsLocal())) {
            aPntr.save();
            aPntr.transform(child.getLocalToParent());
            if(child.getClip()!=null)
                aPntr.clip(child.getClip());
            child.paintAll(aPntr);
            aPntr.restore();
        }
    }
    
    // If ClipToBounds, Restore original clip
    if(isClipToBounds()) aPntr.restore();
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
    if(theIns==null) theIns = getDefaultPadding();
    if(SnapUtils.equals(theIns,_padding)) return; super.setPadding(theIns);
    relayout();
}

/**
 * Override to forward to children.
 */
public void playAnimDeep()
{
    super.playAnimDeep();
    for(View child : getChildren()) child.playAnimDeep();
}

/**
 * Override to forward to children.
 */
public void stopAnimDeep()
{
    super.stopAnimDeep();
    for(View child : getChildren()) child.stopAnimDeep();
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