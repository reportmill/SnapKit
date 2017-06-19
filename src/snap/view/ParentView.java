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
    ViewList       _children = new ViewList();
    
    // Whether node needs layout, or node has children that need layout
    boolean        _needsLayout, _needsLayoutDeep;

    // Whether this node is performing layout
    boolean        _inLayout, _inLayoutDeep;
    
    // Constants for properties
    public static final String Child_Prop = "Child";
    
    // Shared empty view array
    private static View[] EMPTY_VIEWS = new View[0];
    
    // Constants for properties
    public static final String ClipToBounds_Prop = "ClipToBounds";
    public static final String NeedsLayout_Prop = "NeedsLayout";

/**
 * Returns the ViewList that holds children.
 */
public ViewList getViewList()  { return _children; }

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
public View[] getChildren()  { return _children.getAll(); }

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
    _children.add(aChild, anIndex);
    relayout(); relayoutParent(); repaint(); setNeedsLayoutDeep(true);
    
    // If this shape has PropChangeListeners, start listening to children as well
    if(_pcs.hasDeepListener()) {
        aChild.addPropChangeListener(this); aChild.addDeepChangeListener(this); }
    
    // Fire property change
    firePropChange(Child_Prop, null, aChild, anIndex); //relayout(); repaint();
}

/**
 * Remove's the child at the given index from this node's children list.
 */
protected View removeChild(int anIndex)
{
    // Remove child from children list and clear parent
    View child = _children.remove(anIndex);
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
public int indexOfChild(View aChild)  { return _children.indexOf(aChild); }

/**
 * Returns the last child of this node.
 */
public View getChildLast()  { return _children.getLast(); }

/**
 * Returns the child at given point.
 */
public View getChildAt(Point aPnt)  { return _children.getViewAt(aPnt.x, aPnt.y); }

/**
 * Returns the child at given point.
 */
public View getChildAt(double aX, double aY)  { return _children.getViewAt(aX, aY); }

/**
 * Returns the first child view of given class (optional) hit by given shape, excluding given view (optional).
 */
public <T extends View> T getChildAt(Shape aShape, Class <T> aClass, View aChild)
{
    return _children.getViewAt(aShape, aClass, aChild);
}

/**
 * Returns the child at given point.
 */
public <T extends View> List <T> getChildrenAt(Shape aShape, Class <T> aClass, View aChild)
{
    return _children.getViewsAt(aShape, aClass, aChild);
}

/**
 * Returns the managed children.
 */
public View[] getChildrenManaged()  { return _children.getManaged(); }

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
 * Override to forward to children.
 */
public boolean intersects(Shape aShape)
{
    // Do normal version (just return if miss or this ParentView has border/fill)
    boolean hit = super.intersects(aShape);
    if(!hit || getBorder()!=null || getFill()!=null)
        return hit;
    
    // If any child is hit, return true
    View hview = getViewList().getViewAt(aShape, null, null);
    if(hview!=null)
        return true;
    return false;
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
    // If view clip set, save painter state and set
    Shape vclip = getClip();
    if(vclip!=null) {
        aPntr.save();
        aPntr.clip(vclip);
    }

    // Get painter clip
    Shape pclip = aPntr.getClip();
    
    // Iterate over children and paint any that intersect clip
    for(View child : getChildren()) {
        if(!child.isVisible()) continue;
        Shape clip = child.parentToLocal(pclip);
        if(clip.intersects(child.getBoundsLocal())) {
            aPntr.save();
            aPntr.transform(child.getLocalToParent());
            child.paintAll(aPntr);
            aPntr.restore();
        }
    }
    
    // If ClipToBounds, Restore original clip
    if(vclip!=null) aPntr.restore();
}

/**
 * Paints above children.
 */
protected void paintAbove(Painter aPntr)
{
    // Check for odd case of Border with PaintAbove set
    Border bdr = getBorder();
    if(bdr!=null && bdr.isPaintAbove())
        bdr.paint(aPntr, getBoundsShape());
}

/**
 * Override Node version to really request layout from RootView.
 */
public void relayout()  { setNeedsLayout(true); }

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
    firePropChange(NeedsLayout_Prop, _needsLayout, _needsLayout = true);
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
    if(_inLayoutDeep) return;
    ParentView par = getParent(); if(par!=null) par.setNeedsLayoutDeep(true);
}

/**
 * Returns whether view is currently performing layout.
 */
public boolean isInLayout()  { return _inLayout; }

/**
 * Override to layout children.
 */
public void layout()
{
    if(_inLayout) return;
    _inLayout = true;
    layoutImpl(); _inLayout = false;
}

/**
 * Actual method to layout children.
 */
protected void layoutImpl()  { }

/**
 * Lays out children deep.
 */
public void layoutDeep()
{
    // Set InLayoutDeep
    _inLayoutDeep = true;
    
    // Do layout
    if(_needsLayout) layout();
    
    // Do layout deep (several times, if necessary)
    for(int i=0;_needsLayoutDeep;i++) {
        _needsLayoutDeep = false;
        layoutDeepImpl();
        if(i==5) { System.err.println("ParentView.layoutDeep: Too many calls to relayout inside layout"); break; }
    }
    
    // Clear flags
    _needsLayout = _needsLayoutDeep = _inLayoutDeep = false;
}

/**
 * Lays out children deep.
 */
protected void layoutDeepImpl()
{
    for(View child : getChildren())
        if(child instanceof ParentView) { ParentView par = (ParentView)child;
            if(par._needsLayout || par._needsLayoutDeep)
                par.layoutDeep(); }
}

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
 * Override to add this view as change listener to children on first call.
 */
public void addDeepChangeListener(DeepChangeListener aDCL)
{
    boolean first = !_pcs.hasDeepListener();
    super.addDeepChangeListener(aDCL);
    
    // If first listener, add for children
    if(first)
        for(View child : getChildren()) {
            child.addPropChangeListener(this); child.addDeepChangeListener(this); }
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