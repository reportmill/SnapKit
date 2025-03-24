/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.HPos;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A ButtonBaseNode for MenuItem.
 */
public class MenuItem extends ButtonBase implements Cloneable {

    // The accelerator string
    private String _shortcut;
    
    // The accelerator key combo
    private KeyCombo _keyCombo;
    
    // Whether item is selected
    private boolean _selected;
    
    // The parent Menu (if there is one)
    protected Menu _parentMenu;
    
    // Constants for properties
    public static final String Selected_Prop = "Selected";
    public static final String Shortcut_Prop = "Shortcut";

    // Custom property defaults
    private static boolean DEFAULT_MENU_ITEM_SHOW_AREA = false;

    /**
     * Constructor.
     */
    public MenuItem()
    {
        super();
        _showArea = DEFAULT_MENU_ITEM_SHOW_AREA;
    }

    /**
     * Returns whether button is selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether button is selected.
     */
    public void setSelected(boolean aValue)
    {
        if (isSelected() == aValue) return;
        firePropChange(Selected_Prop, _selected, _selected=aValue);
    }

    /**
     * Returns the key string.
     */
    public String getShortcut()  { return _shortcut; }

    /**
     * Sets the key string.
     */
    public void setShortcut(String aValue)
    {
        firePropChange(Shortcut_Prop, _shortcut, _shortcut = aValue);

        // Set graphic
        if (aValue == null) { setGraphicAfter(null); return; }
        String str = getShortcutText();
        StringView text = new StringView(); text.setText(str); text.setLeanX(HPos.RIGHT);
        setGraphicAfter(text); getLabel().setSpacing(12);
        getLabel().setGrowWidth(true);
    }

    /**
     * Returns the accelerator key combo.
     */
    public KeyCombo getShortcutCombo()
    {
        return _keyCombo != null || _shortcut == null ? _keyCombo : (_keyCombo = KeyCombo.get(_shortcut));
    }

    /**
     * Returns the shortcut key as string.
     */
    public String getShortcutText()
    {
        KeyCombo keyCombo = getShortcutCombo();
        String key = Character.toString((char) keyCombo.getKeyCode());
        String str = (SnapEnv.isMac ? Character.toString((char)8984) : "^") + key;
        if (keyCombo.isShiftDown())
            str = (char) 8679 + str;
        return str;
    }

    /**
     * Returns whether menu item is a separator.
     */
    public boolean isSeparator()
    {
        String text = getText();
        View graphic = getGraphic();
        return (text == null || text.isEmpty()) && graphic == null;
    }

    /**
     * Returns the parent Menu (if there is one).
     */
    public Menu getParentMenu()  { return _parentMenu; }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()
    {
        return this instanceof CheckBoxMenuItem ? "Selected" : "Text";
    }

    /**
     * Sets whether button is under mouse.
     */
    @Override
    protected void setTargeted(boolean aValue)
    {
        // Do normal version
        if (aValue == isTargeted()) return;
        super.setTargeted(aValue);

        // Update Fill/TextColor to show Targetted
        setFill(aValue ? ViewUtils.getTargetFill() : null);
        getLabel().setTextColor(aValue ? ViewUtils.getTextTargetedColor() : Color.BLACK);

        // If targeting this menu item, hide any previous peer menu popups
        if (aValue) {
            Menu parentMenu = getParentMenu();
            if (parentMenu != null)
                parentMenu.hideChildPopupWindows();
        }
    }

    /**
     * Override to suppress painting.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        if (isSeparator())
            paintSeparator(aPntr);
    }

    /**
     * Paints the menu item as a separator.
     */
    protected void paintSeparator(Painter aPntr)
    {
        double ly = 1.5;
        double px = 2;
        double pw = getWidth() - px - 2;
        aPntr.setPaint(new Color(1,1,1,.5));
        aPntr.drawLine(px,ly-1,px+pw,ly-1);
        aPntr.drawLine(px,ly+1,px+pw,ly+1);
        aPntr.setPaint(Color.LIGHTGRAY);
        aPntr.drawLine(px,ly,px+pw,ly);
    }

    /**
     * Returns the preferred height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return isSeparator() ? 3 : super.getPrefHeightImpl(aW);
    }

    /**
     * Override to notify parent menu (to hide all).
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        super.fireActionEvent(anEvent);

        // Notify ParentMenu that action fired
        if (_parentMenu != null)
            _parentMenu.itemFiredActionEvent();
    }

    /**
     * Copies a menu item.
     */
    public MenuItem clone()
    {
        MenuItem clone;
        if (this instanceof Menu) {
            clone = new Menu();
            MenuItem[] menuItems = ((Menu) this).getMenuItems();
            MenuItem[] menuItemsCopy = ArrayUtils.map(menuItems, item -> item.clone(), MenuItem.class);
            ((Menu) clone).setMenuItems(menuItemsCopy);
        }
        else if (this instanceof CheckBoxMenuItem)
            clone = new CheckBoxMenuItem();
        else clone = new MenuItem();

        clone.setText(getText());
        clone.setImage(getImage());
        clone.setName(getName());
        clone.setSelected(isSelected());
        return clone;
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Shortcut
        aPropSet.addPropNamed(Shortcut_Prop, String.class, EMPTY_OBJECT);

        // Reset ShowArea default
        aPropSet.getPropForName(ShowArea_Prop).setDefaultValue(DEFAULT_MENU_ITEM_SHOW_AREA);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Selected, Shortcut
            case Selected_Prop: case "Value": return isSelected();
            case Shortcut_Prop: return getShortcut();

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

            // Selected, Shortcut
            case Selected_Prop: case "Value": setSelected(Convert.boolValue(aValue));
            case Shortcut_Prop: setShortcut(Convert.stringValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Shortcut
        if (!isPropDefault(Shortcut_Prop))
            e.add(Shortcut_Prop, getShortcut());

        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Shortcut
        if (anElement.hasAttribute(Shortcut_Prop)) // Archival legacy: Key
            setShortcut(anElement.getAttributeValue(Shortcut_Prop));
        else if (anElement.hasAttribute("Key"))
            setShortcut(anElement.getAttributeValue("Key"));
    }
}