/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Point;
import snap.geom.Size;
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

    // The items
    private List<MenuItem>  _items = new ArrayList<>();

    /**
     * Constructor.
     */
    public MenuButton()
    {
        setShowArrow(true);
    }

    /**
     * Returns the items.
     */
    public List<MenuItem> getItems()  { return _items; }

    /**
     * Sets the items.
     */
    public void setItems(List<MenuItem> theItems)
    {
        _items.clear();
        if (theItems != null)
            for (MenuItem mi : theItems)
                addItem(mi);
    }

    /**
     * Adds a new item.
     */
    public void addItem(MenuItem anItem)
    {
        _items.add(anItem);
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
            setGraphicAfter(arrowView);
        }

        // Otherwise, remove
        else setGraphicAfter(null);

        // Set value and firePropChange
        firePropChange("ShowArrow", _showArrow, _showArrow = aValue);
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
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            setTargeted(true);
            repaint();
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit()) {
            setTargeted(false);
            repaint();
        }

        // Handle MousePress
        else if (anEvent.isMousePress()) {
            setPressed(false);
            setTargeted(false);
            fireActionEvent(anEvent);
            repaint();
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease()) {
            setPressed(false);
            setTargeted(false);
            repaint();
        }
    }

    /**
     * Override to show menu.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        super.fireActionEvent(anEvent);
        Menu popup = new Menu();
        for (MenuItem item : getItems()) popup.addItem(item);
        popup.show(this, 0, getHeight());
    }

    /**
     * Override to send to items.
     */
    public void setOwner(ViewOwner anOwner)
    {
        super.setOwner(anOwner);
        for (View child : _items) child.setOwner(anOwner);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowArrow, PopupPoint, PopupSize
        if (!isShowArrow()) e.add("ShowArrow", false);
        if (getPopupPoint() != null) {
            e.add("PopupX", getPopupPoint().x);
            e.add("PopupY", getPopupPoint().y);
        }
        if (getPopupSize() != null) {
            e.add("PopupWidth", getPopupSize().width);
            e.add("PopupHeight", getPopupSize().height);
        }

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
        setShowArrow(anElement.getAttributeBooleanValue("ShowArrow", true));

        // Unarchive PopupPoint
        if (anElement.hasAttribute("PopupX") || anElement.hasAttribute("PopupY")) {
            int x = anElement.getAttributeIntValue("PopupX");
            int y = anElement.getAttributeIntValue("PopupY");
            setPopupPoint(new Point(x, y));
        }

        // Unarchive PopupSize
        if (anElement.hasAttribute("PopupWidth") || anElement.hasAttribute("PopupHeight")) {
            int w = anElement.getAttributeIntValue("PopupWidth");
            int h = anElement.getAttributeIntValue("PopupHeight");
            setPopupSize(new Size(w, h));
        }
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (View child : getItems())
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
            Class<?> childClass = anArchiver.getClass(childXML.getName());
            if (childClass != null && MenuItem.class.isAssignableFrom(childClass)) {
                MenuItem mitem = (MenuItem) anArchiver.fromXML(childXML, this);
                addItem(mitem);
            }
        }
    }
}