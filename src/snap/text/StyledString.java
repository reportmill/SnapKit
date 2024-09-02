/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.ViewUtils;

/**
 * This class holds a String and TextStyle and provides painting and sizing information.
 */
public class StyledString implements Cloneable {

    // The text to be measured
    private String  _string;

    // The TextStyle
    private TextStyle _textStyle = TextStyle.DEFAULT;

    // Whether to size to font instead of glyphs
    protected boolean  _fontSizing;

    // The width/advance of glyphs for current string/font
    private double  _textWidth;

    // The height of glyphs for current string/font
    private double  _textHeight;

    // The ascent, descent for current string/font
    private double  _ascent, _descent;

    /**
     * Constructor.
     */
    public StyledString()  { }

    /**
     * Constructor.
     */
    public StyledString(String aString)
    {
        this();
        setString(aString);
    }

    /**
     * Returns the string.
     */
    public String getString()  { return _string; }

    /**
     * Sets the string.
     */
    public void setString(String aValue)
    {
        _string = aValue;
        _ascent = -1;
    }

    /**
     * Returns the TextStyle.
     */
    public TextStyle getTextStyle()  { return _textStyle; }

    /**
     * Sets the TextStyle.
     */
    public void setTextStyle(TextStyle aStyle)
    {
        _textStyle = aStyle;
        _ascent = -1;
    }

    /**
     * Returns the font.
     */
    public Font getFont()
    {
        return _textStyle.getFont();
    }

    /**
     * Sets the font.
     */
    public void setFont(Font aFont)
    {
        TextStyle textStyle = _textStyle.copyFor(aFont);
        setTextStyle(textStyle);
    }

    /**
     * Returns the text color.
     */
    public Color getTextColor()
    {
        Color color = _textStyle.getColor();
        Color defColor = ViewUtils.getTextColor();
        if (defColor != Color.BLACK && color == Color.BLACK)
            color = defColor;
        return color;
    }

    /**
     * Sets the text color.
     */
    public void setTextColor(Color aColor)
    {
        TextStyle textStyle = _textStyle.copyFor(aColor);
        setTextStyle(textStyle);
    }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()  { return _textStyle.isUnderlined(); }

    /**
     * Sets whether this run is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        TextStyle textStyle = _textStyle.copyFor(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0);
        setTextStyle(textStyle);
    }

    /**
     * Returns the run's scripting.
     */
    public int getScripting()  { return _textStyle.getScripting(); }

    /**
     * Returns whether to size to font (looser) instead of glyphs (tighter).
     */
    public boolean isFontSizing()  { return _fontSizing; }

    /**
     * Sets whether to size to font (looser) instead of glyphs (tighter).
     */
    public void setFontSizing(boolean aValue)
    {
        _fontSizing = aValue;
        _ascent = -1;
    }

    /**
     * Returns the text length.
     */
    public int length()  { return _string != null ? _string.length() : 0; }

    /**
     * Returns the width of the string (aka the 'advance').
     */
    public double getTextWidth()
    {
        if (_ascent < 0) loadMetrics();
        return _textWidth;
    }

    /**
     * Returns the height of the current string/font (aka the 'line height').
     */
    public double getTextHeight()
    {
        if (_ascent < 0) loadMetrics();
        return _textHeight;
    }

    /**
     * Returns the ascent.
     */
    public double getAscent()
    {
        if (_ascent < 0) loadMetrics();
        return _ascent;
    }

    /**
     * Returns the descent.
     */
    public double getDescent()
    {
        if (_ascent < 0) loadMetrics();
        return _descent;
    }

    /**
     * Loads the metrics.
     */
    private void loadMetrics()
    {
        if (isFontSizing())
            loadMetricsForFontSizing();
        else loadMetricsForGlyphSizing();
    }

    /**
     * Loads the metrics for default 'Glyph sizing', where text box is snug around glyphs for string+font.
     */
    private void loadMetricsForGlyphSizing()
    {
        // Get exact bounds around string glyphs for font
        Font font = getFont();
        String text = getString();
        Rect textBounds = text != null && !text.isEmpty() ? font.getGlyphBounds(text) : Rect.ZeroRect;

        // Get text width from text bounds
        _textWidth = Math.ceil(textBounds.width);

        // Get Ascent, Descent from text bounds
        _ascent = Math.ceil(-textBounds.y);
        _descent = Math.ceil(textBounds.height + textBounds.y);
        if (_textStyle.isUnderlined())
            _descent = Math.max(_descent, Math.ceil(Math.abs(font.getUnderlineOffset()) + font.getUnderlineThickness()));

        // Get text height from text bounds
        _textHeight = _ascent + _descent;
    }

    /**
     * Loads the metrics.
     */
    private void loadMetricsForFontSizing()
    {
        // Get StringWidth for string + font (aka Advance)
        String text = getString(); if (text == null) text = "";
        Font font = getFont();
        _textWidth = Math.ceil(font.getStringAdvance(text));

        // Add char spacing
        double charSpacing = _textStyle.getCharSpacing();
        if (charSpacing != 0 && text.length() > 1)
            _textWidth += charSpacing * (text.length() - 1);

        // Get Font Ascent, Descent, StringHeight (aka LineHeight)
        _ascent = Math.ceil(font.getAscent());
        _descent = Math.ceil(font.getDescent());
        _textHeight = _ascent + _descent;
    }

    /**
     * Returns the char index for the X location.
     */
    public int getCharIndexForX(double aX)
    {
        String text = getString();
        Font font = getFont();
        double textX = 0;
        for (int i = 0, iMax = text.length(); i < iMax; i++) {
            char c = text.charAt(i);
            double dx = font.charAdvance(c);
            if (aX <= textX + dx / 2)
                return i;
            textX += dx;
        }
        return text.length();
    }

    /**
     * Paints StringView.
     */
    public void paintString(Painter aPntr, double aX, double aY)
    {
        // Set font and text color
        Font font = getFont();
        aPntr.setFont(font);
        aPntr.setPaint(getTextColor());

        // Get String X/Y and paint
        String text = getString();
        double charSpacing = _textStyle.getCharSpacing();
        aPntr.drawString(text, aX, aY, charSpacing);

        // Handle Underline
        if (_textStyle.isUnderlined()) {
            double underlineThickness = font.getUnderlineThickness();
            aPntr.setStrokeWidth(underlineThickness);
            double lineMaxX = aX + font.getStringAdvance(text);
            double lineY = aY + Math.ceil(Math.abs(font.getUnderlineOffset()));
            aPntr.drawLine(aX, lineY, lineMaxX, lineY);
        }

        // Handle TextBorder: Get outline and stroke
        Border border = _textStyle.getBorder();
        if (border != null) {
            aPntr.setPaint(border.getColor());
            aPntr.setStroke(border.getStroke());
            aPntr.strokeString(text, aX, aY, charSpacing);
        }
    }

    /**
     * Paints StringView with upper left XY.
     */
    public void paintStringTopLeft(Painter aPntr, double aX, double aY)
    {
        paintString(aPntr, aX, aY + getAscent());
    }

    /**
     * Returns a copy of this TextRun.
     */
    public StyledString clone()
    {
        StyledString copy;
        try { copy = (StyledString) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        return copy;
    }
}
