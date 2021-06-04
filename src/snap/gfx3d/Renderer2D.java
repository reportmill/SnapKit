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
    private List <Path3D>  _paths = new ArrayList();

    // Bounds of paths in scene
    private Rect  _sceneBounds;

    // Whether paths list needs to be rebuilt
    private boolean  _rebuildPaths;

    // The Painter
    private Painter  _pntr;

    /**
     * Constructor.
     */
    public Renderer2D(Camera aCamera)
    {
        super(aCamera);
    }

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
            Point3D[] bb2 = path.getBBox();
            xmin = Math.min(xmin, bb2[0].x);
            ymin = Math.min(ymin, bb2[0].y);
            xmax = Math.max(xmax, bb2[1].x);
            ymax = Math.max(ymax, bb2[1].y);
        }
        return _sceneBounds = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    /**
     * Returns the number of Path3Ds in the display list.
     */
    public int getPathCount()  { return getPaths().size(); }

    /**
     * Returns the specific Path3D at the given index from the display list.
     */
    public Path3D getPath(int anIndex)  { return getPaths().get(anIndex); }

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
        // Adjust Z
        if (_camera.isAdjustZ())
            _camera.adjustZ();

        // Remove all existing Path3Ds
        removePaths();
        _sceneBounds = null;

        // Iterate over shapes and add paths
        List <Shape3D> shapes = _scene._shapes;
        for (Shape3D shp : shapes)
            addPathsForShape(shp);

        // Resort paths
        Path3D.sortPaths(_paths);
        _rebuildPaths = false;
    }

    /**
     * Adds the paths for shape.
     */
    protected void addPathsForShape(Shape3D aShape)
    {
        // Get the camera transform & optionally align it to the screen
        Transform3D xform = _camera.getTransform();
        Light light = _scene.getLight();
        Color color = aShape.getColor();

        // Iterate over paths
        for (Path3D path3d : aShape.getPath3Ds()) {

            // Get path copy transformed by scene transform
            path3d = path3d.copyFor(xform);

            // Backface culling : Only add paths that face the camera
            if (_camera.isFacingAway(path3d.getNormal()))
                continue;

            // If color on shape, set color on path for scene lights
            if (color!=null) {
                Color rcol = light.getRenderColor(_camera, path3d, color);
                path3d.setColor(rcol);
            }

            // Add path
            addPath(path3d);
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
            case Camera.Width_Prop:
            case Camera.Height_Prop:
            case Camera.Depth_Prop:
            case Camera.Yaw_Prop:
            case Camera.Pitch_Prop:
            case Camera.Roll_Prop:
            case Camera.FocalLength_Prop:
            case Camera.OffsetZ_Prop:
            case Camera.AdjustZ_Prop:
            case Camera.Pseudo3D_Prop:
            case Camera.PseudoSkewX_Prop:
            case Camera.PseudoSkewY_Prop:
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
    public void renderAll(Painter aPainter)
    {
        _pntr = aPainter;

        paintPaths();
    }

    /**
     * Paints shape children.
     */
    public void paintPaths()
    {
        // Iterate over Path3Ds and paint
        List<Path3D> paths = getPaths();
        for (int i=0, iMax=paths.size(); i<iMax; i++) { Path3D child = paths.get(i);

            // Paint path and path layers
            paintPath3D(child);
            if (child.getLayers().size()>0)
                for (Path3D layer : child.getLayers())
                    paintPath3D(layer);
        }
    }

    /**
     * Paints a Path3D.
     */
    protected void paintPath3D(Path3D aPath3D)
    {
        // Get path, fill and stroke
        Shape path = aPath3D.getPath();
        Paint fill = aPath3D.getColor(), stroke = aPath3D.getStrokeColor();

        // Get opacity and set if needed
        double op = aPath3D.getOpacity(), oldOP = 0;
        if (op<1) {
            oldOP = _pntr.getOpacity();
            _pntr.setOpacity(op*oldOP);
        }

        // Do fill and stroke
        if (fill!=null) {
            _pntr.setPaint(fill);
            _pntr.fill(path);
        }
        if (stroke!=null) {
            _pntr.setPaint(stroke);
            _pntr.setStroke(aPath3D.getStroke());
            _pntr.draw(path);
        }

        // Reset opacity if needed
        if (op<1)
            _pntr.setOpacity(oldOP);
    }

    /** Paints a Path3D with labels on sides. */
    /*private void paintPath3DDebug(Painter aPntr, Path3D aPath3D, String aStr) {
        aPntr.setOpacity(.8); paintPath3D(aPntr, aPath3D); aPntr.setOpacity(1);
        Font font = Font.Arial14.getBold(); double asc = font.getAscent(); aPntr.setFont(font);
        Rect r = font.getStringBounds(aStr), r2 = aPath3D.getPath().getBounds();
        aPntr.drawString(aStr, r2.x + (r2.width - r.width)/2, r2.y + (r2.height - r.height)/2 + asc);
    }*/
}
