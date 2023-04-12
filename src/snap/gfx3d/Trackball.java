/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.Random;

import snap.geom.Point;
import snap.gfx.*;
import snap.view.*;

/**
 * This class implements the trackball widget.  It is an optional replacement for the Scene3DControl.
 * 
 * Trackball encapsulates the Scene3D's behavior, which is that mouse motion in the x direction changes the pitch, and 
 * mouse motion in the y direction changes the yaw. The controll adds rotation about the z axis (roll) by clicking
 * on a ring outside the trackball.
 *   
 * Note that this particular behavior quickly goes wrong, since
 *  Rotate(y,x,z) * Rotate(dy,dx,dz) != Rotate (y+dy, x+dx, z+dz)
 * 
 * To make the behavior more reasonable, we could try 
 * any of :
 *   1.  Make Scene3D keep a matrix,instead of the euler angles and just keep rotating that by dx,dy,dz
 *   2.  Get the matrix, rotate by dx,dy,dz, decompose into new euler angles and set those
 *   3.  Use quaternions (gasp) 
 */
public class Trackball extends ParentView {

    // The Scene3D used to draw 3d (contains the trackball's scuffmarks)
    private Scene3D  _scene;

    // The Camera used to draw 3d (renders scene)
    private Camera  _camera;

    // The radius of the trackball sphere, which sits at the origin
    private double  _radius = 36;
   
    // hit test result, for dragging
    private int  _hitPart;

    // saved angle for calculating new roll during drags on the collar
    private double  _lastRollAngle;

    // MouseHandler for normal drags
    private MouseHandler  _mouseHandler;

    // The trackball image, highlight image and knob image
    private ImageView  _tball = new ImageView(Image.get(getClass(), "pkg.images/Trackball.png"));
    private ImageView  _tball_lit = new ImageView(Image.get(getClass(), "pkg.images/Trackball_lit.png"));
    private ImageView  _knob = new ImageView(Image.get(getClass(), "pkg.images/Trackball_knob.png"));
   
    // Location of the important parts of the control image
    static final float LEFT_EDGE = 2;
    static final float TOP_EDGE = 2;
    static final float COLLAR_THICKNESS = 16;
    static final float INNER_RADIUS = 39;
    static final float CENTER_X = LEFT_EDGE+COLLAR_THICKNESS+INNER_RADIUS;
    static final float CENTER_Y = TOP_EDGE+COLLAR_THICKNESS+INNER_RADIUS;
    static final float KNOB_WIDTH = 14;
    static final float KNOB_CENTER_X = 9;
    static final float KNOB_CENTER_Y = 11;
    static final float IMAGE_SIZE = 118;
   
    // Possible hit test results
    static final int HIT_NONE = 0;
    static final int HIT_COLLAR = 1;
    static final int HIT_TRACKBALL = 2;
    
    // Constants
    static Color SCUFF_COLOR = new Color(.2f,.2f,.2f,.5f);

    /**
     * Creates a Trackball.
     */
    public Trackball()
    {
        // Set size
        setPrefSize(IMAGE_SIZE, IMAGE_SIZE);

        // Fix image sizes
        _tball.setSize(IMAGE_SIZE, IMAGE_SIZE);
        _tball_lit.setSize(IMAGE_SIZE, IMAGE_SIZE);
        _knob.setSize(20, 20);

        // Add trackball image
        addChild(_tball);

        // Create/configure scene and camera
        _scene = new Scene3D();

        // Create/configure camera
        _camera = _scene.getCamera();
        _camera.setViewWidth(IMAGE_SIZE);
        _camera.setViewHeight(IMAGE_SIZE); // set X to 2 ???

        // Set to Renderer2D
        Renderer2D renderer2D = new Renderer2D(_camera);
        renderer2D.setSortSurfaces(false);
        _camera.setRenderer(renderer2D);

        // Set GimbalRadius
        _camera.setPrefGimbalRadius(_camera.getFocalLength());

        // Enable mouse/action events
        setActionable(true);
        enableEvents(MousePress, MouseDrag, MouseRelease);
        _mouseHandler = new MouseHandler(_camera);
    }

    /**
     * Returns the rotation about the Y axis in degrees.
     */
    public double getYaw()  { return _camera.getYaw(); }

    /**
     * Sets the rotation about the Y axis in degrees.
     */
    public void setYaw(double aValue)  { _camera.setYaw(aValue); }

    /**
     * Returns the rotation about the X axis in degrees.
     */
    public double getPitch()  { return _camera.getPitch(); }

    /**
     * Sets the rotation about the X axis in degrees.
     */
    public void setPitch(double aValue)  { _camera.setPitch(aValue); }

    /**
     * Returns the rotation about the Z axis in degrees.
     */
    public double getRoll()  { return _camera.getRoll(); }

    /**
     * Sets the rotation about the Z axis in degrees.
     */
    public void setRoll(double aValue)  { _camera.setRoll(aValue); }

    /**
     * Override to add scuff marks.
     */
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        addScuffMarks();
    }

    /**
     * Adds scuffmark polygons at random points on the trackball.
     */
    protected void addScuffMarks()
    {
        _scene.removeChildren();
        Random ran = new Random();
        for (int i=0; i<50; i++) {
            double th = ran.nextDouble()*360;
            double ph = ran.nextDouble()*360;
            addScuffMark(th, ph);
        }
    }

    /**
     * Adds a polygon to the scene which attempts to represent a scuffmark on the sphere at polar location {theta,phi}
     */
    private void addScuffMark(double theta, double phi)
    {
        // Small triangle at the origin to represent a scuff mark
        Polygon3D path = new Polygon3D();
        path.addPoint(-1,-1,0);
        path.addPoint(0,1,0);
        path.addPoint(1,-1,0);

        // Get Scene origin
        double midx = IMAGE_SIZE / 2;
        double midy = IMAGE_SIZE / 2;
        double midz = IMAGE_SIZE / 2;

        // translate out to surface of sphere and rotate to latitude+longitude, translate to scene origin
        Matrix3D transform = new Matrix3D();
        transform.translate(midx, midy, midz);
        transform.rotateZ(phi);
        transform.rotateY(theta);
        transform.translate(0, 0, -_radius);

        // Transform path
        path.transformBy(transform);

        // If the trackball is shrunk down, draw the scuffmarks a darker color so they'll show up.
        path.setColor(SCUFF_COLOR); //if (getZoomFactor()<.75) path.setColor(new Color(0,0,0,.75f));
        _scene.addChild(path);
    }

    /**
     * Override to paint scene.
     */
    protected void paintAbove(Painter aPntr)
    {
        _camera.paintScene(aPntr);
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMousePress())
            mousePressed(anEvent);
        else if (anEvent.isMouseDrag())
            mouseDragged(anEvent);
        else if (anEvent.isMouseRelease())
            mouseReleased(anEvent);
    }

    /**
     * Handle mouse press.
     */
    protected void mousePressed(ViewEvent anEvent)
    {
        double scale = 1; //getZoomFactor(); ???
        Point point = anEvent.getPoint(); point.x /= scale; point.y /= scale;
        double distance = point.getDistance(CENTER_X,CENTER_Y);

        // If inside trackball, replace image with lit version
        if (distance <= INNER_RADIUS) {
            _hitPart = HIT_TRACKBALL; // turn on hilight
            removeChild(_tball);
            addChild(_tball_lit);
            _mouseHandler.processEvent(anEvent);
        }

        // Else if in collar, add knob
        else if (distance <= INNER_RADIUS+COLLAR_THICKNESS) {
            _hitPart = HIT_COLLAR;
            addChild(_knob);
            _lastRollAngle = getMouseAngle(point);
            positionKnob(point);
        }

        // Else
        else _hitPart = HIT_NONE;
    }

    /**
     * Handle mouse drag.
     */
    protected void mouseDragged(ViewEvent anEvent)
    {
        // If
        if (_hitPart == HIT_COLLAR) {
            //double scale = 1; //getZoomFactor(); ??? point.x /= scale; point.y /= scale;
            Point point = anEvent.getPoint();
            double theta = getMouseAngle(point);
            double newRoll = _camera.getRoll() - Math.toDegrees(theta - _lastRollAngle);
            _camera.setRoll(newRoll);
            _lastRollAngle = theta;
            positionKnob(point);
        }

        // Otherwise, forward to scene
        else _mouseHandler.processEvent(anEvent);

        // Repaint and fire action event
        repaint();
        fireActionEvent(anEvent);
    }

    /**
     * Handle mouse release.
     */
    protected void mouseReleased(ViewEvent anEvent)
    {
        if (_hitPart == HIT_TRACKBALL) {
            _mouseHandler.processEvent(anEvent);
            removeChild(_tball_lit);
            addChild(_tball);
        }
        else if (_hitPart == HIT_COLLAR)
            removeChild(_knob);

        // Send ViewEvent to owner
        fireActionEvent(anEvent);
    }

    /**
     * Returns the angle from the mousePoint to the center of the control, in radians.
     */
    private double getMouseAngle(Point p)
    {
        double dx = p.x - CENTER_X;
        double dy = p.y - CENTER_Y;
        return Math.atan2(dy, dx);
    }

    /**
     * Move the collar knob to the correct location for the given mouse point.
     */
    private void positionKnob(Point p)
    {
        double theta = getMouseAngle(p);
        double radius = INNER_RADIUS + KNOB_WIDTH / 2;
        double x = CENTER_X + radius * Math.cos(theta) - KNOB_CENTER_X;
        double y = CENTER_Y + radius * Math.sin(theta) - KNOB_CENTER_Y;
        _knob.setXY(x, y);
    }

    /**
     * Sync from given camera to this trackball.
     */
    public void syncFrom(Camera aScene)
    {
        sync(aScene, _camera);
    }

    /**
     * Sync to a given camera from this trackball.
     */
    public void syncTo(Camera aScene)
    {
        sync(_camera, aScene);
    }

    /** Sync cameras. */
    private void sync(Camera s1, Camera s2)
    {
        s2.setPitch(s1.getPitch());
        s2.setYaw(s1.getYaw());
        s2.setRoll(s1.getRoll());
    }
}