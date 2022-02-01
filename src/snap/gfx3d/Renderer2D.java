package snap.gfx3d;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.gfx.Painter;
import snap.util.PropChange;
import java.util.ArrayList;
import java.util.List;

/**
 * This Renderer subclass tries to render the Scene using the standard Painter (2D).
 */
public class Renderer2D extends Renderer {

    // List of Path3Ds - for rendering
    private List<Path3D>  _paths = new ArrayList<>();

    // Bounds of paths in scene
    private Rect  _sceneBounds;

    // Whether paths list needs to be rebuilt
    private boolean  _rebuildPaths;

    // Constant for name
    private static final String RENDERER_NAME = "Vector 2D";

    /**
     * Constructor.
     */
    public Renderer2D(Camera3D aCamera)
    {
        super(aCamera);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return RENDERER_NAME; }

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
     * Returns the bounding rect for camera paths.
     */
    public Rect getSceneBounds()
    {
        // If already set, just return
        if (_sceneBounds != null) return _sceneBounds;

        // Iterate over paths
        List<Path3D> paths = getPaths();
        double xmin = Float.MAX_VALUE, xmax = -xmin;
        double ymin = Float.MAX_VALUE, ymax = -ymin;
        for (Path3D path : paths) {
            Box3D boundsBox = path.getBoundsBox();
            xmin = Math.min(xmin, boundsBox.getMinX());
            ymin = Math.min(ymin, boundsBox.getMinY());
            xmax = Math.max(xmax, boundsBox.getMaxX());
            ymax = Math.max(ymax, boundsBox.getMaxY());
        }

        // Get camera midpoints
        Camera3D camera = getCamera();
        double dispMidX = camera.getWidth() / 2;
        double dispMidY = camera.getHeight() / 2;

        // Get scene bounds (shift to camera view mid point)
        double sceneX = xmin + dispMidX;
        double sceneY = ymin + dispMidY;
        double sceneW = xmax - xmin;
        double sceneH = ymax - ymin;

        // Create, set, return bounds rect
        return _sceneBounds = new Rect(sceneX, sceneY, sceneW, sceneH);
    }

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
        // Remove all existing Path3Ds
        removePaths();
        _sceneBounds = null;

        // Iterate over shapes and add paths
        rebuildPathsImpl();

        // Resort paths
        Sort3D.sortPaths(_paths);

        // Get projection transform
        Camera3D camera3D = getCamera();
        Transform3D projTrans = camera3D.getProjectionTransform();

        // Iterate over paths and replace with paths in display space
        for (int i = 0, iMax = _paths.size(); i < iMax; i++) {
            Path3D path = _paths.get(i);
            Path3D path2 = path.copyForTransform(projTrans);
            _paths.set(i, path2);
        }

        // Clear RebuildPaths
        _rebuildPaths = false;
    }

    /**
     * Rebuilds display list of Path3Ds from Shapes.
     */
    protected void rebuildPathsImpl()
    {
        // Iterate over shapes and add paths
        List <Shape3D> shapes = _scene._shapes;
        for (Shape3D shp : shapes)
            addPathsForShape(shp);
    }

    /**
     * Adds the paths for shape.
     */
    protected void addPathsForShape(Shape3D aShape)
    {
        // Get the camera transform & optionally align it to the screen
        Transform3D worldToCameraXfm = _camera.getTransform();
        Light3D light = _scene.getLight();
        Color color = aShape.getColor();

        // Iterate over paths
        Path3D[] path3Ds = aShape.getPath3Ds();
        for (Path3D path3d : path3Ds) {

            // If not surface (just line), do simple add
            if (!path3d.isSurface()) {
                Path3D dispPath3D = path3d.copyForTransform(worldToCameraXfm);
                addPath(dispPath3D);
                continue;
            }

            // Get normal
            Vector3D pathNormal = path3d.getNormal();
            Vector3D camPathNormal = worldToCameraXfm.transformVector(pathNormal.clone());
            camPathNormal.normalize();

            // Backface culling : Only add paths that face the camera
            if (_camera.isFacingAway(camPathNormal))
                continue;

            // Get path copy transformed by scene transform
            Path3D dispPath3D = path3d.copyForTransform(worldToCameraXfm);

            // If color on shape, set color on path for scene lights
            if (color != null) {
                Color rcol = light.getRenderColor(camPathNormal, color);
                dispPath3D.setColor(rcol);
            }

            // Add path
            addPath(dispPath3D);
        }
    }

    /**
     * Override to rebuild paths for camera changes.
     */
    @Override
    protected void cameraDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        switch (propName) {
            case Camera3D.Width_Prop:
            case Camera3D.Height_Prop:
            case Camera3D.Depth_Prop:
            case Camera3D.Yaw_Prop:
            case Camera3D.Pitch_Prop:
            case Camera3D.Roll_Prop:
            case Camera3D.FocalLength_Prop:
            case Camera3D.PrefGimbalRadius_Prop:
            case Camera3D.AdjustZ_Prop:
            case Camera3D.Pseudo3D_Prop:
            case Camera3D.PseudoSkewX_Prop:
            case Camera3D.PseudoSkewY_Prop:
                rebuildPaths();
        }
        super.cameraDidPropChange(aPC);
    }

    /**
     * Called when scene changes.
     */
    protected void sceneDidChange()
    {
        rebuildPaths();
    }

    /**
     * Renders scene for given painter, camera and scene.
     */
    public void renderAll(Painter aPntr)
    {
        paintPaths(aPntr);
    }

    /**
     * Paints shape children.
     */
    public void paintPaths(Painter aPntr)
    {
        // Translate to center
        Camera3D camera = getCamera();
        double dispMidX = camera.getWidth() / 2;
        double dispMidY = camera.getHeight() / 2;
        aPntr.translate(dispMidX, dispMidY);

        // Iterate over Path3Ds and paint
        List<Path3D> paths = getPaths();
        for (int i = 0, iMax = paths.size(); i < iMax; i++) {

            // Paint path and path layers
            Path3D child = paths.get(i);
            paintPath3D(aPntr, child);
            if (child.getLayers().size() > 0)
                for (Path3D layer : child.getLayers())
                    paintPath3D(aPntr, layer);
        }

        // Translate back
        aPntr.translate(-dispMidX, -dispMidY);
    }

    /**
     * Paints a Path3D.
     */
    protected void paintPath3D(Painter aPntr, Path3D aPath3D)
    {
        // Get path, fill and stroke
        Shape path = aPath3D.getPath();
        Paint fill = aPath3D.getColor();
        Paint stroke = aPath3D.getStrokeColor();

        // Get opacity and set if needed
        double op = aPath3D.getOpacity(), oldOP = 0;
        if (op < 1) {
            oldOP = aPntr.getOpacity();
            aPntr.setOpacity(op * oldOP);
        }

        // Do fill and stroke
        if (fill != null) {
            aPntr.setPaint(fill);
            aPntr.fill(path);
        }
        if (stroke != null) {
            aPntr.setPaint(stroke);
            aPntr.setStroke(aPath3D.getStroke());
            aPntr.draw(path);
        }

        // Reset opacity if needed
        if (op < 1)
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
     * Registers factory.
     */
    public static void registerFactory()
    {
        // If already set, just return
        for (RendererFactory factory : RendererFactory.getFactories())
            if (factory.getClass() == Renderer2DFactory.class)
                return;

        // Create, add and setDefault
        RendererFactory factory = new Renderer2DFactory();
        RendererFactory.addFactory(factory);
        RendererFactory.setDefaultFactory(factory);
    }

    /**
     * A default implementation.
     */
    private static class Renderer2DFactory extends RendererFactory {

        /**
         * Returns the renderer name.
         */
        public String getRendererName()  { return RENDERER_NAME; }

        /**
         * Returns a new default renderer.
         */
        public Renderer newRenderer(Camera3D aCamera)
        {
            return new Renderer2D(aCamera);
        }
    }
}
