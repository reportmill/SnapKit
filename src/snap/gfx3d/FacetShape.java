/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Shape;
import snap.util.MathUtils;

/**
 * This Shape3D subclass represents a surface.
 */
public abstract class FacetShape extends Shape3D {

    // The texture for the facet
    private Texture  _texture;

    // A Painter3D to paint the surface of path
    private Painter3D  _painter;

    // The path normal vector
    private Vector3D  _normal;

    // The VertexArray holding triangles of Path3D
    private VertexArray  _vertexArray;

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
        _vertexArray = null;
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
        _vertexArray = null;
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
     * Returns a VertexArray of path triangles.
     */
    public VertexArray getVertexArray()
    {
        // If already set, just return
        if (_vertexArray != null) return _vertexArray;

        // Create, set, return
        VertexArray triVA = createVertexArray();
        return _vertexArray = triVA;
    }

    /**
     * Creates a VertexArray of path triangles.
     */
    protected abstract VertexArray createVertexArray();

    /**
     * Returns the painter VertexArray for 'painted' triangles on shape surface.
     */
    protected VertexArray getPainterVertexArray()
    {
        Painter3D painter = getPainter();
        Matrix3D painterToLocal = getPainterToLocal();
        VertexArray vertexArray = painter.getVertexArray();
        VertexArray vertexArrayLocal = vertexArray.copyForTransform(painterToLocal);
        return vertexArrayLocal;
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
        _vertexArray = null;
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