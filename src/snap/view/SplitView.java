/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.*;

/**
 * A View subclass to show children with user adjustable divider.
 */
public class SplitView extends ParentView implements ViewHost {

    // The list of items
    private List <View>  _items = new ArrayList<>();
    
    // The list of dividers
    private List <Divider>  _divs = new ArrayList<>();
    
    // The spacing between items (really the default span of the dividers)
    private double  _divSpan;
    
    // The default divider
    private Divider  _divider;
    
    // The divider currently being dragged (and the offset from center of drag start)
    private Divider  _dragDiv;
    private double  _dragOff;

    // A listener to watch for when item Visible or Min/MaxSize changes
    private PropChangeListener  _itemVisibleOrMinMaxSizeChangedLsnr = pc -> itemVisibleOrMinMaxSizeChanged(pc);

    // Constants for properties
    public static final String DividerSpan_Prop = "DividerSpan";
    
    // Constants for internal use
    public static final Border SPLIT_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);
    public static final int DEFAULT_DIVIDER_SPAN = Divider.DEFAULT_SPAN;

    // Constant for props that all SplitView dividers share with prototype returned by getDivider()
    private static String[] SHARED_DIVIDER_PROPS = { Fill_Prop, Border_Prop, Paintable_Prop, Divider.Span_Prop, Divider.ClickSpan_Prop };


    /**
     * Creates a new SplitView.
     */
    public SplitView()
    {
        super();
        _divSpan = DEFAULT_DIVIDER_SPAN;
        setBorder(SPLIT_VIEW_BORDER);
        setClipToBounds(true);
        addEventFilter(e -> processDividerMouseEvent(e), MouseMove, MousePress, MouseDrag, MouseRelease);
    }

    /**
     * Returns the number of items.
     */
    public int getItemCount()  { return _items.size(); }

    /**
     * Returns the individual item at given index.
     */
    public View getItem(int anIndex)  { return _items.get(anIndex); }

    /**
     * Override to make sure dividers are in place.
     */
    public void addItem(View aView)  { addItem(aView, getItemCount()); }

    /**
     * Returns the SplitView items.
     */
    public List <View> getItems()  { return _items; }

    /**
     * Override to make sure dividers are in place.
     */
    public void addItem(View aView, int anIndex)
    {
        // Add View item
        _items.add(anIndex, aView);

        // If more than one item, add divider
        Divider divider = null;
        if (getItemCount() > 1) {
            divider = createDivider();
            addDivider(divider, anIndex > 0 ? (anIndex - 1) : 0);
            addChild(divider, anIndex > 0 ? (anIndex * 2 - 1) : 0);
        }

        // Add view as child
        addChild(aView, anIndex * 2);

        // Update divider Visible/Disabled for item
        if (divider != null)
            setDividerVisibleAndDisabled(divider);

        // Add listener to view to update divider Visible/Disabled when item changes Visible or Min/MaxSize
        String[] props = { Visible_Prop, MinWidth_Prop, MinHeight_Prop, MaxWidth_Prop, MaxHeight_Prop };
        aView.addPropChangeListener(_itemVisibleOrMinMaxSizeChangedLsnr, props);
    }

    /**
     * Override to remove unused dividers.
     */
    public View removeItem(int anIndex)
    {
        // Remove item and child and listener
        View view = _items.remove(anIndex);
        removeChild(view);
        view.removePropChangeListener(_itemVisibleOrMinMaxSizeChangedLsnr);

        // If at least one item left, remove extra divider
        if (getItemCount() > 0)
            removeDivider(anIndex > 0 ? (anIndex - 1) : 0);
        return view;
    }

    /**
     * Override to remove unused dividers.
     */
    public int removeItem(View aView)
    {
        int index = indexOfItem(aView);
        if (index >= 0)
            removeItem(index);
        return index;
    }

    /**
     * Sets the item at index.
     */
    public void setItem(View aView, int anIndex)
    {
        if (anIndex < getItemCount())
            removeItem(anIndex);
        addItem(aView, anIndex);
    }

    /**
     * Sets the splitview items to given views
     */
    public void setItems(View ... theViews)
    {
        removeItems();
        for (View view : theViews)
            addItem(view);
    }

    /**
     * Sets the splitview items to given views
     */
    public void removeItems()
    {
        for (View view : getItems().toArray(new View[0]))
            removeItem(view);
    }

    /**
     * Returns the index of given item.
     */
    public int indexOfItem(View anItem)  { return ListUtils.indexOfId(_items, anItem); }

    /**
     * Returns the default divider.
     */
    public Divider getDivider()
    {
        // If already set, just return
        if (_divider != null) return _divider;

        // Create/configure prototype divider
        Divider divider = new Divider();
        divider.setVertical(!isVertical());
        divider.setSpan(getDividerSpan());
        divider.addPropChangeListener(pc -> dividerPropChange(pc), SHARED_DIVIDER_PROPS);

        // Set/return
        return _divider = divider;
    }

    /**
     * Creates a new divider.
     */
    protected Divider createDivider()
    {
        // Create/config new divider from prototype
        Divider newDivider = new Divider();
        newDivider.setVertical(!isVertical());

        // Propagate shared divider props
        Divider dividerPrototype = getDivider();
        for (String sharedProp : SHARED_DIVIDER_PROPS) {
            Object sharedPropValue = dividerPrototype.getPropValue(sharedProp);
            newDivider.setPropValue(sharedProp, sharedPropValue);
        }

        // Return
        return newDivider;
    }

    /**
     * Returns the dividers.
     */
    public Divider[] getDividers()  { return _divs.toArray(new Divider[0]); }

    /**
     * Returns the number of dividers.
     */
    public int getDividerCount()  { return _divs.size(); }

    /**
     * Returns the individual divider at given index.
     */
    public Divider getDivider(int anIndex)  { return _divs.get(anIndex); }

    /**
     * Adds a new divider.
     */
    protected void addDivider(Divider aDiv, int anIndex)
    {
        _divs.add(anIndex, aDiv);
    }

    /**
     * Removes a divider.
     */
    protected void removeDivider(int anIndex)
    {
        Divider div = _divs.remove(anIndex);
        removeChild(div);
    }

    /**
     * Returns the default size of the dividers.
     */
    public double getDividerSpan()  { return _divSpan; }

    /**
     * Sets the default size of the dividers.
     */
    public void setDividerSpan(double aValue)
    {
        if (aValue == _divSpan) return;
        for (Divider div : _divs)
            div.setSpan(aValue);
        firePropChange(DividerSpan_Prop, _divSpan, _divSpan = aValue);
    }

    /**
     * Returns the divider at given point.
     */
    public Divider getDividerForXY(double aX, double aY)
    {
        // Handle vertical
        if (isVertical()) {
            for (Divider div : _divs) {
                if (!div.isVisible())
                    continue;
                double midY = div.getMidY();
                double halfClickSpan = Math.max(div.getSpan(), div.getClickSpan()) / 2;
                double min = midY - halfClickSpan;
                double max = midY + halfClickSpan;
                if (aY >= min && aY <= max)
                    return div;
            }
        }

        // Handle horizontal
        else {
            for (Divider div : _divs) {
                if (!div.isVisible())
                    continue;
                double midX = div.getMidX();
                double halfClickSpan = Math.max(div.getSpan(), div.getClickSpan()) / 2;
                double min = midX - halfClickSpan;
                double max = midX + halfClickSpan;
                if (aX >= min && aX <= max)
                    return div;
            }
        }

        // Return not found
        return null;
    }

    /**
     * Adds a child with animation.
     */
    public void addItemWithAnim(View aView, double aSize)
    {
        addItemWithAnim(aView, aSize, getItemCount());
    }

    /**
     * Adds a item with animation.
     */
    public void addItemWithAnim(View aView, double aSize, int anIndex)
    {
        ViewAnimUtils.addSplitViewItemWithAnim(this, aView, aSize, anIndex);
    }

    /**
     * Removes a item with animation.
     */
    public void removeItemWithAnim(View aView)
    {
        ViewAnimUtils.removeSplitViewItemWithAnim(this, aView);
    }

    /**
     * Sets a child visible with animation.
     */
    public void setItemVisibleWithAnim(View aView, boolean aValue)
    {
        ViewAnimUtils.setSplitViewItemVisibleWithAnim(this, aView, aValue);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        ParentViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        ParentViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()
    {
        ParentViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();

        // After any layout, all pref sizes should be set
        makeSurePrefSizesAreSet();
    }

    /**
     * Override to return RowViewProxy or ColViewProxy.
     */
    @Override
    protected ParentViewProxy<?> getViewProxy()
    {
        ParentViewProxy<?> viewProxy = isHorizontal() ? new RowViewProxy<>(this) :
            new ColViewProxy<>(this);
        viewProxy.setFillWidth(true);
        viewProxy.setFillHeight(true);
        return viewProxy;
    }

    /**
     * Handle MouseDrag event: Calculate and set new location.
     */
    protected void processDividerMouseEvent(ViewEvent anEvent)
    {
        // Handle MouseMove: If over divider, update cursor
        if (anEvent.isMouseMove()) {
            Divider divider = getDividerForXY(anEvent.getX(), anEvent.getY());
            if (divider != null && divider.isEnabled()) {
                WindowView win = getWindow();
                if (win != null)
                    win.setActiveCursor(divider.getCursor());
            }
        }

        // Handle MousePress: Check for divider hit
        else if (anEvent.isMousePress()) {

            // Get divider at mouse
            Divider divider = getDividerForXY(anEvent.getX(), anEvent.getY());
            if (divider == null || divider.isDisabled())
                return;

            // Set divider drag offset
            _dragDiv = divider;
            _dragOff = isVertical() ? _dragDiv.getY() - anEvent.getY() : _dragDiv.getX() - anEvent.getX();
            anEvent.consume();
        }

        // Handle MouseDrag: Calculate new location and set
        else if (anEvent.isMouseDrag()) {
            if (_dragDiv != null) {
                View peer0 = _dragDiv.getViewBefore();
                double loc = _dragDiv.isVertical() ? (anEvent.getX() - peer0.getX()) : (anEvent.getY() - peer0.getY());
                _dragDiv.setLocation(loc + _dragOff);
                anEvent.consume();
            }
        }

        // Handle MouseRelease: Clear DragDiv
        else if (anEvent.isMouseRelease()) {
            if (_dragDiv != null) {
                _dragDiv = null;
                anEvent.consume();
            }
        }
    }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return SPLIT_VIEW_BORDER; }

    /**
     * Override to forward to dividers.
     */
    public void setVertical(boolean aValue)
    {
        if (aValue == isVertical()) return;
        super.setVertical(aValue);
        if (_divider != null)
            _divider.setVertical(!aValue);
        for (Divider div : _divs)
            div.setVertical(!aValue);
    }

    /**
     * ViewHost method.
     */
    @Override
    public int getGuestCount()  { return getItemCount(); }

    /**
     * ViewHost method.
     */
    @Override
    public View getGuest(int anIndex)  { return getItem(anIndex); }

    /**
     * ViewHost method.
     */
    @Override
    public void addGuest(View aChild, int anIndex)  { addItem(aChild, anIndex); }

    /**
     * ViewHost method.
     */
    @Override
    public View removeGuest(int anIndex)  { return removeItem(anIndex); }

    /**
     * Called when prototype divider has prop change to forward to existing dividers.
     */
    private void dividerPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        Object propValue = _divider.getPropValue(propName);
        _divs.forEach(div -> div.setPropValue(propName, propValue));
    }

    /**
     * Called when an item changes the value of Visible or Min/Max size properties to update divider Visible/Disabled.
     */
    private void itemVisibleOrMinMaxSizeChanged(PropChange aPC)
    {
        // If no dividers, just return
        if (getItemCount() < 2) return;

        // Get whether divider should be visible and fix
        View view = (View) aPC.getSource();
        int viewIndex = getItems().indexOf(view);

        // Update divider visible/disabled to left of view
        if (viewIndex > 0) {
            Divider divider = getDivider(viewIndex - 1);
            setDividerVisibleAndDisabled(divider);
        }

        // Update divider visible/disabled to right of view
        if (viewIndex < getItemCount() - 1) {
            Divider divider = getDivider(viewIndex);
            setDividerVisibleAndDisabled(divider);
        }
    }

    /**
     * After any layout, all pref sizes should be set
     */
    private void makeSurePrefSizesAreSet()
    {
        List<View> items = getItems();

        // Handle Vertical: Iterate over items and make sure PrefHeight is set
        if (isVertical()) {
            for (View item : items)
                if (!item.isPrefHeightSet() && item.isVisible())
                    item.setPrefHeight(item.getHeight());
        }

        // Handle Vertical: Iterate over items and make sure PrefWidth is set
        else {
            for (View item : items)
                if (!item.isPrefWidthSet() && item.isVisible())
                    item.setPrefWidth(item.getWidth());
        }
    }

    /**
     * Sets a divider Visible and Disabled property for given views.
     */
    private void setDividerVisibleAndDisabled(Divider divider)
    {
        // Get view before/after
        View view1 = divider.getViewBefore();
        View view2 = divider.getViewAfter();

        // Set Divider.Visible
        boolean isVisible = view1.isVisible() && view2.isVisible();
        divider.setVisible(isVisible);

        // Set Divider.Disabled
        boolean isDisabled = isViewFrozen(view1) || isViewFrozen(view2);
        divider.setDisabled(isDisabled);
    }

    /**
     * Utility method to return whether SplitView item view is frozen (can't be resized) because Min/Max sizes are equal.
     */
    private boolean isViewFrozen(View aView)
    {
        if (isVertical())
            return aView.isMinHeightSet() && aView.isMaxHeightSet() && aView.getMinHeight() == aView.getMaxHeight();
        return aView.isMinWidthSet() && aView.isMaxWidthSet() && aView.getMinWidth() == aView.getMaxWidth();
    }

    /**
     * XML Archival of basic view.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive DividerSpan
        if (getDividerSpan() != DEFAULT_DIVIDER_SPAN)
            e.add(DividerSpan_Prop, getDividerSpan());

        // Return
        return e;
    }

    /**
     * XML unarchival of basic view.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive DividerSpan
        if(anElement.hasAttribute(DividerSpan_Prop))
            setDividerSpan(anElement.getAttributeFloatValue(DividerSpan_Prop));
    }

    /**
     * XML archival deep.
     */
    public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive items
        for (View item : getItems()) {
            XMLElement cxml = anArchiver.toXML(item, this);
            anElement.add(cxml);
        }
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive as child nodes
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement childXML = anElement.get(i);
            Class<?> cls = anArchiver.getClass(childXML.getName());
            if (cls != null && View.class.isAssignableFrom(cls)) {
                View view = (View) anArchiver.fromXML(childXML, this);
                addItem(view);
            }
        }
    }

    /**
     * Replaces the given view with a SplitView.
     */
    public static SplitView makeSplitView(View aView)
    {
        // Create SplitView to match given view
        SplitView splitView = new SplitView();
        splitView.setVertical(aView.isVertical());
        splitView.setLeanX(aView.getLeanX());
        splitView.setLeanY(aView.getLeanY());
        splitView.setGrowWidth(aView.isGrowWidth());
        splitView.setGrowHeight(aView.isGrowHeight());

        // Handle ViewHost
        if (aView instanceof ViewHost) {
            ViewHost host = (ViewHost) aView;
            splitView.setItems(host.getGuests());
        }

        // Replace given View with new SplitView and return SplitView
        if (aView.getParent() != null)
           ViewUtils.replaceView(aView, splitView);

        // Return
        return splitView;
    }
}