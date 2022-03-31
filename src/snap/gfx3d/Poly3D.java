/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.gfx.Color;
import java.util.Arrays;

/**
 * This class represents a polygon surface in 3D space.
 */
public class Poly3D extends FacetShape implements Cloneable {

    // The float array to hold actual vertex point components
    private float[]  _pointsArray = new float[24];

    // The number of components in vertex points array
    private int  _pointsArrayLen = 0;

    // The float array to hold actual vertex color components
    private float[]  _colorsArray = new float[0];

    // The number of components in vertex colors array
    private int  _colorsArrayLen = 0;

    // Constants
    public static final int POINT_COMP_COUNT = 3;
    public static final int COLOR_COMP_COUNT = 4;

    /**
     * Constructor.
     */
    public Poly3D()
    {
        super();
    }

    /**
     * Returns the vertex points components array.
     */
    public float[] getPointsArray()
    {
        trim();
        return _pointsArray;
    }

    /**
     * Returns the number of vertex points in array.
     */
    @Override
    public int getPointCount()  { return _pointsArrayLen / POINT_COMP_COUNT; }

    /**
     * Returns the Point3D at index.
     */
    @Override
    public Point3D getPoint(int anIndex)
    {
        return getPoint(new Point3D(), anIndex);
    }

    /**
     * Returns the Point3D at index.
     */
    public Point3D getPoint(Point3D aPoint, int anIndex)
    {
        int index = anIndex * POINT_COMP_COUNT;
        aPoint.x = _pointsArray[index + 0];
        aPoint.y = _pointsArray[index + 1];
        aPoint.z = _pointsArray[index + 2];
        return aPoint;
    }

    /**
     * Adds value triplet to array.
     */
    public void addPoint(double aVal1, double aVal2, double aVal3)
    {
        // Expand Vertex components array if needed
        if (_pointsArrayLen + 3 > _pointsArray.length)
            _pointsArray = Arrays.copyOf(_pointsArray, Math.max(_pointsArray.length * 2, 24));

        // Add values
        _pointsArray[_pointsArrayLen++] = (float) aVal1;
        _pointsArray[_pointsArrayLen++] = (float) aVal2;
        _pointsArray[_pointsArrayLen++] = (float) aVal3;
    }

    /**
     * Returns the vertex colorscomponents array.
     */
    public float[] getColorsArray()
    {
        trim();
        return _colorsArray;
    }

    /**
     * Adds a color to vertex color components array.
     */
    public void addColor(Color aColor)
    {
        // Expand color components array if needed
        if (_colorsArrayLen + COLOR_COMP_COUNT > _colorsArray.length)
            _colorsArray = Arrays.copyOf(_colorsArray, Math.max(_colorsArray.length * 2, 24));

        // Add values
        _colorsArray[_colorsArrayLen++] = (float) aColor.getRed();
        _colorsArray[_colorsArrayLen++] = (float) aColor.getGreen();
        _colorsArray[_colorsArrayLen++] = (float) aColor.getBlue();
        if (COLOR_COMP_COUNT > 3)
            _colorsArray[_colorsArrayLen++] = (float) aColor.getAlpha();
    }

    /**
     * Returns the normal of the path3d. Right hand rule for clockwise/counter-clockwise defined polygons.
     */
    @Override
    protected Vector3D createNormal()
    {
        return Vector3D.getNormalForPoints3fv(new Vector3D(), _pointsArray, getPointCount());
    }

    /**
     * Returns the 2D shape for the path3d (should only be called when path is facing Z).
     */
    @Override
    public Shape getShape2D()
    {
        return new Shape() {
            public PathIter getPathIter(Transform aT)
            {
                return new PointArrayPathIter2D(_pointsArray, _pointsArrayLen, null);
            }
        };
    }

    /**
     * Creates a VertexArray of path triangles.
     */
    @Override
    protected VertexArray createVertexArray()
    {
        // Create/configure VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(getColor());
        vertexArray.setDoubleSided(isDoubleSided());

        // If no normal, just return empty
        Vector3D pathNormal = getNormal();
        if (Double.isNaN(pathNormal.x))
            return vertexArray;

        // Get transform matrix to transform this path to/from facing Z
        Matrix3D localToFacingZ = getTransformToAlignToVector(0, 0, 1);
        Matrix3D facingZToLocal = localToFacingZ.clone().invert();

        // Get copy of path facing Z
        Poly3D polyFacingZ = copyForMatrix(localToFacingZ);
        float zVal = (float) polyFacingZ.getMinZ();

        // Get Poly2D, break into triangles
        Shape poly2D = polyFacingZ.getShape2D();
        Polygon[] triangles = Polygon.getConvexPolys(poly2D, 3);

        // Create loop variables
        Vector3D pointsNormal = new Vector3D();
        float[] pointsArray = new float[9];

        // Get Path3Ds
        for (Polygon triangle : triangles) {

            // Get triangle points
            for (int i = 0, i3 = 0; i < 3; i++, i3 += 3) {
                pointsArray[i3 + 0] = (float) triangle.getX(i);
                pointsArray[i3 + 1] = (float) triangle.getY(i);
                pointsArray[i3 + 2] = zVal;
            }

            // Transform points back and add to VertexArray
            facingZToLocal.transformArrayPoints(pointsArray, 3);

            // If points normal facing backwards, reverse points (swap p0 and p2)
            Vector3D.getNormalForPoints3fv(pointsNormal, pointsArray, 3);
            boolean addReversed = !pointsNormal.equals(pathNormal);

            // Add points
            if (addReversed) {
                vertexArray.addPoint(pointsArray[6], pointsArray[7], pointsArray[8]);
                vertexArray.addPoint(pointsArray[3], pointsArray[4], pointsArray[5]);
                vertexArray.addPoint(pointsArray[0], pointsArray[1], pointsArray[2]);
            }
            else {
                vertexArray.addPoint(pointsArray[0], pointsArray[1], pointsArray[2]);
                vertexArray.addPoint(pointsArray[3], pointsArray[4], pointsArray[5]);
                vertexArray.addPoint(pointsArray[6], pointsArray[7], pointsArray[8]);
            }
        }

        // Add colors
        //for (Color color : _colors)
        //    vertexArray.addColor(color);

        // Handle Stroke: Create/add stroke VertexArray
        if (getStrokeColor() != null) {
            VertexArray strokeVA = getStrokeVertexArray();
            vertexArray.setLast(strokeVA);
        }

        // Handle Painter: Create/add painterVertexArray
        Painter3D painter3D = getPainter();
        if (painter3D != null) {
            VertexArray painterVA = getPainterVertexArray();
            vertexArray.setLast(painterVA);
        }

        // Return
        return vertexArray;
    }

    /**
     * Returns a VertexArray for path stroke.
     */
    protected VertexArray getStrokeVertexArray()
    {
        // Get info
        Vector3D facetNormal = getNormal();
        Color strokeColor = getStrokeColor();
        double strokeWidth = getStroke() != null ? getStroke().getWidth() : 1;

        // Create/configure VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(strokeColor != null ? strokeColor : Color.BLACK);

        // Path3D iteration vars
        int pointCount = getPointCount();
        float[] pointsArray = getPointsArray();
        Point3D p0 = new Point3D(pointsArray[0], pointsArray[1], pointsArray[2]);
        Point3D p1 = new Point3D();

        // Iterate over points and add line stroke
        for (int i = 0, i3 = 0; i < pointCount; i++, i3 += 3) {
            p1.x = pointsArray[i3];
            p1.y = pointsArray[i3 + 1];
            p1.z = pointsArray[i3 + 2];
            VertexArrayUtils.addLineStrokePoints(vertexArray, p0, p1, facetNormal, strokeWidth);
            p0.setPoint(p1);
        }

        // Return
        return vertexArray;
    }

    /**
     * Transforms the path by the given transform matrix.
     */
    public void transform(Matrix3D xform)
    {
        float[] pointsArray = getPointsArray();
        int pointCount = getPointCount();
        xform.transformArrayPoints(pointsArray, pointCount);
        clearCachedValues();
    }

    /**
     * Copies path for given transform matrix.
     */
    @Override
    public Poly3D copyForMatrix(Matrix3D aTrans)
    {
        Poly3D copy = clone();
        copy.transform(aTrans);
        return copy;
    }

    /**
     * Reverses the path3d.
     */
    public void reverse()
    {
        // Reverse PointsArray
        float[] pointsArray = getPointsArray();
        int pointCount = getPointCount();
        reverseArray(pointsArray, pointCount, POINT_COMP_COUNT);

        // Reverse ColorsArray
        float[] colorsArray = getColorsArray();
        int colorCount = _colorsArrayLen / COLOR_COMP_COUNT;
        reverseArray(colorsArray, colorCount, COLOR_COMP_COUNT);

        // Clear cached values
        clearCachedValues();
    }

    /**
     * Trims arrays.
     */
    public void trim()
    {
        // Trim PointsArray
        if (_pointsArray.length != _pointsArrayLen)
            _pointsArray = Arrays.copyOf(_pointsArray, _pointsArrayLen);

        // Trim ColorsArray
        if (_colorsArray.length != _colorsArrayLen)
            _colorsArray = Arrays.copyOf(_colorsArray, _colorsArrayLen);
    }

    /**
     * Returns the bounds.
     */
    @Override
    protected Bounds3D createBounds3D()
    {
        // Create and init bounds
        Bounds3D bounds = new Bounds3D();
        bounds.setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        bounds.setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        // Iterate over points
        float[] pointsArray = getPointsArray();
        int arrayCount = getPointCount() * POINT_COMP_COUNT;
        for (int i = 0; i < arrayCount; i += POINT_COMP_COUNT) {
            float x = pointsArray[i + 0];
            float y = pointsArray[i + 1];
            float z = pointsArray[i + 2];
            bounds.addXYZ(x, y, z);
        }

        // Return
        return bounds;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public Poly3D clone()
    {
        // Trim
        trim();

        // Do normal version
        Poly3D clone  = (Poly3D) super.clone();

        // Clone arrays
        clone._pointsArray = _pointsArray.clone();
        clone._colorsArray = _colorsArray.clone();

        // Return clone
        return clone;
    }

    /**
     * Standard toStringProps implementation.
     */
    @Override
    public String toStringProps()
    {
        String superProps = super.toStringProps();
        return superProps + ", PointCount=" + getPointCount();
    }

    /**
     * Reverses a float array with given number of records of given component count.
     */
    private static void reverseArray(float[] theArray, int recordCount, int componentCount)
    {
        // Get number of values in array
        int arrayCount = recordCount * componentCount;
        int halfCount = recordCount / 2;

        // Iterate over array records
        for (int recIndex = 0; recIndex < halfCount; recIndex++) {

            // Get array index for current record and opposite array index for swap record
            int arrayIndex = recIndex * componentCount;
            int oppositeIndex = arrayCount - arrayIndex - componentCount;

            // Iterate over components and swap
            for (int compIndex = 0; compIndex < componentCount; compIndex++) {
                float temp = theArray[arrayIndex + compIndex];
                theArray[arrayIndex + compIndex] = theArray[oppositeIndex + compIndex];
                theArray[oppositeIndex + compIndex] = temp;
            }
        }
    }

    /**
     * PathIter for PointArray.
     */
    public static class PointArrayPathIter2D extends PathIter {

        // Ivars
        private float[]  _pointsArray;
        private int  _pointCount;
        private int  _pointIndex;

        /**
         * Constructor.
         */
        public PointArrayPathIter2D(float[] aPointsArray, int aPointsArrayLength, Transform aTransform)
        {
            super(aTransform);
            _pointsArray = aPointsArray;
            _pointCount = aPointsArrayLength / POINT_COMP_COUNT;
            if (_pointCount == 0)
                _pointIndex = 999;
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext() { return _pointIndex <= _pointCount; }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            // Return a lineTo for each segment
            if (_pointIndex < _pointCount) {

                // Get X/Y for PointIndex
                int pointsArrayIndex = _pointIndex * POINT_COMP_COUNT;
                float x = _pointsArray[pointsArrayIndex + 0];
                float y = _pointsArray[pointsArrayIndex + 1];
                _pointIndex++;

                // Handle first segment
                if (_pointIndex == 1)
                    return moveTo(x, y, coords);

                // Handle successive segments
                return lineTo(x, y, coords);
            }

            // Close
            if (_pointIndex == _pointCount) {
                _pointIndex++;
                return close();
            }

            // Handle the impossible
            throw new RuntimeException("Poly3D.PointArrayPathIter2D: Index beyond bounds " + _pointIndex + " " + _pointCount);
        }
    }
}