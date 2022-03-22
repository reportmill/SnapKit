/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
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
public class Camera3D {
    
    // The scene being viewed
    private Scene3D  _scene;
    
    // Width, height, depth
    private double _viewWidth, _viewHeight, _depth = 40;
    
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
    
    // Camera normal
    private Vector3D  _normal = new Vector3D(0, 0, -1);
    
    // The currently cached transform 3d
    private Transform3D  _xform3D;

    // The Renderer
    private Renderer  _renderer;

    // Mouse drag variable - mouse drag last point
    private Point  _pointLast;
    
    // used for shift-drag to indicate which axis to constrain rotation to
    private int  _dragConstraint;
    
    // The PropChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String ViewWidth_Prop = "ViewWidth";
    public static final String ViewHeight_Prop = "ViewHeight";
    public static final String Depth_Prop = "Depth";
    public static final String Yaw_Prop = "Yaw";
    public static final String Pitch_Prop = "Pitch";
    public static final String Roll_Prop = "Roll";
    public static final String FocalLength_Prop = "FocalLength";
    public static final String GimbalRadius_Prop = "GimbalRadius";
    public static final String PrefGimbalRadius_Prop = "PrefGimbalRadius";
    public static final String Renderer_Prop = "Renderer";

    // Constants for mouse drag constraints
    public final int CONSTRAIN_NONE = 0;
    public final int CONSTRAIN_PITCH = 1;
    public final int CONSTRAIN_YAW = 2;

    /**
     * Constructor.
     */
    public Camera3D()  { }

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
        _xform3D = null;
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
        _xform3D = null;
    }

    /**
     * Returns the depth of the scene.
     */
    public double getDepth()  { return _depth; }

    /**
     * Sets the depth of the scene.
     */
    public void setDepth(double aValue)
    {
        if (aValue == _depth) return;
        firePropChange(Depth_Prop, _depth, _depth = aValue);
        _xform3D = null;
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
        _xform3D = null;
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
        _xform3D = null;
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
        _xform3D = null;
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
        _xform3D = null;
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
        _xform3D = null;
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
        return getPrefGimbalRadiusImpl();
    }

    /**
     * Sets the optimal distance from center of scene to camera when in gimbal mode.
     */
    public void setPrefGimbalRadius(double aValue)
    {
        if (aValue == _prefGimbalRadius) return;
        firePropChange(PrefGimbalRadius_Prop, _prefGimbalRadius, _prefGimbalRadius = aValue);
        _xform3D = null;
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
     * Returns the camera normal as a vector.
     */
    public Vector3D getNormal()  { return _normal; }

    /**
     * Returns the transform from camera coords to display coords.
     */
    public Transform3D getProjectionTransform()
    {
        double fovY = getFieldOfViewY();
        double viewW = getViewWidth();
        double viewH = getViewHeight();
        double aspect = viewW / viewH;
        Transform3D xfm = Transform3D.newPerspective(fovY, aspect, 10, 10000);
        return xfm;
    }

    /**
     * Returns the transform from scene coords to camera coords.
     */
    public Transform3D getTransform()
    {
        // If already set, just return
        if (_xform3D != null) return _xform3D;

        // Reset GimbalRadius
        _gimbalRadius = getPrefGimbalRadius();

        // Get transform, set, return
        Transform3D xfm = getTransformImpl();
        return _xform3D = xfm;
    }

    /**
     * Returns the transform from scene coords to camera coords.
     */
    private Transform3D getTransformImpl()
    {
        // Create transform
        Transform3D xfm = new Transform3D();

        // Add translation from Scene center to world origin
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        double midx = sceneBounds.getMidX();
        double midy = sceneBounds.getMidY();
        double midz = sceneBounds.getMidZ();
        xfm.translate(-midx, -midy, -midz);

        // Rotate
        xfm.rotateXYZ(_pitch, _yaw, _roll);

        // Translate by Offset Z
        double gimbalRadius = getGimbalRadius();
        xfm.translate(0, 0, -gimbalRadius);
        return xfm;
    }

    /**
     * Returns the optimal distance from center of scene to camera when in gimbal mode.
     */
    protected double getPrefGimbalRadiusImpl()
    {
        // Get camera transform for GimbalRadius = 0
        double gimbalRadius = getGimbalRadius();
        _gimbalRadius = 0;
        Transform3D cameraTrans = getTransformImpl();
        _gimbalRadius = gimbalRadius;

        // Get bounds in camera coords with no Z offset
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        Bounds3D sceneBoundsInCamera = sceneBounds.copyForTransform(cameraTrans);

        // Get second offset Z from bounding box and restore original Z offset
        double focalLen = getFocalLength();
        double prefGR = focalLen + sceneBoundsInCamera.getMaxZ();
        return prefGR;
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
     * Returns whether a Path3d is facing camera.
     */
    public boolean isFacing(Path3D aPath)
    {
        return isFacing(aPath.getNormal());
    }

    /**
     * Returns whether a Path3d is facing away from camera.
     */
    public boolean isFacingAway(Path3D aPath)
    {
        return isFacingAway(aPath.getNormal());
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
            System.err.println("Camera3D.setRenderer: Need to free renderer");

        // Set new renderer
        _renderer = aRenderer;

        // Fire prop change
        firePropChange(Renderer_Prop, oldRenderer, _renderer);
        sceneDidChange();
    }

    /**
     * Paints the scene from the view of this camera for given painter.
     */
    public void paintScene(Painter aPntr)
    {
        if (getScene().getChildCount() == 0)
            return;

        Renderer renderer = getRenderer();
        renderer.renderAll(aPntr);
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
        _xform3D = null;
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        // Do normal version
        if (anEvent.isConsumed()) return;

        // Handle MousePressed: Set last point to event location in scene coords and _dragConstraint
        if (anEvent.isMousePress()) {
            _pointLast = anEvent.getPoint(); //_valueAdjusting = true;
            _dragConstraint = CONSTRAIN_NONE;
        }

        // Handle MouseDragged
        else if (anEvent.isMouseDrag())
            mouseDragged(anEvent);

        // Handle Scroll: Assume + 1x per 60 points (1 inches)
        else if (anEvent.isScroll()) {
            int SCROLL_SCALE = 10;
            double scroll = anEvent.getScrollY();
            double distZ = scroll * SCROLL_SCALE;
            double focalLen = getFocalLength();
            double gimbalRad = getGimbalRadius();
            double gimbalRad2 = Math.max(gimbalRad + distZ, -focalLen + 100);
            setPrefGimbalRadius(gimbalRad2);
        }
    }

    /**
     * Viewer method.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        // Get event location in this scene shape coords
        Point point = anEvent.getPoint();

        // If right-mouse, muck with perspective
        if (anEvent.isShortcutDown()) {
            double gimbalRad = getGimbalRadius();
            setPrefGimbalRadius(gimbalRad + _pointLast.y - point.y);
        }

        // Otherwise, just do pitch and roll
        else {

            // Shift-drag constrains to just one axis at a time
            if (anEvent.isShiftDown()) {

                // If no constraint
                if (_dragConstraint==CONSTRAIN_NONE) {
                    if (Math.abs(point.y-_pointLast.y) > Math.abs(point.x-_pointLast.x))
                        _dragConstraint = CONSTRAIN_PITCH;
                    else _dragConstraint = CONSTRAIN_YAW;
                }

                // If Pitch constrained
                if (_dragConstraint==CONSTRAIN_PITCH)
                    point.x = _pointLast.x;

                // If Yaw constrained
                else point.y = _pointLast.y;
            }

            // Set pitch & yaw
            setPitch(getPitch() + (point.y - _pointLast.y)/1.5f);
            setYaw(getYaw() + (point.x - _pointLast.x)/1.5f);
        }

        // Set last point
        _pointLast = point;
    }

    /**
     * Copy attributes of another scene.
     */
    public void copy3D(Camera3D aCam)
    {
        setDepth(aCam.getDepth());
        setYaw(aCam.getYaw());
        setPitch(aCam.getPitch());
        setRoll(aCam.getRoll());
        setFocalLength(aCam.getFocalLength());
        setPrefGimbalRadius(aCam.isPrefGimbalRadiusSet() ? aCam.getPrefGimbalRadius() : 0);
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