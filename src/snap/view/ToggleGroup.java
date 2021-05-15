/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.*;

/**
 * A class to manage a single selection for a group of objects that can each be selected.
 */
public class ToggleGroup {
    
    // The name
    private String  _name;

    // Whether ToggleGroup allows empty
    private boolean  _allowEmpty;
    
    // The buttons
    private List<ToggleButton>  _toggles = new ArrayList<>();
    
    // The selected node
    private ToggleButton  _sel;
    
    // A Listener to watch for button Selection change
    private PropChangeListener  _btnLsnr = pc -> buttonSelectionDidChange(pc);

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)  { _name = aName; }

    /**
     * Returns whether group can be empty.
     */
    public boolean isAllowEmpty()  { return _allowEmpty; }

    /**
     * Sets whether group can be empty.
     */
    public void setAllowEmpty(boolean aValue)
    {
        _allowEmpty = aValue;
    }

    /**
     * Returns the selected toggle.
     */
    public ToggleButton getSelected()  { return _sel; }

    /**
     * Sets the selected toggle.
     */
    public void setSelected(ToggleButton aToggle)
    {
        // If already set, just return
        if (aToggle == _sel) return;

        // Clear old selected button
        if (_sel != null)
            _sel.setSelected(false);

        // Set new button
        _sel = aToggle;

        // Select new button
        if (_sel != null)
            _sel.setSelected(true);
    }

    /**
     * Returns the buttons.
     */
    public List<ToggleButton> getButtons()  { return _toggles; }

    /**
     * Add adds a toggle.
     */
    public void add(ToggleButton aToggle)
    {
        _toggles.add(aToggle);
        aToggle.addPropChangeListener(_btnLsnr, ToggleButton.Selected_Prop);
        if (aToggle.isSelected())
            setSelected(aToggle);
    }

    /**
     * Removes a toggle.
     */
    public void remove(ToggleButton aToggle)
    {
        _toggles.remove(aToggle);
        aToggle.removePropChangeListener(_btnLsnr, ToggleButton.Selected_Prop);
        if (aToggle == _sel)
            setSelected(null);
    }

    /**
     * PropChangeListener method.
     */
    protected void buttonSelectionDidChange(PropChange anEvent)
    {
        // If button turned on, make it selected
        ToggleButton toggleButton = (ToggleButton) anEvent.getSource();
        boolean isSelected = toggleButton.isSelected();
        if (isSelected)
            setSelected(toggleButton);

        // If button turned off and group allows empty, clear selection
        else if (toggleButton == getSelected() && isAllowEmpty())
            setSelected(null);
    }
}