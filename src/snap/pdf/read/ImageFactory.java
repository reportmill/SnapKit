/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Image;
import java.awt.color.ColorSpace;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * A class implementing the ImageFactory interface knows how to create
 * java.awt.Image classes for the various formats supported by PDF.
 */
public interface ImageFactory {

/** Given a pdf xobject dictionary, return an awt Image object */
public Image getImage(PDFStream imageDict, ColorSpace cspace, PDFFile srcfile);

}
