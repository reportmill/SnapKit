/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.util.FormatUtils;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A class to format tick labels.
 */
public class NumberFormat implements TextFormat {

    // The format pattern
    protected String  _pattern;

    // The DecimalFormat
    protected DecimalFormat  _format;

    // The Exponent Style
    private ExpStyle  _expStyle = ExpStyle.None;

    // A pattern constant to indicate format should make guess at decimal places
    protected static String NULL_PATTERN = "";

    // Constant for upper value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_UPPER_START = 10000;

    // Constant for lower value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_LOWER_START = .001;

    // Constant for exponent string
    private static final String EXPONENT_STRING = "x10^";

    // Constants for Tick Label exponent style
    public enum ExpStyle { None, Scientific, Financial }

    /**
     * Constructor.
     */
    public NumberFormat(String aPattern)
    {
        _pattern = aPattern != null ? aPattern : NULL_PATTERN;
    }

    /**
     * Constructor.
     */
    public NumberFormat(String aPattern, ExpStyle anExpStyle)
    {
        this(aPattern);
        _expStyle = anExpStyle;
    }

    /**
     * Returns whether pattern is set.
     */
    public boolean isPatternSet()
    {
        String pattern = getPattern();
        return pattern != NULL_PATTERN;
    }

    /**
     * Returns the format pattern.
     */
    public String getPattern()  { return _pattern; }

    /**
     * Sets the format pattern.
     */
    public void setPattern(String aPattern)
    {
        if (Objects.equals(aPattern, _pattern)) return;
        _pattern = aPattern;
        _format = null;
    }

    /**
     * Returns the DecimalFormat.
     */
    protected DecimalFormat getFormat()
    {
        // If already set, just return
        if (_format != null) return _format;

        // Get pattern, get format, set and return
        String pattern = getPattern();
        DecimalFormat format = FormatUtils.getDecimalFormat(pattern);
        return _format = format;
    }

    /**
     * Returns the exponent style.
     */
    public ExpStyle getExpStyle()  { return _expStyle; }

    /**
     * Sets the exponent style.
     */
    protected void setExpStyle(ExpStyle anExpStyle)
    {
        _expStyle = anExpStyle;
    }

    /**
     * Returns a formatted value.
     */
    public String format(Object anObj)
    {
        double value = SnapUtils.doubleValue(anObj);
        return format(value);
    }

    /**
     * Returns a formatted value.
     */
    public String format(double aValue)
    {
        // Handle ExpStyle
        switch (_expStyle) {

            // Handle Financial
            case Financial: return formatFinancial(aValue);

            // Handle Scientific
            case Scientific: return formatScientific(aValue);

            // Handle None
            default: return formatBasicDecimal(aValue);
        }
    }

    /**
     * Returns a formatted value.
     */
    private String formatBasicDecimal(double aValue)
    {
        // If no pattern, format for best guess
        if (!isPatternSet())
            return FormatUtils.formatNum(aValue);

        // Return formatted value
        DecimalFormat format = getFormat();
        try {
            return format.format(aValue);
        }

        // TeaVM 0.6.0 threw an exception here
        catch (RuntimeException e) {
            System.err.println("Failed to format with: " + format.toPattern() + ", value: " + aValue);
            return FormatUtils.formatNum(aValue);
        }
    }

    /**
     * Does format with financial exponents (k, M, B, T).
     */
    private String formatFinancial(double aValue)
    {
        // Get absolute value
        double absVal = Math.abs(aValue);
        long divisor = 1;
        String suffix = "";

        // Handle case of value in the trillions
        if (absVal >= 1000000000000L) {
            divisor = 1000000000000L;
            suffix = "T";
        }

        // Handle case of value in the billions
        else if (absVal >= 1000000000) {
            divisor = 1000000000;
            suffix = "B";
        }

        // Handle case of value in the millions
        else if (absVal >= 1000000) {
            divisor = 1000000;
            suffix = "M";
        }

        // Handle case of value in the thousands
        else if (absVal >= 1000) {
            divisor = 1000;
            suffix = "k";
        }

        // Handle case of value in the thousands
        String valStr = formatBasicDecimal(aValue / divisor);
        return valStr + suffix;
    }

    /**
     * Does format with Scientific notation.
     */
    private String formatScientific(double aValue)
    {
        // Zero is special case, just format as basic decimal
        if (aValue == 0)
            return formatBasicDecimal(0);

        // Get exponent for value
        int exp = getExponentForValue(aValue);

        // If exponent is zero, just format as basic decimal
        if (exp == 0)
            return formatBasicDecimal(aValue);

        // Format value for exponent
        double baseValue = aValue / Math.pow(10, exp);
        String baseStr = formatBasicDecimal(baseValue);
        String expStr = baseStr + EXPONENT_STRING + exp;
        return expStr;
    }

    /**
     * Returns the exponent for given value.
     */
    protected int getExponentForValue(double aValue)
    {
        // If value in reasonable range, just format as decimal (if no more than 3 zeros of magnitude in value)
        if (aValue < SCI_UPPER_START && aValue >= SCI_LOWER_START)
            return 0;

        // Calculate exponent from magnitude of maxValue (if negative, bump by -1 so 1 <= maxValue <= 10)
        double logValue = Math.log10(aValue);
        int exp = (int) logValue;
        if (logValue < 0 && logValue != (int) logValue)
            exp--;

        // Return exponent
        return exp;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "NumberFormat { Format='" + _pattern + '\'' +
                ", ExpStyle=" + _expStyle +
                '}';
    }

    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        return null;
    }

    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        return null;
    }
}
