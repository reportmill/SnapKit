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

    /** Writes a given View hierarchy to a PDF file (recursively). */
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

    /** Override to write Image. */
    protected void writeShape(T anImageView, PDFWriter aWriter)
    {
        // Do normal version
        super.writeShape(anImageView, aWriter);
        
        // Get page writer, image and image bounds (just return if missing or invalid)
        PDFPageWriter pwriter = aWriter.getPageWriter();
        Image image = anImageView.getImage(); if(image==null) return;
        Rect bnds = anImageView.getImageBounds();
        
        // Apply clip if needed
        /*if(anImageView.getRadius()>.001) {
            Shape path = anImageView.getPath(); pwriter.writePath(path); pwriter.append(" re W n "); }*/
            
        pwriter.writeImage(image, bnds.x, bnds.y, bnds.width, bnds.height);
    }
}

/**
 * This ViewPdfr subclass writes PDF for PageView.
 */
public static class SnapPageViewPdfr <T extends PageView> extends SnapViewPdfr <T> {

    /** Writes a given View hierarchy to a PDF file (recursively). */
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