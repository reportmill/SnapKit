/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.StringUtils;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * This class represents a property.
 */
public class Prop {

    // The property name
    private String  _name;

    // The property class
    private Class<?>  _propClass;

    // The default property class
    private Class<?>  _defaultPropClass;

    // Whether property is array class
    private boolean  _array;

    // Whether property is relation class
    private Boolean  _relation;

    // The property default value
    private Object  _defaultValue;

    // Whether to skip archival
    private boolean  _skipArchival;

    // Whether prop value already exists in parent (should be used in place during unarchival)
    private boolean  _preexisting;

    // Whether this prop can change an object's props (ArchivalPropsExtra)
    private boolean  _propChanger;

    /**
     * Constructor.
     */
    public Prop(String aName)
    {
        _name = aName;
    }

    /**
     * Constructor.
     */
    public Prop(String aName, Class<?> aClass, Object aDefault)
    {
        _name = aName;
        setPropClass(aClass);
        setDefaultValue(aDefault);
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the return class.
     */
    public Class<?> getPropClass()  { return _propClass; }

    /**
     * Sets the return class.
     */
    protected void setPropClass(Class<?> aClass)
    {
        _propClass = aClass;
        _array = isArrayClass(aClass);

        // If PropClass is viable instance, set to DefaultPropClass
        boolean isAbstract = aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers()) || aClass == Object.class;
        if (!isAbstract || PropArchiverHpr.getProxyClassForClass(aClass) != null)
            _defaultPropClass = aClass;
    }

    /**
     * Returns the default property class (if different from prop class).
     */
    public Class<?> getDefaultPropClass()  { return _defaultPropClass; }

    /**
     * Sets the default property class (if different from prop class).
     */
    public void setDefaultPropClass(Class<?> aClass)
    {
        _defaultPropClass = aClass;
    }

    /**
     * Returns whether prop is always instance of PropClass (subclasses not allowed).
     *
     * This undoubtedly needs to be a real property one day (PropClassVaries?).
     */
    public boolean isPropClassConstant()  { return _propClass == _defaultPropClass; }

    /**
     * Returns whether prop is array.
     */
    public boolean isArray()  { return _array; }

    /**
     * Returns whether prop is relation (not primitive).
     */
    public boolean isRelation()
    {
        if (_relation != null) return _relation;

        return _relation = isRelationPropClass(_propClass);
    }

    /**
     * Returns the default value.
     */
    public Object getDefaultValue()  { return _defaultValue; }

    /**
     * Sets the default value.
     */
    public void setDefaultValue(Object aValue)
    {
        _defaultValue = aValue;
    }

    /**
     * Returns whether this prop is skipped for archival.
     */
    public boolean isSkipArchival()  { return _skipArchival; }

    /**
     * Sets whether this prop is skipped for archival.
     */
    public void setSkipArchival(boolean aValue)
    {
        _skipArchival = aValue;
    }

    /**
     * Returns whether prop value already exists in parent (should be used in place during unarchival).
     */
    public boolean isPreexisting()  { return _preexisting; }

    /**
     * Sets whether prop value already exists in parent (should be used in place during unarchival).
     */
    public void setPreexisting(boolean aValue)
    {
        _preexisting = aValue;
    }

    /**
     * Returns whether this prop can change an object's props (ArchivalPropsExtra).
     */
    public boolean isPropChanger()  { return _propChanger; }

    /**
     * Sets whether this prop can change an object's props (ArchivalPropsExtra).
     */
    public void setPropChanger(boolean aValue)
    {
        _propChanger = aValue;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add Name
        StringBuffer sb = new StringBuffer();
        String name = getName();
        StringUtils.appendProp(sb, "Name", name);

        // Add PropClass, Array, Relation, DefaultPropClass
        StringUtils.appendProp(sb, "PropClass", _propClass != null ? _propClass.getSimpleName() : "null");
        if (isArray())
            StringUtils.appendProp(sb, "Array", true);
        if (isRelation())
            StringUtils.appendProp(sb, "Relation", true); _relation = null;
        if (_defaultPropClass != _propClass)
            StringUtils.appendProp(sb, "DefaultPropClass", _defaultPropClass != null ? _defaultPropClass.getSimpleName() : "null");

        // Add Default value
        Object defValue = getDefaultValue();
        String defStr = defValue != null ? defValue.toString() : "null";
        StringUtils.appendProp(sb, "DefaultValue", defStr);

        // Add SkipArchival, Preexisting
        if (isSkipArchival())
            StringUtils.appendProp(sb, "SkipArchival", true);
        if (isPreexisting())
            StringUtils.appendProp(sb, "Preexisting", true);

        // Return string
        return sb.toString();
    }

    /**
     * Returns whether given class is array/list.
     */
    private static boolean isArrayClass(Class<?> aClass)
    {
        // Handle array class
        if (aClass.isArray())
            return true;

        // Handle List class
        if (List.class.isAssignableFrom(aClass))
            return true;

        // Return
        return false;
    }

    /**
     * Returns whether given class is PropObject class or array/list of PropObject.
     */
    private static boolean isRelationPropClass(Class<?> aClass)
    {
        // Handle primitive
        if (aClass.isPrimitive())
            return false;

        // Handle PropObject class/subclass
        if (PropObject.class.isAssignableFrom(aClass))
            return true;

        // Handle array
        if (aClass.isArray()) {
            Class<?> compClass = aClass.getComponentType();
            return isRelationPropClass(compClass);
        }

        // Handle List class
        if (List.class.isAssignableFrom(aClass))
            return true;

        // Additional relation classes
        Class<? extends PropObjectProxy> proxyClass = PropArchiverHpr.getProxyClassForClass(aClass);
        if (proxyClass != null)
            return true;

        // Return
        return false;
    }
}
