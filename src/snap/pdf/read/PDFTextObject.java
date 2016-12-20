/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Font;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * Represents text spacing details that are defined in between BT & ET operation. Only one text object is alive at any
 * given time, and only certain operations are allowed inside a text object.
 */
public class PDFTextObject {

    // You can't nest text objects.  isOpen gets reset on BT & ET operations
    boolean isOpen = false;
    
    // The matrix for the current glyph
    AffineTransform textMatrix = new AffineTransform();
    
    // The matrix for the current line
    AffineTransform lineMatrix = new AffineTransform();
    
    // combined rendering matrix
    AffineTransform renderingMatrix = new AffineTransform();
    
    // Buffer used to convert bytes in font's encoding to unichars or some other form that font's cmap will understand
    char unicodeBuffer[] = new char[32];
    
    // A FontRenderContext to help create glyphs
    FontRenderContext rendercontext;
    
    // Text state parameters can persist across many text objects, so they're stored in the gstate
    // TODO:  Only horizontal writing mode supported at the moment. Eventually we'll need to do vertical, too.

/** Main constructor. */
public PDFTextObject(FontRenderContext ctxt)  { rendercontext = ctxt; }

/** start a new text object */
public void begin()
{
    if (!isOpen) {
        textMatrix.setToIdentity();
        lineMatrix.setToIdentity();
        isOpen=true;
    }
    else throw new PDFException("Attempt to nest text objects");
}

/** End the text object */
public void end()
{
    if(isOpen) isOpen = false;
    else throw new PDFException("Attempt to close a nonexistent text object");
}

/**
 * Check if the text object is active.  This can be used to raise errors for operations
 * that are only legal within or without a text object.
 */
public boolean isOpen() { return isOpen; }

/** Set text position relative to current line matrix.  Used by Td, TD, T*, ', "*/
public void positionText(float x, float y)
{
    lineMatrix.translate(x,y);
    textMatrix.setTransform(lineMatrix);
}

public void setTextMatrix(float a, float b, float c, float d, float e, float f)
{
    textMatrix.setTransform(a,b,c,d,e,f);
    lineMatrix.setTransform(a,b,c,d,e,f);
}

/**
 * Get a glyph vector by decoding the string bytes according to the font encoding,
 * and calculating spacing using the text parameters in the gstate.
 */
public void showText(byte pageBytes[], int offset, int length, PDFGState gs, PDFFile file) 
{
    // Get the font factory and use it to create a glyphmapper & an awt font
    Map f = gs.font;      // Get the font dictionary from the gstate
    FontFactory factory = file.getFontFactory();
    GlyphMapper gmapper = factory.getGlyphMapper(f,file);
    Font awtFont = factory.getFont(f, file);
    Point2D.Float pt = new Point2D.Float();
    
    // TODO: This is probably a huge mistake (performance-wise) The font returned by the factory has a font size of 1
    // so we include the gstate's font size in the text rendering matrix. For any number of reasons, it'd probably be
    // better to do a deriveFont() with the font size and adjust the rendering matrix calculations.
    // I'm pretty sure this strategy completely mucks with rendering hints.
    //
    // NB: rendering matrix includes flip (since font matrices are filpped)
    //   It might be wise at some point to look at the actual font matrix
    renderingMatrix.setTransform(gs.fontSize*gs.thscale, 0, 0, -gs.fontSize, 0, -gs.trise);
 
    // Ensure the buffer is big enough for the bytes->cid conversion
    int bufmax = gmapper.maximumOutputBufferSize(pageBytes, offset, length);
    int buflen = unicodeBuffer.length;
    if (buflen < bufmax) {
        while(buflen<bufmax)
            buflen += buflen;
        unicodeBuffer = new char[buflen];
    }
    
    // Convert to cids
    int numMappedChars = gmapper.mapBytesToChars(pageBytes, offset, length, unicodeBuffer);
    
    // get the metrics (actually just the widths)
    Object wobj = factory.getGlyphWidths(f,file);

    // Two nearly identical routines broken out for performance (and readability) reasons
    GlyphVector glyphs;
    if (gmapper.isMultiByte()) 
        glyphs = getMultibyteCIDGlyphVector(unicodeBuffer, numMappedChars, gs, awtFont, wobj, gmapper, pt);
    else glyphs = getSingleByteCIDGlyphVector(pageBytes, offset, length, unicodeBuffer, numMappedChars,
                                             gs, awtFont, wobj, pt);
                           
    // replace the gstate ctm with one that includes the text transforms
    AffineTransform saved_ctm=(AffineTransform)gs.trans.clone();
    gs.trans.concatenate(textMatrix);
    gs.trans.concatenate(renderingMatrix);
    
    // draw, restore ctm and update the text matrix
    file.getMarkupHandler().showText(gs, glyphs);
    gs.trans=saved_ctm;
    textMatrix.translate(pt.x*gs.fontSize*gs.thscale, pt.y);
}

/**
 * For simple fonts.  The bytes have been mapped through the encoding into unicode values.  The font itself will
 * create the glyphs, and the font metric lookups are done by assuming that a single byte in pageBytes will get
 * mapped to single unicode value (and therefore a single glyph) in the glyph vector.
 * TODO: check if there are any issues with composed chars or other cases such that the assumption that
 * 1 byte->1 unicode->1 glyph fails.
 */
GlyphVector getSingleByteCIDGlyphVector(byte pageBytes[], int offset, int length, char uchars[], int numChars,
                                        PDFGState gs, Font awtFont, Object wobj, Point2D.Float textoffset)
{
    // widths for single byte fonts is just a simple map
    float widths[] = (float[])wobj;
    
    // Tell the font (assumed to have a point size of 1) to create the glyphs
    char chars[] = uchars.length==numChars? uchars : Arrays.copyOf(uchars, numChars);
    GlyphVector glyphs = awtFont.createGlyphVector(rendercontext, chars);
     
    // position adjustments.  For performance reasons, we can probably skip this step if word and character spacing
    // are both 0, although we still need to calculate advance for the whole thing (maybe with help from glyphVector)
    textoffset.x = textoffset.y = 0;
    for(int i=0; i<length; ++i) {
        byte c = pageBytes[offset+i];
        glyphs.setGlyphPosition(i,textoffset);
        float advance = widths[c&255];
            
            // add word space, but only once if multiple spaces appear in a row
            //TODO: I think Acrobat considers other whitespace (like \t or \r) to also be wordbreaks worthy of adding space.
            if ((c==32) && ((i==0) || (pageBytes[offset+i-1] != 32)))
                // Check this again - include scale & font or not?? This matches up with preview & Acrobat for char but
                // only matches word when thscale=1. Mac.pdf has good example
                advance += gs.tws/(gs.fontSize*gs.thscale);
            
            // add character space
            advance += gs.tcs/(gs.fontSize*gs.thscale);
            textoffset.x += advance;
        }
    return glyphs;
}

/**
 * For cid fonts.  The bytes have been mapped through the encoding into two-byte, big-endian cids.  
 * This will ask the GlyphMapper to map the cids to glyph ids, which it will do with the help of the CIDToGIDMap
 * stream, and then create the GlyphVector.  It then uses a widthTable to look up each cid's width
 * for advancement calculations.
 */
GlyphVector getMultibyteCIDGlyphVector(char cids[], int numCIDs, PDFGState gs, Font awtFont, Object wobj,
                                     GlyphMapper mapper, Point2D.Float textoffset)
{
    // widths for cid fonts are looked up in a widthtable
    PDFGlyphWidthTable widths = (PDFGlyphWidthTable)wobj;
 
    // make a conversion buffer.  note again the 1 cid->1 gid assumption
    int glyphIDs[] = new int[numCIDs];
    
    // Get the glyph ids
    mapper.mapCharsToGIDs(cids, numCIDs, glyphIDs); // int nglyphs = ?

    // Create a glyphVector using the gids. Note that although the javadoc
    // for Font claims that the int array is for glyphCodes, the description
    // is identical to the char method, which uses the font's unicode cmap.
    // I'm assuming the desrciption is a cut&paste bug and that the int 
    // array called glyphCodes is really used as an array of glyph codes.
    GlyphVector glyphs = awtFont.createGlyphVector(rendercontext, glyphIDs);
    
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
}


/** Like the previous routine, except using a list of strings & spacing adjustments */
public void showText(byte pageBytes[], List tokens, PDFGState gs, PDFFile file) 
{
    //TODO: This could be made much more efficient by creating a single glyphvector.
    double hscale=-gs.fontSize*gs.thscale/1000;
    for(int i=0, n=tokens.size(); i<n; ++i) {
        PageToken tok = (PageToken)tokens.get(i);
        if(tok.type==PageToken.PDFNumberToken)
            textMatrix.translate(tok.floatValue()*hscale, 0);
        else showText(pageBytes, tok.tokenLocation(), tok.tokenLength(), gs, file);
    }
}

// Debugging foolishness
public void dddshowText(byte pageBytes[], int offset, int length, PDFGState gs, PDFFile file) 
{
    // Get the font factory and use it to create a glyphmapper & an awt font
    Map f = gs.font;       // Get the font dictionary from the gstate
    FontFactory factory = file.getFontFactory();
    Font awtFont = factory.getFont(f, file);
    Point2D.Float textoffset = new Point2D.Float();
    
    renderingMatrix.setTransform(gs.fontSize*gs.thscale, 0, 0, -gs.fontSize, 0, -gs.trise);
 
    int nglyphs = awtFont.getNumGlyphs(); int gbuff[] = new int[nglyphs];
    for(int i=0; i<nglyphs; ++i) gbuff[i]=i;
    
    GlyphVector glyphs = awtFont.createGlyphVector(rendercontext, gbuff);
    Rectangle2D bigBounds = awtFont.getMaxCharBounds(rendercontext);
    float dx = (float)bigBounds.getWidth(), dy = (float)bigBounds.getHeight();
  
    textoffset.x = textoffset.y = 0;
    for(int i=0; i<nglyphs; ++i) {
        glyphs.setGlyphPosition(i,textoffset);
        if(i%10==9) {
            textoffset.y += dy; textoffset.x = 0; }
        else textoffset.x += dx;
    }
    
    // replace the gstate ctm with one that includes the text transforms
    AffineTransform saved_ctm=(AffineTransform)gs.trans.clone();
    gs.trans.concatenate(textMatrix); gs.trans.concatenate(renderingMatrix);
    
    // draw and restore ctm
    file.getMarkupHandler().showText(gs, glyphs);
    gs.trans=saved_ctm;
}

}