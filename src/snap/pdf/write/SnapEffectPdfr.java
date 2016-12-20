/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import snap.gfx.*;
import snap.pdf.PDFWriter;
import snap.view.*;

/**
 * A PDF helper/writer class for Effect.
 */
public class SnapEffectPdfr {

/**
 * Writes a shape with effect.
 */
public static void writeShapeEffect(View aShape, PDFWriter aWriter)
{
    Effect eff = aShape.getEffect();
    if(eff instanceof BlurEffect) writeBlurEffect(aShape, (BlurEffect)eff, aWriter);
    else if(eff instanceof ShadowEffect) writeShadowEffect(aShape, (ShadowEffect)eff, aWriter);
    else if(eff instanceof ReflectEffect) writeRefectionEffect(aShape, (ReflectEffect)eff, aWriter);
    else if(eff instanceof EmbossEffect) writeEmbossEffect(aShape, (EmbossEffect)eff, aWriter);
}
    
/**
 * Writes pdf for given blur effect and shape.
 */
public static void writeBlurEffect(View aShape, BlurEffect aBlur, PDFWriter aWriter)
{
    // If radius is less than 1, do default drawing and return
    if(aBlur.getRadius()<1) { SnapViewPdfr.getPdfr(aShape).writeShapeAll(aShape, aWriter); return; }
    
    // Get effect image and image fill
    Image effImg = getEffectImage(aShape);
    ImagePaint ifill = new ImagePaint(effImg);
    
    // Get bounds for image fill and write
    Rect bounds = new Rect(-aBlur.getRadius()*2,-aBlur.getRadius()*2,effImg.getWidth(),effImg.getHeight());
    SnapPaintPdfr.writeImagePaint(aWriter, ifill, null, bounds);
}

/**
 * Writes pdf for given shadow effect and shape.
 */
public static void writeShadowEffect(View aShape, ShadowEffect aShadow, PDFWriter aWriter)
{
    // Get effect image and image fill
    Image effImg = getEffectImage(aShape);
    ImagePaint ifill = new ImagePaint(effImg);
    
    // Get bounds for image fill and write
    double rad = aShadow.getRadius(), x = -rad*2 + aShadow.getDX(), y = -rad*2 + aShadow.getDY();
    Rect bounds = new Rect(x, y, effImg.getWidth(), effImg.getHeight());
    SnapPaintPdfr.writeImagePaint(aWriter, ifill, null, bounds);
    
    // Do normal pdf write
    SnapViewPdfr.getPdfr(aShape).writeShapeAll(aShape, aWriter);
}
    
/**
 * Writes pdf for given reflection effect and shape.
 */
private static void writeRefectionEffect(View aShape, ReflectEffect aReflect, PDFWriter aWriter)
{
    // If valid reflection and fade heights, do reflection
    if(aReflect.getReflectHeight()>0 && aReflect.getFadeHeight()>0) {
    
        // Get reflection image for shape and fill
        Image refImg = getEffectImage(aShape);
        ImagePaint ifill = new ImagePaint(refImg);
        
        // Get bounds of reflected image and write
        Rect bounds = aShape.getBoundsInside(); //aShape.getBoundsStroked();
        bounds = new Rect(bounds.getX(), bounds.getMaxY() + aReflect.getGap(), refImg.getWidth(), refImg.getHeight());
        SnapPaintPdfr.writeImagePaint(aWriter, ifill, null, bounds);
    }
    
    // Do normal write pdf
    SnapViewPdfr.getPdfr(aShape).writeShapeAll(aShape, aWriter);
}
    
/**
 * Writes pdf for given emboss effect and shape.
 */
private static void writeEmbossEffect(View aShape, EmbossEffect anEmboss, PDFWriter aWriter)
{
    // Get effect image and image fill
    Image effectImage = getEffectImage(aShape);
    ImagePaint ifill = new ImagePaint(effectImage);
    
    // Get bounds for image fill and write
    Rect bounds = new Rect(0, 0, effectImage.getWidth(), effectImage.getHeight());
    SnapPaintPdfr.writeImagePaint(aWriter, ifill, null, bounds);
}
    
/**
 * Returns the effect image.
 */
private static Image getEffectImage(View aShape)
{
    Effect eff = aShape.getEffect();
    Rect bnds = aShape.getBoundsInside(); //aShape.getBoundsStrokedDeep();
    PainterDVR pntr = new PainterDVR();
    ViewUtils.paintAll(aShape, pntr); //paintShapeAll
    if(eff instanceof BlurEffect) return ((BlurEffect)eff).getBlurImage(pntr, bnds);
    if(eff instanceof EmbossEffect) return ((EmbossEffect)eff).getEmbossImage(pntr, bnds);
    if(eff instanceof ReflectEffect) return ((ReflectEffect)eff).getReflectImage(pntr, bnds);
    if(eff instanceof ShadowEffect) return ((ShadowEffect)eff).getShadowImage(pntr, bnds);
    throw new RuntimeException("RMEffectPdfr.getEffectImage: Effect not supported " + eff);
}

}