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
 * Sets a value for given string.
 */
public abstract void set(String aKey, Object aValue);

/**
 * Returns the default prefs.
 */
public static Prefs get()  { return GFXEnv.getEnv().getPrefs(); }

}