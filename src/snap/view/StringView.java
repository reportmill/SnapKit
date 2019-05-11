/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View {
    
    // Text
    String         _text;
    
    // The text paint
    Paint          _textFill = Color.BLACK;
    
/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aValue)  { _text = aValue; relayoutParent(); repaint(); }

/**
 * Returns the text fill.
 */
public Paint getTextFill()  { return _textFill; }

/**
 * Sets the text fill.
 */
public void setTextFill(Paint aPnt)  { _textFill = aPnt; repaint(); }

/**
 * Returns the text length.
 */
public int length()  { return _text!=null? _text.length() : 0; }

/**
 * Returns the text width.
 */
public double getTextWidth()  { return length()>0? Math.ceil(getFont().getStringAdvance(getText())) : 0; }

/**
 * Returns the text height.
 */
public double getTextHeight()
{
    if(length()==0) return 0;
    return Math.ceil(getFont().getLineHeight());
}

/**
 * Returns the text bounds.
 */
public Rect getTextBounds()
{
    // Get basic bounds for TextField size/insets and font/string width/height
    Insets ins = getInsetsAll(); double width = getWidth(), height = getHeight();
    double tx = ins.left, ty = ins.top, tw = getTextWidth(), th = getTextHeight();
    
    // Adjust rect by alignment
    double ax = ViewUtils.getAlignX(this), ay = ViewUtils.getAlignY(this);
    if(ax>0) { double extra = width - tx - ins.right - tw; 
        tx = Math.max(tx+extra*ax,tx); }
    if(ay>0) { double extra = height - ty - ins.bottom - th;
        ty = Math.max(ty+Math.round(extra*ay),ty); }
        
    // Create/return rect
    return new Rect(tx,ty,tw,th);
}

/**
 * Returns the text bounds for the given range of characters.
 */
public Rect getTextBounds(int aStart, int aEnd)
{
    // Get string, font and full text bounds
    String str = getText(); Font font = getFont();
    Rect bnds = getTextBounds();

    // Trim left edge by characters up to start
    for(int i=0; i<aStart; i++) {
        double dx = font.charAdvance(str.charAt(i));
        bnds.x += dx; bnds.width -= dx;
    }
    
    // Trim right edge by characters after end
    for(int i=aEnd, iMax=str.length(); i<iMax; i++)
        bnds.width -= font.charAdvance(str.charAt(i));
    
    // Return bounds
    return bnds;
}

/**
 * Returns the char index for the X location.
 */
public int getCharIndexForX(double aX)
{
    String str = getText(); Font font = getFont();
    double x = getTextBounds().x;
    for(int i=0, iMax=str.length(); i<iMax; i++) {
        double dx = font.charAdvance(str.charAt(i));
        if(aX<=x+dx/2) return i;
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
    Insets ins = getInsetsAll(); double tw = getTextWidth();
    return tw + ins.getWidth();
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll(); double th = getTextHeight();
    return th + ins.getHeight();
}

/**
 * Paints StringView.
 */
protected void paintFront(Painter aPntr)
{
    if(length()==0) return;
    Rect bnds = getTextBounds(); Font font = getFont(); double baseline = Math.ceil(font.getAscent());
    aPntr.setFont(font); aPntr.setPaint(_textFill);
    aPntr.drawString(_text, bnds.x, bnds.y + baseline);
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver);
    String text = getText(); if(text!=null && text.length()>0) e.add("text", text);
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