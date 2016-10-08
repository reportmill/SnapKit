/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.*;
import snap.parse.ParseRule.*;

/**
 * A Parser to parse simple snap grammar rule files.
 */
public class ParseRuleParser extends Parser {

/**
 * Creates a new ParseRule rule.
 */
public ParseRule createRule()
{
    // Number, String, Name
    ParseRule number = new ParseRule("Number").setPattern("[1-9][0-9]*");
    ParseRule string = new ParseRule("String").setPattern("\"(([^\"\\\\\\n\\r])|(\\\\.))*\"");
    ParseRule name = new ParseRule("Name").setPattern("[$a-zA-Z][$\\w]*");
    
    // Predefine Expression
    ParseRule expression = new ParseRule("Expression");
    
    // CountExpr { Expression ( "*" "+" "?" )? }
    ParseRule countExpr = new ParseRule("CountExpr");
    countExpr.or(expression).and(new ParseRule().or("*").or("+").or("?"), '?');
    
    // AndExpr { CountExpr CountExpr* }
    ParseRule andExpr = new ParseRule("AndExpr").or(countExpr).and(countExpr, '*');
    
    // OrExpr { AndExpr ( "|" AndExpr )* }
    ParseRule orExpr = new ParseRule("OrExpr").or(andExpr).and(new ParseRule().or("|").and(andExpr), '*');
    
    // Expression { String | "LookAhead" "(" (Number | OrExpr) ")" | Name | "(" OrExpr ")" }
    expression.or(string).or("LookAhead").and("(").and(new ParseRule().or(number).or(orExpr)).and(")");
    expression.or(name).or("(").and(orExpr).and(")");
    
    // ParseRule { Name "{" OrExpr "}" }
    ParseRule prrule = new ParseRule("ParseRule").or(name).and("{").and(orExpr).and("}");
    
    // ParseRuleFile { ParseRule* }
    ParseRule prfile = new ParseRule("ParseRuleFile", Op.ZeroOrMore, prrule);
    
    // Set handlers and return file rule
    expression.setHandler(new ExpressionHandler());
    countExpr.setHandler(new CountExprHandler());
    andExpr.setHandler(new AndExprHandler());
    orExpr.setHandler(new OrExprHandler());
    prrule.setHandler(new ParseRuleHandler());
    prfile.setHandler(new ParseRuleFileHandler());
    return prfile;
}

/**
 * Override to allow rules files to have standard Java single/multiple line comments.
 */
protected Tokenizer createTokenizer()
{
    Tokenizer tz = super.createTokenizer();
    tz.setReadSingleLineComments(true); tz.setReadMultiLineComments(true);
    return tz;
}

/**
 * Returns a named rule.
 */
private static ParseRule getRule2(String aName)
{
    ParseRule rule = _rules.get(aName);
    if(rule==null)
        _rules.put(aName, rule=new ParseRule(aName));
    return rule;
} static Map <String,ParseRule> _rules = new HashMap();

/**
 * ParseRuleFile Handler: { ParseRule* }
 */
public static class ParseRuleFileHandler extends ParseHandler <ParseRule> {

    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    // Called when node is parsed.
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Get first
        if(_part==null) {
            ParseRule rule = (ParseRule)aNode.getCustomNode();
            if(rule.getPattern()==null)
                _part = rule;
        }
    }
    
    /** Override to reset Rules map. */
    public ParseRule parsedAll()  { _rules = new HashMap(); return super.parsedAll(); }
}
        
/**
 * ParseRule Handler: { Name "{" OrExpr "}" }
 */
public static class ParseRuleHandler extends ParseHandler <ParseRule> {

    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    // Called when node is parsed.
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _part = getRule2(aNode.getString());
        
        // Handle OrExpr
        if(anId=="OrExpr") {
            ParseRule rule = (ParseRule)aNode.getCustomNode();
            _part._op = rule._op;
            _part._child0 = rule._child0; _part._child1 = rule._child1;
            _part._pattern = rule._pattern; _part._literal = rule._literal;
            _part._lookAhead = rule._lookAhead;
        }
    }
}

/**
 * OrExpr Handler: { AndExpr ( "|" AndExpr )* }
 */
public static class OrExprHandler extends ParseHandler <ParseRule> {

    ParseRule _more;

    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle AndExpr
        if(anId=="AndExpr") {
            ParseRule rule = (ParseRule)aNode.getCustomNode();
            if(_part==null) { _part = rule; _more = null; }
            else if(_more==null) _part = _more = new ParseRule(Op.Or, _part, rule);
            else { _more._child1 = new ParseRule(Op.Or, _more._child1, rule); _more = _more._child1; }
        }
    }
}

/**
 * AndExpr Handler: { CountExpr CountExpr* }
 */
public static class AndExprHandler extends ParseHandler <ParseRule> {

    ParseRule _more;
    
    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle CountExpr
        if(anId=="CountExpr") {
            ParseRule rule = (ParseRule)aNode.getCustomNode();
            if(_part==null) { _part = rule; _more = null; }
            else if(_more==null) _part = _more = new ParseRule(Op.And, _part, rule);
            else { _more._child1 = new ParseRule(Op.And, _more._child1, rule); _more = _more._child1; }
        }
    }
}

/**
 * CountExpr Handler: { Expression ( "*" "+" "?" )? }
 */
public static class CountExprHandler extends ParseHandler <ParseRule> {

    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Expression
        if(anId=="Expression")
            _part = (ParseRule)aNode.getCustomNode();
        
        // Handle Counts
        else if(anId=="*") _part = new ParseRule(Op.ZeroOrMore, _part);
        else if(anId=="+") _part = new ParseRule(Op.OneOrMore, _part);
        else if(anId=="?") _part = new ParseRule(Op.ZeroOrOne, _part);
    }
}

/**
 * Expression Handler: { String | "LookAhead" "(" (Number | OrExpr) ")" | Name | "(" OrExpr ")" }
 */
public static class ExpressionHandler extends ParseHandler <ParseRule> {

    /** Returns the part class. */
    protected Class <ParseRule> getPartClass()  { return ParseRule.class; }

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _part = getRule2(aNode.getString());
        
        // Handle string
        else if(anId=="String") {
            String string = aNode.getString(), pattern = string.substring(1, string.length()-1);
            getPart().setPattern(pattern);
        }
        
        // Handle OrExpr
        else if(anId=="OrExpr") {
            ParseRule rule = (ParseRule)aNode.getCustomNode();
            if(_part==null) _part = rule;
            else _part._child0 = rule;  // LookAhead
        }
        
        // Handle LookAhead
        else if(aNode.getPattern()=="LookAhead")
            getPart().setLookAhead(99);
        
        // Handle Number
        else if(anId=="Number") {
            String str = aNode.getString(); int count = Integer.valueOf(str);
            getPart().setLookAhead(count);
        }
    }
}

}