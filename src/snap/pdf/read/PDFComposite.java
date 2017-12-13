package snap.pdf.read;
import snap.gfx.Painter;
import snap.pdf.PDFException;

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

/**
 * Returns the composite mode for given blend mode.
 */
public static int getBlendModeID(String pdfName)
{
    if(pdfName.equals("/Normal") || pdfName.equals("/Compatible")) return NormalBlendMode;
    if(pdfName.equals("/Multiply")) return MultiplyBlendMode;
    if(pdfName.equals("/Screen")) return ScreenBlendMode;
    if(pdfName.equals("/Overlay")) return OverlayBlendMode;
    if(pdfName.equals("/Darken")) return DarkenBlendMode;
    if(pdfName.equals("/Lighten")) return LightenBlendMode;
    if(pdfName.equals("/ColorDodge")) return ColorDodgeBlendMode;
    if(pdfName.equals("/ColorBurn")) return ColorBurnBlendMode;
    if(pdfName.equals("/HardLight")) return HardLightBlendMode;
    if(pdfName.equals("/SoftLight")) return SoftLightBlendMode;
    if(pdfName.equals("/Difference")) return DifferenceBlendMode;
    if(pdfName.equals("/Exclusion")) return ExclusionBlendMode;
    if(pdfName.equals("/Hue")) return HueBlendMode;
    if(pdfName.equals("/Saturation")) return SaturationBlendMode;
    if(pdfName.equals("/Color")) return ColorBlendMode;
    if(pdfName.equals("/Luminosity")) return LuminosityBlendMode;
    throw new PDFException("Unknown blend mode name \""+pdfName+"\"");
}

public static Painter.Composite getComposite(int blendMode)
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
        case LuminosityBlendMode: return Painter.Composite.SRC_OVER;
    }
    return null;
}

}