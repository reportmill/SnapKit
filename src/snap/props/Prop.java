/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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

    // The getter
    private Callable<?>  _getter;

    // The setter
    private Consumer<?>  _setter;

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
     * Returns the getter.
     */
    public Callable<?> getGetter()  { return _getter; }

    /**
     * Sets the getter.
     */
    public void setGetter(Callable<?> aValue)
    {
        _getter = aValue;
    }

    /**
     * Returns the setter.
     */
    public Consumer<?> getSetter()  { return _setter; }

    /**
     * Sets the setter.
     */
    public void setSetter(Consumer<?> aValue)
    {
        _setter = aValue;
    }
}
