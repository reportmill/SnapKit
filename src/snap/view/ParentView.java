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
    
    // Wether view has children that need repaint
    boolean        _needsRepaintDeep;
    
    // Whether view needs layout, or has children that need layout
    boolean        _needsLayout, _needsLayoutDeep;

    // Whether this view is performing layout
    boolean        _inLayout, _inLayoutDeep;
    
    // Constants for properties
    public static final String Child_Prop = "Child";
    
    // Constants for properties
    public static final String ClipToBounds_Prop = "ClipToBounds";
    public static final String NeedsLayout_Prop = "NeedsLayout";

/**
 * Returns the ViewList that holds children.
 */
public ViewList getViewList()  { return _children; }

/**
 * Returns the number of children associated with this view.
 */
public int getChildCount()  { return _children.size(); }

/**
 * Returns the child at the given index.
 */
public View getChild(int anIndex)  { return _children.get(anIndex); }

/**
 * Returns the array of children associated with this view.
 */
public View[] getChildren()  { return _children.getAll(); }

/**
 * Adds the given child to the end of this view's children list.
 */
protected void addChild(View aChild)  { addChild(aChild, getChildCount()); }

/**
 * Adds the given child to this view's children list at the given index.
 */
protected void addChild(View aChild, int anIndex)
{
    // If child already has parent, remove from parent
    if(aChild.getParent()!=null) aChild.getParent().removeChild(aChild);
    
    // Add child to children list and set child's parent to this view
    aChild.setParent(this);
    
    // Add child to Children list
    _children.add(aChild, anIndex);
    relayout(); relayoutParent(); repaint(); setNeedsLayoutDeep(true);
    
    // If this view has child prop listeners, add to this child as well
    if(_childPCL!=null) {
        aChild.addPropChangeListener(_childPCL); aChild.addDeepChangeListener(_childDCL); }
    
    // Fire property change
    firePropChange(Child_Prop, null, aChild, anIndex);
}

/**
 * Remove's the child at the given index from this view's children list.
 */
protected View removeChild(int anIndex)
{
    // Remove child from children list and clear parent
    View child = _children.remove(anIndex);
    child.setParent(null);
    
    // If this view has child prop listeners, clear from child
    if(_childPCL!=null) {
        child.removePropChangeListener(_childPCL); child.removeDeepChangeListener(_childDCL); }
    
    // Register for layout
    relayout(); relayoutParent(); repaint();
    
    // Fire property change and return
    firePropChange(Child_Prop, child, null, anIndex);
    return child;
}

/**
 * Removes the given child from this view's children list.
 */
protected int removeChild(View aChild)
{
    int index = indexOfChild(aChild);
    if(index>=0) removeChild(index);
    return index;
}

/**
 * Removes all children from this view (in reverse order).
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
    for(View child : getChildren()) {
        if(aName.equals(child.getName())) return child;
        if(child instanceof ParentView && child.getOwner()==getOwner()) {
            View n = ((ParentView)child).getChild(aName); if(n!=null) return n; }
    }
    return null;
}

/**
 * Returns the index of the given child in this view's children list.
 */
public int indexOfChild(View aChild)  { return _children.indexOf(aChild); }

/**
 * Returns the last child of this view.
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
public void setFont(Font aFont)
{
    // If no change and not null, just return
    if(SnapUtils.equals(aFont, _font) && aFont!=null) return;
    
    // Do normal version
    super.setFont(aFont);
    
    // Let all children that inherrit font know
    for(int i=0,iMax=getChildCount();i<iMax;i++) { View child = getChild(i);
        if(!child.isFontSet()) child.setFont(null); }
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
    if(_viewEff==null) {
        paintChildren(aPntr);
        paintAbove(aPntr);
    }
    _needsRepaintDeep = false;
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
        if(!child.isVisible() || !child.isPaintable()) continue;
        Rect clip = child.parentToLocal(pclip).getBounds();
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
 * Returns whether any children need repaint.
 */
public boolean isNeedsRepaintDeep()  { return _needsRepaintDeep; }

/**
 * Sets whether any children need repaint.
 */
protected void setNeedsRepaintDeep(boolean aVal)
{
    if(_needsRepaintDeep) return; _needsRepaintDeep = true;
    ParentView par = getParent(); if(par!=null) par.setNeedsRepaintDeep(true);
}

/**
 * Override to really request layout from RootView.
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
    if(getWidth()>0 && getHeight()>0) {
        layoutImpl();
        layoutFloatingViews();
    }
    _inLayout = false;
}

/**
 * Actual method to layout children.
 */
protected void layoutImpl()  { }

/**
 * Called to layout floating children (those unmanaged with lean) according to their Lean, Grow and Margin.
 */
protected void layoutFloatingViews()
{
    // If no floating, just return
    if(getChildrenManaged().length==getChildCount()) return;
    double pw = getWidth(), ph = getHeight();
    
    // Layout floating (unmanaged + leaning) children
    for(View child : getChildren()) { if(child.isManaged()) continue;
    
        // Get child lean, grow, margin and current bounds
        HPos leanX = child.getLeanX(); VPos leanY = child.getLeanY(); if(leanX==null && leanY==null) continue;
        Insets marg = child.getMargin(); boolean growX = child.isGrowWidth(), growY = child.isGrowHeight();
        double x = child.getX(), y = child.getY(), w = child.getWidth(), h = child.getHeight();
        
        // Handle LeanX: If grow, make width fill parent (minus margin). Set X for lean, width, margin.
        if(leanX!=null) {
            if(growX) w = pw - marg.getWidth();
            x = marg.left + (pw - marg.getWidth() - w)*ViewUtils.getAlignX(leanX);
        }
        
        // Handle LeanY: If grow, make height fill parent (minus margin). Set Y for lean, height, margin.
        if(leanY!=null) {
            if(growY) h = ph - marg.getHeight();
            y = marg.top + (ph - marg.getHeight() - h)*ViewUtils.getAlignY(leanY);
        }
        
        // Set bounds
        child.setBounds(x, y, w, h);
    }
}

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
 * Override to handle ParentView changes.
 */
public void doChange(PropChange aPC, Object oldVal, Object newVal)
{
    String pname = aPC.getPropName();
    if(pname==Child_Prop) {
        int ind = aPC.getIndex();
        if(newVal!=null) addChild((View)newVal, ind);
        else removeChild(ind);
    }
    
    // Do normal version
    else super.doChange(aPC, oldVal, newVal);
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
 * Returns the anim time.
 */
public int getAnimTimeDeep()
{
    ViewAnim anim = getAnim(-1); if(anim!=null) return anim.getTime();
    for(View child : getChildren()) if(child.getAnim(-1)!=null) return child.getAnim(-1).getTime();
    return 0;
}

/**
 * Sets the anim time deep.
 */
public void setAnimTimeDeep(int aValue)
{
    super.setAnimTimeDeep(aValue);
    for(View child : getChildren()) child.setAnimTimeDeep(aValue);
}

/**
 * Override to break toXML into toXMLView and toXMLChildren.
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
    // Do normal version
    super.addDeepChangeListener(aDCL);
    
    // If child listeners not yet set, create/add for children
    if(_childPCL==null) {
        _childPCL = pc -> childDidPropChange(pc); _childDCL = (lsnr,pc) -> childDidDeepChange(lsnr,pc);
        for(View child : getChildren()) {
            child.addPropChangeListener(_childPCL); child.addDeepChangeListener(_childDCL); }
    }
}

/**
 * Override to remove this view as change listener to children when not needed.
 */
public void removeDeepChangeListener(DeepChangeListener aDCL)
{
    // Do normal version
    super.removeDeepChangeListener(aDCL);
    
    // If no more deep listeners, remove 
    if(!_pcs.hasDeepListener() && _childPCL!=null) {
        for(View child : getChildren()) {
            child.removePropChangeListener(_childPCL); child.removeDeepChangeListener(_childDCL); }
        _childPCL = null; _childDCL = null;
    }
}

// PropChange Listener for Child changes to propogate changes when there is DeepChangeListener
PropChangeListener _childPCL;
DeepChangeListener _childDCL;

/**
 * Property change listener implementation to forward changes on to deep listeners.
 */
protected void childDidPropChange(PropChange aPC)  { _pcs.fireDeepChange(this, aPC); }

/**
 * Deep property change listener implementation to forward to this View's deep listeners.
 */
protected void childDidDeepChange(Object aLsnr, PropChange aPC)  { _pcs.fireDeepChange(aLsnr, aPC); }

/**
 * XML Archival of basic view.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)  { return super.toXML(anArchiver); }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

/**
 * Override to break fromXML into fromXMLView and fromXMLChildren.
 */
public View fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive shape and children and return
    fromXMLView(anArchiver, anElement); // Unarchive shape
    fromXMLChildren(anArchiver, anElement); // Unarchive children
    return this;
}

/**
 * XML unarchival of basic view.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)  { super.fromXML(anArchiver,anElement); }

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

}