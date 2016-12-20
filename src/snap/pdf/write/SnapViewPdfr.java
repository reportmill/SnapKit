/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import snap.gfx.*;
import snap.pdf.PDFWriter;
import snap.view.*;

/**
 * This RMObjectPdfr subclass writes PDF for RMShape.
 */
public class SnapViewPdfr <T extends View> {

    // Shared SnapViewPdfr
    static SnapViewPdfr    _viewPdfr = new SnapViewPdfr();

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
public void writePDF(T aShape, PDFWriter aWriter)
{
    // Write shape
    writeShapeBefore(aShape, aWriter);
    
    // If shape has effect, forward to it
    if(aShape.getEffect()!=null) SnapEffectPdfr.writeShapeEffect(aShape, aWriter);
    
    // Otherwise, do basic write shape all
    else writeShapeAll(aShape, aWriter);
    
    // Write shape after children
    writeShapeAfter(aShape, aWriter);    
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeBefore(T aShape, PDFWriter aWriter)
{
    // Get page
    PDFPageWriter pdfPage = aWriter.getPageWriter();
    
    // Save the graphics transform
    pdfPage.gsave();
        
    // If not rotated/scaled, write simple translation matrix
    if(aShape.isLocalToParentSimple())
        pdfPage.append("1 0 0 1 ").append(aShape.getX()).append(' ').append(aShape.getY()).appendln(" cm");
    
    // If rotated/scaled, write full transform
    else pdfPage.writeTransform(aShape.getLocalToParent());
}
    
/**
 * Writes the shape and then the shape's children.
 */
protected void writeShapeAll(T aShape, PDFWriter aWriter)
{
    // Write shape fills
    writeShape(aShape, aWriter);
    
    // Write shape children
    writeShapeChildren(aShape, aWriter);
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShape(T aShape, PDFWriter aWriter)
{
    // Get pdf page
    PDFPageWriter pdfPage = aWriter.getPageWriter();
    
    // Set shape opacity
    pdfPage.setOpacity(aShape.getOpacityAll(), PDFPageWriter.OPACITY_SHAPE);
    
    // Clip to bounds???
    //pageBuffer.print(aShape.getBoundsInside()); pageBuffer.println(" re W n"));
        
    // Get fill and write pdf if not null
    Paint fill = aShape.getFill();
    if(fill!=null)
        SnapPaintPdfr.writeShapeFill(aShape, fill, aWriter);
    
    // Get stroke and write pdf if not null
    Border stroke = aShape.getBorder();
    if(stroke!=null)
        SnapPaintPdfr.writeShapeStroke(aShape, stroke, aWriter);
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeChildren(View aView, PDFWriter aWriter)
{
    // Write children
    ParentView pview = aView instanceof ParentView? (ParentView)aView : null; if(pview==null) return;
    for(int i=0, iMax=pview.getChildCount(); i<iMax; i++) { View child = pview.getChild(i);
        if(child.isVisible())
            getPdfr(child).writePDF(child, aWriter);
    }
}
    
/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeAfter(T aShape, PDFWriter aWriter)
{
    // Get pdf page
    PDFPageWriter pdfPage = aWriter.getPageWriter();
    
    // Restore graphics state (with hack since RMPagePdfr doesn't do a gsave and unbalances things)
    if(pdfPage._gstack.getStackSize()>1)
        pdfPage.grestore();

    // Add link, if it's there (What happens with rotated or skewed shapes?)
    /*if(aShape.getURL() != null) {
        RMRect frame = aShape.getBoundsInside(); aShape.convertRectToShape(frame, null);
        frame.setY(aShape.getPageShape().getHeight() - frame.getMaxY());
        PDFAnnotation link = new PDFAnnotation.Link(frame, aShape.getURL());
        pdfPage.addAnnotation(link);
    }*/
}

/**
 * Returns the shape pdfr for a shape.
 */
public static SnapViewPdfr getPdfr(View aView)
{
    if(aView instanceof TextView)
        return SnapViewPdfrs._textViewPdfr;
    if(aView instanceof ImageView)
        return SnapViewPdfrs._imgViewPdfr;
    if(aView instanceof PageView)
        return SnapViewPdfrs._pageViewPdfr;
    return _viewPdfr;
}

}