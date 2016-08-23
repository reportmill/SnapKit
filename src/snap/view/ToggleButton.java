package snap.view;
import snap.util.*;

/**
 * A ButtonBase subclass for ToggleButton.
 */
public class ToggleButton extends ButtonBase {

    // Whether button is selected
    boolean              _selected;

    // The toggle group name
    String               _groupName;
    
    // Constants for properties
    public static final String Selected_Prop = "Selected";
    public static final String ToggleGroupName_Prop = "ToggleGroupName";
    
/**
 * Creates a new ToggleButton.
 */
public ToggleButton()  { }

/**
 * Creates a new ToggleButton with given text.
 */
public ToggleButton(String aStr)  { setText(aStr); }

/**
 * Returns whether button is selected.
 */
public boolean isSelected()  { return _selected; }

/**
 * Sets whether button is selected.
 */
public void setSelected(boolean aValue)
{
    if(aValue==isSelected()) return;
    firePropChange(Selected_Prop, _selected, _selected=aValue);
    repaint();
}

/**
 * Returns the button group name.
 */
public String getToggleGroupName()  { return _groupName; }

/**
 * Sets the button group name.
 */
public void setToggleGroupName(String aName)
{
    if(SnapUtils.equals(aName,_groupName)) return;
    firePropChange(ToggleGroupName_Prop, _groupName, _groupName=aName);
}

/**
 * Returns whether button is pressed (visibly), regardless of state.
 */
public boolean isPressed()  { return super.isPressed() || _selected; }

/**
 * Override to toggle Selected state (if no ToggleGroup or not selected).
 */
public void fire()
{
    if(getToggleGroupName()==null || !isSelected())
        setSelected(!isSelected());
    fireActionEvent();
}

/**
 * Returns a mapped property name name.
 */
protected String getValuePropName()  { return "Selected"; }

/**
 * Override to add to ToggleGroup if name is set.
 */
public void initUI(ViewOwner anOwner)
{
    super.initUI(anOwner);
    if(getToggleGroupName()!=null)
        anOwner.getToggleGroup(getToggleGroupName()).add(this);
}
    
/**
 * XML archival.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver); e.setName("jtogglebutton");
    if(this instanceof CheckBox) e.setName("jcheckbox");
    else if(this instanceof RadioButton) e.setName("jradiobutton");
    
    // Archive selected state
    if(isSelected()) e.add("selected", true);
    
    // Archive ButtonGroupName
    if(getToggleGroupName()!=null) e.add("bgroup", getToggleGroupName());
    return e;
}
    
/**
 * XML unarchival.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive selected state
    setSelected(anElement.getAttributeBoolValue("selected"));
    
    // Unarchive ButtonGroupName
    if(anElement.hasAttribute("bgroup"))
        setToggleGroupName(anElement.getAttributeValue("bgroup"));
}

}