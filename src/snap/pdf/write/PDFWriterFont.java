/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import snap.gfx.*;
import snap.pdf.PDFStream;
import snap.pdf.PDFWriter;
import snap.pdf.PDFWriterBase;
import snap.pdf.PDFXTable;
import snap.util.*;

/**
 * PDFWriter utility method(s) for writing fonts.
 */
public class PDFWriterFont {

/**
 * Writes the font entry to the pdf buffer.
 */
public static void writeFontEntry(PDFWriter aWriter, PDFFontEntry aFontEntry)
{
    if(aFontEntry.getCharSet()>0) { 
        writeExtendedFont(aWriter, aFontEntry); return; }
    
    // Get pdf xref table and pdf buffer
    PDFXTable xref = aWriter.getXRefTable();

    // Get font file and font entry
    FontFile fontFile = aFontEntry.getFont().getFontFile();
    
    // If font is standard font, just declare BaseFont and return
    if(isStandardFont(fontFile)) {
        aWriter.appendln("<< /Type /Font /Subtype /TrueType /Name /" + aFontEntry.getPDFName());
        aWriter.appendln("/BaseFont /" + getStandardFontName(fontFile));
        aWriter.appendln("/Encoding /WinAnsiEncoding");
        aWriter.append(">>");
        return;
    }

    // Write font dictionary basics
    aWriter.appendln("<< /Type /Font /Subtype /Type3 /Name /" + aFontEntry.getPDFName());

    // Write FontBBox and FontMatrix
    int y = (int)(-fontFile.getDescent()*1000);
    int w = (int)(fontFile.getMaxAdvance()*1000);
    int h = (int)(fontFile.getLineHeight()*1000);
    aWriter.appendln(" /FontBBox [0 " + y + " " + w + " " + h + "]");
    aWriter.appendln(" /FontMatrix [0.001 0 0 0.001 0 0]");
    
    // Write "CharProcs" operator and dictionary opener
    aWriter.append(" /CharProcs << ");
    
    // Create string buffer for char proc references
    StringBuffer encoding = new StringBuffer();
    
    // Declare variables for first and last chars present in font
    int firstChar = -1, lastChar = -1;
    
    // Declare variable for whether last char was missing
    boolean gap = true;
    
    // Iterate over char array
    for(int i=0; i<256; i++) {
        
        // If char at index is present, write glyph for it and add to encoding string
        if(aFontEntry._chars[i]==true) {
            
            // Write char proc for char
            String charProcRef = xref.addObject(charProcStreamForChar(fontFile, (char)i));
            
            // Write reference for char
            aWriter.append('/').append(i).append(' ').append(charProcRef).append(' ');
            
            // If coming off a gap, write current index
            if(gap) {
                encoding.append(i);
                encoding.append(' ');
                gap = false;
            }
            
            // Write char index
            encoding.append('/'); encoding.append(i); encoding.append(' ');
            
            // Set first char/last char (first char only if uninitialized)
            if(firstChar<0) firstChar = i;
            lastChar = i;
        }
        
        // If char at index not present, set gap variable
        else gap = true;
    }
    
    // Remove trailing space from encoding
    if(encoding.length()>0) encoding.deleteCharAt(encoding.length()-1);
    
    // Close char procs dictionary
    aWriter.appendln(">>");

    // Write Encoding
    aWriter.appendln(" /Encoding << /Type /Encoding /Differences [" + encoding + "] >>");
    
    // Write FirstChar/LastChar/Widths
    aWriter.appendln(" /FirstChar " + firstChar);
    aWriter.appendln(" /LastChar " + lastChar);
    
    // Write widths
    aWriter.append(" /Widths [");
    for(int i=firstChar; i<=lastChar; i++) {
        if(aFontEntry._chars[i]==true)
            aWriter.append(fontFile.charAdvance((char)i)*1000);
        else aWriter.append('0');
        aWriter.append(' ');
    }
    aWriter.appendln("]");
    
    // Close encoding
    aWriter.append(">>");
}

/**
 * Writes the given font char set to the given pdf file.
 */
public static void writeExtendedFont(PDFWriter aWriter, PDFFontEntry aFontEntry)
{
    // Get pdf xref table and pdf buffer
    PDFXTable xref = aWriter.getXRefTable();

    // Get font file and font entry
    FontFile fontFile = aFontEntry.getFont().getFontFile();
    
    // Write font dictionary basics
    aWriter.appendln("<< /Type /Font /Subtype /Type3 /Name /" + aFontEntry.getPDFName());

    // Write FontBBox and FontMatrix
    int y = (int)(-fontFile.getDescent()*1000);
    int w = (int)(fontFile.getMaxAdvance()*1000);
    int h = (int)(fontFile.getLineHeight()*1000);
    aWriter.appendln(" /FontBBox [0 " + y + " " + w + " " + h + "]");
    aWriter.appendln(" /FontMatrix [0.001 0 0 0.001 0 0]");
    
    // Create CharProcs (and encoding and width) and print
    aWriter.append(" /CharProcs << ");
    StringBuffer encoding = new StringBuffer("0 ");
    int lastChar = -1;
    for(int i=0, iMax=aFontEntry.getCharCount(); i<iMax; i++) {
        char c = aFontEntry.getChar(i);
        if(c==0)
            break;

        String charProcRef = xref.addObject(charProcStreamForChar(fontFile, c));
        aWriter.append('/').append(i).append(' ').append(charProcRef).append(' ');
        encoding.append('/').append(i).append(' ');
        lastChar = i;
    }
    if(encoding.length()>0) encoding.deleteCharAt(encoding.length()-1);
    aWriter.appendln(">>");
    
    // Write Encoding
    aWriter.appendln(" /Encoding << /Type /Encoding /Differences [" + encoding + "] >>");
    
    // Write FirstChar & LastChar
    aWriter.appendln(" /FirstChar 0");
    aWriter.appendln(" /LastChar " + lastChar);
    
    // Write widths
    aWriter.append(" /Widths [");
    for(int i=0, iMax=aFontEntry.getCharCount(); i<iMax; i++) {
        char c = aFontEntry.getChar(i);
        if(c==0)
            break;
        aWriter.append(fontFile.charAdvance(c)*1000);
        aWriter.append(' ');
    }
    aWriter.appendln("]");

    // Close encoding
    aWriter.append(">>");
}

/**
 * Returns a pdf stream buffer with given char written as a char proc.
 */
public static PDFStream charProcStreamForChar(FontFile fontFile, char aChar)
{
    // Get outline of given char
    Shape path = fontFile.getCharPath(aChar);
    
    // Create buffer for char proc
    PDFWriterBase buffer = new PDFWriterBase();
    
    // Write glyph width, height, bbox
    Rect bounds = path.getBounds();
    buffer.append(bounds.getWidth()).append(' ').append(bounds.getHeight()).append(' ');
    buffer.append(bounds.getX()).append(' ').append(bounds.getY()).append(' ');
    buffer.append(bounds.getMaxX()).append(' ').append(bounds.getMaxY()).appendln(" d1");

    // Iterate over path segments
    PathIter piter = path.getPathIter(null); double p[] = new double[6], lastX = 0, lastY = 0;
    while(piter.hasNext()) switch(piter.getNext(p)) {
        
        // Handle MoveTo, LineTo
        case MoveTo: buffer.moveTo(Math.round(p[0]), Math.round(p[1])); lastX = p[0]; lastY = p[1]; break;
        case LineTo: buffer.lineTo(Math.round(p[0]), Math.round(p[1])); lastX = p[0]; lastY = p[1]; break;
        
        // Handle QuadTo
        case QuadTo: {
            
            // Convert quad control point (in conjunction with path last point) to cubic bezier control points
            double cp1x = (lastX + 2*p[0])/3, cp1y = (lastY + 2*p[1])/3;
            double cp2x = (2*p[0] + p[2])/3, cp2y = (2*p[1] + p[3])/3;
            buffer.curveTo(Math.round(cp1x), Math.round(cp1y), Math.round(cp2x),
                Math.round(cp2y), Math.round(p[2]), Math.round(p[3]));
                
            // Update last point and break
            lastX = p[2]; lastY = p[3]; break;
        }
            
        // Handle CubicTo
        case CubicTo: {
            buffer.curveTo(Math.round(p[0]), Math.round(p[1]),
                Math.round(p[2]), Math.round(p[3]), Math.round(p[4]), Math.round(p[5]));
            lastX = p[4]; lastY = p[5]; break;
        }
            
        // Handle Close
        case Close: buffer.appendln("h"); break;
    }
    
    // Append font char and return char proc stream
    buffer.append('f');
    return new PDFStream(buffer.toByteArray(), null);
}

/**
 * Returns whether the given font is one of the standard PDF fonts.
 * All PDF readers are guaranteed to have the following standard fonts.
 */
static boolean isStandardFont(FontFile fontFile)
{
    String name = getStandardFontNameSanitized(fontFile.getPSName());
    return ArrayUtils.contains(_pdfBuiltIns, name);
}

/**
 * Returns the standard font name for any variant of a standard font name.
 */
static String getStandardFontName(FontFile fontFile)
{
    String name = getStandardFontNameSanitized(fontFile.getPSName());
    int index = Math.max(0, ArrayUtils.indexOf(_pdfBuiltIns, name));
    return _pdfBuiltIns2[index];
}

/**
 * Strips any bogus stuff from a standard font name.
 */
static String getStandardFontNameSanitized(String aName)
{
    aName = StringUtils.replace(aName, "New", "");
    aName = StringUtils.replace(aName, "Neue", "");
    aName = StringUtils.replace(aName, "Plain", "");
    aName = StringUtils.replace(aName, "Roman", "");
    aName = StringUtils.replace(aName, "MT", "");
    aName = StringUtils.replace(aName, "PS", "");
    aName = StringUtils.replace(aName, "Oblique", "Italic");
    aName = StringUtils.replace(aName, " ", "");
    aName = StringUtils.replace(aName, "-", "");
    return aName;
}

/**
 * Holds a list of all the PDF built in font name variants.
 */
protected static final String _pdfBuiltIns[] =
{
    "Arial", "ArialBold", "ArialItalic", "ArialBoldItalic",
    "Helvetica", "HelveticaBold", "HelveticaItalic", "HelveticaBoldItalic",
    "Times", "TimesBold", "TimesItalic", "TimesBoldItalic",
    "Courier", "CourierBold", "CourierItalic", "CourierBoldItalic",
    "Symbol", "ZapfDingbats"
};

/**
 * Holds a list of all the PDF build font names.
 */
protected static final String _pdfBuiltIns2[] =
{
    "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
    "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
    "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic",
    "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
    "Symbol", "ZapfDingbats"
};

}