/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import java.awt.geom.*;
import snap.pdf.*;

/**
 * An object which holds on to a stream of markup operations for later drawing. In PDF a form xobject is a stream.
 * This object saves the stream data and when it comes time to draw, the PageParser will parse the stream. The tokens
 * returned by the lexing portion of the parsing will be held on to by the PDFForm so that the markup operations can be
 * executed over and over without repeating that step.
 * This method can be used by form xobjects as well as pattern colorspaces.
 */
public class PDFForm {
    
    // The stream bytes
    byte     _streamBytes[];
    
    // The form dictionary
    Map      _formDict;
    
    // The tokens
    List <PageToken>     _tokens;
    
/**
 * Creates a PDFForm for given PDFStream.
 */
public PDFForm(PDFStream aStream)
{
    _streamBytes = aStream.decodeStream();
    _formDict = aStream.getDict();
}

/**
 * Returns list of tokens that defines this form. The PDFPageParser is used to parse the stream the first time around.
 */
public List <PageToken> getTokens()  { return _tokens!=null? _tokens : (_tokens=PageToken.getTokens(_streamBytes)); }

/**
 * Returns the stream data.  The tokens maintain pointers into this byte array for all string storage.
 */
public byte[] getBytes() { return _streamBytes; }

/**
 * The form space->user space transform, from the Form's Matrix entry.
 */
public AffineTransform getTransform() 
{
    // Matrix is optional - default is identity
    AffineTransform xform = PDFDictUtils.getTransform(_formDict,null,"Matrix");
    if(xform==null)
        xform = new AffineTransform();
    return xform;
}

/**
 * The Form bounding box (in form space).
 */
public Rectangle2D getBBox() 
{
    Rectangle2D r = PDFDictUtils.getRectangle(_formDict, null, "BBox");
    if(r==null)
        throw new PDFException("Error reading form bbox");
      
    // Make sure form bboxes always have positive widths & heights (new GeneralPath(box) doesn't like negatives)
    double w = r.getWidth();
    double h = r.getHeight();
    if(w<0 || h<0) {
        double x = r.getX() + (w<0 ? w : 0);
        double y = r.getY() + (h<0 ? h : 0);
        w = Math.abs(w);
        h = Math.abs(h);
        r.setRect(x,y,w,h);
    }
    return r;
}

/** The form's resources dictionary */
public Map getResources(PDFFile srcfile)  { return (Map)srcfile.getXRefObj(_formDict.get("Resources")); }

}