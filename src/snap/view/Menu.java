/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A MenuItem subclass to show child menu items.
 */
public class Menu extends MenuItem {

    // MenuItems
    private ObjectArray<MenuItem> _items = new ObjectArray<>(MenuItem.class);

    // The Arrow graphic
    private static Polygon _arrow = new Polygon(0, 0, 9, 5, 0, 10);

    // The PopupWindow
    private PopupWindow _popupWindow;

    // Constants for properties
    public static final String MenuItems_Prop = "MenuItems";

    // Constants for property defaults
    protected static Font DEFAULT_MENU_FONT = MenuBar.DEFAULT_MENU_BAR_FONT;

    /**
     * Constructor.
     */
    public Menu()
    {
        super();
        _font = DEFAULT_MENU_FONT;
    }

    /**
     * Returns whether menu is showing arrow graphic.
     */
    public boolean isShowArrow()
    {
        Label label = getLabel();
        return label.getGraphicAfter() != null;
    }

    /**
     * Sets whether menu is showing arrow graphic.
     */
    public void setShowArrow(boolean aVal)
    {
        // If already set, just return
        if (aVal == isShowArrow()) return;

        // If off, remove graphic and return
        if (!aVal) {
            setGraphicAfter(null);
            return;
        }

        // Create/configure Arrow ShapeView and set
        ShapeView arrowView = new ShapeView(_arrow);
        arrowView.setFill(Color.DARKGRAY);
        arrowView.setLean(Pos.CENTER_RIGHT);
        setGraphicAfter(arrowView);

        // Adjust label
        getLabel().setSpacing(12);
        getLabel().setGrowWidth(true);
    }

    /**
     * Returns the child menu items.
     */
    public MenuItem[] getMenuItems()  { return _items.getArray(); }

    /**
     * Sets the child menu items.
     */
    public void setMenuItems(MenuItem[] theItems)
    {
        _items.clear();
        for (MenuItem item : theItems)
            addItem(item);
    }

    /**
     * Override to return child as MenuItem.
     */
    public void addItem(MenuItem aMenuItem)
    {
        // Add item
        _items.add(aMenuItem);
        aMenuItem._parentMenu = this;

        // If child is menu, add arrow graphic
        if (aMenuItem instanceof Menu)
            ((Menu) aMenuItem).setShowArrow(true);
    }

    /**
     * Adds a separator.
     */
    public void addSeparator()
    {
        MenuItem seperator = new MenuItem();
        addItem(seperator);
    }

    /**
     * Returns a popup node for this menu.
     */
    public PopupWindow getPopup()
    {
        // If already set, just return
        if (_popupWindow != null) return _popupWindow;

        // Create PopupWindow, configure with items and return
        _popupWindow = new PopupWindow();
        _popupWindow.setFont(getFont());

        // Create ItemColView to hold MenuItems
        ColView itemColView = new ColView();
        itemColView.setMinWidth(125);
        itemColView.setFillWidth(true);
        itemColView.setPadding(4, 1, 4, 1);

        // Add MenuItems to ItemColView
        for (MenuItem menuItem : getMenuItems())
            itemColView.addChild(menuItem);

        // Set PopupWindow.Content to ItemColView and return
        _popupWindow.setContent(itemColView);
        return _popupWindow;
    }

    /**
     * Show menu.
     */
    public void showMenu()
    {
        ParentView parentView = getParent();
        double menuX = parentView instanceof ColView ? getWidth() - 1 : 0;
        double menuY = parentView instanceof MenuBar ? getHeight() - 1 : 0;
        showMenuAtXY(this, menuX, menuY);
    }

    /**
     * Show menu.
     */
    public void showMenuAtXY(View aView, double menuX, double menuY)
    {
        if (isPopupShowing()) return;
        PopupWindow popupWindow = getPopup();
        popupWindow.show(aView, menuX, menuY);
    }

    /**
     * Hides the popup.
     */
    public void hide()
    {
        // If already set, just return
        if (_popupWindow == null) return;

        // Hide PopupWindow
        _popupWindow.hide();

        // Clear PopupWindow
        _popupWindow = null;
    }

    /**
     * Returns whether popup is showing.
     */
    public boolean isPopupShowing()  { return _popupWindow != null && _popupWindow.isShowing(); }

    /**
     * Hides this menu and parent menus.
     */
    protected void hideAll()
    {
        if (_parentMenu != null)
            _parentMenu.hideAll();
        else hide();
    }

    /**
     * Hides child menu popup windows.
     */
    protected void hideChildPopupWindows()
    {
        PopupWindow popupWindow = getPopup();
        if (popupWindow != null) {
            PopupWindow childPopupWindow = popupWindow.getPopup();
            if (childPopupWindow != null)
                childPopupWindow.hide();
        }
    }

    /**
     * Called when child MenuItem fires action.
     */
    protected void itemFiredActionEvent()
    {
        hideAll();
    }

    /**
     * Override to show popup.
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        // Show menu
        showMenu();
    }

    /**
     * Override to show if in MenuBar or Menu.
     */
    @Override
    protected void setTargeted(boolean aValue)
    {
        // Do normal version
        if (aValue == _targeted) return;
        super.setTargeted(aValue);

        // Handle targeted true
        if (aValue) {

            // Handle when in MenuBar
            ParentView parentView = getParent();
            if (parentView instanceof MenuBar) {
                MenuBar menuBar = (MenuBar) parentView;
                Menu menuShowing = menuBar.getMenuShowing();
                if (menuShowing == null)
                    return;
                menuShowing.hide();
                showMenu();
            }

            // Handle when in Menu
            if (parentView instanceof ColView)
                showMenu();
        }
    }

    /**
     * Override to include child menu items.
     */
    @Override
    public View getChildForName(String aName)
    {
        // Do normal version (just return if null)
        View child = super.getChildForName(aName);
        if (child != null)
            return child;

        // Search MenuItems for name, return if found
        child = ArrayUtils.findMatch(getMenuItems(), item -> Objects.equals(aName, item.getName()));
        if (child != null)
            return child;

        // Return not found
        return null;
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
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // MenuItems
        aPropSet.addPropNamed(MenuItems_Prop, MenuItem[].class, EMPTY_OBJECT);

        // Reset defaults
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_MENU_FONT);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // MenuItems
        if (aPropName.equals(MenuItems_Prop))
            return getMenuItems();

        // Do normal version
        return super.getPropValue(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // MenuItems
        if (aPropName.equals(MenuItems_Prop))
            setMenuItems((MenuItem[]) aValue);

        // Do normal version
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        for (MenuItem child : getMenuItems())
            anElement.add(anArchiver.toXML(child, this));
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive MenuItems
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement childXML = anElement.get(i);
            Class<?> cls = anArchiver.getClassForName(childXML.getName());
            if (cls != null && MenuItem.class.isAssignableFrom(cls)) {
                MenuItem mi = (MenuItem) anArchiver.fromXML(childXML, this);
                addItem(mi);
            }
        }
    }

}