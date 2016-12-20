/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;

/**
 * PDFException.java 
 * Subclass of Error because declaring every method
 * as throwing an exception seems stupid.
 */
public class PDFException extends Error {

    public PDFException() { }
    
    public PDFException(String message) { super(message); }
    
    public PDFException(String message, Throwable cause) { super(message, cause); }
    
    public PDFException(Throwable cause) { super(cause); }
}
