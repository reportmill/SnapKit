/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.text.TextDocUtils.CharsChange;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.web.WebURL;

/**
 * This TextDoc subclass wraps a TextDoc for a character range.
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

        // Set vars
        _textDoc = aTextDoc;
        _startCharIndexInDoc = aStart;
        _endCharIndexInDoc = aStart;

        // Set DefaultStyle, DefaultLineStyle
        super.setDefaultStyle(_textDoc.getDefaultStyle());
        super.setDefaultLineStyle(_textDoc.getDefaultLineStyle());

        // Add TextDoc text for chars range
        String subString = _textDoc.getString().substring(aStart, anEnd);
        setString(subString);

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
     * Sets the default style.
     */
    @Override
    public void setDefaultStyle(TextStyle aStyle)
    {
        _textDoc.setDefaultStyle(aStyle);
    }

    /**
     * Sets the default line style.
     */
    @Override
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        _textDoc.setDefaultLineStyle(aLineStyle);
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    @Override
    public void addChars(CharSequence theChars, TextStyle theStyle, int charIndex)
    {
        _textDoc.addChars(theChars, theStyle, _startCharIndexInDoc + charIndex);
    }

    /**
     * Removes characters in given range.
     */
    @Override
    public void removeChars(int startCharIndex, int endCharIndex)
    {
        _textDoc.removeChars(_startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setStyle(TextStyle aStyle, int startCharIndex, int endCharIndex)
    {
        _textDoc.setStyle(aStyle, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    @Override
    public void setStyleValue(String aKey, Object aValue, int startCharIndex, int endCharIndex)
    {
        _textDoc.setStyleValue(aKey, aValue, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setLineStyle(TextLineStyle aStyle, int startCharIndex, int endCharIndex)
    {
        _textDoc.setLineStyle(aStyle, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setLineStyleValue(String aKey, Object aValue, int startCharIndex, int endCharIndex)
    {
        _textDoc.setLineStyleValue(aKey, aValue, _startCharIndexInDoc + startCharIndex, _startCharIndexInDoc + endCharIndex);
    }

    /**
     * Save TextDoc text to Source file.
     */
    public void saveToSourceFile()  { _textDoc.saveToSourceFile(); }

    /**
     * Override to use TextDoc tokenizer.
     */
    @Override
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        TextToken[] tokens = _textDoc.createTokensForTextLine(aTextLine);
        return tokens;
    }

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
            CharsChange charsChange = (CharsChange) aPC;
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

        // If add charIndex before StartCharIndexInDoc, update StartCharIndex/EndCharIndex  and return
        if (charIndex < _startCharIndexInDoc) {
            _startCharIndexInDoc += addChars.length();
            _endCharIndexInDoc += addChars.length();
            return;
        }

        // Update EndCharIndexInDoc for add chars
        _endCharIndexInDoc += addChars.length();

        // Get style at index and CharIndex in SubText
        TextStyle textStyle = _textDoc.getStyleForCharIndex(charIndex);
        int charIndexInSub = charIndex - getStartCharIndex();

        // Do original addChars
        super.addChars(addChars, textStyle, charIndexInSub);

        // Create/fire CharsChange for SubText
        CharsChange subCharsChange = new CharsChange(this, null, addChars, charIndexInSub);
        firePropChange(subCharsChange);
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
        int endCharIndex = charIndex + removeChars.length();
        if (endCharIndex <= _startCharIndexInDoc) {
            _startCharIndexInDoc -= removeChars.length();
            _endCharIndexInDoc -= removeChars.length();
            return;
        }

        // If charIndex before StartCharIndexInDoc, update Start/EndCharIndexInDoc
        if (charIndex < _startCharIndexInDoc) {
            int beforeCharsLength = _startCharIndexInDoc - charIndex;
            _startCharIndexInDoc -= beforeCharsLength;
            _endCharIndexInDoc -= beforeCharsLength;
            removeChars = removeChars.subSequence(beforeCharsLength, removeChars.length());
            charIndex = _startCharIndexInDoc;
        }

        // If endCharIndex beyond EndCharIndexInDoc, update EndCharIndex
        if (endCharIndex > _endCharIndexInDoc) {
            int beyondCharsLength = charIndex - _endCharIndexInDoc;
            removeChars = removeChars.subSequence(0, removeChars.length() - beyondCharsLength);
            endCharIndex = _endCharIndexInDoc;
        }

        // Trim EndCharIndexInDoc by deleted chars
        _endCharIndexInDoc -= removeChars.length();

        // Get SubTextLine
        int charIndexInSub = charIndex - getStartCharIndex();
        int endCharIndexInSub = endCharIndex - getStartCharIndex();

        // Do original removeChars
        super.removeChars(charIndexInSub, endCharIndexInSub);

        // Create/fire CharsChange for SubText
        CharsChange subCharsChange = new CharsChange(this, removeChars, null, charIndexInSub);
        firePropChange(subCharsChange);
    }

    /**
     * Called when SubText is no longer needed to remove TextDoc prop listener.
     */
    public void dispose()
    {
        _textDoc.removePropChangeListener(_textDocPropLsnr);
    }
}
