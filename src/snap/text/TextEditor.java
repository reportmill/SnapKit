/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Path;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;

/**
 * This class provides all of the event and drawing code necessary to edit text in the form of a RichText.
 * (separated from an actual UI View).
 *
 * Current this just subclasses TextArea. But if there's a problem referencing View package, this class could instead
 * use all the code from TextArea, and have TextArea encapsulate one of these instead.
 */
public class TextEditor extends TextArea {

    /**
     * Sets whether TextEditor is showing and focused.
     */
    public void setActive(boolean aValue)
    {
        setShowing(aValue);
        setFocused(aValue);
    }
    /**
     * Returns the character spacing of the current selection or cursor.
     */
    public float getCharSpacing()  { return (float)getSelStyle().getCharSpacing(); }

    /**
     * Returns the character spacing of the current selection or cursor.
     */
    public void setCharSpacing(float aValue)
    {
        setSelStyleValue(TextStyle.CHAR_SPACING_KEY, aValue);
    }

    /**
     * Returns the line spacing for current selection.
     */
    public double getLineSpacing()  { return getSelLineStyle().getSpacingFactor(); }

    /**
     * Sets the line spacing for current selection.
     */
    public void setLineSpacing(float aHeight)
    {
        setSelLineStyleValue(TextLineStyle.SPACING_FACTOR_KEY, aHeight);
    }

    /**
     * Returns the line gap for current selection.
     */
    public double getLineGap()  { return getSelLineStyle().getSpacing(); }

    /**
     * Sets the line gap for current selection.
     */
    public void setLineGap(double aHeight)
    {
        setSelLineStyleValue(TextLineStyle.SPACING_KEY, aHeight);
    }

    /**
     * Returns the min line height for current selection.
     */
    public double getLineHeightMin()  { return getSelLineStyle().getMinHeight(); }

    /**
     * Sets the min line height for current selection.
     */
    public void setLineHeightMin(float aHeight)
    {
        setSelLineStyleValue(TextLineStyle.MIN_HEIGHT_KEY, aHeight);
    }

    /**
     * Returns the maximum line height for a line of text (even if font size would dictate higher).
     */
    public double getLineHeightMax()  { return getSelLineStyle().getMaxHeight(); }

    /**
     * Sets the maximum line height for a line of text (even if font size would dictate higher).
     */
    public void setLineHeightMax(float aHeight)
    {
        setSelLineStyleValue(TextLineStyle.MIN_HEIGHT_KEY, aHeight);
    }

    /**
     * Paints a given TextEditor.
     */
    public void paintActiveText(Painter aPntr)
    {
        // Get selection path
        Shape path = getSelPath();

        // If empty selection, draw caret
        if(isSelEmpty() && path!=null) {
            if (isShowCaret()) {
                aPntr.setColor(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1); // Set color and stroke of cursor
                aPntr.setAntialiasing(false);
                aPntr.draw(path);
                aPntr.setAntialiasing(true); // Draw cursor
            }
        }

        // If selection, get selection path and fill
        else {
            aPntr.setColor(new Color(128, 128, 128, 128));
            aPntr.fill(path);
        }

        // If spell checking, get path for misspelled words and draw
        if (isSpellChecking() && length()>0) {
            Shape spath = getSpellingPath();
            if(spath!=null) {
                aPntr.setColor(Color.RED); aPntr.setStroke(Stroke.StrokeDash1);
                aPntr.draw(spath);
                aPntr.setColor(Color.BLACK); aPntr.setStroke(Stroke.Stroke1);
            }
        }

        // Paint TextBox
        getTextBox().paint(aPntr);
    }

    /**
     * Returns a path for misspelled word underlining.
     */
    public Shape getSpellingPath()
    {
        // Get text box and text string and path object
        TextBox textBox = getTextBox();
        String string = textBox.getString();
        Path spellingPath = new Path();

        // Iterate over text
        for (SpellCheck.Word word = SpellCheck.getMisspelledWord(string, 0); word != null;
             word = SpellCheck.getMisspelledWord(string, word.getEnd())) {

            // Get word bounds
            int wordStart = word.getStart();
            if (wordStart >= textBox.getEndCharIndex())
                break;
            int wordEnd = word.getEnd();
            if (wordEnd > textBox.getEndCharIndex())
                wordEnd = textBox.getEndCharIndex();

            // If text editor selection starts in word bounds, just continue - they are still working on this word
            int selStart = getSelStart();
            if (wordStart <= selStart && selStart <= wordEnd)
                continue;

            // Get the selection's start line index and end line index
            int startLineIndex = textBox.getLineForCharIndex(wordStart).getIndex();
            int endLineIndex = textBox.getLineForCharIndex(wordEnd).getIndex();

            // Iterate over selected lines
            for (int i = startLineIndex; i <= endLineIndex; i++) {
                TextBoxLine textBoxLine = textBox.getLine(i);

                // Get the bounds of line
                double lineX = textBoxLine.getX();
                double lineMaxX = textBoxLine.getMaxX();
                double lineBaseY = textBoxLine.getBaseline() + 3;

                // If starting line, adjust x1 for starting character
                if (i == startLineIndex)
                    lineX = textBoxLine.getXForChar(wordStart - textBoxLine.getStartCharIndex() - textBox.getStartCharIndex());

                // If ending line, adjust x2 for ending character
                if (i == endLineIndex)
                    lineMaxX = textBoxLine.getXForChar(wordEnd - textBoxLine.getStartCharIndex() - textBox.getStartCharIndex());

                // Append rect for line to path
                spellingPath.moveTo(lineX, lineBaseY);
                spellingPath.lineTo(lineMaxX, lineBaseY);
            }
        }

        // Return path
        return spellingPath;
    }
}