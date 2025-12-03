package snap.webapi;

/**
 * This class is a wrapper for Web API FileReader (https://developer.mozilla.org/en-US/docs/Web/API/FileReader).
 */
public class FileReader extends JSProxy implements EventTarget {

    /**
     * Constructor.
     */
    public FileReader()
    {
        super(WebEnv.get().newFileReaderJS());
    }

    /**
     * Starts reading the contents of a specified Blob or File.
     * When the read operation is finished, the readyState becomes DONE, and the loadend is triggered.
     * At that time, the result attribute contains an ArrayBuffer representing the file's data.
     */
    public void readAsArrayBuffer(Blob aBlob)
    {
        call("readAsArrayBuffer", aBlob._jsObj);
    }

    /**
     * Returns the bytes.
     */
    public byte[] getResultBytes()
    {
        Object arrayBufferJS = getMember("result");
        ArrayBuffer arrayBuffer = new ArrayBuffer(arrayBufferJS);
        return arrayBuffer.getBytes();
    }

    /**
     * readBytesAndWait
     */
    public void readBytesAndRunLater(Blob aBlob, Runnable aRun)
    {
        addEventListener("loadend", e -> aRun.run());
        readAsArrayBuffer(aBlob);
    }
}