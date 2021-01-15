/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.*;

import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
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
    
    // List of Path3Ds - for rendering
    private List <Path3D>  _paths = new ArrayList();
    
    // Whether paths list needs to be rebuilt
    private boolean  _rebuildPaths;
    
    // Mouse drag variable - mouse drag last point
    private Point  _pointLast;
    
    // used for shift-drag to indicate which axis to constrain rotation to
    private int  _dragConstraint;
    
    // The PropChangeSupport
    protected PropChangeSupport    _pcs = PropChangeSupport.EMPTY;

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
        if (aValue==_width) return;
        firePropChange("Width", _width, _width = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_height) return;
        firePropChange("Height", _height, _height = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_depth) return;
        firePropChange("Depth", _depth, _depth = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_yaw) return;
        firePropChange("Yaw", _yaw, _yaw = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_pitch) return;
        firePropChange("Pitch", _pitch, _pitch = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_roll) return;
        firePropChange("Roll", _roll, _roll = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_focalLen) return;
        firePropChange("FocalLength", _focalLen, _focalLen = aValue);
        rebuildPaths(); _xform3D = null;
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
        if (aValue==_offsetZ) return;
        firePropChange("OffsetZ", _offsetZ, _offsetZ = aValue);
        rebuildPaths(); _xform3D = null;
    }

    /**
     * Returns whether to adjust Z to keep scene positive.
     */
    public boolean isAdjustZ()  { return _adjustZ; }

    /**
     * Sets whether to adjust Z to keep scene positive.
     */
    public void setAdjustZ(boolean aValue)  { _adjustZ = aValue; rebuildPaths(); }

    /**
     * Returns whether scene is rendered in pseudo 3d.
     */
    public boolean isPseudo3D()  { return _pseudo3D; }

    /**
     * Sets whether scene is rendered in pseudo 3d.
     */
    public void setPseudo3D(boolean aFlag)
    {
        if (_pseudo3D==aFlag) return;
        firePropChange("Pseudo3D", _pseudo3D, _pseudo3D = aFlag);
        rebuildPaths(); _xform3D = null;
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
        if (anAngle==_pseudoSkewX) return;
        firePropChange("PseudoSkewX", _pseudoSkewX, _pseudoSkewX = anAngle);
        rebuildPaths(); _xform3D = null;
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
        if (anAngle==_pseudoSkewY) return;
        firePropChange("PseudoSkewY", _pseudoSkewY, _pseudoSkewY = anAngle);
        rebuildPaths(); _xform3D = null;
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

        // If pseudo 3d, just return skewed transform
        if (isPseudo3D()) {
            Transform3D t = new Transform3D(); t.skew(_pseudoSkewX, _pseudoSkewY);
            t.perspective(getFocalLength());
            return t;
        }

        // Bar chart used to do this rotation to "make pitch always relative to camera" instead of rotate below
        //t.rotateY(_yaw); t.rotate(new Vector3D(1,0,0), _pitch); t.rotate(new Vector3D(0,0,1), _roll);

        // Normal transform: translate about center, rotate X & Y, translate by Z, perspective, translate back
        double midx = getWidth()/2, midy = getHeight()/2, midz = getDepth()/2;
        Transform3D t = new Transform3D(-midx, -midy, -midz);
        t.rotate(_pitch, _yaw, _roll);
        t.translate(0, 0, getOffsetZ() - _offsetZ2);
        if (_focalLen>0)
            t.perspective(getFocalLength());
        t.translate(midx, midy, midz);
        return _xform3D = t;
    }

    /**
     * Resets secondary Z offset to keep scene in positive axis space.
     */
    protected void adjustZ()
    {
        // Cache and clear Z offset and second Z offset
        double offZ = getOffsetZ(),  _offsetZ = _offsetZ2 = 0; _xform3D = null;

        // Get bounding box in camera coords with no Z offset
        double w = getWidth();
        double h = getHeight();
        double d = getDepth();
        Path3D bbox = new Path3D();
        bbox.moveTo(0, 0, 0);
        bbox.lineTo(0, 0, d);
        bbox.lineTo(w, 0, d);
        bbox.lineTo(w, 0, 0);
        bbox.lineTo(w, h, 0);
        bbox.lineTo(w, h, d);
        bbox.lineTo(0, h, d);
        bbox.lineTo(0, h, 0);
        bbox.transform(getTransform());

        // Get second offset Z from bounding box and restore original Z offset
        _offsetZ2 = bbox.getZMin();
        _offsetZ = offZ;
        _xform3D = null;
        if (Math.abs(_offsetZ2)>w)
            _offsetZ2 = w*MathUtils.sign(_offsetZ2); // Something is brokey
    }

    /**
     * Returns a point in camera coords for given point in scene coords.
     */
    public Point3D sceneToCamera(Point3D aPoint)  { return sceneToCamera(aPoint.x, aPoint.y, aPoint.z); }

    /**
     * Returns a point in camera coords for given point in scene coords.
     */
    public Point3D sceneToCamera(double aX, double aY, double aZ)  { return getTransform().transformPoint(aX, aY, aZ); }

    /**
     * Returns a path in camera coords for given path in scene coords.
     */
    public Path3D sceneToCamera(Path3D aPath)  { return aPath.copyFor(getTransform()); }

    /**
     * Returns whether a vector is facing camera.
     */
    public boolean isFacing(Vector3D aV3D)  { return aV3D.isAway(getNormal(), true); }

    /**
     * Returns whether a vector is facing away from camera.
     */
    public boolean isFacingAway(Vector3D aV3D)  { return aV3D.isAligned(getNormal(), false); }

    /**
     * Returns whether a Path3d is facing camera.
     */
    public boolean isFacing(Path3D aPath)  { return isFacing(aPath.getNormal()); }

    /**
     * Returns whether a Path3d is facing away from camera.
     */
    public boolean isFacingAway(Path3D aPath)  { return isFacingAway(aPath.getNormal()); }

    /**
     * Returns the specific Path3D at the given index from the display list.
     */
    public List <Path3D> getPaths()
    {
        if (_rebuildPaths)
            rebuildPathsNow();
        return _paths;
    }

    /**
     * Returns the number of Path3Ds in the display list.
     */
    public int getPathCount()  { return getPaths().size(); }

    /**
     * Returns the specific Path3D at the given index from the display list.
     */
    public Path3D getPath(int anIndex)  { return getPaths().get(anIndex); }

    /**
     * Adds a path to the end of the display list.
     */
    protected void addPath(Path3D aShape)  { _paths.add(aShape); }

    /**
     * Removes the shape at the given index from the shape list.
     */
    protected void removePaths()  { _paths.clear(); }

    /**
     * Called to indicate that paths list needs to be rebuilt.
     */
    protected void rebuildPaths()  { _rebuildPaths = true; }

    /**
     * Rebuilds display list of Path3Ds from Shapes.
     */
    protected void rebuildPathsNow()
    {
        // Adjust Z
        if (isAdjustZ()) adjustZ();

        // Remove all existing Path3Ds
        removePaths(); _sceneBounds = null;

        // Iterate over shapes and add paths
        List <Shape3D> shapes = _scene._shapes;
        for (Shape3D shp : shapes)
            addPathsForShape(shp);

        // Resort paths
        Path3D.sort(_paths);
        _rebuildPaths = false;
    }

    /**
     * Adds the paths for shape.
     */
    protected void addPathsForShape(Shape3D aShape)
    {
        // Get the camera transform & optionally align it to the screen
        Transform3D xform = getTransform();
        Light light = _scene.getLight();
        Color color = aShape.getColor();

        // Iterate over paths
        for (Path3D path3d : aShape.getPath3Ds()) {

            // Get path copy transformed by scene transform
            path3d = path3d.copyFor(xform);

            // Backface culling : Only add paths that face the camera
            if (isFacingAway(path3d.getNormal())) continue;

            // If color on shape, set color on path for scene lights
            if (color!=null) {
                Color rcol = light.getRenderColor(this, path3d, color);
                path3d.setColor(rcol);
            }

            // Add path
            addPath(path3d);
        }
    }

    /**
     * Paints shape children.
     */
    public void paintPaths(Painter aPntr)
    {
        // Iterate over Path3Ds and paint
        List <Path3D> paths = getPaths();
        for (int i=0, iMax=paths.size(); i<iMax; i++) { Path3D child = paths.get(i);

            // Paint path and path layers
            paintPath3D(aPntr, child);
            if (child.getLayers().size()>0)
                for (Path3D layer : child.getLayers())
                    paintPath3D(aPntr, layer);
        }
    }

    /**
     * Paints a Path3D.
     */
    protected void paintPath3D(Painter aPntr, Path3D aPath3D)
    {
        // Get path, fill and stroke
        Shape path = aPath3D.getPath();
        Paint fill = aPath3D.getColor(), stroke = aPath3D.getStrokeColor();

        // Get opacity and set if needed
        double op = aPath3D.getOpacity(), oldOP = 0;
        if (op<1) {
            oldOP = aPntr.getOpacity();
            aPntr.setOpacity(op*oldOP);
        }

        // Do fill and stroke
        if (fill!=null) {
            aPntr.setPaint(fill);
            aPntr.fill(path);
        }
        if (stroke!=null) {
            aPntr.setPaint(stroke);
            aPntr.setStroke(aPath3D.getStroke());
            aPntr.draw(path);
        }

        // Reset opacity if needed
        if (op<1)
            aPntr.setOpacity(oldOP);
    }

    /** Paints a Path3D with labels on sides. */
    /*private void paintPath3DDebug(Painter aPntr, Path3D aPath3D, String aStr) {
        aPntr.setOpacity(.8); paintPath3D(aPntr, aPath3D); aPntr.setOpacity(1);
        Font font = Font.Arial14.getBold(); double asc = font.getAscent(); aPntr.setFont(font);
        Rect r = font.getStringBounds(aStr), r2 = aPath3D.getPath().getBounds();
        aPntr.drawString(aStr, r2.x + (r2.width - r.width)/2, r2.y + (r2.height - r.height)/2 + asc);
    }*/

    /**
     * Returns the bounding rect for camera paths.
     */
    public Rect getSceneBounds()
    {
        // If already set, just return
        if (_sceneBounds!=null) return _sceneBounds;

        // Iterate over paths
        List <Path3D> paths = getPaths();
        double xmin = Float.MAX_VALUE, ymin = Float.MAX_VALUE, xmax = -xmin, ymax = -ymin;
        for (Path3D path : paths) { Point3D bb2[] = path.getBBox();
            xmin = Math.min(xmin, bb2[0].x);
            ymin = Math.min(ymin, bb2[0].y);
            xmax = Math.max(xmax, bb2[1].x);
            ymax = Math.max(ymax, bb2[1].y);
        }
        return _sceneBounds = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
    } Rect _sceneBounds;

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

        // Handle MouseReleased
        //else if (anEvent.isMouseRelease()) { _valueAdjusting = false; repaint(); relayout(); }
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
                    if (Math.abs(point.y-_pointLast.y)>Math.abs(point.x-_pointLast.x))
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
        setYaw(aCam.getYaw()); setPitch(aCam.getPitch()); setRoll(aCam.getRoll());
        setFocalLength(aCam.getFocalLength()); setOffsetZ(aCam.getOffsetZ());
        setPseudo3D(aCam.isPseudo3D());
        setPseudoSkewX(aCam.getPseudoSkewX()); setPseudoSkewY(aCam.getPseudoSkewY());
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aLsnr);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aLsnr)  { _pcs.removePropChangeListener(aLsnr); }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPCE)  { _pcs.firePropChange(aPCE); }
}