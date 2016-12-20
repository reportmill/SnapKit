/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.ColorSpace;
import java.util.*;

/** 
 * A java.awt.color.ColorSpace subclass to represent a pdf /Separation colorspace.
 * This is just a subclass of PDFDeviceNColorSpace with a single colorant.
 */
public class PDFSeparationColorSpace extends PDFDeviceNColorSpace {

public PDFSeparationColorSpace(String name, ColorSpace altspace, PDFFunction tinttrans) 
{
    super(Collections.singletonList(name), altspace, tinttrans, null);
}

}