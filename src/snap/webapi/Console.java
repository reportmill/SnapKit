package snap.webapi;

/**
 * This class is a wrapper for Web API Console (https://developer.mozilla.org/en-US/docs/Web/API/Console).
 */
public class Console extends JSProxy {

    /**
     * Constructor.
     */
    public Console(Object winJS)
    {
        super(winJS);
    }

    /**
     * Log given object.
     */
    public static void log(Object anObj)
    {
        Object obj = anObj;
        if (anObj instanceof JSProxy)
            obj = ((JSProxy) anObj)._jsObj;

        Console console = WebEnv.get().console();
        console.call("log", obj);
    }
}