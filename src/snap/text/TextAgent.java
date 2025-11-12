package snap.text;
import snap.web.WebFile;
import java.util.function.Supplier;

/**
 * This class manages the interaction of a text model with a file and facilitates sharing a text model with multiple editors.
 */
public class TextAgent {

    // The source file
    private WebFile _textFile;

    // The TextModel for text file
    private TextModel _textModel;

    // File sync support
    private String _unmodifiedString;

    // An optional supplier to create TextModel
    private Supplier<TextModel> _textModelSupplier;

    /**
     * Constructor for given file.
     */
    private TextAgent(WebFile aFile)
    {
        _textFile = aFile;

        // Set File TextAgent property to this agent
        _textFile.setMetadataForKey(TextAgent.class.getName(), this);
    }

    /**
     * Closes this agent.
     */
    public void closeAgent()
    {
        // If already close, complain and return
        if (_textFile == null) {
            System.err.println("TextAgent.closeAgent: Multiple closes");
            return;
        }

        // Clear everything
        _textFile.setMetadataForKey(TextAgent.class.getName(), null);
        _textFile.reset();
        _textFile = null;
        _textModel = null;
    }

    /**
     * Returns the TextModel for the text file.
     */
    public TextModel getTextModel()
    {
        if (_textModel != null) return _textModel;

        // Create/load TextModel
        _textModel = createTextModel();
        syncTextModelToSourceFile();

        // Listen for changes
        _textModel.addPropChangeListener(pc -> handleTextModelCharsChange(), TextModel.Chars_Prop);

        // Set, return
        return _textModel;
    }

    /**
     * Returns the TextModel for the text file.
     */
    protected TextModel createTextModel()
    {
        if (_textModelSupplier != null)
            return _textModelSupplier.get();
        return new TextModel();
    }

    /**
     * Sets the text model supplier.
     */
    public void setTextModelSupplier(Supplier<TextModel> supplier)  { _textModelSupplier = supplier; }

    /**
     * Reloads agent from file.
     */
    public void reloadFile()
    {
        // Reset file
        _textFile.resetAndVerify();

        // Reload TextModel from text file
        if (_textModel != null)
            syncTextModelToSourceFile();
    }

    /**
     * Synchronizes TextModel and SourceFile.
     */
    public void syncTextModelToSourceFile()
    {
        // Reload TextModel from File string
        _unmodifiedString = _textFile.getText();
        _textModel.setString(_unmodifiedString);
    }

    /**
     * Called when TextModel gets chars changes.
     */
    private void handleTextModelCharsChange()
    {
        boolean textModified = !_unmodifiedString.contentEquals(_textModel);
        if (textModified == _textModel.isTextModified())
            return;

        // Update SourceFile.Updater
        if (textModified && _textFile.getUpdater() == null)
            _textFile.setUpdater(file -> updateSourceFileFromTextModel());
        else if (!textModified && _textFile.getUpdater() != null)
            _textFile.setUpdater(null);

        // Update TextModified
        _textModel.setTextModified(textModified);
    }

    /**
     * Called when file is saved.
     */
    private void updateSourceFileFromTextModel()
    {
        _unmodifiedString = _textModel.getString();
        _textFile.setText(_unmodifiedString);
        _textModel.setTextModified(false);
    }

    /**
     * Returns the TextAgent for given file.
     */
    public static TextAgent getAgentForFile(WebFile aFile)
    {
        // Get TextAgent for given source file - just return if found
        TextAgent textAgent = (TextAgent) aFile.getMetadataForKey(TextAgent.class.getName());
        if (textAgent != null)
            return textAgent;

        // Return not found
        return new TextAgent(aFile);
    }
}
