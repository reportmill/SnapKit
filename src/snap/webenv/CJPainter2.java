package snap.webenv;
import snap.gfx.*;
import snap.util.SnapEnv;
import snap.webapi.*;
import java.util.Arrays;

/**
 * A snap Painter for rendering to a CheerpJ HTMLCanvasElement.
 */
public class CJPainter2 extends PainterDVR2 {

    // The RenderContext2D
    protected CanvasRenderingContext2D _cntx;

    /**
     * Constructor for given canvas.
     */
    public CJPainter2(HTMLCanvasElement aCnvs, int aScale)
    {
        super(new CJPainter(aCnvs, aScale));
    }

    /**
     * Returns the real painter.
     */
    public CJPainter getPainter()  { return (CJPainter) _pntr; }

    /**
     * Override to have CJPainter paint stacks.
     */
    @Override
    public void flush()
    {
        CJPainter painter = (CJPainter) _pntr;
        _cntx = painter._cntx;

        // Convert Native stack objects to JS (where applicable)
        for (int i = 0; i < _nativeStackSize; i++)
            _nativeStack[i] = toNative(_nativeStack[i]);

        // If JXBrowser, make arrays exactly the right size
        if (SnapEnv.isJxBrowser) {
            int[] instructionStack = Arrays.copyOf(_instructionStack, _instructionStackSize);
            int[] intStack = Arrays.copyOf(_intStack, _intStackSize);
            double[] doubleStack = Arrays.copyOf(_doubleStack, _doubleStackSize);
            String[] stringStack = Arrays.copyOf(_stringStack, _stringStackSize);
            Object[] nativeStack = Arrays.copyOf(_nativeStack, _nativeStackSize);
            painter.paintStacks(instructionStack, _instructionStackSize, intStack, doubleStack, stringStack, nativeStack);
        }

        // Otherwise just send through Paint stacks
        else painter.paintStacks(_instructionStack, _instructionStackSize, _intStack, _doubleStack, _stringStack, _nativeStack);

        // Clear painter
        clear(); _cntx = null;
    }

    /**
     * Converts objects in native stack to JavaScript friendly object.
     */
    private Object toNative(Object anObj)
    {
        // Handle Color: Convert to color string
        if (anObj instanceof Color)
            return CJ.getColorJS((Color) anObj);

        // Handle texture, gradient: Convert to canvas versions
        if (anObj instanceof Paint) {
            if (anObj instanceof ImagePaint)
                return CJ.getTextureJS((ImagePaint) anObj, _cntx).getJS();
            if (anObj instanceof GradientPaint)
                return CJ.getGradientJS((GradientPaint) anObj, _cntx).getJS();
            return CJ.getColorJS(((Paint) anObj).getColor());
        }

        // Handle Font: Convert to font string
        if (anObj instanceof Font)
            return CJ.getFontJS((Font) anObj);

        // Handle image: Convert to Native.JS
        if (anObj instanceof Image) {
            CanvasImageSource imgSrc = (CanvasImageSource) ((Image) anObj).getNative();
            return ((HTMLElement) imgSrc).getJS();
        }

        return anObj;
    }
}