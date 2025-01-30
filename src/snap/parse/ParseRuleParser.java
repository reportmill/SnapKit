/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.parse.ParseRule.Op;

import java.util.HashMap;
import java.util.Map;

/**
 * A Parser to parse simple snap grammar rule files.
 */
public class ParseRuleParser extends Parser {

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
    public ParseRule createRule()
    {
        // Number, String, Name
        ParseRule number = new ParseRule("Number").setPattern("[1-9][0-9]*");
        ParseRule string = new ParseRule("String").setPattern("\"(([^\"\\\\])|(\\\\.))*\"");
        ParseRule name = new ParseRule("Name").setPattern("[$a-zA-Z][$\\w]*");

        // Predefine Primary
        ParseRule primary = new ParseRule("Primary");

        // CountExpr { Primary ( "*" "+" "?" )? }
        ParseRule countExpr = new ParseRule("CountExpr");
        countExpr.or(primary).and(new ParseRule().or("*").or("+").or("?"), '?');

        // AndExpr { CountExpr CountExpr* }
        ParseRule andExpr = new ParseRule("AndExpr").or(countExpr).and(countExpr, '*');

        // Expression { AndExpr ( "|" AndExpr )* }
        ParseRule expression = new ParseRule("Expression").or(andExpr).and(new ParseRule().or("|").and(andExpr), '*');

        // Primary { String | "LookAhead" "(" (Number | Expression) ")" | Name | "(" Expression ")" }
        primary.or(string);
        primary.or("LookAhead").and("(").and(new ParseRule().or(number).or(expression)).and(")");
        primary.or(name);
        primary.or("(").and(expression).and(")");

        // ParseRule { Name "{" Expression "}" }
        ParseRule parseRule = new ParseRule("ParseRule").or(name).and("{").and(expression).and("}");

        // Grammar { ParseRule* }
        ParseRule grammar = new ParseRule("Grammar", Op.ZeroOrMore, parseRule);

        // Set handlers
        grammar.setHandler(new GrammarHandler());
        parseRule.setHandler(new ParseRuleHandler());
        expression.setHandler(new ExpressionHandler());
        andExpr.setHandler(new AndExprHandler());
        countExpr.setHandler(new CountExprHandler());
        primary.setHandler(new PrimaryHandler());

        // Return
        return grammar;
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
     * Returns a named rule.
     */
    private static ParseRule getRule2(String aName)
    {
        ParseRule rule = _rules.get(aName);
        if (rule == null)
            _rules.put(aName, rule = new ParseRule(aName));
        return rule;
    }

    static Map<String, ParseRule> _rules = new HashMap<>();

    /**
     * Grammar Handler: { ParseRule* }
     */
    public static class GrammarHandler extends ParseHandler<ParseRule> {

        /**
         * Returns the part class.
         */
        protected Class<ParseRule> getPartClass()
        {
            return ParseRule.class;
        }

        // Called when node is parsed.
        protected void parsedOne(ParseNode aNode, String anId)
        {
            // Get first
            if (_part == null) {
                ParseRule rule = (ParseRule) aNode.getCustomNode();
                if (rule.getPattern() == null)
                    _part = rule;
            }
        }

        /**
         * Override to reset Rules map.
         */
        public ParseRule parsedAll()
        {
            _rules = new HashMap<>();
            return super.parsedAll();
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
            if (anId == "Name")
                _part = getRule2(aNode.getString());

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
                    _part = getRule2(aNode.getString());
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