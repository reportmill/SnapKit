/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;

/**
 * A class to load an XMLElement from aSource.
 */
public class XMLParser extends Parser {
    
    /**
     * Creates a new XMLParser.
     */
    public XMLParser()
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
        // Get string from source
        String str = SnapUtils.getText(aSource);
        if (str==null) {
            System.err.println("XMLParser.parseXML: Couldn't load text from source: " + aSource); return null; }

        // Parse string, get XML element and return
        ParseNode node = parse(str);
        XMLElement xml = (XMLElement)node.getCustomNode();
        return xml;
    }

    /**
     * Override to return XMLTokenizer.
     */
    protected Tokenizer createTokenizerImpl()  { return new XMLTokenizer(); }

    /**
     * A Tokenizer subclass to read XML contents.
     */
    public static class XMLTokenizer extends Tokenizer {

        /** Called to return the value of an element and update the char index. */
        protected String getContent()
        {
            // Mark content start and skip to next element-start char
            int start = _charIndex;
            while (!isNext("<") && hasChar())
                eatChar();

            // Handle CDATA: Gobble until close and return string
            if (isNext("<![CDATA[")) {
                _charIndex += "<![CDATA[".length();
                if (Character.isWhitespace(_charIndex)) eatChar();
                start = _charIndex;
                while (!isNext("]]>")) eatChar();
                String str = getInput().subSequence(start, _charIndex).toString();
                _charIndex += "]]>".length();
                return str;
            }

            // If next char isn't close tag, return null (assumes we hit child element instead of text content)
            if (!isNext("</"))
                return null;

            // Return string for content
            String str = getInput().subSequence(start, _charIndex).toString();
            return decodeXMLString(str);
        }

        /** Returns whether the given string is up next. */
        public boolean isNext(String aStr)
        {
            if (_charIndex+aStr.length()>length()) return false;
            for (int i=0,iMax=aStr.length();i<iMax;i++)
                if (charAt(_charIndex+i)!=aStr.charAt(i))
                    return false;
            return true;
        }
    }

    /**
     * Document Handler: Document { Prolog? DocType? Element }
     */
    public static class DocumentHandler extends ParseHandler <XMLElement> {

        /** Returns the part class. */
        protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Element
            if (anId=="Element")
                _part = (XMLElement)aNode.getCustomNode();
        }
    }

    /**
     * Prolog Handler: Prolog { "<?xml" Attribute* "?>" }
     */
    public static class PrologHandler extends ParseHandler <XMLElement> {

        /** Returns the part class. */
        protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Attribute
            if (anId=="Attribute")
                getPart().addAttribute((XMLAttribute)aNode.getCustomNode());
        }
    }

    /**
     * Element Handler: Element { "<" Name Attribute* ("/>" | (">" Content "</" Name ">")) }
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
            if (anId=="Name") {
                if (_part==null) { _part = new XMLElement(aNode.getString()); _checkedContent = false; }
                else if (!_part.getName().equals(aNode.getString()))
                    throw new RuntimeException("XMLParser: Expected closing tag " + _part.getName());
            }

            // Handle Attribute
            else if (anId=="Attribute")
                _part.addAttribute((XMLAttribute)aNode.getCustomNode());

            // Handle Element
            else if (anId=="Element")
                _part.addElement((XMLElement)aNode.getCustomNode());

            // Handle close: On first close, check for content
            else if (anId==">" && !_checkedContent) {
                XMLTokenizer xt = (XMLTokenizer)aNode.getParser().getTokenizer();
                String content = xt.getContent(); _checkedContent = true;
                _part.setValue(content);
            }
        }
    }

    /**
     * Attribute Handler: Attribute { Name "=" String }
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
            if (anId=="Name")
                _name = aNode.getString();

            // Handle String
            else if (anId=="String") { String str = aNode.getString();
                str = str.substring(1, str.length()-1);
                str = decodeXMLString(str);
                _part = new XMLAttribute(_name, str);
            }
        }
    }

    /** Converts an XML string to plain. This implementation is a bit bogus. */
    private static String decodeXMLString(String aStr)
    {
        // If no entity refs, just return
        if (aStr.indexOf('&')<0) return aStr;

        // Do common entity ref replacements
        aStr = aStr.replace("&amp;", "&");
        aStr = aStr.replace("&lt;", "<").replace("&gt;", ">");
        aStr = aStr.replace("&quot;", "\"").replace("&apos;", "'");

        // Iterate over string to find numeric/hex references and replace with char
        for (int start=aStr.indexOf("&#"); start>=0;start=aStr.indexOf("&#",start)) {
            int end = aStr.indexOf(";", start); if (end<0) continue;
            String str0 = aStr.substring(start, end+1), str1 = str0.substring(2,str0.length()-1);
            int val = Integer.valueOf(str1); String str2 = String.valueOf((char)val);
            aStr = aStr.replace(str0, str2);
        }

        // Return string
        return aStr;
    }

    /**
     * Test.
     */
    public static void main(String args[]) throws Exception
    {
        XMLParser parser = new XMLParser();
        XMLElement xml = parser.parseXML("/Temp/SnapCode/src/snap/app/AppPane.snp");
        System.err.println(xml);
    }
}