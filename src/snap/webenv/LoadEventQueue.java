package snap.webenv;
import netscape.javascript.JSObject;
import snap.webapi.*;

/**
 * This class is meant to handle JavaScript load callbacks.
 */
public class LoadEventQueue {

    // The current event thread
    private Thread _eventThread;

    // Shared event queue
    private static LoadEventQueue _shared = new LoadEventQueue();

    /**
     * Constructor.
     */
    public LoadEventQueue()
    {
        _shared = this;
        _eventThread = new Thread(LoadEventQueue.this::eventLoop);
        _eventThread.start();
    }

    /**
     * Waits for events and dispatches them.
     */
    private void eventLoop()
    {
        while(true) {

            // Wait for next event
            Object[] eventRecordArray = getNextEvent();

            // Handle load event
            EventListener<Event> eventLsnr = (EventListener<Event>) eventRecordArray[1];
            JSObject eventJS = (JSObject) eventRecordArray[2];
            Event event = new Event(eventJS);
            eventLsnr.handleEvent(event);
        }
    }

    /**
     * Registers a load event handler on the EventTarget
     */
    public static void addLoadEventListener(EventTarget eventTarget, EventListener<?> eventLsnr)
    {
        JSProxy jsproxy = (JSProxy) eventTarget;
        addLoadEventListenerImpl((JSObject) jsproxy.getJS(), eventLsnr);
    }

    /**
     * Waits for next event.
     */
    private static native Object[] getNextEvent();

    /**
     * LoadEventQueue: addLoadEventListenerImpl().
     */
    private static native void addLoadEventListenerImpl(JSObject eventTargetJS, EventListener<?> eventLsnr);
}
