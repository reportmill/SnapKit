package snap.util;


/**
 * This class has utility methods to code and decode into various different formats: hex, ASCII85, base 64.
 */
public class ASCIICodec {

    // A string of base64 chars 0123456789012345678901234567890123456789012345678901234567890123
    static final String _cvt = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    static final int    _fillchar = '=';

/**
 * Returns a hex string for given byte array.
 */
public static String encodeHex(byte theBytes[])
{
    // Declare array of hex chars
    char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    // Create char buffer
    char buffer[] = new char[theBytes.length*2];
    
    // Iterate over bytes
    for(int i=0, j=0, iMax=theBytes.length; i<iMax; i++) {
        short b = (short)(theBytes[i] & 0x00ff);
        buffer[j++] = hexChars[b>>4];
        buffer[j++] = hexChars[b&0xf];
    }
    
    // Return string
    return new String(buffer);
}

/**
 * Returns a byte array for given hex string.
 */
public static byte[] decodeHex(String aString)
{
    // Remove angle brackets
    if(aString.charAt(0)=='<') aString = aString.substring(1, aString.length()-1);
    
    // Create byte array
    byte bytes[] = new byte[aString.length()/2];

    // Iterate over chars
    for(int i=0, iMax=aString.length()/2; i<iMax; i++) {
        char c1 = aString.charAt(i*2), c2 = aString.charAt(i*2+1);
        int i1 = c1>='a'? c1 - 'a' + 10 : c1>='A'? c1 - 'A' + 10 : c1 - '0';
        int i2 = c2>='a'? c2 - 'a' + 10 : c2>='A'? c2 - 'A' + 10 : c2 - '0';
        bytes[i] = (byte)((i1*16 + i2) & 0xff);
    }

    // Return bytes
    return bytes;
}

/**
 * Returns a base64 string for given byte array.
 */
public static String encodeBase64(byte theBytes[])
{
    // Create buffer
    StringBuffer buffer = new StringBuffer((theBytes.length/3 + 1)*4);
    
    // Iterate over chars
    for(int i=0, iMax=theBytes.length; i<iMax; i++) {
        int c = (theBytes[i]>>2) & 0x3f;
        buffer.append(_cvt.charAt(c));
        c = (theBytes[i]<<4) & 0x3f;
        if(++i<iMax)
            c |= (theBytes[i]>>4) & 0x0f;

        buffer.append(_cvt.charAt(c));
        if(i<iMax) {
            c = (theBytes[i]<<2) & 0x3f;
            if(++i<iMax)
                c |= (theBytes[i]>>6) & 0x03;

            buffer.append(_cvt.charAt(c));
        }
        else {
            ++i;
            buffer.append((char)_fillchar);
        }

        if(i<iMax) {
            c = theBytes[i] & 0x3f;
            buffer.append(_cvt.charAt(c));
        }
        else buffer.append((char)_fillchar);
    }

    // Return string
    return buffer.toString();
}

/**
 * Returns a byte array for given base64 string.
 */
public static byte[] decodeBase64(String aString)
{
    // Create buffer
    StringBuffer buffer = new StringBuffer(aString.length()*3/4);
    
    // Iterate over chars
    for(int i=0, iMax=aString.length(); i<iMax; i++) {
        int c = _cvt.indexOf(aString.charAt(i)); i++;
        int c1 = _cvt.indexOf(aString.charAt(i));
        c = ((c<<2) | ((c1>>4) & 0x3));
        buffer.append((char) c);
        if(++i<iMax) {
            c = aString.charAt(i);
            if(_fillchar==c)
                break;

            c = _cvt.indexOf((char)c);
            c1 = ((c1<<4) & 0xf0) | ((c>>2) & 0xf);
            buffer.append((char)c1);
        }

        if(++i<iMax) {
            c1 = aString.charAt(i);
            if(_fillchar==c1)
                break;

            c1 = _cvt.indexOf((char)c1);
            c = ((c<<6) & 0xc0) | c1;
            buffer.append((char) c);
        }
    }
    
    // Create byte array for chars
    byte bytes[] = new byte[buffer.length()];
    for(int i=0; i<bytes.length; i++)
        bytes[i] = (byte)buffer.charAt(i);

    // Return bytes
    return bytes;
}

/**
 * Returns an ASCII85 string for given byte array.
 */
public static String encodeASCII85(byte theBytes[])
{
    int len = theBytes.length, blen = 0;
    byte buffer[] = new byte[(len+3)/4*5 + 2];
    
    for(int i=0; i<len; i+=4) {
        byte b1 = theBytes[i];
        byte b2 = i+1<len? theBytes[i+1] : 0;
        byte b3 = i+2<len? theBytes[i+2] : 0;
        byte b4 = i+3<len? theBytes[i+3] : 0;
        long word = (b1&0xFFL)<<24 | (b2&0xFF)<<16 | (b3&0xFF)<<8 | (b4&0xFF);
        if(word==0 && i+3<len) {
            buffer[blen++] = 'z';
            continue;
        }
        char c1 = (char)(word/52200625L); word -= c1*52200625L;
        char c2 = (char)(word/614125L); word -= c2*614125L;
        char c3 = (char)(word/7225L); word -= c3*7225L;
        char c4 = (char)(word/85L);
        char c5 = (char)(word%85L);
        
        buffer[blen++] = (byte)(c1 + '!');
        buffer[blen++] = (byte)(c2 + '!');
        if(i+1<len)
            buffer[blen++] = (byte)(c3 + '!');
        if(i+2<len)
            buffer[blen++] = (byte)(c4 + '!');
        if(i+3<len)
            buffer[blen++] = (byte)(c5 + '!');
    }

    // Write ASCII85 trailer
    buffer[blen++] = '~';
    buffer[blen++] = '>';
    
    // Return string for bytes
    return new String(buffer, 0, blen);
}

/**
 * Returns a byte array for given ASCII85 string.
 */
public static byte[] decodeASCII85(String aString)
{
    // Remove any non ASCII85 chars (anything not between '!' and 'u' (except 'z') and trailing "~>"
    // Should probably just modify character iteration loop to skip invalid chars
    StringBuffer sb = new StringBuffer(aString);
    
    // Trim everything after '~>' (EOD - End of Data)
    int index = aString.indexOf("~>");
    if(index>=0)
        sb.delete(index, sb.length());
    
    // Trim everything before '<~' (Not sure this ever happens)
    index = aString.indexOf("<~");
    if(index>=0) 
        sb.delete(0, index + 2);
    
    // Trim any invalid chars in stream (probably only whitespace)
    for(int i=sb.length()-1; i>=0; i--)
        if(sb.charAt(i)!='z' && (sb.charAt(i)<'!' || sb.charAt(i)>'u'))
            sb.delete(i, i+1);
    
    // Get trimmed chars
    aString = sb.toString();

    // Get string length and an RMData
    int len = aString.length();
    ByteArray data = new ByteArray(len);
    
    // Iterate over ASCII85 chars and write uncompressed data
    for(int i=0; i<len; i+=5) {
        int c1 = aString.charAt(i);
        if(c1=='z') {
            data.appendInt(0);
            i -= 4;
            continue;
        }
        else c1 -= '!';
        int c2 = aString.charAt(i+1) - '!';
        int c3 = i+2<len? aString.charAt(i+2) - '!' : 127;
        int c4 = i+3<len? aString.charAt(i+3) - '!' : 127;
        int c5 = i+4<len? aString.charAt(i+4) - '!' : 127;
        
        long word = c1*52200625L + c2*614125L + c3*7225L + c4*85L + c5;
        byte b4 = (byte)(word & 0xFF); word >>>= 8;
        byte b3 = (byte)(word & 0xFF); word >>>= 8;
        byte b2 = (byte)(word & 0xFF); word >>>= 8;
        byte b1 = (byte)(word & 0xFF); word >>>= 8;
        data.appendByte(b1);
        if(i+2<len)
            data.appendByte(b2);
        if(i+3<len)
            data.appendByte(b3);
        if(i+4<len)
            data.appendByte(b4);
    }
    
    // Return byte array
    return data.toByteArray();
}

}