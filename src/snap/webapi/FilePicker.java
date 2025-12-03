package snap.webapi;
import java.util.function.Consumer;

/**
 * A class to represent a FilePicker.
 */
public class FilePicker {

    // The function to be called when file picked
    private Consumer<FilePicker> _filePickedHandler;

    // The last picked file name
    private String _pickedFilename;

    // The last read file bytes
    private byte[] _pickedFileBytes;

    // The last input element
    private HTMLInputElement _inputElement;

    /**
     * Constructor.
     */
    public FilePicker()
    {
        super();
    }

    /**
     * Returns the last picked file name.
     */
    public String getPickedFilename()  { return _pickedFilename; }

    /**
     * Returns the last picked file name.
     */
    public byte[] getPickedFileBytes()  { return _pickedFileBytes; }

    /**
     * Show file picker.
     */
    public void showFilePicker(String[] fileTypes, Consumer<FilePicker> filePickedHandler)
    {
        _filePickedHandler = filePickedHandler;

        // Get inputElement
        HTMLInputElement inputElement = getInputElement(fileTypes);

        // Add to doc body and click
        HTMLBodyElement.getBody().appendChild(inputElement);
        inputElement.click();
    }

    /**
     * Creates and returns the input element.
     */
    private HTMLInputElement getInputElement(String[] fileTypes)
    {
        // Remove last inputElement from page
        if (_inputElement != null) {
            Node parentNode = _inputElement.getParentNode();
            if (parentNode != null)
                parentNode.removeChild(_inputElement);
        }

        // Create and configure inputElement
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLInputElement inputElement = (HTMLInputElement) doc.createElement("input");
        inputElement.setType("file");
        inputElement.setAcceptTypes(fileTypes);
        inputElement.getStyle().setCssText("display: none;");
        inputElement.addEventListener("change", e -> handleFilePickerChange(e));

        // Return
        return _inputElement = inputElement;
    }

    /**
     * Called when user selects a file or cancels file picker.
     */
    private void handleFilePickerChange(Event changeEvent)
    {
        // If file picked, read bytes and call handler
        File file = getFileForChangeEvent(changeEvent);
        if (file != null) {
            FileReader fileReader = new FileReader();
            fileReader.readBytesAndRunLater(file, () -> handleFilePickerFileLoaded(file, fileReader));
        }

        // Otherwise just call handler
        else _filePickedHandler.accept(this);
    }

    /**
     * Called when file bytes are available.
     */
    private void handleFilePickerFileLoaded(File file, FileReader fileReader)
    {
        _pickedFilename = file.getName();
        _pickedFileBytes = fileReader.getResultBytes();
        _filePickedHandler.accept(this);
    }

    /**
     * Returns the file for a change event.
     */
    private static File getFileForChangeEvent(Event changeEvent)
    {
        // Get changeEvent.target.files (just return if null)
        Object target = changeEvent.getMember("target");
        Object filesList = WebEnv.get().getMember(target, "files");
        if (filesList == null)
            return null;

        // Convert to File
        Array<Object> dropFilesArray = new Array<>(filesList);
        Object[] dropFilesJS = dropFilesArray.toArray(Object.class);
        Object dropFileJS = dropFilesJS.length > 0 ? dropFilesJS[0] : null;
        return dropFileJS != null ? new File(dropFileJS) : null;
    }
}
