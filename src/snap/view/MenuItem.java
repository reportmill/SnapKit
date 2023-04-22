/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.*;

/**
 * A ButtonBaseNode for MenuItem.
 */
public class MenuItem extends ButtonBase implements Cloneable {

    // The accelerator string
    private String  _key;
    
    // The accelerator key combo
    private KeyCombo  _kcombo;
    
    // Whether item is selected
    private boolean  _selected;
    
    // The parent Menu (if there is one)
    protected Menu  _parentMenu;
    
    // Constants for properties
    public static final String Selected_Prop = "Selected";
    public static final String Shortcut_Prop = "Shortcut";

    // Custom property defaults
    private static boolean DEFAULT_MENU_ITEM_SHOW_AREA = false;
    private static Insets  DEFAULT_MENU_ITEM_PADDING = new Insets(4, 8, 4, 6);

    /**
     * Constructor.
     */
    public MenuItem()
    {
        super();
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
    public String getShortcut()  { return _key; }

    /**
     * Sets the key string.
     */
    public void setShortcut(String aValue)
    {
        firePropChange(Shortcut_Prop, _key, _key = aValue);

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
        return _kcombo != null || _key == null ? _kcombo : (_kcombo = KeyCombo.get(_key));
    }

    /**
     * Returns the shortcut key as string.
     */
    public String getShortcutText()
    {
        KeyCombo kcombo = getShortcutCombo();
        String key = Character.toString((char)kcombo.getKeyCode());
        String str = (SnapUtils.isMac ? Character.toString((char)8984) : "^") + key;
        if (kcombo.isShiftDown())
            str = (char) 8679 + str;
        return str;
    }

    /**
     * Returns whether menu item is a separator.
     */
    public boolean isSeparator()
    {
        String txt = getText(); View gfc = getGraphic();
        return (txt==null || txt.length()==0) && gfc==null;
    }

    /**
     * Returns the parent Menu (if there is one).
     */
    public Menu getParentMenu()  { return _parentMenu; }

    /**
     * Returns the default alignment for button.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

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

        // Update Fill/TextFill to show Targetted
        setFill(aValue ? ViewUtils.getTargetFill() : null);
        getLabel().setTextFill(aValue ? ViewUtils.getTargetTextFill() : Color.BLACK);

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
     * Override because TeaVM hates reflection.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals("Value") || aPropName.equals(Selected_Prop))
            return isSelected();
        return super.getPropValue(aPropName);
    }

    /**
     * Override because TeaVM hates reflection.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals("Value") || aPropName.equals(Selected_Prop))
            setSelected(Convert.boolValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Override for custom defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // ShowArea
            case ShowArea_Prop: return DEFAULT_MENU_ITEM_SHOW_AREA;

            // Padding
            case Padding_Prop: return DEFAULT_MENU_ITEM_PADDING;

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Copies a menu item.
     */
    public MenuItem clone()
    {
        MenuItem clone;
        if (this instanceof Menu) { Menu menu = (Menu) this;
            clone = new Menu();
            for (int i=0, iMax=menu.getItemCount(); i<iMax; i++) { MenuItem item = menu.getItem(i);
                MenuItem iclone = item.clone(); ((Menu)clone).addItem(iclone);
            }
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
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Accelerator
        if (getShortcut() != null && getShortcut().length() > 0)
            e.add("key", getShortcut());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Accelerator
        String key = anElement.getAttributeValue("key"); if (key != null) setShortcut(key);
    }
}