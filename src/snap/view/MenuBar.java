/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

import java.util.List;

/**
 * A View to display menus in a menu bar.
 */
public class MenuBar extends ParentView {

    // Constants for properties
    public static final String Menus_Prop = "Menus";

    /**
     * Constructor.
     */
    public MenuBar()
    {
        super();
    }

    /**
     * Returns the child menus.
     */
    public List<Menu> getMenus()
    {
        ViewList children = getChildren();
        return ListUtils.filterByClass(children, Menu.class);
    }

    /**
     * Sets the child menus.
     */
    public void setMenus(List<Menu> theMenus)
    {
        while (getChildCount() > 0)
            removeChild(0);
        theMenus.forEach(this::addChild);
    }

    /**
     * Adds a Menu.
     */
    public void addMenu(Menu aMenu)  { addChild(aMenu); }

    /**
     * Remove's the child at the given index from this node's children list.
     */
    public View removeMenu(int anIndex)  { return removeChild(anIndex); }

    /**
     * Removes the given menu from this node's children list.
     */
    public int removeMenu(View aChild)  { return removeChild(aChild); }

    /**
     * Returns the menu showing (or null).
     */
    public Menu getMenuShowing()
    {
        return ListUtils.findMatch(getMenus(), menu -> menu.isPopupShowing());
    }

    /**
     * Override to handle accelerators.
     */
    private void handleKeyPressEvent(ViewEvent anEvent)
    {
        if (!anEvent.isShortcutDown())
            return;

        for (Menu menu : getMenus()) {
            MenuItem match = getMatchingMenuItem(menu, anEvent);
            if (match != null) {
                match.fireActionEvent(anEvent);
                anEvent.consume(); return;
            }
        }
    }

    /**
     * Returns a matching menu item.
     */
    public MenuItem getMatchingMenuItem(MenuItem aMenuItem, ViewEvent anEvent)
    {
        // Handle Menu
        if (aMenuItem instanceof Menu menu) {
            for (MenuItem item : menu.getMenuItems()) {
                MenuItem match = getMatchingMenuItem(item, anEvent);
                if(match != null)
                    return match;
            }
        }

        // Handle MenuItem
        else if (anEvent.getKeyCombo().equals(aMenuItem.getShortcutCombo()))
            return aMenuItem;

        // Return not found
        return null;
    }

    /**
     * Paints background.
     */
    protected void paintBack(Painter aPntr)
    {
        super.paintBack(aPntr);
        double viewW = getWidth();
        double viewH = getHeight() - .5;
        aPntr.setColor(Color.LIGHTGRAY);
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.drawLine(0, viewH, viewW, viewH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getMinHeightImpl()  { return getFont().getSize() + 12; }

    /**
     * Override to return row layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()
    {
        return new RowViewLayout(this, true);
    }

    /**
     * Override to handle menu bar short cut key actions.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
        if (aValue) {
            RootView rootView = getRootView();
            if (rootView != null)
                rootView.addEventHandler(_rootViewKeyPressListener);
        }
        else if (_lastRootView != null) {
            _lastRootView.removeEventHandler(_rootViewKeyPressListener);
            _lastRootView = null;
        }

    }

    // RootView key press handler support
    private EventListener _rootViewKeyPressListener = this::handleKeyPressEvent;
    private RootView _lastRootView;

    /**
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);
        aPropSet.addPropNamed(Menus_Prop, List.class);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals(Menus_Prop))
            return getMenus();
        return super.getPropValue(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals(Menus_Prop))
            setMenus((List<Menu>) aValue);
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Creates a ColView holding given MenuBar and content view with a Key EventListener to send shortcut keys to MenuBar.
     */
    public static ColView createMenuBarView(MenuBar aMenuBar, View aView)
    {
        // Create ColView that makes given MenuBar FillWidth and given View fill extra height
        ColView menuBarView = new ColView();
        menuBarView.setFillWidth(true);
        menuBarView.addChild(aMenuBar);
        menuBarView.addChild(aView);
        aView.setGrowHeight(true);
        return menuBarView;
    }
}