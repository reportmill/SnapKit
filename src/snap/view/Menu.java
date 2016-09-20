/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A MenuItem subclass to show child menu items.
 */
public class Menu extends MenuItem {
    
    // List of MenuItems
    List <MenuItem>        _items = new ArrayList();
    
    // The Arrow graphic
    static Polygon         _arrow = new Polygon(0, 0, 9, 5, 0, 10);

/**
 * Creates a new Menu.
 */
public Menu()  { setFont(MenuBar.MENU_BAR_FONT); }

/**
 * Returns whether menu is showing arrow graphic.
 */
public boolean isShowArrow()  { return getLabel().getGraphicAfter()!=null; }

/**
 * Sets whether menu is showing arrow graphic.
 */
public void setShowArrow(boolean aVal)
{
    if(aVal==isShowArrow()) return;
    if(!aVal) { setGraphicAfter(null); return; }
    ShapeView sview = new ShapeView(_arrow); sview.setFill(Color.DARKGRAY); sview.setLean(Pos.CENTER_RIGHT);
    setGraphicAfter(sview); getLabel().setSpacing(12);
    getLabel().setGrowWidth(true);
}

/**
 * Returns the child items.
 */
public List <MenuItem> getItems()  { return _items; }

/**
 * Returns the number of items.
 */
public int getItemCount()  { return _items.size(); }

/**
 * Override to return child as MenuItem.
 */
public MenuItem getItem(int anIndex)  { return _items.get(anIndex); }

/**
 * Override to return child as MenuItem.
 */
public void addItem(MenuItem aMenuItem)
{
    // Add item
    _items.add(aMenuItem);
    aMenuItem._parentMenu = this;
    
    // If child is menu, add arrow graphic
    if(aMenuItem instanceof Menu)
        ((Menu)aMenuItem).setShowArrow(true);
}

/**
 * Adds a separator.
 */
public void addSeparator()  { addItem(new MenuItem()); }

/**
 * Override to include child menu items.
 */
public View getChild(String aName)
{
    View n = super.getChild(aName); if(n!=null) return n;
    for(View node : _items)
        if(SnapUtils.equals(aName,node.getName()))
            return node;
    return null;
}

/**
 * Returns a popup node for this menu.
 */
public PopupWindow getPopup()
{
    hide();
    PopupWindow pop = new PopupWindow(); pop.setFont(getDefaultFont());
    VBox vbox = new VBox(); vbox.setMinWidth(125); vbox.setFillWidth(true); vbox.setPadding(4,1,4,1);
    for(View node : _items) { vbox.addChild(node); node.addEventHandler(_lsnr, Action); }
    pop.setContent(vbox);
    return _pop = pop;
}

// A listener to close popup
PopupWindow _pop;
EventListener _lsnr = e -> hide();

/**
 * Show menu.
 */
public void show(View aView, double aX, double aY)
{
    PopupWindow pop = getPopup();
    pop.show(aView,aX,aY);
}

/**
 * Hides the popup.
 */
public void hide()
{
    if(_pop==null) return;
    _pop.hide();
    VBox vbox = (VBox)_pop.getContent();
    for(View node : vbox.getChildren()) node.removeEventHandler(_lsnr, Action);
    _pop = null;
}

/**
 * Returns whether popup is showing.
 */
public boolean isPopupShowing()  { return _pop!=null && _pop.isShowing(); }

/**
 * Override to show popup.
 */
public void fireActionEvent()
{
    double x = getParent() instanceof VBox? getWidth()-1 : 0;
    double y = getParent() instanceof MenuBar? getHeight()-1 : 0;
    show(this, x, y);
}

/**
 * Override to show if in MenuBar or Menu.
 */
protected void setTargeted(boolean aValue)
{
    if(aValue==_targeted) return; super.setTargeted(aValue);
    
    // Handle when in MenuBar
    if(getParent() instanceof MenuBar && aValue) { MenuBar mbar = (MenuBar)getParent();
        Menu showing = mbar.getMenuShowing(); if(showing==null) return;
        showing.hide();
        fireActionEvent();
    }
    
    // Handle when in Menu
    if(getParent() instanceof VBox) {
        if(aValue) fireActionEvent();
        //else hide();
    }
}

/**
 * Returns the menu showing (or null).
 */
public Menu getMenuShowing()
{
    for(MenuItem mi : getItems()) if(mi instanceof Menu && ((Menu)mi).isPopupShowing()) return (Menu)mi;
    return null;
}

/**
 * Override to send to items.
 */
public void setOwner(ViewOwner anOwner)
{
    super.setOwner(anOwner);
    for(View child : _items) child.setOwner(anOwner);
}

/**
 * Returns the default font.
 */
public Font getDefaultFont()  { return MenuBar.MENU_BAR_FONT; }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getItemCount(); i<iMax; i++) { MenuItem child = getItem(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive MenuItems
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && MenuItem.class.isAssignableFrom(cls)) {
            MenuItem mi = (MenuItem)anArchiver.fromXML(childXML, this);
            addItem(mi);
        }
    }
}

}