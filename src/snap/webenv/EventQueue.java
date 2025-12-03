package snap.webenv;
import netscape.javascript.JSObject;
import snap.webapi.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

/**
 * This class is meant to handle JavaScript callbacks like setTimeout(), setInterval(), addEventListener(), etc.
 *
 *    - An "_eventNotifyMutex" mutex is created in cjdom.js as a promise (wrapped in a dictionary)
 *    - EventQueue.eventLoop() calls getNextEvent() to get next event as array [ name, lambda func, [ arg ] ]
 *    - JS callback functions register with callback to fireEvent(name, func, arg)
 *    - fireEvent() triggers the promise
 *    - getNextEventImpl() returns the event arg array after resolved mutex promise
 */
public class EventQueue {

    // The current event thread
    private EventQueueThread _eventThread;

    // A stack of event threads
    private Deque<EventQueueThread> _eventThreadStack = new ArrayDeque<>();

    // Shared event queue
    private static EventQueue _shared = new EventQueue();

    // Constants for event types
    public static final String ANIMATION_EVENT = "animation";
    public static final String INVOCATION_EVENT = "invocation";

    /**
     * Constructor.
     */
    public EventQueue()
    {
        _shared = this;

        // Start new event thread
        startNewEventThread();
    }

    /**
     * Starts a new event thread.
     */
    public void startNewEventThread()
    {
        // If another thread is running, add to stack
        if (_eventThread != null) {
            if (!isEventThread())
                throw new RuntimeException("EventQueue.startNewEventThreadAndWait: Attempt to start new event thread from unknown thread");
            _eventThreadStack.push(_eventThread);
        }

        // Create and start new event thread
        _eventThread = new EventQueueThread();
        _eventThread.start();
    }

    /**
     * Starts a new event thread.
     */
    public void startNewEventThreadAndWait()
    {
        EventQueueThread eventThread = _eventThread;
        startNewEventThread();

        // Wait last thread while new one is running
        eventThread.startWaiting();
    }

    /**
     * Stops a new event thread (after delay so this thread can finish).
     */
    public void stopEventThreadAndNotify()
    {
        setTimeout(this::stopEventThreadAndNotifyImpl, 0);
    }

    /**
     * Stops a new event thread.
     */
    private void stopEventThreadAndNotifyImpl()
    {
        _eventThread = _eventThreadStack.pop();
        _eventThread.stopWaiting();
    }

    /**
     * Waits for events and dispatches them.
     */
    private void eventLoop()
    {
        while (true) {

            // Wait for next event
            Object[] eventRecordArray = getNextEvent();

            // Process event
            try { processEvent(eventRecordArray); }

            // If exception is thrown, forward to UncaughtExceptionHandler or report
            catch (Throwable t) {
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
                if (uncaughtExceptionHandler != null)
                    uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t);
                else t.printStackTrace();
            }

            // If no longer main event thread, just return
            if (Thread.currentThread() != _eventThread)
                return;
        }
    }

    /**
     * This method decodes given event array and forwards to handler/listener associated with it.
     */
    private void processEvent(Object[] eventRecordArray)
    {
        // Get event type and function
        String type = (String) eventRecordArray[0];
        Object func = eventRecordArray[1];

        switch (type) {

            // Handle ANIMATION_EVENT
            case ANIMATION_EVENT:
                DoubleConsumer doubleConsumer = (DoubleConsumer) func;
                double timestamp = eventRecordArray[2] instanceof Number num ? num.doubleValue() : 0;
                doubleConsumer.accept(timestamp);
                break;

            // Handle INVOCATION_EVENT
            case INVOCATION_EVENT:
                Runnable run = (Runnable) func;
                run.run();
                break;

            // Handle KeyboardEvents
            case "keydown":
            case "keyup":
                EventListener<Event> keyLsnr = (EventListener<Event>) func;
                Object keyEventJS = eventRecordArray[2];
                Event keyEvent = new KeyboardEvent(keyEventJS);
                keyLsnr.handleEvent(keyEvent);
                break;

            // Handle MouseEvents
            case "mousedown":
            case "mousemove":
            case "mouseup":
            case "pointerdown":
            case "click":
            case "contextmenu":
                EventListener<Event> mouseLsnr = (EventListener<Event>) func;
                Object mouseEventJS = eventRecordArray[2];
                Event mouseEvent = new MouseEvent(mouseEventJS);
                mouseLsnr.handleEvent(mouseEvent);
                break;

            // Handle DragEvents
            case "dragenter":
            case "dragover":
            case "dragexit":
            case "drop":
            case "dragstart":
            case "dragend":
                EventListener<Event> dragLsnr = (EventListener<Event>) func;
                Object dragEventJS = eventRecordArray[2];
                Event dragEvent = new DragEvent(dragEventJS);
                dragLsnr.handleEvent(dragEvent);
                break;

            // Handle TouchEvents
            case "touchstart":
            case "touchmove":
            case "touchend":
                EventListener<Event> touchLsnr = (EventListener<Event>) func;
                Object touchEventJS = eventRecordArray[2];
                Event touchEvent = new TouchEvent(touchEventJS);
                touchLsnr.handleEvent(touchEvent);
                break;

            // Handle wheel events
            case "wheel":
                EventListener<Event> wheelLsnr = (EventListener<Event>) func;
                Object wheelJS = eventRecordArray[2];
                Event wheelEvent = new WheelEvent(wheelJS);
                wheelLsnr.handleEvent(wheelEvent);
                break;

            // Handle resize events
            case "load":
            case "loadend":
            case "resize":
            case "select":
            case "selectstart":
            case "selectend":
            case "focus":
            case "blur":
            case "change":
                EventListener<Event> eventLsnr = (EventListener<Event>) func;
                Object eventJS = eventRecordArray[2];
                Event event = new Event(eventJS);
                eventLsnr.handleEvent(event);
                break;

            // Handle promise
            case "promise":
                Function<JSObject,Object> promiseThenFunc = (Function<JSObject,Object>) func;
                JSObject value = (JSObject) eventRecordArray[2];
                promiseThenFunc.apply(value);
                break;

            // Handle Mutation
            case "mutation":
                MutationObserver.Callback callback = (MutationObserver.Callback) func;
                Object mutationRecordsArrayHolder = eventRecordArray[2];
                JSObject mutationRecordsArrayJS = (JSObject) WebEnv.get().getMember(mutationRecordsArrayHolder, "value");
                MutationRecord[] mutationRecords = MutationRecord.getMutationRecordArrayForArrayJS(mutationRecordsArrayJS);
                callback.handleMutations(mutationRecords);
                break;

            // Handle unknown
            default: System.out.println("EventQueue.eventLoop: Unknown event type: " + type);
        }
    }

    /**
     * Returns whether current thread is event thread.
     */
    public static boolean isEventThread()  { return Thread.currentThread() == _shared._eventThread; }

    /**
     * Returns the shared event queue.
     */
    public static EventQueue getShared()  { return _shared; }

    /**
     * Waits for next event.
     */
    private static native Object[] getNextEvent();

    /**
     * Request animation frame.
     */
    public static int requestAnimationFrame(DoubleConsumer callback)
    {
        return requestAnimationFrameImpl(ANIMATION_EVENT, callback);
    }

    /**
     * Sets a timeout.
     */
    public static void setTimeout(Runnable aRun, int aDelay)
    {
        setTimeoutImpl(INVOCATION_EVENT, aRun, aDelay);
    }

    /**
     * Sets an interval.
     */
    public static int setInterval(Runnable aRun, int aDelay)
    {
        return setIntervalImpl(INVOCATION_EVENT, aRun, aDelay);
    }

    /**
     * Registers an event handler of a specific event type on the EventTarget
     */
    public static void addEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture)
    {
        JSProxy jsproxy = (JSProxy) eventTarget;
        int lsnrId = System.identityHashCode(eventLsnr);
        addEventListenerImpl((JSObject) jsproxy.getJS(), aName, eventLsnr, lsnrId, useCapture);
    }

    /**
     * Removes an event handler of a specific event type from the EventTarget
     */
    public static void removeEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture)
    {
        JSProxy jsproxy = (JSProxy) eventTarget;
        int lsnrId = System.identityHashCode(eventLsnr);
        removeEventListenerImpl((JSObject) jsproxy.getJS(), aName, eventLsnr, lsnrId, useCapture);
    }

    /**
     * Sets a promise.then() function.
     */
    public static <T,V> Promise<V> setPromiseThen(Promise<T> aPromise, Function<? super T, ? extends V> aFunc)
    {
        Object promiseJS = aPromise.getJS();
        Object thenPromiseJS = setPromiseThenImpl((JSObject) promiseJS, aFunc);
        return new Promise<>(thenPromiseJS);
    }

    /**
     * EventQueue: requestAnimationFrameImpl().
     */
    private static native int requestAnimationFrameImpl(String aName, DoubleConsumer callback);

    /**
     * EventQueue: setTimeoutImpl().
     */
    private static native void setTimeoutImpl(String aName, Runnable aRun, int ms);

    /**
     * EventQueue: setIntervalImpl().
     */
    private static native int setIntervalImpl(String aName, Runnable aRun, int aDelay);

    /**
     * EventQueue: addEventListenerImpl().
     */
    private static native void addEventListenerImpl(JSObject eventTargetJS, String aName, EventListener<?> eventLsnr, int lsnrId, boolean useCapture);

    /**
     * EventQueue: removeEventListenerImpl().
     */
    private static native void removeEventListenerImpl(JSObject eventTargetJS, String aName, EventListener<?> eventLsnr, int lsnrId, boolean useCapture);

    /**
     * EventQueue: setPromiseThenImpl().
     */
    private static native JSObject setPromiseThenImpl(JSObject promiseJS, Function<?,?> aFunc);

    /**
     * This thread subclass is used to get and process events.
     */
    private class EventQueueThread extends Thread {

        public EventQueueThread()
        {
            super(EventQueue.this::eventLoop);
        }

        public synchronized void startWaiting()
        {
            try { wait(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }

        public synchronized void stopWaiting()
        {
            notify();
        }
    }
}
