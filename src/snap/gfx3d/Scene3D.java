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
    private Camera  _camera;
    
    // Light that illuminates the scene
    private Light  _light = new Light();
    
    // List of Shape3ds - the model
    protected List <Shape3D>  _shapes = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public Scene3D()
    {
        _camera = new Camera();
        _camera.setScene(this);
    }

    /**
     * Returns the camera that renders this scene.
     */
    public Camera getCamera()  { return _camera; }

    /**
     * Returns the light that illumiates this scene.
     */
    public Light getLight()  { return _light; }

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
    }

    /**
     * Removes the shape at the given index from the shape list.
     */
    public void removeShapes()
    {
        _shapes.clear();
        _camera.sceneDidChange();
    }

    /**
     * Returns the transform 3d for the scene's camera.
     */
    public Transform3D getLocalToCamera()
    {
        return _camera.getTransform();
    }

    /**
     * Returns a point in camera coords for given point in local coords.
     */
    public Point3D localToCamera(Point3D aPoint)
    {
        return localToCamera(aPoint.x, aPoint.y, aPoint.z);
    }

    /**
     * Returns a point in camera coords for given point in local coords.
     */
    public Point3D localToCamera(double aX, double aY, double aZ)
    {
        return getLocalToCamera().transformPoint(aX, aY, aZ);
    }

    /**
     * Returns a path in camera coords for given path in local coords.
     */
    public Path3D localToCamera(Path3D aPath)
    {
        return aPath.copyFor(getLocalToCamera());
    }

    /**
     * Returns the given vector in camera coords.
     */
    public Vector3D localToCamera(Vector3D aV3D)
    {
        return localToCameraForVector(aV3D.x, aV3D.y, aV3D.z);
    }

    /**
     * Returns the given vector in camera coords.
     */
    public Vector3D localToCameraForVector(double aX, double aY, double aZ)
    {
        Vector3D v2 = new Vector3D(aX, aY, aZ); v2.transform(getLocalToCamera()); return v2;
    }
}