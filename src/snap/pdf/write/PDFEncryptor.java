/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;

/**
 * The PDFEncyptor is a subclass of PDFSecurityHandler that is used
 * to encrypt strings and streams for pdf output.
 */
public class PDFEncryptor extends snap.pdf.PDFSecurityHandler {

  // The pdf encryption dictionary representation of this object
  Map     _encryptionDict;
  
/** 
 * Creates a new new PDF encryptor. Both the owner and user passwords are optional.
 */
public PDFEncryptor(byte fileID[], String ownerP, String userP, int permissionFlags)
{
    // set all reserved flags (spec says to do this, although acrobat doesn't)
    permissionFlags |= 0xFFFFF0C0;
    
    // clear bottom reserved flags
    permissionFlags &= ~3;
   
    // Initialize the superclass
    setEncryptionParameters(fileID, ownerP, userP, permissionFlags);
    
    // Create the encryption dict
    _encryptionDict = new Hashtable();
 
    // values specific to this handler type;
    _encryptionDict.put("R", 3);
    _encryptionDict.put("P", permissionFlags);
 
    // this is a bit of a hack -
    // The PDFFile will encrypt all strings inside objects, but the encryption dict itself shouldn't get encrypted.
    // When the file goes to output a string, it checks to see if it starts with a '(', in which case it will
    // encrypt the string.  We add a single space in front of the two password strings, so they don't get touched.
    _encryptionDict.put("O", " " + getOwnerPasswordEntry(ownerP, userP));
    _encryptionDict.put("U", " " + getUserPasswordEntry());
    
    // common values
    _encryptionDict.put("Filter", "/Standard");
    
    // WARNING - WARNING : using 128 bit keys means bumping the pdf version to 1.4
    _encryptionDict.put("V", 2);
    _encryptionDict.put("Length", 128);
}

/**
 * Returns the encryption dictionary.
 */
public Map getEncryptionDict()  { return _encryptionDict; }

/**
 * Encryption of strings and streams use the object number and generation number as part of the
 * encryption algorithm. For strings inside other objects, the object & generation number used
 * are the ones for the enclosing object.
 * When an object from the xref table is output, it calls startEncrypt() to save away these numbers
 * so all objects inside the xref object will use the right values.
 * Encrypt-decrypt is symmetric, so call superclass decrypt to create the encryption key
 */
public void startEncrypt(int oNum, int gNum)  { startDecrypt(oNum, gNum); }

/** 
 * Returns the contents of the pdf string, encrypted 
 */
public byte[] encryptString(String s) 
{
    int len = s.length();
    
    // If bogus string, just return
    if(len<=2 || s.charAt(0)!='(' || s.charAt(len-1)!=')') return new byte[0];
    
    // encrypt the ascii bytes
    byte buffer[] = new byte[len-2];
    for(int i=0; i<len-2; ++i) buffer[i]=(byte)s.charAt(i+1);
    arcfour_decrypt(buffer);
    
    // check for chars needing escapes
    int escapes = 0;
    byte escaped_bytes[] = {'(',')','\\','\n','\r','\t','\b','\f'};
    byte escape_sequence[] = {'(',')','\\','n','r','t','b','f'};
    for(int i=0; i<buffer.length; ++i) {
        for(int j=0; j<escaped_bytes.length; ++j)
            if(buffer[i]==escaped_bytes[j]) {
              ++escapes;
              break;
            }
    }
    
    // If any found, allocate new buffer of the right size and do the escapes
    if(escapes>0) {
        byte escaped_buf[] = new byte[buffer.length + escapes];
        for(int i=0, outp=0; i<buffer.length; ++i) { byte b = buffer[i];
            for(int j=0; j<escaped_bytes.length; ++j)
                if(b==escaped_bytes[j]) {
                    escaped_buf[outp++] = '\\';
                    b = escape_sequence[j];
                    break;
                }
            escaped_buf[outp++] = b;
        }
        return escaped_buf;
    }
    return buffer;
}

/**
 * Returns a new copy of the input buffer, encrypted.
 */
public byte[] encryptBytes(byte aBuffer[])
{
    // If no buffer bytes, just return
    if(aBuffer.length==0) return aBuffer;

    // Create new buffer, copy, encrypt and return
    byte newbuf[] = new byte[aBuffer.length];
    System.arraycopy(aBuffer, 0, newbuf, 0, aBuffer.length);
    arcfour_decrypt(newbuf);
    return newbuf;
}

}