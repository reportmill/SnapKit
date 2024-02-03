/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.ArrayUtils;
import snap.view.View;
import snap.view.ViewEvent;

import java.util.Arrays;

/**
 * This class supports convenient drawing methods for quick and convenient vector graphics drawing.
 */
public class QuickDraw extends View {

    // Whether to draw the grid
    private boolean  _showGrid;

    // Whether to draw the grid
    private double  _gridSpacing = DEFAULT_GRID_SPACING;

    // Whether to animate drawing (by returning pens that animate).
    private boolean  _animate = true;

    // The pens
    private QuickDrawPen[]  _pens = new QuickDrawPen[1];

    // Array of current animating pens
    private QuickDrawPenAnim[]  _animPens = new QuickDrawPenAnim[0];

    // A run to trigger anim pen updates
    private Runnable  _repaintRun = () -> updateAnimPens();

    // The last mouse coords
    private int  _mouseX, _mouseY;

    // Constants
    private static final Color GRID_COLOR = Color.GRAY9;
    private static final Stroke GRID_STROKE = Stroke.Stroke1;
    private static final Color COORDS_COLOR = Color.GRAY6;
    private static final Font COORDS_FONT = Font.Arial11;
    private static final double DEFAULT_GRID_SPACING = 50;
    private static final Color MOUSE_COORDS_COLOR = Color.GRAY4;
    private static final Font MOUSE_COORDS_FONT = Font.Arial12;

    /**
     * Constructor.
     */
    public QuickDraw(int aWidth, int aHeight)
    {
        setPrefSize(aWidth, aHeight);
        setBorder(Color.GRAY9, 2);
        setBorderRadius(4);
        setFill(Color.WHITE);
        setShowGrid(true);
        enableEvents(MouseMove, MouseExit);
    }

    /**
     * Returns whether to draw coordinate grid.
     */
    public boolean isShowGrid()  { return _showGrid; }

    /**
     * Sets whether to draw coordinate grid.
     */
    public void setShowGrid(boolean aValue)
    {
        _showGrid = aValue;
        repaint();
    }

    /**
     * Returns the space between gridlines in points.
     */
    public double getGridSpacing()  { return _gridSpacing; }

    /**
     * Sets the space between gridlines in points.
     */
    public void setGridSpacing(double aValue)
    {
        _gridSpacing = aValue;
    }

    /**
     * Returns whether to animate drawing (by returning pens that animate).
     */
    public boolean isAnimate()  { return _animate; }

    /**
     * Sets whether to animate drawing (by returning pens that animate).
     */
    public void setAnimate(boolean aValue)
    {
        _animate = aValue;
    }

    /**
     * Returns the main pen.
     */
    public QuickDrawPen getPen()
    {
        return getPen(0);
    }

    /**
     * Returns the pen at given index.
     */
    public QuickDrawPen getPen(int anIndex)
    {
        // Constrain number of pens to 100
        int index = Math.abs(anIndex) % 100;
        if (_pens.length <= index)
            _pens = Arrays.copyOf(_pens, index + 1);

        // Get pen - create if missing
        QuickDrawPen pen = _pens[index];
        if (pen == null)
            pen = _pens[index] = new QuickDrawPen(this);

        // If animate is set, get anim pen
        if (isAnimate())
            pen = pen.getAnimPen();

        // Return
        return pen;
    }

    /**
     * Sets the current pen color.
     */
    public void setPenColor(Color aColor)
    {
        QuickDrawPen pen = getPen();
        pen.setColor(aColor);
    }

    /**
     * Sets the current pen width.
     */
    public void setPenWidth(double aWidth)
    {
        QuickDrawPen pen = getPen();
        pen.setWidth(aWidth);
    }

    /**
     * Moves the current path point to given point.
     */
    public void moveTo(double aX, double aY)
    {
        QuickDrawPen pen = getPen();
        pen.moveTo(aX, aY);
    }

    /**
     * Adds a line segment to current draw path from last path point to given point.
     */
    public void lineTo(double aX, double aY)
    {
        QuickDrawPen pen = getPen();
        pen.lineTo(aX, aY);
    }

    /**
     * Adds a line segment to current draw path from last path point to last moveTo point.
     */
    public void closePath()
    {
        QuickDrawPen pen = getPen();
        pen.closePath();
    }

    /**
     * Moves the default pen forward by given length for current Direction.
     */
    public void forward(double aLength)
    {
        QuickDrawPen pen = getPen();
        pen.forward(aLength);
    }

    /**
     * Sets the path drawing direction to the current direction plus given angle.
     */
    public void turn(double anAngle)
    {
        QuickDrawPen pen = getPen();
        pen.turn(anAngle);
    }

    /**
     * Override to paint pen paths.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // Paint grid
        if (isShowGrid())
            paintGrid(aPntr);

        // Clip
        aPntr.clipRect(0, 0, getWidth(), getHeight());

        // Paint Pens
        for (QuickDrawPen pen : _pens)
            if (pen != null)
                pen.paintPaths(aPntr);
    }

    /**
     * Draws the coordinate system.
     */
    private void paintGrid(Painter aPntr)
    {
        // Set grid color/stroke
        aPntr.setPaint(GRID_COLOR);
        aPntr.setStroke(GRID_STROKE);

        // Draw grid lines
        double viewW = getWidth();
        double viewH = getHeight();
        double gridSpacing = getGridSpacing();
        for (double lineX = gridSpacing; lineX <= viewW; lineX += gridSpacing)
            aPntr.drawLine(lineX, 0, lineX, viewH);
        for (double lineY = gridSpacing; lineY <= viewH; lineY += gridSpacing)
            aPntr.drawLine(0, lineY, viewW, lineY);

        // Set text color/font
        aPntr.setPaint(COORDS_COLOR);
        aPntr.setFont(COORDS_FONT);
        double coordsStrH = Math.ceil(COORDS_FONT.getAscent());

        // Draw (width,0) and (0,height) coord labels
        String widthCoordStr = "( " + (int) viewW + ", 0 )";
        double widthCoordStrW = COORDS_FONT.getStringAdvance(widthCoordStr);
        aPntr.drawString(widthCoordStr, viewW - widthCoordStrW - 4, coordsStrH + 4);
        String heightCoordStr = "( 0, " + (int) viewH + " )";
        aPntr.drawString(heightCoordStr, 4, viewH - 6);

        // Set mouse coords color
        aPntr.setPaint(MOUSE_COORDS_COLOR);
        aPntr.setFont(MOUSE_COORDS_FONT);

        // Draw mouse coords label
        if (_mouseX > 0 && _mouseY > 0 && _mouseX < viewW && _mouseY < viewH) {
            String mouseCoordsStr = "( " + _mouseX + ", " + _mouseY + " )";
            double mouseCoordsStrH = Math.ceil(MOUSE_COORDS_FONT.getAscent());
            double mouseCoordsStrX = _mouseX + 10;
            double mouseCoordsStrY = _mouseY + mouseCoordsStrH + 10;
            aPntr.drawString(mouseCoordsStr, mouseCoordsStrX, mouseCoordsStrY);
        }
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if(anEvent.isMouseEvent() && isShowGrid()) {

            // Handle MouseMove
            if(anEvent.isMouseMove()) {
                Rect repaintRect = Rect.getRectForPoints(anEvent.getPoint(), new Point(_mouseX, _mouseY));
                repaintRect.inset(-80, -25);
                repaint(repaintRect);
            }
            _mouseX = (int) anEvent.getX();
            _mouseY = (int) anEvent.getY();

            // Handle MouseExit
            if (anEvent.isMouseExit()) {
                _mouseX = -1;
                _mouseY = -1;
                repaint(0, 0, getWidth() + 80, getHeight() + 30);
            }
        }
    }

    /**
     * Updates animations.
     */
    private void updateAnimPens()
    {
        QuickDrawPenAnim[] animPens = _animPens.clone();
        for (QuickDrawPenAnim pen : animPens)
            pen.processInstructions();
    }

    /**
     * Adds an anim pen.
     */
    protected void addAnimPen(QuickDrawPenAnim aPen)
    {
        // Add pen
        _animPens = ArrayUtils.add(_animPens, aPen);

        // If first pen, start animation
        if (_animPens.length == 1)
            getEnv().runIntervals(_repaintRun, 10);
    }

    /**
     * Removes an animating pen.
     */
    protected void removeAnimPen(QuickDrawPenAnim aPen)
    {
        // Remove pen
        _animPens = ArrayUtils.removeId(_animPens, aPen);

        // If no pens, stop animation
        if (_animPens.length == 0)
            getEnv().stopIntervals(_repaintRun);
    }

    /**
     * Creates and returns a default view with size 400 x 400.
     */
    public static QuickDraw createDrawView()
    {
        return createDrawView(400, 400);
    }

    /**
     * Creates and returns a view for given width and height.
     */
    public static QuickDraw createDrawView(int aWidth, int aHeight)
    {
        QuickDraw quickDraw = new QuickDraw(aWidth, aHeight);
        return quickDraw;
    }
}
