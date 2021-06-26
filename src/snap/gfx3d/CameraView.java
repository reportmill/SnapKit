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
    public static String   Yaw_Prop = "Yaw";
    public static String   Pitch_Prop = "Pitch";
    public static String   Roll_Prop = "Roll";
    public static String   OffsetZ_Prop = "OffsetZ";
    
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
     * Returns the depth of the scene.
     */
    public double getDepth()  { return _camera.getDepth(); }

    /**
     * Sets the depth of the scene.
     */
    public void setDepth(double aValue)  { _camera.setDepth(aValue); }

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
     * Returns the focal length of the camera (derived from the field of view and with view size).
     */
    public double getFocalLength()  { return _camera.getFocalLength(); }

    /**
     * Sets the focal length of the camera. Two feet is normal (1728 points).
     */
    public void setFocalLength(double aValue)  { _camera.setFocalLength(aValue); }

    /**
     * Returns the Z offset of the scene (for zooming).
     */
    public double getOffsetZ()  { return _camera.getOffsetZ(); }

    /**
     * Sets the Z offset of the scene (for zooming).
     */
    public void setOffsetZ(double aValue)  { _camera.setOffsetZ(aValue); }

    /**
     * Returns whether scene is rendered in pseudo 3d.
     */
    public boolean isPseudo3D()  { return _camera.isPseudo3D(); }

    /**
     * Sets whether scene is rendered in pseudo 3d.
     */
    public void setPseudo3D(boolean aFlag)  { _camera.setPseudo3D(aFlag); }

    /**
     * Returns the skew angle for X by Z.
     */
    public double getPseudoSkewX()  { return _camera.getPseudoSkewX(); }

    /**
     * Sets the skew angle for X by Z.
     */
    public void setPseudoSkewX(double anAngle)  { _camera.setPseudoSkewX(anAngle); }

    /**
     * Returns the skew angle for Y by Z.
     */
    public double getPseudoSkewY()  { return _camera.getPseudoSkewY(); }

    /**
     * Sets the skew angle for Y by Z.
     */
    public void setPseudoSkewY(double anAngle)  { _camera.setPseudoSkewY(anAngle); }

    /**
     * Returns the field of view of the camera (derived from focalLength).
     */
    public double getFieldOfView()  { return _camera.getFieldOfView(); }

    /**
     * Sets the field of view of the camera.
     */
    public void setFieldOfView(double aValue)  { _camera.setFieldOfView(aValue); }

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
     * Override to forward to Scene3D.
     */
    public void setWidth(double aValue)
    {
        super.setWidth(aValue);
        _camera.setWidth(aValue);
    }

    /**
     * Override to forward to Scene3D.
     */
    public void setHeight(double aValue)
    {
        super.setHeight(aValue);
        _camera.setHeight(aValue);
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
        //relayout();
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
        if (aPropName.equals(OffsetZ_Prop))
            return getOffsetZ();
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
        else if (aPropName.equals(OffsetZ_Prop))
            setOffsetZ(SnapUtils.doubleValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }
}