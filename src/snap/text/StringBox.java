package snap.text;
import snap.geom.*;
import snap.gfx.*;
import java.util.Objects;

/**
 * This class manages a string and a bounding rect.
 */
public class StringBox extends RoundRect {

    // The String
    private String  _string;

    // The TextStyle for characters in this run
    protected TextStyle  _style = TextStyle.DEFAULT;

    // The border
    private Border  _border;

    // The padding
    private Insets  _padding = Insets.EMPTY;

    // The alignment
    private Pos  _align = Pos.TOP_LEFT;

    // Whether to size to font instead of glyphs
    private boolean  _fontSizing;

    // The width/advance of glyphs for current string/font
    private double _strWidth;

    // The height of glyphs for current string/font
    private double _strHeight;

    // The ascent, descent for current string/font
    private double  _ascent, _descent;

    // Whether box needs to be sized
    private boolean  _needsResize = true;

    // The version of this box with font such that string fits
    private StringBox  _boxThatFits;

    /**
     * Constructor.
     */
    public StringBox()
    {
        super();
    }

    /**
     * Constructor.
     */
    public StringBox(String aString)
    {
        super();
        setString(aString);
    }

    /**
     * Returns the string.
     */
    public String getString()  { return _string; }

    /**
     * Returns the length of the string.
     */
    public int length()  { return _string!=null ? _string.length() : 0; }

    /**
     * Sets the string.
     */
    public void setString(String aString)
    {
        _string = aString;
        _needsResize = true;
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
        _needsResize = true;
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
        setStyle(_style.copyFor(aFont));
    }

    /**
     * Returns the color for string text.
     */
    public Color getTextColor()  { return _style.getColor(); }

    /**
     * Sets the color for string text.
     */
    public void setTextColor(Color aColor)
    {
        if (Objects.equals(aColor, getTextColor())) return;
        setStyle(_style.copyFor(aColor));
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
        setStyle(_style.copyFor(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0));
    }

    /**
     * Returns the run's scripting.
     */
    public int getScripting()  { return _style.getScripting(); }

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
        _needsResize = true;
    }

    /**
     * Sets the border for given color and stroke width.
     */
    public void setBorder(Color aColor, double aWidth)
    {
        setBorder(Border.createLineBorder(aColor, aWidth));
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
        _needsResize = true;
    }

    /**
     * Sets the padding to given insets.
     */
    public void setPadding(double aTop, double aRgt, double aBtm, double aLft)
    {
        setPadding(new Insets(aTop, aRgt, aBtm, aLft));
    }

    /**
     * Returns how the string is positioned in the box if larger than String width/height (default is Top-Left).
     */
    public Pos getAlign()  { return _align; }

    /**
     * Sets how the string is positioned in the box if larger than String width/height (default is Top-Left).
     */
    public void setAlign(Pos aPos)
    {
        _align = aPos;
    }

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
    }

    /**
     * Returns whether this run has a hyphen at the end.
     */
    public boolean isHyphenated()  { return getString().endsWith("-"); }

    /**
     * Sets whether this run has a hyphen at the end.
     */
    public void setHyphenated(boolean aFlag)
    {
        if (aFlag==isHyphenated()) return;
        String str = getString();
        String str2 = aFlag ? (str + '-') : (str.substring(0, str.length()-1));
        setString(str2);
    }

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
        double sboxW = getStringWidth() + ins.getWidth();
        double sboxH = getStringHeight() + ins.getHeight();
        setSize(sboxW, sboxH);
    }

    /**
     * Returns the width of the string (aka the 'advance').
     */
    public double getStringWidth()
    {
        if (_ascent<0) loadMetrics();
        return _strWidth;
    }

    /**
     * Returns the height of the current string/font (aka the 'line height').
     */
    public double getStringHeight()
    {
        if (_ascent<0) loadMetrics();
        return _strHeight;
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
     * Returns the preferred width of box.
     */
    public double getPrefWidth()
    {
        Insets ins = getInsetsAll();
        double strW = getStringWidth();
        return strW + ins.getWidth();
    }

    /**
     * Returns the preferred height of box.
     */
    public double getPrefHeight()
    {
        Insets ins = getInsetsAll();
        double strH = getStringHeight();
        return strH + ins.getHeight();
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
        String str = getString();
        Rect bnds = str!=null && str.length()>0 ? font.getGlyphBounds(str) : Rect.ZeroRect;

        // Get StringWidth from GlyphBounds
        _strWidth = Math.ceil(bnds.width);

        // Get Ascent, Descent, LineHeight from GlyphBounds
        _ascent = Math.round(-bnds.y);
        _descent = Math.round(bnds.height + bnds.y);
        _strHeight = _ascent + _descent;
    }

    /** Was using this. */
    /*private Rect getGlyphBoundsRound() {
        Rect rect = getFont().getGlyphBounds(getString());
        rect.y = Math.round(rect.y); rect.height = Math.round(rect.height);
        return rect;
    }*/

    /**
     * Loads the metrics.
     */
    private void loadMetricsForFontSizing()
    {
        // Get StringWidth for string + font (aka Advance)
        String str = getString();
        Font font = getFont();
        _strWidth = Math.ceil(font.getStringAdvance(str));

        // Get Font Ascent, Descent, StringHeight (aka LineHeight)
        _ascent = Math.ceil(font.getAscent());
        _descent = Math.ceil(font.getDescent());
        _strHeight = _ascent + _descent;
    }

    /**
     * Returns the string X point.
     */
    public double getStringX()
    {
        // Get String X
        Insets ins = getInsetsAll();
        double strX = getX() + ins.left;

        // If alignment is not Left, adjust
        HPos alignX = getAlign().getHPos();
        if (alignX != HPos.LEFT) {
            double areaW = getWidth() - ins.getWidth();
            double strW = getStringWidth();
            if (areaW > strW) {
                double dx = Math.round((areaW - strW) * alignX.doubleValue());
                strX += dx;
            }
        }

        // Return String X
        return strX;
    }

    /**
     * Returns the string Y point.
     */
    public double getStringY()
    {
        // Get String Y
        Insets ins = getInsetsAll();
        double ascent = getAscent();
        double strY = getY() + ins.top + ascent;

        // If alignment is not Top, adjust
        VPos alignY = getAlign().getVPos();
        if (alignY != VPos.TOP) {
            double areaH = getHeight() - ins.getHeight();
            double strH = getStringHeight();
            if (areaH > strH) {
                double dx = Math.round((areaH - strH) * alignY.doubleValue());
                strY += dx;
            }
        }

        // Return String Y
        return strY;
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
        // If no string, just bail
        if (length() == 0) return;

        // If NeverBeenSized, sizeToFit
        if (_needsResize) resize();

        // Get info
        String str = getString();
        double strX = getStringX();
        double strY = getStringY();
        Font font = getFont();
        Color color = getTextColor();
        double cspace = getStyle().getCharSpacing();

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
        if (_needsResize) resize();

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
        // If Border, paint border
        Border border = getBorder();
        if (border != null)
            border.paint(aPntr, this);

        // Paint string
        drawString(aPntr);
    }

    /**
     * Override to mark sized.
     */
    @Override
    public void setWidth(double aValue)
    {
        super.setWidth(aValue);
        _needsResize = false;
    }

    /**
     * Override to size.
     */
    @Override
    public void setCenteredXY(double aX, double aY)
    {
        if (_needsResize) resize();
        super.setCenteredXY(aX, aY);
    }

    /**
     * Returns whether text fits in box.
     */
    public boolean isTextFits()
    {
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        double strW = getStringWidth();
        double strH = getStringHeight();
        return strW <= areaW && strH <= areaH;
    }

    /**
     * Sets the font so that it fits.
     */
    public void setFontToFit()
    {
        // If text fits, just return
        if (isTextFits()) return;

        // Get Text area available
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Declare dampening variables
        Font font = getFont();
        double fontSize = font.getSize();
        double scaleLow = 0;
        double scaleHigh = 1;

        // Loop while dampening variables are normal
        while (true) {

            // Reset fontScale to mid-point of fsHi and fsLo
            double fontScale = (scaleLow + scaleHigh)/2;
            double fontSize2 = fontSize * fontScale;
            setFont(font.deriveFont(fontSize2));

            double strW = getStringWidth();
            double strH = getStringHeight();
            boolean textFits = strW <= areaW && strH <= areaH;

            // If text exceeded layout bounds, reset fsHi to fontScale
            if (!textFits) {
                scaleHigh = fontScale;
                if ((scaleHigh + scaleLow)/2 == 0) {
                    System.err.println("StringBox.setFontToFit: Couldn't fit text in box at any size (wft)"); break; }
            }

            // If text didn't exceed layout bounds, reset scaleLow to fontScale
            else {

                // Set new low (if almost fsHi, just return)
                scaleLow = fontScale;
                double detaFS = scaleHigh - scaleLow;
                if (detaFS<.05)
                    break;

                // If almost fit width, stop
                double diffW = areaW - strW;
                if (diffW < 1)
                    break;

                // If almost fit height, stop
                double diffH = areaH - strH;
                if (diffH<1)
                    break;
            }
        }
    }

    /**
     * Returns a version of this box with font such that text fits.
     */
    public StringBox getBoxThatFits()
    {
        // If already set, just return
        if (_boxThatFits!=null) return _boxThatFits;

        // If TextFits, just set/return this
        if (isTextFits())
            return _boxThatFits = this;

        // Clone box, setFontToFit() and set/return
        StringBox clone = clone();
        clone.setFontToFit();
        return _boxThatFits = clone;
    }

    /**
     * Standard clone implementation.
     */
    public StringBox clone()
    {
        return (StringBox) super.clone();
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String cname = getClass().getSimpleName();
        return cname + " { String='" + _string + '\'' + ", Style=" + _style + ", Rect=[" + super.getString() + ']' +
            ", Padding=" + _padding + ", Border=" + _border +
            ", Ascent=" + getAscent() + ", Descent=" + getDescent() + ", LineHeight=" + _strHeight +
            ", Advance=" + _strWidth + " }";
    }

    /**
     * Returns a StringBox for string and attributes.
     */
    public static StringBox getForStringAndAttributes(String aStr, Object ... theAttrs)
    {
        StringBox sbox = new StringBox(aStr);
        TextStyle textStyle = sbox.getStyle().copyFor(theAttrs);
        sbox.setStyle(textStyle);
        return sbox;
    }
}
