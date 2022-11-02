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
    public Parser setInput(CharSequence aSequence)
    {
        _input = aSequence;
        getTokenizer().setInput(_input);
        setCharIndex(0);
        return this;
    }

    /**
     * CharSequence method.
     */
    public final int length()
    {
        return getTokenizer().length();
    }

    /**
     * CharSequence method.
     */
    public final char charAt(int anIndex)
    {
        return getTokenizer().charAt(anIndex);
    }

    /**
     * Returns the current parse char.
     */
    public final char getChar()
    {
        return getTokenizer().getChar();
    }

    /**
     * Returns the char at the current index plus offset.
     */
    public final char getChar(int anOffset)
    {
        return getTokenizer().getChar(anOffset);
    }

    /**
     * Returns the char at the current index plus offset.
     */
    public final char eatChar()
    {
        return getTokenizer().eatChar();
    }

    /**
     * Returns the next given number of chars as a string.
     */
    public final String getChars(int aValue)
    {
        return getTokenizer().getChars(aValue);
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
        _lookAheadTokens.clear();
        _token = null;
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
    protected Tokenizer createTokenizer()
    {
        return new Tokenizer();
    }

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
        return _token != null ? _token : (_token = getNextToken());
    }

    /**
     * Fetches and returns the next token.
     */
    protected ParseToken getNextToken()
    {
        if (_lookAheadTokens.size() > 0)
            return _lookAheadTokens.remove(0);
        return getTokenizer().getNextToken();
    }

    /**
     * Returns the look ahead token at given index.
     */
    protected ParseToken getLookAheadToken(int anIndex)
    {
        if (anIndex == 0)
            return getToken();
        while (anIndex > _lookAheadTokens.size())
            _lookAheadTokens.add(getTokenizer().getNextToken());
        return _lookAheadTokens.get(anIndex - 1);
    }

    /**
     * Clears any currently set tokens.
     */
    public void clearTokens()
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
                ParseRule r0 = aRule.getChild0();
                if (parseAndHandle(r0, href))
                    break;
                ParseRule r1 = aRule.getChild1();
                if (parseAndHandle(r1, href))
                    break;
                return null;
            }

            // Handle And
            case And: {
                ParseRule r0 = aRule.getChild0();
                ParseRule r1 = aRule.getChild1();

                // Handle rule 0 LookAhead(x)
                if (r0.isLookAhead() && r0.getChild0() == null) {
                    if (lookAhead(r1, r0.getLookAhead(), 0) < 0)
                        return null;
                    if (parseAndHandle(r1, href))
                        break;
                    parseFailed(r1, href.handler());
                    break;
                }

                // Parse first rule (just return if parse not successful+optional)
                boolean parsed0 = parseAndHandle(r0, href);
                if (!parsed0 && !r0.isOptional())
                    return null;

                // If handler requested bypass, just break
                if (href != null && href.handler().isBypass())
                    break;

                // Parse second rule
                if (parseAndHandle(r1, href))
                    break;
                if (parsed0 && r1.isOptional())
                    break;
                if (!parsed0)
                    return null;
                parseFailed(r1, href.handler());
                break;
            }

            // Handle ZeroOrOne
            case ZeroOrOne: {
                ParseRule r0 = aRule.getChild0();
                if (!parseAndHandle(r0, href))
                    return null;
                break;
            }

            // Handle ZeroOrMore
            case ZeroOrMore: {
                ParseRule r0 = aRule.getChild0();
                if (!parseAndHandle(r0, href))
                    return null;
                while (true) {
                    if (!parseAndHandle(r0, href))
                        break;
                }
                break;
            }

            // Handle OneOrMore
            case OneOrMore: {
                ParseRule r0 = aRule.getChild0();
                if (!parseAndHandle(r0, href))
                    return null;
                while (true) {
                    if (!parseAndHandle(r0, href))
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
                    ParseHandler handler = href.handler();
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
                int tokenCount = aRule.getLookAhead();
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
    boolean parseAndHandle(ParseRule aRule, HandlerRef anHRef)
    {
        // Do normal parse (just return if not successful)
        ParseNode node = parse(aRule, anHRef);
        if (node == null)
            return false;

        // If no handler, just return
        if (anHRef == null || aRule.isAnonymous())
            return true;

        // Send handler parsedOne
        ParseHandler handler = anHRef.handler();
        return sendHandlerParsedOne(node, handler);
    }

    /**
     * Send handler parsedOne (with check for handler fail).
     */
    boolean sendHandlerParsedOne(ParseNode aNode, ParseHandler aHandler)
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
                ParseRule r0 = aRule.getChild0();
                ParseRule r1 = aRule.getChild1();
                int remainder = lookAhead(r0, aTokenCount, aTokenIndex);
                if (remainder >= 0)
                    return remainder;
                return lookAhead(r1, aTokenCount, aTokenIndex);
            }

            // Handle And
            case And: {
                ParseRule r0 = aRule.getChild0();
                ParseRule r1 = aRule.getChild1();

                // Handle rule 0 LookAhead(x)
                if (r0.isLookAhead() && r0.getChild0() == null) {
                    if (lookAhead(r1, r0.getLookAhead(), aTokenIndex) < 0)
                        return -1;
                    return lookAhead(r1, aTokenCount, aTokenIndex);
                }

                // Handle normal And
                int rmdr0 = lookAhead(r0, aTokenCount, aTokenIndex);
                if (rmdr0 < 0 && !r0.isOptional() || rmdr0 == 0)
                    return rmdr0;
                boolean parsed0 = rmdr0 > 0;
                if (!parsed0)
                    rmdr0 = aTokenCount;
                int rmdr1 = lookAhead(r1, rmdr0, aTokenIndex + aTokenCount - rmdr0);
                if (rmdr1 >= 0)
                    return rmdr1;
                if (parsed0 && r1.isOptional())
                    return rmdr0;
                return -1;
            }

            // Handle ZeroOrOne
            case ZeroOrOne:
                return lookAhead(aRule.getChild0(), aTokenCount, aTokenIndex);

            // Handle ZeroOrMore
            case ZeroOrMore: {
                ParseRule r0 = aRule.getChild0();
                int remainder = lookAhead(r0, aTokenCount, aTokenIndex);
                int r = remainder;
                while (r > 0) {
                    r = lookAhead(r0, r, aTokenIndex + aTokenCount - r);
                    if (r >= 0)
                        remainder = r;
                }
                return remainder;
            }

            // Handle OneOrMore: Why is this identical to above???
            case OneOrMore: {
                ParseRule r0 = aRule.getChild0();
                int remainder = lookAhead(r0, aTokenCount, aTokenIndex);
                int r = remainder;
                while (r > 0) {
                    r = lookAhead(r0, r, aTokenIndex + aTokenCount - r);
                    if (r >= 0)
                        remainder = r;
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
                int tokenCount = aRule.getLookAhead();
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
    protected void parseFailed(ParseRule aRule, ParseHandler aHandler)
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
        ParseHandler handler = aRule.getHandler();
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
        protected final ParseRule _rule;
        private ParseHandler _handler;

        /** Constructor. */
        HandlerRef(ParseRule aRule)
        {
            _rule = aRule;
        }

        /** Returns next available handler. */
        private ParseHandler handler()
        {
            if (_handler != null) return _handler;
            ParseHandler<?> handler = _rule.getHandler();
            ParseHandler<?> nextHandler = handler.getAvailableHandler();
            return _handler = nextHandler;
        }
    }
}