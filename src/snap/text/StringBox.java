package snap.text;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.gfx.*;

/**
 * This class manages a string and a bounding rect.
 */
public class StringBox extends RoundRect {

    // The String
    private String  _string;

    // The TextStyle for characters in this run
    private TextStyle  _style = TextStyle.DEFAULT;

    // The padding
    private Insets  _padding = Insets.EMPTY;

    // The border
    private Border  _border;

    // Whether to size to font instead of glyphs
    private boolean  _fontSizing;

    // The ascent, descent for current string/font
    private double  _ascent, _descent;

    // The height of glyphs for current string/font
    private double  _glyphHeight;

    // The advance of glyphs for current string/font
    private double  _advance;

    // Whether box has ever been sized
    private boolean  _neverBeenSized = true;

    /**
     * Constructor.
     */
    public StringBox(String aString)
    {
        setString(aString);
    }

    /**
     * Returns the string.
     */
    public String getString()  { return _string; }

    /**
     * Sets the string.
     */
    public void setString(String aString)
    {
        _string = aString;
        _ascent = -1;
    }

    /**
     * Returns the font.
     */
    public Font getFont()  { return _style.getFont(); }

    /**
     * Sets the font.
     */
    public void setFont(Font aFont)
    {
        setTextStyle(_style.copyFor(aFont));
    }

    /**
     * Returns the TextStyle.
     */
    public TextStyle getTextStyle()  { return _style; }

    /**
     * Sets the TextStyle.
     */
    public void setTextStyle(TextStyle aStyle)
    {
        _style = aStyle;
        _ascent = -1;
    }

    /**
     * Returns the padding.
     */
    public Insets getPadding()  { return _padding; }

    /**
     * Sets the padding.
     */
    public void setPadding(Insets theInsets)
    {
        _padding = theInsets;
    }

    /**
     * Sets the padding to given insets.
     */
    public void setPadding(double aTop, double aRgt, double aBtm, double aLft)
    {
        setPadding(new Insets(aTop, aRgt, aBtm, aLft));
    }

    /**
     * Returns the border.
     */
    public Border getBorder()  { return _border; }

    /**
     * Sets the border.
     */
    public void setBorder(Border aBorder)
    {
        _border = aBorder;
    }

    /**
     * Sets the border for given color and stroke width.
     */
    public void setBorder(Color aColor, double aWidth)
    {
        setBorder(Border.createLineBorder(aColor, aWidth));
    }

    /**
     * Returns whether to size to font (looser) instead of glyphs (tighter).
     */
    public boolean isFontSizing()  { return _fontSizing; }

    /**
     * Sets whether to size to font (looser) instead of glyphs (tighter).
     */
    public void setFontSizing(boolean aValue)  { _fontSizing = aValue; }

    /**
     * Returns the total insets due to border and/or padding.
     */
    public Insets getInsetsAll()
    {
        Insets pad = getPadding();
        Border border = getBorder();
        if (border==null) return pad;
        Insets borderIns = border.getInsets();
        return Insets.add(pad, borderIns);
    }

    /**
     * Resizes bounds to fit string.
     */
    public void resize()
    {
        Insets ins = getInsetsAll();
        double sboxW = getAdvance() + ins.getWidth();
        double sboxH = getGlyphHeight() + ins.getHeight();
        setSize(sboxW, sboxH);
    }

    /**
     * Returns the ascent.
     */
    public double getAscent()
    {
        if (_ascent<0) loadMetrics();
        return _ascent;
    }

    /**
     * Returns the descent.
     */
    public double getDescent()
    {
        if (_ascent<0) loadMetrics();
        return _descent;
    }

    /**
     * Returns the advance.
     */
    public double getAdvance()
    {
        if (_ascent<0) loadMetrics();
        return _advance;
    }

    /**
     * Returns the height of the current string/font.
     */
    public double getGlyphHeight()
    {
        if (_ascent<0) loadMetrics();
        return _glyphHeight;
    }

    /**
     * Returns the string X point.
     */
    public double getStringX()
    {
        Insets ins = getInsetsAll();
        return getX() + ins.left;
    }

    /**
     * Returns the string Y point.
     */
    public double getStringY()
    {
        Insets ins = getInsetsAll();
        double ascent = getAscent();
        return getY() + ins.top + ascent;
    }

    /**
     * Sets the string X/Y.
     */
    public void setStringXY(double aX, double aY)
    {
        Insets ins = getInsetsAll();
        double ascent = getAscent();
        double newX = aX - ins.left;
        double newY = aY - ins.top - ascent;
        setXY(newX, newY);
    }

    /**
     * Draws the string.
     */
    public void drawString(Painter aPntr)
    {
        // If NeverBeenSized, sizeToFit
        if (_neverBeenSized) resize();

        // Get info
        String str = getString();
        double strX = getStringX();
        double strY = getStringY();
        Font font = getFont();
        Color color = getTextStyle().getColor();
        double cspace = getTextStyle().getCharSpacing();

        // Set style and draw string
        aPntr.setFont(font);
        aPntr.setColor(color);
        aPntr.drawString(str, strX, strY, cspace);
    }

    /**
     * Draws the string.
     */
    public void drawRect(Painter aPntr)
    {
        // If NeverBeenSized, sizeToFit
        if (_neverBeenSized) resize();

        // Get info
        Border border = getBorder();
        Stroke stroke = border!=null ? border.getStroke() : aPntr.getStroke();
        double strokeWidth = stroke.getWidth();
        double strokeInset = strokeWidth/2;

        // Set color
        if (border!=null) {
            aPntr.setColor(border.getColor());
            aPntr.setStroke(stroke);
        }

        // Draw rect
        double sboxX = x + strokeInset;
        double sboxY = y + strokeInset;
        double sboxW = width - strokeWidth;
        double sboxH = height - strokeWidth;
        aPntr.drawRect(sboxX, sboxY, sboxW, sboxH);
    }

    /**
     * Paints the string box.
     */
    public void paint(Painter aPntr)
    {
        drawRect(aPntr);
        drawString(aPntr);
    }

    /**
     * Loads the metrics.
     */
    private void loadMetrics()
    {
        Font font = getFont();
        String str = getString();
        Rect bnds = font.getGlyphBoundsRound(str);
        _ascent = Math.round(-bnds.y);
        _descent = Math.round(bnds.height + bnds.y);
        _glyphHeight = Math.round(bnds.height);
        _advance = bnds.width;
    }

    /**
     * Override to mark sized.
     */
    @Override
    public void setWidth(double aValue)
    {
        super.setWidth(aValue);
        _neverBeenSized = false;
    }

    /**
     * Override to size.
     */
    @Override
    public void setCenteredXY(double aX, double aY)
    {
        if (_neverBeenSized) resize();
        super.setCenteredXY(aX, aY);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "StringBox { String='" + _string + '\'' + ", Style=" + _style + ", Rect=[" + super.getString() + ']' +
            ", Padding=" + _padding + ", Border=" + _border +
            ", Ascent=" + getAscent() + ", Descent=" + getDescent() + ", GlyphHeight=" + _glyphHeight +
            ", Advance=" + _advance + " }";
    }

    /**
     * Returns a StringBox for string and attributes.
     */
    public static StringBox getForStringAndAttributes(String aStr, Object ... theAttrs)
    {
        StringBox sbox = new StringBox(aStr);
        TextStyle textStyle = sbox.getTextStyle().copyFor(theAttrs);
        sbox.setTextStyle(textStyle);
        return sbox;
    }
}
