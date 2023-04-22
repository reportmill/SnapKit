/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.Convert;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A base class for anything that wants to work with props.
 */
public abstract class PropObject implements PropChange.DoChange {

    // The PropSet to hold prop info
    private PropSet _propSet;

    // PropertyChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // A constant for empty instance
    public static final Object EMPTY_OBJECT = new Object();

    /**
     * Constructor.
     */
    public PropObject()
    {
        super();
    }

    /**
     * Returns the PropSet.
     */
    public PropSet getPropSet()
    {
        // If already set, just return
        if (_propSet != null) return _propSet;

        // Create, set and return
        PropSet propSet = PropSet.getPropSetForPropObject(this);
        return _propSet = propSet;
    }

    /**
     * Returns the prop for given name.
     */
    public Prop getPropForName(String aPropName)
    {
        // Look for prop in standard prop set
        PropSet propSet = getPropSet();
        Prop prop = propSet.getPropForName(aPropName);
        if (prop != null)
            return prop;

        // Look for prop in extra prop set
        Prop[] propsExtra = getPropsForArchivalExtra();
        if (propsExtra != null)
        for (Prop prp : propsExtra)
            if (prp.getName().equals(aPropName))
                return prp;

        // Complain since it's unexpected to ask for a non-existant prop
        System.err.println("PropObject.getPropForName: Prop not found for: " + getClass() + ": " + aPropName);
        return null;
    }

    /**
     * Returns the props for archival.
     */
    public Prop[] getPropsForArchival()
    {
        PropSet propSet = getPropSet();
        Prop[] props = propSet.getArchivalProps();
        return props;
    }

    /**
     * Returns extra props for archival.
     */
    public Prop[] getPropsForArchivalExtra()  { return null; }

    /**
     * Returns the parent PropObject (if available).
     */
    public PropObject getPropParent()  { return null; }

    /**
     * Initialize Props. Override to support props for this class.
     */
    protected void initProps(PropSet aPropSet)
    { /*
        // Do normal version
        super.initProps(aPropSet);

        // Add props for this class
        aPropSet.addPropNamed(Something_Prop, double.class, DEFAULT_SOMETHING_VALUE);
    */
    }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String aPropName)
    { /*
        switch (aPropName) {

            // Something
            case Something_Prop: return getSomething();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    */

        return null;
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String aPropName, Object aValue)
    { /*
        switch (aPropName) {

            // Something
            case Something_Prop: setSomething(aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    */
    }

    /**
     * Returns whether give prop is set to default.
     */
    public boolean isPropDefault(String aPropName)
    {
        // Get Prop value and default - just return true if equal
        Object propValue = getPropValue(aPropName);
        Object propDefault = getPropDefault(aPropName);
        if (Objects.equals(propValue, propDefault))
            return true;

        // If default not EMPTY_OBJECT, return false
        if (propValue == null || propDefault != EMPTY_OBJECT)
            return false;

        // Special handling for EMPTY_OBJECT
        if (propValue.getClass().isArray()) {
            if (Array.getLength(propValue) == 0)
                return true;
        }
        if (propValue instanceof List) {
            if (((List<?>) propValue).size() == 0)
                return true;
        }

        // Return not equal
        return false;
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropDefault(String aPropName)
    {
        // Get prop and return DefaultValue
        Prop prop = getPropForName(aPropName);
        if (prop != null)
            return prop.getDefaultValue();

        // Complain and return null
        System.err.println("PropObject.getPropDefault: No default found for: " + aPropName);
        return null;
    }

    /**
     * Returns prop default as int.
     */
    public final boolean getPropDefaultBool(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return Convert.boolValue(val);
    }

    /**
     * Returns prop default as int.
     */
    public final int getPropDefaultInt(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return Convert.intValue(val);
    }

    /**
     * Returns prop default as double.
     */
    public final double getPropDefaultDouble(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return Convert.doubleValue(val);
    }

    /**
     * Returns prop default as String.
     */
    public final String getPropDefaultString(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return Convert.stringValue(val);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        if (_pcs == PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL, String ... theProps)
    {
        if (_pcs == PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        for (String prop : theProps)
            _pcs.addPropChangeListener(aPCL, prop);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)
    {
        _pcs.removePropChangeListener(aPCL);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL, String ... theProps)
    {
        for (String prop : theProps)
            _pcs.removePropChangeListener(aPCL, prop);
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected final void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (_pcs == PropChangeSupport.EMPTY) return; // if (!_pcs.hasListener(aProp)) return;
        PropChange propChange = new PropChange(this, aProp, oldVal, newVal);
        firePropChange(propChange);
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected final void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if (_pcs == PropChangeSupport.EMPTY) return; // if (!_pcs.hasListener(aProp)) return;
        PropChange propChange = new PropChange(this, aProp, oldVal, newVal, anIndex);
        firePropChange(propChange);
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPC)
    {
        _pcs.firePropChange(aPC);
    }

    /**
     * Add DeepChange listener.
     */
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addDeepChangeListener(aDCL);
    }

    /**
     * Remove DeepChange listener.
     */
    public void removeDeepChangeListener(DeepChangeListener aPCL)
    {
        _pcs.removeDeepChangeListener(aPCL);
    }

    /**
     * PropChange.DoChange method.
     */
    public void processPropChange(PropChange aPC, Object oldVal, Object newVal)
    {
        setPropValue(aPC.getPropName(), newVal);
    }

    /**
     * Standard clone implementation.
     */
    @Override
    protected PropObject clone() throws CloneNotSupportedException
    {
        PropObject clone = (PropObject) super.clone();
        clone._pcs = PropChangeSupport.EMPTY;
        return clone;
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
        // Get props
        Prop[] props = getPropsForArchival();
        StringBuilder sb = new StringBuilder();

        // Iterate over props and add to string
        for (Prop prop : props) {

            // Skip relations and arrays
            if (prop.isRelation()) continue;
            if (prop.isArray()) continue;
            if (isPropDefault(prop.getName())) continue;;

            // If not default value, add to string
            String propName = prop.getName();
            Object propValue = getPropValue(propName);
            String stringValue = StringCodec.SHARED.codeString(propValue);
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(propName).append('=').append(stringValue);
        }

        // Return
        return sb.toString();
    }
}
