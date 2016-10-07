package snap.swing;
import java.util.prefs.Preferences;
import snap.util.*;

/**
 * A custom class.
 */
public class AWTPrefs extends Prefs {
    
    // The AWT Preferences
    Preferences      _prefs;

    // The shared AWT Prefs
    static AWTPrefs  _shared = new AWTPrefs(null);

/**
 * Creates new AWT Prefs.
 */
public AWTPrefs(String aName)
{
    _prefs = PrefsUtils.prefs();
    if(aName!=null) _prefs = _prefs.node(aName);
}

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
public AWTPrefs getChild(String aName)  { return new AWTPrefs(aName); }

/**
 * Returns the shared AWTPrefs.
 */
public static AWTPrefs get()  { return _shared; }

}