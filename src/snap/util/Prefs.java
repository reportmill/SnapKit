package snap.util;
import snap.gfx.GFXEnv;

/**
 * A platform implementation of preferences.
 */
public abstract class Prefs {

    // The default preferences
    static Prefs  _default;
    
/**
 * Returns a value for given string.
 */
public abstract String get(String aKey);

/**
 * Returns a value for given string and default.
 */
public abstract String get(String aKey, String aDefault);

/**
 * Sets a value for given string.
 */
public abstract void set(String aKey, Object aValue);

/**
 * Removes a value for given key.
 */
public abstract void remove(String aKey);

/**
 * Returns an int value for given key.
 */
public abstract int getInt(String aKey, int aDefault);

/**
 * Returns a float value for given key.
 */
public abstract float getFloat(String aKey, float aDefault);

/**
 * Returns an boolean value for given key.
 */
public abstract boolean getBoolean(String aKey, boolean aDefault);

/**
 * Returns the currently set prefs keys.
 */
public abstract String[] getKeys();

/**
 * Returns a child prefs for given name.
 */
public abstract Prefs getChild(String aName);

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
    if(_default!=null) return _default;
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
    String pname = pkgEndInd>0? cname.substring(0, pkgEndInd) : "<unnamed>";
    String ppath = "/" + pname.replace('.', '/');
    return getPrefs(ppath);
}

/**
 * Returns a prefs instance that doesn't do anything.
 */
public static Prefs getFake()  { return _fp!=null? _fp : (_fp=new FakePrefs()); } static Prefs _fp;

/**
 * A Prefs implementation that doesn't do anything.
 */
private static class FakePrefs extends Prefs {
    
    /** Returns a value for given string. */
    public String get(String aKey)  { return null; }

    /** Returns a value for given string and default. */
    public String get(String aKey, String aDefault)  { return aDefault; }

    /** Sets a value for given string. */
    public void set(String aKey, Object aValue)  { }
    
    /** Removes a value for given key. */
    public void remove(String aKey)  { }

    /** Returns an int value for given key. */
    public int getInt(String aKey, int aDefault)  { return aDefault; }
    
    /** Returns a float value for given key. */
    public float getFloat(String aKey, float aDefault)  { return aDefault; }

    /** Returns a boolean value for given key. */
    public boolean getBoolean(String aKey, boolean aDefault)  { return aDefault; }
    
    /** Returns the currently set prefs keys. */
    public String[] getKeys()  { return new String[0]; }

    /** Returns a child prefs for given name. */
    public Prefs getChild(String aName)  { return this; }
}

}