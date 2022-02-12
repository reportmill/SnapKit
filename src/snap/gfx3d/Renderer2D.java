package snap.gfx3d;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.gfx.Painter;
import snap.util.PropChange;
import java.util.ArrayList;
import java.util.Collections;
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
    private Rect _sceneBounds2D;

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
            Collections.sort(pathsInCameraCoords, (p0, p1) -> Sort3D.comparePath3D_MinZs(p0, p1));
            Collections.sort(pathsInCameraCoords, (p0, p1) -> Sort3D.comparePath3Ds(p0, p1));
        }

        // Get Camera projection transform
        Camera3D camera = getCamera();
        Transform3D projTrans = camera.getProjectionTransform();

        // Get Camera display transform
        double viewW = camera.getViewWidth();
        double viewH = camera.getViewHeight();
        Transform3D dispTrans = projTrans.clone().scale(viewW / 2, -viewH / 2, 1);

        // Iterate over paths and replace with paths in view coords
        List<Path3D> pathsInViewCoords = new ArrayList<>();
        for (int i = 0, iMax = pathsInCameraCoords.size(); i < iMax; i++) {
            Path3D pathInWorld = pathsInCameraCoords.get(i);
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
        // Get all Scene.Shapes
        List<Shape3D> sceneShapes = _scene._shapes;

        // Create Path3D list
        List<Path3D> pathsList = new ArrayList<>();

        // Iterate over shapes and add paths
        for (Shape3D shape : sceneShapes)
            addShapeSurfacesInCameraCoords(shape, pathsList);

        // Return list of shape surface paths in world coords
        return pathsList;
    }

    /**
     * Adds the paths for shape.
     */
    protected void addShapeSurfacesInCameraCoords(Shape3D aShape, List<Path3D> thePathsList)
    {
        // Get the camera transform & optionally align it to the screen
        Transform3D worldToCameraXfm = _camera.getTransform();
        Light3D light = _scene.getLight();
        Color color = aShape.getColor();

        // Iterate over paths
        Path3D[] surfacePaths = aShape.getPath3Ds();
        for (Path3D surfacePath : surfacePaths) {

            // If not surface (just line), do simple add
            if (!surfacePath.isSurface()) {
                Path3D dispPath3D = surfacePath.copyForTransform(worldToCameraXfm);
                thePathsList.add(dispPath3D);
                continue;
            }

            // Get path normal in camera coords
            Vector3D pathNormLocal = surfacePath.getNormal();
            Vector3D pathNormCamera = worldToCameraXfm.transformVector(pathNormLocal.clone());
            pathNormCamera.normalize();

            // Get camera-to-path vector in camera coords
            Point3D pathCenterLocal = surfacePath.getCenter();
            Point3D pathCenterCamera = worldToCameraXfm.transformPoint(pathCenterLocal.clone());
            Vector3D cameraToPathVect = new Vector3D(pathCenterCamera.x, pathCenterCamera.y, pathCenterCamera.z);

            // Backface culling : If path pointed away from camera, skip path
            if (cameraToPathVect.isAligned(pathNormCamera, false))
                continue;

            // Get path copy transformed by scene transform
            Path3D dispPath3D = surfacePath.copyForTransform(worldToCameraXfm);

            // If color on shape, set color on path for scene lights
            if (color != null) {
                Color rcol = light.getRenderColor(pathNormCamera, color);
                dispPath3D.setColor(rcol);
            }

            // Add path
            thePathsList.add(dispPath3D);
        }
    }

    /**
     * Returns the bounding rect for camera paths.
     */
    public Rect getSceneBounds2D()
    {
        // If already set, just return
        if (_sceneBounds2D != null) return _sceneBounds2D;

        // Get all surface paths in view coords
        List<Path3D> surfacePathsInViewCoords = getSurfacesInViewCoords();

        // Iterate over all surface paths and get combined X/Y min/max points
        double xmin = Float.MAX_VALUE, xmax = -xmin;
        double ymin = Float.MAX_VALUE, ymax = -ymin;
        for (Path3D path : surfacePathsInViewCoords) {
            Box3D boundsBox = path.getBoundsBox();
            xmin = Math.min(xmin, boundsBox.getMinX());
            ymin = Math.min(ymin, boundsBox.getMinY());
            xmax = Math.max(xmax, boundsBox.getMaxX());
            ymax = Math.max(ymax, boundsBox.getMaxY());
        }

        // Get camera midpoints
        Camera3D camera = getCamera();
        double viewW = camera.getViewWidth();
        double viewH = camera.getViewHeight();

        // Get scene bounds (shift to camera view mid point)
        double sceneX = xmin + viewW / 2;
        double sceneY = ymin + viewH / 2;
        double sceneW = xmax - xmin;
        double sceneH = ymax - ymin;

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
        for (int i = 0, iMax = paths.size(); i < iMax; i++) {

            // Paint path and path layers
            Path3D child = paths.get(i);
            paintPath3D(aPntr, child);
            if (child.getLayers().size() > 0)
                for (Path3D layer : child.getLayers())
                    paintPath3D(aPntr, layer);
        }

        // Translate back
        aPntr.translate(-viewMidX, -viewMidY);
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
