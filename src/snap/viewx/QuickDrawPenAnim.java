/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Point;
import snap.gfx.Color;
import snap.util.MathUtils;

import java.util.Arrays;

/**
 * This pen subclass animates a pen.
 */
class QuickDrawPenAnim extends QuickDrawPen {

    // The real pen
    private QuickDrawPen  _realPen;

    // The array of anim instructions
    private int[]  _instructions = new int[0];

    // The array of anim instruction args
    private Object[]  _instrArgs = new Object[0];

    // The array of anim instruction times
    private int[]  _instrTimes = new int[0];

    // The current start instruction index
    private int  _instrStart;

    // The current end instruction index
    private int  _instrEnd;

    // The last anim time
    protected long  _lastAnimTime;

    // The cumulative elapsed time for current (continuing) instruction
    private long  _instrElapsedTime;

    // Last X/Y when adding instructions
    private double  _lastX, _lastY, _lastMoveX, _lastMoveY;

    // Last Direction when adding instructions
    private double  _lastDir;

    // Constants for instructions
    private static final int Color_Id = 1;
    private static final int Width_Id = 2;
    private static final int Direction_Id = 3;
    private static final int MoveTo_Id = 4;
    private static final int LineTo_Id = 5;
    private static final int Close_Id = 6;
    private static final int Forward_Id = 7;
    private static final int Turn_Id = 8;

    /**
     * Constructor.
     */
    public QuickDrawPenAnim(QuickDraw aDrawView, QuickDrawPen aPen)
    {
        super(aDrawView);
        _realPen = aPen;
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void setColor(Color aColor)
    {
        addInstruction(Color_Id, aColor, 0);
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void setWidth(double aValue)
    {
        addInstruction(Width_Id, aValue, 0);
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void setDirection(double theDegrees)
    {
        addInstruction(Direction_Id, theDegrees, 0);
        _lastDir = theDegrees;
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void moveTo(double aX, double aY)
    {
        addInstruction(MoveTo_Id, new Object[] { aX, aY }, 0);
        _lastX = _lastMoveX = aX;
        _lastY = _lastMoveY = aY;
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void lineTo(double aX, double aY)
    {
        // Calculate time
        double lineX = aX - _lastX;
        double lineY = aY - _lastY;
        double length = Math.sqrt(lineX * lineX + lineY * lineY);
        int time = (int) Math.round(length * 1000 / 200);

        // Add instruction and update LastX/Y
        addInstruction(LineTo_Id, new Object[] { aX, aY }, time);
        _lastX = aX;
        _lastY = aY;
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void closePath()
    {
        // Calculate time
        double lineX = _lastMoveX - _lastX;
        double lineY = _lastMoveY - _lastY;
        double length = Math.sqrt(lineX * lineX + lineY * lineY);
        int time = (int) Math.round(length * 1000 / 200);

        // Add instruction and update LastX/Y
        addInstruction(Close_Id, new Object[] { _lastMoveX, _lastMoveY }, time);
        _lastX = _lastMoveX = lineX;
        _lastY = _lastMoveY = lineY;
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void forward(double aLength)
    {
        int time = (int) Math.round(aLength * 1000 / 200);
        addInstruction(Forward_Id, aLength, time);

        // Calculate next point using Direction and length
        _lastX = _lastX + aLength * Math.cos(Math.toRadians(_lastDir));
        _lastY = _lastY + aLength * Math.sin(Math.toRadians(_lastDir));
    }

    /**
     * Override to add instruction.
     */
    @Override
    public void turn(double anAngle)
    {
        addInstruction(Turn_Id, anAngle, 0);
        _lastDir += anAngle;
    }

    /**
     * Sets whether pen is animating.
     */
    protected void setAnimating(boolean aValue)
    {
        if (aValue == isAnimating()) return;
        super.setAnimating(aValue);

        // Handle Animating: Add to DrawView.AnimPens
        if (aValue) {
            _drawView.addAnimPen(this);
            _lastAnimTime = System.currentTimeMillis();
        }

        // Handle Animating false: Remove from DrawView.AnimPens
        else _drawView.removeAnimPen(this);
    }

    /**
     * Adds an instruction.
     */
    private void addInstruction(int anId, Object theArgs, int aTime)
    {
        // Extend Instructions/Args arrays if needed
        if (_instrEnd + 1 >= _instructions.length) {
            _instructions = Arrays.copyOf(_instructions, Math.max(_instructions.length * 2, 16));
            _instrArgs = Arrays.copyOf(_instrArgs, _instructions.length);
            _instrTimes = Arrays.copyOf(_instrTimes, _instructions.length);
        }

        // Add Instruction and Args to arrays
        _instructions[_instrEnd] = anId;
        _instrArgs[_instrEnd] = theArgs;
        _instrTimes[_instrEnd] = aTime;
        _instrEnd++;

        // Turn on
        setAnimating(true);
    }

    /**
     * Process instruction.
     */
    protected void processInstructions()
    {
        // Get CurrentTime, ElapsedTime
        long currTime = System.currentTimeMillis();
        long elapsedTime = currTime - _lastAnimTime;

        // Process available instructions within ElapsedTime
        int procTime = 0;
        while (_instrStart < _instrEnd && procTime < elapsedTime) {
            int instrTime = processInstruction(elapsedTime);
            procTime += instrTime;
        }

        // Update LastTime
        _lastAnimTime = currTime;

        // If done, stop animating
        int instrCount = _instrEnd - _instrStart;
        if (instrCount == 0) {
            _instrStart = _instrEnd = 0;
            setAnimating(false);
        }
    }

    /**
     * Process next instruction.
     */
    private int processInstruction(long elapsedTime)
    {
        // Get next instruction Id, Args and Time
        int instrId = _instructions[_instrStart];
        Object args = _instrArgs[_instrStart];
        int instrTime = _instrTimes[_instrStart];

        // Handle instruction id
        switch (instrId) {

            // Handle Color
            case Color_Id:
                _realPen.setColor((Color) args);
                _instrStart++;
                break;

            // Handle Width
            case Width_Id:
                _realPen.setWidth((Double) args);
                _instrStart++;
                break;

            // Handle MoveTo
            case MoveTo_Id: {
                Object[] argsArray = (Object[]) args;
                double moveX = (Double) argsArray[0];
                double moveY = (Double) argsArray[1];
                _realPen.moveTo(moveX, moveY);
                _instrStart++;
                break;
            }

            // Handle LineTo
            case Close_Id:
            case LineTo_Id: {

                // If instruction is continued from previous processing, remove last path seg
                PenPath penPath = _realPen.getPenPath();
                if (_instrElapsedTime > 0) {
                    penPath.removeLastSeg();
                    elapsedTime += _instrElapsedTime;
                }
                _instrElapsedTime = elapsedTime;

                // Get previous point
                Point lastPoint = penPath.getLastPoint();
                if (lastPoint == null) {
                    penPath.moveTo(0, 0);
                    lastPoint = penPath.getLastPoint();
                }

                // Get adjusted length for ElapsedTime
                double timeRatio = Math.min(elapsedTime / (double) instrTime, 1);

                // Get LineTo args
                Object[] argsArray = (Object[]) args;
                double lineToX = (Double) argsArray[0];
                double lineToY = (Double) argsArray[1];

                // Get next X/Y and do lineTo
                double nextX = MathUtils.mapFractionalToRangeValue(timeRatio, lastPoint.x, lineToX);
                double nextY = MathUtils.mapFractionalToRangeValue(timeRatio, lastPoint.y, lineToY);
                _realPen.lineTo(nextX, nextY);

                // If completed, increment InstrStart and reset Continued
                if (timeRatio >= 1) {
                    _instrStart++;
                    _instrElapsedTime = 0;
                    if (instrId == Close_Id) {
                        penPath.removeLastSeg();
                        penPath.close();
                    }
                }
                break;
            }

            // Handle Turn
            case Turn_Id: {
                _realPen.turn((Double) args);
                _instrStart++;
                break;
            }

            // Handle Forward
            case Forward_Id: {

                // If instruction is continued from previous processing, remove last path seg
                if (_instrElapsedTime > 0) {
                    PenPath penPath = _realPen.getPenPath();
                    penPath.removeLastSeg();
                    elapsedTime += _instrElapsedTime;
                }
                _instrElapsedTime = elapsedTime;

                // Get adjusted length for ElapsedTime
                double length = (Double) args;
                double timeRatio = Math.min(elapsedTime / (double) instrTime, 1);
                double adjustedLength = length * timeRatio;

                // Add new forward
                _realPen.forward(adjustedLength);

                // If completed, increment InstrStart and reset Continued
                if (timeRatio >= 1) {
                    _instrStart++;
                    _instrElapsedTime = 0;
                }
                break;
            }
        }

        // Return
        return instrTime;
    }
}
