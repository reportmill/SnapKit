package snap.javafx;
import snap.gfx.*;
import snap.swing.AWTEnv;
import snap.util.*;
import snap.web.*;

/**
 * A custom class.
 */
public class JFXGfxEnv extends GFXEnv {

    // The shared JFXGfxEnv
    static JFXGfxEnv     _shared = new JFXGfxEnv();
    
    // The AWT GFXEnv to fill in gaps for now
    static AWTEnv        _senv = AWTEnv.get();

/**
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new JFXImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new JFXImage(aWidth,aHeight,hasAlpha); }

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)  { return new SoundData(aSource); }

/**
 * Creates a new sound clip.
 */
public SoundClip createSound()  { return new SoundData(); }

/** Returns a list of all system fontnames (excludes any that don't start with capital A-Z). */
public String[] getFontNames()  { return _senv.getFontNames(); }

/** Returns a list of all system family names. */
public String[] getFamilyNames()  { return _senv.getFamilyNames(); }

/** Returns a list of all font names for a given family name. */
public String[] getFontNames(String aFamilyName)  { return _senv.getFontNames(aFamilyName); }

/** Returns a font file for given name. */
public FontFile getFontFile(String aName)  { return _senv.getFontFile(aName); }

/** Returns a URL for given source. */
public WebURL getURL(Object aSource)  { return _senv.getURL(aSource); }

/** Returns a URL for given class and name/path string. */
public WebURL getURL(Class aClass, String aName)  { return _senv.getURL(aClass, aName); }

/** Returns a site for given source URL. */
public WebSite getSite(WebURL aSiteURL)  { return _senv.getSite(aSiteURL); }

/** Tries to open the given file source with the platform reader. */
public void openFile(Object aSource)  { _senv.openFile(aSource); }

/** Tries to open the given URL source with the platform URL reader. */
public void openURL(Object aSource)  { _senv.openURL(aSource); }

/** Returns the screen resolution. */
public double getScreenResolution()  { return _senv.getScreenResolution(); }

/** Plays a beep. */
public void beep()  { _senv.beep(); }

/** Returns the platform preferences object. */
public Prefs getPrefs()  { return _senv.getPrefs(); }

/** Sets this JVM to be headless. */
public void setHeadless()  { _senv.setHeadless(); }

/** Returns the current platform. */
public SnapUtils.Platform getPlatform()  { return _senv.getPlatform(); }

/**
 * Returns a shared instance.
 */
public static JFXGfxEnv get()  { return _shared; }

}