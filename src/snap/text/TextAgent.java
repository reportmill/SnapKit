package snap.text;
import snap.props.PropChange;
import snap.props.Undoer;
import snap.util.ListUtils;
import snap.web.WebFile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class manages the interaction of a text model with a file and facilitates sharing a text model with multiple editors.
 */
public class TextAgent {

    // The source file
    private WebFile _textFile;

    // Whether text model is modified
    private boolean _textModified;

    // The TextModel for text file
    private TextModel _textModel;

    // The shared undoer for the text model
    private Undoer _textUndoer;

    // File sync support
    private String _unmodifiedString;

    // An optional supplier to create TextModel
    private Supplier<TextModel> _textModelSupplier;

    // List of agent providers registered with addAgentProvider()
    private static List<Function<WebFile,TextAgent>> _agentProviders = new ArrayList<>();

    /**
     * Constructor for given file.
     */
    protected TextAgent(WebFile aFile)
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
     * Returns whether text model is modified.
     */
    public boolean isTextModified()  { return _textModified; }

    /**
     * Sets whether text model is modified.
     */
    public void setTextModified(boolean aValue)
    {
        if (aValue == _textModified) return;
        _textModified = aValue;

        // If file saved, mark undoer last saved state
        if (!aValue && _textUndoer != null)
            _textUndoer.markLastSaveState();
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
        _textModel.addPropChangeListener(this::handleTextModelCharsChange, TextModel.Chars_Prop);

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
        return TextModel.createDefaultTextModel();
    }

    /**
     * Sets the text model supplier.
     */
    public void setTextModelSupplier(Supplier<TextModel> supplier)  { _textModelSupplier = supplier; }

    /**
     * Returns the text undoer.
     */
    public Undoer getTextUndoer()
    {
        if (_textUndoer != null) return _textUndoer;
        _textUndoer = new Undoer();
        _textUndoer.setAutoSave(true);
        return _textUndoer;
    }

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
        setTextModified(false);
    }

    /**
     * Called when TextModel gets chars changes.
     */
    protected void handleTextModelCharsChange(PropChange propChange)
    {
        boolean textModified = !_unmodifiedString.contentEquals(_textModel.getChars());
        if (textModified == isTextModified())
            return;

        // Update SourceFile.Updater
        if (textModified && _textFile.getUpdater() == null)
            _textFile.setUpdater(file -> updateSourceFileFromTextModel());
        else if (!textModified && _textFile.getUpdater() != null)
            _textFile.setUpdater(null);

        // Update TextModified
        setTextModified(textModified);
    }

    /**
     * Called when file is saved.
     */
    private void updateSourceFileFromTextModel()
    {
        _unmodifiedString = _textModel.getString();
        _textFile.setText(_unmodifiedString);
        setTextModified(false);
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

        // If providers can create agent, return it
        TextAgent newAgent = ListUtils.findNonNull(_agentProviders, prov -> prov.apply(aFile));
        if (newAgent != null)
            return newAgent;

        // Return new text agent
        return new TextAgent(aFile);
    }

    /**
     * Adds a provider to create agent subclasses for specific files.
     */
    public static void addAgentProvider(Function<WebFile,TextAgent> agentProvider)  { _agentProviders.add(agentProvider); }
}
