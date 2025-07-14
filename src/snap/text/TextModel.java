/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.VPos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.props.PropObject;
import snap.util.*;
import snap.web.WebFile;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public class TextModel extends PropObject implements CharSequenceX, Cloneable, XMLArchiver.Archivable {

    // The URL of the file that provided the text
    private WebURL _sourceUrl;

    // Whether text is rich
    private boolean _rich;

    // The TextLines in this text
    protected List<TextLine>  _lines = new ArrayList<>();

    // The length of this text
    protected int  _length;

    // The default text style for this text
    protected TextStyle _defaultTextStyle = TextStyle.DEFAULT;

    // The default line style for this text
    protected TextLineStyle  _defaultLineStyle = TextLineStyle.DEFAULT;

    // Whether text is modified
    private boolean _textModified;

    // The X/Y of the text model
    private double _x, _y;

    // The width/height of the text model
    private double _width = Float.MAX_VALUE, _height;

    // The pref width of the text model
    protected double _prefW = -1;

    // They y alignment
    private VPos _alignY = VPos.TOP;

    // The y alignment amount
    private double _alignedY = -1;

    // Whether property change is enabled
    protected boolean  _propChangeEnabled = true;

    // The last mouse Y, to help in caret placement (can be ambiguous for start/end of line)
    protected double _mouseY;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";
    public static final String DefaultTextStyle_Prop = "DefaultTextStyle";
    public static final String DefaultLineStyle_Prop = "DefaultLineStyle";
    public static final String TextModified_Prop = "TextModified";

    /**
     * Constructor.
     */
    public TextModel()
    {
        this(false);
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextModel(boolean isRich)
    {
        super();
        _rich = isRich;
        TextLine defaultLine = new TextLine(this);
        addLine(defaultLine, 0);
    }

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    public boolean isRichText()  { return _rich; }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRichText()) return;

        // Set value
        _rich = aValue;

        // Set DefaultStyle, because RichText never inherits from parent
        if (aValue && _defaultTextStyle == null)
            _defaultTextStyle = TextStyle.DEFAULT;
    }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return false; }

    /**
     * Returns the number of characters in the text.
     */
    public int length()  { return _length; }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        TextLine line = getLineForCharIndex(anIndex);
        return line.charAt(anIndex - line.getStartCharIndex());
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        StringBuffer sb = new StringBuffer(anEnd - aStart);
        TextLine line = getLineForCharIndex(aStart);
        while (aStart < anEnd) {
            int end = Math.min(line.getEndCharIndex(), anEnd);
            sb.append(line.subSequence(aStart - line.getStartCharIndex(), end - line.getStartCharIndex()));
            aStart = end;
            line = line.getNext();
        }

        // Return
        return sb;
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder(length());
        for (TextLine line : _lines)
            sb.append(line._sb);

        // Return
        return sb.toString();
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        String string = aString != null ? aString : "";

        if (!string.contentEquals(this)) {
            setPropChangeEnabled(false);
            replaceChars(string, 0, length());
            setPropChangeEnabled(true);
        }

        setTextModified(false);
    }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultTextStyle()  { return _defaultTextStyle; }

    /**
     * Sets the default text style.
     */
    public void setDefaultTextStyle(TextStyle textStyle)
    {
        // If already set, just return
        if (Objects.equals(textStyle, _defaultTextStyle)) return;

        // Set
        TextStyle oldStyle = _defaultTextStyle;
        _defaultTextStyle = textStyle;

        // Update existing lines
        if (!isRichText() || length() == 0) {
            for (TextLine line : getLines())
                line.setTextStyle(textStyle);
        }

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(DefaultTextStyle_Prop, oldStyle, textStyle);
    }

    /**
     * Sets default text style for given style string.
     */
    public void setDefaultTextStyleString(String styleString)
    {
        TextStyle textStyle = getDefaultTextStyle();
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        setDefaultTextStyle(textStyle2);
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _defaultLineStyle; }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle lineStyle)
    {
        // If already set, just return
        if (Objects.equals(lineStyle, _defaultLineStyle)) return;

        // Set
        TextLineStyle oldStyle = _defaultLineStyle;
        _defaultLineStyle = lineStyle;

        // Upgrade existing lines
        if (!isRichText() || length() == 0) {
            for (TextLine line : getLines())
                line.setLineStyle(lineStyle);
        }

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(DefaultLineStyle_Prop, oldStyle, lineStyle);
    }

    /**
     * Returns the default font.
     */
    public Font getDefaultFont()  { return _defaultTextStyle.getFont(); }

    /**
     * Sets the default font.
     */
    public void setDefaultFont(Font aFont)
    {
        if (aFont.equals(getDefaultFont())) return;
        TextStyle newTextStyle = _defaultTextStyle.copyForStyleValue(aFont);
        setDefaultTextStyle(newTextStyle);
    }

    /**
     * Returns the default text color.
     */
    public Color getDefaultTextColor()  { return _defaultTextStyle.getColor(); }

    /**
     * Sets the default text color.
     */
    public void setDefaultTextColor(Color aColor)
    {
        if (aColor == null) aColor = Color.BLACK;
        if (aColor.equals(getDefaultTextColor())) return;
        TextStyle newTextStyle = _defaultTextStyle.copyForStyleValue(aColor);
        setDefaultTextStyle(newTextStyle);
    }

    /**
     * Returns whether text is modified.
     */
    public boolean isTextModified()  { return _textModified; }

    /**
     * Sets whether text is modified.
     */
    public void setTextModified(boolean aValue)
    {
        if (aValue == isTextModified()) return;
        firePropChange(TextModified_Prop, _textModified, _textModified = aValue);
    }

    /**
     * Adds characters to this text.
     */
    public void addChars(CharSequence theChars)
    {
        addCharsWithStyle(theChars, null, length());
    }

    /**
     * Adds characters to this text at given index.
     */
    public void addChars(CharSequence theChars, int anIndex)
    {
        addCharsWithStyle(theChars, null, anIndex);
    }

    /**
     * Adds characters with given style to this text.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle)
    {
        addCharsWithStyle(theChars, theStyle, length());
    }

    /**
     * Adds characters with given style to this text at given index.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        addCharsWithStyleImpl(theChars, theStyle, anIndex);
    }

    /**
     * Adds characters with given style to this text at given index.
     */
    protected void addCharsWithStyleImpl(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // If no chars, just return
        if (theChars == null) return;

        // Get line for char index
        TextLine textLine = getLineForCharIndex(anIndex);

        // If adding at text end and last line and ends with newline, create/add new line (should never happen)
        if (anIndex == textLine.getEndCharIndex() && textLine.isLastCharNewline())
            textLine = splitLineAtIndex(textLine, textLine.length());

        // Loop vars
        int charsLength = theChars.length();
        int charIndexInChars = 0;

        // Iterate over chars until all added
        while (charIndexInChars < charsLength) {

            // Get chars up to first newline or end
            CharSequence chars = theChars;
            int newlineIndex = CharSequenceUtils.indexAfterNewline(theChars, charIndexInChars);
            int endCharIndexInChars = newlineIndex > 0 ? newlineIndex : charsLength;
            if (charIndexInChars > 0 || endCharIndexInChars < charsLength)
                chars = theChars.subSequence(charIndexInChars, endCharIndexInChars);

            // Get char index and update line
            int charIndex = anIndex + charIndexInChars;
            if (charIndexInChars > 0) {
                while (charIndex > textLine.getEndCharIndex() || charIndex == textLine.getEndCharIndex() && textLine.isLastCharNewline())
                    textLine = textLine.getNext();
            }

            // Add chars to line
            addCharsToLine(chars, theStyle, charIndex, textLine, newlineIndex > 0);

            // Set start to last end
            charIndexInChars += chars.length();
        }

        // Send PropertyChange
        if (isPropChangeEnabled())
            firePropChange(new TextModelUtils.CharsChange(this, null, theChars, anIndex));
        _prefW = -1;
    }

    /**
     * Adds a block of chars to line - each block is guaranteed to either have no newlines or end with newline.
     */
    protected void addCharsToLine(CharSequence theChars, TextStyle theStyle, int charIndex, TextLine textLine, boolean charsHaveNewline)
    {
        // Add chars to line
        int charIndexInLine = charIndex - textLine.getStartCharIndex();
        textLine.addCharsWithStyle(theChars, theStyle, charIndexInLine);

        // If chars have newline, move chars after newline to next line
        if (charsHaveNewline) {
            int moveCharsIndexInLine = charIndexInLine + theChars.length();
            moveLineCharsToNextLine(textLine, moveCharsIndexInLine);
        }

        // Reset line alignment
        textLine._x = 0;

        // Perform post processing
        addCharsToLineFinished(textLine);
    }

    /**
     * Called after chars added to line to do further processing, like horizontal alignment or wrapping.
     */
    protected void addCharsToLineFinished(TextLine textLine)
    {
        textLine.updateAlignmentAndJustify();
    }

    /**
     * Move line chars from given start char index to line end to next line.
     */
    protected void moveLineCharsToNextLine(TextLine textLine, int startCharIndex)
    {
        // Get next line to move chars to
        TextLine nextLine = textLine.getNext();

        // If no next line or moving chars + newline, clone line and remove chars
        if (nextLine == null || startCharIndex < textLine.length() && textLine.isLastCharNewline()) {
            nextLine = textLine.clone();
            nextLine.removeChars(0, nextLine.length());
            addLine(nextLine, textLine.getLineIndex() + 1);
        }

        // Get last run
        TextRun lastRun = textLine.getLastRun();

        // Iterate over runs from end of line, moving chars from each to next line
        while (textLine.length() > startCharIndex) {

            // Remove run chars from text line (chars after startCharIndex)
            int runStartCharIndex = Math.max(startCharIndex, lastRun.getStartCharIndex());
            CharSequence moveChars = textLine.subSequence(runStartCharIndex, textLine.length());
            textLine.removeChars(runStartCharIndex, textLine.length());

            // Add run chars to next line
            TextStyle textStyle = lastRun.getTextStyle();
            int nextLineStartCharIndex = nextLine.getStartCharIndex();
            boolean charsHaveNewline = CharSequenceUtils.indexAfterNewline(moveChars, 0) > 0;
            addCharsToLine(moveChars, textStyle, nextLineStartCharIndex, nextLine, charsHaveNewline);

            // Get previous run
            lastRun = lastRun.getPrevious();
        }
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStartCharIndex, int anEndCharIndex)
    {
        // If empty range, just return
        if (anEndCharIndex == aStartCharIndex) return;

        // If PropChangeEnabled, get chars to be deleted
        CharSequence removedChars = isPropChangeEnabled() ? subSequence(aStartCharIndex, anEndCharIndex) : null;

        // Delete lines/chars for range from end to start
        int removeEndCharIndex = anEndCharIndex;
        while (removeEndCharIndex > aStartCharIndex) {

            // Get line at end index
            TextLine textLine = getLineForCharIndex(removeEndCharIndex);
            if (removeEndCharIndex == textLine.getStartCharIndex())
                textLine = textLine.getPrevious();

            // Get Line.Start
            int lineStartCharIndex = textLine.getStartCharIndex();
            int removeStartCharIndex = Math.max(aStartCharIndex, lineStartCharIndex);
            removeCharsFromLine(removeStartCharIndex, removeEndCharIndex, textLine);

            // Reset end
            removeEndCharIndex = lineStartCharIndex;
        }

        // If deleted chars is set, send property change
        if (removedChars != null && isPropChangeEnabled())
            firePropChange(new TextModelUtils.CharsChange(this, removedChars, null, aStartCharIndex));
        _prefW = -1;
    }

    /**
     * Remove chars from line.
     */
    protected void removeCharsFromLine(int startCharIndex, int endCharIndex, TextLine textLine)
    {
        // Simple case: If range is whole line, can just remove line if more than one line ...
        int lineStartCharIndex = textLine.getStartCharIndex();
        if (startCharIndex == lineStartCharIndex && endCharIndex == textLine.getEndCharIndex() && getLineCount() > 1) {

            // ... and either (1) line is first line or (2) previous line wrapped or (3) not last line
            TextLine previousLine = textLine.getPrevious();
            if (previousLine == null || !previousLine.isLastCharNewline() || textLine != getLastLine()) {
                removeLine(textLine.getLineIndex());
                return;
            }
        }

        // Remove chars from line
        int startCharIndexInLine = startCharIndex - lineStartCharIndex;
        int endCharIndexInLine = endCharIndex - lineStartCharIndex;
        textLine.removeChars(startCharIndexInLine, endCharIndexInLine);

        // If no newline remaining in line, join with next line
        if (!textLine.isLastCharNewline())
            joinLineWithNextLine(textLine);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, int aStart, int anEnd)
    {
        replaceCharsWithStyle(theChars, null, aStart, anEnd);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        // Get TextStyle for add chars range (if not provided)
        TextStyle style = theStyle;
        if (style == null)
            style = getTextStyleForCharRange(aStart, anEnd);

        // Remove given range and add chars
        if (anEnd > aStart)
            removeChars(aStart, anEnd);
        addCharsWithStyle(theChars, style, aStart);
    }

    /**
     * Sets the given text style for given range.
     */
    public void setTextStyle(TextStyle textStyle, int aStart, int anEnd)
    {
        // If plaint text, just return (can't apply style to range for plain text)
        if (!isRichText()) return;

        // Get run iter and split end runs
        TextRunIter runIter = getRunIterForCharRange(aStart, anEnd);
        runIter.splitEndRuns();
        TextLine startLine = runIter.getLine();

        // Iterate over runs and reset style
        while (runIter.hasNextRun()) {

            // Set style
            TextRun textRun = runIter.getNextRun();
            TextStyle oldStyle = textRun.getTextStyle();
            textRun.setTextStyle(textStyle);

            // Fire prop change
            if (isPropChangeEnabled()) {
                int lineStartCharIndex = textRun.getLine().getStartCharIndex();
                int runStart = textRun.getStartCharIndex() + lineStartCharIndex;
                int runEnd = textRun.getEndCharIndex() + lineStartCharIndex;
                PropChange pc = new TextModelUtils.StyleChange(this, oldStyle, textStyle, runStart, runEnd);
                firePropChange(pc);
            }
        }

        // Update lines
        startLine.updateText();
        _prefW = -1;
    }

    /**
     * Sets a text style value for given key, value and range.
     */
    public void setTextStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // If plaint text, just return (can't apply style to range for plain text)
        if (!isRichText()) return;

        // Iterate over lines in range and set attribute
        while (aStart < anEnd) {

            // Get run for range
            TextRun textRun = getRunForCharRange(aStart, anEnd);
            TextLine textLine = textRun.getLine();
            int lineStart = textLine.getStartCharIndex();
            int runEndInText = textRun.getEndCharIndex() + lineStart;
            int newStyleEndInText = Math.min(runEndInText, anEnd);

            // Get current run style, get new style for given key/value
            TextStyle style = textRun.getTextStyle();
            TextStyle newStyle = style.copyForStyleKeyValue(aKey, aValue);

            // Set new style for run range
            setTextStyle(newStyle, aStart, newStyleEndInText);

            // Reset start to run end
            aStart = runEndInText;
        }
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Handle Rich
        if (isRichText()) {
            setLineStyleRich(aStyle, aStart, anEnd);
            return;
        }

        // Handle Plain: Propagate to Lines
        TextLineStyle oldStyle = getLine(0).getLineStyle();
        if (aStyle.equals(oldStyle))
            return;

        // Propagate to all lines
        for (TextLine line : getLines())
            line.setLineStyle(aStyle);

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, aStyle, 0));

        _prefW = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Handle Rich
        if (isRichText())
            setLineStyleValueRich(aKey, aValue, aStart, anEnd);

        // Handle plain
        else {
            TextLineStyle oldStyle = getLine(0).getLineStyle();
            TextLineStyle newStyle = oldStyle.copyForPropKeyValue(aKey, aValue);
            setLineStyle(newStyle, 0, length());
        }

        _prefW = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    protected void setLineStyleRich(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Get start/end line indexes for char range
        int startLineIndex = getLineForCharIndex(aStart).getLineIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getLineIndex();

        // Iterate over lines and set new style
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            if (!aStyle.equals(oldStyle)) {
                line.setLineStyle(aStyle);
                if (isPropChangeEnabled())
                    firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, aStyle, i));
            }
        }
    }

    /**
     * Sets a given style to a given range.
     */
    protected void setLineStyleValueRich(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Get start/end line indexes
        int startLineIndex = getLineForCharIndex(aStart).getLineIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getLineIndex();

        // Iterate over lines and set value independently for each
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            TextLineStyle newStyle = oldStyle.copyForPropKeyValue(aKey, aValue);
            if (!newStyle.equals(oldStyle)) {
                line.setLineStyle(newStyle);
                if (isPropChangeEnabled())
                    firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, newStyle, i));
            }
        }
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public TextLine getLine(int anIndex)  { return _lines.get(anIndex); }

    /**
     * Returns the list of blocks.
     */
    public List<TextLine> getLines()  { return _lines; }

    /**
     * Adds a block at given index.
     */
    protected void addLine(TextLine aLine, int anIndex)
    {
        _lines.add(anIndex, aLine);
        aLine._textModel = this;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected void removeLine(int anIndex)
    {
        TextLine line = _lines.remove(anIndex);
        line._textModel = null;
        updateLines(anIndex - 1);
    }

    /**
     * Returns the longest line.
     */
    public TextLine getLineLongest()
    {
        TextLine longLine = null;
        double longW = 0;
        for (TextLine line : _lines) {
            if (line.getWidth() > longW) {
                longLine = line;
                longW = line.getWidth();
            }
        }

        // Return
        return longLine;
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        // Remove chars and reset style
        removeChars(0, length());
        setTextStyle(getDefaultTextStyle(), 0, 0);
        setLineStyle(getDefaultLineStyle(), 0, 0);
    }

    /**
     * Returns the block at the given char index.
     */
    public TextLine getLineForCharIndex(int charIndex)
    {
        // Check for index outside bounds or index at end
        int length = length();
        if (charIndex < 0 || charIndex >= length) {
            if (charIndex == length)
                return getLastLine();
            throw new IndexOutOfBoundsException("Index " + charIndex + " outside bounds " + length);
        }

        // Get Low/high indexes
        int lowIndex = 0;
        int highIndex = getLineCount() - 1;

        // Iterate over lines until found
        while (lowIndex <= highIndex) {
            int midIndex = (lowIndex + highIndex) / 2;
            TextLine textLine = getLine(midIndex);
            if (charIndex < textLine.getStartCharIndex())
                highIndex = midIndex - 1;
            else if (charIndex >= textLine.getEndCharIndex())
                lowIndex = midIndex + 1;
            else return textLine;
        }

        // Should be impossible - lines would have to be misconfigured
        throw new IndexOutOfBoundsException("Index not found " + charIndex + " beyond " + length());
    }

    /**
     * Returns the last text line (or null if none).
     */
    public TextLine getLastLine()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Returns the TextRun that contains the given char index.
     */
    public TextRun getRunForCharIndex(int charIndex)
    {
        // Get line for start char index and convert char index to line
        TextLine textLine = getLineForCharIndex(charIndex);
        int lineStart = textLine.getStartCharIndex();
        int charIndexInLine = charIndex - lineStart;

        // Forward to line
        return textLine.getRunForCharIndex(charIndexInLine);
    }

    /**
     * Returns the TextRun for the given char range (usually just run for start, but can be next run if at boundary).
     */
    public TextRun getRunForCharRange(int startIndex, int endIndex)
    {
        // Get line for start char index and convert start/end index to line
        TextLine textLine = getLineForCharIndex(startIndex);
        int lineStart = textLine.getStartCharIndex();
        int startIndexInLine = startIndex - lineStart;
        int endIndexInLine = endIndex - lineStart;

        // Forward to line
        return textLine.getRunForCharRange(startIndexInLine, endIndexInLine);
    }

    /**
     * Returns a TextRunIter to easily traverse the runs for a given range of chars.
     */
    public TextRunIter getRunIterForCharRange(int startCharIndex, int endCharIndex)
    {
        return new TextRunIter(this, startCharIndex, endCharIndex, true);
    }

    /**
     * Returns the token at given char index.
     */
    public TextToken getTokenForCharIndex(int charIndex)
    {
        TextLine textLine = getLineForCharIndex(charIndex);
        int lineStart = textLine.getStartCharIndex();
        int selStartInLine = charIndex - lineStart;
        return textLine.getTokenForCharIndex(selStartInLine);
    }

    /**
     * Returns the Font for run at given character index.
     */
    public Font getFontForCharIndex(int charIndex)
    {
        TextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getFont();
    }

    /**
     * Returns the TextStyle for the run at the given character index.
     */
    public TextStyle getTextStyleForCharIndex(int charIndex)
    {
        TextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getTextStyle();
    }

    /**
     * Returns the TextStyle for the run for given char range.
     */
    public TextStyle getTextStyleForCharRange(int startIndex, int endIndex)
    {
        TextRun textRun = getRunForCharRange(startIndex, endIndex);
        return textRun.getTextStyle();
    }

    /**
     * Returns the TextLineStyle for the run at the given character index.
     */
    public TextLineStyle getLineStyleForCharIndex(int charIndex)
    {
        TextLine textLine = getLineForCharIndex(charIndex);
        return textLine.getLineStyle();
    }

    /**
     * Returns the line for the given y value.
     */
    public TextLine getLineForY(double aY)
    {
        // If y less than zero, return null
        if (aY < 0) return null;

        // Get Y in text
        double textY = aY - getAlignedY();

        // Iterate over lines and return one that spans given y
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            TextLine line = getLine(i);
            if (textY < line.getMaxY())
                return line;
        }

        // If no line for given y, return last line
        return getLastLine();
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndexForXY(double anX, double aY)
    {
        // Get text line for y (just return 0 if not found)
        TextLine textLine = getLineForY(aY);
        if (textLine == null)
            return 0;

        // Get x in text
        double textX = anX - getX();

        // Get char index for x in line and return
        int charIndex = textLine.getCharIndexForX(textX);
        return textLine.getStartCharIndex() + charIndex;
    }

    /**
     * Returns whether text contains an underlined run.
     */
    public boolean isUnderlined()
    {
        // Handle Rich
        if (isRichText()) {
            for (TextLine line : _lines)
                if (line.isUnderlined())
                    return true;
        }

        TextStyle textStyle = getTextStyleForCharIndex(0);
        return textStyle.isUnderlined();
    }

    /**
     * Sets text to be underlined.
     */
    public void setUnderlined(boolean aFlag)
    {
        setTextStyleValue(TextStyle.UNDERLINE_KEY, aFlag ? 1 : null, 0, length());
    }

    /**
     * Returns the horizontal alignment of the first paragraph of the text.
     */
    public HPos getAlignX()
    {
        TextLineStyle lineStyle = getLineStyleForCharIndex(0);
        return lineStyle.getAlign();
    }

    /**
     * Sets the horizontal alignment of the text.
     */
    public void setAlignX(HPos anAlignX)
    {
        setLineStyleValue(TextLineStyle.Align_Prop, anAlignX, 0, length());
    }

    /**
     * Returns the Y alignment.
     */
    public VPos getAlignY()  { return _alignY; }

    /**
     * Sets the Y alignment.
     */
    public void setAlignY(VPos aPos)
    {
        if (aPos == _alignY) return;
        _alignY = aPos;
        _alignedY = -1;
    }

    /**
     * Returns the y for alignment.
     */
    public double getAlignedY()
    {
        // If already set, just return
        if (_alignedY >= 0) return getY() + _alignedY;

        // Calculated aligned Y
        _alignedY = 0;
        if (_alignY != VPos.TOP) {
            double textModelW = getWidth();
            double prefH = getPrefHeight(textModelW);
            double textModelH = getHeight();
            if (textModelH > prefH)
                _alignedY = _alignY.doubleValue() * (textModelH - prefH);
        }

        // Return
        return getY() + _alignedY;
    }

    /**
     * Returns the start char index (always 0, unless this is SubText).
     */
    public int getStartCharIndex()  { return 0; }

    /**
     * Returns the end char in source text.
     */
    public int getEndCharIndex()
    {
        int startCharIndex = getStartCharIndex();
        TextLine lastLine = getLastLine();
        int lastLineEnd = lastLine != null ? lastLine.getEndCharIndex() : 0;
        return startCharIndex + lastLineEnd;
    }

    /**
     * Scales all the fonts in text by given factor.
     */
    public void scaleFonts(double aScale)
    {
        // If scale 1, just return
        if (aScale == 1) return;

        // Iterate over lines
        for (TextLine line : getLines()) {
            for (TextRun run : line.getRuns()) {
                TextStyle runStyle = run.getTextStyle();
                TextStyle runStyleScaled = runStyle.copyForStyleValue(run.getFont().copyForScale(aScale));
                run.setTextStyle(runStyleScaled);
            }
        }
    }

    /**
     * Returns the index of given string.
     */
    public int indexOf(String aStr, int aStart)
    {
        // Iterate over lines
        for (TextLine line : getLines()) {

            // If startIndex beyond line.End, skip
            if (aStart >= line.getEndCharIndex()) continue;

            // Convert startIndex to line charIndex
            int lineStart = line.getStartCharIndex();
            int startIndexInLine = Math.max(aStart - lineStart, 0);

            // Forward to line and return if found
            int index = line.indexOf(aStr, startIndexInLine);
            if (index >= 0)
                return index + lineStart;
        }

        // Return not found
        return -1;
    }

    /**
     * Returns the X location.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X location.
     */
    public void setX(double anX)  { _x = anX; }

    /**
     * Returns the Y location.
     */
    public double getY()  { return _y; }

    /**
     * Sets the Y location.
     */
    public void setY(double aY)  { _y = aY; }

    /**
     * Returns the width.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width.
     */
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        _width = aValue;
        _lines.forEach(line -> line.updateAlignmentAndJustify());
    }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the width.
     */
    public void setHeight(double aValue)
    {
        if (aValue == _height) return;
        _height = aValue;
        _alignedY = -1;
    }

    /**
     * Returns the current bounds.
     */
    public Rect getBounds()  { return new Rect(_x, _y, _width, _height); }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(Rect aRect)
    {
        setBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX);
        setY(aY);
        setWidth(aW);
        setHeight(aH);
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
     * Returns a path for two char indexes - it will be a simple box with extensions for first/last lines.
     */
    public Shape getPathForCharRange(int aStartCharIndex, int aEndCharIndex)
    {
        return TextModelUtils.getPathForCharRange(this, aStartCharIndex, aEndCharIndex);
    }

    /**
     * Returns whether property change is enabled.
     */
    public boolean isPropChangeEnabled()  { return _propChangeEnabled; }

    /**
     * Sets whether property change is enabled.
     */
    public void setPropChangeEnabled(boolean aValue)
    {
        _propChangeEnabled = aValue;
    }

    /**
     * Paint text to given painter.
     */
    public void paint(Painter aPntr)
    {
        // Just return if no lines
        int lineCount = getLineCount();
        if (lineCount == 0)
            return;

        // Save painter state
        aPntr.save();

        // Get text bounds and clip
        Rect textBounds = getBounds();
        Rect clipBounds = aPntr.getClipBounds();
        clipBounds = clipBounds != null ? clipBounds.getIntersectRect(textBounds) : textBounds;
        aPntr.clip(clipBounds);

        // Iterate over lines
        for (int i = 0; i < lineCount; i++) {

            // If line not yet visible, skip
            TextLine textLine = getLine(i);
            if (textLine.getTextMaxY() < clipBounds.y)
                continue;

            // If line no longer visible, break
            if (textLine.getTextY() >= clipBounds.getMaxY())
                break;

            // Paint line
            double lineY = getAlignedY() + textLine.getBaseline();
            paintLine(aPntr, textLine, lineY);
        }

        // Paint underlines
        if (isUnderlined())
            TextModelUtils.paintTextModelUnderlines(aPntr, this, clipBounds);

        // Restore state
        aPntr.restore();
    }

    /**
     * Paint text line to given painter.
     */
    public void paintLine(Painter aPntr, TextLine textLine, double lineY)
    {
        TextToken[] lineTokens = textLine.getTokens();

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
    }

    /**
     * Returns the width of text.
     */
    public double getPrefWidth()
    {
        // If already set, just return
        if (_prefW >= 0) return _prefW;

        // Calc, set, return
        TextLine longestLine = getLineLongest();
        double longestLineW = longestLine != null ? longestLine.getWidth() : 0;
        double prefW = Math.ceil(longestLineW);
        return _prefW = prefW;
    }

    /**
     * Returns the width of text from given start char index.
     */
    public double getPrefWidthForStartCharIndex(int startCharIndex)
    {
        // If given char index 0, return cached version
        if (startCharIndex <= 0)
            return getPrefWidth();

        // Get line for startCharIndex
        TextLine textLine = getLineForCharIndex(startCharIndex);
        int startCharIndexInLine = startCharIndex - textLine.getStartCharIndex();
        double prefW = textLine.getWidthForStartCharIndex(startCharIndexInLine) - textLine.getTrailingWhitespaceWidth();

        // Iterate till end looking for longer line
        TextLine nextLine = textLine.getNext();
        while (nextLine != null) {
            double lineW = nextLine.getWidth() - nextLine.getTrailingWhitespaceWidth();
            prefW = Math.max(prefW, lineW);
            nextLine = nextLine.getNext();
        }

        // Return
        return prefW;
    }

    /**
     * Returns the preferred height.
     */
    public double getPrefHeight()
    {
        // Return bottom of last line minus box Y
        TextLine lastLine = getLastLine();
        if (lastLine == null)
            return 0;
        TextLine firstLine = getLine(0);
        double lastLineMaxY = lastLine.getMaxY();
        double firstLineY = firstLine.getY();
        return Math.ceil(lastLineMaxY - firstLineY);
    }

    /**
     * Returns the preferred height.
     */
    public double getPrefHeight(double aW)
    {
        // If WrapLines and given Width doesn't match current Width, setWidth
        if (isWrapLines() && !MathUtils.equals(aW, _width) && aW > 0) {
            double oldH = _height, oldW = _width;
            _height = Float.MAX_VALUE;
            setWidth(aW);
            double prefH = getPrefHeight();
            _height = oldH;
            setWidth(oldW); // Seems like this should be unnecessary, since width is likely to be set to aW
            return prefH;
        }

        // Return normal version
        return getPrefHeight();
    }

    /**
     * Adds characters for given TextModel to this text at given index.
     */
    public void addCharsForTextModel(TextModel textModel, int anIndex)
    {
        List<TextLine> textLines = textModel.getLines();
        for (TextLine line : textLines) {
            TextRun[] lineRuns = line.getRuns();
            for (TextRun run : lineRuns) {
                int index = anIndex + line.getStartCharIndex() + run.getStartCharIndex();
                addCharsWithStyle(run.getString(), run.getTextStyle(), index);
                setLineStyle(line.getLineStyle(), index, index + run.length());
            }
        }
    }

    /**
     * Splits given line at given character index and adds remainder to text and returns it.
     */
    protected TextLine splitLineAtIndex(TextLine textLine, int anIndex)
    {
        // Create remainder from clone and remove respective chars from given line and remainder
        TextLine remainderLine = textLine.clone();
        textLine.removeChars(anIndex, length());
        remainderLine.removeChars(0, anIndex);

        // Add remainder
        addLine(remainderLine, textLine.getLineIndex() + 1);

        // Return
        return remainderLine;
    }

    /**
     * Joins given line with next line.
     */
    protected void joinLineWithNextLine(TextLine textLine)
    {
        TextLine nextLine = textLine.getNext();
        if (nextLine == null)
            return;

        // Iterate over NextLine runs and add chars for each
        TextRun[] textRuns = nextLine.getRuns();
        for (TextRun textRun : textRuns) {
            CharSequence chars = textRun.getString();
            TextStyle textStyle = textRun.getTextStyle();
            int endCharIndex = textLine.getEndCharIndex();
            addCharsToLine(chars, textStyle, endCharIndex, textLine, false);
        }

        // Remove NextLine
        removeLine(nextLine.getLineIndex());
    }

    /**
     * Creates TextTokens for a TextLine.
     */
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        return TextModelUtils.createTokensForTextLine(aTextLine);
    }

    /**
     * Resets Line Y positions for lines after given line index.
     */
    protected void resetLineYForLinesAfterIndex(int lineIndex)
    {
        // Iterate over lines beyond given lineIndex and reset Y (stop if line is already reset)
        for (int i = lineIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            TextLine line = getLine(i);
            if (line._y == -1)
                return;
            line._y = -1;
        }
    }

    /**
     * Updates Lines (Index, Start) from index line to text end.
     */
    protected void updateLines(int anIndex)
    {
        // Get BaseLine and length at end of BaseLine
        TextLine baseLine = anIndex >= 0 ? getLine(anIndex) : null;
        _length = baseLine != null ? baseLine.getEndCharIndex() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            TextLine line = getLine(i);
            updateLine(line, i, _length);
            _length += line.length();
        }

        // Reset AlignY offset
        _alignedY = -1;
    }

    /**
     * Updates an individual line for new index and start char index.
     */
    protected void updateLine(TextLine textLine, int newIndex, int newStartCharIndex)
    {
        textLine._lineIndex = newIndex;
        textLine._startCharIndex = newStartCharIndex;
        textLine._y = -1;
    }

    /**
     * Returns underlined runs for text.
     */
    public TextRun[] getUnderlineRuns(Rect aRect)  { return TextModelUtils.getUnderlineRuns(this, aRect); }

    /**
     * Returns a copy of this text for given char range.
     */
    public TextModel copyForRange(int aStart, int aEnd)
    {
        // Create new RichText and iterate over lines in range to add copies for subrange
        TextModel textCopy = new TextModel(isRichText());
        textCopy._lines.remove(0);

        // Get start/end line indexes
        int startLineIndex = getLineForCharIndex(aStart).getLineIndex();
        int endLineIndex = getLineForCharIndex(aEnd).getLineIndex();

        // Iterate over lines and add
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            int lineStart = line.getStartCharIndex();
            int start = Math.max(aStart - lineStart, 0), end = Math.min(aEnd - lineStart, line.length());
            TextLine lineCopy = line.copyForRange(start, end);
            textCopy.addLine(lineCopy, textCopy.getLineCount());
        }

        // Return
        return textCopy;
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceUrl()  { return _sourceUrl; }

    /**
     * Sets the Source URL.
     */
    public void setSourceUrl(WebURL aURL)
    {
        _sourceUrl = aURL;
    }

    /**
     * Returns the source file.
     */
    public WebFile getSourceFile()
    {
        WebURL sourceURL = getSourceUrl();
        return sourceURL != null ? sourceURL.createFile(false) : null;
    }

    /**
     * Load text from source URL.
     */
    public void readTextFromSourceFile(WebFile sourceFile)
    {
        // Set Source URL
        setSourceUrl(sourceFile.getUrl());

        // Get file text and set content
        String text = sourceFile.getText();
        setString(text);
    }

    /**
     * Write text to source file.
     */
    public void writeTextToSourceFile()
    {
        // Get SourceFile
        WebFile sourceFile = getSourceFile();
        if (sourceFile == null) {
            System.err.println("TextModel.writeToSourceFile: No source file specified");
            return;
        }

        // Get text string and set in file
        String fileText = getString();
        sourceFile.setText(fileText);

        // Save file
        sourceFile.save();

        // Set TextModified to false since it matches file
        setTextModified(false);
    }

    /**
     * Synchronizes TextModel and SourceFile.
     */
    public void syncTextModelToSourceFile()  { new TextModelUtils.TextModelFileSyncer(this); }

    /**
     * Standard clone implementation.
     */
    @Override
    public TextModel clone()
    {
        // Do normal clone
        TextModel clone;
        try { clone = (TextModel) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Reset lines array and length
        clone._lines = new ArrayList<>(getLineCount());
        clone._length = 0;

        // Copy lines deep
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            TextLine line = getLine(i);
            TextLine lineClone = line.clone();
            clone.addLine(lineClone, i);
        }

        // Return
        return clone;
    }

    /**
     * Standard toStringProps implementation.
     */
    @Override
    public String toStringProps()
    {
        // LineCount, Length
        StringBuilder sb = new StringBuilder();
        sb.append("Length=").append(length());
        sb.append(", LineCount=").append(getLineCount());

        // Add String
        for (int i = 0, iMax = Math.min(getLineCount(), 5); i < iMax; i++)
            sb.append('\n').append(getLine(i));

        // Return
        return sb.toString();
    }

    /**
     * XMLArchiver.Archivable archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)  { return TextModelUtils.toXML(this, anArchiver); }

    /**
     * XMLArchiver.Archivable unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        TextModelUtils.fromXML(this, anArchiver, anElement);
        return this;
    }
}
