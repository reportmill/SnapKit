/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.lang.reflect.Method;
import java.net.URL;
import snap.geom.Rect;
import snap.web.JRTSite;
import snap.util.*;
import snap.web.*;

/**
 * An adapter class for drawing in a native environment (Java2D, JavaFX).
 */
public abstract class GFXEnv {

    // The node environment
    protected static GFXEnv  _env;

    // Whether is WebVM Swing
    public static boolean isWebVMSwing;

    /**
     * Returns the Graphics environment.
     */
    public static GFXEnv getEnv()
    {
        if (_env != null) return _env;
        setDefaultEnv();
        return _env;
    }

    /**
     * Sets the default GFXEnv.
     */
    private static void setDefaultEnv()
    {
        // Get class name for platform GFXEnv
        String className = SnapUtils.isTeaVM ? "snaptea.TV" : "snap.swing.AWTEnv";
        if (SnapUtils.isWebVM)
            className = "snapcj.CJEnv";

        // Get GFXEnv class and create instance to set
        try { Class.forName(className).newInstance(); return; }
        catch(Exception e) { System.err.println("GFXEnv.setDefaultEnv: Can't set GFXEnv " + className + ", " + e); }

        if (SnapUtils.isWebVM)
            isWebVMSwing = true;

        // Fall back on swing (but so teavm doesn't try to include swing classes)
        try { Class.forName("fool-teavm".length() > 0 ? "snap.swing.AWTEnv" : "").newInstance(); }
        catch(Exception e) { System.err.println("ViewEnv.setDefaultEnv: Can't set ViewEnv: " + className + ", " + e); }
    }

    /**
     * Returns resource for class and path.
     */
    public URL getResource(Class<?> aClass, String aPath)
    {
        return aClass.getResource(aPath);
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
     * Creates image from source.
     */
    public abstract Image getImageForSource(Object aSource);

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public abstract Image getImageForSizeAndDpiScale(double aWidth, double aHeight, boolean hasAlpha, double dpiScale);

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
     * Tries to open the given file source with the platform reader.
     */
    public void downloadFile(WebFile aFile)  { System.err.println("GFXEnv.downloadFile: Not implemented"); }

    /**
     * Tries to open the given file source with the platform text file reader.
     */
    public void openTextFile(Object aSource)  { openFile(aSource); }

    /**
     * Returns the screen resolution.
     */
    public abstract double getScreenResolution();

    /**
     * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
     */
    public abstract double getScreenScale();

    /**
     * Returns the screen bounds inset to usable area.
     */
    public Rect getScreenBoundsInset()  { return snap.view.ViewEnv.getEnv().getScreenBoundsInset(); }

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
    public String getClassRoot()
    {
        Class<?> thisClass = getClass();
        String className = thisClass.getName();
        String simpleName = thisClass.getSimpleName();

        // Get URL
        URL classFileUrl = thisClass.getResource(simpleName + ".class");
        String classFilePath = classFileUrl.toString();
        String classRootPath = classFilePath.substring(0, classFilePath.length() - className.length() - 1);

        // Return
        return classRootPath;
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public abstract Method getMethod(Class<?> aClass, String aName, Class<?>... theClasses) throws NoSuchMethodException;

    /**
     * Creates a site for a URL.
     */
    public WebSite createSiteForURL(WebURL siteURL)
    {
        // Handle JRT - Java runtime modules URLs
        String scheme = siteURL.getScheme();
        if (scheme.equals("jrt"))
            return new JRTSite();

        // Return not found
        return null;
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public abstract void exit(int aValue);

    /**
     * This is really just here to help with TeaVM.
     */
    public String getHostname()
    {
        return "localhost";
    }

    /**
     * Sets the Browser window.location.hash (if running in browser).
     */
    public void setBrowserWindowLocationHash(String aString)  { }

    /**
     * Executes a process.
     */
    public Object execProcess(String[] args)  { return null; }
}