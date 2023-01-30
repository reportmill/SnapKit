/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
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

    // The TabBar
    private TabBar  _tabBar;

    // The view to hold content
    private BoxView  _contentBox;

    // A child view holding Tab.Content views that aren't currently showing, so getView(name) works
    private RowView  _hiddenKids;

    // Constants for properties
    public static final String TabSide_Prop = "TabSide";
    public static final String Classic_Prop = "Classic";

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

        // Create and configure content cradle
        _contentBox = new BoxView(null, true, true);
        _contentBox.setFill(ViewUtils.getBackFill());
        _contentBox.setBorder(Color.LIGHTGRAY, 1);
        _contentBox.setGrowHeight(true);

        // Create/configure HiddenKids
        _hiddenKids = new RowView();
        _hiddenKids.setManaged(false);
        _hiddenKids.setVisible(false);

        // Add shelf and content cradle, enable action event
        setChildren(_tabBar, _contentBox, _hiddenKids);

        // Test
        setTabSide(Side.BOTTOM);
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

        // Add child at right index
        removeChild(_tabBar);
        if (aSide == Side.BOTTOM)
            addChild(_tabBar, 1);
        else addChild(_tabBar, 0);

        // Set, fire prop change
        firePropChange(TabSide_Prop, _tabSide, _tabSide = aSide);

        // If side not top, turn off classic
        if (aSide != Side.TOP)
            setClassic(false);
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
     * Returns the number of tabs in pane.
     */
    public int getTabCount()  { return _tabBar.getTabCount(); }

    /**
     * Returns the tab at given index.
     */
    public Tab getTab(int anIndex)  { return _tabBar.getTab(anIndex); }

    /**
     * Adds the given tab shape to tabbed pane shape.
     */
    public void addTab(String aTitle, View aView)  { _tabBar.addTab(aTitle, aView); }

    /**
     * Adds the given tab shape to tabbed pane shape.
     */
    public void addTab(String aTitle, View aView, int anIndex)  { _tabBar.addTab(aTitle, aView, anIndex); }

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
     * Returns tab content at index.
     */
    public String getTabTitle(int anIndex)  { return _tabBar.getTabTitle(anIndex); }

    /**
     * Returns tab content at index.
     */
    public View getTabContent(int anIndex)  { return getTab(anIndex).getContent(); }

    /**
     * Sets tab content at index.
     */
    public void setTabContent(View aView, int anIndex)
    {
        Tab tab = getTab(anIndex);
        if (tab.getContent() != null)
            _hiddenKids.removeChild(tab.getContent());
        if (aView != null)
            _hiddenKids.addChild(aView,0);

        // Set Tab.Content
        tab.setContent(aView);

        // If selected tab, reset content
        if (anIndex == getSelIndex())
            setContent(tab.getContent());
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
        View old = getContent();
        _contentBox.setContent(aView);

        // If old is a tab content, add back to hidden kids
        boolean isKid = _tabBar.getTabs().stream().anyMatch(tab -> tab.getContent() == old);
        if (old != null && isKid)
            _hiddenKids.addChild(old,0);

        relayout();
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return ColView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return ColView.getPrefHeight(this, aW);
    }

    /**
     * Override to layout children with ColView layout.
     */
    protected void layoutImpl()
    {
        ColView.layout(this, true);
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
            aPntr.fillRect(rectX, _contentBox.getY(),tabButton.getWidth() - 2,1);
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

        // Handle Tabs
        else if (propName == TabBar.Tabs_Prop) {
            if (aPC.getNewValue() instanceof Tab) {
                Tab newTab = (Tab) aPC.getNewValue();
                if (newTab.getContent() != null)
                    _hiddenKids.addChild(newTab.getContent(),0);
            }
            if (aPC.getOldValue() instanceof Tab) {
                Tab oldTab = (Tab) aPC.getOldValue();
                if (oldTab.getContent() != null)
                    _hiddenKids.removeChild(oldTab.getContent());
            }
        }
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
    protected void setOwnerChildren(ViewOwner anOwner)  { }

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
            View child = getTabContent(i);
            String title = getTabTitle(i);
            XMLElement childXML = anArchiver.toXML(child, this);
            childXML.add("title", title);
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