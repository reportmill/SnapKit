/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.util.List;

/**
 * This class represents a property.
 */
public class Prop {

    // The property name
    private String  _name;

    // The property class
    private Class  _propClass;

    // Whether property is relation
    private boolean  _relation;

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
    public void setPropClass(Class aClass)
    {
        _propClass = aClass;

        _relation = PropObject.class.isAssignableFrom(aClass) ||
                List.class.isAssignableFrom(aClass) ||
                aClass.isArray() && PropObject.class.isAssignableFrom(aClass.getComponentType());
    }

    /**
     * Returns whether prop is relation (not primitive).
     */
    public boolean isRelation()  { return _relation; }

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
}
