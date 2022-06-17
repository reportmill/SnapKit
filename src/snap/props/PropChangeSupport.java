/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;

import java.util.Objects;

/**
 * A class to easily add propery change support to a class (and DeepChange support).
 */
public class PropChangeSupport {
    
    // The source
    private Object  _src;
    
    // The PropChangeListener
    private PropChangeListener[]  _lsnrs = EMPTY_LISTENER_ARRAY;
    
    // The named prop (optional)
    private String[]  _lsnrProps = EMPTY_PROP_ARRAY;
    
    // The DeepChangeListener
    private DeepChangeListener[]  _deepLsnrs = EMPTY_DEEP_ARRAY;
    
    // Constants
    private static final PropChangeListener[] EMPTY_LISTENER_ARRAY = new PropChangeListener[0];
    private static final String[] EMPTY_PROP_ARRAY = new String[0];
    private static final DeepChangeListener[] EMPTY_DEEP_ARRAY = new DeepChangeListener[0];

    // An empty PropChangeSupport
    public static final PropChangeSupport EMPTY = new PropChangeSupport("");

    /**
     * Constructor.
     */
    public PropChangeSupport(Object aSrc)
    {
        _src = aSrc;
    }

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

        // Add to listener and prop array
        _lsnrs = ArrayUtils.add(_lsnrs, aLsnr);
        _lsnrProps = ArrayUtils.add(_lsnrProps, null);
    }

    /**
     * Removes a PropChangeListener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr)
    {
        for (int i = _lsnrs.length - 1; i >= 0; i--) {
            if (aLsnr == _lsnrs[i]) {
                _lsnrs = ArrayUtils.remove(_lsnrs, i);
                _lsnrProps = ArrayUtils.remove(_lsnrProps, i);
            }
        }
    }

    /**
     * Adds a PropChangeListener for given prop.
     */
    public void addPropChangeListener(PropChangeListener aLsnr, String aProp)
    {
        // Do check for given listener
        if (hasListener(aLsnr, aProp)) {
            System.err.println("PropChangeSupport.add: Adding duplicate listener for prop: " + aProp);
            return;
        }

        // Add to listener and prop array
        _lsnrs = ArrayUtils.add(_lsnrs, aLsnr);
        _lsnrProps = ArrayUtils.add(_lsnrProps, aProp);
    }

    /**
     * Removes a PropChangeListener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr, String aProp)
    {
        for (int i = _lsnrs.length - 1; i >= 0; i--) {
            if (Objects.equals(aProp, _lsnrProps[i]) && aLsnr == _lsnrs[i]) {
                _lsnrs = ArrayUtils.remove(_lsnrs, i);
                _lsnrProps = ArrayUtils.remove(_lsnrProps, i);
            }
        }
    }

    /**
     * Returns whether there is a listener for given prop.
     */
    public boolean hasListener(String aProp)
    {
        for (int i = 0; i < _lsnrs.length; i++) {
            if (_lsnrProps[i] == null || aProp == null || Objects.equals(_lsnrProps[i], aProp))
                return true;
        }
        return false;
    }

    /**
     * Returns whether listener already included.
     */
    private boolean hasListener(PropChangeListener aLsnr, String aProp)
    {
        for (int i = 0; i < _lsnrs.length; i++) {
            if (_lsnrs[i] == aLsnr && Objects.equals(_lsnrProps[i], aProp))
                return true;
        }
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
    public void firePropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        boolean hasOneShot = false;
        for (int i = 0; i < _lsnrs.length; i++) {
            PropChangeListener lsnr = _lsnrs[i];
            String prop = _lsnrProps[i];
            if (prop == null || prop.equals(propName)) {
                lsnr.propertyChange(aPC);
                hasOneShot |= lsnr instanceof PropChangeListener.OneShot;
            }
        }

        if (hasOneShot) {
            for (int i = _lsnrs.length - 1; i >= 0; i--) {
                PropChangeListener lsnr = _lsnrs[i];
                if (lsnr instanceof PropChangeListener.OneShot) {
                    String prop = _lsnrProps[i];
                    if (prop == null || prop.equals(propName)) {
                        _lsnrs = ArrayUtils.remove(_lsnrs, i);
                        _lsnrProps = ArrayUtils.remove(_lsnrProps, i);
                    }
                }
            }
        }
    }

    /**
     * Returns whether there is a deep listener.
     */
    public boolean hasDeepListener()
    {
        return _deepLsnrs.length > 0;
    }

    /**
     * Adds a DeepChangeListener.
     */
    public void addDeepChangeListener(DeepChangeListener aLsnr)
    {
        _deepLsnrs = ArrayUtils.add(_deepLsnrs, aLsnr);
    }

    /**
     * Removes a DeepChangeListener.
     */
    public void removeDeepChangeListener(DeepChangeListener aLsnr)
    {
        _deepLsnrs = ArrayUtils.remove(_deepLsnrs, aLsnr);
    }

    /**
     * Sends the deep change.
     */
    public void fireDeepChange(Object aSrc, PropChange aPC)
    {
        for (DeepChangeListener lsnr : _deepLsnrs)
            lsnr.deepChange(aSrc, aPC);
    }
}