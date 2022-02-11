/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.*;

/**
 * This class manages a list of shapes, cameras and lights.
 * 
 * Right now this is really like a World class. Eventually it should have it's own transform and be a subclass of
 * Shape3D, so it can hold a hierarchy of scenes.
 */
public class Scene3D {
    
    // Camera that renders the scene
    private Camera3D _camera;
    
    // Light that illuminates the scene
    private Light3D _light = new Light3D();
    
    // List of Shape3ds - the model
    protected List <Shape3D>  _shapes = new ArrayList<>();

    // The scene bounding box
    private Box3D  _boundsBox;
    
    /**
     * Constructor.
     */
    public Scene3D()
    {
        _camera = new Camera3D();
        _camera.setScene(this);
    }

    /**
     * Returns the camera that renders this scene.
     */
    public Camera3D getCamera()  { return _camera; }

    /**
     * Returns the light that illumiates this scene.
     */
    public Light3D getLight()  { return _light; }

    /**
     * Returns the shapes.
     */
    public List<Shape3D> getShapes()  { return _shapes; }

    /**
     * Returns the number of shapes in the shape list.
     */
    public int getShapeCount()  { return _shapes.size(); }

    /**
     * Returns the specific shape at the given index from the shape list.
     */
    public Shape3D getShape(int anIndex)  { return _shapes.get(anIndex); }

    /**
     * Adds a shape to the end of the shape list.
     */
    public void addShape(Shape3D aShape)
    {
        _shapes.add(aShape);
        _camera.sceneDidChange();
        _boundsBox = null;
    }

    /**
     * Removes the shape at the given index from the shape list.
     */
    public void removeShapes()
    {
        _shapes.clear();
        _camera.sceneDidChange();
        _boundsBox = null;
    }

    /**
     * Returns the bounds box.
     */
    public Box3D getBoundsBox()
    {
        // If already set, just return
        if (_boundsBox != null) return _boundsBox;

        // Get, set and return
        Box3D boundsBox = getBoundsBoxImpl();
        return _boundsBox = boundsBox;
    }

    /**
     * Returns the bounds box.
     */
    protected Box3D getBoundsBoxImpl()
    {
        // Get all shapes and first shape
        List<Shape3D> shapes = getShapes();
        Shape3D shape0 = shapes.size() > 0 ? shapes.get(0) : null;

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
     * Returns a path in camera coords for given path in local coords.
     */
    public Path3D localToCamera(Path3D aPath)
    {
        Transform3D localToCamera = _camera.getTransform();
        return aPath.copyForTransform(localToCamera);
    }

    /**
     * Returns the given vector in camera coords.
     */
    public Vector3D localToCameraForVector(double aX, double aY, double aZ)
    {
        Vector3D v2 = new Vector3D(aX, aY, aZ);
        Transform3D localToCamera = _camera.getTransform();
        localToCamera.transformVector(v2);
        return v2;
    }
}