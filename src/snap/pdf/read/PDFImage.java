/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Image;
import java.awt.image.*;
import java.util.*;
import snap.gfx.ColorSpace;
import snap.pdf.*;
import snap.pdf.read.PDFImageColorModel.SoftMask;

/**
 * PDFImage represents an image in a PDF file. 
 */
public class PDFImage {

/**
 * Given a pdf xobject dictionary, return an awt Image object.
 */
public static Image getImage(PDFStream imageStream, ColorSpace cspace, PDFFile srcfile)
{
    // First check to see if we've done this already
    Map imageDict = imageStream.getDict();
    Image image = (Image)imageDict.get("_rbcached_awtimage_");
    if (image!=null)
        return image;
    
    // image xobjects only, although a form xobject could be made someday
    Object val = imageDict.get("Subtype");
    if(val==null) 
        return null;
    
    if(!val.equals("/Image")) {
        System.err.println(val+" xObjects not supported yet"); return null; }
    
    if(imageStream.usesFilter("/JPXDecode")) {
        System.err.println("JPeg2000 (/JPXDecode filter) not supported yet "); return null; }
        
    if(imageStream.usesFilter("/DCTDecode")) {
        image = getDCTImage(imageStream);
        imageDict.put("_rbcached_awtimage_", image);
        return image;
    }

    boolean expandBitmap = false;
    
    // Width (required)
    val = imageDict.get("Width"); if(val==null) return null;
    int w = ((Number)val).intValue();
    
    // Height (required)
    val = imageDict.get("Height"); if(val==null) return null;
    int h = ((Number)val).intValue();
    
    // ImageMask (optional)
    val = imageDict.get("ImageMask");
    boolean isMask = ((val!=null) && ((Boolean)val).booleanValue());
    
    // SMask (alpha) is stored as a separate image XObject
    // size of mask doesn't have to match size of image, so tell mask how big image is so it maps values correctly
    val = srcfile.getXRefObj(imageDict.get("SMask"));
    SoftMask alphaMask = null;
    if (val != null) {
        alphaMask = readSMask((PDFStream)val);
        alphaMask.setSourceImageSize(w,h);
    }
    
    // Bits per component (required except for image masks or /JPXDecode)
    val = imageDict.get("BitsPerComponent");
    int bpc = 0;
    if (val!=null)
        bpc = ((Number)val).intValue();

    // An image mask will get turned into an image with 8 byte samples, with two values - 1 & 0.  To draw image,
    // we create an indexed Colorspace with 2 colors - one the stroke color and the other transparent black. 
    //
    // A few issues :
    //   1. this routine doesn't have access to stroke color
    //   2. image can't be cached, since it may be drawn with a different stroke color
    //   3. what happens if the colorspace is a pattern or shading?  would need clip path.
    if(isMask || bpc==1) {
        
        // For enormous monochrome images, use image producer that renders image in strips (see RM14)
        //if(w*h>4*1024*1024 && imageStream.usesFilter("/CCITTFaxDecode")) {
        //    awtImage = getCCITTFaxDecodeImage(imageStream,w,h); return awtImage; }
        
        //current color and alpha for mask, black and white for image
        byte clut[] = isMask? new byte[] { 0,0,0,-1,-1,-1,-1,0 } : new byte[] { 0,0,0,-1,-1,-1 };
        cspace = new PDFColorSpaces.IndexedColorSpace(PDFColorSpaces.DeviceRGB.get(), 1, isMask, clut);
        bpc = 8; expandBitmap = true;
    }
    
    if(bpc==0)
        throw new PDFException("Illegal image format");
        
    // Components per pixel (from the colorspace)
    int cpp = cspace.getNumComponents();
    
    // Get the actual bytes from the pdf stream, running through any filters if necessary
    byte streamBytes[];
    try { streamBytes = imageStream.decodeStream(); }
    catch (Exception e) { System.err.println("Error decoding image stream: "+e); return null; }
    
    // For image masks, check to see if filter already expanded the data out to 8bpp. If not, do it now.
    if (expandBitmap) {
        if (w*h != streamBytes.length)
            streamBytes = expandBitmapBits(streamBytes,w,h);
        // if w & h are both 1, bit count equals byte count regardless of whether we've exapanded them or not.
        // So do it again right here just in case.
        else if(w==1 && h==1)
            streamBytes[0]=(byte)(streamBytes[0]&1);
    }
    
    // Decode array (optional). The decode array tells you how to turn the bits of an individual
    // component sample into a float which would be valid in the colorspace.
    float dmins[] = new float[cpp];
    float dmaxs[] = new float[cpp];
    val = imageDict.get("Decode");
    if (val != null) { List dList = (List)val;
        
        if (dList.size() != cpp*2) {
            System.err.println("Wrong number of components in decode array"); return null; }
        for(int i=0; i<cpp; ++i) {
            dmins[i]=((Number)dList.get(2*i)).floatValue();
            dmaxs[i]=((Number)dList.get(2*i+1)).floatValue();
        }
    }
    else { 
        //default decodes are [0 1 .. 0 1] except for indexed spaces in which case its [0 2^bps-1]
        if (cspace instanceof PDFColorSpaces.IndexedColorSpace) {
            dmins[0] = 0; dmaxs[0] = (1<<bpc)-1; }
        else {
            for(int i=0; i<cpp; ++i) {
                dmins[i] = 0; dmaxs[i] = 1; }
        }
    }
    
    // Create a Raster for the image samples. The raster will use meshed samples (as they are in pdf).
    // If the image specifies a softmask, its data will get meshed in with the color samples.
    WritableRaster praster;
    if(alphaMask==null) praster = PDFImageColorModel.createPDFRaster(streamBytes, cspace, bpc, w, h);
    else praster = PDFImageColorModel.createPDFRaster(streamBytes, alphaMask, cspace, bpc, w, h);
    
    // Now create a PDFColorModel for the image. The model takes care of colorspace conversion of the samples and 
    // knows how to return sRGB pixels with or without alpha for the BufferedImage
    if (praster != null) {
        PDFImageColorModel pixModel = PDFImageColorModel.createPDFModel(cspace, bpc, dmins, dmaxs, alphaMask!=null);
        pixModel.setSoftMask(alphaMask);
        image = new BufferedImage(pixModel, praster, false, null);
    }
  
    if(image!=null) 
        imageDict.put("_rbcached_awtimage_", image);
    return image;
}

/**
 * Alpha channels can be specified for images via a "SMask" image, which is a /DeviceGray image that gets mapped to 
 * size of destination image. Samples in the original image may or may not be premultiplied by the smask alpha samples,
 * along with an additional matte color if specified by a /Matte entry.
 */
private static SoftMask readSMask(PDFStream smaskStream)
{
    Map smaskDict = smaskStream.getDict();
    Object typ = smaskDict.get("Type"), styp = smaskDict.get("Subtype"), cspc = smaskDict.get("ColorSpace");
    if((!"/XObject".equals(typ)) || (!"/Image".equals(styp)) || (!"/DeviceGray".equals(cspc)))
        throw new PDFException("Illegal soft mask declaration");
    
    Object val = smaskDict.get("Width");
    int w = ((Number)val).intValue();
    val = smaskDict.get("Height");
    int h = ((Number)val).intValue();
    val = smaskDict.get("BitsPerComponent");
    int bpc = ((Number)val).intValue();
    
    val = smaskDict.get("Matte");
    float matte_components[] = null;
    if(val instanceof List) { List mlist = (List)val;
        matte_components = new float[mlist.size()];
        for(int i=0, n=mlist.size(); i<n; ++i)
            matte_components[i] = ((Number)mlist.get(i)).floatValue();
    }
    
    // create the softmask object to hold the alpha
    return new SoftMask(smaskStream.decodeStream(), w, h, bpc, matte_components);
}

/**
 * Expand 1 bit-per-pixel data out into 8 bits per pixel so we can create an
 * awt image from it.  The image code will create a color table for black & white
 */
private static byte[] expandBitmapBits(byte streamBytes[], int width, int height)
{
    byte expandedBytes[] = new byte[width*height];
    int rowbytes = width/8;
    
    // rows are padded to byte boundaries
    if (width % 8 != 0)
        ++rowbytes;
    // (in)sanity check
    if (height * rowbytes != streamBytes.length)
        throw new PDFException("wrong amount of data for image mask");
    
    int expanded_position = 0, src_position = 0;
    for(int y=0; y<height; ++y) {
        int mask = 128;
        src_position = y*rowbytes;
        int bits = streamBytes[src_position];
        for(int x=0; x<width; ++x) {
            expandedBytes[expanded_position++] = (byte)((bits & mask)==0 ? 0 : 1);
            mask>>=1;
            if (mask==0 && x<width-1) {
                mask = 128;
                bits = streamBytes[++src_position];
            }
        }
    }
    return expandedBytes;
}

/**        
 * For DCTDecode images, try assuming the data is a valid jpeg stream and let awt read it.
 * TODO:  A big problem here is color spaces.  The awt version won't work for cmyk images that are dct encoded,
 * for example.  Other,  more outlandish colorspaces definitely wont work.
 */ 
static Image getDCTImage(PDFStream imageStream)
{
    // Since DCTDecode is an image-specifc encoding, it makes no sense to have any filters AFTER it (at least not
    // with the kind of filters pdf currently supports).
    // Also, since we're going to try to pass the DCT encoded bytes to awt, we have to run any filters that come
    // BEFORE the dct, but leave the stream as DCT.
    //   /Filters [/ASCII85 /DCTDecode]
    if(imageStream.indexOfFilter("/DCTDecode")!=imageStream.numFilters()-1)
        throw new PDFException("Illegal image stream");
    byte dctbytes[] = imageStream.decodeStream(imageStream.numFilters()-1);
    
    // Load image and return AWT image (was java.awt.Toolkit.getDefaultToolkit().createImage(bytes))
    return (Image)snap.gfx.Image.get(dctbytes).getNative();
}

}