/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Rect;
import snap.geom.VPos;
import snap.props.PropObject;
import snap.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a basic implementation of the TextLayout class.
 */
public class TextLayoutDefault extends PropObject implements TextLayout {

    // Whether text is rich
    protected boolean _rich;

    // The TextLines in this text
    protected List<TextLine>  _lines = new ArrayList<>();

    // The length of this text
    protected int  _length;

    // The X/Y of the text model
    protected double _x, _y;

    // The width/height of the text model
    protected double _width = Float.MAX_VALUE, _height;

    // The pref width of the text model
    protected double _prefW = -1;

    // They y alignment
    protected VPos _alignY = VPos.TOP;

    // The y alignment amount
    protected double _alignedY = -1;

    /**
     * Constructor.
     */
    public TextLayoutDefault()
    {
        this(false);
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextLayoutDefault(boolean isRich)
    {
        super();
        _rich = isRich;
    }

    /**
     * Whether this text supports multiple styles (font, color, etc.).
     */
    public boolean isRichText()  { return _rich; }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return false; }

    /**
     * Returns the number of characters in the text.
     */
    public int length()  { return _length; }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder(length());
        for (TextLine line : _lines)
            sb.append(line._sb);

        // Return
        return sb.toString();
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public TextLine getLine(int anIndex)  { return _lines.get(anIndex); }

    /**
     * Returns the list of blocks.
     */
    public List<TextLine> getLines()  { return _lines; }

    /**
     * Returns the Y alignment.
     */
    public VPos getAlignY()  { return _alignY; }

    /**
     * Sets the Y alignment.
     */
    public void setAlignY(VPos aPos)
    {
        if (aPos == _alignY) return;
        _alignY = aPos;
        _alignedY = -1;
    }

    /**
     * Returns the y for alignment.
     */
    public double getAlignedY()
    {
        // If already set, just return
        if (_alignedY >= 0) return getY() + _alignedY;

        // Calculated aligned Y
        _alignedY = 0;
        if (_alignY != VPos.TOP) {
            double textModelW = getWidth();
            double prefH = getPrefHeight(textModelW);
            double textModelH = getHeight();
            if (textModelH > prefH)
                _alignedY = _alignY.doubleValue() * (textModelH - prefH);
        }

        // Return
        return getY() + _alignedY;
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
        if (aValue == _width) return;
        _width = aValue;
        _lines.forEach(line -> line.updateAlignmentAndJustify());
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
        if (aValue == _height) return;
        _height = aValue;
        _alignedY = -1;
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
        setBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Sets the rect location and size.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX);
        setY(aY);
        setWidth(aW);
        setHeight(aH);
    }

    /**
     * Returns the width of text.
     */
    public double getPrefWidth()
    {
        // If already set, just return
        if (_prefW >= 0) return _prefW;

        // Calc, set, return
        TextLine longestLine = getLineLongest();
        double longestLineW = longestLine != null ? longestLine.getWidth() : 0;
        double prefW = Math.ceil(longestLineW);
        return _prefW = prefW;
    }

    /**
     * Returns the preferred height.
     */
    public double getPrefHeight(double aW)
    {
        // If WrapLines and given Width doesn't match current Width, setWidth
        if (isWrapLines() && !MathUtils.equals(aW, _width) && aW > 0) {
            double oldH = _height, oldW = _width;
            _height = Float.MAX_VALUE;
            setWidth(aW);
            double prefH = getPrefHeight();
            _height = oldH;
            setWidth(oldW); // Seems like this should be unnecessary, since width is likely to be set to aW
            return prefH;
        }

        // Return normal version
        return getPrefHeight();
    }
}
