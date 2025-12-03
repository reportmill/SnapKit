package snap.webapi;
import java.util.function.DoubleConsumer;

/**
 * This class is a wrapper for Web API Window (https://developer.mozilla.org/en-US/docs/Web/API/Window).
 */
public class Window extends JSProxy implements EventTarget {

    // The current Window
    private static Window _window;

    // The current document
    private static HTMLDocument _document;

    // The current Location
    private static Location _location;

    /**
     * Constructor.
     */
    public Window(Object winJS)
    {
        super(winJS);
    }

    /**
     * Returns the ratio of the resolution in physical pixels to the resolution in CSS pixels for the current display device.
     */
    public static double getDevicePixelRatio()  { return get().getMemberDouble("devicePixelRatio"); }

    /**
     * Return window InnerWidth.
     */
    public int getInnerWidth()  { return getMemberInt("innerWidth"); }

    /**
     * Return window InnerHeight.
     */
    public int getInnerHeight()  { return getMemberInt("innerHeight"); }

    /**
     * Wrapper method for Web API method.
     */
    public void open(String url, String target)  { open(url, target, null); }

    /**
     * Wrapper method for Web API method.
     */
    public void open(String url, String target, String windowFeatures)
    {
        WebEnv.get().open(url, target, windowFeatures);
    }

    /**
     * Request animation frame.
     */
    public static int requestAnimationFrame(DoubleConsumer callback)
    {
        return WebEnv.get().requestAnimationFrame(callback);
    }

    /**
     * Schedules a runnable to execute after a delay of given milliseconds.
     */
    public static void setTimeout(Runnable aRun, int aDelay)  { WebEnv.get().setTimeout(aRun, aDelay); }

    /**
     * Schedules a runnable to execute every time a given number of milliseconds elapses.
     */
    public static int setInterval(Runnable aRun, int aPeriod)  { return WebEnv.get().setInterval(aRun, aPeriod); }

    /**
     * Stops intervals for given id.
     */
    public static void clearInterval(int anId)  { WebEnv.get().clearInterval(anId); }

    /**
     * Returns the current window.
     */
    public static Window get()
    {
        if (_window != null) return _window;
        return _window = WebEnv.get().window();
    }

    /**
     * Returns the current location.
     */
    public static Location location()
    {
        if (_location != null) return _location;
        Window window = get();
        Object locationJS = window.getMember("location");
        return _location = new Location(locationJS);
    }

    /**
     * Returns the current window.
     */
    public static HTMLDocument getDocument()
    {
        if (_document != null) return _document;
        Window window = get();
        Object documentJS = window.getMember("document");
        return _document = new HTMLDocument(documentJS);
    }

    @Deprecated
    public static Window current() { return get(); }
}
