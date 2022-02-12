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
    private Camera3D _camera;
    
    // The Scene
    private Scene3D  _scene;

    // Constants for properties
    public static final String Yaw_Prop = Camera3D.Yaw_Prop;
    public static final String Pitch_Prop = Camera3D.Pitch_Prop;
    public static final String Roll_Prop = Camera3D.Roll_Prop;

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
    public Camera3D getCamera()  { return _camera; }

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
     * Returns the number of shapes in the shape list.
     */
    public int getShapeCount()  { return _scene.getShapeCount(); }

    /**
     * Returns the specific shape at the given index from the shape list.
     */
    public Shape3D getShape(int anIndex)  { return _scene.getShape(anIndex); }

    /**
     * Adds a shape to the end of the shape list.
     */
    public void addShape(Shape3D aShape)  { _scene.addShape(aShape); }

    /**
     * Removes the shape at the given index from the shape list.
     */
    public void removeShapes()  { _scene.removeShapes(); }

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
        Rect camBnds = _camera.getSceneBounds();
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
        //_pcs.fireDeepChange(this, aPC);
        relayout();
        repaint();
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals(Yaw_Prop))
            return getYaw();
        if (aPropName.equals(Pitch_Prop))
            return getPitch();
        if (aPropName.equals(Roll_Prop))
            return getRoll();
        return super.getPropValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals(Yaw_Prop))
            setYaw(SnapUtils.doubleValue(aValue));
        else if (aPropName.equals(Pitch_Prop))
            setPitch(SnapUtils.doubleValue(aValue));
        else if (aPropName.equals(Roll_Prop))
            setRoll(SnapUtils.doubleValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }
}