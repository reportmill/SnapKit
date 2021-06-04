package snap.gfx3d;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.util.PropChange;

/**
 * This is an abstract class to renders a Scene3D for a Camera.
 */
public abstract class Renderer {

    // The Camera
    protected Camera  _camera;

    // The Scene
    protected Scene3D  _scene;

    /**
     * Constructor.
     */
    public Renderer(Camera aCamera)
    {
        _camera = aCamera;
        _scene = _camera.getScene();

        _camera.addPropChangeListener(pc -> cameraDidPropChange(pc));
    }

    /**
     * Returns the 2D bounding rect for scene in camera bounds.
     */
    public abstract Rect getSceneBounds();

    /**
     * Renders scene for given painter, camera and scene.
     */
    public abstract void renderAll(Painter aPainter);

    /**
     * Called when camera changes.
     */
    protected void cameraDidPropChange(PropChange aPC)  { }

    /**
     * Called when scene changes.
     */
    protected void sceneDidChange()  { }

    /**
     * Returns a new default renderer.
     */
    public static Renderer newRenderer(Camera aCamera)
    {
        return new Renderer2D(aCamera);
    }
}
