package snap.swing;
import java.security.AccessControlException;
import java.util.*;
import java.util.prefs.*;
import snap.util.*;

/**
 * AWT implementation of Snap Prefs.
 */
public class AWTPrefs extends Prefs {
    
    // The AWT Preferences
    Preferences      _prefs;

    // The shared AWT Prefs
    static Map<String,AWTPrefs>  _shared = new HashMap();

    // A special preferences instance to use if we don't have preferences permissions
    private static Preferences   _bogus = null;

/**
 * Creates new AWTPrefs.
 */
public AWTPrefs(String aName)
{
    // Get root prefs
    try { _prefs = Preferences.userRoot(); }
    catch(AccessControlException ex) { _prefs = getBogus(); }

    // Get named prefs
    if(aName!=null) _prefs = _prefs.node(aName);
}

/**
 * Creates new AWTPrefs.
 */
public AWTPrefs(Preferences aPrefs)  { _prefs = aPrefs; }

/**
 * Returns a value for given string.
 */
public String get(String aKey)  { return _prefs.get(aKey, null); }

/**
 * Returns a value for given string and default.
 */
public String get(String aKey, String aDefault)  { return _prefs.get(aKey, aDefault); }

/**
 * Sets a value for given string.
 */
public void set(String aKey, Object aValue)
{
    // Put value
    if(aValue instanceof String)
        _prefs.put(aKey, (String)aValue);
    else if(aValue instanceof Boolean)
        _prefs.putBoolean(aKey, ((Boolean)aValue).booleanValue());
    else if(aValue instanceof Float)
        _prefs.putFloat(aKey, ((Float)aValue).floatValue());
    else if(aValue instanceof Double)
        _prefs.putDouble(aKey, ((Double)aValue).doubleValue());
    else if(aValue instanceof Integer)
        _prefs.putInt(aKey, ((Integer)aValue).intValue());
    else if(aValue instanceof Long)
        _prefs.putLong(aKey, ((Long)aValue).longValue());
    else if(aValue instanceof Enum)
        _prefs.put(aKey, aValue.toString());
    else if(aValue==null)
        _prefs.remove(aKey);
    else System.err.println("Unsupported prefsPut() type: " + aValue.getClass().getName());
}

/**
 * Removes a value for given key.
 */
public void remove(String aKey)  { _prefs.remove(aKey); }

/**
 * Returns an int value for given key.
 */
public int getInt(String aKey, int aDefault)  { return _prefs.getInt(aKey, aDefault); }

/**
 * Returns a float value for given key.
 */
public float getFloat(String aKey, float aDefault)  { return _prefs.getFloat(aKey, aDefault); }

/**
 * Returns a boolean value for given key.
 */
public boolean getBoolean(String aKey, boolean aDefault)  { return _prefs.getBoolean(aKey, aDefault); }

/**
 * Returns the currently set prefs keys.
 */
public String[] getKeys()
{
    try { return _prefs.keys(); }
    catch(Exception e)  { System.err.println(e); return new String[0]; }
}

/**
 * Returns a child prefs for given name.
 */
public AWTPrefs getChild(String aName)  { return new AWTPrefs(_prefs.node(aName)); }

/**
 * Updates this persistant store associated with these preferences.
 */
public void flush()
{
    try { _prefs.flush(); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Clears all the preferences.
 */
public void clear()
{
    try { _prefs.removeNode(); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Returns the shared AWTPrefs.
 */
public static AWTPrefs getPrefs(String aName)
{
    AWTPrefs prefs = _shared.get(aName);
    if(prefs==null) _shared.put(aName, prefs = new AWTPrefs(aName));
    return prefs;
}

/**
 * Returns a shared bogus preferences instance.
 */
private static Preferences getBogus()  { return _bogus!=null? _bogus : (_bogus=new BogusPrefs(null,"")); }

/**
 * A Preferences implementation that just stores prefs to a map, in case we don't have permission
 * to read & write permissions.
 */
private static class BogusPrefs extends AbstractPreferences {

    Map _store = new HashMap();
    
    public BogusPrefs(AbstractPreferences parent, String name) { super(parent, name); }
    
    protected void syncSpi() throws BackingStoreException { }
    protected void flushSpi() throws BackingStoreException { }
    protected void removeSpi(String key) { _store.remove(key); }
    protected void removeNodeSpi() throws BackingStoreException { _store.clear(); }
    protected void putSpi(String key, String value) { _store.put(key,value); }
    protected String[] keysSpi() throws BackingStoreException { return (String[])_store.keySet().toArray(); }
    protected String getSpi(String key) { return (String)_store.get(key); }
    protected AbstractPreferences childSpi(String name) { return this; }
    protected String[] childrenNamesSpi() throws BackingStoreException { return null; }
}

}