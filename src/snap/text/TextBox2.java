/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.VPos;
import snap.gfx.Border;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages a TextBlock to be rendered and edited in a bounding area.
 */
public class TextBox2 extends TextBlock {

    // The TextBlock
    private TextBlock _textBlock;

    // The X/Y of the text block
    private double _x, _y;

    // The width/height of the text block
    private double _width = Float.MAX_VALUE, _height;

    // Whether to wrap lines that overrun bounds
    private boolean _wrapLines;

    // Whether to hyphenate text
    private boolean _hyphenate;

    // They y alignment
    private VPos _alignY = VPos.TOP;

    // The y alignment amount
    private double _alignedY;

    // Whether text is linked to another text
    private boolean _linked;

    // The start char index of this box in TextBlock
    private int _startCharIndex;

    // The font scale for this box
    protected double _fontScale = 1;

    // The bounds path
    private Shape _boundsPath;

    // A Listener to catch TextBlock PropChanges
    private PropChangeListener _textBlockLsnr = pc -> textBlockDidPropChange(pc);

    /**
     * Constructor.
     */
    public TextBox2()
    {
        TextDoc textBlock = createTextDoc();
        setTextDoc(textBlock);
    }

    /**
     * Creates a new TextBox initialized with the given String and no attributes.
     */
    public TextBox2(CharSequence theChars)
    {
        this();
        addChars(theChars, null, 0);
    }

    /**
     * Returns the TextBlock.
     */
    public TextDoc getTextDoc()  { return (TextDoc) _textBlock; }

    /**
     * Sets the TextBlock.
     */
    public void setTextDoc(TextBlock aTextBlock)
    {
        // If already set, just return
        if (aTextBlock == _textBlock) return;

        // Stop listening to old TextBlock PropChanges, start listening to new
        if (_textBlock != null)
            _textBlock.removePropChangeListener(_textBlockLsnr);
        _textBlock = aTextBlock;
        _textBlock.addPropChangeListener(_textBlockLsnr);

        // Update all
        setNeedsUpdateAll();
    }

    /**
     * Creates the default TextBlock.
     */
    protected TextDoc createTextDoc()  { return new RichText(); }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()
    {
        TextBlock textBlock = getTextDoc();
        return textBlock.isRichText();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRichText()) return;

        // Convert to/from plain/rich text
        TextBlock textBlock = getTextDoc();
        TextDoc textBlock2 = textBlock instanceof RichText ? new TextDoc() : new RichText();
        textBlock2.addTextBlock(textBlock, 0);
        setTextDoc(textBlock2);
    }

    /**
     * Override to do wrapping.
     */
    @Override
    protected TextLine addCharsToLine(CharSequence theChars, TextStyle theStyle, int charIndex, TextLine textLine, int newlineIndex)
    {
        return super.addCharsToLine(theChars, theStyle, charIndex, textLine, newlineIndex);
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
     * Returns the start char in source TextBlock.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Sets the start char in source TextBlock.
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
     * Returns the end char in source TextBlock.
     */
    public int getEndCharIndex()
    {
        int startCharIndex = getStartCharIndex();
        TextLine lastLine = getLineLast();
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
        if (str.length() == length() && str.equals(getString())) return;

        TextBlock textBlock = getTextDoc();
        textBlock.setString(str);
        setNeedsUpdateAll();
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.addChars(theChars, theStyle, anIndex);
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.removeChars(aStart, anEnd);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.replaceChars(theChars, theStyle, aStart, anEnd);
    }

    /**
     * Updates lines for TextBlock changes.
     */
    protected void textBlockDidPropChange(PropChange aPC)
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
                textBlockChangedChars(index, index);
            if (newVal != null)
                textBlockChangedChars(index, index + newVal.length());
        }

        // Handle StyleChange
        else if (aPC instanceof TextDocUtils.StyleChange) {
            TextDocUtils.StyleChange styleChange = (TextDocUtils.StyleChange) aPC;
            textBlockChangedChars(styleChange.getStart(), styleChange.getEnd());
        }

        // Handle LineStyleChange
        else if (aPC instanceof TextDocUtils.LineStyleChange) {
            TextDocUtils.LineStyleChange lineStyleChange = (TextDocUtils.LineStyleChange) aPC;
            TextBlock textBlock = getTextDoc();
            TextLine textLine = textBlock.getLine(lineStyleChange.getIndex());
            textBlockChangedChars(textLine.getStartCharIndex(), textLine.getEndCharIndex());
        }

        // Handle DefaultTextStyle, ParentTextStyle
        else if (propName == TextBlock.DefaultTextStyle_Prop || propName == TextBlock.ParentTextStyle_Prop) {
            if (!isRichText())
                textBlockChangedChars(0, length());
        }
    }

    /**
     * Called when chars changed in TextBlock to track range in box and text to be synchronized.
     */
    protected void textBlockChangedChars(int aStart, int aEnd)
    {
        TextBlock textBlock = getTextDoc();
        int textBlockLength = textBlock.length();
        int fromEndCharIndex = textBlockLength - aEnd;
        setUpdateBounds(aStart, fromEndCharIndex);
    }

    /**
     * Updates lines for given char start and an old/new char end.
     */
    protected void updateLines(int lineIndex)
    {
        // Do normal version
        super.updateLines(lineIndex);

        // Reset AlignY offset
        _alignedY = 0;

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
     * Updates all lines.
     */
    protected void setNeedsUpdateAll()  { }

    /**
     * Sets the update bounds (in characters from start and from end).
     */
    protected void setUpdateBounds(int aStart, int aEnd)  { }

    /**
     * Removes the lines from given char index to given char index.
     */
    protected void addLinesForCharRange(int aLineIndex, int aStartCharIndex, int aEndCharIndex)
    {
        // Get start char index
        int startCharIndex = Math.max(aStartCharIndex, getStartCharIndex());
        if (startCharIndex > getTextDoc().length())
            return;

        // Track TextBox insertion line index
        int textBoxInsertLineIndex = aLineIndex;

        // Get TextBlock startLineIndex, endLineIndex
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
                TextLine textBoxLine = createTextBoxLine(textLine, charIndex, textBoxInsertLineIndex);
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
                TextLine textBoxLine = createTextBoxLine(textLine, textLine.length(), getLineCount());
                if (!((isLinked() || _boundsPath != null) && textBoxLine.getMaxY() > getMaxY()))
                    _lines.add(textBoxLine);
            }
        }
    }

    /**
     * Create and return TextBoxLines for given TextLine, start char index and line index.
     */
    protected TextLine createTextBoxLine(TextLine aTextLine, int startCharIndex, int aLineIndex)
    {
        // Get text vars
        boolean wrap = isWrapLines();
        boolean hyphenate = isHyphenate();
        double fontScale = getFontScale();

        // Get TextToken at start char index - if in middle of token, split token
        TextToken textToken = aTextLine.getLastTokenForCharIndex(startCharIndex);
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
        TextLine prevTextBoxLine = aLineIndex > 0 ? getLine(aLineIndex - 1) : null;
        if (prevTextBoxLine != null)
            lineY = prevTextBoxLine.getY() + prevTextBoxLine.getMetrics().getLineAdvance();
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
        TextLine boxLine = new TextLine(this);//, textTokenStyle, aTextLine, startCharIndex);
        boxLine._y = lineY - getY();

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
            TextToken textBoxToken = new TextToken(boxLine, tokenStartCharIndex, tokenEndCharIndex, null);//textTokenStyle);
            textBoxToken.setName(textToken.getName());
//            textBoxToken.setX(tokenXInBox);
//            textBoxToken.setWidth(textToken.getWidth() * fontScale);
            textBoxToken.setTextColor(textToken.getTextColor());
//            boxLine.addToken(textBoxToken);

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
//        boxLine.resetSizes();
        return boxLine;
    }

    /**
     * Removes the lines from given char index to given char index, extended to cover entire TextBlock.TextLines.
     */
    protected void removeLinesForCharRange(int startCharIndex, int endCharIndex)
    {
        // If no lines, just return
        if (getLineCount() == 0) return;

        // Get StartLine for UpdateStartCharIndex (extend to TextDocLine)
        TextLine startLine = getLineForCharIndex(startCharIndex);
        TextLine endLine = getLineForCharIndex(endCharIndex);

        // If TextBox.WrapLines, extend EndLine to end of TextDoc line
        if (isWrapLines()) {
            while (true) {
                TextLine nextLine = endLine.getNext();
//                if (nextLine != null && endLine.getTextLine() == nextLine.getTextLine())
//                    endLine = nextLine;
//                else break;
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
        if (_boundsPath == null || aY + aH > getMaxY())
            return anIndent;
        Rect rect = new Rect(getX() + anIndent, aY, 20, aH);
        while (!_boundsPath.contains(rect) && rect.x <= getMaxX())
            rect.x++;
        return rect.x - getX();
    }

    /**
     * Returns the max x value that doesn't hit right border for given y/height.
     */
    protected double getMaxHitX(double aY, double aH)
    {
        if (_boundsPath == null || aY + aH > getMaxY())
            return getMaxX();
        Rect rect = new Rect(getMaxX() - 1, aY, 1, aH);
        while (!_boundsPath.contains(rect) && rect.x > 1)
            rect.x--;
        return rect.x;
    }

    /**
     * Returns underlined runs for text box.
     */
    public List<TextRun> getUnderlineRuns(Rect aRect)
    {
        // Iterate over lines to add underline runs to list
        List<TextRun> underlineRuns = new ArrayList<>();
        for (TextLine line : getLines()) {

            // If line above rect, continue, if below, break
            if (aRect != null) {
                if (line.getMaxY() < aRect.y) continue;
                else if (line.getY() >= aRect.getMaxY())
                    break;
            }

            // If run underlined, add to list
            for (TextRun run : line.getRuns())
                if (run.getStyle().isUnderlined())
                    underlineRuns.add(run);
        }

        // Return
        return underlineRuns;
    }

    /**
     * Returns the line for the given y value.
     */
    public TextLine getLineForY(double aY)
    {
        // If y less than zero, return null
        if (aY < 0) return null;

        // Iterate over lines and return one that spans given y
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            TextLine line = getLine(i);
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
        TextLine textBoxLine = getLineForY(aY);
        if (textBoxLine == null)
            return 0;
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
        TextLine startLine = getLineForCharIndex(aStartCharIndex);
        TextLine endLine = aStartCharIndex == aEndCharIndex ? startLine : getLineForCharIndex(aEndCharIndex);
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
            TextLine line = getLine(i);
            double lineY = line.getBaseline();
            if (line.getMaxY() < clip.getMinY())
                continue;
            if (line.getY() >= clip.getMaxY())
                break;

            // Iterate over line tokens
            TextToken[] lineTokens = line.getTokens();
            for (TextToken token : lineTokens) {

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

            for (TextRun run : getUnderlineRuns(clip)) {

                // Set underline color and width
                TextLine line = run.getLine();
                double uy = run.getFont().getUnderlineOffset();
                double uw = run.getFont().getUnderlineThickness();
                aPntr.setColor(run.getColor());
                aPntr.setStrokeWidth(uw);

                // Get under line endpoints and draw line
                double x0 = run.getX();
                double y0 = line.getBaseline() - uy;
                double x1 = run.getMaxX();
                if (run.getEndCharIndex() == line.getEndCharIndex())
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
        TextBlock textBlock = getTextDoc();
        double textPrefW = textBlock.getPrefWidth();
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
        TextLine lastLine = getLineLast();
        if (lastLine == null)
            return 0;
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
                    System.err.println("Error scaling text. Could only fit " + length() + " of " + getTextDoc().length());
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
            TextLine line = getLineLongest();
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
}