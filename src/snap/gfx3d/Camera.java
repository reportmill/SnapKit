/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropChangeSupport;
import snap.view.ViewEvent;

/**
 * This class represent a camera focusing on a scene and manages a display list of simple paths based on
 * the scene shapes and the camera transform.
 * 
 * Camera transform is currently relative to scene. At some point, that may become an option instead.
 * 
 * 3D conventions:
 * 
 *   Coordinate system: Right handed (not left handed)
 *   Polygon front: Right hand rule (counter-clockwise defined polygons face forward)
 *   Transforms: Row major notation (as opposed to column major, points are assumed row vectors) 
 */
public class Camera {
    
    // The scene being viewed
    private Scene3D  _scene;
    
    // Width, height, depth
    private double  _viewWidth, _viewHeight;

    // Rotation around y axis
    private double  _yaw = 0;
    
    // Rotation around x axis
    private double  _pitch = 0;
    
    // Rotation around z axis
    private double  _roll = 0;
    
    // Distance from center of scene to camera when in gimbal mode
    private double  _gimbalRadius;

    // Optimal distance from center of scene to camera when in gimbal mode, when explicitly set
    private double  _prefGimbalRadius;

    // Perspective
    private double  _focalLen = 60*72;

    // Whether camera is showing in Orthographic projection
    private boolean  _ortho;

    // Camera normal
    private Vector3D  _normal = new Vector3D(0, 0, -1);
    
    // Cached SceneToCamera transform
    private Matrix3D  _sceneToCamera, _cameraToClip, _cameraToView, _sceneToView;

    // The Renderer
    private Renderer  _renderer;

    // The MouseHandler
    private MouseHandler  _mouseHandler;

    // The PropChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String ViewWidth_Prop = "ViewWidth";
    public static final String ViewHeight_Prop = "ViewHeight";
    public static final String Yaw_Prop = "Yaw";
    public static final String Pitch_Prop = "Pitch";
    public static final String Roll_Prop = "Roll";
    public static final String FocalLength_Prop = "FocalLength";
    public static final String GimbalRadius_Prop = "GimbalRadius";
    public static final String Ortho_Prop = "Ortho";
    public static final String PrefGimbalRadius_Prop = "PrefGimbalRadius";
    public static final String Renderer_Prop = "Renderer";

    /**
     * Constructor.
     */
    public Camera()
    {
        _mouseHandler = new MouseHandler(this);
    }

    /**
     * Returns the scene this camera is associated with.
     */
    public Scene3D getScene()  { return _scene; }

    /**
     * Sets the scene this camera is associated with.
     */
    public void setScene(Scene3D aScene)  { _scene = aScene; }

    /**
     * Returns the width of the camera viewing plane.
     */
    public double getViewWidth()  { return _viewWidth; }

    /**
     * Sets the width of the camera viewing plane.
     */
    public void setViewWidth(double aValue)
    {
        if (aValue == _viewWidth) return;
        firePropChange(ViewWidth_Prop, _viewWidth, _viewWidth = aValue);
        clearCachedValues();
    }

    /**
     * Returns the height of the camera viewing plane.
     */
    public double getViewHeight()  { return _viewHeight; }

    /**
     * Sets the height of the camera viewing plane.
     */
    public void setViewHeight(double aValue)
    {
        if (aValue == _viewHeight) return;
        firePropChange(ViewHeight_Prop, _viewHeight, _viewHeight = aValue);
        clearCachedValues();
    }

    /**
     * Returns the rotation about the Y axis in degrees.
     */
    public double getYaw()  { return _yaw; }

    /**
     * Sets the rotation about the Y axis in degrees.
     */
    public void setYaw(double aValue)
    {
        if (aValue == _yaw) return;
        firePropChange(Yaw_Prop, _yaw, _yaw = aValue);
        clearCachedValues();
    }

    /**
     * Returns the rotation about the X axis in degrees.
     */
    public double getPitch()  { return _pitch; }

    /**
     * Sets the rotation about the X axis in degrees.
     */
    public void setPitch(double aValue)
    {
        if (aValue == _pitch) return;
        firePropChange(Pitch_Prop, _pitch, _pitch = aValue);
        clearCachedValues();
    }

    /**
     * Returns the rotation about the Z axis in degrees.
     */
    public double getRoll()  { return _roll; }

    /**
     * Sets the rotation about the Z axis in degrees.
     */
    public void setRoll(double aValue)
    {
        if (aValue == _roll) return;
        firePropChange(Roll_Prop, _roll, _roll = aValue);
        clearCachedValues();
    }

    /**
     * Returns the focal length of the camera (derived from the field of view and with view size).
     */
    public double getFocalLength()  { return _focalLen; }

    /**
     * Sets the focal length of the camera. Two feet is normal (1728 points).
     */
    public void setFocalLength(double aValue)
    {
        if (aValue == _focalLen) return;
        firePropChange(FocalLength_Prop, _focalLen, _focalLen = aValue);
        clearCachedValues();
    }

    /**
     * Returns the distance from center of scene to camera when in gimbal mode.
     */
    public double getGimbalRadius()  { return _gimbalRadius; }

    /**
     * Sets the distance from center of scene to camera when in gimbal mode.
     */
    protected void setGimbalRadius(double aValue)
    {
        if (aValue == _gimbalRadius) return;
        firePropChange(GimbalRadius_Prop, _gimbalRadius, _gimbalRadius = aValue);
        clearCachedValues();
    }

    /**
     * Returns whether PrefGimbalRadius is explicitly set.
     */
    public boolean isPrefGimbalRadiusSet()  { return _prefGimbalRadius > 0; }

    /**
     * Returns the optimal distance from center of scene to camera when in gimbal mode.
     */
    public double getPrefGimbalRadius()
    {
        // If explicitly set, just return
        if (_prefGimbalRadius > 0)
            return _prefGimbalRadius;

        // Calculate and return
        return calcPrefGimbalRadius();
    }

    /**
     * Sets the optimal distance from center of scene to camera when in gimbal mode.
     */
    public void setPrefGimbalRadius(double aValue)
    {
        if (aValue == _prefGimbalRadius) return;
        firePropChange(PrefGimbalRadius_Prop, _prefGimbalRadius, _prefGimbalRadius = aValue);
        clearCachedValues();
    }

    /**
     * Returns the optimal distance from center of scene to camera when in gimbal mode.
     */
    public double calcPrefGimbalRadius()
    {
        // Get scene to camera transform for GimbalRadius = 0
        double gimbalRadius = getGimbalRadius();
        _gimbalRadius = 0;
        Matrix3D sceneToCamera = getSceneToCameraImpl();
        _gimbalRadius = gimbalRadius;

        // Get bounds in camera coords with no Z offset
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        Bounds3D sceneBoundsInCamera = sceneBounds.copyForMatrix(sceneToCamera);

        // Get second offset Z from bounding box and restore original Z offset
        double focalLen = getFocalLength();
        double prefGR = focalLen + sceneBoundsInCamera.getMaxZ();
        return prefGR;
    }

    /**
     * Returns the field of view of the camera (derived from focalLength).
     */
    public double getFieldOfViewX()
    {
        double viewW = getViewWidth();
        double fieldOfView = Math.toDegrees( Math.atan( viewW / (2 * _focalLen) ) );
        return fieldOfView * 2;
    }

    /**
     * Returns the field of view Y of camera (derived from focalLength).
     */
    public double getFieldOfViewY()
    {
        double viewH = getViewHeight();
        double fieldOfView = Math.toDegrees( Math.atan( viewH / (2 * _focalLen) ) );
        return fieldOfView * 2;
    }

    /**
     * Sets the field of view Y of camera.
     */
    public void setFieldOfViewY(double aValue)
    {
        double viewH = getViewHeight();
        double tanTheta = Math.tan( Math.toRadians(aValue / 2) );
        double focalLength = viewH / (2 * tanTheta);
        setFocalLength(focalLength);
    }

    /**
     * Returns whether camera is viewing in orthographic projection (no perspective).
     */
    public boolean isOrtho()  { return _ortho; }

    /**
     * Sets whether camera is viewing in orthographic projection (no perspective).
     */
    public void setOrtho(boolean aValue)
    {
        if (aValue == _ortho) return;
        firePropChange(Ortho_Prop, _ortho, _ortho = aValue);
        clearCachedValues();
    }

    /**
     * Returns the camera normal as a vector.
     */
    public Vector3D getNormal()  { return _normal; }

    /**
     * Returns the transform from scene coords to camera coords.
     */
    public Matrix3D getSceneToCamera()
    {
        // If already set, just return
        if (_sceneToCamera != null) return _sceneToCamera;

        // Reset GimbalRadius
        _gimbalRadius = getPrefGimbalRadius();

        // Get transform, set, return
        Matrix3D xfm = getSceneToCameraImpl();
        return _sceneToCamera = xfm;
    }

    /**
     * Returns the transform from scene coords to camera coords.
     */
    private Matrix3D getSceneToCameraImpl()
    {
        // Create transform
        Matrix3D sceneToCamera = new Matrix3D();

        // Translate by gimbalRadius
        double gimbalRadius = getGimbalRadius();
        sceneToCamera.translate(0, 0, -gimbalRadius);

        // Rotate
        sceneToCamera.rotateXYZ(_pitch, _yaw, _roll);

        // Add translation from Scene center to world origin
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        double midx = sceneBounds.getMidX();
        double midy = sceneBounds.getMidY();
        double midz = sceneBounds.getMidZ();
        sceneToCamera.translate(-midx, -midy, -midz);

        // Return
        return sceneToCamera;
    }

    /**
     * Returns the transform from camera coords to clip space (AKA the 'Projection' matrix).
     */
    public Matrix3D getCameraToClip()
    {
        // If already set, just return
        if (_cameraToClip != null) return _cameraToClip;

        // If Orthographic projection, do that
        if (isOrtho()) {
            double viewW = getViewWidth(), halfW = viewW / 2;
            double viewH = getViewHeight(), halfH = viewH / 2;
            Matrix3D ortho = Matrix3D.newOrtho(-halfW, halfW, -halfH, halfH, 10, 5000);
            return _cameraToClip = ortho;
        }

        // Calc, set and return
        double fovY = getFieldOfViewY();
        double viewW = getViewWidth();
        double viewH = getViewHeight();
        double aspect = viewW / viewH;
        return _cameraToClip = Matrix3D.newPerspective(fovY, aspect, 10, 10000);
    }

    /**
     * Returns the transform from camera to View space.
     */
    public Matrix3D getCameraToView()
    {
        // If already set, just return
        if (_cameraToView != null) return _cameraToView;

        // Translate from View origin to View center
        double viewW = getViewWidth();
        double viewH = getViewHeight();
        Matrix3D cameraToView = new Matrix3D().translate(viewW / 2, viewH / 2, 0);

        // Scale from Clip space (NDC) to View space
        cameraToView.scale(viewW / 2, -viewH / 2, 1);

        // Transform from Camera space to Clip space (NDC)
        Matrix3D cameraToClip = getCameraToClip();
        cameraToView.multiply(cameraToClip);

        // Return
        return _cameraToView = cameraToView;
    }

    /**
     * Returns the transform from scene to View space.
     */
    public Matrix3D getSceneToView()
    {
        // If already set, just return
        if (_sceneToView != null) return _sceneToView;

        // Calc, set and return
        Matrix3D cameraToView = getCameraToView();
        Matrix3D sceneToCamera = getSceneToCamera();
        Matrix3D sceneToView = cameraToView.clone().multiply(sceneToCamera);
        return _sceneToView = sceneToView;
    }

    /**
     * Returns the SceneToCamera transform as double array.
     */
    public double[] getSceneToCameraArray()
    {
        Matrix3D sceneToCamera = getSceneToCamera();
        return sceneToCamera.mtx;
    }

    /**
     * Returns the CameraToClip transform as double array.
     */
    public double[] getCameraToClipArray()
    {
        Matrix3D cameraToClip = getCameraToClip();
        return cameraToClip.mtx;
    }

    /**
     * Set camera view to given side.
     */
    public void setYawPitchRollForSideAndPos(Side3D aSide, Pos aPos)
    {
        // Declare base rotations
        double yaw = 0;
        double pitch = 0;
        double roll = 0;

        // Update rotations for side
        switch (aSide) {

            // Handle Top/Bottom
            case TOP: pitch = 90; break;
            case BOTTOM: pitch = -90; break;

            // Handle Left/right
            case LEFT: yaw = 90; break;
            case RIGHT: yaw = -90; break;

            // Handle Front/Back
            case FRONT: break;
            case BACK: yaw = 180; break;

            // Handle the impossible
            default: throw new RuntimeException("Camera: setYawPitchRollForSide: Unknown side: " + aSide);
        }

        // Handle Pos: Shift rotations by addition amount based on Pos
        if (aPos != null) {
            switch (aPos.getHPos()) {
                case LEFT: yaw += 45; break;
                case RIGHT: yaw -= 45; break;
            }
            switch (aPos.getVPos()) {
                case TOP: pitch += 45; break;
                case BOTTOM: pitch -= 45; break;
            }
        }

        // Set new rotations
        setYaw(yaw);
        setPitch(pitch);
        setRoll(roll);
    }

    /**
     * Returns whether a vector is facing camera.
     */
    public boolean isFacing(Vector3D aV3D)
    {
        return aV3D.isAway(getNormal(), true);
    }

    /**
     * Returns whether a vector is facing away from camera.
     */
    public boolean isFacingAway(Vector3D aV3D)
    {
        return aV3D.isAligned(getNormal(), false);
    }

    /**
     * Returns the renderer.
     */
    public Renderer getRenderer()
    {
        // If already set, just return
        if (_renderer != null) return _renderer;

        // Create, set, return
        Renderer renderer = Renderer.newRenderer(this);
        return _renderer = renderer;
    }

    /**
     * Sets the renderer.
     */
    public void setRenderer(Renderer aRenderer)
    {
        // Clear old renderer
        Renderer oldRenderer = _renderer;
        if (_renderer != null)
            System.err.println("Camera.setRenderer: Need to free renderer");

        // Set new renderer
        _renderer = aRenderer;

        // Fire prop change
        firePropChange(Renderer_Prop, oldRenderer, _renderer);
        sceneDidChange();
    }

    /**
     * Calculates and sets the ray (origin,dir) from camera origin to 2D point in camera view coords.
     */
    public void getRayToViewPoint(double aX, double aY, Point3D rayOrigin, Vector3D rayDir)
    {
        // Get transform from camera to view
        double viewW = getViewWidth();
        double viewH = getViewHeight();
        Matrix3D cameraToView = new Matrix3D().translate(viewW / 2, viewH / 2, 0);
        cameraToView.scale(2 / 2, -2 / 2, 1);

        // Convert camera view XY point to 3D point in camera coords
        Matrix3D viewToCamera = cameraToView.clone().invert();
        Point3D cameraPoint = new Point3D(aX, aY, 0);
        viewToCamera.transformPoint(cameraPoint);
        double camX = cameraPoint.x;
        double camY = cameraPoint.y;
        Point3D rayViewPoint = new Point3D(camX, camY, -getFocalLength());

        // Get transform from camera to scene
        Matrix3D sceneToCamera = getSceneToCamera();
        Matrix3D cameraToScene = sceneToCamera.clone().invert();

        // Convert camera origin from zero to scene
        rayOrigin.setPoint(0, 0, 0);
        cameraToScene.transformPoint(rayOrigin);

        // Convert camera view point
        cameraToScene.transformPoint(rayViewPoint);
        rayDir.setVectorBetweenPoints(rayOrigin, rayViewPoint);
    }

    /**
     * Paints the scene from the view of this camera for given painter.
     */
    public void paintScene(Painter aPntr)
    {
        if (getScene().getChildCount() == 0)
            return;

        Renderer renderer = getRenderer();
        renderer.renderAndPaint(aPntr);
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        _mouseHandler.processEvent(anEvent);
    }

    /**
     * Returns the bounding rect for camera paths.
     */
    public Rect getSceneBounds2D()
    {
        Renderer renderer = getRenderer();
        return renderer.getSceneBoundsInView();
    }

    /**
     * Called when Scene changes.
     */
    protected void sceneDidChange()
    {
        if (_renderer != null)
            _renderer.sceneDidChange();
        clearCachedValues();
    }

    /**
     * Clears cached values.
     */
    protected void clearCachedValues()
    {
        _sceneToCamera = null;
        _cameraToClip = null;
        _cameraToView = null;
        _sceneToView = null;
    }

    /**
     * Copy attributes of another camera.
     */
    public void copy3D(Camera aCam)
    {
        setYaw(aCam.getYaw());
        setPitch(aCam.getPitch());
        setRoll(aCam.getRoll());
        setFocalLength(aCam.getFocalLength());
        setPrefGimbalRadius(aCam.isPrefGimbalRadiusSet() ? aCam.getPrefGimbalRadius() : 0);
        setOrtho(aCam.isOrtho());
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs == PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aLsnr);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr)
    {
        _pcs.removePropChangeListener(aLsnr);
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected final void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPCE)
    {
        _pcs.firePropChange(aPCE);
    }
}