/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import snap.util.ASCIICodec;

/**
 * Implementation of the Adobe Standard security handler using the Sun security extensions for md5 & rc4.
 */
public class PDFSecurityHandler {

    // The encryption key, calculated once per file
    byte            _encryption_key[];
    
    // The RC4 or AES decryption cipher
    Cipher          _decrypter;
    
    // A subclass of the Key interface that holds a buffer for the per-object keys
    PDFKey          _decrypter_key;
    
    // Standard security algorithm revision number
    int             _revision;
    
    // Permissions
    int             _access_permissions;
    
    // Even though the metadata is never read, the fact that it gets encrypted has an effect on encryption algorithm
    boolean         _encrypt_metadata;
    
    // fileID bytes - Used to calculate key in rev 2&3, but also used in password authentication in rev 3 and greater
    byte            _fileID[];
    
    // Used in standard security handler to pad out passwords smaller than 32 bytes.
    // Declared as an array of ints so we can specify them in hex without casting. From the PDF 1.6 spec, p. 100
    static final int PASSWORDPAD[] = {
        0x28, 0xbf, 0x4e, 0x5e, 0x4e, 0x75, 0x8a, 0x41, 0x64, 0x00, 0x4e, 0x56, 0xff, 0xfa, 0x01, 0x08,
        0x2e, 0x2e, 0x00, 0xb6, 0xd0, 0x68, 0x3e, 0x80, 0x2f, 0x0c, 0xa9, 0xfe, 0x64, 0x53, 0x69, 0x7a
    };
    
    // bit positions for the user access permissions
    public static final int PRINTING_ALLOWED = 4;
    public static final int MODIFICATIONS_ALLOWED = 8;
    public static final int EXTRACT_TEXT_AND_IMAGES_ALLOWED = 16;
    public static final int ADD_ANNOTATIONS_ALLOWED = 32;
    public static final int FILL_IN_FORMS_ALLOWED = 256;
    public static final int ACCESSABILITY_EXTRACTS_ALLOWED = 512;
    public static final int ASSEMBLE_DOCUMENT_ALLOWED = 1024;
    public static final int MAXIMUM_RESOLUTION_PRINTING_ALLOWED = 2048;

/**
 * Returns an instance of the appropriate PDFSecurityHandler subclass. Since any number of different security
 * handlers can be used in pdf, callers should use this method rather than instantiating a handler directly.
 * This also does authentication with the handler, possibly raising a panel to ask for passwords, keys, etc.
 */
public static PDFSecurityHandler getInstance(Map encryptionDict, List <String> fileID, double pdfversion)
{
    // Get security handler instance
    String hname = (String)encryptionDict.get("Filter");
    Class hclass = hname.equals("/Standard")? PDFSecurityHandler.class : null;
    PDFSecurityHandler handler; try { handler = (PDFSecurityHandler)hclass.newInstance(); }
    catch(Exception e) { throw new PDFException("Couldn't create security manager : " + hname + " - " + e); }
    
    // Now try to initialize it with a null password. If it throws badpassword exception, bring up a panel.
    // Probably should let the manager in on this, since it could be passwords, certificates, whatnot.
    handler.init(encryptionDict, fileID, pdfversion, null);
    /*String pass = null, msg = "File requires a password";
    while(true) try { handler.init(encryptionDict, fileID, pdfversion, pass); break; }
    catch(PDFBadPasswordException e) {
        JPasswordField pfield = new JPasswordField(30);
        int choice = JOptionPane.showConfirmDialog(null, pfield, msg, JOptionPane.OK_CANCEL_OPTION);
        if(choice==JOptionPane.CANCEL_OPTION) throw new PDFException("Cancelled");
        pass = new String(pfield.getPassword()); msg = "The password incorrect.  Try again"; }*/
    
    // Return handler
    return handler;
}

/**
 * Main security handler initialization.  
 * Calculates everything necessary to decrypt the pdf objects and performs authorization if necessary.
 */
public void init(Map encryptDict, List <String> fileID, double pdfversion, String uPass) throws PDFBadPasswordException
{
    // Initialize the security handler by calculating the encryption key
    _encryption_key = getEncryptionKey(encryptDict, fileID, uPass);
    
    // Intialize a Key buffer
    _decrypter_key = new PDFKey(_encryption_key.length);
    
    // Get the stream cipher (pre 1.6 files used RC4, 1.6 uses AES)
    try {
        
        // RC4 is implemented as ARCFOUR, but isn't included until SunJCE 1.5, in J2SE5.0
        if(pdfversion < 1.6)
            _decrypter = null;  // use our own arcfour implementation instead of Cipher.getInstance("ARCFOUR");
        
        // "CBC" specifies cipher block chaining. decrypter will need to be initialized with
        // an IvParameterSpec and an initialization vector from the first 16 bytes of the stream.
        else _decrypter = Cipher.getInstance("AES/CBC");
    }
    
    // Catch excpetions
    catch(NumberFormatException nfe) { throw new PDFException("Error getting pdf file version"); }
    catch(NoSuchAlgorithmException nsae) { throw new PDFException(nsae); }
    catch(NoSuchPaddingException nspe) { throw new PDFException(nspe); }
    
    // Will throw an exception if can't authenticate
    authenticateUserPassword(PDFSecurityHandler.getBytesForEncryptionEntry(encryptDict, "U"));
}

/**
 * throws an exception if authentication fails
 * uEntry is the value of /U in the encryption dictionary, and the key is assumed to have already been calculated.
 */
void authenticateUserPassword(byte uEntry[]) throws PDFBadPasswordException
{
    // This is an error in the file, not a password mismatch
    if(uEntry.length != PASSWORDPAD.length) 
        throw new PDFException("Illegal value in encryption dictionary");
    
    // Get bytes for encrypted user password and throw exception if not equal the /U entry from the dictionary
    byte encrypted[] = getUserPasswordEntryBytes();
    for(int i=0;i<encrypted.length;++i)
        if(encrypted[i] != uEntry[i]) 
            throw new PDFBadPasswordException("User password incorrect");
}

/**
 * Initialization for encryption.  Sets parameters to revision=3, 128 bit keys.
 */
public void setEncryptionParameters(byte fileID[], String ownerP, String userP, int permissionFlags)
{
    // make a local copy of the fileID
    _fileID = copyOf(fileID, fileID.length);
    
    // Set encryption parameters
    _revision = 3; _access_permissions = permissionFlags; _encrypt_metadata = true;
    
    // calculate /O entry
    byte oBytes[] = getOwnerPasswordEntryBytes(ownerP, userP);
    
    // Initialize the security handler by calculating the encryption key
    _encryption_key = getEncryptionKey(oBytes, userP, 16);

    // Initialize a Key buffer
    _decrypter_key = new PDFKey(_encryption_key.length);
}

/**
 *  Gets the key length and algorithm from the dictionary and creats the key
 */
public byte[] getEncryptionKey(Map encrypt, List <String> fileID, String uPass)
{
     // First figure out the length of the key
     Object obj = encrypt.get("V");
     int vers = obj==null? 0: ((Number)obj).intValue();
     int keybytes = 0;
 
     // only handle V==1 or V==2
     if(vers==1) keybytes = 5;
     else if(vers==2) {
         int keybits = ((Number)encrypt.get("Length")).intValue();
         if(keybits%8==0) keybytes = keybits/8;
     }
     
     // Get/set Revision
     obj = encrypt.get("R");
     if(!(obj instanceof Number)) throw new PDFException("Missing values in encryption dictionary");
     _revision = ((Number)obj).intValue();
     
     // Get/set Permissions
     obj = encrypt.get("P");
     if(!(obj instanceof Number)) throw new PDFException("Missing values in encryption dictionary");
     _access_permissions = ((Number)obj).intValue();
     
     // Get/set EncryptMetadata
     obj = encrypt.get("EncryptMetadata");
     _encrypt_metadata = obj instanceof Boolean? ((Boolean)obj).booleanValue() : true;
     
     // Set FileId
     if(fileID.size()>0)
         _fileID = PDFUtils.bytesForASCIIHex(fileID.get(0));
     
     // Pull the /O entry out of the dictionary as an array of bytes
     byte oEntry[] = PDFSecurityHandler.getBytesForEncryptionEntry(encrypt, "O");
 
     // supports 40-128 bit keys
     if(keybytes>=5 && keybytes<=16 && (_revision==2 || _revision==3))
         return getEncryptionKey(oEntry, uPass, keybytes);

     throw new PDFException("Unhandled encryption method : " + encrypt);
}

/**
 * Uses algorithm 3.2 from pdf spec to generate a key from the file.
 */
public byte[] getEncryptionKey(byte oEntry[], String uPass, int keyLen)
{
    // Create MD5 MessageDigest to generate hash value
    MessageDigest md5; try { md5 = MessageDigest.getInstance("MD5"); }
    catch(NoSuchAlgorithmException e) { throw new PDFException(e); }
    
    // Step 1 + 2: fill buffer with user password padded to 32 bytes and append to MD5
    byte upad[] = pad(uPass);
    md5.update(upad);
     
    // Step 3: append owner password entry to MD5
    md5.update(oEntry);
     
    // Step 4: append 4 bytes of P, low->high. Spec claims that bits 13-32 of permissions are reserved and should
    // always be 1. However, Acrobat creates files where that isn't the case. I use values exactly as they appear in
    // file, but if the bits need to be forced on, do this first:  p |= 0xfffff000
    for(int i=0, p=_access_permissions; i<4; i++, p>>=8) md5.update((byte)(p&0xff));
     
    // Step 5: pass first element of file identifier array
    if(_fileID!=null)
        md5.update(_fileID);
     
    // Step 6: revision 3 only
    if(_revision>=3 && !_encrypt_metadata) 
        for(int i=0; i<4; ++i)
            md5.update((byte)0xff);
     
    // Step 7: finish the hash
    byte ekey[] = md5.digest();
    
    // Step 8: revision 3 only: rehash 50 times (seems like overkill)
    if(_revision>=3) for(int i=0; i<50; i++) {
        md5.reset();
        md5.update(ekey);
        ekey = md5.digest();
    }
        
    // Trim key to KeyLen if needed and return
    if(ekey.length>keyLen) ekey = copyOf(ekey, keyLen);
    return ekey;
}

/** 
 * Returns the string corresponding to the /U entry. The encryption key is assumed to be calculated already.
 */
public String getUserPasswordEntry()
{
    byte uentry[] = getUserPasswordEntryBytes();
    return getPDFStringForBytes(uentry);
}

/** 
 * Returns the string corresponding to the /O entry.
 */
public String getOwnerPasswordEntry(String ownerP, String userP)
{
    byte oentry[] = getOwnerPasswordEntryBytes(ownerP, userP);
    return getPDFStringForBytes(oentry);
}

/**
 * Return User password entry bytes.
 */
public byte[] getUserPasswordEntryBytes()
{
    // Get encryption key
    byte encryptKey[] = _encryption_key;
    
    // Get the full padding string
    byte pad[] = pad(null), uentry[];
    
    // Revision 2: RC4 the 32 byte padding string with the encryption key (which was calculated using user password)
    if(_revision==2) {
        arcfour_decrypt(pad, encryptKey);
        uentry = pad;
    }
    
    // Revision 3 or greater
    else if(_revision>=3) {
        
        // Step 1 + 2: Create MD5 MessageDigest to generate hash value and append padded user password
        MessageDigest md5; try { md5 = MessageDigest.getInstance("MD5"); }
        catch(NoSuchAlgorithmException e) { throw new PDFException(e); }
        md5.update(pad);
        
        // Step 3: Append FileID
        if(_fileID != null)
            md5.update(_fileID);
        
        // Step 4: finish the hash & RC4 the first 16 bytes with the key
        uentry = md5.digest(); if(uentry.length>16) uentry = copyOf(uentry, 16);
        arcfour_decrypt(uentry, encryptKey);
        
        // Step 5: rc4 19 more times (who are these people?)
        int klen = encryptKey.length;
        byte step5Key[] = new byte[klen];
        for(int i=1; i<=19; i++) {
            for(int j=0; j<klen; j++) step5Key[j] = (byte)(encryptKey[j]^i);
            arcfour_decrypt(uentry, step5Key);
        }
        
        // Step 6: Append 16 bytes of arbitrary padding
        uentry = copyOf(uentry, 32);
    }
    
    // Should never happen
    else throw new PDFException("Unknown revision number in encryption dictionary");
    
    // Return encrypted
    return uentry;
}

/**
 * For encryption - generate a /O encryption entry for an encryption dictionary (from Algorithm 3.3 of the spec)
 * This routine uses Revision 3 of the algorithm and 128 bit keys.
 */
public byte[] getOwnerPasswordEntryBytes(String ownerP, String userP)
{
    // Create MD5 MessageDigest to generate hash value
    MessageDigest md5; try { md5 = MessageDigest.getInstance("MD5"); }
    catch(NoSuchAlgorithmException e) { throw new PDFException(e); }
    
    // Step 1 + 2: get padded owner password and append to MD5
    byte opad[] = pad(ownerP!=null? ownerP : userP);
    md5.update(opad);
    
    // Step 3: Rehash 50 times
    for(int i=0; i<50; ++i) {
        byte digest[] = md5.digest();
        md5.reset();
        md5.update(digest);
    }
    
    // Step 4: Make an rc4 key with 16 bytes (128 bits) of the md5
    int klen = 16;
    byte rc4Key[] = md5.digest(); if(rc4Key.length>klen) rc4Key = copyOf(rc4Key, klen);
    
    // Step 5: Pad out user password
    byte oentry[] = pad(userP);
    
    // Step 6: Encrypt padded user password with rc4
    arcfour_decrypt(oentry, rc4Key);
    
    // Step 7 - 18 more rc4s with a little xor dance
    byte step7Key[] = new byte[klen];
    for(int i=1; i<=19; ++i) {
        for(int j=0; j<klen; ++j) step7Key[j] = (byte)(rc4Key[j]^i);
        arcfour_decrypt(oentry, step7Key);
    }
    
    // Return owner password entry bytes
    return oentry;
}

/**
 * Decrypt strings & streams using the algorithm from the encryption dictionary.
 */
public Object decryptObject(Object o, int objNum, int generationNum) 
{
    startDecrypt(objNum, generationNum);
    return decryptDeep(o);
}

/**
 * Decrypt.
 */
public Object decryptDeep(Object o) 
{
    // Handle PDFStream
    if(o instanceof PDFStream) { PDFStream oStream = (PDFStream)o;
        oStream._dict = (Map)decryptDeep(oStream._dict);  // decrypt stream dictionary (recursive)
        if(_decrypter==null) arcfour_decrypt(oStream.getBytes());  // decrypt stream in place
        else { }  // Use cipher for AES
    }
    
    // Handle Map: decrypt the keys and other crap (recursive)
    else if(o instanceof Map) { Map map = (Map)o;
        Map newMap = new Hashtable(map.size()); o = newMap;
        for(Map.Entry entry : (Set <Map.Entry>)map.entrySet())
            newMap.put(entry.getKey(), decryptDeep(entry.getValue()));
    }
    
    // Handle List: decrypt list members
    else if(o instanceof List) { List list = (List)o;
        for(int i=0, n=list.size(); i<n; ++i) 
            list.set(i,decryptDeep(list.get(i)));
    }
    
    // Handle String: See comment above about PDFCharStream. will strings have () and <>?  probably
    else if(o instanceof String) { String str = (String)o;
        if(str.charAt(0) != '/') {
            int buflen = str.length(); byte stbuf[] = new byte[buflen];
            for(int i=0; i<buflen; ++i) stbuf[i] = (byte)(str.charAt(i) & 0xff);
            if(_decrypter == null) arcfour_decrypt(stbuf); else { } // use cipher for AES
            try { o = new String(stbuf, "US-ASCII"); }  // once it's decrypted, it should be in ascii
            catch(UnsupportedEncodingException uee) { throw new PDFException(uee); }
        }
    }
    
    // Return decrypted object
    return o;
}

/**
 * Create encryption key for given object numbers and cache it for recursive invocations of decryptDeep().
 */
public void startDecrypt(int objNum, int generationNum) 
{
    try { startDecryptImpl(objNum, generationNum); }
    catch(NoSuchAlgorithmException e) { throw new PDFException("Error decrypting file" + e); }
    catch(InvalidKeyException e) { throw new PDFException("Error decrypting file" + e); }
}

/**
 * Create encryption key for given object numbers and cache it for recursive invocations of decryptDeep().
 */
void startDecryptImpl(int objNum, int generationNum) throws NoSuchAlgorithmException, InvalidKeyException
{
    // Get md
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(_encryption_key);
      
    // Append low->high 3 bytes of objNum, 2 bytes of generationNum
    int p = objNum; for(int i=0;i<3;++i) { md.update((byte)(p & 0xff)); p>>=8; }
    p = generationNum; for(int i=0; i<2; ++i) { md.update((byte)(p & 0xff)); p>>=8; }
      
    // Read the new object key into our pre-allocated buffer (with potential zero pad)
    byte md_digest[] = md.digest(), keybuf[] = _decrypter_key.getKeyBuffer();
    for(int i=0; i<keybuf.length; ++i) 
        keybuf[i] = i<md_digest.length? md_digest[i] : 0;
      
    // Initialize the cipher. AES will need to get the Initialization vector
    if(_decrypter!=null)
        _decrypter.init(Cipher.DECRYPT_MODE,  _decrypter_key);
}

/**
 * Similar to, but not exactly like normal string escape.
 */
public static byte[] getBytesForEncryptionEntry(Map encDict, String key)
{
    // Get string (all are required entries)
    String s = (String)encDict.get(key);
    if(s==null) throw new PDFException("Missing /" + key + " entry in encryption dictionary");
    
    // Strings are enclosed in (), with escapes on parens and single slashes.
    // chars are all binary, with no octal escapes. Hex strings work too, but Acrobat doesn't create them this way.
    int slen = s.length();
    if((s.charAt(0)=='<') && (s.charAt(slen-1)=='>'))
        return PDFUtils.bytesForASCIIHex(s);
    if((s.charAt(0) != '(') || (slen<2) || (s.charAt(slen-1) != ')'))
        throw new PDFException("Illegal value in encryption dictionary");
    
    // Get converted bytes
    byte conversionbytes[] = new byte[slen-2]; int blen = 0;
    for(int i=1; i<slen-1; ++i) { char c = s.charAt(i);
        if(c=='\\') { // if it's an escape, get next char
            if(i<slen-2) c = s.charAt(++i); else throw new PDFException("Illegal value in encryption dictionary"); }
        conversionbytes[blen++] = (byte)c;
    }
    
    // Return truncated array (unless already right size) 
    if(blen==slen-2) return conversionbytes;
    byte finalbytes[] = new byte[blen]; System.arraycopy(conversionbytes, 0, finalbytes, 0, blen);
    return finalbytes;
}

/**
 * Inverse of above
 */
public static String getPDFStringForBytes(byte buf[])
{
    return new StringBuffer(buf.length).append('<').append(ASCIICodec.encodeHex(buf)).append('>').toString();
}

/** 
 * Returns a 32 byte array with the characters of the input string, padded with the password padding.
 */
private static byte[] pad(String aPW)
{
    // Treating passwords as 1-byte per character works for simple passwords, but Acrobat seems to allow unicode
    // characters in password fields. TODO: unclear how unicode passwords are handled.
    int plen = aPW!=null? aPW.length() : 0; if(plen>32) plen = 32; byte buffer[] = new byte[32];
    for(int i=0; i<plen; i++) buffer[i] = (byte)aPW.charAt(i);
    for(int i=plen; i<32; i++) buffer[i] = (byte)PASSWORDPAD[i-plen];
    return buffer;
}

/**
 * decrypt using the cached encryption key.
 */
public void arcfour_decrypt(byte inout[]) { arcfour_decrypt(inout, _decrypter_key.getKeyBuffer()); }

/**
 * decrypt using the cached encryption key.
 */
void arcfour_decrypt(byte inout[], byte key[]) { _arcfour_decrypt(inout, key); }

/**
 * An implementation of the Arcfour algorithm as described in 
 *   http://www.mozilla.org/projects/security/pki/nss/draft-kaukonen-cipher-arcfour-03.txt
 *
 * I verified this by comparing the output with the results of the openssl implementation of rc4:
 *    openssl rc4 -nosalt -K 6162636465666768696A6B6C6D6E6F70 -iv 0
 * should always match the output of arcfour_decrypt(buffer, "abcdefghijklmnop")
 */
static void _arcfour_decrypt(byte inout[], byte key[])
{
    // initialize sbox & s2
    int sbox[] = new int[256], s2[] = new int[256];
    for(int i=0; i<256; ++i) { sbox[i] = i; s2[i] = key[i%key.length] & 0xff; }
    for(int i=0, j=0; i<256; ++i) { j = (j+sbox[i]+s2[i]) % 256; int tmp = sbox[i]; sbox[i] = sbox[j]; sbox[j] = tmp; }

    // decrypt in place
    for(int i=0, j=0, buffindex=0, buffmax=inout.length; buffindex<buffmax; ++buffindex) {
        i = (i+1)%256; j = (j+sbox[i])%256;  // Generate pseudo-random K
        int temp = sbox[i]; sbox[i] = sbox[j]; sbox[j] = temp;
        int t = (sbox[i]+sbox[j])%256;
        int K = sbox[t]; inout[buffindex] ^= K; // xor and hope for the best
    }
}

/**
 * Copies the given array at the new length.
 */
static byte[] copyOf(byte orig[], int newLen)
{
    byte[] cpy = new byte[newLen]; System.arraycopy(orig, 0, cpy, 0, Math.min(orig.length, newLen)); return cpy;
}

/**
 * An exception class that's thrown if authentication fails
 */
static class PDFBadPasswordException extends RuntimeException {
  public PDFBadPasswordException(String message) { super(message); }
}

/**
 * I have no idea if this will work.  I doubt it.
 */
static class PDFKey extends Object implements Key {
    byte kbuf[];
    public PDFKey(int klen)  { klen = klen + 5; if(klen>16) klen = 16; kbuf = new byte[klen]; }
    public String getAlgorithm()  { return "RC4"; } //If this works, add AES
    public String getFormat()  { return "RAW"; }
    public byte[] getEncoded()  { return kbuf; }
    public byte[] getKeyBuffer()  { return kbuf; }
    public int getKeyLength()  { return kbuf.length; }
}

/**
 * Testing.
 */
public static void main(String args[]) throws UnsupportedEncodingException, IOException
{
    if(args.length!=1) { System.err.println("usage: java PDFSecurityHandler key"); System.exit(1); }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(); int b; while((b=System.in.read()) != -1) baos.write(b);
    byte bigbuf[] = baos.toByteArray(); byte key[] = args[0].getBytes("US-ASCII");
    _arcfour_decrypt(bigbuf, key); System.out.write(bigbuf);
}

}