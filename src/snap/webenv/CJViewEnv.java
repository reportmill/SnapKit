package snap.webenv;
import snap.geom.Rect;
import snap.view.*;
import snap.view.Clipboard;
import snap.web.WebFile;
import snap.webapi.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A ViewEnv implementation for CheerpJ.
 */
public class CJViewEnv extends ViewEnv {

    // The clipboard
    private Clipboard _clipboard;

    // A map of window.setIntervals() return ids
    private Map <Runnable,Integer> _intervalIds = new HashMap<>();

    /**
     * Constructor.
     */
    public CJViewEnv()
    {
        if (_env == null) {
            _env = this;
            CJRenderer.registerFactory();
        }
    }

    /**
     * Returns whether current thread is event thread.
     */
    public boolean isEventThread()  { return WebEnv.get().isEventThread(); }

    /**
     * Run later.
     */
    public void runLater(Runnable aRun)
    {
        Window.setTimeout(aRun, 0);
    }

    /**
     * Runs given runnable after delay.
     */
    @Override
    public void runDelayed(Runnable aRun, int aDelay)
    {
        Window.setTimeout(aRun, aDelay);
    }

    /**
     * Runs given runnable repeatedly every period milliseconds.
     */
    @Override
    public void runIntervals(Runnable aRun, int aPeriod)
    {
        int id = Window.setInterval(aRun, aPeriod);
        _intervalIds.put(aRun, id);
    }

    /**
     * Stops running given runnable.
     */
    @Override
    public void stopIntervals(Runnable aRun)
    {
        Integer id = _intervalIds.get(aRun);
        if (id != null)
            Window.clearInterval(id);
    }

    /**
     * Returns the system clipboard.
     */
    public Clipboard getClipboard()
    {
        if (_clipboard!=null) return _clipboard;
        return _clipboard = CJClipboard.get();
    }

    /**
     * Returns a new ViewHelper for given native component.
     */
    public WindowView.WindowHpr createHelper(View aView)
    {
        return new CJWindowHpr();
    }

    /**
     * Creates an event for a UI view.
     */
    @Override
    public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
    {
        // Create, configure event
        ViewEvent event = new CJEvent();
        event.setView(aView);
        event.setEvent(anEvent);
        event.setType(aType);
        event.setName(aName!=null ? aName : aView!=null? aView.getName() : null);

        // Return
        return event;
    }

    /**
     * Returns the screen bounds inset to usable area.
     */
    public Rect getScreenBoundsInset()
    {
        return CJ.getViewportBounds();
    }

    /**
     * Shows a file picker.
     */
    @Override
    public void showFilePicker(String[] fileTypes, Consumer<WebFile> pickedFileHandler)
    {
        FilePicker filePicker = new FilePicker();
        filePicker.showFilePicker(fileTypes, fp -> handleFilePicked(fp, pickedFileHandler));
    }

    /**
     * Called when user has picked file (or cancelled).
     */
    private void handleFilePicked(FilePicker filePicker, Consumer<WebFile> pickedFileHandler)
    {
        // Get filename and file bytes
        String filename = filePicker.getPickedFilename();
        byte[] fileBytes = filePicker.getPickedFileBytes();

        // Create pickedFile if filename and file bytes are available
        WebFile pickedFile = null;
        if (filename != null && fileBytes != null) {
            pickedFile = WebFile.createTempFileForName(filename, false);
            pickedFile.setBytes(fileBytes);
            pickedFile.save();
        }

        // Call handler
        pickedFileHandler.accept(pickedFile);
    }
}