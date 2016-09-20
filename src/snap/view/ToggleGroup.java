/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.*;

/**
 * A class to manage a single selection for a group of objects that can each be selected.
 */
public class ToggleGroup implements PropChangeListener {
    
    // The name
    String                    _name;
    
    // The buttons
    List <ToggleButton>   _toggles = new ArrayList();
    
    // The selected node
    ToggleButton          _sel;

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
    if(_sel!=null) _sel.setSelected(false);
    _sel = aToggle;
}

/**
 * Add adds a toggle.
 */
public void add(ToggleButton aToggle)
{
    _toggles.add(aToggle);
    aToggle.addPropChangeListener(this);
    if(aToggle.isSelected())
        setSelected(aToggle);
}

/**
 * Removes a toggle.
 */
public void remove(ToggleButton aToggle)
{
    _toggles.remove(aToggle);
    aToggle.removePropChangeListener(this);
    if(aToggle==_sel)
        setSelected(null);
}

/**
 * PropChangeListener method.
 */
public void propertyChange(PropChange anEvent)
{
    if(anEvent.getPropertyName().equals("Selected") && SnapUtils.boolValue(anEvent.getNewValue())) {
        if(_sel!=null) _sel.setSelected(false);
        _sel = (ToggleButton)anEvent.getSource();
    }
}

}