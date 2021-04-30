package snap.gfx;
import snap.util.ArrayUtils;
import snap.util.ByteArray;
import snap.web.WebURL;

/**
 * Some Image utility methods.
 */
public class ImageUtils {

    // An image placeholder for missing images
    private static Image  _emptyImage;

    // An image URL for missing images
    private static WebURL  _emptyImageURL;

    /**
     * Returns the type of the image bytes provided.
     */
    public static String getImageType(byte[] bytes)
    {
        // Get number of bytes
        int size = bytes!=null ? bytes.length : 0;

        // If GIF file, return "gif"
        if (size>3 && bytes[0]==(byte)'G' && bytes[1]==(byte)'I' && bytes[2]==(byte)'F')
            return "gif";

        // If JPEG file, return "jpg"
        if (size>2 && bytes[0]==(byte)0xff && bytes[1] == (byte)0xd8)
            return "jpg";

        // If PNG file, return "png"
        if (size>4 && bytes[0]==-119 && bytes[1]==(byte)'P' && bytes[2]==(byte)'N' && bytes[3]==(byte)'G')
            return "png";

        // If TIFF file (little endian), return "tiff"
        if (size>4 && bytes[0]==0x49 && bytes[1]==0x49 && bytes[2]==0x2a && bytes[3] == 0x00)
            return "tiff";

        // If TIFF file (big endian), return "tiff"
        if (size>4 && bytes[0]==0x4d && bytes[1]==0x4d && bytes[2]==0x00 && bytes[3]==0x2a)
            return "tiff";

        // If BMP file, return "bmp"
        if (size>2 && bytes[0]==0x42 && bytes[1]==0x4d)
            return "bmp";

        // If file type not recognized, return null
        return null;
    }

    /**
     * Returns an image for string and font.
     */
    public static Image getImage(String aStr, Font aFont)
    {
        int sw = (int) Math.ceil(aFont.getStringAdvance(aStr));
        int sh = (int) Math.ceil(aFont.getLineHeight());
        Image img = Image.get(sw+8,sh+8,true);
        Painter pntr = img.getPainter();
        pntr.setColor(Color.BLACK);
        pntr.setFont(aFont);
        pntr.drawString(aStr, 4,aFont.getAscent() + 4);
        return img;
    }

    /**
     * Reads basic image info specifically optimized for JPEG images (without having to create Java RenderedImage).
     */
    public static ImageInfo getInfoJPG(byte[] theBytes)
    {
        // Create ImageInfo
        ImageInfo info = new ImageInfo();

        // Get reader for image bytes and index for current read index
        ByteArray reader = new ByteArray(theBytes);
        int index = 2;

        // Iterate over JPEG markers
        while (true) {

            // Get marker from first 4 bytes (just return if not valid marker)
            int marker = reader.bigUShortAtIndex(index); index += 2;
            if ((marker & 0xff00) != 0xff00)
                return info;

            // Get size from next 4 bytes
            int size = reader.bigUShortAtIndex(index); index += 2;

            // Decode spp, bps, width & height
            if (marker >= 0xffc0 && marker <= 0xffcf && marker != 0xffc4 && marker != 0xffc8) {
                info.spp = reader.getByte(index+5) & 0xff;
                info.bps = reader.getByte(index) & 0xff;
                info.width = reader.bigShortAtIndex(index + 3);
                info.height = reader.bigShortAtIndex(index + 1);
                return info;
            }

            // Decode DPI (APPx)
            else if (marker==0xffe0) {

                // APPx header must be larger than 14 bytes
                if (size<14)
                    return info;

                // Declare variable for 12 bytes
                byte[] data = new byte[12];

                // Read next 12 bytes
                reader.getBytes(index, index+12, data);

                // Declare APP0_ID
                final byte[] APP0_ID = { 0x4a, 0x46, 0x49, 0x46, 0x00 };

                // If arrays are equal, read dpi values
                if (ArrayUtils.equals(APP0_ID, data, 5)) {

                    if (data[7] == 1) {
                        float x = reader.bigShortAtIndex(index + 8);
                        float y = reader.bigShortAtIndex(index + 10);
                        if (x > 50) info.dpiX = x;
                        if (y > 50) info.dpiY = y;
                    }

                    else if (data[7] == 2) {
                        int x = reader.bigShortAtIndex(index + 8);
                        int y = reader.bigShortAtIndex(index + 10);
                        info.dpiX = (int)(x * 2.54f);
                        info.dpiY = (int)(y * 2.54f);
                    }
                }
            }

            // Any other marker should just be skipped
            index += (size - 2);
        }
    }

    /**
     * A class to hold Image Info.
     */
    public static class ImageInfo {

        // Image size
        public int  width, height;

        // Samples per pixel
        public int     spp;

        // Bits per sample
        public int     bps;

        // DPI horizontal, vertical
        public double  dpiX = 72, dpiY = 72;
    }

    /**
     * Returns an image place holder for missing images.
     */
    public static Image getEmptyImage()
    {
        // If already set, just return
        if (_emptyImage != null) return _emptyImage;

        // If URL set, load from URL
        if (_emptyImageURL != null) {
            Image img = Image.get(_emptyImageURL);
            return _emptyImage = img;
        }

        // Otherwise, just create empty image
        Image img = Image.get(10, 10, true);
        return _emptyImage = img;
    }

    /**
     * Returns an image place holder for missing images.
     */
    public static void setEmptyImageURL(WebURL aURL)
    {
        _emptyImageURL = aURL;
        _emptyImage = null;
    }
}