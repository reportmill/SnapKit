/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.*;
import snap.util.SnapUtils;
import java.util.Arrays;

/**
 * A Painter subclass that records painting operations for later execution instead of executing them immediately.
 */
public class PainterDVR2 extends PainterImpl {

    // The instruction stack
    protected int[] _instructionStack = new int[100];

    // The instruction stack size
    protected int _instructionStackSize = 0;

    // The int stack
    protected int[] _intStack = new int[100];

    // The int stack size
    protected int _intStackSize = 0;

    // The double stack
    protected double[] _doubleStack = new double[100];

    // The double stack size
    protected int _doubleStackSize = 0;

    // The string stack
    protected String[] _stringStack = new String[100];

    // The string stack size
    protected int _stringStackSize = 0;

    // The native stack
    protected Object[] _nativeStack = new Object[100];

    // The native stack size
    protected int _nativeStackSize = 0;

    // The Painter (for EffectPntr)
    protected Painter  _pntr;

    // Constants for operations
    public static final int SET_FONT = 1;
    public static final int SET_PAINT = 2;
    public static final int SET_STROKE = 3;
    public static final int SET_OPACITY = 4;
    public static final int DRAW_SHAPE = 5;
    public static final int FILL_SHAPE = 6;
    public static final int CLIP_SHAPE = 7;
    public static final int DRAW_IMAGE = 8;
    public static final int DRAW_IMAGE2 = 9;
    public static final int DRAW_STRING = 10;
    public static final int STROKE_STRING = 11;
    public static final int TRANSFORM = 12;
    public static final int SET_TRANSFORM = 13;
    public static final int GSAVE = 14;
    public static final int GRESTORE = 15;
    public static final int CLEAR_RECT = 16;
    public static final int SET_IMAGE_QUALITY = 17;

    /**
     * Constructor.
     */
    public PainterDVR2()  { }

    /**
     * Constructor for given painter (which supports applyEffect()).
     */
    public PainterDVR2(Painter aPntr)
    {
        super();
        _pntr = aPntr;
    }

    /**
     * Adds an instruction.
     */
    public void addInstruction(int aValue)
    {
        if (_instructionStackSize == _instructionStack.length)
            _instructionStack = Arrays.copyOf(_instructionStack, _instructionStackSize * 2);
        _instructionStack[_instructionStackSize++] = aValue;
    }

    /**
     * Adds an int.
     */
    public void addInt(int aValue)
    {
        if (_intStackSize == _intStack.length)
            _intStack = Arrays.copyOf(_intStack, _intStackSize * 2);
        _intStack[_intStackSize++] = aValue;
    }

    /**
     * Adds a double.
     */
    public void addDouble(double aValue)
    {
        if (_doubleStackSize == _doubleStack.length)
            _doubleStack = Arrays.copyOf(_doubleStack, _doubleStackSize * 2);
        _doubleStack[_doubleStackSize++] = aValue;
    }

    /**
     * Adds a string.
     */
    public void addString(String aValue)
    {
        if (_stringStackSize == _stringStack.length)
            _stringStack = Arrays.copyOf(_stringStack, _stringStackSize * 2);
        _stringStack[_stringStackSize++] = aValue;
    }

    /**
     * Adds a double.
     */
    public void addNative(Object aValue)
    {
        if (_nativeStackSize == _nativeStack.length)
            _nativeStack = Arrays.copyOf(_nativeStack, _nativeStackSize * 2);
        _nativeStack[_nativeStackSize++] = aValue;
    }

    @Override
    public void flush()
    {
        new Executor(_pntr).exec();
    }

    /**
     * Executes the instructions stored in this PainterDVR to given painter.
     */
    public void exec(Painter aPntr)
    {
        _pntr = aPntr;
        new Executor(aPntr).exec();
    }

    /**
     * Clears the instructions stored in this PainterDVR.
     */
    public void clear()
    {
        _gfxStateStackSize = 0;
        _gfxState = new GState();
        _instructionStackSize = 0;
        _intStackSize = 0;
        _doubleStackSize = 0;
        _stringStackSize = 0;
        _nativeStackSize = 0;
    }

    /** Sets the font. */
    public void setFont(Font aFont)
    {
        super.setFont(aFont);
        addInstruction(SET_FONT);
        addNative(aFont);
    }

    /** Sets the paint. */
    public void setPaint(Paint aPaint)
    {
        super.setPaint(aPaint);
        addInstruction(SET_PAINT);
        addNative(aPaint);
    }

    /** Sets the stroke. */
    public void setStroke(Stroke aStroke)
    {
        // Let's be nice and map null to reasonable default
        if (aStroke == null) aStroke = Stroke.Stroke1;

        // Do normal version
        super.setStroke(aStroke);
        addInstruction(SET_STROKE);

        // Set LineWidth
        addDouble(aStroke.getWidth());

        // Set DashArray and DashOffset
        double[] dashArray = aStroke.getDashArray();
        int dashArrayLen = dashArray != null ? dashArray.length : 0;
        addInt(dashArrayLen);
        if (dashArrayLen > 0) {
            for (double dashArrayVal : dashArray)
                addDouble(dashArrayVal);

            // Set DashOffset
            addDouble(aStroke.getDashOffset());
        }

        // Set cap
        switch (aStroke.getCap()) {
            case Round: addInt(0); break;
            case Butt: addInt(1); break;
            case Square: addInt(2); break;
        }

        // Set join
        switch (aStroke.getJoin()) {
            case Round: addInt(0); break;
            case Bevel: addInt(1); break;
            case Miter:
                addInt(2);
                addDouble(aStroke.getMiterLimit());
                break;
        }
    }

    /** Sets the opacity. */
    public void setOpacity(double aValue)
    {
        super.setOpacity(aValue);
        addInstruction(SET_OPACITY);
        addDouble(aValue);
    }

    /** Stroke the given shape. */
    public void draw(Shape aShape)
    {
        // If gradient is set, resize to given shape
        if (getPaint() instanceof GradientPaint)
            sizeGradientPaintToShape(aShape);

        //super.draw(aShape);
        addInstruction(DRAW_SHAPE);
        addShape(aShape);
    }

    /** Fill the given shape. */
    public void fill(Shape aShape)
    {
        // If gradient is set, resize to given shape
        if (getPaint() instanceof GradientPaint)
            sizeGradientPaintToShape(aShape);

        //super.fill(aShape);
        addInstruction(FILL_SHAPE);
        addShape(aShape);
    }

    /**
     * Resets gradient paint to given shape.
     */
    private void sizeGradientPaintToShape(Shape aShape)
    {
        GradientPaint gradientPaint = (GradientPaint) getPaint();
        if (!gradientPaint.isAbsolute()) {
            GradientPaint gradientPaint2 = gradientPaint.copyForRect(aShape.getBounds());
            setPaint(gradientPaint2);
        }
    }

    /** Draw image with transform. */
    public void drawImage(Image image, Transform aTrans)
    {
        //super.drawImage(anImg,aTrans);
        addInstruction(DRAW_IMAGE2);
        addNative(image);
        addTransform(aTrans);
    }

    /** Draw image in rect. */
    public void drawImage(Image image, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        // Correct source width/height for image dpi
        if (SnapUtils.isWebVM) {
            double scaleX = image.getDpiX() / 72;
            double scaleY = image.getDpiY() / 72;
            if (scaleX != 1 || scaleY != 1) {
                sx *= scaleX; sy *= scaleX;
                sw *= scaleY; sh *= scaleY;
            }
        }

        //super.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
        addInstruction(DRAW_IMAGE);
        addNative(image);
        addDouble(sx); addDouble(sy);
        addDouble(sw); addDouble(sh);
        addDouble(dx); addDouble(dy);
        addDouble(dw); addDouble(dh);
    }

    /** Draw string at location with char spacing. */
    public void drawString(String aStr, double aX, double aY, double charSpacing)
    {
        //super.drawString(aStr, aX, aY, charSpacing);
        addInstruction(DRAW_STRING);
        addString(aStr);
        addDouble(aX);
        addDouble(aY);
        addDouble(charSpacing);
    }

    /** Stroke string at location with char spacing. */
    public void strokeString(String aStr, double aX, double aY, double charSpacing)
    {
        //super.strokeString(aStr, aX, aY, charSpacing);
        addInstruction(STROKE_STRING);
        addString(aStr);
        addDouble(aX);
        addDouble(aY);
        addDouble(charSpacing);
    }

    /**
     * Transform by transform.
     */
    public void setTransform(Transform aTrans)
    {
        super.setTransform(aTrans);
        addInstruction(SET_TRANSFORM);
        addTransform(aTrans);
    }

    /** Transform by transform. */
    public void transform(Transform aTrans)
    {
        super.transform(aTrans);
        addInstruction(TRANSFORM);
        addTransform(aTrans);
    }

    /** Clip by shape. */
    public void clip(Shape aShape)
    {
        super.clip(aShape);
        addInstruction(CLIP_SHAPE);
        addShape(aShape);
    }

    /** Standard clone implementation. */
    public void save()
    {
        super.save();
        addInstruction(GSAVE);
    }

    /** Disposes this painter. */
    public void restore()
    {
        super.restore();
        addInstruction(GRESTORE);
    }

    /**
     * Clears a rect.
     */
    public void clearRect(double aX, double aY, double aW, double aH)
    {
        addInstruction(CLEAR_RECT);
        addDouble(aX); addDouble(aY);
        addDouble(aW); addDouble(aH);
    }

    /** Sets image rendering quality. */
    @Override
    public void setImageQuality(double aValue)
    {
        addInstruction(SET_IMAGE_QUALITY);
        addDouble(aValue);
    }

    /** Override to forward to real painter. */
    public Props getProps()
    {
        return _pntr != null ? _pntr.getProps() : null;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        System.err.println("PainterDVR2: equals not implemented");
        return true;
    }

    /**
     * Adds a transform.
     */
    private void addTransform(Transform aTransform)
    {
        double[] xfm = aTransform.getMatrix();
        addDouble(xfm[0]); addDouble(xfm[1]);
        addDouble(xfm[2]); addDouble(xfm[3]);
        addDouble(xfm[4]); addDouble(xfm[5]);
    }

    /**
     * Adds a shape.
     */
    private void addShape(Shape aShape)
    {
        // If rect, handle special: Add special op count and bounds values
        if (aShape instanceof Rect) {
            Rect rect = (Rect) aShape;
            addInt(-1);
            addDouble(rect.getX()); addDouble(rect.getY());
            addDouble(rect.getWidth()); addDouble(rect.getHeight());
            return;
        }

        double[] pnts = new double[6];
        PathIter pathIter = aShape.getPathIter(null);
        int intOpCount = 0;
        int intStackIndex = _intStackSize;
        addInt(0);
        while (pathIter.hasNext()) {
            intOpCount++;
            switch (pathIter.getNext(pnts)) {
                case MoveTo: addInt(0); addDouble(pnts[0]); addDouble(pnts[1]); break;
                case LineTo: addInt(1); addDouble(pnts[0]); addDouble(pnts[1]); break;
                case CubicTo: addInt(2); addDouble(pnts[0]); addDouble(pnts[1]); addDouble(pnts[2]); addDouble(pnts[3]); addDouble(pnts[4]); addDouble(pnts[5]); break;
                case Close: addInt(3); break;
            }
        }
        _intStack[intStackIndex] = intOpCount;
    }

    /**
     * This class paints the instruction arrays.
     */
    private class Executor {

        // The painter
        private Painter _painter;

        // The stack indexes
        private int _instructionIndex = 0;
        private int _intIndex = 0;
        private int _doubleIndex = 0;
        private int _stringIndex = 0;
        private int _nativeIndex = 0;

        /**
         * Constructor.
         */
        public Executor(Painter aPntr)
        {
            super();
            _painter = aPntr;
        }

        /**
         * Execute.
         */
        public void exec()
        {
            // Iterate over instructions
            for (int i = 0; i < _instructionStackSize; i++) {

                // Get next instruction and handle
                switch (_instructionStack[i]) {
                    case SET_FONT: setFont(); break;
                    case SET_PAINT: setPaint(); break;
                    case SET_STROKE: setStroke(); break;
                    case SET_OPACITY: setOpacity(); break;
                    case DRAW_SHAPE: drawShape(); break;
                    case FILL_SHAPE: fillShape(); break;
                    case CLIP_SHAPE: clipShape(); break;
                    case DRAW_IMAGE: drawImage(); break;
                    case DRAW_IMAGE2: drawImage2(); break;
                    case DRAW_STRING: drawString(); break;
                    case STROKE_STRING: strokeString(); break;
                    case TRANSFORM: transform(); break;
                    case SET_TRANSFORM: setTransform(); break;
                    case GSAVE: save(); break;
                    case GRESTORE: restore(); break;
                    case SET_IMAGE_QUALITY: setImageQuality(); break;
                    default: System.out.println("Executor.exec: Unknown instruction"); break;
                }
            }
        }

        private void setFont()
        {
            Font font = (Font) getNative();
            _painter.setFont(font);
        }

        public void setPaint()
        {
            Paint paint = (Paint) getNative();
            _painter.setPaint(paint);
        }

        public void setStroke()
        {
            // Get line width
            double lineWidth = getDouble();

            // Get line dash
            int dashArrayLen = getInt();
            double[] dashArray = dashArrayLen > 0 ? new double[dashArrayLen] : null;
            for (int i = 0; i < dashArrayLen; i++)
                dashArray[i] = getDouble();

            // Get line dash offset
            double dashOffset = dashArrayLen > 0 ? getDouble() : 0;

            // Get line cap
            int capInt = getInt();
            Stroke.Cap cap = capInt == 0 ? Stroke.Cap.Round : capInt == 1 ? Stroke.Cap.Butt : Stroke.Cap.Square;

            // Get line join
            int joinInt = getInt();
            Stroke.Join join = joinInt == 0 ? Stroke.Join.Round : joinInt == 1 ? Stroke.Join.Bevel : Stroke.Join.Miter;
            double miterLimit = joinInt == 2 ? getDouble() : 0;

            // Create/set stroke
            Stroke stroke = new Stroke(lineWidth, cap, join, miterLimit, dashArray, dashOffset);
            _painter.setStroke(stroke);
        }

        public void setOpacity()
        {
            double opacity = getDouble();
            _painter.setOpacity(opacity);
        }

        public void drawShape()
        {
            Shape shape = getShape();
            _painter.draw(shape);
        }

        public void fillShape()
        {
            Shape shape = getShape();
            _painter.fill(shape);
        }

        public void clipShape()
        {
            Shape shape = getShape();
            _painter.clip(shape);
        }

        public void drawImage2()
        {
            Image image = (Image) getNative();
            Transform xfm = null;
            _painter.drawImage(image, xfm);
        }

        public void drawImage()
        {
            Image image = (Image) getNative();
            double sx = getDouble(), sy = getDouble();
            double sw = getDouble(), sh = getDouble();
            double dx = getDouble(), dy = getDouble();
            double dw = getDouble(), dh = getDouble();
            _painter.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
        }

        /** Draw string at location with char spacing. */
        public void drawString()
        {
            String str = getString();
            double x = getDouble();
            double y = getDouble();
            double cs = getDouble();
            _painter.drawString(str, x, y, cs);
        }

        /** Stroke string at location with char spacing. */
        public void strokeString()
        {
            String str = getString();
            double x = getDouble();
            double y = getDouble();
            double cs = getDouble();
            _painter.strokeString(str, x, y, cs);
        }

        /**
         * Transform by transform.
         */
        public void setTransform()
        {
            Transform transform = getTransform();
            _painter.setTransform(transform);
        }

        /** Transform by transform. */
        public void transform()
        {
            Transform transform = getTransform();
            _painter.transform(transform);
        }

        public void save()  { _painter.save(); }

        public void restore()  { _painter.restore(); }

        public void setImageQuality()
        {
            double quality = getDouble();
            _painter.setImageQuality(quality);
        }

        public Transform getTransform()
        {
            double[] matrix = new double[6];
            matrix[0] = getDouble(); matrix[1] = getDouble();
            matrix[2] = getDouble(); matrix[3] = getDouble();
            matrix[4] = getDouble(); matrix[5] = getDouble();
            return new Transform(matrix);
        }

        public Shape getShape()
        {
            int opCount = getInt();

            // Handle rect special case
            if (opCount == -1)
                return new Rect(getDouble(), getDouble(), getDouble(), getDouble());

            // Unpack path
            Path2D path2D = new Path2D();
            for (int i = 0; i < opCount; i++) {
                int op = getInt();
                switch (op) {
                    case 0: path2D.moveTo(getDouble(), getDouble()); break;
                    case 1: path2D.lineTo(getDouble(), getDouble()); break;
                    case 2: path2D.curveTo(getDouble(), getDouble(), getDouble(), getDouble(), getDouble(), getDouble()); break;
                    case 3: path2D.close(); break;
                }
            }

            // Return
            return path2D;
        }

        // Get stack values
        private int getInt()  { return _intStack[_intIndex++]; }
        private double getDouble()  { return _doubleStack[_doubleIndex++]; }
        private String getString()  { return _stringStack[_stringIndex++]; }
        private Object getNative()  { return _nativeStack[_nativeIndex++]; }
    }
}