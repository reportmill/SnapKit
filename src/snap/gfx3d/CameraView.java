package snap.gfx3d;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A View subclass to render.
 */
public class CameraView extends ParentView {

    // The Camera
    private Camera  _camera;
    
    // The Scene
    private Scene3D  _scene;

    // Constants for properties
    public static final String Yaw_Prop = Camera.Yaw_Prop;
    public static final String Pitch_Prop = Camera.Pitch_Prop;
    public static final String Roll_Prop = Camera.Roll_Prop;
    public static final String PrefGimbalRadius_Prop = Camera.PrefGimbalRadius_Prop;

    /**
     * Constructor.
     */
    public CameraView()
    {
        _scene = new Scene3D();
        _camera = _scene.getCamera();
        _camera.addPropChangeListener(pce -> cameraChanged(pce));
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll);
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
     * Rebuilds display list of Path3Ds from Shapes.
     */
    protected void layoutImpl()  { }

    /**
     * Paints shape children.
     */
    protected void paintChildren(Painter aPntr)
    {
        // Paint Scene paths
        _camera.paintScene(aPntr);

        // Do normal version
        super.paintChildren(aPntr);
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        _camera.processEvent(anEvent);
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
     * Override to account for Scene3D bounds.
     */
    public void repaint()
    {
        Rect bnds = getBoundsMarked();
        repaintInParent(bnds);
    }

    /**
     * Override to account for Scene3D bounds.
     */
    public Rect getBoundsMarked()
    {
        Rect bounds = getBoundsLocal();
        Rect camBnds = _camera.getSceneBounds2D();
        if (camBnds.x < bounds.x)
            bounds.x = camBnds.x;
        if (camBnds.y < bounds.y)
            bounds.y = camBnds.y;
        if (camBnds.getMaxX() > bounds.getMaxX())
            bounds.width = camBnds.getMaxX() - bounds.x;
        if (camBnds.getMaxY() > bounds.getMaxY())
            bounds.height = camBnds.getMaxY() - bounds.y;
        bounds.inset(-2);
        return bounds;
    }

    /**
     * Called when scene changes.
     */
    protected void cameraChanged(PropChange aPC)
    {
        // Forward on basic Camera prop changes
        String propName = aPC.getPropName();
        if (propName == Yaw_Prop || propName == Pitch_Prop || propName == Roll_Prop || propName == PrefGimbalRadius_Prop)
            _pcs.firePropChange(aPC);

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