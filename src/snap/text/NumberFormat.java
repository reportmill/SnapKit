/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.props.PropSet;
import snap.props.PropObject;
import snap.util.*;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A class to format tick labels.
 */
public class NumberFormat extends PropObject implements TextFormat, Cloneable {

    // The format pattern
    protected String  _pattern;

    // The DecimalFormat
    protected DecimalFormat  _format;

    // The Exponent Style
    private ExpStyle  _expStyle = DEFAULT_EXP_STYLE;

    // Constant for upper value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_UPPER_START = 10000;

    // Constant for lower value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_LOWER_START = .001;

    // Constant for exponent string
    private static final String EXPONENT_STRING = "x10^";

    // A pattern constant to indicate no pattern is set and format should make guess at decimal places
    protected static String NULL_PATTERN = "";

    // Constants for Tick Label exponent style
    public enum ExpStyle { None, Scientific, Financial }

    // Constants for properties
    public static final String Pattern_Prop = "Pattern";
    public static final String ExpStyle_Prop = "ExpStyle";

    // Constants for property defaults
    private static final ExpStyle DEFAULT_EXP_STYLE = ExpStyle.None;

    // A default instance
    public static final NumberFormat DEFAULT = new NumberFormat(null);

    /**
     * Constructor.
     */
    public NumberFormat()
    {
        this(null);
    }

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
        // If already set, just return
        if (Objects.equals(aPattern, _pattern)) return;

        // If null or NULL_PATTERN, use official NULL_PATTERN
        if (aPattern == null || aPattern.equals(NULL_PATTERN))
            aPattern = NULL_PATTERN;

        // Set value, firePropChange, clear Format
        firePropChange(Pattern_Prop, _pattern, _pattern = aPattern);
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
        if (anExpStyle == _expStyle) return;
        firePropChange(ExpStyle_Prop, _expStyle, _expStyle = anExpStyle);
    }

    /**
     * Returns a formatted value.
     */
    public String format(Object anObj)
    {
        double value = Convert.doubleValue(anObj);
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
     * Copy object for given array of property names and values.
     */
    public NumberFormat copyForProps(Object ... theProps)
    {
        NumberFormat clone = clone();
        for (int i = 0; i < theProps.length; i+= 2) {
            String propName = (String) theProps[i];
            Object propValue = theProps[i + 1];
            clone.setPropValue(propName, propValue);
        }
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public NumberFormat clone()
    {
        NumberFormat clone;
        try { clone = (NumberFormat) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
        return clone;
    }

    /**
     * Standard equals implementation.
     */
    @Override
    public boolean equals(Object anObj)
    {
        if (this == anObj) return true;
        if (anObj == null || getClass() != anObj.getClass()) return false;
        NumberFormat other = (NumberFormat) anObj;
        if (!Objects.equals(other._pattern, _pattern)) return false;
        if (other._expStyle != _expStyle) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(_pattern, _expStyle);
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

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Pattern, ExpStyle
        aPropSet.addPropNamed(Pattern_Prop, String.class, NULL_PATTERN);
        aPropSet.addPropNamed(ExpStyle_Prop, ExpStyle.class, DEFAULT_EXP_STYLE);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Pattern, ExpStyle
            case Pattern_Prop: return getPattern();
            case ExpStyle_Prop: return getExpStyle();

            // Handle super class properties (or unknown)
            default: System.err.println("NumberFormat.getPropValue: Unknown prop: " + aPropName); return null;
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Pattern, ExpStyle
            case Pattern_Prop: setPattern(Convert.stringValue(aValue)); break;
            case ExpStyle_Prop: setExpStyle((ExpStyle) aValue); break;

            // Handle super class properties (or unknown)
            default: System.err.println("NumberFormat.setPropValue: Unknown prop: " + aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Create element
        XMLElement e = new XMLElement("NumberFormat");

        // Archive Pattern, ExpStyle
        if (!isPropDefault(Pattern_Prop))
            e.add(Pattern_Prop, getPattern());
        if (!isPropDefault(ExpStyle_Prop))
            e.add(ExpStyle_Prop, getExpStyle());

        // Return XML
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Pattern, ExpStyle
        if (anElement.hasAttribute(Pattern_Prop))
            setPattern(anElement.getAttributeValue(Pattern_Prop));
        if (anElement.hasAttribute(ExpStyle_Prop))
            setExpStyle(anElement.getAttributeEnumValue(ExpStyle_Prop, ExpStyle.class, DEFAULT_EXP_STYLE));

        // Return this NumberFormat
        return this;
    }

    /**
     * Returns a NumberFormat or null.
     */
    public static NumberFormat getFormat(TextFormat aFormat)
    {
        return aFormat instanceof NumberFormat ? (NumberFormat) aFormat : null;
    }

    /**
     * Returns a NumberFormat or DEFAULT.
     */
    public static NumberFormat getFormatOrDefault(TextFormat aFormat)
    {
        return aFormat instanceof NumberFormat ? (NumberFormat) aFormat : DEFAULT;
    }
}
