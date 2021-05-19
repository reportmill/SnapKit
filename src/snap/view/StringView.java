/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.text.StringBox;
import snap.util.*;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View {

    // Text
    String         _text;
    
    // The text paint
    Paint          _textFill;

    // Whether text should shrink to fit
    public boolean  _shrinkToFit;

    // Constants for properties
    public static final String ShrinkToFit_Prop = "ShrinkToFit";

    /**
     * Constructor.
     */
    public StringView()
    {
        super();
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
        _text = aValue;
        relayoutParent();
        repaint();
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()  { return _textFill!=null ? _textFill : ViewUtils.getTextFill(); }

    /**
     * Sets the text fill.
     */
    public void setTextFill(Paint aPaint)
    {
        _textFill = aPaint;
        repaint();
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
    public int length()  { return _text!=null ? _text.length() : 0; }

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
     * Returns the text width.
     */
    public double getTextWidth()
    {
        if (length()==0) return 0;
        Font font = getFont();
        String str = getText();
        return Math.ceil(font.getStringAdvance(str));
    }

    /**
     * Returns the text height.
     */
    public double getTextHeight()
    {
        if (length()==0) return 0;
        return Math.ceil(getFont().getLineHeight());
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
        if (alignX>0) {
            double areaW = getWidth() - ins.getWidth();
            double extraW = areaW - textW;
            areaX = Math.max(areaX + extraW*alignX, areaX);
        }

        // Shift AreaY for Y alignment
        double alignY = ViewUtils.getAlignY(this);
        if (alignY>0) {
            double areaH = getHeight() - ins.getHeight();
            double extraH = areaH - textH;
            areaY = Math.max(areaY + Math.round(extraH*alignY), areaY);
        }

        // Create/return rect
        return new Rect(areaX, areaY, textW, textH);
    }

    /**
     * Returns the text bounds for the given range of characters.
     */
    public Rect getTextBounds(int aStart, int aEnd)
    {
        // Get string, font and full text bounds
        String str = getText();
        Font font = getFont();
        Rect bnds = getTextBounds();

        // Trim left edge by characters up to start
        for (int i=0; i<aStart; i++) {
            double dx = font.charAdvance(str.charAt(i));
            bnds.x += dx; bnds.width -= dx;
        }

        // Trim right edge by characters after end
        for (int i=aEnd, iMax=str.length(); i<iMax; i++) { char c = str.charAt(i);
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
        String str = getText();
        Font font = getFont();
        double x = getTextBounds().x;
        for (int i=0, iMax=str.length(); i<iMax; i++) { char c = str.charAt(i);
            double dx = font.charAdvance(c);
            if (aX<=x+dx/2)
                return i;
            x += dx;
        }
        return str.length();
    }

    /**
     * Override to make default center-left.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

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
     * Paints StringView.
     */
    protected void paintFront(Painter aPntr)
    {
        // If no text, just return
        if (length()==0) return;

        // If ShrinkToFit and text doesn't fit, paint special
        if (isShrinkToFit() && !isTextFits()) {
            StringBox sbox = createStringBox().getBoxThatFits();
            sbox.drawString(aPntr);
            return;
        }

        // Set font and text fill
        Font font = getFont();
        aPntr.setFont(font);
        aPntr.setPaint(getTextFill());

        // Get String X/Y and paint
        Rect bnds = getTextBounds();
        double strY = bnds.y + Math.ceil(font.getAscent());
        aPntr.drawString(_text, bnds.x, strY);
    }

    /**
     * Creates a StringBox.
     */
    private StringBox createStringBox()
    {
        StringBox sbox = new StringBox(getText());
        sbox.setFont(getFont());
        sbox.setTextColor(getTextFill().getColor());
        sbox.setSize(getWidth(), getHeight());
        sbox.setPadding(getPadding());
        sbox.setBorder(getBorder());
        return sbox;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        String text = getText(); if (text!=null && text.length()>0) e.add("text", text);
        return e;
    }

    /**
     * XML unarchival.
     */
    public View fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        setText(anElement.getAttributeValue("text", anElement.getAttributeValue("value")));
        return this;
    }
}