/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.web.*;

/**
 * An adapter class for drawing in a native environment (Java2D, JavaFX).
 */
public abstract class GFXEnv {

    // The node environment
    static GFXEnv        _env = snap.swing.AWTEnv.get(), _senv = _env;

/**
 * Returns the Graphics environment.
 */
public static GFXEnv getEnv()  { return _env; }

/**
 * Pushes an environment.
 */
public static void setEnv(GFXEnv anEnv)  { _env = anEnv; }

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public String[] getFontNames()  { return _senv.getFontNames(); }

/**
 * Returns a list of all system family names.
 */
public String[] getFamilyNames()  { return _senv.getFamilyNames(); }

/**
 * Returns a list of all font names for a given family name.
 */
public String[] getFontNames(String aFamilyName)  { return _senv.getFontNames(aFamilyName); }

/**
 * Returns a font file for given name.
 */
public FontFile getFontFile(String aName)  { return _senv.getFontFile(aName); }

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
public WebURL getURL(Object aSource)  { return _senv.getURL(aSource); }

/**
 * Returns a URL for given class and name/path string.
 */
public WebURL getURL(Class aClass, String aName)  { return _senv.getURL(aClass, aName); }

/**
 * Returns a site for given source URL.
 */
public WebSite getSite(WebURL aSiteURL)  { return _senv.getSite(aSiteURL); }

/**
 * Tries to open the given file source with the platform reader.
 */
public void openFile(Object aSource)  { _senv.openFile(aSource); }

/**
 * Tries to open the given URL source with the platform URL reader.
 */
public void openURL(Object aSource)  { _senv.openURL(aSource); }

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()  { return _senv.getScreenResolution(); }

/**
 * Plays a beep.
 */
public void beep()  { _senv.beep(); }

/**
 * Sets this JVM to be headless.
 */
public void setHeadless()  { _senv.setHeadless(); }

}