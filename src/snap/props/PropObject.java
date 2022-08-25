/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.SnapUtils;
import java.util.*;

/**
 * A base class for anything that wants to work with props.
 */
public class PropObject implements PropChange.DoChange {

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
     * Initialize Props. Override to provide custom defaults.
     */
    protected void initProps(PropSet aPropSet)
    {
        // super.initProps(aPropSet);
        // aPropSet.addPropNamed(Something_Prop, double.class, DEFAULT_SOMETHING_VALUE);
    }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String aPropName)
    {
        // switch (aPropName) {
        //     case Something_Prop: return getSomething();
        //     default: return super.getPropValue(aPropName);
        // }

        return null;
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        // switch (aPropName) {
        //     case Something_Prop: setSomething(aValue); break;
        //     default: super.setPropValue(aPropName, aValue);
        // }
    }

    /**
     * Returns whether give prop is set to default.
     */
    public boolean isPropDefault(String aPropName)
    {
        Object propValue = getPropValue(aPropName);
        Object propDefault = getPropDefault(aPropName);
        return Objects.equals(propValue, propDefault);
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
        return SnapUtils.boolValue(val);
    }

    /**
     * Returns prop default as int.
     */
    public final int getPropDefaultInt(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return SnapUtils.intValue(val);
    }

    /**
     * Returns prop default as double.
     */
    public final double getPropDefaultDouble(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return SnapUtils.doubleValue(val);
    }

    /**
     * Returns prop default as String.
     */
    public final String getPropDefaultString(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return SnapUtils.stringValue(val);
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
    public String toStringProps()  { return ""; }
}
