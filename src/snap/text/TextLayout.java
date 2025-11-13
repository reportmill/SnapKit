package snap.text;
import snap.geom.HPos;
import snap.geom.Rect;
import snap.geom.VPos;
import snap.gfx.Border;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.util.CharSequenceX;
import java.util.List;

/**
 * This interface provides the functionality to describe the layout of text.
 */
public interface TextLayout extends CharSequenceX {

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    boolean isRichText();

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    boolean isWrapLines();

    /**
     * Returns the number of characters in the text.
     */
    int length();

    /**
     * Returns the char value at the specified index.
     */
    default char charAt(int anIndex)
    {
        TextLine line = getLineForCharIndex(anIndex);
        return line.charAt(anIndex - line.getStartCharIndex());
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    default CharSequence subSequence(int aStart, int anEnd)
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
    String getString();

    /**
     * Returns the number of block in this doc.
     */
    int getLineCount();

    /**
     * Returns the individual block in this doc.
     */
    TextLine getLine(int anIndex);

    /**
     * Returns the list of blocks.
     */
    List<TextLine> getLines();

    /**
     * Returns the block at the given char index.
     */
    default TextLine getLineForCharIndex(int charIndex)
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
    default TextLine getLastLine()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Returns the longest line.
     */
    default TextLine getLineLongest()
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
    default TextRun getRunForCharIndex(int charIndex)
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
    default TextRun getRunForCharRange(int startIndex, int endIndex)
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
    default TextToken getTokenForCharIndex(int charIndex)
    {
        TextLine textLine = getLineForCharIndex(charIndex);
        int lineStart = textLine.getStartCharIndex();
        int selStartInLine = charIndex - lineStart;
        return textLine.getTokenForCharIndex(selStartInLine);
    }

    /**
     * Returns the Font for run at given character index.
     */
    default Font getFontForCharIndex(int charIndex)
    {
        TextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getFont();
    }

    /**
     * Returns the TextStyle for the run at the given character index.
     */
    default TextStyle getTextStyleForCharIndex(int charIndex)
    {
        TextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getTextStyle();
    }

    /**
     * Returns the TextStyle for the run for given char range.
     */
    default TextStyle getTextStyleForCharRange(int startIndex, int endIndex)
    {
        TextRun textRun = getRunForCharRange(startIndex, endIndex);
        return textRun.getTextStyle();
    }

    /**
     * Returns the TextLineStyle for the run at the given character index.
     */
    default TextLineStyle getLineStyleForCharIndex(int charIndex)
    {
        TextLine textLine = getLineForCharIndex(charIndex);
        return textLine.getLineStyle();
    }

    /**
     * Returns the line for the given y value.
     */
    default TextLine getLineForY(double aY)
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
    default int getCharIndexForXY(double anX, double aY)
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
    default boolean isUnderlined()
    {
        // Handle Rich
        if (isRichText()) {
            for (TextLine line : getLines())
                if (line.isUnderlined())
                    return true;
        }

        TextStyle textStyle = getTextStyleForCharIndex(0);
        return textStyle.isUnderlined();
    }

    /**
     * Returns the horizontal alignment of the first paragraph of the text.
     */
    default HPos getAlignX()
    {
        TextLineStyle lineStyle = getLineStyleForCharIndex(0);
        return lineStyle.getAlign();
    }

    /**
     * Returns the Y alignment.
     */
    VPos getAlignY();

    /**
     * Returns the y for alignment.
     */
    double getAlignedY();

    /**
     * Returns the start char index (always 0, unless this is SubText).
     */
    default int getStartCharIndex()  { return 0; }

    /**
     * Returns the end char in source text.
     */
    default int getEndCharIndex()
    {
        int startCharIndex = getStartCharIndex();
        TextLine lastLine = getLastLine();
        int lastLineEnd = lastLine != null ? lastLine.getEndCharIndex() : 0;
        return startCharIndex + lastLineEnd;
    }

    /**
     * Returns the index of given string.
     */
    default int indexOf(String aStr, int aStart)
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
    double getX();

    /**
     * Returns the Y location.
     */
    double getY();

    /**
     * Returns the width.
     */
    double getWidth();

    /**
     * Returns the height.
     */
    double getHeight();

    /**
     * Returns the current bounds.
     */
    Rect getBounds();

    /**
     * Returns the max X.
     */
    default double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    default double getMaxY()  { return getY() + getHeight(); }

    /**
     * Paint text to given painter.
     */
    default void paint(Painter aPntr)
    {
        // Just return if no lines
        int lineCount = getLineCount();
        if (lineCount == 0)
            return;

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
            double lineY = getAlignedY() + textLine.getBaseline();
            paintLine(aPntr, textLine, lineY);
        }

        // Paint underlines
//        if (isUnderlined())
//            TextModelUtils.paintTextModelUnderlines(aPntr, this, textClipBounds);

        // Restore state
        aPntr.restore();
    }

    /**
     * Paint text line to given painter.
     */
    default void paintLine(Painter aPntr, TextLine textLine, double lineY)
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
    double getPrefWidth();

    /**
     * Returns the width of text from given start char index.
     */
    default double getPrefWidthForStartCharIndex(int startCharIndex)
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
    default double getPrefHeight()
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
    double getPrefHeight(double aW);
}
