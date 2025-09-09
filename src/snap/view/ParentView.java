/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropSet;
import snap.util.*;
import java.util.List;
import java.util.Objects;

/**
 * A View subclass for views with children.
 */
public class ParentView extends View {

    // The children
    protected ViewList  _children = new ViewList();
    
    // Whether view has children that need repaint
    private boolean  _needsRepaintDeep;
    
    // Whether view needs layout
    private boolean  _needsLayout;

    // Whether view has children that need layout
    private boolean  _needsLayoutDeep;

    // Whether this view is performing layout
    private boolean  _inLayout, _inLayoutDeep;

    // PropChange Listener for Child changes to propagate changes when there is DeepChangeListener
    private PropChangeListener _childPCL;
    private DeepChangeListener _childDCL;

    // Constants for properties
    public static final String Children_Prop = "Children";
    public static final String NeedsLayout_Prop = "NeedsLayout";

    /**
     * Constructor.
     */
    public ParentView()
    {
        super();
    }

    /**
     * Returns the ViewList that holds children.
     */
    public ViewList getChildren()  { return _children; }

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
    public View[] getChildrenArray()  { return _children.getAll(); }

    /**
     * Adds the given child to the end of this view's children list.
     */
    protected void addChild(View aChild)
    {
        addChild(aChild, getChildCount());
    }

    /**
     * Adds the given child to this view's children list at the given index.
     */
    protected void addChild(View aChild, int anIndex)
    {
        // If child already has parent, remove from parent
        ParentView parentView = aChild.getParent();
        if (parentView == this) {
            System.err.println("ParentView.addChild: Trying to re-add child to parent"); return; }
        if (parentView != null)
            parentView.removeChild(aChild);

        // Add child to children list and set child's parent to this view
        aChild.setParent(this);

        // Add child to Children list
        _children.addView(aChild, anIndex);
        relayout();
        relayoutParent();
        repaint();
        setNeedsLayoutDeep(true);

        // If this view has child prop listeners, add to this child as well
        if (_childPCL != null) {
            aChild.addPropChangeListener(_childPCL);
            aChild.addDeepChangeListener(_childDCL);
        }

        // Fire property change
        firePropChange(Children_Prop, null, aChild, anIndex);
    }

    /**
     * Remove's the child at the given index from this view's children list.
     */
    protected View removeChild(int anIndex)
    {
        // Remove child from children list and clear parent
        View child = _children.removeView(anIndex);
        child.setParent(null);

        // If this view has child prop listeners, clear from child
        if (_childPCL != null) {
            child.removePropChangeListener(_childPCL);
            child.removeDeepChangeListener(_childDCL);
        }

        // Register for layout
        relayout();
        relayoutParent();
        repaint();

        // Fire property change and return
        firePropChange(Children_Prop, child, null, anIndex);
        return child;
    }

    /**
     * Removes the given child from this view's children list.
     */
    protected int removeChild(View aChild)
    {
        int index = indexOfChild(aChild);
        if (index >= 0)
            removeChild(index);
        return index;
    }

    /**
     * Removes all children from this view (in reverse order).
     */
    protected void removeChildren()
    {
        for (int i = getChildCount() - 1; i >= 0; i--)
            removeChild(i);
    }

    /**
     * Sets children to given list.
     */
    protected void setChildren(View ... theChildren)
    {
        removeChildren();
        for (View c : theChildren)
            addChild(c);
    }

    /**
     * Returns the children in paint order.
     */
    protected View[] getChildrenInPaintOrder()  { return getChildrenArray(); }

    /**
     * Returns the child with given name.
     */
    public View getChildForName(String aName)
    {
        // Iterate over children
        for (View child : getChildren()) {

            // If child has name, return
            if (aName.equals(child.getName()))
                return child;

            // If child is ParentView, recurse
            if (child instanceof ParentView) {
                if (child.getOwner() == getOwner() || child.getOwner() == null) {
                    ParentView parent = (ParentView) child;
                    View childForName = parent.getChildForName(aName);
                    if (childForName != null)
                        return childForName;
                }
            }
        }

        // Return not found
        return null;
    }

    /**
     * Returns the index of the given child in this view's children list.
     */
    public int indexOfChild(View aChild)
    {
        return _children.indexOf(aChild);
    }

    /**
     * Returns the last child of this view.
     */
    public View getLastChild()  { return _children.getLast(); }

    /**
     * Returns the child for given component class.
     */
    public <E extends View> E getChildForClass(Class<E> aClass)
    {
        for (View child : getChildren())
            if (aClass.isInstance(child))
                return (E) child;
        return null;
    }

    /**
     * Returns the children for given component class.
     */
    public <E extends View> List<E> getChildrenForClass(Class<E> aClass)
    {
        ViewList children = getChildren();
        return ListUtils.filterByClass(children, aClass);
    }

    /**
     * Returns the child at given point.
     */
    public View getChildAtXY(double aX, double aY)  { return _children.getViewAtXY(aX, aY); }

    /**
     * Returns the number of managed children.
     */
    public int getChildCountManaged()  { return getChildrenManaged().length; }

    /**
     * Returns the managed children.
     */
    public View[] getChildrenManaged()  { return _children.getManaged(); }

    /**
     * Returns the next focus View after given view (null to return first).
     */
    protected View getFocusNext(View aChild)
    {
        int ind = aChild != null ? indexOfChild(aChild) : -1;
        for (int i = ind + 1, iMax = getChildCount(); i < iMax; i++) {

            View child = getChild(i);
            if (child.isFocusable())
                return child;

            ParentView par = child instanceof ParentView? (ParentView) child : null;
            if (par == null)
                continue;

            View focusNext = par.getFocusNext(null);
            if (focusNext != null)
                return focusNext;
        }

        return getFocusNext();
    }

    /**
     * Returns the next focus View after given view (null to return last).
     */
    protected View getFocusPrev(View aChild)
    {
        int ind = aChild != null ? indexOfChild(aChild) : getChildCount();
        for (int i = ind - 1; i >= 0; i--) {

            View child = getChild(i);
            if (child.isFocusable())
                return child;

            ParentView par = child instanceof ParentView ? (ParentView) child : null;
            if (par == null)
                continue;

            View focusPrev = par.getFocusPrev(null);
            if (focusPrev != null)
                return focusPrev;
        }

        return getFocusPrev();
    }

    /**
     * Override to propagate to children.
     */
    public void setFont(Font aFont)
    {
        // Do normal version
        if (Objects.equals(aFont, _font)) return;
        super.setFont(aFont);

        // Notify children that inherit font
        for (View child : getChildren())
            if (!child.isFontSet())
                child.parentFontChanged();
    }

    /**
     * Override to forward to children that inherit font.
     */
    @Override
    protected void parentFontChanged()
    {
        // Do normal version
        super.parentFontChanged();

        // Notify children that inherit font
        for (View child : getChildren())
            if (!child.isFontSet())
                child.parentFontChanged();
    }

    /**
     * Override to send to children.
     */
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == _showing) return;
        super.setShowing(aValue);

        // Send to children
        for (View child : getChildren())
            child.setShowing(aValue && child.isVisible());
    }

    /**
     * Override to send to children.
     */
    public void setOwner(ViewOwner anOwner)
    {
        // Do normal version
        if (getOwner() != null) return;
        super.setOwner(anOwner);

        // Send to children
        setOwnerChildren(anOwner);
    }

    /**
     * Forwards setOwner() call to children.
     */
    protected void setOwnerChildren(ViewOwner anOwner)
    {
        for (View child : getChildren())
            child.setOwner(anOwner);
    }

    /**
     * Override to forward to children.
     */
    public boolean intersectsShape(Shape aShape)
    {
        // Do normal version (just return if miss or this ParentView has border/fill)
        boolean hit = super.intersectsShape(aShape);
        if (!hit || getBorder() != null || getFill() != null)
            return hit;

        // If any child is hit, return true
        View hview = getChildren().getViewIntersectingShape(aShape, null, null);
        if (hview != null)
            return true;
        return false;
    }

    /**
     * Override to add call to paintChildren.
     */
    protected void paintAll(Painter aPntr)
    {
        super.paintAll(aPntr);
        _needsRepaintDeep = false;
    }

    /**
     * Paint children.
     */
    @Override
    protected void paintChildren(Painter aPntr)
    {
        // Get painter clip
        Shape pntrClip = aPntr.getClip();

        // Get children in paint order
        View[] children = getChildrenInPaintOrder();

        // Iterate over children and paint any that intersect clip
        for (View child : children) {

            // If child not visible or paintable, just skip
            if (!child.isVisible() || !child.isPaintable())
                continue;

            // If child not hit by clip, skip
            Rect clipBnds = child.parentToLocal(pntrClip).getBounds();
            Rect childBnds = child.getBoundsLocal();
            if (!clipBnds.intersectsRectAndNotEmpty(childBnds))
                continue;

            // Paint child
            aPntr.save();
            aPntr.transform(child.getLocalToParent());
            child.paintAll(aPntr);
            aPntr.restore();
        }
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
        if (_needsRepaintDeep) return;
        _needsRepaintDeep = true;
        ParentView par = getParent();
        if (par != null)
            par.setNeedsRepaintDeep(true);
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
        if (_needsLayout || _inLayout) return;
        firePropChange(NeedsLayout_Prop, _needsLayout, _needsLayout = true);
        ParentView par = getParent();
        if (par != null)
            par.setNeedsLayoutDeep(true);
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
        if (_needsLayoutDeep) return;
        _needsLayoutDeep = true;
        if (_inLayoutDeep) return;
        ParentView par = getParent();
        if (par != null)
            par.setNeedsLayoutDeep(true);
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
        if (_inLayout) return;
        _inLayout = true;
        if (getWidth() > 0 && getHeight() > 0) {
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
        if (getChildrenManaged().length == getChildCount()) return;

        // Get view bounds
        double viewW = getWidth();
        double viewH = getHeight();

        // Layout floating (unmanaged + leaning) children
        for (View child : getChildren()) {

            if (child.isManaged()) continue;

            // Get child info
            Insets childMargin = child.getMargin();
            double childX = child.getX();
            double childY = child.getY();
            Size prefSize = child.getBestSize();
            double childW = prefSize.width;
            double childH = prefSize.height;

            // Handle grow width
            if (child.isGrowWidth()) {
                childX = childMargin.left;
                childW = viewW - childMargin.getWidth();
            }

            // Handle LeanX: Set X for lean, width, margin.
            else if (child.getLeanX() != null)
                childX = childMargin.left + (viewW - childMargin.getWidth() - childW) * child.getLeanX().doubleValue();

            // Handle grow height
            if (child.isGrowHeight()) {
                childY = childMargin.top;
                childH = viewH - childMargin.getHeight();
            }

            // Handle LeanY: Set Y for lean, height, margin.
            if (child.getLeanY() != null)
                childY = childMargin.top + (viewH - childMargin.getHeight() - childH) * child.getLeanY().doubleValue();

            // Set bounds
            child.setBounds(childX, childY, childW, childH);
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
        if (_needsLayout)
            layout();

        // Do layout deep (several times, if necessary)
        for (int i = 0; _needsLayoutDeep; i++) {
            _needsLayoutDeep = false;
            layoutDeepImpl();
            if (i == 5) {
                System.err.println("ParentView.layoutDeep: Too many calls to relayout inside layout");
                break;
            }
        }

        // Clear flags
        _needsLayout = _needsLayoutDeep = _inLayoutDeep = false;
    }

    /**
     * Lays out children deep.
     */
    protected void layoutDeepImpl()
    {
        for (View child : getChildren())
            if (child instanceof ParentView) {
                ParentView par = (ParentView)child;
                if (par._needsLayout || par._needsLayoutDeep)
                    par.layoutDeep();
            }
    }

    /**
     * Returns a ViewProxy for this View. Some classes already have this, but I'm hoping to do something really clever
     * with this one day, like maybe caching PrefSize info.
     */
    protected ViewProxy<?> getViewProxy()
    {
        return new ViewProxy<>(this);
    }

    /**
     * Override to handle ParentView changes.
     */
    @Override
    public void processPropChange(PropChange aPC, Object oldVal, Object newVal)
    {
        String propName = aPC.getPropName();
        if (propName == Children_Prop) {
            int index = aPC.getIndex();
            if (newVal != null)
                addChild((View) newVal, index);
            else removeChild(index);
        }

        // Do normal version
        else super.processPropChange(aPC, oldVal, newVal);
    }

    /**
     * Override to add this view as change listener to children on first call.
     */
    @Override
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        // Do normal version
        super.addDeepChangeListener(aDCL);

        // If child listeners not yet set, create/add for children
        if (_childPCL == null) {
            _childPCL = pc -> childDidPropChange(pc);
            _childDCL = (lsnr,pc) -> childDidDeepChange(lsnr,pc);
            for (View child : getChildren()) {
                child.addPropChangeListener(_childPCL);
                child.addDeepChangeListener(_childDCL);
            }
        }
    }

    /**
     * Override to remove this view as change listener to children when not needed.
     */
    @Override
    public void removeDeepChangeListener(DeepChangeListener aDCL)
    {
        // Do normal version
        super.removeDeepChangeListener(aDCL);

        // If no more deep listeners, remove
        if (!_pcs.hasDeepListener() && _childPCL != null) {
            for (View child : getChildren()) {
                child.removePropChangeListener(_childPCL);
                child.removeDeepChangeListener(_childDCL);
            }
            _childPCL = null; _childDCL = null;
        }
    }

    /**
     * Property change listener implementation to forward changes on to deep listeners.
     */
    protected void childDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Deep property change listener implementation to forward to this View's deep listeners.
     */
    protected void childDidDeepChange(Object aLsnr, PropChange aPC)
    {
        _pcs.fireDeepChange(aLsnr, aPC);
    }

    /**
     * Called when ViewTheme changes.
     */
    @Override
    protected void themeChanged(ViewTheme oldTheme, ViewTheme newTheme)
    {
        super.themeChanged(oldTheme, newTheme);
        for (View child : getChildren())
            child.themeChanged(oldTheme, newTheme);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Children, NeedsLayout
        if (this instanceof ViewHost)
            aPropSet.addPropNamed(Children_Prop, View[].class, EMPTY_OBJECT);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Children
            case Children_Prop: return ((ViewHost) this).getGuests();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Children
            case Children_Prop: ((ViewHost) this).setGuests((View[]) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
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
     * XML Archival of basic view.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        return super.toXML(anArchiver);
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        if (this instanceof ViewHost)
            ViewHost.toXMLGuests((ViewHost) this, anArchiver, anElement);
    }

    /**
     * Override to break fromXML into fromXMLView and fromXMLChildren.
     */
    public View fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        fromXMLView(anArchiver, anElement);
        fromXMLChildren(anArchiver, anElement);
        return this;
    }

    /**
     * XML unarchival of basic view.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver,anElement);
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        if (this instanceof ViewHost)
            ViewHost.fromXMLGuests((ViewHost) this, anArchiver, anElement);
    }
}