/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.gfx.Font;
import snap.props.PropObject;
import snap.util.CharSequenceUtils;
import snap.util.CharSequenceX;
import snap.web.WebFile;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public class TextDoc extends PropObject implements CharSequenceX, Cloneable {

    // The URL of the file that provided the text
    private WebURL  _sourceURL;

    // The TextLines in this text
    protected List<TextLine>  _lines = new ArrayList<>();

    // The length of this text
    protected int  _length;

    // The default text style for this text
    protected TextStyle  _defaultTextStyle;

    // The default line style for this text
    protected TextLineStyle  _defaultLineStyle = TextLineStyle.DEFAULT;

    // The current text style for TextDoc parent/container (probably TextArea).
    protected TextStyle  _parentTextStyle = TextStyle.DEFAULT;

    // Whether property change is enabled
    protected boolean  _propChangeEnabled = true;

    // The width of the rich text
    protected double  _width = -1;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";
    public static final String DefaultTextStyle_Prop = "DefaultTextStyle";
    public static final String ParentTextStyle_Prop = "ParentTextStyle";

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
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _sourceURL; }

    /**
     * Sets the Source URL.
     */
    public void setSourceURL(WebURL aURL)
    {
        _sourceURL = aURL;
    }

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
        setPropChangeEnabled(false);
        replaceChars(aString, null, 0, length());
        setPropChangeEnabled(true);
    }

    /**
     * Returns whether the default text style is explicitly set (vs. coming from parent).
     */
    public boolean isDefaultTextStyleSet()  { return _defaultTextStyle != null; }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultStyle()  { return _defaultTextStyle != null ? _defaultTextStyle : _parentTextStyle; }

    /**
     * Sets the default text style.
     */
    public void setDefaultStyle(TextStyle aStyle)
    {
        // If already set, just return
        if (Objects.equals(aStyle, _defaultTextStyle)) return;

        // Set
        TextStyle oldStyle = _defaultTextStyle;
        _defaultTextStyle = aStyle;

        // Update existing lines
        if (!isRichText()) {
            TextStyle textStyle = getDefaultStyle();
            List<TextLine> lines = getLines();
            for (TextLine line : lines)
                line.setStyle(textStyle);
        }

        // Fire prop change
        firePropChange(DefaultTextStyle_Prop, oldStyle, aStyle);
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _defaultLineStyle; }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        _defaultLineStyle = aLineStyle;
        for (TextLine line : getLines())
            line.setLineStyle(aLineStyle);
    }

    /**
     * Returns the current style for TextDoc parent/container (probably a TextArea).
     */
    public TextStyle getParentTextStyle()  { return _parentTextStyle; }

    /**
     * Sets the current style for TextDoc parent/container (probably a TextArea).
     */
    public void setParentTextStyle(TextStyle aStyle)
    {
        // If already set, just return
        if (aStyle == null) aStyle = TextStyle.DEFAULT;
        if (Objects.equals(aStyle, _defaultTextStyle)) return;

        // Set
        TextStyle oldStyle = _parentTextStyle;
        _parentTextStyle = aStyle;

        // If DefaultTextStyle not set, update existing lines
        if (!isRichText() && !isDefaultTextStyleSet()) {
            TextStyle textStyle = getDefaultStyle();
            List<TextLine> lines = getLines();
            for (TextLine line : lines)
                line.setStyle(textStyle);
        }

        // Fire prop change
        firePropChange(ParentTextStyle_Prop, oldStyle, aStyle);
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
        if (!isRichText())
            theStyle = null;

        // Get line for char index
        TextLine textLine = getLineForCharIndex(anIndex);

        // If adding at text end and last line and ends with newline, create/add new line
        if (anIndex == textLine.getEndCharIndex() && textLine.isLastCharNewline()) {
            TextLine remainder = textLine.splitLineAtIndex(textLine.length());
            addLine(remainder, textLine.getIndex() + 1);
            textLine = remainder;
        }

        // Loop vars
        int charsLength = theChars.length();
        int charIndexInLine = anIndex - textLine.getStartCharIndex();
        int charIndexInChars = 0;

        // Iterate over chars until all added
        while (charIndexInChars < charsLength) {

            // Get index of newline in insertion chars (if there) and end of line block
            int newlineIndex = CharSequenceUtils.indexAfterNewline(theChars, charIndexInChars);
            int endCharIndexInChars = newlineIndex > 0 ? newlineIndex : charsLength;

            // Get chars and add
            CharSequence chars = theChars;
            if (charIndexInChars > 0 || endCharIndexInChars < charsLength)
                chars = theChars.subSequence(charIndexInChars, endCharIndexInChars);
            textLine.addChars(chars, theStyle, charIndexInLine);

            // If newline added and there are more chars in line, split line and add remainder
            if (newlineIndex > 0) {
                if (endCharIndexInChars < charsLength || charIndexInLine + chars.length() < textLine.length()) {
                    TextLine remainder = textLine.splitLineAtIndex(charIndexInLine + chars.length());
                    addLine(remainder, textLine.getIndex() + 1);
                    textLine = remainder;
                    charIndexInLine = 0;
                }
            }

            // Set start to last end
            charIndexInChars = endCharIndexInChars;
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

        // Delete lines/chars for range from end to start
        int removeEndCharIndex = anEnd;
        while (removeEndCharIndex > aStart) {

            // Get line at end index
            TextLine textLine = getLineForCharIndex(removeEndCharIndex);
            if (removeEndCharIndex == textLine.getStartCharIndex())
                textLine = textLine.getPrevious();

            // Get Line.Start
            int lineStartCharIndex = textLine.getStartCharIndex();
            int removeStartCharIndex = Math.max(aStart, lineStartCharIndex);

            // If whole line in range, remove line
            if (removeStartCharIndex == lineStartCharIndex && removeEndCharIndex == textLine.getEndCharIndex() && getLineCount() > 1)
                removeLine(textLine.getIndex());

            // Otherwise remove chars from line
            else {

                // Remove chars from line
                int removeStartCharIndexInLine = removeStartCharIndex - lineStartCharIndex;
                int removeEndCharIndexInLine = removeEndCharIndex - lineStartCharIndex;
                textLine.removeChars(removeStartCharIndexInLine, removeEndCharIndexInLine);

                // If no newline remaining in line, join with next line
                if (!textLine.isLastCharNewline()) {

                    // Get NextLine
                    TextLine nextLine = textLine.getNext();
                    if (nextLine != null) {

                        // Iterate over NextLine runs and add chars for each
                        TextRun[] textRuns = nextLine.getRuns();
                        for (TextRun textRun : textRuns)
                            textLine.addChars(textRun.getString(), textRun.getStyle(), textLine.length());

                        // Remove NextLine
                        removeLine(nextLine.getIndex());
                    }
                }
            }

            // Reset end
            removeEndCharIndex = lineStartCharIndex;
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
        // Get TextStyle for add chars range (if not provided)
        TextStyle style = theStyle;
        if (style == null)
            style = getStyleForCharRange(aStart, anEnd);

        // Remove given range and add chars
        if (anEnd > aStart)
            removeChars(aStart, anEnd);
        addChars(theChars, style, aStart);
    }

    /**
     * Adds given TextDoc to this text at given index.
     */
    public void addTextDoc(TextDoc aTextDoc, int anIndex)
    {
        for (TextLine line : aTextDoc.getLines()) {
            TextRun[] lineRuns = line.getRuns();
            for (TextRun run : lineRuns) {
                int index = anIndex + line.getStartCharIndex() + run.getStartCharIndex();
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
        TextStyle styleForRange = getStyleForCharRange(aStart, anEnd);
        TextStyle newStyle = styleForRange.copyFor(aKey, aValue);
        setStyle(newStyle, aStart, anEnd);
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
    protected void removeLine(int anIndex)
    {
        TextLine line = _lines.remove(anIndex);
        line._textDoc = null;
        updateLines(anIndex - 1);
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
            if (anIndex < line.getEndCharIndex()) {
                double lineW = line.getWidth(anIndex - line.getStartCharIndex());
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
            if (anIndex < line.getEndCharIndex())
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
     * Returns the TextStyle for the run for given char range.
     */
    public TextStyle getStyleForCharRange(int startIndex, int endIndex)
    {
        TextRun textRun = getRunForCharRange(startIndex, endIndex);
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
        _length = baseLine != null ? baseLine.getEndCharIndex() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            TextLine line = getLine(i);
            line._index = i;
            line._startCharIndex = _length;
            _length += line.length();
        }
    }

    /**
     * Load TextDoc from source URL.
     */
    public void readFromSourceURL(WebURL aURL)
    {
        // Set Doc Source URL
        setSourceURL(aURL);

        // Get URL text and set in doc
        String text = aURL.getText();
        setString(text);
    }

    /**
     * Write TextDoc text to source file.
     */
    public void writeToSourceFile() throws Exception
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
        sourceFile.save();
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

        // Create TextDoc and open from URL
        TextDoc textDoc = new TextDoc();
        textDoc.readFromSourceURL(url);

        // Return
        return textDoc;
    }
}
