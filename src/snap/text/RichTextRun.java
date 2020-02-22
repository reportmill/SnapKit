/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;

/**
 * The Run class represents a range of characters in an TextLine that share common attributes.
 * 
 * This class makes a point to treat its attributes map as read-only so they can be shared among multiple runs.
 */
public class RichTextRun implements Cloneable, CharSequence {
    
    // The characters in this line
    StringBuffer   _sb = new StringBuffer();
    
    // The attributes of the Run
    TextStyle      _style = TextStyle.DEFAULT;
    
    // The index of this run in line
    int            _index;
    
    // The start char index of this run in line
    int            _start;
    
    // The width of run
    double         _width = -1;
    
/**
 * Returns the string for this run.
 */
public String getString()  { return _sb.toString(); }

/**
 * Returns the length in characters for this run.
 */
public int length()  { return _sb.length(); }

/**
 * CharSequence method returning character at given index.
 */
public char charAt(int anIndex)  { return _sb.charAt(anIndex); }

/**
 * CharSequence method return character sequence for range.
 */
public CharSequence subSequence(int aStart, int anEnd) { return _sb.subSequence(aStart, anEnd); }

/**
 * Returns the run style.
 */
public TextStyle getStyle()  { return _style; }

/**
 * Sets the run style.
 */
protected void setStyle(TextStyle aStyle)  { _style = aStyle; _width = -1; }

/**
 * Returns the font for this run.
 */
public Font getFont()  { return getStyle().getFont(); }

/**
 * Returns the color for this run.
 */
public Color getColor()  { return getStyle().getColor(); }

/**
 * Returns the format for this run.
 */
public TextFormat getFormat()  { return getStyle().getFormat(); }

/**
 * Returns the border for this run.
 */
public Border getBorder()  { return getStyle().getBorder(); }

/**
 * Returns whether this run is underlined.
 */
public boolean isUnderlined()  { return getStyle().isUnderlined(); }

/**
 * Returns the underline style of this run.
 */
public int getUnderlineStyle()  { return getStyle().getUnderlineStyle(); }
    
/**
 * Returns the scripting for this run (1=SuperScripting, -1=Subscripting, 0=none).
 */
public int getScripting()  { return getStyle().getScripting(); }

/**
 * Returns the char spacing.
 */
public float getCharSpacing()  { return (float)getStyle().getCharSpacing(); }

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
 * Returns the line advance.
 */
public double getLineAdvance()  { return getAscent() + getDescent() + getLeading(); }

/**
 * Returns the run index.
 */
public int getIndex()  { return _index; }

/**
 * Returns the start character index for this run.
 */
public int getStart()  { return _start; }

/**
 * Returns the end character index for this run.
 */
public int getEnd()  { return _start + _sb.length(); }

/**
 * Returns the width of run.
 */
public double getWidth()
{
    if(_width>=0) return _width; int len = length(); while(len-1>0 && Character.isWhitespace(charAt(len-1))) len--;
    _width = 0; for(int i=0;i<len;i++) _width += _style.getCharAdvance(charAt(i));
    if(len>1) _width += (len-1)*getCharSpacing();
    return _width;
}

/**
 * Returns the width of run from given index.
 */
public double getWidth(int anIndex)
{
    if(anIndex<=0) return getWidth();
    double width = 0; int len = length(); while(len-1>0 && Character.isWhitespace(charAt(len-1))) len--;
    for(int i=anIndex;i<len;i++) width += _style.getCharAdvance(charAt(i));
    if(len-anIndex>1) width += (len-anIndex-1)*getCharSpacing();
    return width;
}

/**
 * Insets chars at index.
 */
protected void insert(int anIndex, CharSequence theChars)  { _sb.insert(anIndex,theChars); _width = -1; }

/**
 * Deletes chars from index.
 */
protected void delete(int aStart, int anEnd)  { _sb.delete(aStart,anEnd); _width = -1; }

/**
 * Returns whether this run is equal to the given object.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other
    if(anObj==this) return true;
    RichTextRun other = anObj instanceof RichTextRun? (RichTextRun)anObj : null; if(other==null) return false;
    
    // Check StringBuffer and Style
    if(other.length()!=length()) return false;
    for(int i=0, iMax=_sb.length(); i<iMax; i++) // Can't just do SB.equals()
        if(other._sb.charAt(i)!=_sb.charAt(i)) return false;
    if(!other.getStyle().equals(getStyle())) return false;
    return true;  // Return true since all checks passed
}

/**
 * Standard hashCode implementation.
 */
public int hashCode()  { return length(); }

/**
 * Returns a basic clone of this object.
 */
public RichTextRun clone()
{
    RichTextRun clone = null; try { clone = (RichTextRun)super.clone(); } catch(CloneNotSupportedException e) { }
    clone._sb = new StringBuffer(_sb); return clone;
}

/**
 * Returns a string representation of this run.
 */
public String toString()
{
    String string = getString(); string = string.replace("\n", "\\n");
    return getClass().getSimpleName() + "(" + getStart() + "," + getEnd() + "): " + string;
}

}