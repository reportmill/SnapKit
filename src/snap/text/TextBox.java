/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This TextBlock subclass adds support for text wrapping and syncs to a source TextBlock.
 */
public class TextBox extends TextBlock {

    // The source TextBlock
    private TextBlock _sourceText;

    // Whether to wrap lines that overrun bounds
    private boolean _wrapLines;

    // Whether to hyphenate text
    private boolean _hyphenate;

    // Whether text is linked to another text
    private boolean _linked;

    // The start char index of this box in TextBlock
    private int _startCharIndex;

    // The font scale for this box
    protected double _fontScale = 1;

    // The bounds path
    private Shape _boundsPath;

    // A Listener to catch TextBlock PropChanges
    private PropChangeListener _textBlockLsnr = this::handleTextBlockPropChange;

    // A temp var to hold TextLineStyle when updating runs from source text
    private TextLineStyle _updateTextLineStyle;

    /**
     * Constructor.
     */
    public TextBox()
    {
        this(false);
    }

    /**
     * Constructor with option for rich text.
     */
    public TextBox(boolean isRichText)
    {
        super(isRichText);
        TextBlock textBlock = new TextBlock(isRichText);
        setSourceText(textBlock);
    }

    /**
     * Constructor for source text block.
     */
    public TextBox(TextBlock sourceText)
    {
        super(sourceText.isRichText());
        setSourceText(sourceText);
    }

    /**
     * Returns the source TextBlock.
     */
    public TextBlock getSourceText()  { return _sourceText; }

    /**
     * Sets the source TextBlock.
     */
    public void setSourceText(TextBlock aTextBlock)
    {
        // If already set, just return
        if (aTextBlock == _sourceText) return;

        // Stop listening to old TextBlock PropChanges, start listening to new
        if (_sourceText != null)
            _sourceText.removePropChangeListener(_textBlockLsnr);
        _sourceText = aTextBlock;
        _sourceText.addPropChangeListener(_textBlockLsnr);

        // Sync TextStyle/LineStyle
        super.setDefaultTextStyle(aTextBlock.getDefaultTextStyle());
        super.setDefaultLineStyle(aTextBlock.getDefaultLineStyle());

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
        _sourceText.setRichText(aValue);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        _sourceText.addCharsWithStyle(theChars, theStyle, anIndex);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void removeChars(int aStartCharIndex, int anEndCharIndex)
    {
        _sourceText.removeChars(aStartCharIndex, anEndCharIndex);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        _sourceText.replaceCharsWithStyle(theChars, theStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setTextStyle(TextStyle textStyle, int aStart, int anEnd)
    {
        _sourceText.setTextStyle(textStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setTextStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        _sourceText.setTextStyleValue(aKey, aValue, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        _sourceText.setLineStyle(aStyle, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        _sourceText.setLineStyleValue(aKey, aValue, aStart, anEnd);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setDefaultTextStyle(TextStyle textStyle)
    {
        _sourceText.setDefaultTextStyle(textStyle);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public void setDefaultLineStyle(TextLineStyle lineStyle)
    {
        _sourceText.setDefaultLineStyle(lineStyle);
    }

    /**
     * Override to forward to source text block.
     */
    @Override
    public TextBlock copyForRange(int aStart, int aEnd)
    {
        return _sourceText.copyForRange(aStart, aEnd);
    }

    /**
     * Used to call super.addCharsWithStyle().
     */
    private void super_addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Get start char index - just return if index before text start
        int startCharIndex = Math.max(anIndex, getStartCharIndex());
        if (startCharIndex >= _sourceText.length())
            return;

        // If FontScale is set, replace style with scaled style
        double fontScale = getFontScale();
        if (fontScale != 1)
            theStyle = theStyle.copyForStyleValue(theStyle.getFont().copyForScale(fontScale));

        // Do normal version
        super.addCharsWithStyle(theChars, theStyle, anIndex);

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
    }

    /**
     * Override to handle wrap lines.
     */
    @Override
    protected void addCharsToLineFinished(TextLine textLine)
    {
        // Wrap line
        if (isWrapLines())
            wrapLineIfNeeded(textLine);

        // Do normal version
        super.addCharsToLineFinished(textLine);
    }

    /**
     * Wraps given line if needed by moving chars from last token(s) to next line.
     */
    protected void wrapLineIfNeeded(TextLine textLine)
    {
        // If no chars or line is beyond bounds, just return
        if (textLine.getTokenCount() <= 0 || textLine.getMaxY() >= getHeight())
            return;

        // Get last token
        TextToken lastToken = textLine.getLastToken();

        // If line is now outside bounds, move chars to new line
        if (isHitRight(lastToken.getMaxX(), textLine.getY(), textLine.getHeight())) {

            // Find first token that is outside bounds and use that instead (if any)
            TextToken prevToken = lastToken.getPrevious();
            while (prevToken != null && isHitRight(prevToken.getMaxX(), textLine.getY(), textLine.getHeight())) {
                lastToken = prevToken;
                prevToken = prevToken.getPrevious();
            }

            // If first token, move the minimum number of end chars to next line
            if (lastToken.getIndex() == 0)
                moveMinimumLineEndCharsToNextLine(textLine, lastToken);

            // Otherwise, move last token (and trailing chars) to next line
            else moveLineCharsToNextLine(textLine, lastToken.getStartCharIndexInLine());
        }
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
                moveLineCharsToNextLine(textLine, token.getEndCharIndexInLine());
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
            moveLineCharsToNextLine(textLine, token.getStartCharIndexInLine() + newLength);
        }
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
    @Override
    public String getString()
    {
        String textStr = _sourceText.getString();

        // If not same size, trim string
        if (length() < textStr.length()) {
            int startCharIndex = getStartCharIndex();
            int endCharIndex = getEndCharIndex();
            textStr = textStr.substring(startCharIndex, endCharIndex);
        }

        // Return
        return textStr;
    }

    /**
     * Sets the text to the given string.
     */
    @Override
    public void setString(String aString)
    {
        // If already set, just return
        String str = aString != null ? aString : "";
        if (str.length() == length() && str.equals(getString())) return;

        // Forward to SourceText and update all
        _sourceText.setString(str);
    }

    /**
     * Called when TextBlock does property change.
     */
    protected void handleTextBlockPropChange(PropChange aPC)
    {
        // Get PropName
        String propName = aPC.getPropName();

        // Handle CharsChange: Update lines for old/new range
        if (aPC instanceof TextBlockUtils.CharsChange) {
            TextBlockUtils.CharsChange charsChange = (TextBlockUtils.CharsChange) aPC;
            int index = charsChange.getIndex();

            // Handle add chars
            CharSequence addChars = charsChange.getNewValue();
            if (addChars != null)
                updateTextForCharRange(index, index, index + addChars.length());

            // Handle remove chars
            CharSequence removeChars = charsChange.getOldValue();
            if (removeChars != null)
                updateTextForCharRange(index, index + removeChars.length(), index);
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
            TextLine textLine = _sourceText.getLine(lineStyleChange.getIndex());
            int startCharIndex = textLine.getStartCharIndex();
            int endCharIndex = textLine.getEndCharIndex();
            updateTextForCharRange(startCharIndex, endCharIndex, endCharIndex);
        }

        // Handle DefaultTextStyle
        else if (propName == TextBlock.DefaultTextStyle_Prop) {
            TextStyle newStyle = (TextStyle) aPC.getNewValue();
            super.setDefaultTextStyle(newStyle);
        }

        // Handle DefaultLineStyle
        else if (propName == TextBlock.DefaultLineStyle_Prop) {
            TextLineStyle newStyle = (TextLineStyle) aPC.getNewValue();
            super.setDefaultLineStyle(newStyle);
        }
    }

    /**
     * Updates all lines.
     */
    protected void updateTextAll()
    {
        int endCharIndexBox = length();
        int endCharIndexBlock = _sourceText.length();
        updateTextForCharRange(0, endCharIndexBox, endCharIndexBlock);
    }

    /**
     * Updates all lines.
     */
    protected void updateTextForCharRange(int startCharIndex, int endCharIndexBox, int endCharIndexBlock)
    {
        // Skip if no text
        if (length() == 0 && _sourceText.length() == 0) return;

        // If WrapLines, mark location of first line's first token - if it shrinks, might need to re-wrap previous line
        boolean wrapLines = isWrapLines();
        TextLine firstLine = wrapLines ? getLineForCharIndex(startCharIndex) : null;
        double firstLineTokenMaxX = wrapLines ? getFirstTokenMaxXForLineIfPreviousLineCares(firstLine) : 0;

        // Remove chars in range
        super.removeChars(startCharIndex, endCharIndexBox);

        // Get run iterator for range (adjusted if this text is overflow from linked)
        int textStartCharIndex = getStartCharIndex();
        int charIndex = Math.max(textStartCharIndex, startCharIndex);
        TextRunIter runIter = _sourceText.getRunIterForCharRange(charIndex, endCharIndexBlock);

        // Iterate over source text runs for range and add
        while (runIter.hasNextRun()) {

            // Set temp LineStyle
            TextRun nextRun = runIter.getNextRun();
            TextLine textLine = nextRun.getLine();
            _updateTextLineStyle = textLine.getLineStyle();

            // Add run chars
            super_addCharsWithStyle(nextRun.getString(), nextRun.getTextStyle(), charIndex - textStartCharIndex);
            _updateTextLineStyle = null;
            charIndex += nextRun.length();
        }

        // If first token shrank, re-wrap previous line
        if (firstLineTokenMaxX > 0 && firstLineTokenMaxX > getFirstTokenMaxXForLineIfPreviousLineCares(firstLine)) {
            TextLine previousLine = firstLine.getPrevious();
            joinLineWithNextLine(previousLine);
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
        if (isWrapLines())
            updateTextAll();
    }

    /**
     * Returns the preferred width.
     */
    @Override
    public double getPrefWidth()
    {
        double textPrefW = _sourceText.getPrefWidth();
        double fontScale = getFontScale();
        return Math.ceil(textPrefW * fontScale);
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
                    System.err.println("Error scaling text. Could only fit " + length() + " of " + _sourceText.length());
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
                    double prefW = getPrefWidth();
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
        if (lineMaxY >= tboxMaxY || getEndCharIndex() < _sourceText.length())
            return true;

        // If not WrapLines, check X
        if (!isWrapLines()) {
            TextLine line = getLineLongest();
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
        TextLine lastLine = getLastLine();

        // While last line not fully above bottom, remove line
        while (lastLine.getMaxY() > getHeight()) {

            // If line 0, just remove chars
            int lineIndex = lastLine.getLineIndex();
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
        return _sourceText.createTokensForTextLine(aTextLine);
    }

    /**
     * Override to use SourceText.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)  { return _sourceText.toXML(anArchiver); }

    /**
     * Override to use SourceText.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        _sourceText.setPropChangeEnabled(false);
        _sourceText.fromXML(anArchiver, anElement);
        _sourceText.setPropChangeEnabled(true);
        updateTextAll();
        return this;
    }

    /**
     * Returns the MaxX of first token in given line.
     * If no first token or there is no way previous line would need to re-wrap if this shrinks, returns 0.
     */
    private static double getFirstTokenMaxXForLineIfPreviousLineCares(TextLine textLine)
    {
        if (textLine.getTokenCount() == 0)
            return 0;
        TextLine previousLine = textLine.getPrevious();
        if (previousLine == null || previousLine.isLastCharNewline())
            return 0;
        TextToken firstToken = textLine.getToken(0);
        return firstToken.getMaxX();
    }
}