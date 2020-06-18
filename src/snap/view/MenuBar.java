/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.Arrays;
import snap.gfx.*;
import snap.util.*;

/**
 * A View to display menus in a menu bar.
 */
public class MenuBar extends ParentView {

    // The menubar fill
    static Color c1 = new Color("#F9"), c2 = new Color("#EF"), c3 = new Color("#EA");
    static Color c4 = new Color("#E6"), c5 = new Color("#E1");
    static GradientPaint.Stop stops[] = GradientPaint.getStops(new double[] { 0,.25,.5,.75,1 }, c1, c2, c3, c4, c5);
    static GradientPaint MENU_BAR_PAINT = new GradientPaint(0,0,0,1,stops);
    static Font MENU_BAR_FONT = new Font("Arial", 13);

    /**
     * Creates a new MenuBarNode.
     */
    public MenuBar()
    {
        setFill(MENU_BAR_PAINT);
        setPadding(0,10,0,10);
        setFont(MENU_BAR_FONT);
    }

    /**
     * Returns the child menus.
     */
    public Menu[] getMenus()
    {
        return Arrays.copyOf(getChildren(), getChildCount(), Menu[].class);
    }

    /**
     * Returns the number of menus.
     */
    public int getMenuCount()  { return getChildCount(); }

    /**
     * Override to return child as Menu.
     */
    public Menu getMenu(int anIndex)  { return (Menu)getChild(anIndex); }

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
        for (View item : getChildren()) {
            MenuItem match = getMatchingMenuItem((MenuItem)item, anEvent);
            if (match!=null) {
                match.fireActionEvent(anEvent);
                anEvent.consume(); return;
            }
        }
    }

    /**
     * Returns a matching menu item.
     */
    public MenuItem getMatchingMenuItem(MenuItem aMI, ViewEvent anEvent)
    {
        if (aMI instanceof Menu) { Menu menu = (Menu)aMI;
            for (MenuItem item : menu.getItems()) {
                MenuItem match = getMatchingMenuItem(item, anEvent);
                if(match!=null)
                    return match;
            }
        }
        else if (anEvent.getKeyCombo().equals(aMI.getShortcutCombo()))
            return aMI;
        return null;
    }

    /**
     * Paints background.
     */
    protected void paintBack(Painter aPntr)
    {
        super.paintBack(aPntr);
        double w = getWidth(), h = getHeight() - .5;
        aPntr.setColor(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1);
        aPntr.drawLine(0,h,w,h);
    }

    /**
     * Returns the preferred height.
     */
    protected double getMinHeightImpl()  { return getFont().getSize() + 10; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, aW); }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { RowView.layout(this, true); }

    /**
     * Override to return default.
     */
    public Paint getDefaultFill()  { return MENU_BAR_PAINT; }

    /**
     * Returns the default font.
     */
    public Font getDefaultFont()  { return MENU_BAR_FONT; }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (int i=0, iMax=getMenuCount(); i<iMax; i++) { Menu child = getMenu(i);
            anElement.add(anArchiver.toXML(child, this)); }
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive MenuItems
        for (int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
            Class cls = anArchiver.getClass(childXML.getName());
            if (cls!=null && Menu.class.isAssignableFrom(cls)) {
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
        ColView colView = new ColView(); colView.setFillWidth(true);
        colView.addChild(aMenuBar); colView.addChild(aView);
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