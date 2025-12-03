package snap.webapi;

/**
 * This class is a wrapper for Web API Navigator (https://developer.mozilla.org/en-US/docs/Web/API/Navigator).
 */
public class Navigator {

    // The navigator
    private static Object _navigator;

    // The user agent string
    private static String _userAgent;

    // Returns whether platform is windows
    private static Boolean _isWindows;

    // Returns whether platform is mac
    private static Boolean _isMac;

    // Returns whether platform is iOS
    private static Boolean _isIOS;

    // Returns whether user agent is Safari
    private static Boolean _isSafari;

    /**
     * Returns the navigator.
     */
    private static Object getNavigator()
    {
        if (_navigator != null) return _navigator;
        Window window = Window.get();
        return _navigator = window.getMember("navigator");
    }

    /**
     * Returns whether platform is windows.
     */
    public static boolean isWindows()
    {
        if (_isWindows != null) return _isWindows;
        String userAgent = getUserAgent();
        return _isWindows = userAgent.contains("Win");
    }

    /**
     * Returns whether platform is Mac.
     */
    public static boolean isMac()
    {
        if (_isMac != null) return _isMac;
        String userAgent = getUserAgent();
        return _isMac = userAgent.contains("Mac");
    }

    /**
     * Returns whether platform is iOS.
     */
    public static boolean isIOS()
    {
        if (_isIOS != null) return _isIOS;
        String userAgent = getUserAgent();
        return _isIOS = userAgent.contains("iOS");
    }

    /**
     * Returns whether UserAgent is Safari.
     */
    public static boolean isSafari()
    {
        if (_isSafari != null) return _isSafari;
        String userAgent = getUserAgent();
        return _isSafari = userAgent.contains("Safari") && !userAgent.contains("Chrome") && !userAgent.contains("CriOS");
    }

    /**
     * Returns the platform.
     */
    public static String getPlatform()
    {
        Object navigatorJS = getNavigator();
        return WebEnv.get().getMemberString(navigatorJS, "platform");
    }

    /**
     * Returns the platform.
     */
    public static String getUserAgent()
    {
        if (_userAgent != null) return _userAgent;
        Object navigatorJS = getNavigator();
        return _userAgent = WebEnv.get().getMemberString(navigatorJS, "userAgent");
    }
}
