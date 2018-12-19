/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;

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
    if(_env==null) setDefaultEnv();
    return _env;
}

/**
 * Pushes an environment.
 */
public static void setEnv(GFXEnv anEnv)  { _env = anEnv; }

/**
 * Sets the default GFXEnv.
 */
public static void setDefaultEnv()
{
    // If platform is Cheerp, try to install Cheerp
    if(SnapUtils.getPlatform()==SnapUtils.Platform.CHEERP) {
        Class cls = null; try { cls = ClassUtils.getClass("snapcj.CJEnv"); }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't find snapcj.CJEnv"); }
        if(cls!=null) try { cls.getMethod("set").invoke(null); return; }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set CJEnv: " + e); }
    }
    
    // If platform is Cheerp, try to install Cheerp
    if(SnapUtils.getPlatform()==SnapUtils.Platform.TEAVM) {
        Class cls = null; try { cls = ClassUtils.getClass("snaptea.TV"); } catch(Exception e) { }
        if(cls==null) System.err.println("GFXEnv.setDefaultEnv: Can't find snaptea.TV");
        else try { cls.getMethod("set").invoke(null); return; }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set TVEnv: " + e); }
    }
    
    // Try Swing
    try { ClassUtils.getClass("snap.swing.AWTEnv").getMethod("set").invoke(null); }
    catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set AWTEnv " + e); }
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
public Prefs getPrefs(String aName)  { return Prefs.getFake(); }

}