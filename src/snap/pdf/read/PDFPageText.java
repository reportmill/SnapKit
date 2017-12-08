/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Font;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import snap.pdf.*;

/**
 * Represents text spacing details that are defined in between BT & ET operation. Only one text object is alive at any
 * given time, and only certain operations are allowed inside a text object.
 */
public class PDFPageText {

    // You can't nest text objects.  isOpen gets reset on BT & ET operations
    boolean isOpen = false;
    
    // The matrix for the current glyph
    AffineTransform textMatrix = new AffineTransform();
    
    // The matrix for the current line
    AffineTransform lineMatrix = new AffineTransform();
    
    // combined rendering matrix
    AffineTransform renderMatrix = new AffineTransform();
    
    // Buffer used to convert bytes in font's encoding to unichars or some other form that font's cmap will understand
    char unicodeBuf[] = new char[32];
    
    // A FontRenderContext to help create glyphs
    FontRenderContext rendercontext;
    
    // Text state parameters can persist across many text objects, so they're stored in the gstate
    // TODO:  Only horizontal writing mode supported at the moment. Eventually we'll need to do vertical, too.

/** Create new PDFPageText. */
public PDFPageText(FontRenderContext ctxt)  { rendercontext = ctxt; }

/** start new text. */
public void begin()
{
    if(!isOpen) { textMatrix.setToIdentity(); lineMatrix.setToIdentity(); isOpen = true; }
    else throw new PDFException("Attempt to nest text objects");
}

/** End text. */
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
 * Get a glyph vector by decoding string bytes according to font encoding,
 * and calculating spacing using text parameters in gstate.
 */
public void showText(byte pageBytes[], int offset, int length, PDFGState gs, PDFFile file, PDFMarkupHandler aPntr) 
{
    // TODO: This is probably a huge mistake (performance-wise) The font returned by the factory has a font size of 1
    // so we include the gstate's font size in the text rendering matrix. For any number of reasons, it'd probably be
    // better to do a deriveFont() with the font size and adjust the rendering matrix calculations.
    // I'm pretty sure this strategy completely mucks with rendering hints.
    // NB: rendering matrix includes flip (since font matrices are filpped)
    renderMatrix.setTransform(gs.fontSize*gs.thscale, 0, 0, -gs.fontSize, 0, -gs.trise);
 
    // Ensure the buffer is big enough for the bytes->cid conversion
    Map fontDict = gs.font;      // Get the font dictionary from the gstate
    GlyphMapper gmap = PDFFont.getGlyphMapper(fontDict, file);
    int bufmax = gmap.maximumOutputBufferSize(pageBytes, offset, length);
    int buflen = unicodeBuf.length;
    if(buflen<bufmax) {
        while(buflen<bufmax) buflen += buflen;
        unicodeBuf = new char[buflen];
    }
    
    // Convert to cids and get metrics (actually just the widths)
    int numMappedChars = gmap.mapBytesToChars(pageBytes, offset, length, unicodeBuf);
    Object wobj = PDFFont.getGlyphWidths(fontDict, file, aPntr);

    // Two nearly identical routines broken out for performance (and readability) reasons
    Font font = PDFFont.getFont(fontDict, file);
    GlyphVector glyphs; Point2D.Float pt = new Point2D.Float();
    if(gmap.isMultiByte()) glyphs = getMultibyteCIDGlyphVector(unicodeBuf, numMappedChars, gs, font, wobj, gmap, pt);
    else glyphs = getSingleByteCIDGlyphVector(pageBytes, offset, length, unicodeBuf,numMappedChars,gs,font,wobj,pt);
                           
    // replace the gstate ctm with one that includes the text transforms
    AffineTransform saved_ctm = (AffineTransform)gs.trans.clone();
    gs.trans.concatenate(textMatrix);
    gs.trans.concatenate(renderMatrix);
    
    // draw, restore ctm and update the text matrix
    aPntr.showText(gs, glyphs);
    gs.trans = saved_ctm;
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
    for(int i=0; i<length; ++i) { byte c = pageBytes[offset+i];
        glyphs.setGlyphPosition(i,textoffset);
        float advance = widths[c&255];
            
        // add word space, but only once if multiple spaces appear in a row
        //TODO: I think Acrobat considers other whitespace (like \t or \r) to be wordbreaks worthy of adding space
        if(c==32 && ((i==0) || (pageBytes[offset+i-1] != 32)))
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
GlyphVector getMultibyteCIDGlyphVector(char cids[], int numCIDs, PDFGState gs, Font aFont, Object wobj,
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
    for(int i=0; i<numCIDs; ++i) { int c = cids[i]&0xffff;
        glyphs.setGlyphPosition(i,textoffset);
        float advance = widths.getWidth(c);
         
        // This is only right for fonts that map cid 32 to the space char.
        // How you determine that unambiguously is unclear.
        if(c==32 && ((i==0) || (cids[i-1] != 32)))
            advance += gs.tws/(gs.fontSize*gs.thscale);
         
        // add character space
        advance += gs.tcs/(gs.fontSize*gs.thscale);
        textoffset.x += advance;
    }
    return glyphs;
}


/** Like the previous routine, except using a list of strings & spacing adjustments */
public void showText(byte pageBytes[], List <PageToken> tokens, PDFGState gs, PDFFile file, PDFMarkupHandler aPntr) 
{
    double hscale = -gs.fontSize*gs.thscale/1000;
    for(PageToken tok : tokens) {
        if(tok.type==PageToken.PDFNumberToken)
            textMatrix.translate(tok.floatValue()*hscale, 0);
        else showText(pageBytes, tok.getStart(), tok.getLength(), gs, file, aPntr);
    }
}

}