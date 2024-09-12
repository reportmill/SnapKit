/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.Map;
import java.util.Objects;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.Convert;

/**
 * A class to hold style attributes for a text run.
 */
public class TextStyle extends PropObject implements Cloneable {

    // The font
    private Font _font = Font.Arial12;

    // The color
    private Color _color = Color.BLACK;

    // Underline style
    private int _underline;

    // The scripting
    private int _scripting;

    // The char spacing
    private double _charSpacing;

    // The link
    private TextLink _link;

    // The format
    private TextFormat _format;

    // The text border (outline)
    private Border _border;

    // Constants for properties
    public static final String Font_Prop = "Font";
    public static final String Color_Prop = "Color";
    public static final String Underline_Prop = "Underline";
    public static final String Border_Prop = "Border";
    public static final String Scripting_Prop = "Scripting";
    public static final String CharSpacing_Prop = "CharSpacing";
    public static final String Format_Prop = "Format";
    public static final String Link_Prop = "Link";

    // Constants for style attribute keys
    public static final String COLOR_KEY = Color_Prop;
    public static final String UNDERLINE_KEY = Underline_Prop;

    // Constants for default text styles
    public static final TextStyle DEFAULT = new TextStyle();

    // Constant for Link attributes
    public static final TextStyle DEFAULT_LINK_STYLE = DEFAULT.copyForStyleString("Color:BLUE; Underline:1;");

    /**
     * Creates a new TextStyle.
     */
    public TextStyle()
    {
    }

    /**
     * Creates a new TextStyle for given attributes.
     */
    public TextStyle(Object... theAttrs)
    {
        for (Object obj : theAttrs) {
            String key = getStyleKey(obj);
            if (key != null)
                setPropValue(key, obj);
        }
    }

    /**
     * Returns the font for this run.
     */
    public Font getFont()  { return _font; }

    /**
     * Returns the color for this run.
     */
    public Color getColor()  { return _color; }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()  { return _underline!=0; }

    /**
     * Returns the underline style of this run.
     */
    public int getUnderlineStyle()  { return _underline; }

    /**
     * Returns the scripting for this run (1=SuperScripting, -1=Subscripting, 0=none).
     */
    public int getScripting()  { return _scripting; }

    /**
     * Returns the char spacing.
     */
    public double getCharSpacing()  { return _charSpacing; }

    /**
     * Returns the link.
     */
    public TextLink getLink()  { return _link; }

    /**
     * Returns the format.
     */
    public TextFormat getFormat()  { return _format; }

    /**
     * Returns the text border.
     */
    public Border getBorder()  { return _border; }

    /**
     * Returns the char advance for a given character.
     */
    public double getCharAdvance(char aChar)  { return _font.charAdvance(aChar); }

    /**
     * Returns the max distance above the baseline for this run font.
     */
    public double getAscent()  { return _font.getAscent(); }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()  { return _font.getDescent(); }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()  { return _font.getLeading(); }

    /**
     * Returns the line height.
     */
    public double getLineHeight()  { return _font.getLineHeight(); }

    /**
     * Returns the line advance.
     */
    public double getLineAdvance()  { return _font.getLineAdvance(); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        TextStyle other = anObj instanceof TextStyle ? (TextStyle) anObj : null;
        if (other == null) return false;
        if (!Objects.equals(other.getFont(), getFont())) return false;
        if (!Objects.equals(other.getColor(), getColor())) return false;
        if (!Objects.equals(other.getFormat(), getFormat())) return false;
        if (other._underline != _underline) return false;
        if (other._scripting != _scripting) return false;
        if (other._charSpacing != _charSpacing) return false;
        if (!Objects.equals(other.getBorder(), getBorder())) return false;
        if (!Objects.equals(other.getLink(), getLink())) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return getFont() != null ? getFont().hashCode() : 0;
    }

    /**
     * Standard clone implementation.
     */
    public TextStyle clone()
    {
        try { return (TextStyle) super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Clone with new value.
     */
    public TextStyle copyFor(Object anObj)
    {
        String key = getStyleKey(anObj);
        return key != null ? copyFor(key, anObj) : this;
    }

    /**
     * Clone with new values.
     */
    public TextStyle copyFor(Object ... theObjs)
    {
        // If no attributes, just return
        if (theObjs == null || theObjs.length == 0) return this;

        // Clone and set attributes
        TextStyle clone = clone();
        for (Object obj : theObjs) {
            String key = getStyleKey(obj);
            if (key != null)
                clone.setPropValue(key, obj);
        }

        // Return
        return clone;
    }

    /**
     * Clone with key/value.
     */
    public TextStyle copyFor(String aKey, Object aValue)
    {
        TextStyle clone = clone();
        clone.setPropValue(aKey, aValue);
        return clone;
    }

    /**
     * Clone with map.
     */
    public TextStyle copyFor(Map<? super Object, ? super Object> aMap)
    {
        TextStyle clone = clone();
        for (Map.Entry<Object,Object> entry : aMap.entrySet())
            clone.setPropValue((String) entry.getKey(), entry.getValue());
        return clone;
    }

    /**
     * Returns a copy of this style for given props string.
     */
    public TextStyle copyForStyleString(String styleString)
    {
        TextStyle clone = clone();
        clone.setPropsString(styleString);
        return clone;
    }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Font, Color, Underline, Border, Scripting, CharSpacing, Format, Link
        aPropSet.addPropNamed(Font_Prop, Font.class, null);
        aPropSet.addPropNamed(Color_Prop, Color.class, null);
        aPropSet.addPropNamed(Underline_Prop, int.class, 0);
        aPropSet.addPropNamed(Border_Prop, Border.class, 0);
        aPropSet.addPropNamed(Scripting_Prop, double.class, 0d);
        aPropSet.addPropNamed(CharSpacing_Prop, double.class, 0d);
        aPropSet.addPropNamed(Format_Prop, TextFormat.class, null);
        aPropSet.addPropNamed(Link_Prop, TextLink.class, null);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Font, Color, Underline, Border, Scripting, CharSpacing, Format, Link
            case Font_Prop: return getFont();
            case Color_Prop: return getColor();
            case Underline_Prop: return getUnderlineStyle();
            case Border_Prop: return getBorder();
            case Scripting_Prop: return getScripting();
            case CharSpacing_Prop: return getCharSpacing();
            case Format_Prop: return getFormat();
            case Link_Prop: return getLink();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the value for given prop name.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Font, Color, Underline, Border, Scripting, CharSpacing, Format, Link
            case Font_Prop: _font = Font.of(aValue); break;
            case Color_Prop: _color = Color.get(aValue); break;
            case Underline_Prop: _underline = Convert.intValue(aValue); break;
            case Border_Prop: _border = Border.of(aValue); break;
            case Scripting_Prop: _scripting = Convert.intValue(aValue); break;
            case CharSpacing_Prop: _charSpacing = Convert.doubleValue(aValue); break;
            case Format_Prop: _format = (TextFormat) aValue; break;
            case Link_Prop:
                _link = TextLink.of(aValue);
                _color = DEFAULT_LINK_STYLE.getColor();
                _underline = DEFAULT_LINK_STYLE.getUnderlineStyle();
                break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Returns the most likely style key for a given attribute.
     */
    public static String getStyleKey(Object anAttr)
    {
        if (anAttr instanceof Font) return Font_Prop;
        if (anAttr instanceof Color) return Color_Prop;
        if (anAttr instanceof TextFormat) return Format_Prop;
        if (anAttr instanceof TextLink) return Link_Prop;
        if (anAttr instanceof Border) return Border_Prop;
        System.out.println("TextStyle.getStyleKey: Unknown key for " + (anAttr != null ? anAttr.getClass() : null));
        return null;
    }

    /**
     * Creates a TextStyle for given prop string.
     */
    public static TextStyle createForPropsString(String propsString)
    {
        TextStyle textStyle = new TextStyle();
        textStyle.setPropsString(propsString);
        return textStyle;
    }
}