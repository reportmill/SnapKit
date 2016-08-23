package snap.gfx;

/**
 * A custom class.
 */
public class TextBoxRun {
    
    // The line this run is from
    TextBoxLine    _line;

    // The style
    TextStyle      _style;
    
    // The line start/stop
    int            _start, _end;
    
    // The line token start/stop indexes
    int            _startTokInd, _endTokInd;

    // The string
    String         _str;
    
    // The run x and width
    double         _x = -1, _width = -1;
    
    // Whether run is hyphenated
    boolean        _hyph;
    
    // Returns the next run
    TextBoxRun     _next;
    
/**
 * Creates a new TextBoxRun.
 */
public TextBoxRun(TextBoxToken aToken, int anIndex)
{
    _line = aToken.getLine(); _style = aToken.getStyle();
    _start = aToken.getStart(); _end = aToken.getEnd();
    _startTokInd = _endTokInd = anIndex;
}

/**
 * Returns the line.
 */
public TextBoxLine getLine()  { return _line; }

/**
 * Returns the run style.
 */
public TextStyle getStyle()  { return _style; }

/**
 * Returns the font.
 */
public Font getFont()  { return _style.getFont(); }

/**
 * Returns the color.
 */
public Color getColor()  { return _style.getColor(); }

/**
 * Returns the run start index in line.
 */
public int getStart()  { return _start; }

/**
 * Returns the end index in line.
 */
public int getEnd()  { return _end; }

/**
 * Returns the length of run.
 */
public int length()  { return getEnd() - getStart(); }

/**
 * Returns the run start token index in line.
 */
public int getStartTokenIndex()  { return _startTokInd; }

/**
 * Returns the end token index in line.
 */
public int getEndTokenIndex()  { return _endTokInd; }

/**
 * Returns the string.
 */
public String getString()  { return _str!=null? _str : (_str=_line.subSequence(_start,_end).toString()); }

/**
 * Returns an individual char in run.
 */
public char charAt(int anIndex)  { return getString().charAt(anIndex); }

/**
 * Returns the x location of run.
 */
public double getX()  { return _x>=0? _x : (_x=_line.getXForChar(_start)); }

/**
 * Returns the width of run.
 */
public double getWidth()  { return _width>=0? _width : (_width=_line.getXForChar(_end) - getX()); }

/**
 * Returns the max x location of run.
 */
public double getMaxX()  { return getX() + getWidth(); }

/**
 * Returns the baseline y value of run.
 */
public double getBaseline()  { return _line.getBaseline(); }

/**
 * Returns whether run is hyphenated.
 */
public boolean isHyphenated()  { return _hyph; }

/**
 * Returns the next run for text box line.
 */
public TextBoxRun getNext()  { return _next!=null? _next : (_next=_line.getRunForTokenIndex(_endTokInd+1)); }

}