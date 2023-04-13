/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.*;

/**
 * A MenuItem subclass to show child menu items.
 */
public class Menu extends MenuItem {

    // List of MenuItems
    List<MenuItem>  _items = new ArrayList<>();

    // The Arrow graphic
    static Polygon  _arrow = new Polygon(0, 0, 9, 5, 0, 10);

    // The PopupWindow
    private PopupWindow  _popupWindow;

    // A listener to close popup
    private EventListener  _itemFiredActionListener = e -> itemFiredActionEvent(e);

    /**
     * Constructor.
     */
    public Menu()
    {
        setFont(MenuBar.MENU_BAR_FONT);
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
        _popupWindow.setFont(getDefaultFont());

        // Create ItemColView to hold MenuItems
        ColView itemColView = new ColView();
        itemColView.setMinWidth(125);
        itemColView.setFillWidth(true);
        itemColView.setPadding(4, 1, 4, 1);

        // Add MenuItems to ItemColView
        for (MenuItem menuItem : _items) {
            itemColView.addChild(menuItem);
            menuItem.addEventHandler(_itemFiredActionListener, Action);
        }

        // Set PopupWindow.Content to ItemColView and return
        _popupWindow.setContent(itemColView);
        return _popupWindow;
    }

    /**
     * Show menu.
     */
    public void show(View aView, double aX, double aY)
    {
        PopupWindow pop = getPopup();
        pop.show(aView, aX, aY);
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

        // Remove MenuItem itemFiredActionListener
        ColView itemColView = (ColView) _popupWindow.getContent();
        for (View item : itemColView.getChildren())
            item.removeEventHandler(_itemFiredActionListener, Action);

        // Clear PopupWindow
        _popupWindow = null;
    }

    /**
     * Returns whether popup is showing.
     */
    public boolean isPopupShowing()
    {
        return _popupWindow != null && _popupWindow.isShowing();
    }

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
    protected void itemFiredActionEvent(ViewEvent anEvent)
    {
        hideAll();
    }

    /**
     * Override to show popup.
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        double x = getParent() instanceof ColView ? getWidth() - 1 : 0;
        double y = getParent() instanceof MenuBar ? getHeight() - 1 : 0;
        show(this, x, y);
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

        // Handle when in MenuBar
        if (getParent() instanceof MenuBar && aValue) {
            MenuBar mbar = (MenuBar) getParent();
            Menu showing = mbar.getMenuShowing();
            if (showing == null) return;
            showing.hide();
            fireActionEvent(null);
        }

        // Handle when in Menu
        if (getParent() instanceof ColView) {
            if (aValue)
                fireActionEvent(null);
            //else hide();
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
     * Returns the default font.
     */
    public Font getDefaultFont()
    {
        return MenuBar.MENU_BAR_FONT;
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