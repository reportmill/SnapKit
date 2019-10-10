/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A base class with prop change support.
 */
public class SnapObject {
    
    // The PropChangeSupport
    protected PropChangeSupport    _pcs = PropChangeSupport.EMPTY;

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aLsnr)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aLsnr);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aLsnr)  { _pcs.removePropChangeListener(aLsnr); }

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    PropChange pc = new PropChange(this, aProp, oldVal, newVal);
    firePropChange(pc);
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    if(!_pcs.hasListener(aProp)) return;
    PropChange pc = new PropChange(this, aProp, oldVal, newVal, anIndex);
    firePropChange(pc);
}

/**
 * Fires a given property change.
 */
protected void firePropChange(PropChange aPC)
{
    _pcs.firePropChange(aPC);
}

/**
 * Returns whether has PropChangeListener.
 */
public boolean hasPropChangeListener()  { return _pcs.hasListener(null); }

/**
 * Adds a deep change listener to shape to listen for shape changes and property changes received by shape.
 */
public void addDeepChangeListener(DeepChangeListener aLsnr)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addDeepChangeListener(aLsnr);
}

/**
 * Removes a deep change listener from shape.
 */
public void removeDeepChangeListener(DeepChangeListener aLsnr)  { _pcs.removeDeepChangeListener(aLsnr); }

/**
 * Returns whether has DeepChangeListener.
 */
public boolean hasDeepChangeListener()  { return _pcs.hasDeepListener(); }

/** Property change listener implementation to forward changes on to deep listeners. */
//protected void propertyChange(PropChange aPC)  { _pcs.fireDeepChange(this, aPC); }

/** Deep property change listener implementation to forward to this View's deep listeners. */
//protected void deepChange(Object aLsnr, PropChange aPC)  { _pcs.fireDeepChange(aLsnr, aPC); }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    // Do normal clone
    SnapObject clone = null; try { clone = (SnapObject)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    
    // Clear PropChangeSupport and return
    clone._pcs = PropChangeSupport.EMPTY;
    return clone;
}
  
/**
 * Returns a string representation.
 */
public String toString()  { return getClass().getSimpleName()+"@"+Integer.toHexString(System.identityHashCode(this)); }

}