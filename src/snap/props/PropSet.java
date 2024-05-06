/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;
import snap.util.ClassUtils;
import java.util.*;

/**
 * This class holds a list of props for a PropObject class.
 */
public class PropSet {

    // The class that this prop set is for
    private Class<? extends PropObject> _propObjectClass;

    // An array of all known properties for class
    private Prop[]  _props = new Prop[0];

    // A map of props by name
    private Map<String,Prop>  _propsMap = new HashMap<>();

    // A default instance of PropObject class
    private PropObject _defaultInstance;

    // An array of all known prop names for class
    private String[]  _propNames;

    // A cached array of archival props (Prop.SkipArchival = false)
    private Prop[]  _archivalProps;

    // A Map of PropSets for Classes
    private static final Map<Class<? extends PropObject>, PropSet>  _propSets = new HashMap<>();

    /**
     * Constructor.
     */
    public PropSet(Class<? extends PropObject> propObjectClass)
    {
        super();
        _propObjectClass = propObjectClass;
    }

    /**
     * Returns the PropObject class.
     */
    public Class<? extends PropObject> getPropObjectClass()  { return _propObjectClass; }

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
        _propsMap.put(aProp.getName().toLowerCase(), aProp);

        // Clear caches
        clearCaches();
    }

    /**
     * Removes prop at given index.
     */
    public void removeProp(int anIndex)
    {
        // Remove prop
        _props = ArrayUtils.remove(_props, anIndex);

        // Clear caches
        clearCaches();
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
     * Adds new property for given name and class.
     */
    public Prop addPropNamed(String aPropName, Class<?> aClass)
    {
        PropObject defaultInstance = getDefaultInstance();
        Object defaultValue = defaultInstance.getPropValue(aPropName);
        return addPropNamed(aPropName, aClass, defaultValue);
    }

    /**
     * Adds new property for given name, class and default value.
     */
    public Prop addPropNamed(String aPropName, Class<?> aClass, Object aDefault)
    {
        Prop prop = new Prop(aPropName, aClass, aDefault);
        addProp(prop);
        return prop;
    }

    /**
     * Returns the default object.
     */
    public PropObject getDefaultInstance()
    {
        // If already set, just return
        if (_defaultInstance != null) return _defaultInstance;

        // Get instance
        Class<? extends PropObject> propObjClass = getPropObjectClass();
        PropObject defaultInstance = null;
        while (defaultInstance == null) {
            try { defaultInstance = ClassUtils.newInstance(propObjClass); }
            catch (Throwable t) {
                System.err.println("PropSet.getDefaultInstance: Couldn't create instance of class " + propObjClass.getName());
                propObjClass = (Class<? extends PropObject>) propObjClass.getSuperclass();
            }
        }

        // Set/return
        return _defaultInstance = defaultInstance;
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
        String[] propNames = ArrayUtils.map(props, prop -> prop.getName(), String.class);

        // Set/return
        return _propNames = propNames;
    }

    /**
     * Returns array of archival props (Prop.SkipArchival is false).
     */
    public Prop[] getArchivalProps()
    {
        // If already set, just return
        if (_archivalProps != null) return _archivalProps;

        // Stream props to archivalProps via filter
        Prop[] props = getProps();
        Prop[] archivalProps = ArrayUtils.filter(props, prop -> !prop.isSkipArchival());

        // Set/return
        return _archivalProps = archivalProps;
    }

    /**
     * Clears any caches.
     */
    protected void clearCaches()
    {
        _propNames = null;
        _archivalProps = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String name = getClass().getSimpleName();
        String propNames = Arrays.toString(getPropNames());
        return name + "{ " + propNames + " }";
    }

    /**
     * Returns the PropSet for given class.
     */
    public static PropSet getPropSetForPropObject(PropObject aPropObj)
    {
        // Get PropSet for class (if already set, just return)
        Class<? extends PropObject> propObjClass = aPropObj.getClass();
        PropSet propSet = _propSets.get(propObjClass);
        if (propSet != null)
            return propSet;

        // Create PropSet, init, and add to map
        propSet = new PropSet(propObjClass);
        aPropObj.initProps(propSet);
        _propSets.put(propObjClass, propSet);

        // Return
        return propSet;
    }
}
