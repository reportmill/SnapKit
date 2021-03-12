/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.util.*;

/**
 * This class evaluates a string expression on a given object: KeyChain.getValue(object, expression).
 */
public class KeyChain {
    
    // The operator of key chain
    private Op  _op;
    
    // Possible children of key chain
    private Object  _children;
    
    // The KeyChain parser
    private static KeyChainParser  _parser = new KeyChainParser();

    // A shared map of previously encountered key chains
    private static Map <Object,KeyChain>  _keyChains = new Hashtable();
    
    // A thread local to vend per assignment maps
    private static ThreadLocal <Map> _assTL = new ThreadLocal() { public Object initialValue() { return new HashMap(); } };

    // KeyChain Operators
    public enum Op {
        Literal, Add, Subtract, Multiply, Divide, Mod, Negate,
        Equal, NotEqual, GreaterThan, LessThan, GreaterThanOrEqual, LessThanOrEqual, And, Or, Not,
        Key, ArrayIndex, FunctionCall, ArgList, Chain, Conditional, Assignment
    };
    
    /**
     * This is interface is implemented by objects that can get key chain values themselves.
     */
    public interface Get {
        public Object getKeyChainValue(Object aRoot, KeyChain aKeyChain);
    }

    /**
     * Returns a keyChain for aSource (should be a String or existing KeyChain).
     */
    public static KeyChain getKeyChain(Object aSource)
    {
        // If passed key chain, just return it
        if (aSource instanceof KeyChain)
            return (KeyChain)aSource;

        // If not passed string, return Null KeyChain
        if (!(aSource instanceof String) || ((String)aSource).length()==0)
            return new KeyChain(Op.Literal);

        // Get KeyChain (create and cache if needed) and return
        KeyChain kchain = _keyChains.get(aSource);
        if (kchain==null)
            _keyChains.put(aSource, kchain = createKeyChain((String)aSource));
        return kchain;
    }

    /**
     * Returns a keyChain for aSource (should be a String or existing KeyChain).
     */
    private static synchronized KeyChain createKeyChain(String aString)
    {
        return _parser.keyChain(aString);
    }

    /**
     * Returns a thread-local assignments map.
     */
    public static Map getAssignments()  { return _assTL.get(); }

    /**
     * Constructor.
     */
    protected KeyChain(Op anOp)
    {
        _op = anOp;
    }

    /**
     * Constructor.
     */
    protected KeyChain(Op anOp, Object child)
    {
        _op = anOp; addChild(child);
    }

    /**
     * Constructor.
     */
    protected KeyChain(Op anOp, Object left, Object right)
    {
        _op = anOp; addChild(left); addChild(right);
    }

    /**
     * Constructor.
     */
    protected KeyChain(Object cond, Object tExp, Object fExp)
    {
        _op = Op.Conditional;
        addChild(cond);
        addChild(tExp);
        if (fExp!=null)
            addChild(fExp);
    }

    /**
     * Returns the top level operator of the keychain.
     */
    public Op getOp()  { return _op; }

    /**
     * Returns the value of the keychain.
     */
    public Object getValue()  { return _children; }

    /**
     * Returns the value of the keychain as a string.
     */
    public String getValueString()
    {
        return _children instanceof String ? (String) _children : null;
    }

    /**
     * Returns the number of children in the keychain.
     */
    public int getChildCount()
    {
        return _children instanceof List ? ((List)_children).size() : _children!=null ? 1 : 0;
    }

    /**
     * Returns the child at the given index in the keychain.
     */
    public Object getChild(int anIndex)
    {
        if (_children instanceof List)
            return ((List)_children).get(anIndex);
        if (anIndex == 0)
            return _children;
        return null;
    }

    /**
     * Adds a child to the end of the keychain's child list.
     */
    public void addChild(Object child)
    {
        // If first child, just set Children to point to it
        if (_children==null)
            _children = child;

        // If Children already list, just add child
        else if (_children instanceof List)
            ((List)_children).add(child);

        // Else, create list and add
        else {
            List c = new ArrayList(4);
            c.add(_children);
            c.add(child);
            _children = c;
        }
    }

    /**
     * Returns the child at the given index in the keychain as a string.
     */
    public String getChildString(int i)
    {
        Object o = getChild(i);
        return o == null ? "<null string>" : o.toString();
    }

    /**
     * Returns the child at the given index in the keychain as a keychain.
     */
    public KeyChain getChildKeyChain(int i)  { return (KeyChain) getChild(i); }

    /**
     * Override to give list chance to implement this.
     */
    public KeyChain subchain(int anIndex)
    {
        int ccount = getChildCount();
        if (anIndex+1 == ccount)
            return getChildKeyChain(anIndex);
        KeyChain kc = new KeyChain(Op.Chain);
        for (int i=anIndex; i<ccount; i++)
            kc.addChild(getChild(i));
        return kc;
    }

    /**
     * Returns the result of evaluating the given key chain on the given object.
     */
    public static Object getValue(Object anObj, Object aKeyChain)
    {
        KeyChain keyChain = getKeyChain(aKeyChain);
        return getValue(anObj, keyChain);
    }

    /**
     * Returns the result of evaluating the given key chain on the given object.
     */
    public static Object getValue(Object anObj, KeyChain aKeyChain)
    {
        return getValue(anObj, anObj, aKeyChain);
    }

    /**
     * Returns the result of evaluating the given key chain on the given object.
     */
    public static Object getValue(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // If object is null, just return null
        if (anObj==null) return null;

        // If list, use aggregator
        if (anObj instanceof List) { List list = (List) anObj;
            Object val = getValueImpl(aRoot, anObj, aKeyChain);
            if (val==null && list.size()>0)
                val = getValueImpl(aRoot, list.get(0), aKeyChain);
            return val;
        }

        // Invoke the general implementation
        return getValueImpl(aRoot, anObj, aKeyChain);
    }

    /**
     * Returns the result of evaluating the given key chain on the given object.
     * Broken out so objects can implement custom getKeyChainValue but still have access to default implementation.
     */
    public static Object getValueImpl(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // Evaluate key chain based on operator type
        switch (aKeyChain.getOp()) {

            // Handle Literals: String, Number, Null
            case Literal: return aKeyChain.getValue();

            // Handle binary math ops: Add, Subtract, Multiply, Divide, Mod
            case Add:
            case Subtract:
            case Multiply:
            case Divide:
            case Mod: return getValueBinaryMathOp(aRoot, anObj, aKeyChain);

            // Handle Negate
            case Negate: {
                Object o1 = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));
                return o1 instanceof Number ? MathUtils.negate((Number)o1) : null;
            }

            // Handle binary compare ops: GreaterThan, LessThan, Equal, NotEqual, GreaterThanOrEqual, LessThanOrEqual
            case GreaterThan:
            case LessThan:
            case Equal:
            case NotEqual:
            case GreaterThanOrEqual:
            case LessThanOrEqual: return getValueBinaryCompareOp(aRoot, anObj, aKeyChain);

            // Handle Not
            case Not: return !getBoolValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));

            // Handle binary logical ops: And, Or
            case And:
            case Or: {
                boolean b1 = getBoolValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));
                boolean b2 = getBoolValue(aRoot, anObj, aKeyChain.getChildKeyChain(1));
                return  aKeyChain.getOp()==Op.And ? (b1 && b2) : (b1 || b2);
            }

            // Handle basic Key
            case Key: {
                Object value = Key.getValue(anObj, aKeyChain.getValueString());
                if (value==null)
                    value = getAssignments().get(aKeyChain.getValue());
                return value;
            }

            // Handle ArrayIndex
            case ArrayIndex: return getValueArrayIndex(aRoot, anObj, aKeyChain);

            // Handle FunctionCall
            case FunctionCall: return getValueFunctionCall(aRoot, anObj, aKeyChain);

            // Handle Chain
            case Chain: return getValueChain(aRoot, anObj, aKeyChain);

            // Handle Conditional
            case Conditional: {
                boolean result = getBoolValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));
                if (result)
                    return getValue(aRoot, anObj, aKeyChain.getChildKeyChain(1));
                return aKeyChain.getChildCount()==3 ? getValue(aRoot, anObj, aKeyChain.getChildKeyChain(2)) : null;
            }

            // Handle Assignment
            case Assignment: {
                Object value = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(1));
                getAssignments().put(aKeyChain.getChildString(0), value); return "";
            }

            // Handle the impossible
            default: throw new RuntimeException("KeyChain.getValueImpl: Invalid op " + aKeyChain.getOp());
        }
    }

    /**
     * Returns a boolean value.
     */
    private static boolean getBoolValue(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        Object value = getValue(aRoot, anObj, aKeyChain);
        return SnapUtils.boolValue(value);
    }

    /**
     * Returns the result of evaluating the given key chain with binary math operator.
     */
    private static Object getValueBinaryMathOp(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // Get value of operands
        Object o1 = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));
        Object o2 = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(1));

        // If non-numeric operand values (except add), just return
        if (!(o1 instanceof Number && o2 instanceof Number) && aKeyChain.getOp()!=Op.Add) return null;

        // Handle Math ops: Add, Subtract, Multiply, Divide, Mod
        switch (aKeyChain.getOp()) {
            case Add: return add(o1, o2);
            case Subtract: return MathUtils.subtract((Number)o1, (Number)o2);
            case Multiply: return MathUtils.multiply((Number)o1, (Number)o2);
            case Divide: return MathUtils.divide((Number)o1, (Number)o2);
            case Mod: return MathUtils.mod(SnapUtils.doubleValue(o1), SnapUtils.doubleValue(o2));
            default: throw new RuntimeException("KeyChain.getValueBinaryMathOp: Not a math op.");
        }
    }

    /**
     * Returns the sum of the two given objects (assumed to be strings or numbers).
     */
    private static Object add(Object obj1, Object obj2)
    {
        // If strings, do string concat (accounting for nulls)
        if (obj1 instanceof String || obj2 instanceof String)
            try { return (obj1==null ? "" : obj1.toString()) + (obj2==null ? "" : obj2.toString()); }
            catch(Exception e) { return null; }

        // If numbers, do Math.add()
        if (obj1 instanceof Number || obj2 instanceof Number)
            return MathUtils.add(SnapUtils.numberValue(obj1), SnapUtils.numberValue(obj2));

        // If nulls, just return null
        if (obj1==null && obj2==null) return null;

        // Fallback, try to add as strings or bail with null
        try { return (obj1==null ? "" : obj1.toString()) + (obj2==null ? "" : obj2.toString()); }
        catch(Exception e) { return null; }
    }

    /**
     * Returns the result of evaluating the given key chain with binary compare operator.
     */
    private static Object getValueBinaryCompareOp(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // Get value of operands
        Object o1 = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(0));
        Object o2 = getValue(aRoot, anObj, aKeyChain.getChildKeyChain(1));

        // Handle binary compare ops: GreaterThan, LessThan, Equal, NotEqual, GreaterThanOrEqual, LessThanOrEqual
        switch (aKeyChain.getOp()) {
            case GreaterThan: return Sort.Compare(o1, o2)==Sort.ORDER_DESCEND;
            case LessThan: return Sort.Compare(o1, o2)==Sort.ORDER_ASCEND;
            case Equal: return Sort.Compare(o1, o2)==Sort.ORDER_SAME;
            case NotEqual: return Sort.Compare(o1, o2)!=Sort.ORDER_SAME;
            case GreaterThanOrEqual: return Sort.Compare(o1, o2)!=Sort.ORDER_ASCEND;
            case LessThanOrEqual: return Sort.Compare(o1, o2)!=Sort.ORDER_DESCEND;
            default: throw new RuntimeException("KeyChain.getValueBinaryCompareOp: Not a compare op.");
        }
    }

    /**
     * Returns the result of evaluating the given ArrayIndex KeyChain on the given object.
     */
    private static Object getValueArrayIndex(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        KeyChain arrayKeyChain = aKeyChain.getChildKeyChain(0);
        Object o1 = getValue(anObj, arrayKeyChain); if (!(o1 instanceof List)) return null;
        KeyChain indexKeyChain = aKeyChain.getChildKeyChain(1);
        int index = getIntValue(aRoot, indexKeyChain);
        return ListUtils.get((List)o1, index);
    }

    /**
     * Returns the result of evaluating the given Function KeyChain on the given object.
     */
    private static Object getValueFunctionCall(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        System.out.println("KeyChain.getValueFunctionCall: Not implemented"); return null;
    }

    /**
     * Returns the result of evaluating the given Chain KeyChain on the given object.
     */
    private static Object getValueChain(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        Object value = anObj;
        for (int i=0, iMax=aKeyChain.getChildCount(); i<iMax; i++) {
            KeyChain child = aKeyChain.getChildKeyChain(i);
            value = getValue(aRoot, value, child);
        }
        return value;
    }

    /** Convenience - returns a string for an object and key chain. */
    public static String getStringValue(Object anObj, Object aKeyChain)
    { return SnapUtils.stringValue(getValue(anObj, aKeyChain)); }

    /** Convenience - returns a number for an object and key chain. */
    public static Number getNumberValue(Object anObj, Object aKeyChain)
    { return SnapUtils.numberValue(getValue(anObj, aKeyChain)); }

    /** Convenience - returns an int for an object and key chain. */
    public static int getIntValue(Object anObj, Object aKeyChain)
    { return SnapUtils.intValue(getValue(anObj, aKeyChain)); }

    /** Convenience - returns a float for an object and key chain. */
    public static float getFloatValue(Object anObj, Object aKeyChain)
    { return SnapUtils.floatValue(getValue(anObj, aKeyChain)); }

    /** Convenience - returns a double for an object and key chain. */
    public static double getDoubleValue(Object anObj, Object aKeyChain)
    { return SnapUtils.doubleValue(getValue(anObj, aKeyChain)); }

    /** Convenience - returns a boolean for an object and key chain. */
    public static boolean getBoolValue(Object anObj, Object aKeyChain)
    { return SnapUtils.boolValue(getValue(anObj, aKeyChain)); }

    /**
     * Returns the last error encountered by the key chain parser (or null).
     */
    public static String getError()  { return _parser.getError(); }

    /**
     * Returns the last error encountered by the key chain parser and resets parser.
     */
    public static String getAndResetError()  { return _parser.getAndResetError(); }

    /**
     * Sets the given value for the given key chain + property.
     * This is a real bogus loser implementation.
     */
    public static void setValue(Object anObj, Object aKeyChain, Object aValue)
    {
        setValue(anObj, getKeyChain(aKeyChain), aValue);
    }

    /**
     * Sets the given value for the given key chain + property.
     * This is a real bogus loser implementation that only supports Op.Key and Op.Chain.
     */
    public static void setValue(Object anObj, KeyChain aKeyChain, Object aValue)
    {
        // Get real object and key
        Object obj = anObj;
        KeyChain kchain = aKeyChain;

        // Handle Chain: Evaluate down chain to get last object & KeyChain
        if (kchain.getOp() == Op.Chain) {
            int cc = aKeyChain.getChildCount();
            KeyChain kc = new KeyChain(Op.Chain);
            for (int i=0;i<cc-1;i++)
                kc.addChild(aKeyChain.getChild(i));
            obj = getValue(anObj, kc);
            kchain = aKeyChain.getChildKeyChain(cc-1);
        }

        // If not Key, just return
        if (kchain.getOp()!=Op.Key)  {
            System.err.println("KeyChain.setValue: Last op not key.");
            return;
        }

        // Get key and set
        String key = kchain.getChildString(0);
        try { Key.setValue(obj, key, aValue); }
        catch(Exception e)  { throw new RuntimeException(e); }
    }

    /**
     * Sets the value but only prints a warning if it fails.
     */
    public static void setValueSafe(Object anObj, String aKey, Object aValue)
    {
        try { setValue(anObj, aKey, aValue); }
        catch(Exception e) { Class cls = ClassUtils.getClass(anObj);
            String msg = (cls!=null ? cls.getSimpleName() : "null") + " " + aKey + " " + aValue;
            System.err.println("KeyChain.setValue (" + msg + ") failed: " + e);
        }
    }

    /**
     * Tries to set value in given object, ignoring failure exceptions.
     */
    public static void setValueSilent(Object anObj, String aKey, Object aValue)
    {
        try { setValue(anObj, aKey, aValue); }
        catch(Exception e) { }
    }

    /**
     * Returns a string representation of the key chain.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int cc = getChildCount();
        switch (getOp()) {
            case Key: case Literal: sb.append(getValue()); break;
            case Add: sb.append(getChild(0)).append('+').append(getChild(1)); break;
            case Subtract: sb.append(getChild(0)).append('-').append(getChild(1)); break;
            case Multiply: sb.append(getChild(0)).append('*').append(getChild(1)); break;
            case Divide: sb.append(getChild(0)).append('/').append(getChild(1)); break;
            case Mod: sb.append(getChild(0)).append('%').append(getChild(1)); break;
            case Negate: sb.append('-').append(getChild(0)); break;
            case GreaterThan: sb.append(getChild(0)).append('>').append(getChild(1)); break;
            case LessThan: sb.append(getChild(0)).append('<').append(getChild(1)); break;
            case GreaterThanOrEqual: sb.append(getChild(0)).append(">=").append(getChild(1)); break;
            case LessThanOrEqual: sb.append(getChild(0)).append("<=").append(getChild(1)); break;
            case Equal: sb.append(getChild(0)).append("==").append(getChild(1)); break;
            case NotEqual: sb.append(getChild(0)).append("!=").append(getChild(1)); break;
            case Not: sb.append('!').append(getChild(0)); break;
            case And: sb.append(getChild(0)).append(" && ").append(getChild(1)); break;
            case Or: sb.append(getChild(0)).append(" || ").append(getChild(1)); break;
            case ArrayIndex: sb.append(getChild(0)).append('[').append(getChild(1)).append(']'); break;
            case FunctionCall: sb.append(getChild(0)).append('(').append(getChild(1)).append(')'); break;
            case ArgList: for (int i=0,iMax=cc;i<iMax;i++) { if (i>0) sb.append(','); sb.append(getChild(i)); } break;
            case Conditional: sb.append(getChild(0)).append('?').append(getChild(1));
                if (cc>2) sb.append(':').append(getChild(2)); break;
            case Chain: for (int i=0,iMax=cc;i<iMax; i++) { if (i>0) sb.append('.'); sb.append(getChild(i)); } break;
        }
        return sb.toString();
    }

    /**
     * Simple main implementation, so RM's expressions can be used for simple math.
     */
    public static void main(String args[]) throws IOException
    {
        // If there is an arg, evaluate it, otherwise if no args, read from standard in until control-d
        if (args.length>0 && args[0].length()>0) {
            Object value = KeyChain.getValue(new Object(), args[0]);
            System.out.println(value instanceof Number ? SnapUtils.getBigDecimal(value) : value);
        }
        else {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
            for (String ln=rdr.readLine(); ln!=null; ln=rdr.readLine())
                main(new String[] { ln });
        }
    }
}