/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.parse.ParseRule.Op;

/**
 * A Parser to parse simple snap grammar rule files.
 */
public class GrammarParser extends Parser {

    // The Grammar currently being read
    private static Grammar _grammar;

    /**
     * Constructor.
     */
    public GrammarParser()
    {
        super();
    }

    /**
     * Parses given grammar string and returns grammar.
     */
    public Grammar parseGrammarString(String grammarStr)
    {
        synchronized (GrammarParser.class) {
            _grammar = new Grammar();
            parse(grammarStr);
            return _grammar;
        }
    }

    /**
     * Creates a new ParseRule rule.
     * <br>
     * Grammar { ParseRule* }
     * ParseRule { Name "{" Expression "}" }
     * Expression { AndExpr ( "|" AndExpr )* }
     * AndExpr { CountExpr CountExpr* }
     * CountExpr { Primary ( "*" "+" "?" )? }
     * Primary { String | "LookAhead" "(" (Number | Expression) ")" | Name | "(" Expression ")"
     */
    @Override
    protected Grammar createGrammar()
    {
        // Create rule builder
        ParseRuleBuilder builder = new ParseRuleBuilder();

        // Number, String, Name
        ParseRule number = builder.name("Number").pattern("[1-9][0-9]*").build();
        ParseRule string = builder.name("String").pattern("\"(([^\"\\\\])|(\\\\.))*\"").build();
        ParseRule name = builder.name("Name").pattern("[$a-zA-Z][$\\w]*").build();

        // Predefine Primary
        ParseRule primary = builder.name("Primary").build();

        // CountExpr { Primary ( "*" "+" "?" )? }
        ParseRule countOpsRule = builder.or("*").or("+").or("?").build();
        builder.name("CountExpr");
        builder.or(primary).and(countOpsRule, '?');
        ParseRule countExpr = builder.build();

        // AndExpr { CountExpr CountExpr* }
        ParseRule andExpr = builder.name("AndExpr").or(countExpr).and(countExpr, '*').build();

        // Expression { AndExpr ( "|" AndExpr )* }
        ParseRule andExprMore = builder.or("|").and(andExpr).build();
        builder.name("Expression").or(andExpr).and(andExprMore, '*');
        ParseRule expression = builder.build();

        // Primary { String | "LookAhead" "(" (Number | Expression) ")" | Name | "(" Expression ")" }
        ParseRule lookAheadArg = builder.or(number).or(expression).build();
        builder.reset(primary);
        builder.or(string);
        builder.or("LookAhead").and("(").and(lookAheadArg).and(")");
        builder.or(name);
        builder.or("(").and(expression).and(")");
        builder.build();

        // ParseRule { Name "{" Expression "}" }
        ParseRule parseRule = builder.name("ParseRule").or(name).and("{").and(expression).and("}").build();

        // Grammar { ParseRule* }
        ParseRule grammar = builder.name("Grammar").op(Op.ZeroOrMore).rule(parseRule).build();

        // Set handlers
        grammar.setHandler(new GrammarHandler());
        parseRule.setHandler(new ParseRuleHandler());
        expression.setHandler(new ExpressionHandler());
        andExpr.setHandler(new AndExprHandler());
        countExpr.setHandler(new CountExprHandler());
        primary.setHandler(new PrimaryHandler());

        // Return
        return new Grammar(grammar);
    }

    /**
     * Override to allow rules files to have standard Java single/multiple line comments.
     */
    @Override
    protected Tokenizer createTokenizer()
    {
        CodeTokenizer tokenizer = new CodeTokenizer();
        tokenizer.setReadSingleLineComments(true);
        tokenizer.setReadMultiLineComments(true);
        return tokenizer;
    }

    /**
     * Grammar Handler: { ParseRule* }
     */
    public static class GrammarHandler extends ParseHandler<Grammar> {

        /**
         * Returns the part class.
         */
        protected Class<Grammar> getPartClass()
        {
            return Grammar.class;
        }

        // Called when node is parsed.
        protected void parsedOne(ParseNode aNode, String anId)
        {
            _part = _grammar;
        }
    }

    /**
     * ParseRule Handler: { Name "{" Expression "}" }
     */
    public static class ParseRuleHandler extends ParseHandler<ParseRule> {

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass()  { return ParseRule.class; }

        // Called when node is parsed.
        protected void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Name
            if (anId == "Name") {
                String ruleName = aNode.getString();
                _part = _grammar.addRuleForName(ruleName);
            }

            // Handle Expression
            else if (anId == "Expression") {
                ParseRule rule = (ParseRule) aNode.getCustomNode();
                _part._op = rule._op;
                _part._child0 = rule._child0;
                _part._child1 = rule._child1;
                _part._pattern = rule._pattern;
                _part._lookAheadCount = rule._lookAheadCount;
            }
        }
    }

    /**
     * Expression Handler: { AndExpr ( "|" AndExpr )* }
     */
    public static class ExpressionHandler extends ParseHandler<ParseRule> {

        ParseRule _more;

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass() { return ParseRule.class; }

        /**
         * Called when node is parsed.
         */
        protected void parsedOne(ParseNode aNode, String anId)
        {
            // Handle AndExpr
            if (anId == "AndExpr") {
                ParseRule rule = (ParseRule) aNode.getCustomNode();
                if (_part == null) {
                    _part = rule;
                    _more = null;
                }
                else if (_more == null)
                    _part = _more = new ParseRule(Op.Or, _part, rule);
                else {
                    _more._child1 = new ParseRule(Op.Or, _more._child1, rule);
                    _more = _more._child1;
                }
            }
        }
    }

    /**
     * AndExpr Handler: { CountExpr CountExpr* }
     */
    public static class AndExprHandler extends ParseHandler<ParseRule> {

        ParseRule _more;

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass() { return ParseRule.class; }

        /**
         * Called when node is parsed.
         */
        protected void parsedOne(ParseNode aNode, String anId)
        {
            // Handle CountExpr
            if (anId == "CountExpr") {
                ParseRule rule = (ParseRule) aNode.getCustomNode();
                if (_part == null) {
                    _part = rule;
                    _more = null;
                }
                else if (_more == null)
                    _part = _more = new ParseRule(Op.And, _part, rule);
                else {
                    _more._child1 = new ParseRule(Op.And, _more._child1, rule);
                    _more = _more._child1;
                }
            }
        }
    }

    /**
     * CountExpr Handler: { Primary ( "*" "+" "?" )? }
     */
    public static class CountExprHandler extends ParseHandler<ParseRule> {

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass() { return ParseRule.class; }

        /**
         * Called when node is parsed.
         */
        protected void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {

                // Handle Primary
                case "Primary": _part = (ParseRule) aNode.getCustomNode(); break;

                // Handle Counts
                case "*": _part = new ParseRule(Op.ZeroOrMore, _part); break;
                case "+": _part = new ParseRule(Op.OneOrMore, _part); break;
                case "?": _part = new ParseRule(Op.ZeroOrOne, _part); break;
            }
        }
    }

    /**
     * Primary Handler: { String | "LookAhead" "(" (Number | Expression) ")" | Name | "(" Expression ")" }
     */
    public static class PrimaryHandler extends ParseHandler<ParseRule> {

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass() { return ParseRule.class; }

        /**
         * Called when node is parsed.
         */
        protected void parsedOne(ParseNode aNode, String anId)
        {
            switch (anId) {

                // Handle Name
                case "Name":
                    String ruleName = aNode.getString();
                    _part = _grammar.addRuleForName(ruleName);
                    break;

                // Handle String
                case "String":
                    String string = aNode.getString();
                    String pattern = string.substring(1, string.length() - 1);
                    getPart().setPattern(pattern);
                    break;

                // Handle Expression
                case "Expression":
                    ParseRule rule = (ParseRule) aNode.getCustomNode();
                    if (_part == null) _part = rule;
                    else _part._child0 = rule;  // LookAhead
                    break;

                // Handle LookAhead
                case "LookAhead":
                    getPart().setLookAheadCount(99);
                    break;

                // Handle Number
                case "Number":
                    String str = aNode.getString();
                    int count = Integer.parseInt(str);
                    getPart().setLookAheadCount(count);
                    break;
            }
        }
    }
}