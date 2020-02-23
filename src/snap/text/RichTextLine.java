/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;

import java.util.*;

/**
 * A class to represent a line of text (for each newline) in RichText.
 */
public class RichTextLine implements CharSequence, Cloneable {

    // The RichText that contains this line
    RichText             _text;
    
    // The run for this line
    List <RichTextRun>   _runs = new ArrayList();
    
    // The line style
    TextLineStyle        _lstyle;

    // The index of this line in text
    int                  _index;
    
    // The char index of the start of this line in text
    int                  _start, _length;
    
    // The width of this line
    double               _width = -1;

/**
 * Creates a new RichTextLine.
 */
public RichTextLine(RichText aRichText)
{
    _text = aRichText;
    _lstyle = _text.getDefaultLineStyle();
    addRun(createRun(), 0);
}

/**
 * Returns the RichText.
 */
public RichText getText()  { return _text; }

/**
 * Returns the length of this text line.
 */
public int length()  { return _length; }

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)
{
    RichTextRun r = getRunAt(anIndex);
    return r.charAt(anIndex - r.getStart());
}

/**
 * Returns a new char sequence that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd)
{
    StringBuffer sb = new StringBuffer(anEnd - aStart);
    while(aStart<anEnd) {
        RichTextRun run = getRunAt(aStart); int runStart = run.getStart();
        int rend = Math.min(run.getEnd(), anEnd);
        sb.append(run.subSequence(aStart - runStart, rend - runStart));
        aStart = run.getEnd();
    }
    return sb;
}

/**
 * Returns the index of given string in line.
 */
public int indexOf(String aStr, int aStart)
{
    for(RichTextRun run : getRuns()) { if(aStart>=run.getEnd()) continue; int rstrt = run.getStart();
        int index = run._sb.indexOf(aStr, aStart - rstrt); if(index>=0) return index + rstrt; }
    return -1;
}

/**
 * Returns the string for the line.
 */
public String getString()  { return subSequence(0, length()).toString(); }

/**
 * Adds characters with attributes to this line at given index.
 */
public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
{
    // Get run at index - if empty, go ahead and set style
    RichTextRun run = getRunAt(anIndex);
    if(run.length()==0 && theStyle!=null)
        run.setStyle(theStyle);
    
    // If style provided and different from current style, get dedicated run
    if(theStyle!=null && !theStyle.equals(run.getStyle())) {
        if(anIndex==run.getStart())
            run = addRun(theStyle, run.getIndex());
        else if(anIndex==run.getEnd())
            run = addRun(theStyle, run.getIndex()+1);
        else { run = splitRun(run, anIndex - run.getStart()); run = addRun(theStyle, run.getIndex()); }
    }
    
    // Add chars
    run.insert(anIndex - run.getStart(), theChars);  // Add chars to StringBuffer
    updateRuns(run.getIndex());  // Update Line/Text length
}

/**
 * Removes characters in given range.
 */
public void removeChars(int aStart, int anEnd)
{
    // If empty range, just return
    if(anEnd==aStart) return;
    
    // Iterate over effected runs and remove chars
    int end = anEnd;
    while(aStart<end) {
        
        // Get run at end
        RichTextRun run = getRunAt(end); if(end==run.getStart()) run = getRun(run.getIndex()-1);
        int rstart = run.getStart(), start = Math.max(aStart, rstart);
        
        // If range matches run range, just remove it
        if(start==rstart && end==run.getEnd() && getRunCount()>1) { int rindex = run.getIndex();
            _runs.remove(rindex);
            updateRuns(rindex-1);
        }
            
        // Otherwise delete chars from run
        else {
            run.delete(start - rstart, end - rstart);   // Delete chars from StringBuffer
            updateRuns(run.getIndex());                     // Update Line/Text length
        }
        end = rstart;
    }
}

/**
 * Returns the number of runs for this line.
 */
public int getRunCount()  { return _runs.size(); }

/**
 * Returns the individual run at given index.
 */
public RichTextRun getRun(int anIndex)  { return _runs.get(anIndex); }

/**
 * Returns the line runs.
 */
public List <RichTextRun> getRuns()  { return _runs; }

/**
 * Creates a new run.
 */
protected RichTextRun createRun()  { return new RichTextRun(); }

/**
 * Adds a new run at given index.
 */
private RichTextRun addRun(TextStyle theStyle, int anIndex)
{
    RichTextRun run = createRun(); if(theStyle!=null) run.setStyle(theStyle);
    addRun(run, anIndex); return run;
}

/**
 * Adds a run to line.
 */
private void addRun(RichTextRun aRun, int anIndex)
{
    _runs.add(anIndex, aRun);
    updateRuns(anIndex-1); //invalidate();
}

/**
 * Returns the last run.
 */
public RichTextRun getRunLast()  { return getRunCount()>0? getRun(getRunCount()-1) : null; }

/**
 * Returns the head run for the line.
 */
public RichTextRun getRunAt(int anIndex)
{
    for(RichTextRun run : _runs)
        if(anIndex<run.getEnd())
            return run;
    if(anIndex==length()) return getRunLast();
    throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
}

/**
 * Splits given run at given index and returns a run containing the remaining characters (and identical attributes).
 */
protected RichTextRun splitRun(RichTextRun aRun, int anIndex)
{
    RichTextRun remainder = aRun.clone();
    aRun.delete(anIndex, aRun.length());
    remainder.delete(0, anIndex);
    addRun(remainder, aRun.getIndex()+1);
    return remainder;
}

/**
 * Updates length due to change in given run.
 */
protected void updateRuns(int aRunIndex)
{
    // Get BaseRun and Length at end of BaseRun
    RichTextRun baseRun = aRunIndex>=0? getRun(aRunIndex) : null;
    int length = baseRun!=null? baseRun.getEnd() : 0;
    
    // Iterate over runs beyond BaseRun and update Index, Start and Length
    for(int i=aRunIndex+1, iMax=getRunCount(); i<iMax; i++) { RichTextRun run = getRun(i);
        run._index = i; run._start = length; length += run.length(); }
    
    // If Length changed, update and call UpdateLines
    if(length!=_length) {
        _length = length;
        if(_text!=null)
            _text.updateLines(getIndex());
    }
    _width = -1;
}

/**
 * Sets the style for the line (propogates to runs).
 */
protected void setStyle(TextStyle aStyle)
{
    for(RichTextRun run : getRuns())
        run.setStyle(aStyle);
    _width = -1;
}

/**
 * Returns the line style.
 */
public TextLineStyle getLineStyle()  { return _lstyle; }

/**
 * Sets the line style.
 */
public void setLineStyle(TextLineStyle aLineStyle)  { _lstyle = aLineStyle; }

/**
 * Returns the alignment associated with this paragraph.
 */
public HPos getAlignX()  { return _lstyle.getAlign(); }

/**
 * Sets the alignment associated with this paragraph.
 */
public void setAlignX(HPos anAlign)  { setLineStyle(getLineStyle().copyFor(anAlign)); }

/**
 * Returns the last char.
 */
public char getLastChar()  { int len = length(); return len>0? charAt(len-1) : 0; }

/**
 * Returns whether line ends with space.
 */
public boolean isLastCharWhiteSpace()  { char c = getLastChar(); return c==' ' || c=='\t'; }

/**
 * Returns whether run ends with newline.
 */
public boolean isLastCharNewline()  { char c = getLastChar(); return c=='\r' || c=='\n'; }

/**
 * Returns the index of this line in text.
 */
public int getIndex()  { return _index; }

/**
 * Returns the start char index of this line in text.
 */
public int getStart()  { return _start; }

/**
 * Returns the end char index of this line in text.
 */
public int getEnd()  { return _start + _length; }

/**
 * Returns the next line, if available.
 */
public RichTextLine getNext() { return _text!=null && _index+1<_text.getLineCount()? _text.getLine(_index+1) :null; }

/**
 * Splits the line at given index in line.
 */
protected RichTextLine split(int anIndex)
{
    RichTextLine remainder = clone();
    remainder.removeChars(0, anIndex);
    removeChars(anIndex, length());
    return remainder;
}

/**
 * Joins the given line to the end of this line.
 */
protected void join(RichTextLine aLine)
{
    for(int i=0, iMax=aLine.getRunCount(); i<iMax; i++) { RichTextRun run = aLine.getRun(i), run2 = run.clone();
        addRun(run2, getRunCount()); }
}

/**
 * Returns a RichTextLine for given char range.
 */
public RichTextLine subline(int aStart, int aEnd)
{
    RichTextLine clone = clone();
    if(aEnd<length()) clone.removeChars(aEnd,length());
    if(aStart>0) clone.removeChars(0,aStart);
    return clone;
}

/**
 * Returns the width of line.
 */
public double getWidth()
{
    if(_width>=0) return _width;
    _width = 0; for(RichTextRun run : _runs) _width += run.getWidth();
    return _width;
}

/**
 * Returns the width of line from given index.
 */
public double getWidth(int anIndex)
{
    if(anIndex<=0) return getWidth(); double width = 0;
    for(RichTextRun run : _runs) if(anIndex<run.getEnd()) width += run.getWidth(anIndex-run.getStart());
    return width;
}

/**
 * Returns whether line contains an underlined run.
 */
public boolean isUnderlined()
{
    for(RichTextRun run : _runs) if(run.isUnderlined() && run.length()>0) return true;
    return false;
}

/**
 * Standard clone implementation.
 */
public RichTextLine clone()
{
    RichTextLine clone = null; try { clone = (RichTextLine)super.clone(); }
    catch(Exception e) { throw new RuntimeException(e);}
    clone._runs = new ArrayList();
    clone.join(this);
    return clone;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getString(); str = str.replace("\n", "\\n");
    return getClass().getSimpleName() + "[" + getIndex() + "](" + getStart() + "," + getEnd() + "): str=\"" + str +"\"";
}

}