package snap.swing;
import java.awt.image.*;
import java.io.*;
import java.util.*;
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
 * Returns the width of given image in pixels.
 */
protected int getPixWidthImpl()  { return getNative().getWidth(); }

/**
 * Returns the height of given image in pixels.
 */
protected int getPixHeightImpl()  { return getNative().getHeight(); }

/**
 * Returns the width of given image.
 */
protected double getDPIXImpl()  { return _wdpi; }

/**
 * Returns the height of given image.
 */
protected double getDPIYImpl()  { return _hdpi; }

/**
 * Returns whether image has alpha.
 */
protected boolean hasAlphaImpl()  { return getNative().getColorModel().hasAlpha(); }

/**
 * Returns the integer representing the color at the given x,y point.
 */
public int getRGB(int aX, int aY)  { return getNative().getRGB(aX, aY); }

/**
 * Returns the decoded RGB bytes of this image.
 */
protected byte[] getBytesRGBImpl()
{
    // Get ARGB pixel int array    
    int w = getPixWidth(), h = getPixHeight(), boff = 0;
    int pixInts[] = getNative().getRGB(0, 0, w, h, null, 0, w);
    
    // Create RGB byte array and load from pixel int array
    byte rgb[] = new byte[w*h*3];
    for(int y=0;y<h;y++) for(int x=0;x<w;x++) { int pix = pixInts[y*w+x];
        rgb[boff++] = (byte)(pix>>16 & 0xff);
        rgb[boff++] = (byte)(pix>>8 & 0xff);
        rgb[boff++] = (byte)(pix & 0xff);
    }
    
    // Return RGB byte array
    return rgb;
}

/**
 * Returns the decoded RGBA bytes of this image.
 */
protected byte[] getBytesRGBAImpl()
{
    // Get ARGB pixel int array    
    int w = getPixWidth(), h = getPixHeight(), boff = 0;
    int pixInts[] = getNative().getRGB(0, 0, w, h, null, 0, w);
    
    // Create RGBA byte array and load from pixel int array
    byte rgba[] = new byte[w*h*4];
    for(int y=0;y<h;y++) for(int x=0;x<w;x++) { int pix = pixInts[y*w+x];
        rgba[boff++] = (byte)(pix>>16 & 0xff);
        rgba[boff++] = (byte)(pix>>8 & 0xff);
        rgba[boff++] = (byte)(pix & 0xff);
        rgba[boff++] = (byte)(pix>>24 & 0xff);
    }
    
    // Return RGBA byte array
    return rgba;
}

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
 * Returns the JPEG bytes for image.
 */
public byte[] getBytesJPEG()  { return AWTImageUtils.getBytesJPEG(getNative()); }

/**
 * Returns the PNG bytes for image.
 */
public byte[] getBytesPNG()  { return AWTImageUtils.getBytesPNG(getNative()); }

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
        return _native = AWTImageUtils.getBufferedImage((java.awt.Image)getSource());
        
    if(getType()=="gif")
        return getGif();

    // Get image bytes
    byte bytes[] = getBytes();
    if(bytes==null) {
        System.out.println("J2DImage.getNative: No bytes for source: " + getSource()); return null; }
    InputStream istream = new ByteArrayInputStream(bytes);
    
    // Read file
    try { _native = ImageIO.read(istream); }
    catch(IOException e)  { System.err.println(e); return null; }
    catch(SecurityException e) {
        if(ImageIO.getUseCache()) {
            System.out.println("J2DGfxNative.getNative: ImageIO Security Exception - turning off image cache");
            ImageIO.setUseCache(false);
            return getNative();
        }
        throw e;
    }
    
    // Read DPI
    try { getDPI(bytes); }
    catch(Exception e) { System.err.println("J2DImage.getDPI: " + e); }
    
    // Return native
    return _native;
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

/**
 * Returns the native image.
 */
public BufferedImage getGif()
{
    // Create images array, initialized with this image
    List <Image> images = new ArrayList(); images.add(this);
    
    // Read image (or images, if more than one)
    try {
        byte bytes[] = getBytes();
        InputStream istream = new ByteArrayInputStream(bytes);
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream stream = ImageIO.createImageInputStream(istream);
        reader.setInput(stream);
        
        // Read first image
        BufferedImage img0 = reader.read(0); _native = img0;
        int w = img0.getWidth(), h = img0.getHeight();
    
        // Read successive images
        int count = reader.getNumImages(true);
        for(int ind=1;ind<count;ind++) {
            
            // Read next image into J2DImage
            BufferedImage bimg = reader.read(ind);
            Image img2 = new J2DImage(bimg);
            
            // If partial, center in full image
            if(img2.getPixWidth()!=w || img2.getPixHeight()!=h) {
                Point offset = getGIFOffset(reader.getImageMetadata(ind));
                img2 = img2.getFramedImage(w, h, offset.x, offset.y);
            }
            
            // Add to images
            images.add(img2);
        }
    }
    
    // Catch exception
    catch(IOException e) { System.err.println(e); return null; }
    
    // If multiple images, create set
    if(images.size()>1) new ImageSet(images);
    return _native;
}

/**
 * Returns the GIF offset.
 */
private Point getGIFOffset(IIOMetadata metaData)
{
    Node tree = metaData.getAsTree("javax_imageio_gif_image_1.0");
    NodeList childNodes = tree.getChildNodes();

    for(int j=0;j<childNodes.getLength();j++) { Node nodeItem = childNodes.item(j);

        if(nodeItem.getNodeName().equals("ImageDescriptor")){
            NamedNodeMap attrs = nodeItem.getAttributes();
            Node attrX = attrs.getNamedItem("imageLeftPosition");
            int dx = Integer.valueOf(attrX.getNodeValue());
            Node attrY = attrs.getNamedItem("imageTopPosition");
            int dy = Integer.valueOf(attrY.getNodeValue());
            return new Point(dx,dy);
        }
    }
    return new Point();
}

}