package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A ButtonBaseNode for MenuItem.
 */
public class MenuItem extends ButtonBase implements Cloneable {

    // The accelerator string
    String           _key;
    
    // The accelerator key combo
    KeyCombo         _kcombo;
    
    // Whether item is selected
    boolean          _selected;
    
    // The parent Menu (if there is one)
    Menu             _parentMenu;
    
    // Constants for properties
    public static final String Image_Prop = "Image";
    public static final String Selected_Prop = "Selected";

/**
 * Create new menu item node.
 */
public MenuItem()
{
    enableEvents(Action); setShowBorder(false);
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
    if(isSelected()==aValue) return;
    firePropChange(Selected_Prop, _selected, _selected=aValue);
}

/**
 * Returns the key string.
 */
public String getAccelerator()  { return _key; }

/**
 * Sets the key string.
 */
public void setAccelerator(String aValue)
{
    firePropChange("Accelerator", _key, _key = aValue);
    
    // Set graphic
    if(aValue==null) { setGraphicAfter(null); return; }
    String str = getShortcutText();
    StringView text = new StringView(); text.setText(str); text.setLeanX(HPos.RIGHT);
    setGraphicAfter(text); getLabel().setSpacing(12);
    getLabel().setGrowWidth(true);
}

/**
 * Returns the accelerator key combo.
 */
public KeyCombo getShortcutCombo()  { return _kcombo!=null || _key==null? _kcombo : (_kcombo=KeyCombo.get(_key)); }

/**
 * Returns the shortcut key as string.
 */
public String getShortcutText()
{
    KeyCombo kcombo = getShortcutCombo();
    String key = Character.toString((char)kcombo.getKeyCode());
    String str = (SnapUtils.isMac? Character.toString((char)8984) : "^") + key;
    if(kcombo.isShiftDown()) str = Character.toString((char)8679) + str;
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
public Pos getAlignDefault()  { return Pos.CENTER_LEFT; }

/**
 * Returns the padding default.
 */
public Insets getPaddingDefault()  { return _mitemIns; } static Insets _mitemIns = new Insets(4,8,4,6);

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return this instanceof CheckBoxMenuItem? "Selected" : "Text"; }

/**
 * Sets whether button is under mouse.
 */
protected void setTargeted(boolean aValue)
{
    if(aValue==isTargeted()) return;
    super.setTargeted(aValue);
    setFill(aValue? ViewUtils.getTargetFill() : null);
    getLabel().setTextFill(aValue? ViewUtils.getTargetTextFill() : Color.BLACK);
}

/**
 * Override to suppress painting.
 */
public void paintFront(Painter aPntr)
{
    if(isSeparator()) {
        double ly = 1.5, px = 2, pw = getWidth() - px - 2;
        aPntr.setPaint(new Color(1,1,1,.5)); aPntr.drawLine(px,ly-1,px+pw,ly-1); aPntr.drawLine(px,ly+1,px+pw,ly+1);
        aPntr.setPaint(Color.LIGHTGRAY); aPntr.drawLine(px,ly,px+pw,ly);
    }
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return isSeparator()? 3 : super.getPrefHeightImpl(aW); }

/**
 * Copies a menu item.
 */
public MenuItem clone()
{
    MenuItem clone;
    if(this instanceof Menu) { Menu menu = (Menu)this; clone = new Menu();
        for(int i=0, iMax=menu.getItemCount(); i<iMax; i++) { MenuItem item = menu.getItem(i);
            MenuItem iclone = item.clone(); ((Menu)clone).addItem(iclone); }}
    else if(this instanceof CheckBoxMenuItem) clone = new CheckBoxMenuItem();
    else clone = new MenuItem();
    clone.setText(getText()); clone.setImage(getImage()); clone.setName(getName());
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
    e.removeAttribute(ShowBorder_Prop);

    // Archive Accelerator
    if(getAccelerator()!=null && getAccelerator().length()>0) e.add("key", getAccelerator());
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
    String key = anElement.getAttributeValue("key"); if(key!=null) setAccelerator(key);
}

}