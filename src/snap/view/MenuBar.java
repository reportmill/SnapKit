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

    // Constants for properties
    protected static Font DEFAULT_MENU_BAR_FONT = new Font("Arial", 13);

    /**
     * Constructor.
     */
    public MenuBar()
    {
        super();
        _font = DEFAULT_MENU_BAR_FONT;
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
    protected void processEvent(ViewEvent anEvent)
    {
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
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Menus
        aPropSet.addPropNamed(Menus_Prop, List.class);

        // Reset defaults
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_MENU_BAR_FONT);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Menus
        if (aPropName.equals(Menus_Prop))
            return getMenus();

        // Do normal version
        return super.getPropValue(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Menus
        if (aPropName.equals(Menus_Prop))
            setMenus((List<Menu>) aValue);

            // Do normal version
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (Menu childMenu : getMenus())
            anElement.add(anArchiver.toXML(childMenu, this));
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive MenuItems
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) { XMLElement childXML = anElement.get(i);
            Class<?> cls = anArchiver.getClassForName(childXML.getName());
            if (cls != null && Menu.class.isAssignableFrom(cls)) {
                Menu menu = (Menu)anArchiver.fromXML(childXML, this);
                addMenu(menu);
            }
        }
    }

    /**
     * Creates a ColView holding given MenuBar and content view with a Key EventListener to send shortcut keys to MenuBar.
     */
    public static ColView createMenuBarView(MenuBar aMenuBar, View aView)
    {
        // Create ColView that makes given MenuBar FillWidth and given View fill extra height
        ColView colView = new ColView();
        colView.setFillWidth(true);
        colView.addChild(aMenuBar);
        colView.addChild(aView);
        aView.setGrowHeight(true);

        // Add EventListener (filter) to intercept any KeyPress + ShortCut events and run by MenuBar
        colView.addEventHandler(e -> menuBarViewContentDidKeyPress(aMenuBar, e), KeyPress);
        return colView;
    }

    /** Forwards KeyPress + ShortCut events to MenuBar. */
    private static void menuBarViewContentDidKeyPress(MenuBar aMenuBar, ViewEvent anEvent)
    {
        if (anEvent.isShortcutDown())
            ViewUtils.processEvent(aMenuBar, anEvent);
    }
}