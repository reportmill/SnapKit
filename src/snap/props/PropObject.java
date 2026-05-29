/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A base class for anything that wants to work with props.
 */
public abstract class PropObject implements PropChange.DoChange {

    // The PropSet to hold prop info
    private PropSet _propSet;

    // PropertyChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Head of registered batch prop changes
    private PropChange _batchPropChange;

    // A constant for empty instance
    public static final Object EMPTY_OBJECT = new Object();

    // A placeholder for batch prop changes
    private static final PropChange BATCH_PROP_CHANGE_PLACEHOLDER = new PropChange(null, null, null, null);

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
    public Prop getPropForName(String aPropName)  { return getPropSet().getPropForName(aPropName); }

    /**
     * Initialize Props. Override to support props for this class.
     */
    protected void initProps(PropSet propSet)
    {
        /*
        super.initProps(aPropSet);
        aPropSet.addPropNamed(Something_Prop, double.class, DEFAULT_SOMETHING_VALUE);
        */
    }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String propName)
    {
        /* return switch (propName) {
            case Something_Prop -> getSomething();
            default -> return super.getPropValue(aPropName);
        }; */

        return null;
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String propName, Object aValue)
    {
        /* switch (aPropName) {
            case Something_Prop -> setSomething(aValue);
            default -> super.setPropValue(aPropName, aValue);
        } */
    }

    /**
     * Returns whether property for given name is set to default.
     */
    public boolean isPropDefault(String aPropName)
    {
        // Get Prop value and default - just return true if equal
        Object propValue = getPropValue(aPropName);
        Object propDefault = getPropDefault(aPropName);
        if (Objects.deepEquals(propValue, propDefault))
            return true;

        // If EMPTY_OBJECT and null or empty String, return true
        if (propDefault == EMPTY_OBJECT) {
            if (propValue == null || propValue instanceof String && ((String) propValue).isEmpty())
                return true;
        }

        // If propValue or propDefault null, return false
        if (propValue == null || propDefault == null)
            return false;

        // Special handling for EMPTY_OBJECT or empty array
        boolean isEmptyObject = propDefault == EMPTY_OBJECT || propDefault.getClass().isArray() && Array.getLength(propDefault) == 0;
        if (isEmptyObject) {
            if (propValue.getClass().isArray()) {
                if (Array.getLength(propValue) == 0)
                    return true;
            }
            if (propValue instanceof List) {
                if (((List<?>) propValue).isEmpty())
                    return true;
            }
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
    protected void firePropChange(PropChange propChange)
    {
        if (_batchPropChange == null)
            _pcs.firePropChange(propChange);
        else batchPropChange(propChange);
    }

    /**
     * Registers to send all prop changes in batch.
     */
    protected void batchPropChanges()
    {
        if (_batchPropChange == null)
            _batchPropChange = BATCH_PROP_CHANGE_PLACEHOLDER;
    }

    /**
     * Registers a batch prop change.
     */
    protected void batchPropChange(String aProp, Object oldVal, Object newVal)
    {
        if (_pcs == PropChangeSupport.EMPTY) return; // if (!_pcs.hasListener(aProp)) return;
        PropChange propChange = new PropChange(this, aProp, oldVal, newVal);
        batchPropChange(propChange);
    }

    /**
     * Registers a given batch prop change.
     */
    protected void batchPropChange(PropChange propChange)
    {
        // If head pointer available, just set
        if (_batchPropChange == null || _batchPropChange == BATCH_PROP_CHANGE_PLACEHOLDER)
            _batchPropChange = propChange;

        // Otherwise find tail batch prop change and link given prop change
        else {
            PropChange tailPropChange = _batchPropChange;
            while (tailPropChange._nextBatchPropChange != null)
                tailPropChange = tailPropChange._nextBatchPropChange;
            tailPropChange._nextBatchPropChange = propChange;
        }
    }

    /**
     * Fires the batched prop changes.
     */
    protected void fireBatchPropChanges()
    {
        if (_batchPropChange != null) {
            PropChange batchPropChange = _batchPropChange;
            _batchPropChange = null;
            _pcs.fireBatchPropChange(batchPropChange);
        }
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
     * A hook to provide opportunity to modify archived PropMap.
     */
    protected void processArchivedMap(PropMap propMap)  { }

    /**
     * A hook to provide opportunity to modify un archived object.
     */
    protected void processUnarchivedMap(PropMap propMap)  { }

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
    protected String toStringProps()
    {
        return PropUtils.getPropsString(this, ", ", "=");
    }
}
