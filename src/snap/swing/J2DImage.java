package snap.swing;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import snap.geom.Point;
import snap.gfx.*;
import snap.viewx.DialogBox;

/**
 * An Image subclass for Java2D.
 */
public class J2DImage extends Image {
    
    // The buffered image
    private BufferedImage  _native;
    
    /**
     * Constructor for size, alpha and dpi scale.
     */
    public J2DImage(double aWidth, double aHeight, boolean hasAlpha, double dpiScale)
    {
        super();

        // Get image with/height
        _width = (int) Math.round(aWidth);
        _height = (int) Math.round(aHeight);

        // Get pixel width/height by rounding scaled width/height
        _pixW = (int) Math.round(aWidth * dpiScale);
        _pixH = (int) Math.round(aHeight * dpiScale);

        // Create internal buffered image for pixel width/height and alpha
        _hasAlpha = hasAlpha;
        if (hasAlpha)
            _native = new BufferedImage(_pixW, _pixH, BufferedImage.TYPE_INT_ARGB);
        else _native = new BufferedImage(_pixW, _pixH, BufferedImage.TYPE_INT_RGB);

        // Reset dpi for scale
        _dpiScale = (int) Math.round(dpiScale);
        if (dpiScale != 1) {
            _dpiX *= dpiScale;
            _dpiY *= dpiScale;
        }
    }

    /**
     * Constructor for given source.
     */
    public J2DImage(Object aSource)
    {
        super();

        // Set image source
        setSource(aSource);

        // Initialize size to placeholder 20 x 20
        _width = _height = 20;
        _pixW = _pixH = 20;

        // Load image and set properties
        setLoaded(false);
        CompletableFuture.runAsync(this::loadImage);
    }

    /**
     * Called to load image in background.
     */
    private void loadImage()
    {
        // Load image
        BufferedImage image = getNative();
        _pixW = image.getWidth();
        _pixH = image.getHeight();
        _width = _pixW * 72d / _dpiX;
        _height = _pixH * 72d / _dpiY;
        _hasAlpha = image.getColorModel().hasAlpha();
        _dpiScale = _dpiX != 72 ? _dpiX / 72 : 1;

        // Set loaded
        setLoaded(true);
    }

    /**
     * Returns the integer representing the color at the given x,y point.
     */
    public int getRGB(int aX, int aY)  { return getNative().getRGB(aX, aY); }

    /**
     * Sets an RGB integer for given x, y.
     */
    public void setRGB(int aX, int aY, int rgb)
    {
        getNative().setRGB(aX, aY, rgb);
    }

    /**
     * Returns the decoded RGB bytes of this image.
     */
    protected byte[] getBytesRGBImpl()
    {
        // Get ARGB pixel int array
        int pixW = getPixWidth();
        int pixH = getPixHeight();
        int boff = 0;
        int[] pixInts = getNative().getRGB(0, 0, pixW, pixH, null, 0, pixW);

        // Create RGB byte array and load from pixel int array
        byte[] rgb = new byte[pixW * pixH * 3];
        for (int y = 0; y < pixH; y++) {
            for (int x = 0; x < pixW; x++) {
                int pix = pixInts[y * pixW + x];
                rgb[boff++] = (byte) (pix >> 16 & 0xff);
                rgb[boff++] = (byte) (pix >> 8 & 0xff);
                rgb[boff++] = (byte) (pix & 0xff);
            }
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
        int pixW = getPixWidth();
        int pixH = getPixHeight();
        int boff = 0;
        int[] pixInts = getNative().getRGB(0, 0, pixW, pixH, null, 0, pixW);

        // Create RGBA byte array and load from pixel int array
        byte[] rgba = new byte[pixW * pixH * 4];
        for (int y = 0; y < pixH; y++) {
            for (int x = 0; x < pixW; x++) {
                int pix = pixInts[y * pixW + x];
                rgba[boff++] = (byte) (pix >> 16 & 0xff);
                rgba[boff++] = (byte) (pix >> 8 & 0xff);
                rgba[boff++] = (byte) (pix & 0xff);
                rgba[boff++] = (byte) (pix >> 24 & 0xff);
            }
        }

        // Return RGBA byte array
        return rgba;
    }

    /**
     * Returns the JPEG bytes for image.
     */
    public byte[] getBytesJPEG()
    {
        // If HiDPI, get 72 dpi image and return that instead
        if (getDpiScale() != 1) {
            System.out.println("J2DImage.getBytesJPEG: Downsampling to 72 dpi since other dpi not supported");
            Image downSampledImage = copyForDpiScale(1);
            return downSampledImage.getBytesJPEG();
        }

        // Return JPEG bytes
        return AWTImageUtils.getBytesJPEG(getNative());
    }

    /**
     * Returns the PNG bytes for image.
     */
    public byte[] getBytesPNG()
    {
        // If HiDPI, get 72 dpi image and return that instead
        if (getDpiScale() != 1) {
            System.out.println("J2DImage.getBytesPNG: Downsampling to 72 dpi since other dpi not supported");
            Image downSampledImage = copyForDpiScale(1);
            return downSampledImage.getBytesPNG();
        }

        // Return PNG bytes
        return AWTImageUtils.getBytesPNG(getNative());
    }

    /**
     * Returns a painter for image.
     */
    public Painter getPainter()
    {
        // Get painter for Graphics
        Painter pntr = new J2DPainter(getNative().createGraphics());

        // If hidpi, scale default transform
        double scale = getDpiScale();
        if (scale!=1)
            pntr.transform(scale,0,0,scale,0,0);

        // Clip to image bounds and return
        pntr.clipRect(0,0,getWidth(),getHeight());
        return pntr;
    }

    /**
     * Blurs the image by mixing pixels with those around it to given radius.
     */
    public void blur(int aRad, Color aColor)
    {
        // If color provided, apply to image with SRC_IN
        if (aColor != null) {
            Painter pntr = getPainter();
            pntr.setComposite(Painter.Composite.SRC_IN);
            pntr.setColor(aColor);
            pntr.fillRect(0, 0, getWidth(), getHeight());
        }

        // Make image premultiplied
        setPremultiplied(true);

        // Get image data (and temp data)
        int pixW = getPixWidth();
        int pixH = getPixHeight();
        int[] spix = getArrayARGB(); if (spix == null) { System.err.println("Image.blur: No data"); return; }
        int[] tpix = new int[pixW * pixH];

        // Apply 1D gausian kernal for speed, as horizontal, then vertical (order = 2*rad instead of rad^2)
        float[] kern1 = AWTImageUtils.getGaussianKernel(aRad,0); // size = aRad*2+1 x 1
        AWTImageUtils.convolve(spix, tpix, pixW, pixH, kern1, aRad*2+1);  // Horizontal 1D, kern size = aRad*2+1 x 1
        AWTImageUtils.convolve(tpix, spix, pixW, pixH, kern1, 1);         // Vertical 1D, kern size = 1 x aRad*2+1

        // Convert blur image to non-premultiplied and return
        setPremultiplied(false);
    }

    /**
     * Embosses the image by mixing pixels with those around it to given radius.
     */
    public void emboss(double aRadius, double anAzi, double anAlt)
    {
        // Get basic info
        int pixW = getPixWidth();
        int pixH = getPixHeight();
        int radius = (int) Math.round(aRadius);
        int rad = Math.abs(radius);

        // Create bump map: original graphics offset by radius, blurred. Color doesn't matter - only alpha channel used.
        J2DImage bumpImg = (J2DImage) Image.getImageForSize(pixW + rad * 2, pixH + rad * 2, true);
        Painter ipntr = bumpImg.getPainter();
        ipntr.setImageQuality(1); //ipntr.clipRect(0, 0, width, height);
        ipntr.drawImage(this, rad, rad, pixW, pixH);
        bumpImg.blur(rad, null);

        // Get source and bump pixels as int arrays and call general emboss method
        int[] srcPixels = getArrayARGB(); if (srcPixels == null) { System.err.println("Image.emboss: No data"); return; }
        int[] bumpPixels = bumpImg.getArrayARGB();
        AWTImageUtils.emboss(srcPixels, bumpPixels, pixW, pixH, radius, anAzi * Math.PI / 180, anAlt * Math.PI / 180);
    }

    /**
     * Returns whether image data is premultiplied.
     */
    private boolean isPremultiplied()  { return _native.isAlphaPremultiplied(); }

    /**
     * Sets whether image data is premultiplied.
     */
    private void setPremultiplied(boolean aValue)  { _native.coerceData(aValue); }

    /**
     * Returns the ARGB array of this image.
     */
    private int[] getArrayARGB()
    {
        // Get Raster, DataBuffer
        Raster raster = _native.getRaster();
        DataBuffer dataBuffer = raster.getDataBuffer();

        // If not int type, convert
        if (dataBuffer.getDataType() != DataBuffer.TYPE_INT || dataBuffer.getNumBanks() != 1) {
            convertNativeToIntType();
            dataBuffer = _native.getRaster().getDataBuffer();
        }

        // Return int[] data
        return ((DataBufferInt) dataBuffer).getData();
    }

    /**
     * Converts native to int type.
     */
    private void convertNativeToIntType()
    {
        // Create new buffered image and draw old image in new buffered image
        int imageW = getPixWidth();
        int imageH = getPixHeight();
        BufferedImage intTypeImage = AWTImageUtils.getBufferedImage(imageW, imageH, hasAlpha());
        java.awt.Graphics2D gfx2D = intTypeImage.createGraphics();
        gfx2D.drawImage(_native, 0, 0, null);
        gfx2D.dispose();
        if (isPremultiplied())
            intTypeImage.coerceData(true);

        // Reset native to int type
        _native = intTypeImage;
    }

    /** Blurs the image by mixing pixels with those around it to given radius. */
    /*public void blur2(int aRad)  {
        // Check whether premultiplied
        if (!isPremultiplied()) System.err.println("J2DImage.blur: Need to set premultiply for convolve");

        // Apply horizontal 1D gausian kernal and 1D vertical gaussian kernal for speed (order = 2*rad instead of rad^2)
        Kernel kern1 = new Kernel(aRad*2+1, 1, GFXUtils.getGaussianKernel(aRad,0));
        ConvolveOp cop1 = new ConvolveOp(kern1, ConvolveOp.EDGE_NO_OP, null); //ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage temp = cop1.filter(_native, null);
        Kernel kern2 = new Kernel(1, aRad*2+1, GFXUtils.getGaussianKernel(0,aRad));
        ConvolveOp cop2 = new ConvolveOp(kern2, ConvolveOp.EDGE_NO_OP, null); //ConvolveOp.EDGE_ZERO_FILL, null);
        cop2.filter(temp, _native);

        // Convert blur image to non-premultiplied
        _native.coerceData(false); }*/

    /**
     * Returns the native image.
     */
    public BufferedImage getNative()
    {
        // If already set, just return
        if (_native != null) return _native;

        // Get image for source (complain if not found and use error image)
        BufferedImage buffImage = getNativeImpl();
        if (buffImage == null) {
            System.err.println("J2DImage.getNativeImpl: Image not found for source: " + getSource());
            buffImage = (BufferedImage) DialogBox.errorImage.getNative();
        }

        // Set/return image
        return _native = buffImage;
    }

    /**
     * Returns the native image.
     */
    private BufferedImage getNativeImpl()
    {
        // Not sure this is used - should replace with static createForNative()?
        if (getSource() instanceof java.awt.Image)
            return _native = AWTImageUtils.getBufferedImage((java.awt.Image) getSource());

        // Special gif support for anim
        if (getType() == "gif")
            return getGif();

        // Get image bytes
        byte[] bytes = getBytes();
        if (bytes == null) {
            System.out.println("J2DImage.getNative: No bytes for source: " + getSource()); return null; }
        InputStream istream = new ByteArrayInputStream(bytes);

        // Read file
        try { _native = ImageIO.read(istream); }
        catch(IOException e)  { System.err.println(e); return null; }
        catch(SecurityException e) {
            if (ImageIO.getUseCache()) {
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
    private void getDPI(byte[] theBytes) throws IOException
    {
        // Get ImageIO Readers
        InputStream inputStream = new ByteArrayInputStream(theBytes);
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

        // Iterate over readers to find DPI
        if (imageReaders.hasNext()) {
            ImageReader imageReader = imageReaders.next();
            imageReader.setInput(imageInputStream);
            IIOMetadata metadata = imageReader.getImageMetadata(0);
            IIOMetadataNode rootNode = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);

            // Check for horizontal DPI
            NodeList hps = rootNode.getElementsByTagName("HorizontalPixelSize");
            IIOMetadataNode hps2 = hps.getLength() > 0 ? (IIOMetadataNode) hps.item(0) : null;
            NamedNodeMap hnnm = hps2 != null ? hps2.getAttributes() : null;
            Node hitem = hnnm != null ? hnnm.item(0) : null;
            if (hitem != null)
                _dpiX = (int) Math.round(25.4 / Double.parseDouble(hitem.getNodeValue()));

            // Check for vertical DPI
            NodeList vps = rootNode.getElementsByTagName("VerticalPixelSize");
            IIOMetadataNode vps2 = vps.getLength()>0 ? (IIOMetadataNode) vps.item(0) : null;
            NamedNodeMap vnnm = vps2 != null ? vps2.getAttributes() : null;
            Node vitem = vnnm != null ? vnnm.item(0) : null;
            if (vitem!=null)
                _dpiY = (int) Math.round(25.4 / Double.parseDouble(vitem.getNodeValue()));
        }
    }

    /**
     * Returns the native image.
     */
    public BufferedImage getGif()
    {
        // Create images array, initialized with this image
        List<Image> images = new ArrayList<>();
        images.add(this);

        // Read image (or images, if more than one)
        try {
            byte[] bytes = getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            ImageReader imageReader = ImageIO.getImageReadersByFormatName("gif").next();
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            imageReader.setInput(imageInputStream);

            // Read first image
            BufferedImage bufferedImage = imageReader.read(0);
            _native = bufferedImage;
            int imageW = bufferedImage.getWidth();
            int imageH = bufferedImage.getHeight();

            // Read successive images
            int imageCount = imageReader.getNumImages(true);
            for (int imageIndex = 1; imageIndex < imageCount; imageIndex++) {

                // Read next image into J2DImage
                BufferedImage nextBufferedImage = imageReader.read(imageIndex);
                Image nextImage = new J2DImage(nextBufferedImage);

                // If partial, center in full image
                if (nextImage.getPixWidth() != imageW || nextImage.getPixHeight() != imageH) {
                    Point offset = getGIFOffset(imageReader.getImageMetadata(imageIndex));
                    nextImage = nextImage.getFramedImage(imageW, imageH, offset.x, offset.y);
                }

                // Add to images
                images.add(nextImage);
            }
        }

        // Catch exception
        catch(IOException e) { System.err.println(e.getMessage()); return null; }

        // If multiple images, create set
        if (images.size() > 1)
            new ImageSet(images);

        // Return
        return _native;
    }

    /**
     * Returns the GIF offset.
     */
    private Point getGIFOffset(IIOMetadata metaData)
    {
        Node tree = metaData.getAsTree("javax_imageio_gif_image_1.0");
        NodeList childNodes = tree.getChildNodes();

        for (int j = 0; j < childNodes.getLength(); j++) {
            Node nodeItem = childNodes.item(j);

            if (nodeItem.getNodeName().equals("ImageDescriptor")){
                NamedNodeMap attrs = nodeItem.getAttributes();
                Node attrX = attrs.getNamedItem("imageLeftPosition");
                int dx = Integer.parseInt(attrX.getNodeValue());
                Node attrY = attrs.getNamedItem("imageTopPosition");
                int dy = Integer.parseInt(attrY.getNodeValue());
                return new Point(dx,dy);
            }
        }
        return new Point();
    }
}