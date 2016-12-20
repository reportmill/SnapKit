/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import snap.parse.*;
import snap.pdf.PDFException;
import snap.pdf.PDFStream;
import snap.pdf.PDFXEntry;
import snap.pdf.read.PDFDictUtils;
import snap.util.*;

/**
 * A custom class.
 */
public class PDFReader extends Parser {
    
    // The PDF FIle
    PDFFile                 _pfile;
    
    // The bytes
    byte                    _bytes[];
    
/**
 * Creates a new PDFReader.
 */
public PDFReader(PDFFile aPF, byte theBytes[])
{
    _pfile = aPF; _bytes = theBytes;
    setInput(new ByteCharSequence());
}

/**
 * Returns the parser bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Returns the bytes in range.
 */
public byte[] getBytes(int aStart, int aEnd)  { return Arrays.copyOfRange(_bytes, aStart, aEnd); }

/**
 * Returns the individual XRef at given index.
 */
public PDFXEntry getXRef(int anIndex)  { return _pfile.getXRef(anIndex); }

/**
 * Given an object, check to see if its an indirect reference - if so, resolve the reference.
 */
public Object getXRefObj(Object anObj)  { return _pfile.getXRefObj(anObj); }

/**
 * Reads a file.
 */
public void readFile()
{
    // Get version
    ParseRule versRule = getRule("Version");
    String versStr = parse(versRule).getString();
    _pfile.setVersionString(versStr);
    
    // Create XTable
    _pfile._xtable = new PDFXTable(_pfile, this);
    
    // Read XRefs
    int xrefstart = readXRefTablePos();
    Map trailer = _pfile._trailer = readXRefSection(xrefstart);
    
    // Get the catalog
    Map catalog = _pfile._catalogDict = (Map)getXRefObj(trailer.get("Root"));
    if(catalog==null)
        throw new PDFException("Couldn't find Catalog");

    // Get the file identifier (optional)
    List <String> fileIds = _pfile._fileIds = (List)getXRefObj(trailer.get("ID"));

    // If there was an encryption dictionary, make a handler for it
    Map encrypter = (Map)getXRefObj(trailer.get("Encrypt"));
    if(encrypter!=null)
        _pfile._securityHandler = snap.pdf.PDFSecurityHandler.getInstance(encrypter, fileIds, _pfile.getVersion());
    
    // Get pages
    _pfile._pagesDict = (Map)getXRefObj(catalog.get("Pages"));
}

/**
 * Returns the file offset to the main xref table.
 * PDF reading starts at file end - this routine starts at end and searches backwards until it finds startxref key
 * StartXRef { "startxref" Integer "%%EOF" }
 */
protected int readXRefTablePos()
{
    int start = StringUtils.lastIndexOf(getInput(), "startxref"); if(start<0) return -1;
    return readIntAt(start + "startxref".length()); // check for %%EOF marker
}

/**
 * Reads the XRef Section(s) and returns the trailer dictionary.
 */
protected Map readXRefSection(int aPos)
{
    // Read section start
    setCharIndex(aPos);
    String str0 = getToken().getString();
    Map trailer;
    
    // If not "xref", read as stream
    if(str0.equals("xref")) {
        readString();
        trailer = readXRefTable();
    }
    
    // Otherwise read XRefStream
    else trailer = readXRefStream();
    
    // Check for presence of previous xref table
    Integer newOffset = (Integer)trailer.get("Prev");
    if(newOffset!=null)
        readXRefSection(newOffset);
        
    // Return the trailer
    return trailer;
}

/**
 * Reads an XRefTable and returns the trailer dictionary.
 * 
 * XRefTable { "xref" (XRefSection+ Trailer) | XRefStream }
 * XRefSection { Integer Integer XRefEntry+ }
 * XRefEntry { Integer Integer ("f" | "n") }
 * XRefStream { ObjectDef }
 * Trailer { "trailer" Dictionary }
 */
protected Map readXRefTable()
{
    // Read sub section start index and count
    int start = readInt();
    int count = readInt();
    
    // Add missing PDFXEntrys
    _pfile.getXRefTable().setXRefMax(start+count);
    
    // Iterate over subsection entries
    for(int i=0;i<count;i++) {
        PDFXEntry xref = getXRef(start + i);
        xref.fileOffset = readInt();
        xref.generation = readInt();
        String str = readString();
        xref.state = str.equals("n")? PDFXEntry.EntryNotYetRead : str.equals("f")? PDFXEntry.EntryDeleted :
            PDFXEntry.EntryUnknown;
    }
    
    // If next string is "trailer", read trailer dict
    Token token = getToken();
    if(token.getString().equals("trailer")) {
        String tstr = readString();
        return (Map)readObject();
    }
    
    // Otherwise read next subsection
    return readXRefTable();
}

/**
 * Reads an XRefStream and returns the trailer dictionary.
 */
protected Map readXRefStream()
{
    Object obj = readObjectDef();
    if(!(obj instanceof PDFStream)) throw new RuntimeException("PDFReader.readXRefStream: Bogus");
    
    PDFStream xstm = (PDFStream)obj;
    Map xmap = xstm.getDict();
    if(!"/XRef".equals(xmap.get("Type"))) throw new RuntimeException("PDFReader.readXRefStream Type: Bogus");

    int maxObjPlusOne = ((Number)xmap.get("Size")).intValue();
    int fieldWidths[] = PDFDictUtils.getIntArray(xmap, null, "W");
    int fields[] = new int[fieldWidths.length];
    int indices[] = PDFDictUtils.getIntArray(xmap, null, "Index");
    if(indices==null) indices = new int[]{0, maxObjPlusOne};
    byte xrefdata[] = xstm.decodeStream();
    int xrefdatapos=0;

    // Allocate space for all xrefs to come
    _pfile.getXRefTable().setXRefMax(maxObjPlusOne-1);
    
    // Read in each subsection
    int nsubsections = indices.length/2;
    for(int i=0; i<nsubsections; ++i) {
        int subStart = indices[2*i];
        int numEntries = indices[2*i+1];
        for(int j=0; j<numEntries; ++j) {
            // Pull out the fields from the stream data.
            for(int k=0; k<fields.length;++k) {
                fields[k] = 0;
                for(int l=0; l<fieldWidths[k]; ++l)
                    fields[k] = (fields[k])<<8 | (xrefdata[xrefdatapos++] & 0xff);
            }
            // Get the xref and set the values if not already set
            PDFXEntry anEntry = getXRef(subStart+j);
            if(anEntry.state==PDFXEntry.EntryUnknown) {
                switch(fields[0]) {
                    case 0: anEntry.state = PDFXEntry.EntryDeleted; break;
                    case 1: anEntry.state = PDFXEntry.EntryNotYetRead;
                            anEntry.fileOffset = fields[1];
                            anEntry.generation = fieldWidths[2]>0 ? fields[2] : 0;
                            break;
                    case 2: anEntry.state = PDFXEntry.EntryCompressed;
                            anEntry.fileOffset = fields[1]; //really the object number of object stream
                            anEntry.generation = fields[2]; //and index of object within object stream
                            break;
                    default: throw new RuntimeException("PDFReader.readXRefStream: Unknown state: " + fields[0]);
                }
            }
        }
    }
        
    return xmap;
}

/**
 * Reads next int.
 */
public int readInt()  { return parseCustom(getIntegerRule(), Integer.class); }

/**
 * Reads next String.
 */
protected String readString()
{
    snap.parse.Token token = getToken();
    setCharIndex(token.getInputEnd());
    return token.getString();
}

/**
 * Reads next PDF object.
 */
public Object readObject()  { return parseCustom(getObjectRule(), Object.class); }

/**
 * Reads next PDF object definition.
 */
protected Object readObjectDef()  { return parseCustom(getObjectDefRule(), Object.class); }

/**
 * Reads an int at given position.
 */
protected int readIntAt(int aPos)
{
    int opos = getCharIndex(); setCharIndex(aPos);
    int value = readInt();
    setCharIndex(opos);
    return value;
}

/**
 * Reads a PDF object at given position.
 */
public Object readObjectDefAt(int aPos)
{
    int opos = getCharIndex(); setCharIndex(aPos);
    Object obj = readObjectDef();
    setCharIndex(opos);
    return obj;
}

/** Returns the Integer rule and ObjectDef rules. */
protected ParseRule getIntegerRule()  { return _ir!=null? _ir : (_ir=getRule("Integer")); } ParseRule _ir;
protected ParseRule getObjectRule()  { return _or!=null? _or : (_or=getRule("Object")); } ParseRule _or;
protected ParseRule getObjectDefRule()  { return _odr!=null? _odr : (_odr=getRule("ObjectDef")); } ParseRule _odr;

/**
 * A simple class to vend a byte array as a CharSequence.
 */
private class ByteCharSequence implements CharSequence {
    public char charAt(int anIndex)  { return (char)_bytes[anIndex]; }
    public int length()  { return _bytes.length; }
    public CharSequence subSequence(int s, int e)  { return new String(Arrays.copyOfRange(_bytes, s, e)); }
}
    
/**
 * Creates the rule.
 */
protected ParseRule createRule()
{
    if(_sharedRule!=null) return _sharedRule;
    ParseRule rule = ParseUtils.loadRule(getClass(), null);
    ParseUtils.installHandlers(getClass(), rule);
    return _sharedRule = rule; //.getRule("JavaFile");
} static ParseRule _sharedRule;

/**
 * Override to provide tokenizer that skips whitespace and comments.
 */
protected Tokenizer createTokenizerImpl()
{
    return new Tokenizer() {
        protected void skipWhiteSpace()
        {
            super.skipWhiteSpace();
            if(hasChar() && getChar()=='%' && getCharIndex()>0) {
                while(getChar()!='\n' && hasChar()) eatChar();
                skipWhiteSpace();
            }
        }
    };
}

/**
 * Override to suppress Parse exception.
 */
protected void parseFailed(ParseRule aRule, ParseHandler aHandler)
{
    if(aHandler!=null) aHandler.reset();
    //throw new ParseException(this, aRule);
    System.err.println("Failed to parse " + aRule);
}

/**
 * Dictionary Handler: { "<<" (Name Object)* ">>" }
 */
public static class DictionaryHandler extends ParseHandler <HashMap> {
    
    Object key;

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            key = aNode.getCustomNode(String.class).substring(1);
            
        // Handle Object
        else if(anId=="Object")
            getPart().put(key, aNode.getCustomNode());
    }
}

/**
 * Array Handler: { "[" Object* "]" }
 */
public static class ArrayHandler extends ParseHandler <ArrayList> {
    
    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Object
        if(anId=="Object")
            getPart().add(aNode.getCustomNode());
    }
}

/**
 * Object Handler: { Array | (Dictionary ("stream" "endstream")?) | LookAhead(3) ObjectRef | Leaf }
 */
public static class ObjectHandler extends ParseHandler <Object> {
    
    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Stream
        if(anId=="stream") {
            PDFReader parser = (PDFReader)aNode.getParser();
            Map dict = (Map)getPart();
            Object lenObj = parser.getXRefObj(dict.get("Length"));
            if(parser.getChar()=='\r') parser.eatChar();
            if(parser.getChar()=='\n') parser.eatChar();
            int start = parser.getCharIndex();
            int len = SnapUtils.intValue(lenObj);
            byte sbytes[] = parser.getBytes(start, start+len);
            _part = new PDFStream(sbytes, dict);
            parser.setCharIndex(start+len);
        }
        
        // Handle endstream
        else if(anId=="endstream");

        // Handle anything else
        else _part = aNode.getCustomNode();
    }
}

/**
 * ObjectRef Handler: { Integer Integer "R" }
 */
public static class ObjectRefHandler extends ParseHandler <PDFXEntry> {
    
    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Integer
        if(anId=="Integer" && _part==null) {
            int index = aNode.getCustomNode(Integer.class);
            PDFReader reader = (PDFReader)aNode.getParser();
            _part = reader.getXRef(index);
        }
    }
}

/**
 * ObjectDef Handler: { Integer Integer "obj" Object "endobj" }
 */
public static class ObjectDefHandler extends ParseHandler <Object> {
    
    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Object
        if(anId=="Object")
            _part = aNode.getCustomNode();
    }
}

/**
 * Leaf Handler: { "true" | "false" | "null" | Integer | Real | Name | String | HexString }
 */
public static class LeafHandler extends ParseHandler <Object> {
    
    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        _part = aNode.getCustomNode();
        if(_part==null) {
            _part = aNode.getString();
            if(_part.equals("true")) _part = Boolean.TRUE;
            else if(_part.equals("false")) _part = Boolean.FALSE;
        }
    }
}

/**
 * Integer Handler: { "[\+\-]?[0-9]+" }
 */
public static class IntegerHandler extends ParseHandler <Integer> {

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Get node string
        String s = aNode.getString();
        _part = Integer.valueOf(s);
    }
}

/**
 * Real Handler: { "[\+\-]?[0-9]*\.[0-9]+" }
 */
public static class RealHandler extends ParseHandler <Double> {

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Get node string
        String s = aNode.getString();
        _part = Double.valueOf(s);
    }
}

/**
 * Name Handler: { "/[0-9a-zA-Z]+" }
 */
public static class NameHandler extends ParseHandler <String> {

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)  {  _part = aNode.getString(); }
}

/**
 * String Handler: { "(" }
 */
public static class StringHandler extends ParseHandler <String> {

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Get parser and parser bytes
        PDFReader parser = (PDFReader)aNode.getParser();
        byte bytes[] = parser.getBytes();
        
        // Get start/end of string - increment end util final close paren
        int start = parser.getCharIndex() - 1, end = start + 1, nested = 1;
        while(true) {
            char c = (char)bytes[end++];
            if(c=='(') nested++;
            else if(c==')') { nested--; if(nested==0) break; }
            else if(c=='\\') end++;
        }
        
        // Create string and reset Parser.CharIndex
        _part = new String(bytes, start, end-start);
        parser.setCharIndex(end);
    }
}

/**
 * HexString Handler: { "<" "[0-9a-fA-F]*" ">" }
 */
public static class HexStringHandler extends ParseHandler <String> {

    /** ParseHandler method. */
    protected void parsedOne(ParseNode aNode, String anId)  { _part = aNode.getString(); }
}

}