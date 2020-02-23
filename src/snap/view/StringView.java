/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View {
    
    // Text
    String         _text;
    
    // The text paint
    Paint          _textFill;
    
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
    if(SnapUtils.equals(aValue, _text)) return;
    
    // Set new value, fire prop change, relayout and return
    _text = aValue;
    relayoutParent();
    repaint();
}

/**
 * Returns the text fill.
 */
public Paint getTextFill()  { return _textFill!=null? _textFill : ViewUtils.getTextFill(); }

/**
 * Sets the text fill.
 */
public void setTextFill(Paint aPaint)
{
    _textFill = aPaint;
    repaint();
}

/**
 * Returns the text length.
 */
public int length()  { return _text!=null? _text.length() : 0; }

/**
 * Returns the text width.
 */
public double getTextWidth()
{
    if(length()==0) return 0;
    String text = getText();
    Font font = getFont();
    double tw = Math.ceil(font.getStringAdvance(text));
    return tw;
}

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
    Insets ins = getInsetsAll();
    double width = getWidth(), height = getHeight();
    double tx = ins.left, tw = getTextWidth();
    double ty = ins.top, th = getTextHeight();
    
    // Adjust rect by alignment
    double ax = ViewUtils.getAlignX(this);
    double ay = ViewUtils.getAlignY(this);
    if(ax>0) {
        double extra = width - tx - ins.right - tw; 
        tx = Math.max(tx+extra*ax,tx);
    }
    if(ay>0) {
        double extra = height - ty - ins.bottom - th;
        ty = Math.max(ty+Math.round(extra*ay),ty);
    }
        
    // Create/return rect
    return new Rect(tx, ty, tw, th);
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
    for(int i=0; i<aStart; i++) {
        double dx = font.charAdvance(str.charAt(i));
        bnds.x += dx; bnds.width -= dx;
    }
    
    // Trim right edge by characters after end
    for(int i=aEnd, iMax=str.length(); i<iMax; i++) { char c = str.charAt(i);
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
    for(int i=0, iMax=str.length(); i<iMax; i++) { char c = str.charAt(i);
        double dx = font.charAdvance(c);
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
    Insets ins = getInsetsAll();
    double tw = getTextWidth();
    return tw + ins.getWidth();
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    double th = getTextHeight();
    return th + ins.getHeight();
}

/**
 * Paints StringView.
 */
protected void paintFront(Painter aPntr)
{
    // If no text, just return
    if(length()==0) return;
    
    
    Rect bnds = getTextBounds();
    Font font = getFont();
    Paint textFill = getTextFill();
    double baseline = Math.ceil(font.getAscent());
    aPntr.setFont(font);
    aPntr.setPaint(textFill);
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