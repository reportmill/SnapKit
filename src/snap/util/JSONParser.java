/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import snap.web.*;

/**
 * A JSONParser subclass (with handlers).
 */
public class JSONParser extends Parser {
    
    /**
     * Creates a JSONParser.
     */
    public JSONParser()
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
    public JSONNode readSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        return readString(url.getText());
    }

    /**
     * Returns a KeyChain for given string.
     */
    public JSONNode readString(String aString)
    {
        // Parse string
        try { return parse(aString).getCustomNode(JSONNode.class); }
        catch(Throwable e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Object Handler.
     */
    public static class ObjectHandler extends ParseHandler <JSONNode> {

        /** Returns the part class. */
        protected Class <JSONNode> getPartClass()  { return JSONNode.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Pair
            if (anId=="Pair") {
                getPart()._type = JSONNode.Type.Object;
                getPart().addNode(aNode.getCustomNode(JSONNode.class));
            }
        }
    }

    /**
     * Pair Handler.
     */
    public static class PairHandler extends ParseHandler <JSONNode> {

        String _key;

        /** Returns the part class. */
        protected Class <JSONNode> getPartClass()  { return JSONNode.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle String
            if (anId=="String") {
                _key = aNode.getString();
                _key = _key.substring(1, _key.length()-1);
            }

            // Handle Value
            else if (anId=="Value") {
                Object value = aNode.getCustomNode();
                if(value instanceof JSONNode) { JSONNode jnode = (JSONNode)value;
                    jnode._key = _key;
                    _part = jnode;
                }
                else _part = new JSONNode(_key, value);
            }
        }
    }

    /**
     * Array Handler.
     */
    public static class ArrayHandler extends ParseHandler <JSONNode> {

        /** Returns the part class. */
        protected Class <JSONNode> getPartClass()  { return JSONNode.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Value
            if (anId=="Value") {
                getPart()._type = JSONNode.Type.Array;
                getPart().addValue(aNode.getCustomNode());
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
            if (anId=="Int" || anId=="Float") {
                try { _part = Double.valueOf(aNode.getString()); }
                catch(Exception e) { _part = new java.math.BigDecimal(aNode.getString()); }
            }

            // Handle String: Get string, strip quotes, convert any escaped forward slash to forward slash
            else if (anId=="String") {
                String str = aNode.getString();
                str = str.substring(1, str.length()-1);
                str = str.replace("\\/", "/");
                _part = str;
            }

            // Handle Boolean
            else if (anId=="Boolean")
                _part = Boolean.valueOf(aNode.getString());

            // Handle Object or Array - We need equals() for TeaVM bug (can go soon, bug was fixed)
            else if (anId.equals("Object") || anId=="Array")
                _part = aNode.getCustomNode();
        }
    }

    public static void main(String args[])
    {
        JSONNode jnode = new JSONParser().readSource(args[0]);
        System.out.println(jnode);
    }
}