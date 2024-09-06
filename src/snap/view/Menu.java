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
public class Menu extends MenuItem implements ViewHost {

    // List of MenuItems
    private List<MenuItem> _items = new ArrayList<>();

    // The Arrow graphic
    private static Polygon _arrow = new Polygon(0, 0, 9, 5, 0, 10);

    // The PopupWindow
    private PopupWindow _popupWindow;

    // Constants for properties
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
     * Returns the child items.
     */
    public List<MenuItem> getItems()  { return _items; }

    /**
     * Returns the number of items.
     */
    public int getItemCount()
    {
        return _items.size();
    }

    /**
     * Override to return child as MenuItem.
     */
    public MenuItem getItem(int anIndex)
    {
        return _items.get(anIndex);
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
        for (MenuItem menuItem : _items)
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
     * Returns the menu showing (or null).
     */
    public Menu getMenuShowing()
    {
        for (MenuItem menuItem : getItems())
            if (menuItem instanceof Menu && ((Menu) menuItem).isPopupShowing())
                return (Menu) menuItem;
        return null;
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
        child = ListUtils.findMatch(_items, item -> Objects.equals(aName, item.getName()));
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
        for (View child : _items)
            child.setOwner(anOwner);
    }

    /**
     * ViewHost method: Returns the number of guest views.
     */
    public int getGuestCount()  { return getItemCount(); }

    /**
     * ViewHost method: Returns the guest view at given index.
     */
    public View getGuest(int anIndex)  { return getItem(anIndex); }

    /**
     * ViewHost method: Adds the given view to this host's guest (children) list at given index.
     */
    public void addGuest(View aChild, int anIndex)
    {
        addItem((MenuItem) aChild);
    }

    /**
     * ViewHost method: Remove's guest at given index from this host's guest (children) list.
     */
    public View removeGuest(int anIndex)
    {
        return null; //removeItem(anIndex);
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
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_MENU_FONT);
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (int i = 0, iMax = getItemCount(); i < iMax; i++) {
            MenuItem child = getItem(i);
            anElement.add(anArchiver.toXML(child, this));
        }
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive MenuItems
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement childXML = anElement.get(i);
            Class<?> cls = anArchiver.getClass(childXML.getName());
            if (cls != null && MenuItem.class.isAssignableFrom(cls)) {
                MenuItem mi = (MenuItem) anArchiver.fromXML(childXML, this);
                addItem(mi);
            }
        }
    }

}