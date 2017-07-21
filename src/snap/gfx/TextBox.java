/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.*;
import snap.util.*;
import snap.web.*;

/**
 * This class manages a TextDoc to be rendered and edited in a bounding area.
 */
public class TextBox implements PropChangeListener {

    // The RichText
    RichText             _text;
    
    // The TextBox length as currently configured
    int                  _boxlen;
    
    // The bounds of the text block
    double               _x, _y, _width, _height;
    
    // Whether to wrap text in box
    boolean              _wrapText;
    
    // Whether to hyphenate text
    boolean              _hyphenate;
    
    // They y alignment
    VPos                 _alignY = VPos.TOP;
    
    // The y alignment amount
    double               _alignedY;
    
    // Whether text is linked to another text
    boolean              _linked;
    
    // The starting character of this box in RichText
    int                  _start;
    
    // The font scale for this box
    double               _fontScale = 1;
    
    // The bounds path
    Shape                _bpath;

    // The lines in this text
    List <TextBoxLine>   _lines = new ArrayList();
    
    // The preferred width/height
    double               _prefWidth = -1, _prefHeight = -1;
    
    // Whether text box needs updating
    boolean              _needsUpdate, _updating;
    
    // The update start/end char indexes in RichText
    int                  _updStart, _updEnd, _lastLen;
    
/**
 * Creates a new TextBox.
 */
public TextBox()  { setText(new RichText()); }

/**
 * Creates a new TextBox initialized with the given String and no attributes.
 */
public TextBox(CharSequence theChars)  { this(); addChars(theChars, null, 0); }

/**
 * Returns the RichText.
 */
public RichText getText()  { return _text; }

/**
 * Sets the RichText.
 */
public void setText(RichText aRichText)
{
    if(aRichText==_text) return;
    if(_text!=null) _text.removePropChangeListener(this);
    _text = aRichText;
    _text.addPropChangeListener(this);
    setNeedsUpdateAll();
}

/**
 * Loads the text from the given source.
 */
public void setSource(Object aSource)
{
    getText().setSource(aSource);
    setNeedsUpdateAll();
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return getText().getSourceURL(); }

/**
 * Sets the source URL.
 */
public void setSourceURL(WebURL aURL)  { setSource(aURL); }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { return getText().getSourceFile(); }

/**
 * Returns the X location.
 */
public double getX()  { return _x; }

/**
 * Sets the X location.
 */
public void setX(double anX)  { _x = anX; }

/**
 * Returns the Y location.
 */
public double getY()  { return _y; }

/**
 * Sets the Y location.
 */
public void setY(double aY)  { _y = aY; }

/**
 * Returns the width.
 */
public double getWidth()  { return _width; }

/**
 * Returns the height.
 */
public double getHeight()  { return _height; }

/**
 * Returns the current bounds.
 */
public Rect getBounds()  { return new Rect(_x, _y, _width, _height); }

/**
 * Sets the rect location and size.
 */
public void setBounds(Rect aRect)  { setBounds(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight()); }

/**
 * Sets the rect location and size.
 */
public void setBounds(double aX, double aY, double aW, double aH)
{
    // If bounds already set, just return (ignore width if not WrapText)
    if(_x==aX && _y==aY && _width==aW && _height==aH) return;
    if(_x==aX && _y==aY && _height==aH && !isWrapText()) { _width = aW; return; }
    
    // Set bounds and update all
    _x = aX; _y = aY; _width = aW; _height = aH;
    setNeedsUpdateAll();
}

/**
 * Returns the max X.
 */
public double getMaxX()  { return getX() + getWidth(); }

/**
 * Returns the max Y.
 */
public double getMaxY()  { return getY() + getHeight(); }

/**
 * Returns the Y alignment.
 */
public VPos getAlignY()  { return _alignY; }

/**
 * Sets the Y alignment.
 */
public void setAlignY(VPos aPos)
{
    if(aPos==_alignY) return;
    _alignY = aPos; setNeedsUpdateAll();
}

/**
 * Returns the y for alignment.
 */
public double getAlignedY()  { return getY() + _alignedY; }

/**
 * Returns whether text wraps.
 */
public boolean isWrapText()  { return _wrapText; }

/**
 * Sets whether text wraps.
 */
public void setWrapText(boolean aValue)  { _wrapText = aValue; }

/**
 * Returns whether layout tries to hyphenate wrapped words.
 */
public boolean isHyphenate()  { return _hyphenate; }

/**
 * Sets whether layout tries to hyphenate wrapped words.
 */
public void setHyphenate(boolean aValue)
{
    if(aValue==_hyphenate) return;
    _hyphenate = aValue; setNeedsUpdateAll();
}

/**
 * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
 */
public boolean isLinked()  { return _linked; }

/**
 * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
 */
public void setLinked(boolean aValue)  { _linked = aValue; }

/**
 * Returns the start char in RichText.
 */
public int getStart()  { return _start; }

/**
 * Sets the start char in RichText.
 */
public void setStart(int anIndex)
{
    if(anIndex==_start) return;
    _start = anIndex;
    setNeedsUpdateAll();
}

/**
 * Returns the end char in RichText.
 */
public int getEnd()  { return getLineCount()>0? getLineLast().getEnd() : getStart(); }

/**
 * Returns the font scale of the text box.
 */
public double getFontScale()  { return _fontScale; }

/**
 * Sets the font scale of the text box.
 */
public void setFontScale(double aValue)
{
    if(aValue==_fontScale) return;
    _fontScale = aValue; setNeedsUpdateAll();
}

/**
 * Returns the bounds path.
 */
public Shape getBoundsPath()  { return _bpath; }

/**
 * Sets the bounds path.
 */
public void setBoundsPath(Shape aPath)  { _bpath = aPath; }

/**
 * Returns the number of characters in the text.
 */
public int length()  { return getText().length(); }

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)  { return getText().charAt(anIndex); }

/**
 * Returns the string for the text.
 */
public String getString()  { return getText().getString(); }

/**
 * Sets the text to the given string.
 */
public void setString(String aString)
{
    String str = aString!=null? aString : "";
    if(str.length()==length() && str.equals(getString())) return;
    getText().setString(str);
    setNeedsUpdateAll();
}

/**
 * Adds characters with attributes to this text at given index.
 */
public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
{
    getText().addChars(theChars, theStyle, anIndex);
}

/**
 * Removes characters in given range.
 */
public void removeChars(int aStart, int anEnd)  { getText().removeChars(aStart, anEnd); }

/**
 * Replaces chars in given range, with given String, using the given attributes.
 */
public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
{
    getText().replaceChars(theChars, theStyle, aStart, anEnd);
}

/**
 * Sets a given attribute to a given value for a given range.
 */
public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
{
    getText().setStyleValue(aKey, aValue, aStart, anEnd);
}

/**
 * Returns the current box length (could be out of sync with text).
 */
protected int boxlen()  { return _boxlen; }

/**
 * Returns the number of lines in this text.
 */
public int getLineCount()
{
    if(_needsUpdate && !_updating) update();
    return _lines.size();
}

/**
 * Returns the individual text line in this text.
 */
public TextBoxLine getLine(int anIndex)
{
    if(_needsUpdate && !_updating) update();
    return _lines.get(anIndex);
}

/**
 * Returns the list of lines.
 */
public List <TextBoxLine> getLines()
{
    if(_needsUpdate && !_updating) update();
    return _lines;
}

/**
 * Returns the TextLine at the given char index.
 */
public TextBoxLine getLineAt(int anIndex)
{
    if(_needsUpdate && !_updating) update();
    for(TextBoxLine line : _lines)
        if(anIndex<line.getEnd())
            return line;
    TextBoxLine last = getLineLast(); if(last!=null && anIndex==last.getEnd()) return last;
    throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + boxlen());
}

/**
 * Returns the last line.
 */
public TextBoxLine getLineLast()  { int lc = getLineCount(); return lc>0? getLine(lc-1) : null; }

/**
 * Returns the longest line.
 */
public TextBoxLine getLineLongest()
{
    TextBoxLine line = getLineCount()>0? getLine(0) : null; if(line==null) return null;
    double w = line.getWidth();
    for(TextBoxLine ln : _lines) if(ln.getWidth()>w) { line = ln; w = ln.getWidth(); }
    return line;
}

/**
 * Updates lines for RichText changes.
 */
public void propertyChange(PropChange aPCE)
{
    // Handle CharsChange: Update lines for old/new range
    if(aPCE instanceof RichText.CharsChange) { RichText.CharsChange cc = (RichText.CharsChange)aPCE;
        CharSequence nval = cc.getNewValue(), oval = cc.getOldValue(); int index = cc.getIndex();
        if(oval!=null) textRemovedChars(index, index+oval.length());
        if(nval!=null) textAddedChars(index, index+nval.length());
    }
    else if(aPCE instanceof RichText.StyleChange) { RichText.StyleChange sc = (RichText.StyleChange)aPCE;
        textChangedChars(sc.getStart(), sc.getEnd()); }
    else if(aPCE instanceof RichText.LineStyleChange) { RichText.LineStyleChange lsc = (RichText.LineStyleChange)aPCE;
        RichTextLine rtl = getText().getLine(lsc.getIndex());
        textChangedChars(rtl.getStart(), rtl.getEnd());
    }
}

/**
 * Called when chars added to RichText to track range in box and text to be synchronized.
 */
protected void textAddedChars(int aStart, int aEnd)  { setUpdateBounds(aStart, length() - aEnd); }

/**
 * Called when chars removed from RichText to track range in box and text to be synchronized.
 */
protected void textRemovedChars(int aStart, int aEnd)  { setUpdateBounds(aStart, length() - aStart); }

/**
 * Called when chars changed in RichText to track range in box and text to be synchronized.
 */
protected void textChangedChars(int aStart, int aEnd)  { setUpdateBounds(aStart, length() - aEnd); }

/**
 * Updates all lines.
 */
protected void setNeedsUpdateAll()  { setUpdateBounds(0,0); }

/**
 * Sets the update bounds (in characters from start and from end).
 */
protected void setUpdateBounds(int aStart, int aEnd)
{
    // If first call, set values
    if(!_needsUpdate) {
        _updStart = aStart; _updEnd = aEnd;
        _needsUpdate = true; _prefWidth = _prefHeight = -1;
    }
    
    // Successive calls update values
    else {
        _updStart = Math.min(_updStart, aStart);
        _updEnd = Math.min(_updEnd, aEnd);
    }
}

/**
 * Updates text box.
 */
protected void update()
{
    // Set updating
    _updating = true;
    
    // Get count, start and end of currently configured lines
    int lcount = _lines.size();
    int lstart = lcount>0? _lines.get(0).getStart() : getStart();
    int lend = lcount>0? _lines.get(lcount-1).getEnd() : getStart();
    
    // Get update start, linesEnd and textEnd to synchronize lines to text
    int start = Math.max(_updStart, lstart);
    int linesEnd = Math.min(_lastLen - _updEnd, lend);
    int textEnd = length() - _updEnd;
    if(start<=linesEnd || _lastLen==0)
        updateLines(start, linesEnd, textEnd);
        
    // Reset Updating, NeedsUpdate and LastLen
    _updating = false; _needsUpdate = false;
    _lastLen = length();
}

/**
 * Updates lines for given char start and an old/new char end.
 */
protected void updateLines(int aStart, int linesEnd, int textEnd)
{
    // Modify params for text box start
    int tbstart = getStart(), lcount = getLineCount();
    _alignedY = 0;
    
    // Remove lines for old range RichTextLines
    int sline = lcount>0? getLineAt(aStart).getIndex() : 0;
    int eline = lcount>0? getLineAt(linesEnd).getIndex() : -1;
    while(eline+1<lcount && getLine(eline+1).getRichTextLine()==getLine(eline).getRichTextLine()) eline++;
    for(int i=eline;i>=sline;i--) aStart = Math.min(_lines.remove(i).getStart(), aStart);
    
    // Add lines for updated RichTextLines
    int start = Math.max(aStart, tbstart); if(start>length()) return;
    int startRTL = getText().getLineAt(start).getIndex(), endRTL = getText().getLineAt(textEnd).getIndex();
    for(int i=startRTL, lindex=sline;i<=endRTL;i++) { RichTextLine rtl = getText().getLine(i);
        int lstart = Math.max(start-rtl.getStart(),0); if(lstart==rtl.length()) continue;
        List <TextBoxLine> lines = createLines(rtl, lindex, lstart);
        for(TextBoxLine line : lines) { line._index = lindex;
            if((isLinked() || _bpath!=null) && line.getMaxY()>getMaxY()) {
                i = Short.MAX_VALUE; break; }
            _lines.add(lindex++, line);
        }
    }
    
    // If we added last line and it is empty or ends with newline, add blank line
    if(endRTL==getText().getLineCount()-1) {
        RichTextLine rtl = getText().getLine(endRTL); if(rtl.length()==0 || rtl.isLastCharNewline()) {
            TextBoxLine line = createLines(rtl, getLineCount(), rtl.length()).get(0); line._index = getLineCount();
            if(!((isLinked() || _bpath!=null) && line.getMaxY()>getMaxY())) _lines.add(line); }
    }
    
    // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
    TextBoxLine baseLine = sline>0? getLine(sline-1) : null;
    _boxlen = baseLine!=null? baseLine.getEnd() - getLine(0).getStart() : 0;
    for(int i=sline, iMax=_lines.size(); i<iMax; i++) { TextBoxLine line = getLine(i);
        line._index = i; line._start = _boxlen + tbstart; _boxlen += line.length(); line._yloc = -1; }
        
    // This check can go after testing period
    if((!isLinked() && _bpath==null) && getEnd()!=_text.length()) {
        int blen = getEnd(), tlen = _text.length(), lcnt = getLineCount();
        int bstr = lcnt>0? getLine(0).getStart() : getStart(), bend = lcnt>0? getLine(lcnt-1).getEnd() : getStart();
        String str = "BoxLen: " + blen + ", TextLen: " + tlen + ", Bstrt: " + bstr + ", Bend: " + bend;
        System.err.println("TextBox: Invalid State: " + str); //throw new RuntimeException("TextBox: Invalid State: " + str);
    }
        
    // Calculated aligned Y
    if(_alignY!=VPos.TOP) {
        double ph = getPrefHeight(), height = getHeight();
        if(height>ph) _alignedY = _alignY.asDouble()*(height-ph);
    }
}

/**
 * Create and return TextBoxLines for given RichTextLine with line index and start char index.
 */
protected List <TextBoxLine> createLines(RichTextLine aTextLine, int aLineIndex, int aStart)
{
    // Get iteration variables
    int start = aStart, len = aTextLine.length(), lineStart = aStart;
    RichTextRun run = aTextLine.getRun(0); int rend = run.getEnd();
    TextStyle style = run.getStyle(); if(_fontScale!=1) style = style.copyFor(style.getFont().scaleFont(_fontScale));
    double lineHt = style.getLineHeight();
    boolean wrap = isWrapText(), hyphenate = isHyphenate();
    
    // Get start x/y
    TextBoxLine lastLn = aLineIndex>0? getLine(aLineIndex-1) : null;
    double y = lastLn!=null? lastLn.getY() + lastLn.getLineAdvance() : getY();
    double x = getMinHitX(y,lineHt); while(x>getMaxX() && y<=getMaxY()) { y++; x = getMinHitX(y,lineHt); }
    double w = 0, cspace = style.getCharSpacing(); char c;
    
    // Create lines list and create/add first line
    TextBoxLine line = new TextBoxLine(this, style, aTextLine, aStart); line._yloc = y - getY();
    List <TextBoxLine> lines = new ArrayList(); lines.add(line);
    
    // Iterate over line chars
    while(start<len) {
        
        // Reset run if needed
        if(start>=rend) {
            run = aTextLine.getRun(run.getIndex()+1); rend = run.getEnd();
            style = run.getStyle(); if(_fontScale!=1) style = style.copyFor(style.getFont().scaleFont(_fontScale));
            lineHt = Math.max(lineHt, style.getLineHeight());
            cspace = style.getCharSpacing();
        }
        
        // Skip past whitespace
        while(start<len && Character.isWhitespace(c=aTextLine.charAt(start))) {
            if(c=='\t') x = line.getXForTabAtIndexAndX(start, x + getX()) - getX();
            else x += style.getCharAdvance(c) + cspace; //aTextLine.getLineStyle().getTabForX(x)-getX()
            start++; if(start>=rend && start<len) break;
        }
        
        // Iterate through non-whitespace
        int end = start; while(end<len && end<rend && !Character.isWhitespace(c=aTextLine.charAt(end))) {
            w += style.getCharAdvance(c) + cspace; end++; }
        
        // If char range was found, create and add token
        if(start<end) {
            
            // If last char ouside box, try for hyphen or add new line
            boolean didHyph = false;
            if(wrap && isHitRight(x+w-cspace,y,lineHt)) {
                
                // If hyphenating, see if we can break token
                if(hyphenate) {
                    int hyph = end, end2 = end; double w2 = w, hypw = 0;
                    while(hyph>0 && isHitRight(x+w2-cspace+hypw,y,lineHt)) {
                        hyph = TextHyphenDict.getShared().getHyphen(aTextLine, start, hyph);
                        if(hyph>0) { hypw = cspace+style.getCharAdvance('-');
                            while(end2>hyph) { --end2; w2 -= style.getCharAdvance(aTextLine.charAt(end2)) + cspace; }}
                    }
                    if(hyph>0 && hyph<end) { end = end2; w = w2 + hypw; didHyph = true; }
                }
                
                // If no hyphen and token start is line start, shorten token until it fits
                if(!didHyph && start==aStart)
                    while(isHitRight(x+w-cspace,y,lineHt) && end>lineStart+1) {
                        --end; w -= style.getCharAdvance(aTextLine.charAt(end)) + cspace; }

                // If no hyphen, create and add new line. If token doesn't fit on new line, shorten until it does
                else if(!didHyph) {
                    line.resetSizes(); y += line.getLineAdvance();
                    lines.add(line = new TextBoxLine(this, style, aTextLine, start)); line._yloc = y - getY();
                    lineStart = start;
                    x = getMinHitX(y,lineHt); while(x>getWidth()) { y++; x = getMinHitX(y,lineHt); }
                    while(isHitRight(w-cspace,y,lineHt) && end>lineStart+1) {
                        --end; w -= style.getCharAdvance(aTextLine.charAt(end)) + cspace; }
                }
            }
            
            // Create new token and add to line
            TextBoxToken token = new TextBoxToken(line, style, start - lineStart, end - lineStart);
            token.setXLocal(x); token.setWidth(w - cspace); if(didHyph) token.setHyphenated(true);
            line.addToken(token);
            start = end; x += w; w = 0;
        }
    }
    
    // Reset sizes and return
    line.resetSizes(); line._yloc = y - getY();
    return lines;
}

/**
 * Returns whether given x location and run hit right border.
 */
protected boolean isHitRight(double aX, double aY, double aH)
{
    if(_bpath==null || aY+aH>getMaxY()) return aX>getWidth();
    Rect rect = new Rect(getX()+aX-1,aY,1,aH);
    return !_bpath.contains(rect);
}

/**
 * Returns the minimum x value that doesn't hit left border for given y and style.
 */
protected double getMinHitX(double aY, double aH)
{
    if(_bpath==null || aY+aH>getMaxY()) return 0; 
    Rect rect = new Rect(getX(),aY,20,aH);
    while(!_bpath.contains(rect) && rect.x<=getMaxX()) rect.x++;
    return rect.x - getX();
}

/**
 * Returns the minimum x value that doesn't hit left border for given y and style.
 */
protected double getMaxHitX(double aY, double aH)
{
    if(_bpath==null || aY+aH>getMaxY()) return getWidth(); 
    Rect rect = new Rect(getMaxX()-1,aY,1,aH);
    while(!_bpath.contains(rect) && rect.x>0) rect.x--;
    return rect.x - getX();
}

/**
 * Returns the token at given index.
 */
public TextBoxToken getTokenAt(int anIndex)
{
    TextBoxLine line = getLineAt(anIndex);
    return line.getTokenAt(anIndex - line.getStart());
}

/**
 * Returns the TextRun that contains the given index.
 */
public RichTextRun getRunAt(int anIndex)  { return getText().getRunAt(anIndex); }

/**
 * Returns the TextStyle for the run at the given character index.
 */
public TextStyle getStyleAt(int anIndex)  { return getText().getStyleAt(anIndex); }
    
/**
 * Returns the line for the given y value.
 */
public TextBoxLine getLineForY(double aY)
{
    // If y less than zero, return null
    if(aY<0) return null;
    
    // Iterate over lines and return one that spans given y
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { TextBoxLine line = getLine(i);
        if(aY < line.getMaxY())
            return line; }
    
    // If no line for given y, return last line
    return getLineLast();
}

/**
 * Returns the character index for the given x/y point.
 */
public int getCharIndex(double anX, double aY)
{
    TextBoxLine line = getLineForY(aY); if(line==null) return 0;
    int index = line.getCharIndex(anX);
    return line.getStart() + index;
}

/**
 * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
 */
public Path getPathForChars(int aStart, int anEnd)
{
    // Create new path for return
    Path path = new Path();
    
    // If invalid range, just return
    if(aStart>getEnd() || anEnd<getStart()) return path;
    if(anEnd>getEnd()) anEnd = getEnd();

    // Get StartLine, EndLine and start/end points
    TextBoxLine startLine = getLineAt(aStart), endLine = aStart==anEnd? startLine : getLineAt(anEnd);
    double startX = startLine.getXForChar(aStart-startLine.getStart()), startY = startLine.getBaseline();
    double endX = endLine.getXForChar(anEnd-endLine.getStart()), endY = endLine.getBaseline();
    startX = Math.min(startX,getMaxX()); endX = Math.min(endX,getMaxX());
    
    // Get start top/height
    double startTop = startLine.getY() - 1;
    double startHeight = startLine.getHeight() + 2;
    
    // Get path for upper left corner of sel start
    path.moveTo(startX, startTop + startHeight);
    path.lineTo(startX, startTop);
    if(aStart==anEnd)
        return path;
    
    // If selection spans more than one line, add path components for middle lines and end line
    if(startY!=endY) {  //!SnapMath.equals(startY, endY)
        double endTop = endLine.getY() - 1;
        double endHeight = endLine.getHeight() + 2;
        path.lineTo(getWidth(), startTop); path.lineTo(getWidth(), endTop);
        path.lineTo(endX, endTop); path.lineTo(endX, endTop + endHeight);
        path.lineTo(getX(), endTop + endHeight); path.lineTo(getX(), startTop + startHeight);
    }
    
    // If selection spans only one line, add path components for upper-right, lower-right
    else { path.lineTo(endX, startTop); path.lineTo(endX, startTop + startHeight); }
    
    // Close path and return
    path.close();
    return path;
}

/**
 * Paint TextBox to given painter.
 */
public void paint(Painter aPntr)
{
    // Get intersection of clip rect and bounds
    aPntr.save();
    Rect clip = aPntr.getClipBounds();
    clip = clip!=null? clip.getIntersectRect(getBounds()) : getBounds();
    aPntr.clip(clip);
    
    // Iterate over lines
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { TextBoxLine line = getLine(i); double ly = line.getBaseline();
        if(line.getMaxY()<clip.getMinY()) continue;
        if(line.getY()>=clip.getMaxY()) break;
        
        // Iterate over line tokens
        for(int j=0,jMax=line.getTokenCount(); j<jMax;j++) { TextBoxToken token = line.getToken(j);
        
            // Do normal paint token
            double tx = token.getX();
            aPntr.setFont(token.getFont()); aPntr.setPaint(token.getColor()); //aPntr.setPaint(SnapColor.RED);
            aPntr.drawString(token.getString(), tx, ly, token.getStyle().getCharSpacing());
                
            // Handle TextBorder: Get outline and stroke
            if(token.getStyle().getBorder()!=null) { Border border = token.getStyle().getBorder();
                Shape shape = token.getFont().getOutline(token.getString(), tx, ly, token.getStyle().getCharSpacing());
                aPntr.setPaint(border.getColor());
                aPntr.setStroke(Stroke.Stroke1.copyForWidth(border.getWidth()));
                aPntr.draw(shape);
            }
        }
        
        // Iterate over runs and draw underlines
        for(TextBoxRun run : line.getRuns()) {
            if(run.getStyle().isUnderlined()) { double rx = run.getX(), rw = run.getWidth();
                double uy = run.getFont().getUnderlineOffset(), uw = run.getFont().getUnderlineThickness();
                aPntr.setStrokeWidth(uw); aPntr.drawLine(rx, ly-uy, rx + rw, ly-uy);
            }
        }
    }
    
    // Restore state
    aPntr.restore();
}

/**
 * Returns the preferred width.
 */
public double getPrefWidth()  { return _prefWidth>=0? _prefWidth : (_prefWidth=computePrefWidth()); }

/**
 * Returns the preferred height.
 */
public double getPrefHeight()  { return _prefHeight>=0? _prefHeight : (_prefHeight=computePrefHeight()); }

/**
 * Compute the preferred width.
 */
protected double computePrefWidth()
{
    TextBoxLine ln = getLineLongest(); if(ln==null) return 0;
    return Math.ceil(ln.getWidth());
}

/**
 * Returns the preferred height.
 */
protected double computePrefHeight()
{
    TextBoxLine ln = getLineLast(); if(ln==null) return 0;
    return Math.ceil(ln.getMaxY() - getAlignedY());
}

/**
 * Scales font sizes of all text in an RMText shape to fit in bounds. Caches font scale factor in xstring.
 */
public void scaleTextToFit()
{
    // Do normal layout
    if(!isOutOfRoom()) return;
    
    // Declare starting fontScale factor and dampening variables
    double fontScale = 1, fsLo = 0, fsHi = 1;

    // Loop while dampening variables are normal
    while(true) {
        
        // Reset fontScale to mid-point of fsHi and fsLo
        fontScale = (fsLo + fsHi)/2;
        setFontScale(fontScale);

        // If text exceeded layout bounds, reset fsHi to fontScale
        if(isOutOfRoom()) {
            fsHi = fontScale;
            if((fsHi + fsLo)/2 == 0) {
                System.err.println("Error scaling text. Could only fit " + boxlen() + " of " + length()); break; }
        }
        
        // If text didn't exceed layout bounds, reset fsLo to fontScale
        else {
            fsLo = fontScale;
            if(getHeight()-getPrefHeight()<1 || fsHi-fsLo<.05)
                break;
        }
    }
}

/**
 * Returns whether this text box couldn't fit all text.
 */
public boolean isOutOfRoom()
{
    int lcount = getLineCount();
    return lcount>0 && getLine(lcount-1).getMaxY()>=getMaxY() || getEnd()<getText().length();
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getClass().getSimpleName() + " [" + getBounds().getString() + "]: ";
    str += _lines.size() + " lines, " + boxlen() + " chars";
    return str;
}

}