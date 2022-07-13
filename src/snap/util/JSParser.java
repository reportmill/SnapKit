/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import snap.web.*;

/**
 * A JSONParser subclass (with handlers).
 */
public class JSParser extends Parser {
    
    /**
     * Creates a JSONParser.
     */
    public JSParser()
    {
        // Install Hanlders
        installHandlers(new ObjectHandler(), new PairHandler(), new ArrayHandler(), new ValueHandler());
    }

    /**
     * Installs handlers for given handler class.
     */
    void installHandlers(ParseHandler ... theHandlers)
    {
        for (ParseHandler hdlr : theHandlers) {
            String name = hdlr.getClass().getSimpleName().replace("Handler", "");
            getRule(name).setHandler(hdlr);
        }
    }

    /**
     * Reads JSON from a source.
     */
    public JSValue readSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        return readString(url.getText());
    }

    /**
     * Returns a KeyChain for given string.
     */
    public JSValue readString(String aString)
    {
        // Parse string
        try { return parse(aString).getCustomNode(JSValue.class); }
        catch(Throwable e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Object Handler.
     */
    public static class ObjectHandler extends ParseHandler<JSObject> {

        /** Returns the part class. */
        protected Class<JSObject> getPartClass()  { return JSObject.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Pair
            if (anId == "Pair") {
                JSPair pair = aNode.getCustomNode(JSPair.class);
                getPart().setValue(pair._key, pair._value);
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
            // Handle String
            if (anId == "String") {
                String key = aNode.getString();
                key = key.substring(1, key.length()-1);
                getPart()._key = key;
            }

            // Handle Value
            else if (anId == "Value") {
                Object value = aNode.getCustomNode();
                if(value instanceof JSValue)
                    getPart()._value = (JSValue) value;
                else getPart()._value = new JSValue(value);
            }
        }
    }

    public static class JSPair {
        String  _key;
        JSValue _value;
    }

    /**
     * Array Handler.
     */
    public static class ArrayHandler extends ParseHandler <JSArray> {

        /** Returns the part class. */
        protected Class<JSArray> getPartClass()  { return JSArray.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Value
            if (anId == "Value") {
                Object value = aNode.getCustomNode();
                if (value instanceof JSValue)
                    getPart().addValue(((JSValue) value));
                else getPart().addNativeValue(value);
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
            // Handle INT or Float
            if (anId == "Int" || anId == "Float") {
                try { _part = Double.valueOf(aNode.getString()); }
                catch(Exception e) { _part = new java.math.BigDecimal(aNode.getString()); }
            }

            // Handle String: Get string, strip quotes, convert any escaped forward slash to forward slash
            else if (anId == "String") {
                String str = aNode.getString();
                str = str.substring(1, str.length()-1);
                str = str.replace("\\/", "/");
                _part = str;
            }

            // Handle Boolean
            else if (anId == "Boolean")
                _part = Boolean.valueOf(aNode.getString());

            // Handle Object or Array
            else if (anId == "Object" || anId == "Array")
                _part = aNode.getCustomNode();
        }
    }

    public static void main(String args[])
    {
        JSValue jnode = new JSParser().readSource(args[0]);
        System.out.println(jnode);
    }
}