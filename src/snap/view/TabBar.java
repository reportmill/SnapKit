/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.Selectable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages and displays a list of tabs.
 */
public class TabBar extends ParentView implements Selectable<Tab> {

    // The tabs
    private List<Tab>  _tabs = new ArrayList<>();

    // Whether it is acceptable for tool bar to have no button selected
    private boolean  _allowEmptySelection;

    // The tab min size
    private double  _tabMinWidth;

    // The selected tab index
    private int  _selIndex = -1;

    // The inner view that actually holds tab buttons
    private RowView  _tabsBox;

    // A shared listener for tab button action
    private EventListener  _buttonActionLsnr = e -> tabButtonDidFireAction(e);

    // Constants for properties
    public static final String Tabs_Prop = "Tabs";
    public static final String AllowEmptySelection_Prop = "AllowEmptySelection";

    // Constants
    public static final Insets DEFAULT_PADDING = new Insets(3, 3, 3, 5);
    public static final double DEFAULT_SPACING = 3;

    /**
     * Constructor.
     */
    public TabBar()
    {
        super();
        setActionable(true);
        _padding = DEFAULT_PADDING;

        // Create and configure TabsBox
        _tabsBox = new TabRowView();
        _tabsBox.setSpacing(DEFAULT_SPACING);
        addChild(_tabsBox);
    }

    /**
     * Returns the tabs.
     */
    public List<Tab> getTabs()  { return _tabs; }

    /**
     * Returns the number of tabs in pane.
     */
    public int getTabCount()  { return _tabs.size(); }

    /**
     * Returns the tab at given index.
     */
    public Tab getTab(int anIndex)  { return _tabs.get(anIndex); }

    /**
     * Adds the given tab shape to tabbed pane shape.
     */
    public void addTab(String aTitle, View aView)  { addTab(aTitle, aView, getTabCount()); }

    /**
     * Adds the given tab shape to tabbed pane shape.
     */
    public void addTab(String aTitle, View aView, int anIndex)
    {
        Tab tab = new Tab();
        tab.setTitle(aTitle);
        tab.setContent(aView);
        addTab(tab, anIndex);
    }

    /**
     * Adds a tab.
     */
    public void addTab(Tab aTab)  { addTab(aTab, getTabCount()); }

    /**
     * Adds a tab at given index.
     */
    public void addTab(Tab aTab, int anIndex)
    {
        // Add Tab to Tabs list
        _tabs.add(anIndex, aTab);
        aTab._tabBar = this;

        // Create/configure tab button
        ToggleButton tabButton = aTab.getButton();
        tabButton.setMinWidth(getTabMinWidth());
        tabButton.addEventHandler(_buttonActionLsnr, Action);

        // Add button to shelf
        _tabsBox.addChild(tabButton, anIndex);

        // Fire prop change
        firePropChange(Tabs_Prop, null, aTab, anIndex);

        // If first tab, select 0
        int selIndex = getSelIndex();
        if (selIndex < 0 && !isAllowEmptySelection())
            setSelIndex(0);
    }

    /**
     * Removes the tab at given index.
     */
    public void removeTab(int anIndex)
    {
        // Remove Tab button
        ToggleButton tabButton = getTabButton(anIndex);
        _tabsBox.removeChild(tabButton);

        // Remove Tab
        Tab tab = _tabs.remove(anIndex);

        // Fire prop change
        firePropChange(Tabs_Prop, tab, null, anIndex);

        // Reset Selection
        if (anIndex == getSelIndex()) {
            setSelIndex(-1);
            if (anIndex < getTabCount())
                setSelIndex(anIndex);
        }
    }

    /**
     * Removes the given tab.
     */
    public void removeTab(Tab aTab)
    {
        int index = _tabs.indexOf(aTab);
        if (index >= 0)
            removeTab(index);
    }

    /**
     * Removes all tabs.
     */
    public void removeTabs()
    {
        while (getTabCount() > 0)
            removeTab(0);
    }

    /**
     * Returns whether it is acceptable for tool bar to have no button selected.
     *
     * If true, clicking the selected button results in it being deselected.
     */
    public boolean isAllowEmptySelection()  { return _allowEmptySelection; }

    /**
     * Sets whether it is acceptable for tool bar to have no button selected.
     */
    public void setAllowEmptySelection(boolean aValue)
    {
        if (aValue == _allowEmptySelection) return;
        firePropChange(AllowEmptySelection_Prop, _allowEmptySelection, _allowEmptySelection = aValue);
    }

    /**
     * Returns tab content at index.
     */
    public String getTabTitle(int anIndex)  { return getTab(anIndex).getTitle(); }

    /**
     * Returns the tab min width.
     */
    public double getTabMinWidth()  { return _tabMinWidth; }

    /**
     * Sets the tab min width.
     */
    public void setTabMinWidth(double aValue)
    {
        _tabMinWidth = aValue;
        for (Tab tab : _tabs)
            tab.getButton().setMinWidth(aValue);
    }

    /**
     * Returns the tap pane's selected index.
     */
    public int getSelIndex()  { return _selIndex; }

    /**
     * Sets the TabView's selected index.
     */
    public void setSelIndex(int anIndex)
    {
        // If already set, just return
        if (anIndex == _selIndex) return;

        // Update old/new ToggleButtons
        ToggleButton oldButton = getTabButton(_selIndex);
        if (oldButton != null)
            oldButton.setSelected(false);

        ToggleButton newButton = getTabButton(anIndex);
        if (newButton != null)
            newButton.setSelected(true);

        // FirePropChange and fireActionEvent
        firePropChange(SelIndex_Prop, _selIndex, _selIndex = anIndex);
    }

    /**
     * Returns the selected item (tab).
     */
    public Tab getSelItem()
    {
        return _selIndex >= 0 ? getTab(_selIndex) : null;
    }

    /**
     * Sets the selected item (tab).
     */
    public void setSelItem(Tab aTab)
    {
        int index = _tabs.indexOf(aTab);
        setSelIndex(index);
    }

    /**
     * Returns a tab button.
     */
    public ToggleButton getTabButton(int anIndex)
    {
        if (anIndex < 0 || anIndex >= getTabCount())
            return null;
        return getTab(anIndex).getButton();
    }

    /**
     * Returns the actual box that holds the tabs.
     */
    public ParentView getTabsBox()  { return _tabsBox; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _tabsBox, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _tabsBox, aW);
    }

    /**
     * Override to layout children with BoxView layout.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _tabsBox, true, true);
    }

    /**
     * Called when tab button fires action.
     */
    protected void tabButtonDidFireAction(ViewEvent anEvent)
    {
        // Get index for button
        View button = anEvent.getView();
        int index = button.indexInParent();

        // If selected button was pressed, either reset index or return
        if (index == getSelIndex()) {
            if (isAllowEmptySelection())
                index = -1;
            else return;
        }

        // Set selected index
        setSelIndex(index);

        // Fire action event
        fireActionEvent(anEvent);
    }

    /**
     * Override to suppress so tab buttons don't fireAction to Owner.
     */
    @Override
    protected void setOwnerChildren(ViewOwner anOwner)  { }

    /**
     * A RowView to stretch buttons if configured like actual tabs.
     */
    private class TabRowView extends RowView {
        protected void layoutImpl()
        {
            super.layoutImpl();
            if (getTabCount() > 0 && getTabButton(0).getPosition() == Pos.TOP_CENTER)
                getTabs().forEach(tab -> tab.getButton().setHeight(tab.getButton().getHeight() + 15));
        }
    }
}
