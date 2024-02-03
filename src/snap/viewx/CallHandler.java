package snap.viewx;

/**
 * This class handles Repl calls.
 */
public abstract class CallHandler {

    // The shared instance
    private static CallHandler _shared;

    /**
     * Constructor.
     */
    public CallHandler()
    {
        _shared = this;
    }

    /**
     * This method is a dispatcher for Java source classes.
     */
    public abstract Object call(String className, String methodName, Object thisObject, Object[] args);

    /**
     * This method is a dispatcher for Java source classes.
     */
    public static Object Call(String className, String methodName, Object thisObject, Object[] args)
    {
        return _shared.call(className, methodName, thisObject, args);
    }
}
