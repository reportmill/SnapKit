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

    // Whether to sort surfaces
    private boolean  _sortSurfaces = true;

    // List of all Scene Shape Path3Ds in view coords
    private List<Path3D>  _surfacesInViewCoords = new ArrayList<>();

    // Bounds of paths in scene
    private Rect  _sceneBounds2D;

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
     * Returns whether to sort surfaces.
     */
    public boolean isSortSurfaces()  { return _sortSurfaces; }

    /**
     * Sets whether to sort surfaces.
     */
    public void setSortSurfaces(boolean aValue)  { _sortSurfaces = aValue; }

    /**
     * Returns a list of all Scene.Shapes component surfaces Path3Ds in view coords.
     */
    public List<Path3D> getSurfacesInViewCoords()
    {
        // If already set, just return
        if (_surfacesInViewCoords != null) return _surfacesInViewCoords;

        // Get paths, set and return
        List<Path3D> paths = getSurfacesInViewCoordsImpl();
        return _surfacesInViewCoords = paths;
    }

    /**
     * Returns a list of all Scene.Shapes component surfaces Path3Ds in view coords.
     */
    protected List<Path3D> getSurfacesInViewCoordsImpl()
    {
        // Get surfaces in camera coords
        List<Path3D> pathsInCameraCoords = getSurfacesInCameraCoords();

        // Sort surface paths
        if (isSortSurfaces()) {
            try {
                pathsInCameraCoords.sort((p0, p1) -> Sort3D.comparePath3D_MinZs(p0, p1));
                pathsInCameraCoords.sort((p0, p1) -> Sort3D.comparePath3Ds(p0, p1));
            }
            catch (Exception e) {
                System.err.println("Renderer2D.getSurfacesInViewCoordsImpl: Sort failed: " + e);
            }
        }

        // Add in Path3D painter paths
        addPathPainterPathsInCameraCoords(pathsInCameraCoords);

        // Get Camera projection transform
        Camera3D camera = getCamera();
        Transform3D projTrans = camera.getProjectionTransform();

        // Get Camera display transform
        double viewW = camera.getViewWidth();
        double viewH = camera.getViewHeight();
        Transform3D dispTrans = projTrans.clone().scale(viewW / 2, -viewH / 2, 1);

        // Iterate over paths and replace with paths in view coords
        List<Path3D> pathsInViewCoords = new ArrayList<>();
        for (Path3D pathInWorld : pathsInCameraCoords) {
            Path3D pathInView = pathInWorld.copyForTransform(dispTrans);
            pathsInViewCoords.add(pathInView);
        }

        // Return paths in view coords
        return pathsInViewCoords;
    }

    /**
     * Returns a list of all Scene.Shapes component surfaces Path3Ds in camera coords.
     */
    protected List<Path3D> getSurfacesInCameraCoords()
    {
        // Get scene and add surfaces deep
        Scene3D scene = getScene();
        List<Path3D> pathsList = new ArrayList<>();
        addShapeSurfacesInCameraCoords(scene, pathsList);

        // Return list of shape surface paths in world coords
        return pathsList;
    }

    /**
     * Adds the paths for shape.
     */
    protected void addShapeSurfacesInCameraCoords(Shape3D aShape, List<Path3D> thePathsList)
    {
        // Handle ParentShape: Get children and recurse
        if (aShape instanceof ParentShape3D) {
            ParentShape3D parentShape = (ParentShape3D) aShape;
            Shape3D[] children = parentShape.getChildren();
            for (Shape3D child : children)
                addShapeSurfacesInCameraCoords(child, thePathsList);
        }

        // Handle Path3D
        else if (aShape instanceof Path3D) {
            Path3D path3D = (Path3D) aShape;
            addShapeSurfacesInCameraCoords(path3D, thePathsList);
        }
    }

    /**
     * Adds the paths for shape.
     */
    protected void addShapeSurfacesInCameraCoords(Path3D surfacePath, List<Path3D> thePathsList)
    {
        // Get the camera transform & optionally align it to the screen
        Transform3D sceneToCamera = _camera.getTransform();
        Light3D light = _scene.getLight();
        Color color = surfacePath.getColor();

        // Get path normal (if bogus, complain and return - not sure this happens anymore)
        Vector3D pathNormLocal = surfacePath.getNormal();
        if (Double.isNaN(pathNormLocal.x)) {
            System.err.println("Renderer2D.addShapeSurfacesInCameraCoords: Invalid path");
            return;
        }

        // Get path normal in camera coords
        Vector3D pathNormCamera = sceneToCamera.transformVector(pathNormLocal.clone());
        pathNormCamera.normalize();

        // Get camera-to-path vector in camera coords
        Point3D pathCenterLocal = surfacePath.getBoundsCenter();
        Point3D pathCenterCamera = sceneToCamera.transformPoint(pathCenterLocal.clone());
        Vector3D cameraToPathVect = new Vector3D(pathCenterCamera.x, pathCenterCamera.y, pathCenterCamera.z);

        // Backface culling : If path pointed away from camera, skip path
        if (cameraToPathVect.isAligned(pathNormCamera, false)) {

            // If not double-sided, just skip
            if (!surfacePath.isDoubleSided())
                return;

            // Otherwise, negate normal
            pathNormCamera.negate();
        }

        // Get path copy transformed by scene transform
        Path3D dispPath3D = surfacePath.copyForTransform(sceneToCamera);

        // If color on shape, set color on path for scene lights
        if (color != null) {
            Color rcol = light.getRenderColor(pathNormCamera, color);
            dispPath3D.setColor(rcol);
        }

        // Add path
        thePathsList.add(dispPath3D);
    }

    /**
     * Iterates over path list and adds Path.Painter.PainterTasks as Path3Ds in Camera coords.
     */
    private void addPathPainterPathsInCameraCoords(List<Path3D> thePaths)
    {
        // Iterate over paths
        for (int i = thePaths.size() - 1; i >= 0; i--) {

            // Get path (just skip if no Painter)
            Path3D path3D = thePaths.get(i); if (path3D.getPainter() == null) continue;

            // Get Paths for painter
            Matrix3D cameraToScene = new Matrix3D().fromArray(_camera.getTransform().toArray()).invert();
            Path3D path3DInSceneCoords = path3D.copyForMatrix(cameraToScene);
            Path3D[] painterPathsInCameraCoords = getPathPainterPathsInCameraCoords(path3DInSceneCoords);

            // Add paths to list right behind the original path
            for (int j = 0; j < painterPathsInCameraCoords.length; j++) {
                Path3D painterPath = painterPathsInCameraCoords[j];
                thePaths.add(i + j + 1, painterPath);
            }
        }
    }

    /**
     * Returns a path.
     */
    private Path3D[] getPathPainterPathsInCameraCoords(Path3D path3D)
    {
        // Get Painter, PainterTasks and pathPainterPaths array
        Painter3D painter3D = path3D.getPainter();
        Painter3D.PaintTask[] paintTasks = painter3D.getPaintTasks();
        Path3D[] pathPainterPaths = new Path3D[paintTasks.length];

        // Get transform from painter to camera
        Matrix3D painterToPath = path3D.getPainterToLocal();
        Matrix3D pathToCamera = new Matrix3D().fromArray(_camera.getTransform().toArray());

        // Get transform from painter to view
        for (int i = 0; i < paintTasks.length; i++) {
            Painter3D.PaintTask paintTask = paintTasks[i];
            Path3D paintTaskPath3D = new Path3D(paintTask.getShape(), 0);
            paintTaskPath3D.transform(painterToPath);
            paintTaskPath3D.transform(pathToCamera);
            paintTaskPath3D.setStroke(paintTask.getColor(), paintTask.getStroke().getWidth());
            pathPainterPaths[i] = paintTaskPath3D;
        }

        // Return
        return pathPainterPaths;
    }

    /**
     * Returns the bounding rect of scene in view coords.
     */
    @Override
    public Rect getSceneBoundsInView()
    {
        // If already set, just return
        if (_sceneBounds2D != null) return _sceneBounds2D;

        // Get Scene to View transform
        Transform3D sceneToCamera = _camera.getTransform();
        Transform3D cameraToNDC = _camera.getProjectionTransform();
        double viewW = _camera.getViewWidth();
        double viewH = _camera.getViewHeight();
        Transform3D cameraToView = cameraToNDC.clone().scale(viewW / 2, -viewH / 2, 1);
        Transform3D sceneToView = sceneToCamera.multiply(cameraToView);

        // Get Scene bounds in View
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        Bounds3D sceneBoundsInView = sceneBounds.copyForTransform(sceneToView);

        // Get scene bounds (shift to camera view mid point)
        double sceneX = sceneBoundsInView.getMinX() + viewW / 2;
        double sceneY = sceneBoundsInView.getMinY() + viewH / 2;
        double sceneW = sceneBoundsInView.getWidth();
        double sceneH = sceneBoundsInView.getHeight();

        // Create, set, return bounds rect
        return _sceneBounds2D = new Rect(sceneX, sceneY, sceneW, sceneH);
    }

    /**
     * Called to indicate that paths list needs to be rebuilt.
     */
    protected void rebuildPaintSurfaces()
    {
        _surfacesInViewCoords = null;
        _sceneBounds2D = null;
    }

    /**
     * Override to rebuild paths for camera changes.
     */
    @Override
    protected void cameraDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        switch (propName) {
            case Camera3D.ViewWidth_Prop:
            case Camera3D.ViewHeight_Prop:
            case Camera3D.Depth_Prop:
            case Camera3D.Yaw_Prop:
            case Camera3D.Pitch_Prop:
            case Camera3D.Roll_Prop:
            case Camera3D.FocalLength_Prop:
            case Camera3D.PrefGimbalRadius_Prop:
                rebuildPaintSurfaces();
        }
        super.cameraDidPropChange(aPC);
    }

    /**
     * Called when scene changes.
     */
    protected void sceneDidChange()
    {
        rebuildPaintSurfaces();
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
        // Translate to center of camera view
        Camera3D camera = getCamera();
        double viewW = camera.getViewWidth();
        double viewH = camera.getViewHeight();
        double viewMidX = viewW / 2;
        double viewMidY = viewH / 2;
        aPntr.translate(viewMidX, viewMidY);

        // Iterate over Path3Ds and paint
        List<Path3D> paths = getSurfacesInViewCoords();
        for (Path3D child : paths)
            paintPath3D(aPntr, child);

        // Translate back
        aPntr.translate(-viewMidX, -viewMidY);
    }

    /**
     * Paints a Path3D.
     */
    protected void paintPath3D(Painter aPntr, Path3D aPath3D)
    {
        // Get path, fill and stroke
        Shape path = aPath3D.getShape2D();
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
