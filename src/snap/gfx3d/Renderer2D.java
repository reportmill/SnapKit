package snap.gfx3d;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This Renderer subclass tries to render the Scene using the standard Painter (2D).
 */
public class Renderer2D extends Renderer {

    // Whether to sort surfaces
    private boolean  _sortSurfaces = true;

    // List of all Scene FacetShapes in view coords
    private List<FacetShape>  _surfacesInViewCoords;

    // Bounds of shapes in scene
    private Rect  _sceneBounds2D;

    // Constant for name
    private static final String RENDERER_NAME = "Vector 2D";

    /**
     * Constructor.
     */
    public Renderer2D(Camera aCamera)
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
     * Returns a list of all Scene FacetShapes in view coords.
     */
    public List<FacetShape> getFacetShapesInViewCoords()
    {
        // If already set, just return
        if (_surfacesInViewCoords != null) return _surfacesInViewCoords;

        // Get FacetShape, set and return
        List<FacetShape> facetShapes = getFacetShapesInViewCoordsImpl();
        return _surfacesInViewCoords = facetShapes;
    }

    /**
     * Returns a list of all Scene FacetShape in view coords.
     */
    protected List<FacetShape> getFacetShapesInViewCoordsImpl()
    {
        // Get surfaces in camera coords
        List<FacetShape> facetShapesInCameraCoords = getFacetShapesInCameraCoords();

        // Sort FacetShapes
        if (isSortSurfaces())
            BinarySpaceTree.sortShapesBackToFront(facetShapesInCameraCoords);

        // Add in FacetShape painter paths
        addFacetShapePainterPathsInCameraCoords(facetShapesInCameraCoords);

        // Get facet shapes in view coords
        Camera camera = getCamera();
        Matrix3D cameraToView = camera.getCameraToView();
        List<FacetShape> facetsInViewCoords = ListUtils.map(facetShapesInCameraCoords, fs -> fs.copyForMatrix(cameraToView));

        // Return FacetShapes in view coords
        return facetsInViewCoords;
    }

    /**
     * Returns a list of all Scene FacetShapes in camera coords.
     */
    protected List<FacetShape> getFacetShapesInCameraCoords()
    {
        // Get scene and add surfaces deep
        Scene3D scene = getScene();
        List<FacetShape> facetShapeList = new ArrayList<>();
        addFacetShapesInCameraCoords(scene, facetShapeList);

        // Return
        return facetShapeList;
    }

    /**
     * Adds the paths for shape.
     */
    protected void addFacetShapesInCameraCoords(Shape3D aShape, List<FacetShape> facetShapeList)
    {
        // If shape not visible, just return
        if (!aShape.isVisible())
            return;

        // Handle ParentShape: Get children and recurse
        if (aShape instanceof ParentShape) {
            ParentShape parentShape = (ParentShape) aShape;
            Shape3D[] children = parentShape.getChildren();
            for (Shape3D child : children)
                addFacetShapesInCameraCoords(child, facetShapeList);
        }

        // Handle FacetShape
        else if (aShape instanceof FacetShape) {
            FacetShape facetShape = (FacetShape) aShape;
            addFacetShapeInCameraCoords(facetShape, facetShapeList);
        }

        // Handle VertexArrayShape
        else if (aShape instanceof VertexArrayShape) {
            VertexArrayShape vertexArrayShape = (VertexArrayShape) aShape;
            VertexArray triangleArray = vertexArrayShape.getTriangleArray();
            float[] pointsArray = triangleArray.getPointArray();
            float[] colorsArray = triangleArray.isColorArraySet() ? triangleArray.getColorArray() : null;
            Color color = triangleArray.getColor();

            int pointCount = triangleArray.getPointCount();
            int triangleCount = pointCount / 3;
            for (int triangleIndex = 0, i = 0; triangleIndex < triangleCount; triangleIndex++) {
                float p1x = pointsArray[i], c1x = colorsArray != null ? colorsArray[i++] : (float) color.getRed();
                float p1y = pointsArray[i], c1y = colorsArray != null ? colorsArray[i++] : (float) color.getGreen();
                float p1z = pointsArray[i], c1z = colorsArray != null ? colorsArray[i++] : (float) color.getBlue();
                float p2x = pointsArray[i], c2x = colorsArray != null ? colorsArray[i++] : (float) color.getRed();
                float p2y = pointsArray[i], c2y = colorsArray != null ? colorsArray[i++] : (float) color.getGreen();
                float p2z = pointsArray[i], c2z = colorsArray != null ? colorsArray[i++] : (float) color.getBlue();
                float p3x = pointsArray[i], c3x = colorsArray != null ? colorsArray[i++] : (float) color.getRed();
                float p3y = pointsArray[i], c3y = colorsArray != null ? colorsArray[i++] : (float) color.getGreen();
                float p3z = pointsArray[i], c3z = colorsArray != null ? colorsArray[i++] : (float) color.getBlue();
                Polygon3D polygon3D = new Polygon3D();
                polygon3D.addPoint(p1x, p1y, p1z);
                polygon3D.addPoint(p2x, p2y, p2z);
                polygon3D.addPoint(p3x, p3y, p3z);
                float red = (c1x + c2x + c3x) / 3;
                float green = (c1y + c2y + c3y) / 3;
                float blue = (c1z + c2z + c3z) / 3;
                polygon3D.setColor(new Color(red, green, blue));
                polygon3D.setDoubleSided(true);
                addFacetShapeInCameraCoords(polygon3D, facetShapeList);
            }
        }
    }

    /**
     * Adds given FacetShape in camera space if visible.
     */
    protected void addFacetShapeInCameraCoords(FacetShape facetShape, List<FacetShape> facetShapeList)
    {
        // Get facet normal (if bogus, complain and return - not sure this happens anymore)
        Vector3D facetNormal = facetShape.getNormal();
        if (Double.isNaN(facetNormal.x)) {
            System.err.println("Renderer2D.addShapeSurfacesInCameraCoords: Invalid facet normal");
            return;
        }

        // Get facet normal in camera coords
        Matrix3D sceneToCamera = _camera.getSceneToCamera();
        Vector3D facetNormalInCamera = sceneToCamera.transformVector(facetNormal);
        facetNormalInCamera.normalize();

        // Get camera-to-facet vector in camera coords
        Point3D facetCenter = facetShape.getBoundsCenter();
        Point3D facetCenterInCamera = sceneToCamera.transformPoint(facetCenter.clone());
        Vector3D cameraToFacetVector = new Vector3D(facetCenterInCamera.x, facetCenterInCamera.y, facetCenterInCamera.z);

        // Backface culling : If facet pointed away from camera, skip facet
        boolean shapeFacingAway = cameraToFacetVector.isAligned(facetNormalInCamera, false);
        if (shapeFacingAway) {
            if (!facetShape.isDoubleSided())
                return;
        }

        // Get facetShape in camera space (reverse if shape was facing away but double-sided)
        FacetShape facetShapeInCamera = facetShape.copyForMatrix(sceneToCamera);
        if (shapeFacingAway) {
            facetShapeInCamera.reverse();
            facetNormalInCamera.negate();
        }

        // If color on shape, set color on facet for scene lights
        Light3D light = _scene.getLight();
        Color color = facetShape.getColor();
        if (color != null) {
            Color rcol = light.getRenderColor(facetNormalInCamera, color);
            facetShapeInCamera.setColor(rcol);
        }

        // Add facetShape
        facetShapeList.add(facetShapeInCamera);
    }

    /**
     * Iterates over FacetShape list and adds FacetShape.Painter.PainterTasks as Path3Ds in Camera coords.
     */
    private void addFacetShapePainterPathsInCameraCoords(List<FacetShape> facetShapeList)
    {
        // Iterate over paths
        for (int i = facetShapeList.size() - 1; i >= 0; i--) {

            // Get facetShape (just skip if no Painter)
            FacetShape facetShape = facetShapeList.get(i);
            if (facetShape.getPainter() == null)
                continue;

            // Get Paths for painter
            Matrix3D sceneToCamera = _camera.getSceneToCamera();
            Matrix3D cameraToScene = sceneToCamera.clone().invert();
            FacetShape facetShapeInScene = facetShape.copyForMatrix(cameraToScene);
            Path3D[] painterPathsInCamera = getFacetShapePainterPathsInCameraCoords(facetShapeInScene);

            // Add paths to list right behind the original path
            for (int j = 0; j < painterPathsInCamera.length; j++) {
                Path3D painterPath = painterPathsInCamera[j];
                facetShapeList.add(i + j + 1, painterPath);
            }
        }
    }

    /**
     * Returns the FacetShape.Painter.PainterPaths as Path3D in Camera space.
     */
    private Path3D[] getFacetShapePainterPathsInCameraCoords(FacetShape aFacetShape)
    {
        // Get Painter, PainterTasks and pathPainterPaths array
        Painter3D painter3D = aFacetShape.getPainter();
        Painter3D.PaintTask[] paintTasks = painter3D.getPaintTasks();
        Path3D[] painterPaths = new Path3D[paintTasks.length];

        // Get transform from painter to camera
        Matrix3D painterToShape = aFacetShape.getPainterToLocal();
        Matrix3D shapeToCamera = _camera.getSceneToCamera();

        // Get transform from painter to view
        for (int i = 0; i < paintTasks.length; i++) {
            Painter3D.PaintTask paintTask = paintTasks[i];
            Path3D paintTaskPath = new Path3D(paintTask.getShape(), 0);
            paintTaskPath.transform(painterToShape);
            paintTaskPath.transform(shapeToCamera);
            paintTaskPath.setStroke(paintTask.getColor(), paintTask.getStroke().getWidth());
            painterPaths[i] = paintTaskPath;
        }

        // Return
        return painterPaths;
    }

    /**
     * Returns the bounding rect of scene in view coords.
     */
    @Override
    public Rect getSceneBoundsInView()
    {
        // If already set, just return
        if (_sceneBounds2D != null) return _sceneBounds2D;

        // If empty view, just return
        double viewW = _camera.getViewWidth();
        double viewH = _camera.getViewHeight();
        if (viewW < 1 || viewH < 1)
            return Rect.ZeroRect;
        if (getScene().getChildCount() == 0)
            return Rect.ZeroRect;

        // Get Scene to View transform
        Matrix3D sceneToView = _camera.getSceneToView();

        // Get Scene bounds in View
        Scene3D scene = getScene();
        Bounds3D sceneBounds = scene.getBounds3D();
        Bounds3D sceneBoundsInView = sceneBounds.copyForMatrix(sceneToView);

        // Get scene bounds (shift to camera view mid point)
        double sceneX = sceneBoundsInView.getMinX();
        double sceneY = sceneBoundsInView.getMinY();
        double sceneW = sceneBoundsInView.getWidth();
        double sceneH = sceneBoundsInView.getHeight();

        // Create, set, return bounds rect
        return _sceneBounds2D = new Rect(sceneX, sceneY, sceneW, sceneH);
    }

    /**
     * Called to indicate that FacetShapes list needs to be rebuilt.
     */
    protected void rebuildFacetShapes()
    {
        _surfacesInViewCoords = null;
        _sceneBounds2D = null;
    }

    /**
     * Override to rebuild FacetShapes for camera changes.
     */
    @Override
    protected void cameraDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        switch (propName) {
            case Camera.ViewWidth_Prop:
            case Camera.ViewHeight_Prop:
            case Camera.Yaw_Prop:
            case Camera.Pitch_Prop:
            case Camera.Roll_Prop:
            case Camera.FocalLength_Prop:
            case Camera.PrefGimbalRadius_Prop:
                rebuildFacetShapes();
        }
        super.cameraDidPropChange(aPC);
    }

    /**
     * Called when scene changes.
     */
    protected void sceneDidChange()
    {
        rebuildFacetShapes();
    }

    /**
     * Renders scene for given painter, camera and scene.
     */
    public void renderAndPaint(Painter aPntr)
    {
        paintFacetShapes(aPntr);
    }

    /**
     * Paints shape children.
     */
    public void paintFacetShapes(Painter aPntr)
    {
        // Iterate over FacetShapes and paint
        List<FacetShape> facetShapeList = getFacetShapesInViewCoords();
        for (FacetShape facetShape : facetShapeList)
            paintFacetShape(aPntr, facetShape);
    }

    /**
     * Paints a Path3D.
     */
    protected void paintFacetShape(Painter aPntr, FacetShape aFacetShape)
    {
        // Get path, fill and stroke
        Shape path = aFacetShape.getShape2D();
        Paint fill = aFacetShape.getColor();
        Paint stroke = aFacetShape.getStrokeColor();

        // Get opacity and set if needed
        double op = aFacetShape.getOpacity(), oldOP = 0;
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
            aPntr.setStroke(aFacetShape.getStroke());
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
        public Renderer newRenderer(Camera aCamera)
        {
            return new Renderer2D(aCamera);
        }
    }
}
