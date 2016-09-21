package snap.swing;
import snap.util.*;

/**
 * A custom class.
 */
public class AWTPrefs extends Prefs {

    // The shared AWT Prefs
    static AWTPrefs  _shared = new AWTPrefs();

/**
 * Returns a value for given string.
 */
public String get(String aKey)  { return PrefsUtils.prefs().get(aKey, null); }

/**
 * Sets a value for given string.
 */
public void set(String aKey, Object aValue)  { PrefsUtils.prefsPut(aKey, aValue); }

/**
 * Returns the shared AWTPrefs.
 */
public static AWTPrefs get()  { return _shared; }

}