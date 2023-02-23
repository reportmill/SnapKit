/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Side;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;

/**
 * A View subclass to show multiple children under user selectable tabs.
 */
public class TabView extends ParentView implements Selectable<Tab> {

    // The side that tabs should be on
    private Side  _tabSide;

    // Whether tabView should render in classic style
    private boolean  _classic;

    // Whether tab changes should animate
    private boolean  _animateTabChange;

    // The TabBar
    private TabBar  _tabBar;

    // The view to hold content
    private BoxView  _contentBox;

    // The separator between toolbar and content
    private RectView  _separator;

    // Constants for properties
    public static final String TabSide_Prop = "TabSide";
    public static final String Classic_Prop = "Classic";
    public static final String AnimateTabChange_Prop = "AnimateTabChange";

    // The default TabBar fill
    private static Color c1 = new Color("#d6d6d6");
    private static Color c2 = new Color("#dddddd");
    private static GradientPaint.Stop[] SHELF_FILL_STOPS = GradientPaint.getStops(0, c1,.2, c2,1,c2);
    private static Paint CLASSIC_TAB_BAR_FILL = new GradientPaint(.5,0,.5,1, SHELF_FILL_STOPS);

    // Constants
    private static final Insets CLASSIC_TAB_BAR_INSETS = new Insets(5, 5, 0, 5);
    private static final double CLASSIC_TAB_BAR_SPACING = 2;

    /**
     * Creates a new TabView.
     */
    public TabView()
    {
        setActionable(true);
        _tabSide = Side.TOP;
        _classic = true;

        // Create ToolBar
        _tabBar = new TabBar();
        _tabBar.setFill(CLASSIC_TAB_BAR_FILL);
        _tabBar.setPadding(CLASSIC_TAB_BAR_INSETS);
        _tabBar.getTabsBox().setSpacing(CLASSIC_TAB_BAR_SPACING);
        _tabBar.addPropChangeListener(pc -> tabBarDidPropChange(pc));
        _tabBar.addEventHandler(e -> tabBarDidFireAction(e), Action);

        // Create separator
        _separator = new RectView();
        _separator.setFill(Color.GRAY8);
        _separator.setPrefSize(1, 1);

        // Create and configure content cradle
        _contentBox = new BoxView(null, true, true);
        _contentBox.setFill(ViewUtils.getBackFill());
        _contentBox.setGrowWidth(true);
        _contentBox.setGrowHeight(true);
        _contentBox.addPropChangeListener(pc -> contentBoxDidContentChange(), BoxView.Content_Prop);

        // Add shelf and content cradle, enable action event
        setChildren(_tabBar, _separator, _contentBox);
    }

    /**
     * Returns the side where tabs are shown.
     */
    public Side getTabSide()  { return _tabSide; }

    /**
     * Sets the side where tabs are shown.
     */
    public void setTabSide(Side aSide)
    {
        if (aSide == _tabSide) return;

        // Remove TabBar
        View tabBarView = _tabBar.getParent() == this ? _tabBar : _tabBar.getParent();
        removeChild(tabBarView);
        removeChild(_separator);

        // Get new view to add (need to wrap/rotate tabBar if side is left/right)
        tabBarView = _tabBar;
        _tabBar.setRotate(0);
        _tabBar.getTabsBox().setAlignX(aSide == Side.LEFT ? HPos.RIGHT : HPos.LEFT);
        if (aSide.isLeftOrRight()) {
            tabBarView = new WrapView(_tabBar, true, true);
            _tabBar.setRotate(aSide == Side.LEFT ? -90 : 90);
        }

        // Add child at right index
        if (aSide == Side.BOTTOM || aSide == Side.RIGHT) {
            addChild(_separator);
            addChild(tabBarView);
        }
        else {
            addChild(tabBarView, 0);
            addChild(_separator, 1);
        }

        // Set, fire prop change
        firePropChange(TabSide_Prop, _tabSide, _tabSide = aSide);

        // If side not top, turn off classic
        if (aSide != Side.TOP)
            setClassic(false);

        // Update Vertical
        setVertical(aSide.isTopOrBottom());
    }

    /**
     * Returns whether tab view should render tabs in classic style.
     */
    public boolean isClassic()  { return _classic; }

    /**
     * Sets whether tab view should render tabs in classic style.
     */
    public void setClassic(boolean aValue)
    {
        // If already set, just return
        if (aValue == _classic) return;

        // Set classic
        _classic = aValue;

        // Handle configure classic
        if (aValue) {
            _tabBar.setFill(CLASSIC_TAB_BAR_FILL);
            _tabBar.setPadding(CLASSIC_TAB_BAR_INSETS);
            _tabBar.getTabsBox().setSpacing(CLASSIC_TAB_BAR_SPACING);
        }

        // Handle configure non-classic
        else {
            _tabBar.setFill(null);
            _tabBar.setPadding(TabBar.DEFAULT_PADDING);
            _tabBar.getTabsBox().setSpacing(TabBar.DEFAULT_SPACING);
        }

        // Fire prop change
        firePropChange(Classic_Prop, !_classic, _classic);
    }

    /**
     * Returns the TabBar.
     */
    public TabBar getTabBar()  { return _tabBar; }

    /**
     * Returns the ContentBox.
     */
    public BoxView getContentBox()  { return _contentBox; }

    /**
     * Returns the number of tabs in pane.
     */
    public int getTabCount()  { return _tabBar.getTabCount(); }

    /**
     * Returns the tab at given index.
     */
    public Tab getTab(int anIndex)  { return _tabBar.getTab(anIndex); }

    /**
     * Adds a tab.
     */
    public void addTab(Tab aTab)  { _tabBar.addTab(aTab, getTabCount()); }

    /**
     * Adds a tab at given index.
     */
    public void addTab(Tab aTab, int anIndex)  { _tabBar.addTab(aTab, anIndex); }

    /**
     * Removes the tab at given index.
     */
    public void removeTab(int anIndex)  { _tabBar.removeTab(anIndex); }

    /**
     * Creates and adds a tab for given title and content view.
     */
    public void addTab(String aTitle, View aView)  { _tabBar.addTab(aTitle, aView); }

    /**
     * Returns whether to animate tab changes.
     */
    public boolean isAnimateTabChange()  { return _animateTabChange; }

    /**
     * Sets whether to animate tab changes.
     */
    public void setAnimateTabChange(boolean aValue)
    {
        if (aValue == _animateTabChange) return;
        firePropChange(AnimateTabChange_Prop, _animateTabChange, _animateTabChange = aValue);
    }

    /**
     * Returns the tap pane's selected index.
     */
    public int getSelIndex()  { return _tabBar.getSelIndex(); }

    /**
     * Sets the TabView's selected index.
     */
    public void setSelIndex(int anIndex)  { _tabBar.setSelIndex(anIndex); }

    /**
     * Returns the selected item (tab).
     */
    public Tab getSelItem()  { return  _tabBar.getSelItem(); }

    /**
     * Sets the selected item (tab).
     */
    public void setSelItem(Tab aTab)  { _tabBar.setSelItem(aTab); }

    /**
     * Returns the selected child.
     */
    public View getContent()  { return _contentBox.getContent(); }

    /**
     * Sets the current tab content.
     */
    protected void setContent(View aView)
    {
        // If already set, just return
        if (aView == getContent()) return;

        // If animating (and showing), setContentWithAnim
        if (isAnimateTabChange() && isShowing())
            _contentBox.setContentWithAnim(aView);

        // Otherwise, do normal version
        else _contentBox.setContent(aView);

        // Update Separator
        _separator.setVisible(aView != null);
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        if (_tabSide.isLeftOrRight())
            return RowView.getPrefWidth(this, aH);
        return ColView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        if (_tabSide.isLeftOrRight())
            return RowView.getPrefHeight(this, aW);
        return ColView.getPrefHeight(this, aW);
    }

    /**
     * Override to layout children with ColView layout.
     */
    protected void layoutImpl()
    {
        if (_tabSide.isLeftOrRight())
            RowView.layout(this, true);
        else ColView.layout(this, true);
    }

    /**
     * Override to clear border under selected tab if tabs are configured like actual tabs.
     */
    @Override
    protected void paintAbove(Painter aPntr)
    {
        int selIndex = getSelIndex();
        ToggleButton tabButton = _tabBar.getTabButton(selIndex);
        if (tabButton != null && tabButton.getPosition() == Pos.TOP_CENTER) {
            aPntr.setPaint(_contentBox.getFill());
            double rectX = _tabBar.getInsetsAll().left + tabButton.getX() + 1;
            double rectY = _separator.getY();
            aPntr.fillRect(rectX, rectY,tabButton.getWidth() - 2,1);
        }
    }

    /**
     * Called when TabBar does prop change.
     */
    private void tabBarDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle SelIndex
        if (propName == TabBar.SelIndex_Prop) {
            Tab tab = _tabBar.getSelItem();
            View content = tab != null ? tab.getContent() : null;
            setContent(content);
            firePropChange(SelIndex_Prop, aPC.getOldValue(), aPC.getNewValue());
        }
    }

    /**
     * Called when ContentBox does content change.
     */
    private void contentBoxDidContentChange()
    {
        // Get Visible/Vertical
        boolean isVertical = getTabSide().isTopOrBottom();
        boolean isContentSet = _contentBox.getContent() != null;

        // Update TabBar Min/Max size to make TabView unsizable when there is no content for sake of SplitView
        if (isVertical) {
            double prefH = isContentSet ? -1 : getPrefHeightImpl(-1);
            setMinHeight(prefH);
            setMaxHeight(prefH);
        }
        else {
            double prefW = isContentSet ? -1 : getPrefWidthImpl(-1);
            setMinWidth(prefW);
            setMaxWidth(prefW);
        }
    }

    /**
     * Override to trigger content contentBoxDidContentChange() when first showing.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        super.setShowing(aValue);
        if (aValue)
            contentBoxDidContentChange();
    }

    /**
     * Called when Theme changes.
     */
    @Override
    protected void themeChanged()
    {
        super.themeChanged();
        Paint tabBarFill = ViewTheme.get().getClass().getSimpleName().equals("ViewTheme") ? CLASSIC_TAB_BAR_FILL : ViewUtils.getBackDarkFill();
        _tabBar.setFill(tabBarFill);
        _contentBox.setFill(ViewUtils.getBackFill());
    }

    /**
     * Called when TabBar does fireAction
     */
    protected void tabBarDidFireAction(ViewEvent anEvent)
    {
        fireActionEvent(anEvent);
    }

    /**
     * Override to suppress so TabBar doesn't fireAction to Owner.
     */
    @Override
    protected void setOwnerChildren(ViewOwner anOwner)
    {
        View[] children = getChildren();
        for (View child : children) {
            if (child == _tabBar)
                _tabBar.setOwnerChildren(anOwner);
            else child.setOwner(anOwner);
        }
    }

    /**
     * Override for custom defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Vertical
        if (aPropName == Vertical_Prop)
            return true;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);
        if (getSelIndex() > 0)
            e.add(SelIndex_Prop, getSelIndex());
        return e;
    }

    /**
     * XML archival deep.
     */
    public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (int i = 0, iMax = getTabCount(); i < iMax; i++) {
            Tab tab = getTab(i);
            View tabContent = tab.getContent();
            String tabTitle = tab.getTitle();
            XMLElement childXML = anArchiver.toXML(tabContent, this);
            childXML.add("title", tabTitle);
            anElement.add(childXML);
        }
    }

    /**
     * XML unarchival for children. Only panels do anything here so far.
     */
    public void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive shapes
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {

            XMLElement cxml = anElement.get(i);
            Class<?> childClass = anArchiver.getClass(cxml.getName());

            // If child class is View, unarchive and add
            if (childClass != null && View.class.isAssignableFrom(childClass)) {
                View shape = (View) anArchiver.fromXML(cxml, this);
                String title = cxml.getAttributeValue("title"); if (title == null) title = "";
                addTab(title, shape);
            }
        }

        // Unarchive SelIndex (after children unarchival - otherwise it may be out of bounds)
        if (anElement.hasAttribute(SelIndex_Prop))
            setSelIndex(anElement.getAttributeIntValue(SelIndex_Prop));
    }
}