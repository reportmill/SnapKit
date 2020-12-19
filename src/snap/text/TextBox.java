/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;

import snap.geom.Path;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.VPos;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * This class manages a TextDoc to be rendered and edited in a bounding area.
 */
public class TextBox {

    // The RichText
    private RichText  _text;
    
    // The bounds of the text block
    private double  _x, _y, _width = Float.MAX_VALUE, _height;
    
    // Whether to wrap lines that overrun bounds
    private boolean  _wrapLines;
    
    // Whether to hyphenate text
    private boolean  _hyphenate;
    
    // They y alignment
    private VPos _alignY = VPos.TOP;
    
    // The y alignment amount
    private double  _alignedY;
    
    // Whether text is linked to another text
    private boolean  _linked;
    
    // The starting character of this box in RichText
    private int  _start;
    
    // The font scale for this box
    protected double  _fontScale = 1;
    
    // The bounds path
    private Shape  _bpath;

    // The lines in this text
    private List <TextBoxLine>  _lines = new ArrayList<>();
    
    // Whether text box needs updating
    private boolean  _needsUpdate, _updating;
    
    // The update start/end char indexes in RichText
    private int  _updStart, _updEnd, _lastLen;
    
    // A Listener to catch RichText PropChanges
    private PropChangeListener  _richTextLsnr = pc -> richTextDidPropChange(pc);
    
    /**
     * Creates a new TextBox.
     */
    public TextBox()
    {
        setRichText(new RichText());
    }

    /**
     * Creates a new TextBox initialized with the given String and no attributes.
     */
    public TextBox(CharSequence theChars)
    {
        this();
        addChars(theChars, null, 0);
    }

    /**
     * Returns the source of current content (URL, File, String path, etc.)
     */
    public Object getSource()  { return getRichText().getSource(); }

    /**
     * Loads the text from the given source.
     */
    public void setSource(Object aSource)
    {
        getRichText().setSource(aSource);
        setNeedsUpdateAll();
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return getRichText().getSourceURL(); }

    /**
     * Returns the source file.
     */
    public WebFile getSourceFile()  { return getRichText().getSourceFile(); }

    /**
     * Returns the RichText.
     */
    public RichText getRichText()  { return _text; }

    /**
     * Sets the RichText.
     */
    public void setRichText(RichText aRichText)
    {
        // If already set, just return
        if (aRichText==_text) return;

        // Stop listening to old RichText PropChanges, start listening to new
        if (_text!=null)
            _text.removePropChangeListener(_richTextLsnr);
        _text = aRichText;
        _text.addPropChangeListener(_richTextLsnr);

        // Update all
        setNeedsUpdateAll();
    }

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
     * Sets the width.
     */
    public void setWidth(double aValue)
    {
        if (aValue==_width) return;
        _width = aValue;
        if (isWrapLines()) setNeedsUpdateAll();
    }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the width.
     */
    public void setHeight(double aValue)
    {
        if (aValue==_height) return;
        _height = aValue;
        setNeedsUpdateAll();
    }

    /**
     * Returns the current bounds.
     */
    public Rect getBounds()  { return new Rect(_x, _y, _width, _height); }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(Rect aRect)
    {
        setBounds(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight());
    }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX); setY(aY); setWidth(aW); setHeight(aH);
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
        if (aPos==_alignY) return;
        _alignY = aPos; setNeedsUpdateAll();
    }

    /**
     * Returns the y for alignment.
     */
    public double getAlignedY()  { return getY() + _alignedY; }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return _wrapLines; }

    /**
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)  { _wrapLines = aValue; }

    /**
     * Returns whether layout tries to hyphenate wrapped words.
     */
    public boolean isHyphenate()  { return _hyphenate; }

    /**
     * Sets whether layout tries to hyphenate wrapped words.
     */
    public void setHyphenate(boolean aValue)
    {
        if (aValue==_hyphenate) return;
        _hyphenate = aValue; setNeedsUpdateAll();
    }

    /**
     * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
     */
    public boolean isLinked()  { return _linked; }

    /**
     * Returns whether text is linked to another text (and shouldn't add lines below bottom border).
     */
    public void setLinked(boolean aValue)
    {
        if (aValue==_linked) return;
        _linked = aValue;
        setNeedsUpdateAll();
    }

    /**
     * Returns the start char in RichText.
     */
    public int getStart()  { return _start; }

    /**
     * Sets the start char in RichText.
     */
    public void setStart(int anIndex)
    {
        if (anIndex==_start) return;
        _start = anIndex;
        setNeedsUpdateAll();
    }

    /**
     * Returns the end char in RichText.
     */
    public int getEnd()
    {
        int start = getStart();
        int lastLineEnd = getLineCount()>0 ? getLineLast().getEnd() : 0;
        return start + lastLineEnd;
    }

    /**
     * Returns the font scale of the text box.
     */
    public double getFontScale()  { return _fontScale; }

    /**
     * Sets the font scale of the text box.
     */
    public void setFontScale(double aValue)
    {
        if (aValue==_fontScale) return;
        _fontScale = aValue;
        setNeedsUpdateAll();
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
    public int length()  { return getRichText().length(); }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return getRichText().charAt(anIndex); }

    /**
     * Returns the string for the text.
     */
    public String getString()  { return getRichText().getString(); }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        String str = aString!=null ? aString : "";
        if (str.length()==length() && str.equals(getString())) return;
        getRichText().setString(str);
        setNeedsUpdateAll();
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        getRichText().addChars(theChars, theStyle, anIndex);
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)  { getRichText().removeChars(aStart, anEnd); }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        getRichText().replaceChars(theChars, theStyle, aStart, anEnd);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        getRichText().setStyleValue(aKey, aValue, aStart, anEnd);
    }

    /**
     * Returns the current box length (could be out of sync with text).
     */
    protected int boxlen()
    {
        int lcount = getLineCount(); if (lcount==0) return 0;
        int start = getStart();
        int end = getLineLast().getEnd();
        return end - start;
    }

    /**
     * Returns the number of lines in this text.
     */
    public int getLineCount()
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines.size();
    }

    /**
     * Returns the individual text line in this text.
     */
    public TextBoxLine getLine(int anIndex)
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines.get(anIndex);
    }

    /**
     * Returns the list of lines.
     */
    public List <TextBoxLine> getLines()
    {
        if (_needsUpdate && !_updating)
            update();
        return _lines;
    }

    /**
     * Returns the TextLine at the given char index.
     */
    public TextBoxLine getLineAt(int anIndex)
    {
        if (_needsUpdate && !_updating) update();
        for (TextBoxLine line : _lines)
            if (anIndex<line.getEnd())
                return line;
        TextBoxLine last = getLineLast();
        if (last!=null && anIndex==last.getEnd()) return last;
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + boxlen());
    }

    /**
     * Returns the last line.
     */
    public TextBoxLine getLineLast()
    {
        int lc = getLineCount();
        return lc>0 ? getLine(lc-1) : null;
    }

    /**
     * Returns the longest line.
     */
    public TextBoxLine getLineLongest()
    {
        TextBoxLine line = getLineCount()>0 ? getLine(0) : null; if (line==null) return null;
        double lineW = line.getWidth();
        for (TextBoxLine ln : _lines)
            if (ln.getWidth()>lineW) {
                line = ln; lineW = ln.getWidth(); }
        return line;
    }

    /**
     * Updates lines for RichText changes.
     */
    protected void richTextDidPropChange(PropChange aPC)
    {
        // Handle CharsChange: Update lines for old/new range
        if (aPC instanceof RichText.CharsChange) { RichText.CharsChange cc = (RichText.CharsChange)aPC;
            CharSequence nval = cc.getNewValue();
            CharSequence oval = cc.getOldValue();
            int index = cc.getIndex();
            if (oval!=null)
                textRemovedChars(index, index+oval.length());
            if (nval!=null)
                textAddedChars(index, index+nval.length());
        }

        else if (aPC instanceof RichText.StyleChange) { RichText.StyleChange sc = (RichText.StyleChange)aPC;
            textChangedChars(sc.getStart(), sc.getEnd()); }

        else if (aPC instanceof RichText.LineStyleChange) { RichText.LineStyleChange lsc = (RichText.LineStyleChange)aPC;
            RichTextLine rtl = getRichText().getLine(lsc.getIndex());
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
        if (!_needsUpdate) {
            _updStart = aStart; _updEnd = aEnd;
            _needsUpdate = true;
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
        int lend = lcount>0 ? _lines.get(lcount-1).getEnd() : getStart();

        // Get update start, linesEnd and textEnd to synchronize lines to text
        int start = _updStart; //Math.max(_updStart, getStart());
        int linesEnd = Math.min(_lastLen - _updEnd, lend);
        int textEnd = length() - _updEnd;
        if (start<=linesEnd || _lastLen==0)
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
        // Reset AlignY offset
        _alignedY = 0;

        // Get start-line-index and start-char-index
        int lcount = getLineCount();
        int sline = lcount>0 ? getLineAt(aStart).getIndex() : 0;
        int start = lcount>0 ? getLine(sline).getStart() : aStart;

        // Remove lines for old range
        removeLines(aStart, linesEnd);

        // Add lines for new range
        addLines(sline, start, textEnd);

        // Iterate over lines beyond start line and update lines Index, Start and Y_Local
        int len = sline>0 ? getLine(sline-1).getEnd() : 0;
        for (int i=sline, iMax=_lines.size(); i<iMax; i++) { TextBoxLine line = getLine(i);
            line._index = i; line._start = len;
            len += line.length(); line._yloc = -1;
        }

        // Calculated aligned Y
        if (_alignY!=VPos.TOP) {
            double prefH = getPrefHeight(getWidth());
            double height = getHeight();
            if (height>prefH)
                _alignedY = _alignY.doubleValue()*(height-prefH);
        }
    }

    /**
     * Removes the lines from given char index to given char index.
     */
    protected void removeLines(int aStart, int aEnd)
    {
        // Get LineCount, start-line-index and end-line-index
        int lcount = getLineCount(); if (lcount==0) return;
        int sline = getLineAt(aStart).getIndex();
        int eline = getLineAt(aEnd).getIndex();

        // Extend end-line-index to end of RichTextLine
        RichTextLine endRTL = getLine(eline).getRichTextLine();
        while (eline+1<lcount && getLine(eline+1).getRichTextLine()==endRTL)
            eline++;

        // Remove lines in range
        for (int i=eline;i>=sline;i--)
            _lines.remove(i);
    }

    /**
     * Removes the lines from given char index to given char index.
     */
    protected void addLines(int aLineIndex, int aStart, int aEnd)
    {
        // Get start char index
        int lcount = getLineCount();
        int start = Math.max(aStart, getStart()); if (start>length()) return;

        // Get RichText start-line-index, end-line-index
        RichText richText = getRichText();
        int startRTL = richText.getLineAt(start).getIndex();
        int endRTL = richText.getLineAt(aEnd).getIndex();

        // Iterate over RichText lines, create TextBox lines and add
        for (int i=startRTL, lindex=aLineIndex; i<=endRTL; i++) { RichTextLine rtl = richText.getLine(i);

            // Get start-char-index for line
            int lstart = Math.max(start-rtl.getStart(),0); if (lstart==rtl.length()) continue;

            // Add TextBoxLine(s) for RichTextLine
            while (lstart<rtl.length()) {
                TextBoxLine line = createLine(rtl, lstart, lindex);
                if ((isLinked() || _bpath!=null) && line.getMaxY()>getMaxY()) {
                    i = Short.MAX_VALUE;
                    break;
                }
                _lines.add(lindex++, line);
                lstart += line.length();
            }
        }

        // If we added last line and it is empty or ends with newline, add blank line
        if (endRTL==richText.getLineCount()-1) {
            RichTextLine rtl = richText.getLine(endRTL);
            if (rtl.length()==0 || rtl.isLastCharNewline()) {
                TextBoxLine line = createLine(rtl, rtl.length(), getLineCount());
                if (!((isLinked() || _bpath!=null) && line.getMaxY()>getMaxY()))
                    _lines.add(line);
            }
        }
    }

    /**
     * Create and return TextBoxLines for given RichTextLine, start char index and line index.
     */
    protected TextBoxLine createLine(RichTextLine aTextLine, int aStart, int aLineIndex)
    {
        // Get iteration variables
        int start = aStart;
        int len = aTextLine.length();
        int lineStart = aStart;
        RichTextRun run = aTextLine.getRun(0);
        int rend = run.getEnd();
        TextStyle style = run.getStyle();
        double fontScale = getFontScale();
        if (fontScale!=1)
            style = style.copyFor(style.getFont().scaleFont(fontScale));
        double lineHt = style.getLineHeight();
        boolean wrap = isWrapLines();
        boolean hyphenate = isHyphenate();

        // Get start x/y
        TextBoxLine lastLn = aLineIndex>0 ? getLine(aLineIndex-1) : null;
        double y = lastLn!=null ? lastLn.getY() + lastLn.getLineAdvance() : getY();
        double indent = aStart==0 ? aTextLine.getLineStyle().getFirstIndent() : aTextLine.getLineStyle().getLeftIndent();
        double x = getMinHitX(y, lineHt, indent);
        while (x>getMaxX() && y<=getMaxY()) { y++; x = getMinHitX(y,lineHt,indent); }
        double w = 0;
        double cspace = style.getCharSpacing();
        char c;

        // Create lines list and create/add first line
        TextBoxLine line = new TextBoxLine(this, style, aTextLine, aStart);
        line._yloc = y - getY();

        // Iterate over line chars
        while (start<len) {

            // Reset run if needed
            if (start>=rend) {
                run = aTextLine.getRun(run.getIndex()+1);
                rend = run.getEnd();
                style = run.getStyle();
                if (fontScale!=1)
                    style = style.copyFor(style.getFont().scaleFont(fontScale));
                lineHt = Math.max(lineHt, style.getLineHeight());
                cspace = style.getCharSpacing();
            }

            // Skip past whitespace
            while (start<len && Character.isWhitespace(c=aTextLine.charAt(start))) {
                if (c=='\t')
                    x = line.getXForTabAtIndexAndX(start, x + getX()) - getX();
                else x += style.getCharAdvance(c) + cspace; //aTextLine.getLineStyle().getTabForX(x)-getX()
                start++;
                if (start>=rend && start<len)
                    break;
            }

            // Iterate through non-whitespace
            int end = start;
            while (end<len && end<rend && !Character.isWhitespace(c=aTextLine.charAt(end))) {
                w += style.getCharAdvance(c) + cspace;
                end++;
            }

            // If char range was found, create and add token
            if (start<end) {

                // If last char ouside box, try for hyphen or add new line
                boolean didHyph = false;
                if (wrap && isHitRight(x+w-cspace, y, lineHt)) {

                    // If hyphenating, see if we can break token
                    if (hyphenate) {
                        int hyph = end, end2 = end;
                        double w2 = w, hypw = 0;
                        while (hyph>0 && isHitRight(x+w2-cspace+hypw, y, lineHt)) {
                            hyph = TextHyphenDict.getShared().getHyphen(aTextLine, start, hyph);
                            if (hyph>0) {
                                hypw = cspace+style.getCharAdvance('-');
                                while (end2>hyph) {
                                    --end2;
                                    w2 -= style.getCharAdvance(aTextLine.charAt(end2)) + cspace;
                                }
                            }
                        }
                        if (hyph>0 && hyph<end) {
                            end = end2;
                            w = w2 + hypw;
                            didHyph = true;
                        }
                    }

                    // If no hyphen and token start is line start, shorten token until it fits
                    if (!didHyph && start==aStart)
                        while (isHitRight(x+w-cspace, y, lineHt) && end>lineStart+1) {
                            --end;
                            w -= style.getCharAdvance(aTextLine.charAt(end)) + cspace;
                        }

                    // If no hyphen, break
                    else if (!didHyph)
                        break;
                }

                // Create new token and add to line
                TextBoxToken token = new TextBoxToken(line, style, start - lineStart, end - lineStart);
                token.setX(x);
                token.setWidth(w - cspace);
                if (didHyph)
                    token.setHyphenated(true);
                line.addToken(token);
                start = end;
                x += w; w = 0;
            }
        }

        // Reset sizes and return
        line.resetSizes();
        return line;
    }

    /**
     * Returns whether given x location and run hit right border.
     */
    protected boolean isHitRight(double aX, double aY, double aH)
    {
        if (_bpath==null || aY+aH>getMaxY())
            return aX>getWidth();
        Rect rect = new Rect(getX()+aX-1,aY,1,aH);
        return !_bpath.contains(rect);
    }

    /**
     * Returns the min x value that doesn't hit left border for given y/height and indent.
     */
    protected double getMinHitX(double aY, double aH, double anIndent)
    {
        if (_bpath==null || aY+aH>getMaxY()) return anIndent;
        Rect rect = new Rect(getX() + anIndent, aY, 20, aH);
        while (!_bpath.contains(rect) && rect.x<=getMaxX()) rect.x++;
        return rect.x - getX();
    }

    /**
     * Returns the max x value that doesn't hit right border for given y/height.
     */
    protected double getMaxHitX(double aY, double aH)
    {
        if (_bpath==null || aY+aH>getMaxY()) return getMaxX();
        Rect rect = new Rect(getMaxX()-1,aY,1,aH);
        while (!_bpath.contains(rect) && rect.x>1) rect.x--;
        return rect.x;
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
    public RichTextRun getRunAt(int anIndex)
    {
        return getRichText().getRunAt(anIndex);
    }

    /**
     * Returns the TextStyle for the run at the given character index.
     */
    public TextStyle getStyleAt(int anIndex)
    {
        return getRichText().getStyleAt(anIndex);
    }

    /**
     * Returns whether text box contains an underlined run.
     */
    public boolean isUnderlined()  { return getRichText().isUnderlined(); }

    /**
     * Returns underlined runs for text box.
     */
    public List <TextBoxRun> getUnderlineRuns(Rect aRect)
    {
        // Iterate over lines to add underline runs to list
        List <TextBoxRun> uruns = new ArrayList();
        for (TextBoxLine line : getLines()) {

            // If line above rect, continue, if below, break
            if (aRect!=null) {
                if (line.getMaxY() < aRect.y) continue;
                else if (line.getY() >= aRect.getMaxY())
                    break;
            }

            // If run underlined, add to list
            for (TextBoxRun run : line.getRuns())
                if (run.getStyle().isUnderlined())
                    uruns.add(run);
        }

        // Return list
        return uruns;
    }

    /**
     * Returns the line for the given y value.
     */
    public TextBoxLine getLineForY(double aY)
    {
        // If y less than zero, return null
        if (aY<0) return null;

        // Iterate over lines and return one that spans given y
        for (int i=0, iMax=getLineCount(); i<iMax; i++) { TextBoxLine line = getLine(i);
            if (aY < line.getMaxY())
                return line; }

        // If no line for given y, return last line
        return getLineLast();
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndex(double anX, double aY)
    {
        TextBoxLine line = getLineForY(aY); if (line==null) return 0;
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
        if (aStart>getEnd() || anEnd<getStart()) return path;
        if (anEnd>getEnd()) anEnd = getEnd();

        // Get StartLine, EndLine and start/end points
        TextBoxLine startLine = getLineAt(aStart);
        TextBoxLine endLine = aStart==anEnd ? startLine : getLineAt(anEnd);
        double startX = startLine.getXForChar(aStart-startLine.getStart()), startY = startLine.getBaseline();
        double endX = endLine.getXForChar(anEnd-endLine.getStart()), endY = endLine.getBaseline();
        startX = Math.min(startX,getMaxX()); endX = Math.min(endX,getMaxX());

        // Get start top/height
        double startTop = startLine.getY() - 1;
        double startHeight = startLine.getHeight() + 2;

        // Get path for upper left corner of sel start
        path.moveTo(startX, startTop + startHeight);
        path.lineTo(startX, startTop);
        if (aStart==anEnd)
            return path;

        // If selection spans more than one line, add path components for middle lines and end line
        if (startY!=endY) {  //!SnapMath.equals(startY, endY)
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
        clip = clip!=null ? clip.getIntersectRect(getBounds()) : getBounds();
        aPntr.clip(clip);

        // Iterate over lines
        for (int i=0, iMax=getLineCount(); i<iMax; i++) {
            TextBoxLine line = getLine(i);
            double lineY = line.getBaseline();
            if (line.getMaxY()<clip.getMinY()) continue;
            if (line.getY()>=clip.getMaxY()) break;

            // Iterate over line tokens
            for (int j=0,jMax=line.getTokenCount(); j<jMax;j++) { TextBoxToken token = line.getToken(j);

                // Do normal paint token
                String tokenStr = token.getString();
                double tokenX = token.getTextBoxX();
                aPntr.setFont(token.getFont());
                aPntr.setPaint(token.getColor()); //aPntr.setPaint(SnapColor.RED);
                aPntr.drawString(tokenStr, tokenX, lineY, token.getStyle().getCharSpacing());

                // Handle TextBorder: Get outline and stroke
                Border border = token.getStyle().getBorder();
                if (border!=null) {
                    Shape shape = token.getFont().getOutline(tokenStr, tokenX, lineY, token.getStyle().getCharSpacing());
                    aPntr.setPaint(border.getColor());
                    aPntr.setStroke(Stroke.Stroke1.copyForWidth(border.getWidth()));
                    aPntr.draw(shape);
                }
            }
        }

        // Paint underlines
        if (isUnderlined()) {

            for (TextBoxRun run : getUnderlineRuns(clip)) {

                // Set underline color and width
                TextBoxLine line = run.getLine();
                double uy = run.getFont().getUnderlineOffset();
                double uw = run.getFont().getUnderlineThickness();
                aPntr.setColor(run.getColor());
                aPntr.setStrokeWidth(uw);

                // Get under line endpoints and draw line
                double x0 = run.getX();
                double y0 = line.getBaseline() - uy;
                double x1 = run.getMaxX();
                if (run.getEnd()==line.getEnd())
                    x1 = line.getX() + line.getWidthNoWhiteSpace();
                aPntr.drawLine(x0, y0, x1, y0);
            }
        }

        // Restore state
        aPntr.restore();
    }

    /**
     * Returns the preferred width.
     */
    public double getPrefWidth(double aH)
    {
        RichText richText = getRichText();
        double rtextW = richText.getPrefWidth();
        double fontScale = getFontScale();
        double prefW = Math.ceil(rtextW * fontScale);
        return prefW;
    }

    /**
     * Returns the preferred height.
     */
    public double getPrefHeight(double aW)
    {
        // If WrapLines and given Width doesn't match current Width, setWidth
        if (isWrapLines() && !MathUtils.equals(aW,getWidth()) && aW>0) { //double oldW = getWidth();
            setWidth(aW);
            double prefH = getPrefHeight(aW); //setWidth(oldW); Should really reset old width - but why would they ask,
            return prefH;                     // if they didn't plan to use this width?
        }

        // Return bottom of last line minus box Y
        TextBoxLine lastLine = getLineLast();
        if (lastLine==null) return 0;
        double lineMaxY = lastLine.getMaxY();
        double alignedY = getAlignedY();
        return Math.ceil(lineMaxY - alignedY);
    }

    /**
     * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()
    {
        // Do normal layout
        if (!isTextOutOfBounds()) return;

        // Declare starting fontScale factor and dampening variables
        double fontScale = 1;
        double textW = getWidth();
        double textH = getHeight();
        double fsLo = 0;
        double fsHi = 1;

        // Loop while dampening variables are normal
        while (true) {

            // Reset fontScale to mid-point of fsHi and fsLo
            fontScale = (fsLo + fsHi)/2;
            setFontScale(fontScale);

            // If text exceeded layout bounds, reset fsHi to fontScale
            if (isTextOutOfBounds()) {
                fsHi = fontScale;
                if ((fsHi + fsLo)/2 == 0) {
                    System.err.println("Error scaling text. Could only fit " + boxlen() + " of " + length()); break; }
            }

            // If text didn't exceed layout bounds, reset fsLo to fontScale
            else {

                // Set new low (if almost fsHi, just return)
                fsLo = fontScale;
                double detaFS = fsHi - fsLo;
                if (detaFS<.05)
                    break;

                // If no line-wrap and PrefWidth almost TextBox.Width, stop
                if (!isWrapLines()) {
                    double prefW = getPrefWidth(-1);
                    double diffW = textW - prefW;
                    if (diffW < 1)
                        break;
                }

                // If PrefHeight almost TextBox.Height, stop
                double prefH = getPrefHeight(textW);
                double diffH = textH - prefH;
                if (diffH<1)
                    break;
            }
        }
    }

    /**
     * Returns whether this text box couldn't fit all text.
     */
    public boolean isTextOutOfBounds()
    {
        // Check Y no matter what
        int lcount = getLineCount();
        double lineMaxY = lcount > 0 ? getLine(lcount - 1).getMaxY() : 0;
        double tboxMaxY = getMaxY();
        if (lineMaxY >= tboxMaxY || getEnd() < getRichText().length())
            return true;

        // If not WrapLines, check X
        if (!isWrapLines()) {
            TextBoxLine line = getLineLongest();
            //double lineMaxX = line != null ? line.getMaxX() : 0;
            //double tboxMaxX = getMaxX();
            //if (lineMaxX > tboxMaxX)
            //    return true;
            double lineW = line != null ? line.getWidth() : 0;
            double tboxW = getWidth();
            if (lineW > tboxW)
                return true;
        }

        // Return false
        return false;
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