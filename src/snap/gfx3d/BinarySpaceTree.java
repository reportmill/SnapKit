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

    // The node on same plane
    private BinarySpaceTree _planarNode;

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
        int paintOrder = compareShapePlanes(_shape, nodeShape);

        // If order not determined, return false
        if (paintOrder == ORDER_INDETERMINATE)
            return false;

        // Handle Node in front: Add node as FrontNode or to FrontNode
        if (paintOrder == ORDER_BACK_TO_FRONT) {

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
        }

        // Handle Node in front: Add node as FrontNode or to FrontNode
        else if (paintOrder == ORDER_FRONT_TO_BACK) {

            // If FrontNode not set, just set
            if (_backNode == null)
                _backNode = aNode;

            // Add node to FrontNode (swap out if fails)
            else {
                boolean didAdd = _backNode.addNode(aNode);
                if (!didAdd) {
                    aNode.addNode(_backNode);
                    _backNode = aNode;
                }
            }
        }

        // Handle Planar
        else {

            // If FrontNode not set, just set
            if (_planarNode == null)
                _planarNode = aNode;

            // Add node to FrontNode (swap out if fails)
            else {
                boolean didAdd = _planarNode.addNode(aNode);
                if (!didAdd) {
                    aNode.addNode(_planarNode);
                    _planarNode = aNode;
                }
            }
        }

        // Add Node to BackNode
        return true;
    }

    /**
     * Loads a list from this node.
     */
    public void loadBackToFrontList(List<FacetShape> sortedList)
    {
        // Add BackNodes
        if (_backNode != null)
            _backNode.loadBackToFrontList(sortedList);

        // Add Shape
        sortedList.add(_shape);

        // Add PlanarNodes
        if (_planarNode != null)
            _planarNode.loadBackToFrontList(sortedList);

        // Add FrontNodes
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
