/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;

/**
 * Hit detection between Ray and Shape/VertexArray/Triangle.
 */
public class HitDetector {

    // The current shape
    private FacetShape  _hitShape;

    // The current triangle VertexArray
    private VertexArray  _hitTriangleArray;

    // The hit indexes
    private int[]  _hitTriangleIndexArray = new int[3];

    // The current hit point
    private Point3D  _hitPoint = new Point3D();

    // The current barycentric point
    private Point3D  _baryPoint = new Point3D();

    // The current triangle points
    private Point3D  _vertex0 = new Point3D(), _vertex1 = new Point3D(), _vertex2 = new Point3D();

    // Constant for hit detection
    private static final double EPSILON = 0.0000001;

    /**
     * Returns the last hit shape.
     */
    public FacetShape getHitShape()  { return _hitShape; }

    /**
     * Returns the last hit triangle VertexArray.
     */
    public VertexArray getHitTriangleArray()  { return _hitTriangleArray; }

    /**
     * Returns the array of index for hit triangle in triangle VertexArray.
     */
    public int[] getHitTriangleIndexArray()  { return _hitTriangleIndexArray; }

    /**
     * Returns the last hit point.
     */
    public Point3D getHitPoint()  { return _hitPoint; }

    /**
     * Returns the last hit barycentric point.
     */
    public Point3D getHitBarycentricPoint()  { return _baryPoint; }

    /**
     * Returns the Texture coord of hit point.
     */
    public Point getHitTexCoord()
    {
        float[] texCoordArray = _hitTriangleArray.getTexCoordArray();
        int index1 = _hitTriangleIndexArray[0] * 2;
        int index2 = _hitTriangleIndexArray[1] * 2;
        int index3 = _hitTriangleIndexArray[2] * 2;
        float tex1u = texCoordArray[index1];
        float tex1v = texCoordArray[index1 + 1];
        float tex2u = texCoordArray[index2];
        float tex2v = texCoordArray[index2 + 1];
        float tex3u = texCoordArray[index3];
        float tex3v = texCoordArray[index3 + 1];
        double texU = tex1u * _baryPoint.x + tex2u * _baryPoint.y + tex3u * _baryPoint.z;
        double texV = tex1v * _baryPoint.x + tex2v * _baryPoint.y + tex3v * _baryPoint.z;
        return new Point(texU, texV);
    }

    /**
     * Returns whether ray hits shape.
     */
    public boolean isRayHitShape(Point3D rayOrigin, Vector3D rayDir, Shape3D aShape)
    {
        // Handle FacetShape
        if (aShape instanceof FacetShape) {

            // If single sided and shape.Normal is same dir as ray, return null
            FacetShape facetShape = (FacetShape) aShape;
            if (!facetShape.isDoubleSided()) {
                Vector3D normal = facetShape.getNormal();
                if (rayDir.isAligned(normal, true))
                    return false;
            }

            // Get triangles VertexArray and check triangles
            _hitTriangleArray = aShape.getVertexArray();
            boolean isHit = isRayHitTriangleArray(rayOrigin, rayDir);
            if (isHit)
                _hitShape = facetShape;
            return isHit;
        }

        // Handle anything else: Complain and return null
        System.err.println("Ray.isRayHitShape: Unsupported shape class: " + aShape.getClass());
        return false;
    }

    /**
     * Returns whether ray hits VertexArray.
     */
    public boolean isRayHitTriangleArray(Point3D rayOrigin, Vector3D rayDir)
    {
        // Get pointArray, indexArray and pointCount
        float[] pointArray = _hitTriangleArray.getPointArray();
        int[] indexArray = _hitTriangleArray.getIndexArray();
        int pointCount = indexArray.length;

        // Iterate over indexArray points
        for (int i = 0; i + 2 < pointCount; i += 3) {
            int v0i = indexArray[i] * 3;
            int v1i = indexArray[i + 1] * 3;
            int v2i = indexArray[i + 2] * 3;
            _vertex0.setPoint(pointArray[v0i], pointArray[v0i + 1], pointArray[v0i + 2]);
            _vertex1.setPoint(pointArray[v1i], pointArray[v1i + 1], pointArray[v1i + 2]);
            _vertex2.setPoint(pointArray[v2i], pointArray[v2i + 1], pointArray[v2i + 2]);
            boolean hit = isRayHitTriangle(rayOrigin, rayDir);
            if (hit) {
                _hitTriangleIndexArray[0] = indexArray[i];
                _hitTriangleIndexArray[1] = indexArray[i + 1];
                _hitTriangleIndexArray[2] = indexArray[i + 2];
                return true;
            }
        }

        // Return false since no hit
        return false;
    }

    /**
     * Returns whether ray hits triangle defined by TriangleArray vertex points.
     *
     * Uses Moller–Trumbore from wiki: https://en.wikipedia.org/wiki/Möller–Trumbore_intersection_algorithm
     *
     * Also described at scratchapixel:
     *     https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-rendering-a-triangle/
     *          ./moller-trumbore-ray-triangle-intersection
     */
    public boolean isRayHitTriangle(Point3D rayOrigin, Vector3D rayDir)
    {
        // Get edge vectors
        Vector3D edgeV0V1 = new Vector3D(_vertex0, _vertex1);
        Vector3D edgeV0V2 = new Vector3D(_vertex0, _vertex2);

        // Get ray x edge perpendicular and bail if also perpendicular to other edge (ray is parallel to triangle)
        Vector3D rayEdge2Perpendicular = rayDir.getCrossProduct(edgeV0V2);
        double det = edgeV0V1.getDotProduct(rayEdge2Perpendicular);
        if (-EPSILON < det && det < EPSILON)
            return false;

        // Get barycentric point U value (return if out of range)
        Vector3D s = new Vector3D(_vertex0, rayOrigin);
        double invDet = 1 / det;
        double u = invDet * s.getDotProduct(rayEdge2Perpendicular);
        if (u < 0 || u > 1)
            return false;

        // Get barycentric point V value (return if out of range)
        Vector3D q = s.getCrossProduct(edgeV0V1);
        double v = invDet * rayDir.getDotProduct(q);
        if (v < 0 || u + v > 1)
            return false;

        // Compute t to find out where the intersection point is on the line.
        double t = invDet * edgeV0V2.getDotProduct(q);
        if (t > EPSILON) {

            // Calculate hit point XYZ values
            double hitX = rayOrigin.x + rayDir.x * t;
            double hitY = rayOrigin.y + rayDir.y * t;
            double hitZ = rayOrigin.z + rayDir.z * t;
            double w = 1 - u - v;

            // Set HitPoint and Barycentric point
            _hitPoint.setPoint(hitX, hitY, hitZ); //hitPoint.scaleAdd(t, rayVector, rayOrigin);
            _baryPoint.setPoint(w, u, v);
            return true;
        }

        // This means that there is a line intersection but not a ray intersection.
        return false;
    }
}