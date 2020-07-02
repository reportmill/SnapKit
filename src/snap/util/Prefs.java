package snap.util;
import snap.gfx.GFXEnv;

import java.util.HashMap;
import java.util.Map;

/**
 * A platform implementation of preferences.
 */
public abstract class Prefs {

    // The default preferences
    private static Prefs  _default;
    
    /**
     * Returns a value for given string.
     */
    public Object getValue(String aKey)
    {
        return getValue(aKey, null);
    }

    /**
     * Returns a value for given string and default.
     */
    public abstract Object getValue(String aKey, Object aDefault);

    /**
     * Sets a value for given string.
     */
    public abstract void setValue(String aKey, Object aValue);

    /**
     * Removes a value for given key.
     */
    public void remove(String aKey)
    {
        setValue(aKey, null);
    }

    /**
     * Returns a value for given string.
     */
    public String getString(String aKey)
    {
        return getString(aKey, null);
    }

    /**
     * Returns a value for given string and default.
     */
    public String getString(String aKey, String aDefault)
    {
        Object val = getValue(aKey, aDefault);
        return SnapUtils.stringValue(val);
    }

    /**
     * Returns an int value for given key.
     */
    public int getInt(String aKey, int aDefault)
    {
        Object val = getValue(aKey);
        return val!=null ? SnapUtils.intValue(val) : aDefault;
    }

    /**
     * Returns a float value for given key.
     */
    public double getDouble(String aKey, double aDefault)
    {
        Object val = getValue(aKey);
        return val!=null ? SnapUtils.doubleValue(val) : aDefault;
    }

    /**
     * Returns an boolean value for given key.
     */
    public boolean getBoolean(String aKey, boolean aDefault)
    {
        Object val = getValue(aKey);
        return val!=null ? SnapUtils.boolValue(val) : aDefault;
    }

    /**
     * Returns the currently set prefs keys.
     */
    public abstract String[] getKeys();

    /**
     * Returns a child prefs for given name.
     */
    public Prefs getChild(String aName)
    {
        return this;
    }

    /**
     * Updates any persistant store associated with these preferences.
     */
    public void flush()  { }

    /**
     * Clears all the preferences.
     */
    public void clear()  { }

    /**
     * Returns the default prefs.
     */
    public static Prefs get()  { return getPrefsDefault(); }

    /**
     * Returns the default prefs.
     */
    public static Prefs getPrefsDefault()
    {
        if (_default!=null) return _default;
        return _default = getPrefs("DefaultPrefs");
    }

    /**
     * Sets the default preferences instance.
     */
    public static void setPrefsDefault(Prefs thePrefs)  { _default = thePrefs; }

    /**
     * Returns the preferences for given node name.
     */
    public static Prefs getPrefs(String aName)  { return GFXEnv.getEnv().getPrefs(aName); }

    /**
     * Returns the preferences for given class (package really).
     */
    public static Prefs getPrefs(Class aClass)
    {
        String cname = aClass.getName();
        int pkgEndInd = cname.lastIndexOf('.');
        String pname = pkgEndInd>0 ? cname.substring(0, pkgEndInd) : "<unnamed>";
        String ppath = "/" + pname.replace('.', '/');
        return getPrefs(ppath);
    }

    /**
     * Returns a prefs instance that doesn't do anything.
     */
    public static Prefs getFake()  { return _fp!=null? _fp : (_fp=new MapPrefs()); } static Prefs _fp;

    /**
     * A Prefs implementation that doesn't do anything.
     */
    private static class MapPrefs extends Prefs {

        // A map to hold prefs
        private Map<String,Object> _map = new HashMap<>();

        /** Returns a value for given string and default. */
        public Object getValue(String aKey, Object aDefault)
        {
            Object val = _map.get(aKey);
            return val!=null ? val : aDefault;
        }

        /** Sets a value for given string. */
        public void setValue(String aKey, Object aValue)
        {
            _map.put(aKey, aValue);
        }

        /** Returns the currently set prefs keys. */
        public String[] getKeys()
        {
            return _map.keySet().toArray(new String[0]);
        }
    }
}