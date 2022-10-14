package snap.text;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.web.WebURL;

/**
 * This TextDoc subclass wraps a TextDoc and a sub range.
 */
public class SubText extends TextDoc {

    // The real TextDoc
    private TextDoc  _textDoc;

    // The start char index
    protected int  _startCharIndexInDoc;

    // The length
    protected int  _endCharIndexInDoc;

    // PropChangeListener to update Start/End char indexes when text is edited
    private PropChangeListener  _textDocPropLsnr = pc -> textDocDidPropChange(pc);

    /**
     * Constructor.
     */
    public SubText(TextDoc aTextDoc, int aStart, int anEnd)
    {
        super();
        _textDoc = aTextDoc;
        _startCharIndexInDoc = aStart;
        _endCharIndexInDoc = aStart;

        // Add TextDoc chars
        String subString = _textDoc.getString().substring(aStart, anEnd);
        textDocDidAddChars(aStart, subString);

        // Listen to TextDoc
        _textDoc.addPropChangeListener(_textDocPropLsnr);
    }

    /**
     * Returns the TextDoc that this SubText references.
     */
    public TextDoc getTextDoc()  { return _textDoc; }

    /**
     * Returns the start char index.
     */
    @Override
    public int getStartCharIndex()  { return _startCharIndexInDoc; }

    /**
     * Returns the end char index.
     */
    public int getEndCharIndex()  { return _endCharIndexInDoc; }

    /**
     * Override to suppress.
     */
    @Override
    protected void addDefaultLine()  { }

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    public boolean isRichText()  { return _textDoc.isRichText(); }

    /**
     * Returns the source for the current text content.
     */
    public Object getSource()  { return _textDoc.getSource(); }

    /**
     * Loads the text from the given source.
     */
    public void setSource(Object aSource)  { _textDoc.setSource(aSource); }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _textDoc.getSourceURL(); }

    /**
     * Returns the number of characters in the text.
     */
    //public int length()  { return _endCharIndexInDoc - _startCharIndexInDoc; }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _textDoc.charAt(_startCharIndexInDoc + anIndex); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textDoc.subSequence(_startCharIndexInDoc + aStart, _startCharIndexInDoc + anEnd);
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        return subSequence(0, length()).toString();
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        _textDoc.replaceChars(aString, _startCharIndexInDoc, _endCharIndexInDoc);
    }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()  { return _textDoc.getDefaultStyle(); }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)  { _textDoc.setDefaultStyle(aStyle); }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _textDoc.getDefaultLineStyle(); }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textDoc.setDefaultLineStyle(aLineStyle); }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int charIndex)
    {
        _textDoc.addChars(theChars, theStyle, _startCharIndexInDoc + charIndex);
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int startCharIndex, int endCharIndex)
    {
        _textDoc.removeChars(_startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Adds given TextDoc to this text at given index.
     */
    public void addTextDoc(TextDoc aTextDoc, int charIndex)
    {
        _textDoc.addTextDoc(aTextDoc, _startCharIndexInDoc + charIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setStyle(TextStyle aStyle, int startCharIndex, int endCharIndex)
    {
        _textDoc.setStyle(aStyle, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue, int startCharIndex, int endCharIndex)
    {
        _textDoc.setStyleValue(aKey, aValue, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int startCharIndex, int endCharIndex)
    {
        _textDoc.setLineStyle(aStyle, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int startCharIndex, int endCharIndex)
    {
        _textDoc.setLineStyleValue(aKey, aValue, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Save TextDoc text to Source file.
     */
    public void saveToSourceFile()  { _textDoc.saveToSourceFile(); }

    /**
     * Called when textDoc does prop change.
     */
    private void textDocDidPropChange(PropChange aPC)
    {
        // Get PropName
        String propName = aPC.getPropName();

        // Handle Chars change
        if (propName == TextDoc.Chars_Prop) {

            // Get CharsChange and charIndex
            TextDocUtils.CharsChange charsChange = (TextDocUtils.CharsChange) aPC;
            int charIndex = charsChange.getIndex();
            CharSequence addChars = charsChange.getNewValue();
            CharSequence removeChars = charsChange.getOldValue();

            // Forward to textDocDidAddChars or textDocDidRemoveChars
            if (addChars != null)
                textDocDidAddChars(charIndex, addChars);
            else textDocDidRemoveChars(charIndex, removeChars);
        }

    }

    /**
     * Called when TextDoc adds chars.
     */
    private void textDocDidAddChars(int charIndex, CharSequence addChars)
    {
        // If add charIndex beyond SubText.EndCharIndexInDoc, just return (nothing changes for SubText)
        if (charIndex > _endCharIndexInDoc)
            return;

        // If add charIndex before StartCharIndexInDoc, update StartCharIndex for SubText and TextLines and return
        if (charIndex < _startCharIndexInDoc) {
            _startCharIndexInDoc += addChars.length();
            _endCharIndexInDoc += addChars.length();
            updateLines(-1);
            return;
        }

        // Update EndCharIndexInDoc for add chars
        int charIndexInSubText = charIndex - getStartCharIndex();
        int charsLength = addChars.length();
        int endCharIndex = charIndex + charsLength;
        _endCharIndexInDoc += charsLength;

        // Get TextLine
        TextLine textLine = _textDoc.getLineForCharIndex(charIndex);

        // Get SubTextLine
        int charIndexInSub = charIndex - getStartCharIndex();
        SubTextLine subTextLine = (SubTextLine) getLineForCharIndex(charIndexInSub);
        int subTextLength = _endCharIndexInDoc - _startCharIndexInDoc;

        // Add or lengthen subTextLine until synched
        while (true) {

            // If no SubTextLine for TextLine, create and add
            if (subTextLine == null || subTextLine._textLine != textLine) {

                // Get new line index from SubTextLine.Index, unless at end of line (end of text, really)
                int lineIndex = getLineCount();
                if (subTextLine != null) {
                    lineIndex = subTextLine.getIndex();
                    if (charIndexInSub == subTextLine.getEndCharIndex())
                        lineIndex++;
                }

                // Create/add SubTextLine for TextLine
                int lineLength = Math.min(textLine.length(), _endCharIndexInDoc - textLine.getStartCharIndex());
                subTextLine = new SubTextLine(this, textLine, lineLength);
                addLine(subTextLine, lineIndex);
            }

            // Otherwise, update subTextLine for add chars and break
            else {
                int lineLength = Math.min(textLine.length(), subTextLength - subTextLine.getStartCharIndex());
                subTextLine.setLength(lineLength);
            }

            // Get next lines
            textLine = textLine.getNext();
            if (textLine == null)
                break;
            int textLineStartCharIndex = textLine.getStartCharIndex();
            if (textLineStartCharIndex > endCharIndex || textLineStartCharIndex >= _endCharIndexInDoc)
                break;
            subTextLine = (SubTextLine) subTextLine.getNext();
        }

        // Create/fire CharsChange for SubText
        TextDocUtils.CharsChange subCharsChange = new TextDocUtils.CharsChange(this,
                null, addChars, charIndexInSubText);
        firePropChange(subCharsChange);
        checkSynch();
    }

    /**
     * Called when TextDoc removes chars.
     */
    private void textDocDidRemoveChars(int charIndex, CharSequence removeChars)
    {
        // If remove charIndex beyond EndCharIndexInDoc, just return
        if (charIndex >= _endCharIndexInDoc)
            return;

        // If remove endCharIndex before StartCharIndex, update StartCharIndex for SubText and TextLines and return
        int charsLength = removeChars.length();
        int endCharIndex = charIndex + removeChars.length();
        if (endCharIndex <= _startCharIndexInDoc) {
            _startCharIndexInDoc -= charsLength;
            _endCharIndexInDoc -= charsLength;
            updateLines(-1);
            return;
        }

        // If charIndex before StartCharIndexInDoc, update Start/EndCharIndexInDoc
        if (charIndex < _startCharIndexInDoc) {
            int beforeCharsLength = _startCharIndexInDoc - charIndex;
            _startCharIndexInDoc -= beforeCharsLength;
            _endCharIndexInDoc -= beforeCharsLength;
            removeChars = removeChars.subSequence(beforeCharsLength, removeChars.length());
            charIndex = _startCharIndexInDoc;
            charsLength = removeChars.length();
        }

        // If endCharIndex beyond EndCharIndexInDoc, update EndCharIndex
        if (endCharIndex > _endCharIndexInDoc) {
            int beyondCharsLength = charIndex - _endCharIndexInDoc;
            removeChars = removeChars.subSequence(0, removeChars.length() - beyondCharsLength);
            endCharIndex = _endCharIndexInDoc;
            charsLength = removeChars.length();
        }

        // Trim EndCharIndexInDoc by deleted chars
        int charIndexInSub = charIndex - getStartCharIndex();
        _endCharIndexInDoc -= charsLength;

        // Get TextLine
        int subTextLength = _endCharIndexInDoc - _startCharIndexInDoc;
        TextLine textLine = _textDoc.getLineForCharIndex(charIndex);
        if (charIndex == textLine.getStartCharIndex() && charIndex == _endCharIndexInDoc && subTextLength > 0)
            textLine = textLine.getPrevious();

        // Get SubTextLine
        int endCharIndexInSub = endCharIndex - getStartCharIndex();
        SubTextLine subTextLine = (SubTextLine) getLineForCharIndex(endCharIndexInSub);

        // Remove or short subTextLine until synched
        while (true) {

            // If Lines don't match, remove line
            if (subTextLine._textLine != textLine) {

                // Remove line
                int lineIndex = subTextLine.getIndex();
                removeLine(lineIndex);

                // If removed line 0, see if we need to add single empty line
                if (lineIndex == 0) {
                    if (length() == 0) {
                        subTextLine = new SubTextLine(this, textLine, 0);
                        addLine(subTextLine, 0);
                    }
                    break;
                }
                subTextLine = (SubTextLine) getLine(lineIndex - 1);
            }

            // Otherwise, update subTextLine for add chars and break
            else {
                int lineLength = Math.min(textLine.length(), subTextLength - subTextLine.getStartCharIndex());
                subTextLine.setLength(lineLength);
                break;
            }
        }

        // Create/fire CharsChange for SubText
        TextDocUtils.CharsChange subCharsChange = new TextDocUtils.CharsChange(this,
                removeChars, null, charIndexInSub);
        firePropChange(subCharsChange);
        checkSynch();
    }

    /**
     * Check Synch  of SubText to TextDoc.
     */
    private void checkSynch()
    {
        // Get first TextLine and SubTextLine
        TextLine textLine = _textDoc.getLineForCharIndex(getStartCharIndex());
        SubTextLine subTextLine = (SubTextLine) getLine(0);

        // Iterate over each
        while (subTextLine != null) {

            // If TextLines don't match, complain
            if (textLine != subTextLine._textLine)
                System.out.println("Lines don't match!!!");

            // If not last line, check lengths
            else if (subTextLine.getNext() != null) {

                if (textLine.length() != subTextLine.length())
                    System.out.println("Lengths don't match");
            }

            // If last line...
            else {

                // If ending doesn't match, complain
                if (textLine.getStartCharIndex() + subTextLine.length() != getEndCharIndex())
                    System.out.println("Endings don't match");
            }

            textLine = textLine.getNext();
            subTextLine = (SubTextLine) subTextLine.getNext();
        }
    }

    /**
     * Called when SubText is no longer needed to remove TextDoc prop listener.
     */
    public void dispose()
    {
        _textDoc.removePropChangeListener(_textDocPropLsnr);
    }
}
