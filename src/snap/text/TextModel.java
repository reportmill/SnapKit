/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChange;
import snap.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public abstract class TextModel extends TextLayout implements Cloneable, XMLArchiver.Archivable {

    // The TextLines in this text
    protected List<TextLine> _lines = new ArrayList<>();

    // The default text style for this text
    protected TextStyle _defaultTextStyle = TextStyle.DEFAULT;

    // The default line style for this text
    protected TextLineStyle _defaultLineStyle = TextLineStyle.DEFAULT;

    // Whether property change is enabled
    protected boolean _propChangeEnabled = true;

    // The last mouse Y, to help in caret placement (can be ambiguous for start/end of line)
    protected double _mouseY;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";
    public static final String DefaultTextStyle_Prop = "DefaultTextStyle";
    public static final String DefaultLineStyle_Prop = "DefaultLineStyle";

    /**
     * Constructor.
     */
    public TextModel()
    {
        super();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        if (aValue == isRichText()) return;
        _rich = aValue;

        // Set DefaultStyle, because RichText never inherits from parent
        if (aValue && _defaultTextStyle == null)
            _defaultTextStyle = TextStyle.DEFAULT;
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        String string = aString != null ? aString : "";

        if (!string.contentEquals(this)) {
            //setPropChangeEnabled(false);
            replaceChars(string, 0, length());
            //setPropChangeEnabled(true);
        }
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
     * Returns a TextRunIter to easily traverse the runs for a given range of chars.
     */
    public TextRunIter getRunIterForCharRange(int startCharIndex, int endCharIndex)
    {
        return new TextRunIter(this, startCharIndex, endCharIndex, true);
    }

    /**
     * Sets text to be underlined.
     */
    public void setUnderlined(boolean aFlag)
    {
        setTextStyleValue(TextStyle.UNDERLINE_KEY, aFlag ? 1 : null, 0, length());
    }

    /**
     * Sets the horizontal alignment of the text.
     */
    public void setAlignX(HPos anAlignX)
    {
        setLineStyleValue(TextLineStyle.Align_Prop, anAlignX, 0, length());
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
     * Returns whether property change is enabled.
     */
    public boolean isPropChangeEnabled()  { return _propChangeEnabled; }

    /**
     * Sets whether property change is enabled.
     */
    public void setPropChangeEnabled(boolean aValue)  { _propChangeEnabled = aValue; }

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
        TextModel textCopy = TextModel.createDefaultTextModel(isRichText());
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
     * Sets the width.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        super.setWidth(aValue);
        _lines.forEach(line -> line.updateAlignmentAndJustify());
    }

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

    /**
     * Creates a default text model.
     */
    public static TextModel createDefaultTextModel()  { return new TextBlock(); }

    /**
     * Creates a default text model.
     */
    public static TextModel createDefaultTextModel(boolean isRich)  { return new TextBlock(isRich); }
}
