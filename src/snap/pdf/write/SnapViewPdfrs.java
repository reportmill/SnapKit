package snap.pdf.write;
import snap.gfx.*;
import snap.pdf.*;
import snap.view.*;

/**
 * PDF helpers for various view classes.
 */
public class SnapViewPdfrs {

    // SnapTextViewPdfr
    static SnapTextViewPdfr     _textViewPdfr = new SnapTextViewPdfr();

    // SnapImageViewPdfr
    static SnapImageViewPdfr    _imgViewPdfr = new SnapImageViewPdfr();

    // SnapPageViewPdfr
    static SnapPageViewPdfr     _pageViewPdfr = new SnapPageViewPdfr();

/**
 * This class generates PDF for an TextView.
 */
public static class SnapTextViewPdfr <T extends TextView> extends SnapViewPdfr<T> {

    /** Writes a given RMShape hierarchy to a PDF file (recursively). */
    protected void writeShape(T aTextShape, PDFWriter aWriter)
    {
        super.writeShape(aTextShape, aWriter);
        PDFWriterText.writeText(aWriter, aTextShape.getTextBox());
    }
}

/**
 * PDF writer for ImageView.
 */
public static class SnapImageViewPdfr <T extends ImageView> extends SnapViewPdfr <T> {

    /** Override to write ImageData. */
    protected void writeShape(T anImageShape, PDFWriter aWriter)
    {
        // Do normal version
        super.writeShape(anImageShape, aWriter);
        
        // Get image fill and image data (just return if missing or invalid)
        Image idata = anImageShape.getImage(); //if(idata==null || !idata.isValid()) return;
        
        // Get whether image fill is for pdf image (and just return if no page contents - which is apparently legal)
        //boolean pdfImage = idata instanceof RMImageDataPDF;
        /*if(pdfImage) {
            RMImageDataPDF pdata = (RMImageDataPDF)idata;
            if(pdata.getPDFFile().getPage(idata.getPageIndex()).getPageContentsStream()==null)
                return;
        }*/
    
        // Add image data
        aWriter.addImageData(idata);
    
        // Get PDF page
        PDFPageWriter pdfPage = aWriter.getPageWriter();
        
        // Gsave
        pdfPage.gsave();
        
        // If pdf image, reset gstate defaults
        //if(pdfImage) {
        //    pdfPage.setLineCap(0);
        //    pdfPage.setLineJoin(0);
        //}
        
        // Apply clip if needed
        /*if(anImageShape.getRadius()>.001) {
            Shape path = anImageShape.getPath();
            pdfPage.writePath(path); pdfPage.append(" re W n ");
        }*/
        
        // Get image bounds width and height
        Rect bounds = anImageShape.getBounds(); //anImageShape.getImageBounds();
        double width = bounds.getWidth(), height = bounds.getHeight();
    
        // pdfImage writes out scale of imageBounds/imageSize
        /*if(pdfImage) {
            width /= idata.getImageWidth();
            height /= idata.getImageHeight();
        }*/
    
        // Apply CTM - image coords are flipped from page coords ( (0,0) at upper-left )
        pdfPage.writeTransform(width, 0, 0, -height, bounds.getX(), bounds.getMaxY());
        
        // Do image
        pdfPage.appendln("/" + aWriter.getImageName(idata) + " Do");
            
        // Grestore
        pdfPage.grestore();
        
        // If image has alpha, declare output to be PDF-1.4
        //if(idata.hasAlpha() && idata.getSamplesPerPixel()==4)
        //    aWriter.setVersion(1.4f);
    }
}

/**
 * This ViewPdfr subclass writes PDF for PageView.
 */
public static class SnapPageViewPdfr <T extends PageView> extends SnapViewPdfr <T> {

    /** Writes a given RMShape hierarchy to a PDF file (recursively). */
    protected void writeShapeBefore(T aPageShape, PDFWriter aWriter)
    {
        // Get pdf page
        PDFPageWriter pdfPage = aWriter.getPageWriter();
        
        // Write page header comment
        int page = 1; //aPageShape.page();
        pdfPage.appendln("\n% ------ page " + (page - 1) + " -----");
            
        // legacy defaults different from pdf defaults
        pdfPage.setLineCap(1);
        pdfPage.setLineJoin(1);
        
        // Flip coords to match java2d model
        pdfPage.append("1 0 0 -1 0 ").append(aPageShape.getHeight()).appendln(" cm");    
    }
}

}