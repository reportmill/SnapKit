/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.Map;

import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.SnapUtils;

/**
 * A class to hold style attributes for a text run.
 */
public class TextStyle implements Cloneable {

    // The font
    private Font  _font = Font.Arial12;
    
    // The color
    private Color  _color = Color.BLACK;
    
    // Underline style
    private int  _underline;
    
    // The scripting
    private int  _scripting;
    
    // The char spacing
    private double  _charSpacing;
    
    // The link
    private TextLink  _link;
    
    // The format
    private TextFormat  _format;
    
    // The text border (outline)
    private Border  _border;
    
    // Constants for style attribute keys
    public static final String FONT_KEY = "Font";
    public static final String COLOR_KEY = "Color";
    public static final String UNDERLINE_KEY = "Underline";
    public static final String BORDER_KEY = "Border";
    public static final String SCRIPTING_KEY = "Scripting";
    public static final String CHAR_SPACING_KEY = "CharSpacing";
    public static final String LINK_KEY = "Link";
    public static final String FORMAT_KEY = "Format";
    
    // Constants for default text styles
    public static final TextStyle DEFAULT = new TextStyle();
    public static final TextStyle MONOSPACE_DEFAULT = new TextStyle(new Font("Consolas", 12));

    // Constant for Link attributes
    private static final TextStyle DEFAULT_LINK_STYLE = DEFAULT.copyFor(COLOR_KEY, Color.BLUE).copyFor(UNDERLINE_KEY, 1);

    /**
     * Creates a new TextStyle.
     */
    public TextStyle()  { }

    /**
     * Creates a new TextStyle for given attributes.
     */
    public TextStyle(Object ... theAttrs)
    {
        for (Object obj : theAttrs) {
            String key = getStyleKey(obj);
            if (key!=null)
                setValue(key, obj);
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
    public double getCharAdvance(char aChar)  { return getFont().charAdvance(aChar); }

    /**
     * Returns the max distance above the baseline for this run font.
     */
    public double getAscent()  { return getFont().getAscent(); }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()  { return getFont().getDescent(); }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()  { return getFont().getLeading(); }

    /**
     * Returns the line height.
     */
    public double getLineHeight()  { return getFont().getLineHeight(); }

    /**
     * Returns the line advance.
     */
    public double getLineAdvance()  { return getFont().getLineAdvance(); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        TextStyle other = anObj instanceof TextStyle ? (TextStyle)anObj : null; if (other==null) return false;
        if (!SnapUtils.equals(other.getFont(), getFont())) return false;
        if (!SnapUtils.equals(other.getColor(), getColor())) return false;
        if (!SnapUtils.equals(other.getFormat(), getFormat())) return false;
        if (other._underline!=_underline) return false;
        if (other._scripting!=_scripting) return false;
        if (other._charSpacing!=_charSpacing) return false;
        if (!SnapUtils.equals(other.getBorder(), getBorder())) return false;
        if (!SnapUtils.equals(other.getLink(), getLink())) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()  { return getFont()!=null ? getFont().hashCode() : 0; }

    /**
     * Standard clone implementation.
     */
    public TextStyle clone()
    {
        TextStyle clone = null; try { clone = (TextStyle)super.clone(); }
        catch(CloneNotSupportedException e) { }
        return clone;
    }

    /**
     * Clone with new value.
     */
    public TextStyle copyFor(Object anObj)
    {
        String key = getStyleKey(anObj);
        return key!=null ? copyFor(key,anObj) : this;
    }

    /**
     * Clone with new values.
     */
    public TextStyle copyFor(Object ... theObjs)
    {
        if (theObjs==null || theObjs.length==0) return this;
        TextStyle clone = clone();
        for (Object obj : theObjs) {
            String key = getStyleKey(obj);
            if (key!=null) clone.setValue(key, obj);
        }
        return clone;
    }

    /**
     * Clone with key/value.
     */
    public TextStyle copyFor(String aKey, Object aValue)
    {
        TextStyle clone = clone(); clone.setValue(aKey, aValue); return clone;
    }

    /**
     * Clone with map.
     */
    public TextStyle copyFor(Map<?,?> aMap)
    {
        TextStyle clone = clone();
        for (Map.Entry entry : aMap.entrySet())
            clone.setValue((String)entry.getKey(), entry.getValue());
        return clone;
    }

    /**
     * Clone with key/value.
     */
    protected void setValue(String aKey, Object aValue)
    {
        if (aKey.equals(FONT_KEY))
            _font = (Font) aValue;
        else if (aKey.equals(COLOR_KEY))
            _color = (Color) aValue;
        else if (aKey.equals(UNDERLINE_KEY))
            _underline = SnapUtils.intValue(aValue);
        else if (aKey.equals(SCRIPTING_KEY))
            _scripting = SnapUtils.intValue(aValue);
        else if (aKey.equals(CHAR_SPACING_KEY))
            _charSpacing = SnapUtils.doubleValue(aValue);
        else if (aKey.equals(FORMAT_KEY))
            _format = (TextFormat)aValue;
        else if (aKey.equals(BORDER_KEY))
            _border = (Border)aValue;

        // Handle LINK_KEY special: Set DEFAULT_LINK_STYLE attributes
        else if (aKey.equals(LINK_KEY)) {
            _link = (TextLink) aValue;
            _color = DEFAULT_LINK_STYLE.getColor();
            _underline = DEFAULT_LINK_STYLE.getUnderlineStyle();
        }

        // Handle unknown key: Complain
        else System.err.println("TextStyle: Unknown key: " + aKey);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("TextStyle { ");
        sb.append("Font=").append(getFont());
        sb.append(", Color=").append(getColor());
        if (getLink()!=null) sb.append(", Link=").append(getLink().getString());
        sb.append(" }");
        return sb.toString();
    }

    /**
     * Returns the most likely style key for a given attribute.
     */
    public static String getStyleKey(Object anAttr)
    {
        if (anAttr instanceof Font) return FONT_KEY;
        if (anAttr instanceof Color) return COLOR_KEY;
        if (anAttr instanceof TextFormat) return FORMAT_KEY;
        if (anAttr instanceof TextLink) return LINK_KEY;
        if (anAttr instanceof Border) return BORDER_KEY;
        System.out.println("TextStyle.getStyleKey: Unknown key for " + (anAttr!=null ? anAttr.getClass() : null));
        return null;
    }
}