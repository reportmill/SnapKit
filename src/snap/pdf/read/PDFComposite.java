package snap.pdf.read;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.color.ColorSpace;

/**
 * Represents a PDF composite.
 */
public class PDFComposite {

    /** Blend modes */
    public static final int NormalBlendMode = 0;
    public static final int MultiplyBlendMode = 1;
    public static final int ScreenBlendMode = 2;
    public static final int OverlayBlendMode = 3;
    public static final int DarkenBlendMode = 4;
    public static final int LightenBlendMode = 5;
    public static final int ColorDodgeBlendMode = 6;
    public static final int ColorBurnBlendMode = 7;
    public static final int HardLightBlendMode = 8;
    public static final int SoftLightBlendMode = 9;
    public static final int DifferenceBlendMode = 10;
    public static final int ExclusionBlendMode = 11;
    public static final int HueBlendMode = 12;
    public static final int SaturationBlendMode = 13;
    public static final int ColorBlendMode = 14;
    public static final int LuminosityBlendMode = 15;

public static Composite createComposite(ColorSpace sourcespace, int blendMode, boolean alphaIsShape, float alpha)
{
    // TODO: implement blend modes
    switch (blendMode) {
        case NormalBlendMode:
        case MultiplyBlendMode:
        case ScreenBlendMode:
        case OverlayBlendMode:
        case DarkenBlendMode:
        case LightenBlendMode:
        case ColorDodgeBlendMode:
        case ColorBurnBlendMode:
        case HardLightBlendMode:
        case SoftLightBlendMode:
        case DifferenceBlendMode:
        case ExclusionBlendMode:
        case HueBlendMode:
        case SaturationBlendMode:
        case ColorBlendMode:
        case LuminosityBlendMode: return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }
    return null;
}

}