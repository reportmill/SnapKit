/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;

/**
 * This class represents a line of text in a Text.
 */
public class TextLine implements CharSequence, Cloneable {

    // The TextDoc that contains this line
    protected TextDoc  _textDoc;

    // The StringBuffer that holds line chars
    protected StringBuffer  _sb = new StringBuffer();

    // The char index of the start of this line in text
    protected int  _startCharIndex;

    // The run for this line
    protected TextRun[]  _runs = EMPTY_RUNS;

    // The line style
    protected TextLineStyle  _lineStyle;

    // The index of this line in text
    protected int  _index;

    // The width of this line
    protected double  _width = -1;

    // The TextTokens for this line
    protected TextToken[]  _tokens;

    // Constants
    private static final TextRun[] EMPTY_RUNS = new TextRun[0];

    /**
     * Constructor.
     */
    public TextLine(TextDoc aTextDoc)
    {
        _textDoc = aTextDoc;
        _lineStyle = _textDoc.getDefaultLineStyle();
        addRun(createRun(), 0);
    }

    /**
     * Returns the TextDoc.
     */
    public TextDoc getTextDoc()  { return _textDoc; }

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
     * Sets the style for the line (propagates to runs).
     */
    protected void setStyle(TextStyle aStyle)
    {
        for (TextRun run : getRuns())
            run.setStyle(aStyle);
        _width = -1;
        _tokens = null;
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
     * Returns the width of line from given index.
     */
    public double getWidth(int anIndex)
    {
        // If index 0, use cached
        if (anIndex <= 0) return getWidth();

        // Calculate
        double width = 0;
        for (TextRun run : _runs)
            if (anIndex < run.getEnd())
                width += run.getWidth(anIndex - run.getStart());

        // Return
        return width;
    }

    /**
     * Returns the head run for the line.
     */
    public TextRun getRunForCharIndex(int anIndex)
    {
        // Iterate over runs and return run containing char index
        for (TextRun run : _runs)
            if (anIndex <= run.getEnd())
                return run;

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Returns the last run.
     */
    public TextRun getRunLast()
    {
        int runCount = getRunCount();
        return runCount > 0 ? getRun(runCount - 1) : null;
    }

    /**
     * Splits the line at given character index.
     */
    protected TextLine splitLineAtIndex(int anIndex)
    {
        TextLine remainder = clone();
        remainder.removeChars(0, anIndex);
        removeChars(anIndex, length());
        return remainder;
    }

    /**
     * Appends the given line to the end of this line.
     */
    protected void appendLine(TextLine aLine)
    {
        // Add chars
        _sb.append(aLine._sb);

        // Add runs
        for (int i = 0, iMax = aLine.getRunCount(); i < iMax; i++) {
            TextRun run = aLine.getRun(i);
            TextRun run2 = run.clone();
            run2._textLine = this;
            addRun(run2, getRunCount());
        }
    }

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
     * Returns the tokens.
     */
    public TextToken[] getTokens()
    {
        // If already set, just return
        if (_tokens != null) return _tokens;

        TextToken[] tokens = _textDoc.createTokensForTextLine(this);
        for (int i = 0; i < tokens.length; i++)
            tokens[i]._index = i;
        return _tokens = tokens;
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
     * Returns the token at given char index.
     */
    public TextToken getTokenForCharIndex(int charIndex)
    {
        // Get tokens
        TextToken[] tokens = getTokens();

        // Iterate over tokens and return first one in range
        for (TextToken token : tokens) {
            if (charIndex < token.getEndAllCharIndex())
                return token;
        }

        // If at end, return last token
        if (charIndex <= length())
            return getLastToken();

        // Complain
        throw new IndexOutOfBoundsException("TextLine.getTokenForCharIndex: Index " + charIndex + " beyond " + length());
    }

    /**
     * Returns the next line, if available.
     */
    public TextLine getNext()
    {
        int nextIndex = _index + 1;
        return _textDoc != null && nextIndex < _textDoc.getLineCount() ? _textDoc.getLine(nextIndex) : null;
    }

    /**
     * Returns the previous line, if available.
     */
    public TextLine getPrevious()
    {
        int prevIndex = _index - 1;
        return _textDoc != null && prevIndex >= 0 ? _textDoc.getLine(prevIndex) : null;
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
        for (TextRun run : _runs)
            if (run.isUnderlined() && run.length() > 0)
                return true;
        return false;
    }

    /**
     * Returns the last char.
     */
    public char getLastChar()
    {
        int len = length();
        return len > 0 ? charAt(len - 1) : 0;
    }

    /**
     * Returns whether run ends with newline.
     */
    public boolean isLastCharNewline()
    {
        char c = getLastChar();
        return c == '\r' || c == '\n';
    }

    /**
     * Updates length due to change in given run.
     */
    protected void updateRuns(int aRunIndex)
    {
        // Get BaseRun and Length at end of BaseRun
        TextRun baseRun = aRunIndex >= 0 ? getRun(aRunIndex) : null;
        int length = baseRun != null ? baseRun.getEnd() : 0;

        // Iterate over runs beyond BaseRun and update Index, Start and Length
        for (int i = aRunIndex + 1, iMax = getRunCount(); i < iMax; i++) {
            TextRun run = getRun(i);
            run._index = i;
            run._start = length;
            length += run.length();
        }

        // Clear Width, Tokens
        _width = -1;
        _tokens = null;
    }

    /**
     * Updates text.
     */
    protected void updateText()
    {
        // Clear Width, Tokens
        _width = -1;
        _tokens = null;

        // Update Lines
        if (_textDoc != null)
            _textDoc.updateLines(getIndex());
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
