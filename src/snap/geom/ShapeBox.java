package snap.geom;

/**
 * A Shape implementation to present another shape in a given rect.
 */
public class ShapeBox extends Shape {
    
    // The encapsulated shape
    private Shape  _shape;
    
    /**
     * Creates a ShapeBox for given shape and rect.
     */
    public ShapeBox(Shape aShape, double aWidth, double aHeight)
    {
        _shape = aShape;
        _bounds = new Rect(0, 0, aWidth, aHeight);
    }

    /**
     * Returns the ecapsulated shape.
     */
    public Shape getShape()  { return _shape; }

    /**
     * Returns whether shape contains x/y.
     */
    public boolean contains(double aX, double aY)
    {
        if (!getBounds().contains(aX, aY)) return false;
        return _shape.contains(aX, aY);
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aT)  { return new BoxIter(aT); }

    /**
     * PathIter for ShapeBox.
     */
    private class BoxIter extends PathIter {

        // The encapsulated shape iterator
        PathIter _piter;

        /** Create new BoxIter. */
        BoxIter(Transform t)  { super(t); _piter = _shape.getPathIter(null); }

        /** Returns whether there are more segments. */
        public boolean hasNext() { return _piter.hasNext(); }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            Seg seg = _piter.getNext(coords);
            switch (seg) {
                case MoveTo: return moveTo(coords[0], coords[1], coords);
                case LineTo: return lineTo(coords[0], coords[1], coords);
                case QuadTo: return quadTo(coords[0], coords[1], coords[2], coords[3], coords);
                case CubicTo: return cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords);
                case Close: return close();
                default: throw new RuntimeException("ShapeBox.BoxIter: Unsuported seg " + seg);
            }
        }
    }
}