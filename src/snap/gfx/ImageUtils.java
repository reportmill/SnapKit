package snap.gfx;

/**
 * Some Image utility methods.
 */
public class ImageUtils {

/**
 * Returns the type of the image bytes provided.
 */
public static String getImageType(byte bytes[])
{
    // Get number of bytes
    int size = bytes!=null? bytes.length : 0;
    
    // If GIF file, return "gif"
    if(size>3 && bytes[0]==(byte)'G' && bytes[1]==(byte)'I' && bytes[2]==(byte)'F')
        return "gif";
    
    // If JPEG file, return "jpg"
    if(size>2 && bytes[0]==(byte)0xff && bytes[1] == (byte)0xd8)
        return "jpg";
    
    // If PNG file, return "png"
    if(size>4 && bytes[0]==-119 && bytes[1]==(byte)'P' && bytes[2]==(byte)'N' && bytes[3]==(byte)'G')
        return "png";
    
    // If TIFF file (little endian), return "tiff"
    if(size>4 && bytes[0]==0x49 && bytes[1]==0x49 && bytes[2]==0x2a && bytes[3] == 0x00)
        return "tiff";
    
    // If TIFF file (big endian), return "tiff"
    if(size>4 && bytes[0]==0x4d && bytes[1]==0x4d && bytes[2]==0x00 && bytes[3]==0x2a)
        return "tiff";
    
    // If BMP file, return "bmp"
    if(size>2 && bytes[0]==0x42 && bytes[1]==0x4d)
        return "bmp";
    
    // If file type not recognized, return null
    return null;
}

/**
 * Returns an image for string and font.
 */
public static Image getImage(String aStr, Font aFont)
{
    int sw = (int)Math.ceil(aFont.getStringAdvance(aStr)), sh = (int)Math.ceil(aFont.getLineHeight());
    Image img = Image.get(sw+8,sh+8,true);
    Painter pntr = img.getPainter(); pntr.setColor(Color.BLACK); pntr.setFont(aFont);
    pntr.drawString(aStr, 4,aFont.getAscent()+4);
    return img;
}

}