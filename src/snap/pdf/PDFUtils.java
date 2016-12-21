/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.io.*;
import java.util.Map;
import java.util.zip.*;
import snap.pdf.read.SnapDecodeCCITTFax;
import snap.pdf.read.SnapDecodeLZW;

/*
 * Implementations of pdf decode filters
 */
public class PDFUtils {

/**
 * ASCII85 decoder.
 */
public static byte[] bytesForASCII85(byte bytes[], int offset, int length)
{
    int end=offset+length;
    int decode=0, matchlen=0;
    byte zzzz[] = {0,0,0,0};
    ByteArrayOutputStream out = new ByteArrayOutputStream(length*4/5);
    
    for(int i=offset; i<end; ++i) {
        byte c=bytes[i];
        //skip whitespace
        if ((c==' ') || (c=='\t') || (c=='\n') || (c=='\r') || (c=='\f'))
            continue;
        //valid ascii85 char
        if ((c>='!') && (c<='u')) {
            // ascii85 is 5 bytes interpreted as 5 base-85 digits
            decode = decode*85+c-'!';
            if (++matchlen==5) {
                // convert to 4 base-256 digits and reset
                out.write((decode>>24) & 0xff);
                out.write((decode>>16) & 0xff);
                out.write((decode>>8) & 0xff);
                out.write(decode & 0xff);
                decode=0;
                matchlen=0;
            }
        }
        else if ((c=='z') && (matchlen==0)) {
            // z is shorthand for 4 zero bytes
            out.write(zzzz,0,4);
        }
        else if ((c=='~') && (i<end-1) && (bytes[i+1]=='>')) {
            // ~> is the EOD marker
            break;
        }
        else throw new PDFException("Illegal character in ASCII85 stream");
    }
    
    //take care of odd bytes at the end
    if (matchlen==1)
        throw new PDFException("wrong number of characters in ASCII85 stream");
    // oh my god how freaky
    // instead of leaving the last bytes as the low digits of the base-85 number,
    // they are actually the high digits, with a 1 added to the least significant digit
    // thrown in for good measure.  Don't ask me.
    ++decode;
    for(int i=matchlen; i<5; ++i)
        decode*=85;
    
    for(int i=0; i<matchlen-1; ++i)
        out.write((decode>>((3-i)*8)) & 0xff);
   
    return out.toByteArray();
}

/**
 * ASCIIHex decoder.
 */
public static byte[] bytesForASCIIHex(byte bytes[], int offset, int length)
{
    // Due to whitespace & odd chars at end, outlength not necessarily inlength/2
    ByteArrayOutputStream out = new ByteArrayOutputStream((length+1)/2);
    int end = offset+length;
    int matchlen=0;
    int decode = 0;
    int nibble;
    
    for(int i=offset; i<end; i++) {
        byte c = bytes[i];
        
        //skip whitespace
        if ((c==' ') || (c=='\t') || (c=='\n') || (c=='\r') || (c=='\f'))
            continue;
        
        if ((c>='a') && (c<='f'))
            nibble = c-'a'+10;
        else if ((c>='A') && (c<='F'))
            nibble = c-'A'+10;
        else if ((c>='0') && (c<='9'))
            nibble = c-'0';
        else if (c=='>')
            break;
        else
            throw new PDFException("Illegal character in ASCIIHex stream");
        decode = (decode<<4) | nibble;
        if (++matchlen==2) {
            out.write(decode);
            decode=0;
            matchlen=0;
        }
    }
    if (matchlen==1)
        out.write(0);
    
    return out.toByteArray();
}

/**
 * ASCIIHex decoder for hex strings
 */
public static byte[] bytesForASCIIHex(String hstring)
{
    byte ascii[];
    try {
        ascii = hstring.getBytes("US-ASCII");
        if((ascii.length>1) && (ascii[0]=='<'))
            return bytesForASCIIHex(ascii,1,ascii.length-1);
        throw new PDFException("String is not a hexadecimal string");
    }
    // this should never happen
    catch(UnsupportedEncodingException uee) { throw new PDFException("Couldn't convert string to ascii"); }
}

/**
 * Returns the result of runnning the bytes through a particular filter (/FlateDecode, /LZW, /ASCII85Decode, etc.).
 */
public static byte[] getBytesDecoded(byte bytes[], int offset, int length, String aFilter, Map params)
{
    byte decoded[];
    
    // Get predictor parameters
    int predictor = 1, columns = 1, colors = 1, bits = 8;
    
    if(params!=null) {
        Object obj = params.get("Predictor");
            
        if(obj instanceof Number) {
            predictor = ((Number)obj).intValue();
            
            obj = params.get("Columns");
            if(obj instanceof Number)
                columns = ((Number)obj).intValue();
            obj = params.get("Colors");
            if(obj instanceof Number)
                colors = ((Number)obj).intValue();
            obj = params.get("BitsPerComponent");
            if(obj instanceof Number)
                bits = ((Number)obj).intValue();
        }
    }

    // Handle FlateDecode
    if(aFilter.equals("/FlateDecode")) {

        // Get input stream for compressed bytes, inflator stream for bytes and output stream for uncompressed bytes
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(bytes, offset, length);
        InflaterInputStream inflaterInStream = new InflaterInputStream(byteInStream, new Inflater(false));
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        
        // Transfer from inflaterStream to byteOutStream
        try {
            byte chunk[] = new byte[1024];
            for(int len=0; len>=0; len=inflaterInStream.read(chunk, 0, chunk.length))
                byteOutStream.write(chunk, 0, len);
    
            // get the buffer
            decoded = byteOutStream.toByteArray();
        }
        
        // Catch Exceptions
        catch(IOException e) { throw new PDFException(e); }
    }
    
    // Handle LZW
    else if(aFilter.equals("/LZWDecode") || aFilter.equals("/LZW")) {
    
        // Get extra parameters
        int early = 1;
        Object obj = params!=null? params.get("EarlyChange") : null;
        if(obj instanceof Number)
            early = ((Number)obj).intValue();

        // LZW decode
        decoded = SnapDecodeLZW.decode(bytes, offset, length, early);
    }
    
    // handle CCITTFaxDecode: Create decoder instance from parameters dictionary and decode stream
    else if(aFilter.equals("/CCITTFaxDecode")) {
        SnapDecodeCCITTFax ccittDecoder = SnapDecodeCCITTFax.createDecoder(params, bytes, offset, length);
        decoded = ccittDecoder.decodeStream();
    }
    
    // Handle ASCII85
    else if(aFilter.equals("/ASCII85Decode"))
        decoded = snap.pdf.PDFUtils.bytesForASCII85(bytes, offset, length);
    
    // Handle ASCIIHex
    else if(aFilter.equals("/ASCIIHexDecode"))
        decoded = snap.pdf.PDFUtils.bytesForASCIIHex(bytes, offset, length);
    
    // Complain if unsupported Decode
    else { System.err.println("Decompression type " + aFilter + " not yet supported."); return new byte[0]; }
    
    // apply predictor
    return postprocessBytesForPredictor(decoded, predictor, colors, bits, columns);
}

/**
 * Flate & LZW predictor algorithms
 * Predictors are simple algorithms performed on samples prior to compression in hopes of getting better
 * compression ratios. This method is called after lzw or flate decompression in order to undo the predictor.
 */
private static byte[] postprocessBytesForPredictor(byte buffer[], int predictor, int colors, int bitspersample, int width)
{
    if (predictor==1) return buffer;  // No prediction
    if (predictor==2) { // TIFF Predictor 2  
        // Sample actually represents a distance from same sample of pixel to it's left.
        // This makes rows of the same color collapse into zeros.
        int bitsperpixel=colors*bitspersample;
        int bytesperrow=(bitsperpixel*width+7)/8;
        int height = buffer.length/bytesperrow;
        int row,column,src=0;
        if (bitspersample==8) {
            for(row=0; row<height; ++row) {
                for(column=colors; column<bytesperrow; ++column)
                   buffer[src+column] += buffer[src+column-colors];
                src+=bytesperrow;
            }
        }
        else {
           System.err.println("Predictor not yet implemented for this image configuration");
           System.err.println(" bitspersample="+bitspersample+", samples per pixel="+colors);
        }
    }
    else if ((predictor>=10) && (predictor<=15)) {
        /* PNG Predictors
         * In images using PNG predictors, the predictor is selected on a per row basis.
         * The first byte in the row is the predictor selector for that row.
         * Also, unlike the TIFF predictor, which works on samples, PNG predictors always
         * work on bytes, regardless of the size of the components.
         * For a given byte, the algorithms select the 'corresponding' byte in the 
         * three neighboring pixels (left, above, above-left).
         * Since PNG predictors include an extra tag byte for each row, conversion is
         * done into a new buffer instead of in place.
         */
        /* The wierdest thing of all, however, is that the predictor's bitsPerSample/samplesPerPixel
         * doesn't necessarily match that of the image.
         * An 8 bit per sample rgb image can be compressed with a 4 bits per sample,
         * 3 sample per pixel predictor.  Seems like that would be pointless, but Illustrator
         * and Acrobat are happy to generate images like that.
         */
         
        int bitsperpixel=colors*bitspersample;
        int bytesperpixel=(bitsperpixel+7)/8;
        int dest_bytesperrow=(bitsperpixel*width+7)/8;
        int src_bytesperrow = dest_bytesperrow+1;  // +1 for tag byte
        int height = buffer.length/(src_bytesperrow);
        int row,column,src=0,dest=0;
        byte x,left,above,aboveleft;
        byte newbyte;
        
        // Since predictor pixelsize may not match the pixelsize of the image, there may be a final incomplete scanline
        if (buffer.length%src_bytesperrow != 0) {
            ++height; } //...
        
        // The real image buffer size is the size of the post-predicted buffer
        // minus one byte for each predicted scanline (the predictor tag byte)
        byte newbuffer[] = new byte[buffer.length - height];

        for(row=0; row<height; ++row) {
            predictor = buffer[src];
            // last scanline may not be complete
            if (row==height-1)
                src_bytesperrow=buffer.length-src;
            for(column=1; column<src_bytesperrow; ++column) {
                x = buffer[src+column];
                left = column-1<bytesperpixel ? 0 : newbuffer[dest+column-1-bytesperpixel];
                if (predictor==0) // None
                    newbyte=x;
                else if (predictor==1) // Sub
                    newbyte=(byte)(x+left);
                else {
                    above = row==0 ? 0 : newbuffer[dest+column-1-dest_bytesperrow];
                    if (predictor==2) // Up
                        newbyte=(byte)(x+above);
                    else if (predictor==3) // Average
                        newbyte=(byte)(x+(left+above)/2);
                    else if (predictor==4) { //Paeth
                        int p,pa,pb,pc,pr;
                        if ((row==0)||(column<bytesperpixel)) 
                            aboveleft = 0;
                        else 
                            aboveleft = newbuffer[dest+column-1-dest_bytesperrow-bytesperpixel];
                        //TODO : double check sign extension, since all java bytes are signed
                        p =  left+above-aboveleft;
                        pa = Math.abs(p-left) & 255;
                        pb = Math.abs(p-above) & 255;
                        pc = Math.abs(p-aboveleft) & 255;
                        if ((pa<=pb) && (pa<=pc)) 
                            pr = left;
                        else if (pb<=pc) 
                            pr = above;
                        else pr = aboveleft;
                        newbyte  = (byte)(x + pr);
                    }
                    else throw new PDFException("Illegal value for PNG predictor tag");
                }
                newbuffer[dest+column-1] = newbyte;
            }
           src+=src_bytesperrow;
           dest+=dest_bytesperrow;
        }
    buffer = newbuffer;
    }
    else System.err.println("Predictor algorithm #"+predictor+" not applied - image will look funny");
    
    return buffer;
}

}