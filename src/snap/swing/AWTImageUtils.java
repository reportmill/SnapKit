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
    
    // Create new buffered image, draw old image in new buffered image and return
    BufferedImage bi = getBufferedImage(w, h, withAlpha);
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

}