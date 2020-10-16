package snap.geom;
import java.util.List;
import snap.util.ArrayUtils;

/**
 * A Shape subclass that represents one or more polygons.
 */
public class PolygonList extends Shape {

    // The polygons
    Polygon     _polys[] = new Polygon[0];
    
    // The number of polygons
    int         _plen;
    
    // The current polygon
    Polygon     _poly;

    /**
     * Creates a new PolygonList.
     */
    public PolygonList()  { }

    /**
     * Creates a new PolygonList for given Polygons.
     */
    public PolygonList(Polygon ... thePolys)
    {
        _polys = thePolys; _plen = thePolys.length;
    }

    /**
     * Creates a new PolygonList for given Polygons.
     */
    public PolygonList(List <Polygon> thePolys)
    {
        this(thePolys.toArray(new Polygon[thePolys.size()]));
    }

    /**
     * Creates a new PolygonList for given Shape.
     */
    public PolygonList(Shape aShape)
    {
        this(aShape.getPathIter(null));
    }

    /**
     * Creates a PolygonList for given PathIter.
     */
    public PolygonList(PathIter aPI)
    {
        aPI = new Path(aPI).getFlat().getPathIter(null);
        double pnts[] = new double[6];
        while (aPI.hasNext()) switch (aPI.getNext(pnts)) {
            case MoveTo: moveTo(pnts[0], pnts[1]); break;
            case LineTo: lineTo(pnts[0], pnts[1]); break;
            case QuadTo: quadTo(pnts[0], pnts[1], pnts[2], pnts[3]); break;
            case CubicTo: curveTo(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
            case Close: break; // All Polygons assumed to be closed
        }
    }

    /**
     * Returns the Polygons.
     */
    public Polygon[] getPolys()  { return _polys; }

    /**
     * Returns the number of polygons.
     */
    public int getPolyCount()  { return _plen; }

    /**
     * Returns the individual polygon at given index.
     */
    public Polygon getPoly(int anIndex)  { return _polys[anIndex]; }

    /**
     * Adds a polygon.
     */
    public void addPoly(Polygon aPoly, int anIndex)
    {
        _polys = ArrayUtils.add(_polys, aPoly, anIndex); _plen++;
        shapeChanged();
    }

    /**
     * Returns the last polygon.
     */
    public Polygon getLast()  { return _plen>0 ? _polys[_plen-1] : null; }

    /**
     * Returns the last polygon last point.
     */
    public Point getLastPoint()  { return _plen>0 ? getLast().getLastPoint() : null; }

    /**
     * Moveto.
     */
    public void moveTo(double x, double y)
    {
        // Handle two consecutive MoveTos
        if (_poly!=null && _poly.getPointCount()==1)
            _poly.setPoint(0, x, y);
        else {
            _poly = new Polygon();
            addPoly(_poly, getPolyCount());
            _poly.addPoint(x, y);
        }
        shapeChanged();
    }

    /**
     * LineTo.
     */
    public void lineTo(double x, double y)
    {
        // If no poly, start one
        if (_poly==null) moveTo(0,0);

        // If closing last poly, just return
        if (_poly.getPointCount()>0 && Point.equals(x, y, _poly.getX(0), _poly.getY(0))) return;

        // Add point and clear bounds
        _poly.addPoint(x,y);
        shapeChanged();
    }

    /**
     * QuadTo by adding lineTos.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        // If distance from control point to base line less than tolerance, just add line
        Point last = getLastPoint();
        double dist0 = Point.getDistance(last.x, last.y, x, y); if (dist0<.5) return;
        double dist1 = Line.getDistance(last.x, last.y, x, y, cpx, cpy);
        if (dist1<.5) {
            lineTo(x,y); return; }

        // Split curve at midpoint and add parts
        Quad c0 = new Quad(last.x, last.y, cpx, cpy, x, y), c1 = c0.split(.5);
        quadTo(c0.cpx, c0.cpy, c0.x1, c0.y1);
        quadTo(c1.cpx, c1.cpy, c1.x1, c1.y1);
    }

    /**
     * CubicTo by adding lineTos.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        // If distance from control points to base line less than tolerance, just add line
        Point last = getLastPoint();
        double dist0 = Point.getDistance(last.x, last.y, x, y); if (dist0<.5) return;
        double dist1 = Line.getDistance(last.x, last.y, x, y, cp1x, cp1y);
        double dist2 = Line.getDistance(last.x, last.y, x, y, cp2x, cp2y);
        if (dist1<.5 && dist2<.5) {
            lineTo(x,y); return; }

        // Split curve at midpoint and add parts
        Cubic c0 = new Cubic(last.x, last.y, cp1x, cp1y, cp2x, cp2y, x, y), c1 = c0.split(.5);
        curveTo(c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1);
        curveTo(c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1);
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        int pc = getPolyCount(); if (pc==0) return new Rect(0,0,0,0);
        Rect bnds = getPoly(0).getBounds();
        for (int i=1;i<pc;i++) bnds.union(getPoly(i).getBounds());
        return bnds;
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new PolyListIter(_polys, aTrans); }

    /**
     * PathIter for Line.
     */
    private static class PolyListIter extends PathIter {

        // Ivars
        PathIter _polyIters[], _pi;
        int plen, index;

        /** Create new LineIter. */
        PolyListIter(Polygon thePolys[], Transform at)
        {
            super(at); plen = thePolys.length;
            _polyIters = new PathIter[plen];
            for (int i=0;i<plen;i++)
                _polyIters[i] = thePolys[i].getPathIter(at);
            _pi = plen>0 ? _polyIters[0] : null;
        }

        /** Returns whether there are more polygons. */
        public boolean hasNext() { return _pi!=null && _pi.hasNext(); }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            Seg seg = _pi.getNext(coords);
            while (!_pi.hasNext() && index<plen)
                _pi = _polyIters[index++];
            return seg;
        }
    }
}