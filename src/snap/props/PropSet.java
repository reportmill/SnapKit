/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class holds a list of props for a PropObject class.
 */
public class PropSet {

    // An array of all known properties for class
    private Prop[]  _props = new Prop[0];

    // A map of props by name
    private Map<String,Prop>  _propsMap = new HashMap<>();

    // An array of all known prop names for class
    private String[]  _propNames;

    // A Map of PropSets for Classes
    private static final Map<Class<? extends PropObject>, PropSet>  _propSets = new HashMap<>();

    /**
     * Constructor.
     */
    public PropSet()
    {
    }

    /**
     * Returns the props.
     */
    public Prop[] getProps()  { return _props; }

    /**
     * Adds a Prop.
     */
    public void addProp(Prop aProp)
    {
        int length = _props.length;
        _props = Arrays.copyOf(_props, length + 1);
        _props[length] = aProp;
        _propsMap.put(aProp.getName(), aProp);

        // Clear caches
        _propNames = null;
    }

    /**
     * Removes prop at given index.
     */
    public void removeProp(int anIndex)
    {
        // Remove prop
        _props = ArrayUtils.remove(_props, anIndex);

        // Clear caches
        _propNames = null;
    }

    /**
     * Removes prop at given index.
     */
    public void removeProp(Prop aProp)
    {
        int index = ArrayUtils.indexOfId(_props, aProp);
        if (index >= 0)
            removeProp(index);
    }

    /**
     * Returns a prop for given name.
     */
    public Prop getPropForName(String aName)
    {
        return _propsMap.get(aName);
    }

    /**
     * Adds property names.
     */
    public void addPropNamed(String aPropName, Class<?> aClass, Object aDefault)
    {
        Prop prop = new Prop(aPropName, aClass, aDefault);
        addProp(prop);
    }

    /**
     * Returns all known property names.
     */
    public String[] getPropNames()
    {
        // If already set, just return
        if (_propNames != null) return _propNames;

        // Stream props to propNames via map
        Prop[] props = getProps();
        Stream<String> propNamesStream = Arrays.stream(props).map(i -> i.getName());
        String[] propNames = propNamesStream.toArray(size -> new String[size]);

        // Set/return
        return _propNames = propNames;
    }

    /**
     * Returns the PropSet for given class.
     */
    public static PropSet getPropSetForPropObject(PropObject aPropObj)
    {
        // Get PropSet for class (if already set, just return)
        Class<? extends PropObject> cls = aPropObj.getClass();
        PropSet propSet = _propSets.get(cls);
        if (propSet != null)
            return propSet;

        // Create PropSet, init, and add to map
        propSet = new PropSet();
        aPropObj.initProps(propSet);
        _propSets.put(cls, propSet);

        // Return
        return propSet;
    }
}
