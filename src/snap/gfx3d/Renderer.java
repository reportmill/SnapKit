package snap.gfx3d;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.util.PropChange;

/**
 * This is an abstract class to renders a Scene3D for a Camera.
 */
public abstract class Renderer {

    // The Camera
    protected Camera3D _camera;

    // The Scene
    protected Scene3D  _scene;

    // The factory that made this renderer
    protected RendererFactory  _factory;

    // Whether FrontFace is clockwise
    public static boolean FRONT_FACE_IS_CW = true;

    /**
     * Constructor.
     */
    public Renderer(Camera3D aCamera)
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
    public Camera3D getCamera()  { return _camera; }

    /**
     * Returns the Scene.
     */
    public Scene3D getScene()  { return _scene; }

    /**
     * Returns the Scene.
     */
    public Shape3D[] getSceneShapes()  { return _scene._shapes.toArray(new Shape3D[0]); }

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
    public static Renderer newRenderer(Camera3D aCamera)
    {
        Renderer renderer = RendererFactory.newDefaultRenderer(aCamera);
        return renderer;
    }
}
