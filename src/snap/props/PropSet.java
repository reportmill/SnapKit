/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ClassUtils;
import snap.util.ListUtils;
import java.util.*;

/**
 * This class holds a list of props for a PropObject class.
 */
public class PropSet {

    // The class that this prop set is for
    private Class<? extends PropObject> _propObjectClass;

    // An array of all known properties for class
    private List<Prop> _props = new ArrayList<>(0);

    // A map of props by name
    private Map<String,Prop> _propsMap = new HashMap<>();

    // A default instance of PropObject class
    private PropObject _defaultInstance;

    // A cached array of archival props (Prop.SkipArchival = false)
    private List<Prop> _archivalProps;

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
    public List<Prop> getProps()  { return _props; }

    /**
     * Adds a Prop.
     */
    public void addProp(Prop aProp)
    {
        _props.add(aProp);
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
        _props.remove(anIndex);
        clearCaches();
    }

    /**
     * Removes prop at given index.
     */
    public void removeProp(Prop aProp)
    {
        int index = _props.indexOf(aProp);
        if (index >= 0)
            removeProp(index);
    }

    /**
     * Returns a prop for given name.
     */
    public Prop getPropForName(String aName)  { return _propsMap.get(aName); }

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
     * Returns array of archival props (Prop.SkipArchival is false).
     */
    public List<Prop> getArchivalProps()
    {
        if (_archivalProps != null) return _archivalProps;
        return _archivalProps = getProps().stream().filter(prop -> !prop.isSkipArchival()).toList();
    }

    /**
     * Clears any caches.
     */
    protected void clearCaches()
    {
        _archivalProps = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String name = getClass().getSimpleName();
        String propNames = ListUtils.joinStrings(getProps(), ", ");
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
