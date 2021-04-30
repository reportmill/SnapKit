/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.Arrays;

import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Transform;
import snap.util.*;

/**
 * A Paint subclass to represent a gradient.
 */
public class GradientPaint implements Paint, Cloneable, XMLArchiver.Archivable {
    
    // The start points
    private double  _sx, _sy = .5;
    
    // The end points
    private double  _ex = 1, _ey = .5;

    // The roll (if linear and not absolute)
    private double  _roll;
    
    // The stops
    private Stop[]  _stops = DEFAULT_STOPS;
    
    // The type
    private Type  _type = Type.LINEAR;
    
    // Whether paint is defined in terms independent of primitive to be filled
    private boolean  _abs;
    
    // The types
    public enum Type { LINEAR, RADIAL }

    // Default stops
    public static Stop[]  DEFAULT_STOPS = new Stop[] { new Stop(0,Color.BLACK), new Stop(1,Color.WHITE) };
    
    /**
     * Creates a new GradientPaint.
     */
    public GradientPaint()  { }

    /**
     * Creates a new linear GradientPaint with given stops and roll.
     */
    public GradientPaint(double anAngle, Stop theStops[])
    {
        _stops = theStops;
        setRoll(anAngle, new Rect(0,0,1,1));
    }

    /**
     * Creates a new linear GradientPaint with given stops and roll.
     */
    public GradientPaint(Color aC1, Color aC2, double anAngle)
    {
        _stops = getStops(0, aC1, 1, aC2);
        setRoll(anAngle, new Rect(0,0,1,1));
    }

    /**
     * Creates a new linear GradientPaint with given type, start/end points, stops.
     */
    public GradientPaint(double aSX, double aSY, Color aC1, double aEX, double aEY, Color aC2)
    {
        this(Type.LINEAR, aSX, aSY, aEX, aEY, getStops(0, aC1, 1, aC2));
    }

    /**
     * Creates a new linear GradientPaint with given type, start/end points, stops.
     */
    public GradientPaint(double aSX, double aSY, double aEX, double aEY, Stop[] theStops)
    {
        this(Type.LINEAR, aSX, aSY, aEX, aEY, theStops);
    }

    /**
     * Creates a new GradientPaint with given type and stops and roll.
     */
    public GradientPaint(Type aType, Stop[] theStops)
    {
        this(aType, aType==Type.LINEAR ? 0 : .5, .5, 1d, .5, theStops);
    }

    /**
     * Creates a new GradientPaint with given type, start/end points, stops.
     */
    public GradientPaint(Type aType, double aSX, double aSY, double aEX, double aEY, Stop[] theStops)
    {
        _type = aType;
        _sx = aSX; _sy = aSY;
        _ex = aEX; _ey = aEY;
        _stops = theStops;
        _abs = Math.abs(_ex-_sx)>2 || Math.abs(_ey-_sy)>2;
    }

    /**
     * Creates a new GradientPaint with given type, start/end points, stops and absolute flag.
     */
    public GradientPaint(Type aType, double aSX, double aSY, double aEX, double aEY, Stop[] theStops, boolean isAbs)
    {
        _type = aType;
        _sx = aSX; _sy = aSY;
        _ex = aEX; _ey = aEY;
        _stops = theStops;
        _abs = isAbs;
    }

    /**
     * Returns the type.
     */
    public Type getType()  { return _type; }

    /**
     * Returns whether gradient is linear.
     */
    public boolean isLinear()  { return _type==Type.LINEAR; }

    /**
     * Returns whether gradient is radial.
     */
    public boolean isRadial()  { return _type==Type.RADIAL; }

    /**
     * Returns the start x.
     */
    public double getStartX()  { return _sx; }

    /**
     * Returns the start y.
     */
    public double getStartY()  { return _sy; }

    /**
     * Returns the end x.
     */
    public double getEndX()  { return _ex; }

    /**
     * Returns the end y.
     */
    public double getEndY()  { return _ey; }

    /**
     * Returns the stops.
     */
    public Stop[] getStops()  { return _stops; }

    /**
     * Returns the roll.
     */
    public double getRoll()  { return _roll; }

    /**
     * Sets the roll.
     */
    protected void setRoll(double aRoll, Rect aRect)
    {
        // Set roll
        _roll = aRoll;

        // Handle common cases
        if (aRoll == 0) {
            _sx = aRect.getX(); _sy = aRect.getMidY();
            _ex = aRect.getMaxX(); _ey = aRect.getMidY();
            return;
        }
        if (aRoll == 90) {
            _sx = aRect.getMidX(); _sy = aRect.getY();
            _ex = aRect.getMidX(); _ey = aRect.getMaxY();
            return;
        }

        // Do arbitrary version
        Transform t = new Transform(aRect.getMidX(), aRect.getMidY());   // Get transform of reverse rotation
        t.rotate(-_roll);
        t.translate(-aRect.getMidX(), -aRect.getMidY());

        // Get bounds of transformed rect
        Rect r2 = aRect.copyFor(t).getBounds();
        Point p1 = new Point(r2.getX(), r2.getMidY()), p2 = new Point(r2.getMaxX(), r2.getMidY());

        //
        t = new Transform(r2.getMidX(), r2.getMidY());
        t.rotate(_roll);
        t.translate(-r2.getMidX(), -r2.getMidY());
        p1.transformBy(t); p2.transformBy(t);
        _sx = p1.x; _sy = p1.y; _ex = p2.x; _ey = p2.y;
    }

    /**
     * Returns the number of stops.
     */
    public int getStopCount()  { return _stops.length; }

    /**
     * Returns the stop at given index.
     */
    public Stop getStop(int anIndex)  { return _stops[anIndex]; }

    /**
     * Returns the stop color at given index.
     */
    public Color getStopColor(int anIndex)  { return _stops[anIndex].getColor(); }

    /**
     * Returns the stop offset at given index.
     */
    public double getStopOffset(int anIndex)  { return _stops[anIndex].getOffset(); }

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isAbsolute()  { return _abs; }

    /**
     * Returns whether paint is opaque.
     */
    public boolean isOpaque()
    {
        for (int i=0, iMax=getStopCount(); i<iMax; i++)
            if (!getStop(i).getColor().isOpaque())
                return false;
        return true;
    }

    /**
     * Returns the closest color approximation of this paint.
     */
    public Color getColor()  { return getStopColor(0); }

    /**
     * Derives an instance of this class from given color.
     */
    public GradientPaint copyForColor(Color aColor)
    {
        GradientPaint.Stop stops[] = Arrays.copyOf(getStops(), getStopCount());
        stops[0] = new Stop(getStopOffset(0), aColor);
        return copyForStops(stops);
    }

    /**
     * Returns an absolute paint for given bounds of primitive to be filled.
     */
    public GradientPaint copyForRect(Rect aRect)
    {
        if (_abs) return this;
        if (_roll!=0) {
            GradientPaint gp = new GradientPaint(_type, _sx, _sy, _ex, _ey, _stops, true);
            gp.setRoll(_roll, aRect);
            return gp;
        }
        double sx = aRect.x + _sx*aRect.width, sy = aRect.y + _sy*aRect.height;
        double ex = aRect.x + _ex*aRect.width, ey = aRect.y + _ey*aRect.height;
        return new GradientPaint(_type, sx, sy, ex, ey, _stops, true);
    }

    /**
     * Returns a copy of this paint for new start/end points.
     */
    public GradientPaint copyForPoints(double aSX, double aSY, double aEX, double aEY)
    {
        GradientPaint copy = clone();
        copy._sx = aSX; copy._sy = aSY;
        copy._ex = aEX; copy._ey = aEY;
        return copy;
    }

    /**
     * Returns a copy of this paint with stops from given array.
     */
    public GradientPaint copyForStops(Stop theStops[])
    {
        GradientPaint clone = clone();
        clone._stops = Arrays.copyOf(theStops, theStops.length);
        return clone;
    }

    /**
     * Returns a new gradient which is a copy of this gradient but of a different type.
     */
    public GradientPaint copyForType(Type aType)
    {
        if (aType == _type) return this;
        GradientPaint clone = clone();
        clone._type = aType;
        if (clone.isRadial()) { clone._sx = .5; clone._sy = .5; clone._ex = 1; clone._ey = .5; _roll = 0; }
        else { clone._sx = 0; clone._sy = .5; clone._ex = 1; clone._ey = .5; }
        return clone;
    }

    /**
     * Returns a new gradient which is a copy of this gradient but with a different roll value.
     */
    public GradientPaint copyForRoll(double aRoll)
    {
        GradientPaint clone = clone();
        clone.setRoll(aRoll, new Rect(0,0,1,1));
        return clone;
    }

    /**
     * Reverse the order of the color stops
     */
    public GradientPaint copyForReverseStops()
    {
        int nstops = getStopCount();
        Stop[] stops = new Stop[nstops];
        for (int i=0; i<nstops; i++)
            stops[nstops-i-1] = new Stop(1 - getStopOffset(i), getStopColor(i));
        return copyForStops(stops);
    }

    /**
     * Returns a copy of this gradient paint.
     */
    public GradientPaint clone()
    {
        try { return (GradientPaint)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if (anObj == this) return true;
        GradientPaint other = anObj instanceof GradientPaint ? (GradientPaint) anObj : null; if (other == null) return false;

        // Check Type, Points, Stops, Roll
        if (other._type != _type) return false;
        if (_sx != other._sx || _sy != other._sy) return false;
        if (_ex != other._ex || _ey != other._ey) return false;
        if (other._roll != _roll) return false;
        if (!Arrays.equals(other._stops, _stops)) return false;
        return true; // Return true since checks passed
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic fill attributes
        XMLElement e = new XMLElement("fill");

        // Archive Type
        if (isLinear()) e.add("type", "gradient");
        else e.add("type", "radial");

        // Archive Points/Roll
        if (isRadial()) {
            e.add("x0", _sx); e.add("y0", _sy);
            e.add("x1", _ex); e.add("y1", _ey);
        }
        else if (getRoll() != 0)
            e.add("roll", _roll);

        // Archive first color
        if (!getStopColor(0).equals(Color.BLACK))
            e.add("color", "#" + getStopColor(0).toHexString());

        // Archive all colors beyond the first one as color2,color3 (for compatibility)
        for (int i=1, iMax=getStopCount(); i<iMax; ++i) {
            Color c = getStopColor(i);
            if (!c.equals(Color.BLACK))
                e.add("color"+(i+1), "#" + c.toHexString());
        }

        // Archive stop positions (stop 0 defaults to 0.0, and last stop defaults to 1.0)
        for (int i=0, iMax=getStopCount(); i<iMax; ++i) {
            double offset = getStopOffset(i);
            if (i == 0 && MathUtils.equalsZero(offset)) continue;
            if (i == iMax-1 && MathUtils.equals(offset, 1)) continue;
            e.add("stop"+(i==0 ? "" : (i+1)), offset);
        }

        // Archive the number of stops, since the defaults in the above lists make it possibly indeterminate
        if (getStopCount() != 2)
            e.add("nstops", getStopCount());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive type
        String type = anElement.getAttributeValue("type", "gradient");
        if (type.equals("radial")) _type = Type.RADIAL;

        // Unarchive points
        _sx = anElement.getAttributeDoubleValue("x0", _sx);
        _sy = anElement.getAttributeDoubleValue("y0", _sy);
        _ex = anElement.getAttributeDoubleValue("x1", _ex);
        _ey = anElement.getAttributeDoubleValue("y1", _ey);

        // Unarchive roll
        double roll = anElement.getAttributeFloatValue("roll");
        if (roll != 0)
            setRoll(roll, new Rect(0,0,1,1));

        // Unarchive stops
        int nstops = anElement.getAttributeIntValue("nstops", 2); _stops = new Stop[nstops];
        for (int i=0; i<nstops; i++) {
            String cstring = anElement.getAttributeValue("color" + (i==0 ? "" : (i+1))); // unarchive color,color2...
            Color c = cstring==null ? Color.BLACK : new Color(cstring);
            double offset;
            XMLAttribute stopAttr = anElement.getAttribute("stop" + (i==0 ? "" : (i+1))); // unarchive stop,stop2...
            if (stopAttr==null) {
                if (i == 0) offset = 0;
                else if (i == nstops-1) offset = 1;
                else continue;
            }
            else offset = stopAttr.getFloatValue();
            _stops[i] = new Stop(offset, c);
        }

        // Return this gradient paint
        return this;
    }

    /**
     * Standard to string implementation.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Gradient { ");
        for (Stop s : getStops()) sb.append(s).append(", ");
        return sb.append(" }").toString();
    }

    /**
     * A class to describe gradient paint stops.
     */
    public static class Stop {

        // The offset of stop (0..1)
        double   _offset;

        // The color of stop
        Color    _color;

        /** Returns a new stop. */
        public Stop(double anOffset, Color aColor)  { _offset = anOffset; _color = aColor; }

        /** Returns the offset. */
        public double getOffset()  { return _offset; }

        /** Returns the color. */
        public Color getColor()  { return _color; }

        /** Standard equals implementation. */
        public boolean equals(Object anObj)
        {
            if (anObj==this) return true;
            Stop other = anObj instanceof Stop ? (Stop)anObj : null; if (other == null) return false;
            return SnapUtils.equals(_color, other._color) && MathUtils.equals(_offset, other._offset);
        }

        /** Standard to string implementation. */
        public String toString() { return "ColorStop { Color=" + getColor().toHexString() + ", Offset=" + getOffset()+"}"; }

    }

    /**
     * Returns true if any of the colors in the gradient have alpha
     */
    public static boolean getStopsHaveAlpha(Stop[] theStops)
    {
        for (int i=0, iMax=theStops.length; i<iMax; i++)
            if (theStops[i].getColor().getAlphaInt() != 255)
                return true;
        return false;
    }

    /**
     * Creates an array of stops.
     */
    public static Stop[] getStops(double off1, Color col1, double off2, Color col2)
    {
        return new Stop[] { new Stop(off1,col1), new Stop(off2,col2) };
    }

    /**
     * Creates an array of stops.
     */
    public static Stop[] getStops(double off1, Color col1, double off2, Color col2, double off3, Color col3)
    {
        return new Stop[] { new Stop(off1,col1), new Stop(off2,col2), new Stop(off3,col3) };
    }

    /**
     * Creates an array of stops.
     */
    public static Stop[] getStops(double o1, Color c1, double o2, Color c2, double o3, Color c3,
        double o4, Color c4)
    {
        return new Stop[] { new Stop(o1,c1), new Stop(o2,c2), new Stop(o3,c3), new Stop(o4,c4) };
    }

    /**
     * Adds stops to a stops array.
     */
    public static Stop[] getStops(double[] theOffs, Color ... theColors)
    {
        Stop[] stops = new Stop[theOffs.length];
        for (int i=0; i<theOffs.length; i++)
            stops[i] = new Stop(theOffs[i], theColors[i]);
        return stops;
    }
}