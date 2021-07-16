/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.gfx.*;
import snap.view.ViewEvent;

/**
 * Inspector for EmbossEffect.
 */
public class EmbossEffectTool extends StylerOwner {

/**
 * Return emboss effect (or default emboss effect, if not available).
 */
public EmbossEffect getEmbossEffect()
{
    Styler styler = getStyler();
    Effect eff = styler.getEffect();
    return eff instanceof EmbossEffect ? (EmbossEffect)eff : new EmbossEffect();
}

/**
 * Called to reset UI controls.
 */
public void resetUI()
{
    // Get currently selected effect
    EmbossEffect emboss = getEmbossEffect();
    
    // Update everybody
    setViewValue("RadiusWheel", emboss.getRadius());
    setViewValue("RadiusTextField", emboss.getRadius());
    setViewValue("AzimuthTextField", emboss.getAzimuth());
    setViewValue("AzimuthWheel", emboss.getAzimuth());
    setViewValue("AltitudeTextField", emboss.getAltitude());
    setViewValue("AltitudeWheel", emboss.getAltitude());
}

/**
 * Responds to changes from the UI panel controls and updates currently selected shape.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected effect
    EmbossEffect eff = getEmbossEffect(), neff = null;

    // Handle AltitudeTextField and AltitudeWheel
    if (anEvent.equals("AltitudeWheel") || anEvent.equals("AltitudeTextField"))
        neff = eff.copyForAltitude(anEvent.getFloatValue());

    // Handle AltitudeTextField and AltitudeWheel
    if (anEvent.equals("AzimuthWheel") || anEvent.equals("AzimuthTextField"))
        neff = eff.copyForAzimuth(anEvent.getFloatValue());

    // Handle AltitudeTextField and AltitudeWheel
    if (anEvent.equals("RadiusWheel") || anEvent.equals("RadiusTextField"))
        neff = eff.copyForRadius(anEvent.getIntValue());

    // Set new effect
    if (neff!=null)
        getStyler().setEffect(neff);
}

}