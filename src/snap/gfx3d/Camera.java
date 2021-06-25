/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
import snap.view.ViewEvent;
import java.util.ArrayList;
import java.util.List;

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
    private double _width, _height, _depth = 40;
    
    // Rotation around y axis
    private double  _yaw = 0;
    
    // Rotation around x axis
    private double  _pitch = 0;
    
    // Rotation around z axis
    private double  _roll = 0;
    
    // Offset from z axis
    private double  _offsetZ = 0, _offsetZ2;
    
    // Whether to adjust Z to keep scene positive
    private boolean  _adjustZ;
    
    // Perspective
    private double  _focalLen = 60*72;
    
    // Whether to do simple 3d rendering effect by skewing geometry a little bit
    private boolean  _pseudo3D;
    
    // The skew in radians along x/y axis when doing pseudo 3d
    private double  _pseudoSkewX, _pseudoSkewY;
    
    // Camera normal
    private Vector3D  _normal = new Vector3D(0, 0, 1);
    
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
    public static final String Width_Prop = "Width";
    public static final String Height_Prop = "Height";
    public static final String Depth_Prop = "Depth";
    public static final String Yaw_Prop = "Yaw";
    public static final String Pitch_Prop = "Pitch";
    public static final String Roll_Prop = "Roll";
    public static final String FocalLength_Prop = "FocalLength";
    public static final String OffsetZ_Prop = "OffsetZ";
    public static final String AdjustZ_Prop = "AdjustZ";
    public static final String Pseudo3D_Prop = "Pseudo3D";
    public static final String PseudoSkewX_Prop = "PseudoSkewX";
    public static final String PseudoSkewY_Prop = "PseudoSkewY";

    // Constants for mouse drag constraints
    public final int CONSTRAIN_NONE = 0;
    public final int CONSTRAIN_PITCH = 1;
    public final int CONSTRAIN_YAW = 2;

    /**
     * Constructor.
     */
    public Camera()  { }

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
    public double getWidth()  { return _width; }

    /**
     * Sets the width of the camera viewing plane.
     */
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        firePropChange(Width_Prop, _width, _width = aValue);
        _xform3D = null;
    }

    /**
     * Returns the height of the camera viewing plane.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the height of the camera viewing plane.
     */
    public void setHeight(double aValue)
    {
        if (aValue == _height) return;
        firePropChange(Height_Prop, _height, _height = aValue);
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
     * Returns the Z offset of the scene (for zooming).
     */
    public double getOffsetZ()  { return _offsetZ; }

    /**
     * Sets the Z offset of the scene (for zooming).
     */
    public void setOffsetZ(double aValue)
    {
        if (aValue == _offsetZ) return;
        firePropChange(OffsetZ_Prop, _offsetZ, _offsetZ = aValue);
        _xform3D = null;
    }

    /**
     * Returns whether to adjust Z to keep scene positive.
     */
    public boolean isAdjustZ()  { return _adjustZ; }

    /**
     * Sets whether to adjust Z to keep scene positive.
     */
    public void setAdjustZ(boolean aValue)
    {
        if (aValue == isAdjustZ()) return;
        firePropChange(AdjustZ_Prop, _adjustZ, _adjustZ = aValue);
    }

    /**
     * Returns whether scene is rendered in pseudo 3d.
     */
    public boolean isPseudo3D()  { return _pseudo3D; }

    /**
     * Sets whether scene is rendered in pseudo 3d.
     */
    public void setPseudo3D(boolean aFlag)
    {
        if (_pseudo3D == aFlag) return;
        firePropChange(Pseudo3D_Prop, _pseudo3D, _pseudo3D = aFlag);
        _xform3D = null;
    }

    /**
     * Returns the skew angle for X by Z.
     */
    public double getPseudoSkewX()  { return _pseudoSkewX; }

    /**
     * Sets the skew angle for X by Z.
     */
    public void setPseudoSkewX(double anAngle)
    {
        if (anAngle == _pseudoSkewX) return;
        firePropChange(PseudoSkewX_Prop, _pseudoSkewX, _pseudoSkewX = anAngle);
        _xform3D = null;
    }

    /**
     * Returns the skew angle for Y by Z.
     */
    public double getPseudoSkewY()  { return _pseudoSkewY; }

    /**
     * Sets the skew angle for Y by Z.
     */
    public void setPseudoSkewY(double anAngle)
    {
        if (anAngle == _pseudoSkewY) return;
        firePropChange(PseudoSkewY_Prop, _pseudoSkewY, _pseudoSkewY = anAngle);
        _xform3D = null;
    }

    /**
     * Returns the field of view of the camera (derived from focalLength).
     */
    public double getFieldOfView()
    {
        double height = Math.max(getWidth(), getHeight());
        double fieldOfView = Math.toDegrees(Math.atan(height/(2*_focalLen)));
        return fieldOfView*2;
    }

    /**
     * Sets the field of view of the camera.
     */
    public void setFieldOfView(double aValue)
    {
        double height = Math.max(getWidth(), getHeight());
        double tanTheta = Math.tan(Math.toRadians(aValue/2));
        double focalLength = height/(2*tanTheta);
        setFocalLength(focalLength);
    }

    /**
     * Returns the camera normal as a vector.
     */
    public Vector3D getNormal()  { return _normal; }

    /**
     * Returns the transform from scene coords to camera coords.
     */
    public Transform3D getTransform()
    {
        // If already set, just return
        if (_xform3D!=null) return _xform3D;

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
        double midx = getWidth() / 2;
        double midy = getHeight() / 2;
        double midz = getDepth() / 2;
        xfm.translate(-midx, -midy, -midz);

        // If pseudo 3d, just return skewed transform
        if (isPseudo3D()) {
            xfm.skew(_pseudoSkewX, _pseudoSkewY);
            return xfm;
        }

        // Rotate
        xfm.rotate(_pitch, _yaw, _roll);

        // Translate by Offset Z
        double offsetZ = getOffsetZ() - _offsetZ2;
        xfm.translate(0, 0, offsetZ);
        return xfm;
    }

    /**
     * Resets secondary Z offset to keep scene in positive axis space.
     */
    protected void adjustZ()
    {
        // Cache and clear Z offset and second Z offset
        double offZ = getOffsetZ();
        _offsetZ = 0;
        _offsetZ2 = 0;
        _xform3D = null;

        // Get bounding box in camera coords with no Z offset
        double boxW = getWidth();
        double boxH = getHeight();
        double boxD = getDepth();
        Path3D bbox = new Path3D();
        bbox.moveTo(0, 0, 0);
        bbox.lineTo(0, 0, boxD);
        bbox.lineTo(boxW, 0, boxD);
        bbox.lineTo(boxW, 0, 0);
        bbox.lineTo(boxW, boxH, 0);
        bbox.lineTo(boxW, boxH, boxD);
        bbox.lineTo(0, boxH, boxD);
        bbox.lineTo(0, boxH, 0);

        // Transform
        Transform3D xfm = getTransform();
        bbox.transform(xfm);

        // Get second offset Z from bounding box and restore original Z offset
        _offsetZ2 = bbox.getZMin();
        _offsetZ = offZ;
        _xform3D = null;

        // Something is brokey
        if (Math.abs(_offsetZ2) > boxW)
            _offsetZ2 = boxW*MathUtils.sign(_offsetZ2);
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
        if (_renderer != null) return _renderer;
        return _renderer = Renderer.newRenderer(this);
    }

    /**
     * Paints the scene from the view of this camera for given painter.
     */
    public void paintScene(Painter aPntr)
    {
        Renderer renderer = getRenderer();
        renderer.renderAll(aPntr);
    }

    /**
     * Returns the bounding rect for camera paths.
     */
    public Rect getSceneBounds()
    {
        Renderer renderer = getRenderer();
        return renderer.getSceneBounds();
    }

    /**
     * Returns the scene as a set of Path3Ds, if possible.
     */
    public List<Path3D> getPaths()
    {
        // Get paths from renderer
        Renderer renderer = getRenderer();
        Renderer2D r2d = renderer instanceof Renderer2D ? (Renderer2D) renderer : new Renderer2D(this);
        List<Path3D> paths = r2d.getPaths();

        // This is bogus, but it's only used by PDF and I don't want to change that right now
        List<Path3D> paths2 = new ArrayList<>();
        double dispMidX = getWidth() / 2;
        double dispMidY = getHeight() / 2;
        Transform3D cameraToDisplay = new Transform3D(dispMidX, dispMidY, 0);
        for (Path3D path3D : paths)
            paths2.add(path3D.copyForTransform(cameraToDisplay));
        return paths2;
    }

    /**
     * Called when Scene changes.
     */
    protected void sceneDidChange()
    {
        if (_renderer != null)
            _renderer.sceneDidChange();
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
            double offZ = Math.max(getOffsetZ() + distZ, -focalLen + 100);
            setOffsetZ(offZ);
            setAdjustZ(false);
        }
    }

    /**
     * Viewer method.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        // Get event location in this scene shape coords
        Point point = anEvent.getPoint();

        // If pseudo3d, set skew using event offset
        if (isPseudo3D()) {
            setPseudoSkewX(getPseudoSkewX() + (point.x - _pointLast.x)/100);
            setPseudoSkewY(getPseudoSkewY() + (point.y - _pointLast.y)/100);
        }

        // If right-mouse, muck with perspective
        else if (anEvent.isShortcutDown())
            setOffsetZ(getOffsetZ() + _pointLast.y - point.y);

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
            setYaw(getYaw() - (point.x - _pointLast.x)/1.5f);
        }

        // Set last point
        _pointLast = point;
    }

    /**
     * Copy attributes of another scene.
     */
    public void copy3D(Camera aCam)
    {
        setDepth(aCam.getDepth());
        setYaw(aCam.getYaw());
        setPitch(aCam.getPitch());
        setRoll(aCam.getRoll());
        setFocalLength(aCam.getFocalLength());
        setOffsetZ(aCam.getOffsetZ());
        setPseudo3D(aCam.isPseudo3D());
        setPseudoSkewX(aCam.getPseudoSkewX());
        setPseudoSkewY(aCam.getPseudoSkewY());
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