/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.view.StringView;
import snap.view.ViewUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * This ParentShape subclass displays an axis box.
 */
public class CubeShape extends ParentShape {

    // The Front/Back sides
    private Poly3D  _frontSide, _backSide;

    // The Left/Right sides
    private Poly3D  _leftSide, _rightSide;

    // The Top/Bottom sides
    private Poly3D  _topSide, _bottomSide;

    // An extra shape to act as shadow
    private Poly3D  _shadowShape;

    // Map cache of textures for sides
    private static Map<Side3D,Texture>  _textures = new HashMap<>();

    // Constants
    private static final Color SIDE_COLOR = Color.WHITE;
    private static final Color SIDE_BORDER_COLOR = Color.LIGHTGRAY.darker();
    private static final Color BOTTOM_COLOR = Color.LIGHTGRAY.brighter().brighter();
    private static final Color SHADOW_COLOR = Color.GRAY;
    private static final Color c1 = Color.WHITE, c2 =  new Color("#F8"), c3 =  new Color("#D8");
    private static final GradientPaint.Stop[] side_stops = GradientPaint.getStops(0, c1, .5, c2, 1, c3);
    private static final GradientPaint SIDE_PAINT = new GradientPaint(90, side_stops);

    /**
     * Constructor.
     */
    public CubeShape()
    {
        super();
        addSides();
        addSideTextures();
    }

    /**
     * Returns the side shape.
     */
    public Poly3D getSideShape(Side3D aSide)
    {
        switch (aSide) {
            case FRONT: return _frontSide;
            case BACK: return _backSide;
            case LEFT: return _leftSide;
            case RIGHT: return _rightSide;
            case TOP: return _topSide;
            case BOTTOM: return _bottomSide;
            default: return null;
        }
    }

    /**
     * Returns whether given side is visible.
     */
    public boolean isSideVisible(Side3D aSide)
    {
        switch (aSide) {
            case FRONT: return _frontSide.isVisible();
            case BACK: return _backSide.isVisible();
            case LEFT: return _leftSide.isVisible();
            case RIGHT: return _rightSide.isVisible();
            case BOTTOM: return _bottomSide.isVisible();
            case TOP: return !_bottomSide.isVisible();
            default: throw new RuntimeException("AxisBoxShape.isSideVisible: Unknown side: " + aSide);
        }
    }

    /**
     * Build sides.
     */
    private void addSides()
    {
        // Get preferred width, height, depth
        double width = 50;
        double height = 50;
        double depth = 50;

        // Add shape for front/back sides
        _frontSide = addSideFrontBack(width, height, depth);
        _backSide = addSideFrontBack(width, height, 0);

        // Add shape for left/right sides
        _leftSide = addSideLeftRight(0, height, depth);
        _rightSide = addSideLeftRight(width, height, depth);

        // Add shape for top/bottom sides
        _bottomSide = addSideTopBottom(width, 0, depth);
        _topSide = addSideTopBottom(width, height, depth);

        // Add Shadow shape
        _shadowShape = new Poly3D();
        _shadowShape.setName("CubeShadow");
        _shadowShape.setColor(SHADOW_COLOR);
        _shadowShape.setOpacity(.8f);
        double shadowY = - height * .1;
        double shadowMinX = width * .03, shadowMaxX = width - shadowMinX;
        double shadowMinZ = depth * .03, shadowMaxZ = depth - shadowMinZ;
        _shadowShape.addPoint(shadowMaxX, shadowY, shadowMinZ);
        _shadowShape.addPoint(shadowMinX, shadowY, shadowMinZ);
        _shadowShape.addPoint(shadowMinX, shadowY, shadowMaxZ);
        _shadowShape.addPoint(shadowMaxX, shadowY, shadowMaxZ);

        // Reset shapes
        setChildren(_frontSide, _backSide, _leftSide, _rightSide, _topSide, _bottomSide, _shadowShape);

        // Go ahead and set Bounds to avoid calculation
        Bounds3D bounds3D = new Bounds3D(0, 0, 0, width, height, depth);
        setBounds3D(bounds3D);
    }

    /**
     * Adds geometry for front/back sides.
     */
    private Poly3D addSideFrontBack(double width, double height, double sideZ)
    {
        // Create wall shape
        Poly3D side = new Poly3D();
        side.setName(sideZ == 0 ? "AxisBack" : "AxisFront");
        side.setOpacity(.8f);
        side.setColor(SIDE_COLOR);

        // Add side points
        if (sideZ != 0) {
            side.addPoint(0, 0, sideZ);
            side.addPoint(width, 0, sideZ);
            side.addPoint(width, height, sideZ);
            side.addPoint(0, height, sideZ);
        }
        else {
            side.addPoint(width, 0, sideZ);
            side.addPoint(0, 0, sideZ);
            side.addPoint(0, height, sideZ);
            side.addPoint(width, height, sideZ);
        }

        // Return
        return side;
    }

    /**
     * Add geometry for left/right sides.
     */
    private Poly3D addSideLeftRight(double sideX, double height, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideX == 0 ? "AxisLeft" : "AxisRight");
        side.setColor(SIDE_COLOR);
        side.setOpacity(.8f);

        // Add side points
        if (sideX != 0) {
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, height, 0);
            side.addPoint(sideX, height, depth);
        }
        else {
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, height, depth);
            side.addPoint(sideX, height, 0);
        }

        // Return
        return side;
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private Poly3D addSideTopBottom(double width, double sideY, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(sideY == 0 ? BOTTOM_COLOR : SIDE_COLOR);
        side.setOpacity(.8f);

        // Add side points
        if (sideY != 0) {
            side.addPoint(width, sideY, 0);
            side.addPoint(0, sideY, 0);
            side.addPoint(0, sideY, depth);
            side.addPoint(width, sideY, depth);
        }
        else {
            side.addPoint(width, sideY, depth);
            side.addPoint(0, sideY, depth);
            side.addPoint(0, sideY, 0);
            side.addPoint(width, sideY, 0);
        }

        // Return
        return side;
    }

    /**
     * Add side textures.
     */
    public void addSideTextures()
    {
        // Create and set texture for projected sides
        Side3D[] sides = Side3D.values();
        for (Side3D side : sides) {
            Texture texture = getTextureForSide(side);
            setTextureForSide(texture, side);
        }
    }

    /**
     * Returns the texture for a given side.
     */
    private Texture getTextureForSide(Side3D aSide)
    {
        // If texture already cached, just return
        Texture texture = _textures.get(aSide);
        if (texture != null)
            return texture;

        // Create, cache and return texture
        texture = createTextureForSide(aSide);
        _textures.put(aSide, texture);
        return texture;
    }

    /**
     * Generates the texture for a given side.
     */
    private Texture createTextureForSide(Side3D aSide)
    {
        // Create/configure texture
        StringView stringView = new StringView(aSide.name());
        stringView.setFont(Font.Arial14.deriveFont(28).getBold());
        if (aSide == Side3D.BOTTOM) stringView.setFont(Font.Arial14.deriveFont(24));
        stringView.setTextFill(Color.DARKGRAY);
        stringView.setAlign(Pos.CENTER);
        stringView.setFill(aSide == Side3D.TOP ? Color.WHITE : aSide == Side3D.BOTTOM ? BOTTOM_COLOR : SIDE_PAINT);
        stringView.setBorder(SIDE_BORDER_COLOR, 3);
        stringView.setSize(120, 120);

        // Get image
        Image image = ViewUtils.getImageForScale(stringView, 1);

        // Create texture and return
        Texture texture = new Texture(image);
        return texture;
    }

    /**
     * Sets the texture for a given side.
     */
    private void setTextureForSide(Texture aTexture, Side3D aSide)
    {
        Poly3D sideShape = getSideShape(aSide);
        sideShape.setTexture(aTexture);
        sideShape.addTexCoord(0, 0);
        sideShape.addTexCoord(1, 0);
        sideShape.addTexCoord(1, 1);
        sideShape.addTexCoord(0, 1);
    }
}
