/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.parse.ParseRule.Op;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.web.WebFile;
import snap.web.WebURL;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * Reads/Writes ParseRules from/to file.
 */
public class ParseUtils {

    /**
     * Returns all unique rules nested in given rule.
     */
    public static ParseRule[] getAllRulesForRule(ParseRule aRule)
    {
        Set<ParseRule> allRules = new HashSet<>();
        findAllRulesForRule(aRule, allRules);
        return allRules.toArray(new ParseRule[0]);
    }

    /**
     * Finds all unique rules nested in given rule and adds to given set.
     */
    private static void findAllRulesForRule(ParseRule aRule, Set<ParseRule> allRules)
    {
        allRules.add(aRule);

        // Recurse into rule nested left / right rules
        ParseRule rule0 = aRule.getChild0();
        if (rule0 != null && !allRules.contains(rule0))
            findAllRulesForRule(rule0, allRules);
        ParseRule rule1 = aRule.getChild1();
        if (rule1 != null && !allRules.contains(rule1))
            findAllRulesForRule(rule1, allRules);
    }

    /**
     * Returns all rules with a name for given top level rule.
     */
    public static ParseRule[] getNamedRulesForRule(ParseRule aRule)
    {
        ParseRule[] allRules = getAllRulesForRule(aRule);
        return ArrayUtils.filter(allRules, rule -> rule.getName() != null);
    }

    /**
     * Returns all rules with a pattern for given top level rule.
     */
    public static ParseRule[] getPatternRulesForRule(ParseRule aRule)
    {
        ParseRule[] allRules = getAllRulesForRule(aRule);
        return ArrayUtils.filter(allRules, rule -> rule.getPattern() != null);
    }

    /**
     * Prints the names of all rules.
     */
    public static void printAllRuleNames(ParseRule aRule, int namesPerLine)
    {
        // Get all rule names in quotes
        ParseRule[] namedRules = getNamedRulesForRule(aRule);
        String[] quotedRuleNames = ArrayUtils.map(namedRules, rule -> '"' + rule.getName() + '"', String.class);

        // Iterate over names and print with newline for every namesPerLine
        for (int i = 0; i < quotedRuleNames.length; i++) {
            System.out.print(quotedRuleNames[i] + ", ");
            if (i > 0 && i % namesPerLine == 0) System.out.println();
        }
    }

    /**
     * Writes a rule to a file.
     */
    public static void writeAllRulesForRuleToFile(ParseRule aRule, WebFile aFile)
    {
        // Get string for given rule
        ParseRule[] allRules = ParseUtils.getAllRulesForRule(aRule);
        String rulesString = getStringForRules(allRules);

        // Set in file
        aFile.setText(rulesString);
        try { aFile.save(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a string for the currently loaded set of rules.
     */
    public static String getStringForRules(ParseRule[] theRules)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');

        // Write normal rules
        for (ParseRule rule : theRules) {
            if (rule.getName() != null && rule.getPattern() == null) {
                String s = getString(rule), s2 = s.replaceAll("\\s+", " ").trim();
                if (s2.length() <= 120) s = s2 + "\n\n";
                sb.append(s);
            }
        }

        // Write Regex rules
        for (ParseRule rule : theRules) {
            if (rule.getPattern() != null && rule.getName() != null) {
                String s = getString(rule);
                sb.append(s);
            }
        }

        return sb.toString();
    }

    /**
     * Returns string definition of rule.
     */
    public static String getString(ParseRule aRule)
    {
        String str = getStringBody(aRule);
        String name = aRule.getName();
        String pattern = aRule.getPattern();
        if (name != null) {
            if (pattern != null) str = name + " { " + str + " }\n\n";
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
        if (aRule.getPattern() != null) return getPatternQuoted(aRule.getPattern());

        switch (aRule.getOp()) {

            // Handle Or
            case Or: {
                ParseRule c0 = aRule.getChild0();
                String s0 = c0.getName() != null ? c0.getName() : c0.toString();
                ParseRule c1 = aRule.getChild1();
                String s1 = c1.getName() != null ? c1.getName() : c1.toString();
                return s0 + " | " + s1;
            }

            // Handle And
            case And: {
                ParseRule c0 = aRule.getChild0();
                String s0 = c0.getName() != null ? c0.getName() : c0.toString();
                ParseRule c1 = aRule.getChild1();
                String s1 = c1.getName() != null ? c1.getName() : c1.toString();
                if (c0.getOp() == Op.Or && c0.getName() == null) s0 = '(' + s0 + ')';
                if (c1.getOp() == Op.Or && c1.getName() == null) s1 = '(' + s1 + ')';
                return s0 + ' ' + s1;
            }

            // Handle ZeroOrOne
            case ZeroOrOne: {
                ParseRule c = aRule.getChild0();
                String s = c.getName() != null ? c.getName() : c.toString();
                if (c.getName() == null && (c.getOp() == Op.Or || c.getOp() == Op.And)) s = '(' + s + ')';
                return s + '?';
            }

            // Handle ZeroOrMore
            case ZeroOrMore: {
                ParseRule c = aRule.getChild0();
                String s = c.getName() != null ? c.getName() : c.toString();
                if (c.getName() == null && (c.getOp() == Op.Or || c.getOp() == Op.And)) s = '(' + s + ')';
                return s + '*';
            }

            // Handle OneOrMore
            case OneOrMore: {
                ParseRule c = aRule.getChild0();
                String s = c.getName() != null ? c.getName() : c.toString();
                if (c.getName() == null && (c.getOp() == Op.Or || c.getOp() == Op.And)) s = '(' + s + ')';
                return s + '+';
            }

            // Handle Pattern
            case Pattern:
                return getPatternQuoted(aRule.getPattern());

            // Handle LookAhead
            case LookAhead: {
                if (aRule.getLookAheadCount() < 99) return "LookAhead(" + aRule.getLookAheadCount() + ")";
                ParseRule c = aRule.getChild0();
                String s = c.getName() != null ? c.getName() : c.toString();
                return "LookAhead(" + s + ")";
            }

            // Default
            default:
                throw new RuntimeException("ParseUtils.getStringBody: Unsupported Op " + aRule.getOp());
        }
    }

    /**
     * Appends pattern to a given StringBuffer with escapes.
     * */
    private static String getPatternQuoted(String aStr)
    {
        StringBuffer sb = new StringBuffer().append('"'); boolean isEscape = false;
        for(int i = 0; i < aStr.length(); i++) {
            char c = aStr.charAt(i);
            if(isEscape) { sb.append(c); isEscape = false; }
            else switch(c) {
                case '\\': sb.append(c); isEscape = true; break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\r': sb.append("\\r"); break;
                case '\f': sb.append("\\f"); break;
                default: sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Loads a rule for a class.
     */
    public static ParseRule loadRule(Class<?> aClass, String aName)
    {
        // Get resource for rule
        String name = aName != null ? aName : aClass.getSimpleName() + ".txt";
        WebURL url = WebURL.getURL(aClass, name); //java.net.URL url = aClass.getResource(name);
        if (url == null) {
            System.err.println("ParseUtils.loadRule: Couldn't find " + name);
            return null;
        }

        // Get text for parser grammar
        String grammarStr = SnapUtils.getText(url);
        if (grammarStr == null) return null;

        // Load rule from string
        try {
            ParseRuleParser parseRuleParser = new ParseRuleParser();
            ParseNode parseNode = parseRuleParser.parse(grammarStr);
            return parseNode.getCustomNode(ParseRule.class);
        }

        // Handle exceptions
        catch (ParseException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the handler classes for a parent class.
     */
    public static Class<? extends ParseHandler<?>>[] getHandlerClassesInsideClass(Class<?> aClass)
    {
        // Get array of inner classes. TeaVM doesn't support this yet
        Class<?>[] innerClasses = aClass.getDeclaredClasses();

        // Filter to only get real ParseHandler subclasses
        Stream<Class<?>> handlerClassesStream = Arrays.stream(innerClasses).filter(c -> isHandlerClass(c));
        Class<? extends ParseHandler<?>>[] handlerClasses = handlerClassesStream.toArray(size -> new Class[size]);

        // Return
        return handlerClasses;
    }

    /**
     * Searches given class for inner handler classes and installs instance in rule.
     */
    public static void installHandlerForClass(Class<? extends ParseHandler<?>> handlerClass, ParseRule aRule)
    {
        // Get rule name and rule by stripping "Handler" from class name
        String simpleName = handlerClass.getSimpleName();
        String ruleName = simpleName.substring(0, simpleName.length() - "Handler".length());
        ParseRule parseRule = aRule.getRule(ruleName);
        if (parseRule == null) {
            System.out.println("ParseUtils.installHandlerForClass: Couldn't find rule for name: " + ruleName);
            return;
        }

        // Create handler instance
        ParseHandler<?> parseHandler;
        try { parseHandler = handlerClass.newInstance(); }
        catch(Exception e) { throw new RuntimeException(e); }

        // Install handler in rule
        parseRule.setHandler(parseHandler);
    }

    /**
     * Searches given class for inner handler classes and installs instance in rule.
     */
    public static void installHandlersForParentClass(Class<?> aClass, ParseRule aRule)
    {
        // Get handler classes
        Class<? extends ParseHandler<?>>[] handlerClasses = getHandlerClassesInsideClass(aClass);

        // Iterate over handler classes and install in rule for each
        for(Class<? extends ParseHandler<?>> handlerClass : handlerClasses)
            installHandlerForClass(handlerClass, aRule);
    }

    /**
     * Prints handler classes so parsers can include as constant and avoid reflection for handler install.
     */
    public static void printHandlerClassesForParentClass(Class<?> aClass, int classesPerLine)
    {
        // Get handler classes
        Class<?>[] handlerClasses = getHandlerClassesInsideClass(aClass);

        // Iterate over and print
        for (int i = 1; i <= handlerClasses.length; i++) {
            Class<?> handlerClass = handlerClasses[i];
            if (i % classesPerLine == 0)
                System.out.println(handlerClass.getSimpleName() + ".class,");
            else System.out.print(handlerClass.getSimpleName() + ".class, ");
        }
    }

    /**
     * Returns the handler classes for a parent class.
     */
    private static boolean isHandlerClass(Class<?> aClass)
    {
        return ParseHandler.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers());
    }

    /**
     * Test.
     */
    public static void main(String[] args)
    {
        //WebFile file = WebURL.getURL("/tmp/KeyChain.txt").createFile(false);
        //new ParseUtils().write(new snap.util.KeyChainParser().getRule(), file);
        //System.out.println(KeyChain.getValue(new Object(), "1+1*2+3"));
        //System.out.println(KeyChain.getValue(new Object(), "1+2"));
        //new ParseUtils().write(new ParseRuleParser().getRule(), WebURL.getURL("/tmp/ParseRuleParser.txt").createFile(false));
    }

}