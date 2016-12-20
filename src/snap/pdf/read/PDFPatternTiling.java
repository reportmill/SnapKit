/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/*
 * PDFTilingPattern.java. Created Dec 15, 2005. Copyright (c) 2005 by Joshua Doenias
 */
public class PDFPatternTiling extends PDFPattern {
  int paintType; // could be different classes, but really no point 
  int tilingType;
  Rectangle2D bounds;
  AffineTransform xform;
  float xstep, ystep;
  Map resources;
  byte pdfData[];
  TexturePaint tile;
  
public static PDFPattern getInstance(PDFStream pstream, PDFFile srcFile) { return new PDFPatternTiling(pstream, srcFile); }

public PDFPatternTiling(PDFStream pstream, PDFFile srcFile)
{
    Map pmap = pstream.getDict();
    
    paintType = PDFDictUtils.getInt(pmap, srcFile, "PaintType");
    tilingType = PDFDictUtils.getInt(pmap, srcFile, "TilingType");
    bounds = PDFDictUtils.getRectangle(pmap, srcFile, "BBox");
    xstep = PDFDictUtils.getFloat(pmap, srcFile, "XStep");
    ystep = PDFDictUtils.getFloat(pmap, srcFile, "YStep");
    xform = PDFDictUtils.getTransform(pmap, srcFile, "Matrix");
    if (xform==null)
        xform = new AffineTransform();
    
    Object obj = srcFile.getXRefObj(pmap.get("Resources"));
    if (obj instanceof Map)
        resources = (Map)obj;
    else throw new PDFException("Illegal resources dictionary in pattern");
    
    pdfData = pstream.decodeStream();
}

public AffineTransform getTransform()  { return xform; }
public Rectangle2D getBounds()  { return bounds; }
public Map getResources()  { return resources; }
public byte[] getContents()  { return pdfData; }

public void setTile(BufferedImage timage)
{
    Rectangle2D b = getBounds();
    Rectangle2D anchor = new Rectangle2D.Float((float)b.getX(), (float)b.getY(), xstep, ystep);
    tile = new TexturePaint(timage, anchor);
    // release everything else
    pdfData=null;
    resources=null;
}

public Paint getPaint()  { return tile; }

}