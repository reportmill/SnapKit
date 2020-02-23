package snap.viewx;
import snap.geom.Size;
import snap.gfx.*;

/**
 * A class to handle printing.
 */
public class Printer {
    
    // The Master printer
    protected static Printer   _master = new Printer();
    
/**
 * Returns the painter.
 */
public Painter getPainter()  { return null; }

/**
 * Starts the print process.
 */
protected void printImpl(Printable aPrintable, String aPrinterName, boolean showPanel)  { }

/**
 * Starts the print process.
 */
public static void print(Printable aPrintable, String aPrinterName, boolean showPanel)
{
    // If no master, complain
    if(_master==null) { System.err.println("Printer.print: No print master configured for this environment"); return; }

    // Have Master printer do print
    _master.printImpl(aPrintable, aPrinterName, showPanel);
}

/**
 * An interface for an graphical object that wants to print.
 */
public interface Printable {
    
    /**
     * Returns a print page count for given printer.
     */
    public int getPageCount(Printer aPrinter);
    
    /**
     * Returns the page size for given page index.
     */
    public Size getPageSize(Printer aPrinter, int anIndex);
    
    /**
     * Executes a print for given printer and page index.
     */
    public void print(Printer aPrinter, int anIndex);
}

}