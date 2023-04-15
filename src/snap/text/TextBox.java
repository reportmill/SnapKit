/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;

import snap.geom.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.*;

/**
 * This class manages a TextDoc to be rendered and edited in a bounding area.
 */
public class TextBox {

    // The TextDoc
    private TextDoc  _text;

    // The bounds of the text block
    private double  _x, _y, _width = Float.MAX_VALUE, _height;

    // Whether to wrap lines that overrun bounds
    private boolean  _wrapLines;

    // Whether to hyphenate text
    private boolean  _hyphenate;

    // They y alignment
    private VPos  _alignY = VPos.TOP;

    // The y alignment amount
    private double  _alignedY;

    // Whether text is linked to another text
    private boolean  _linked;

    // The start char index of this box in TextDoc
    private int  _startCharIndex;

    // The font scale for this box
    protected double  _fontScale = 1;

    // The bounds path
    private Shape  _boundsPath;

    // The lines in this text
    private List<TextBoxLine>  _lines = new ArrayList<>();

    // Whether text box needs updating
    private boolean  _needsUpdate, _updating;

    // The update start/end char indexes in TextDoc
    private int _updateStartCharIndex, _updateFromEndCharIndex;

    // A Listener to catch TextDoc PropChanges
    private PropChangeListener  _textDocLsnr = pc -> textDocDidPropChange(pc);

    /**
     * Constructor.
     */
    public TextBox()
    {
        TextDoc textDoc = createTextDoc();
        setTextDoc(textDoc);
    }

    /**
     * Creates a new TextBox initialized with the given String and no attributes.
     */
    public TextBox(CharSequence theChars)
    {
        this();
        addChars(theChars, null, 0);
    }

    /**
     * Returns the TextDoc.
     */
    public TextDoc getTextDoc()  { return _text; }

    /**
     * Sets the TextDoc.
     */
    public void setTextDoc(TextDoc aTextDoc)
    {
        // If already set, just return
        if (aTextDoc == _text) return;

        // Stop listening to old TextDoc PropChanges, start listening to new
        if (_text != null)
            _text.removePropChangeListener(_textDocLsnr);
        _text = aTextDoc;
        _text.addPropChangeListener(_textDocLsnr);

        // Update all
        setNeedsUpdateAll();
    }

    /**
     * Creates the default TextDoc.
     */
    protected TextDoc createTextDoc()  { return new RichText(); }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.isRichText();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRichText()) return;

        // Convert to/from plain/rich text
        TextDoc textDoc = getTextDoc();
        TextDoc textDoc1 = textDoc instanceof RichText ? new TextDoc() : new RichText();
        textDoc1.addTextDoc(textDoc, 0);
        setTextDoc(textDoc1);
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
        if (isWrapLines()) setNeedsUpdateAll();
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
        setNeedsUpdateAll();
    }

    /**
     * Returns the current bounds.
     */
    public Rect getBounds()
    {
        return new Rect(_x, _y, _width, _height);
    }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(Rect aRect)
    {
        setBounds(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight());
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
        setNeedsUpdateAll();
    }

    /**
     * Returns the y for alignment.
     */
    public double getAlignedY()
    {
        return getY() + _alignedY;
    }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return _wrapLines; }

    /**
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)  { _wrapLines = aValue; }

    /**
     * Returns whether layout tries to hyphenate wrapped words.
     */
    public boolean isHyphenate()  { return _hyphenate; }

    /**
     * Sets whether layout tries to hyphenate wrapped words.
     */
    public void setHyphenate(boolean aValue)
    {
        if (aValue == _hyphenate) return;
        _hyphenate = aValue;
        setNeedsUpdateAll();
    }

    /**
     * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
     */
    public boolean isLinked()  { return _linked; }

    /**
     * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
     */
    public void setLinked(boolean aValue)
    {
        if (aValue == _linked) return;
        _linked = aValue;
        setNeedsUpdateAll();
    }

    /**
     * Returns the start char in TextDoc.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Sets the start char in TextDoc.
     */
    public void setStartCharIndex(int charIndex)
    {
        // If already set, just return
        if (charIndex == _startCharIndex) return;

        // Set and update
        _startCharIndex = charIndex;
        setNeedsUpdateAll();
    }

    /**
     * Returns the end char in TextDoc.
     */
    public int getEndCharIndex()
    {
        int startCharIndex = getStartCharIndex();
        TextBoxLine lastLine = getLineLast();
        int lastLineEnd = lastLine != null ? lastLine.getEndCharIndex() : 0;
        return startCharIndex + lastLineEnd;
    }

    /**
     * Returns the font scale of the text box.
     */
    public double getFontScale()  { return _fontScale; }

    /**
     * Sets the font scale of the text box.
     */
    public void setFontScale(double aValue)
    {
        if (aValue == _fontScale) return;
        _fontScale = aValue;
        setNeedsUpdateAll();
    }

    /**
     * Returns the bounds path.
     */
    public Shape getBoundsPath()  { return _boundsPath; }

    /**
     * Sets the bounds path.
     */
    public void setBoundsPath(Shape aPath)  { _boundsPath = aPath; }

    /**
     * Returns the number of chars currently in text box.
     */
    public int length()
    {
        // Get last line - If no lines, just return 0
        TextBoxLine lastLine = getLineLast();
        if (lastLine == null)
            return 0;

        // Return LastLine.EndCharIndex - this is length
        int endCharIndex = lastLine.getEndCharIndex();
        return endCharIndex;
    }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        TextBoxLine textLine = getLineForCharIndex(anIndex);
        int textLineStart = textLine.getStartCharIndex();
        int charIndexInLine = anIndex - textLineStart;
        return textLine.charAt(charIndexInLine);
    }

    /**
     * Returns the number of characters in the text.
     */
    public int getTextDocLength()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.length();
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getString();
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        String str = aString != null ? aString : "";
        if (str.length() == getTextDocLength() && str.equals(getString())) return;

        TextDoc textDoc = getTextDoc();
        textDoc.setString(str);
        setNeedsUpdateAll();
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.addChars(theChars, theStyle, anIndex);
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.removeChars(aStart, anEnd);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.replaceChars(theChars, theStyle, aStart, anEnd);
    }

    /**
     * Returns the number of lines in this text.
     */
    public int getLineCount()
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines.size();
    }

    /**
     * Returns the individual text line in this text.
     */
    public TextBoxLine getLine(int anIndex)
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines.get(anIndex);
    }

    /**
     * Returns the list of lines.
     */
    public List<TextBoxLine> getLines()
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines;
    }

    /**
     * Returns the TextLine at the given char index.
     */
    public TextBoxLine getLineForCharIndex(int anIndex)
    {
        // If NeedsUpdate, do update
        if (_needsUpdate && !_updating)
            update();

        // Iterate over lines and return first that contains index
        for (TextBoxLine line : _lines)
            if (anIndex < line.getEndCharIndex())
                return line;

        // Get last line
        TextBoxLine lastLine = getLineLast();
        if (anIndex == lastLine.getEndCharIndex())
            return lastLine;

        // Complain
        int textBoxLength = length();
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + textBoxLength);
    }

    /**
     * Returns the last line.
     */
    public TextBoxLine getLineLast()
    {
        int lc = getLineCount();
        return lc > 0 ? getLine(lc - 1) : null;
    }

    /**
     * Returns the longest line.
     */
    public TextBoxLine getLineLongest()
    {
        TextBoxLine line = getLineCount() > 0 ? getLine(0) : null;
        if (line == null) return null;
        double lineW = line.getWidth();
        for (TextBoxLine ln : _lines)
            if (ln.getWidth() > lineW) {
                line = ln;
                lineW = ln.getWidth();
            }
        return line;
    }

    /**
     * Updates lines for TextDoc changes.
     */
    protected void textDocDidPropChange(PropChange aPC)
    {
        // Get PropName
        String propName = aPC.getPropName();

        // Handle CharsChange: Update lines for old/new range
        if (aPC instanceof TextDocUtils.CharsChange) {
            TextDocUtils.CharsChange charsChange = (TextDocUtils.CharsChange) aPC;
            CharSequence newVal = charsChange.getNewValue();
            CharSequence oldVal = charsChange.getOldValue();
            int index = charsChange.getIndex();
            if (oldVal != null)
                textDocChangedChars(index, index);
            if (newVal != null)
                textDocChangedChars(index, index + newVal.length());
        }

        // Handle StyleChange
        else if (aPC instanceof TextDocUtils.StyleChange) {
            TextDocUtils.StyleChange styleChange = (TextDocUtils.StyleChange) aPC;
            textDocChangedChars(styleChange.getStart(), styleChange.getEnd());
        }

        // Handle LineStyleChange
        else if (aPC instanceof TextDocUtils.LineStyleChange) {
            TextDocUtils.LineStyleChange lineStyleChange = (TextDocUtils.LineStyleChange) aPC;
            TextDoc textDoc = getTextDoc();
            TextLine textLine = textDoc.getLine(lineStyleChange.getIndex());
            textDocChangedChars(textLine.getStartCharIndex(), textLine.getEndCharIndex());
        }

        // Handle DefaultTextStyle, ParentTextStyle
        else if (propName == TextDoc.DefaultTextStyle_Prop || propName == TextDoc.ParentTextStyle_Prop) {
            if (!isRichText())
                textDocChangedChars(0, getTextDocLength());
        }
    }

    /**
     * Called when chars changed in TextDoc to track range in box and text to be synchronized.
     */
    protected void textDocChangedChars(int aStart, int aEnd)
    {
        int textDocLength = getTextDocLength();
        int fromEndCharIndex = textDocLength - aEnd;
        setUpdateBounds(aStart, fromEndCharIndex);
    }

    /**
     * Updates all lines.
     */
    protected void setNeedsUpdateAll()
    {
        setUpdateBounds(0, 0);
    }

    /**
     * Sets the update bounds (in characters from start and from end).
     */
    protected void setUpdateBounds(int aStart, int aEnd)
    {
        // If first call, set values
        if (!_needsUpdate) {
            _updateStartCharIndex = aStart;
            _updateFromEndCharIndex = aEnd;
            _needsUpdate = true;
        }

        // Successive calls update values
        else {
            _updateStartCharIndex = Math.min(_updateStartCharIndex, aStart);
            _updateFromEndCharIndex = Math.min(_updateFromEndCharIndex, aEnd);
        }
    }

    /**
     * Updates text box.
     */
    protected void update()
    {
        // Set updating
        _updating = true;

        // Convert FromEndCharIndex to endCharIndex for textBox and textDoc
        int textBoxLength = length();
        int textBoxEndCharIndex = textBoxLength - _updateFromEndCharIndex;
        int textDocLength = getTextDocLength();
        int textDocEndCharIndex = textDocLength - _updateFromEndCharIndex;

        // Update lines
        updateLines(_updateStartCharIndex, textBoxEndCharIndex, textDocEndCharIndex);

        // Reset Updating, NeedsUpdate
        _updating = false;
        _needsUpdate = false;
    }

    /**
     * Updates lines for given char start and an old/new char end.
     */
    protected void updateLines(int aStartCharIndex, int textBoxEndCharIndex, int textDocEndCharIndex)
    {
        // Reset AlignY offset
        _alignedY = 0;

        // Get StartLine Index and startCharIndex
        int lineCount = getLineCount();
        TextBoxLine startLine = lineCount > 0 ? getLineForCharIndex(aStartCharIndex) : null;
        int startLineIndex = startLine != null ? startLine.getIndex() : 0;
        int startCharIndex = startLine != null ? startLine.getStartCharIndex() : aStartCharIndex;

        // Remove lines for TextBox change range
        removeLinesForCharRange(aStartCharIndex, textBoxEndCharIndex);

        // Add lines for new range
        addLinesForCharRange(startLineIndex, startCharIndex, textDocEndCharIndex);

        // Iterate over TextBoxLines from startLineIndex to end: Update lines Index, StartCharIndex and Y_Local
        int charIndex = startCharIndex;
        for (int i = startLineIndex, iMax = _lines.size(); i < iMax; i++) {
            TextBoxLine textBoxLine = getLine(i);
            textBoxLine._index = i;
            textBoxLine._startCharIndex = charIndex;
            charIndex += textBoxLine.length();
            textBoxLine._yloc = -1;
        }

        // Calculated aligned Y
        if (_alignY != VPos.TOP) {
            double textBoxW = getWidth();
            double prefH = getPrefHeight(textBoxW);
            double textBoxH = getHeight();
            if (textBoxH > prefH)
                _alignedY = _alignY.doubleValue() * (textBoxH - prefH);
        }
    }

    /**
     * Removes the lines from given char index to given char index.
     */
    protected void addLinesForCharRange(int aLineIndex, int aStartCharIndex, int aEndCharIndex)
    {
        // Get start char index
        int startCharIndex = Math.max(aStartCharIndex, getStartCharIndex());
        if (startCharIndex > getTextDocLength())
            return;

        // Track TextBox insertion line index
        int textBoxInsertLineIndex = aLineIndex;

        // Get TextDoc startLineIndex, endLineIndex
        TextDoc textDoc = getTextDoc();
        int startLineIndex = textDoc.getLineForCharIndex(startCharIndex).getIndex();
        int endLineIndex = textDoc.getLineForCharIndex(aEndCharIndex).getIndex();

        // Iterate over TextDoc lines, create TextBox lines and add
        for (int i = startLineIndex; i <= endLineIndex; i++) {

            // Get text line
            TextLine textLine = textDoc.getLine(i);

            // Get start char index for line
            int charIndex = Math.max(startCharIndex - textLine.getStartCharIndex(), 0);
            if (charIndex == textLine.length())
                continue;

            // Add TextBoxLine(s) for TextLine
            while (charIndex < textLine.length()) {

                // Create line
                TextBoxLine textBoxLine = createTextBoxLine(textLine, charIndex, textBoxInsertLineIndex);
                if ((isLinked() || _boundsPath != null) && textBoxLine.getMaxY() > getMaxY()) {
                    i = Short.MAX_VALUE;
                    break;
                }

                // Add line
                _lines.add(textBoxInsertLineIndex++, textBoxLine);
                charIndex += textBoxLine.length();
            }
        }

        // If we added last line and it is empty or ends with newline, add blank line
        if (endLineIndex == textDoc.getLineCount() - 1 && textBoxInsertLineIndex == getLineCount()) {
            TextLine textLine = textDoc.getLine(endLineIndex);
            if (textLine.length() == 0 || textLine.isLastCharNewline()) {
                TextBoxLine textBoxLine = createTextBoxLine(textLine, textLine.length(), getLineCount());
                if (!((isLinked() || _boundsPath != null) && textBoxLine.getMaxY() > getMaxY()))
                    _lines.add(textBoxLine);
            }
        }
    }

    /**
     * Create and return TextBoxLines for given TextLine, start char index and line index.
     */
    protected TextBoxLine createTextBoxLine(TextLine aTextLine, int startCharIndex, int aLineIndex)
    {
        // Get text vars
        boolean wrap = isWrapLines();
        boolean hyphenate = isHyphenate();
        double fontScale = getFontScale();

        // Get TextToken at start char index - if in middle of token, split token
        TextToken textToken = aTextLine.getTokenForCharIndex(startCharIndex);
        if (textToken != null) {
            if (startCharIndex >= textToken.getEndCharIndex())
                textToken = textToken.getNext();
            else if (startCharIndex > textToken.getStartCharIndex())
                textToken = textToken.copyFromCharIndex(startCharIndex - textToken.getStartCharIndex());
        }
        else if (aTextLine.getTokens().length > 0)
            textToken = aTextLine.getTokens()[0];

        // Get TextToken info
        double startCharX = aTextLine.getXForCharIndex(startCharIndex) * fontScale;
        TextStyle textTokenStyle = textToken != null ? textToken.getTextStyle() : aTextLine.getRunLast().getStyle();
        if (fontScale != 1)
            textTokenStyle = textTokenStyle.copyFor(textTokenStyle.getFont().scaleFont(fontScale));

        // Get LineY, LineH
        double lineY = getY();
        TextBoxLine prevTextBoxLine = aLineIndex > 0 ? getLine(aLineIndex - 1) : null;
        if (prevTextBoxLine != null)
            lineY = prevTextBoxLine.getY() + prevTextBoxLine.getLineAdvance();
        double lineH = textTokenStyle.getLineHeight(); // Should ask remaining tokens instead

        // Get TextBox.X for LineY
        TextLineStyle lineStyle = aTextLine.getLineStyle();
        double lineIndent = startCharIndex == 0 ? lineStyle.getFirstIndent() : lineStyle.getLeftIndent();
        double textBoxX = getMinHitX(lineY, lineH, lineIndent);
        while (textBoxX > getMaxX() && lineY <= getMaxY()) {
            lineY++;
            textBoxX = getMinHitX(lineY, lineH, lineIndent);
        }

        // Create TextBoxLine
        TextBoxLine boxLine = new TextBoxLine(this, textTokenStyle, aTextLine, startCharIndex);
        boxLine._yloc = lineY - getY();

        // While next token is found, add to line
        while (textToken != null) {

            // Handle line wrapping
            double tokenXInBox = textBoxX + textToken.getX() * fontScale - startCharX;
            if (wrap) {

                // If token hits right side, either split or stop
                double tokenMaxXInBox = tokenXInBox + textToken.getWidth() * fontScale;
                if (isHitRight(tokenMaxXInBox, lineY, lineH)) {

                    // If Hyphenate and splittable, get split and try again
                    if (hyphenate && textToken.isSplittable()) {
                        textToken = textToken.copyForSplittable();
                        continue;
                    }

                    // If no tokens added to this line, split off last char and try again so there is at least 1 char
                    if (boxLine.getTokenCount() == 0) {
                        int tokenLength = textToken.getLength();
                        if (tokenLength > 1) {
                            textToken = textToken.copyToCharIndex(tokenLength - 1);
                            continue;
                        }
                    }

                    // Otherwise just break
                    else break;
                }
            }

            // Get textToken info
            int tokenStartCharIndex = textToken.getStartCharIndex() - startCharIndex;
            int tokenEndCharIndex = textToken.getEndCharIndex() - startCharIndex;

            // Create textBoxToken, configure and add
            TextBoxToken textBoxToken = new TextBoxToken(boxLine, textTokenStyle, tokenStartCharIndex, tokenEndCharIndex);
            textBoxToken.setName(textToken.getName());
            textBoxToken.setX(tokenXInBox);
            textBoxToken.setWidth(textToken.getWidth() * fontScale);
            textBoxToken.setTextColor(textToken.getTextColor());
            boxLine.addToken(textBoxToken);

            // If Hyphenated, set TextBoxToken.Hyphenated and break
            if (textToken._split) {
                textBoxToken.setHyphenated(true);
                break;
            }

            // Get next token and update TextToken/TextTokenStyle
            TextToken nextToken = textToken.getNext();
            if (nextToken != null && nextToken.getTextRun() != textToken.getTextRun()) {
                textTokenStyle = nextToken.getTextStyle();
                if (fontScale != 1)
                    textTokenStyle = textTokenStyle.copyFor(textTokenStyle.getFont().scaleFont(fontScale));
            }
            textToken = nextToken;
        }

        // Reset sizes and return
        boxLine.resetSizes();
        return boxLine;
    }

    /**
     * Removes the lines from given char index to given char index, extended to cover entire TextDoc.TextLines.
     */
    protected void removeLinesForCharRange(int startCharIndex, int endCharIndex)
    {
        // If no lines, just return
        if (getLineCount() == 0) return;

        // Get StartLine for UpdateStartCharIndex (extend to TextDocLine)
        TextBoxLine startLine = getLineForCharIndex(startCharIndex);
        TextBoxLine endLine = getLineForCharIndex(endCharIndex);

        // If TextBox.WrapLines, extend EndLine to end of TextDoc line
        if (isWrapLines()) {
            while (true) {
                TextBoxLine nextLine = endLine.getNext();
                if (nextLine != null && endLine.getTextLine() == nextLine.getTextLine())
                    endLine = nextLine;
                else break;
            }
        }

        // Remove lines in range
        int startLineIndex = startLine.getIndex();
        int endLineIndex = endLine.getIndex() + 1;
        _lines.subList(startLineIndex, endLineIndex).clear();
    }

    /**
     * Returns whether given x location and run hit right border.
     */
    protected boolean isHitRight(double aX, double aY, double aH)
    {
        if (_boundsPath == null || aY + aH > getMaxY())
            return aX > getWidth();
        Rect rect = new Rect(getX() + aX - 1, aY, 1, aH);
        return !_boundsPath.contains(rect);
    }

    /**
     * Returns the min x value that doesn't hit left border for given y/height and indent.
     */
    protected double getMinHitX(double aY, double aH, double anIndent)
    {
        if (_boundsPath == null || aY + aH > getMaxY()) return anIndent;
        Rect rect = new Rect(getX() + anIndent, aY, 20, aH);
        while (!_boundsPath.contains(rect) && rect.x <= getMaxX()) rect.x++;
        return rect.x - getX();
    }

    /**
     * Returns the max x value that doesn't hit right border for given y/height.
     */
    protected double getMaxHitX(double aY, double aH)
    {
        if (_boundsPath == null || aY + aH > getMaxY()) return getMaxX();
        Rect rect = new Rect(getMaxX() - 1, aY, 1, aH);
        while (!_boundsPath.contains(rect) && rect.x > 1) rect.x--;
        return rect.x;
    }

    /**
     * Returns the token at given index.
     */
    public TextBoxToken getTokenForCharIndex(int anIndex)
    {
        TextBoxLine line = getLineForCharIndex(anIndex);
        int indexInLine = anIndex - line.getStartCharIndex();
        return line.getTokenForCharIndex(indexInLine);
    }

    /**
     * Returns whether text box contains an underlined run.
     */
    public boolean isUnderlined()
    {
        return getTextDoc().isUnderlined();
    }

    /**
     * Returns underlined runs for text box.
     */
    public List<TextBoxRun> getUnderlineRuns(Rect aRect)
    {
        // Iterate over lines to add underline runs to list
        List<TextBoxRun> uruns = new ArrayList<>();
        for (TextBoxLine line : getLines()) {

            // If line above rect, continue, if below, break
            if (aRect != null) {
                if (line.getMaxY() < aRect.y) continue;
                else if (line.getY() >= aRect.getMaxY())
                    break;
            }

            // If run underlined, add to list
            for (TextBoxRun run : line.getRuns())
                if (run.getStyle().isUnderlined())
                    uruns.add(run);
        }

        // Return list
        return uruns;
    }

    /**
     * Returns the line for the given y value.
     */
    public TextBoxLine getLineForY(double aY)
    {
        // If y less than zero, return null
        if (aY < 0) return null;

        // Iterate over lines and return one that spans given y
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            TextBoxLine line = getLine(i);
            if (aY < line.getMaxY())
                return line;
        }

        // If no line for given y, return last line
        return getLineLast();
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndexForXY(double anX, double aY)
    {
        TextBoxLine textBoxLine = getLineForY(aY);
        if (textBoxLine == null) return 0;
        int charIndex = textBoxLine.getCharIndexForX(anX);
        return textBoxLine.getStartCharIndex() + charIndex;
    }

    /**
     * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
     */
    public Shape getPathForCharRange(int aStartCharIndex, int aEndCharIndex)
    {
        // Create new path for return
        Path2D path = new Path2D();

        // If invalid range, just return
        if (aStartCharIndex > getEndCharIndex() || aEndCharIndex < getStartCharIndex())
            return path;
        if (aEndCharIndex > getEndCharIndex())
            aEndCharIndex = getEndCharIndex();

        // Get StartLine, EndLine and start/end points
        TextBoxLine startLine = getLineForCharIndex(aStartCharIndex);
        TextBoxLine endLine = aStartCharIndex == aEndCharIndex ? startLine : getLineForCharIndex(aEndCharIndex);
        double startX = startLine.getXForCharIndex(aStartCharIndex - startLine.getStartCharIndex());
        double startY = startLine.getBaseline();
        double endX = endLine.getXForCharIndex(aEndCharIndex - endLine.getStartCharIndex());
        double endY = endLine.getBaseline();
        startX = Math.min(startX, getMaxX());
        endX = Math.min(endX, getMaxX());

        // Get start top/height
        double startTop = startLine.getY() - 1;
        double startHeight = startLine.getHeight() + 2;

        // Get path for upper left corner of sel start
        path.moveTo(startX, startTop + startHeight);
        path.lineTo(startX, startTop);
        if (aStartCharIndex == aEndCharIndex)
            return path;

        // If selection spans more than one line, add path components for middle lines and end line
        if (startY != endY) {  //!SnapMath.equals(startY, endY)
            double endTop = endLine.getY() - 1;
            double endHeight = endLine.getHeight() + 2;
            path.lineTo(getWidth(), startTop);
            path.lineTo(getWidth(), endTop);
            path.lineTo(endX, endTop);
            path.lineTo(endX, endTop + endHeight);
            path.lineTo(getX(), endTop + endHeight);
            path.lineTo(getX(), startTop + startHeight);
        }

        // If selection spans only one line, add path components for upper-right, lower-right
        else {
            path.lineTo(endX, startTop);
            path.lineTo(endX, startTop + startHeight);
        }

        // Close path and return
        path.close();
        return path;
    }

    /**
     * Paint TextBox to given painter.
     */
    public void paint(Painter aPntr)
    {
        // Get intersection of clip rect and bounds
        aPntr.save();
        Rect clip = aPntr.getClipBounds();
        clip = clip != null ? clip.getIntersectRect(getBounds()) : getBounds();
        aPntr.clip(clip);

        // Iterate over lines
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            TextBoxLine line = getLine(i);
            double lineY = line.getBaseline();
            if (line.getMaxY() < clip.getMinY()) continue;
            if (line.getY() >= clip.getMaxY()) break;

            // Iterate over line tokens
            for (int j = 0, jMax = line.getTokenCount(); j < jMax; j++) {
                TextBoxToken token = line.getToken(j);

                // Do normal paint token
                String tokenStr = token.getString();
                double tokenX = token.getTextBoxX();
                aPntr.setFont(token.getFont());
                aPntr.setPaint(token.getTextColor()); //aPntr.setPaint(SnapColor.RED);
                aPntr.drawString(tokenStr, tokenX, lineY, token.getTextStyle().getCharSpacing());

                // Handle TextBorder: Get outline and stroke
                Border border = token.getTextStyle().getBorder();
                if (border != null) {
                    Shape shape = token.getFont().getOutline(tokenStr, tokenX, lineY, token.getTextStyle().getCharSpacing());
                    aPntr.setPaint(border.getColor());
                    aPntr.setStroke(Stroke.Stroke1.copyForWidth(border.getWidth()));
                    aPntr.draw(shape);
                }
            }
        }

        // Paint underlines
        if (isUnderlined()) {

            for (TextBoxRun run : getUnderlineRuns(clip)) {

                // Set underline color and width
                TextBoxLine line = run.getLine();
                double uy = run.getFont().getUnderlineOffset();
                double uw = run.getFont().getUnderlineThickness();
                aPntr.setColor(run.getColor());
                aPntr.setStrokeWidth(uw);

                // Get under line endpoints and draw line
                double x0 = run.getX();
                double y0 = line.getBaseline() - uy;
                double x1 = run.getMaxX();
                if (run.getEnd() == line.getEndCharIndex())
                    x1 = line.getX() + line.getWidthNoWhiteSpace();
                aPntr.drawLine(x0, y0, x1, y0);
            }
        }

        // Restore state
        aPntr.restore();
    }

    /**
     * Returns the preferred width.
     */
    public double getPrefWidth(double aH)
    {
        TextDoc textDoc = getTextDoc();
        double textPrefW = textDoc.getPrefWidth();
        double fontScale = getFontScale();
        double prefW = Math.ceil(textPrefW * fontScale);
        return prefW;
    }

    /**
     * Returns the preferred height.
     */
    public double getPrefHeight(double aW)
    {
        // If WrapLines and given Width doesn't match current Width, setWidth
        if (isWrapLines() && !MathUtils.equals(aW, getWidth()) && aW > 0) { //double oldW = getWidth();
            setWidth(aW);
            double prefH = getPrefHeight(aW); //setWidth(oldW); Should really reset old width - but why would they ask,
            return prefH;                     // if they didn't plan to use this width?
        }

        // Return bottom of last line minus box Y
        TextBoxLine lastLine = getLineLast();
        if (lastLine == null) return 0;
        double lineMaxY = lastLine.getMaxY();
        double alignedY = getAlignedY();
        return Math.ceil(lineMaxY - alignedY);
    }

    /**
     * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()
    {
        // Do normal layout
        if (!isTextOutOfBounds()) return;

        // Declare starting fontScale factor and dampening variables
        double fontScale;
        double textW = getWidth();
        double textH = getHeight();
        double fsLo = 0;
        double fsHi = 1;

        // Loop while dampening variables are normal
        while (true) {

            // Reset fontScale to mid-point of fsHi and fsLo
            fontScale = (fsLo + fsHi) / 2;
            setFontScale(fontScale);

            // If text exceeded layout bounds, reset fsHi to fontScale
            if (isTextOutOfBounds()) {
                fsHi = fontScale;
                if ((fsHi + fsLo) / 2 == 0) {
                    System.err.println("Error scaling text. Could only fit " + length() + " of " + getTextDocLength());
                    break;
                }
            }

            // If text didn't exceed layout bounds, reset fsLo to fontScale
            else {

                // Set new low (if almost fsHi, just return)
                fsLo = fontScale;
                double detaFS = fsHi - fsLo;
                if (detaFS < .05)
                    break;

                // If no line-wrap and PrefWidth almost TextBox.Width, stop
                if (!isWrapLines()) {
                    double prefW = getPrefWidth(-1);
                    double diffW = textW - prefW;
                    if (diffW < 1)
                        break;
                }

                // If PrefHeight almost TextBox.Height, stop
                double prefH = getPrefHeight(textW);
                double diffH = textH - prefH;
                if (diffH < 1)
                    break;
            }
        }
    }

    /**
     * Returns whether this text box couldn't fit all text.
     */
    public boolean isTextOutOfBounds()
    {
        // Check Y no matter what
        int lineCount = getLineCount();
        double lineMaxY = lineCount > 0 ? getLine(lineCount - 1).getMaxY() : 0;
        double tboxMaxY = getMaxY();
        if (lineMaxY >= tboxMaxY || getEndCharIndex() < getTextDoc().length())
            return true;

        // If not WrapLines, check X
        if (!isWrapLines()) {
            TextBoxLine line = getLineLongest();
            //double lineMaxX = line != null ? line.getMaxX() : 0;
            //double tboxMaxX = getMaxX();
            //if (lineMaxX > tboxMaxX) return true;
            double lineW = line != null ? line.getWidth() : 0;
            double tboxW = getWidth();
            if (lineW > tboxW)
                return true;
        }

        // Return false
        return false;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getClass().getSimpleName() + " [" + getBounds().getSvgString() + "]: ";
        str += _lines.size() + " lines, " + length() + " chars";
        return str;
    }
}