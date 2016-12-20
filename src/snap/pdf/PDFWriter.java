/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.io.ByteArrayOutputStream;
import java.text.*;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import snap.gfx.*;
import snap.view.*;
import java.util.zip.Deflater;
import snap.pdf.write.*;
import snap.util.SnapUtils;

/**
 * PDF Writer.
 */
public class PDFWriter extends PDFWriterBase {
    
    // The PDFFile
    protected PDFFile                     _pfile;
    
    // The XRefTable
    protected PDFXTable                   _xtable;

    // The current PDF page
    protected PDFPageWriter               _pageWriter;

    // Whether PDF stream objects should be compressed
    protected boolean                     _compress;
    
    // Shared deflater
    Deflater                    _deflater = new Deflater(6, false);

    // Security handler for adding password protection
    PDFEncryptor                _encryptor;
    
    // Map of pdfread XRefs to pdfwrite XRefs
    public Map                  _readerWriterXRefMap = new HashMap();
  
    // The default viewer preferences map
    static Map <String,String>  _viewerPreferencesDefault = Collections.singletonMap("PrintScaling", "/None");
    
    // The current font entry
    PDFFontEntry                _fontEntry;
    
    // Font entry map
    Map <String, PDFFontEntry>  _fonts = new Hashtable();
    
    // Map of image data names to image reference strings
    public Map <String,String>         _images = new Hashtable();
    
    // Map of unique image datas
    List <Image>                _imageDatas = new ArrayList();
    
/**
 * Returns a PDF byte array for a given RMDocument.
 */
public byte[] getBytes(DocView aDoc)
{
    // Create PDFFile
    _pfile = new PDFFile();
    
    // Init and add info dict to xref
    _xtable = _pfile._xtable = new PDFXTable(_pfile, null);
    _pfile._infoDict.put("CreationDate", new SimpleDateFormat("(dd MMM yyy HH:mm)").format(new Date()));
    _xtable.addObject(_pfile._infoDict);

    // Init and add catalog to xref
    _pfile._catalogDict.put("Type", "/Catalog");
    _pfile._catalogDict.put("PageMode", "/UseNone");
    _xtable.addObject(_pfile._catalogDict);
    
    // Init and add to xref and catalog
    _pfile._pageTree = new PDFPageTree(_pfile);
    _pfile._catalogDict.put("Pages", _xtable.addObject(_pfile._pageTree));

    // Add fonts and images to xref
    _xtable.addObject(_fonts);
    _xtable.addObject(_images);
    
    // Tell acrobat reader not to scale when printing by default (only works in PDF 1.6, but is harmless in < 1.6)
    _pfile._catalogDict.put("ViewerPreferences", PDFWriter.getViewerPreferencesDefault());
    
    // Validate and resolve doc page references
    aDoc.setWidth(aDoc.getBestWidth(-1));
    aDoc.setHeight(aDoc.getBestHeight(-1));
    ViewUtils.layoutDeep(aDoc); //aDoc.resolvePageReferences();
    
    // Get doc pdf attributes
    _compress = false; //aDoc.getCompress();
    
    // Set PDF file author
    _pfile.setAuthor("Snap User");
    
    // Set PDF file creator
    String version = "SnapPDF " + SnapUtils.getVersion();
    String build = ", Build: " + SnapUtils.getBuildInfo();
    String jvm = ", JVM: " + System.getProperty("java.version");
    _pfile.setCreator(version + build + jvm);
    
    // Iterate over doc pages
    //for(int i=0, iMax=aDoc.getPageCount(); i<iMax; i++) { PageView page = aDoc.getPage(i);
    if(aDoc.getPage()!=null) { PageView page = (PageView)aDoc.getPage();
        
        // Get pdf page, set media box and add to pages tree and xref
        _pageWriter = new PDFPageWriter(_pfile, this);
        _pageWriter.setMediaBox(page.getBoundsInside());
        
        // Have page pdfr write pdf
        SnapViewPdfr.getPdfr(page).writePDF(page, this);

        // Close page contents
        _pageWriter.closeContents(this);
    }
      
    // run a pass though all the _pages to resolve any forward references
    _pfile._pageTree.resolvePageReferences();
    
    // Write out header but save away version in case it gets updated
    String versionString = _pfile.getVersionString();
    appendln("%" + versionString);
    
    // Write 4 binary bytes in comment to indicate we may use 8 bit binary
    append(new byte[] { (byte)'%', (byte)'\242', (byte)'\243', (byte)'\245', (byte)'\250' });
    appendln();
    
    // The _objects & the xref table 
    int off = writeXRefTable();
    
    // The trailer
    appendln("trailer");
    appendln("<<");
    append("/Size ").append(_pfile._xtable.getEntryCount() + 1).appendln();
    append("/Root ").appendln(_pfile._xtable.getRefString(_pfile._catalogDict));
    append("/Info ").appendln(_pfile._xtable.getRefString(_pfile._infoDict));
    
    // If encryption was specified, add the encryption dict
    if(getEncryptor() != null) 
        append("/Encrypt ").appendln(_pfile._xtable.getRefString(getEncryptor().getEncryptionDict()));
    
    // Add a uniqueID to the trailer
    String idString = _pfile.getFileIDString();
    append("/ID [").append(idString).append(idString).append(']').appendln();

    // Write cross reference table and end of file marker
    appendln(">>");
    appendln("startxref");
    append(off).appendln();
    appendln("%%EOF");
    
    // Now get actual pdf bytes
    byte pdfBytes[] = toByteArray();
    
    // If version string was bumped during generation, go back and update header
    String newVersion = _pfile.getVersionString();
    if(!versionString.equals(newVersion)) {
        
        // pdf files are extremely sensitive to position, so make sure the headers are the same size
        int newLen = newVersion.length();
        int oldLen = versionString.length();

        // Complain if version string increases in length
        if(newLen > oldLen) 
            throw new RuntimeException("error trying to update pdf version number to " + newVersion);
        
        // Copy new version in (pad with spaces if new version is smaller)
        for(int i=0; i<oldLen; i++)
            pdfBytes[i+1] = (byte)(i<newLen? newVersion.charAt(i) : ' ');
    }
    
    // Return pdf bytes
    return pdfBytes;
}

/**
 * Returns the PDFFile.
 */
public PDFFile getPDFFile()  { return _pfile; }

/**
 * Returns the XRefTable.
 */
public PDFXTable getXRefTable()  { return _xtable; }

/**
 * Returns the current page writer.
 */
public PDFPageWriter getPageWriter()  { return _pageWriter; }

/**
 * Returns whether to compress or not.
 */
public boolean getCompress()  { return _compress; }

/**
 * Sets whether to compress or not.
 */
public void setCompress(boolean aValue)  { _compress = aValue; }

/**
 * Returns a shared deflater.
 */
public Deflater getDeflater()  { return _deflater; }

/**
 * Returns the current PDF encryptor.
 */
public PDFEncryptor getEncryptor()  { return _encryptor; }

/**
 * Set the access permissions on the file such that the document can be opened by anyone, but the user cannot
 * modify the document in any way. To modify these settings in Acrobat, the owner password would have to be provided.
 */
public void setUnmodifiable(String ownerPwd)
{
    setAccessPermissions(ownerPwd, null, PDFEncryptor.PRINTING_ALLOWED | PDFEncryptor.EXTRACT_TEXT_AND_IMAGES_ALLOWED |
        PDFEncryptor.ACCESSABILITY_EXTRACTS_ALLOWED | PDFEncryptor.MAXIMUM_RESOLUTION_PRINTING_ALLOWED);
}

/** 
 * Sets pdf user access restrictions.
 * 
 * The user password is the password that will be required to open the file.
 * The owner password is the password that will be required to make future changes to the security settings,
 * such as the passwords. Either of the passwords may be null.
 * If both passwords are null, the file will not be password protected, but it will still be encrypted.
 * Fine-grained access can be limited by setting accessFlags, to limit such things as printing or editing the file.
 * See PDFSecurityHandler for a list of the access flag constants. (or the pdf spec v1.6, pp. 99-100)
 */
public void setAccessPermissions(String ownerPwd, String userPwd, int accessFlags)
{
    // Create encryptor
    _encryptor = new PDFEncryptor(_pfile.getFileID(), ownerPwd, userPwd, accessFlags);
    
    // Add the encryption dictionary to the file
    _xtable.addObject(_encryptor.getEncryptionDict());
    
    // Since we're using 128 bit keys, bump the pdf revision to 1.4. If we add a parameter to select the
    // encryption scheme, we could optionally use 40 bit keys and leave the version at 1.2
    _pfile.setVersion(1.4f);
}

/**
 * Returns default viewer preferences map.
 */
public static Map<String, String> getViewerPreferencesDefault()  { return _viewerPreferencesDefault; } 

/**
 * Sets default viewer preferences map.
 */
public static void setViewerPreferencesDefault(Map<String, String> aMap)  { _viewerPreferencesDefault = aMap; } 

/**
 * Returns the current pdf font entry.
 */
public PDFFontEntry getFontEntry()  { return _fontEntry; }

/**
 * Sets the current font entry.
 */
public void setFontEntry(PDFFontEntry aFontEntry)  { _fontEntry = aFontEntry; }

/**
 * Returns the pdf file's fonts.
 */
public Map <String, PDFFontEntry> getFonts()  { return _fonts; }

/**
 * Returns the pdf font entry for a specific font.
 */
public PDFFontEntry getFontEntry(Font aFont, int fontCharSet)
{
    // Get font entry name
    String fontEntryName = fontCharSet==0? aFont.getName() : aFont.getName() + "." + fontCharSet;
    
    // Get FontEntry for base chars
    PDFFontEntry fontEntry = getFonts().get(fontEntryName);
    
    // If not present, create new font entry for font and add to fonts map
    if(fontEntry==null) {
        PDFFontEntry rootEntry = fontCharSet!=0? getFontEntry(aFont, 0) : null;
        fontEntry = new PDFFontEntry(aFont, fontCharSet, rootEntry);
        _xtable.addObject(fontEntry);
        _fonts.put(fontEntryName, fontEntry);
    }
    
    // Return font entry
    return fontEntry;
}

/**
 * Returns a map of image data names to image data references.
 */
public Map <String,String> getImages()  { return _images; }

/**
 * Returns the image name.
 */
public String getImageName(Image anImage)  { return "Image" + System.identityHashCode(anImage); }

/**
 * Adds an image data (uniqued) to file reference table, if not already present. 
 */
public void addImageData(Image anImage)
{
    // If not present, unique, add to xref table and add to image refs
    String iname = getImageName(anImage);
    if(!_images.containsKey(iname))
        _images.put(iname, _xtable.addObject(getUniqueImage(anImage)));
}

/**
 * Returns a unique image for given image.
 */
public Image getUniqueImage(Image anImage)
{
    int index = _imageDatas.indexOf(anImage);
    if(index<0) _imageDatas.add(index = _imageDatas.size(), anImage);
    return _imageDatas.get(index);
}

/**
 * Writes any kind of object to the PDF buffer.
 */
public void writeXRefEntry(Object anObj)
{
    // Handle strings
    if(anObj instanceof String) { String string = (String)anObj;
    
        // If not a PDF string, just append
        if(!string.startsWith("("))
            append(string);
        
        // If encryption is enabled, all strings get encrypted
        else if(getEncryptor()!= null) {
            append('(');
            append(getEncryptor().encryptString((String)anObj));
            append(')');
        }
        
        // Otherwise just add string
        else writePDFString((String)anObj);
    }
    
    // Handle numbers
    else if(anObj instanceof Number)
        append(((Number)anObj).doubleValue());
    
    // Handle fonts map
    else if(anObj==getFonts()) {
        appendln("<<");
        for(PDFFontEntry fontEntry : getFonts().values())
            appendln("/" + fontEntry.getPDFName() + " " + _xtable.getRefString(fontEntry));
        appendln(">>");
    }
    
    // Handle Maps
    else if(anObj instanceof Map) { Map <String,Object>map = (Map)anObj;
    
        // Write dictionary contents surrounded by dictionary brackets
        appendln("<<");
        for(String key : map.keySet()) {
            
            // Skip entries that we put in for caching purposes
            if(key.startsWith("_rbcached"))
                continue;
            
            append('/').append(key).append(' ');
            writeXRefEntry(map.get(key));
            appendln();
        }
        append(">>");
    }
    
    // Handle Lists
    else if(anObj instanceof List) { List list = (List)anObj;
        append('[');
        for(int i=0, iMax=list.size(); i<iMax; i++) {
            if(i>0) append(' ');
            writeXRefEntry(list.get(i));
        }
        append(']');
    }
    
    // Handle PDFPage
    else if(anObj instanceof PDFPageWriter)
        ((PDFPageWriter)anObj).writePDF(this);
    
    // Handle font entries
    else if(anObj instanceof PDFFontEntry)
        PDFWriterFont.writeFontEntry(this, (PDFFontEntry)anObj);
    
    // Handle image data
    else if(anObj instanceof Image)
        PDFWriterImage.writeImage(this, (Image)anObj);
    
    // Handle PDFPagesTree
    else if(anObj instanceof PDFPageTree)
        ((PDFPageTree)anObj).writePDF(this);
    
    // Handle PDFStream
    else if(anObj instanceof PDFStream)
        writeStream((PDFStream)anObj);
    
    // Handle PDFAnnotation
    else if(anObj instanceof PDFAnnotation) { PDFAnnotation annotation = (PDFAnnotation)anObj;
        writeXRefEntry(annotation.getAnnotationMap());
    }
    
    // Handle color
    else if(anObj instanceof Color)
        append((Color)anObj);
    
    // Handle boolean
    else if(anObj instanceof Boolean)
        append(anObj.toString());
    
    // Complain about anything else
    else System.err.println("RMPDFWriter: Unsupported PDF object: " + anObj.getClass().getName());
}

/**
 * Writes all entry objects to pdf buffer.
 */
public int writeXRefTable()
{
    // Create list for offsets
    List <Integer> offsets = new Vector(_xtable.getEntryCount());
    
    // First write the objects themselves, saving the file offsets for later use.
    // Call entries.size() every time in loop because objects are added as descriptions are generated.
    for(int i=0; i<_xtable.getEntryCount(); i++) {
        
        offsets.add(length());
        appendln((i+1) + " 0 obj");
        Object entry = _xtable.getEntry(i);
        
        // If encryption has been turned on, notify the encryptor of the top-level object we're about to write out.
        if(getEncryptor() != null)
            getEncryptor().startEncrypt(i+1, 0);
        
        writeXRefEntry(entry);
            
        appendln();
        appendln("endobj");
    }
        
    // Record the offset where the xref table lands
    int xoff = length();
    
    // And spit out the table
    int count = _xtable.getEntryCount();
    appendln("xref");
    appendln("0 " + (count+1));
    
    // The entries have to be 20 chars long each.
    DecimalFormat format = new DecimalFormat("0000000000");
    appendln("0000000000 65535 f ");
    for(int i=0; i<count; i++)
        appendln(format.format(offsets.get(i)) + " 00000 n ");
    
    // Return offset
    return xoff;
}

/**
 * Writes a stream to a pdf buffer.
 */
public void writeStream(PDFStream aStream)
{
    // Get bytes and length
    byte bytes[] = aStream.getBytes(); int length = bytes.length;
    Map dict = aStream.getDict();
  
    // Compress the data if it hasn't already been filtered
    Object filter = dict.get("Filter");
    if(filter==null && length>64 && getCompress()) {
        
        // Get flate encoded bytes and swap them in if smaller
        byte bytes2[] = getBytesEncoded(bytes, 0, length);
        if(bytes2.length<length) {
            bytes = bytes2; length = bytes2.length;
            aStream.addFilter("/FlateDecode");
        }
    }
    
    // If encryption is enabled, encrypt the stream data
    if(getEncryptor() != null)
        bytes = getEncryptor().encryptBytes(bytes);

    // Now set the length key to represent the real length
    dict.put("Length", new Integer(length));
    
    // Stick dict description in stream, followed by "stream" keyword, data & "endstream" keyword
    writeXRefEntry(dict);
    
    // Write bytes
    appendln();
    appendln("stream");
    append(bytes, 0, length);
    appendln();
    appendln("endstream");
}

/**
 * Returns Flate encoded bytes from the given raw bytes.
 */
public byte[] getBytesEncoded(byte bytes[], int offset, int length)
{
    // Get byte array output stream for bytes
    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(bytes.length);
    
    // Get deflator output stream for bytes (use shared deflater)
    DeflaterOutputStream deflaterOutStream = new DeflaterOutputStream(byteOutStream, getDeflater());

    // Catch exceptions
    try {

    // Write bytes to deflator output stream
    deflaterOutStream.write(bytes);
    deflaterOutStream.close();
    
    // Reset shared deflater
    getDeflater().reset();
    
    // Catch exceptions
    } catch(Exception e) { e.printStackTrace(); return null; }
    
    // Return bytes
    return byteOutStream.toByteArray();
}

}