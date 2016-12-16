package snap.swing;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/**
 * Utility methods for AWT Images.
 */
public class AWTImageUtils {

/**
 * Returns a JPeg byte array for the given buffered image.
 */
public static byte[] getBytesJPEG(Image anImage)
{
    // Catch exceptions
    try {
        BufferedImage image = getBufferedImage(anImage, false); // Get buffered image
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // Get byte array output stream
        ImageIO.write(image, "jpg", out);  // Write jpg image to output stream
        return out.toByteArray();  // Return byte array output stream bytes
    } catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns a PNG byte array for the given buffered image.
 */
public static byte[] getBytesPNG(Image anImage)
{
    // Catch exceptions
    try {
        BufferedImage image = getBufferedImage(anImage); // Get buffered image
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // Get byte array output stream
        ImageIO.write(image, "png", out); // Write png image to output stream
        return out.toByteArray(); // Return output stream bytes
    } catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns a buffered image for an AWT image with transparency.
 */
public static BufferedImage getBufferedImage(Image anImage)
{
    // If image is already a buffered image, just return it
    if(anImage instanceof BufferedImage)
        return (BufferedImage)anImage;

    // Return buffered image for image with transparency
    return getBufferedImage(anImage, true);
}

/**
 * Returns a buffered image for an AWT image.
 */
public static BufferedImage getBufferedImage(Image anImage, boolean withAlpha)
{
    // If image is already a buffered image with given transparency, just return it
    if(anImage instanceof BufferedImage) {
        BufferedImage image = (BufferedImage)anImage;
        if((image.getTransparency()==BufferedImage.TRANSLUCENT && withAlpha) ||
            (image.getTransparency()==BufferedImage.OPAQUE && !withAlpha))
            return image;
    }
    
    // Get image width and height
    int w = anImage.getWidth(null);
    int h = anImage.getHeight(null);
    
    // Create new buffered image
    BufferedImage bi = getBufferedImage(w, h, withAlpha);
    
    // Draw old image in new buffered image and return
    Graphics2D g = bi.createGraphics();
    g.drawImage(anImage, 0, 0, null);
    g.dispose();
    return bi;
}

/**
 * Returns a compatible buffered image for width and height with given transparency.
 */
public static BufferedImage getBufferedImage(int aWidth, int aHeight, boolean withAlpha)
{
    // Get graphics configuration
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    
    // Return buffered image
    return gc.createCompatibleImage(aWidth, aHeight, withAlpha? Transparency.TRANSLUCENT : Transparency.OPAQUE);
}

/**
 * Loads and sets the decoded bytes for the image data.
 */
public static byte[] getBytesRGBA(BufferedImage anImage)
{
    // Load bytes for standard color models
    ColorModel cm = anImage.getColorModel();
    if(cm instanceof ComponentColorModel)
        return getBytesRGBA(anImage, (ComponentColorModel)cm);
    if(cm instanceof DirectColorModel)
        return getBytesRGBA(anImage, (DirectColorModel)cm);
    if(cm instanceof IndexColorModel)
        return getBytesRGBA(anImage, (IndexColorModel)cm);
    
    // If anything else, complain
    throw new RuntimeException("AWTImageUtils.getBytesRGBA: Color model not supported: " + cm.getClass().getName());
}

/**
 * Load component color model bytes.
 */
private static byte[] getBytesRGBA(BufferedImage anImage, ComponentColorModel ccm)
{
    // Get storage for pixel according to ComponentColorModel's transfer type
    Object out = null;
    switch(ccm.getTransferType()) {
        case DataBuffer.TYPE_BYTE: out = new byte[ccm.getNumComponents()]; break;
        case DataBuffer.TYPE_USHORT: out = new short[ccm.getNumComponents()]; break;
        case DataBuffer.TYPE_INT: out = new int[ccm.getNumComponents()]; break;
    }

    // Get raster to load pixels and allocate _byte array
    int width = anImage.getWidth(), height = anImage.getHeight();
    int spp = ccm.getNumComponents(); if(spp==2) spp = 4; // Promote GA to RGBA
    int bps = ccm.getComponentSize(0);
    int bpp = spp*bps;
    int bytesPerRow = (width*bpp+7)/8; // Might be bogus
    Raster raster = anImage.getData();
    byte bytes[] = new byte[height*bytesPerRow];
    
    for(int y=0; y<height; y++) {
        for(int x=0; x<width; x++) {
            out = raster.getDataElements(x, y, out);
            int sample = (y*width + x)*spp;

            switch(spp) {
                case 4: bytes[sample+3] = (byte)ccm.getAlpha(out);
                case 3: bytes[sample+2] = (byte)ccm.getBlue(out);
                        bytes[sample+1] = (byte)ccm.getGreen(out);
                case 1: bytes[sample] = (byte)ccm.getRed(out); break;
            }
        }
    }
    return bytes;
}

/**
 * Load direct color model bytes.
 */
private static byte[] getBytesRGBA(BufferedImage anImage, DirectColorModel dcm)
{
    // Get pixel size (if not standard, throw fit)
    int psize = dcm.getPixelSize();
    if(psize!=24 && psize!=32)
        throw new RuntimeException("AWTImageUtils.getBytesRGBA: Can't read DCM images with pixel size " + psize);

    // Load _imageData._bytesDecoded from raster
    int width = anImage.getWidth(), height = anImage.getHeight();
    int spp = dcm.getNumComponents(); if(spp==2) spp = 4; // Promote GA to RGBA
    int bps = dcm.getComponentSize(0);
    int bpp = spp*bps;
    int bytesPerRow = (width*bpp+7)/8; // Might be bogus
    Raster raster = anImage.getData();
    int pixels[] = new int[width*spp];
    byte bytes[] = new byte[height*bytesPerRow];

    for(int y=0; y<height; y++) {
        raster.getPixels(0, y, width, 1, pixels);
        for(int x=0; x<width; x++) {
            int sample = (y*width + x)*spp;
            bytes[sample] = (byte)pixels[x*spp];
            bytes[sample + 1] = (byte)pixels[x*spp+1];
            bytes[sample + 2] = (byte)pixels[x*spp+2];
            if(spp==4)
                bytes[sample + 3] = (byte)pixels[x*spp+3];
        }
    }
    
    return bytes;
}

/**
 * Load index color model bytes
 */
private static byte[] getBytesRGBA(BufferedImage anImage, IndexColorModel icm)
{
    // Load _imageData._bytes (I guess we ignore the pixel size and expand to 8 bit, oh-well)
    int width = anImage.getWidth(), height = anImage.getHeight();
    Raster raster = anImage.getData();
    
    // Get bytes decoded
    byte bytes[] = new byte[width*height];
    raster.getDataElements(0, 0, width, height, bytes);
    return bytes;
}

/**
 * Color map support: returns the index of the transparent color in a color map image.
 */
public static int getAlphaColorIndex(BufferedImage anImage)
{
    IndexColorModel icm = (IndexColorModel)anImage.getColorModel();
    return icm.getTransparentPixel();
}

/**
 * Load index color model.
 */
public static byte[] getColorMap(BufferedImage anImage)
{
    // Get basic info
    IndexColorModel icm = (IndexColorModel)anImage.getColorModel();
    int spp = 1;
    int bps = icm.getComponentSize(0);
    int bpp = spp*bps;
    int tableColors = icm.getMapSize();
    int numColors = 1<<bpp;
    
    // Load components
    byte reds[] = new byte[numColors]; icm.getReds(reds);
    byte greens[] = new byte[numColors]; icm.getGreens(greens);
    byte blues[] = new byte[numColors]; icm.getBlues(blues);
    
    // Load ColorMap
    byte colorMap[] = new byte[3*numColors];
    for(int i=0, j=0; i<tableColors; i++) {
        colorMap[j++] = reds[i]; colorMap[j++] = greens[i]; colorMap[j++] = blues[i]; }
    
    // Get _imageData._transparentColorIndex (JAI doesn't seem to set transparentPixel, but does set alpha entry)
    int transparentColorIndex = icm.getTransparentPixel();
    if(transparentColorIndex<0 && icm.getTransparency()==IndexColorModel.BITMASK) {
        icm.getAlphas(reds);
        for(int i=0; i<tableColors; i++)
            if(reds[i]==(byte)0) {
                transparentColorIndex = i; break; }
    }
    
    return colorMap;
}

}