package snap.swing;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import snap.gfx.*;

/**
 * A custom class.
 */
public class J2DImage extends Image {
    
    // The width/height dpi
    double            _wdpi = 72, _hdpi = 72;
    
    // The color map
    byte              _colorMap[];
    
    // The buffered image
    BufferedImage     _native;

/**
 * Returns the native image object for image.
 */
public J2DImage(Object aSource)  { setSource(aSource); }

/**
 * Returns the native image object for image.
 */
public J2DImage(int aWidth, int aHeight, boolean hasAlpha)
{
    if(hasAlpha) _native = new BufferedImage(aWidth, aHeight, BufferedImage.TYPE_INT_ARGB);
    else _native = new BufferedImage(aWidth, aHeight, BufferedImage.TYPE_INT_RGB);
}

/**
 * Returns the width of given image.
 */
public double getWidthDPI()  { return _wdpi; }

/**
 * Returns the height of given image.
 */
public double getHeightDPI()  { return _hdpi; }

/**
 * Returns the width of given image in pixels.
 */
public int getPixWidth()  { return getNative().getWidth(); }

/**
 * Returns the height of given image in pixels.
 */
public int getPixHeight()  { return getNative().getHeight(); }

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return getNative().getColorModel().hasAlpha(); }

/**
 * Returns number of components.
 */
public int getSamplesPerPixel()  { return getNative().getColorModel().getNumComponents(); }

/**
 * Returns the number of bits per sample.
 */
public int getBitsPerSample()  { return getNative().getColorModel().getComponentSize(0); }

/**
 * Returns whether index color model.
 */
public boolean isIndexedColor()  { return getNative().getColorModel() instanceof IndexColorModel; }

/**
 * Color map support: returns the bytes of color map from a color map image.
 */
public byte[] getColorMap()
{
    if(_colorMap!=null) return _colorMap; //_spp = 1;
    
    // Get basic info
    IndexColorModel icm = (IndexColorModel)getNative().getColorModel();
    int tableColors = icm.getMapSize();
    int numColors = 1<<getBitsPerPixel();
    
    // Load components
    byte reds[] = new byte[numColors]; icm.getReds(reds);
    byte greens[] = new byte[numColors]; icm.getGreens(greens);
    byte blues[] = new byte[numColors]; icm.getBlues(blues);
    
    // Load ColorMap
    _colorMap = new byte[3*numColors];
    for(int i=0, j=0; i<tableColors; i++) {
        _colorMap[j++] = reds[i]; _colorMap[j++] = greens[i]; _colorMap[j++] = blues[i]; }
    
    // Get _imageData._transparentColorIndex (JAI doesn't seem to set transparentPixel, but does set alpha entry)
    int _transparentColorIndex = icm.getTransparentPixel();
    if(_transparentColorIndex<0 && icm.getTransparency()==IndexColorModel.BITMASK) {
        icm.getAlphas(reds);
        for(int i=0; i<tableColors; i++)
            if(reds[i]==(byte)0) {
                _transparentColorIndex = i; break; }
    }
    
    return _colorMap;
}

/**
 * Returns the integer representing the color at the given x,y point.
 */
public int getRGB(int aX, int aY)  { return getNative().getRGB(aX, aY); }

/**
 * Returns the JPEG bytes for image.
 */
public byte[] getBytesJPEG()  { return AWTUtils.getBytesJPEG(getNative()); }

/**
 * Returns the PNG bytes for image.
 */
public byte[] getBytesPNG()  { return AWTUtils.getBytesPNG(getNative()); }

/**
 * Returns a painter for image.
 */
public Painter getPainter()
{
    Painter pntr = new J2DPainter(getNative().createGraphics());
    pntr.clipRect(0,0,getWidth(),getHeight());
    return pntr;
}

/**
 * Returns whether image data is premultiplied.
 */
public boolean isPremultiplied()  { return _native.isAlphaPremultiplied(); }

/**
 * Sets whether image data is premultiplied.
 */
public void setPremultiplied(boolean aValue)  { _native.coerceData(aValue); }

/**
 * Returns the ARGB array of this image.
 */
public int[] getArrayARGB()
{
    Raster raster = _native.getRaster();
    DataBuffer buf = raster.getDataBuffer();
    if(buf.getDataType() != DataBuffer.TYPE_INT || buf.getNumBanks() != 1)
        throw new RuntimeException("unknown data format");
    int pix[] = ((DataBufferInt)buf).getData();
    return pix;
}

/**
 * Blurs the image by mixing pixels with those around it to given radius.
 */
public void blur2(int aRad)
{
    // Check whether premultiplied
    if(!isPremultiplied()) System.err.println("J2DImage.blur: Need to set premultiply for convolve");

    // Apply horizontal 1D gausian kernal and 1D vertical gaussian kernal for speed (order = 2*rad instead of rad^2)
    Kernel kern1 = new Kernel(aRad*2+1, 1, GFXUtils.getGaussianKernel(aRad,0));
    ConvolveOp cop1 = new ConvolveOp(kern1, ConvolveOp.EDGE_NO_OP, null); //ConvolveOp.EDGE_ZERO_FILL, null);
    BufferedImage temp = cop1.filter(_native, null);
    Kernel kern2 = new Kernel(1, aRad*2+1, GFXUtils.getGaussianKernel(0,aRad));
    ConvolveOp cop2 = new ConvolveOp(kern2, ConvolveOp.EDGE_NO_OP, null); //ConvolveOp.EDGE_ZERO_FILL, null);
    cop2.filter(temp, _native);
    
    // Convert blur image to non-premultiplied and return
    _native.coerceData(false);
}

/**
 * Returns the native image.
 */
public BufferedImage getNative()
{
    // If already set, just return
    if(_native!=null) return _native;
    
    if(getSource() instanceof java.awt.Image)
        return _native = AWTUtils.getBufferedImage((java.awt.Image)getSource());

    // Get image bytes
    byte bytes[] = getBytes();
    if(bytes==null) {
        System.out.println("J2DImage.getNative: No bytes for source: " + getSource()); return null; }
    InputStream istream = new ByteArrayInputStream(bytes);
    
    // Read file
    try {
        _native = ImageIO.read(istream);
        getDPI(bytes);
        return _native;
    }
    catch(IOException e)  { System.err.println(e); return null; }
    catch(SecurityException e) {
        if(ImageIO.getUseCache()) {
            System.out.println("J2DGfxNative.getNative: ImageIO Security Exception - turning off image cache");
            ImageIO.setUseCache(false);
            return getNative();
        }
        throw e;
    }
}

/**
 * What a load of junk!
 */
private void getDPI(byte theBytes[]) throws IOException
{
    InputStream istream = new ByteArrayInputStream(theBytes);
    ImageInputStream stream = ImageIO.createImageInputStream(istream);
    Iterator <ImageReader> readers = ImageIO.getImageReaders(stream);
    if(readers.hasNext()) {
        ImageReader reader = readers.next();
        reader.setInput(stream);
        IIOMetadata mdata = reader.getImageMetadata(0);
        IIOMetadataNode root = (IIOMetadataNode)mdata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        
        NodeList hps = root.getElementsByTagName("HorizontalPixelSize");
        IIOMetadataNode hps2 = hps.getLength()>0? (IIOMetadataNode)hps.item(0) : null;
        NamedNodeMap hnnm = hps2!=null? hps2.getAttributes() : null;
        Node hitem = hnnm!=null? hnnm.item(0) : null;
        if(hitem!=null) _wdpi = Math.round(25.4/Double.parseDouble(hitem.getNodeValue()));
        
        NodeList vps = root.getElementsByTagName("VerticalPixelSize");
        IIOMetadataNode vps2 = vps.getLength()>0? (IIOMetadataNode)vps.item(0) : null;
        NamedNodeMap vnnm = vps2!=null? vps2.getAttributes() : null;
        Node vitem = vnnm!=null? vnnm.item(0) : null;
        if(vitem!=null) _hdpi = Math.round(25.4/Double.parseDouble(vitem.getNodeValue()));
    }
}

}
