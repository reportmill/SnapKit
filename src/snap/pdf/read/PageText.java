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
    
    // Combined rendering Transform
    Transform        _renderMatrix = new Transform();
    
    // Buffer used to convert bytes in font's encoding to unichars or some other form that font's cmap will understand
    char             _unicodeBuffer[] = new char[32];
    
    // A FontRenderContext to help create glyphs
    //FontRenderContext rendercontext;
    
    // Text state parameters can persist across many text objects, so they're stored in the gstate
    // TODO:  Only horizontal writing mode supported at the moment. Eventually we'll need to do vertical, too.

/** Main constructor. */
//public PageText(FontRenderContext ctxt)  { rendercontext = ctxt; }
public PageText(PagePainter aPntr)
{
    _ppntr = aPntr;
    _pntr = _ppntr._pntr;
}

/** start a new text object */
public void begin()
{
    if (!_open) {
        _textMatrix.clear();
        _lineMatrix.clear();
        _open = true;
    }
    else throw new PDFException("Attempt to nest text objects");
}

/** End the text object */
public void end()
{
    if(_open) _open = false;
    else throw new PDFException("Attempt to close a nonexistent text object");
}

/**
 * Check if the text object is active.  This can be used to raise errors for operations
 * that are only legal within or without a text object.
 */
public boolean isOpen() { return _open; }

/** Set text position relative to current line matrix.  Used by Td, TD, T*, ', "*/
public void positionText(double x, double y)
{
    _lineMatrix.translate(x,y);
    _textMatrix.setMatrix(_lineMatrix);
}

public void setTextMatrix(double a, double b, double c, double d, double e, double f)
{
    _textMatrix.setMatrix(a,b,c,d,e,f);
    _lineMatrix.setMatrix(a,b,c,d,e,f);
}

/**
 * Get a glyph vector by decoding string bytes according to font encoding,
 * and calculating spacing using text parameters in gstate.
 */
//public void showText(byte pageBytes[], int offset, int length, PDFGState gs, PDFFile file, PDFMarkupHandler aPntr) 
public void showText(String aStr) 
{
    // Get the glyphmapper & font
    //Map fontDict = gs.font;      // Get the font dictionary from the gstate
    //GlyphMapper gmapper = PDFFont.getGlyphMapper(fontDict, file);
    //Font font = PDFFont.getFont(fontDict, file);
    //Point pt = new Point();
    
    // TODO: This is probably a huge mistake (performance-wise) The font returned by the factory has a font size of 1
    // so we include the gstate's font size in the text rendering matrix. For any number of reasons, it'd probably be
    // better to do a deriveFont() with the font size and adjust the rendering matrix calculations.
    // I'm pretty sure this strategy completely mucks with rendering hints.
    // NB: rendering matrix includes flip (since font matrices are filpped)
    Font font = _pntr.getFont();
    double fsize = 1;//font.getSize(); //gs.fontSize;
    double thscale = 1; //gs.thscale;
    double trise = 0; //gs.trise;
    _renderMatrix.setMatrix(fsize*thscale, 0, 0, -fsize, 0, -trise);
 
    // Ensure the buffer is big enough for the bytes->cid conversion
    //int bufmax = gmapper.maximumOutputBufferSize(pageBytes, offset, length);
    //int buflen = _unicodeBuffer.length;
    //if (buflen < bufmax) {
    //    while(buflen<bufmax) buflen += buflen;
    //    _unicodeBuffer = new char[buflen]; }
    
    // Convert to cids
    //int numMappedChars = gmapper.mapBytesToChars(pageBytes, offset, length, unicodeBuffer);
    
    // get the metrics (actually just the widths)
    //Object wobj = PDFFont.getGlyphWidths(fontDict, file, aPntr);

    // Two nearly identical routines broken out for performance (and readability) reasons
    //GlyphVector glyphs;
    //if(gmapper.isMultiByte()) 
    //    glyphs = getMultibyteCIDGlyphVector(unicodeBuffer, numMappedChars, gs, font, wobj, gmapper, pt);
    //else glyphs = getSingleByteCIDGlyphVector(pageBytes,offset,length,_unicodeBuffer,numMappedChars,gs,font,wobj,pt);
                           
    // replace the gstate ctm with one that includes the text transforms
    _pntr.save(); //Transform saved_ctm = _pntr.get(Transform)gs.trans.clone();
    _pntr.transform(_textMatrix); //gs.trans.concatenate(_textMatrix);
    _pntr.transform(_renderMatrix); //gs.trans.concatenate(_renderMatrix);
    
    // draw, restore ctm and update the text matrix
    _pntr.drawString(aStr, 0, 0); //_pntr.showText(gs, glyphs);
    _pntr.restore(); //gs.trans = saved_ctm;
    //_textMatrix.translate(pt.x*gs.fontSize*gs.thscale, pt.y);
}

/**
 * For simple fonts.  The bytes have been mapped through the encoding into unicode values.  The font itself will
 * create the glyphs, and the font metric lookups are done by assuming that a single byte in pageBytes will get
 * mapped to single unicode value (and therefore a single glyph) in the glyph vector.
 * TODO: check if there are any issues with composed chars or other cases such that the assumption that
 * 1 byte->1 unicode->1 glyph fails.
 */
/*GlyphVector getSingleByteCIDGlyphVector(byte pageBytes[], int offset, int length, char uchars[], int numChars,
                                        PDFGState gs, Font aFont, Object wobj, Point2D.Float textoffset)
{
    // widths for single byte fonts is just a simple map
    float widths[] = (float[])wobj;
    
    // Tell font (assumed to have a point size of 1) to create glyphs
    char chars[] = uchars.length==numChars? uchars : Arrays.copyOf(uchars, numChars);
    GlyphVector glyphs = aFont.createGlyphVector(rendercontext, chars);
     
    // position adjustments.  For performance reasons, we can probably skip this step if word and character spacing
    // are both 0, although we still need to calculate advance for the whole thing (maybe with help from glyphVector)
    textoffset.x = textoffset.y = 0;
    for(int i=0; i<length; ++i) {
        byte c = pageBytes[offset+i];
        glyphs.setGlyphPosition(i,textoffset);
        float advance = widths[c&255];
            
        // add word space, but only once if multiple spaces appear in a row
        //TODO: I think Acrobat considers other whitespace (like \t or \r) to be wordbreaks worthy of adding space
        if ((c==32) && ((i==0) || (pageBytes[offset+i-1] != 32)))
            // Check this again - include scale & font or not?? This matches up with preview & Acrobat for char but
            // only matches word when thscale=1. Mac.pdf has good example
            advance += gs.tws/(gs.fontSize*gs.thscale);
        
        // add character space
        advance += gs.tcs/(gs.fontSize*gs.thscale);
        textoffset.x += advance;
    }
    return glyphs;
}*/

/**
 * For cid fonts.  The bytes have been mapped through the encoding into two-byte, big-endian cids.  
 * This will ask the GlyphMapper to map the cids to glyph ids, which it will do with the help of the CIDToGIDMap
 * stream, and then create the GlyphVector.  It then uses a widthTable to look up each cid's width
 * for advancement calculations.
 */
/*GlyphVector getMultibyteCIDGlyphVector(char cids[], int numCIDs, PDFGState gs, Font aFont, Object wobj,
                                     GlyphMapper mapper, Point2D.Float textoffset)
{
    // widths for cid fonts are looked up in a widthtable
    PDFGlyphWidthTable widths = (PDFGlyphWidthTable)wobj;
 
    // make a conversion buffer.  note again the 1 cid->1 gid assumption
    int glyphIDs[] = new int[numCIDs];
    
    // Get the glyph ids
    mapper.mapCharsToGIDs(cids, numCIDs, glyphIDs); // int nglyphs = ?

    // Create a glyphVector using the gids. Note that although the javadoc for Font claims that the int array is for
    // glyphCodes, the description is identical to char method, which uses font's unicode cmap. I assume desrciption
    // is a cut&paste bug and that the int array called glyphCodes is really used as an array of glyph codes.
    GlyphVector glyphs = aFont.createGlyphVector(rendercontext, glyphIDs);
    
    // position adjustments.  See single-byte routine for comments
    textoffset.x = textoffset.y = 0;
    for(int i=0; i<numCIDs; ++i) {
        int c = cids[i]&0xffff;
        glyphs.setGlyphPosition(i,textoffset);
        float advance = widths.getWidth(c);
         
        // This is only right for fonts that map cid 32 to the space char.
        // How you determine that unambiguously is unclear.
        if ((c==32) && ((i==0) || (cids[i-1] != 32)))
             advance += gs.tws/(gs.fontSize*gs.thscale);
         
         // add character space
         advance += gs.tcs/(gs.fontSize*gs.thscale);
         textoffset.x += advance;
     }
    return glyphs;
}*/


/** Like the previous routine, except using a list of strings & spacing adjustments */
//public void showText(byte pageBytes[], List tokens, PDFGState gs, PDFFile file, PDFMarkupHandler aPntr) 
public void showText(List <PageToken> theTokens)
{
    double hscale = 1; //-gs.fontSize*gs.thscale/1000;
    for(PageToken tok : theTokens) {
        if(tok.type==PageToken.PDFNumberToken)
            _textMatrix.translate(tok.floatValue()*hscale, 0);
        else showText(tok.getString()); //showText(pageBytes, tok.getStart(), tok.getLength(), gs, file, aPntr);
    }
}

}