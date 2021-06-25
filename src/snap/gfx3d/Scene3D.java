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
        v2.transform(localToCamera);
        return v2;
    }
}