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
            if (anId == "KEY")
                _part = new KeyChain(Op.Key, aNode.getString());

            // Handle Expression
            else if (anId == "Expression") {
                KeyChain expr = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = expr;
                else if (_op == 1)
                    _part = new KeyChain(Op.Assignment, _part, expr);
                else _part = new KeyChain(Op.Assignment, _part, new KeyChain(KeyChain.Op.Add, _part, expr));
                _op = 0;
            }

            // Handle Ops
            else if (anId == "=")
                _op = 1;
            else if (anId == "+=")
                _op = 2;
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
                KeyChain kc = aNode.getCustomNode(KeyChain.class);
                if (_part==null)
                    _part = kc;
                else if (_part.getOp() != Op.Conditional)
                    _part = new KeyChain(Op.Conditional, _part, kc);
                else _part.addChild(kc);
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
                KeyChain kc = aNode.getCustomNode(KeyChain.class);
                if (_part==null)
                    _part = kc;
                else _part = new KeyChain(_op, _part, kc);
            }

            // Handle Ops
            else if (anId=="+") _op = Op.Add;
            else if (anId=="-") _op = Op.Subtract;
            else if (anId=="*") _op = Op.Multiply;
            else if (anId=="/") _op = Op.Divide;
            else if (anId=="%") _op = Op.Mod;
            else if (anId=="==") _op = Op.Equal;
            else if (anId=="!=") _op = Op.NotEqual;
            else if (anId==">") _op = Op.GreaterThan;
            else if (anId=="<") _op = Op.LessThan;
            else if (anId==">=") _op = Op.GreaterThanOrEqual;
            else if (anId=="<=") _op = Op.LessThanOrEqual;
            else if (anId=="||") _op = Op.Or;
            else if (anId=="&&") _op = Op.And;
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
            if (anId=="KeyChain") {
                KeyChain kc = aNode.getCustomNode(KeyChain.class);
                _part = _op==null ? kc : new KeyChain(_op, kc);
                _op = null;
            }

            // Handle Ops
            else if (anId=="-")
                _op = Op.Negate;
            else if (anId=="!")
                _op = Op.Not;
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
                KeyChain kc = aNode.getCustomNode(KeyChain.class);
                if (_part == null)
                    _part = kc;
                else if (_part.getOp() != Op.Chain)
                    _part = new KeyChain(Op.Chain, _part, kc);
                else _part.addChild(kc);
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
            if (anId=="KEY")
                _part = new KeyChain(Op.Key, aNode.getString());

            // Handle ArgList
            else if (anId=="ArgList") {
                KeyChain args = aNode.getCustomNode(KeyChain.class);
                _part = new KeyChain(Op.FunctionCall, _part.getChildString(0), args);
            }

            // Handle empty ArgList
            else if (anId == "(") {
                _startArgs = _part!=null;
            }
            else if (anId == ")") {
                if (_startArgs && _part.getOp()==Op.Key) {
                    _part = new KeyChain(Op.FunctionCall, _part.getChildString(0), new KeyChain(Op.ArgList));
                    _startArgs = false;
                }
            }

            // Handle INT or Float
            else if (anId=="INT" || anId=="FLOAT") {
                java.math.BigDecimal d = new java.math.BigDecimal(aNode.getString());
                _part = new KeyChain(Op.Literal, d);
            }

            // Handle STRING
            else if (anId=="STRING") {
                String str = aNode.getString(); str = str.substring(1, str.length()-1); // Strip quotes
                _part = new KeyChain(Op.Literal, str);
            }

            // Handle Expression
            else if (anId=="Expression") {
                KeyChain expr = aNode.getCustomNode(KeyChain.class);
                _part = _part!=null ? new KeyChain(Op.ArrayIndex, _part, expr) : expr;
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
            if (anId=="Expression") {
                KeyChain arg = aNode.getCustomNode(KeyChain.class);
                if (_part==null)
                    _part = new KeyChain(Op.ArgList, arg);
                else _part.addChild(arg);
            }
        }

        /** Returns the part class. */
        protected Class <KeyChain> getPartClass()  { return KeyChain.class; }
    }
}