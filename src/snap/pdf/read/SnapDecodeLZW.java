/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.io.ByteArrayOutputStream;

/**
 * LZW decompressor
 */
public class SnapDecodeLZW {
   
    // Bytes, offset and length
    byte bytes[];
    int offset, originalOffset, length;
    
    // Parameters
    int predictor, columns, colors, bits, early;

    int look;   // 1 character lookahead
    
    // LZW alogrithm vars
    int code_buf, num_bits;
    int clear_code, end_code; // values for Clear and End codes
    
    int input_code_size;
    int code_size;              // current actual code size
    int limit_code;             // 2^code_size
    int real_limit_code;        // limit_code adjusted for earlyChange
    int max_code;               // first unused code value
    boolean first_time;         // flags first call to LZWReadByte
    
    // Private state for LZWReadByte
    int oldcode;            // previous LZW symbol
    int firstcode;      // first byte of oldcode's expansion
    
    // LZW symbol table and expansion stack
    int symbol_head[];        // => table of prefix symbols
    short symbol_tail[];      // => table of suffix bytes
    short symbol_stack[];     // => stack for symbol expansions
    int sp = -1;              // stack pointer

    static int MAX_LZW_BITS = 12; // maximum LZW code size
    static int LZW_TABLE_SIZE = 1<<MAX_LZW_BITS; // # of possible LZW symbols


/** Returns a decoded byte array for given LZW encoded byte array. */
public static byte[] decode(byte bytes[], int offset, int length, int early)
{
    SnapDecodeLZW dec = new SnapDecodeLZW(bytes, offset, length, early);
    ByteArrayOutputStream out;
    int c;
    
    out = new ByteArrayOutputStream(length);
    while((c=dec.getChar()) >= 0)
        out.write(c);
    
    return out.toByteArray();
}

/** Creates a new LZW decoder for given bytes. */
public SnapDecodeLZW(byte bytes[], int offset, int length, int early1)
{
    this.bytes = bytes;
    this.offset = originalOffset = offset;
    this.length = length;
    early = early1;
    reset();
}

/** Resets decoder for new bytes. */
private void reset()
{
    offset = originalOffset;
    look = -1;
    if(sp<0)
        initLZW();
    reInitLZW();
}

/** Initialize LZW decoding state. */
private void initLZW()
{
    input_code_size = 8;
    symbol_head = new int[LZW_TABLE_SIZE];
    symbol_tail = new short[LZW_TABLE_SIZE];
    symbol_stack = new short[LZW_TABLE_SIZE];
    
    // GetCode initialization
    code_buf = 0; // nothing in the buffer
    num_bits = 0; // force buffer load on first call
    
    // LZWReadByte initialization:
    // compute special code values (note that these do not change later)
    clear_code = 1 << input_code_size;
    end_code = clear_code + 1;
    first_time = true;
}  

/** Reinitialize LZW state; shared code for startup and Clear processing. */
private void reInitLZW()
{
    code_size = input_code_size + 1;
    limit_code = clear_code << 1;   // 2^code_size
    real_limit_code = limit_code;
    if(early!=0)
        --real_limit_code;
    max_code = clear_code + 2; // first unused code value
    sp = 0; // init stack to empty
}

/** Returns some LZW thing. */
private int getCode()
{
    while(num_bits < code_size) {
        int c = (char)bytes[offset++];
        if(offset > originalOffset+length)
            return end_code;
        code_buf <<= 8;
        code_buf |= (c & 0xff);
        num_bits += 8;
    }
    
    int code = (code_buf >> (num_bits - code_size));
    code &= (1<<code_size)-1;
    num_bits -= code_size;
    return code;
}

/** Reads an LZW-compressed byte. */
private int readLZWByte()
{
    int code; // Current working code
    int incode; // Saves actual input code
    
    // First time, just eat expected Clear code(s) and return next code (should be raw byte)
    if(first_time) {
        first_time = false;
        code = clear_code; // Enables sharing code with Clear case
    }
    
    else {

        // If any codes are stacked from a previously read symbol, return them
        if(sp>0)
            return symbol_stack[--sp];
        // Time to read a new symbol
        code = getCode();
    }

    if(code == clear_code) {
    
        // Reinit state, swallow any extra Clear codes, and return next code (should be raw byte)
        reInitLZW();
        do {
            code = getCode();
        } while (code == clear_code);
    
        // Make sure it is raw byte
        if(code > clear_code)
            return -1;

        // Make firstcode, oldcode valid!
        firstcode = oldcode = code;
        return code;
    }

    // EOF
    if(code == end_code)
        return -1;

    // Got normal raw byte or LZW symbol
    incode = code; // Save for a moment
  
    // Special case for not-yet-defined symbol
    if(code >= max_code) {
    
        // code == max_code is OK; anything bigger is bad data
        if (code > max_code)
            incode = 0; // Prevent creation of loops in symbol table

        // This symbol will be defined as oldcode/firstcode
        symbol_stack[sp++] = (short)firstcode;
        code = oldcode;
    }

    // If it's a symbol, expand it into the stack
    while(code >= clear_code) {
        symbol_stack[sp++] = symbol_tail[code]; // Tail is a byte value
        code = symbol_head[code]; // Head is another LZW symbol
    }
    
    // At this point code just represents a raw byte
    firstcode = code; // save for possible future use

    // If there's room in table,
    if((code = max_code) < LZW_TABLE_SIZE) {
        // Define a new symbol = prev sym + head of this sym's expansion
        symbol_head[code] = oldcode;
        symbol_tail[code] = (short)firstcode;
        max_code++;
        
        // Is it time to increase code_size?
        if((max_code >= real_limit_code) && (code_size < MAX_LZW_BITS)) {
            code_size++;
            limit_code <<= 1; // keep equal to 2^code_size
            real_limit_code = limit_code;
            if(early!=0)
                --real_limit_code;
        }
    }
  
    oldcode = incode; // Save last input symbol for future use
    return firstcode; // Return first byte of symbol's expansion
}

/** Returns next char from LZW stream. */
private int getChar()
{
    int c = look;

    if(c==-1)
        c = readLZWByte();
    else look = -1;
    
    return c;
}

}
