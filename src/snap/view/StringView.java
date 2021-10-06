/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.Transform;
import snap.gfx.*;
import snap.text.TextStyle;
import snap.util.*;
import java.util.Objects;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View implements Cloneable {

    // Text
    private String  _text;
    
    // The text style
    protected TextStyle  _textStyle = TextStyle.DEFAULT;

    // Whether to size to font instead of glyphs
    protected boolean  _fontSizing;

    // Whether text should shrink to fit
    private boolean  _shrinkToFit;

    // The width/advance of glyphs for current string/font
    private double  _textWidth;

    // The height of glyphs for current string/font
    private double  _textHeight;

    // The ascent, descent for current string/font
    private double  _ascent, _descent;

    // Whether box needs to be sized
    private boolean  _needsResize = true;

    // The version of this StringView with font such that string fits
    private StringView  _copyThatFits;

    // Constants for properties
    //public static final String Text_Prop = "Text";
    public static final String TextStyle_Prop = "TextStyle";
    public static final String FontSizing_Prop = "FontSizing";
    public static final String ShrinkToFit_Prop = "ShrinkToFit";

    // Constants for defaults
    private static final boolean DEFAULT_FONT_SIZING = true;

    /**
     * Constructor.
     */
    public StringView()
    {
        super();

        // Set property defaults
        _fontSizing = DEFAULT_FONT_SIZING;
    }

    /**
     * Constructor with string.
     */
    public StringView(String aString)
    {
        this();
        setText(aString);
    }

    /**
     * Returns the text.
     */
    public String getText()  { return _text; }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        // if already set, just return
        if (SnapUtils.equals(aValue, _text)) return;

        // Set new value, fire prop change, relayout and return
        firePropChange(Text_Prop, _text, _text = aValue);
        relayoutParent();
        repaint();
    }

    /**
     * Returns the TextStyle.
     */
    public TextStyle getStyle()  { return _textStyle; }

    /**
     * Sets the TextStyle.
     */
    public void setStyle(TextStyle aStyle)
    {
        if (Objects.equals(aStyle, _textStyle)) return;
        firePropChange(TextStyle_Prop, _textStyle, _textStyle = aStyle);
        relayoutParent();
        repaint();
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()
    {
        Color color = getStyle().getColor();
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
        if (Objects.equals(aPaint, getTextFill())) return;
        TextStyle textStyle = getStyle().copyFor(aPaint);
        setStyle(textStyle);
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
        if (aValue == _fontSizing) return;
        firePropChange(FontSizing_Prop, _fontSizing, _fontSizing = aValue);
        relayoutParent();
    }

    /**
     * Returns whether text should shrink to fit.
     */
    public boolean isShrinkToFit()  { return _shrinkToFit; }

    /**
     * Sets whether text should shrink to fit.
     */
    public void setShrinkToFit(boolean aValue)
    {
        if (aValue == isShrinkToFit()) return;
        firePropChange(ShrinkToFit_Prop, _shrinkToFit, _shrinkToFit = aValue);
    }

    /**
     * Returns the text length.
     */
    public int length()  { return _text != null ? _text.length() : 0; }

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
        String text = getText();
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
        String text = getText();
        Font font = getFont();
        _textWidth = text != null ? Math.ceil(font.getStringAdvance(text)) : 0;

        // Get Font Ascent, Descent, StringHeight (aka LineHeight)
        _ascent = Math.ceil(font.getAscent());
        _descent = Math.ceil(font.getDescent());
        _textHeight = _ascent + _descent;
    }

    /**
     * Returns whether text fits in box.
     */
    public boolean isTextFits()
    {
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        double strW = getTextWidth();
        double strH = getTextHeight();
        return strW <= areaW && strH <= areaH;
    }

    /**
     * Returns the text bounds.
     */
    public Rect getTextBounds()
    {
        // Get basic bounds for TextField size/insets and font/string width/height
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double textW = getTextWidth();
        double textH = getTextHeight();

        // Shift AreaX for X alignment
        double alignX = ViewUtils.getAlignX(this);
        if (alignX > 0) {
            double areaW = getWidth() - ins.getWidth();
            double extraW = areaW - textW;
            areaX = Math.max(areaX + extraW * alignX, areaX);
        }

        // Shift AreaY for Y alignment
        double alignY = ViewUtils.getAlignY(this);
        if (alignY > 0) {
            double areaH = getHeight() - ins.getHeight();
            double extraH = areaH - textH;
            areaY = Math.max(areaY + Math.round(extraH * alignY), areaY);
        }

        // Create/return rect
        return new Rect(areaX, areaY, textW, textH);
    }

    /**
     * Returns the text bounds for the given range of characters.
     */
    public Rect getTextBounds(int aStart, int aEnd)
    {
        // Get text, font and full text bounds
        String text = getText();
        Font font = getFont();
        Rect bnds = getTextBounds();

        // Trim left edge by characters up to start
        for (int i = 0; i < aStart; i++) {
            char c = text.charAt(i);
            double dx = font.charAdvance(c);
            bnds.x += dx;
            bnds.width -= dx;
        }

        // Trim right edge by characters after end
        for (int i = aEnd, iMax = text.length(); i < iMax; i++) {
            char c = text.charAt(i);
            bnds.width -= font.charAdvance(c);
        }

        // Return bounds
        return bnds;
    }

    /**
     * Returns the char index for the X location.
     */
    public int getCharIndexForX(double aX)
    {
        String text = getText();
        Font font = getFont();
        double textX = getTextBounds().x;
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
     * Override to make default center-left.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

    /**
     * Override to clear size info.
     */
    @Override
    public void relayout()
    {
        super.relayout();
        _copyThatFits = null;
    }

    /**
     * Override to clear size info.
     */
    @Override
    public void relayoutParent()
    {
        super.relayoutParent();
        _ascent = -1;
        if (getParent() == null)
            _needsResize = true;
        _copyThatFits = null;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double textW = getTextWidth();
        return textW + ins.getWidth();
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double textH = getTextHeight();
        return textH + ins.getHeight();
    }

    /**
     * A paint method for painting StringView outside of View hierarchy.
     */
    public void paintStringView(Painter aPntr)
    {
        aPntr.save();
        Transform xfm = getLocalToParent();
        aPntr.transform(xfm);
        paintAll(aPntr);
        aPntr.restore();
    }

    /**
     * Paints StringView.
     */
    protected void paintFront(Painter aPntr)
    {
        // If no text, just return
        if (length() == 0) return;

        // If painting before resize, set to pref size
        if (_needsResize) {
            double prefW = getPrefWidth();
            double prefH = getPrefHeight();
            setSize(prefW, prefH);
        }

        // If ShrinkToFit and text doesn't fit, paint special
        if (isShrinkToFit() && !isTextFits()) {
            StringView copyThatFits = getCopyThatFits();
            copyThatFits.paintFront(aPntr);
            return;
        }

        // Paint string
        paintString(aPntr);
    }

    /**
     * Paints StringView.
     */
    protected void paintString(Painter aPntr)
    {
        // Set font and text fill
        Font font = getFont();
        Paint textFill = getTextFill();
        aPntr.setFont(font);
        aPntr.setPaint(textFill);

        // Get String X/Y and paint
        String text = getText();
        Rect textBounds = getTextBounds();
        double textX = textBounds.x;
        double textY = textBounds.y + getAscent();
        aPntr.drawString(text, textX, textY);
    }

    /**
     * Returns a version of this box with font such that text fits.
     */
    private StringView getCopyThatFits()
    {
        // If already set, just return
        if (_copyThatFits != null) return _copyThatFits;

        // If TextFits, just set/return this
        if (isTextFits())
            return _copyThatFits = this;

        // Clone box, setFontToFit() and set/return
        StringView clone = clone();
        clone.setXY(0, 0);
        clone.setShrinkToFit(false);
        clone.setFontToFit();
        return _copyThatFits = clone;
    }

    /**
     * Sets the font so that it fits.
     */
    private void setFontToFit()
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
            double fontScale = (scaleLow + scaleHigh) / 2;
            double fontSize2 = fontSize * fontScale;
            setFont(font.deriveFont(fontSize2));

            double textW = getTextWidth();
            double textH = getTextHeight();
            boolean textFits = textW <= areaW && textH <= areaH;

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
                if (detaFS < .05)
                    break;

                // If almost fit width, stop
                double diffW = areaW - textW;
                if (diffW < 1)
                    break;

                // If almost fit height, stop
                double diffH = areaH - textH;
                if (diffH < 1)
                    break;
            }
        }
    }

    /**
     * Override to clear NeedsResize.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        _needsResize = false;
    }

    /**
     * Override to clear NeedsResize.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        _needsResize = false;
    }

    /**
     * Returns a copy of this StringView.
     */
    public StringView clone()
    {
        StringView copy;
        try { copy = (StringView) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        return copy;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        String text = getText();
        if (text != null && text.length() > 0)
            e.add("text", text);
        return e;
    }

    /**
     * XML unarchival.
     */
    public View fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        if (anElement.hasAttribute("text"))
            setText(anElement.getAttributeValue("text", anElement.getAttributeValue("value")));
        return this;
    }
}