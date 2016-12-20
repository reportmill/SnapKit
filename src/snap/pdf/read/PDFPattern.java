/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.geom.*;
import java.awt.Paint;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * PDFPattern.java 
 * Created Dec 15, 2005
 * Copyright (c) 2005 by Joshua Doenias
 */
public abstract class PDFPattern {
    
public static PDFPattern getInstance(Object pat, PDFFile srcFile)
{
    Map pdict = pat instanceof PDFStream ? ((PDFStream)pat).getDict() : (Map)pat;
    Object v = pdict.get("PatternType");
   
    if (v instanceof Number) {
        int ptype = ((Number)v).intValue();
        switch(ptype) {
            case 1: return PDFPatternTiling.getInstance((PDFStream)pat, srcFile);
            case 2: return PDFPatternShading.getInstance(pdict, srcFile);
        }
    }
    throw new PDFException("Illegal pattern definition");
}

/** Returns the pattern space->default space transform */
public abstract AffineTransform getTransform();

/** Returns the extended gstate object (if one exists) associated with the pattern */
public Map getGState() { return null; }

/** Returns the awt Paint object which will render the pattern */
public abstract Paint getPaint();

}