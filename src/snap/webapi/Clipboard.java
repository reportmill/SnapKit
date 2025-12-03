package snap.webapi;
import java.util.stream.Stream;

/**
 * This class is a wrapper for Web API Clipboard (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard).
 */
public class Clipboard extends JSProxy {

    /**
     * Constructor.
     */
    public Clipboard(Object eventJS)
    {
        super(eventJS);
    }

    /**
     * Reads clipboard and returns an array of ClipboardItem.
     */
    public static ClipboardItem[] readClipboardItems()
    {
        Object[] clipboardItemsJS = WebEnv.get().readClipboardItemsJS();
        return Stream.of(clipboardItemsJS).map(item -> new ClipboardItem(item)).toArray(size -> new ClipboardItem[size]);
    }

    /**
     * Writes array of ClipboardItem to clipboard.
     */
    public static void writeClipboardItems(ClipboardItem[] clipboardItems)
    {
        Array<ClipboardItem> clipboardItemsArray = new Array<>(clipboardItems);
        WebEnv.get().writeClipboardItemsJS(clipboardItemsArray._jsObj);
    }
}