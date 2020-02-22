/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Color;
import snap.util.SnapUtils;

/**
 * A class to represent a hyperlink in a RichText TextStyle.
 */
public class TextLink {

    // The link string
    String          _string;

/**
 * Creates a new TextLink.
 */
public TextLink()  { }

/**
 * Creates a new TextLink.
 */
public TextLink(String aLink)  { _string = aLink; }

/**
 * Returns the link string.
 */
public String getString()  { return _string; }

/**
 * Sets the link string.
 */
public void setString(String aString)  { _string = aString; }

/**
 * Returns the link color.
 */
public Color getColor()  { return Color.BLUE; }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    TextLink other = (TextLink)anObj;
    if(!SnapUtils.equals(other._string, _string)) return false;
    return true;
}

/**
 * Standard hashCode implementation.
 */
public int hashCode()  { return _string!=null? _string.hashCode() : 0; }

/**
 * Standard toString implementation.
 */
public String toString()  { return "TextLink: " + _string; }

}