/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;

import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.geom.HPos;
import snap.util.*;
import snap.web.*;

/**
 * This class represents a block of text (lines).
 */
public class RichText implements CharSequence, Cloneable, XMLArchiver.Archivable {

    // The Source of the current content
    Object               _source;
    
    // The URL of the file that provided the text
    WebURL               _sourceURL;

    // The TextDocLine in this text
    List <RichTextLine>  _lines = new ArrayList();
    
    // The length of this text
    int                  _length;
    
    // The default text style for this text
    TextStyle            _defStyle = TextStyle.DEFAULT;

    // The default line style for this text
    TextLineStyle        _defLineStyle = TextLineStyle.DEFAULT;

    // Whether text only allows a single font, color, etc.
    boolean              _plainText;
    
    // Whether property change is enabled
    boolean              _propChangeEnabled = true;
    
    // The width of the rich text
    double               _width = -1;

    // The PropChangeSupport
    PropChangeSupport    _pcs = PropChangeSupport.EMPTY;
    
    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";

/**
 * Creates RichText.
 */
public RichText()
{
    addLine(createLine(), 0);
}

/**
 * Creates RichText initialized with given String and attributes (font, color).
 */
public RichText(CharSequence theChars, Object ... theAttrs)
{
    this();
    addCharsWithStyleValues(theChars, theAttrs);
}

/**
 * Returns the source for the current text content.
 */
public Object getSource()  { return _source; }

/**
 * Loads the text from the given source.
 */
public void setSource(Object aSource)
{
    // Declare text/url vars
    String text = null;
    WebURL url;
    
    // Try WebFile
    if(aSource instanceof WebFile) { WebFile file = (WebFile)aSource;
        _source = aSource;
        text = file.getText();
        url = file.getURL();
    }
    
    // Try WebURL
    else {
        url = WebURL.getURL(aSource);
        if(url!=null) {
            _source = aSource;
            text = url.getText();
        }
    }
    
    // Try to get text directly from source 
    if(text==null)
        text = SnapUtils.getText(aSource);
    
    // Set text and source
    setString(text);
    _sourceURL = url;
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _sourceURL; }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { WebURL surl = getSourceURL(); return surl!=null? surl.getFile() : null; }

/**
 * Returns the number of characters in the text.
 */
public int length()  { return _length; }

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)
{
    RichTextLine line = getLineAt(anIndex);
    return line.charAt(anIndex - line.getStart());
}

/**
 * Returns a new char sequence that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd)
{
    StringBuffer sb = new StringBuffer(anEnd - aStart);
    RichTextLine line = getLineAt(aStart);
    while(aStart<anEnd) {
        int end = Math.min(line.getEnd(), anEnd);
        sb.append(line.subSequence(aStart - line.getStart(), end - line.getStart()));
        aStart = end; line = line.getNext();
    }
    return sb;
}

/**
 * Returns the string for the text.
 */
public String getString()
{
    StringBuilder sb = new StringBuilder(length());
    for(RichTextLine line : _lines)
        for(RichTextRun run : line._runs)
            sb.append(run._sb);
    return sb.toString();
}

/**
 * Sets the text to the given string.
 */
public void setString(String aString)
{
    setPropChangeEnabled(false);
    replaceChars(aString, null, 0, length());
    setPropChangeEnabled(true);
}

/**
 * Returns the default style for text.
 */
public TextStyle getDefaultStyle()  { return _defStyle; }

/**
 * Sets the default style.
 */
public void setDefaultStyle(TextStyle aStyle)
{
    _defStyle = aStyle;
    for(RichTextLine line : getLines())
        line.setStyle(aStyle);
}

/**
 * Returns the default line style for text.
 */
public TextLineStyle getDefaultLineStyle()  { return _defLineStyle; }

/**
 * Sets the default line style.
 */
public void setDefaultLineStyle(TextLineStyle aLineStyle)
{
    _defLineStyle = aLineStyle;
    for(RichTextLine line : getLines()) line.setLineStyle(aLineStyle);
}

/**
 * Whether this text is really just plain text (has single font, color, etc.). Defaults to false.
 */
public boolean isPlainText()  { return _plainText; }

/**
 * Sets whether this text really just plain text (has single font, color, etc.).
 */
public void setPlainText(boolean aValue)  { _plainText = aValue; }

/**
 * Adds characters with attributes to this text at given index.
 */
public void addChars(CharSequence theChars)
{
    addChars(theChars, null, length());
}

/**
 * Adds characters with attributes to this text at given index.
 */
public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
{
    // If no chars, just return
    if(theChars==null) return;

    // If monofont, clear attributes
    if(isPlainText()) theStyle = null;

    // Get line for index - if adding at text end and last line and ends with newline, create/add new line
    RichTextLine line = getLineAt(anIndex);
    if(anIndex==line.getEnd() && line.isLastCharNewline()) {
        RichTextLine nline = line.split(line.length());
        addLine(nline, line.getIndex()+1);
        line = nline;
    }
    
    // Add chars line by line
    int start = 0;
    int len = theChars.length();
    int lindex = anIndex - line.getStart();
    while(start<len) {
        
        // Get index of newline in insertion chars (if there) and end of line block
        int newline = StringUtils.indexAfterNewline(theChars, start);
        int end = newline>0? newline : len;
        
        // Get chars and add
        CharSequence chars = start==0 && end==len ? theChars : theChars.subSequence(start, end);
        line.addChars(chars, theStyle, lindex);
        
        // If newline added and there are more chars in line, split line and add remainder
        if(newline>0 && (end<len || lindex+chars.length()<line.length())) {
            RichTextLine remainder = line.split(lindex + chars.length());
            addLine(remainder, line.getIndex()+1);
            line = remainder; lindex = 0;
        }
        
        // Set start to last end
        start = end;
    }
    
    // Send PropertyChange
    if(isPropChangeEnabled())
        firePropChange(new CharsChange(null, theChars, anIndex));
    _width = -1;
}

public void addCharsWithStyleMap(CharSequence theChars, Map<String,Object> theAttrs)
{
    TextStyle style = getStyleAt(length());
    style = style.copyFor(theAttrs);
    addChars(theChars, style, length());
}

/**
 * Appends the given chars with the given attribute(s).
 */
public void addCharsWithStyleValues(CharSequence theChars, Object ... theAttrs)
{
    // Get style at end and get first attribute
    TextStyle style = getStyleAt(length());
    Object attr0 = theAttrs!=null && theAttrs.length>0 ? theAttrs[0] : null;

    // Get modified style for given attributes
    if(attr0 instanceof TextStyle)
        style = (TextStyle)attr0;
    else if(attr0!=null)
        style = style.copyFor(theAttrs);

    // Add chars
    addChars(theChars, style, length());
}

/**
 * Removes characters in given range.
 */
public void removeChars(int aStart, int anEnd)
{
    // If empty range, just return
    if(anEnd==aStart) return;
    
    // If PropChangeEnabled, get chars to be deleted
    CharSequence dchars = isPropChangeEnabled()? subSequence(aStart, anEnd) : null;

    // Delete lines/chars for range
    int end = anEnd;
    while(end>aStart) {
        RichTextLine line = getLineAt(end); if(end==line.getStart()) line = getLine(line.getIndex()-1);
        int lineStart = line.getStart(), start = Math.max(aStart, lineStart);
        if(start==lineStart && end==line.getEnd() && getLineCount()>1)
            removeLine(line.getIndex());
        else {
            line.removeChars(start - lineStart, end - lineStart);
            if(!line.isLastCharNewline() && line.getIndex()+1<getLineCount()) {
                RichTextLine next = removeLine(line.getIndex()+1);
                line.join(next);
            }
        }
        end = lineStart;
    }
    
    // If deleted chars is set, send property change
    if(dchars!=null)
        firePropChange(new CharsChange(dchars, null, aStart));
    _width = -1;
}

/**
 * Replaces chars in given range, with given String, using the given attributes.
 */
public void replaceChars(CharSequence theChars, int aStart, int anEnd)
{
    replaceChars(theChars, null, aStart, anEnd);
}

/**
 * Replaces chars in given range, with given String, using the given attributes.
 */
public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
{
    // Get style and linestyle for add chars
    TextStyle style = theStyle!=null? theStyle : getStyleAt(aStart);
    TextLineStyle lstyle = theChars!=null && theChars.length()>0 && !isPlainText()? getLineStyleAt(aStart) : null;
    
    // Remove given range and add chars
    if(anEnd>aStart) removeChars(aStart, anEnd);
    addChars(theChars, style, aStart);
    
    // Restore linestyle (needed if range includes a newline)
    if(lstyle!=null) setLineStyle(lstyle, aStart, aStart+theChars.length());
}

/**
 * Adds a RichText to this string at given index.
 */
public void addText(RichText aRichText, int anIndex)
{
    for(RichTextLine line : aRichText.getLines()) {
        for(RichTextRun run : line.getRuns()) { int index = anIndex + line.getStart() + run.getStart();
            addChars(run.getString(), run.getStyle(), index);
            setLineStyle(line.getLineStyle(), index, index + run.length());
        }
    }
}

/**
 * Replaces the chars in given range, with given RichText.
 */
public void replaceText(RichText aRichText, int aStart, int anEnd)
{
    if(anEnd>aStart) removeChars(aStart, anEnd);
    addText(aRichText, aStart);
}

/**
 * Sets a given style to a given range.
 */
public void setStyle(TextStyle aStyle, int aStart, int anEnd)
{
    // If single style, set style on all line runs
    if(isPlainText()) {
        TextStyle ostyle = getStyleAt(aStart);
        for(RichTextLine line : _lines)
            line.setStyle(aStyle);
        if(isPropChangeEnabled())
            firePropChange(new StyleChange(ostyle, aStyle, 0, length()));
    }
    
    // Iterate over runs in range and set style
    else while(aStart<anEnd) {
        RichTextLine line = getLineAt(aStart); int lstart = line.getStart();
        RichTextRun run = getRunAt(aStart); TextStyle ostyle = run.getStyle();
        if(aStart-lstart>run.getStart())
            run = line.splitRun(run, aStart-lstart-run.getStart());
        if(anEnd-lstart<run.getEnd())
            line.splitRun(run, anEnd-lstart-run.getStart());
        run.setStyle(aStyle); line._width = -1;
        aStart = run.getEnd() + lstart;
        if(isPropChangeEnabled())
            firePropChange(new StyleChange(ostyle, aStyle, run.getStart()+lstart, run.getEnd()+lstart));
    }
    
    _width = -1;
}

/**
 * Sets a given style value to given value for a given range.
 */
public void setStyleValue(Object aValue)
{
    setStyleValue(aValue, 0, length());
}

/**
 * Sets a given style value to given value for a given range.
 */
public void setStyleValue(Object aValue, int aStart, int aEnd)
{
    String key = TextStyle.getStyleKey(aValue);
    setStyleValue(key, aValue, aStart, aEnd);
}

/**
 * Sets a given attribute to a given value for a given range.
 */
public void setStyleValue(String aKey, Object aValue)
{
    setStyleValue(aKey, aValue, 0, length());
}

/**
 * Sets a given attribute to a given value for a given range.
 */
public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
{
    // If not multifont, set attribute and invalidate everything
    if(isPlainText()) {
        TextStyle style = getStyleAt(aStart).copyFor(aKey, aValue);
        setStyle(style, aStart, anEnd);
    }
    
    // Iterate over lines in range and set attribute
    else while(aStart<anEnd) {
        RichTextLine line = getLineAt(aStart); int lstart = line.getStart();
        RichTextRun run = getRunAt(aStart); int rend = run.getEnd();
        TextStyle style = run.getStyle().copyFor(aKey, aValue);
        setStyle(style, aStart, Math.min(rend + lstart, anEnd));
        aStart = rend + lstart;
    }
}

/**
 * Sets a given style to a given range.
 */
public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
{
    // Handle PlainText
    if(isPlainText()) {
        TextLineStyle ostyle = getLine(0).getLineStyle();
        for(RichTextLine ln : getLines()) ln.setLineStyle(aStyle);
        if(isPropChangeEnabled())
            firePropChange(new LineStyleChange(ostyle, aStyle, 0));
    }
    
    // Handle MultiStyle
    else {
        int sline = getLineAt(aStart).getIndex(), eline = getLineAt(anEnd).getIndex();
        for(int i=sline;i<=eline;i++) { RichTextLine line = getLine(i);
            TextLineStyle ostyle = line.getLineStyle();
            line.setLineStyle(aStyle);
            if(isPropChangeEnabled())
                firePropChange(new LineStyleChange(ostyle, aStyle, i));
        }
    }
    
    _width = -1;
}

/**
 * Sets a given style to a given range.
 */
public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
{
    // Handle PlainText
    if(isPlainText()) {
        TextLineStyle ostyle = getLine(0).getLineStyle();
        TextLineStyle nstyle = ostyle.copyFor(aKey, aValue);
        setLineStyle(nstyle, 0, length());
    }
    
    // Handle MultiStyle
    else {
        int sline = getLineAt(aStart).getIndex(), eline = getLineAt(anEnd).getIndex();
        for(int i=sline;i<=eline;i++) { RichTextLine line = getLine(i);
            TextLineStyle ostyle = line.getLineStyle(), nstyle = ostyle.copyFor(aKey, aValue);
            line.setLineStyle(nstyle);
            if(isPropChangeEnabled())
                firePropChange(new LineStyleChange(ostyle, nstyle, i));
        }
        _width = -1;
    }
}

/**
 * Clears the text.
 */
public void clear()
{
    removeChars(0,length());
    setStyle(getDefaultStyle(),0,0);
    setLineStyle(getDefaultLineStyle(),0,0);
}

/**
 * Returns the number of block in this doc.
 */
public int getLineCount()  { return _lines.size(); }

/**
 * Returns the individual block in this doc.
 */
public RichTextLine getLine(int anIndex)  { return _lines.get(anIndex); }

/**
 * Returns the list of blocks.
 */
public List <RichTextLine> getLines()  { return _lines; }

/**
 * Creates a new block for use in this text.
 */
protected RichTextLine createLine()  { return new RichTextLine(this); }

/**
 * Adds a block at given index.
 */
private void addLine(RichTextLine aLine, int anIndex)
{
    _lines.add(anIndex, aLine); aLine._text = this;
    updateLines(anIndex-1);
}

/**
 * Removes the block at given index.
 */
private RichTextLine removeLine(int anIndex)
{
    RichTextLine line = _lines.remove(anIndex); line._text = null;
    updateLines(anIndex-1);
    return line;
}

/**
 * Updates blocks from index.
 */
protected void updateLines(int anIndex)
{
    // Get BaseLine and length at end of BaseLine
    RichTextLine baseLine = anIndex>=0? getLine(anIndex) : null;
    _length = baseLine!=null? baseLine.getEnd() : 0;

    // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
    for(int i=anIndex+1, iMax=_lines.size(); i<iMax; i++) { RichTextLine line = getLine(i);
        line._index = i; line._start = _length; _length += line.length(); }
}

/**
 * Returns the block at the given char index.
 */
public RichTextLine getLineAt(int anIndex)
{
    for(RichTextLine line : _lines)
        if(anIndex<line.getEnd())
            return line;
    if(anIndex==length()) return getLineLast();
    throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
}

/**
 * Returns the last block.
 */
public RichTextLine getLineLast()  { int lc = getLineCount(); return lc>0? getLine(lc-1) : null; }

/**
 * Returns the longest line.
 */
public RichTextLine getLineLongest()
{
    RichTextLine line = getLineCount()>0? getLine(0) : null; if(line==null) return null;
    double w = line.getWidth();
    for(RichTextLine ln : _lines) if(ln.getWidth()>w) { line = ln; w = ln.getWidth(); }
    return line;
}

/**
 * Returns the TextRun that contains the given index.
 */
public RichTextRun getRunAt(int anIndex)
{
    RichTextLine line = getLineAt(anIndex);
    return line.getRunAt(anIndex - line.getStart());
}

/**
 * Returns the last run.
 */
public RichTextRun getRunLast()
{
    return getRunAt(length());
}

/**
 * Returns the Font for run at given character index.
 */
public Font getFontAt(int anIndex)
{
    return getRunAt(anIndex).getFont();
}

/**
 * Returns the TextStyle for the run at the given character index.
 */
public TextStyle getStyleAt(int anIndex)
{
    return getRunAt(anIndex).getStyle();
}
    
/**
 * Returns the TextLineStyle for the run at the given character index.
 */
public TextLineStyle getLineStyleAt(int anIndex)
{
    return getLineAt(anIndex).getLineStyle();
}
    
/**
 * Returns whether text contains an underlined run.
 */
public boolean isUnderlined()
{
    if (isPlainText())
        return getStyleAt(0).isUnderlined();
    for (RichTextLine line : _lines)
        if(line.isUnderlined())
            return true;
    return false;
}

/**
 * Sets the RichText to be underlined.
 */
public void setUnderlined(boolean aFlag)
{
    setStyleValue(TextStyle.UNDERLINE_KEY, aFlag? 1 : null, 0, length());
}

/**
 * Returns the horizontal alignment of the first paragraph of the RichText.
 */
public HPos getAlignX()  { return getLineStyleAt(0).getAlign(); }

/**
 * Sets the horizontal alignment of the xstring.
 */
public void setAlignX(HPos anAlignX)
{
    setLineStyleValue(TextLineStyle.ALIGN_KEY, anAlignX, 0, length());
}

/**
 * Scales all the fonts in text by given factor.
 */
public void scaleFonts(double aScale)
{
    if(aScale==1) return;
    for(RichTextLine line : getLines()) { int lstart = line.getStart();
        for(RichTextRun run : line.getRuns()) { int rstrt = run.getStart(), rend = run.getEnd();
            setStyle(run.getStyle().copyFor(run.getFont().scaleFont(aScale)), lstart + rstrt, lstart + rend); }}
}

/**
 * Returns the width of text.
 */
public double getPrefWidth()
{
    if(_width>=0) return _width;
    RichTextLine line = getLineLongest(); if(line==null) return 0;
    return _width = Math.ceil(line.getWidth());
}

/**
 * Returns the width of text from given index.
 */
public double getPrefWidth(int anIndex)
{
    if(anIndex<=0) return getPrefWidth(); double width = 0;
    for(RichTextLine line : _lines)
        if(anIndex<line.getEnd()) width = Math.max(width, line.getWidth(anIndex-line.getStart()));
    return width;
}

/**
 * Returns the index of given string.
 */
public int indexOf(String aStr, int aStart)
{
    for(RichTextLine line : getLines()) { if(aStart>=line.getEnd()) continue; int lstrt = line.getStart();
        int index = line.indexOf(aStr, aStart - lstrt); if(index>=0) return index + lstrt; }
    return -1;
}

/**
 * Returns index of next newline (or carriage-return/newline) starting at given char index. 
 */
public int indexOfNewline(int aStart)  { return StringUtils.indexOfNewline(this, aStart); }

/**
 * Returns index just beyond next newline (or carriage-return/newline) starting at given char index. 
 */
public int indexAfterNewline(int aStart)  { return StringUtils.indexAfterNewline(this, aStart); }

/**
 * Returns index of the previous newline (or carriage-return/newline) starting at given char index. 
 */
public int lastIndexOfNewline(int aStart)  { return StringUtils.lastIndexOfNewline(this, aStart); }

/**
 * Returns index just beyond previous newline (or carriage-return/newline) starting at given char index. 
 */
public int lastIndexAfterNewline(int aStart)  { return StringUtils.lastIndexAfterNewline(this, aStart); }

/**
 * Returns whether the index in the given char sequence is at a line end.
 */
public boolean isLineEnd(int anIndex)  { return StringUtils.isLineEnd(this, anIndex); }

/**
 * Returns whether the index in the given char sequence is at just after a line end.
 */
public boolean isAfterLineEnd(int anIndex)  { return StringUtils.isAfterLineEnd(this, anIndex); }

/**
 * Returns whether a char is a newline char.
 */
public boolean isLineEndChar(int anIndex)  { return StringUtils.isLineEndChar(this, anIndex); }

/**
 * Returns whether property change is enabled.
 */
public boolean isPropChangeEnabled()  { return _propChangeEnabled; }

/**
 * Sets whether property change is enabled.
 */
public void setPropChangeEnabled(boolean aValue)  { _propChangeEnabled = aValue; }

/**
 * Returns an RichText for given char range.
 */
public RichText subtext(int aStart, int aEnd)
{
    // Create new RichText and iterate over lines in range to add copies for subrange
    RichText rtext = new RichText(); rtext._lines.remove(0);
    int sline = getLineAt(aStart).getIndex(), eline = getLineAt(aEnd).getIndex();
    for(int i=sline;i<=eline;i++) { RichTextLine line = getLine(i); int lstart = line.getStart();
        int start = Math.max(aStart-lstart, 0), end = Math.min(aEnd-lstart, line.length());
        RichTextLine lcopy = line.subline(start, end);
        rtext.addLine(lcopy, rtext.getLineCount());
    }
    
    // Return rtext
    return rtext;
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aLsnr)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aLsnr);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aLsnr)
{
    _pcs.removePropChangeListener(aLsnr);
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    PropChange pc = new PropChange(this, aProp, oldVal, newVal);
    firePropChange(pc);
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    if(!_pcs.hasListener(aProp)) return;
    PropChange pc = new PropChange(this, aProp, oldVal, newVal, anIndex);
    firePropChange(pc);
}

/**
 * Fires a given property change.
 */
protected void firePropChange(PropChange aPC)
{
    _pcs.firePropChange(aPC);
}

/**
 * Standard clone implementation.
 */
public RichText clone()
{
    // Do normal clone
    RichText clone;
    try { clone = (RichText)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    
    // Reset lines array and length
    clone._lines = new ArrayList(getLineCount());
    clone._length = 0;
    
    // Copy lines deep
    for(int i=0,iMax=getLineCount();i<iMax;i++) {
        RichTextLine line = getLine(i), lclone = line.clone();
        clone.addLine(lclone,i);
    }
    
    // Reset PropChangeSupport and return
    clone._pcs = PropChangeSupport.EMPTY;
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named xstring
    XMLElement e = new XMLElement("xstring");
    
    // Declare loop variable for xstring attributes: Font, Color, Paragraph, Format, Outline, Underline, Scripting, CS
    TextStyle style = getDefaultStyle(); TextLineStyle lstyle = getDefaultLineStyle();
    Font font = style.getFont(); Color color = style.getColor();
    TextFormat format = style.getFormat(); Border border = null; //RMParagraph pgraph = getDefaultParagraph();
    int scripting = 0; float charSpacing = 0; boolean underline = false;
        
    // Iterate over runs
    for(RichTextLine line : getLines()) {
    for(int i=0, iMax=line.getRunCount(); i<iMax; i++) { RichTextRun run = line.getRun(i);
        
        // If font changed for run, write font element
        if(!SnapUtils.equals(font, run.getFont())) { font = run.getFont();
            e.add(anArchiver.toXML(font)); }
        
        // If color changed for run, write color
        if(!SnapUtils.equals(color, run.getColor())) { color = run.getColor();
            e.add(anArchiver.toXML(color)); }
        
        // If format changed for run, write format
        if(!SnapUtils.equals(format, run.getFormat())) { format = run.getFormat();
            if(format==null) e.add(new XMLElement("format")); else e.add(anArchiver.toXML(format));
        }
        
        // If paragraph style changed for run, write paragraph
        if(!SnapUtils.equals(lstyle, line.getLineStyle())) { lstyle = line.getLineStyle();
            e.add(anArchiver.toXML(lstyle)); }
        
        // If underline style changed, write underline
        if(underline!=run.isUnderlined()) { underline = run.isUnderlined();
            e.add(new XMLElement("underline"));
            if(!underline) e.get(e.size()-1).add("style", -1);
        }
        
        // If border changed, write border
        if(!SnapUtils.equals(border, run.getBorder())) { border = run.getBorder();
            e.add(new XMLElement("TextBorder"));
            if(border!=null) {
                if(border.getWidth()!=1) e.get(e.size()-1).add("stroke", border.getWidth());
                if(border.getColor()!=null) e.get(e.size()-1).add("color", "#" + border.getColor().toHexString());
            }
            else e.get(e.size()-1).add("off", true);
        }
        
        // If scripting changed, write scripting
        if(scripting!=run.getScripting()) { scripting = run.getScripting();
            XMLElement se = new XMLElement("scripting");
            if(scripting!=0) se.add("val", scripting);
            e.add(se);
        }
        
        // If char spacing changed, write char spacing
        if(charSpacing!=run.getCharSpacing()) { charSpacing = run.getCharSpacing();
            XMLElement charSpacingXML = new XMLElement("char-spacing");
            charSpacingXML.add("value", charSpacing);
            e.add(charSpacingXML);
        }
        
        // Write run string
        if(run.length()>0)
            e.add(new XMLElement("string", run.getString()));
    }}
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Get map for run attributes
    TextStyle style = getDefaultStyle();
    TextLineStyle lstyle = null;
    
    // Iterate over child elements to snag common attributes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement e = anElement.get(i);
        
        // Unarchive string
        if(e.getName().equals("string")) {
            String str = e.getValue(); if(str==null || str.length()==0) continue;
            int len = length();
            addChars(str, style, len);
            if(lstyle!=null) { setLineStyle(lstyle, len, len+str.length()); lstyle = null; }
        }
        
        // Unarchive font element
        else if(e.getName().equals("font")) {
            Font font = anArchiver.fromXML(e, Font.class, null);
            style = style.copyFor(font);
        }
        
        // Unarchive color element
        else if(e.getName().equals("color")) {
            Color color = anArchiver.fromXML(e, Color.class,null);
            style = style.copyFor(color);
        }
        
        // If format changed for segment, write format
        else if(e.getName().equals("format")) {
            Object fmt = anArchiver.fromXML(e, null);
            style = style.copyFor(TextStyle.FORMAT_KEY, fmt);
        }
            
        // Unarchive pgraph element
        else if(e.getName().equals("pgraph"))
            lstyle = anArchiver.fromXML(e, TextLineStyle.class, null);
        
        // Unarchive underline element
        else if(e.getName().equals("underline")) {
            if(e.getAttributeIntValue("style")<0) style = style.copyFor(TextStyle.UNDERLINE_KEY, null);
            else style = style.copyFor(TextStyle.UNDERLINE_KEY, 1);
        }
        
        // Unarchive outline element
        else if(e.getName().equals("outline")) {
            if(e.getAttributeBoolValue("off")) style = style.copyFor(TextStyle.BORDER_KEY, null);
            else { double swidth = e.getAttributeFloatValue("stroke", 1);
                String cstr = e.getAttributeValue("color"); Color color = Color.get(cstr);
                Border border = Border.createLineBorder(style.getColor(), swidth);
                style = style.copyFor(border);
                style = style.copyFor(color);
            }
        }
        
        // Unarchive outline element
        else if(e.getName().equals("TextBorder")) {
            double stroke = e.getAttributeFloatValue("stroke", 1);
            String cstr = e.getAttributeValue("color"); Color color = Color.get(cstr);
            Border border = Border.createLineBorder(color, stroke);
            style = style.copyFor(border);
        }
        
        // Unarchive scripting
        else if(e.getName().equals("scripting")) {
            int scripting = e.getAttributeIntValue("val");
            style = style.copyFor(TextStyle.SCRIPTING_KEY, scripting);
        }
        
        // Unarchive char spacing
        else if(e.getName().equals("char-spacing")) {
            double cspace = e.getAttributeFloatValue("value");
            style = style.copyFor(TextStyle.CHAR_SPACING_KEY, cspace);
        }
    }
    
    // If no string was read, apply attributes anyway
    if(length()==0) getLine(0).getRun(0).setStyle(style);
    
    // Return this xstring
    return this;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getClass().getSimpleName() + ": " + getLineCount() + " lines, " + length() + " chars";
    for (int i=0,iMax=Math.min(getLineCount(),5); i<iMax; i++)
        str += "\n" + getLine(i);
    return str;
}

/**
 * A property change event for addChars/removeChars.
 */
public class CharsChange extends PropChange {
    public CharsChange(Object oldV, Object newV, int anInd)  { super(RichText.this, Chars_Prop, oldV, newV, anInd); }
    public CharSequence getOldValue()  { return (CharSequence)super.getOldValue(); }
    public CharSequence getNewValue()  { return (CharSequence)super.getNewValue(); }
    public void doChange(Object oldValue, Object newValue)
    {
        if(oldValue!=null) removeChars(getIndex(), getIndex() + ((CharSequence)oldValue).length());
        else addChars((CharSequence)newValue, null, getIndex());
    }
    public PropChange merge(PropChange anEvent)
    {
        CharsChange event = (CharsChange)anEvent;
        if(getNewValue()!=null && event.getNewValue()!=null && getNewValue().length()+getIndex()==event.getIndex())
            return new CharsChange(null, getNewValue().toString() + event.getNewValue(), getIndex());
        return null;
    }
}

/**
 * A property change event for RMXStringRun.Style change.
 */
public class StyleChange extends PropChange {
    int _start, _end;
    public StyleChange(Object oV, Object nV, int aStart, int anEnd) {
        super(RichText.this, Style_Prop, oV, nV, -1); _start = aStart; _end = anEnd; }
    public int getStart()  { return _start; }
    public int getEnd()  { return _end; }
    public void doChange(Object oldVal, Object newVal)  { setStyle((TextStyle)newVal, _start, _end); }
}

/**
 * A property change event for RMXStringRun.Style change.
 */
public class LineStyleChange extends PropChange {
    public LineStyleChange(Object oV, Object nV, int anIndex) {
        super(RichText.this, LineStyle_Prop, oV, nV, anIndex); }
    public void doChange(Object oval, Object nval)  {
        RichTextLine line = getLine(getIndex());
        setLineStyle((TextLineStyle)nval, line.getStart(), line.getStart());
    }
}

}