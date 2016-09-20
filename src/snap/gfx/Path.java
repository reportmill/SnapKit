/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.Arrays;
import snap.gfx.PathIter.Seg;
import snap.util.*;

/**
 * A custom class.
 */
public class Path implements Shape, XMLArchiver.Archivable {

    // The array of segments
    Seg          _segs[] = new Seg[8];
    
    // The segment count
    int          _scount;
    
    // The array of points
    double       _points[] = new double[16];
    
    // The number of points
    int          _pcount;
    
    // The bounds
    Rect         _bounds;

/**
 * Moveto.
 */
public void moveTo(double x, double y)  { addSeg(Seg.MoveTo); addPoint(x, y); }

/**
 * LineTo.
 */
public void lineTo(double x, double y)  { addSeg(Seg.LineTo); addPoint(x, y); }

/**
 * LineTo.
 */
public void lineBy(double x, double y)
{
    x += _points[_pcount*2-2]; y += _points[_pcount*2-1];
    addSeg(Seg.LineTo); addPoint(x, y);
}

/**
 * Horizontal LineTo.
 */
public void hlineTo(double x)  { double y = _points[_pcount*2-1]; lineTo(x,y); }

/**
 * Vertical LineTo.
 */
public void vlineTo(double y)  { double x = _points[_pcount*2-2]; lineTo(x,y); }

/**
 * QuadTo.
 */
public void quadTo(double cpx, double cpy, double x, double y)  { addSeg(Seg.QuadTo); addPoint(cpx,cpy); addPoint(x,y);}

/**
 * CubicTo.
 */
public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
{
    addSeg(Seg.CubicTo); addPoint(cp1x, cp1y); addPoint(cp2x, cp2y); addPoint(x, y);
}

/**
 * ArcTo: Adds a Cubic using the corner point as a guide.
 */
public void arcTo(double cx, double cy, double x, double y)
{
    double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
    double lx = _points[_pcount*2-2], ly = _points[_pcount*2-1];
    double cpx1 = lx + (cx-lx)*magic, cpy1 = ly + (cy-ly)*magic;
    double cpx2 = x + (cx-x)*magic, cpy2 = y + (cy-y)*magic;
    curveTo(cpx1,cpy1,cpx2,cpy2,x,y);
}

/**
 * Close.
 */
public void close() { addSeg(Seg.Close); }

/**
 * Adds a segment.
 */
private void addSeg(Seg aSeg)
{
    if(_scount+1>_segs.length) _segs = Arrays.copyOf(_segs, _segs.length*2);
    _segs[_scount++] = aSeg; _bounds = null;
}

/**
 * Adds a point.
 */
private void addPoint(double x, double y)
{
    if(_pcount*2+1>_points.length) _points = Arrays.copyOf(_points, _points.length*2);
    _points[_pcount*2] = x; _points[_pcount*2+1] = y; _pcount++;
}

/**
 * Appends a path iterator.
 */
public void append(PathIter aPI)
{
    double crds[] = new double[6];
    while(aPI.hasNext()) switch(aPI.getNext(crds)) {
        case MoveTo: moveTo(crds[0], crds[1]); break;
        case LineTo: lineTo(crds[0], crds[1]); break;
        case QuadTo: quadTo(crds[0], crds[1], crds[2], crds[3]); break;
        case CubicTo: curveTo(crds[0], crds[1], crds[2], crds[3], crds[4], crds[5]); break;
        case Close: close(); break;
    }
}

/**
 * Appends a shape.
 */
public void append(Shape aShape)  { append(aShape.getPathIter(null)); }

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return _bounds!=null? _bounds : (_bounds=Shape.getBounds(this)); }

/**
 * Clears all segments from path.
 */
public void clear()  { _scount = _pcount = 0; _bounds = null; }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aTrans)  { return new PathPathIter(this, aTrans); }

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer();
    PathIter piter = getPathIter(null); double crds[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(crds)) {
        case MoveTo: sb.append(" m ").append(fmt(crds[0])).append(' ').append(fmt(crds[1])); break;
        case LineTo: sb.append(" l ").append(fmt(crds[0])).append(' ').append(fmt(crds[1])); break;
        case QuadTo: sb.append(" q ").append(fmt(crds[2])).append(' ').append(fmt(crds[3])); break;
        case CubicTo: sb.append(" c ").append(fmt(crds[4])).append(' ').append(fmt(crds[5])); break;
        case Close: sb.append(" cl"); break;
    }
    return getClass().getSimpleName() + sb;
}

// Used for print
private static String fmt(double aValue)  { return _fmt.format(aValue); }
private static java.text.DecimalFormat _fmt = new java.text.DecimalFormat("0.##");

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named path
    XMLElement e = new XMLElement("path");
    
    // Archive winding rule
    //if(_windingRule!=WIND_NON_ZERO) e.add("wind", "even-odd");

    // Archive individual elements/points
    PathIter piter = getPathIter(null); double pts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pts)) {
        
        // Handle MoveTo
        case MoveTo: XMLElement move = new XMLElement("mv");
            move.add("x", pts[0]); move.add("y", pts[1]); e.add(move); break;
        
        // Handle LineTo
        case LineTo: XMLElement line = new XMLElement("ln");
            line.add("x", pts[0]); line.add("y", pts[1]); e.add(line); break;
            
        // Handle QuadTo
        case QuadTo: XMLElement quad = new XMLElement("qd");
            quad.add("cx", pts[0]); quad.add("cy", pts[1]);
            quad.add("x", pts[2]); quad.add("y", pts[3]);
            e.add(quad); break;

        // Handle CubicTo
        case CubicTo: XMLElement curve = new XMLElement("cv");
            curve.add("cp1x", pts[0]); curve.add("cp1y", pts[1]);
            curve.add("cp2x", pts[2]); curve.add("cp2y", pts[3]);
            curve.add("x", pts[4]); curve.add("y", pts[5]);
            e.add(curve); break;

        // Handle Close
        case Close: XMLElement close = new XMLElement("cl"); e.add(close); break;
    }
    
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive winding rule
    //if(anElement.getAttributeValue("wind", "non-zero").equals("even-odd")) setWindingRule(WIND_EVEN_ODD);

    // Unarchive individual elements/points
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement e = anElement.get(i);
        if(e.getName().equals("mv"))
            moveTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("ln"))
            lineTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("qd"))
            quadTo(e.getAttributeFloatValue("cx"), e.getAttributeFloatValue("cy"),
                e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("cv"))
            curveTo(e.getAttributeFloatValue("cp1x"), e.getAttributeFloatValue("cp1y"),
                e.getAttributeFloatValue("cp2x"), e.getAttributeFloatValue("cp2y"),
                e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("cl"))
            close();
    }
    
    // Return this path
    return this;
}

/**
 * Returns a path for given path iterator.
 */
public static Path get(PathIter aPI)  { Path p = new Path(); p.append(aPI); return p; }

/**
 * Returns a path for given shape.
 */
public static Path get(Shape aShape)  { Path p = new Path(); p.append(aShape.getPathIter(null)); return p; }

/**
 * A PathIter for Path.
 */
private static class PathPathIter implements PathIter {
    
    // Ivars
    Path _path; Transform _trans; int _sindex, _pindex;
    
    /** Creates a new PathPathIter for Path. */
    PathPathIter(Path aPath, Transform aTrans)  { _path = aPath; _trans = aTrans; }
    
    /** Returns whether PathIter has another segement. */
    public boolean hasNext()  { return _sindex<_path._scount; }
    
    /** Returns the next segment. */
    public Seg getNext(double coords[])
    {
        Seg seg = _path._segs[_sindex++]; int count = seg.getCount();
        for(int i=0;i<count;i++) { coords[i*2] = _path._points[_pindex++]; coords[i*2+1] = _path._points[_pindex++]; }
        if(_trans!=null) _trans.transform(coords, count);
        return seg;
    }
}

}