/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import java.util.prefs.*;
import java.security.AccessControlException;

/**
 * This class offers a number of useful general purpose utilities for storing preferences.
 */
public class PrefsUtils {

    // The class used to store preferences
    static Class  _prefsClass = PrefsUtils.class;
    
    // A special preferences instance to use if we don't have preferences permissions
    private static Preferences _bogusPreferences = null;

/**
 * Returns the user Preferences object.
 */
public static Preferences prefs()  { return getPrefs(); }

/**
 * Returns the user Preferences object (or bogus prefs, if security exception).
 */
public static Preferences getPrefs()
{
    try { return Preferences.userNodeForPackage(_prefsClass); }
    catch(AccessControlException ex) { return getBogusPreferences(); }
}

/**
 * Adds an object to the user Preferences object.
 */
public static void prefsPut(String aKey, Object aValue)  { prefsPut(aKey, aValue, false); }

/**
 * Adds an object to the user Preferences object.
 */
public static void prefsPut(String aKey, Object aValue, boolean doFlush)
{
    // Put value
    if(aValue instanceof String)
        getPrefs().put(aKey, (String)aValue);
    else if(aValue instanceof Boolean)
        getPrefs().putBoolean(aKey, ((Boolean)aValue).booleanValue());
    else if(aValue instanceof Float)
        getPrefs().putFloat(aKey, ((Float)aValue).floatValue());
    else if(aValue instanceof Double)
        getPrefs().putDouble(aKey, ((Double)aValue).doubleValue());
    else if(aValue instanceof Integer)
        getPrefs().putInt(aKey, ((Integer)aValue).intValue());
    else if(aValue instanceof Long)
        getPrefs().putLong(aKey, ((Long)aValue).longValue());
    else if(aValue instanceof Enum)
        getPrefs().put(aKey, aValue.toString());
    else if(aValue==null)
        getPrefs().remove(aKey);
    else System.err.println("Unsupported prefsPut() type: " + aValue.getClass().getName());
    
    // If requested, do flush
    if(doFlush)
        flush();
}

/**
 * Flushes the preferences.
 */
public static void flush()
{
    try { getPrefs().flush(); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Sets the class that preferences are associated with.
 */
public static void setPrefsClass(Class aClass)  { _prefsClass = aClass; }

/**
 * Returns a shared bogus preferences instance.
 */
public static Preferences getBogusPreferences()
{
    if(_bogusPreferences==null)
        _bogusPreferences = new BogusPreferences(null, "");
    return _bogusPreferences;
}

/**
 * A Preferences implementation that just stores prefs to a map, in case we don't have permission
 * to read & write permissions.
 */
private static class BogusPreferences extends AbstractPreferences {

    Map _store = new HashMap();
    
    public BogusPreferences(AbstractPreferences parent, String name) { super(parent, name); }
    
    protected void syncSpi() throws BackingStoreException { }
    protected void flushSpi() throws BackingStoreException { }
    protected void removeSpi(String key) { _store.remove(key); }
    protected void removeNodeSpi() throws BackingStoreException { _store.clear(); }
    protected void putSpi(String key, String value) { _store.put(key,value); }
    protected String[] keysSpi() throws BackingStoreException { return (String[])_store.keySet().toArray(); }
    protected String getSpi(String key) { return (String)_store.get(key); }
    protected AbstractPreferences childSpi(String name) { return null; }
    protected String[] childrenNamesSpi() throws BackingStoreException { return null; }
}

}