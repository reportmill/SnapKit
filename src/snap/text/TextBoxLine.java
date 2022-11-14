/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.util.CharSequenceX;
import snap.util.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a line of text in a TextBox.
 */
public class TextBoxLine implements CharSequenceX {

    // The TextBox that contains this line
    protected TextBox  _textBox;

    // The starting style for this line
    protected TextStyle  _startStyle;

    // The index of this line in text
    protected int  _index;

    // The char index of the start char of this line in text
    protected int  _startCharIndex;

    // The number of chars in this text line
    protected int  _length;

    // The TextLine that this line renders
    protected TextLine  _textLine;

    // The start of this line in TextLine
    protected int  _textLineStart;

    // The bounds of this line in TextBlock
    protected double  _yloc = -1, _width, _height, _widthAll;

    // The x shift of the line due to alignment
    protected double  _alignX;

    // The tokens for this line
    protected List<TextBoxToken>  _tokens = new ArrayList<>();

    // The max Ascent for line fonts
    protected double  _ascent, _descent, _leading, _lineAdvance;

    // An array of character runs for the line
    protected List<TextBoxRun>  _runs;

    /**
     * Creates a new TextBoxLine.
     */
    public TextBoxLine(TextBox aBox, TextStyle aStartStyle, TextLine aTextLine, int theRTLStart)
    {
        _textBox = aBox;
        _startStyle = aStartStyle;
        _textLine = aTextLine;
        _textLineStart = theRTLStart;
    }

    /**
     * Returns the TextBox.
     */
    public TextBox getBox()  { return _textBox; }

    /**
     * Returns the TextStyle at start of line.
     */
    public TextStyle getStartStyle()  { return _startStyle; }

    /**
     * Returns the index of this line in text.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the start char index of this line in text.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index of this line in text.
     */
    public int getEndCharIndex()  { return _startCharIndex + _length; }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _length; }

    /**
     * Returns the string for the line.
     */
    public String getString()
    {
        return subSequence(0, length()).toString();
    }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        return _textLine.charAt(anIndex + _textLineStart);
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textLine.subSequence(aStart + _textLineStart, anEnd + _textLineStart);
    }

    /**
     * Returns the TextLine.
     */
    public TextLine getTextLine()  { return _textLine; }

    /**
     * Returns the start of this line in TextLine.
     */
    public int getTextLineStart()  { return _textLineStart; }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()
    {
        return _textLine.getLineStyle();
    }

    /**
     * Returns the line x.
     */
    public double getX()
    {
        return _textBox.getX() + _alignX;
    }

    /**
     * Returns the line y.
     */
    public double getY()
    {
        return getYLocal() + _textBox.getAlignedY();
    }

    /**
     * Returns the line y.
     */
    public double getYLocal()
    {
        // If already set, just return
        if (_yloc >= 0) return _yloc;

        // Get YLocal from last line. Need to fix this to not stack overflow for large text showing tail first.
        int index = getIndex();
        TextBoxLine lastLine = index > 0 ? _textBox.getLine(index - 1) : null;
        _yloc = lastLine != null ? (lastLine.getYLocal() + lastLine.getLineAdvance()) : 0;
        return _yloc;
    }

    /**
     * Returns the y position for this line (in same coords as the layout frame).
     */
    public double getBaseline()
    {
        return getY() + getAscent();
    }

    /**
     * Returns the width.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _height; }

    /**
     * Returns the max X.
     */
    public double getMaxX()
    {
        return getX() + getWidth();
    }

    /**
     * Returns the max Y.
     */
    public double getMaxY()
    {
        return getY() + getHeight();
    }

    /**
     * Returns the width including trailing whitespace.
     */
    public double getWidthAll()  { return _widthAll; }

    /**
     * Returns the max x including trailing whitespace.
     */
    public double getMaxXAll()
    {
        return getX() + getWidthAll();
    }

    /**
     * Returns the width without whitespace.
     */
    public double getWidthNoWhiteSpace()
    {
        int len = length();
        while (len > 0 && Character.isWhitespace(charAt(len - 1))) len--;
        if (len == length())
            return getWidth();
        return getXForCharIndex(len) - getX();
    }

    /**
     * Validates this line.
     */
    public void resetSizes()
    {
        // Get last token and its info
        TextBoxToken lastToken = getTokenCount() > 0 ? _tokens.get(_tokens.size() - 1) : null;
        int lastTokenEnd = lastToken != null ? lastToken.getEndCharIndex() : 0;
        TextStyle lastTokenStyle = lastToken != null ? lastToken.getTextStyle() : getStartStyle();

        // Get line end and length (extend end to capture trailing whitespace after last token)
        int lineEnd = lastTokenEnd + _textLineStart;
        while (lineEnd < _textLine.length() && Character.isWhitespace(_textLine.charAt(lineEnd)))
            lineEnd++;
        _length = lineEnd - _textLineStart;

        // Iterate over runs and get line metrics
        _ascent = lastTokenStyle.getAscent();
        _descent = lastTokenStyle.getDescent();
        _leading = lastTokenStyle.getLeading();
        for (TextBoxToken tok : _tokens) {
            if (tok.getTextStyle() == lastTokenStyle) continue;
            lastTokenStyle = tok.getTextStyle();
            _ascent = Math.max(lastTokenStyle.getAscent(), _ascent);
            _descent = Math.max(lastTokenStyle.getDescent(), _descent);
            _leading = Math.max(lastTokenStyle.getLeading(), _leading);
        }

        // Get TextLineStyle
        TextLineStyle lineStyle = _textLine.getLineStyle();

        // Set width, height and LineAdvance
        _width = _widthAll = lastToken != null ? lastToken.getTextBoxMaxX() - getX() : 0;
        _height = _ascent + _descent;

        // Calculate LineAdvance
        _lineAdvance = _ascent + _descent + _leading;
        _lineAdvance = MathUtils.clamp(_lineAdvance, lineStyle.getMinHeight(), lineStyle.getMaxHeight());
        _lineAdvance *= lineStyle.getSpacingFactor();
        _lineAdvance += lineStyle.getSpacing();

        // Calculate widthAll (width with trailing whitespace)
        for (int i = lastTokenEnd, iMax = _length; i < iMax; i++) {
            char c = charAt(i);
            if (c == '\t')
                _widthAll = _textLine.getXForTabAtIndexAndX(_textLineStart + i, getX() + _widthAll) - getX();
            else if (c != '\n' && c != '\r')
                _widthAll += lastTokenStyle.getCharAdvance(c);
        }

        // If justify, shift tokens in line (unless line has newline or is last line in RichText)
        if (lineStyle.isJustify() && getTokenCount() > 1) {

            // If line only has newline, just return
            if (isLastCharNewline() || lineEnd == _textLine.length())
                return;

            // Calculate Justify token shift
            double lineY = getY();
            double textBoxMaxX = _textBox.getMaxHitX(lineY, _height);
            double lineMaxX = getMaxX();
            double remainderW = textBoxMaxX - lineMaxX;
            double shiftX = remainderW / (getTokenCount() - 1);
            double runningShiftX = 0;

            // Shift tokens
            for (TextBoxToken token : getTokens()) {
                token._shiftX = runningShiftX;
                runningShiftX += shiftX;
            }

            // Update WidthAll
            _widthAll += runningShiftX - shiftX;
        }

        // Calculate X alignment shift
        else if (_textLine.getAlignX() != HPos.LEFT && _textBox.getWidth() < 9999) {
            double alignX = _textLine.getAlignX().doubleValue();
            double lineY = getY();
            double tboxHitX = _textBox.getMaxHitX(lineY, _height);
            double lineMaxX = getMaxX();
            double remW = tboxHitX - lineMaxX;
            _alignX = Math.round(alignX * remW);
        }
    }

    /**
     * Returns the number of tokens.
     */
    public int getTokenCount()  { return _tokens.size(); }

    /**
     * Returns the individual token at given index.
     */
    public TextBoxToken getToken(int anIndex)  { return _tokens.get(anIndex); }

    /**
     * Returns the tokens for this line.
     */
    public List <TextBoxToken> getTokens()  { return _tokens; }

    /**
     * Adds a token to line.
     */
    public void addToken(TextBoxToken aToken)
    {
        _tokens.add(aToken);
    }

    /**
     * Returns the last token for this line.
     */
    public TextBoxToken getTokenLast()
    {
        int tokenCount = _tokens.size();
        return tokenCount > 0 ? _tokens.get(tokenCount - 1) : null;
    }

    /**
     * Returns the max ascent of the chars in this line.
     */
    public double getAscent()  { return _ascent; }

    /**
     * Returns the max descent of the chars in this line.
     */
    public double getDescent()  { return _descent; }

    /**
     * Returns the leading of the chars in this line.
     */
    public double getLeading()  { return _leading; }

    /**
     * Returns the vertical distance for any line below this line.
     */
    public double getLineAdvance()  { return _lineAdvance; }

    /**
     * Returns the token at character index.
     */
    public TextBoxToken getTokenForCharIndex(int anIndex)
    {
        int tokenCount = getTokenCount();
        if (tokenCount == 0)
            return null;

        // Iterate
        TextBoxToken tok = getToken(0);
        for (int i = 1; i < tokenCount; i++) {
            TextBoxToken next = getToken(i);
            if (next.getStartCharIndex() <= anIndex)
                tok = next;
            else break;
        }

        // Return
        return tok;
    }

    /**
     * Returns the token at index.
     */
    public TextBoxToken getTokenForX(double anX)
    {
        // Get token
        TextBoxToken token = getTokenCount() > 0 ? getToken(0) : null;
        if (token == null || token.getTextBoxX() > anX)
            return null;

        // Iterate
        for (int i = 0, iMax = getTokenCount(); i < iMax; i++) {
            TextBoxToken nextToken = i + 1 < iMax ? getToken(i + 1) : null;
            if (nextToken != null && nextToken.getTextBoxX() <= anX)
                token = nextToken;
            else return token;
        }

        // Return
        return token;
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndexForX(double anX)
    {
        // Get run for x coord (just return zero if null)
        TextBoxToken token = getTokenForX(anX);
        int charIndex = token != null ? token.getStartCharIndex() : 0;
        int lineLength = length();
        TextStyle textStyle = token != null ? token.getTextStyle() : getStartStyle();

        double charX = token != null ? token.getTextBoxX() : getX();

        while (charIndex < lineLength) {
            char loopChar = charAt(charIndex);
            double charW = loopChar == '\t' ?
                _textLine.getXForTabAtIndexAndX(_textLineStart + charIndex, charX) - charX :
                textStyle.getCharAdvance(loopChar);
            if (charX + charW / 2 > anX)
                return charIndex;
            charIndex++;
            charX += charW;
        }

        // If at end of line with newline, back off 1
        if (charIndex == length() && isLastCharNewline())
            charIndex--;

        // Return
        return charIndex;
    }

    /**
     * Returns the X coord for given char index.
     */
    public double getXForCharIndex(int anIndex)
    {
        // If at end, just return MaxX
        if (anIndex == length())
            return getMaxXAll();

        // Get token for char index and token style
        TextBoxToken textBoxToken = getTokenForCharIndex(anIndex);
        if (textBoxToken != null && anIndex < textBoxToken.getStartCharIndex())
            textBoxToken = null;
        TextStyle textStyle = textBoxToken != null ? textBoxToken.getTextStyle() : getStartStyle();

        // Init charX to token start X
        double charX = textBoxToken != null ? textBoxToken.getTextBoxX() : getX();

        // Iterate over subsequent chars after token start and add advance
        for (int i = textBoxToken != null ? textBoxToken.getStartCharIndex() : 0; i < anIndex; i++) {
            char loopChar = charAt(i);
            if (loopChar == '\t')
                charX = _textLine.getXForTabAtIndexAndX(_textLineStart + i, charX);
            else charX += textStyle.getCharAdvance(loopChar) + textStyle.getCharSpacing();
        }

        // Return
        return charX;
    }

    /**
     * Returns the next line, if available.
     */
    public TextBoxLine getNext()
    {
        int nextIndex = _index + 1;
        return nextIndex < _textBox.getLineCount() ? _textBox.getLine(nextIndex) : null;
    }

    /**
     * Returns the previous line if available.
     */
    public TextBoxLine getPrevious()
    {
        int prevIndex = _index - 1;
        return prevIndex >= 0 ? _textBox.getLine(prevIndex) : null;
    }

    /**
     * Returns the max stroke width of any underlined chars in this line.
     */
    public double getUnderlineStroke()
    {
        double stroke = 0;
        for (TextBoxRun run : getRuns())
            stroke = Math.max(stroke, run.getFont().getUnderlineThickness());
        return stroke;
    }

    /**
     * Returns the Y position of any underlined chars in this line.
     */
    public double getUnderlineY()
    {
        double y = 0;
        for (TextBoxRun run : getRuns())
            y = Math.min(y, run.getFont().getUnderlineOffset());
        return y;
    }

    /**
     * Returns an array of runs for the line.
     */
    public List<TextBoxRun> getRuns()
    {
        // If already set, just return
        if (_runs != null) return _runs;

        // Create new list for runs
        List<TextBoxRun> runs = new ArrayList<>(_textLine.getRunCount());

        // Create first run and add to list.
        TextBoxRun run = createRun(0);
        runs.add(run);

        // Continue to create/add runs while not at line end
        while (run.getEnd() < length()) {
            run = createRun(run.getEnd());
            runs.add(run);
        }

        // Set and return
        return _runs = runs;
    }

    /**
     * Creates the TextBoxRun at given char index in line.
     */
    protected TextBoxRun createRun(int aStart)
    {
        // Get TextLine run for char index - if at TextRun.End, move to next if available
        int startInTextLine = _textLineStart + aStart;
        TextRun textRun = _textLine.getRunForCharIndex(startInTextLine);
        if (startInTextLine == textRun.getEndCharIndex()) { // Not sure I like this
            TextRun nextRun = textRun.getNext();
            if (nextRun != null)
                textRun = nextRun;
        }

        // Get TextStyle for run
        TextStyle runStyle = textRun.getStyle();
        double fontScale = _textBox.getFontScale();
        if (fontScale != 1)
            runStyle = runStyle.copyFor(runStyle.getFont().scaleFont(fontScale));

        // Get end of run
        int endCharIndex = textRun.getEndCharIndex() - _textLineStart;
        if (endCharIndex > length())
            endCharIndex = length();

        // If Justify, reset end to start of next token
        if (getLineStyle().isJustify()) {
            TextBoxToken textBoxToken = getTokenForCharIndex(aStart);
            int tokenIndex = textBoxToken != null ? getTokens().indexOf(textBoxToken) : -1;
            TextBoxToken nextToken = tokenIndex >= 0 && tokenIndex + 1 < getTokenCount() ? getToken(tokenIndex + 1) : null;
            if (nextToken != null)
                endCharIndex = nextToken.getStartCharIndex();
        }

        // If there are tabs, end after first tab instead
        for (int i = aStart; i < endCharIndex; i++) {
            if (charAt(i) == '\t') {
                endCharIndex = i + 1;
                break;
            }
        }

        // Create/return new run
        return new TextBoxRun(this, runStyle, aStart, endCharIndex);
    }

    /**
     * Returns the first TextBox run for the line.
     */
    public TextBoxRun getRun()
    {
        return getRuns().get(0);
    }

    /**
     * Returns the last TextBox run for the line.
     */
    public TextBoxRun getRunLast()
    {
        List<TextBoxRun> runs = getRuns();
        return runs.get(runs.size() - 1);
    }

    /**
     * Returns whether line ends with hyphen.
     */
    public boolean isHyphenated()
    {
        TextBoxToken tok = getTokenLast();
        return tok != null && tok.isHyphenated();
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getString();
        str = str.replace("\n", "\\n");
        return getClass().getSimpleName() + "[" + getIndex() + "](" + getStartCharIndex() + "," + getEndCharIndex() + "): str=\"" + str + "\"";
    }
}