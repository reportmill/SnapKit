package snap.webenv;
import snap.webapi.*;
import netscape.javascript.JSObject;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

/**
 * The web environment for CheerpJ.
 */
public class CJWebEnv extends WebEnv<JSObject> {

    // The current Window
    private Window _window;

    // The current Console
    private Console _console;

    /**
     * Constructor.
     */
    public CJWebEnv()
    {
        super();
    }

    /**
     * Returns a named member of a JavaScript object.
     */
    public JSObject getMember(JSObject jsObj, String aName)  { return getMemberImpl(jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMember(JSObject jsObj, String aName, JSObject aValue)  { setMemberImpl(jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object.
     */
    public String getMemberString(JSObject jsObj, String aName)  { return getMemberStringImpl(jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberString(JSObject jsObj, String aName, String aValue)  { setMemberStringImpl(jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object.
     */
    public boolean getMemberBoolean(JSObject jsObj, String aName)  { return getMemberBooleanImpl(jsObj, aName) > 0; }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberBoolean(JSObject jsObj, String aName, boolean aValue)  { setMemberBooleanImpl(jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object as int.
     */
    public int getMemberInt(JSObject jsObj, String aName)  { return getMemberIntImpl(jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberInt(JSObject jsObj, String aName, int aValue)  { setMemberIntImpl(jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object as double.
     */
    public double getMemberDouble(JSObject jsObj, String aName)  { return getMemberDoubleImpl(jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object as double.
     */
    public void setMemberDouble(JSObject jsObj, String aName, double aValue)  { setMemberDoubleImpl(jsObj, aName, aValue); }

    /**
     * Calls a method.
     */
    public Object call(JSObject jsObj, String aName, Object... args)  { return jsObj.call(aName, args); }

    /**
     * Returns an indexed member of a JavaScript object.
     */
    @Override
    public Object getSlot(JSObject jsObj, int anIndex)  { return getSlotImpl(jsObj, anIndex); }

    /**
     * Sets an indexed member of a JavaScript object.
     */
    @Override
    public void setSlot(JSObject jsObj, int anIndex, Object aValue)  { setSlotImpl(jsObj, anIndex, aValue); }

    /**
     * Evaluates given JavaScript string and returns result.
     */
    @Override
    public Object eval(String javaScript)
    {
        JSObject windowJS = (JSObject) window().getJS();
        return windowJS.eval(javaScript);
    }

    /**
     * Returns the current window.
     */
    public Window window()
    {
        if (_window != null) return _window;
        JSObject winJS = windowImpl();
        return _window = new Window(winJS);
    }

    /**
     * Returns the current console.
     */
    public Console console()
    {
        if (_console != null) return _console;
        JSObject consoleJS = consoleImpl();
        return _console = new Console(consoleJS);
    }

    /**
     * Returns a new JavaScript native object.
     */
    public JSObject newObject()  { return newObjectImpl(); }

    /**
     * Does await promise for given promise.
     */
    public Object awaitForPromise(JSObject aPromise)  { return awaitForPromiseImpl(aPromise); }

    /**
     * Request animation frame.
     */
    public int requestAnimationFrame(DoubleConsumer callback)
    {
        return EventQueue.requestAnimationFrame(callback);
    }

    /**
     * Schedules a runnable to execute after a delay of given milliseconds.
     */
    public void setTimeout(Runnable aRun, int aDelay)
    {
        EventQueue.setTimeout(aRun, aDelay);
    }

    /**
     * Schedules a runnable to execute every time a given number of milliseconds elapses.
     */
    public int setInterval(Runnable aRun, int aPeriod)
    {
        //java.util.TimerTask timerTask = new TimerTask() { public void run()  { moveBalls(); } };
        //new java.util.Timer().schedule(timerTask, 0, 25);
        return EventQueue.setInterval(aRun, aPeriod);
    }

    /**
     * Override to handle Safari special.
     */
    @Override
    public void click(JSObject jsObj)
    {
        if (Navigator.isSafari())
            setNeedsClickElement(jsObj);
        else super.click(jsObj);
    }

    /**
     * Returns an array of given length.
     */
    @Override
    public JSObject newArrayJSForLength(int aLength)  { return newArrayJSForLengthImpl(aLength); }

    /**
     * Returns an array of bytes for given array buffer.
     */
    @Override
    public byte[] getBytesArrayForArrayBufferJS(JSObject arrayBufferJS)
    {
        return getBytesArrayForArrayBufferJSImpl(arrayBufferJS);
    }

    /**
     * Returns an array of bytes for this array.
     */
    @Override
    public byte[] getBytesArrayForTypedArrayJS(JSObject typedArrayJS)
    {
        return getBytesArrayForTypedArrayJSImpl(typedArrayJS);
    }

    /**
     * Returns an array of shorts for this array.
     */
    @Override
    public short[] getShortsArrayForTypedArrayJS(JSObject typedArrayJS)
    {
        return getShortsArrayForTypedArrayJSImpl(typedArrayJS);
    }

    /**
     * Returns an array of shorts for this array.
     */
    @Override
    public short[] getShortsArrayForChannelIndexAndCount(JSObject typedArrayJS, int channelIndex, int channelCount)
    {
        return getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl(typedArrayJS, channelIndex, channelCount);
    }

    /**
     * Returns a typed array of given class for given input.
     */
    @Override
    public JSObject getTypedArrayJSForClassAndObject(Class<?> aClass, Object arrayObject)
    {
        if (aClass == Int8Array.class)
            return getInt8ArrayForObject(arrayObject);
        if (aClass == Int16Array.class)
            return getInt16ArrayForObject(arrayObject);
        if (aClass == Float32Array.class)
            return getFloat32ArrayForObject(arrayObject);
        if (aClass == Uint16Array.class)
            return getUint16ArrayForObject(arrayObject);
        if (aClass == Uint8ClampedArray.class)
            return getUint8ClampedArrayForObject(arrayObject);
        throw new RuntimeException("CJWebEnv: No support for class " + aClass.getName());
    }

    /**
     * Returns new ImageData for given short array of RGBA color components and width and height.
     */
    @Override
    public JSObject newImageDataJSForRgbaArrayAndWidthAndHeight(Object arrayObject, int aWidth, int aHeight)
    {
        return newImageDataJSForRgbaArrayAndWidthAndHeightImpl(arrayObject, aWidth, aHeight);
    }

    /**
     * Returns a new Blob for given byte array and type.
     */
    @Override
    public JSObject newBlobJSForBytesAndType(byte[] byteArray, String aType)
    {
        return newBlobJSForBytesAndTypeImpl(byteArray, aType);
    }

    /**
     * Creates a URL for given blob.
     */
    @Override
    public String createUrlForBlobJS(JSObject blobJS)  { return createUrlForBlobJSImpl(blobJS); }

    /**
     * Returns a new File for given name, type and bytes .
     */
    @Override
    public JSObject newFileJSForNameAndTypeAndBytes(String aName, String aType, byte[] byteArray)
    {
        return newFileJSForNameAndTypeAndBytesImpl(aName, aType, byteArray);
    }

    /**
     * Returns a new FileReader.
     */
    @Override
    public JSObject newFileReaderJS()  { return newFileReaderJSImpl(); }

    /**
     * Returns a new MutationObserver.
     */
    @Override
    public JSObject newMutationObserver(MutationObserver.Callback aCallback)  { return newMutationObserverImpl(aCallback); }

    /**
     * Registers a mutation observer to observe given node for given mutations types object.
     */
    @Override
    public void addMutationObserver(MutationObserver mutationObserver, Node aNode, Object optionsObjJS)
    {
        MutationObserver.Callback callback = mutationObserver.getCallback();
        addMutationObserverImpl((JSObject) mutationObserver.getJS(), (JSObject) aNode.getJS(), callback, (JSObject) optionsObjJS);
    }

    /**
     * Returns a new ClipboardItem for given mime type and data string.
     */
    @Override
    public Object newClipboardItemForMimeTypeAndDataString(String mimeType, String dataString)
    {
        return newClipboardItemForMimeTypeAndDataStringImpl(mimeType, dataString);
    }

    /**
     * Returns a new ClipboardItem for given Blob JS.
     */
    @Override
    public Object newClipboardItemForBlobJS(Object blobJS)  { return newClipboardItemForBlobImpl((JSObject) blobJS); }

    /**
     * Returns an array of ClipboardItem JavaScript objects from clipboard.
     */
    @Override
    public Object[] readClipboardItemsJS()  { return readClipboardItemsImpl(); }

    /**
     * Writes a given JavaScript array of ClipboardItem JavaScript objects to clipboard.
     */
    @Override
    public void writeClipboardItemsJS(Object clipboardItemsJS)  { writeClipboardItemsImpl((JSObject) clipboardItemsJS); }

    /**
     * Registers an event handler of a specific event type on the EventTarget.
     */
    @Override
    public void addEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture)
    {
        EventQueue.addEventListener(eventTarget, aName, eventLsnr, useCapture);
    }

    /**
     * Removes an event handler of a specific event type from the EventTarget.
     */
    @Override
    public void removeEventListener(EventTarget eventTarget, String aName, EventListener<?> eventLsnr, boolean useCapture)
    {
        EventQueue.removeEventListener(eventTarget, aName, eventLsnr, useCapture);
    }

    /**
     * Registers an event handler of a specific event type on the EventTarget.
     */
    @Override
    public void addLoadEventListener(EventTarget eventTarget, EventListener<?> eventLsnr)
    {
        LoadEventQueue.addLoadEventListener(eventTarget, eventLsnr);
    }

    /**
     * Returns whether current thread is event thread.
     */
    @Override
    public boolean isEventThread()  { return EventQueue.isEventThread(); }

    /**
     * Starts a new event thread.
     */
    @Override
    public void startNewEventThreadAndWait()  { EventQueue.getShared().startNewEventThreadAndWait(); }

    /**
     * Stops a new event thread (after delay so this thread can finish).
     */
    @Override
    public void stopEventThreadAndNotify()  { EventQueue.getShared().stopEventThreadAndNotify(); }

    /**
     * Sets a promise.then() function.
     */
    @Override
    public <T,V> Promise<V> setPromiseThen(Promise<T> aPromise, Function<? super T, ? extends V> aFunc)
    {
        return EventQueue.setPromiseThen(aPromise, aFunc);
    }

    /**
     * Returns a new DataTransfer for given DataTransfer JavaScript object.
     */
    @Override
    public DataTransfer newDataTransferForDataTransferJS(JSObject jsObj)  { return new CJDataTransfer(jsObj); }

    /**
     * Returns the DataTransfer from last drop.
     */
    @Override
    public DataTransfer getDropDataTransfer()  { return CJDataTransfer.getDropDataTransfer(); }

    /**
     * Returns the rendering context object for given type string and JavaScript object.
     */
    public Object getRenderingContext(String contextType, JSObject jsObj)
    {
        if (contextType.equals("2d"))
            return new CJCanvasRenderingContext2D(jsObj);
        return new WebGLRenderingContext(jsObj);
    }

    /**
     * CJWebEnv method: getMemberImpl()
     */
    private static native JSObject getMemberImpl(JSObject jsObj, String aName);

    /**
     * CJWebEnv method: setMemberImpl()
     */
    private static native void setMemberImpl(JSObject jsObj, String aName, JSObject aValue);

    /**
     * CJWebEnv method: getMemberImpl()
     */
    private static native String getMemberStringImpl(JSObject jsObj, String aName);

    /**
     * CJWebEnv method: setMemberStringImpl()
     */
    private static native void setMemberStringImpl(JSObject jsObj, String aName, String aValue);

    /**
     * CJWebEnv method: getMemberBooleanImpl(). Sometimes problem returning boolean?
     */
    private static native int getMemberBooleanImpl(JSObject jsObj, String aName);

    /**
     * CJWebEnv method: setMemberBooleanImpl()
     */
    private static native void setMemberBooleanImpl(JSObject jsObj, String aName, boolean aValue);

    /**
     * CJWebEnv method: getMemberIntImpl()
     */
    private static native int getMemberIntImpl(JSObject jsObj, String aName);

    /**
     * CJWebEnv method: setMemberIntImpl()
     */
    private static native void setMemberIntImpl(JSObject jsObj, String aName, int aValue);

    /**
     * CJWebEnv method: getMemberDoubleImpl()
     */
    private static native double getMemberDoubleImpl(JSObject jsObj, String aName);

    /**
     * CJWebEnv method: setMemberDoubleImpl()
     */
    private static native void setMemberDoubleImpl(JSObject jsObj, String aName, double aValue);

    /**
     * CJWebEnv: getSlotImpl().
     */
    private static native Object getSlotImpl(JSObject jsObj, int index);

    /**
     * CJWebEnv: setSlotImpl().
     */
    private static native void setSlotImpl(JSObject jsObj, int index, Object aValue);

    /**
     * Returns the current window.
     */
    private static native JSObject windowImpl();

    /**
     * Returns the current console.
     */
    private static native JSObject consoleImpl();

    /**
     * CJWebEnv: newObjectImpl()
     */
    private static native JSObject newObjectImpl();

    /**
     * CJWebEnv method: awaitForPromiseImpl().
     */
    private static native Object awaitForPromiseImpl(JSObject aPromise);

    /**
     * CJWebEnv method: setNeedsClickElement(): Called to set an element that needs a click.
     */
    private static native void setNeedsClickElement(JSObject needsClickElement);

    /**
     * CJWebEnv: getBytesArrayForArrayBufferJSImpl()
     */
    private static native byte[] getBytesArrayForArrayBufferJSImpl(JSObject typedArrayJS);

    /**
     * CJWebEnv: getBytesArrayForTypedArrayJSImpl()
     */
    private static native byte[] getBytesArrayForTypedArrayJSImpl(JSObject typedArrayJS);

    /**
     * CJWebEnv: getShortsArrayForTypedArrayJSImpl()
     */
    private static native short[] getShortsArrayForTypedArrayJSImpl(JSObject typedArrayJS);

    /**
     * CJWebEnv: getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl()
     */
    private static native short[] getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl(JSObject typedArrayJS, int channelIndex, int channelCount);

    /**
     * CJWebEnv: newArrayForLengthImpl().
     */
    private static native JSObject newArrayJSForLengthImpl(int aLength);

    /**
     * CJWebEnv: getInt8ArrayForObject().
     */
    private static native JSObject getInt8ArrayForObject(Object arrayObject);

    /**
     * CJWebEnv: getInt16ArrayForObject().
     */
    private static native JSObject getInt16ArrayForObject(Object arrayObject);

    /**
     * CJWebEnv: getFloat32ArrayForObject().
     */
    private static native JSObject getFloat32ArrayForObject(Object arrayObject);

    /**
     * CJWebEnv: getUint16ArrayForObject().
     */
    private static native JSObject getUint16ArrayForObject(Object arrayObject);

    /**
     * CJWebEnv: getUint8ClampedArrayForObject().
     */
    private static native JSObject getUint8ClampedArrayForObject(Object arrayObject);

    /**
     * CJWebEnv: newImageDataJSForRgbaArrayAndWidthAndHeightImpl().
     */
    private static native JSObject newImageDataJSForRgbaArrayAndWidthAndHeightImpl(Object arrayObject, int aWidth, int aHeight);

    /**
     * CJWebEnv: newBlobJSForBytesAndTypeImpl().
     */
    private static native JSObject newBlobJSForBytesAndTypeImpl(byte[] byteArray, String aType);

    /**
     * CJWebEnv: createUrlForBlobJSImpl().
     */
    private static native String createUrlForBlobJSImpl(JSObject blobJS);

    /**
     * CJWebEnv: newFileJSForNameAndTypeAndBytesImpl().
     */
    private static native JSObject newFileJSForNameAndTypeAndBytesImpl(String aName, String aType, byte[] byteArray);

    /**
     * CJWebEnv: newFileReaderJSImpl().
     */
    private static native JSObject newFileReaderJSImpl();

    /**
     * CJWebEnv: newMutationObserverImpl().
     */
    private static native JSObject newMutationObserverImpl(MutationObserver.Callback aCallback);

    /**
     * CJWebEnv: addMutationObserverImpl().
     */
    private static native void addMutationObserverImpl(JSObject mutationObserverJS, JSObject nodeJS, MutationObserver.Callback callback, JSObject optionsObj);

    /**
     * CJWebEnv: newClipboardItemForMimeTypeAndDataStringImpl()
     */
    private static native JSObject newClipboardItemForMimeTypeAndDataStringImpl(String mimeType, String dataString);

    /**
     * CJWebEnv: newClipboardItemForBlobImpl()
     */
    private static native JSObject newClipboardItemForBlobImpl(JSObject blobJS);

    /**
     * CJWebEnv: readClipboardItemsImpl()
     */
    private static native Object[] readClipboardItemsImpl();

    /**
     * CJWebEnv: writeClipboardItemsImpl()
     */
    private static native void writeClipboardItemsImpl(JSObject clipboardItemsJS);
}
