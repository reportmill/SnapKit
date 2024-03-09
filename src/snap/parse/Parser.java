/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to parse a given input (string) using given rule(s).
 */
public class Parser {

    // The top level rule
    private ParseRule  _rule;

    // The current parse character input
    private CharSequence  _input;

    // The tokenizer
    private Tokenizer  _tokenizer;

    // The current token
    private ParseToken  _token;

    // The list of current look ahead tokens
    private List<ParseToken>  _lookAheadTokens = new ArrayList<>();

    // Whether an exception was hit on last parse
    private boolean  _exceptionHitOnLastParse;

    // The shared node used to report parse success
    private ParseNode _sharedNode = new ParseNode();

    /**
     * Constructor.
     */
    public Parser()
    {
        super();
    }

    /**
     * Constructor for given rule.
     */
    public Parser(ParseRule aRule)
    {
        setRule(aRule);
    }

    /**
     * Returns the top level rule.
     */
    public ParseRule getRule()
    {
        // If already set, just return
        if (_rule != null) return _rule;

        // Create/init/set rule
        _rule = createRule();
        initRule();

        // Return
        return _rule;
    }

    /**
     * Sets the top level rule.
     */
    public void setRule(ParseRule aRule)
    {
        _rule = aRule;
    }

    /**
     * Creates the top level rule. Default version tries to load rules from ClassName.txt.
     */
    protected ParseRule createRule()
    {
        return ParseUtils.loadRule(getClass(), null);
    }

    /**
     * Initializes rule(s).
     */
    protected void initRule()  { }

    /**
     * Returns a named rule.
     */
    public ParseRule getRule(String aName)
    {
        return getRule().getRule(aName);
    }

    /**
     * Returns the current parse character input.
     */
    public CharSequence getInput()
    {
        return _input;
    }

    /**
     * Sets the current parse string.
     */
    public void setInput(CharSequence aSequence)
    {
        _input = aSequence;
        getTokenizer().setInput(_input);
        clearTokens();
    }

    /**
     * Returns the current parse char location.
     */
    public int getCharIndex()
    {
        return getTokenizer().getCharIndex();
    }

    /**
     * Sets the current parse location.
     */
    public void setCharIndex(int aLoc)
    {
        getTokenizer().setCharIndex(aLoc);
        clearTokens();
    }

    /**
     * Returns the tokenizer.
     */
    public Tokenizer getTokenizer()
    {
        // If already set, just return
        if (_tokenizer != null) return _tokenizer;

        // Create tokenizer
        Tokenizer tokenizer = createTokenizer();

        // Add Patterns for Rule
        ParseRule rule = getRule();
        tokenizer.addPatternsForRule(rule);

        // Set, return
        setTokenizer(tokenizer);
        return _tokenizer;
    }

    /**
     * Creates the tokenizer instance.
     */
    protected Tokenizer createTokenizer()  { return new Tokenizer(); }

    /**
     * Sets the tokenizer.
     */
    protected void setTokenizer(Tokenizer aTokenizer)
    {
        _tokenizer = aTokenizer;
    }

    /**
     * Returns the current token.
     */
    public ParseToken getToken()
    {
        if (_token != null) return _token;
        return _token = getNextToken();
    }

    /**
     * Fetches and returns the next token.
     */
    protected ParseToken getNextToken()
    {
        // If LookAheadTokens has available token, remove and return it
        if (_lookAheadTokens.size() > 0)
            return _lookAheadTokens.remove(0);

        // Get next token
        Tokenizer tokenizer = getTokenizer();
        return tokenizer.getNextToken();
    }

    /**
     * Returns the look ahead token at given index.
     */
    protected ParseToken getLookAheadToken(int anIndex)
    {
        if (anIndex == 0)
            return getToken();

        // While not enough lookahead tokens, get and add next token
        while (anIndex > _lookAheadTokens.size()) {
            Tokenizer tokenizer = getTokenizer();
            ParseToken nextToken = tokenizer.getNextToken();
            _lookAheadTokens.add(nextToken);
        }

        // Return lookahead token
        return _lookAheadTokens.get(anIndex - 1);
    }

    /**
     * Clears any currently set tokens.
     */
    protected void clearTokens()
    {
        _lookAheadTokens.clear();
        _token = null;
    }

    /**
     * Parses a given input and returns ParseNode (convenience).
     */
    public ParseNode parse(CharSequence anInput)
    {
        setInput(anInput);
        return parse();
    }

    /**
     * Parses input and returns ParseNode.
     */
    public ParseNode parse()
    {
        ParseRule rule = getRule();
        return parse(rule);
    }

    /**
     * Parses input and returns ParseNode.
     */
    public ParseNode parse(ParseRule aRule)
    {
        // If exception was hit on last parse, reset all handlers
        if (_exceptionHitOnLastParse)
            resetAllHandlers();

        // Do real parse
        _exceptionHitOnLastParse = true;
        ParseNode node = parse(aRule, null);
        _exceptionHitOnLastParse = false;

        // If result node has CustomNode, swap it in
        if (node != null && node.getCustomNode() instanceof ParseNode)
            node = (ParseNode) node.getCustomNode();

        // Return
        return node;
    }

    /**
     * Parses input and returns custom parse tree node.
     */
    public <T> T parseCustom(Class<T> aClass)
    {
        ParseRule rule = getRule();
        return parseCustom(rule, aClass);
    }

    /**
     * Parses input and returns custom parse tree node.
     */
    public <T> T parseCustom(ParseRule aRule, Class<T> aClass)
    {
        ParseNode node = parse(aRule, null);
        T customNode = node != null ? node.getCustomNode(aClass) : null;
        return customNode;
    }

    /**
     * Returns a parse node if this rule matches string.
     */
    protected ParseNode parse(ParseRule aRule, HandlerRef aHRef)
    {
        // Get current token (if no token, just return null)
        ParseToken token = getToken();
        if (token == null)
            return null;

        // Get handler reference for given rule: Reuse if no Rule.Handler, otherwise create new HandlerRef for rule
        HandlerRef href = aRule.getHandler() == null ? aHRef : new HandlerRef(aRule);

        // Handle ops
        switch (aRule.getOp()) {

            // Handle Or: Parse rules and break if either passes (return null if either fail)
            case Or: {

                // Parse rule 1 - just break if successful
                ParseRule rule1 = aRule.getChild0();
                if (parseAndHandle(rule1, href))
                    break;

                // Parse rule 2 - just break if successful
                ParseRule rule2 = aRule.getChild1();
                if (parseAndHandle(rule2, href))
                    break;

                // Return fail since both rules failed
                return null;
            }

            // Handle And
            case And: {

                // Get rules
                ParseRule rule1 = aRule.getChild0();
                ParseRule rule2 = aRule.getChild1();

                // Handle rule 1 LookAhead(count)
                if (rule1.isLookAhead() && rule1.getChild0() == null) {
                    if (lookAhead(rule2, rule1.getLookAheadCount(), 0) < 0)
                        return null;
                    if (parseAndHandle(rule2, href))
                        break;
                    parseFailed(rule2, href.handler());
                    break;
                }

                // Parse first rule - return fail if not successful and not optional
                boolean rule1Success = parseAndHandle(rule1, href);
                if (!rule1Success && !rule1.isOptional())
                    return null;

                // Parse second rule - just break if successful
                boolean rule2Success = parseAndHandle(rule2, href);
                if (rule2Success)
                    break;

                // If rule 2 optional, just break
                if (rule1Success && rule2.isOptional())
                    break;

                // If rule 1 was failed but optional, return fail
                if (!rule1Success)
                    return null;

                // Call parse failed (usually throws exception)
                parseFailed(rule2, href.handler());
                break;
            }

            // Handle ZeroOrOne
            case ZeroOrOne: {
                ParseRule r0 = aRule.getChild0();
                if (!parseAndHandle(r0, href))
                    return null;
                break;
            }

            // Handle ZeroOrMore, OneOrMore: These parse identically, but differ in how parent rule handles them (ZeroOrMore is optional)
            case ZeroOrMore:
            case OneOrMore: {

                // Parse rule - just return if failed
                ParseRule rule = aRule.getChild0();
                if (!parseAndHandle(rule, href))
                    return null;

                // Keep parsing while more are available
                while (true) {
                    if (!parseAndHandle(rule, href))
                        break;
                }
                break;
            }

            // Handle Pattern
            case Pattern: {

                // If no match, just return
                if (aRule.getPattern() != token.getPattern())
                    return null;

                // Otherwise, create node, send handler parsedOne and parsedAll and return
                ParseNode node = createNode(aRule, token, token);
                if (href != aHRef) {
                    ParseHandler<?> handler = href.handler();
                    if (!sendHandlerParsedOne(node, handler))
                        return null;
                    node._customNode = handler.parsedAll();
                }

                // Clear token and return
                _token = null; //getNextToken();
                return node;
            }

            // Handle LookAhead
            case LookAhead: {
                ParseRule rule = aRule.getChild0();
                int tokenCount = aRule.getLookAheadCount();
                int lookAheadCount = lookAhead(rule, tokenCount, 0);
                if (lookAheadCount < 0)
                    return null;
                break;
            }
        }

        // Create new node and return
        ParseNode node = createNode(aRule, token, _sharedNode.getEndToken());
        if (href != aHRef)
            node._customNode = href.handler().parsedAll();
        return node;
    }

    /**
     * Parse rule and send handler parsedOne if successful.
     */
    private boolean parseAndHandle(ParseRule aRule, HandlerRef anHRef)
    {
        // Do normal parse (just return if not successful)
        ParseNode node = parse(aRule, anHRef);
        if (node == null)
            return false;

        // If no handler, just return
        if (anHRef == null || aRule.isAnonymous())
            return true;

        // Send handler parsedOne
        ParseHandler<?> handler = anHRef.handler();
        return sendHandlerParsedOne(node, handler);
    }

    /**
     * Send handler parsedOne (with check for handler fail).
     */
    private boolean sendHandlerParsedOne(ParseNode aNode, ParseHandler<?> aHandler)
    {
        // Get handler and notify parseOne
        aHandler.parsedOne(aNode);

        // If handler indicated fail, call fail and return false
        if (aHandler.isFail()) {
            parseFailed(aNode.getRule(), aHandler);
            return false;
        }

        // Return true
        return true;
    }

    /**
     * Looks ahead given number of tokens and returns the remainder or -1 if it fails.
     */
    protected int lookAhead(ParseRule aRule, int aTokenCount, int aTokenIndex)
    {
        // Handle ops
        switch (aRule.getOp()) {

            // Handle Or
            case Or: {

                // Get rules
                ParseRule rule1 = aRule.getChild0();
                ParseRule rule2 = aRule.getChild1();

                // Do rule 1 look-ahead
                int remainder1 = lookAhead(rule1, aTokenCount, aTokenIndex);
                if (remainder1 >= 0)
                    return remainder1;

                // Return rule 2 look-ahead
                return lookAhead(rule2, aTokenCount, aTokenIndex);
            }

            // Handle And
            case And: {

                // Get rules
                ParseRule rule1 = aRule.getChild0();
                ParseRule rule2 = aRule.getChild1();

                // Handle rule 0 LookAhead(count)
                if (rule1.isLookAhead() && rule1.getChild0() == null) {
                    if (lookAhead(rule2, rule1.getLookAheadCount(), aTokenIndex) < 0)
                        return -1;
                    return lookAhead(rule2, aTokenCount, aTokenIndex);
                }

                // Do rule 1 look-ahead
                int remainder1 = lookAhead(rule1, aTokenCount, aTokenIndex);
                if (remainder1 < 0 && !rule1.isOptional() || remainder1 == 0)
                    return remainder1;

                // If tokens not matched, reset remainder
                boolean rule1Success = remainder1 > 0;
                if (!rule1Success)
                    remainder1 = aTokenCount;

                // Do rule 2 look-ahead
                int remainder2 = lookAhead(rule2, remainder1, aTokenIndex + aTokenCount - remainder1);
                if (remainder2 >= 0)
                    return remainder2;

                // If rule2 optional, return remainder 1
                if (rule1Success && rule2.isOptional())
                    return remainder1;

                // Return fail
                return -1;
            }

            // Handle ZeroOrOne
            case ZeroOrOne:
                return lookAhead(aRule.getChild0(), aTokenCount, aTokenIndex);

            // Handle ZeroOrMore, OneOrMore: These parse identically, but differ in how parent rule handles them (ZeroOrMore is optional)
            case ZeroOrMore:
            case OneOrMore: {

                // Do rule look-ahead
                ParseRule rule = aRule.getChild0();
                int remainder = lookAhead(rule, aTokenCount, aTokenIndex);

                // Keep doing look-ahead
                int remainder2 = remainder;
                while (remainder2 > 0) {
                    remainder2 = lookAhead(rule, remainder2, aTokenIndex + aTokenCount - remainder2);
                    if (remainder2 >= 0)
                        remainder = remainder2;
                }
                return remainder;
            }

            // Handle Pattern
            case Pattern: {
                ParseToken token = getLookAheadToken(aTokenIndex);
                if (token != null && aRule.getPattern() == token.getPattern())
                    return aTokenCount - 1;
                return -1;
            }

            // Handle LookAhead
            case LookAhead: {
                ParseRule r0 = aRule.getChild0();
                int tokenCount = aRule.getLookAheadCount();
                if (lookAhead(r0, tokenCount, aTokenIndex) < 0)
                    return -1;
                return aTokenCount;
            }

            // Complain
            default: throw new RuntimeException("Parser.lookAhead: Unsupported op " + aRule.getOp());
        }
    }

    /**
     * Creates a node for given rule and start/end tokens (returns a shared node by default).
     */
    protected ParseNode createNode(ParseRule aRule, ParseToken aStartToken, ParseToken anEndToken)
    {
        _sharedNode.init(this, aRule, aStartToken, anEndToken);
        return _sharedNode;
    }

    /**
     * Called when parse fails.
     */
    protected void parseFailed(ParseRule aRule, ParseHandler<?> aHandler)
    {
        if (aHandler != null)
            aHandler.reset();
        throw new ParseException(this, aRule);
    }

    /**
     * Parses given input and returns custom parse tree node (convenience).
     */
    public <T> T parseCustom(CharSequence anInput, Class<T> aClass)
    {
        setInput(anInput);
        return parseCustom(aClass);
    }

    /**
     * Traverses rule tree to reset all handlers.
     */
    private void resetAllHandlers()
    {
        ParseRule rule = getRule();
        resetHandlersForRuleDeep(rule, new HashSet<>());
    }

    /**
     * Reset Handlers for given rule, then continues on.
     */
    private void resetHandlersForRuleDeep(ParseRule aRule, Set<ParseRule> visitedRules)
    {
        // Reset handler
        ParseHandler<?> handler = aRule.getHandler();
        while (handler != null) {
            handler.reset();
            handler = handler._backupHandler;
        }

        // Add rule to visited set
        visitedRules.add(aRule);

        // Recurse for children
        ParseRule childRule0 = aRule.getChild0();
        if (childRule0 != null && !visitedRules.contains(childRule0))
            resetHandlersForRuleDeep(childRule0, visitedRules);
        ParseRule childRule1 = aRule.getChild1();
        if (childRule1 != null && !visitedRules.contains(childRule1))
            resetHandlersForRuleDeep(childRule1, visitedRules);
    }

    /**
     * A class to pass Handler by reference, allowing it to be created lazily, but used higher up in stack.
     */
    public static final class HandlerRef {

        // Ivars
        private final ParseRule _rule;
        private ParseHandler<?> _handler;

        /** Constructor. */
        HandlerRef(ParseRule aRule)
        {
            _rule = aRule;
        }

        /** Returns next available handler. */
        private ParseHandler<?> handler()
        {
            if (_handler != null) return _handler;
            ParseHandler<?> handler = _rule.getHandler();
            ParseHandler<?> nextHandler = handler.getAvailableHandler();
            return _handler = nextHandler;
        }
    }
}