package snap.swing;
import java.awt.Desktop;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A GFXEnv implementation using AWT.
 */
public class AWTEnv extends GFXEnv {
    
    // The shared AWTEnv
    static AWTEnv     _shared;

    /**
     * Creates a new AWTEnv.
     */
    public AWTEnv()
    {
        if (_env==null) {
            _env = _shared = this;
            ColorSpace._factory = new AWTColorSpaceFactory();
        }
    }

    /**
     * Returns resource for class and path.
     */
    public URL getResource(Class aClass, String aPath)
    {
        return aClass.getResource(aPath);
    }

    /**
     * Returns the root URL classes in Snap Jar as string.
     */
    public String getClassRoot()
    {
        URL url = getClass().getResource(getClass().getSimpleName() + ".class");
        String urls = url.toString();
        String suffix = getClass().getName();
        String urls2 = urls.substring(0, urls.length() - suffix.length() - 1);
        return urls2;
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public String[] getFontNames()  { return AWTFontUtils.getFontNames(); }

    /**
     * Returns a list of all system family names.
     */
    public String[] getFamilyNames()  { return AWTFontUtils.getFamilyNames(); }

    /**
     * Returns a list of all font names for a given family name.
     */
    public String[] getFontNames(String aFamilyName)  { return AWTFontUtils.getFontNames(aFamilyName); }

    /**
     * Returns a font file for given name.
     */
    public FontFile getFontFile(String aName)  { return new AWTFontFile(aName); }

    /**
     * Creates image from source.
     */
    public Image getImage(Object aSource)  { return new J2DImage(aSource); }

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public Image getImageForSizeAndScale(double aWidth, double aHeight, boolean hasAlpha, double aScale)
    {
        double scale = aScale<=0? getScreenScale() : aScale;
        return new J2DImage(aWidth, aHeight, hasAlpha, scale);
    }

    /**
     * Returns a sound for given source.
     */
    public SoundClip getSound(Object aSource)
    {
        try {
            Class cls = Class.forName("snap.swing.JFXSoundClip");
            return (SoundClip)cls.getDeclaredConstructor(Object.class).newInstance(aSource);
        }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Creates a sound for given source.
     */
    public SoundClip createSound()
    {
        try {
            Class cls = Class.forName("snap.swing.JFXSoundClip");
            return (SoundClip)cls.newInstance();
        }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Tries to open the given file source with the platform reader.
     */
    public void openFile(Object aSource)
    {
        // Get file
        if (aSource instanceof WebFile) aSource = ((WebFile)aSource).getJavaFile();
        if (aSource instanceof WebURL) aSource = ((WebURL)aSource).getJavaURL();
        File file = FileUtils.getFile(aSource);

        // Open with Desktop API
        try { Desktop.getDesktop().open(file); }
        catch(Throwable e) { System.err.println(e.getMessage()); }
    }

    /**
     * Tries to open the given URL with the platform reader.
     */
    public void openURL(Object aSource)
    {
        // Get URL string
        WebURL url = WebURL.getURL(aSource);
        String urls = url!=null? url.getString() : null;

        // Open with Desktop API
        try { Desktop.getDesktop().browse(new URI(urls)); }
        catch(Throwable e) { System.err.println(e.getMessage()); }
    }

    /**
     * Tries to open the given file source with the platform text file reader.
     */
    public void openTextFile(Object aSource)
    {
        // Get file
        if (aSource instanceof WebFile) aSource = ((WebFile)aSource).getJavaFile();
        if (aSource instanceof WebURL) aSource = ((WebURL)aSource).getJavaURL();
        File file = FileUtils.getFile(aSource);

        // Open with Runtime.exec "open -e <file-name>"
        String commands[] = { "open",  "-e", file.getAbsolutePath() };
        try { Runtime.getRuntime().exec(commands); }
        catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Returns the screen resolution.
     */
    public double getScreenResolution()
    {
        try { return Toolkit.getDefaultToolkit().getScreenResolution(); }
        catch(java.awt.HeadlessException he) { return 72; }
    }

    /**
     * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
     */
    public double getScreenScale()
    {
        // Get graphics configuration
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        // The method was private in Java 8.
        try {
            Method  meth = gd.getClass().getMethod("getScaleFactor");
            Number scale = (Number)meth.invoke(gd);
            double ds = scale.doubleValue(); if (ds==1 || ds==2) return ds;
            System.err.println("AWTEnv.getScreenScale: Unexepected value: " + ds); return 1;
        }
        catch(Exception e) { System.out.println("AWTEnv.getScreenScale: Unexepected error: " + e); }

        // This is the way to do it in Java 9.
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        AffineTransform xfm = gc.getDefaultTransform();
        double ds = xfm.getScaleX(); if (ds==1 || ds==2) return ds;
        System.err.println("AWTEnv.getScreenScale: Unexepected value: " + ds); return 1;
    }

    /**
     * Plays a beep.
     */
    public void beep()  { Toolkit.getDefaultToolkit().beep(); }

    /**
     * Override to return AWTPrefs for name.
     */
    public Prefs getPrefs(String aName)  { return AWTPrefs.getPrefs(aName); }

    /**
     * This is really just here to help with TeaVM.
     */
    public Method getMethod(Class aClass, String aName, Class ... theClasses) throws NoSuchMethodException
    {
        return aClass.getMethod(aName, theClasses);
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public void exit(int aValue)
    {
        System.exit(aValue);
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public String getHostname()
    {
        try {
            InetAddress h = InetAddress.getLocalHost();
            return h==null ? "localhost" : h.getHostName();
        }
        catch(Exception e) { return "localhost"; }
    }

    /**
     * Returns a shared instance.
     */
    public static AWTEnv get()
    {
        if (_shared!=null) return _shared;
        return new AWTEnv();
    }

    /**
     * Sets AWTEnv to be the default env.
     */
    public static void set()  { get(); }

    /**
     * Implementation of snap ColorSpace using java.awt.color.ColorSpace.
     */
    public static class AWTColorSpaceFactory implements ColorSpace.ColorSpaceFactory {

        /** Returns a ColorSpace for given type. */
        public ColorSpace getInstance(int aCS)  { return new AWTColorSpace(aCS); }

        /** Create ICC ColorSpace from source. */
        public ColorSpace createColorSpaceICC(Object aSource)
        {
            // Get bytes
            byte bytes[] = SnapUtils.getBytes(aSource);
            if (bytes==null) {
                System.err.println("AWTColorSpaceFactory: Error getting bytes for source: " + aSource); return null; }

            // Load profile and create/return space
            try {
                java.awt.color.ICC_Profile prof = java.awt.color.ICC_Profile.getInstance(bytes);
                java.awt.color.ColorSpace acs = new java.awt.color.ICC_ColorSpace(prof);
                return new AWTColorSpace(acs);
            }
            catch(Exception e) { System.err.println("AWTColorSpaceFactory: Error reading colorspace: " + e); return null; }
        }
    }

    /**
     * Implementation of snap ColorSpace using java.awt.color.ColorSpace.
     */
    public static class AWTColorSpace extends ColorSpace {
        java.awt.color.ColorSpace _acs;
        AWTColorSpace(int aCS)  { super(aCS,0); _acs = java.awt.color.ColorSpace.getInstance(aCS); }
        AWTColorSpace(java.awt.color.ColorSpace aACS)  { super(aACS.getType(),aACS.getNumComponents()); _acs = aACS; }
        public boolean isCS_sRGB() { return _acs.isCS_sRGB(); }
        public float[] toRGB(float[] colorvalue)  { return _acs.toRGB(colorvalue); }
        public float[] fromRGB(float[] rgbvalue)  { return _acs.fromRGB(rgbvalue); }
        public float[] toCIEXYZ(float[] colorvalue)  { return _acs.toCIEXYZ(colorvalue); }
        public float[] fromCIEXYZ(float[] colorvalue)  { return _acs.fromCIEXYZ(colorvalue); }
        public int getType()  { return _acs.getType(); }
        public int getNumComponents()  { return _acs.getNumComponents(); }
        public String getName(int idx)  { return _acs.getName(idx); }
    }
}