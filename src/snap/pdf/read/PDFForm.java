/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import snap.gfx.Rect;
import snap.gfx.Transform;
import snap.pdf.*;

/**
 * An object which holds on to a stream of markup operations for later drawing. In PDF a form xobject is a stream.
 * This object saves the stream data and when it comes time to draw, the PagePainter will parse the stream. The tokens
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
 * Returns list of tokens that defines this form. The PDFPagePainter is used to parse the stream the first time around.
 */
public List <PageToken> getTokens()  { return _tokens!=null? _tokens : (_tokens=PageToken.getTokens(_streamBytes)); }

/**
 * Returns the stream data.  The tokens maintain pointers into this byte array for all string storage.
 */
public byte[] getBytes() { return _streamBytes; }

/**
 * The form space->user space transform, from the Form's Matrix entry.
 */
public Transform getTransform() 
{
    Transform xform = PDFDictUtils.getTransform(_formDict,null,"Matrix");
    if(xform==null) xform = new Transform(); // Matrix is optional - default is identity
    return xform;
}

/**
 * The Form bounding box (in form space).
 */
public Rect getBBox() 
{
    Rect r = PDFDictUtils.getRect(_formDict, null, "BBox");
    if(r==null) throw new PDFException("Error reading form bbox");
      
    // Make sure form bboxes always have positive widths & heights
    double w = r.getWidth(), h = r.getHeight();
    if(w<0 || h<0) { r.x += (w<0? w : 0); r.y += (h<0? h : 0); r.width = Math.abs(w); r.height = Math.abs(h); }
    return r;
}

/** The form's resources dictionary */
public Map getResources(PDFFile srcfile)  { return (Map)srcfile.getXRefObj(_formDict.get("Resources")); }

}