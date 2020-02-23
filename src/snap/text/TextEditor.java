/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
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
}