/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.parse.*;
import static snap.util.StyleSheet.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Style sheet parser.
 */
public class StyleSheetParser extends Parser {

    // The style sheet grammar
    private static String STYLE_SHEET_GRAMMAR = """
            StyleSheet { Rule* }
            Rule { Selector "{" Declaration* "}" }
            Selector { Ident (":" Ident)? | "." Ident | "#" Ident }
            Declaration { Ident ":" }
            Ident { "[a-zA-Z_][\\w.\\-]*" }
            """;

    /**
     * Constructor.
     */
    public StyleSheetParser()
    {
        super();
    }

    @Override
    protected Grammar createGrammar()
    {
        try { return new GrammarParser().parseGrammarString(STYLE_SHEET_GRAMMAR); }
        catch (ParseException e) { throw new RuntimeException(e); }
    }

    /**
     * Override to install handlers.
     */
    @Override
    protected void initGrammar()
    {
        Grammar grammar = getGrammar();
        grammar.installHandlerForClass(StyleSheetHandler.class);
        grammar.installHandlerForClass(RuleHandler.class);
        grammar.installHandlerForClass(SelectorHandler.class);
        grammar.installHandlerForClass(DeclarationHandler.class);
    }

    /**
     * Parses given string to StyleSheet.
     */
    public StyleSheet parseString(String styleSheetString)
    {
        // Parse string
        try { return parse(styleSheetString).getCustomNode(StyleSheet.class); }
        catch(Throwable e) { e.printStackTrace(); }
        return null;
    }

    /**
     * StyleSheet Handler.
     */
    public static class StyleSheetHandler extends ParseHandler<StyleSheet> {

        /** Returns the part class. */
        protected Class<StyleSheet> getPartClass()  { return StyleSheet.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            StyleSheet styleSheet = getPart();

            // Handle Rule
            if (anId == "Rule") {
                Rule styleRule = aNode.getCustomNode(Rule.class);
                styleSheet.addRule(styleRule);
            }
        }
    }

    /**
     * Rule Handler.
     */
    public static class RuleHandler extends ParseHandler<Rule> {

        private Selector _selector;
        private List<Declaration> _declarations = new ArrayList<>();

        /** Returns the part class. */
        protected Class<Rule> getPartClass()  { return Rule.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {
                case "Selector" -> _selector = aNode.getCustomNode(Selector.class);
                case "Declaration" -> _declarations.add(aNode.getCustomNode(Declaration.class));
            }
        }

        @Override
        public Rule parsedAll()  { return new Rule(_selector, _declarations); }

        @Override
        public void reset()
        {
            _selector = null;
            _declarations = new ArrayList<>();
        }
    }

    /**
     * Selector Handler.
     */
    public static class SelectorHandler extends ParseHandler<Selector> {

        private String _name = "";
        private String _pseudoClass;

        /** Returns the part class. */
        protected Class<Selector> getPartClass()  { return Selector.class; }

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            if (anId =="Ident") {
                String ident = aNode.getString();
                if (_name.isEmpty())
                    _name = ident;
                else _pseudoClass = ident;
            }
        }

        @Override
        public Selector parsedAll()  { return new Selector(_name, _pseudoClass); }

        @Override
        public void reset()  { _name = ""; _pseudoClass = null; }
    }

    /**
     * Declaration Handler.
     */
    public static class DeclarationHandler extends ParseHandler<Declaration> {

        private String _key;
        private String _value;

        protected Class<Declaration> getPartClass()  { return Declaration.class; }

        public void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {
                case "Ident" -> _key = aNode.getString();
                case ":" -> _value = readDeclarationValueForNode(aNode);
            }
        }

        @Override
        public Declaration parsedAll()  { return new Declaration(_key, _value); }

        @Override
        public void reset()  { _key = _value = null; }
    }

    /**
     * Reads a declaration value from current parse location.
     */
    private static String readDeclarationValueForNode(ParseNode aNode)
    {
        Tokenizer tokenizer = aNode.getParser().getTokenizer();
        StringBuilder sb = new StringBuilder();

        // Read chars till we hit ';' or '}'
        while (tokenizer.hasChar()) {
            char c = tokenizer.nextChar();
            if (c == ';') {
                tokenizer.eatChar();
                break;
            }
            if (c == '}')
                break;
            sb.append(c);
            tokenizer.eatChar();
        }

        return sb.toString().trim();
    }
}