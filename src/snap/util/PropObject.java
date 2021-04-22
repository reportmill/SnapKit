package snap.util;

/**
 * A base class for anything that wants to work with props.
 */
public class PropObject {

    // PropertyChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        return null;
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)  { }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL, String ... theProps)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        for (String prop : theProps)
            _pcs.addPropChangeListener(aPCL, prop);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

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
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
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
    public void removeDeepChangeListener(DeepChangeListener aPCL)  { _pcs.removeDeepChangeListener(aPCL); }

}
