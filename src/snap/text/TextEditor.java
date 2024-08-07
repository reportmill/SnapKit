/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;

/**
 * This class provides the event and drawing code necessary to edit text in a TextDoc.
 */
public class TextEditor extends TextArea {

    /**
     * Constructor.
     */
    public TextEditor()
    {
        super();
    }

    /**
     * Returns the character spacing of the current selection or cursor.
     */
    public double getCharSpacing()  { return getSelStyle().getCharSpacing(); }

    /**
     * Returns the character spacing of the current selection or cursor.
     */
    public void setCharSpacing(double aValue)
    {
        setSelStyleValue(TextStyle.CharSpacing_Prop, aValue);
    }

    /**
     * Returns the line spacing for current selection.
     */
    public double getLineSpacing()  { return getSelLineStyle().getSpacingFactor(); }

    /**
     * Sets the line spacing for current selection.
     */
    public void setLineSpacing(double aHeight)
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
    public void setLineHeightMin(double aHeight)
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
    public void setLineHeightMax(double aHeight)
    {
        setSelLineStyleValue(TextLineStyle.MIN_HEIGHT_KEY, aHeight);
    }

    /**
     * Sets whether TextEditor is showing and focused.
     */
    public void setActive(boolean aValue)
    {
        setShowing(aValue);
        setFocused(aValue);
    }

    /**
     * Paints a given TextEditor.
     */
    public void paintActiveText(Painter aPntr)
    {
        // Get selection path
        Shape path = getSelPath();

        // If empty selection, draw caret
        if(isSelEmpty() && path != null) {
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
        if (isSpellChecking() && length() > 0) {

            // Get spelling path
            Shape spellingPath = SpellCheck.getSpellingPath(getTextBlock(), getSelStart());

            // Paint spelling path
            aPntr.setColor(Color.RED);
            aPntr.setStroke(Stroke.StrokeDash1);
            aPntr.draw(spellingPath);
            aPntr.setColor(Color.BLACK);
            aPntr.setStroke(Stroke.Stroke1);
        }

        // Paint TextBox
        TextBlock textBlock = getTextBlock();
        textBlock.paint(aPntr);
    }
}