/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.gfx.Painter;
import snap.view.ViewUtils;

/**
 * This class holds a String and TextStyle and provides painting and sizing information.
 */
public class StyledString implements Cloneable {

    // The text to be measured
    private String  _string;

    // The TextStyle
    private TextStyle  _style = TextStyle.DEFAULT;

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
    public TextStyle getStyle()  { return _style; }

    /**
     * Sets the TextStyle.
     */
    public void setStyle(TextStyle aStyle)
    {
        _style = aStyle;
        _ascent = -1;
    }

    /**
     * Returns the font.
     */
    public Font getFont()
    {
        return _style.getFont();
    }

    /**
     * Sets the font.
     */
    public void setFont(Font aFont)
    {
        TextStyle textStyle = _style.copyFor(aFont);
        setStyle(textStyle);
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()
    {
        Color color = _style.getColor();
        Color defColor = (Color) ViewUtils.getTextFill();
        if (defColor != Color.BLACK && color == Color.BLACK)
            color = defColor;
        return color;
    }

    /**
     * Sets the text fill.
     */
    public void setTextFill(Paint aPaint)
    {
        TextStyle textStyle = _style.copyFor(aPaint);
        setStyle(textStyle);
    }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()  { return _style.isUnderlined(); }

    /**
     * Sets whether this run is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        TextStyle textStyle = _style.copyFor(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0);
        setStyle(textStyle);
    }

    /**
     * Returns the run's scripting.
     */
    public int getScripting()  { return _style.getScripting(); }

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
        Rect bnds = text != null && text.length() > 0 ? font.getGlyphBounds(text) : Rect.ZeroRect;

        // Get StringWidth from GlyphBounds
        _textWidth = Math.ceil(bnds.width);

        // Get Ascent, Descent, LineHeight from GlyphBounds
        _ascent = Math.ceil(-bnds.y);
        _descent = Math.ceil(bnds.height + bnds.y);
        _textHeight = _ascent + _descent;
    }

    /**
     * Loads the metrics.
     */
    private void loadMetricsForFontSizing()
    {
        // Get StringWidth for string + font (aka Advance)
        String text = getString();
        Font font = getFont();
        _textWidth = text != null ? Math.ceil(font.getStringAdvance(text)) : 0;

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
        // Set font and text fill
        Font font = getFont();
        Paint textFill = getTextFill();
        aPntr.setFont(font);
        aPntr.setPaint(textFill);

        // Get String X/Y and paint
        String text = getString();
        aPntr.drawString(text, aX, aY);
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
