package snap.webapi;
import java.util.List;
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
    public static List<ClipboardItem> readClipboardItems()
    {
        Object[] clipboardItemsJS = WebEnv.get().readClipboardItemsJS();
        return Stream.of(clipboardItemsJS).map(ClipboardItem::new).toList();
    }

    /**
     * Writes array of ClipboardItem to clipboard.
     */
    public static void writeClipboardItems(List<ClipboardItem> clipboardItems)
    {
        Array<ClipboardItem> clipboardItemsArray = new Array<>(clipboardItems.toArray(new ClipboardItem[0]));
        WebEnv.get().writeClipboardItemsJS(clipboardItemsArray._jsObj);
    }
}