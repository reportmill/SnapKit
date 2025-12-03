package snap.webapi;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

/**
 * This class is an adapter for this package, implementing the fundamental functionality to make it work with
 * CheerpJ, TeaVM, JxBrowser.
 */
public abstract class WebEnv<T> {

    // The shared Env
    private static final WebEnv<Object> _shared = (WebEnv<Object>) createEnv();

    /**
     * Returns a shared instance.
     */
    public static WebEnv<Object> get()  { return _shared; }

    /**
     * Returns a shared instance.
     */
    private static WebEnv<?> createEnv()
    {
        String javaVendor = System.getProperty("java.vendor");
        if (javaVendor.contains("Leaning"))
            return new snap.webenv.CJWebEnv();
        return null; //new JxWebEnv();
    }

    /**
     * Returns a named member of a JavaScript object.
     */
    public abstract T getMember(T jsObj, String aName);

    /**
     * Sets a named member of a JavaScript object.
     */
    public abstract void setMember(T jsObj, String aName, T aValue);

    /**
     * Returns a named member of a JavaScript object.
     */
    public abstract String getMemberString(T jsObj, String aName);

    /**
     * Sets a named member of a JavaScript object.
     */
    public abstract void setMemberString(T jsObj, String aName, String aValue);

    /**
     * Returns a named member of a JavaScript object.
     */
    public abstract boolean getMemberBoolean(T jsObj, String aName);

    /**
     * Sets a named member of a JavaScript object.
     */
    public abstract void setMemberBoolean(T jsObj, String aName, boolean aValue);

    /**
     * Returns a named member of a JavaScript object as int.
     */
    public abstract int getMemberInt(T jsObj, String aName);

    /**
     * Sets a named member of a JavaScript object.
     */
    public abstract void setMemberInt(T jsObj, String aName, int aValue);

    /**
     * Returns a named member of a JavaScript object as double.
     */
    public abstract double getMemberDouble(T jsObj, String aName);

    /**
     * Sets a named member of a JavaScript object as double.
     */
    public abstract void setMemberDouble(T jsObj, String aName, double aValue);

    /**
     * Calls a method.
     */
    public abstract Object call(T jsObj, String aName, Object... args);

    /**
     * Returns an indexed member of a JavaScript object.
     */
    public abstract Object getSlot(T jsObj, int anIndex);

    /**
     * Sets an indexed member of a JavaScript object.
     */
    public abstract void setSlot(T jsObj, int anIndex, Object aValue);

    /**
     * Evaluates given JavaScript string and returns result.
     */
    public abstract Object eval(String javaScript);

    /**
     * Returns the current window.
     */
    public abstract Window window();

    /**
     * Returns the current console.
     */
    public abstract Console console();

    /**
     * Returns a new JavaScript native object.
     */
    public T newObject()  { return (T) eval("new Object();"); }

    /**
     * Does await promise for given promise.
     */
    public abstract Object awaitForPromise(T aPromise);

    /**
     * Wrapper method for Web API method.
     */
    public void open(String url, String target, String windowFeatures)
    {
        Window window = get().window();
        window.call("open", url, target, windowFeatures);
    }

    /**
     * Request animation frame.
     */
    public abstract int requestAnimationFrame(DoubleConsumer callback);

    /**
     * Schedules a runnable to execute after a delay of given milliseconds.
     */
    public abstract void setTimeout(Runnable aRun, int aDelay);

    /**
     * Schedules a runnable to execute every time a given number of milliseconds elapses.
     */
    public abstract int setInterval(Runnable aRun, int aPeriod);

    /**
     * Stops intervals for given id.
     */
    public void clearInterval(int anId)
    {
        Window window = get().window();
        window.call("clearInterval", anId);
    }

    /**
     * Performs click on given HTMLElement JS.
     */
    public void click(T jsObj)  { call(jsObj, "click"); }

    /**
     * Returns an array of given length.
     */
    public abstract T newArrayJSForLength(int aLength);

    /**
     * Returns an array of bytes for given array buffer.
     */
    public abstract byte[] getBytesArrayForArrayBufferJS(T arrayBufferJS);

    /**
     * Returns an array of bytes for given typed array.
     */
    public abstract byte[] getBytesArrayForTypedArrayJS(T typedArrayJS);

    /**
     * Returns an array of shorts for given typed array.
     */
    public abstract short[] getShortsArrayForTypedArrayJS(T typedArrayJS);

    /**
     * Returns an array of shorts for given typed array.
     */
    public abstract short[] getShortsArrayForChannelIndexAndCount(T typedArrayJS, int channelIndex, int channelCount);

    /**
     * Returns a typed array of given class for given input.
     */
    public abstract T getTypedArrayJSForClassAndObject(Class<?> aClass, Object arrayObject);

    /**
     * Returns new ImageData for given short array of RGBA color components and width and height.
     */
    public abstract T newImageDataJSForRgbaArrayAndWidthAndHeight(Object arrayObject, int aWidth, int aHeight);

    /**
     * Returns a new Blob for given byte array and type.
     */
    public abstract T newBlobJSForBytesAndType(byte[] byteArray, String aType);

    /**
     * Creates a URL for given blob.
     */
    public abstract String createUrlForBlobJS(T blobJS);

    /**
     * Returns a new File for given name, type and bytes.
     */
    public abstract T newFileJSForNameAndTypeAndBytes(String aName, String aType, byte[] byteArray);

    /**
     * Returns a new FileReader.
     */
    public T newFileReaderJS()  { return (T) eval("new FileReader();"); }

    /**
     * Returns a new MutationObserver.
     */
    public abstract T newMutationObserver(MutationObserver.Callback aCallback);

    /**
     * Registers a mutation observer to observe given node for given mutations types object.
     */
    public abstract void addMutationObserver(MutationObserver mutationObserver, Node aNode, Object optionsObjJS);

    /**
     * Returns a new ClipboardItem for given mime type and data string.
     */
    public abstract Object newClipboardItemForMimeTypeAndDataString(String mimeType, String dataString);

    /**
     * Returns a new ClipboardItem for given Blob JS.
     */
    public abstract Object newClipboardItemForBlobJS(Object blobJS);

    /**
     * Returns an array of ClipboardItem JavaScript objects from clipboard.
     */
    public abstract Object[] readClipboardItemsJS();

    /**
     * Writes a given JavaScript array of ClipboardItem JavaScript objects to clipboard.
     */
    public abstract void writeClipboardItemsJS(Object clipboardItemsJS);

    /**
     * Registers an event handler of a specific event type on the EventTarget.
     */
    public abstract void addEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture);

    /**
     * Removes an event handler of a specific event type from the EventTarget.
     */
    public abstract void removeEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture);

    /**
     * Registers an event handler of a specific event type on the EventTarget.
     */
    public void addLoadEventListener(EventTarget eventTarget, EventListener<?> eventLsnr)
    {
        addEventListener(eventTarget, "load", eventLsnr, false);
    }

    /**
     * Returns whether current thread is event thread.
     */
    public abstract boolean isEventThread();

    /**
     * Starts a new event thread.
     */
    public abstract void startNewEventThreadAndWait();

    /**
     * Stops a new event thread (after delay so this thread can finish).
     */
    public abstract void stopEventThreadAndNotify();

    /**
     * Sets a promise.then() function.
     */
    public abstract <T,V> Promise<V> setPromiseThen(Promise<T> aPromise, Function<? super T, ? extends V> aFunc);

    /**
     * Returns a new DataTransfer for given DataTransfer JavaScript object.
     */
    public DataTransfer newDataTransfer()  { return newDataTransferForDataTransferJS(null); }

    /**
     * Returns a new DataTransfer for given DataTransfer JavaScript object.
     */
    public abstract DataTransfer newDataTransferForDataTransferJS(T jsObj);

    /**
     * Returns the DataTransfer from last drop.
     */
    public abstract DataTransfer getDropDataTransfer();

    /**
     * Returns the rendering context object for given type string and JavaScript object.
     */
    public abstract Object getRenderingContext(String contextType, T jsObj);
}
