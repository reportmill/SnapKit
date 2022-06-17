package snap.gfx3d;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.props.PropChange;

/**
 * This is an abstract class to renders a Scene3D for a Camera.
 */
public abstract class Renderer {

    // The Camera
    protected Camera  _camera;

    // The Scene
    protected Scene3D  _scene;

    // The factory that made this renderer
    protected RendererFactory  _factory;

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
     * Returns the name of this renderer.
     */
    public abstract String getName();

    /**
     * Returns the Camera.
     */
    public Camera getCamera()  { return _camera; }

    /**
     * Returns the Scene.
     */
    public Scene3D getScene()  { return _scene; }

    /**
     * Returns the 2D bounding rect for scene in camera bounds.
     */
    public Rect getSceneBoundsInView()
    {
        // This is a totally bogus implementation - just returns View bounds
        Camera camera = getCamera();
        double viewW = camera.getViewWidth();
        double viewH = camera.getViewHeight();
        return new Rect(0, 0, viewW, viewH);
    }

    /**
     * Renders scene for given painter, camera and scene.
     */
    public abstract void renderAndPaint(Painter aPainter);

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
        Renderer renderer = RendererFactory.newDefaultRenderer(aCamera);
        return renderer;
    }
}
