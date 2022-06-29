/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;
import snap.util.StringUtils;
import java.util.List;

/**
 * This class represents a property.
 */
public class Prop {

    // The property name
    private String  _name;

    // The property class
    private Class  _propClass;

    // Whether property is array class
    private boolean  _array;

    // Whether property is relation class
    private Boolean  _relation;

    // The property default value
    private Object  _defaultValue;

    // Whether to skip archival
    private boolean  _skipArchival;

    // The getter/setter
    //private Callable<?>  _getter;
    //private Consumer<?>  _setter;

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
    public Prop(String aName, Class aClass, Object aDefault)
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
    public Class getPropClass()  { return _propClass; }

    /**
     * Sets the return class.
     */
    protected void setPropClass(Class<?> aClass)
    {
        _propClass = aClass;

        _array = isArrayClass(aClass);
        //_relation = isRelationPropClass(aClass);
    }

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

        // Add PropClass, Array, Relation
        StringUtils.appendProp(sb, "PropClass", _propClass != null ? _propClass.getSimpleName() : "null");
        StringUtils.appendProp(sb, "Array", isArray());
        StringUtils.appendProp(sb, "Relation", isRelation()); _relation = null;

        // Add Default value
        Object defValue = getDefaultValue();
        String defStr = defValue != null ? defValue.toString() : "null";
        StringUtils.appendProp(sb, "DefaultValue", defStr);

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
        // Handle PropObject class/subclass
        if (PropObject.class.isAssignableFrom(aClass))
            return true;

        // Handle PropObject[] class
        if (aClass.isArray() && PropObject.class.isAssignableFrom(aClass.getComponentType()))
            return true;

        // Handle List class
        if (List.class.isAssignableFrom(aClass))
            return true;

        // Additional relation classes
        for (Class<?> cls : _relationClasses)
            if (cls.isAssignableFrom(aClass))
                return true;

        // Return
        return false;
    }

    // Additional relation classes
    private static Class[]  _relationClasses = new Class[0];

    /**
     * Adds additional relation classes.
     */
    public static void addRelationClass(Class<?> aClass)
    {
        _relationClasses = ArrayUtils.add(_relationClasses, aClass);
    }
}
