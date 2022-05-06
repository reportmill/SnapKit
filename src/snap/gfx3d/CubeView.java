package snap.gfx3d;
import snap.gfx.Painter;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.View;
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
        enableEvents(MousePress, MouseDrag, MouseRelease);
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
        _mouseHandler.processEvent(anEvent);
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