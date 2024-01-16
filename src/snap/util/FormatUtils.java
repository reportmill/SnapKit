/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
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

    // A map of known Decimal formats for pattern
    private static Map<String, DateFormat> _dateFormats = new HashMap<>();

    // A simple date format
    private static SimpleDateFormat  _dateFormat;

    /**
     * Formats a number to a reasonable precision.
     */
    public static String formatNum(double aValue)
    {
        // Get absolute value
        double val = Math.abs(aValue);

        // If greater than 1000 or whole number, just use whole
        if (val >= 1000 || val == (long) val)
            return String.valueOf((long) aValue);

        // If greater than one, provide for 2 decimal places
        if (val >= 1)
            return formatNum("#.##", aValue);

        // If greater than .1, provide for 3 decimal places
        if (val >= .1)
            return formatNum("#.###", aValue);

        // If greater than .01, provide for 4 decimal places
        if (val >= .01)
            return formatNum("#.####", aValue);

        // If greater than .001, provide for 5 decimal places
        if (val >= .001)
            return formatNum("#.#####", aValue);

        // If greater than .0001, provide for 6 decimal places
        if (val >= .0001)
            return formatNum("#.######", aValue);

        // Just splat it all
        return String.valueOf(aValue);
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
        DecimalFormat fmt = getDecimalFormat(aPattern);
        return fmt.format(aNum);
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
     * Returns a date format for given pattern.
     */
    public static DateFormat getDateFormat(String aPattern)
    {
        DateFormat fmt = _dateFormats.get(aPattern);
        if (fmt == null)
            _dateFormats.put(aPattern, fmt = new SimpleDateFormat(aPattern));
        return fmt;
    }

    /**
     * Formats a Date to a standard pattern: .
     */
    public static String formatDate(Date aDate)
    {
        DateFormat dateFormat = getDateFormat();
        return dateFormat.format(aDate);
    }

    /**
     * Formats a Date to a reasonable precision.
     */
    public static String formatDate(Date aDate, String aPattern)
    {
        DateFormat dateFormat = getDateFormat(aPattern);
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

    /**
     * Returns a pattern for given number of fraction digits.
     */
    public static String getPatternForFractionDigits(int aFractionDigitCount)
    {
        // Basic pattern for whole number intervals is just "0"
        String pattern = "0";

        // If fractional digits, add fractional digits to pattern
        if (aFractionDigitCount > 0) {
            pattern = "#.#";
            for (int i = 1; i < aFractionDigitCount; i++)
                pattern += '#';
        }

        // Return pattern
        return pattern;
    }
}
