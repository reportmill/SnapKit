/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.gfx.Font;
import snap.props.PropObject;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.web.WebFile;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public class TextDoc extends PropObject implements CharSequence, Cloneable {

    // The Source of the current content
    private Object  _source;

    // The URL of the file that provided the text
    private WebURL  _sourceURL;

    // The TextLines in this text
    protected List<TextLine>  _lines = new ArrayList<>();

    // The length of this text
    protected int  _length;

    // The default text style for this text
    protected TextStyle  _defStyle = TextStyle.DEFAULT;

    // The default line style for this text
    protected TextLineStyle  _defLineStyle = TextLineStyle.DEFAULT;

    // Whether property change is enabled
    protected boolean  _propChangeEnabled = true;

    // The width of the rich text
    protected double  _width = -1;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";

    /**
     * Constructor.
     */
    public TextDoc()
    {
        super();
        addDefaultLine();
    }

    /**
     * Adds a default line.
     */
    protected void addDefaultLine()
    {
        addLine(createLine(), 0);
    }

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    public boolean isRichText()  { return false; }

    /**
     * Returns the source for the current text content.
     */
    public Object getSource()  { return _source; }

    /**
     * Loads the text from the given source.
     */
    public void setSource(Object aSource)
    {
        // Get/Set URL from Source
        WebURL url = WebURL.getURL(aSource);
        _source = url != null ? aSource : null;
        _sourceURL = url;

        // Get/set text from source
        String text = SnapUtils.getText(aSource);
        setString(text);
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _sourceURL; }

    /**
     * Returns the source file.
     */
    public WebFile getSourceFile()
    {
        WebURL sourceURL = getSourceURL();
        return sourceURL != null ? sourceURL.getFile() : null;
    }

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
        return line.charAt(anIndex - line.getStart());
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        StringBuffer sb = new StringBuffer(anEnd - aStart);
        TextLine line = getLineForCharIndex(aStart);
        while (aStart < anEnd) {
            int end = Math.min(line.getEnd(), anEnd);
            sb.append(line.subSequence(aStart - line.getStart(), end - line.getStart()));
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
        setPropChangeEnabled(false);
        replaceChars(aString, null, 0, length());
        setPropChangeEnabled(true);
    }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()  { return _defStyle; }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)
    {
        _defStyle = aStyle;
        for (TextLine line : getLines())
            line.setStyle(aStyle);
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _defLineStyle; }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        _defLineStyle = aLineStyle;
        for (TextLine line : getLines())
            line.setLineStyle(aLineStyle);
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars)
    {
        addChars(theChars, null, length());
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // If no chars, just return
        if (theChars == null) return;

        // If not rich text, clear style
        if (!isRichText()) theStyle = null;

        // Get line for index - if adding at text end and last line and ends with newline, create/add new line
        TextLine line = getLineForCharIndex(anIndex);
        if (anIndex == line.getEnd() && line.isLastCharNewline()) {
            TextLine remainder = line.splitLineAtIndex(line.length());
            addLine(remainder, line.getIndex() + 1);
            line = remainder;
        }

        // Add chars line by line
        int start = 0;
        int len = theChars.length();
        int lindex = anIndex - line.getStart();
        while (start < len) {

            // Get index of newline in insertion chars (if there) and end of line block
            int newline = StringUtils.indexAfterNewline(theChars, start);
            int end = newline > 0 ? newline : len;

            // Get chars and add
            CharSequence chars = start == 0 && end == len ? theChars : theChars.subSequence(start, end);
            line.addChars(chars, theStyle, lindex);

            // If newline added and there are more chars in line, split line and add remainder
            if (newline > 0 && (end < len || lindex + chars.length() < line.length())) {
                TextLine remainder = line.splitLineAtIndex(lindex + chars.length());
                addLine(remainder, line.getIndex() + 1);
                line = remainder;
                lindex = 0;
            }

            // Set start to last end
            start = end;
        }

        // Send PropertyChange
        if (isPropChangeEnabled())
            firePropChange(new TextDocUtils.CharsChange(this, null, theChars, anIndex));
        _width = -1;
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // If empty range, just return
        if (anEnd == aStart) return;

        // If PropChangeEnabled, get chars to be deleted
        CharSequence removedChars = isPropChangeEnabled() ? subSequence(aStart, anEnd) : null;

        // Delete lines/chars for range
        int end = anEnd;
        while (end > aStart) {

            // Get line at end index
            TextLine line = getLineForCharIndex(end);
            if (end == line.getStart())
                line = getLine(line.getIndex() - 1);

            // Get Line.Start
            int lineStart = line.getStart();
            int start = Math.max(aStart, lineStart);

            // If whole line in range, remove line
            if (start == lineStart && end == line.getEnd() && getLineCount() > 1)
                removeLine(line.getIndex());

                // Otherwise remove chars (if no newline afterwards, join with next line)
            else {
                line.removeChars(start - lineStart, end - lineStart);
                if (!line.isLastCharNewline() && line.getIndex() + 1 < getLineCount()) {
                    TextLine next = removeLine(line.getIndex() + 1);
                    line.appendLine(next);
                }
            }

            // Reset end
            end = lineStart;
        }

        // If deleted chars is set, send property change
        if (removedChars != null)
            firePropChange(new TextDocUtils.CharsChange(this, removedChars, null, aStart));
        _width = -1;
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, int aStart, int anEnd)
    {
        replaceChars(theChars, null, aStart, anEnd);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        // Get style and linestyle for add chars
        TextStyle style = theStyle != null ? theStyle : getStyleForCharIndex(aStart);
        TextLineStyle lineStyle = null;
        if (theChars != null && theChars.length() > 0 && isRichText())
            lineStyle = getLineStyleForCharIndex(aStart);

        // Remove given range and add chars
        if (anEnd > aStart)
            removeChars(aStart, anEnd);
        addChars(theChars, style, aStart);

        // Restore LineStyle (needed if range includes a newline)
        if (lineStyle != null)
            setLineStyle(lineStyle, aStart, aStart + theChars.length());
    }

    /**
     * Adds given TextDoc to this text at given index.
     */
    public void addTextDoc(TextDoc aTextDoc, int anIndex)
    {
        for (TextLine line : aTextDoc.getLines()) {
            TextRun[] lineRuns = line.getRuns();
            for (TextRun run : lineRuns) {
                int index = anIndex + line.getStart() + run.getStart();
                addChars(run.getString(), run.getStyle(), index);
                setLineStyle(line.getLineStyle(), index, index + run.length());
            }
        }
    }

    /**
     * Sets a given style to a given range.
     */
    public void setStyle(TextStyle aStyle, int aStart, int anEnd)
    {
        System.out.println("TextDoc.setStyle: Not implemented");
    }

    /**
     * Sets a given style value to given value for a given range.
     */
    public void setStyleValue(Object aValue)
    {
        setStyleValue(aValue, 0, length());
    }

    /**
     * Sets a given style value to given value for a given range.
     */
    public void setStyleValue(Object aValue, int aStart, int aEnd)
    {
        String key = TextStyle.getStyleKey(aValue);
        setStyleValue(key, aValue, aStart, aEnd);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue)
    {
        setStyleValue(aKey, aValue, 0, length());
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        TextStyle style = getStyleForCharIndex(aStart).copyFor(aKey, aValue);
        setStyle(style, aStart, anEnd);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Propagate to Lines
        TextLineStyle oldStyle = getLine(0).getLineStyle();
        for (TextLine line : getLines())
            line.setLineStyle(aStyle);

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(new TextDocUtils.LineStyleChange(this, oldStyle, aStyle, 0));

        _width = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        TextLineStyle oldStyle = getLine(0).getLineStyle();
        TextLineStyle newStyle = oldStyle.copyFor(aKey, aValue);
        setLineStyle(newStyle, 0, length());
        _width = -1;
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public TextLine getLine(int anIndex)
    {
        return _lines.get(anIndex);
    }

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
        aLine._textDoc = this;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected TextLine removeLine(int anIndex)
    {
        TextLine line = _lines.remove(anIndex);
        line._textDoc = null;
        updateLines(anIndex - 1);
        return line;
    }

    /**
     * Creates a new TextLine for use in this text.
     */
    protected TextLine createLine()  { return new TextLine(this); }

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
        removeChars(0, length());
        setStyle(getDefaultStyle(), 0, 0);
        setLineStyle(getDefaultLineStyle(), 0, 0);
    }

    /**
     * Returns the width of text.
     */
    public double getPrefWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Calc, set, return
        TextLine line = getLineLongest();
        double prefW = Math.ceil(line != null ? line._width : 0);
        return _width = prefW;
    }

    /**
     * Returns the width of text from given index.
     */
    public double getPrefWidth(int anIndex)
    {
        // If given char index 0, do cached version
        if (anIndex <= 0) return getPrefWidth();

        // Iterate over lines and get max line width
        double prefW = 0;
        for (TextLine line : _lines) {
            if (anIndex < line.getEnd()) {
                double lineW = line.getWidth(anIndex - line.getStart());
                prefW = Math.max(prefW, lineW);
            }
        }

        // Return
        return prefW;
    }

    /**
     * Returns the block at the given char index.
     */
    public TextLine getLineForCharIndex(int anIndex)
    {
        // Iterate over lines and return line containing char index
        for (TextLine line : _lines)
            if (anIndex < line.getEnd())
                return line;

        // If index of text end, return last
        if (anIndex == length())
            return getLineLast();

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Returns the last block.
     */
    public TextLine getLineLast()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Returns the TextRun that contains the given char index.
     */
    public TextRun getRunForCharIndex(int charIndex)
    {
        TextLine line = getLineForCharIndex(charIndex);
        return line.getRunForCharIndex(charIndex - line.getStart());
    }

    /**
     * Returns the last run.
     */
    public TextRun getRunLast()
    {
        TextLine lastLine = getLineLast();
        TextRun lastRun = lastLine != null ? lastLine.getRunLast() : null;
        return lastRun;
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
    public TextStyle getStyleForCharIndex(int charIndex)
    {
        TextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getStyle();
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
     * Returns whether text contains an underlined run.
     */
    public boolean isUnderlined()
    {
        TextStyle textStyle = getStyleForCharIndex(0);
        return textStyle.isUnderlined();
    }

    /**
     * Sets text to be underlined.
     */
    public void setUnderlined(boolean aFlag)
    {
        setStyleValue(TextStyle.UNDERLINE_KEY, aFlag ? 1 : null, 0, length());
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
        setLineStyleValue(TextLineStyle.ALIGN_KEY, anAlignX, 0, length());
    }

    /**
     * Returns the start char index (always 0, unless this is SubText).
     */
    public int getStartCharIndex()  { return 0; }

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
                TextStyle runStyle = run.getStyle();
                TextStyle runStyleScaled = runStyle.copyFor(run.getFont().scaleFont(aScale));
                run.setStyle(runStyleScaled);
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
            if (aStart >= line.getEnd()) continue;

            // Convert startIndex to line charIndex
            int lineStart = line.getStart();
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
     * Returns index of next newline (or carriage-return/newline) starting at given char index.
     */
    public int indexOfNewline(int aStart)
    {
        return StringUtils.indexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond next newline (or carriage-return/newline) starting at given char index.
     */
    public int indexAfterNewline(int aStart)
    {
        return StringUtils.indexAfterNewline(this, aStart);
    }

    /**
     * Returns index of the previous newline (or carriage-return/newline) starting at given char index.
     */
    public int lastIndexOfNewline(int aStart)
    {
        return StringUtils.lastIndexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond previous newline (or carriage-return/newline) starting at given char index.
     */
    public int lastIndexAfterNewline(int aStart)
    {
        return StringUtils.lastIndexAfterNewline(this, aStart);
    }

    /**
     * Returns whether the index in the given char sequence is at a line end.
     */
    public boolean isLineEnd(int anIndex)
    {
        return StringUtils.isLineEnd(this, anIndex);
    }

    /**
     * Returns whether the index in the given char sequence is at just after a line end.
     */
    public boolean isAfterLineEnd(int anIndex)
    {
        return StringUtils.isAfterLineEnd(this, anIndex);
    }

    /**
     * Returns whether a char is a newline char.
     */
    public boolean isLineEndChar(int anIndex)
    {
        return StringUtils.isLineEndChar(this, anIndex);
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
     * Creates TextTokens for a TextLine.
     */
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        return TextToken.createTokensForTextLine(aTextLine);
    }

    /**
     * Updates Lines (Index, Start) from index line to text end.
     */
    protected void updateLines(int anIndex)
    {
        // Get BaseLine and length at end of BaseLine
        TextLine baseLine = anIndex >= 0 ? getLine(anIndex) : null;
        _length = baseLine != null ? baseLine.getEnd() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            TextLine line = getLine(i);
            line._index = i;
            line._start = _length;
            _length += line.length();
        }
    }

    /**
     * Save TextDoc text to Source file.
     */
    public void saveToSourceFile()
    {
        // Get SourceFile
        WebURL sourceURL = getSourceURL();
        WebFile sourceFile = sourceURL.getFile();
        if (sourceFile == null)
            sourceFile = sourceURL.createFile(false);

        // Get TextDoc string and set in file
        String fileText = getString();
        sourceFile.setText(fileText);

        // Save file
        try { sourceFile.save(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public TextDoc clone()
    {
        // Do normal clone
        TextDoc clone;
        try { clone = (TextDoc) super.clone(); }
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
        String str = "";
        for (int i = 0, iMax = Math.min(getLineCount(), 5); i < iMax; i++)
            str += "\n" + getLine(i);
        sb.append(str);

        // Return
        return sb.toString();
    }

    /**
     * Returns a new TextDoc from given source.
     */
    public static TextDoc newFromSource(Object aSource)
    {
        // Get/Set URL from Source
        WebURL url = WebURL.getURL(aSource);

        // Create TextDoc
        TextDoc textDoc = new TextDoc();
        textDoc._source = url != null ? aSource : null;
        textDoc._sourceURL = url;

        // Get/set text from source
        String text = SnapUtils.getText(aSource);
        textDoc.setString(text);

        // Return
        return textDoc;
    }
}
