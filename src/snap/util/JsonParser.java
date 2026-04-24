/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import snap.web.*;
import java.util.stream.Stream;

/**
 * A JSON parser.
 */
public class JsonParser extends Parser {
    
    /**
     * Constructor.
     */
    public JsonParser()
    {
        super();
    }

    /**
     * Override to install handlers.
     */
    @Override
    protected void initGrammar()
    {
        Grammar grammar = getGrammar();
        grammar.installHandlerForClass(ObjectHandler.class);
        grammar.installHandlerForClass(PairHandler.class);
        grammar.installHandlerForClass(ArrayHandler.class);
        grammar.installHandlerForClass(ValueHandler.class);
        grammar.installHandlerForClass(StringHandler.class);
    }

    /**
     * Reads JSON from a source.
     */
    public JsonNode readSource(Object aSource)
    {
        WebURL url = WebURL.getUrl(aSource); assert (url != null);
        String urlText = url.getText();
        return readString(urlText);
    }

    /**
     * Returns a KeyChain for given string.
     */
    public JsonNode readString(String aString)
    {
        // Parse string
        try { return parse(aString).getCustomNode(JsonNode.class); }
        catch(Throwable e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Override to return custom tokenizer to handle string.
     */
    @Override
    protected Tokenizer createTokenizer()  { return new JSTokenizer(); }

    /**
     * Object Handler.
     */
    public static class ObjectHandler extends ParseHandler<JsonObject> {

        /** Returns the part class. */
        protected Class<JsonObject> getPartClass()  { return JsonObject.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            JsonObject jsonObj = getPart();

            // Handle Pair
            if (anId == "Pair") {
                JSPair pair = aNode.getCustomNode(JSPair.class);
                jsonObj.setValue(pair._key, pair._value);
            }
        }
    }

    /**
     * Pair Handler.
     */
    public static class PairHandler extends ParseHandler<JSPair> {

        /** Returns the part class. */
        protected Class<JSPair> getPartClass()  { return JSPair.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            JSPair jsonPair = getPart();

            // Handle String
            if (anId == "String")
                jsonPair._key = aNode.getCustomNode(String.class);

            // Handle Value
            else if (anId == "Value") {
                Object value = aNode.getCustomNode();
                if(value instanceof JsonNode)
                    jsonPair._value = (JsonNode) value;
                else jsonPair._value = new JsonNode(value);
            }
        }
    }

    public static class JSPair {
        String  _key;
        JsonNode _value;
    }

    /**
     * Array Handler.
     */
    public static class ArrayHandler extends ParseHandler <JsonArray> {

        /** Returns the part class. */
        protected Class<JsonArray> getPartClass()  { return JsonArray.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            JsonArray jsonArray = getPart();

            // Handle Value
            if (anId == "Value") {
                Object value = aNode.getCustomNode();
                if (value instanceof JsonNode)
                    jsonArray.addValue(((JsonNode) value));
                else jsonArray.addNativeValue(value);
            }
        }
    }

    /**
     * Value Handler.
     */
    public static class ValueHandler extends ParseHandler <Object> {

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {

                // Handle INT or Float
                case "Int": case "Float":
                    try { _part = Double.valueOf(aNode.getString()); }
                    catch (Exception e) { _part = new java.math.BigDecimal(aNode.getString()); }
                    break;

                // Handle String
                case "String": _part = aNode.getCustomNode(String.class); break;

                // Handle Boolean
                case "Boolean": _part = Boolean.valueOf(aNode.getString()); break;

                // Handle Object or Array
                case "Object": case "Array": _part = aNode.getCustomNode(); break;
            }
        }
    }

    /**
     * String Handler.
     */
    public static class StringHandler extends ParseHandler<String> {

        /**
         * ParseHandler method.
         */
        public void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {

                // Handle StringStart
                case "StringStart": _part = ""; break;

                // Handle StringMore: convert any escaped forward slash to forward slash
                case "StringMore":
                    String strMore = aNode.getString();
                    if (!strMore.equals("\"")) {
                        strMore = strMore.replace("\\/", "/");
                        _part += strMore;
                    }
                    break;
            }
        }
    }

    public static void main(String[] args)
    {
        JsonNode jnode = new JsonParser().readSource(args[0]);
        System.out.println(jnode);
    }

    /**
     * Custom tokenizer to handle string.
     */
    private static class JSTokenizer extends Tokenizer {

        // String regexes
        private Regex[] _stringRegexes;

        // Constants
        public static final String STRING_START = "StringStart";
        public static final String STRING_MORE = "StringMore";
        public static final String STRING_PATTERN = "\"";

        /**
         * Sets the regexes for given grammar.
         */
        public void setRegexesForGrammar(Grammar aGrammar)
        {
            // If no TextBlock rule, add it
            aGrammar.getRuleForName(STRING_START).setPattern(STRING_PATTERN);
            _stringRegexes = new Regex[2];
            _stringRegexes[0] = new Regex(STRING_MORE, "(?s).*?(?=\"|\\z)");
            _stringRegexes[1] = new Regex(STRING_MORE, STRING_PATTERN);

            // Do normal
            super.setRegexesForGrammar(aGrammar);
        }

        /**
         * Sets the current tokenizer input.
         */
        @Override
        public void setInput(CharSequence anInput)
        {
            super.setInput(anInput);
            Stream.of(_stringRegexes).forEach(regex -> regex.getMatcher().reset(anInput));
        }

        /**
         * Returns list of Regex for a starting char.
         */
        @Override
        protected Regex[] getRegexesForStartChar(char aChar)
        {
            // If LastToken is MultiLineComment, use special regexes
            ParseToken lastToken = getLastToken();
            if (lastToken != null) {
                String lastTokenName = lastToken.getName();
                if (lastTokenName == STRING_START || lastTokenName == STRING_MORE && !lastToken.getPattern().equals(STRING_PATTERN))
                    return _stringRegexes;
            }

            // Do normal version
            return super.getRegexesForStartChar(aChar);
        }
    }
}