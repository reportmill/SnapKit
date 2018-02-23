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
    String                _name;
    
    // The buttons
    List <ToggleButton>   _toggles = new ArrayList();
    
    // The selected node
    ToggleButton          _sel;
    
    // A Listener to watch for button Selection change
    PropChangeListener    _btnLsnr = pc -> buttonSelectionDidChange(pc);

/**
 * Returns the name.
 */
public String getName()  { return _name; }

/**
 * Sets the name.
 */
public void setName(String aName)  { _name = aName; }

/**
 * Returns the selected toggle.
 */
public ToggleButton getSelected()  { return _sel; }

/**
 * Sets the selected toggle.
 */
public void setSelected(ToggleButton aToggle)
{
    if(aToggle==_sel) return;
    if(_sel!=null) _sel.setSelected(false);
    _sel = aToggle;
    if(_sel!=null) _sel.setSelected(true);
}

/**
 * Add adds a toggle.
 */
public void add(ToggleButton aToggle)
{
    _toggles.add(aToggle);
    aToggle.addPropChangeListener(_btnLsnr, ToggleButton.Selected_Prop);
    if(aToggle.isSelected())
        setSelected(aToggle);
}

/**
 * Removes a toggle.
 */
public void remove(ToggleButton aToggle)
{
    _toggles.remove(aToggle);
    aToggle.removePropChangeListener(_btnLsnr, ToggleButton.Selected_Prop);
    if(aToggle==_sel)
        setSelected(null);
}

/**
 * PropChangeListener method.
 */
protected void buttonSelectionDidChange(PropChange anEvent)
{
    if(SnapUtils.boolValue(anEvent.getNewValue()))
        setSelected((ToggleButton)anEvent.getSource());
}

}