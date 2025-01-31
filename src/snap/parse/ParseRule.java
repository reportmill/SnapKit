/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * A class to represent a parse rule.
 */
public class ParseRule {

    // The name
    private String _name;

    // The op
    protected Op _op;

    // The child rules
    protected ParseRule _child0, _child1;

    // Rule pattern - if simple regex
    protected String _pattern;

    // The look ahead count (implies this rule is really just a look ahead
    protected int _lookAheadCount;

    // The handler for parse rule
    private ParseHandler<?> _handler;

    // Constants for booleans operators
    public enum Op {Or, And, ZeroOrOne, ZeroOrMore, OneOrMore, LookAhead, Pattern}

    /**
     * Constructor.
     */
    public ParseRule()
    {
        super();
    }

    /**
     * Constructor for given name
     */
    public ParseRule(String aName)
    {
        setName(aName);
    }

    /**
     * Constructor for op and child rule
     */
    public ParseRule(Op anOp, ParseRule aPR)
    {
        this(null, anOp, aPR, null);
    }

    /**
     * Constructor for given op and child rules.
     */
    public ParseRule(Op anOp, ParseRule aPR1, ParseRule aPR2)
    {
        this(null, anOp, aPR1, aPR2);
    }

    /**
     * Constructor for given name, op and child rules.
     */
    public ParseRule(String aName, Op anOp, ParseRule aPR1, ParseRule aPR2)
    {
        setName(aName);
        _op = anOp;
        _child0 = aPR1;
        _child1 = aPR2;
    }

    /**
     * Returns rule name.
     */
    public String getName()  { return _name; }

    /**
     * Sets rule name.
     */
    protected void setName(String aName)
    {
        _name = aName != null ? aName.intern() : null;
    }

    /**
     * Returns an identifier string for this rule - either the name or the pattern or null.
     */
    public String getId()
    {
        return _name != null ? _name : _pattern != null ? _pattern : null;
    }

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
    public ParseRule getChild1() { return _child1; }

    /**
     * Returns the rule pattern if simple pattern.
     */
    public String getPattern()  { return _pattern; }

    /**
     * Sets the rule pattern if simple pattern.
     */
    protected ParseRule setPattern(String anPattern)
    {
        _pattern = anPattern.intern();
        _op = Op.Pattern;
        return this;
    }

    /**
     * Returns whether rule is anonymous - true if rule has no name and no pattern.
     */
    public boolean isAnonymous()
    {
        return getName() == null && getPattern() == null;
    }

    /**
     * Returns whether rule is optional.
     */
    public boolean isOptional()
    {
        return _op == Op.ZeroOrOne || _op == Op.ZeroOrMore || _op == Op.And && _child0.isOptional() && _child1.isOptional();
    }

    /**
     * Returns the handler for this rule.
     */
    public ParseHandler<?> getHandler()  { return _handler; }

    /**
     * Sets the handler for this rule.
     */
    public ParseRule setHandler(ParseHandler<?> aHandler)
    {
        _handler = aHandler;
        return this;
    }

    /**
     * Returns whether rule is look ahead.
     */
    public boolean isLookAhead()  { return _lookAheadCount > 0; }

    /**
     * Returns the look ahead count.
     */
    public int getLookAheadCount()  { return _lookAheadCount; }

    /**
     * Sets the look ahead count.
     */
    protected void setLookAheadCount(int aValue)
    {
        _lookAheadCount = aValue;
        _op = Op.LookAhead;
    }

    /**
     * Returns a string representation.
     */
    public String toString()
    {
        String str = ParseUtils.getStringForRule(this);
        String strTrim = str.replaceAll("\\s+", " ").trim();
        return strTrim;
    }
}