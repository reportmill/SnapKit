/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
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

    // A map of all known properties for class as a list
    private List<String>  _propsList = new ArrayList<>();

    // An array of all known properties for class as array
    private String[]  _props;

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
     * Returns all known properties.
     */
    public String[] getPropNames()
    {
        if (_props != null) return _props;
        return _props = _propsList.toArray(new String[0]);
    }

    /**
     * Adds properties.
     */
    public void addProps(String ... theProps)
    {
        Collections.addAll(_propsList, theProps);
        _props = null;
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
