/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;

/**
 * A View subclass to show multiple children under user selectable tabs.
 */
public class TabView extends ParentView implements Selectable<Tab> {

    // The TabBar
    private TabBar  _tabBar;

    // The view to hold content
    private BoxView  _contentBox;
    
    // A child view holding Tab.Content views that aren't currently showing, so getView(name) works
    private RowView  _hiddenKids;

    /**
     * Creates a new TabView.
     */
    public TabView()
    {
        setActionable(true);

        // Create ToolBar
        _tabBar = new TabBar();
        _tabBar.addPropChangeListener(pc -> tabBarDidPropChange(pc));

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
            aPntr.fillRect(tabButton.getX() + 1, _contentBox.getY(),tabButton.getWidth() - 2,1);
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
        _contentBox.setFill(ViewUtils.getBackFill());
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