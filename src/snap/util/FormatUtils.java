package snap.util;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for formatting numbers and dates.
 */
public class FormatUtils {

    // A map of known Decimal formats for pattern
    private static Map<String, DecimalFormat> _formats = new HashMap<>();

    // A simple date format
    private static SimpleDateFormat  _dateFormat;

    /**
     * Formats a number to a reasonable precision.
     */
    public static String formatNum(double aVal)
    {
        if (aVal >= 1000 || aVal == (long) aVal)
            return String.valueOf((long) aVal);
        if (aVal >= 1)
            return formatNum("#.##", aVal);
        if (aVal >= .1)
            return formatNum("#.###", aVal);
        return String.valueOf(aVal);
    }

    /**
     * Formats a number to a reasonable precision.
     */
    public static String formatNum(Number aVal)
    {
        if (aVal instanceof Float || aVal instanceof Double)
            return formatNum(aVal.doubleValue());
        return aVal.toString();
    }

    /**
     * Formats a number with given Decimal format pattern.
     */
    public static String formatNum(String aPattern, Object aNum)
    {
        // Get format and string
        DecimalFormat fmt = getDecimalFormat(aPattern);
        String str = fmt.format(aNum);

        // TeaVM seem to have issues with: #.## and .977757? Fixed: https://github.com/konsoletyper/teavm/issues/557
        if (SnapUtils.isTeaVM && str.indexOf(':') >= 0 || str.equals("-"))
            return String.valueOf(aNum);

        // Return string
        return str;
    }

    /**
     * Returns a decimal format for given pattern.
     */
    public static DecimalFormat getDecimalFormat(String aPattern)
    {
        DecimalFormat fmt = _formats.get(aPattern);
        if (fmt == null)
            _formats.put(aPattern, fmt = new DecimalFormat(aPattern));
        return fmt;
    }

    /**
     * Formats a Date to a reasonable precision.
     */
    public static String formatDate(Date aDate)
    {
        DateFormat dateFormat = getDateFormat();
        return dateFormat.format(aDate);
    }

    /**
     * Returns a SimpleDateFormat.
     */
    public static SimpleDateFormat getDateFormat()
    {
        if (_dateFormat != null) return _dateFormat;
        return _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
