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
    protected void writeView(T aTextView, PDFWriter aWriter)
    {
        super.writeView(aTextView, aWriter);
        PDFWriterText.writeText(aWriter, aTextView.getTextBox());
    }
}

/**
 * PDF writer for ImageView.
 */
public static class SnapImageViewPdfr <T extends ImageView> extends SnapViewPdfr <T> {

    /** Override to write Image. */
    protected void writeView(T anImageView, PDFWriter aWriter)
    {
        // Do normal version
        super.writeView(anImageView, aWriter);
        
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
    protected void writeViewBefore(T aPageView, PDFWriter aWriter)
    {
        // Get pdf page
        PDFPageWriter pdfPage = aWriter.getPageWriter();
        
        // Write page header comment
        int page = 1; //aPageView.page();
        pdfPage.appendln("\n% ------ page " + (page - 1) + " -----");
            
        // legacy defaults different from pdf defaults
        pdfPage.setLineCap(1);
        pdfPage.setLineJoin(1);
        
        // Flip coords to match java2d model
        pdfPage.append("1 0 0 -1 0 ").append(aPageView.getHeight()).appendln(" cm");    
    }
    
    /** Override to suppress grestore. */
    protected void writeViewAfter(T aView, PDFWriter aWriter)  { }
}

}