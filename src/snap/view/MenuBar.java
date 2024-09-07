/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View to display menus in a menu bar.
 */
public class MenuBar extends ParentView implements ViewHost {

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
    public Menu[] getMenus()
    {
        View[] children = getChildren();
        return ArrayUtils.filterByClass(children, Menu.class);
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
        for (Menu m : getMenus())
            if (m.isPopupShowing())
                return m;
        return null;
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
        if (aMenuItem instanceof Menu) {
            Menu menu = (Menu) aMenuItem;
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
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return RowView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { RowView.layout(this, true); }

    /**
     * ViewHost method: Returns the number of guest views.
     */
    public int getGuestCount()  { return getMenus().length; }

    /**
     * ViewHost method: Returns the guest view at given index.
     */
    public View getGuest(int anIndex)  { return getMenus()[anIndex]; }

    /**
     * ViewHost method: Adds the given view to this host's guest (children) list at given index.
     */
    public void addGuest(View aChild, int anIndex)
    {
        addMenu((Menu) aChild);
    }

    /**
     * ViewHost method: Remove's guest at given index from this host's guest (children) list.
     */
    public View removeGuest(int anIndex)
    {
        return removeMenu(anIndex);
    }

    /**
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Reset defaults
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_MENU_BAR_FONT);
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
            Class<?> cls = anArchiver.getClass(childXML.getName());
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