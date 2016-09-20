/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.util.zip.*;

/**
 * This class is like a StringBuffer for bytes instead of chars. It implements StringBuffer styled
 * append() methods, only everything is forced into ASCII bytes.
 * It is also enhanced with additional binary writing capabilities, so that many core types can be written as
 * binary.
 */
public class ByteArray implements Cloneable, Serializable {

    // The simple byte array
    byte      _bytes[] = null;
    
    // The offset into the byte array
    int       _offset = 0;
    
    // The length of the bytes
    int       _length = 0;
    
/**
 * Creates an empty ByteArray.
 */
public ByteArray()  { _bytes = new byte[32]; }

/**
 * Creates an empty ByteArray with the given capacity.
 */
public ByteArray(int aCapacity)  { _bytes = new byte[aCapacity]; }

/**
 * Creates an ByteArray with the given byte array.
 */
public ByteArray(byte theBytes[])  { _bytes = theBytes; _length = theBytes.length; }

/**
 * Creates an ByteArray with the given byte array (only to the given length).
 */
public ByteArray(byte theBytes[], int aLength)  { _bytes = theBytes; _length = aLength; }

/**
 * Returns the bytes in the data.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Returns the length of the data.
 */
public int length()  { return _length; }

/**
 * Returns the specific byte at the given index.
 */
public byte getByte(int index)  { return _bytes[index]; }

/**
 * Copies the bytes from this data to the given byte array.
 */
public void getBytes(int srcBegin, int srcEnd, byte destBytes[])  { getBytes(srcBegin, srcEnd, destBytes, 0); }

/**
 * Copies the bytes from this data to the given byte array.
 */
public void getBytes(int srcBegin, int srcEnd, byte destBytes[], int destBegin)
{
    System.arraycopy(_bytes, srcBegin, destBytes, destBegin, srcEnd - srcBegin);
}

/**
 * Returns an input stream of this data's bytes.
 */
public InputStream getInputStream()  { return new ByteArrayInputStream(_bytes, 0, _length); }

/**
 * Append common types as ASCII.
 */
public ByteArray append(char c)
{
    if(c>255) {
        System.err.println("RMPDFWriter: Attempt to write non-ASCII char: " + c);
        c = 'X';
    }
    setCapacity(_length + 1);
    _bytes[_length] = (byte)c;
    _length++;
    return this;
}

/**
 * Append the given char array.
 */
public ByteArray append(char str[])  { return append(str, 0, str.length); }

/**
 * Appends the given char array.
 */
public ByteArray append(char str[], int offset, int length)
{
    for(int i=offset, iMax=offset+length; i<iMax; i++) append(str[i]);
    return this;
}

/**
 * Appends the given double.
 */
public ByteArray append(double d)  { return append((float)d); }

/**
 * Appends the given float.
 */
public ByteArray append(float f)
{
    // Make sure float is valid
    if(Float.isNaN(f) || Float.isInfinite(f)) {
        System.err.println("RMData: Attempt to write " + (Float.isNaN(f)? "NaN" : "Infinity"));
        f = 0;
    }
    
    // Break the float into a 'whole' and 'fraction', and print 'whole' as a normal integer (if non-zero)
    int whole = (int)f, fraction = (int)((f - whole)*10000 + .5f);
    if(whole!=0 || fraction==0)
        append(whole);
        
    // If fraction is zero, just return
    if(fraction==0)
        return this;
    
    // If fraction is negative, make positive and write minus sign if needed
    if(fraction<0) {
        fraction = -fraction;
        if(whole==0)
            append('-');
    }
    
    // Write radix
    append('.');
    
    // Load fraction digits into _charbuffer and write leading zeros if missing
    int charPos = loadCharBuffer(fraction);
    for(int i=8; i<charPos; i++)
        append('0');
        
    // Get default bufferEnd and trim trailing zeros
    int bufferEnd = 12;
    while(bufferEnd>charPos+1 && _charbuffer[bufferEnd-1]=='0')
        bufferEnd--;
        
    // Write _charbuffer digits
    return append(_charbuffer, charPos, bufferEnd-charPos);
}

/**
 * Appends an int string to the data.
 */
public ByteArray append(int anInt) { int i = loadCharBuffer(anInt); return append(_charbuffer, i, 12-i); }

/**
 * Appends an object's string representation to the data.
 */
public ByteArray append(Object o) { return append(o.toString()); }

/**
 * Appends a string to the data.
 */
public ByteArray append(String aString) { int i = loadCharBuffer(aString); return append(_charbuffer, 0, i); }

/**
 * Appends raw binary from given data to data.
 */
public ByteArray append(ByteArray data) { return append(data._bytes, 0, data._length); }

/**
 * Appends raw binary from byte array to data.
 */
public ByteArray append(byte bytes[]) { return append(bytes, 0, bytes.length); }

/**
 * Appends raw binary from byte array to data.
 */
public ByteArray append(byte bytes[], int offset, int length)
{
    setCapacity(_length + length);
    System.arraycopy(bytes, offset, _bytes, _length, length);
    _length += length;
    return this;
}

/**
 * Inserts raw binary from given data to data.
 */
public void insert(int index, ByteArray data) { insert(index, data._bytes, 0, data._length); }

/**
 * Inserts raw binary from byte array to data.
 */
public void insert(int index, byte bytes[]) { insert(index, bytes, 0, bytes.length); }

/**
 * Inserts raw binary from byte array to data.
 */
public void insert(int index, byte bytes[], int offset, int length) { replace(index, index, bytes, offset, length); }

/**
 * Replace raw binary from byte array in data.
 */
public void replace(int start, int end, byte bytes[], int offset, int length)
{
    int rangeLength = end - start;
    int newLength = _length - rangeLength + length;
    int endBytesCount = _length - end;

    setCapacity(newLength);
    
    // Scoot end bytes to new end
    if(endBytesCount>0)
        System.arraycopy(_bytes, _length - endBytesCount, _bytes, newLength - endBytesCount, endBytesCount);

    // Add bytes at start
    if(length>0)
        System.arraycopy(bytes, offset, _bytes, start, length);
        
    _length = newLength;
}

/**
 * Appends byte to data.
 */
public void appendByte(int aByte)
{
    setCapacity(_length + 1);
    _bytes[_length] = (byte)aByte;
    _length++;
}

/**
 * Appends unsigned byte to data.
 */
public void appendUByte(int aByte)
{
    setCapacity(_length + 1);
    _bytes[_length] = (byte)aByte;
    _length++;
}

/**
 * Appends short to data.
 */
public void appendShort(int aShortInt)
{
    setCapacity(_length + 2);
    short aShort = (short)aShortInt;
    _bytes[_length] = (byte)((aShort & 0xff00) >>> 8);
    _bytes[_length+1] = (byte)((aShort & 0xff));
    _length += 2;
}

/**
 * Appends little endian short to data.
 */
public void appendLittleShort(int aShortInt)
{
    setCapacity(_length + 2);
    short aShort = (short)aShortInt;
    _bytes[_length] = (byte)((aShort & 0xff));
    _bytes[_length+1] = (byte)((aShort & 0xff00) >>> 8);
    _length += 2;
}

/**
 * Appends little endian unsigned short to data.
 */
public void appendLittleUShort(int aShort)
{
    setCapacity(_length + 2);
    _bytes[_length] = (byte)aShort;
    _bytes[_length+1] = (byte)(aShort >>> 8);
    _length += 2;
}

/**
 * Appends int to data.
 */
public void appendInt(int anInt)
{
    setCapacity(_length + 4);
    _bytes[_length] = (byte)((anInt & 0xff000000) >>> 24);
    _bytes[_length+1] = (byte)((anInt & 0xff0000) >>> 16);
    _bytes[_length+2] = (byte)((anInt & 0xff00) >>> 8);
    _bytes[_length+3] = (byte)((anInt & 0xff));
    _length += 4;
}

/**
 * Appends little endian int to data.
 */
public void appendLittleInt(int anInt)
{
    setCapacity(_length + 4);
    _bytes[_length] = (byte)((anInt & 0xff));
    _bytes[_length+1] = (byte)((anInt & 0xff00) >>> 8);
    _bytes[_length+2] = (byte)((anInt & 0xff0000) >>> 16);
    _bytes[_length+3] = (byte)((anInt & 0xff000000) >>> 24);
    _length += 4;
}

/**
 * Appends little endian unsigned int to data.
 */
public void appendLittleUInt(long aUInt)
{
    int anInt = (int)aUInt;
    setCapacity(_length + 4);
    _bytes[_length] = (byte)((anInt & 0xff));
    _bytes[_length+1] = (byte)((anInt & 0xff00) >>> 8);
    _bytes[_length+2] = (byte)((anInt & 0xff0000) >>> 16);
    _bytes[_length+3] = (byte)((anInt & 0xff000000) >>> 24);
    _length += 4;
}

/**
 * Sets little endian short to data at given index.
 */
public void setLittleShortAtIndex(int aShortInt, int byteIndex)
{
    short aShort = (short)aShortInt;
    _bytes[byteIndex++] = (byte)((aShort & 0xff));
    _bytes[byteIndex] =   (byte)((aShort & 0xff00)>>> 8);
}

/**
 * Sets little endian unsigned short to data at given index.
 */
public void setLittleUShortAtIndex(int aShort, int byteIndex)
{    
    _bytes[byteIndex++] = (byte)aShort;
    _bytes[byteIndex] =   (byte)(aShort >>> 8);
}

/**
 * Sets little endian int to data at given index.
 */
public void setLittleIntAtIndex(int anInt, int byteIndex)
{
    _bytes[byteIndex++] = (byte)((anInt & 0xff));
    _bytes[byteIndex++] = (byte)((anInt & 0xff00) >>> 8);
    _bytes[byteIndex++] = (byte)((anInt & 0xff0000) >>> 16);
    _bytes[byteIndex] =   (byte)((anInt & 0xff000000) >>> 24);
}

/**
 * Inserts little endian unsigned int to data at given index.
 */
public void insertLittleUIntAtIndex(long aUInt, int byteIndex)
{
    int anInt = (int)aUInt;
    byte newBytes[] = new byte[4];
    newBytes[0] = (byte)anInt;
    newBytes[1] = (byte)((anInt & 0xff00) >>> 8);
    newBytes[2] = (byte)((anInt & 0xff0000) >>> 16);
    newBytes[3] = (byte)(anInt >>> 24);
    insert(byteIndex, newBytes);
}

/**
 * Returns the short at the given index (assumed to be in big endian format).
 */
public short bigShortAtIndex(int index)
{
    int b0 = _bytes[_offset+index], b1 = (char)_bytes[_offset+index+1] & 255;
    short value = (short)((b0<<8) + b1);
    return value;
}

/**
 * Returns the big endian unsigned short at the given byte index.
 */
public int bigUShortAtIndex(int index)
{
    int b0 = (char)_bytes[_offset+index] & 255, b1 = (char)_bytes[_offset+index+1] & 255;
    int value = (b0<<8) + b1;
    return value;
}

/**
 * Returns the little endian short at the given byte index.
 */
public short littleShortAtIndex(int index)
{
    int b0 = (char)_bytes[_offset+index] & 255, b1 = _bytes[_offset+index+1];
    short value = (short)((b1<<8) + b0);
    return value;
}

/**
 * Returns the little endian unsigned short at the given byte index.
 */
public int littleUShortAtIndex(int index)
{
    int b0 = (char)_bytes[_offset+index] & 255, b1 = (char)_bytes[_offset+index+1] & 255;
    int value = (b1<<8) + b0;
    return value;
}

/**
 * Returns the big endian unsigned int at the given byte index.
 */
public long bigUIntAtIndex(int index)
{
    int b0 = _bytes[_offset+index] & 255, b1 = _bytes[_offset+index+1] & 255;
    int b2 = _bytes[_offset+index+2] & 255, b3 = _bytes[_offset+index+3] & 255;
    long value = (b0<<24) | (b1<<16) | (b2<<8) | b3;
    return value & 0x00000000ffffffffl;
}

/**
 * Returns the little endian unsigned int at the given byte index.
 */
public long littleUIntAtIndex(int index)
{
    long value = (char)_bytes[_offset+index] & 255;
    value |= (((char)_bytes[_offset+index + 1] & 255) << 8);
    value |= (((char)_bytes[_offset+index + 2] & 255) << 16);
    value |= (((char)_bytes[_offset+index + 3] & 255) << 24);
    return value;
}

/**
 * Returns the char array at the given byte index with the given length.
 */
public char[] charArrayAt(int index, int len)
{
    char values[] = new char[len];
    for(int i=0; i<len; i++) values[i] = (char)bigUShortAtIndex(index+i*2);
    return values;
}

/**
 * Returns the array of big endian shorts at the given byte index with the given length.
 */
public short[] bigShortArrayAt(int index, int len)
{
    short values[] = new short[len];
    for(int i=0; i<len; i++) values[i] = bigShortAtIndex(index+i*2);
    return values;
}

/**
 * Returns the array of big endian unsigned shorts at the given byte index with the given length.
 */
public int[] bigUShortArrayAt(int index, int len)
{
    int values[] = new int[len];
    for(int i=0; i<len; i++) values[i] = bigUShortAtIndex(index+i*2);
    return values;
}

/**
 * Returns the size of the internal byte array.
 */
public int getCapacity()  { return _bytes.length; }

/**
 * Makes sure internal byte array is at least a given size.
 */
public void setCapacity(int aMinimumCapacity)
{
    if(_bytes.length < aMinimumCapacity) {
        int powerOfTwoLength = 8;
        while(aMinimumCapacity > powerOfTwoLength)
            powerOfTwoLength *= 2;
        byte newBytes[] = new byte[powerOfTwoLength];
        System.arraycopy(_bytes, 0, newBytes, 0, _bytes.length);
        _bytes = newBytes;
    }
}

/**
 * Loads _charbuffer with characters for aString.
 */
private int loadCharBuffer(String aString)
{
    int len = aString.length();
    if(_charbuffer==null || _charbuffer.length < len) _charbuffer = new char[Math.max(128,len)];
    aString.getChars(0, len, _charbuffer, 0);
    return len;
}
    
/**
 * Loads _charbuffer with characters for anInt.
 */
private int loadCharBuffer(int anInt)
{
    if(_charbuffer==null) _charbuffer = new char[128];
    boolean negative = anInt<0; if(negative) anInt = -anInt;
    int charPos = 12;

    // Pick off integer least significant decimal digits until integer is zero
    do {
        int digit = anInt%10;
        _charbuffer[--charPos] = (char)('0' + digit);
        anInt = anInt/10;
    } while(anInt!=0);

    // If negative, add minus sign
    if(negative)
        _charbuffer[--charPos] = '-';

    // Return char position
    return charPos;
}

// A scratch character buffer for constructing number strings
char      _charbuffer[];

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and class and get other
    if(anObj==this) return true;
    if(!getClass().isInstance(anObj)) return false;
    ByteArray other = (ByteArray)anObj;
    
    // Check length, bytes
    if(other._length!=_length) return false;
    if(!ArrayUtils.equals(other._bytes, _bytes, _length)) return false;
    return true;  // Return true since all checks passed
}

/**
 * Standard hashcode implementation.
 */
public int hashCode()  { return _bytes!=null? _bytes.hashCode() : 0; }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    ByteArray clone = null; try { clone = (ByteArray)super.clone(); } catch(Exception e) { } // Do normal clone 
    clone._bytes = new byte[_length];  // Copy bytes
    System.arraycopy(_bytes, 0, clone._bytes, 0, _length);
    clone._charbuffer = null;  // Clear scratch char buffer
    return clone;  // Return clone
}

/**
 * Returns the string initialized from the data's bytes (ISO Latin).
 */
public String toString()  { return toString(0, _length, "ISO-8859-1"); }

/**
 * Returns the string initialized from the data's bytes in the given encoding.
 */
public String toString(int start, int end, String encoding)
{
    try { return new String(_bytes, start, end - start, encoding); }
    catch(Exception e) { return new String(_bytes, start, end - start); }
}

/**
 * Returns a byte array for the bytes in the data.
 */
public byte[] toByteArray()  { return toByteArray(0, _length); }

/**
 * Returns a byte array for the bytes in the data.
 */
public byte[] toByteArray(int start, int end)
{
    byte bytes[] = new byte[end-start];
    getBytes(start, end, bytes);
    return bytes;
}

/**
 * Flate compresses data.
 */
public void flateCompressedData()
{
    Deflater deflater = new Deflater();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    DeflaterOutputStream defStream = new DeflaterOutputStream(outStream, deflater);
    
    try {
        defStream.write(_bytes, 0, _length);
        defStream.close();
        _bytes = outStream.toByteArray(); _length = _bytes.length;
    }
    catch(Exception e) { throw new RuntimeException("Error occurred durring Deflating" + e); }
}

}