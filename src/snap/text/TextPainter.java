package snap.text;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;

/**
 * This class paints a given text layout.
 */
public class TextPainter {

    // The Selection color
    private static Color TEXT_SEL_COLOR = new Color(181, 214, 254, 255);

    // The default text painter
    public static final TextPainter DEFAULT = new TextPainter();

    /**
     * Constructor.
     */
    public TextPainter()
    {
        super();
    }

    /**
     * Paints the given text.
     */
    public void paintText(Painter aPntr, TextLayout textLayout)
    {
        // Just return if no lines
        int lineCount = textLayout.getLineCount();
        if (lineCount == 0)
            return;

        // Optimize for single line
        if (lineCount == 1) {
            TextLine textLine = textLayout.getLine(0);
            if (!textLine.isBlank()) {
                aPntr.save();
                aPntr.clipRect(textLine.getTextX(), textLine.getTextY(), textLine.getWidth(), textLine.getHeight());
                paintLine(aPntr, textLayout, textLine);
                aPntr.restore();
            }
            return;
        }

        // Get text clip bounds and clip
        Rect textBounds = textLayout.getBounds();
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
            TextLine textLine = textLayout.getLine(i);
            if (textLine.getTextMaxY() < textClipBounds.y)
                continue;

            // If line no longer visible, break
            if (textLine.getTextY() >= textClipBounds.getMaxY())
                break;

            // Paint line
            if (!textLine.isBlank())
                paintLine(aPntr, textLayout, textLine);
        }

        // Restore state
        aPntr.restore();
    }

    /**
     * Paint text line with given painter.
     */
    public void paintLine(Painter aPntr, TextLayout textLayout, TextLine textLine)
    {
        TextToken[] lineTokens = textLine.getTokens();
        double lineY = textLine.getBaseline() + textLayout.getAlignedY();

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

        // If underlined, paint underlines
        if (textLine.isUnderlined())
            paintUnderlines(aPntr, textLine);
    }

    /**
     * Paints text line underlines with given painter.
     */
    private void paintUnderlines(Painter aPntr, TextLine textLine)
    {
        for (TextRun run : textLine.getRuns()) {
            if (!run.isUnderlined() || run.isEmpty())
                continue;

            // Set underline color and width
            Font font = run.getFont();
            double underlineOffset = Math.ceil(Math.abs(font.getUnderlineOffset()));
            double underlineThickness = font.getUnderlineThickness();
            aPntr.setColor(run.getColor());
            aPntr.setStrokeWidth(underlineThickness);

            // Get underline endpoints and draw line
            double lineX = textLine.getTextX() + run.getX();
            double lineMaxX = lineX + run.getWidth() - run.getTrailingWhitespaceWidth();
            double lineY = textLine.getTextBaseline() + underlineOffset;
            aPntr.drawLine(lineX, lineY, lineMaxX, lineY);
        }
    }

    /**
     * Paint selection and text.
     */
    public void paintTextAdapter(Painter aPntr, TextAdapter textAdapter)
    {
        // Paint selection
        paintTextSel(aPntr, textAdapter.getSel(), textAdapter);

        // Paint spell check
        if (textAdapter.isSpellChecking() && textAdapter.length() > 0)
            paintSpellCheck(aPntr, textAdapter);

        // Paint TextModel
        TextLayout textLayout = textAdapter.getTextLayout();
        TextPainter.DEFAULT.paintText(aPntr, textLayout);
    }

    /**
     * Paints the selection.
     */
    public void paintTextSel(Painter aPntr, TextSel textSel, TextAdapter textAdapter)
    {
        // If not editable, just return
        if (!textAdapter.isEditable()) return;

        // Get selection path
        Shape selPath = textSel.getPath();

        // If empty selection, paint carat
        if (textSel.isEmpty()) {
            if (textAdapter.isShowCaret()) {
                aPntr.setPaint(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1);
                aPntr.draw(selPath);
            }
        }

        // Otherwise
        else {
            aPntr.setPaint(TEXT_SEL_COLOR);
            aPntr.fill(selPath);
        }
    }

    /**
     * Paints spell check.
     */
    private void paintSpellCheck(Painter aPntr, TextAdapter textAdapter)
    {
        // Get spelling path
        TextLayout textLayout = textAdapter.getTextLayout();
        Shape spellingPath = getSpellingPath(textLayout, textAdapter.getSelStart());

        // Paint spelling path
        aPntr.setColor(Color.RED);
        aPntr.setStroke(Stroke.StrokeDash1);
        aPntr.draw(spellingPath);
        aPntr.setColor(Color.BLACK);
        aPntr.setStroke(Stroke.Stroke1);
    }

    /**
     * Returns a path for misspelled word underlining.
     */
    public static Shape getSpellingPath(TextLayout textModel, int selStart)
    {
        // Get text string and path object
        String string = textModel.getString();
        Path2D spellingPath = new Path2D();

        // Iterate over text
        for (SpellCheck.Word word = SpellCheck.getMisspelledWord(string, 0); word != null;
             word = SpellCheck.getMisspelledWord(string, word.getEnd())) {

            // Get word bounds
            int wordStart = word.getStart();
            if (wordStart >= textModel.getEndCharIndex())
                break;
            int wordEnd = word.getEnd();
            if (wordEnd > textModel.getEndCharIndex())
                wordEnd = textModel.getEndCharIndex();

            // If text editor selection starts in word bounds, just continue - they are still working on this word
            if (wordStart <= selStart && selStart <= wordEnd)
                continue;

            // Get the selection's start line index and end line index
            int startLineIndex = textModel.getLineForCharIndex(wordStart).getLineIndex();
            int endLineIndex = textModel.getLineForCharIndex(wordEnd).getLineIndex();

            // Iterate over selected lines
            for (int i = startLineIndex; i <= endLineIndex; i++) {
                TextLine textLine = textModel.getLine(i);

                // Get the bounds of line
                double lineX = textLine.getTextX();
                double lineMaxX = textLine.getTextMaxX();
                double lineBaseY = textLine.getTextBaseline() + 3;

                // If starting line, adjust x1 for starting character
                if (i == startLineIndex)
                    lineX = textLine.getTextXForCharIndex(wordStart - textLine.getStartCharIndex());

                // If ending line, adjust x2 for ending character
                if (i == endLineIndex)
                    lineMaxX = textLine.getTextXForCharIndex(wordEnd - textLine.getStartCharIndex());

                // Append rect for line to path
                spellingPath.moveTo(lineX, lineBaseY);
                spellingPath.lineTo(lineMaxX, lineBaseY);
            }
        }

        // Return
        return spellingPath;
    }
}
