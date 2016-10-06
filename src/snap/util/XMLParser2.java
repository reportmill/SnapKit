/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import snap.web.WebURL;

/**
 * A class to load an RXElement from aSource.
 */
public class XMLParser2 extends Parser {
    
/**
 * Creates a new XMLParser.
 */
public XMLParser2()
{
    // Install handlers: ParseUtils.installHandlers(getClass(), getRule());
    getRule("Document").setHandler(new DocumentHandler());
    getRule("Prolog").setHandler(new PrologHandler());
    getRule("Element").setHandler(new ElementHandler());
    getRule("Attribute").setHandler(new AttributeHandler());
}
    
/**
 * Kicks off xml parsing from given source and builds on this parser's element.
 */
public XMLElement parseXML(Object aSource) throws Exception
{
    WebURL url = WebURL.getURL(aSource);
    String str = url!=null? url.getText() : null;
    if(str==null && aSource instanceof byte[]) str = new String((byte[])aSource);
    return (XMLElement)parse(str).getCustomNode();
}

/**
 * Document Handler.
 */
public static class DocumentHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Element
        if(anId=="Element")
            _part = (XMLElement)aNode.getCustomNode();
    }
}

/**
 * Prolog Handler.
 */
public static class PrologHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Attribute
        if(anId=="Attribute")
            getPart().addAttribute((XMLAttribute)aNode.getCustomNode());
    }
}

/**
 * Element Handler.
 */
public static class ElementHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name") {
            if(_part==null) _part = new XMLElement(aNode.getString());
            else if(!_part.getName().equals(aNode.getString()))
                throw new RuntimeException("XMLParser: Expected closing tag " + _part.getName());
        }
            
        // Handle Attribute
        else if(anId=="Attribute")
            _part.addAttribute((XMLAttribute)aNode.getCustomNode());
            
        // Handle Element
        else if(anId=="Element")
            _part.addElement((XMLElement)aNode.getCustomNode());
    }
}

/**
 * Attribute Handler.
 */
public static class AttributeHandler extends ParseHandler <XMLAttribute> {
    
    // The attribute name
    String _name;
    
    /** Returns the part class. */
    protected Class <XMLAttribute> getPartClass()  { return XMLAttribute.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _name = aNode.getString();
            
        // Handle String
        else if(anId=="String") { String str = aNode.getString();
            _part = new XMLAttribute(_name, str.substring(1, str.length()-1)); }
    }
}

/**
 * Test.
 */
public static void main(String args[]) throws Exception
{
    XMLParser2 parser = new XMLParser2();
    XMLElement xml = parser.parseXML("/Temp/SnapCode/src/snap/app/AppPane.snp");
    System.err.println(xml);
}

}