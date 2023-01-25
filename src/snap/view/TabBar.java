/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Paint;
import snap.util.Selectable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages and displays a list of tabs.
 */
public class TabBar extends ParentView implements Selectable<Tab> {

    // The tabs
    private List<Tab>  _tabs = new ArrayList<>();

    // The tab min size
    private double  _tabMinWidth;

    // The selected tab index
    private int  _selIndex = -1;

    // The tab shelf
    private RowView  _shelf;

    // A shared listener for tab button action
    private EventListener  _buttonActionLsnr = e -> shelfButtonPressed(e);

    // Constants for properties
    public static final String Tabs_Prop = "Tabs";

    // The default back fill
    private static Paint BACK_FILL = ViewUtils.getBackFill();
    private static Color c1 = new Color("#d6d6d6");
    private static Color c2 = new Color("#dddddd");
    private static GradientPaint.Stop[] SHELF_FILL_STOPS = GradientPaint.getStops(0, c1,.2, c2,1,c2);
    private static Paint SHELF_FILL = new GradientPaint(.5,0,.5,1, SHELF_FILL_STOPS);

    /**
     * Constructor.
     */
    public TabBar()
    {
        super();
        setActionable(true);

        // Create and configure shelf
        _shelf = new TabRowView();
        _shelf.setSpacing(1);
        _shelf.setPadding(5,5,0,5);
        _shelf.setFill(SHELF_FILL);
        addChild(_shelf);
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
        _shelf.addChild(tabButton, anIndex);

        // If first tab, select 0
        int selIndex = getSelIndex();
        if (anIndex <= selIndex || selIndex < 0) {
            setSelIndex(-1);
            setSelIndex(selIndex + 1);
        }
    }

    /**
     * Removes the tab at given index.
     */
    public void removeTab(int anIndex)
    {
        // Remove Tab button
        ToggleButton btabButtonn = getTabButton(anIndex);
        _shelf.removeChild(btabButtonn);

        // Remove Tab
        _tabs.remove(anIndex);

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
        if (oldButton != null) {
            oldButton.setSelected(false);
            oldButton.setButtonFill(null);
        }

        ToggleButton newButton = getTabButton(anIndex);
        if (newButton != null) {
            newButton.setSelected(true);
            newButton.setButtonFill(BACK_FILL);
        }

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
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _shelf, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _shelf, aW);
    }

    /**
     * Override to layout children with BoxView layout.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _shelf, true, true);
    }

    /**
     * Called when shelf button pressed.
     */
    protected void shelfButtonPressed(ViewEvent anEvent)
    {
        // Get index for button
        View button = anEvent.getView();
        int index = -1;
        for (int i = 0; i < _shelf.getChildCount(); i++)
            if (_shelf.getChild(i) == button)
                index = i;

        // Set selected index
        setSelIndex(index);

        // Fire action event
        fireActionEvent(anEvent);
    }

    /**
     * Called when Theme changes.
     */
    @Override
    protected void themeChanged()
    {
        super.themeChanged();
        Paint shelfFill = ViewTheme.get().getClass().getSimpleName().equals("ViewTheme") ? SHELF_FILL : ViewUtils.getBackDarkFill();
        _shelf.setFill(shelfFill);
    }

    /**
     * A RowView to stretch buttons if configured like actual tabs.
     */
    private class TabRowView extends RowView {
        protected void layoutImpl()
        {
            super.layoutImpl();
            if (getTabCount() > 0 && getTabButton(0).getPosition() == Pos.TOP_CENTER)
                getTabs().stream().forEach(tab -> tab.getButton().setHeight(tab.getButton().getHeight() + 15));
        }
    }
}
