/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.ArrayUtils;

/**
 * This Shape3D subclass holds child shapes.
 */
public class ParentShape3D extends Shape3D {

    // The child shape array
    private Shape3D[]  _children = EMPTY_SHAPE_ARRAY;

    // Whether parent shape needs to rebuild children
    private boolean  _needsRebuildShape;

    // Constants
    private static final Shape3D[] EMPTY_SHAPE_ARRAY = new Shape3D[0];

    /**
     * Returns the number of children.
     */
    public int getChildCount()  { return getChildren().length; }

    /**
     * Returns the child at given index.
     */
    public Shape3D getChild(int anIndex)  { return _children[anIndex]; }

    /**
     * Returns the children.
     */
    public Shape3D[] getChildren()
    {
        buildShape();
        return _children;
    }

    /**
     * Sets the children.
     */
    public void setChildren(Shape3D[] theChildren)
    {
        // Cache old, set new
        Shape3D[] oldChildren = _children;
        _children = theChildren;

        // Set children parents
        for (Shape3D child : _children)
            child.setParent(this);

        // Clear others
        for (Shape3D child : oldChildren)
            if (!ArrayUtils.containsId(_children, child))
                child.setParent(null);

        // Clear cached values
        clearCachedValues();
    }

    /**
     * Adds a child.
     */
    public void addChild(Shape3D aShape)
    {
        addChild(aShape, getChildCount());
    }

    /**
     * Adds a child.
     */
    public void addChild(Shape3D aShape, int anIndex)
    {
        _children = ArrayUtils.add(_children, aShape, anIndex);
        aShape.setParent(this);
        clearCachedValues();
    }

    /**
     * Removes the child at given index.
     */
    public Shape3D removeChild(int anIndex)
    {
        Shape3D child = _children[anIndex];
        _children = ArrayUtils.remove(_children, anIndex);
        child.setParent(null);
        clearCachedValues();
        return child;
    }

    /**
     * Removes the given child.
     */
    public void removeChild(Shape3D aShape)
    {
        int index = ArrayUtils.indexOfId(_children, aShape);
        if (index >= 0)
            removeChild(index);
    }

    /**
     * Removes the shape at the given index from the shape list.
     */
    public void removeChildren()
    {
        setChildren(EMPTY_SHAPE_ARRAY);
    }

    /**
     * Override to calculate BoundsBox from children.
     */
    @Override
    protected Box3D createBoundsBox()
    {
        // Get all shapes and first shape
        Shape3D[] shapes = getChildren();
        Shape3D shape0 = shapes.length > 0 ? shapes[0] : null;

        // Create new Box3D from first shape
        Box3D boundsBox0 = shape0 != null ? shape0.getBoundsBox() : null;
        Box3D boundsBox = boundsBox0 != null ? boundsBox0.clone() : new Box3D();

        // Iterate over shapes to get total boundsBox
        for (Shape3D shape : shapes) {
            boundsBox.addXYZ(shape.getMinX(), shape.getMinY(), shape.getMinZ());
            boundsBox.addXYZ(shape.getMaxX(), shape.getMaxY(), shape.getMaxZ());
        }

        // Return total boundsBox
        return boundsBox;
    }

    /**
     * Calls to register for rebuild.
     */
    public void rebuildShape()
    {
        if (_needsRebuildShape) return;

        _needsRebuildShape = true;
    }

    /**
     * Calls to build shape.
     */
    public void buildShape()
    {
        if (!_needsRebuildShape) return;
        buildShapeImpl();
        _needsRebuildShape = false;
    }

    /**
     * Called to actually build shape.
     */
    protected void buildShapeImpl()  { }

    /**
     * Override to return null (parent don't have VertexArray).
     */
    public VertexArray getVertexArray()  { return null; }
}
