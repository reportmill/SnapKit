/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.image.*;
import snap.gfx.ColorSpace;
import snap.pdf.PDFException;

/**
 * The PDFImageColorModel is a subclass of PackedColorModel which can be used to display a pdf image.  
 * It maintains the image's colorspace, and will do color conversions from the image colorspace to sRGB.
 * 
 * Alpha may be taken from a SoftMask image.
 * 
 * This goes through colorspace conversion for every single pixel, and so is blindingly slow.  A subclass which handles
 * images in device colorspaces without conversion would probably be a good idea.
 */
public class PDFImageColorModel extends ColorModel {
    
    // Ivars
    int componentmask, componentshift;
    float componentscale[];
    float componentmin[];
    float conversion_buffer[];
    SoftMask softmask;

/**
 * Create new PDFImageColorModel.
 */  
public PDFImageColorModel(ColorSpace space, int bits, int significantBits[], boolean hasalpha, int transferType)
{
    super(bits, significantBits, new AWTColorSpace(space), hasalpha, true, hasalpha?TRANSLUCENT:OPAQUE, transferType);
}

public static PDFImageColorModel createPDFModel(ColorSpace space, int bps, float decodemins[], float decodemaxs[],
    boolean hasalpha)
{
    int spp = space.getNumComponents()+(hasalpha?1:0);
    int bpp = bps*spp, transferType;

    //color model expects a pixel to fit in one type
    if(bpp<=8) transferType = DataBuffer.TYPE_BYTE;
    else if(bpp<=16) transferType = DataBuffer.TYPE_USHORT;
    else if(bpp<=32) transferType = DataBuffer.TYPE_INT;
    else { System.err.println("Unknown image format: bps=" + bps + "spp=" + spp); return null; }
    
    int significantBits[] = new int[spp]; for(int i=0;i<spp;i++) significantBits[i] = bps;
    
    //PDFImageColorModel bugger_off = new PDFImageColorModel(space, bpp, maskArray, 0, false, OPAQUE, transferType);
    PDFImageColorModel bugger_off = new PDFImageColorModel(space, bpp, significantBits, hasalpha, transferType);
    bugger_off.init(bps, decodemins, decodemaxs);
    return bugger_off;
}

// Create raster based on samplesPerPixel bytes interleaved in some unspecified fashion (eg. RGBRGB, RGBARGBA, etc)
static WritableRaster createInterleavedPDFRaster(DataBuffer buf, int spp, int w, int h)
{
    // for 8 bit samples
    int bandaids[] = new int[spp]; for(int i=0; i<spp; i++) bandaids[i] = i;
    return Raster.createInterleavedRaster(buf,w,h,w*spp, spp, bandaids, new java.awt.Point(0,0));
}

public static WritableRaster createPDFRaster(byte packedbytes[], ColorSpace space, int bps, int w, int h) 
{
    DataBufferByte byteBuffer;
    int spp = space.getNumComponents();

    if(bps==8) { byteBuffer = new DataBufferByte(packedbytes, packedbytes.length); }
    else if(bps==1 || bps==2 || bps==4) {
        // All pixel formats where a sample is smaller than a byte get expanded out.
        // Could probably use a SinglePixelPackedSampleModel for colorspaces with 1 sample per pixel
        // Some spaces may require scaling of samples as well, but I think decode arrays should cover things.
        byteBuffer = new DataBufferByte(w*h*spp);
        byte rasterbytes[] = byteBuffer.getData();
        int src=0, dest=0;
        int sampmask = ((1<<bps)-1)<<(8-bps), shift=0; // a mask for a single sample at the most significant position
        byte sourcebyte=0;
        for(int row=0; row<h; row++) {
            int m = 0;
            for(int col=0; col<w*spp; col++) {
                if(m==0) {
                   sourcebyte = packedbytes[src++]; m = sampmask; shift = 8-bps; }
                rasterbytes[dest++]=(byte)((sourcebyte&m)>>>shift);
                m >>>= bps; shift -= bps;
            }
            // done with a row. If mask is nonzero here, rest of bits are padding so rows fit on byte boundaries.
        }
        // sanity check
        if(dest!=h*w*spp) throw new PDFException("Internal error - bad pixel conversion");
    }
    else throw new PDFException("Illegal pixel format [ SPP=" + spp + " BPS=" + bps + " ]");
    
    return createInterleavedPDFRaster(byteBuffer, spp, w, h);
}

public static WritableRaster createPDFRaster(byte packedbytes[], SoftMask mask, ColorSpace space, int bps, int w, int h)
{
    // java.awt.image seems to support color models where all components are meshed or all components are planar,
    // but not a mixture of the two. In PDF the color samples are meshed, but the alpha samples, if available, are 
    // in a separated plane (from the SMask).  This routine creates new storage for the raster and copies the samples
    // and the alpha into the new buffer, meshing them as RGBA
    DataBufferByte byteBuffer = null;
    int spp = space.getNumComponents();
    
    if(bps==8) {
        byteBuffer = new DataBufferByte((spp+1)*w*h);
        byte rasterbytes[] = byteBuffer.getData();
        int in = 0, out = 0;
        for(int y=0; y<h; y++) {
            for(int x=0;  x<w; x++) {
                for(int i=0; i<spp; i++)
                    rasterbytes[out++] = packedbytes[in++];
                rasterbytes[out++]=(byte)mask.getUnnormalizedAlpha(x,y);
            }
        }
    }

    return byteBuffer!=null? createInterleavedPDFRaster(byteBuffer, spp+1, w, h) : null;
}

public void init(int bps, float decodemins[], float decodemaxs[]) 
{
    int spp = getColorSpace().getNumComponents();
    componentmask = (1<<bps)-1; // mask to get component out of a pixel
    componentshift = bps;  // amount to shift to get next component
    componentmin = new float[spp];  // scale factors to get to colorspace range
    componentscale = new float[spp];
  
    for(int i=0; i<spp; ++i) {
      componentmin[i] = decodemins[i];
      componentscale[i] = (decodemaxs[i]-decodemins[i])/componentmask;
    }
  
    // a buffer to place the normalized values for colorspace conversion
    conversion_buffer = new float[spp];
}

/** Specify softmask (alpha) information for this image */
public void setSoftMask(SoftMask m)  { softmask =  m; }

/** CoerceData. */
public ColorModel coerceData(WritableRaster r, boolean premultipliedAlpha)  { return this; }

/** Not sure. */
public boolean isCompatibleRaster(Raster r)  { return true; }

/**
 * Implemented under duress.  The class defines these as abstract, so we have to provide implementations of them
 * even though they'll never get called.
 */
public int getRGB(int pixel)  { return 0; }
public int getRed(int pixel)  { return 0; };
public int getGreen(int pixel)  { return 0; }
public int getBlue(int pixel)  { return 0; }
public int getAlpha(int pixel)  { return 0; }

/**
 * Convert an array of elements, whose size is defined by the transfertype, into sRGB+alpha.  
 * First we normalize the components and then let the saved pdf colorspace convert into sRGB.
 * We then merge in the alpha to get a pixel.
 */
public int getRGB(Object inData)
{
    byte pix[] = (byte[])inData;  
    int ncomps = conversion_buffer.length;
  
    // normalize values so colorspace can do conversion
    for(int i=0; i<ncomps; ++i)
        conversion_buffer[i] = componentmin[i]+(pix[i]&255)*componentscale[i];
  
    // do actual color space conversion and turn into a pixel
    float srgbvals[] = getColorSpace().toRGB(conversion_buffer);
    int alpha = (softmask != null) ? (pix[ncomps]<<24) : 0xff000000;
    return alpha | (((int)(255*srgbvals[0]))<<16) | (((int)(255*srgbvals[1]))<<8) | ((int)(255*srgbvals[2]));
}

/**
 * A representation of the alpha channel.  The SoftMask maintains a width, height, & bitspersample, since they do not
 * necessarily have to be the same as the source image's. Can also contain a Matte array, which controls
 * premultiplication of source color samples.
 */
protected static class SoftMask {

    // Ivars
    public int width, height, bitspersample;
    public byte alphabits[];
    public float matte[];
    float wscale = 1, hscale = 1; // default is mask size and image size match
    int rowbytes;
  
    /** Create SoftMask. */
    public SoftMask(byte alpha[], int w, int h, int bps, float marray[])
    {
        alphabits = alpha; width = w; height = h; bitspersample = bps; matte = marray;
        rowbytes = (w*bps+7)/8;
    }
    
    public void setSourceImageSize(int sw, int sh)   { wscale = ((float)width)/sw; hscale = ((float)height)/sh; }
    
    /**
     * Returns alpha value that would be mapped to the point in the source image. Since image dimensions may not match
     * mask dimensions, these routines find the alpha value based on sizes of source and destination.
     */
    public float getAlpha(int x, int y)
    {
        float alph = getUnnormalizedAlpha(x,y);
        int scale = (1<<bitspersample)-1;
        return alph/scale;
    }
    
    public int getUnnormalizedAlpha(int x, int y)
    {
        // any number of optimizations can go here, including checking for src & dest being the same size, which 
        // is probably the most common case, anyway. Stretching is all done nearest-neighbor.  No interpolations
        int destx = (int)(x*wscale), desty = (int)(y*hscale), a;
        
        switch(bitspersample) {
            case 8: return alphabits[desty*rowbytes+destx]&255;
            case 4: a = alphabits[desty*rowbytes+destx/2];
                if(destx%2==1) a>>=4;
                return (a&15);
            case 2: a = alphabits[desty*rowbytes+destx/4];
                a >>= 6 - 2*(destx%4);
                return (a&3);
            case 1: a = alphabits[desty*rowbytes+destx/8];
                a >>= 7 - (destx%8);
                return (a&1);
        }
        return 0;   
    }
}

/**
 * Implementation of snap ColorSpace using java.awt.color.ColorSpace.
 */
public static class AWTColorSpace extends java.awt.color.ColorSpace {
    ColorSpace _cs;
    AWTColorSpace(ColorSpace aCS)  { super(aCS.getType(),aCS.getNumComponents()); _cs = aCS; }
    public boolean isCS_sRGB() { return _cs.isCS_sRGB(); }
    public float[] toRGB(float[] colorvalue)  { return _cs.toRGB(colorvalue); }
    public float[] fromRGB(float[] rgbvalue)  { return _cs.fromRGB(rgbvalue); }
    public float[] toCIEXYZ(float[] colorvalue)  { return _cs.toCIEXYZ(colorvalue); }
    public float[] fromCIEXYZ(float[] colorvalue)  { return _cs.fromCIEXYZ(colorvalue); }
    public int getType()  { return _cs.getType(); }
    public int getNumComponents()  { return _cs.getNumComponents(); }
    public String getName(int idx)  { return _cs.getName(idx); }
}

}