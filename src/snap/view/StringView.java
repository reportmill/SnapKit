/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.Transform;
import snap.gfx.*;
import snap.text.StyledString;
import snap.text.TextStyle;
import snap.util.*;
import java.util.Objects;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View implements Cloneable {

    // TextRun
    private StyledString _styledString;
    
    // Whether text should shrink to fit
    private boolean  _shrinkToFit;

    // Whether box needs to be sized
    private boolean  _needsResize = true;

    // The version of this StringView with font such that string fits
    private StringView  _copyThatFits;

    // Constants for properties
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

        // Create TextRun
        _styledString = new StyledString();
        _styledString.setFontSizing(DEFAULT_FONT_SIZING);
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
    public String getText()  { return _styledString.getString(); }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        // if already set, just return
        String old = getText();
        if (Objects.equals(aValue, old)) return;

        // Set new value, fire prop change, relayout and return
        _styledString.setString(aValue);
        firePropChange(Text_Prop, old, aValue);
        relayoutParent();
        repaint();
    }

    /**
     * Returns the TextStyle.
     */
    public TextStyle getStyle()  { return _styledString.getStyle(); }

    /**
     * Sets the TextStyle.
     */
    public void setStyle(TextStyle aStyle)
    {
        // if already set, just return
        TextStyle old = getStyle();
        if (Objects.equals(aStyle, old)) return;

        // Set new value, fire prop change, relayout and return
        _styledString.setStyle(aStyle);
        firePropChange(TextStyle_Prop, old, aStyle);
        relayoutParent();
        repaint();
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()
    {
        return _styledString.getTextFill();
    }

    /**
     * Sets the text fill.
     */
    public void setTextFill(Paint aPaint)
    {
        if (Objects.equals(aPaint, getTextFill())) return;
        TextStyle textStyle = getStyle().copyFor(TextStyle.COLOR_KEY, aPaint);
        setStyle(textStyle);
    }

    /**
     * Returns whether to size to font (looser) instead of glyphs (tighter).
     */
    public boolean isFontSizing()  { return _styledString.isFontSizing(); }

    /**
     * Sets whether to size to font (looser) instead of glyphs (tighter).
     */
    public void setFontSizing(boolean aValue)
    {
        // If already set, just return
        boolean old = isFontSizing();
        if (aValue == old) return;

        // Set, fire prop change, relayout parent, return
        _styledString.setFontSizing(aValue);
        firePropChange(FontSizing_Prop, old, aValue);
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
    public int length()  { return _styledString.length(); }

    /**
     * Returns the width of the string (aka the 'advance').
     */
    public double getTextWidth()
    {
        return _styledString.getTextWidth();
    }

    /**
     * Returns the height of the current string/font (aka the 'line height').
     */
    public double getTextHeight()
    {
        return _styledString.getTextHeight();
    }

    /**
     * Returns the ascent.
     */
    public double getAscent()
    {
        return _styledString.getAscent();
    }

    /**
     * Returns the descent.
     */
    public double getDescent()
    {
        return _styledString.getDescent();
    }

    /**
     * Returns whether text fits in box.
     */
    public boolean isTextFits()
    {
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        double textW = getTextWidth();
        double textH = getTextHeight();
        return textW <= areaW && textH <= areaH;
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
        double textX = getTextBounds().x;
        double runX = aX - textX;
        return _styledString.getCharIndexForX(runX);
    }

    /**
     * Sets the box centered around given X/Y.
     */
    public void setCenteredXY(double aX, double aY)
    {
        double width = getWidth();
        double height = getHeight();
        int x2 = (int) Math.round(aX - width / 2);
        int y2 = (int) Math.round(aY - height / 2);
        setXY(x2, y2);
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
        Rect textBounds = getTextBounds();
        _styledString.paintStringTopLeft(aPntr, textBounds.x, textBounds.y);
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
     * Override to update TextRun.
     */
    @Override
    public void setFont(Font aFont)
    {
        // Do normal version
        if (Objects.equals(aFont, getFont())) return;
        super.setFont(aFont);

        // Set font in TextRun
        Font font = getFont();
        _styledString.setFont(font);
    }

    /**
     * Override to update TextRun.
     */
    @Override
    protected void parentFontChanged()
    {
        // Do normal version
        super.parentFontChanged();

        // Set font in TextRun
        Font font = getFont();
        _styledString.setFont(font);
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
        copy._styledString = _styledString.clone();
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