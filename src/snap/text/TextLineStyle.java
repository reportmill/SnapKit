/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.Arrays;

import snap.geom.HPos;
import snap.util.*;

/**
 * A class to represent a line of text (for each newline) in RichText.
 */
public class TextLineStyle implements Cloneable, XMLArchiver.Archivable {

    // Horizontal text alignment
    HPos _align = HPos.LEFT;
    
    // Whether text in line should be justified
    boolean              _justify;
    
    // Indentation for first line of paragraph
    double               _firstIndent = 0;
    
    // Indention for whole paragraph
    double               _leftIndent = 0;
    
    // Indentation for right margin
    double               _rightIndent = 0;
    
    // Space between lines expressed as a constant in points
    double               _spacing = 0;
    
    // Space between lines expressed as a factor of the current line height
    double               _spacingFactor = 1;
    
    // Spacing after a newline character
    double               _newlineSpacing = 0;
    
    // Min line height
    double               _minHeight = 0;
    
    // Max line height
    double               _maxHeight = Float.MAX_VALUE;
    
    // Tab stops
    double               _tabs[] = _defaultTabs;
    
    // Tab stop types
    char                 _tabTypes[] = _defaultTypes;
    
    // Default tab positions
    static double _defaultTabs[] = { 36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f, 360f, 396f, 432f };
    
    // Default tab types
    static char  _defaultTypes[] = { 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L' };

    // Constants for tab types
    public static final char TAB_LEFT = 'L';
    public static final char TAB_RIGHT = 'R';
    public static final char TAB_CENTER = 'C';
    public static final char TAB_DECIMAL = 'D';
    
    // Constants for LineStyle keys
    public static final String ALIGN_KEY = "Align";
    public static final String JUSTIFY_KEY = "Justify";
    public static final String FIRST_INDENT_KEY = "FirstIndent";
    public static final String LEFT_INDENT_KEY = "LeftIndent";
    public static final String RIGHT_INDENT_KEY = "RightIndent";
    public static final String SPACING_KEY = "Spacing";
    public static final String SPACING_FACTOR_KEY = "SpacingFactor";
    public static final String NEWLINE_SPACING_KEY = "NewlineSpacing";
    public static final String MIN_HEIGHT_KEY = "MinHeight";
    public static final String MAX_HEIGHT_KEY = "MaxHeight";
    
    // The System default line style
    public static final TextLineStyle DEFAULT = new TextLineStyle();
    public static final TextLineStyle DEFAULT_CENTERED = DEFAULT.copyFor(HPos.CENTER);

/**
 * Creates a new TextLineStyle.
 */
public TextLineStyle()  { }

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
public double getFirstIndent() { return _firstIndent; }

/**
 * Returns the left side indentation of this paragraph.
 */
public double getLeftIndent() { return _leftIndent; }

/**
 * Returns the right side indentation of this paragraph.
 */
public double getRightIndent() { return _rightIndent; }

/**
 * Returns the spacing between lines expressed as a constant amount in points.
 */
public double getSpacing() { return _spacing; }

/**
 * Returns the spacing of lines expressed as a factor of a given line's height.
 */
public double getSpacingFactor() { return _spacingFactor; }

/**
 * Returns the spacing between paragraphs in printer points associated with this paragraph.
 */
public double getNewlineSpacing() { return _newlineSpacing; }

/**
 * Returns the minimum line height in printer points associated with this paragraph.
 */
public double getMinHeight() { return _minHeight; }

/**
 * Returns the maximum line height in printer points associated with this paragraph.
 */
public double getMaxHeight() { return _maxHeight; }

/**
 * Returns the number of tabs associated with this paragraph.
 */
public int getTabCount() { return _tabs.length; }

/**
 * Returns the specific tab value for the given index in printer points.
 */
public double getTab(int anIndex) { return _tabs[anIndex]; }

/**
 * Returns the type of tab at the given index.
 */
public char getTabType(int anIndex) { return _tabTypes[anIndex]; }

/**
 * Returns the raw tab array
 */
public double[] getTabs() { return _tabs; }

/**
 * Returns the raw tab type array
 */
public char[] getTabTypes() { return _tabTypes; }

/**
 * Returns the tab location for given location.
 */
public double getTabForX(double aX)
{
    int tind = getTabIndex(aX);
    return tind>=0? getTab(tind) : aX;
}

/**
 * Returns the tab index for the given location.
 */
public int getTabIndex(double aX)
{
    // Iterate over tabs until we find one greater than given location
    for(int i=0, iMax=getTabCount(); i<iMax; i++)
        if(getTab(i)>aX)
            return i;
    return -1; // If location was greater than all tab stops, return -1
}

/**
 * Returns the values of all the tabs associated with this paragraph as a comma separated string.
 */
public String getTabsString()
{
    // Iterate over tabs and add string rep to StringBuffer
    StringBuffer sb = new StringBuffer();
    for(int i=0, iMax=_tabs.length; i<iMax; i++) {
        if(_tabs[i]==(int)_tabs[i]) sb.append((int)_tabs[i]); // If tab is really int, append value as int
        else sb.append(_tabs[i]);  // Otherwise append value as double
        if(_tabTypes[i]!=TAB_LEFT) sb.append(_tabTypes[i]); // If tab is not left tab, append type
        if(i+1<iMax) sb.append(','); // If not end of tabs, append comma
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
    String tabs[] = aString.split("\\s*\\,\\s*");
    if(tabs.length==1 && tabs[0].length()==0)
        tabs = new String[0];
    
    // Create tabs and types arrays
    _tabs = new double[tabs.length];
    _tabTypes = new char[tabs.length];
    
    // Iterate over tabs and set individual doubles and types
    for(int i=0, iMax=tabs.length; i<iMax; i++) {
        _tabs[i] = SnapUtils.doubleValue(tabs[i]);
        char type = tabs[i].charAt(tabs[i].length()-1);
        _tabTypes[i] = Character.isLetter(type)? type : TAB_LEFT;
    }
}

/**
 * Returns a clone with the new given value.
 */
public TextLineStyle copyFor(Object anObj)
{
    if(anObj instanceof HPos)
        return copyFor(ALIGN_KEY, anObj);
    return this;
}

/**
 * Returns a clone with the new given value.
 */
public TextLineStyle copyFor(String aKey, Object aValue)
{
    TextLineStyle clone = clone(); clone.setValue(aKey,aValue); return clone;
}

/**
 * Returns a paragraph identical to the receiver, but with the given indentation values.
 */
public TextLineStyle copyForIndents(double firstIndent, double leftIndent, double rightIndent)
{
    TextLineStyle ls =  copyFor(TextLineStyle.FIRST_INDENT_KEY, firstIndent);
    ls = ls.copyFor(TextLineStyle.LEFT_INDENT_KEY, leftIndent);
    ls = ls.copyFor(TextLineStyle.RIGHT_INDENT_KEY, rightIndent);
    return ls;
}

/**
 * Sets a value for given key.
 */
protected void setValue(String aKey, Object aValue)
{
    if(aKey.equals(ALIGN_KEY)) { _align = (HPos)aValue; _justify = false; }
    else if(aKey.equals(JUSTIFY_KEY)) _justify = SnapUtils.boolValue(aValue);
    else if(aKey.equals(SPACING_KEY)) _spacing = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(SPACING_FACTOR_KEY)) _spacingFactor = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(NEWLINE_SPACING_KEY)) _newlineSpacing = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(MIN_HEIGHT_KEY)) _minHeight = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(MAX_HEIGHT_KEY)) _maxHeight = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(FIRST_INDENT_KEY)) _firstIndent = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(LEFT_INDENT_KEY)) _leftIndent = SnapUtils.doubleValue(aValue);
    else if(aKey.equals(RIGHT_INDENT_KEY)) _rightIndent = SnapUtils.doubleValue(aValue);
    else System.err.println("TextLineStyle.setValue: Unsupported key: " + aKey);
}

/**
 * Standard clone implementation.
 */
public TextLineStyle clone()
{
    TextLineStyle clone = null; try { clone = (TextLineStyle)super.clone(); }
    catch(Exception e) { throw new RuntimeException(e);}
    return clone;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(this==anObj) return true;
    TextLineStyle other = anObj instanceof TextLineStyle? (TextLineStyle)anObj : null; if(other==null) return false;
    if(other._align!=_align) return false;
    if(other._justify!=_justify) return false;
    if(other._firstIndent!=_firstIndent) return false;
    if(other._leftIndent!=_leftIndent) return false;
    if(other._rightIndent!=_rightIndent) return false;
    if(other._spacing!=_spacing) return false;
    if(other._spacingFactor!=_spacingFactor) return false;
    if(other._newlineSpacing!=_newlineSpacing) return false;
    if(other._minHeight!=_minHeight) return false;
    if(other._maxHeight!=_maxHeight) return false;
    if(!Arrays.equals(other._tabs, _tabs)) return false;
    if(!Arrays.equals(other._tabTypes, _tabTypes)) return false;
    return true;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = "LineStyle { Align=" + _align;
    if(_justify) str += ", Justify=true";
    if(_firstIndent!=0) str += ", FirstIndent=" + StringUtils.toString(_firstIndent);
    if(_leftIndent!=0) str += ", LeftIndent=" + StringUtils.toString(_leftIndent);
    if(_rightIndent!=0) str += ", RightIndent=" + StringUtils.toString(_rightIndent);
    if(_spacing!=0) str += ", Spacing=" + StringUtils.toString(_spacing);
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
    String astr = _justify? "full" : _align.toString().toLowerCase();
    if(!astr.equals("left")) e.add("align", astr);
    if(_firstIndent!=_leftIndent) { e.add("FirstIndent", _firstIndent); e.add("left-indent-0", _firstIndent); }
    if(_leftIndent!=0) e.add("left-indent", _leftIndent);
    if(_rightIndent!=0) e.add("right-indent", _rightIndent);
        
    // Archive Spacing, SpacingFactor, LineHeightMin, LineHeightMax, ParagraphSpacing
    if(_spacing!=0) e.add("line-gap", _spacingFactor);
    if(_spacingFactor!=1) e.add("line-space", _spacingFactor);
    if(_minHeight!=0) e.add("min-line-ht", _minHeight);
    if(_maxHeight!=Float.MAX_VALUE) e.add("max-line-ht", _maxHeight);
    if(_newlineSpacing!=0) e.add("pgraph-space", _newlineSpacing);
        
    // Archive Tabs
    if(!Arrays.equals(_tabs, _defaultTabs) || !Arrays.equals(_tabTypes, _defaultTypes))
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
    if(astr.equals("full")) _justify = true; else _align = HPos.get(astr);
    if(anElement.hasAttribute("FirstIndent")) _firstIndent = anElement.getAttributeDoubleValue("FirstIndent");
    else if(anElement.hasAttribute("left-indent-0")) _firstIndent = anElement.getAttributeFloatValue("left-indent-0");
    _leftIndent = anElement.getAttributeFloatValue("left-indent");
    _rightIndent = anElement.getAttributeFloatValue("right-indent");
    
    // Archive Spacing, SpacingFactor, LineHeightMin, LineHeightMax, ParagraphSpacing
    _spacing = anElement.getAttributeFloatValue("line-gap");
    _spacingFactor = anElement.getAttributeFloatValue("line-space", 1);
    _minHeight = anElement.getAttributeFloatValue("min-line-ht");
    _maxHeight = anElement.getAttributeFloatValue("max-line-ht", Float.MAX_VALUE);
    _newlineSpacing = anElement.getAttributeFloatValue("pgraph-space");
    
    // Unarchive Tabs
    if(anElement.hasAttribute("tabs"))
        setTabsString(anElement.getAttributeValue("tabs"));
    
    // Return paragraph
    return this;
}

}