/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Color;
import java.awt.Composite;
import java.awt.color.*;

/**
 * ColorFactories are responsible for creating java.awt.Color objects from any of the pdf color spaces.
 */
public interface ColorFactory {

/** Colorspace constants */
public static final int DeviceGrayColorspace = 1;
public static final int CalibratedGrayColorspace = 2;
public static final int DeviceRGBColorspace = 3;
public static final int CalibratedRGBColorspace = 4;
public static final int DeviceCMYKColorspace = 5;
public static final int LabColorspace = 6;
public static final int IndexedColorspace = 7;
public static final int ICCBasedColorspace = 8;
public static final int SeparationColorspace = 9;
public static final int DeviceNColorspace = 10;
public static final int PatternColorspace = 11;
public static final int UnknownColorspace = -1;

/** Rendering intents */
public static final int AbsoluteColorimetricIntent = 0;
public static final int RelativeColorimetricIntent = 1;
public static final int SaturationIntent = 2;
public static final int PerceptualIntent = 3;

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

/** Create a colorspace object from one of the above space IDs.
 *
 * The value of "params" can be as follows:
 *   Device spaces - ignored
 *   CIE spaces -  a Map
 *   ICC spaces -  a PDF Stream
 *   Indexed spaces - a Map with keys 'base", "hival", and "lookup"
 *   Pattern - null
 *   Separation - a Map with "Colorant", "Base", & "TintTransform"
 *   DeviceN - - a Map with "Colorants", "Base", "TintTransform", & "Attributes"
 */
public ColorSpace createColorSpace(int type, Object params);

/** Create a specific color in the colorspace */
public Color createColor(ColorSpace space, float values[]);

/** Create a Composite for the blend mode and alpha parameters.
 * The destination onto which things will be composited will always be
 * 32bit argb, but the source colorspace can be arbitrary.
 * The composite will need to do some conversions if the source 
 * colorspace is anything other than deviceRGB.
 **/
public Composite createComposite(ColorSpace sourcespace, int blendMode, boolean alphaIsShape, float alpha);
}
