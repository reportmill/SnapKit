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

    // The end char index
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
        _endCharIndexInDoc = anEnd;
        rebuildLines();

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
    public int length()  { return _endCharIndexInDoc - _startCharIndexInDoc; }

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
        String propName = aPC.getPropName();
        if (propName == TextDoc.Chars_Prop) {

            // Get CharsChange and charIndex
            TextDocUtils.CharsChange charsChange = (TextDocUtils.CharsChange) aPC;
            CharSequence addChars = charsChange.getNewValue();
            CharSequence removeChars = charsChange.getOldValue();
            int charIndex = charsChange.getIndex();
            boolean needSubPropChange = false;

            // Handle add chars
            if (addChars != null) {
                if (charIndex < _startCharIndexInDoc) {
                    _startCharIndexInDoc += addChars.length();
                    _endCharIndexInDoc += addChars.length();
                }
                if (charIndex >= _startCharIndexInDoc && charIndex <= _endCharIndexInDoc) {
                    _endCharIndexInDoc += addChars.length();
                    needSubPropChange = true;
                }
            }

            // Handle remove chars
            else {
                if (charIndex < _endCharIndexInDoc) {
                    int endCharIndex = Math.min(_endCharIndexInDoc - charIndex, removeChars.length());
                    _endCharIndexInDoc -= endCharIndex;
                    if (charIndex < _startCharIndexInDoc)
                        _startCharIndexInDoc -= Math.min(_startCharIndexInDoc - charIndex, removeChars.length());
                    else needSubPropChange = true;
                }
            }

            // Reset Lines, Width
            rebuildLines();

            // Fire our own CharsChange
            if (needSubPropChange) {

                // If removeChars outside SubText start/end range, trim to range
                if (removeChars != null) {
                    int charsStart = Math.max(charIndex, _startCharIndexInDoc) - charIndex;
                    int charsEnd = Math.min(charIndex + removeChars.length(), _endCharIndexInDoc) - charIndex;
                    if (charsStart != 0 || charsEnd != removeChars.length())
                        removeChars = removeChars.subSequence(charsStart, charsEnd);
                }

                // Create/fire CharsChange for SubText
                int charIndexInSubText = charIndex - _startCharIndexInDoc;
                TextDocUtils.CharsChange subCharsChange = new TextDocUtils.CharsChange(this,
                        removeChars, addChars, charIndexInSubText);
                firePropChange(subCharsChange);
            }
        }
    }

    /**
     * Rebuilds lines.
     */
    protected void rebuildLines()
    {
        _lines.clear();
        _width = -1;

        // Get TextLine at start char index
        int charIndex = _startCharIndexInDoc;
        TextLine textLine = _textDoc.getLineForCharIndex(charIndex);

        // Iterate over TextLines and add SubTextLines for each
        while (true) {

            // Create, configure, add SubTextLine for TextLine
            int lineEnd = Math.min(textLine.getEndCharIndex(), _endCharIndexInDoc);
            SubTextLine subLine = new SubTextLine(this, textLine, charIndex, lineEnd);
            subLine._index = _lines.size();
            _lines.add(subLine);

            // Get next TextLine - if beyond SubText.End, just break
            textLine = textLine.getNext();
            if (textLine == null || _endCharIndexInDoc <= textLine.getStartCharIndex())
                break;
            charIndex = lineEnd;
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
