/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.*;

/**
 * A ButtonBase subclass for ToggleButton.
 */
public class ToggleButton extends ButtonBase {

    // Whether button is selected
    private boolean  _selected;

    // The toggle group name
    private String  _groupName;
    
    // Constants for properties
    public static final String Selected_Prop = "Selected";
    public static final String Group_Prop = "Group";
    
    /**
     * Creates a new ToggleButton.
     */
    public ToggleButton()  { }

    /**
     * Creates a new ToggleButton with given text.
     */
    public ToggleButton(String aStr)
    {
        setText(aStr);
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
        if (aValue == isSelected()) return;
        firePropChange(Selected_Prop, _selected, _selected = aValue);
        repaint();
    }

    /**
     * Returns the button group name.
     */
    public ToggleGroup getToggleGroup()
    {
        ViewOwner ownr = getOwner(); if (ownr == null) return null;
        return _groupName!=null? ownr.getToggleGroup(_groupName) : null;
    }

    /**
     * Returns the button group name.
     */
    public String getGroupName()  { return _groupName; }

    /**
     * Sets the button group name.
     */
    public void setGroupName(String aName)
    {
        if (SnapUtils.equals(aName,_groupName)) return;
        firePropChange(Group_Prop, _groupName, _groupName = aName);
    }

    /**
     * Override to toggle Selected state (if no ToggleGroup or not selected).
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        // Toggle Selected property (unless ToggleGroup is set and doesn't allow it)
        if (getGroupName() == null || getToggleGroup().isAllowEmpty() || !isSelected())
            setSelected(!isSelected());

        // Do normal version
        super.fireActionEvent(anEvent);
    }

    /**
     * Override because TeaVM hates reflection.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals("Value") || aPropName == Selected_Prop) return isSelected();
        return super.getPropValue(aPropName);
    }

    /**
     * Override because TeaVM hates reflection.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals("Value") || aPropName == Selected_Prop)
            setSelected(SnapUtils.boolValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Returns a mapped property name name.
     */
    protected String getValuePropName()  { return Selected_Prop; }

    /**
     * Override to add to ToggleGroup if name is set.
     */
    public void setOwner(ViewOwner anOwner)
    {
        // Do normal version
        super.setOwner(anOwner);

        // If GroupName provided, add this button to ToggleGroup
        String groupName = getGroupName();
        if (groupName != null) {
            ToggleGroup toggleGroup = anOwner.getToggleGroup(groupName);
            toggleGroup.add(this);
        }
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Selected
        if (isSelected())
            e.add(Selected_Prop, true);

        // Archive ToggleGroup
        if (getGroupName() != null)
            e.add(Group_Prop, getGroupName());
        return e;
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Selected
        setSelected(anElement.getAttributeBoolValue(Selected_Prop));

        // Unarchive ToggleGroup
        if (anElement.hasAttribute(Group_Prop) || anElement.hasAttribute("ToggleGroup"))
            setGroupName(anElement.getAttributeValue(Group_Prop, anElement.getAttributeValue("ToggleGroup")));
    }
}