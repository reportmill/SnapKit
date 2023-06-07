package snap.gfx3d;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Image;
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
            boolean textureSet = triangleArray.isTextureSetAndReady();;
            Image textureImage = textureSet ? triangleArray.getTexture().getImage() : null;
            float[] textureCoords = textureSet ? triangleArray.getTexCoordArray() : null;
            int textImageW = textureImage != null ? textureImage.getPixWidth() - 1 : 0;
            int textImageH = textureImage != null ? textureImage.getPixHeight() - 1 : 0;

            // Get indexes
            int[] indexArray = triangleArray.getIndexArray();
            if (!triangleArray.isIndexArraySet()) {
                int pointCount = triangleArray.getPointCount();
                indexArray = new int[pointCount];
                for (int i = 0; i < pointCount; i++)
                    indexArray[i] = i;
            }

            // Get point count, triangle count (increment to avoid painting more than 3000 mesh triangles)
            int pointCount = indexArray.length;
            int triangleCount = pointCount / 3;
            int increment = Math.max(triangleCount / 3000, 1);

            // Iterate over triangles and add polygon 3d for each
            for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex += increment) {

                // Get indexes
                int indexIndex = triangleIndex * 3;
                int index1 = indexArray[indexIndex];
                int index2 = indexArray[indexIndex + 1];
                int index3 = indexArray[indexIndex + 2];

                // Get triangle points
                int pointIndex1 = index1 * 3;
                int pointIndex2 = index2 * 3;
                int pointIndex3 = index3 * 3;
                float p1x = pointsArray[pointIndex1];
                float p1y = pointsArray[pointIndex1 + 1];
                float p1z = pointsArray[pointIndex1 + 2];
                float p2x = pointsArray[pointIndex2];
                float p2y = pointsArray[pointIndex2 + 1];
                float p2z = pointsArray[pointIndex2 + 2];
                float p3x = pointsArray[pointIndex3];
                float p3y = pointsArray[pointIndex3 + 1];
                float p3z = pointsArray[pointIndex3 + 2];

                // Create polygon 3D
                Polygon3D polygon3D = new Polygon3D();
                polygon3D.addPoint(p1x, p1y, p1z);
                polygon3D.addPoint(p2x, p2y, p2z);
                polygon3D.addPoint(p3x, p3y, p3z);

                // Get colors
                float c1x, c1y, c1z;
                float c2x, c2y, c2z;
                float c3x, c3y, c3z;
                if (colorsArray != null) {
                    c1x = colorsArray[pointIndex1];
                    c1y = colorsArray[pointIndex1 + 1];
                    c1z = colorsArray[pointIndex1 + 2];
                    c2x = colorsArray[pointIndex2];
                    c2y = colorsArray[pointIndex2 + 1];
                    c2z = colorsArray[pointIndex2 + 2];
                    c3x = colorsArray[pointIndex3];
                    c3y = colorsArray[pointIndex3 + 1];
                    c3z = colorsArray[pointIndex3 + 2];
                }
                else if (textureSet) {
                    int texIndex1 = index1 * 2;
                    int texIndex2 = index2 * 2;
                    int texIndex3 = index3 * 2;
                    int t1x = Math.round(textureCoords[texIndex1] * textImageW);
                    int t1y = Math.round(textureCoords[texIndex1 + 1] * textImageH);
                    int c1 = textureImage.getRGB(t1x, t1y);
                    c1x = (c1 >> 16 & 0xff) / 255f;
                    c1y = (c1 >> 8 & 0Xff) / 255f;
                    c1z = (c1 & 0xff) / 255f;
                    int t2x = Math.round(textureCoords[texIndex2] * textImageW);
                    int t2y = Math.round(textureCoords[texIndex2 + 1] * textImageH);
                    int c2 = textureImage.getRGB(t2x, t2y);
                    c2x = (c2 >> 16 & 0xff) / 255f;
                    c2y = (c2 >> 8 & 0Xff) / 255f;
                    c2z = (c2 & 0xff) / 255f;
                    int t3x = Math.round(textureCoords[texIndex3] * textImageW);
                    int t3y = Math.round(textureCoords[texIndex3 + 1] * textImageH);
                    int c3 = textureImage.getRGB(t3x, t3y);
                    c3x = (c3 >> 16 & 0xff) / 255f;
                    c3y = (c3 >> 8 & 0Xff) / 255f;
                    c3z = (c3 & 0xff) / 255f;
                }
                else {
                    c1x = (float) color.getRed();
                    c1y = (float) color.getGreen();
                    c1z = (float) color.getBlue();
                    c2x = (float) color.getRed();
                    c2y = (float) color.getGreen();
                    c2z = (float) color.getBlue();
                    c3x = (float) color.getRed();
                    c3y = (float) color.getGreen();
                    c3z = (float) color.getBlue();
                }

                // Get color components
                float red = (c1x + c2x + c3x) / 3;
                float green = (c1y + c2y + c3y) / 3;
                float blue = (c1z + c2z + c3z) / 3;

                // Set color
                polygon3D.setColor(new Color(red, green, blue));
                polygon3D.setDoubleSided(true);

                // Add shape in camera coords
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
        if (facetShapeInCamera == null)
            return; // AxisBoxShape
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
     * A default implementation.
     */
    public static class Renderer2DFactory extends RendererFactory {

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
