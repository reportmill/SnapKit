/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;
import snap.web.*;

/**
 * An adapter class for drawing in a native environment (Java2D, JavaFX).
 */
public abstract class GFXEnv {

    // The node environment
    static GFXEnv        _env;

/**
 * Returns the Graphics environment.
 */
public static GFXEnv getEnv()
{
    if(_env==null) setAWTEnv();
    return _env;
}

/**
 * Pushes an environment.
 */
public static void setEnv(GFXEnv anEnv)  { _env = anEnv; }

/**
 * Sets the SwingEnv.
 */
public static void setAWTEnv()
{
    try {
        Class cls = Class.forName("snap.swing.AWTEnv");
        cls.getMethod("set").invoke(null);
    }
    catch(Exception e) { System.err.println("ViewEnv: No Environment set " + e); }
}

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public abstract String[] getFontNames();

/**
 * Returns a list of all system family names.
 */
public abstract String[] getFamilyNames();

/**
 * Returns a list of all font names for a given family name.
 */
public abstract String[] getFontNames(String aFamilyName);

/**
 * Returns a font file for given name.
 */
public abstract FontFile getFontFile(String aName);

/**
 * Creates a new image from source.
 */
public abstract Image getImage(Object aSource);

/**
 * Creates a new image for width, height and alpha.
 */
public abstract Image getImage(int aWidth, int aHeight, boolean hasAlpha);

/**
 * Returns a sound for given source.
 */
public abstract SoundClip getSound(Object aSource);

/**
 * Creates a sound for given source.
 */
public abstract SoundClip createSound();

/**
 * Returns a URL for given source.
 */
public abstract WebURL getURL(Object aSource);

/**
 * Returns a URL for given class and name/path string.
 */
public abstract WebURL getURL(Class aClass, String aName);

/**
 * Returns a site for given source URL.
 */
public abstract WebSite getSite(WebURL aSiteURL);

/**
 * Tries to open the given file source with the platform reader.
 */
public abstract void openFile(Object aSource);

/**
 * Tries to open the given URL source with the platform URL reader.
 */
public abstract void openURL(Object aSource);

/**
 * Returns the screen resolution.
 */
public abstract double getScreenResolution();

/**
 * Plays a beep.
 */
public abstract void beep();

/**
 * Returns the platform preferences object.
 */
public abstract Prefs getPrefs();

/**
 * Sets this JVM to be headless.
 */
public abstract void setHeadless();

/**
 * Returns the current platform.
 */
public abstract SnapUtils.Platform getPlatform();

/**
 * Returns a key value.
 */
public abstract Object getKeyValue(Object anObj, String aKey);

/**
 * Sets a key value.
 */
public abstract void setKeyValue(Object anObj, String aKey, Object aValue);

/**
 * Returns a key chain value.
 */
public abstract Object getKeyChainValue(Object anObj, String aKeyChain);

/**
 * Sets a key chain value.
 */
public abstract void setKeyChainValue(Object anObj, String aKeyChain, Object aValue);

/**
 * Returns a key list value.
 */
public abstract Object getKeyListValue(Object anObj, String aKey, int anIndex);

/**
 * Adds a key list value.
 */
public abstract void setKeyListValue(Object anObj, String aKey, Object aValue, int anIndex);

}