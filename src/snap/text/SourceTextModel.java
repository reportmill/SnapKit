package snap.text;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.web.WebFile;

/**
 * This TextModel subclass embeds a source text to forward all changes and to update from when needed.
 */
public class SourceTextModel extends TextModel {

    // The source text model
    protected TextModel _sourceText;

    // A temp var to hold TextLineStyle when updating runs from source text
    private TextLineStyle _updateTextLineStyle;

    /**
     * Constructor.
     */
    public SourceTextModel()
    {
        this(false);
    }

    /**
     * Constructor with option for rich text.
     */
    public SourceTextModel(boolean isRichText)
    {
        super(isRichText);
        TextModel textModel = new TextModel(isRichText);
        setSourceText(textModel);
    }

    /**
     * Constructor for source text.
     */
    public SourceTextModel(TextModel sourceText)
    {
        super(sourceText.isRichText());
        setSourceText(sourceText);
    }

    /**
     * Returns the source text model.
     */
    public TextModel getSourceText()  { return _sourceText; }

    /**
     * Sets the source TextModel.
     */
    public void setSourceText(TextModel sourceText)
    {
        if (sourceText == getSourceText()) return;

        // Sync default TextStyle/LineStyle
        _sourceText = null;
        setDefaultTextStyle(sourceText.getDefaultTextStyle());
        setDefaultLineStyle(sourceText.getDefaultLineStyle());

        // Set value
        _sourceText = sourceText;
        reloadTextFromSourceText();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    @Override
    public void setRichText(boolean aValue)
    {
        if (_sourceText != null)
            _sourceText.setRichText(aValue);
        super.setRichText(aValue);
    }

    /**
     * Sets the text to the given string.
     */
    @Override
    public void setString(String aString)
    {
        if (_sourceText != null)
            _sourceText.setString(aString);
        super.setString(aString);
    }

    /**
     * Sets the default text style.
     */
    @Override
    public void setDefaultTextStyle(TextStyle textStyle)
    {
        if (_sourceText != null)
            _sourceText.setDefaultTextStyle(textStyle);
        super.setDefaultTextStyle(textStyle);
    }

    /**
     * Sets the default line style.
     */
    @Override
    public void setDefaultLineStyle(TextLineStyle lineStyle)
    {
        if (_sourceText != null)
            _sourceText.setDefaultLineStyle(lineStyle);
        super.setDefaultLineStyle(lineStyle);
    }

    /**
     * Sets whether text is modified.
     */
    @Override
    public void setTextModified(boolean aValue)
    {
        if (_sourceText != null)
            _sourceText.setTextModified(aValue);
        super.setTextModified(aValue);
    }

    /**
     * Adds characters with given style to this text at given index.
     */
    @Override
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        if (_sourceText != null)
            _sourceText.addCharsWithStyle(theChars, theStyle, anIndex);
        super.addCharsWithStyle(theChars, theStyle, anIndex);
    }

    /**
     * Override to do wrapping.
     */
    @Override
    protected void addCharsToLine(CharSequence theChars, TextStyle theStyle, int charIndex, TextLine textLine, boolean charsHaveNewline)
    {
        // If updating text from source text, update line style
        if (_updateTextLineStyle != null)
            textLine.setLineStyle(_updateTextLineStyle);

        // Do normal version
        super.addCharsToLine(theChars, theStyle, charIndex, textLine, charsHaveNewline);
    }

    /**
     * Removes characters in given range.
     */
    @Override
    public void removeChars(int aStartCharIndex, int anEndCharIndex)
    {
        if (_sourceText != null)
            _sourceText.removeChars(aStartCharIndex, anEndCharIndex);
        super.removeChars(aStartCharIndex, anEndCharIndex);
    }

    /**
     * Sets the given text style for given range.
     */
    @Override
    public void setTextStyle(TextStyle textStyle, int aStart, int anEnd)
    {
        if (_sourceText != null)
            _sourceText.setTextStyle(textStyle, aStart, anEnd);
        super.setTextStyle(textStyle, aStart, anEnd);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        if (_sourceText != null)
            _sourceText.setLineStyle(aStyle, aStart, anEnd);
        super.setLineStyle(aStyle, aStart, anEnd);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    protected void setLineStyleRich(TextLineStyle aStyle, int aStart, int anEnd)
    {
        if (_sourceText != null)
            _sourceText.setLineStyleRich(aStyle, aStart, anEnd);
        super.setLineStyleRich(aStyle, aStart, anEnd);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    protected void setLineStyleValueRich(String aKey, Object aValue, int aStart, int anEnd)
    {
        if (_sourceText != null)
            _sourceText.setLineStyleValueRich(aKey, aValue, aStart, anEnd);
        super.setLineStyleValueRich(aKey, aValue, aStart, anEnd);
    }

    /**
     * Creates TextTokens for a TextLine.
     */
    @Override
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        if (_sourceText != null)
            return _sourceText.createTokensForTextLine(aTextLine);
        return super.createTokensForTextLine(aTextLine);
    }

    /**
     * Returns a copy of this text for given char range.
     */
    @Override
    public TextModel copyForRange(int aStart, int aEnd)
    {
        if (_sourceText != null)
            return _sourceText.copyForRange(aStart, aEnd);
        return super.copyForRange(aStart, aEnd);
    }

    /**
     * Override to forward to SourceText.
     */
    @Override
    public void readTextFromSourceFile(WebFile sourceFile)
    {
        if (_sourceText != null) {
            _sourceText.readTextFromSourceFile(sourceFile);
            reloadTextFromSourceText();
        }

        else super.readTextFromSourceFile(sourceFile);
    }

    /**
     * Override to forward to SourceText.
     */
    @Override
    public void writeTextToSourceFile()
    {
        if (_sourceText != null && _sourceText.getSourceFile() != null)
            _sourceText.writeTextToSourceFile();
        else super.writeTextToSourceFile();
    }

    /**
     * Override to forward to SourceText.
     */
    public void syncTextModelToSourceFile()
    {
        if (_sourceText != null && _sourceText.getSourceFile() != null)
            _sourceText.syncTextModelToSourceFile();
        else super.syncTextModelToSourceFile();
    }

    /**
     * XMLArchiver.Archivable archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        if (_sourceText != null)
            return _sourceText.toXML(anArchiver);
        return super.toXML(anArchiver);
    }

    /**
     * Reloads text from SourceText.
     */
    protected void reloadTextFromSourceText()
    {
        // Skip if no text
        if (length() == 0 && _sourceText.isEmpty()) return;

        // Update ranges
        int startCharIndex = 0;
        int endCharIndexBox = length();
        int endCharIndexBlock = _sourceText.length();

        // Cache SourceText
        TextModel sourceText = _sourceText;
        _sourceText = null;
        setPropChangeEnabled(false);

        // If WrapLines, mark location of first line's first token - if it shrinks, might need to re-wrap previous line
        boolean wrapLines = isWrapLines();
        TextLine firstLine = wrapLines ? getLineForCharIndex(startCharIndex) : null;
        double firstLineTokenMaxX = wrapLines ? getFirstTokenMaxXForLineIfPreviousLineCares(firstLine) : 0;

        // Remove chars in range
        super.removeChars(startCharIndex, endCharIndexBox);

        // Get run iterator for range (adjusted if this text is overflow from linked)
        int textStartCharIndex = getStartCharIndex();
        int charIndex = Math.max(textStartCharIndex, startCharIndex);
        TextRunIter runIter = sourceText.getRunIterForCharRange(charIndex, endCharIndexBlock);

        // Iterate over source text runs for range and add
        while (runIter.hasNextRun()) {

            // Set temp LineStyle
            TextRun nextRun = runIter.getNextRun();
            TextLine textLine = nextRun.getLine();
            _updateTextLineStyle = textLine.getLineStyle();

            // Add run chars
            addCharsWithStyle(nextRun.getString(), nextRun.getTextStyle(), charIndex - textStartCharIndex); // Was super_
            _updateTextLineStyle = null;
            charIndex += nextRun.length();
        }

        // If first token shrank, re-wrap previous line
        if (firstLineTokenMaxX > 0 && firstLineTokenMaxX > getFirstTokenMaxXForLineIfPreviousLineCares(firstLine)) {
            TextLine previousLine = firstLine.getPrevious();
            joinLineWithNextLine(previousLine);
        }

        // Reset SourceText
        _sourceText = sourceText;
        setPropChangeEnabled(true);
    }

    /**
     * Returns the MaxX of first token in given line.
     * If no first token or there is no way previous line would need to re-wrap if this shrinks, returns 0.
     */
    private static double getFirstTokenMaxXForLineIfPreviousLineCares(TextLine textLine)
    {
        if (textLine.getTokenCount() == 0)
            return 0;
        TextLine previousLine = textLine.getPrevious();
        if (previousLine == null || previousLine.isLastCharNewline())
            return 0;
        TextToken firstToken = textLine.getToken(0);
        return firstToken.getMaxX();
    }
}