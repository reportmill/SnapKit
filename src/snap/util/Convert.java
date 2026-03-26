package snap.util;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds utilities to convert values.
 */
public class Convert {

    // A regex pattern to find integer/long numbers
    private static Pattern _intLongPattern = Pattern.compile("[-+]?[0-9]+");

    // A formatter to format double without exponent
    private static DecimalFormat  _doubleFmt = new DecimalFormat("0.#########");

    // A regex pattern to find numbers in strings (supports positive/negative, floating point & exponents)
    private static Pattern _numberPattern = Pattern.compile("[-+]?(([0-9]+\\.?[0-9]*)|([0-9]*\\.[0-9]+))([eE][-+]?[0-9]+)?");

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
        if (anObj instanceof String) {
            String str = ((String) anObj).trim();
             if (StringUtils.equalsIC(str, "false"))
                return false;
        }

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
            return longValue((String) anObj);
        if (anObj instanceof Character)
            return (Character) anObj;
        if (anObj instanceof Boolean)
            return ((Boolean) anObj) ? 1 : 0;

        // Return default
        return 0;
    }

    /**
     * Returns an double value by parsing the given string starting at the given index.
     */
    public static long longValue(String aString)
    {
        // Bail if string is null
        if (aString == null) return 0;

        // Get number matcher for string
        Matcher matcher = _intLongPattern.matcher(aString);

        // If number found, have Long parse it
        if (matcher.find()) {
            String string = matcher.group();
            try { return Long.parseLong(string); }
            catch (Exception ignore) { }
        }

        // Return zero since number not found
        return 0;
    }

    /**
     * Returns the float value for a given object (assumed to be a string or number).
     */
    public static float floatValue(Object anObj)  { return (float) doubleValue(anObj); }

    /**
     * Returns a float value by parsing the given string.
     */
    public static float floatValue(String aString)  { return (float) doubleValue(aString); }

    /**
     * Returns the double value for a given object (assumed to be a string or number).
     */
    public static double doubleValue(Object anObj)
    {
        // Handle Number or String
        if (anObj instanceof Number)
            return ((Number) anObj).doubleValue();
        if (anObj instanceof String)
            return doubleValue((String) anObj);
        if (anObj instanceof Character)
            return (Character) anObj;
        if (anObj instanceof Boolean)
            return ((Boolean) anObj) ? 1 : 0;

        // Return default
        return 0;
    }

    /**
     * Returns a double value by parsing the given string.
     */
    public static double doubleValue(String aString)  { return doubleValue(aString, 0); }

    /**
     * Returns a double value by parsing the given string starting at the given index.
     */
    public static double doubleValue(String aString, int aStart)
    {
        // Bail if string is null or start index beyond bounds
        if (aString == null || aStart > aString.length()) return 0;

        // Get number matcher for string
        Matcher matcher = _numberPattern.matcher(aString);

        // If number found, have Double parse it
        if (matcher.find(aStart)) {
            String str = matcher.group();
            try { return Double.parseDouble(str); }
            catch (Exception ignore) { }
        }

        // Return zero since number not found
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

        // Handle Character or Boolean
        if (anObj instanceof Character || anObj instanceof Boolean)
            return String.valueOf(anObj);

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
}
