package snap.webapi;

/**
 * This class is a wrapper for Web API PermissionStatus (https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus).
 */
public class PermissionStatus extends JSProxy {

    // Constant for state
    public enum State { Granted, Denied, Prompt }

    /**
     * Constructor.
     */
    public PermissionStatus(Object promiseJS)
    {
        super(promiseJS);
    }

    /**
     * Returns the state.
     */
    public State getState()
    {
        String stateStr = getMemberString("state");
        for (State state : State.values())
            if (state.toString().equalsIgnoreCase(stateStr))
                return state;
        return State.valueOf(stateStr);
    }
}