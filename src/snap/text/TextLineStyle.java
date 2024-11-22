/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.Arrays;
import snap.geom.HPos;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.*;

/**
 * A class to represent a line of text (for each newline) in RichText.
 */
public class TextLineStyle extends PropObject implements Cloneable, XMLArchiver.Archivable {

    // Horizontal text alignment
    private HPos _align;

    // Whether text in line should be justified
    private boolean _justify;

    // Indentation for first line of paragraph
    private double _firstIndent;

    // Indention for whole paragraph
    private double _leftIndent;

    // Indentation for right margin
    private double _rightIndent;

    // Space between lines expressed as a constant in points
    private double _spacing;

    // Space between lines expressed as a factor of the current line height
    private double _spacingFactor;

    // Spacing after a newline character
    private double _newlineSpacing;

    // Min line height
    private double _minHeight;

    // Max line height
    private double _maxHeight;

    // Tab stops
    private double[] _tabs;

    // Tab stop types
    private char[] _tabTypes;

    // Constants for tab types
    public static final char TAB_LEFT = 'L';
    public static final char TAB_RIGHT = 'R';
    public static final char TAB_CENTER = 'C';
    public static final char TAB_DECIMAL = 'D';

    // Constants for LineStyle keys
    public static final String Align_Prop = "Align";
    public static final String Justify_Prop = "Justify";
    public static final String FirstIndent_Prop = "FirstIndent";
    public static final String LeftIndent_Prop = "LeftIndent";
    public static final String RightIndent_Prop = "RightIndent";
    public static final String Spacing_Prop = "Spacing";
    public static final String SpacingFactor_Prop = "SpacingFactor";
    public static final String NewlineSpacing_Prop = "NewlineSpacing";
    public static final String MinHeight_Prop = "MinHeight";
    public static final String MaxHeight_Prop = "MaxHeight";

    // The System default line style
    public static final TextLineStyle DEFAULT = new TextLineStyle();
    public static final TextLineStyle DEFAULT_CENTERED = DEFAULT.copyForAlign(HPos.CENTER);

    // Constants for defaults
    private static final HPos DEFAULT_ALIGN = HPos.LEFT;
    private static final double DEFAULT_SPACING_FACTOR = 1;
    private static final double DEFAULT_MAX_HEIGHT = Float.MAX_VALUE;
    private static double[] DEFAULT_TABS = { 36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f, 360f, 396f, 432f };
    private static char[] DEFAULT_TAB_TYPES = { 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L' };

    /**
     * Constructor.
     */
    public TextLineStyle()
    {
        super();
        _align = DEFAULT_ALIGN;
        _spacingFactor = DEFAULT_SPACING_FACTOR;
        _maxHeight = DEFAULT_MAX_HEIGHT;
        _tabs = DEFAULT_TABS;
        _tabTypes = DEFAULT_TAB_TYPES;
    }

    /**
     * Returns the alignment associated with this paragraph.
     */
    public HPos getAlign()  { return _align; }

    /**
     * Returns whether text in line should be justified.
     */
    public boolean isJustify()  { return _justify; }

    /**
     * Returns the indentation of first line in paragraph (this can be set different than successive lines).
     */
    public double getFirstIndent()  { return _firstIndent; }

    /**
     * Returns the left side indentation of this paragraph.
     */
    public double getLeftIndent()  { return _leftIndent; }

    /**
     * Returns the right side indentation of this paragraph.
     */
    public double getRightIndent()  { return _rightIndent; }

    /**
     * Returns the spacing between lines expressed as a constant amount in points.
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Returns the spacing of lines expressed as a factor of a given line's height.
     */
    public double getSpacingFactor()  { return _spacingFactor; }

    /**
     * Returns the spacing between paragraphs in printer points associated with this paragraph.
     */
    public double getNewlineSpacing()  { return _newlineSpacing; }

    /**
     * Returns the minimum line height in printer points associated with this paragraph.
     */
    public double getMinHeight()  { return _minHeight; }

    /**
     * Returns the maximum line height in printer points associated with this paragraph.
     */
    public double getMaxHeight()  { return _maxHeight; }

    /**
     * Returns the number of tabs associated with this paragraph.
     */
    public int getTabCount()  { return _tabs.length; }

    /**
     * Returns the specific tab value for the given index in printer points.
     */
    public double getTab(int anIndex)  { return _tabs[anIndex]; }

    /**
     * Returns the type of tab at the given index.
     */
    public char getTabType(int anIndex)  { return _tabTypes[anIndex]; }

    /**
     * Returns the raw tab array
     */
    public double[] getTabs()  { return _tabs; }

    /**
     * Returns the raw tab type array
     */
    public char[] getTabTypes()  { return _tabTypes; }

    /**
     * Returns the tab location for given location.
     */
    public double getXForTabForX(double aX)
    {
        int tabIndex = getTabIndexForX(aX);
        return tabIndex >= 0 ? getTab(tabIndex) : aX;
    }

    /**
     * Returns the tab index for the given location.
     */
    public int getTabIndexForX(double aX)
    {
        // Iterate over tabs until we find one greater than given location
        for (int i = 0, iMax = getTabCount(); i < iMax; i++)
            if (getTab(i) > aX)
                return i;

        // Return not found
        return -1;
    }

    /**
     * Returns the values of all the tabs associated with this paragraph as a comma separated string.
     */
    public String getTabsString()
    {
        // Iterate over tabs and add string rep to StringBuffer
        StringBuilder sb = new StringBuilder();
        for (int i = 0, iMax = _tabs.length; i < iMax; i++) {
            if (_tabs[i] == (int) _tabs[i]) // If tab is really int, append value as int
                sb.append((int) _tabs[i]);  // Otherwise append value as double
            else sb.append(_tabs[i]);
            if (_tabTypes[i] != TAB_LEFT) // If tab is not left tab, append type
                sb.append(_tabTypes[i]);
            if (i + 1 < iMax) // If not end of tabs, append comma
                sb.append(',');
        }

        // Return tabs string
        return sb.toString();
    }

    /**
     * Sets the value of tabs from the given tabs string.
     */
    protected void setTabsString(String aString)
    {
        // Get individual tab strings
        String[] tabs = aString.split("\\s*\\,\\s*");
        if (tabs.length == 1 && tabs[0].isEmpty())
            tabs = new String[0];

        // Create tabs and types arrays
        _tabs = new double[tabs.length];
        _tabTypes = new char[tabs.length];

        // Iterate over tabs and set individual doubles and types
        for (int i = 0, iMax = tabs.length; i < iMax; i++) {
            _tabs[i] = Convert.doubleValue(tabs[i]);
            char type = tabs[i].charAt(tabs[i].length() - 1);
            _tabTypes[i] = Character.isLetter(type) ? type : TAB_LEFT;
        }
    }

    /**
     * Returns a copy with given align value.
     */
    public TextLineStyle copyForAlign(HPos anObj)  { return copyForPropKeyValue(Align_Prop, anObj); }

    /**
     * Returns a copy with given value for given property name.
     */
    public TextLineStyle copyForPropKeyValue(String aKey, Object aValue)
    {
        TextLineStyle clone = clone();
        clone.setPropValue(aKey, aValue);
        return clone;
    }

    /**
     * Returns a paragraph identical to the receiver, but with the given indentation values.
     */
    public TextLineStyle copyForIndents(double firstIndent, double leftIndent, double rightIndent)
    {
        TextLineStyle ls = copyForPropKeyValue(TextLineStyle.FirstIndent_Prop, firstIndent);
        ls = ls.copyForPropKeyValue(TextLineStyle.LeftIndent_Prop, leftIndent);
        ls = ls.copyForPropKeyValue(TextLineStyle.RightIndent_Prop, rightIndent);
        return ls;
    }

    /**
     * Standard clone implementation.
     */
    public TextLineStyle clone()
    {
        TextLineStyle clone;
        try { clone = (TextLineStyle) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }
        return clone;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (this == anObj) return true;
        TextLineStyle other = anObj instanceof TextLineStyle ? (TextLineStyle) anObj : null;
        if (other == null) return false;
        if (other._align != _align) return false;
        if (other._justify != _justify) return false;
        if (other._firstIndent != _firstIndent) return false;
        if (other._leftIndent != _leftIndent) return false;
        if (other._rightIndent != _rightIndent) return false;
        if (other._spacing != _spacing) return false;
        if (other._spacingFactor != _spacingFactor) return false;
        if (other._newlineSpacing != _newlineSpacing) return false;
        if (other._minHeight != _minHeight) return false;
        if (other._maxHeight != _maxHeight) return false;
        if (!Arrays.equals(other._tabs, _tabs)) return false;
        if (!Arrays.equals(other._tabTypes, _tabTypes)) return false;
        return true;
    }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Align, Justify, FirstIndent, RightIndent, Spacing, SpacingFactor, NewlineSpacing, MinHeight, MaxHeight
        aPropSet.addPropNamed(Align_Prop, HPos.class, DEFAULT_ALIGN);
        aPropSet.addPropNamed(Justify_Prop, boolean.class, false);
        aPropSet.addPropNamed(FirstIndent_Prop, double.class, 0d);
        aPropSet.addPropNamed(LeftIndent_Prop, double.class, 0d);
        aPropSet.addPropNamed(RightIndent_Prop, double.class, 0d);
        aPropSet.addPropNamed(Spacing_Prop, double.class, 0d);
        aPropSet.addPropNamed(SpacingFactor_Prop, double.class, DEFAULT_SPACING_FACTOR);
        aPropSet.addPropNamed(NewlineSpacing_Prop, double.class, 0d);
        aPropSet.addPropNamed(MinHeight_Prop, double.class, 0d);
        aPropSet.addPropNamed(MaxHeight_Prop, double.class, DEFAULT_MAX_HEIGHT);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Align, Justify, FirstIndent, LeftIndent, RightIndent, Spacing, SpacingFactor, NewlineSpacing, MinHeight, MaxHeight
            case Align_Prop: return getAlign();
            case Justify_Prop: return isJustify();
            case FirstIndent_Prop: return getFirstIndent();
            case LeftIndent_Prop: return getLeftIndent();
            case RightIndent_Prop: return getRightIndent();
            case Spacing_Prop: return getSpacing();
            case SpacingFactor_Prop: return getSpacingFactor();
            case NewlineSpacing_Prop: return getNewlineSpacing();
            case MinHeight_Prop: return getMinHeight();
            case MaxHeight_Prop: return getMaxHeight();

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

            // Align, Justify, FirstIndent, LeftIndent, RightIndent, Spacing, SpacingFactor, NewlineSpacing, MinHeight, MaxHeight
            case Align_Prop: _align = HPos.of(aValue); _justify = false; break;
            case Justify_Prop: _justify = Convert.boolValue(aValue); break;
            case FirstIndent_Prop: _firstIndent = Convert.doubleValue(aValue); break;
            case LeftIndent_Prop: _leftIndent = Convert.doubleValue(aValue); break;
            case RightIndent_Prop: _rightIndent = Convert.doubleValue(aValue); break;
            case Spacing_Prop: _spacing = Convert.doubleValue(aValue); break;
            case SpacingFactor_Prop: _spacingFactor = Convert.doubleValue(aValue); break;
            case NewlineSpacing_Prop: _newlineSpacing = Convert.doubleValue(aValue); break;
            case MinHeight_Prop: _minHeight = Convert.doubleValue(aValue); break;
            case MaxHeight_Prop: _maxHeight = Convert.doubleValue(aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = "LineStyle { Align=" + _align;
        if (_justify) str += ", Justify=true";
        if (_firstIndent != 0) str += ", FirstIndent=" + StringUtils.toString(_firstIndent);
        if (_leftIndent != 0) str += ", LeftIndent=" + StringUtils.toString(_leftIndent);
        if (_rightIndent != 0) str += ", RightIndent=" + StringUtils.toString(_rightIndent);
        if (_spacing != 0) str += ", Spacing=" + StringUtils.toString(_spacing);
        return str + " }";
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element named pgraph
        XMLElement e = new XMLElement("pgraph");

        // Archive AlignX, FirstIndent, LeftIndent, RightIndent
        String astr = _justify ? "full" : _align.toString().toLowerCase();
        if (!astr.equals("left")) e.add("align", astr);
        if (_firstIndent != _leftIndent) {
            e.add("FirstIndent", _firstIndent);
            e.add("left-indent-0", _firstIndent);
        }
        if (_leftIndent != 0) e.add("left-indent", _leftIndent);
        if (_rightIndent != 0) e.add("right-indent", _rightIndent);

        // Archive Spacing, SpacingFactor, LineHeightMin, LineHeightMax, ParagraphSpacing
        if (_spacing != 0) e.add("line-gap", _spacingFactor);
        if (_spacingFactor != 1) e.add("line-space", _spacingFactor);
        if (_minHeight != 0) e.add("min-line-ht", _minHeight);
        if (_maxHeight != Float.MAX_VALUE) e.add("max-line-ht", _maxHeight);
        if (_newlineSpacing != 0) e.add("pgraph-space", _newlineSpacing);

        // Archive Tabs
        if (!Arrays.equals(_tabs, DEFAULT_TABS) || !Arrays.equals(_tabTypes, DEFAULT_TAB_TYPES))
            e.add("tabs", getTabsString());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public TextLineStyle fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive AlignX, FirstIndent, LeftIndent, RightIndent
        String astr = anElement.getAttributeValue("align", "left");
        if (astr.equals("full")) _justify = true;
        else _align = HPos.get(astr);
        if (anElement.hasAttribute("FirstIndent")) _firstIndent = anElement.getAttributeDoubleValue("FirstIndent");
        else if (anElement.hasAttribute("left-indent-0"))
            _firstIndent = anElement.getAttributeFloatValue("left-indent-0");
        _leftIndent = anElement.getAttributeFloatValue("left-indent");
        _rightIndent = anElement.getAttributeFloatValue("right-indent");

        // Archive Spacing, SpacingFactor, LineHeightMin, LineHeightMax, ParagraphSpacing
        _spacing = anElement.getAttributeFloatValue("line-gap");
        _spacingFactor = anElement.getAttributeFloatValue("line-space", 1);
        _minHeight = anElement.getAttributeFloatValue("min-line-ht");
        _maxHeight = anElement.getAttributeFloatValue("max-line-ht", Float.MAX_VALUE);
        _newlineSpacing = anElement.getAttributeFloatValue("pgraph-space");

        // Unarchive Tabs
        if (anElement.hasAttribute("tabs"))
            setTabsString(anElement.getAttributeValue("tabs"));

        // Return paragraph
        return this;
    }
}