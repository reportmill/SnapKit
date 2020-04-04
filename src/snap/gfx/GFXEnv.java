/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.net.URL;
import java.util.function.Consumer;
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
        if(cls!=null) try { ClassUtils.getMethodOrThrow(cls, "set").invoke(null); return; }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set CJEnv: " + e); }
    }
    
    // If platform is Cheerp, try to install Cheerp
    if(SnapUtils.getPlatform()==SnapUtils.Platform.TEAVM) {
        Class cls = null; try { cls = ClassUtils.getClass("snaptea.TV"); } catch(Exception e) { }
        if(cls==null) System.err.println("GFXEnv.setDefaultEnv: Can't find snaptea.TV");
        else try { ClassUtils.getMethodOrThrow(cls, "set").invoke(null); return; }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set TVEnv: " + e); }
    }
    
    // Try Swing
    Class cls = null; try { cls = ClassUtils.getClass("snap.swing.AWTEnv"); } catch(Exception e) { }
    try { ClassUtils.getMethodOrThrow(cls, "set").invoke(null); }
    catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set AWTEnv " + e); }
}

/**
 * Returns resource for class and path.
 */
public abstract URL getResource(Class aClass, String aPath);

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
 * Creates image from source.
 */
public abstract Image getImage(Object aSource);

/**
 * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
 */
public abstract Image getImageForSizeAndScale(double aWidth, double aHeight, boolean hasAlpha, double aScale);

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
 * Tries to open the given file source with the platform text file reader.
 */
public void openTextFile(Object aSource)  { openFile(aSource); }

/**
 * Executes request and invokes callback with response.
 */
public boolean getResponseAndCall(WebRequest aReq, Consumer <WebResponse> aCallback)
{
    return false;
}

/**
 * Returns the screen resolution.
 */
public abstract double getScreenResolution();

/**
 * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
 */
public abstract double getScreenScale();

/**
 * Plays a beep.
 */
public abstract void beep();

/**
 * Returns the platform preferences object.
 */
public Prefs getPrefs(String aName)  { return Prefs.getFake(); }

/**
 * Returns the root URL string of classes.
 */
public abstract String getClassRoot();

}