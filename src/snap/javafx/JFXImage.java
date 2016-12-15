package snap.javafx;
import java.awt.image.BufferedImage;
import java.io.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import snap.swing.AWTUtils;

/**
 * A custom class.
 */
public class JFXImage extends snap.gfx.Image {

    // The buffered image
    Image     _native;

/**
 * Returns the native image object for image.
 */
public JFXImage(Object aSource)  { setSource(aSource); }

/**
 * Returns the native image object for image.
 */
public JFXImage(int aWidth, int aHeight, boolean hasAlpha)
{
    _native = new WritableImage(aWidth, aHeight);
}

/**
 * Returns the width of given image.
 */
public int getPixWidth()  { return (int)Math.round(getNative().getWidth()); }

/**
 * Returns the height of given image.
 */
public int getPixHeight()  { return (int)Math.round(getNative().getHeight()); }

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  //{ return getBI().getColorModel().hasAlpha(); }
{
    PixelFormat.Type type = getNative().getPixelReader().getPixelFormat().getType();
    return type==PixelFormat.Type.BYTE_BGRA || type==PixelFormat.Type.BYTE_BGRA_PRE ||
        type==PixelFormat.Type.INT_ARGB || type==PixelFormat.Type.INT_ARGB_PRE;
}

/**
 * Returns number of components.
 */
public int getSamplesPerPixel()  { return hasAlpha()? 4 : 3; }

/**
 * Returns the number of bits per sample.
 */
public int getBitsPerSample()  { return 8; }

/**
 * Returns whether index color model.
 */
public boolean isIndexedColor()  { return false; }

/**
 * Color map support: returns the bytes of color map from a color map image.
 */
public byte[] getColorMap()  { return null; }

/**
 * Returns the integer representing the color at the given x,y point.
 */
public int getRGB(int aX, int aY)  { return getNative().getPixelReader().getArgb(aX, aY); }

/**
 * Returns the JPEG bytes for image.
 */
public byte[] getBytesJPEG()  { return AWTUtils.getBytesJPEG(getBI()); }

/**
 * Returns the PNG bytes for image.
 */
public byte[] getBytesPNG()  { return AWTUtils.getBytesPNG(getBI()); }

/**
 * Returns a painter for image.
 */
public snap.gfx.Painter getPainter()  { return new JFXPainter((WritableImage)_native); }

/**
 * Returns whether image data is premultiplied.
 */
public boolean isPremultiplied()  { return false; }

/**
 * Sets whether image data is premultiplied.
 */
public void setPremultiplied(boolean aValue)  { System.err.println("JFXImage.setPremultiplied: Not implemented"); }

/**
 * Blurs the image by mixing pixels with those around it to given radius.
 */
public void blur(int aRadius)  { System.err.println("JFXImage.blur: Not implemented"); }

/**
 * Embosses the image by mixing pixels with those around it to given radius.
 */
public void emboss(double aRad, double anAzi, double anAlt)  { System.err.println("JFXImage.emboss: Not implemented"); }

/**
 * Returns a buffered image.
 */
public BufferedImage getBI()  { return SwingFXUtils.fromFXImage(getNative(),null); }

/**
 * Returns the native image.
 */
public Image getNative()
{
    // If already set, just return
    if(_native!=null) return _native;
    
    if(getSource() instanceof Image)
        return _native = (Image)getSource();

    byte bytes[] = getBytes(); if(bytes==null) return new WritableImage(1,1);
    InputStream istream = new ByteArrayInputStream(bytes);
    try { return _native = new Image(istream); }
    catch(Exception e)  { System.err.println(e); return new WritableImage(1,1); }
}

}