package snap.swing;
import java.awt.print.*;
import javax.print.*;
import snap.gfx.Painter;
import snap.geom.Size;
import snap.viewx.*;

/**
 * A snap Printer subclass that uses Swing.
 */
public class SwingPrinter extends Printer {
    
    // The Printable
    Printable         _printable;
    
    // The number of pages
    int               _pageCount;
    
    // The Painter
    Painter           _painter;

/**
 * Returns the Painter.
 */
public Painter getPainter()  { return _painter; }

/**
 * This method creates a java.awt.print.PrintJob with a java.awt.print.Book.
 */
public void printImpl(Printable aPrintable, String aPrinterName, boolean showPanel)
{
    // Set Printable and PageCount
    _printable = aPrintable;
    _pageCount = aPrintable.getPageCount(this);
    
    // Create PrinterJob and book
    PrinterJob job = PrinterJob.getPrinterJob();
    Book book = getBook();
    job.setPageable(book);
    
    // If PrinterName provided, try to find service with that name
    if(aPrinterName!=null) {
        PrintService services[] = PrinterJob.lookupPrintServices(), service = null;
        for(int i=0; i<services.length; i++)
            if(aPrinterName.equals(services[i].getName()))
                service = services[i];
        if(service!=null)
            try { job.setPrintService(service); }
            catch(Exception e) { e.printStackTrace(); service = null; }
        if(service==null) {
            System.err.println("RMViewer:Print: Couldn't find printer named " + aPrinterName);
            System.err.println("Available Services:");
            for(int i=0; i<services.length; i++)
                System.err.println("\t- " + services[i].getName());
            return;
        }
    }
    
    // Flip the orientation if printer has funny definition of portrait/landscape
    Size psize = aPrintable.getPageSize(this, 0);
    int orient = psize.height>=psize.width? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
    PageFormat pf = job.defaultPage();
    if(pf.getOrientation()==PageFormat.PORTRAIT && pf.getWidth()>pf.getHeight() ||
        pf.getOrientation()==PageFormat.LANDSCAPE && pf.getHeight()>pf.getWidth()) {
        orient = orient==PageFormat.PORTRAIT? PageFormat.LANDSCAPE : PageFormat.PORTRAIT;
        book.getPageFormat(0).setOrientation(orient);
    }
    
    // Run printDialog, and if successful, execute print
    boolean shouldPrint = !showPanel || job.printDialog();
    try { if(shouldPrint) job.print(); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Returns a java.awt.print.Book, suitable for AWT printing.
 */
public Book getBook()
{
    // Get document, generic viewer printable and book
    java.awt.print.Printable printable = new AWTPrintable();
    Book book = new Book();
    
    // Iterate over pages and add to book
    for(int i=0, iMax=_pageCount; i<iMax; i++) {
    
	    // Get doc width, height and orientation
        Size psize = _printable.getPageSize(this, i);
	    double width = psize.getWidth(), height = psize.getHeight();
	    int orientation = PageFormat.PORTRAIT;
	    if(width>height) {
	        orientation = PageFormat.LANDSCAPE; width = height; height = psize.getWidth(); }
	    
	    // Get paper and configure with appropriate paper size and imageable area
	    Paper paper = new Paper();
	    paper.setSize(width, height);
	    paper.setImageableArea(0, 0, width, height);
	
	    // Get pageFormat and configure with appropriate orientation and paper
	    PageFormat pageFormat = new PageFormat();
	    pageFormat.setOrientation(orientation);
	    pageFormat.setPaper(paper);
	    
	    // Appends page to book
	    book.append(printable, pageFormat);
    }

    // Return book
    return book;
}

/**
 * This class simply tells the Snap Printable to paint to a given Graphics object for specific page index.
 */
protected class AWTPrintable implements java.awt.print.Printable {

    /** Print method. */
    public int print(java.awt.Graphics aGr, PageFormat pageFormat, int pageIndex)
    {
        // If bogus range, bail
        if(pageIndex>=_pageCount) return java.awt.print.Printable.NO_SUCH_PAGE;
        
        // Get page at index, get/configure shape painter, paint shape, return success
        _painter = new J2DPainter(aGr); _painter.setPrinting(true);
        _printable.print(SwingPrinter.this, pageIndex);
        return java.awt.print.Printable.PAGE_EXISTS; // Return success
    }
}

/** Sets a SwingPrinter to be Printer.Master. */
public static void set()  { _master = new SwingPrinter(); }

}