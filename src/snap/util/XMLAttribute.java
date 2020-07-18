/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.text.*;
import java.util.Locale;

/**
 * The <code>RXAttribute</code> class represents an XML attribute by simply having a name and value.
 * It also forms the super class of RXElement
 */
public class XMLAttribute implements Cloneable {
    
    // The name of the attribute
    private String  _name;
    
    // The full name of the attribute (if namespaced)
    private String  _fname;
    
    // The value string of the attribute
    private String  _value;

    // Formatter
    private static DecimalFormat _format = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ENGLISH));

    /**
     * Creates an empty attribute.
     */
    public XMLAttribute() { }

    /**
     * Creates an attribute initialized with the given name and value.
     */
    public XMLAttribute(String aName, String aValue)
    {
        // Set name and value
        setFullName(aName);
        setValue(aValue);

        // Complain if value is null?
        if (aValue==null)
            System.err.println("RXAttribute: Warning: null value for attribute " + _name);
    }

    /**
     * Creates an attribute initialized with the given name and boolean value.
     */
    public XMLAttribute(String aName, boolean aValue)  { this(aName, aValue? "true" : "false"); }

    /**
     * Creates an attribute initialized with the given name and Boolean value.
     */
    public XMLAttribute(String aName, Boolean aValue)  { this(aName, aValue.booleanValue()); }

    /**
     * Creates an attribute initialized with the given name and int value.
     */
    public XMLAttribute(String aName, int aValue)  { this(aName, Integer.toString(aValue)); }

    /**
     * Creates an attribute initialized with the given name and float value.
     */
    public XMLAttribute(String aName, double aValue)
    {
        // Set name
        setFullName(aName);
        setValue(_format.format(aValue));
    }

    /**
     * Returns the name for this attribute.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name for this attribute.
     */
    public void setName(String aName)  { _name = aName; }

    /**
     * Returns the full name for this attribute.
     */
    public String getFullName()  { return _fname!=null? _fname : _name; }

    /**
     * Sets the full name for this attribute.
     */
    public void setFullName(String aName)
    {
        _fname = aName;
        int ind = aName.indexOf(':');
        if (ind>0) aName = aName.substring(ind+1);
        setName(aName);
    }

    /**
     * Returns the prefix if full name is different.
     */
    public String getPrefix()
    {
        if (getFullName().equals(getName())) return null;
        int ind = getFullName().indexOf(':');
        if (ind>0) return getFullName().substring(0, ind);
        return null;
    }

    /**
     * Returns the value for this attribute.
     */
    public String getValue()  { return _value; }

    /**
     * Sets the value for this attribute.
     */
    public void setValue(String aValue)  { _value = aValue; }

    /**
     * Returns the value for this attribute as an int.
     */
    public int getIntValue()  { return SnapUtils.intValue(_value); }

    /**
     * Returns the value for this attribute as a float.
     */
    public float getFloatValue()  { return SnapUtils.floatValue(_value); }

    /**
     * Returns the value for this attribute as a float.
     */
    public double getDoubleValue()  { return SnapUtils.doubleValue(_value); }

    /**
     * Returns value for the attribute as a Number (can be any of Integer, Double, etc).
     */
    public Number getNumberValue()  { return SnapUtils.numberValue(_value); }

    /**
     * Returns a clone of this attribute.
     */
    public XMLAttribute clone()
    {
        try { return (XMLAttribute)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a basic string representation of this attribute.
     */
    public String toString()  { return '"' + getName() + "\" = \"" + getValue() + '"'; }
}