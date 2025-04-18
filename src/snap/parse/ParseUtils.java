/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.parse.ParseRule.Op;
import snap.util.ArrayUtils;
import snap.util.ListUtils;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Reads/Writes ParseRules from/to file.
 */
public class ParseUtils {

    /**
     * Prints the names of all rules.
     */
    public static void printRuleNames(Grammar aGrammar, int namesPerLine)
    {
        // Get all rule names in quotes
        List<ParseRule> namedRules = aGrammar.getNamedRules();
        List<String> quotedRuleNames = ListUtils.map(namedRules, rule -> '"' + rule.getName() + '"');

        // Iterate over names and print with newline for every namesPerLine
        for (int i = 0; i < quotedRuleNames.size(); i++) {
            System.out.print(quotedRuleNames.get(i) + ", ");
            if (i > 0 && i % namesPerLine == 0) System.out.println();
        }
    }

    /**
     * Returns a string for the currently loaded set of rules.
     */
    public static String getStringForRules(Grammar aGrammar)
    {
        List<ParseRule> allRules = aGrammar.getNamedRules();
        StringBuilder sb = new StringBuilder();
        sb.append('\n');

        // Write normal rules
        for (ParseRule rule : allRules) {
            String s = getStringForRule(rule);
            String s2 = s.replaceAll("\\s+", " ").trim();
            if (s2.length() <= 120) s = s2 + "\n\n";
            sb.append(s);
        }

        return sb.toString();
    }

    /**
     * Returns string definition of rule.
     */
    public static String getStringForRule(ParseRule aRule)
    {
        String str = getStringBodyForRule(aRule);
        String ruleName = aRule.getName();
        String pattern = aRule.getPattern();
        if (ruleName != null) {
            if (pattern != null)
                str = ruleName + " { " + str + " }\n\n";
            else str = ruleName + "\n{\n    " + str + "\n}\n\n";
        }
        return str;
    }

    /**
     * Returns string definition of rule body.
     */
    private static String getStringBodyForRule(ParseRule aRule)
    {
        ParseRule.Op ruleOp = aRule.getOp();

        switch (ruleOp) {

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
            case ZeroOrOne:
            case ZeroOrMore:
            case OneOrMore: {
                ParseRule childRule = aRule.getChild0();
                String ruleString = childRule.getName() != null ? childRule.getName() : childRule.toString();
                if (childRule.getName() == null && (childRule.getOp() == Op.Or || childRule.getOp() == Op.And))
                    ruleString = '(' + ruleString + ')';
                char opChar = ruleOp == Op.ZeroOrOne ? '?' : ruleOp == Op.ZeroOrMore ? '*' : '+';
                return ruleString + opChar;
            }

            // Handle Pattern
            case Pattern: return getPatternQuoted(aRule.getPattern());

            // Handle LookAhead
            case LookAhead: {
                if (aRule.getLookAheadCount() < 99) return "LookAhead(" + aRule.getLookAheadCount() + ")";
                ParseRule c = aRule.getChild0();
                String s = c.getName() != null ? c.getName() : c.toString();
                return "LookAhead(" + s + ")";
            }

            // Default
            default: throw new RuntimeException("ParseUtils.getStringBody: Unsupported Op " + aRule.getOp());
        }
    }

    /**
     * Appends pattern to a given StringBuffer with escapes.
     * */
    private static String getPatternQuoted(String aStr)
    {
        StringBuilder sb = new StringBuilder().append('"'); boolean isEscape = false;
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
    private static Class<? extends ParseHandler<?>>[] getHandlerClassesInsideClass(Class<?> aClass)
    {
        Class<?>[] innerClasses = aClass.getDeclaredClasses();
        return (Class<? extends ParseHandler<?>>[]) ArrayUtils.filter(innerClasses, c -> isHandlerClass(c));
    }

    /**
     * Returns the handler classes for a parent class.
     */
    private static boolean isHandlerClass(Class<?> aClass)
    {
        return ParseHandler.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers());
    }
}