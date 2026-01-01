package snap.text;
import snap.geom.HPos;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.VPos;
import snap.gfx.Painter;
import snap.props.PropObject;
import snap.util.CharSequenceX;
import snap.util.ListUtils;
import snap.util.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface provides the functionality to describe the layout of text.
 */
public abstract class TextLayout extends PropObject {

    // Whether text is rich
    protected boolean _rich;

    // The X/Y of the text model
    protected double _x, _y;

    // The width/height of the text model
    protected double _width = Float.MAX_VALUE, _height;

    // The pref width of the text model
    protected double _prefW = -1;

    // They y alignment
    protected VPos _alignY = VPos.TOP;

    // The y alignment amount
    protected double _alignedY = -1;

    // A version of this layout as a CharSequenceX
    protected CharSequenceX _charsX;

    /**
     * Constructor.
     */
    public TextLayout()
    {
        this(false);
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextLayout(boolean isRich)
    {
        super();
        _rich = isRich;
    }

    /**
     * Returns the text model that is being displayed.
     */
    public TextModel getTextModel()  { return (TextModel) this; }

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    public boolean isRichText()  { return _rich; }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return false; }

    /**
     * Returns the number of characters in the text.
     */
    public abstract int length();

    /**
     * Returns whether text is empty.
     */
    public boolean isEmpty()  { return length() == 0; }

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
        if (aStart == 0 && anEnd == length())
            return getChars();
        if (getLineCount() == 1)
            return getLine(0).subSequence(aStart, anEnd);

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
    public abstract String getString();

    /**
     * Returns a char sequence for layout.
     */
    public CharSequence getChars()
    {
        if (getLineCount() == 1)
            return getLine(0).getChars();
        return getCharsX();
    }

    /**
     * Returns a char sequence for layout.
     */
    public CharSequenceX getCharsX()
    {
        if (_charsX != null) return _charsX;
        return _charsX = new CharSequenceX() {
            public int length()  { return TextLayout.this.length(); }
            public char charAt(int i)  { return TextLayout.this.charAt(i); }
            public CharSequence subSequence(int aStart, int anEnd)  { return TextLayout.this.subSequence(aStart, anEnd); }
        };
    }

    /**
     * Returns the number of block in this doc.
     */
    public abstract int getLineCount();

    /**
     * Returns the individual block in this doc.
     */
    public abstract TextLine getLine(int anIndex);

    /**
     * Returns the list of blocks.
     */
    public abstract List<TextLine> getLines();

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
     * Returns the longest line.
     */
    public TextLine getLineLongest()
    {
        TextLine longLine = null;
        double longW = 0;
        for (TextLine line : getLines()) {
            if (line.getWidth() > longW) {
                longLine = line;
                longW = line.getWidth();
            }
        }

        // Return
        return longLine;
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
        if (isRichText())
            return ListUtils.hasMatch(getLines(), line -> line.isUnderlined());

        TextStyle textStyle = getTextStyleForCharIndex(0);
        return textStyle.isUnderlined();
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
     * Paint text to given painter.
     */
    public void paint(Painter aPntr)
    {
        // Just return if no lines
        int lineCount = getLineCount();
        if (lineCount == 0)
            return;
        if (lineCount == 1) {
            getLine(0).paint(aPntr);
            return;
        }

        // Get text clip bounds and clip
        Rect textBounds = getBounds();
        Rect pntrClipBounds = aPntr.getClipBounds();
        Rect textClipBounds = pntrClipBounds != null ? pntrClipBounds.getIntersectRect(textBounds) : textBounds;
        if (textClipBounds.isEmpty())
            return;

        // Save painter state and clip
        aPntr.save();
        aPntr.clip(textClipBounds);

        // Iterate over lines
        for (int i = 0; i < lineCount; i++) {

            // If line not yet visible, skip
            TextLine textLine = getLine(i);
            if (textLine.getTextMaxY() < textClipBounds.y)
                continue;

            // If line no longer visible, break
            if (textLine.getTextY() >= textClipBounds.getMaxY())
                break;

            // Paint line
            if (!textLine.isBlank())
                textLine.paintLine(aPntr);
        }

        // Restore state
        aPntr.restore();
    }

    /**
     * Returns underlined runs for text.
     */
    public List<TextRun> getUnderlineRuns(Rect clipRect)
    {
        List<TextRun> underlineRuns = new ArrayList<>();

        // Iterate over lines to add underline runs to list
        for (TextLine line : getLines()) {

            // If line above rect, continue, if below, break
            if (clipRect != null) {
                if (line.getMaxY() < clipRect.y)
                    continue;
                else if (line.getY() >= clipRect.getMaxY())
                    break;
            }

            // If run underlined, add to list
            for (TextRun run : line.getRuns())
                if (run.getTextStyle().isUnderlined())
                    underlineRuns.add(run);
        }

        // Return
        return underlineRuns;
    }

    /**
     * Returns whether this text couldn't fit all text.
     */
    public boolean isTextOutOfBounds()
    {
        // Check Y no matter what
        int lineCount = getLineCount();
        double lineMaxY = lineCount > 0 ? getLine(lineCount - 1).getMaxY() : 0;
        double textMaxY = getMaxY();
        if (lineMaxY >= textMaxY || getEndCharIndex() < getTextModel().length())
            return true;

        // If not WrapLines, check X
        if (!isWrapLines()) {
            TextLine line = getLineLongest();
            double lineW = line != null ? line.getWidth() : 0;
            double textW = getWidth();
            if (lineW > textW)
                return true;
        }

        // Return false
        return false;
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
     * Returns a path for two char indexes - it will be a simple box with extensions for first/last lines.
     */
    public Shape getPathForCharRange(int aStartCharIndex, int aEndCharIndex)
    {
        return TextModelUtils.getPathForCharRange(this, aStartCharIndex, aEndCharIndex);
    }
}
