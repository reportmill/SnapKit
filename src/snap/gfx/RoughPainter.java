/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A Painter subclass that renders in a hand-drawn sketch style, inspired by the JavaScript library Rough.js.
 *
 * <p>All shape-drawing operations (lines, rects, ovals, arbitrary paths) are rendered with a slightly
 * wobbly, imprecise look. Fills use configurable patterns: hachure (default), cross-hatch, dots,
 * dashed, zigzag, or solid.
 *
 * <p>RoughPainter wraps a real delegate Painter that performs the actual pixel rendering. The delegate
 * should generally be created fresh with identity transform (e.g., {@code image.getPainter()}).
 * RoughPainter manages all state (transform, clip, paint, stroke, font, opacity) itself via the
 * inherited PainterImpl machinery, and syncs the required subset of state to the delegate immediately
 * before each drawing call.
 *
 * <p>Usage:
 * <pre>
 *   Painter realPainter = image.getPainter();
 *   RoughPainter rough = new RoughPainter(realPainter);
 *   rough.setRoughness(1.5);
 *   rough.setFillStyle(RoughPainter.FillStyle.HACHURE);
 *
 *   rough.setColor(new Color(.8f, .9f, 1f));
 *   rough.fill(new Rect(50, 50, 200, 100));
 *   rough.setColor(Color.DARK_GRAY);
 *   rough.draw(new Rect(50, 50, 200, 100));
 * </pre>
 */
public class RoughPainter extends Painter {

    // Delegate painter for actual pixel rendering
    private final Painter _pntr;

    // Roughness level: 0 = smooth, 3+ = very rough
    private double _roughness = 1.5;

    // Bowing: how much straight lines bow outward relative to their length
    private double _bowing = 1.0;

    // Fill style for filled shapes
    private FillStyle _fillStyle = FillStyle.HACHURE;

    // Hachure line angle in degrees
    private double _hachureAngle = -41.0;

    // Gap between hachure lines (units); -1 = auto (4 × stroke width)
    private double _hachureGap = -1;

    // Weight of fill lines (units); -1 = auto (stroke width / 2)
    private double _fillWeight = -1;

    // Whether to skip the second slightly-offset stroke pass
    private boolean _disableMultiStroke = false;

    // Flatness for bezier curve flattening during intersection detection
    private static final double FLATNESS = 0.5;

    // Random source (seeded for reproducible results; reset per shape)
    private final Random _random = new Random(0);

    // -------- Fill Style Enum --------

    /**
     * Fill styles for filled shapes, mirroring Rough.js options.
     */
    public enum FillStyle {
        /** Parallel diagonal lines (default, like Rough.js). */
        HACHURE,
        /** Two perpendicular sets of parallel lines. */
        CROSS_HATCH,
        /** Solid fill painted directly with no texture. */
        SOLID,
        /** Grid of small dots. */
        DOTS,
        /** Dashed diagonal lines. */
        DASHED,
        /** Zigzag lines alternating between adjacent scan rows. */
        ZIGZAG
    }

    // -------- Constructor --------

    /**
     * Creates a RoughPainter that delegates actual pixel rendering to the given Painter.
     */
    public RoughPainter(Painter aPainter)
    {
        _pntr = aPainter;
    }

    // -------- Properties --------

    /** Returns the roughness level (0 = smooth, 3+ = very rough). */
    public double getRoughness()  { return _roughness; }

    /** Sets the roughness level. */
    public void setRoughness(double aValue)  { _roughness = aValue; }

    /** Returns the bowing factor applied to straight lines. */
    public double getBowing()  { return _bowing; }

    /** Sets the bowing factor. */
    public void setBowing(double aValue)  { _bowing = aValue; }

    /** Returns the fill style used for filled shapes. */
    public FillStyle getFillStyle()  { return _fillStyle; }

    /** Sets the fill style. */
    public void setFillStyle(FillStyle aStyle)  { _fillStyle = aStyle; }

    /** Returns the hachure line angle in degrees. */
    public double getHachureAngle()  { return _hachureAngle; }

    /** Sets the hachure line angle in degrees. */
    public void setHachureAngle(double angleDeg)  { _hachureAngle = angleDeg; }

    /** Returns the gap between hachure/fill lines, or -1 for auto. */
    public double getHachureGap()  { return _hachureGap; }

    /** Sets the gap between hachure/fill lines. Use -1 for auto. */
    public void setHachureGap(double aValue)  { _hachureGap = aValue; }

    /** Returns the fill line weight, or -1 for auto. */
    public double getFillWeight()  { return _fillWeight; }

    /** Sets the fill line weight. Use -1 for auto. */
    public void setFillWeight(double aValue)  { _fillWeight = aValue; }

    /** Returns whether the double-stroke outline effect is disabled. */
    public boolean isDisableMultiStroke()  { return _disableMultiStroke; }

    /** Sets whether to disable the double-stroke effect. */
    public void setDisableMultiStroke(boolean aValue)  { _disableMultiStroke = aValue; }

    // -------- PainterImpl Overrides: draw / fill --------

    @Override
    public Font getFont()  { return _pntr.getFont(); }

    @Override
    public void setFont(Font font)  { _pntr.setFont(font); }

    @Override
    public Paint getPaint()  { return _pntr.getPaint(); }

    @Override
    public void setPaint(Paint paint)  { _pntr.setPaint(paint); }

    @Override
    public Stroke getStroke()  { return _pntr.getStroke(); }

    @Override
    public void setStroke(Stroke s)  { _pntr.setStroke(s); }

    @Override
    public double getOpacity()  { return _pntr.getOpacity(); }

    @Override
    public void setOpacity(double aValue)  { _pntr.setOpacity(aValue); }

    /**
     * Draws a rough hand-drawn outline of the given shape.
     * Strokes it twice with slightly different wobble for a natural sketch look.
     */
    @Override
    public void draw(Shape aShape)
    {
        Shape rough1 = aShape instanceof Ellipse e ? createRoughEllipse(e) : createRoughOutline(aShape);
        _pntr.draw(rough1);
        if (!_disableMultiStroke) {
            Shape rough2 = aShape instanceof Ellipse e ? createRoughEllipse(e) : createRoughOutline(aShape);
            _pntr.draw(rough2);
        }
    }

    /**
     * Fills the given shape using the current fill style.
     */
    @Override
    public void fill(Shape aShape)
    {
        drawRoughFill(aShape);
    }

    /**
     * Draws an image — delegates directly to underlying painter with no roughness applied.
     */
    @Override
    public void drawImage(Image anImage, double sx, double sy, double sw, double sh,
                           double dx, double dy, double dw, double dh)
    {
        _pntr.drawImage(anImage, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    /**
     * Draws a string — delegates directly to underlying painter with no roughness applied.
     */
    @Override
    public void drawString(String aStr, double aX, double aY, double charSpacing)
    {
        _pntr.drawString(aStr, aX, aY, charSpacing);
    }

    /**
     * Strokes a string — delegates directly to underlying painter.
     */
    @Override
    public void strokeString(String aStr, double aX, double aY, double charSpacing)
    {
        _pntr.strokeString(aStr, aX, aY, charSpacing);
    }

    @Override
    public Transform getTransform()  { return _pntr.getTransform(); }

    @Override
    public void setTransform(Transform aTrans)  { _pntr.setTransform(aTrans);}

    @Override
    public void transform(Transform aTrans)  { _pntr.transform(aTrans); }

    @Override
    public Shape getClip()  { return _pntr.getClip(); }

    @Override
    public void clip(Shape s)  { _pntr.clip(s); }

    @Override
    public void save()  { _pntr.save();}

    @Override
    public void restore()  { _pntr.restore(); }

    // -------- Rough Outline Generation --------

    /**
     * Converts an arbitrary shape into a rough hand-drawn version by perturbing each path segment.
     * All coordinates are in local (painter) space.
     */
    private Shape createRoughOutline(Shape shape)
    {
        Path2D path = new Path2D();
        PathIter pi = shape.getPathIter(null);
        double[] c = new double[6];
        double lastX = 0, lastY = 0, moveX = 0, moveY = 0;

        while (pi.hasNext()) {
            Seg seg = pi.getNext(c);
            switch (seg) {
                case MoveTo -> {
                    moveX = lastX = c[0]; moveY = lastY = c[1];
                    path.moveTo(c[0] + roughOff(), c[1] + roughOff());
                }
                case LineTo -> {
                    appendRoughLine(path, lastX, lastY, c[0], c[1]);
                    lastX = c[0]; lastY = c[1];
                }
                case QuadTo -> {
                    double o = _roughness * 1.5;
                    path.quadTo(c[0] + roughOff() * o, c[1] + roughOff() * o,
                                c[2] + roughOff(), c[3] + roughOff());
                    lastX = c[2]; lastY = c[3];
                }
                case CubicTo -> {
                    double o = _roughness * 1.5;
                    path.curveTo(c[0] + roughOff() * o, c[1] + roughOff() * o,
                                 c[2] + roughOff() * o, c[3] + roughOff() * o,
                                 c[4] + roughOff(), c[5] + roughOff());
                    lastX = c[4]; lastY = c[5];
                }
                case Close -> {
                    appendRoughLine(path, lastX, lastY, moveX, moveY);
                    path.close();
                    lastX = moveX; lastY = moveY;
                }
            }
        }
        return path;
    }

    /**
     * Creates a rough hand-drawn ellipse using Catmull-Rom interpolation through
     * randomly perturbed points around the ellipse boundary.
     * Traces slightly more than 360° so the start and end naturally overlap.
     */
    private Shape createRoughEllipse(Ellipse ellipse)
    {
        double rx = ellipse.getWidth() / 2.0;
        double ry = ellipse.getHeight() / 2.0;
        double cx = ellipse.getX() + rx;
        double cy = ellipse.getY() + ry;
        double maxR = Math.max(rx, ry);

        int numPts = Math.max(10, (int) (Math.PI * Math.sqrt((rx * rx + ry * ry) / 2.0) / 6));
        double overlapAngle = Math.PI * 2 * 1.05; // 5% overlap for natural sketch join

        double[] ptX = new double[numPts];
        double[] ptY = new double[numPts];
        for (int i = 0; i < numPts; i++) {
            double angle = (i / (double) numPts) * overlapAngle;
            ptX[i] = cx + rx * Math.cos(angle) + roughOff() * _roughness * maxR * 0.05;
            ptY[i] = cy + ry * Math.sin(angle) + roughOff() * _roughness * maxR * 0.05;
        }
        return catmullRomToPath(ptX, ptY, true);
    }

    /**
     * Appends a rough line from (x1,y1) to (x2,y2) to the path using a bowed quadratic bezier,
     * based on the Rough.js line algorithm. The path's current position should be near (x1, y1).
     */
    private void appendRoughLine(Path2D path, double x1, double y1, double x2, double y2)
    {
        double lenSq = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        double len = Math.sqrt(lenSq);

        // Reduce roughness for long lines so they don't stray too far
        double gain = len < 200 ? 1.0 : len > 500 ? 0.4 : -0.0016668 * len + 1.233334;
        double offset = _roughness * gain * 2;
        if (offset * offset * 100 > lenSq)
            offset = len / 10;

        // Perpendicular mid-point bowing (like Rough.js)
        double bowMag = _bowing * _roughness * 2;
        double bowX = bowMag != 0 ? (bowMag * (y2 - y1) / 200) * (0.5 + _random.nextDouble()) : 0;
        double bowY = bowMag != 0 ? (bowMag * (x1 - x2) / 200) * (0.5 + _random.nextDouble()) : 0;

        // Rough end point and bowed midpoint
        double sx2 = x2 + randomNeg() * offset;
        double sy2 = y2 + randomNeg() * offset;
        double midX = (x1 + sx2) / 2 + bowX;
        double midY = (y1 + sy2) / 2 + bowY;

        path.quadTo(midX, midY, sx2, sy2);
    }

    // -------- Fill Methods --------

    /**
     * Dispatches to the appropriate fill algorithm based on the current fill style.
     */
    private void drawRoughFill(Shape shape)
    {
        // Always paint a solid background first so the fill color shows correctly.
        // Without this, gaps between texture lines expose whatever is behind the shape.
        if (_fillStyle != RoughPainter.FillStyle.SOLID) {
            _pntr.fill(shape);
        }

        switch (_fillStyle) {
            case HACHURE     -> drawHachureFill(shape, _hachureAngle, false);
            case CROSS_HATCH -> {
                drawHachureFill(shape, _hachureAngle, false);
                drawHachureFill(shape, _hachureAngle + 90, false);
            }
            case DASHED      -> drawHachureFill(shape, _hachureAngle, true);
            case DOTS        -> drawDotFill(shape);
            case ZIGZAG      -> drawZigzagFill(shape);
            case SOLID       -> { _pntr.fill(shape); }
        }
    }

    /**
     * Fills the shape with parallel diagonal lines (hachure or dashed hachure).
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Rotate shape by {@code -angleDeg} so hachure lines become horizontal.</li>
     *   <li>Flatten the rotated shape boundary to line segments.</li>
     *   <li>Walk horizontal scan lines at gap intervals across the bounding box.</li>
     *   <li>For each scan line, collect x-intersections and pair them.</li>
     *   <li>Draw each segment pair as a rough line in local coordinates.</li>
     * </ol>
     */
    private void drawHachureFill(Shape shape, double angleDeg, boolean dashed)
    {
        double strokeW = getStroke().getWidth();
        double gap = _hachureGap > 0 ? _hachureGap : strokeW * 4;
        double fillW = _fillWeight > 0 ? _fillWeight : Math.max(0.5, strokeW / 2.0);

        // Rotate shape so hachure lines become horizontal
        Transform toFlat = Transform.getRotate(-angleDeg);
        Transform toLocal = Transform.getRotate(angleDeg);
        Shape rotShape = shape.copyFor(toFlat);
        Rect bounds = rotShape.getBounds();

        // Flatten the rotated shape boundary for fast scan-line intersection
        List<double[]> flatSegs = flattenShape(rotShape);

        double startY = bounds.y - gap;
        double endY   = bounds.y + bounds.height + gap;

        // Collect all fill line segments in local coordinates
        List<double[]> fillSegs = new ArrayList<>();
        for (double y = startY; y <= endY; y += gap) {
            List<Double> xs = getXIntersections(flatSegs, y);
            Collections.sort(xs);
            for (int i = 0; i + 1 < xs.size(); i += 2) {
                // Points in rotated space
                Point p1 = toLocal.transformXY(xs.get(i), y);
                Point p2 = toLocal.transformXY(xs.get(i + 1), y);
                fillSegs.add(new double[]{p1.x, p1.y, p2.x, p2.y});
            }
        }

        // Set fill stroke on delegate and draw each segment as a rough line
        Stroke fillStroke = dashed
            ? Stroke.Stroke1.copyForWidth(fillW).copyForCap(Stroke.Cap.Round)
                            .copyForDashes(gap * 0.4, gap * 0.6)
            : Stroke.getStrokeRound(fillW);
        _pntr.setStroke(fillStroke);

        for (double[] seg : fillSegs) {
            Path2D linePath = new Path2D();
            linePath.moveTo(seg[0] + roughOff(), seg[1] + roughOff());
            appendRoughLine(linePath, seg[0], seg[1], seg[2], seg[3]);
            _pntr.draw(linePath);
        }
    }

    /**
     * Fills the shape with a grid of small dots, each jittered slightly for a hand-drawn feel.
     */
    private void drawDotFill(Shape shape)
    {
        double strokeW = getStroke().getWidth();
        double gap = _hachureGap > 0 ? _hachureGap : strokeW * 4;
        double dotR = _fillWeight > 0 ? _fillWeight / 2.0 : Math.max(0.5, strokeW / 2.0);
        Rect bounds = shape.getBounds();

        _pntr.setStroke(Stroke.getStroke(dotR));

        double y = bounds.y + gap / 2;
        while (y < bounds.y + bounds.height) {
            double x = bounds.x + gap / 2;
            while (x < bounds.x + bounds.width) {
                double jx = x + randomNeg() * gap * 0.3;
                double jy = y + randomNeg() * gap * 0.3;
                if (shape.contains(jx, jy))
                    _pntr.fill(new Ellipse(jx - dotR, jy - dotR, dotR * 2, dotR * 2));
                x += gap;
            }
            y += gap;
        }
    }

    /**
     * Fills the shape with zigzag lines that alternate between two adjacent scan rows.
     */
    private void drawZigzagFill(Shape shape)
    {
        double strokeW = getStroke().getWidth();
        double gap = _hachureGap > 0 ? _hachureGap : strokeW * 4;
        double fillW = _fillWeight > 0 ? _fillWeight : Math.max(0.5, strokeW / 2.0);

        Transform toFlat = Transform.getRotate(-_hachureAngle);
        Transform toLocal = Transform.getRotate(_hachureAngle);
        Shape rotShape = shape.copyFor(toFlat);
        Rect bounds = rotShape.getBounds();
        List<double[]> flatSegs = flattenShape(rotShape);

        double startY = bounds.y - gap;
        double endY   = bounds.y + bounds.height + gap;

        _pntr.setStroke(Stroke.getStrokeRound(fillW));

        for (double y = startY; y <= endY; y += gap) {
            List<Double> xs1 = getXIntersections(flatSegs, y);
            List<Double> xs2 = getXIntersections(flatSegs, y + gap / 2);
            Collections.sort(xs1);
            Collections.sort(xs2);
            int pairs = Math.min(xs1.size(), xs2.size()) / 2 * 2;
            for (int i = 0; i + 1 < pairs; i += 2) {
                Point a = toLocal.transformXY(xs1.get(i), y);
                Point b = toLocal.transformXY(xs2.get(i), y + gap / 2);
                Point d = toLocal.transformXY(xs1.get(i + 1), y);
                Path2D zPath = new Path2D();
                zPath.moveTo(a.x, a.y);
                zPath.lineTo(b.x, b.y);
                zPath.lineTo(d.x, d.y);
                _pntr.draw(zPath);
            }
        }
    }

    // -------- Bezier Flattening --------

    /**
     * Flattens all bezier segments of a shape to an array of line segments {x1,y1,x2,y2}.
     * Uses adaptive subdivision to stay within FLATNESS tolerance.
     */
    private List<double[]> flattenShape(Shape shape)
    {
        List<double[]> segs = new ArrayList<>();
        PathIter pi = shape.getPathIter(null);
        double[] c = new double[6];
        double lastX = 0, lastY = 0, moveX = 0, moveY = 0;

        while (pi.hasNext()) {
            Seg seg = pi.getNext(c);
            switch (seg) {
                case MoveTo -> { moveX = lastX = c[0]; moveY = lastY = c[1]; }
                case LineTo -> {
                    segs.add(new double[]{lastX, lastY, c[0], c[1]});
                    lastX = c[0]; lastY = c[1];
                }
                case QuadTo -> {
                    flattenQuad(segs, lastX, lastY, c[0], c[1], c[2], c[3]);
                    lastX = c[2]; lastY = c[3];
                }
                case CubicTo -> {
                    flattenCubic(segs, lastX, lastY, c[0], c[1], c[2], c[3], c[4], c[5]);
                    lastX = c[4]; lastY = c[5];
                }
                case Close -> {
                    if (lastX != moveX || lastY != moveY)
                        segs.add(new double[]{lastX, lastY, moveX, moveY});
                    lastX = moveX; lastY = moveY;
                }
            }
        }
        return segs;
    }

    /**
     * Recursively flattens a quadratic bezier to line segments via de Casteljau subdivision.
     */
    private void flattenQuad(List<double[]> segs,
                              double x0, double y0, double cx, double cy, double x1, double y1)
    {
        // Deviation: distance from midpoint of chord to midpoint of curve
        double mx = (x0 + 2 * cx + x1) / 4;
        double my = (y0 + 2 * cy + y1) / 4;
        double lmx = (x0 + x1) / 2, lmy = (y0 + y1) / 2;
        double dev = Math.abs(mx - lmx) + Math.abs(my - lmy);

        if (dev <= FLATNESS) {
            segs.add(new double[]{x0, y0, x1, y1});
        } else {
            double cx0 = (x0 + cx) / 2, cy0 = (y0 + cy) / 2;
            double cx1 = (cx + x1) / 2, cy1 = (cy + y1) / 2;
            double xm  = (cx0 + cx1) / 2, ym = (cy0 + cy1) / 2;
            flattenQuad(segs, x0, y0, cx0, cy0, xm, ym);
            flattenQuad(segs, xm, ym, cx1, cy1, x1, y1);
        }
    }

    /**
     * Recursively flattens a cubic bezier to line segments via de Casteljau subdivision.
     */
    private void flattenCubic(List<double[]> segs,
                               double x0, double y0,
                               double c1x, double c1y, double c2x, double c2y,
                               double x1, double y1)
    {
        // Deviation using control point distances
        double dev = Math.abs(c1x - x0) + Math.abs(c1y - y0)
                   + Math.abs(c2x - x1) + Math.abs(c2y - y1);

        if (dev <= FLATNESS * 4) {
            segs.add(new double[]{x0, y0, x1, y1});
        } else {
            // de Casteljau at t = 0.5
            double m1x = (x0 + c1x) / 2,  m1y = (y0 + c1y) / 2;
            double m2x = (c1x + c2x) / 2, m2y = (c1y + c2y) / 2;
            double m3x = (c2x + x1) / 2,  m3y = (c2y + y1) / 2;
            double m4x = (m1x + m2x) / 2, m4y = (m1y + m2y) / 2;
            double m5x = (m2x + m3x) / 2, m5y = (m2y + m3y) / 2;
            double m6x = (m4x + m5x) / 2, m6y = (m4y + m5y) / 2;
            flattenCubic(segs, x0, y0, m1x, m1y, m4x, m4y, m6x, m6y);
            flattenCubic(segs, m6x, m6y, m5x, m5y, m3x, m3y, x1, y1);
        }
    }

    // -------- Scan-Line Intersection --------

    /**
     * Returns the x-coordinates where the horizontal line {@code y = scanY} crosses
     * the boundary of the flattened shape. Uses a half-open interval [y1, y2) on each
     * segment to avoid double-counting shared vertices.
     */
    private List<Double> getXIntersections(List<double[]> segments, double scanY)
    {
        List<Double> xs = new ArrayList<>();
        for (double[] seg : segments) {
            double x1 = seg[0], y1 = seg[1], x2 = seg[2], y2 = seg[3];
            if ((y1 <= scanY && y2 > scanY) || (y2 <= scanY && y1 > scanY)) {
                double t = (scanY - y1) / (y2 - y1);
                xs.add(x1 + t * (x2 - x1));
            }
        }
        return xs;
    }

    // -------- Catmull-Rom Spline --------

    /**
     * Builds a Path2D from a Catmull-Rom spline through the given points.
     * Each segment is converted to an equivalent cubic bezier curve.
     */
    private Path2D catmullRomToPath(double[] ptX, double[] ptY, boolean closed)
    {
        int n = ptX.length;
        Path2D path = new Path2D();
        if (n == 0) return path;

        path.moveTo(ptX[0], ptY[0]);
        double tension = 0.5;

        for (int i = 0; i < n - 1; i++) {
            int p0 = Math.max(0, i - 1);
            int p1 = i;
            int p2 = i + 1;
            int p3 = Math.min(n - 1, i + 2);

            double cp1x = ptX[p1] + (ptX[p2] - ptX[p0]) * tension / 3;
            double cp1y = ptY[p1] + (ptY[p2] - ptY[p0]) * tension / 3;
            double cp2x = ptX[p2] - (ptX[p3] - ptX[p1]) * tension / 3;
            double cp2y = ptY[p2] - (ptY[p3] - ptY[p1]) * tension / 3;

            path.curveTo(cp1x, cp1y, cp2x, cp2y, ptX[p2], ptY[p2]);
        }

        if (closed) path.close();
        return path;
    }

    // -------- Utility --------

    /** Returns a signed random value in (-1, 1). */
    private double randomNeg()  { return _random.nextDouble() * 2 - 1; }

    /** Returns a small random offset scaled by roughness. */
    private double roughOff()  { return randomNeg() * _roughness * 0.5; }

    /** Returns the delegate painter. */
    public Painter getPainter()  { return _pntr; }
}
