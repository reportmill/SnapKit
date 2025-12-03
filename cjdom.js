

/**
 * CJWebEnv method: getMemberImpl()
 */
function Java_snap_webenv_CJWebEnv_getMemberImpl(lib, jsObj, aName)  { return jsObj[aName]; }

/**
 * CJWebEnv method: setMemberImpl()
 */
function Java_snap_webenv_CJWebEnv_setMemberImpl(lib, jsObj, aName, aValue)  { jsObj[aName] = aValue; }

/**
 * CJWebEnv method: getMemberStringImpl()
 */
function Java_snap_webenv_CJWebEnv_getMemberStringImpl(lib, jsObj, aName)  { return jsObj[aName]; }

/**
 * CJWebEnv method: setMemberStringImpl()
 */
function Java_snap_webenv_CJWebEnv_setMemberStringImpl(lib, jsObj, aName, aValue)  { jsObj[aName] = aValue; }

/**
 * CJWebEnv method: getMemberBooleanImpl()
 */
function Java_snap_webenv_CJWebEnv_getMemberBooleanImpl(lib, jsObj, aName)  { return jsObj[aName] ? 1 : 0; }

/**
 * CJWebEnv method: setMemberBooleanImpl()
 */
function Java_snap_webenv_CJWebEnv_setMemberBooleanImpl(lib, jsObj, aName, aValue)  { jsObj[aName] = aValue; }

/**
 * CJWebEnv method: getMemberIntImpl()
 */
function Java_snap_webenv_CJWebEnv_getMemberIntImpl(lib, jsObj, aName)  { return jsObj[aName]; }

/**
 * CJWebEnv method: setMemberIntImpl()
 */
function Java_snap_webenv_CJWebEnv_setMemberIntImpl(lib, jsObj, aName, aValue)  { jsObj[aName] = aValue; }

/**
 * CJWebEnv method: getMemberDoubleImpl()
 */
function Java_snap_webenv_CJWebEnv_getMemberDoubleImpl(lib, jsObj, aName)  { return jsObj[aName]; }

/**
 * CJWebEnv method: setMemberDoubleImpl()
 */
function Java_snap_webenv_CJWebEnv_setMemberDoubleImpl(lib, jsObj, aName, aValue)  { jsObj[aName] = aValue; }

/**
 * CJWebEnv: getSlotImpl()
 */
function Java_snap_webenv_CJWebEnv_getSlotImpl(lib, array, index)  { return array[index]; }

/**
 * CJWebEnv: setSlotImpl()
 */
function Java_snap_webenv_CJWebEnv_setSlotImpl(lib, array, index, aValue)  { array[index] = aValue; }

/**
 * CJWebEnv: windowImpl().
 */
function Java_snap_webenv_CJWebEnv_windowImpl(lib, anObj)  { return window; }

/**
 * CJWebEnv: consoleImpl().
 */
function Java_snap_webenv_CJWebEnv_consoleImpl(lib, anObj)  { return console; }

/**
 * CJWebEnv: newObjectImpl()
 */
function Java_snap_webenv_CJWebEnv_newObjectImpl(lib)  { return { }; }

/**
 * CJWebEnv method: awaitForPromiseImpl()
 */
async function Java_snap_webenv_CJWebEnv_awaitForPromiseImpl(lib, promiseJS)  { return await promiseJS; }

// An element that needs a click
var _needsClickElement;

/**
 * CJWebEnv setNeedsClickElement(): Called to set an element that needs a click.
 */
function Java_snap_webenv_CJWebEnv_setNeedsClickElement(lib, needsClickElement)  { _needsClickElement = needsClickElement; }

/**
 * CJWebEnv: newArrayJSForLengthImpl().
 */
function Java_snap_webenv_CJWebEnv_newArrayJSForLengthImpl(lib, arrayLength)  { return new Array(arrayLength); }

/**
 * CJWebEnv: getBytesArrayForArrayBufferJSImpl().
 */
function Java_snap_webenv_CJWebEnv_getBytesArrayForArrayBufferJSImpl(lib, arrayBufferJS)  { return new Int8Array(arrayBufferJS); }

/**
 * CJWebEnv: getBytesArrayForTypedArrayJSImpl().
 */
function Java_snap_webenv_CJWebEnv_getBytesArrayForTypedArrayJSImpl(lib, typedArrayJS)  { return Int8Array.from(typedArrayJS); }

/**
 * CJWebEnv: getShortsArrayForTypedArrayJSImpl().
 */
function Java_snap_webenv_CJWebEnv_getShortsArrayForTypedArrayJSImpl(lib, typedArrayJS)  { return Int16Array.from(typedArrayJS); }

/**
 * CJWebEnv: getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl().
 */
function Java_snap_webenv_CJWebEnv_getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl(lib, typedArrayJS, channelIndex, channelCount)
{
    var length = typedArrayJS.length / channelCount;
    var int16_Array = new Int16Array(length);
    for (let i = 0, j = channelIndex; i < length; i++, j = j + channelCount)
        int16_Array[i + 1] = typedArrayJS[j];
    return int16_Array;
}

/**
 * CJWebEnv: getInt8ArrayForObject().
 */
function Java_snap_webenv_CJWebEnv_getInt8ArrayForObject(lib, arrayObj)
{
    return arrayObj instanceof Int8Array ? arrayObj : new Int8Array(arrayObj);
}

/**
 * CJWebEnv: getInt16ArrayForObject().
 */
function Java_snap_webenv_CJWebEnv_getInt16ArrayForObject(lib, arrayObj)
{
    return arrayObj instanceof Int16Array ? arrayObj : new Int16Array(arrayObj);
}

/**
 * CJWebEnv: getFloat32ArrayForObject().
 */
function Java_snap_webenv_CJWebEnv_getFloat32ArrayForObject(lib, arrayObj)
{
    return arrayObj instanceof Float32Array ? arrayObj : new Float32Array(arrayObj);
}

/**
 * CJWebEnv: getUint16ArrayForObject().
 */
function Java_snap_webenv_CJWebEnv_getUint16ArrayForObject(lib, arrayObj)
{
    return arrayObj instanceof Uint16Array ? arrayObj : new Uint16Array(arrayObj);
}

/**
 * CJWebEnv: getUint8ClampedArrayForObject().
 */
function Java_snap_webenv_CJWebEnv_getUint8ClampedArrayForObject(lib, arrayObj)
{
    return arrayObj instanceof Uint8ClampedArray ? arrayObj : new Uint8ClampedArray(arrayObj);
}

/**
 * CJWebEnv: newImageDataJSForRgbaArrayAndWidthAndHeightImpl().
 */
function Java_snap_webenv_CJWebEnv_newImageDataJSForRgbaArrayAndWidthAndHeightImpl(lib, arrayObj, aWidth, aHeight)
{
    const uint8ClampedArrayJS = arrayObj instanceof Uint8ClampedArray ? arrayObj : new Uint8ClampedArray(arrayObj);
    return new ImageData(uint8ClampedArrayJS, aWidth, aHeight);
}

/**
 * CJWebEnv: Creates a Blob from given bytes in JS.
 */
function Java_snap_webenv_CJWebEnv_newBlobJSForBytesAndTypeImpl(lib, arrayObj, typeStr)
{
    const int8Array = arrayObj instanceof Int8Array ? arrayObj : new Int8Array(arrayObj);
    return new Blob([ int8Array ], typeStr ? { type: typeStr } : null);
}

/**
 * CJWebEnv: createUrlForBlobJSImpl().
 */
function Java_snap_webenv_CJWebEnv_createUrlForBlobJSImpl(lib, blobJS)  { return URL.createObjectURL(blobJS); }

/**
 * CJWebEnv: newFileJSForNameAndTypeAndBytesImpl().
 */
function Java_snap_webenv_CJWebEnv_newFileJSForNameAndTypeAndBytesImpl(lib, name, type, arrayObj)
{
    const int8Array = arrayObj instanceof Int8Array ? arrayObj : new Int8Array(arrayObj);
    return new File([ int8Array ], name, type ? { type: type } : null);
}

/**
 * CJWebEnv: newFileReaderJSImpl().
 */
function Java_snap_webenv_CJWebEnv_newFileReaderJSImpl(lib)  { return new FileReader(); }

/**
 * CJWebEnv: newMutationObserverImpl().
 */
function Java_snap_webenv_CJWebEnv_newMutationObserverImpl(lib, aCallback)
{
    return new MutationObserver((mutationRecords, observer) => mutationObserved(aCallback, mutationRecords));
}

/**
 * CJWebEnv: addMutationObserverImpl().
 */
function Java_snap_webenv_CJWebEnv_addMutationObserverImpl(lib, mutationObserverJS, nodeJS, callback, optionsObj)
{
    mutationObserverJS.observe(nodeJS, optionsObj);
}

/**
 * CJWebEnv: newClipboardItemForMimeTypeAndDataStringImpl()
 */
function Java_snap_webenv_CJWebEnv_newClipboardItemForMimeTypeAndDataStringImpl(lib, type, string)
{
    var blob = new Blob([ string ], { type });
    var entry = { [blob.type]: blob };
    return new ClipboardItem(entry);
}

/**
 * CJWebEnv: newClipboardItemForBlobImpl()
 */
function Java_snap_webenv_CJWebEnv_newClipboardItemForBlobImpl(lib, blob)
{
    var entry = { [blob.type]: blob };
    return new ClipboardItem(entry);
}

// Clipboard.read() ClipboardItems - read upon meta+v key press, write() upon meta-c
var clipboardReadItems;
var clipboardWriteItems;

/**
 * CJWebEnv: readClipboardItemsImpl()
 */
async function Java_snap_webenv_CJWebEnv_readClipboardItemsImpl(lib)
{
    // If clipboardReadItems set, clear and return
    if (clipboardReadItems != null) {
        var temp = clipboardReadItems;
        clipboardReadItems = null;
        return temp;
    }

    // Try to read items
    try {
        var clipboardReadPromise = navigator.clipboard.read()
            .catch((e) => { console.log("Clipboard.readClipboardItemsImpl: Ignoring error: " + e); return [ ]; });
        return await clipboardReadPromise;
    }

    // Can happen on Safari iOS with localhost
    catch (e) { console.log("Clipboard.readClipboardItemsImpl:" + e); return [ ]; }
}

/**
 * CJWebEnv: writeClipboardItemsImpl().
 */
async function Java_snap_webenv_CJWebEnv_writeClipboardItemsImpl(lib, clipboardItems)
{
    // Will fail on Safari because this is not directly triggered from user event
    try {
        navigator.clipboard.write(clipboardItems).catch((e) => clipboardWriteItems = clipboardItems);
        clipboardReadItems = clipboardItems;
    }

    // Can happen on Safari iOS with localhost
    catch (e) { console.log("Clipboard.writeClipboardItemsImpl:" + e); }
}

/**
 * delayedClipboardWrite(): Called a moment after meta+C to try to write lingering clipboard items.
 */
function delayedClipboardWrite()
{
    if (clipboardWriteItems != null) {
        try { navigator.clipboard.write(clipboardWriteItems); }
        catch (e) { console.log("delayedClipboardWrite:" + e); }
        clipboardReadItems = clipboardWriteItems;
        clipboardWriteItems = null;
    }

    // If NeedsClickElement is set, tell it to click (Safari)
    if (_needsClickElement != null) {
        _needsClickElement.click();
        _needsClickElement = null;
    }
}

/**
 * eagerClipboardRead(): Called on meta+V (paste) to try to read items.
 */
async function eagerClipboardRead()
{
    try {
        clipboardReadItems = await navigator.clipboard.read().catch((e) => console.log("Ignoring: " + e));
    }

    // Can happen on Safari iOS with localhost
    catch (e) { console.log("eagerClipboardRead:" + e); }
}

/**
 * CJDataTransfer: newDataTransfer().
 */
function Java_snap_webenv_CJDataTransfer_newDataTransfer(lib)  { return new DataTransfer(); }

// Cached drag drop data
var _dragDataTransferTypes;
var _dropDataTransfer;
var _dropDataTransferFiles

/**
 * CJDataTransfer: getDropDataTransferImpl().
 */
function Java_snap_webenv_CJDataTransfer_getDropDataTransferImpl(lib)  { return _dropDataTransfer; }

/**
 * CJDataTransfer: getDropDataTransferTypesImpl().
 */
function Java_snap_webenv_CJDataTransfer_getDropDataTransferTypesImpl(lib)  { return _dragDataTransferTypes; }

/**
 * CJDataTransfer: getDropDataTransferFilesImpl().
 */
function Java_snap_webenv_CJDataTransfer_getDropDataTransferFilesImpl(lib)  { return _dropDataTransferFiles; }

// Drag gesture data transfer
var _dragGestureDataTransfer = null;
var _dragGestureDragImage;
var _dragGestureDragImageX;
var _dragGestureDragImageY;

/**
 * CJDataTransfer: startDragImpl().
 */
function Java_snap_webenv_CJDataTransfer_startDragImpl(lib, dataTransfer, dragImage, dx, dy)
{
    _dragGestureDataTransfer = dataTransfer;
    _dragGestureDragImage = dragImage;
    _dragGestureDragImageX = dx;
    _dragGestureDragImageY = dy;
}

/**
 * Called when 'dragstart' called to configure dragGestureEvent.dataTransfer if values set in mousedown.
 */
function handleDragstart(dragEvent)
{
    // If dragGestureDataTransfer not set, just suppress and return
    if (_dragGestureDataTransfer === null) {
        dragEvent.preventDefault();
        dragEvent.stopPropagation();
        return;
    }

    // Copy _dragGestureDataTransfer to dragEvent.dataTransfer and image and return
    for (var type of _dragGestureDataTransfer.types) {
        var dataStr = _dragGestureDataTransfer.getData(type);
        dragEvent.dataTransfer.setData(type, dataStr);
    }

    // Set DragImage, add to body (and register to remove later), and clear dataTransfer / dragImage
    var dragImage = _dragGestureDragImage;
    dragEvent.dataTransfer.setDragImage(dragImage, _dragGestureDragImageX, _dragGestureDragImageY);
    document.body.appendChild(dragImage);
    setTimeout(() => dragImage.parentNode.removeChild(dragImage), 1000);
    _dragGestureDataTransfer = null; _dragGestureDragImage = null;
}

/**
 * Called on 'drop' event to cache data transfer, since it gets reset before CJ can use it.
 */
function handleDrop(dropEvent)
{
    // Create new DataTransfer to persist after drop DataTransfer is reset
    var dropDataTransfer = dropEvent.dataTransfer;
    var dropTypes = dropDataTransfer.types;
    _dropDataTransfer = new DataTransfer();

    // Copy data types to cached DataTransfer
    for (var type of dropTypes) {
        var dataStr = dropDataTransfer.getData(type);
        _dropDataTransfer.setData(type, dataStr);
    }

    // Copy files to cached DataTransfer
    _dropDataTransferFiles = [ ];
    for (var dropFile of dropDataTransfer.files)
        _dropDataTransferFiles.push(dropFile);
}

// This wrapped promise is used to trigger getNextEvent
var _eventNotifyMutex = null;

// This array holds event records (which are also arrays of name, lambda func and optional arg)
let _eventQueue = [ ];

function createMutex()
{
    let fulfill = null;
    let promise = new Promise(f => { fulfill = f; });
    return { fulfill, promise };
}

async function fireEvent(name, callback, arg1, arg2)
{
    // Assume we want to steal all events, since preventDefault won't work with async event delivery)
    if (arg1 instanceof Event) {

        // If KeyboardEvent, suppress some browser keys and do some copy/paste
        if (arg1 instanceof KeyboardEvent) {
            if (arg1.metaKey) {
                var key = arg1.key;

                // Ignore meta+l (select address bar) and meta+alt+i (show dev tools)
                if (key === "l" || arg1.altKey)
                    return;

                // If meta+C (copy) or meta+X (cut), write clipboardWriteItems
                if (key === 'c' || key ==='x')
                    setTimeout(delayedClipboardWrite, 100);

                // If meta+V (paste), read and set clipboardReadItems
                else if (key === 'v')
                    eagerClipboardRead();
            }
        }

        // If MouseEvent, do some copy/paste
        if (arg1 instanceof MouseEvent) {
            if (arg1.type === 'mouseup') {
                setTimeout(delayedClipboardWrite, 100);
                _dragGestureDataTransfer = null; _dragGestureDragImage = null;
            }
        }

        // Handle DragStart: Forward to handleDragstart() and return (_dragGestureDataTransfer needs to be set in mousedown)
        var type = arg1.type;
        if (type === 'dragstart') {
            handleDragstart(arg1);
            return;
        }

        // Handle DragEnd
        if (type === 'dragend') {
            if (_dragGestureDataTransfer === null) {
                arg1.preventDefault();
                arg1.stopPropagation();
            }
        }

        // Stop default/propagation for most events
        else if (type !== 'mousedown' && type !== 'mousemove' && type !== 'mouseup' && type !== 'click' && type !== 'pointerdown') { // && type != "wheel") {
            arg1.preventDefault();
            arg1.stopPropagation();
        }

        // Special support for drag/drop
        if (arg1 instanceof DragEvent) {
            _dragDataTransferTypes = arg1.dataTransfer.types;
            if (arg1.type === 'drop')
                handleDrop(arg1);
        }
    }

    // Add event to queue
    _eventQueue.push([ name, callback, arg1, arg2 ]);

    // If mutex set, trigger it
    if (_eventNotifyMutex != null) {
        _eventNotifyMutex.fulfill();
        _eventNotifyMutex = null;
    }
}

/**
 * EventQueue: getNextEvent().
 */
async function Java_snap_webenv_EventQueue_getNextEvent(lib)
{
    // If event already in queue, just return it
    if (_eventQueue.length > 0)
        return _eventQueue.shift();

    // Otherwise create mutex and wait for next event
    _eventNotifyMutex = createMutex();
    await _eventNotifyMutex.promise;

    // Clear mutex and return event
    _eventNotifyMutex = null;
    return _eventQueue.shift();
}

/**
 * EventQueue: requestAnimationFrameImpl().
 */
function Java_snap_webenv_EventQueue_requestAnimationFrameImpl(lib, aName, doubleFunction)
{
    requestAnimationFrame(timestamp => fireEvent(aName, doubleFunction, timestamp));
}

/**
 * EventQueue: setTimeoutImpl().
 */
function Java_snap_webenv_EventQueue_setTimeoutImpl(lib, aName, aRun, aDelay)
{
    setTimeout(() => fireEvent(aName, aRun, null), aDelay);
}

/**
 * EventQueue: setIntervalImpl().
 */
function Java_snap_webenv_EventQueue_setIntervalImpl(lib, aName, aRun, aDelay)
{
    return setInterval(() => fireEvent(aName, aRun, null), aDelay);
}

// This dictionary holds all addEventListener() listeners with the JS mapped version so removeEventListener() can work
let _listenerDict = { };

/**
 * Registers an event handler of a specific event type on the EventTarget
 */
function Java_snap_webenv_EventQueue_addEventListenerImpl(lib, eventTargetJS, name, eventLsnr, lsnrId, useCapture)
{
    let lsnrJS = e => fireEvent(name, eventLsnr, e, null);
    if (name !== 'load')
        _listenerDict[lsnrId] = lsnrJS;
    eventTargetJS.addEventListener(name, lsnrJS, useCapture);
}

/**
 * Removes an event handler of a specific event type from the EventTarget
 */
function Java_snap_webenv_EventQueue_removeEventListenerImpl(lib, eventTarget, aName, eventLsnr, lsnrId, useCapture)
{
    let lsnrJS = _listenerDict[lsnrId];
    if (lsnrJS != null) {
        eventTarget.removeEventListener(aName, lsnrJS, useCapture);
        _listenerDict[lsnrId] = null;
    }
}

/**
 * Called when mutation observed. Have to wrap mutation records, since event array is returned as Object[] and JNI doesn't know
 * whether to convert the array to JSObject or Object[].
 */
function mutationObserved(callback, mutationRecords)
{
    fireEvent("mutation", callback, { value : mutationRecords }, null);
}

/**
 * EventQueue: setPromiseThenImpl().
 */
function Java_snap_webenv_EventQueue_setPromiseThenImpl(lib, promiseWrapper, aFunc)
{
    let promise = promiseWrapper[0]; // Could pass a mutex in to supported chained promises with a fireEventAndWait() method
    return [ promise.then(value => fireEvent("promise", aFunc, value, null)) ];
}

// This wrapped promise is used to trigger getNextEvent
var _loadEventNotifyMutex = null;

// This array holds event records (which are also arrays of name, lambda func and optional arg)
let _loadEventQueue = [ ];

function createLoadMutex()
{
    let fulfill = null;
    let promise = new Promise(f => { fulfill = f; });
    return { fulfill, promise };
}

async function fireLoadEvent(name, callback, arg1, arg2)
{
    // Add event to queue
    _loadEventQueue.push([ name, callback, arg1, arg2 ]);

    // If mutex set, trigger it
    if (_loadEventNotifyMutex != null) {
        _loadEventNotifyMutex.fulfill();
        _loadEventNotifyMutex = null;
    }
}

/**
 * LoadEventQueue: getNextEvent().
 */
async function Java_snap_webenv_LoadEventQueue_getNextEvent(lib)
{
    // If event already in queue, just return it
    if (_loadEventQueue.length > 0)
        return _loadEventQueue.shift();

    // Otherwise create mutex and wait for next event
    _loadEventNotifyMutex = createLoadMutex();
    await _loadEventNotifyMutex.promise;

    // Clear mutex and return event
    _loadEventNotifyMutex = null;
    return _loadEventQueue.shift();
}

/**
 * Registers a load event handler on the EventTarget
 */
function Java_snap_webenv_LoadEventQueue_addLoadEventListenerImpl(lib, eventTargetJS, eventLsnr)
{
    let lsnrJS = e => fireLoadEvent('load', eventLsnr, e, null);
    eventTargetJS.addEventListener('load', lsnrJS);
}

/**
 * CJCanvasRenderingContext2D: setLineDashImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_setLineDashImpl(lib, cntxJS, doubleArray)
{
    var dashArray = Array.from(doubleArray);
    cntxJS.setLineDash(dashArray);
}

/**
 * CJCanvasRenderingContext2D: fillTextImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_fillTextImpl(lib, cntxJS, aString, aX, aY)  { cntxJS.fillText(aString, aX, aY); }

/**
 * CJCanvasRenderingContext2D: fillTextImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_fillTextImpl2(lib, cntxJS, aString, aX, aY, maxWidth)  { cntxJS.fillText(aString, aX, aY, maxWidth); }

/**
 * CJCanvasRenderingContext2D: strokeText().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_strokeTextImpl(lib, cntxJS, aString, aX, aY)  { cntxJS.strokeText(aString, aX, aY); }

/**
 * CJCanvasRenderingContext2D: strokeText().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_strokeTextImpl2(lib, cntxJS, aString, aX, aY, maxWidth)  { cntxJS.strokeText(aString, aX, aY, maxWidth); }

/**
 * CJCanvasRenderingContext2D: drawImageImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl(lib, cntxJS, imageJS, aX, aY)  { cntxJS.drawImage(imageJS, aX, aY); }

/**
 * CJCanvasRenderingContext2D: drawImageImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl2(lib, cntxJS, imageJS, aX, aY, aW, aH)  { cntxJS.drawImage(imageJS, aX, aY, aW, aH); }

/**
 * CJCanvasRenderingContext2D: drawImageImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl3(lib, cntxJS, imageJS, srcX, srcY, srcW, srcH, destX, destY, destW, destH)
{
    cntxJS.drawImage(imageJS, srcX, srcY, srcW, srcH, destX, destY, destW, destH);
}

/**
 * CJCanvasRenderingContext2D: getImageData().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_getImageDataImpl(lib, canvas, x, y, w, h)
{
    return canvas.getImageData(x, y, w, h);
}

/**
 * CJCanvasRenderingContext2D: putImageDataImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_putImageDataImpl(lib, canvas, imageDataJS, aX, aY, dirtyX, dirtyY, dirtyW, dirtyH)
{
    canvas.putImageData(imageDataJS, aX, aY, dirtyX, dirtyY, dirtyW, dirtyH);
}

/**
 * CJCanvasRenderingContext2D: createLinearGradientImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_createLinearGradientImpl(lib, contextJS, x0, y0, x1, y1)
{
    return contextJS.createLinearGradient(x0, y0, x1, y1);
}

/**
 * CJCanvasRenderingContext2D: createRadialGradientImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_createRadialGradientImpl(lib, contextJS, x0, y0, r0, x1, y1, r1)
{
    return contextJS.createRadialGradient(x0, y0, r0, x1, y1, r1);
}

/**
 * CJCanvasRenderingContext2D: createPatternImpl().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_createPatternImpl(lib, contextJS, imageJS, repetition)
{
    return contextJS.createPattern(imageJS, repetition);
}

var _cntx;
var _cntxScale;
var _instructionStack;
var _intStack; var _intIndex;
var _doubleStack; var _doubleIndex;
var _stringStack; var _stringIndex;
var _nativeStack; var _nativeIndex;

/**
 * CJCanvasRenderingContext2D: paintStacks().
 */
function Java_snap_webenv_CJCanvasRenderingContext2D_paintStacksImpl(lib, contextJS, contextScale, instructionStack, instructionStackSize, intStack, doubleStack, stringStack, objectStack)
{
    _cntx = contextJS;
    _cntxScale = contextScale;
    _instructionStack = instructionStack;
    _intStack = intStack; _intIndex = 0;
    _doubleStack = doubleStack; _doubleIndex = 0;
    _stringStack = stringStack; _stringIndex = 0;
    _nativeStack = objectStack; _nativeIndex = 0;

    _cntx.setTransform(_cntxScale, 0, 0, _cntxScale, 0, 0);

    for (var i = 0; i < instructionStackSize; i++) {
        switch (_instructionStack[i]) {
            case 1: setFont(); break;
            case 2: setPaint(); break;
            case 3: setStroke(); break;
            case 4: setOpacity(); break;
            case 5: drawShape(); break;
            case 6: fillShape(); break;
            case 7: clipShape(); break;
            case 8: drawImage(); break;
            case 9: drawImage2(); break;
            case 10: drawString(); break;
            case 11: strokeString(); break;
            case 12: transform(); break;
            case 13: setTransform(); break;
            case 14: save(); break;
            case 15: restore(); break;
            case 16: clearRect(); break;
            case 17: setImageQuality(); break;
            default: console.log("CJDom.js-paintStacks: Unknown instruction"); break;
        }
    }
}

function setFont(){ _cntx.font = getNative(); }

function setPaint()
{
    var cstr = getNative();
    _cntx.fillStyle = cstr;
    _cntx.strokeStyle = cstr;
}

function setStroke()
{
    // Set line width
    _cntx.lineWidth = getDouble();

    // set line dash
    var dashArrayLen = getInt();
    var dashArray = [];
    for (var i = 0; i < dashArrayLen; i++)
        dashArray.push(getDouble());
    _cntx.setLineDash(dashArray);

    // Set line dash offset
    _cntx.lineDashOffset = dashArrayLen > 0 ? getDouble() : 0;

    // Set line cap
    var lineCap = getInt();
    _cntx.lineCap = lineCap === 0 ? 'round' : lineCap === 1 ? 'butt' : 'square';

    // Set line join
    var lineJoin = getInt();
    _cntx.lineJoin = lineJoin === 0 ? 'round' : lineJoin === 1 ? 'bevel' : 'miter';
    if (lineJoin === 2)
        _cntx.miterLimit = getDouble();
}

function setOpacity(){ _cntx.globalAlpha = getDouble(); }

function drawShape()
{
    setShape();
    _cntx.stroke();
}

function fillShape()
{
    setShape();
    _cntx.fill();
}

function clipShape()
{
    setShape();
    _cntx.clip();
}

function drawImage2()
{
    var image = getNative();
    _cntx.save();
    setTransform();
    if (image != null)
        _cntx.drawImage(image, 0, 0);
    _cntx.restore();
}

function drawImage()
{
    var image = getNative();
    var srcX = getDouble();
    var srcY = getDouble();
    var srcW = getDouble();
    var srcH = getDouble();
    var dx = getDouble();
    var dy = getDouble();
    var dw = getDouble();
    var dh = getDouble();

    // Correct source width/height for image dpi
    // double scaleX = anImg.getDpiX() / 72;
    // double scaleY = anImg.getDpiY() / 72;
    // if (scaleX != 1 || scaleY != 1) {
    //     srcX *= scaleX;
    //     srcW *= scaleX;
    //     srcY *= scaleY;
    //     srcH *= scaleY;
    // }

    if (image != null)
        _cntx.drawImage(image, srcX, srcY, srcW, srcH, dx, dy, dw, dh);
}

/** Draw string at location with char spacing. */
function drawString()
{
    var str = getString();
    var x = getDouble();
    var y = getDouble();
    var cs = getDouble();
    if (cs === 0)
        _cntx.fillText(str, x, y);
    else {
        let charX = x;
        for (let i = 0; i < str.length; i++) {
            const char = str[i];
            _cntx.fillText(char, charX, y);
            charX += _cntx.measureText(char).width + cs;
        }
    }
}

/** Stroke string at location with char spacing. */
function strokeString()
{
    var str = getString();
    var x = getDouble();
    var y = getDouble();
    var cs = getDouble();
    if (cs === 0)
        _cntx.strokeText(str, x, y);
    else {
        let charX = x;
        for (let i = 0; i < str.length; i++) {
            const char = str[i];
            _cntx.strokeText(char, charX, y);
            charX += _cntx.measureText(char).width + cs;
        }
    }
}

/**
 * Transform by transform.
 */
function setTransform()
{
    var m0 = getDouble(); var m1 = getDouble();
    var m2 = getDouble(); var m3 = getDouble();
    var m4 = getDouble(); var m5 = getDouble();
    _cntx.setTransform(m0 * _cntxScale, m1, m2, m3 * _cntxScale, m4, m5);
}

/** Transform by transform. */
function transform()
{
    var m0 = getDouble(); var m1 = getDouble();
    var m2 = getDouble(); var m3 = getDouble();
    var m4 = getDouble(); var m5 = getDouble();
    _cntx.transform(m0, m1, m2, m3, m4, m5);
}

function save()  { _cntx.save(); }

function restore()  { _cntx.restore(); }

function clearRect()
{
    var x = getDouble(); var y = getDouble();
    var w = getDouble(); var h = getDouble();
    _cntx.clearRect(x, y, w, h);
}

function setImageQuality()
{
    var quality = getDouble();
    var qualityStr = quality > .67 ? "high" : quality >.33 ? "medium" : "low";
    _cntx.imageSmoothingQuality = qualityStr;
    _cntx.imageSmoothingEnabled = quality > .33;
}

function setShape()
{
    var opCount = getInt();

    _cntx.beginPath();

    // Handle rect shape
    if (opCount === -1) {
        _cntx.rect(getDouble(), getDouble(), getDouble(), getDouble());
        return;
    }

    // Handle path shape: Iterate over path ops and add to context
    for (var i = 0; i < opCount; i++) {
        var op = getInt();
        switch (op) {
            case 0: _cntx.moveTo(getDouble(), getDouble()); break;
            case 1: _cntx.lineTo(getDouble(), getDouble()); break;
            case 2: _cntx.bezierCurveTo(getDouble(), getDouble(), getDouble(), getDouble(), getDouble(), getDouble()); break;
            case 3: _cntx.closePath(); break;
        }
    }
}

// Get stack values
function getInt()  { return _intStack[_intIndex++]; }
function getDouble()  { return _doubleStack[_doubleIndex++]; }
function getString()  { return _stringStack[_stringIndex++]; }
function getNative()  { return _nativeStack[_nativeIndex++]; }

// Open the database
function openDB()
{
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("cjFS_/files/", 1);
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
}

// Read a record and modify a field
async function Java_snap_web_FileSite_setLastModified(lib, filePath, newModTime)
{
    // Open db
    const db = await openDB();

    // Fetch file and reset mod time
    return new Promise((resolve, reject) => {
        const tx = db.transaction("files", "readwrite");
        const store = tx.objectStore("files");

        const getRequest = store.get(filePath);

        // Handle success
        getRequest.onsuccess = function() {

            // Get file
            const file = getRequest.result;
            if (!file) {
                reject("File not found: " + filePath);
                return;
            }

            // Reset file lastModified to new mod time in seconds from given millis BigInt
            //console.log("Last mod time for " + filePath + " is " + file.lastModified + " " + file.lastModified.constructor.name);
            file.lastModified = Number(newModTime / 1000n);

            // Save new file
            const putRequest = store.put(file, filePath);
            putRequest.onsuccess = () => resolve("File updated successfully");
            putRequest.onerror = () => reject(putRequest.error);
        };

        // Handle error
        getRequest.onerror = () => reject(getRequest.error);
    });
}

/**
 * Constant for registering with CJ.
 */
let cjdomNativeMethods = {

    Java_snap_webenv_CJWebEnv_getMemberImpl, Java_snap_webenv_CJWebEnv_setMemberImpl,
    Java_snap_webenv_CJWebEnv_getMemberStringImpl, Java_snap_webenv_CJWebEnv_setMemberStringImpl,
    Java_snap_webenv_CJWebEnv_getMemberBooleanImpl, Java_snap_webenv_CJWebEnv_setMemberBooleanImpl,
    Java_snap_webenv_CJWebEnv_getMemberIntImpl, Java_snap_webenv_CJWebEnv_setMemberIntImpl,
    Java_snap_webenv_CJWebEnv_getMemberDoubleImpl, Java_snap_webenv_CJWebEnv_setMemberDoubleImpl,
    Java_snap_webenv_CJWebEnv_getSlotImpl, Java_snap_webenv_CJWebEnv_setSlotImpl,
    Java_snap_webenv_CJWebEnv_windowImpl,
    Java_snap_webenv_CJWebEnv_consoleImpl,
    Java_snap_webenv_CJWebEnv_newObjectImpl,
    Java_snap_webenv_CJWebEnv_awaitForPromiseImpl,
    Java_snap_webenv_CJWebEnv_setNeedsClickElement,
    Java_snap_webenv_CJWebEnv_newArrayJSForLengthImpl,
    Java_snap_webenv_CJWebEnv_getBytesArrayForArrayBufferJSImpl,
    Java_snap_webenv_CJWebEnv_getBytesArrayForTypedArrayJSImpl,
    Java_snap_webenv_CJWebEnv_getShortsArrayForTypedArrayJSImpl,
    Java_snap_webenv_CJWebEnv_getShortsArrayForTypedArrayJSAndChannelIndexAndCountImpl,
    Java_snap_webenv_CJWebEnv_getInt8ArrayForObject,
    Java_snap_webenv_CJWebEnv_getInt16ArrayForObject,
    Java_snap_webenv_CJWebEnv_getFloat32ArrayForObject,
    Java_snap_webenv_CJWebEnv_getUint16ArrayForObject,
    Java_snap_webenv_CJWebEnv_getUint8ClampedArrayForObject,
    Java_snap_webenv_CJWebEnv_newImageDataJSForRgbaArrayAndWidthAndHeightImpl,
    Java_snap_webenv_CJWebEnv_newBlobJSForBytesAndTypeImpl,
    Java_snap_webenv_CJWebEnv_createUrlForBlobJSImpl,
    Java_snap_webenv_CJWebEnv_newFileJSForNameAndTypeAndBytesImpl,
    Java_snap_webenv_CJWebEnv_newFileReaderJSImpl,
    Java_snap_webenv_CJWebEnv_newMutationObserverImpl,
    Java_snap_webenv_CJWebEnv_addMutationObserverImpl,
    Java_snap_webenv_CJWebEnv_newClipboardItemForMimeTypeAndDataStringImpl,
    Java_snap_webenv_CJWebEnv_newClipboardItemForBlobImpl,
    Java_snap_webenv_CJWebEnv_readClipboardItemsImpl,
    Java_snap_webenv_CJWebEnv_writeClipboardItemsImpl,

    Java_snap_webenv_CJDataTransfer_newDataTransfer,
    Java_snap_webenv_CJDataTransfer_getDropDataTransferImpl,
    Java_snap_webenv_CJDataTransfer_getDropDataTransferTypesImpl,
    Java_snap_webenv_CJDataTransfer_getDropDataTransferFilesImpl,
    Java_snap_webenv_CJDataTransfer_startDragImpl,

    Java_snap_webenv_EventQueue_getNextEvent,
    Java_snap_webenv_EventQueue_requestAnimationFrameImpl,
    Java_snap_webenv_EventQueue_setTimeoutImpl, Java_snap_webenv_EventQueue_setIntervalImpl,
    Java_snap_webenv_EventQueue_addEventListenerImpl, Java_snap_webenv_EventQueue_removeEventListenerImpl,
    Java_snap_webenv_EventQueue_setPromiseThenImpl,
    Java_snap_webenv_LoadEventQueue_getNextEvent,
    Java_snap_webenv_LoadEventQueue_addLoadEventListenerImpl,

    Java_snap_webenv_CJCanvasRenderingContext2D_setLineDashImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_fillTextImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_fillTextImpl2,
    Java_snap_webenv_CJCanvasRenderingContext2D_strokeTextImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_strokeTextImpl2,
    Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl2,
    Java_snap_webenv_CJCanvasRenderingContext2D_drawImageImpl3,
    Java_snap_webenv_CJCanvasRenderingContext2D_getImageDataImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_putImageDataImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_createLinearGradientImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_createRadialGradientImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_createPatternImpl,
    Java_snap_webenv_CJCanvasRenderingContext2D_paintStacksImpl,

    Java_snap_web_FileSite_setLastModified
};
