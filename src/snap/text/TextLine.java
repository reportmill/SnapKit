/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.gfx.Border;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.util.ArrayUtils;
import snap.util.CharSequenceX;

/**
 * This class represents a line of text in a Text.
 */
public class TextLine implements CharSequenceX, Cloneable {

    // The TextModel that contains this line
    protected TextModel _textModel;

    // The line chars (either String or StringBuilder)
    protected CharSequence _chars = "";

    // The char index of the start of this line in text
    protected int  _startCharIndex;

    // The run for this line
    protected TextRun[]  _runs = EMPTY_RUNS;

    // The TextTokens for this line
    protected TextToken[]  _tokens;

    // The line style
    protected TextLineStyle  _lineStyle;

    // The index of this line in text
    protected int _lineIndex;

    // The line bounds
    protected double _x, _y, _width, _height;

    // The TextMetrics
    private TextMetrics _textMetrics;

    // Constants
    protected static final TextRun[] EMPTY_RUNS = new TextRun[0];

    /**
     * Constructor.
     */
    public TextLine(TextModel textModel)
    {
        _textModel = textModel;
        _lineStyle = _textModel.getDefaultLineStyle();
        addRun(createRun(), 0);
        _y = _width = _height = -1;
    }

    /**
     * Returns the TextModel.
     */
    public TextModel getTextModel()  { return _textModel; }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _chars.length(); }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _chars.charAt(anIndex); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)  { return _chars.subSequence(aStart, anEnd); }

    /**
     * Returns the index of given string in line.
     */
    public int indexOf(String aStr, int aStart)
    {
        if (_chars instanceof StringBuilder stringBuilder)
            return stringBuilder.indexOf(aStr, aStart);
        return _chars.toString().indexOf(aStr, aStart);
    }

    /**
     * Returns the characters for line.
     */
    public CharSequence getChars()  { return _chars; }

    /**
     * Returns whether text is blank.
     */
    public boolean isBlank()
    {
        if (_chars instanceof String string)
            return string.isBlank();
        for (int i = 0, iMax = _chars.length(); i < iMax; i++)
            if (!Character.isWhitespace(_chars.charAt(i)))
                return false;
        return true;
    }

    /**
     * Returns the string for the line.
     */
    public String getString()  { return _chars.toString(); }

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
    public int getLineIndex()  { return _lineIndex; }

    /**
     * Adds characters with text style to this line at given index.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle aStyle, int anIndex)
    {
        // Add length to run
        TextRun run = getRunForCharIndexAndStyle(anIndex, aStyle);
        run.addLength(theChars.length());

        // Add chars
        if (anIndex == 0 && _chars.isEmpty())
            _chars = theChars.toString();
        else {
            if (_chars instanceof String) _chars = new StringBuilder(_chars);
            ((StringBuilder)_chars).insert(anIndex, theChars);
        }

        // Update runs and text
        updateRuns(run.getIndex());
        updateText();
    }

    /**
     * Returns the run to add chars to for given style and char index.
     * Will try to use any adjacent run with conforming style, otherwise, will create/add new.
     */
    public TextRun getRunForCharIndexAndStyle(int charIndex, TextStyle aStyle)
    {
        // Get run at index (just return if style is null or equal)
        TextRun run = getRunForCharIndex(charIndex);
        if (aStyle == null || aStyle.equals(run.getTextStyle()))
            return run;

        // If trying to add new style to plain text, complain
        if (!_textModel.isRichText()) {
            System.out.println("TextLine.getRunForCharIndexAndStyle: Trying to add new style to plain text");
            return run;
        }

        // If empty, just set style and return
        if (run.isEmpty()) {
            run.setTextStyle(aStyle);
            return run;
        }

        // If charIndex at run end and next run has same style, return it instead
        if (charIndex == run.getEndCharIndex()) {
            TextRun nextRun = run.getNext();
            if (nextRun != null && aStyle.equals(nextRun.getTextStyle()))
                return nextRun;
        }

        // Get index to insert new run (need to split run if charIndex in middle)
        int newRunIndex = run.getIndex();
        if (charIndex > run.getStartCharIndex()) {
            newRunIndex++;
            if (charIndex < run.getEndCharIndex())
                splitRunForCharIndex(run, charIndex - run.getStartCharIndex());
        }

        // Create new run for new chars, add and return
        TextRun newRun = createRun();
        newRun.setTextStyle(aStyle);
        addRun(newRun, newRunIndex);
        return newRun;
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // If empty range, just return
        if (anEnd == aStart) return;

        // Make sure chars are StringBuilder
        if (!(_chars instanceof StringBuilder))
            _chars = new StringBuilder(_chars);

        // Handle plain text: Just remove length from run and chars from string and update text
        if (!_textModel.isRichText()) {
            TextRun run = getRun(0);
            run.addLength(aStart - anEnd);
            ((StringBuilder) _chars).delete(aStart, anEnd);
        }

        // Handle RichText: Iterate over effected runs and remove chars
        else {
            int end = anEnd;
            while (aStart < end) {

                // Get run at end
                TextRun run = getRunForCharIndex(end);
                int runStart = run.getStartCharIndex();
                int start = Math.max(aStart, runStart);

                // If range matches run range, just remove it
                if (start == runStart && end == run.getEndCharIndex() && getRunCount() > 1) {
                    int runIndex = run.getIndex();
                    removeRun(runIndex);
                    ((StringBuilder) _chars).delete(start, end);
                    updateRuns(runIndex - 1);
                }

                // Otherwise delete chars from run
                else {
                    run.addLength(start - end);
                    ((StringBuilder) _chars).delete(start, end);
                    updateRuns(run.getIndex());
                }

                // Reset end to runStart
                end = runStart;
            }
        }

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
    protected TextRun createRun()  { return new TextRun(this); }

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
    protected void setTextStyle(TextStyle textStyle)
    {
        for (TextRun run : getRuns())
            run.setTextStyle(textStyle);
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

        // Get previous line - if this is first line, just return/set 0
        TextLine previousLine = getPrevious();
        if (previousLine == null)
            return _y = 0;

        // If previous line Y not yet defined, define lines in a non-recursive way to avoid stack overflow
        if (previousLine._y < 0)
            defineLineYsNoRecursion();

        // Get Y from previous line Y + advance
        double prevY = previousLine.getY();
        double prevH = previousLine.getMetrics().getLineAdvance();
        return _y = prevY + prevH;
    }

    /**
     * Finds the last line with defined Y and define forward to avoid recursion and stack overflow.
     */
    private void defineLineYsNoRecursion()
    {
        // Get last defined line index
        int lineIndex = getLineIndex();
        int lastDefinedLineIndex = lineIndex - 1;
        while (lastDefinedLineIndex > 0 && _textModel.getLine(lastDefinedLineIndex)._y < 0)
            lastDefinedLineIndex--;

        // Define line Y's forward to this line
        while (lastDefinedLineIndex != lineIndex)
            _textModel.getLine(lastDefinedLineIndex++).getY();
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
     * Returns the max X.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Returns the width of line from given index.
     */
    public double getWidthForStartCharIndex(int startCharIndex)
    {
        // If index 0, return cached version
        if (startCharIndex <= 0)
            return getWidth();

        // Calculate
        double width = 0;
        TextRun[] runs = getRuns();
        for (TextRun run : runs) {
            if (startCharIndex < run.getEndCharIndex())
                width += run.getWidthForStartCharIndex(startCharIndex - run.getStartCharIndex());
        }

        // Return
        return width;
    }

    /**
     * Returns the width of the trailing whitespace.
     */
    public double getTrailingWhitespaceWidth()
    {
        TextRun lastRun = getLastRun();
        return lastRun.getTrailingWhitespaceWidth();
    }

    /**
     * Returns the y position for this line (in same coords as the layout frame).
     */
    public double getBaseline()  { return getY() + getMetrics().getAscent(); }

    /**
     * Returns the line x in text model coords.
     */
    public double getTextX()  { return getX() + _textModel.getX(); }

    /**
     * Returns the line y.
     */
    public double getTextY()  { return getY() + _textModel.getAlignedY(); }

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
            TextStyle textStyle = textRun.getTextStyle();
            return aX + textStyle.getCharAdvance(' ') * 4;
        }

        // Get tab position and type - If left-tab, just return tab position
        double tabX = lineStyle.getXForTabForX(aX);
        char tabType = lineStyle.getTabType(tabIndex);
        if (tabType == TextLineStyle.TAB_LEFT)
            return tabX;

        // Get width of characters after tab (until next tab, newline or decimal)
        TextRun textRun = getRunForCharIndex(charIndex);
        TextStyle textStyle = textRun.getTextStyle();
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
     * Creates the tokens (via TextModel.createTokensForTextLine() to provide another hook).
     */
    protected TextToken[] createTokens()
    {
        return _textModel.createTokensForTextLine(this);
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
            if (charIndex > token.getEndCharIndexInLine())
                break;
            if (charIndex >= token.getStartCharIndexInLine())
                return token;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the token at or after given char index.
     */
    public TextToken getNextTokenForCharIndex(int charIndex)
    {
        // Check bounds
        if (charIndex < 0 || charIndex > length())
            throw new IndexOutOfBoundsException("TextLine.getNextTokenForCharIndex: Index " + charIndex + " beyond " + length());

        // Get tokens
        TextToken[] tokens = getTokens();
        return ArrayUtils.findMatch(tokens, token -> charIndex <= token.getStartCharIndexInLine());
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
            if (charIndex >= token.getStartCharIndexInLine())
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
        TextStyle textStyle = textToken != null ? textToken.getTextStyle() : getRun(0).getTextStyle();
        double charSpacing = textStyle.getCharSpacing();

        // Init charX to token start X
        int startCharIndex = textToken != null ? textToken.getStartCharIndexInLine() : 0;
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
        int charIndex = token != null ? token.getStartCharIndexInLine() : 0;
        TextStyle textStyle = token != null ? token.getTextStyle() : getRun(0).getTextStyle();
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
        int nextIndex = _lineIndex + 1;
        return nextIndex < _textModel.getLineCount() ? _textModel.getLine(nextIndex) : null;
    }

    /**
     * Returns the previous line, if available.
     */
    public TextLine getPrevious()
    {
        int prevIndex = _lineIndex - 1;
        return prevIndex >= 0 ? _textModel.getLine(prevIndex) : null;
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
        TextLineStyle lineStyle = getLineStyle().copyForAlign(anAlign);
        setLineStyle(lineStyle);
    }

    /**
     * Returns whether line contains an underlined run.
     */
    public boolean isUnderlined()
    {
        if (!_textModel.isRichText())
            return getRun(0).isUnderlined() && !isEmpty();
        TextRun[] runs = getRuns();
        return ArrayUtils.hasMatch(runs, run -> run.isUnderlined() && !run.isEmpty());
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
        if (_textModel instanceof TextBlock textBlock)
            textBlock.resetLineYForLinesAfterIndex(getLineIndex());
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
        if (_textModel instanceof TextBlock textBlock)
            textBlock.updateLines(getLineIndex());
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

            boolean justifiable = getTokenCount() > 1 && _textModel.getWidth() < 9999 && !isLastCharNewline() &&
                    getEndCharIndex() != _textModel.length();
            if (!justifiable)
                return;

            // Calculate Justify token shift
            TextToken lastToken = getLastToken();
            double lineW = lastToken != null ? lastToken.getMaxX() : getWidth(); // getMaxX()
            double lineMaxW = _textModel.getWidth(); //_textModel.getMaxHitX(getY(), _height);
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
        else if (lineStyle.getAlign() != HPos.LEFT && _textModel.getWidth() < 9999) {
            TextToken lastToken = getLastToken();
            double lineW = lastToken != null ? lastToken.getMaxX() : getWidth(); // getMaxX()
            double lineMaxW = _textModel.getWidth(); //_textModel.getMaxHitX(getY(), _height);
            double extraW = Math.max(lineMaxW - lineW, 0);
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
        if (!_textModel.isRichText())
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
     * Splits this line at given character index and adds remainder to text and returns it.
     */
    protected TextLine splitLineAtIndex(int anIndex)
    {
        TextLine remainderLine = clone();
        removeChars(anIndex, length());
        remainderLine.removeChars(0, anIndex);
        return remainderLine;
    }

    /**
     * Paint text line with given painter.
     */
    public void paint(Painter aPntr)
    {
        if (isBlank())
            return;

        // Save painter state and clip
        aPntr.save();
        aPntr.clipRect(getTextX(), getTextY(), getWidth(), getHeight());

        // Paint line
        paintLine(aPntr);

        // Restore state
        aPntr.restore();
    }

    /**
     * Paint text line with given painter.
     */
    public void paintLine(Painter aPntr)
    {
        TextToken[] lineTokens = getTokens();
        double lineY = getBaseline() + _textModel.getAlignedY();

        // Iterate over line tokens
        for (TextToken token : lineTokens) {

            // Set token font and color
            aPntr.setFont(token.getFont());
            aPntr.setPaint(token.getTextColor());

            // Do normal paint token
            String tokenStr = token.getString();
            double tokenX = token.getTextX();
            double charSpacing = token.getTextStyle().getCharSpacing();
            aPntr.drawString(tokenStr, tokenX, lineY, charSpacing);

            // Handle TextBorder: Get outline and stroke
            Border border = token.getTextStyle().getBorder();
            if (border != null) {
                aPntr.setPaint(border.getColor());
                aPntr.setStroke(border.getStroke());
                aPntr.strokeString(tokenStr, tokenX, lineY, charSpacing);
            }
        }

        // If underlined, paint underlines
        if (isUnderlined())
            paintUnderlines(aPntr);
    }

    /**
     * Paints text line underlines with given painter.
     */
    private void paintUnderlines(Painter aPntr)
    {
        for (TextRun run : getRuns()) {
            if (!run.isUnderlined() || run.isEmpty())
                continue;

            // Set underline color and width
            Font font = run.getFont();
            double underlineOffset = Math.ceil(Math.abs(font.getUnderlineOffset()));
            double underlineThickness = font.getUnderlineThickness();
            aPntr.setColor(run.getColor());
            aPntr.setStrokeWidth(underlineThickness);

            // Get underline endpoints and draw line
            double lineX = getTextX() + run.getX();
            double lineMaxX = lineX + run.getWidth() - run.getTrailingWhitespaceWidth();
            double lineY = getTextBaseline() + underlineOffset;
            aPntr.drawLine(lineX, lineY, lineMaxX, lineY);
        }
    }

    /**
     * Standard clone implementation.
     */
    @Override
    protected TextLine clone()
    {
        // Do normal version
        TextLine clone;
        try { clone = (TextLine) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Clone chars, Runs
        clone._chars = _chars.toString();
        clone._runs = _runs.clone();
        for (int i = 0; i < _runs.length; i++) {
            TextRun runClone = clone._runs[i] = _runs[i].clone();
            runClone._textLine = clone;
        }

        // Return
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        // Get props string for props: Start, End, Length, Index, String
        String propsStr = "Start=" + getStartCharIndex() + ", End=" + getEndCharIndex() +
            ", Length=" + length() + ", Index=" + getLineIndex() +
            ", String=" + getString().replace("\n", "\\n");

        // Return string
        return getClass().getSimpleName() + " { " + propsStr + " }";
    }
}
