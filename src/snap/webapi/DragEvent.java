package snap.webapi;

/**
 * This class is a wrapper for Web API DragEvent (https://developer.mozilla.org/en-US/docs/Web/API/DragEvent).
 */
public class DragEvent extends MouseEvent {

    /**
     * Constructor.
     */
    public DragEvent(Object eventJS)
    {
        super(eventJS);
    }

    /**
     * Returns the data that is transferred during a drag and drop interaction.
     */
    public DataTransfer getDataTransfer()
    {
        // Try getting drop data transfer - CJDom has cached version since it gets reset after any delay for security
        DataTransfer dataTransfer = WebEnv.get().getDropDataTransfer();
        if (dataTransfer != null)
            return dataTransfer;

        // Do version that would be used if drop data transfer didn't have to be cached
        Object dataTransferJS = getMember("dataTransfer");
        return WebEnv.get().newDataTransferForDataTransferJS(dataTransferJS);
    }
}