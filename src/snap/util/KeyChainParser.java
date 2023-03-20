/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.util.KeyChain.*;
import snap.parse.*;

/**
 * A Parser subclass to parse strings to KeyChains.
 */
public class KeyChainParser extends Parser {

    // The error from last parse
    private String  _error;

    /**
     * Returns a KeyChain for given string.
     */
    public KeyChain keyChain(String aString)
    {
        // Parse string
        try { return parse(aString).getCustomNode(KeyChain.class); }
        catch(Throwable t) { _error = "Error parsing keychain: @" + aString + "@\n" + t.getMessage(); }
        return new KeyChain(KeyChain.Op.Literal);
    }

    /**
     * Returns the error from last key chain parse.
     */
    public String getError() { return _error; }

    /**
     * Returns the error from last KeyChain parse and clears error.
     */
    public String getAndResetError() { String e = _error; _error = null; return e; }

    /**
     * Load rule from rule file and install handlers.
     */
    protected void initRule()
    {
        // Install Handlers (TeaVM doesn't like auto version)
        //ParseUtils.installHandlers(getClass(), rule);  // Install Handlers
        getRule("Statement").setHandler(new StatementHandler());
        getRule("Expression").setHandler(new ExpressionHandler());
        getRule("LogicalOrExpr").setHandler(new LogicalOrExprHandler());
        getRule("LogicalAndExpr").setHandler(new LogicalAndExprHandler());
        getRule("EqualityExpr").setHandler(new EqualityExprHandler());
        getRule("ComparativeExpr").setHandler(new ComparativeExprHandler());
        getRule("AdditiveExpr").setHandler(new AdditiveExprHandler());
        getRule("MultiplicativeExpr").setHandler(new MultiplicativeExprHandler());
        getRule("UnaryExpr").setHandler(new UnaryExprHandler());
        getRule("KeyChain").setHandler(new KeyChainHandler());
        getRule("Object").setHandler(new ObjectHandler());
        getRule("ArgList").setHandler(new ArgListHandler());
    }

    /**
     * Statement Handler: Statement { LookAhead(2) KEY ("=" | "+=") Expression | Expression }
     */
    public static class StatementHandler extends ParseHandler <KeyChain> {

        // The Op
        int    _op;

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle KEY
            switch (anId) {

                // Handle KEY
                case "KEY": _part = new KeyChain(Op.Key, aNode.getString()); break;

                // Handle Expression
                case "Expression":
                    KeyChain expr = aNode.getCustomNode(KeyChain.class);
                    if (_part == null)
                        _part = expr;
                    else if (_op == 1)
                        _part = new KeyChain(Op.Assignment, _part, expr);
                    else _part = new KeyChain(Op.Assignment, _part, new KeyChain(Op.Add, _part, expr));
                    _op = 0;
                    break;

                // Handle Ops
                case "=": _op = 1; break;
                case "+=": _op = 2; break;
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * Expression Handler: Expression { LogicalOrExpr (LookAhead(2) "?" Expression (LookAhead(2) ":" Expression)?)? }
     */
    public static class ExpressionHandler extends ParseHandler <KeyChain> {

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle KeyChain
            if (aNode.getCustomNode() instanceof KeyChain) {
                KeyChain keyChain = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = keyChain;
                else if (_part.getOp() != Op.Conditional)
                    _part = new KeyChain(Op.Conditional, _part, keyChain);
                else _part.addChild(keyChain);
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * LogicalOrExpr Handler: LogicalOrExpr { LogicalAndExpr ("||" LogicalAndExpr)* }
     */
    public static class LogicalOrExprHandler extends BinaryExprHandler { }

    /**
     * LogicalAndExpr Handler: LogicalAndExpr { EqualityExpr ("&&" EqualityExpr)* }
     */
    public static class LogicalAndExprHandler extends BinaryExprHandler { }

    /**
     * EqualityExpr Handler: EqualityExpr { ComparativeExpr (("==" | "!=") ComparativeExpr)* }
     */
    public static class EqualityExprHandler extends BinaryExprHandler { }

    /**
     * ComparativeExpr Handler: ComparativeExpr { AdditiveExpr ((">" | "<" | ">=" | "<=") AdditiveExpr)* }
     */
    public static class ComparativeExprHandler extends BinaryExprHandler { }

    /**
     * AdditiveExpr Handler: AdditiveExpr { MultiplicativeExpr (("+" | "-") MultiplicativeExpr)* }
     */
    public static class AdditiveExprHandler extends BinaryExprHandler { }

    /**
     * MultiplicativeExpr Handler: MultiplicativeExpr { UnaryExpr (("*" | "/" | "%") UnaryExpr)* }
     */
    public static class MultiplicativeExprHandler extends BinaryExprHandler { }

    /**
     * BinaryExpr Handler.
     */
    public static abstract class BinaryExprHandler extends ParseHandler <KeyChain> {

        // The Op
        Op _op;

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle KeyChain
            if (aNode.getCustomNode() instanceof KeyChain) {
                KeyChain keyChain = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = keyChain;
                else _part = new KeyChain(_op, _part, keyChain);
                return;
            }

            // Handle Ops
            switch (anId) {
                case "+": _op = Op.Add; break;
                case "-": _op = Op.Subtract; break;
                case "*": _op = Op.Multiply; break;
                case "/": _op = Op.Divide; break;
                case "%": _op = Op.Mod; break;
                case "==": _op = Op.Equal; break;
                case "!=": _op = Op.NotEqual; break;
                case ">": _op = Op.GreaterThan; break;
                case "<": _op = Op.LessThan; break;
                case ">=": _op = Op.GreaterThanOrEqual; break;
                case "<=": _op = Op.LessThanOrEqual; break;
                case "||": _op = Op.Or; break;
                case "&&": _op = Op.And; break;
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * UnaryExpr Handler: UnaryExpr { "-" KeyChain | "!" KeyChain | KeyChain }
     */
    public static class UnaryExprHandler extends ParseHandler <KeyChain> {

        // The Op
        Op _op;

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle KeyChain
            switch (anId) {
                case "KeyChain":
                    KeyChain kc = aNode.getCustomNode(KeyChain.class);
                    _part = _op == null ? kc : new KeyChain(_op, kc);
                    _op = null;
                    break;

                // Handle Ops
                case "-": _op = Op.Negate; break;
                case "!": _op = Op.Not; break;
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * KeyChain Handler: KeyChain { Object ("." Object)* }
     */
    public static class KeyChainHandler extends ParseHandler <KeyChain> {

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Object (TeaVM having issue with ==)
            if (anId.equals("Object")) {
                KeyChain keyChain = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = keyChain;
                else if (_part.getOp() != Op.Chain)
                    _part = new KeyChain(Op.Chain, _part, keyChain);
                else _part.addChild(keyChain);
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * Object Handler: LookAhead(3) KEY "(" ArgList? ")" | LookAhead(2) KEY "[" Expression "]" | LookAhead(3) KEY | INT |
     *                     FLOAT | STRING | "(" Expression ")"
     */
    public static class ObjectHandler extends ParseHandler <KeyChain> {

        // Whether starting args
        boolean   _startArgs;

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Key
            switch (anId) {

                // Handle KEY
                case "KEY": _part = new KeyChain(Op.Key, aNode.getString()); break;

                // Handle ArgList
                case "ArgList":
                    KeyChain args = aNode.getCustomNode(KeyChain.class);
                    _part = new KeyChain(Op.FunctionCall, _part.getChildString(0), args);
                    break;

                // Handle empty ArgList
                case "(": _startArgs = _part != null; break;
                case ")":
                    if (_startArgs && _part.getOp() == Op.Key) {
                        _part = new KeyChain(Op.FunctionCall, _part.getChildString(0), new KeyChain(Op.ArgList));
                        _startArgs = false;
                    }
                    break;

                // Handle INT or Float
                case "INT":
                case "FLOAT":
                    java.math.BigDecimal d = new java.math.BigDecimal(aNode.getString());
                    _part = new KeyChain(Op.Literal, d);
                    break;

                // Handle STRING
                case "STRING":
                    String str = aNode.getString();
                    str = str.substring(1, str.length() - 1); // Strip quotes
                    _part = new KeyChain(Op.Literal, str);
                    break;

                // Handle Expression
                case "Expression":
                    KeyChain expr = aNode.getCustomNode(KeyChain.class);
                    _part = _part != null ? new KeyChain(Op.ArrayIndex, _part, expr) : expr;
                    break;
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }

    /**
     * ArgList Handler: ArgList { Expression ("," Expression)* }
     */
    public static class ArgListHandler extends ParseHandler <KeyChain> {

        /** ParseHandler method. */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Expression
            if (anId == "Expression") {
                KeyChain arg = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = new KeyChain(Op.ArgList, arg);
                else _part.addChild(arg);
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }
}