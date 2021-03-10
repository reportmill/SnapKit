/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A class to easily add propery change support to a class (and DeepChange support).
 */
public class PropChangeSupport {
    
    // The source
    private Object  _src;
    
    // The PropChangeListener
    private PropChangeListener  _pcl;
    
    // The named prop (optional)
    private String  _pclProp;
    
    // The DeepChangeListener
    private DeepChangeListener  _dcl;
    
    // Nested PropChangeSupport
    private PropChangeSupport  _pcs;
    
    // An empty PropChangeSupport
    public static final PropChangeSupport EMPTY = new PropChangeSupport("");
    
    /**
     * Constructor.
     */
    public PropChangeSupport(Object aSrc)  { _src = aSrc; }

    /**
     * Adds a PropChangeListener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        // Do check for given listener
        if (hasListener(aLsnr,null)) {
            System.err.println("PropChangeSupport.add: Adding duplicate listener");
            return;
        }

        // If none, just set, otherwise forward to nested PCS
        if (_pcl == null)
            _pcl = aLsnr;
        else {
            if (_pcs == null)
                _pcs = new PropChangeSupport(_src);
            _pcs.addPropChangeListener(aLsnr);
        }
    }

    /**
     * Removes a PropChangeListener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcl == aLsnr && _pclProp == null)
            _pcl = null;
        if (_pcs != null)
            _pcs.removePropChangeListener(aLsnr);
    }

    /**
     * Adds a PropChangeListener for given prop.
     */
    public void addPropChangeListener(PropChangeListener aLsnr, String aProp)
    {
        // Do check for given listener
        if (hasListener(aLsnr,aProp)) {
            System.err.println("PropChangeSupport.add: Adding duplicate listener");
            return;
        }

        // If none, just set, otherwise forward to nested PCS
        if (_pcl == null) {
            _pcl = aLsnr;
            _pclProp = aProp;
        }
        else {
            if (_pcs == null)
                _pcs = new PropChangeSupport(_src);
            _pcs.addPropChangeListener(aLsnr, aProp);
        }
    }

    /**
     * Removes a PropChangeListener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr, String aProp)
    {
        if (_pcl == aLsnr && aProp.equals(_pclProp)) {
            _pcl = null;
            _pclProp = null;
        }
        if (_pcs != null)
            _pcs.removePropChangeListener(aLsnr, aProp);
    }

    /**
     * Returns whether there is a listener for given prop.
     */
    public boolean hasListener(String aProp)
    {
        if (_pcl != null && (_pclProp == null || aProp == null || _pclProp.equals(aProp)))
            return true;
        if (_pcs != null)
            return _pcs.hasListener(aProp);
        return false;
    }

    /**
     * Returns whether listener already included.
     */
    private boolean hasListener(PropChangeListener aLsnr, String aProp)
    {
        if (_pcl == aLsnr && SnapUtils.equals(_pclProp,aProp))
            return true;
        if (_pcs != null)
            return _pcs.hasListener(aLsnr, aProp);
        return false;
    }

    /**
     * Fires a property change.
     */
    public void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (hasListener(aProp))
            firePropChange(new PropChange(_src, aProp, oldVal, newVal));
    }

    /**
     * Fires an indexed property change.
     */
    public void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if (hasListener(aProp))
            firePropChange(new PropChange(_src, aProp, oldVal, newVal, anIndex));
    }

    /**
     * Sends the property change.
     */
    public void firePropChange(PropChange aPCE)
    {
        if (_pcl != null && (_pclProp == null || _pclProp.equals(aPCE.getPropName()))) {
            _pcl.propertyChange(aPCE);
            if (_pcl instanceof PropChangeListener.OneShot)
                _pcl = null;
        }
        if (_pcs != null)
            _pcs.firePropChange(aPCE);
    }

    /**
     * Returns whether there is a deep listener.
     */
    public boolean hasDeepListener()
    {
        return _dcl!=null || (_pcs!=null && _pcs.hasDeepListener());
    }

    /**
     * Adds a DeepChangeListener.
     */
    public void addDeepChangeListener(DeepChangeListener aLsnr)
    {
        if (_dcl == null)
            _dcl = aLsnr;
        else {
            if (_pcs == null)
                _pcs = new PropChangeSupport(_src);
            _pcs.addDeepChangeListener(aLsnr);
        }
    }

    /**
     * Removes a DeepChangeListener.
     */
    public void removeDeepChangeListener(DeepChangeListener aLsnr)
    {
        if (_dcl == aLsnr)
            _dcl = null;
        if (_pcs != null)
            _pcs.removeDeepChangeListener(aLsnr);
    }

    /**
     * Sends the deep change.
     */
    public void fireDeepChange(Object aSrc, PropChange aPCE)
    {
        if (_dcl != null)
            _dcl.deepChange(aSrc, aPCE);
        if (_pcs != null)
            _pcs.fireDeepChange(aSrc, aPCE);
    }
}