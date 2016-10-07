package snap.util;
import snap.gfx.GFXEnv;

/**
 * A platform implementation of preferences.
 */
public abstract class Prefs {

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
 * Returns the currently set prefs keys.
 */
public abstract String[] getKeys();

/**
 * Returns a child prefs for given name.
 */
public abstract Prefs getChild(String aName);

/**
 * Returns the default prefs.
 */
public static Prefs get()  { return GFXEnv.getEnv().getPrefs(); }

}