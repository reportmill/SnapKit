/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;

import java.util.*;

/**
 * This class holds a set of default values for a PropObject/PropSheet.
 *
 * Defaults can be specified programmatically, grouped by class name:
 *
 *    PropDefaults viewDefs = new PropDefaults("View");
 *    viewDefs.setPropDefault(FillColor_Prop, Color.WHITE);
 *    viewDefs.setPropDefault(Font_Prop, PropDefaults.INHERIT_VALUE);
 *
 *    PropDefaults buttonDefs = viewDefs.getPropDefaultsForClassName("Button");
 *    buttonDefs.setPropDefault(Border_Prop, Border.blackBorder());
 *    buttonDefs.setPropDefault(FillColor_Prop, null);
 *
 */
public class PropDefaults {

    // The class name
    private String  _name;

    // A map of key/values
    private Map<String,Object>  _propVals = new HashMap<>();

    // An array of all known properties for class
    private String[]  _props = new String[0];

    // An array of all known relations for class
    private String[]  _relations = new String[0];

    // A Map of PropDefaults for Classes
    private static final Map<Class,PropDefaults>  _propDefaults = new HashMap<>();

    // A constant to indicate that the prop should be inherited from parent
    public static final Object INHERIT_VALUE = new Object();

    /**
     * Constructor.
     */
    public PropDefaults()
    {

    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the default value for a given property.
     */
    public Object getPropDefault(String aPropName)
    {
        return _propVals.get(aPropName);
    }

    /**
     * Sets the default value for a given property.
     */
    public void setPropDefault(String aPropName, Object aValue)
    {
        _propVals.put(aPropName, aValue);
    }

    /**
     * Returns all known property names.
     */
    public String[] getPropNames()  { return _props; }

    /**
     * Adds property names.
     */
    public void addProps(String ... thePropNames)
    {
        _props = ArrayUtils.addAll(_props, thePropNames);
    }

    /**
     * Returns all known relation names.
     */
    public String[] getRelationNames()  { return _relations; }

    /**
     * Adds relation names.
     */
    public void addRelations(String ... theNames)
    {
        _relations = ArrayUtils.addAll(_relations, theNames);
    }

    /**
     * Removes relation names.
     */
    public void removeRelations(String ... theNames)
    {
        for (String name : theNames)
            _relations = ArrayUtils.remove(_relations, name);
    }

    /**
     * Returns the defaults for given class.
     */
    public static PropDefaults getPropDefaultsForPropObject(PropObject aPropObj)
    {
        // Get PropDefaults for class (if already set, just return)
        Class cls = aPropObj.getClass();
        PropDefaults propDefaults = _propDefaults.get(cls);
        if (propDefaults != null)
            return propDefaults;

        // Create new PropDefaults, init, add to map and return
        propDefaults = new PropDefaults();
        aPropObj.initPropDefaults(propDefaults);
        _propDefaults.put(cls, propDefaults);
        return propDefaults;
    }
}
