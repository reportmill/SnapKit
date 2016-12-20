/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;
import snap.gfx.*;
import snap.pdf.*;

/**
 * A class to write images.
 */
public class PDFWriterImage {

/**
 * Writes the PDF to embed the actual image bytes.
 */
public static void writeImage(PDFWriter aWriter, Image anImage)
{
    // Get image bits per sample and samples per pixel
    int bps = 8; //anImage.getBitsPerSample();
    int spp = anImage.getSamplesPerPixel();
    
    // Get image color space and whether image is jpg
    String colorspace = getColorSpace(anImage, aWriter);
    boolean isJPG = anImage.getType().equals("jpg");

    // Declare variable for image bytes to be encoded in PDF stream
    byte bytes[] = null;

    // Get bytes JPG: Just original file bytes
    if(isJPG)
        bytes = anImage.getBytes();
    
    // Get bytes standard formats: Just raw decoded bytes if valid format (RGB24 or Gray8)
    else if(isValidPDFImageFormat(anImage))
        bytes = anImage.getBytesRGBA();
    
    // Get bytes general: covert all else to RGB24
    else { bytes = getBytesRGB24(anImage); bps = 8; colorspace = "/DeviceRGB"; }
    
    // Create image dictionary
    Map imageDict = new Hashtable(8);
    imageDict.put("Type", "/XObject");
    imageDict.put("Subtype", "/Image");
    imageDict.put("Name", "/" + aWriter.getImageName(anImage));
    imageDict.put("Width", anImage.getWidth());
    imageDict.put("Height", anImage.getHeight());
    imageDict.put("BitsPerComponent", bps);
    imageDict.put("ColorSpace", colorspace);
    
    // If JPG CMYK, put in bogus decode entry
    if(isJPG && spp==4)
        imageDict.put("Decode", "[1 0 1 0 1 0 1 0]");

    // If indexed image with transparency, define mask color index (chromakey masking)
    if(anImage.isIndexedColor() && anImage.getAlphaColorIndex()>=0) {
        int tindex = anImage.getAlphaColorIndex();
        String tarray = "[" + tindex + " " + tindex + "]";
        imageDict.put("Mask", tarray);
    }
    
    // If image has alpha channel, create soft-mask dictionary
    else if(anImage.hasAlpha()) {
        
        // Get alpha bytes (should really do this with getBytesRGB24 above so we don't go through image bytes twice).
        byte alpha[] = getBytesAlpha8(anImage);
        if(alpha!=null) {
            
            // Create soft-mask dict with basic attributes
            Map softMask = new Hashtable();
            softMask.put("Type", "/XObject");
            softMask.put("Subtype", "/Image");
            softMask.put("Width", anImage.getWidth());
            softMask.put("Height", anImage.getHeight());
            softMask.put("BitsPerComponent", 8);
            softMask.put("ColorSpace", "/DeviceGray");
            
            // create alpha bytes stream, xref and add to parent image dict
            PDFStream smask = new PDFStream(alpha, softMask);
            String smaskXRef = aWriter.getXRefTable().addObject(smask);
            imageDict.put("SMask", smaskXRef);
        }
    }
    
    // Create stream for image with image bytes and image dict
    PDFStream istream = new PDFStream(bytes, imageDict);
    
    // If JPG, add DCTDecode filter
    if(isJPG)
        istream.addFilter("/DCTDecode");
    
    // Write stream
    aWriter.writeStream(istream);
}

/**
 * Returns whether given image data image bytes are natively in supported PDF form.
 * PDF supports 1,2,4,8 bit gray or 1,2,4,8 bit indexed or 3,6,12,24 bit rgb or 4,8,16,32 bit cmyk images.
 * Also, data should be packed - no slop bytes at row end, although bits should be padded to byte boundary.
 */
static boolean isValidPDFImageFormat(Image anImage)
{
    // Get samples per pixel, bits per sample and whether image is color
    int spp = anImage.getSamplesPerPixel(), bps = anImage.getBitsPerSample();
    boolean isColor = anImage.isColor();
    
    // If bytes aren't packed, return false
    byte bytes[] = anImage.getBytesRGBA();
    if(((bps*spp*anImage.getPixWidth()+7)/8)*anImage.getPixHeight() != bytes.length)
        return false;

    // If image is indexed with one 8-bit sample, return true
    if(anImage.isIndexedColor() && (bps==8) && (spp==1))
        return true;

    // If image is color with 3 samples, return true
    if((isColor && spp==3) && (bps==1 || bps==2 || bps==4 || bps==8))
        return true;
    
    // If image is grayscale with 1 sample, return true
    if((!isColor && spp==1) && (bps==1 || bps==2 || bps==4 || bps==8))
        return true;
    
    // All else is invalid
    return false;
}

/**
 * Returns the image data's raw image bytes as RGB24.
 */
private static byte[] getBytesRGB24(Image anImage)
{
    // Get samples per pixel, bits per sample, whether image is color and bytes
    int spp = anImage.getSamplesPerPixel(), bps = anImage.getBitsPerSample();
    boolean isColor = anImage.isColor();
    byte bytes[] = anImage.getBytesRGBA();
    
    // Only conversion we do is to blow off alpha in 32bit rgba images.
    if(isColor && spp==4 && bps==8) {
        
        // Get image width, height and bytes per row
        int width = anImage.getPixWidth(), rows = anImage.getPixHeight();
        int bpr = anImage.getBytesPerRow(), dstoff = 0;
        
        // Create byte array for RGB bytes
        byte rgb[] = new byte[rows*3*width];
        
        // Iterate over image rows
        for(int i=0; i<rows; i++) {
            
            // Calculate offset to this row
            int srcoff = i*bpr;
            
            // Iterate over image row bytes
            for(int j=0; j<width; j++) {
                rgb[dstoff++] = bytes[srcoff++];
                rgb[dstoff++] = bytes[srcoff++];        
                rgb[dstoff++] = bytes[srcoff++];
                srcoff++;
            }
        }
        
        // Return RGB bytes
        return rgb;
    }
    
    // If image conversion not supported, complain
    System.err.println("Unknown image format :\n\tBitsPerSample = " + bps + "\n\tSamplesPerPixel = " + spp +
        "\n\tisColor = " + isColor + "\n\tWidth = " + anImage.getWidth() + "\n\tHeight = " + anImage.getHeight() +
        "\n\tLength = " + bytes.length);
    
    // If image conversion not supported, return null
    return null;
}

/**
 * Returns the image data's raw alpha bytes as byte array.
 */
private static byte[] getBytesAlpha8(Image anImage)
{
    // Get samples per pixel, bits per sample, whether image is color and bytes
    int spp = anImage.getSamplesPerPixel(), bps = anImage.getBitsPerSample();
    boolean isColor = anImage.isColor();
    byte bytes[] = anImage.getBytesRGBA();
    
    // Only conversion we do is to extract alpha from 32bit rgba images.
    if(isColor && spp==4 && bps==8) {
        
        // Get image width, height and bytes per row
        int width = anImage.getPixWidth(), rows = anImage.getPixHeight();
        int bpr = anImage.getBytesPerRow(), dstoff = 0;
        
        // Create byte array for alpha bytes
        byte alpha[] = new byte[rows*width];
        
        // Keep track of whether alpha is actually used.
        boolean allOpaque = true;
        
        // Iterate over image rows - assumption is that pixel format is rgba.
        for(int i=0; i<rows; i++) {
            
            // Calculate offset to this row
            int srcoff = i*bpr;
            
            // Iterate over image row bytes
            for(int j=0; j<width; j++) {
                srcoff += 3;
                alpha[dstoff] = bytes[srcoff++];
                if (alpha[dstoff] != -1) 
                    allOpaque=false;
                ++dstoff;
            }
        }
        
        // Return alpha bytes, but only if there's useful information in them
        return allOpaque ? null : alpha;
    }
    
    // If image conversion not supported, complain
    System.err.println("Unknown image format :\n\tbitsPerSample = " + bps + "\n\tsamplesPerPixel = " + spp +
        "\n\tisColor = " + isColor + "\n\tWidth = " + anImage.getWidth() + "\n\tHeight = " + anImage.getHeight() +
        "\n\tdataLength = " + bytes.length);
    
    // If image conversion not supported, return null
    return null;
}

/**
 * Returns the pdf colorspace string for a given image data.
 */
public static String getColorSpace(Image anImage, PDFWriter aWriter)
{
    // If image is gray scale, return /DeviceGray
    if(!anImage.isColor())
        return "/DeviceGray";

    // If image has color map, return map
    if(anImage.isIndexedColor()) {
        
        // Get color map bytes and number of entries
        byte map[] = anImage.getColorMap();
        int entries = 1<<anImage.getBitsPerPixel();
        
        // Error check for bad color map size
        if(entries*3 != map.length) {
            System.err.println("Image has bad color map size"); return null; }
      
        // Create color map bytes stream
        PDFStream colorMapBytesStream = new PDFStream(map, null);
        
        // Add color map bytes stream to PDF file
        String mapref = aWriter.getXRefTable().addObject(colorMapBytesStream);
        
        // Return color space string including reference to color map bytes stream
        return "[/Indexed /DeviceRGB" + " " + (entries-1) + " " + mapref +"]";
    }
    
    // If image has 4 sample per pixel, return CMYK
    if(anImage.getSamplesPerPixel()==4)
        return "/DeviceCMYK";

    // Return RGB
    return "/DeviceRGB";
}

}