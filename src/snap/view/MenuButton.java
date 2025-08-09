/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.HPos;
import snap.geom.Point;
import snap.geom.Size;
import snap.props.PropSet;
import snap.util.*;

/**
 * Button subclass to show a menu when clicked.
 */
public class MenuButton extends ButtonBase {

    // Whether button shows down arrow
    private boolean  _showArrow;

    // The popup point
    private Point  _popPoint;

    // The popup size
    private Size  _popSize;

    // The menu items
    private ObjectArray<MenuItem> _menuItems = new ObjectArray<>(MenuItem.class);

    // The menu
    private Menu _menu;

    // Constants for properties
    public static final String MenuItems_Prop = "MenuItems";
    public static final String ShowArrow_Prop = "ShowArrow";

    /**
     * Constructor.
     */
    public MenuButton()
    {
        super();
        setShowArrow(true);
    }

    /**
     * Returns the menu items.
     */
    public MenuItem[] getMenuItems()  { return _menuItems.getArray(); }

    /**
     * Sets the menu items.
     */
    public void setMenuItems(MenuItem[] theItems)
    {
        MenuItem[] oldItems = _menuItems.getArray();
        _menuItems.clear();
        if (theItems != null)
            for (MenuItem mi : theItems)
                addMenuItem(mi);

        // Fire prop change
        firePropChange(MenuItems_Prop, oldItems, theItems);
    }

    /**
     * Adds a menu item.
     */
    public void addMenuItem(MenuItem anItem)
    {
        _menuItems.add(anItem);
    }

    /**
     * Returns the menu item with given name.
     */
    public MenuItem getMenuItemForName(String aName)
    {
        return ArrayUtils.findMatch(getMenuItems(), menuItem -> Objects.equals(menuItem.getName(), aName));
    }

    /**
     * Returns whether button should show arrow.
     */
    public boolean isShowArrow()  { return _showArrow; }

    /**
     * Sets whether button should show arrow.
     */
    public void setShowArrow(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowArrow()) return;

        // If setting, create/config/add arrowView
        if (aValue) {
            ImageView arrowView = new ImageView(ComboBox.getArrowImage());
            arrowView.setPadding(0, 2, 0, 2);
            arrowView.setLeanX(HPos.RIGHT);
            setGraphicAfter(arrowView);

            // Configure label
            Label label = getLabel();
            label.setGrowWidth(true);
            label.setAlignX(HPos.CENTER);
            label.setPadding(2, 2, 2, 2);
        }

        // Otherwise, remove
        else setGraphicAfter(null);

        // Set value and firePropChange
        firePropChange(ShowArrow_Prop, _showArrow, _showArrow = aValue);
    }

    /**
     * Returns the popup point.
     */
    public Point getPopupPoint()  { return _popPoint; }

    /**
     * Sets the popup point.
     */
    public void setPopupPoint(Point aValue)
    {
        firePropChange("PopupPoint", _popPoint, _popPoint = aValue);
    }

    /**
     * Sets the popup point.
     */
    public void setPopupXY(double popupX, double popupY)
    {
        setPopupPoint(new Point(popupX, popupY));
    }

    /**
     * Returns the popup size.
     */
    public Size getPopupSize()  { return _popSize; }

    /**
     * Sets the popup size.
     */
    public void setPopupSize(Size aValue)
    {
        firePropChange("PopupSize", _popSize, _popSize = aValue);
    }

    /**
     * Sets the popup size.
     */
    public void setPopupSize(double aW, double aH)
    {
        setPopupSize(new Size(aW, aH));
    }

    /**
     * Returns the menu.
     */
    public Menu getMenu()
    {
        // If already set, just return
        if (_menu != null) return _menu;

        // Create menu with items
        Menu menu = new Menu();
        MenuItem[] menuItems = getMenuItems();
        menu.setMenuItems(menuItems);

        // Return
        return _menu = menu;
    }

    /**
     * Returns whether menu is showing.
     */
    public boolean isMenuShowing()  { return _menu != null; }

    /**
     * Shows the popup menu.
     */
    public void showMenu()
    {
        // Get menu with items
        Menu menu = getMenu();

        // Show menu
        double menuY = getHeight();
        menu.showMenuAtXY(this, 0, menuY);
        menu.getPopup().addPropChangeListener(pc -> ViewUtils.runLater(() -> hideMenu()), Showing_Prop);
    }

    /**
     * Hides the menu.
     */
    public void hideMenu()
    {
        if (_menu == null) return;
        _menu.hide();
        _menu = null;
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEnter
        if (anEvent.isMouseEnter())
            setTargeted(true);

        // Handle MouseExit
        else if (anEvent.isMouseExit())
            setTargeted(false);

        // Handle MousePress
        else if (anEvent.isMousePress()) {
            setPressed(false);
            setTargeted(false);
            if (!isMenuShowing())
                showMenu();
            else hideMenu();
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease()) {
            boolean isPressed = isPressed();
            setPressed(false);
            setTargeted(false);

            // Fire action event
            if (isPressed)
                fireActionEvent(anEvent);
        }
    }

    /**
     * Returns the item for given name.
     */
    public MenuItem getItemForName(String aName)
    {
        return ArrayUtils.findMatch(getMenuItems(), menuItem -> Objects.equals(menuItem.getName(), aName));
    }

    /**
     * Override to send to items.
     */
    @Override
    public void setOwner(ViewOwner anOwner)
    {
        super.setOwner(anOwner);
        for (View child : getMenuItems())
            child.setOwner(anOwner);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // MenuItems, ShowArrow
        aPropSet.addPropNamed(MenuItems_Prop, MenuItem[].class, EMPTY_OBJECT);
        aPropSet.addPropNamed(ShowArrow_Prop, boolean.class, true);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // MenuItems, ShowArrow
            case MenuItems_Prop: return getMenuItems();
            case ShowArrow_Prop: return isShowArrow();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // MenuItems, ShowArrow
            case MenuItems_Prop: setMenuItems((MenuItem[]) aValue); break;
            case ShowArrow_Prop: setShowArrow(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowArrow
        if (!isShowArrow()) e.add(ShowArrow_Prop, false);

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive ShowArrow
        setShowArrow(anElement.getAttributeBooleanValue(ShowArrow_Prop, true));
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (View child : getMenuItems())
            anElement.add(anArchiver.toXML(child, this));
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive shapes
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement childXML = anElement.get(i);

            // Get child class - if MenuItem, unarchive and add
            Class<?> childClass = anArchiver.getClassForName(childXML.getName());
            if (childClass != null && MenuItem.class.isAssignableFrom(childClass)) {
                MenuItem mitem = (MenuItem) anArchiver.fromXML(childXML, this);
                addMenuItem(mitem);
            }
        }
    }
}