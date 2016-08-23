/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * A parse node.
 */
public class ParseNode {

    // The parser
    Parser              _parser;

    // The parse rule
    ParseRule           _rule;
    
    // The first and last tokens for this rule
    Token               _startToken, _endToken;
    
    // The substring of the parse input represented by this node
    String              _string;
    
    // The custom node create by this node's rule for this node, should there be one
    Object              _customNode;

/**
 * Creates a new parse node
 */
public void init(Parser aParser, ParseRule aRule, Token aStartToken, Token anEndToken)
{
    _parser = aParser; _rule = aRule; _startToken = aStartToken; _endToken = anEndToken;
    _string = null; _customNode = null;
}

/**
 * Returns the parser.
 */
public Parser getParser()  { return _parser; }

/**
 * Returns the rule.
 */
public ParseRule getRule()  { return _rule; }

/**
 * Returns the id of the node's rule: either the name or pattern or null.
 */
public String getId()  { return _rule.getId(); }

/**
 * Returns the rule name.
 */
public String getName()  { return _rule.getName(); }

/**
 * Returns the rule pattern.
 */
public String getPattern()  { return _rule.getPattern(); }

/**
 * Returns the start token for this node.
 */
public Token getStartToken()  { return _startToken; }

/**
 * Returns the end token for this node.
 */
public Token getEndToken()  { return _endToken; }

/**
 * Returns the match start.
 */
public int getStart()  { return _startToken.getInputStart(); }

/**
 * Returns the match end.
 */
public int getEnd()  { return _endToken.getInputEnd(); }

/**
 * Returns the match length.
 */
public int getLength()  { return getEnd() - getStart(); }

/**
 * Returns the line index.
 */
public int getLineIndex()  { return _startToken.getLineIndex(); }

/**
 * Returns the match string.
 */
public String getString()  { return _string!=null? _string : (_string=createString()); }

/**
 * Creates the match string.
 */
protected String createString()
{
    if(_startToken==_endToken) return _startToken.getString();
    return getParser().getInput().subSequence(getStart(), getEnd()).toString();
}

/**
 * Returns the custom node created by parser or rule handler, should there be one.
 */
public Object getCustomNode()  { return _customNode; }

/**
 * Sets the custom node created by parser or rule handler, should there be one.
 */
public void setCustomNode(Object anObj)  { _customNode = anObj; }

/**
 * Returns the custom node as the given class type.
 */
public <T> T getCustomNode(Class<T> aClass)  { return (T)_customNode; }

/**
 * Returns a string representation of node.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer(toStringSimple());
    sb.append(" \"" + (getLength()>43? (getString().substring(0,40) + "...") : getString()).replace('\n', ' ') + '"');
    return sb.toString();
}

/**
 * Returns a string representation of node (just .
 */
public String toStringSimple()
{
    ParseRule rule = getRule(); String name = rule.getName(); if(name==null) name = rule.toString();
    StringBuffer sb = new StringBuffer(name).append(' ');
    sb.append(String.format("{ line:%d start:%d, end:%d }", getLineIndex()+1, getStart(), getEnd()));
    return sb.toString();
}

}