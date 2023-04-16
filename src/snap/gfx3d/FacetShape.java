/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Polygon;
import snap.geom.Shape;
import snap.gfx.Image;
import snap.util.MathUtils;

/**
 * This Shape3D subclass represents a surface.
 */
public abstract class FacetShape extends Shape3D {

    // The texture for the facet
    private Texture  _texture;

    // A Painter3D to paint the surface of shape
    private Painter3D  _painter;

    // The surface normal vector
    private Vector3D  _normal;

    // The VertexArray holding triangles of surface
    protected VertexArray  _triangleArray;

    /**
     * Returns the texture to render on the facet shape surface.
     */
    public Texture getTexture()  { return _texture; }

    /**
     * Sets the texture to render on the facet shape surface.
     */
    public void setTexture(Texture aTexture)
    {
        _texture = aTexture;
        _triangleArray = null;

        // If texture not loaded, have it do repaint when loaded
        if (aTexture != null) {
            Image image = aTexture.getImage();
            if (!image.isLoaded())
                image.addLoadListener(() -> { if(getParent() != null) getParent().repaintShape(); });
        }
    }

    /**
     * Returns the painter to render texture for this path.
     */
    public Painter3D getPainter()  { return _painter; }

    /**
     * Sets the painter to render texture for this path.
     */
    public void setPainter(Painter3D aPntr)
    {
        _painter = aPntr;
        _triangleArray = null;
    }

    /**
     * Returns the normal of the facet.
     */
    public Vector3D getNormal()
    {
         if (_normal != null) return _normal;
         Vector3D normal = createNormal();
         return _normal = normal;
    }

    /**
     * Returns the normal of the facet.
     */
    protected abstract Vector3D createNormal();

    /**
     * Returns the number of points in the facet.
     */
    public abstract int getPointCount();

    /**
     * Returns the Point3D at given index.
     */
    public abstract Point3D getPoint(int anIndex);

    /**
     * Returns the 2D shape for the FacetShape (should only be called when path is facing Z).
     */
    public abstract Shape getShape2D();

    /**
     * Returns a VertexArray of shape surface triangles.
     */
    public VertexArray getTriangleArray()
    {
        // If already set, just return
        if (_triangleArray != null) return _triangleArray;

        // Create, set, return
        VertexArray triVA = createVertexArrays();
        return _triangleArray = triVA;
    }

    /**
     * Creates the VertexArrays.
     */
    protected VertexArray createVertexArrays()
    {
        // Get Surface VertexArray
        VertexArray surfaceVertexArray = createTriangleArray();
        if (surfaceVertexArray == null)
            return null;

        // Get Stroke VertexArray
        VertexArray strokeVertexArray = getStrokeTriangleArray();
        if (strokeVertexArray != null)
            surfaceVertexArray.setLast(strokeVertexArray);

        // Get Painter VertexArray
        Painter3D painter3D = getPainter();
        if (painter3D != null) {
            VertexArray painterVA = getPainterVertexArray();
            surfaceVertexArray.setLast(painterVA);
        }

        // Return
        return surfaceVertexArray;
    }

    /**
     * Creates a VertexArray of shape surface triangles.
     */
    protected VertexArray createTriangleArray()
    {
        // Create/configure VertexArray
        VertexArray triangleArray = new VertexArray();
        triangleArray.setColor(getColor());
        triangleArray.setDoubleSided(isDoubleSided());

        // If no normal, just return empty
        Vector3D pathNormal = getNormal();
        if (Double.isNaN(pathNormal.x))
            return triangleArray;

        // Get transform matrix to transform this path to/from facing Z
        Matrix3D xfmToZ = getTransformToAlignToVector(0, 0, 1);
        Matrix3D xfmFromZ = xfmToZ.clone().invert();

        // Get copy of path facing Z
        FacetShape pathFacingZ = copyForMatrix(xfmToZ);
        double zVal = pathFacingZ.getMinZ();

        // Get Path2D, break into triangles
        Shape path2D = pathFacingZ.getShape2D();
        Polygon[] triangles = Polygon.getConvexPolygonsWithMaxSideCount(path2D, 3);

        // Create loop variables
        Point3D p0 = new Point3D(0, 0, 0);
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(0, 0, 0);
        Point3D[] points = { p0, p1, p2 };
        Vector3D pointsNormal = new Vector3D(0, 0, 0);

        // Get Path3Ds
        for (Polygon triangle : triangles) {

            // Get triangle points
            p0.x = triangle.getPointX(0);
            p0.y = triangle.getPointY(0);
            p1.x = triangle.getPointX(1);
            p1.y = triangle.getPointY(1);
            p2.x = triangle.getPointX(2);
            p2.y = triangle.getPointY(2);
            p0.z = p1.z = p2.z = zVal;

            // Transform points back and add to VertexArray
            xfmFromZ.transformPoint(p0);
            xfmFromZ.transformPoint(p1);
            xfmFromZ.transformPoint(p2);

            // If points normal facing backwards, reverse points (swap p0 and p2)
            Vector3D.getNormalForPoints(pointsNormal, points);
            if (!pointsNormal.equals(pathNormal)) {
                double px = p0.x, py = p0.y, pz = p0.z;
                p0.x = p2.x; p0.y = p2.y; p0.z = p2.z;
                p2.x = px; p2.y = py; p2.z = pz;
            }

            // Add points to VertexArray
            triangleArray.addPoint(p0.x, p0.y, p0.z);
            triangleArray.addPoint(p1.x, p1.y, p1.z);
            triangleArray.addPoint(p2.x, p2.y, p2.z);
        }

        // Return
        return triangleArray;
    }

    /**
     * Returns a VertexArray for path stroke.
     */
    protected VertexArray getStrokeTriangleArray()  { return null; }

    /**
     * Returns the painter VertexArray for 'painted' triangles on shape surface.
     */
    protected VertexArray getPainterVertexArray()
    {
        // Get Painter (just return if null)
        Painter3D painter = getPainter();
        if (painter == null)
            return null;

        // Get Painter VertexArray in this shape coords
        Matrix3D painterToLocal = getPainterToLocal();
        VertexArray painterVertexArray = painter.getTriangleArray();
        VertexArray painterVertexArrayInLocalCoords = painterVertexArray.copyForTransform(painterToLocal);

        // Return
        return painterVertexArrayInLocalCoords;
    }

    /**
     * Reverses the path3d.
     */
    public abstract void reverse();

    /**
     * Returns the transform from Painter to this shape.
     */
    public Matrix3D getPainterToLocal()
    {
        // Create transform and translate to Path.Center
        Matrix3D painterToLocal = new Matrix3D();
        Point3D pathCenter = getBoundsCenter();
        painterToLocal.translate(pathCenter.x, pathCenter.y, pathCenter.z);

        // Rotate by angle between painter and path normals (around axis perpendicular to them)
        Vector3D pntrNormal = new Vector3D(0, 0, 1);
        Vector3D facetNormal = getNormal();
        double angle = pntrNormal.getAngleBetween(facetNormal);

        // If angle 180 deg, rotate about Y
        if (MathUtils.equals(angle, 180))
            painterToLocal.rotateY(180);

            // If angle non-zero, get perpendicular and rotate about that
        else if (!MathUtils.equalsZero(angle)) {
            Vector3D rotateAxis = pntrNormal.getCrossProduct(facetNormal);
            painterToLocal.rotateAboutAxis(angle, rotateAxis.x, rotateAxis.y, rotateAxis.z);
        }

        // Translate to Painter.Center
        Painter3D painter = getPainter();
        double pntrW = painter.getWidth();
        double pntrH = painter.getHeight();
        Point3D pntrCenter = new Point3D(pntrW / 2, pntrH / 2, 0);
        painterToLocal.translate(-pntrCenter.x, -pntrCenter.y, -pntrCenter.z);

        // Return
        return painterToLocal;
    }

    /**
     * Returns the transform to make this FacetShape align with given vector.
     */
    public Matrix3D getTransformToAlignToVector(double aX, double aY, double aZ)
    {
        // Get angle between this FacetShape.Normal and given vector
        Vector3D vector = new Vector3D(aX, aY, aZ);
        Vector3D norm = getNormal();
        double angle = norm.getAngleBetween(vector);
        if (angle == 0 || angle == 180) // THIS IS WRONG - NO 180!!!
            return Matrix3D.IDENTITY;

        // The point of rotation is located at the shape's center
        Point3D rotOrigin = getBoundsCenter();

        // Get axis about which to rotate the path (its the cross product of Path3D.Normal and given vector)
        Vector3D rotAxis = norm.getCrossProduct(vector);

        // Get transform
        Matrix3D xform = new Matrix3D();
        xform.translate(rotOrigin.x, rotOrigin.y, rotOrigin.z);
        xform.rotateAboutAxis(angle, rotAxis.x, rotAxis.y, rotAxis.z);
        xform.translate(-rotOrigin.x, -rotOrigin.y, -rotOrigin.z);
        return xform;
    }

    /**
     * Clears cached values when path changes.
     */
    @Override
    protected void clearCachedValues()
    {
        super.clearCachedValues();
        _normal = null;
        _triangleArray = null;
    }

    /**
     * Copies path for given transform matrix.
     */
    public abstract FacetShape copyForMatrix(Matrix3D aTrans);

    /**
     * Standard clone implementation.
     */
    @Override
    public FacetShape clone()
    {
        // Normal clone
        FacetShape clone;
        try { clone = (FacetShape) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }

        // Clone painter
        if (_painter != null)
            clone._painter = _painter.clone();

        // Return
        return clone;
    }
}