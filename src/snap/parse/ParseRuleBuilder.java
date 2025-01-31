package snap.parse;

/**
 * This class builds parse rules.
 */
public class ParseRuleBuilder {

    // The rule
    private ParseRule _rule;

    // The current tail rule
    private ParseRule _tailOr, _tailAnd;

    /**
     * Constructor.
     */
    public ParseRuleBuilder()
    {
        reset();
    }

    /**
     * Sets the name.
     */
    public ParseRuleBuilder name(String aName)  { _rule.setName(aName); return this; }

    /**
     * Sets the operator.
     */
    public ParseRuleBuilder op(ParseRule.Op anOp)  { _rule._op = anOp; return this; }

    /**
     * Sets the rule.
     */
    public ParseRuleBuilder rule(ParseRule aRule)
    {
        _rule._child0 = aRule;
        return this;
    }

    /**
     * Sets the pattern.
     */
    public ParseRuleBuilder pattern(String aPattern)
    {
        _rule.setPattern(aPattern);
        return this;
    }

    /**
     * Sets the look ahead count.
     */
    public void lookahead(int aValue)
    {
        _rule._lookAheadCount = aValue;
        _rule._op = ParseRule.Op.LookAhead;
    }

    /**
     * Adds an Or rule to this rule with given pattern.
     */
    public ParseRuleBuilder or(String aPattern)
    {
        return or(aPattern, '1');
    }

    /**
     * Adds an Or rule to this rule with given count and pattern.
     */
    public ParseRuleBuilder or(String aPattern, char aCount)
    {
        ParseRule r = new ParseRule();
        r.setPattern(aPattern);
        return or(r, aCount);
    }

    /**
     * Adds an Or rule to this rule with given rule.
     */
    public ParseRuleBuilder or(ParseRule aRule)
    {
        return or(aRule, '1');
    }

    /**
     * Adds an Or rule to this rule with given rule.
     */
    public ParseRuleBuilder or(ParseRule aRule, char aCount)
    {
        // Wrap rule in count rule
        if (aCount == '?') aRule = new ParseRule(ParseRule.Op.ZeroOrOne, aRule);
        else if (aCount == '*') aRule = new ParseRule(ParseRule.Op.ZeroOrMore, aRule);
        else if (aCount == '+') aRule = new ParseRule(ParseRule.Op.OneOrMore, aRule);

        // Get tail Or rule and add
        if (_rule._child0 == null) {
            _rule._op = ParseRule.Op.Or;
            _rule._child0 = aRule;
        }

        else if (_rule._child1 == null)
            _rule._child1 = aRule;

        else {
            _tailOr._child1 = new ParseRule(ParseRule.Op.Or, _tailOr._child1, aRule);
            _tailOr = _tailAnd = _tailOr._child1;
        }

        return this;
    }

    /**
     * Adds an And rule to this rule with given pattern.
     */
    public ParseRuleBuilder and(String aPattern)
    {
        return and(aPattern, '1');
    }

    /**
     * Adds an And rule to this rule with given count and pattern.
     */
    public ParseRuleBuilder and(String aPattern, char aCount)
    {
        ParseRule r = new ParseRule();
        r.setPattern(aPattern);
        return and(r, aCount);
    }

    /**
     * Adds an And rule to this rule with given rule.
     */
    public ParseRuleBuilder and(ParseRule aRule)
    {
        return and(aRule, '1');
    }

    /**
     * Adds an And rule to this rule with given rule.
     */
    public ParseRuleBuilder and(ParseRule aRule, char aCount)
    {
        // Wrap rule in count rule
        if (aCount == '?') aRule = new ParseRule(ParseRule.Op.ZeroOrOne, aRule);
        else if (aCount == '*') aRule = new ParseRule(ParseRule.Op.ZeroOrMore, aRule);
        else if (aCount == '+') aRule = new ParseRule(ParseRule.Op.OneOrMore, aRule);

        // Get tail Or or And rule and add
        if (_rule._child1 == null) {
            _rule._op = ParseRule.Op.And;
            _rule._child1 = aRule;
        }

        else {
            _tailAnd._child1 = new ParseRule(ParseRule.Op.And, _tailAnd._child1, aRule);
            _tailAnd = _tailAnd._child1;
        }

        return this;
    }

    /**
     * Builds the rule.
     */
    public ParseRule build()
    {
        ParseRule buildRule = _rule;
        reset();
        return buildRule;
    }

    /**
     * Resets the rule.
     */
    private void reset()
    {
        _rule = new ParseRule();
        _tailOr = _tailAnd = _rule;
    }

    /**
     * Resets the rule.
     */
    public void reset(ParseRule aRule)
    {
        _rule = aRule;
        _tailOr = _tailAnd = _rule;
    }
}
