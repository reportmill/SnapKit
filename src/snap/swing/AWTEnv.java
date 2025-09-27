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

    // The current screen scale (1 = normal, 2 = HiDPI/Retina)
    private static double  _screenScale = -1;

    // The shared AWTEnv
    private static AWTEnv     _shared;

    /**
     * Creates a new AWTEnv.
     */
    public AWTEnv()
    {
        if (_env == null) {
            _env = _shared = this;
            ColorSpace._factory = new AWTColorSpaceFactory();
        }
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public String[] getFontNames()
    {
        return AWTFontUtils.getFontNames();
    }

    /**
     * Returns a list of all system family names.
     */
    public String[] getFamilyNames()
    {
        return AWTFontUtils.getFamilyNames();
    }

    /**
     * Returns a list of all font names for a given family name.
     */
    public String[] getFontNames(String aFamilyName)
    {
        return AWTFontUtils.getFontNames(aFamilyName);
    }

    /**
     * Returns a font file for given name.
     */
    public FontFile getFontFile(String aName)
    {
        return new AWTFontFile(aName);
    }

    /**
     * Creates image from source.
     */
    public Image getImageForSource(Object aSource)
    {
        return new J2DImage(aSource);
    }

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public Image getImageForSizeAndDpiScale(double aWidth, double aHeight, boolean hasAlpha, double dpiScale)
    {
        if (dpiScale <= 0)
            dpiScale = getScreenScale();
        return new J2DImage(aWidth, aHeight, hasAlpha, dpiScale);
    }

    /**
     * Returns a sound for given source.
     */
    public SoundClip getSound(Object aSource)
    {
        // return new JFXSoundClip(aSource);
        return new SwingSoundClip(aSource);
    }

    /**
     * Creates a sound for given source.
     */
    public SoundClip createSound()
    {
        // return new JFXSoundClip();
        return new SwingSoundClip(null);
    }

    /**
     * Tries to open the given file source with the platform reader.
     */
    public void openFile(Object aSource)
    {
        // Get file
        if (aSource instanceof WebFile)
            aSource = ((WebFile) aSource).getJavaFile();
        if (aSource instanceof WebURL)
            aSource = ((WebURL) aSource).getJavaUrl();
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
        // Get URL/URI and open with Desktop API
        try {
            WebURL snapURL = WebURL.getUrl(aSource);
            URL javaURL = snapURL != null ? snapURL.getJavaUrl() : null;
            URI uri = javaURL != null ? javaURL.toURI() : null;
            if (uri != null)
                Desktop.getDesktop().browse(uri);
        }

        // If something goes wrong, just complain
        catch(Throwable e) { System.err.println(e.getMessage()); }
    }

    /**
     * Tries to open the given file source with the platform text file reader.
     */
    public void openTextFile(Object aSource)
    {
        // Get file
        if (aSource instanceof WebFile)
            aSource = ((WebFile) aSource).getJavaFile();
        if (aSource instanceof WebURL)
            aSource = ((WebURL) aSource).getJavaUrl();
        File file = FileUtils.getFile(aSource);

        // Open with Runtime.exec "open -e <file-name>"
        String[] commands = { "open",  "-e", file.getAbsolutePath() };
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
        if (_screenScale >= 0) return _screenScale;
        return _screenScale = getScreenScaleImpl();
    }

    /**
     * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
     */
    private double getScreenScaleImpl()
    {
        // Get graphics configuration
        GraphicsEnvironment graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphicsDevice = graphicsEnv.getDefaultScreenDevice();
        GraphicsConfiguration graphicsConfig = graphicsDevice.getDefaultConfiguration();

        // Get DefaultTransform.Scale
        AffineTransform defaultTransform = graphicsConfig.getDefaultTransform();
        double defaultScale = defaultTransform.getScaleX();
        if (defaultScale == 1 || defaultScale == 2)
            return defaultScale;

        // Complain and return 1 since other methods failed
        System.err.println("AWTEnv.getScreenScale: Unexpected value: " + defaultScale);
        return 1;
    }

    /**
     * Plays a beep.
     */
    public void beep()
    {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Override to return AWTPrefs for name.
     */
    public Prefs getPrefs(String aName)
    {
        try { return AWTPrefs.getPrefs(aName); }
        catch (Exception e) { return super.getPrefs(aName); }
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public Method getMethod(Class<?> aClass, String aName, Class<?>... theClasses) throws NoSuchMethodException
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
            return h == null ? "localhost" : h.getHostName();
        }
        catch(Exception e) { return "localhost"; }
    }

    /**
     * Returns new SwingViewEnv.
     */
    @Override
    protected snap.view.ViewEnv createViewEnv()  { return new SwingViewEnv(); }

    /**
     * Returns a shared instance.
     */
    public static AWTEnv get()
    {
        if (_shared != null) return _shared;
        return new AWTEnv();
    }

    /**
     * Sets AWTEnv to be the default env.
     */
    public static void set()
    {
        get();
    }

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
            byte[] bytes = SnapUtils.getBytes(aSource);
            if (bytes == null) {
                System.err.println("AWTColorSpaceFactory: Error getting bytes for source: " + aSource);
                return null;
            }

            // Load profile and create/return space
            try {
                java.awt.color.ICC_Profile prof = java.awt.color.ICC_Profile.getInstance(bytes);
                java.awt.color.ColorSpace acs = new java.awt.color.ICC_ColorSpace(prof);
                return new AWTColorSpace(acs);
            }
            catch(Exception e) {
                System.err.println("AWTColorSpaceFactory: Error reading colorspace: " + e);
                return null;
            }
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