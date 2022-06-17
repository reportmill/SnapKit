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

    // An array of all known relation names for class
    private String[]  _relationNames;

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
        _propNames = _relationNames = null;
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
     * Adds property names.
     */
    public void addProps(String ... thePropNames)
    {
        for (String propName : thePropNames)
            addProp(new Prop(propName));
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
     * Returns all known relation names.
     */
    public String[] getRelationNames()
    {
        // If already set, just return
        if (_relationNames != null) return _relationNames;

        // Stream props to relationNames via filter, map
        Stream<Prop> relPropsStream = Arrays.stream(_props).filter(i -> i.isRelation());
        Stream<String> relNamesStream = relPropsStream.map(i -> i.getName());
        String[] relNames = relNamesStream.toArray(size -> new String[size]);

        // Set and return
        return _relationNames = relNames;
    }

    /**
     * Adds relation names.
     */
    public void addRelations(String ... theNames)
    {
        for (String propName : theNames)
            addPropNamed(propName, PropObject.class, null);
    }

    /**
     * Removes relation names.
     */
    public void removeRelationNamed(String aPropName)
    {
        Prop prop = getPropForName(aPropName);
        _props = ArrayUtils.removeId(_props, prop);
        _propNames = _relationNames = null;
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
