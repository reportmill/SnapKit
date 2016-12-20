/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import snap.gfx.*;
import java.io.*;

/**
 * This class is like a StringBuffer, but for creating PDF files instead of strings.
 */
public class PDFWriterBase {
    
    // This is the actual byte buffer
    protected ByteArrayOutputStream     _source = new ByteArrayOutputStream();
    
/**
 * Returns the current length of the buffer.
 */
public int length()  { return _source.size(); }

/**
 * Adds a character to the pdf buffer.
 */
public PDFWriterBase append(char aChar)
{
    // Assert that char is within ascii range
    if(aChar>255) {
        System.err.println("PDFWriterBase: Attempt to write non-ASCII char: " + aChar); aChar = 'X'; }
    
    // Write char and return
    _source.write(aChar); return this;
}

/**
 * Adds a string to the pdf buffer.
 */
public PDFWriterBase append(String aStr)
{
    for(int i=0, iMax=aStr.length(); i<iMax; i++) append(aStr.charAt(i));
    return this;
}

/**
 * Adds an int to the pdf buffer.
 */
public PDFWriterBase append(int anInt)  { return anInt<0? append('-').append(-anInt) : appendDigits(anInt, 0); }

/**
 * Adds a double and newline to the pdf buffer.
 */
public PDFWriterBase append(double aDouble)
{
    // If value less than zero, add negative sign and negate
    if(aDouble<0)
        return append('-').append(-aDouble);

    // Get integer portion and fraction portion of number
    int whole = (int)aDouble;
    int fraction = (int)((aDouble - whole)*1000);
    
    // Append integer portion and, if fraction is non-zero, append decimal point and fraction
    return fraction==0? append(whole) : append(whole).append('.').appendDigits(fraction, 3);
}

/**
 * Appends digits of a whole number (recursively), padded by zeros on the left to the given number of pad digits.
 */
private PDFWriterBase appendDigits(int anInt, int aPad)
{
    char digit = (char)('0' + anInt%10);
    if(anInt>9 || aPad>1) appendDigits(anInt/10, aPad-1);
    return append(digit);
}

/**
 * Writes a color.
 */
public PDFWriterBase append(Color aColor)
{
    append('[');
    append(aColor.getRed()).append(' ');
    append(aColor.getGreen()).append(' ');
    append(aColor.getBlue());
    return append(']');
}

/**
 * Writes a rect to the pdf buffer.
 */
public PDFWriterBase append(Rect aRect)
{
    append('[');
    append((int)aRect.x); append(' '); append((int)aRect.y); append(' ');
    append((int)aRect.getMaxX()); append(' '); append((int)aRect.getMaxY());
    return append(']');
}

/**
 * Appends an arbitrary byte array.
 */    
public PDFWriterBase append(byte theBytes[])  { return append(theBytes, 0, theBytes.length); }

/**
 * Appends an arbitrary byte array with the given offset and length.
 */ 
public PDFWriterBase append(byte theBytes[], int anOffset, int aLen)
{
    _source.write(theBytes, anOffset, aLen); return this;
}

/**
 * Appends another buffer.
 */
public PDFWriterBase append(PDFWriterBase aBuffer)  { return append(aBuffer.toByteArray()); }

/**
 * Adds a newline to the pdf buffer.
 */
public PDFWriterBase appendln()  { return append('\n'); }

/**
 * Adds a string and newline to the pdf buffer.
 */
public PDFWriterBase appendln(String aString)  { return append(aString).appendln(); }

/**
 * Adds a string object ( a string enclosed in parentheses ) to the buffer.
 * All chars above the seven bit range are represented by an octal version of the form '\ddd'.
 * The characters '(', ')' and '\' are escaped with backslash.
 */
public void writePDFString(String aStr)
{
    // Assert that we were given PDF string
    if(!aStr.startsWith("(") || !aStr.endsWith(")"))
        throw new RuntimeException("PDFWriterBase: Internal error - printPDFString called with non-string object");
    
    // Write string start char
    append('(');
    
    // Iterate over inside string chars
    for(int i=1, iMax=aStr.length()-1; i<iMax; i++) { char c = aStr.charAt(i);
        
        // If char outside seven bit ascii range, have to octal escape
        if(c>127) {
            _source.write('\\');
            char c3 = (char)('0' + c%8);  c/=8;
            char c2 = (char)('0' + c%8);  c/=8;
            char c1 = (char)('0' + c%8);
            _source.write(c1); _source.write(c2); _source.write(c3);
        }
        
        // Handle special chars
        else if(c=='(' || c==')' || c=='\\')
            append('\\').append(c);
        
        // Handle everything else
        else _source.write(c);
    }
    
    // Write string end char
    append(')');
}

/**
 * Writes a transform to pdf buffer.
 */
public void writeTransform(Transform aTrans)
{
    double m[] = aTrans.getMatrix(); writeTransform(m[0], m[1], m[2], m[3], m[4], m[5]);
}

/**
 * Writes a transform to pdf buffer.
 */
public void writeTransform(double a, double b, double c, double d, double tx, double ty)
{
    append(a).append(' ').append(b).append(' ');
    append(c).append(' ').append(d).append(' ');
    append(tx).append(' ').append(ty).appendln(" cm");
}

/**
 * Writes a given path to PDF file.
 */
public void writePath(Shape aShape)
{
    // Iterate over shape segments
    PathIter pathIter = aShape.getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, sx = 0, sy = 0;
    while(pathIter.hasNext()) {
        
        // Handle path segment types
        switch(pathIter.getNext(pts)) {
            case MoveTo: moveTo(sx=lx=pts[0], sy=ly=pts[1]); break;
            case LineTo: lineTo(lx=pts[0], ly=pts[1]); break;
            case QuadTo: quadTo(lx, ly, pts[0], pts[1], lx=pts[2], ly=pts[3]); break;
            case CubicTo: curveTo(pts[0], pts[1], pts[2], pts[3], lx=pts[4], ly=pts[5]); break;
            case Close: appendln("h"); lx = sx; ly = sy; break;
        }
    }
}

/**
 * Writes a moveto operator.
 */
public void moveTo(double x, double y)  { append(x); append(' '); append(y); appendln(" m"); }

/**
 * Writes a lineto operator.
 */
public void lineTo(double x, double y)  { append(x); append(' '); append(y); appendln(" l"); }

/**
 * Writes a quadto operator.
 */
public void quadTo(double lastX, double lastY, double x1, double y1, double x2, double y2)
{
    // Convert single control point and last point to cubic bezier control points
    double cp1x = lastX + 2.0/3*(x1 - lastX);
    double cp1y = lastY + 2.0/3*(y1 - lastY);
    double cp2x = cp1x + 1.0/3.0*(x2 - lastX);
    double cp2y = cp1y + 1.0/3.0*(y2 - lastY);
    curveTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
}

/**
 * Writes a curveto operator.
 */
public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3)
{
    append(x1); append(' '); append(y1); append(' ');
    append(x2); append(' '); append(y2); append(' ');
    append(x3); append(' '); append(y3); appendln(" c");
}

/**
 * Returns the buffer as a byte array.
 */
public byte[] toByteArray()  { return _source.toByteArray(); }

}