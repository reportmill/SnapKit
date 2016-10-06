/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.*;

/**
 * A class to represent a parse rule.
 */
public class ParseRule {

    // The name
    String              _name;

    // The op
    Op                  _op;
    
    // The child rules
    ParseRule           _child0, _child1;

    // Rule pattern - if simple regex
    String              _pattern;
    
    // Returns whether the pattern is literal
    boolean             _literal;
    
    // The look ahead count (implies this rule is really just a look ahead
    int                 _lookAhead;
    
    // The handler for parse rule
    ParseHandler        _handler;
    
    // Constants for booleans operators
    public enum Op { Or, And, ZeroOrOne, ZeroOrMore, OneOrMore, LookAhead, Pattern }
    
/**
 * Creates a new parse rule.
 */
public ParseRule()  { }

/**
 * Creates a new parse rule for given name
 */
public ParseRule(String aName)  { setName(aName); }

/**
 * Creates a new parse rule for given name
 */
public ParseRule(Op anOp, ParseRule aPR)  { this(null, anOp, aPR, null); }

/**
 * Creates a new parse rule for given op and child rules.
 */
public ParseRule(Op anOp, ParseRule aPR1, ParseRule aPR2)  { this(null, anOp, aPR1, aPR2); }

/**
 * Creates a new parse rule for given name, op and child rule.
 */
public ParseRule(String aName, Op anOp, ParseRule aPR)  { this(aName, anOp, aPR, null); }

/**
 * Creates a new parse rule for given name, op and child rules.
 */
public ParseRule(String aName, Op anOp, ParseRule aPR1, ParseRule aPR2)
{
    setName(aName); _op = anOp; _child0 = aPR1; _child1 = aPR2;
}

/**
 * Returns rule name.
 */
public String getName()  { return _name; }

/**
 * Sets rule name.
 */
public void setName(String aName)  { _name = aName!=null? aName.intern() : null; }

/**
 * Returns an identifier string for this rule - either the name or the pattern or null.
 */
public String getId()  { return _name!=null? _name : _pattern!=null? _pattern : null; }

/**
 * Returns the op.
 */
public Op getOp()  { return _op; }

/**
 * Returns the first child.
 */
public ParseRule getChild0()  { return _child0; }

/**
 * Returns the second child.
 */
public ParseRule getChild1()  { return _child1; }

/**
 * Returns the rule pattern if simple pattern.
 */
public String getPattern()  { return _pattern; }

/**
 * Sets the rule pattern if simple pattern.
 */
public ParseRule setPattern(String anPattern)
{
    _pattern = anPattern.intern(); _op = Op.Pattern;
    _literal = _pattern.length()<3 || _pattern.indexOf('[')<0;
    return this;
}

/**
 * Returns whether pattern is literal.
 */
public boolean isLiteral()  { return _literal; }

/**
 * Sets whether pattern is literal.
 */
public ParseRule setLiteral(boolean aFlag)  { _literal = aFlag; return this; }

/**
 * Returns whether rule is anonymous - true if rule has no name and no pattern.
 */
public boolean isAnonymous()  { return getName()==null && getPattern()==null; }

/**
 * Returns whether rule is optional.
 */
public boolean isOptional()
{
    return _op==Op.ZeroOrOne || _op==Op.ZeroOrMore || _op==Op.And && _child0.isOptional() && _child1.isOptional();
}

/**
 * Returns the handler for this rule.
 */
public ParseHandler getHandler()  { return _handler; }

/**
 * Sets the handler for this rule.
 */
public ParseRule setHandler(ParseHandler aHandler)  { _handler = aHandler; return this; }

/**
 * Returns whether rule is look ahead.
 */
public boolean isLookAhead()  { return _lookAhead>0; }

/**
 * Returns the look ahead count.
 */
public int getLookAhead()  { return _lookAhead; }

/**
 * Sets the look ahead count.
 */
public ParseRule setLookAhead(int aValue)  { _lookAhead = aValue; _op = Op.LookAhead; return this; }

/**
 * Returns a rule with given name.
 */
public ParseRule getRule(String aName)
{
    ParseRule rule = getNamedRules().get(aName);
    if(rule==null)
        throw new RuntimeException("ParseRule: Rule not found for name " + aName);
    return rule;
}

/**
 * Returns all the rules.
 */
private Map <String,ParseRule> getNamedRules()
{
    if(_nrules!=null) return _nrules;
    addNamedRule(this, new HashMap());
    return _nrules;
}

/** Loads the named rules. */
private void addNamedRule(ParseRule aRule, Map <String,ParseRule> aMap)
{
    String name = aRule.getName();
    if(name!=null && aMap.get(name)!=null) return;
    if(name!=null) { aMap.put(name, aRule); aRule._nrules = aMap; }
    if(aRule.getChild0()!=null) addNamedRule(aRule.getChild0(), aMap);
    if(aRule.getChild1()!=null) addNamedRule(aRule.getChild1(), aMap);
}

// The named rules
private Map <String,ParseRule> _nrules;

/**
 * Adds an Or rule to this rule with given pattern.
 */
public ParseRule or(String aPattern)  { return or(aPattern, '1'); }

/**
 * Adds an Or rule to this rule with given count and pattern.
 */
public ParseRule or(String aPattern, char aCount)
{ ParseRule r = new ParseRule(); r.setPattern(aPattern); return or(r, aCount); }

/**
 * Adds an Or rule to this rule with given rule.
 */
public ParseRule or(ParseRule aRule)  { return or(aRule, '1'); }

/**
 * Adds an Or rule to this rule with given rule.
 */
public ParseRule or(ParseRule aRule, char aCount)
{
    // Wrap rule in count rule
    if(aCount=='?') aRule = new ParseRule(Op.ZeroOrOne, aRule);
    else if(aCount=='*') aRule = new ParseRule(Op.ZeroOrMore, aRule);
    else if(aCount=='+') aRule = new ParseRule(Op.OneOrMore, aRule);
    
    // Get tail Or rule and add
    if(_child0==null) { _op = Op.Or; _child0 = aRule; }
    else if(_child1==null) _child1 = aRule;
    else { _tailOr._child1 = new ParseRule(Op.Or, _tailOr._child1, aRule); _tailOr = _tailAnd = _tailOr._child1; }
    return this;
}

/**
 * Adds an And rule to this rule with given pattern.
 */
public ParseRule and(String aPattern)  { return and(aPattern, '1'); }

/**
 * Adds an And rule to this rule with given count and pattern.
 */
public ParseRule and(String aPattern, char aCount)
{ ParseRule r = new ParseRule(); r.setPattern(aPattern); return and(r, aCount); }

/**
 * Adds an And rule to this rule with given rule.
 */
public ParseRule and(ParseRule aRule)  { return and(aRule, '1'); }

/**
 * Adds an And rule to this rule with given rule.
 */
public ParseRule and(ParseRule aRule, char aCount)
{
    // Wrap rule in count rule
    if(aCount=='?') aRule = new ParseRule(Op.ZeroOrOne, aRule);
    else if(aCount=='*') aRule = new ParseRule(Op.ZeroOrMore, aRule);
    else if(aCount=='+') aRule = new ParseRule(Op.OneOrMore, aRule);
    
    // Get tail Or or And rule and add
    if(_child1==null) { _op = Op.And; _child1 = aRule; }
    else { _tailAnd._child1 = new ParseRule(Op.And, _tailAnd._child1, aRule); _tailAnd = _tailAnd._child1; }
    return this;
}

// Bogus ivars for building rules programatically.
private ParseRule _tailOr = this, _tailAnd = this;

/**
 * Returns a string representation.
 */
public String toString()  { return ParseUtils.getString(this).replaceAll("\\s+", " ").trim(); }

}