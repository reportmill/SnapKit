package snap.parse;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.web.WebURL;
import java.util.*;

/**
 * This class manages a set of parse rules.
 */
public class Grammar {

    // The list of top level named rules for the grammar
    private List<ParseRule> _namedRules;

    // The primary rule
    private ParseRule _primaryRule;

    // The named rules
    private Map<String, ParseRule> _namedRulesMap;

    /**
     * Constructor.
     */
    protected Grammar()
    {
        _namedRules = new ArrayList<>();
        _namedRulesMap = new HashMap<>();
    }

    /**
     * Constructor for given primary rule.
     */
    protected Grammar(ParseRule primaryRule)
    {
        _primaryRule = primaryRule;
        _namedRulesMap = new HashMap<>();
        findNamedRulesForRule(primaryRule, _namedRulesMap);
        _namedRules = new ArrayList<>(_namedRulesMap.values());
    }

    /**
     * The set of top level named rules in this grammar.
     */
    public List<ParseRule> getNamedRules()  { return _namedRules; }

    /**
     * Adds a rule for given name.
     */
    public ParseRule addRuleForName(String ruleName)
    {
        // If rule name is present, just return
        ParseRule namedRule = _namedRulesMap.get(ruleName);
        if (namedRule != null)
            return namedRule;

        // Create new rule and add to list/map (set primary to first rule)
        namedRule = new ParseRule(ruleName);
        _namedRules.add(namedRule);
        _namedRulesMap.put(ruleName, namedRule);
        if (_primaryRule == null)
            _primaryRule = namedRule;

        // Return
        return namedRule;
    }

    /**
     * Returns the primary rule.
     */
    public ParseRule getPrimaryRule()  { return _primaryRule; }

    /**
     * Returns the rule for name.
     */
    public boolean isRuleSetForName(String ruleName)  { return _namedRulesMap.containsKey(ruleName); }

    /**
     * Returns the rule for name.
     */
    public ParseRule getRuleForName(String ruleName)
    {
        ParseRule rule = _namedRulesMap.get(ruleName);
        if (rule == null)
            throw new RuntimeException("Grammar.getRuleForName: Rule not found for name " + ruleName);
        return rule;
    }

    /**
     * Returns all unique rules.
     */
    public ParseRule[] getAllRules()
    {
        Set<ParseRule> allRules = new LinkedHashSet<>();
        for (ParseRule namedRule : _namedRules)
            findAllRulesForRule(namedRule, allRules);
        return allRules.toArray(new ParseRule[0]);
    }

    /**
     * Returns all rules with a pattern.
     */
    public ParseRule[] getAllPatternRules()
    {
        ParseRule[] allRules = getAllRules();
        return ArrayUtils.filter(allRules, rule -> rule.getPattern() != null);
    }

    /**
     * Returns the Regexes for all token patterns.
     */
    public Regex[] getAllRegexes()
    {
        ParseRule[] patternRules = getAllPatternRules();
        return ArrayUtils.map(patternRules, rule -> new Regex(rule.getName(), rule.getPattern()), Regex.class);
    }

    /**
     * Finds the rule with same name as handler class and sets handler instance.
     */
    public void installHandlerForClass(Class<? extends ParseHandler<?>> handlerClass)
    {
        // Get rule name and rule by stripping "Handler" from class name
        String simpleName = handlerClass.getSimpleName();
        String ruleName = simpleName.substring(0, simpleName.length() - "Handler".length());
        ParseRule parseRule = getRuleForName(ruleName);
        if (parseRule == null) {
            System.out.println("Grammar.installHandlerForClass: Couldn't find rule for name: " + ruleName);
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
     * Finds the rule with same name as handler class and sets handler instance.
     */
    public void installHandlerForClasses(Class<? extends ParseHandler<?>>[] handlerClasses)
    {
        for (Class<? extends ParseHandler<?>> handlerClass : handlerClasses)
            installHandlerForClass(handlerClass);
    }

    /**
     * Creates a grammar for given parser class.
     */
    public static Grammar createGrammarForParserClass(Class<?> parserClass)
    {
        // Get resource for rule
        String name = parserClass.getSimpleName() + ".txt";
        WebURL url = WebURL.getURL(parserClass, name); //java.net.URL url = aClass.getResource(name);
        if (url == null)
            throw new RuntimeException("Couldn't find grammar file for class " + parserClass.getName());

        // Get text for parser grammar
        String grammarStr = SnapUtils.getText(url);
        if (grammarStr == null)
            throw new RuntimeException("Couldn't get grammer content for URL: " + url.getString());

        // Load rule from string
        try {
            GrammarParser grammarParser = new GrammarParser();
            return grammarParser.parseGrammarString(grammarStr);
        }

        // Handle exceptions
        catch (ParseException e) { throw new RuntimeException(e); }
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
     * Finds the named rules for given rule and adds to given map (recursively).
     */
    private static void findNamedRulesForRule(ParseRule aRule, Map<String, ParseRule> aMap)
    {
        String ruleName = aRule.getName();
        if (ruleName != null) {
            if (aMap.get(ruleName) != null)
                return;
            aMap.put(ruleName, aRule);
        }

        // Recurse for children
        if (aRule.getChild0() != null)
            findNamedRulesForRule(aRule.getChild0(), aMap);
        if (aRule.getChild1() != null)
            findNamedRulesForRule(aRule.getChild1(), aMap);
    }
}
