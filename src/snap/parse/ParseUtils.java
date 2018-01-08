/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.lang.reflect.Modifier;
import java.util.*;
import snap.parse.ParseRule.Op;
import snap.util.*;
import snap.web.*;

/**
 * Reads/Writes ParseRules from/to file.
 */
public class ParseUtils {

    // Written rules
    List <ParseRule>  _rules = new ArrayList();

/**
 * Writes a rule to a file.
 */
public void write(ParseRule aRule, WebFile aFile)
{
    addRule(aRule);
    aFile.setText(getString());
    try { aFile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Write a ParseRule.
 */
public void addRule(ParseRule aRule)
{
    if(ListUtils.containsId(_rules, aRule)) return;
    _rules.add(aRule);
    ParseRule r0 = aRule.getChild0(); if(r0!=null) addRule(r0);
    ParseRule r1 = aRule.getChild1(); if(r1!=null) addRule(r1);
}

/**
 * Returns a sstring for the currently loaded set of rules.
 */
public String getString()
{
    StringBuffer sb = new StringBuffer();
    sb.append('\n');
    
    // Write normal rules
    for(ParseRule rule : _rules)
        if(rule.getName()!=null && rule.getPattern()==null) {
            String s = getString(rule), s2 = s.replaceAll("\\s+", " ").trim(); if(s2.length()<=120) s = s2 + "\n\n";
            sb.append(s);
        }

    // Write Regex rules
    for(ParseRule rule : _rules)
        if(rule.getPattern()!=null && rule.getName()!=null) {
            String s = getString(rule);
            sb.append(s);
        }
    
    return sb.toString();
}

/**
 * Returns string definition of rule.
 */
public static String getString(ParseRule aRule)
{
    String str = getStringBody(aRule);
    String name = aRule.getName(); String pattern = aRule.getPattern();
    if(name!=null) {
        if(pattern!=null) str = name + " { " + str + " }\n\n";
        else str = name + "\n{\n    " + str + "\n}\n\n";
    }
    return str;
}

/**
 * Returns string definition of rule body.
 */
private static String getStringBody(ParseRule aRule)
{
    // If pattern, just return pattern quoted string
    if(aRule.getPattern()!=null) return getPatQuoted(aRule.getPattern());
    
    switch(aRule.getOp()) {
    
        // Handle Or
        case Or: {
            ParseRule c0 = aRule.getChild0(); String s0 = c0.getName()!=null? c0.getName() : c0.toString();
            ParseRule c1 = aRule.getChild1(); String s1 = c1.getName()!=null? c1.getName() : c1.toString();
            return s0 + " | " + s1;
        }
        
        // Handle And
        case And: {
            ParseRule c0 = aRule.getChild0(); String s0 = c0.getName()!=null? c0.getName() : c0.toString();
            ParseRule c1 = aRule.getChild1(); String s1 = c1.getName()!=null? c1.getName() : c1.toString();
            if(c0.getOp()==Op.Or && c0.getName()==null) s0 = '(' + s0 + ')';
            if(c1.getOp()==Op.Or && c1.getName()==null) s1 = '(' + s1 + ')';
            return s0 + ' ' + s1;
        }
        
        // Handle ZeroOrOne
        case ZeroOrOne: {
            ParseRule c = aRule.getChild0(); String s = c.getName()!=null? c.getName() : c.toString();
            if(c.getName()==null && (c.getOp()==Op.Or || c.getOp()==Op.And)) s = '(' + s + ')';
            return s + '?';
        }

        // Handle ZeroOrMore
        case ZeroOrMore: {
            ParseRule c = aRule.getChild0(); String s = c.getName()!=null? c.getName() : c.toString();
            if(c.getName()==null && (c.getOp()==Op.Or || c.getOp()==Op.And)) s = '(' + s + ')';
            return s + '*';
        }

        // Handle OneOrMore
        case OneOrMore: {
            ParseRule c = aRule.getChild0(); String s = c.getName()!=null? c.getName() : c.toString();
            if(c.getName()==null && (c.getOp()==Op.Or || c.getOp()==Op.And)) s = '(' + s + ')';
            return s + '+';
        }
        
        // Handle Pattern
        case Pattern: return getPatQuoted(aRule.getPattern());
        
        // Handle LookAhead
        case LookAhead: {
            if(aRule.getLookAhead()<99) return "LookAhead(" + aRule.getLookAhead() + ")";
            ParseRule c = aRule.getChild0(); String s = c.getName()!=null? c.getName() : c.toString();
            return "LookAhead(" + s + ")";
        }
        
        // Default
        default: throw new RuntimeException("ParseUtils.getStringBody: Unsupported Op " + aRule.getOp());
    }
}

/** Appends pattern to a given StringBuffer with escapes. */
private static String getPatQuoted(String aStr)
{
    StringBuffer sb = new StringBuffer().append('"'); boolean isEscape = false;
    for(int i=0;i<aStr.length();i++) { char c = aStr.charAt(i);
        if(isEscape) { sb.append(c); isEscape = false; }
        else switch(c) {
            case '\\': sb.append(c); isEscape = true; break;
            case '"': sb.append("\\\""); break; case '\n': sb.append("\\n"); break;
            case '\t': sb.append("\\t"); break; case '\b': sb.append("\\b"); break;
            case '\r': sb.append("\\r"); break; case '\f': sb.append("\\f"); break;
            default: sb.append(c);
        }
    }
    sb.append('"');
    return sb.toString();
}

/**
 * Loads a rule for a class.
 */
public static ParseRule loadRule(Class aClass, String aName)
{
    // Get resource for rule
    String name = aName!=null? aName : aClass.getSimpleName() + ".txt";
    WebURL url = WebURL.getURL(aClass, name); //java.net.URL url = aClass.getResource(name);
    if(url==null) { System.err.println("ParseUtils.loadRule: Couldn't find " + name); return null; }
    
    // Get text for resource
    String rtext = SnapUtils.getText(url); if(rtext==null) return null;

    // Load rule from string
    try { return new ParseRuleParser().parse(rtext).getCustomNode(ParseRule.class); }
    catch(ParseException e) { throw new RuntimeException(e); }
}

/**
 * Searches given class for inner handler classes and installs them.
 */
public static void installHandlers(Class aClass, ParseRule aRule)
{
    // Set handlers
    for(Class c : aClass.getDeclaredClasses()) {
        if(!ParseHandler.class.isAssignableFrom(c)) continue;
        if(Modifier.isAbstract(c.getModifiers())) continue;
        String name = c.getSimpleName().replace("Handler", "");
        ParseRule pr = aRule.getRule(name);
        ParseHandler hdlr = null; try { hdlr = (ParseHandler)c.newInstance(); }
        catch(Exception e) { throw new RuntimeException(e); }
        if(pr!=null) pr.setHandler(hdlr);
        else System.out.println("Couldn't find rule: " + name);
    }
}

/**
 * Test.
 */
public static void main(String args[])
{
    //WebFile file = WebURL.getURL("/tmp/KeyChain.txt").createFile(false);
    //new ParseUtils().write(new snap.util.KeyChainParser().getRule(), file);
    //System.out.println(KeyChain.getValue(new Object(), "1+1*2+3"));
    //System.out.println(KeyChain.getValue(new Object(), "1+2"));
    //new ParseUtils().write(new ParseRuleParser().getRule(), WebURL.getURL("/tmp/ParseRuleParser.txt").createFile(false));
}

}