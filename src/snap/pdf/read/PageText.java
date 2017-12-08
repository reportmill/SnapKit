/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.List;
import snap.gfx.*;
import snap.pdf.*;

/**
 * Represents text spacing details that are defined in between BT & ET operation. Only one text object is alive at any
 * given time, and only certain operations are allowed inside a text object.
 */
public class PageText {
    
    // The PagePainter
    PagePainter       _ppntr;
    
    // The Snap Painter
    Painter           _pntr;

    // You can't nest text objects.  isOpen gets reset on BT & ET operations
    boolean           _open = false;
    
    // The Transform for the current glyph
    Transform        _textMatrix = new Transform();
    
    // The Transform for the current line
    Transform        _lineMatrix = new Transform();
    
    // Text space attributes (should really be in GState?)
    double           _charSpc, _wordSpc, _leading, _horScale = 1, _rise;

/**
 * Create new PageText.
 */
public PageText(PagePainter aPntr)  { _ppntr = aPntr; _pntr = _ppntr._pntr; }

/** Start new text. */
public void begin()
{
    if(!_open) { _textMatrix.clear(); _lineMatrix.clear(); _open = true;
        _charSpc = _wordSpc = _leading = _rise = 0; _horScale = 1; } // Should really be in GState?
    else throw new PDFException("Attempt to nest text objects");
}

/** End text. */
public void end()
{
    if(_open) _open = false;
    else throw new PDFException("Attempt to close a nonexistent text object");
}

/**
 * Set text position relative to current line matrix.  Used by Td, TD, T*, ', ".
 */
public void positionText(double x, double y)
{
    Transform tm = Transform.getTrans(x,y); tm.multiply(_lineMatrix);
    _lineMatrix.setMatrix(tm);
    _textMatrix.setMatrix(tm);
}

/**
 * Set text matrix. Used by Tm.
 */
public void setTextMatrix(double a, double b, double c, double d, double e, double f)
{
    _textMatrix.setMatrix(a,b,c,d,e,f);
    _lineMatrix.setMatrix(a,b,c,d,e,f);
}

/**
 * Show given string (Tj).
 */
public void showText(String aStr) 
{
    // Save GState
    _pntr.save();
    
    // Transform by TextMatrix, apply horizontal scale and flip and draw string
    _pntr.transform(_textMatrix);
    _pntr.scale(_horScale, -1);
    _pntr.drawString(aStr, 0, 0);
    
    // Restore GState
    _pntr.restore();
    
    // Char char advance
    double adv = getAdvance(aStr);
    Transform tm = Transform.getTrans(adv,0); tm.multiply(_textMatrix);
    _textMatrix.setMatrix(tm);
}

/**
 * Show given tokens (TJ)
 */
public void showText(List <PageToken> theTokens)
{
    // Get horizontal scale
    double hscale = _pntr.getFont().getSize()*_horScale/1000; //-gs.fontSize*gs.thscale/1000;
    
    // Iterate over tokens and show
    for(PageToken tok : theTokens) {
        if(tok.type==PageToken.PDFNumberToken)
            _textMatrix.translate(tok.floatValue()*hscale, 0);
        else showText(tok.getString());
    }
}

/**
 * Returns the advance for a string, based on font, char spacing, word spacing, horizontal scale.
 */
private double getAdvance(String aStr)
{
    // Get font and initialize advance
    Font font = _pntr.getFont();
    double adv = 0;
    
    // Iterate over characters and add charAdvance, CharSpacing, WordSpacing
    for(int i=0, iMax=aStr.length(); i<iMax; i++) { char c = aStr.charAt(i);
        adv += font.charAdvance(c) + _charSpc;
        if(Character.isWhitespace(c) && (i==0 || !Character.isWhitespace(aStr.charAt(i-1)))) adv += _wordSpc;
    }
    
    // Return advance time horizontal scale
    return adv*_horScale;  // Divide by font size?
}

}