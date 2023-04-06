/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.MathUtils;
import java.util.List;

/**
 * This class is a simple implementation of a Binary Space Partitioning Tree.
 * By recursively adding nodes (shapes) this separates nodes into nodes in front and nodes not in front.
 */
public class BinarySpaceTree {

    // The Shape3D at this node
    private FacetShape  _shape;

    // The node definitely in front of shape plane
    private BinarySpaceTree _frontNode;

    // The node not in front (not necessarily geometrically in behind shape plane)
    private BinarySpaceTree _backNode;

    // Constants for comparison/ordering of Path3Ds
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0;
    public static final int ORDER_INDETERMINATE = 2;

    /**
     * Constructor.
     */
    public BinarySpaceTree(FacetShape aShape)
    {
        super();
        _shape = aShape;
    }

    /**
     * Returns the shape.
     */
    public FacetShape getShape()  { return _shape; }

    /**
     * Adds a node.
     */
    public boolean addNode(BinarySpaceTree aNode)
    {
        // Get paint order of this node shape and given node shape
        FacetShape nodeShape = aNode.getShape();
        int paintOrder = compareShapesForPaintOrder(_shape, nodeShape);

        // Handle Node in back: Just return failure
        if (paintOrder == Sort3D.ORDER_FRONT_TO_BACK)
            return false;

        // Handle Node in front: Add node as FrontNode or to FrontNode
        if (paintOrder == Sort3D.ORDER_BACK_TO_FRONT) {

            // If FrontNode not set, just set
            if (_frontNode == null)
                _frontNode = aNode;

            // Add node to FrontNode (swap out if fails)
            else {
                boolean didAdd = _frontNode.addNode(aNode);
                if (!didAdd) {
                    aNode.addNode(_frontNode);
                    _frontNode = aNode;
                }
            }

            // Return success
            return true;
        }

        // If BackNode not set, just set and return
        if (_backNode == null) {
            _backNode = aNode;
            return true;
        }

        // Add Node to BackNode
        return _backNode.addNode(aNode);
    }

    /**
     * Loads a list from this node.
     */
    public void loadBackToFrontList(List<FacetShape> sortedList)
    {
        sortedList.add(_shape);
        if (_backNode != null)
            _backNode.loadBackToFrontList(sortedList);
        if (_frontNode != null)
            _frontNode.loadBackToFrontList(sortedList);
    }

    /**
     * Returns a BinarySpaceNode tree for given list of shapes.
     */
    public static BinarySpaceTree createBinarySpaceTree(List<FacetShape> theShapes)
    {
        // If empty list, just return
        if (theShapes.size() == 0) return null;

        // Create root node from first shape
        FacetShape shape0 = theShapes.get(0);
        BinarySpaceTree rootNode = new BinarySpaceTree(shape0);

        // Iterate over successive shapes
        for (int i = 1, iMax = theShapes.size(); i < iMax; i++) {

            // Get shape and create tree node
            FacetShape shape = theShapes.get(i);
            BinarySpaceTree shapeNode = new BinarySpaceTree(shape);

            // Add node to root
            boolean didAdd = rootNode.addNode(shapeNode);

            // If node not added, make it root and add other
            if (!didAdd) {
                shapeNode.addNode(rootNode);
                rootNode = shapeNode;
            }
        }

        // Return
        return rootNode;
    }

    /**
     * Sorts a list of shapes in paint order from back to front.
     */
    public static void sortShapesBackToFront(List<FacetShape> theShapes)
    {
        // Create BinarySpaceTree for shapes
        BinarySpaceTree binarySpaceTree = createBinarySpaceTree(theShapes);
        if (binarySpaceTree == null)
            return;

        // Clear list and reload in paint order
        theShapes.clear();
        binarySpaceTree.loadBackToFrontList(theShapes);
    }

    /**
     * Compares two facet shapes to determine whether they are definitively ordered.
     */
    private static int compareShapesForPaintOrder(FacetShape shape1, FacetShape shape2)
    {
        // Get ordering based on whether shape2 points are in front or behind shape1 points
        int comp1 = compareShapePlanes(shape1, shape2);
        int comp2 = compareShapePlanes(shape2, shape1);

        // If plane comparisons differ, we may have definitive result
        if (comp1 != comp2) {
            if (comp1 == ORDER_BACK_TO_FRONT || comp1 == ORDER_FRONT_TO_BACK)
                return comp1;
            if (comp2 == ORDER_BACK_TO_FRONT || comp2 == ORDER_FRONT_TO_BACK)
                return -comp2;
        }

        // If comparisons are ordered same, return Z order
        if (comp1 == ORDER_SAME || comp2 == ORDER_SAME) {
            double z1 = shape1.getMinZ();
            double z2 = shape2.getMinZ();
            return Double.compare(z1, z2);
        }

        // Return order INDETERMINATE
        return ORDER_INDETERMINATE;
    }

    /**
     * Returns whether (facet) shapes are ordered BACK_TO_FRONT OR FRONT_TO_BACK.
     * Returns ORDER_SAME if shapes are coplanar.
     * Returns INDETERMINATE if shape2 points lie on both sides of shape1 plane (straddle).
     */
    private static int compareShapePlanes(FacetShape shape1, FacetShape shape2)
    {
        int pointCount = shape2.getPointCount();
        double distToShape2 = 0;

        // Iterate over shape points to check distance for each to plane
        for (int i = 0; i < pointCount; i++) {

            // Get distance from shape point to plane - if zero distance, just skip (point is on path1 plane)
            Point3D shape2Point = shape2.getPoint(i);
            double pointDist = getDistanceFromShapePlaneToPoint(shape1, shape2Point);
            if (MathUtils.equalsZero(pointDist))
                continue;

            // If reference distance not yet set, set
            if (distToShape2 == 0)
                distToShape2 = pointDist;

            // If distance from loop point is opposite side of shape plane (sign flipped), return indeterminate
            else {
                boolean pointsOnBothSidesOfPlane = pointDist * distToShape2 < 0;
                if (pointsOnBothSidesOfPlane)
                    return ORDER_INDETERMINATE;
            }
        }

        // If positive distance, return BACK_TO_FRONT, if negative FRONT_TO_BACK, otherwise SAME (co-planar)
        if (distToShape2 > 0)
            return ORDER_BACK_TO_FRONT;
        if (distToShape2 < 0)
            return ORDER_FRONT_TO_BACK;
        return ORDER_SAME;
    }

    /**
     * Returns the distance from a given shape's plane to given point.
     */
    private static double getDistanceFromShapePlaneToPoint(FacetShape aShape, Point3D aPoint)
    {
        // A plane is defined by a normal (ABC) and a point on the plane (xyz): Ax + By + Cz + D = 0
        Vector3D normal = aShape.getNormal();
        Point3D planePoint = aShape.getPoint(0);

        // Calculate D from Ax + By + Cz + D = 0
        double Ax = normal.x * planePoint.x;
        double By = normal.y * planePoint.y;
        double Cz = normal.z * planePoint.z;
        double D = -Ax - By - Cz;

        // Distance is Ax + By + Cz + D / NormalMagnitude (magnitude of normal is 1)
        double dist = normal.x * aPoint.x + normal.y * aPoint.y + normal.z * aPoint.z + D;
        return Math.abs(dist) < .01 ? 0 : dist;
    }
}
