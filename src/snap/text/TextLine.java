/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.util.ArrayUtils;
import snap.util.CharSequenceX;
import snap.util.SnapUtils;

/**
 * This class represents a line of text in a Text.
 */
public class TextLine implements CharSequenceX, Cloneable {

    // The TextBlock that contains this line
    protected TextBlock _textBlock;

    // The StringBuffer that holds line chars
    protected StringBuffer  _sb = new StringBuffer();

    // The char index of the start of this line in text
    protected int  _startCharIndex;

    // The run for this line
    protected TextRun[]  _runs = EMPTY_RUNS;

    // The TextTokens for this line
    protected TextToken[]  _tokens;

    // The line style
    protected TextLineStyle  _lineStyle;

    // The index of this line in text
    protected int  _index;

    // The X location of line in block
    protected double _x;

    // The Y location of line in block
    protected double _y = -1;

    // The width of this line
    protected double  _width = -1;

    // The height of this line
    protected double  _height = -1;

    // The TextMetrics
    private TextMetrics _textMetrics;

    // Constants
    protected static final TextRun[] EMPTY_RUNS = new TextRun[0];

    /**
     * Constructor.
     */
    public TextLine(TextBlock aTextBlock)
    {
        _textBlock = aTextBlock;
        _lineStyle = _textBlock.getDefaultLineStyle();
        addRun(createRun(), 0);
    }

    /**
     * Returns the TextBlock.
     */
    public TextBlock getTextBlock()  { return _textBlock; }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _sb.length(); }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _sb.charAt(anIndex); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)  { return _sb.subSequence(aStart, anEnd); }

    /**
     * Returns the index of given string in line.
     */
    public int indexOf(String aStr, int aStart)
    {
        if (SnapUtils.isTeaVM)
            return _sb.toString().indexOf(aStr, aStart);
        return _sb.indexOf(aStr, aStart);
    }

    /**
     * Returns the string for the line.
     */
    public String getString()  { return _sb.toString(); }

    /**
     * Returns the start char index of this line in text.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index of this line in text.
     */
    public int getEndCharIndex()  { return _startCharIndex + length(); }

    /**
     * Returns the index of this line in text.
     */
    public int getIndex()  { return _index; }

    /**
     * Adds characters with attributes to this line at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Add length to run
        TextRun run = getRun(0);
        run.addLength(theChars.length());

        // Add chars
        _sb.insert(anIndex, theChars);
        updateText();
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // Remove length from run
        TextRun run = getRun(0);
        run.addLength(aStart - anEnd);

        // Remove chars
        _sb.delete(aStart, anEnd);
        updateText();
    }

    /**
     * Returns the number of runs for this line.
     */
    public int getRunCount()  { return _runs.length; }

    /**
     * Returns the individual run at given index.
     */
    public TextRun getRun(int anIndex)  { return _runs[anIndex]; }

    /**
     * Returns the line runs.
     */
    public TextRun[] getRuns()  { return _runs; }

    /**
     * Adds a run to line.
     */
    protected void addRun(TextRun aRun, int anIndex)
    {
        _runs = ArrayUtils.add(_runs, aRun, anIndex);
        updateRuns(anIndex - 1);
    }

    /**
     * Removes the run at given index.
     */
    protected void removeRun(int anIndex)
    {
        _runs = ArrayUtils.remove(_runs, anIndex);
        updateRuns(anIndex - 1);
    }

    /**
     * Creates a new run.
     */
    protected TextRun createRun()
    {
        return new TextRun(this);
    }

    /**
     * Returns the head run for the line.
     */
    public TextRun getRunForCharIndex(int anIndex)
    {
        // Iterate over runs and return run containing char index
        TextRun[] runs = getRuns();
        for (TextRun run : runs)
            if (anIndex <= run.getEndCharIndex())
                return run;

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Returns the TextRun for the given char range (usually just run for start, but can be next run if at boundary).
     */
    public TextRun getRunForCharRange(int startIndex, int endIndex)
    {
        // Get run at start index
        TextRun textRun = getRunForCharIndex(startIndex);

        // If given non-empty range and startIndex is at end of normal run, get next
        if (endIndex > startIndex) {
            int runEnd = textRun.getEndCharIndex();
            if (startIndex == runEnd) {
                TextRun nextRun = textRun.getNext();
                if (nextRun != null)
                    textRun = nextRun;
            }
        }

        // Return
        return textRun;
    }

    /**
     * Returns the last run (or null if none).
     */
    public TextRun getLastRun()
    {
        int runCount = getRunCount();
        return runCount > 0 ? getRun(runCount - 1) : null;
    }

    /**
     * Sets the style for the line (propagates to runs).
     */
    protected void setStyle(TextStyle aStyle)
    {
        for (TextRun run : getRuns())
            run.setStyle(aStyle);
        updateLineStyle();
    }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()  { return _lineStyle; }

    /**
     * Sets the line style.
     */
    public void setLineStyle(TextLineStyle aLineStyle)
    {
        _lineStyle = aLineStyle;
    }

    /**
     * Returns the line x.
     */
    public double getX()  { return _x; }

    /**
     * Returns the line y.
     */
    public double getY()
    {
        // If already set, just return
        if (_y >= 0) return _y;

        // Get Y from previous line. Need to fix this to not stack overflow for large text showing tail first.
        double y = 0;
        TextLine previousLine = getPrevious();
        if (previousLine != null) {
            double prevY = previousLine.getY();
            double prevH = previousLine.getMetrics().getLineAdvance();
            y = prevY + prevH;
        }

        // Set and return
        return _y = y;
    }

    /**
     * Returns the width of line.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Get from runs
        double width = 0;
        for (TextRun run : _runs)
            width += run.getWidth();

        // Set, return
        return _width = width;
    }

    /**
     * Returns the height of line.
     */
    public double getHeight()
    {
        if (_height >= 0) return _height;
        double ascent = getMetrics().getAscent();
        double descent = getMetrics().getDescent();
        return _height = ascent + descent;
    }

    /**
     * Returns the width of line from given index.
     */
    public double getWidth(int anIndex)
    {
        // If index 0, use cached
        if (anIndex <= 0) return getWidth();

        // Calculate
        double width = 0;
        TextRun[] runs = getRuns();
        for (TextRun run : runs) {
            if (anIndex < run.getEndCharIndex())
                width += run.getWidth(anIndex - run.getStartCharIndex());
        }

        // Return
        return width;
    }

    /**
     * Returns the width without whitespace.
     */
    public double getWidthNoWhiteSpace()
    {
        int length = length();
        int lengthNoWhiteSpace = length;
        while (lengthNoWhiteSpace > 0 && Character.isWhitespace(charAt(lengthNoWhiteSpace - 1)))
            lengthNoWhiteSpace--;

        if (lengthNoWhiteSpace == length)
            return getWidth();

        return getXForCharIndex(lengthNoWhiteSpace) - getX();
    }

    /**
     * Returns the y position for this line (in same coords as the layout frame).
     */
    public double getBaseline()  { return getY() + getMetrics().getAscent(); }

    /**
     * Returns the max X.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Returns the line x in text block coords.
     */
    public double getTextX()  { return getX() + _textBlock.getX(); }

    /**
     * Returns the line y.
     */
    public double getTextY()  { return getY() + _textBlock.getAlignedY(); }

    /**
     * Returns the y position for this line (in same coords as the layout frame).
     */
    public double getTextBaseline()  { return getTextY() + getMetrics().getAscent(); }

    /**
     * Returns the max X.
     */
    public double getTextMaxX()  { return getTextX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    public double getTextMaxY()  { return getTextY() + getHeight(); }

    /**
     * Returns the x for tab at given x.
     */
    protected double getXForTabAtIndexAndX(int charIndex, double aX)
    {
        // Get tab position and type. If beyond stops, just bump by 4 spaces
        TextLineStyle lineStyle = getLineStyle();
        int tabIndex = lineStyle.getTabIndexForX(aX);
        if (tabIndex < 0) {
            TextRun textRun = getRunForCharIndex(charIndex);
            TextStyle textStyle = textRun.getStyle();
            return aX + textStyle.getCharAdvance(' ') * 4;
        }

        // Get tab position and type - If left-tab, just return tab position
        double tabX = lineStyle.getXForTabForX(aX);
        char tabType = lineStyle.getTabType(tabIndex);
        if (tabType == TextLineStyle.TAB_LEFT)
            return tabX;

        // Get width of characters after tab (until next tab, newline or decimal)
        TextRun textRun = getRunForCharIndex(charIndex);
        TextStyle textStyle = textRun.getStyle();
        int lineLength = length();
        double charsW = 0;
        for (int i = charIndex + 1; i < lineLength; i++) {
            char loopChar = charAt(i);
            if (loopChar == '\t' || loopChar == '\r' || loopChar == '\n')
                break;
            charsW += textStyle.getCharAdvance(loopChar) + textStyle.getCharSpacing();
            if (tabType == TextLineStyle.TAB_DECIMAL && loopChar == '.')
                break;
        }

        // If right or decimal, return tab position minus chars width (or tab char location if chars wider than tab stop)
        if (tabType == TextLineStyle.TAB_RIGHT || tabType == TextLineStyle.TAB_DECIMAL)
            return aX + charsW < tabX ? tabX - charsW : aX;

        // if centered, return tab position minus half chars width (or tab char location if chars wider than tab stop)
        return aX + charsW / 2 < tabX ? tabX - charsW / 2 : aX;
    }

    /**
     * Returns the number of tokens.
     */
    public int getTokenCount()  { return getTokens().length; }

    /**
     * Returns the individual token at given index.
     */
    public TextToken getToken(int anIndex)  { return getTokens()[anIndex]; }

    /**
     * Returns the tokens.
     */
    public TextToken[] getTokens()
    {
        // If already set, just return
        if (_tokens != null) return _tokens;

        // Create Tokens and set index for each
        TextToken[] tokens = createTokens();
        for (int i = 0; i < tokens.length; i++)
            tokens[i]._index = i;

        // Set, return
        return _tokens = tokens;
    }

    /**
     * Creates the tokens (via TextBlock.createTokensForTextLine() to provide another hook).
     */
    protected TextToken[] createTokens()
    {
        return _textBlock.createTokensForTextLine(this);
    }

    /**
     * Returns the last token.
     */
    public TextToken getLastToken()
    {
        TextToken[] tokens = getTokens();
        return tokens.length > 0 ? tokens[tokens.length - 1] : null;
    }

    /**
     * Returns the text metrics for line text.
     */
    public TextMetrics getMetrics()
    {
        if (_textMetrics != null) return _textMetrics;
        return _textMetrics = new TextMetrics(this);
    }

    /**
     * Returns the token at given char index.
     */
    public TextToken getTokenForCharIndex(int charIndex)
    {
        // Check bounds
        if (charIndex < 0 || charIndex > length())
            throw new IndexOutOfBoundsException("TextLine.getTokenForCharIndex: Index " + charIndex + " beyond " + length());

        // Get tokens
        TextToken[] tokens = getTokens();

        // Iterate over tokens (backwards) and return first token that starts at or before char index
        for (int i = tokens.length - 1; i >= 0; i--) {
            TextToken token = tokens[i];
            if (charIndex > token.getEndCharIndex())
                break;
            if (charIndex >= token.getStartCharIndex())
                return token;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the token at or before given char index.
     */
    public TextToken getLastTokenForCharIndex(int charIndex)
    {
        // Check bounds
        if (charIndex < 0 || charIndex > length())
            throw new IndexOutOfBoundsException("TextLine.getLastTokenForCharIndex: Index " + charIndex + " beyond " + length());

        // Get tokens
        TextToken[] tokens = getTokens();

        // Iterate over tokens (backwards) and return first token that starts at or before char index
        for (int i = tokens.length - 1; i >= 0; i--) {
            TextToken token = tokens[i];
            if (charIndex >= token.getStartCharIndex())
                return token;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the X coord for given char index.
     */
    public double getXForCharIndex(int anIndex)
    {
        // Get token for char index and token style
        TextToken textToken = getLastTokenForCharIndex(anIndex);
        TextStyle textStyle = textToken != null ? textToken.getTextStyle() : getRun(0).getStyle();
        double charSpacing = textStyle.getCharSpacing();

        // Init charX to token start X
        int startCharIndex = textToken != null ? textToken.getStartCharIndex() : 0;
        double charX = textToken != null ? textToken.getX() : 0;

        // Iterate over subsequent chars after token start and add advance
        for (int i = startCharIndex; i < anIndex; i++) {
            char loopChar = charAt(i);
            if (loopChar == '\t')
                charX = getXForTabAtIndexAndX(i, charX);
            else charX += textStyle.getCharAdvance(loopChar) + charSpacing;
        }

        // Return
        return charX;
    }

    /**
     * Returns the X coord for given char index.
     */
    public double getTextXForCharIndex(int anIndex)
    {
        return getTextX() + getXForCharIndex(anIndex);
    }

    /**
     * Returns the token at index.
     */
    public TextToken getTokenForX(double anX)
    {
        // Get tokens
        TextToken[] tokens = getTokens();
        double xInLineCoords = anX - getX();

        // Iterate over tokens (backwards) and return first token that starts at or before given X
        for (int i = tokens.length - 1; i >= 0; i--) {
            TextToken token = tokens[i];
            if (xInLineCoords >= token.getX())
                return token;
        }

        // Return null since given X is before first token
        return null;
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndexForX(double anX)
    {
        // Get token for x coord
        TextToken token = getTokenForX(anX);
        int charIndex = token != null ? token.getStartCharIndex() : 0;
        TextStyle textStyle = token != null ? token.getTextStyle() : getRun(0).getStyle();
        double charSpacing = textStyle.getCharSpacing();

        // Get char start X and line length
        double xInLineCoords = anX - getX();
        double charX = token != null ? token.getX() : 0;
        int lineLength = length();

        // Iterate over chars and return first char that contains given X
        while (charIndex < lineLength) {
            char loopChar = charAt(charIndex);
            double charW = textStyle.getCharAdvance(loopChar) + charSpacing;
            if (loopChar == '\t')
                charW = getXForTabAtIndexAndX(charIndex, charX) - charX;
            if (charX + charW / 2 > xInLineCoords)
                return charIndex;
            charIndex++;
            charX += charW;
        }

        // If at end of line with newline, back off 1
        if (isLastCharNewline())
            return lineLength - 1;

        // Return
        return lineLength;
    }

    /**
     * Returns the next line, if available.
     */
    public TextLine getNext()
    {
        int nextIndex = _index + 1;
        return _textBlock != null && nextIndex < _textBlock.getLineCount() ? _textBlock.getLine(nextIndex) : null;
    }

    /**
     * Returns the previous line, if available.
     */
    public TextLine getPrevious()
    {
        int prevIndex = _index - 1;
        return _textBlock != null && prevIndex >= 0 ? _textBlock.getLine(prevIndex) : null;
    }

    /**
     * Returns the alignment associated with this line.
     */
    public HPos getAlignX()  { return _lineStyle.getAlign(); }

    /**
     * Sets the alignment associated with this line.
     */
    public void setAlignX(HPos anAlign)
    {
        TextLineStyle lineStyle = getLineStyle().copyFor(anAlign);
        setLineStyle(lineStyle);
    }

    /**
     * Returns whether line contains an underlined run.
     */
    public boolean isUnderlined()
    {
        TextRun[] runs = getRuns();
        for (TextRun run : runs)
            if (run.isUnderlined() && run.length() > 0)
                return true;
        return false;
    }

    /**
     * Returns whether line ends with hyphen.
     */
    public boolean isHyphenated()
    {
        TextToken tok = getLastToken();
        return tok != null && tok.isHyphenated();
    }

    /**
     * Returns the max stroke width of any underlined chars in this line.
     */
    public double getUnderlineStroke()
    {
        double stroke = 0;
        for (TextRun run : getRuns())
            stroke = Math.max(stroke, run.getFont().getUnderlineThickness());
        return stroke;
    }

    /**
     * Returns the Y position of any underlined chars in this line.
     */
    public double getUnderlineY()
    {
        double y = 0;
        for (TextRun run : getRuns())
            y = Math.min(y, run.getFont().getUnderlineOffset());
        return y;
    }

    /**
     * Updates length due to change in given run.
     */
    protected void updateRuns(int aRunIndex)
    {
        // Get BaseRun and Length at end of BaseRun
        TextRun baseRun = aRunIndex >= 0 ? getRun(aRunIndex) : null;
        int length = baseRun != null ? baseRun.getEndCharIndex() : 0;

        // Iterate over runs beyond BaseRun and update Index, Start and Length
        for (int i = aRunIndex + 1, iMax = getRunCount(); i < iMax; i++) {
            TextRun run = getRun(i);
            run._index = i;
            run._startCharIndex = length;
            run._x = -1;
            length += run.length();
        }

        // Update line style
        updateLineStyle();
    }

    /**
     * Updates line style.
     */
    protected void updateLineStyle()
    {
        // Clear Width, Tokens
        _width = _height = -1;
        _tokens = null;
        _textMetrics = null;

        // Update Lines
        if (_textBlock != null)
            _textBlock.resetLineYForLinesAfterIndex(getIndex());
    }

    /**
     * Updates text.
     */
    protected void updateText()
    {
        // Clear Width, Tokens
        _width = _height = -1;
        _tokens = null;
        _textMetrics = null;

        // Update Lines
        if (_textBlock != null)
            _textBlock.updateLines(getIndex());
    }

    /**
     * Update line/token x for center/right alignment or justify.
     */
    protected void updateAlignmentAndJustify()
    {
        TextLineStyle lineStyle = getLineStyle();
        _x = 0;

        // If justify, shift tokens in line (unless line has newline or is last line in RichText)
        if (lineStyle.isJustify()) {

            boolean justifiable = getTokenCount() > 1 && _textBlock.getWidth() < 9999 && !isLastCharNewline() &&
                    getEndCharIndex() != _textBlock.length();
            if (!justifiable)
                return;

            // Calculate Justify token shift
            TextToken lastToken = getLastToken();
            double lineW = lastToken != null ? lastToken.getMaxX() : getWidth(); // getMaxX()
            double lineMaxW = _textBlock.getWidth(); //_textBlock.getMaxHitX(getY(), _height);
            double extraW = lineMaxW - lineW;
            double shiftX = extraW / (getTokenCount() - 1);
            double runningShiftX = 0;

            // Shift tokens
            for (TextToken token : getTokens()) {
                token._x += runningShiftX;
                runningShiftX += shiftX;
            }
        }

        // Calculate X alignment shift
        else if (lineStyle.getAlign() != HPos.LEFT && _textBlock.getWidth() < 9999) {
            TextToken lastToken = getLastToken();
            double lineW = lastToken != null ? lastToken.getMaxX() : getWidth(); // getMaxX()
            double lineMaxW = _textBlock.getWidth(); //_textBlock.getMaxHitX(getY(), _height);
            double extraW = lineMaxW - lineW;
            double alignX = lineStyle.getAlign().doubleValue();
            _x = Math.round(alignX * extraW);
        }
    }

    /**
     * Splits given run at given char index and returns the run containing the remaining chars (and identical attributes).
     */
    protected TextRun splitRunForCharIndex(TextRun aRun, int anIndex)
    {
        // Sanity check
        if (!_textBlock.isRichText())
            System.err.println("TextLine.splitRunForCharIndex: Should never get called for plain text");

        // Clone to get tail and delete chars from each
        TextRun remainder = aRun.clone();
        aRun.addLength(anIndex - aRun.length());
        remainder.addLength(-anIndex);

        // Add remainder and return
        addRun(remainder, aRun.getIndex() + 1);
        return remainder;
    }

    /**
     * Returns a copy of this line for given char range.
     */
    public TextLine copyForRange(int aStart, int aEnd)
    {
        // Do normal clone
        TextLine clone = clone();

        // Remove leading/trailing chars
        if (aEnd < length())
            clone.removeChars(aEnd, length());
        if (aStart > 0)
            clone.removeChars(0, aStart);

        // Return
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    public TextLine clone()
    {
        // Do normal version
        TextLine clone;
        try { clone = (TextLine) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Clone StringBuffer, Runs
        clone._sb = new StringBuffer(_sb);
        if (_runs != null) {
            clone._runs = _runs.clone();
            for (int i = 0; i < _runs.length; i++) {
                TextRun runClone = clone._runs[i] = _runs[i].clone();
                runClone._textLine = clone;
            }
        }

        // Return
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        StringBuilder sb = new StringBuilder();

        // Add Start, End, Length, Index, String
        sb.append("Start=").append(getStartCharIndex());
        sb.append(", End=").append(getEndCharIndex());
        sb.append(", Length=").append(length());
        sb.append(", Index=").append(getIndex());

        // Append String
        String string = getString().replace("\n", "\\n");
        sb.append(", String=").append(string);

        // Return
        return sb.toString();
    }
}
