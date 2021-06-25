/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.Color;

/**
 * This class represents a light in a scene.
 * 
 * The current implementation simply models a distant light aligned with the camera along with an ambient light.
 * 
 * Future versions could expand this into full support for ambient lights, distant lights and spot lights. There
 * could even be a class called LightSet, so multiple lights could be supported but the simple API could be preserved.
 */
public class Light {

    // The normal
    private Vector3D  _normal = new Vector3D(0, 0, -1);

    /**
     * Constructor.
     */
    public Light()  { }

    /**
     * Returns the render color for this light for given normal and color.
     */
    public Color getRenderColor(Vector3D normal, Color aColor)
    {
        // Get dot product of path normal and light normal
        double normalDotLight = normal.getDotProduct(_normal);

        // Get coefficient of ambient (KA) and diffuse (KD) reflection for shading
        double _ka = .7;
        double _kd = .6;

        // Calculate color components based on original color, surface normal, reflection constants and light source
        double r = aColor.getRed() * _ka + aColor.getRed() * _kd * normalDotLight; r = Math.min(r, 1);
        double g = aColor.getGreen() * _ka + aColor.getGreen() * _kd * normalDotLight; g = Math.min(g, 1);
        double b = aColor.getBlue() * _ka + aColor.getBlue() * _kd * normalDotLight; b = Math.min(b, 1);

        // Set new color
        return new Color(r, g, b, aColor.getAlpha());
    }
}