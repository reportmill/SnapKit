/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.HPos;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.VPos;
import snap.gfx.Painter;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.View;
import snap.view.ViewAnim;
import snap.view.ViewEvent;

/**
 * A View subclass to render.
 */
public class CubeView extends View {

    // The Camera
    private Camera  _camera;

    // The Scene
    private Scene3D  _scene;

    // The Cube
    private CubeShape  _cubeShape;

    // The MouseHandler
    private MouseHandler  _mouseHandler;

    // The CameraView to control
    private CameraView  _cameraView;

    // A HitDetector for mouse move highlite updating
    private HitDetector  _hitDetector = new HitDetector();

    // The last hit pos if getSideAtViewXY() hits a side
    private Pos  _hitPos;

    // The side/pos currently under the mouse
    private Side3D  _mouseSide;
    private Pos  _mousePos;

    // Constants for properties
    public static final String Yaw_Prop = Camera.Yaw_Prop;
    public static final String Pitch_Prop = Camera.Pitch_Prop;
    public static final String Roll_Prop = Camera.Roll_Prop;
    public static final String PrefGimbalRadius_Prop = Camera.PrefGimbalRadius_Prop;

    /**
     * Constructor.
     */
    public CubeView()
    {
        _scene = new Scene3D();

        // Get/config camera
        _camera = _scene.getCamera();
        _camera.setFocalLength(2 * 72);
        _camera.addPropChangeListener(pce -> cameraChanged(pce));

        // Set preferred size
        setPrefSize(75, 75);

        // Add Cube to CubeView
        addCube();

        // Enable events for rotations
        enableEvents(MousePress, MouseDrag, MouseRelease, MouseMove, MouseExit);
        _mouseHandler = new MouseHandler(_camera);
    }

    /**
     * Returns the CameraView to sync with.
     */
    public CameraView getCameraView()  { return _cameraView; }

    /**
     * Sets the CameraView to sync with.
     */
    public void setCameraView(CameraView aCameraView)
    {
        // If already set, just return
        if (aCameraView == _cameraView) return;

        // Set value
        _cameraView = aCameraView;

        // Sync Yaw/Pitch/Roll
        setYaw(_cameraView.getYaw());
        setPitch(_cameraView.getPitch());
        setRoll(_cameraView.getRoll());
    }

    /**
     * Returns the camera as a vector.
     */
    public Camera getCamera()  { return _camera; }

    /**
     * Returns the Scene3D.
     */
    public Scene3D getScene()  { return _scene; }

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
     * Adds cube to view.
     */
    protected void addCube()
    {
        _cubeShape = new CubeShape();
        _scene.addChild(_cubeShape);
    }

    /**
     * Returns the shape hit by camera ray going through point in view coords.
     */
    public Side3D getSideAtViewXY(double aX, double aY)
    {
        // Get ray from camera origin to camera view point in scene space
        Camera camera = getCamera();
        Point3D rayOrigin = new Point3D();
        Vector3D rayDir = new Vector3D();
        camera.getRayToViewPoint(aX, aY, rayOrigin, rayDir);

        // Iterate over cube sides
        for (Side3D side : Side3D.values()) {

            // Get side and see if hit
            FacetShape sideShape = _cubeShape.getSideShape(side);
            boolean isHit = _hitDetector.isRayHitShape(rayOrigin, rayDir, sideShape);

            // if hit, calculate Pos on side under mouse, then return side
            if (isHit) {
                Point hitTexCoord = _hitDetector.getHitTexCoord();
                HPos hpos = hitTexCoord.x < .3 ? HPos.LEFT : hitTexCoord.x <= .7 ? HPos.CENTER : HPos.RIGHT;
                VPos vpos = hitTexCoord.y < .3 ? VPos.BOTTOM : hitTexCoord.y <= .7 ? VPos.CENTER : VPos.TOP;
                _hitPos = Pos.get(hpos, vpos);
                return side;
            }
        }

        // Return null since no side hit
        return null;
    }

    /**
     * Set camera view to given side.
     */
    public void setCameraViewToSideAndPosAnimated(Side3D aSide, Pos aPos)
    {
        // Get animator and startAutoRegisterChanges
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        ViewAnim anim = cameraView.getAnimCleared(1000);
        anim.startAutoRegisterChanges(Camera.Yaw_Prop, Camera.Pitch_Prop, Camera.Roll_Prop);

        // Change Camera to view side
        Camera camera = cameraView.getCamera();
        camera.setYawPitchRollForSideAndPos(aSide, aPos);

        // stopAutoRegisterChanges and register to clear PrefGimbalRadius if it was set
        anim.stopAutoRegisterChanges();

        // Play animations
        anim.play();
    }

    /**
     * Paints camera.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        _camera.paintScene(aPntr);
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMouseMove())
            processMouseMove(anEvent);

        // Handle MousePress, MouseDrag, MouseRelease, Scroll
        else {

            // Have standard MouseHander handle drag
            _mouseHandler.processEvent(anEvent);

            // If MouseRelease click, rotate to HitSide/HitPos
            if (anEvent.isMouseRelease() && anEvent.isMouseClick()) {
                Side3D hitSide = getSideAtViewXY(anEvent.getX(), anEvent.getY());
                if (hitSide != null)
                    setCameraViewToSideAndPosAnimated(hitSide, _hitPos);
            }
        }
    }

    /**
     * Handle MouseMove.
     */
    private void processMouseMove(ViewEvent anEvent)
    {
        // Recalculate HitSide/HitPos and reset CubeShape
        Side3D hitSide = getSideAtViewXY(anEvent.getX(), anEvent.getY());
        if (hitSide != _mouseSide || _hitPos != _mousePos) {
            _cubeShape.setTextureForSideAndPos(hitSide, _hitPos);
            _mouseSide = hitSide;
            _mousePos = _hitPos;
            repaint();
        }

        anEvent.consume();
    }

    /**
     * Override to forward to camera.
     */
    public void setWidth(double aValue)
    {
        super.setWidth(aValue);
        _camera.setViewWidth(aValue);
    }

    /**
     * Override to forward to camera.
     */
    public void setHeight(double aValue)
    {
        super.setHeight(aValue);
        _camera.setViewHeight(aValue);
    }

    /**
     * Called when scene changes.
     */
    protected void cameraChanged(PropChange aPC)
    {
        // Forward on basic Camera prop changes
        String propName = aPC.getPropName();
        if (propName == Yaw_Prop || propName == Pitch_Prop || propName == Roll_Prop) {
            _pcs.firePropChange(aPC);
            if (_cameraView != null)
                _cameraView.setPropValue(propName, aPC.getNewValue());
        }

        // Repaint
        repaint();
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Handle Yaw, Pitch, Roll, PrefGimbalRadius
            case Yaw_Prop: return getYaw();
            case Pitch_Prop: return getPitch();
            case Roll_Prop: return getRoll();
            case PrefGimbalRadius_Prop: return _camera.getPrefGimbalRadius();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Handle Yaw, Pitch, Roll, PrefGimbalRadius
            case Yaw_Prop: setYaw(SnapUtils.doubleValue(aValue)); break;
            case Pitch_Prop: setPitch(SnapUtils.doubleValue(aValue)); break;
            case Roll_Prop: setRoll(SnapUtils.doubleValue(aValue)); break;
            case PrefGimbalRadius_Prop: _camera.setPrefGimbalRadius(SnapUtils.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}