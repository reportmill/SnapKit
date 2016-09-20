/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * A class to load an RXElement from aSource using SAX.
 *
 * This loader really loads "XML Data", not "XML Documents", making sure only childless elements can have
 * value text (eg, no "<a>Hello<b>there</b>World</a>")
 */
public class XMLParser extends DefaultHandler implements org.xml.sax.ext.LexicalHandler {
    
    // The element that parser is to load
    XMLElement         _element = new XMLElement();

    // Stack of currently nested elements while parsing
    List <XMLElement>  _estack = new ArrayList();
    
    // Current string buffer for reading values
    StringBuffer      _sb;
    
    // Tracks the current element
    Object            _sbe;
    
    // Static initializer to allow XML parser to accept text blocks up to 128k (up from 64k)
    static { System.setProperty("entityExpansionLimit", "128000"); }
 
/**
 * Kicks off xml parsing from given source and builds on this parser's element.
 */
public XMLElement parse(Object aSource) throws IOException, SAXException
{
    // Declare variable for reader (and exception)
    XMLReader reader = null; SAXException sex = null;
    
    // Try to get default reader
    try { reader = XMLReaderFactory.createXMLReader(); }
    catch(SAXException e) { sex = e; }
    
    // If no reader, try to get any known parsers explicitly
    if(reader==null) {
        String r1 = "com.sun.org.apache.xerces.internal.parsers.SAXParser", r2 = "org.apache.xerces.parsers.SAXParser";
        String readerPaths[] = new String[] { r1, r2 };
        for(String rpath : readerPaths)
            try { reader = XMLReaderFactory.createXMLReader(rpath); }
            catch(SAXException e) { if(sex==null) sex = e; else System.out.println(rpath + " not found"); }
        if(reader==null) throw sex; // If still null, throw first exception
    }
    
    // Add this XML loader to reader
    reader.setContentHandler(this);
    reader.setEntityResolver(this);
    reader.setErrorHandler(this);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        
    // Get input stream from source (if input stream not found, complain)
    InputStream is = SnapUtils.getInputStream(aSource);
    if(is==null)
        throw new IllegalArgumentException("RXElement: XML source cannot be read: " + aSource);
        
    // Parse input stream
    reader.parse(new InputSource(is));
    return _element;
}

/**
 * Start document callback.
 */
public void startDocument( ) throws SAXException { }

/**
 * End document callback.
 */
public void endDocument( ) throws SAXException { }
 
/**
 * Start element callback.
 */
public void startElement(String aNamespace, String aName, String qName, Attributes attrs) throws SAXException
{
    // Get new element and it's parent
    XMLElement element = _estack.size()==0? _element : new XMLElement();
    XMLElement par = ListUtils.getLast(_estack);
    
    // Set new element name and full name and add to parent
    element.setFullName(qName);
    if(par!=null)
        par.add(element);
    
    // Add to element stack
    _estack.add(element);
    
    // Add namespace
    if(aNamespace!=null && aNamespace.length()>0)
        element.setNamespace(aNamespace);
 
    // Iterate over attributes and add them
    for(int i=0, iMax=attrs.getLength(); i<iMax; i++ )
        element.add(attrs.getQName(i), attrs.getValue(i));
}
 
/**
 * Characters read callback.
 */
public void characters(char[] ch, int start, int length) throws SAXException
{
    // Get last element (if no children, just return)
    XMLElement e = ListUtils.getLast(_estack); if(e.size()>0) return;

    // If StringBuffer is null, create
    if(_sb==null) _sb = new StringBuffer(length);
        
    // If we already have a StringBuffer, but we've moved on to a different element, reset it
    else if(_sbe!=e)
        _sb.setLength(0);
        
    // Reset current element tracker
    _sbe = e;
    
    // Append chars to _sb
    _sb.append(ch, start, length);

    //if(e.value()==null) e.setValue(new String(ch, start, length)); else e.setVal(e.val() + new String());
}

/**
 * End element callback.
 */
public void endElement(String aNamespace, String aName, String qName) throws SAXException
{
    // Remove and cache last object from element stack
    XMLElement e = ListUtils.removeLast(_estack);
    
    // If element is one that we were last adding characters for and it has no children, setValue to _sb
    if(_sbe==e && e.size()==0 && _sb!=null) {
        e.setValue(_sb.toString());
        _sb = null; // Can't reuse StingBuffer because of a 1.4.1 bug
    }
}

/**
 * Handle Processing Instruction.
 */
public void processingInstruction(String aTarg, String theData) throws SAXException
{
    XMLElement e = new XMLElement(aTarg); e.setProcInstrData(theData);
    XMLElement par = ListUtils.getLast(_estack);
    if(par!=null)
        par.add(e);
}
 
/**
 * Warning encountered callback.
 */
public void warning(SAXParseException e) throws SAXException
{
    System.err.println("Warning: " + e + " at Line: " + e.getLineNumber() + " Col: " + e.getColumnNumber());
}

/**
 * Error encountered callback.
 */
public void error(SAXParseException e) throws SAXException
{
    throw new RuntimeException("Error: " + e + " at Line: " + e.getLineNumber() + " Col: " + e.getColumnNumber(), e);
}

/**
 * Fatal error encountered callback.
 */
public void fatalError(SAXParseException e) throws SAXException
{
    throw new RuntimeException("Fatal Error: " +e + " at Line: " +e.getLineNumber() + " Col: " +e.getColumnNumber(), e);
}

/**
 * Resolve entity callback.
 */
public InputSource resolveEntity(String publicId, String systemId)
{
    return new InputSource(new ByteArrayInputStream(new byte[0]));
}

// LexicalHandler methods to get CDATA
public void comment(char[] ch, int start, int length) { }
public void endCDATA() { }
public void endDTD() { }
public void endEntity(String aName) { }
public void startCDATA() { }
public void startDTD(String aName, String publicId, String systemId) { }
public void startEntity(String aName) { }
    
}