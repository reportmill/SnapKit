package snap.parse;
import snap.util.SnapUtils;
import snap.web.WebURL;

/**
 * This class manages a set of parse rules.
 */
public class Grammar {

    // The primary rule
    private ParseRule _primaryRule;

    /**
     * Constructor.
     */
    protected Grammar(ParseRule primaryRule)
    {
        _primaryRule = primaryRule;
    }

    /**
     * Returns the primary rule.
     */
    public ParseRule getPrimaryRule()  { return _primaryRule; }

    /**
     * Returns the rule for name.
     */
    public ParseRule getRuleForName(String ruleName)  { return _primaryRule.getRule(ruleName); }

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
            ParseNode parseNode = grammarParser.parse(grammarStr);
            ParseRule primaryRule = parseNode.getCustomNode(ParseRule.class);
            return new Grammar(primaryRule);
        }

        // Handle exceptions
        catch (ParseException e) { throw new RuntimeException(e); }
    }
}
