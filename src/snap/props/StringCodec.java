/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.geom.Insets;
import snap.gfx.Color;
import snap.util.Convert;
import snap.util.EnumUtils;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to convert common types to/from Strings.
 */
public class StringCodec {

    // The Set of codeable classes
    private Set<Class<?>>  _codeableClasses;

    // The Shared instance
    public static StringCodec SHARED = new StringCodec();

    // A formatter to format double without exponent
    private static DecimalFormat  _doubleFmt = new DecimalFormat("0.#########");

    /**
     * Returns whether given object can be converted to/from String.
     */
    public boolean isCodeable(Object anObj)
    {
        // Null is inherently codeable
        if (anObj == null) return true;

        // Empty arrays are codeable
        if (isEmptyArray(anObj))
            return true;

        // Otherwise get object class and check
        Class<?> objClass = anObj.getClass();
        return isCodeableClass(objClass);
    }

    /**
     * Returns whether given object can be converted to/from String.
     */
    public boolean isCodeableClass(Class<?> aClass)
    {
        // Enums are inherently codeable
        if (aClass.isEnum()) return true;

        // Codeable
        if (Codeable.class.isAssignableFrom(aClass))
            return true;

        // If array, check to see if component type is supported
        if (aClass.isArray()) {
            Class<?> compClass = aClass.getComponentType();
            return isCodeableClass(compClass);
        }

        // Otherwise get CodeableClasses and check
        Set<Class<?>> codeableClasses = getCodeableClasses();
        return codeableClasses.contains(aClass);
    }

    /**
     * Returns set of classes that can be converted to/from String.
     */
    public Set<Class<?>> getCodeableClasses()
    {
        // If already set, just return
        if (_codeableClasses != null) return _codeableClasses;

        // Create CodeableClasses HashSet, set and return
        Set<Class<?>> codeableClasses = getCodeableClassesImpl();
        return _codeableClasses = codeableClasses;
    }

    /**
     * Returns set of classes that can be converted to/from String.
     */
    protected Set<Class<?>> getCodeableClassesImpl()
    {
        Set<Class<?>> set = new HashSet<>();

        // Handle String
        set.add(String.class);

        // Handle boolean, int, float, double
        set.add(boolean.class);
        set.add(Boolean.class);
        set.add(int.class);
        set.add(float.class);
        set.add(double.class);
        set.add(Integer.class);
        set.add(Float.class);
        set.add(Double.class);
        set.add(Number.class);

        // Handle String[], double[]
        set.add(String[].class);
        set.add(double[].class);

        // Color, Insets
        set.add(Color.class);
        set.add(Insets.class);

        // Return
        return set;
    }

    /**
     * Returns a String for given object.
     */
    public String codeString(Object anObj)
    {
        // Handle null
        if (anObj == null)
            return "null";

        // Handle enum
        if (anObj.getClass().isEnum())
            return anObj.toString();

        // Handle String
        if (anObj instanceof String)
            return (String) anObj;

        // Handle Boolean, Number
        if (anObj instanceof Boolean)
            return anObj.toString();
        if (anObj instanceof Number)
            return Convert.stringValue(anObj);

        // Handle String[]
        if (anObj instanceof String[])
            return Arrays.toString((String[]) anObj);

        // Handle double[]
        if (anObj instanceof double[])
            return getStringForDoubleArray((double[]) anObj);

        // Handle Codeable
        if (anObj instanceof Codeable)
            return ((Codeable) anObj).codeString();

        // Handle Color
        if (anObj instanceof Color)
            return ((Color) anObj).toHexString();

        // Handle Insets
        if (anObj instanceof Insets)
            return ((Insets) anObj).getString();

        // Handle Array
        Class<?> objClass = anObj.getClass();
        if (objClass.isArray()) {

            // Handle supported component array
            if (!objClass.getComponentType().isPrimitive()) {
                Object[] array = (Object[]) anObj;
                String[] strings = new String[array.length];
                for (int i = 0; i < array.length; i++)
                    strings[i] = codeString(array[i]);
                return Arrays.toString(strings);
            }
        }

        // Complain and return null
        System.err.println("StringCodec.getString: Unsupported class: " + anObj.getClass());
        return null;
    }

    /**
     * Returns an Object for given String and class.
     */
    public <T> T decodeString(String aString, Class<T> aClass)
    {
        // Handle null
        if (aString.equals("null"))
            return null;

        // Handle enum
        if (aClass.isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) aClass;
            Object enumVal = EnumUtils.valueOfIC(enumClass, aString);
            return (T) enumVal;
        }

        // Handle String
        if (aClass == String.class)
            return (T) aString;

        // Handle boolean, int, float, double
        if (aClass == boolean.class || aClass == Boolean.class)
            return (T) Convert.booleanValue(aString);
        if (aClass == int.class || aClass == Integer.class)
            return (T) Convert.getInteger(aString);
        if (aClass == float.class || aClass == Float.class)
            return (T) Convert.getFloat(aString);
        if (aClass == double.class || aClass == Double.class)
            return (T) Convert.getDouble(aString);

        // Handle String[]
        if (aClass == String[].class)
            return (T) getStringArrayForString(aString);

        // Handle double[]
        if (aClass == double[].class)
            return (T) getDoubleArrayForString(aString);

        // Handle Codeable
        if (Codeable.class.isAssignableFrom(aClass)) {
            Codeable codeable;
            try { codeable = (Codeable) aClass.newInstance(); }
            catch (Exception e) { throw new RuntimeException(e); }
            codeable = codeable.decodeString(aString);
            return (T) codeable;
        }

        // Handle Color
        if (aClass == Color.class)
            return (T) Color.get(aString);

        // Handle Insets
        if (aClass == Insets.class)
            return (T) Insets.get(aString);

        // Handle Array
        if (aClass.isArray()) {
            Class compClass = aClass.getComponentType();
            String string = aString.substring(1, aString.length() - 1);
            String[] strings = string.split("\\s*,\\s*");
            Object[] array = (Object[]) Array.newInstance(compClass, strings.length);
            for (int i = 0; i < array.length; i++)
                array[i] = decodeString(strings[i], compClass);
            return (T) array;
        }

        // Complain and return null
        System.err.println("StringCodec.getObjectForString: Unsupported class: " + aClass);
        return null;
    }

    /**
     * Returns an array of String values for given comma separated string.
     */
    public static String[] getStringArrayForString(String aStr)
    {
        // String start/end brackets ( '[ one two three ]')
        String str = aStr;
        if (str.startsWith("[") && str.endsWith("]"))
            str = str.substring(1, str.length() - 1);

        String[] valStrs = str.split("\\s*,\\s*");
        int len = valStrs.length;
        int count = 0;
        String[] vals = new String[len];
        for (String valStr : valStrs) {
            if (valStr.startsWith("\""))
                valStr = valStr.substring(1);
            if (valStr.endsWith("\""))
                valStr = valStr.substring(0, valStr.length() - 1);
            vals[count++] = valStr;
        }
        return vals;
    }

    /**
     * Return string for double array.
     */
    public String getStringForDoubleArray(double[] theValues)
    {
        // If empty, return empty array string
        if (theValues.length == 0) return "[ ]";

        // Create string with open bracket and first val
        StringBuilder sb = new StringBuilder("[ ");
        sb.append(_doubleFmt.format(theValues[0]));

        // Iterate over remaining vals and add separator plus val for each
        for (int i = 1; i < theValues.length; i++)
            sb.append(", ").append(_doubleFmt.format(theValues[i]));

        // Return string with close bracket
        return sb.append(" ]").toString();
    }

    /**
     * Returns an array of double values for given comma separated string.
     */
    public static double[] getDoubleArrayForString(String aStr)
    {
        // Get string, stripped of surrounding non-number chars
        String str = aStr.trim();
        int start = 0;
        while (start < str.length() && !isNumChar(str, start)) start++;
        int end = str.length();
        while(end > start && !isNumChar(str, end - 1)) end--;
        str = str.substring(start, end);

        // Get strings for values separated by comma
        String[] valStrs = str.split("\\s*,\\s*");
        int len = valStrs.length;

        // Create array for return vals
        double[] vals = new double[len];
        int count = 0;

        // Iterate over strings and add valid numbers
        for (String valStr : valStrs) {
            if (valStr.length() > 0) {
                try {
                    double val = Double.parseDouble(valStr);
                    vals[count++] = val;
                }
                catch (Exception e)  { }
            }
        }

        // Return vals (trimmed to size)
        return count<len ? Arrays.copyOf(vals, count) : vals;
    }

    /**
     * Returns whether char at given index in given string is number char.
     */
    private static boolean isNumChar(String aStr, int anIndex)
    {
        char c = aStr.charAt(anIndex);
        return Character.isDigit(c) || c == '.' || c == '-';
    }

    /**
     * Returns whether given object is an empty array.
     */
    private static boolean isEmptyArray(Object anObj)
    {
        Class<?> objClass = anObj.getClass();
        if (!objClass.isArray()) return false;
        if (objClass.getComponentType().isPrimitive()) return false;
        Object[] array = (Object[]) anObj;
        return array.length == 0;
    }

    /**
     * An interface for classes that know how to code/decode themselves.
     */
    public interface Codeable {

        /**
         * Returns a String representation of this object.
         */
        String codeString();

        /**
         * Configures this object from a String representation.
         */
        Codeable decodeString(String aString);
    }
}
