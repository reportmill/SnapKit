/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;

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
    String str = SnapUtils.getText(aSource);
    //WebURL url = WebURL.getURL(aSource);
    //String str = url!=null? url.getText() : null;
    //if(str==null && aSource instanceof byte[]) str = new String((byte[])aSource);
    return (XMLElement)parse(str).getCustomNode();
}

/**
 * Override to return XMLTokenizer.
 */
protected Tokenizer createTokenizerImpl()  { return new XMLTokenizer(); }

/**
 * A Tokenizer subclass to read XML contents.
 */
private static class XMLTokenizer extends Tokenizer {
    
    /** Called to return the value of an element and update the char index. */
    protected String getContent()
    {
        // Skip whitespace
        skipWhiteSpace();
        
        // Handle CDATA: Gobble until close
        if(isNext("<![CDATA[")) {
            _charIndex += "<![CDATA[".length(); if(Character.isWhitespace(_charIndex)) _charIndex++;
            StringBuffer sb = new StringBuffer();
            while(!isNext("]]>"))
                sb.append(charAt(_charIndex++));
            _charIndex += "]]>".length();
            return sb.toString();
        }
        
        // If next char is '<', just return
        if(isNext("<"))
            return null;
        
        // Handle Text: Gobble until next element
        StringBuffer sb = new StringBuffer();
        while(!isNext("<") && _charIndex<length())
            sb.append(charAt(_charIndex++));
        return decodeXMLString(sb.toString());
    }

    /** Returns whether the given string is up next. */
    public boolean isNext(String aStr)
    {
        if(_charIndex+aStr.length()>length()) return false;
        for(int i=0,iMax=aStr.length();i<iMax;i++)
            if(charAt(_charIndex+i)!=aStr.charAt(i))
                return false;
        return true;
    }
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
    
    // Whether element has checked content
    boolean   _checkedContent;
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name") {
            if(_part==null) { _part = new XMLElement(aNode.getString()); _checkedContent = false; }
            else if(!_part.getName().equals(aNode.getString()))
                throw new RuntimeException("XMLParser: Expected closing tag " + _part.getName());
        }
            
        // Handle Attribute
        else if(anId=="Attribute")
            _part.addAttribute((XMLAttribute)aNode.getCustomNode());
            
        // Handle Element
        else if(anId=="Element")
            _part.addElement((XMLElement)aNode.getCustomNode());
            
        // Handle close: On first close, check for content
        else if(anId==">" && !_checkedContent) {
            XMLTokenizer xt = (XMLTokenizer)aNode.getParser().getTokenizer();
            String content = xt.getContent(); _checkedContent = true;
            _part.setValue(content);
        }
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
        else if(anId=="String") { String str = aNode.getString(); str = str.substring(1, str.length()-1);
            str = decodeXMLString(str);
            _part = new XMLAttribute(_name, str);
        }
    }
}

/** Converts an XML string to plain. This implementation is a bit bogus. */
private static String decodeXMLString(String aStr)
{
    if(aStr.indexOf('&')<0) return aStr;
    aStr = aStr.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    aStr = aStr.replace("&quot;", "\"").replace("&apos;", "'");
    int start = 0; while(aStr.indexOf("&#", start)>=0) { start += 2;
        int ind0 = aStr.indexOf("&#"), ind1 = aStr.indexOf(";", ind0); if(ind1<0) continue;
        String str0 = aStr.substring(ind0, ind1+1), str1 = str0.substring(2,str0.length()-1);
        int val = Integer.valueOf(str1); String str2 = String.valueOf((char)val);
        aStr = aStr.replace(str0, str2);
    }
    return aStr;
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