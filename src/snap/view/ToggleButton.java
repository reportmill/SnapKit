/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.props.PropSet;
import snap.util.*;

import java.util.Objects;

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
    public static final String GroupName_Prop = "GroupName";
    
    /**
     * Constructor.
     */
    public ToggleButton()  { }

    /**
     * Constructor for given label text.
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
        resetStyleState();
    }

    /**
     * Returns the button group name.
     */
    public ToggleGroup getToggleGroup()
    {
        ViewController ownr = getController(); if (ownr == null) return null;
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
        if (Objects.equals(aName, _groupName)) return;
        firePropChange(GroupName_Prop, _groupName, _groupName = aName);
    }

    /**
     * Override to toggle Selected state (if no ToggleGroup or not selected).
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        // Toggle Selected property (unless ToggleGroup is set and doesn't allow it)
        if (getGroupName() == null || getToggleGroup().isAllowEmpty() || !isSelected())
            setSelected(!isSelected());

        // Do normal version
        super.fireActionEvent(anEvent);
    }

    /**
     * Returns a mapped property name.
     */
    protected String getValuePropName()  { return Selected_Prop; }

    /**
     * Override to add to ToggleGroup if name is set.
     */
    public void setController(ViewController viewController)
    {
        // Do normal version
        super.setController(viewController);

        // If GroupName provided, add this button to ToggleGroup
        String groupName = getGroupName();
        if (groupName != null) {
            ToggleGroup toggleGroup = viewController.getToggleGroup(groupName);
            toggleGroup.add(this);
        }
    }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Selected, Group
        aPropSet.addPropNamed(Selected_Prop, boolean.class);
        aPropSet.addPropNamed(GroupName_Prop, String.class);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // Selected, Group
            case Selected_Prop, "Value" -> isSelected();
            case GroupName_Prop -> getGroupName();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Sets the value for given prop name.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Selected, Group
            case Selected_Prop, "Value" -> setSelected(Convert.boolValue(aValue));
            case GroupName_Prop -> setGroupName(Convert.stringValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }
}