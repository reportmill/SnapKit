/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A base class with prop change support.
 */
public class SnapObject implements PropChangeListener {
    
    // The PropChangeSupport
    PropChangeSupport    _pcs = PropChangeSupport.EMPTY;

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
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    firePropChange(new PropChange(this, aProp, oldVal, newVal));
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    if(!_pcs.hasListener(aProp)) return;
    firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
}

/**
 * Fires a given property change.
 */
protected void firePropChange(PropChange aPCE)  {  _pcs.firePropChange(aPCE); }

/**
 * Returns whether has PropChangeListener.
 */
public boolean hasPropChangeListener()  { return _pcs.hasListener(null); }

/**
 * Returns whether has DeepChangeListener.
 */
public boolean hasDeepChangeListener()  { return _pcs.hasDeepListener(); }

/**
 * Property change listener implementation to forward changes on to deep listeners.
 */
public void propertyChange(PropChange aPCE)  { _pcs.fireDeepChange(this, aPCE); }

/**
 * Deep property change listener implementation to forward to this View's deep listeners.
 */
public void deepChange(Object aLsnr, PropChange aPCE)  { _pcs.fireDeepChange(aLsnr, aPCE); }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    SnapObject clone = null; try { clone = (SnapObject)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    clone._pcs = PropChangeSupport.EMPTY;  // Clear listeners and return clone
    return clone;
}
  
/**
 * Returns a string representation.
 */
public String toString()  { return getClass().getSimpleName()+"@"+Integer.toHexString(System.identityHashCode(this)); }

}