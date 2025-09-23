package snap.util;
import snap.gfx.GFXEnv;
import snap.view.ViewEnv;

/**
 * This class manages information about the SnapKit environment.
 */
public class SnapEnv {

    // The current platform
    private static Platform platform = getPlatform();

    // Whether app is currently running on Windows
    public static boolean isWindows = platform == Platform.WINDOWS;

    // Whether app is currently running on Mac
    public static boolean isMac = platform == Platform.MAC;

    // Whether app is currently running on TeaVM
    public static boolean isTeaVM = platform == Platform.TEAVM;

    // Whether app is currently running on CheerpJ
    public static boolean isWebVM = platform == Platform.CHEERP;

    // Whether is WebVM Swing
    public static boolean isWebVMSwing;

    // Whether is WebVM Windows
    public static boolean isWebVM_Windows;

    // Whether is WebVM Mac
    public static boolean isWebVM_Mac;

    // Whether is WebVM iOS
    public static boolean isWebVM_iOS;

    // Whether is JxBrowser
    public static boolean isJxBrowser = platform == Platform.JxBrowser;

    // Whether app is currently running on desktop
    public static boolean isDesktop = !isWebVM && !isTeaVM;

    // Constants for platform
    public enum Platform { WINDOWS, MAC, CHEERP, TEAVM, JxBrowser, UNKNOWN }

    /**
     * Returns the GFXEnv class.
     */
    public static Class<? extends GFXEnv> getGfxEnvClass()
    {
        // Get class name for platform GFXEnv
        String className = isTeaVM ? "snaptea.TV" : "snap.swing.AWTEnv";
        if (isWebVM)
            className = "snapcj.CJEnv";

        // Return GFXEnv class
        try { return (Class<? extends GFXEnv>) Class.forName(className); }
        catch(ClassNotFoundException ignored) { }

        if (isWebVM)
            isWebVMSwing = true;

        // Fall back on swing (but so teavm doesn't try to include swing classes)
        try { return (Class<? extends GFXEnv>) Class.forName(!"fool-teavm".isEmpty() ? "snap.swing.AWTEnv" : ""); }
        catch(ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the ViewEnv class.
     */
    public static Class<? extends ViewEnv> getViewEnvClass()
    {
        // Get class name for platform GFXEnv
        String className = isTeaVM ? "snaptea.TV" : "snap.swing.SwingViewEnv";
        if (isWebVM || isJxBrowser)
            className = "snapcj.CJViewEnv";

        // Return GFXEnv class
        try { return (Class<? extends ViewEnv>) Class.forName(className); }
        catch(ClassNotFoundException ignored) { }

        // Fall back on swing (but so teavm doesn't try to include swing classes)
        try { return (Class<? extends ViewEnv>) Class.forName(!"fool-teavm".isEmpty() ? "snap.swing.SwingViewEnv" : ""); }
        catch(ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the current platform.
     */
    private static Platform getPlatform()
    {
        String osName = System.getProperty("os.name");
        if (osName == null) osName = "TeaVM";
        String javaVendor = System.getProperty("java.vendor");
        if (javaVendor == null) javaVendor = "TeaVM";

        // JxBrowser
        try {
            Class.forName("com.teamdev.jxbrowser.js.JsObject");
            return Platform.JxBrowser;
        }
        catch (ClassNotFoundException ignore) { }

        // Windows
        if (osName.contains("Windows"))
            return Platform.WINDOWS;

        // Mac
        if (osName.contains("Mac OS X"))
            return Platform.MAC;

        // CheerpJ
        if (javaVendor.contains("Leaning"))
            return Platform.CHEERP;

        // TeaVM
        if (osName.contains("TeaVM"))
            return Platform.TEAVM;

        // Unknown
        return Platform.UNKNOWN;
    }
}
