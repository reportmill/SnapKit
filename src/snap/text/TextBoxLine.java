/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;

import snap.geom.HPos;
import snap.util.MathUtils;

/**
 * A class to represent a line of text in a TextBox.
 */
public class TextBoxLine implements CharSequence {

    // The TextBox that contains this line
    TextBox              _tbox;
    
    // The starting style for this line
    TextStyle            _startStyle;
    
    // The index of this line in text
    protected int        _index;
    
    // The char index of the start/end of this line in text
    int                  _start, _length;

    // The RichTextLine that this line renders
    RichTextLine         _rtline;
    
    // The start of this line in RichTextLine
    int                  _rtstart;
    
    // The bounds of this line in TextBlock
    double               _yloc = -1, _width, _height, _widthAll;
    
    // The x shift of the line due to alignment
    double               _alignX;
    
    // The tokens for this line
    List <TextBoxToken>  _tokens = new ArrayList();
    
    // The max Ascent for line fonts
    double               _ascent, _descent, _leading, _lineAdvance;
    
    // The first TextBoxRun for this line
    TextBoxRun           _run;
    
    // An array of character runs for the line
    List <TextBoxRun>    _runs;

    /**
     * Creates a new TextBoxLine.
     */
    public TextBoxLine(TextBox aBox, TextStyle aStartStyle, RichTextLine aTextLine, int theRTLStart)
    {
        _tbox = aBox; _startStyle = aStartStyle; _rtline = aTextLine; _rtstart = theRTLStart;
    }

    /**
     * Returns the TextBox.
     */
    public TextBox getBox()  { return _tbox; }

    /**
     * Returns the TextStyle at start of line.
     */
    public TextStyle getStartStyle()  { return _startStyle; }

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
     * Returns the length of this text line.
     */
    public int length()  { return _length; }

    /**
     * Returns the string for the line.
     */
    public String getString()  { return subSequence(0, length()).toString(); }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _rtline.charAt(anIndex + _rtstart); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)  { return _rtline.subSequence(aStart+_rtstart, anEnd+_rtstart); }

    /**
     * Returns the RichTextLine.
     */
    public RichTextLine getRichTextLine()  { return _rtline; }

    /**
     * Returns the start of this line in RichTextLine.
     */
    public int getRichTextLineStart()  { return _rtstart; }

    /**
     * Returns the RichTextRun of char in line.
     */
    public RichTextRun getRichTextRun(int anIndex)  { return _rtline.getRunAt(_rtstart+anIndex); }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()  { return _rtline.getLineStyle(); }

    /**
     * Returns the line x.
     */
    public double getX()  { return _tbox.getX() + _alignX; }

    /**
     * Returns the line y.
     */
    public double getY()  { return getYLocal() + _tbox.getAlignedY(); }

    /**
     * Returns the line y.
     */
    public double getYLocal()
    {
        // If already set, just return
        if (_yloc>=0) return _yloc;

        // Get YLocal from last line. Need to fix this to not stack overflow for large text showing tail first.
        int index = getIndex();
        TextBoxLine lastLine = index>0 ? _tbox.getLine(index-1) : null;
        _yloc = lastLine!=null ? (lastLine.getYLocal() + lastLine.getLineAdvance()) : 0;
        return _yloc;
    }

    /**
     * Returns the y position for this line (in same coords as the layout frame).
     */
    public double getBaseline()  { return getY() + getAscent(); }

    /**
     * Returns the width.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _height; }

    /**
     * Returns the max X.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Returns the width including trailing whitespace.
     */
    public double getWidthAll()  { return _widthAll; }

    /**
     * Returns the max x including trailing whitespace.
     */
    public double getMaxXAll()  { return getX() + getWidthAll(); }

    /**
     * Returns the width without whitespace.
     */
    public double getWidthNoWhiteSpace()
    {
        int len = length(); while (len>0 && Character.isWhitespace(charAt(len-1))) len--;
        if (len==length()) return getWidth();
        return getXForChar(len) - getX();
    }

    /**
     * Validates this line.
     */
    public void resetSizes()
    {
        // Get last token and its info
        TextBoxToken etok = getTokenCount()>0 ? _tokens.get(_tokens.size()-1) : null;
        int etokEnd = etok!=null ? etok.getEnd() : 0;
        TextStyle etokStyle = etok!=null ? etok.getStyle() : getStartStyle();

        // Get line end and length (extend end to capture trailing whitespace after last token)
        int end = etokEnd + _rtstart;
        while (end<_rtline.length() && Character.isWhitespace(_rtline.charAt(end))) end++;
        _length = end - _rtstart; //_end += _rtline.getStart(); _length = _end - _start;

        // Iterate over runs and get line metrics
        _ascent = etokStyle.getAscent(); _descent = etokStyle.getDescent(); _leading = etokStyle.getLeading();
        for (TextBoxToken tok : _tokens) {
            if (tok.getStyle()==etokStyle) continue;
            etokStyle = tok.getStyle();
            _ascent = Math.max(etokStyle.getAscent(), _ascent);
            _descent = Math.max(etokStyle.getDescent(), _descent);
            _leading = Math.max(etokStyle.getLeading(), _leading);
        }

        // Reset line Height
        //height = RMMath.clamp((float)height, getLineHeightMin(), getLineHeightMax());
        //height *= getLineSpacing(); height += getLineGap();

        // Get TextLineStyle
        TextLineStyle lstyle = _rtline.getLineStyle();

        // Set width, height and LineAdvance
        _width = _widthAll = etok!=null ? etok.getTextBoxMaxX() - getX() : 0;
        _height = _ascent + _descent;

        // Calculate LineAdvance
        _lineAdvance = _ascent + _descent + _leading;
        _lineAdvance = MathUtils.clamp(_lineAdvance, lstyle.getMinHeight(), lstyle.getMaxHeight());
        _lineAdvance *= lstyle.getSpacingFactor();
        _lineAdvance += lstyle.getSpacing();

        // Calculate widthAll (width with trailing whitespace)
        for (int i=etokEnd, iMax=_length;i<iMax;i++) { char c = charAt(i);
            if (c=='\t')
                _widthAll = getXForTabAtIndexAndX(i, getX() + _widthAll) - getX();
            else if (c!='\n' && c!='\r') _widthAll += etokStyle.getCharAdvance(c);
        }

        // If justify, shift tokens in line (unless line has newline or is last line in RichText)
        if (lstyle.isJustify() && getTokenCount()>1) {
            if (isLastCharNewline() || end==_rtline.length()) return;
            double y = getY();
            double tmx = _tbox.getMaxHitX(y, _height);
            double lmx = getMaxX(), rem = tmx - lmx;
            double shift = rem/(getTokenCount()-1), shft = 0;
            for (TextBoxToken tok : getTokens()) {
                tok._shiftX = shft;
                shft += shift;
            }
            _widthAll += shft - shift;
        }

        // Calculate X alignment shift
        else if (_rtline.getAlignX()!= HPos.LEFT && _tbox.getWidth()<9999) {
            double ax = _rtline.getAlignX().doubleValue();
            double lineY = getY();
            double tboxHitX = _tbox.getMaxHitX(lineY, _height);
            double lineMaxX = getMaxX();
            double rem = tboxHitX - lineMaxX;
            _alignX = Math.round(ax*rem);
        }
    }

    /**
     * Returns the number of tokens.
     */
    public int getTokenCount()  { return _tokens.size(); }

    /**
     * Returns the individual token at given index.
     */
    public TextBoxToken getToken(int anIndex)  { return _tokens.get(anIndex); }

    /**
     * Returns the tokens for this line.
     */
    public List <TextBoxToken> getTokens()  { return _tokens; }

    /**
     * Adds a token to line.
     */
    public void addToken(TextBoxToken aToken)  { _tokens.add(aToken); }

    /**
     * Returns the last token for this line.
     */
    public TextBoxToken getTokenLast()
    {
        int tc = _tokens.size();
        return tc>0 ? _tokens.get(tc-1) : null;
    }

    /**
     * Returns the max ascent of the chars in this line.
     */
    public double getAscent()  { return _ascent; }

    /**
     * Returns the max descent of the chars in this line.
     */
    public double getDescent()  { return _descent; }

    /**
     * Returns the leading of the chars in this line.
     */
    public double getLeading()  { return _leading; }

    /**
     * Returns the vertical distance for any line below this line.
     */
    public double getLineAdvance()  { return _lineAdvance; }

    /**
     * Returns the last char.
     */
    public char getLastChar()
    {
        int len = length();
        return len>0 ? charAt(len-1) : 0;
    }

    /**
     * Returns whether line ends with space.
     */
    public boolean isLastCharWhiteSpace()  { char c = getLastChar(); return c==' ' || c=='\t'; }

    /**
     * Returns whether run ends with newline.
     */
    public boolean isLastCharNewline()  { char c = getLastChar(); return c=='\r' || c=='\n'; }

    /**
     * Returns the token at character index.
     */
    public TextBoxToken getTokenAt(int anIndex)
    {
        int tc = getTokenCount(); if (tc==0) return null;
        TextBoxToken tok = getToken(0);
        for (int i=1; i<tc; i++) { TextBoxToken next = getToken(i);
            if (next.getStart()<=anIndex)
                tok = next;
            else break;
        }
        return tok;
    }

    /**
     * Returns the token at index.
     */
    public TextBoxToken getTokenForPointX(double anX)
    {
        TextBoxToken tok = getTokenCount()>0 ? getToken(0) : null; if (tok==null || tok.getTextBoxX()>anX) return null;
        for (int i=0, iMax=getTokenCount(); i<iMax; i++) {
            TextBoxToken next = i+1<iMax ? getToken(i+1) : null;
            if (next!=null && next.getTextBoxX()<=anX)
                tok = next;
            else return tok;
        }
        return tok;
    }

    /**
     * Returns the character index for the given x/y point.
     */
    public int getCharIndex(double anX)
    {
        // Get run for x coord (just return zero if null)
        TextBoxToken token = getTokenForPointX(anX);
        int index = token!=null ? token.getStart() : 0, len = length();
        TextStyle style = token!=null ? token.getStyle() : getStartStyle();
        double x = token!=null ? token.getTextBoxX() : getX();
        while (index<len) {
            char c = charAt(index);
            double w = c=='\t' ? getXForTabAtIndexAndX(index,x) - x : style.getCharAdvance(c);
            if (x+w/2>anX)
                return index;
            index++; x += w;
        }

        // If at end of line with newline, back off 1
        if (index==length() && isLastCharNewline()) index--;
        return index;
    }

    /**
     * Returns the x coord for the given character index.
     */
    public double getXForChar(int anIndex)
    {
        if (anIndex==length()) return getMaxXAll();
        TextBoxToken tok = getTokenAt(anIndex); if (tok!=null && anIndex<tok.getStart()) tok = null;
        TextStyle style = tok!=null ? tok.getStyle() : getStartStyle();
        double x = tok!=null ? tok.getTextBoxX() : getX();
        for (int i=tok!=null ? tok.getStart() : 0; i<anIndex; i++) { char c = charAt(i);
            if (c=='\t')
                x = getXForTabAtIndexAndX(i,x);
            else x += style.getCharAdvance(c) + style.getCharSpacing();
        }
        return x;
    }

    /**
     * Returns the x for tab at given x.
     */
    protected double getXForTabAtIndexAndX(int aCharInd, double aX)
    {
        // Get tab position and type. If beyond stops, return text right border. If left-tab, just return tab position
        TextLineStyle lstyle = _rtline.getLineStyle();
        int tindex = lstyle.getTabIndex(aX);
        if (tindex<0)
            return Math.max(aX, _tbox.getMaxX());
        double x = lstyle.getTabForX(aX);
        char ttype = lstyle.getTabType(tindex);
        if (ttype==TextLineStyle.TAB_LEFT)
            return x;

        // Get width of characters after tab (until next tab, newline or decimal)
        int start = aCharInd;
        int len = _rtline.length() - _rtstart;
        double w = 0;
        for (int i=aCharInd+1; i<len; i++) { char c = charAt(i);
            if (c=='\t' || c=='\r' || c=='\n') break;
            w += _startStyle.getCharAdvance(c) + _startStyle.getCharSpacing();
            if (ttype==TextLineStyle.TAB_DECIMAL && c=='.') break;
        }

        // If right or decimal, return tab position minus chars width (or tab char location if chars wider than tab stop)
        if (ttype==TextLineStyle.TAB_RIGHT || ttype==TextLineStyle.TAB_DECIMAL)
            return  aX+w<x ? x-w : aX;

        // if centered, return tab position minus half chars width (or tab char location if chars wider than tab stop)
        return aX+w/2<x ? x-w/2 : aX;
    }

    /**
     * Returns the previous line if available.
     */
    public TextBoxLine getPrevLine()
    {
        return _index>0 ? _tbox.getLine(_index-1) : null;
    }

    /**
     * Returns the next line, if available.
     */
    public TextBoxLine getNextLine()
    {
        return _index+1<_tbox.getLineCount() ? _tbox.getLine(_index+1) : null;
    }

    /**
     * Returns the max stroke width of any underlined chars in this line.
     */
    public double getUnderlineStroke()
    {
        double stroke = 0;
        for (TextBoxRun run : getRuns())
            stroke = Math.max(stroke, run.getFont().getUnderlineThickness());
        return stroke;
    }

    /**
     * Returns the Y position of any underlined chars in this line.
     */
    public double getUnderlineY()
    {
        double y = 0;
        for (TextBoxRun run : getRuns())
            y = Math.min(y, run.getFont().getUnderlineOffset());
        return y;
    }

    /**
     * Returns an array of runs for the line.
     */
    public List <TextBoxRun> getRuns()
    {
        // If already set, just return
        if (_runs!=null) return _runs;

        // Create new list for runs
        List runs = new ArrayList(_rtline.getRunCount());

        // Create first run and add to list.
        TextBoxRun run = createRun(0); runs.add(run);

        // Continue to create/add runs while not at line end
        while (run.getEnd()<length()) {
            run = createRun(run.getEnd()); runs.add(run); }

        // Set and return
        return _runs = runs;
    }

    /**
     * Creates the TextBoxRun at given char index in line.
     */
    protected TextBoxRun createRun(int aStart)
    {
        // Get RichTextRun and TextStyle at char index
        RichTextRun rtrun = getRichTextRun(aStart);
        TextStyle style = rtrun.getStyle();
        double fontScale = _tbox.getFontScale();
        if (fontScale!=1)
            style = style.copyFor(style.getFont().scaleFont(fontScale));

        // Get end of run
        int end = Math.min(length(), rtrun.getEnd() - getRichTextLineStart());

        // If Justify, reset end to start of next token
        if (getLineStyle().isJustify()) {
            TextBoxToken tok = getTokenAt(aStart);
            int ind = tok!=null ? getTokens().indexOf(tok) : -1;
            TextBoxToken tok2 = ind>=0 && ind+1<getTokenCount() ? getToken(ind+1) : null;
            if (tok2!=null)
                end = tok2.getStart();
        }

        // If there are tabs, end after first tab instead
        for (int i=aStart;i<end;i++)
            if (charAt(i)=='\t') {
                end = i+1; break;
            }

        // Create/return new run
        return new TextBoxRun(this, style, aStart, end);
    }

    /**
     * Returns the first TextBox run for the line.
     */
    public TextBoxRun getRun()  { return getRuns().get(0); }

    /**
     * Returns the last TextBox run for the line.
     */
    public TextBoxRun getRunLast()  { List <TextBoxRun> runs = getRuns(); return runs.get(runs.size()-1); }

    /**
     * Returns whether line ends with hyphen.
     */
    public boolean isHyphenated()
    {
        TextBoxToken tok = getTokenLast();
        return tok!=null && tok.isHyphenated();
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