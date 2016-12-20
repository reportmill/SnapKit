package snap.pdf;
import java.util.*;
import snap.pdf.PDFPage;
import snap.pdf.read.*;
import snap.pdf.write.PDFPageTree;

/**
 * Represents a PDF file.
 */
public class PDFFile {

    // The PDF version being generated
    double                  _version = 1.2f;
    
    // The XRefTable
    public PDFXTable        _xtable;
    
    // Encyption dictionary
    PDFSecurityHandler      _securityHandler;
    
    // The reader
    PDFReader               _reader;
    
    // The trailer dictionary
    Map                     _trailer;
    
    // Info dict
    public Map              _infoDict = new Hashtable(4);
    
    // Catalog dict
    public Map              _catalogDict = new Hashtable(4);
    
    // The PDF file pages
    public Map              _pagesDict;
    
    // Cached PDFPage instances
    Map <Integer, PDFPage>  _pages = new Hashtable(4);
    
    // File identifier
    byte                    _fileId[] = null;
    List <String>           _fileIds;
    
    // Factory classes and callback handler
    FontFactory             _fontFact;
    ColorFactory            _colorFact;
    ImageFactory            _imageFact;
    PDFMarkupHandler        _markupHandler;
    
    // Pages tree
    public PDFPageTree      _pageTree;
    
/**
 * Creates a new PDFFile.
 */
public PDFFile()  { }

/**
 * Creates a new PDFFile.
 */
public PDFFile(byte theBytes[])
{
    _reader = new PDFReader(this, theBytes);
    _reader.readFile();
}

/**
 * Gets the pdf version as a float.
 */
public double getVersion()  { return _version; }

/**
 * Sets the version of the pdf being generated.
 */
public void setVersion(double aVersion)  { _version = Math.max(_version, aVersion); }

/**
 * Returns the version of pdf being generated.
 */
public String getVersionString()  { return "PDF-" + _version; }

/**
 * Gets the pdf version as a float.
 */
public void setVersionString(String aStr) 
{
    // parser guarantees that this string looks like %PDF-xxxx
    try { setVersion(Double.parseDouble(aStr.substring(5))); }
    catch (NumberFormatException nfe) { throw new PDFException("Illegal PDF version header"); }
}

/**
 * Returns the cross reference table.
 */
public PDFXTable getXRefTable()  { return _xtable; }

/**
 * Returns the list of PDFEntry objects from XRef table.
 */
public List <PDFXEntry> getXRefs()  { return _xtable.getXRefs(); }

/**
 * Returns the individual XRef at given index.
 */
public PDFXEntry getXRef(int anIndex)  { return _xtable.getXRef(anIndex); }

/**
 * Given an object, check to see if its an indirect reference - if so, resolve the reference.
 */
public Object getXRefObj(Object anObj)  { return _xtable.getXRefObj(anObj); }

/**
 * Returns the PDF reader.
 */
public PDFReader getReader()  { return _reader; }

/**
 * Returns the PDF reader bytes.
 */
public byte[] getBytes()  { return _reader.getBytes(); }

/**
 * Returns the trailer dictionary.
 */
public Map getTrailer()  { return _trailer; }

/**
 * Returns the number of pages in this file.
 */
public int getPageCount()
{
    Object obj = _pagesDict.get("Count");
    return (Integer)getXRefObj(obj); // Can Count really be a reference?
}

/**
 * Returns an individual PDF page for the given page index.
 */
public PDFPage getPage(int aPageIndex)
{ 
    PDFPage page = _pages.get(aPageIndex);
    if(page==null) _pages.put(aPageIndex, page = new PDFPage(this, aPageIndex)); 
    return page;
}

/**
 * Clears the page cache.
 */
public void clearPageCache() { _pages.clear(); }

/** Graphics object creation factories */
public FontFactory getFontFactory() { return _fontFact!=null? _fontFact : (_fontFact=new PDFFontFactory()); }
public ColorFactory getColorFactory() { return  _colorFact!=null? _colorFact : (_colorFact=new PDFColorFactory()); }
public ImageFactory getImageFactory() { return _imageFact!=null? _imageFact : (_imageFact=new PDFImageFactory()); }

/** The callback handler */
public PDFMarkupHandler getMarkupHandler() { return _markupHandler; }
public void setMarkupHandler(PDFMarkupHandler h) { _markupHandler = h; }

/**
 * Returns the PDF file's info dictionary.
 */
public Map getInfoDict()  { return _infoDict; }

/**
 * Returns the catalog dictionary.
 */
public Map getCatalogDict()  { return _catalogDict; }

/**
 * Returns the PDF file's pages tree.
 */
public PDFPageTree getPagesTree()  { return _pageTree; }

/**
 * Sets the author of the pdf file.
 */
public void setAuthor(String  s)  { _infoDict.put("Author", "(" + s + ")"); }

/**
 * Sets the creator of the pdf file.
 */
public void setCreator(String s)  { _infoDict.put("Creator", "(" + s + ")"); }

/**
 * Generates and returns a unique file identifier.
 */
public byte[] getFileID()
{
    // If already set, just return
    if(_fileId!=null) return _fileId;
    
    // In order to be unique to file's contents, fileID is generated with an MD5 digest of contents of info dictionary.
    // The pdf spec suggests using the file size and the file name, but we don't have access to those here.
    // The spec suggests using current time, but that's already present in info dict as value of /CreationDate key.
    try {
        
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        Iterator vals = _infoDict.values().iterator();
        while(vals.hasNext()) {
            String infoString = vals.next().toString();
            md.update(infoString.getBytes());
        }
        
        // Rather than adding the file size, which we won't have until everything gets dumped out, add the number
        // of objects in the xref table (as 4 bytes). This is probably going to be the same for everyone.
        int i, size = _xtable.getEntryCount();
        for(i=0; i<4; ++i) {
            md.update((byte)(size&0xff));
            size>>=8;
        }
        
        // Get the digest and cache it - MD5 is defined to return a 128 bit (16 byte) digest
        byte digest_bytes[] = md.digest();
        
        // This should never happen, so this is here just in case something goes wrong.
        if (digest_bytes.length>16) {
            _fileId = new byte[16]; System.arraycopy(digest_bytes, 0, _fileId, 0, 16); }
        else _fileId = digest_bytes;
    }
    
    // If the md5 fails, just create a fileID with random bytes
    catch (java.security.NoSuchAlgorithmException nsae) {
        _fileId = new byte[16]; new Random().nextBytes(_fileId);  }
    
    return _fileId;
}

/**
 * Returns the file identifier as a hex string.
 */
public String getFileIDString()
{        
    byte id_bytes[] = getFileID(); StringBuffer sb = new StringBuffer("<");
    for(int i=0, iMax=id_bytes.length; i<iMax; i++) {
        int c1 = (id_bytes[i]>>4) & 0xf; sb.append((char)(c1<10? '0' + c1 : 'a' + (c1-10)));
        int c2 = id_bytes[i] & 0xf; sb.append((char)(c2<10? '0' + c2 : 'a' + (c2-10)));
    }
    sb.append('>');
    return sb.toString();
}

}