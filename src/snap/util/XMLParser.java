/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import snap.web.WebURL;

/**
 * A class to load an XMLElement from aSource.
 */
public class XMLParser extends Parser {

    // Whether parser supports mixed content (additional text after child elements)
    private boolean _allowMixedContent;

    /**
     * Constructor.
     */
    public XMLParser()
    {
        super();
    }

    /**
     * Enabled mixed content.
     */
    public void allowMixedContent()  { _allowMixedContent = true; }

    /**
     * Override to install handlers.
     */
    @Override
    protected void initGrammar()
    {
        Grammar grammar = getGrammar();
        grammar.installHandlerForClass(DocumentHandler.class);
        grammar.installHandlerForClass(PrologHandler.class);
        grammar.installHandlerForClass(ElementHandler.class);
        grammar.installHandlerForClass(AttributeHandler.class);
        if (_allowMixedContent)
            getRuleForName("Element").setHandler(new ElementHandlerWithMixedContent());
    }

    /**
     * Kicks off xml parsing from given URL and builds on this parser's element.
     */
    public XMLElement parseXMLFromUrl(WebURL xmlUrl)
    {
        // Get XML string from source
        String xmlString = xmlUrl.getText();
        if (xmlString == null) {
            System.err.println("XMLParser.parseXMLFromUrl: Couldn't load text from url: " + xmlUrl);
            return null;
        }

        // Parse XML from string and return
        return parseXMLFromString(xmlString);
    }

    /**
     * Kicks off xml parsing from given source and builds on this parser's element.
     */
    public XMLElement parseXMLFromString(String xmlString)
    {
        ParseNode node = parse(xmlString);
        return (XMLElement) node.getCustomNode();
    }

    /**
     * Override to return XMLTokenizer.
     */
    protected Tokenizer createTokenizer()  { return new XMLTokenizer(); }

    /**
     * A Tokenizer subclass to read XML contents.
     */
    public static class XMLTokenizer extends Tokenizer {

        /** Override to eat xml comments. */
        @Override
        protected ParseToken getNextTokenImpl()
        {
            // If XML comment, eat chars till XML comment terminator
            if (nextCharsStartWith("<!--")) {
                while (!nextCharsStartWith("-->"))
                    eatChar();
                eatChars("-->".length());
            }

            return super.getNextTokenImpl();
        }

        /** Called to return the value of an element and update the char index. */
        protected String getContent()
        {
            // Mark content start and skip to next element-start char
            int start = _charIndex;
            while (!nextCharEquals('<') && hasChar())
                eatChar();

            // Handle CDATA: Gobble until close and return string
            if (nextCharsStartWith("<![CDATA[")) {
                eatChars("<![CDATA[".length());
                if (Character.isWhitespace(nextChar()))
                    eatChar();
                start = _charIndex;
                while (!nextCharsStartWith("]]>"))
                    eatChar();
                String str = getInput().subSequence(start, _charIndex).toString();
                eatChars("]]>".length());
                return str;
            }

            // If next char isn't close tag, return null (assumes we hit child element instead of text content)
            //if (!nextCharsStartWith("</")) return null;

            // Return string for content
            String str = getInput().subSequence(start, _charIndex).toString();
            if (CharSequenceUtils.isWhiteSpace(str))
                return null;
            return decodeXMLString(str.trim());
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
            if (anId == "Element")
                _part = (XMLElement)aNode.getCustomNode();
        }
    }

    /**
     * Prolog Handler: {@code Prolog { "<?xml" Attribute* "?>" }}
     */
    public static class PrologHandler extends ParseHandler <XMLElement> {

        /** Returns the part class. */
        protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Attribute
            if (anId == "Attribute")
                getPart().addAttribute((XMLAttribute)aNode.getCustomNode());
        }
    }

    /**
     * Element Handler: {@code Element { "<" Name Attribute* ("/>" | (">" Content "</" Name ">")) }}
     */
    public static class ElementHandler extends ParseHandler <XMLElement> {

        // Whether element has checked content
        boolean   _checkedContent;

        /** Returns the part class. */
        protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {

                // Handle Name
                case "Name" -> {
                    if (_part == null) {
                        _part = new XMLElement(aNode.getString());
                        _checkedContent = false;
                    }
                    else if (!_part.getName().equals(aNode.getString()))
                        throw new RuntimeException("XMLParser: Expected closing tag " + _part.getName());
                }

                // Handle Attribute
                case "Attribute" -> _part.addAttribute((XMLAttribute) aNode.getCustomNode());

                // Handle Element
                case "Element" -> _part.addElement((XMLElement) aNode.getCustomNode());

                // Handle close: On first close, check for content
                case ">" -> {
                    if (!_checkedContent) {
                        XMLTokenizer tokenizer = (XMLTokenizer) aNode.getParser().getTokenizer();
                        String content = tokenizer.getContent();
                        if (content != null)
                            _part.setValue(content);
                        _checkedContent = true;
                    }
                }
            }
        }
    }

    /**
     * Element Handler with support for mixed content.
     */
    public static class ElementHandlerWithMixedContent extends ElementHandler {

        /**
         * ParseHandler method.
         */
        public void parsedOne(ParseNode aNode, String anId)
        {
            super.parsedOne(aNode, anId);

            // After child element is found, see if there is text content
            if (anId == "Element") {
                XMLTokenizer tokenizer = (XMLTokenizer) aNode.getParser().getTokenizer();
                String moreContent = tokenizer.getContent();
                if (moreContent != null) {
                    String prevContent = _part.getValue();
                    String newContent = prevContent != null ? prevContent + ' ' + moreContent : moreContent;
                    _part.setValue(newContent);
                }
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
            switch (anId) {
                case "Name":
                    _name = aNode.getString();
                    break;

                // Handle String
                case "String":
                    String str = aNode.getString();
                    str = str.substring(1, str.length() - 1);
                    str = decodeXMLString(str);
                    _part = new XMLAttribute(_name, str);
                    break;
            }
        }
    }

    /** Converts an XML string to plain. This implementation is a bit bogus. */
    private static String decodeXMLString(String aStr)
    {
        // If no entity refs, just return
        if (aStr.indexOf('&') < 0)
            return aStr;

        // Do common entity ref replacements
        aStr = aStr.replace("&amp;", "&");
        aStr = aStr.replace("&lt;", "<").replace("&gt;", ">");
        aStr = aStr.replace("&quot;", "\"").replace("&apos;", "'");

        // Iterate over string to find numeric/hex references and replace with char
        for (int start = aStr.indexOf("&#"); start >= 0; start = aStr.indexOf("&#", start)) {
            int end = aStr.indexOf(";", start);
            if (end < 0)
                continue;
            String str0 = aStr.substring(start, end + 1);
            String str1 = str0.substring(2, str0.length() - 1);
            int val = Integer.parseInt(str1);
            String str2 = String.valueOf((char) val);
            aStr = aStr.replace(str0, str2);
        }

        // Return string
        return aStr;
    }
}