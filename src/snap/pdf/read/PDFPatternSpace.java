/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.ColorSpace;
import java.util.Map;

/**
 * The Pattern colorspace is a special colorspace where shadings and tiling patterns can be declared. PDF treats this
 * like other colorspaces, but it is not a full colorspace as far as awt is concerned.  Awt colorspaces are expected
 * to be able to convert colors between each other, but this doesn't make sense for patterns or shadings. The
 * conversion methods just generate errors, so if the colorspace is ever used in a strange place (like an image) it
 * will generate an exception, and we don't need to always be checking to see if a particular colorspace
 * is appropriate for the given operation.
 * 
 * To draw in a pattern colorspace, you can ask the colorspace for a Paint object. Classes which implement the
 * java.awt.Paint interface can be created for all the different shading types, as well as for tiling patterns.
 */
public class PDFPatternSpace extends ColorSpace {
    public Map patternDict;
    public ColorSpace tileSpace;
    
public PDFPatternSpace()  { super(TYPE_RGB, 0); }

public PDFPatternSpace(ColorSpace tspace)
{
    this();
    tileSpace=tspace;
}

public float[] toRGB(float[] colorvalue)  { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }

public float[] fromRGB(float[] rgbvalue)  { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }

public float[] toCIEXYZ(float[] clrvalue)  { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }

public float[] fromCIEXYZ(float[] clrval)  { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }

}