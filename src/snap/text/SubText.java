package snap.text;
import snap.props.PropChange;
import snap.web.WebURL;

/**
 * This TextDoc subclass wraps a TextDoc and a sub range.
 */
public class SubText extends TextDoc {

    // The real TextDoc
    private TextDoc  _textDoc;

    // The start char index
    protected int _start;

    // The end char index
    protected int _end;

    /**
     * Constructor.
     */
    public SubText(TextDoc aTextDoc, int aStart, int anEnd)
    {
        super();
        _textDoc = aTextDoc;
        _start = aStart;
        _end = anEnd;
        rebuildLines();

        // Listen to TextDoc
        _textDoc.addPropChangeListener(pc -> textDocDidPropChange(pc));
    }

    /**
     * Returns the start char index.
     */
    public int getStartCharIndex()  { return _start; }

    /**
     * Returns the end char index.
     */
    public int getEndCharIndex()  { return _end; }

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
    public int length()  { return _end - _start; }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _textDoc.charAt(anIndex + _start); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textDoc.subSequence(aStart + _start, anEnd + _start);
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
        _textDoc.replaceChars(aString, _start, _end);
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
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        _textDoc.addChars(theChars, theStyle, anIndex + _start);
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        _textDoc.removeChars(aStart + _start, anEnd + _start);
    }

    /**
     * Adds given TextDoc to this text at given index.
     */
    public void addTextDoc(TextDoc aTextDoc, int anIndex)
    {
        _textDoc.addTextDoc(aTextDoc, anIndex + _start);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setStyle(TextStyle aStyle, int aStart, int anEnd)
    {
        _textDoc.setStyle(aStyle, aStart + _start, anEnd + _start);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        _textDoc.setStyleValue(aKey, aValue, aStart + _start, anEnd + _start);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        _textDoc.setLineStyle(aStyle, aStart + _start, anEnd + _start);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        _textDoc.setLineStyleValue(aKey, aValue, aStart + _start, anEnd + _start);
    }

    /**
     * Rebuilds lines.
     */
    private void rebuildLines()
    {
        _lines.clear();
        _width = -1;

        // Get TextLine at start char index
        int charIndex = _start;
        TextLine textLine = _textDoc.getLineForCharIndex(charIndex);

        // Iterate over TextLines and add SubTextLines for each
        while (true) {

            // Create, configure, add SubTextLine for TextLine
            int lineEnd = Math.min(textLine.getEnd(), _end);
            SubTextLine subLine = new SubTextLine(this, textLine, charIndex, lineEnd);
            subLine._index = _lines.size();
            _lines.add(subLine);

            // Get next TextLine - if beyond SubText.End, just break
            textLine = textLine.getNext();
            if (textLine == null || _end <= textLine.getStart())
                break;
            charIndex = lineEnd;
        }
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
                if (charIndex < _start) {
                    _start += addChars.length();
                    _end += addChars.length();
                }
                if (charIndex >= _start && charIndex <= _end) {
                    _end += addChars.length();
                    needSubPropChange = true;
                }
            }

            // Handle remove chars
            else {
                if (charIndex < _end) {
                    int endCharIndex = Math.min(_end - charIndex, removeChars.length());
                    _end -= endCharIndex;
                    if (charIndex < _start)
                        _start -= Math.min(_start - charIndex, removeChars.length());
                    else needSubPropChange = true;
                }
            }

            // Reset Lines, Width
            rebuildLines();

            // Fire our own CharsChange
            if (needSubPropChange) {

                // If removeChars outside SubText start/end range, trim to range
                if (removeChars != null) {
                    int charsStart = Math.max(charIndex, _start) - charIndex;
                    int charsEnd = Math.min(charIndex + removeChars.length(), _end) - charIndex;
                    if (charsStart != 0 || charsEnd != removeChars.length())
                        removeChars = removeChars.subSequence(charsStart, charsEnd);
                }

                // Create/fire CharsChange for SubText
                int charIndexInSubText = charIndex - _start;
                TextDocUtils.CharsChange subCharsChange = new TextDocUtils.CharsChange(this,
                        removeChars, addChars, charIndexInSubText);
                firePropChange(subCharsChange);
            }
        }
    }
}
