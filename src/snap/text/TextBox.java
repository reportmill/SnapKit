/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.VPos;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.MathUtils;

/**
 * This TextBlock subclass adds support for text wrapping and syncs to a source TextBlock.
 */
public class TextBox extends TextBlock {

    // The TextBlock
    private TextBlock _textBlock;

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

    // A temp var to hold TextLineStyle when updating runs from source text
    private TextLineStyle _updateTextLineStyle;

    /**
     * Constructor.
     */
    public TextBox()
    {
        super();

        // Set default text block
        TextDoc textBlock = new RichText();
        setTextDoc(textBlock);
        setRichText(true);
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
        updateTextAll();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRichText()) return;

        // Do normal version
        super.setRichText(aValue);

        // Forward to source text block
        _textBlock.setRichText(aValue);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.addChars(theChars, theStyle, anIndex);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void removeChars(int aStartCharIndex, int anEndCharIndex)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.removeChars(aStartCharIndex, anEndCharIndex);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.replaceChars(theChars, theStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setStyle(TextStyle aStyle, int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.setStyle(aStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.setLineStyle(aStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.setLineStyleValue(aKey, aValue, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setDefaultStyle(TextStyle aStyle)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.setDefaultStyle(aStyle);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setParentTextStyle(TextStyle aStyle)
    {
        TextBlock textBlock = getTextDoc();
        textBlock.setParentTextStyle(aStyle);
    }

    /**
     * Used to call super.addChars().
     */
    private void super_addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Get start char index - just return if index before text start
        int startCharIndex = Math.max(anIndex, getStartCharIndex());
        if (startCharIndex >= _textBlock.length())
            return;

        // If FontScale is set, replace style with scaled style
        double fontScale = getFontScale();
        if (fontScale != 1)
            theStyle = theStyle.copyFor(theStyle.getFont().scaleFont(fontScale));

        // Do normal version
        super.addChars(theChars, theStyle, anIndex);

        // If linked, remove any lines below bounds
        if (isLinked())
            removeOutOfBoundsLines();
    }

    /**
     * Override to do wrapping.
     */
    @Override
    protected void addCharsToLine(CharSequence theChars, TextStyle theStyle, int charIndex, TextLine textLine, boolean charsHaveNewline)
    {
        // If updating text from source text, update line style
        if (_updateTextLineStyle != null)
            textLine.setLineStyle(_updateTextLineStyle);

        // Do normal version
        super.addCharsToLine(theChars, theStyle, charIndex, textLine, charsHaveNewline);

        // Wrap line if needed
        wrapLineIfNeeded(textLine);
    }

    /**
     * Override to do wrapping.
     */
    @Override
    protected void didRemoveChars(CharSequence removedChars, int startCharIndex)
    {
        // Wrap line if needed
        TextLine textLine = getLineForCharIndex(startCharIndex);
        wrapLineIfNeeded(textLine);
    }

    /**
     * Wraps given line if needed by moving chars from last token(s) to next line.
     */
    protected void wrapLineIfNeeded(TextLine textLine)
    {
        // Reset line alignment
        textLine._x = 0;

        // If WrapLines, do wrapping
        if (isWrapLines() && textLine.getMaxY() < getHeight()) {

            // Get last token
            TextToken lastToken = textLine.getLastToken();

            // If line is now outside bounds, move chars to new line
            while (lastToken != null && isHitRight(lastToken.getMaxX(), textLine.getY(), textLine.getHeight())) {

                // Find first token that is outside bounds and use that instead (if any)
                TextToken prevToken = lastToken.getPrevious();
                while (prevToken != null && isHitRight(prevToken.getMaxX(), textLine.getY(), textLine.getHeight())) {
                    lastToken = prevToken;
                    prevToken = prevToken.getPrevious();
                }

                // If first token, move the minimum number of end chars to next line
                if (lastToken.getIndex() == 0) {
                    moveMinimumLineEndCharsToNextLine(textLine, lastToken);
                    break;
                }

                // Move last token (and trailing chars) to next line
                moveLineCharsToNextLine(textLine, lastToken.getStartCharIndex());
                lastToken = textLine.getLastToken();
            }
        }

        // Update line alignment
        textLine.updateAlignmentAndJustify();
    }

    /**
     * Moves the minimum number of line end chars to next line (leaves at least one char, even if it doesn't fit).
     */
    private void moveMinimumLineEndCharsToNextLine(TextLine textLine, TextToken token)
    {
        // While token is splittable, move end chars to next line
        while (isHyphenate() && token.isSplittable()) {
            token = token.copyForSplittable();
            if (!isHitRight(token.getMaxX(), textLine.getY(), textLine.getHeight())) {
                moveLineCharsToNextLine(textLine, token.getEndCharIndex());
                textLine.getLastToken().setHyphenated(true);
                return;
            }
        }

        // If token has more than 1 char, move last chars to next line
        if (token.getLength() > 1) {

            // Find min length that fits
            int newLength = token.getLength() - 1;
            double endX = token.getXForCharIndex(newLength);
            while (newLength > 1 && isHitRight(endX, textLine.getY(), textLine.getHeight())) {
                newLength--;
                endX = token.getXForCharIndex(newLength);
            }

            // Move chars to next line
            moveLineCharsToNextLine(textLine, token.getStartCharIndex() + newLength);
        }
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
        updateTextAll();
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
        updateTextAll();
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
        updateTextAll();
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
        updateTextAll();
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
        updateTextAll();
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
        updateTextAll();
    }

    /**
     * Updates lines for TextBlock changes.
     */
    protected void textBlockDidPropChange(PropChange aPC)
    {
        // Get PropName
        String propName = aPC.getPropName();

        // Handle CharsChange: Update lines for old/new range
        if (aPC instanceof TextBlockUtils.CharsChange) {
            TextBlockUtils.CharsChange charsChange = (TextBlockUtils.CharsChange) aPC;
            CharSequence newVal = charsChange.getNewValue();
            CharSequence oldVal = charsChange.getOldValue();
            int index = charsChange.getIndex();
            if (oldVal != null)
                super.removeChars(index, index + oldVal.length());
            if (newVal != null)
                super_addChars(newVal, _textBlock.getStyleForCharIndex(index), index);
        }

        // Handle StyleChange
        else if (aPC instanceof TextBlockUtils.StyleChange) {
            TextBlockUtils.StyleChange styleChange = (TextBlockUtils.StyleChange) aPC;
            int startCharIndex = styleChange.getStart();
            int endCharIndex = styleChange.getEnd();
            updateTextForCharRange(startCharIndex, endCharIndex, endCharIndex);
        }

        // Handle LineStyleChange
        else if (aPC instanceof TextBlockUtils.LineStyleChange) {
            TextBlockUtils.LineStyleChange lineStyleChange = (TextBlockUtils.LineStyleChange) aPC;
            TextLine textLine = _textBlock.getLine(lineStyleChange.getIndex());
            int startCharIndex = textLine.getStartCharIndex();
            int endCharIndex = textLine.getEndCharIndex();
            updateTextForCharRange(startCharIndex, endCharIndex, endCharIndex);
        }

        // Handle DefaultTextStyle
        else if (propName == TextBlock.DefaultTextStyle_Prop) {
            TextStyle newStyle = (TextStyle) aPC.getNewValue();
            super.setDefaultStyle(newStyle);
        }

        // Handle ParentTextStyle
        else if (propName == TextBlock.ParentTextStyle_Prop) {
            TextStyle newStyle = (TextStyle) aPC.getNewValue();
            super.setParentTextStyle(newStyle);
        }
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
    protected void updateTextAll()
    {
        int endCharIndexBox = length();
        int endCharIndexBlock = _textBlock.length();
        updateTextForCharRange(0, endCharIndexBox, endCharIndexBlock);
    }

    /**
     * Updates all lines.
     */
    protected void updateTextForCharRange(int startCharIndex, int endCharIndexBox, int endCharIndexBlock)
    {
        // Skip if no text
        if (length() == 0 && _textBlock.length() == 0) return;

        // Remove chars in range
        super.removeChars(startCharIndex, endCharIndexBox);

        // Get run iterator for range (adjusted if this text is overflow from linked)
        int textStartCharIndex = getStartCharIndex();
        int charIndex = Math.max(textStartCharIndex, startCharIndex);
        TextRunIter runIter = _textBlock.getRunIterForCharRange(charIndex, endCharIndexBlock);

        // Iterate over source text runs for range and add
        while (runIter.hasNextRun()) {

            // Set temp LineStyle
            TextRun nextRun = runIter.getNextRun();
            TextLine textLine = nextRun.getLine();
            _updateTextLineStyle = textLine.getLineStyle();

            // Add run chars
            super_addChars(nextRun.getString(), nextRun.getStyle(), charIndex - textStartCharIndex);
            _updateTextLineStyle = null;
            charIndex += nextRun.length();
        }
    }

    /**
     * Returns whether given x location and run hit right border.
     */
    protected boolean isHitRight(double aX, double aY, double aH)
    {
        // Simple case: check if x is within bounds
        if (_boundsPath == null || aY + aH > getMaxY())
            return aX > getWidth();

        // Do BoundsPath version
        Rect rect = new Rect(getX() + aX - 1, aY, 1, aH);
        return !_boundsPath.contains(rect);
    }

    /**
     * Returns the min x value that doesn't hit left border for given y/height and indent.
     */
    protected double getMinHitX(double aY, double aH, double anIndent)
    {
        // Simple case: Return indent
        if (_boundsPath == null || aY + aH > getMaxY())
            return anIndent;

        // Do BoundsPath version
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
        // Simple case: Return MaxX
        if (_boundsPath == null || aY + aH > getMaxY())
            return getMaxX();

        // Do BoundsPath version
        Rect rect = new Rect(getMaxX() - 1, aY, 1, aH);
        while (!_boundsPath.contains(rect) && rect.x > 1)
            rect.x--;
        return rect.x;
    }

    /**
     * Override to update layout.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        if (isWrapLines())
            updateTextAll();
    }

    /**
     * Override to update layout.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        updateTextAll();
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

    /**
     * Removes all lines not fully above bottom border.
     */
    private void removeOutOfBoundsLines()
    {
        TextLine lastLine = getLineLast();

        // While last line not fully above bottom, remove line
        while (lastLine.getMaxY() > getHeight()) {

            // If line 0, just remove chars
            int lineIndex = lastLine.getIndex();
            if (lineIndex == 0) {
                lastLine.removeChars(0, lastLine.length());
                break;
            }

            // Otherwise, remove line
            removeLine(lineIndex);
            lastLine = getLine(lineIndex - 1);
        }

    }

    /**
     * Override to forward to source text.
     */
    @Override
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        return _textBlock.createTokensForTextLine(aTextLine);
    }
}