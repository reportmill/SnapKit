package snap.util;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * This class holds utilities to convert values.
 */
public class Convert {

    // A formatter to format double without exponent
    private static DecimalFormat  _doubleFmt = new DecimalFormat("0.#########");

    /**
     * Returns a boolean value for the given object.
     */
    public static boolean boolValue(Object anObj)
    {
        // If Boolean, return bool value
        if (anObj instanceof Boolean)
            return ((Boolean) anObj);

        // If number, return true if number is non-zero
        if (anObj instanceof Number)
            return !MathUtils.equalsZero(((Number) anObj).floatValue());

        // If string and "false", return false
        if (anObj instanceof String && StringUtils.equalsIC((String) anObj, "false"))
            return false;

        // Return true if non-null
        return anObj != null;
    }

    /**
     * Returns the int value for a given object (assumed to be a string or number).
     */
    public static int intValue(Object anObj)
    {
        return (int) longValue(anObj);
    }

    /**
     * Returns the int value for a given object (assumed to be a string or number).
     */
    public static long longValue(Object anObj)
    {
        // Handle Number or String
        if (anObj instanceof Number)
            return ((Number) anObj).longValue();
        if (anObj instanceof String)
            return StringUtils.longValue((String) anObj);

        // Return default
        return 0;
    }

    /**
     * Returns the float value for a given object (assumed to be a string or number).
     */
    public static float floatValue(Object anObj)
    {
        return (float) doubleValue(anObj);
    }

    /**
     * Returns the double value for a given object (assumed to be a string or number).
     */
    public static double doubleValue(Object anObj)
    {
        // Handle Number or String
        if (anObj instanceof Number)
            return ((Number) anObj).doubleValue();
        if (anObj instanceof String)
            return StringUtils.doubleValue((String) anObj);

        // Return default
        return 0;
    }

    /**
     * Returns a String for a given arbitrary object.
     */
    public static String stringValue(Object anObj)
    {
        // If object is null, return null
        if (anObj == null) return null;

        // If object is string, just return it
        if (anObj instanceof String)
            return (String) anObj;

        // If object is number, string format it
        if (anObj instanceof Number)
            return FormatUtils.formatNum((Number) anObj);

        // If object is Date, date format it
        if (anObj instanceof Date)
            return FormatUtils.formatDate((Date) anObj);

        // If byte array, format as base64
        if (anObj instanceof byte[]) {
            String str = ASCIICodec.encodeBase64((byte[]) anObj);
            str = str.replace((char) 0, ' ');
            return str;
        }

        // If class get standard name
        if (anObj instanceof Class)
            return ((Class<?>) anObj).getName().replace('$', '.');

        // Return object's toString
        return anObj.toString();
    }

    /**
     * Returns the Boolean for a given object (assumed to be Number or String).
     */
    public static Boolean booleanValue(Object anObj)
    {
        // Handle Boolean or null
        if (anObj instanceof Boolean || anObj == null)
            return (Boolean) anObj;

        // Return boolean value
        return boolValue(anObj);
    }

    /**
     * Returns the Number for a given object (assumed to be Number or String).
     */
    public static Number numberValue(Object anObj)
    {
        // Handle Number or null
        if (anObj instanceof Number || anObj == null)
            return (Number) anObj;

        // Try returning as BigDecimal  - can fail if is Nan or pos/neg infinity (returns as double)
        try { return getBigDecimal(anObj); }
        catch (Exception e) {
            return doubleValue(anObj);
        }
    }

    /**
     * Returns the Integer for a given object.
     */
    public static Integer getInteger(Object anObj)
    {
        // Handle Integer or null
        if (anObj instanceof Integer || anObj == null)
            return (Integer) anObj;

        // Return int value
        return intValue(anObj);
    }

    /**
     * Returns a Float for a given object.
     */
    public static Float getFloat(Object anObj)
    {
        // Handle Float or null
        if (anObj instanceof Float || anObj == null)
            return (Float) anObj;

        // Return float value
        return floatValue(anObj);
    }

    /**
     * Returns a Double for a given object.
     */
    public static Double getDouble(Object anObj)
    {
        // Handle Double or null
        if (anObj instanceof Double || anObj == null)
            return (Double) anObj;

        // Return double value
        return doubleValue(anObj);
    }

    /**
     * Returns the BigDecimal for a given object (assumed to be a string or number).
     */
    public static BigDecimal getBigDecimal(Object anObj)
    {
        // Handle BigDecimal or null
        if (anObj instanceof BigDecimal || anObj == null)
            return (BigDecimal) anObj;

        // Return double value as BigDecimal
        double doubleValue = doubleValue(anObj);
        return new BigDecimal(doubleValue);
    }

    /**
     * Returns a double array for given floats.
     */
    public static double[] floatArrayToDouble(float[] theFloats)
    {
        if (theFloats == null) return null;
        double[] doubleArray = new double[theFloats.length];
        for (int i = 0; i < theFloats.length; i++)
            doubleArray[i] = theFloats[i];
        return doubleArray;
    }

    /**
     * Returns a float array for given doubles.
     */
    public static float[] doubleArrayToFloat(double[] theDoubles)
    {
        if (theDoubles == null) return null;
        float[] floatArray = new float[theDoubles.length];
        for (int i = 0; i < theDoubles.length; i++)
            floatArray[i] = (float) theDoubles[i];
        return floatArray;
    }

    /**
     * Return string for double array.
     */
    public static String doubleArrayToString(double[] theValues)
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
    public static double[] stringToDoubleArray(String aStr)
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
     * Returns an array of String values for given comma separated string.
     */
    public static String[] stringToStringArray(String aStr)
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
     * Returns a date for given object of arbitrary type.
     */
    public static Date getDate(Object anObj)
    {
        // If object is date or null, just return it
        if (anObj instanceof Date || anObj == null)
            return (Date) anObj;

        // If object is long, return date
        if (anObj instanceof Long)
            return new Date((Long) anObj);

        // Otherwise, try to parse string as simple date
        // Was this: return new java.text.SimpleDateFormat("MM/dd/yy").parse(anObj.toString());
        //try { return RMDateUtils.getDate(anObj.toString()); } catch(Exception e) { return null; }
        return DateParser.parseDate(anObj.toString());
    }

    /**
     * Returns whether char at given index in given string is number char.
     */
    private static boolean isNumChar(String aStr, int anIndex)
    {
        char c = aStr.charAt(anIndex);
        return Character.isDigit(c) || c == '.' || c == '-';
    }
}
