/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.gfx.*;

/**
 * This class provides a RM shape/inspector for editing JSeparator.
 */
public class Separator extends View {
    
    // Color
    private static Color BRIGHT_COLOR = new Color(1,1,1,.5);

    /**
     * Override to wrap in Painter and forward.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Set stroke/color
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.setPaint(BRIGHT_COLOR);

        // Paint horizontal
        if(isHorizontal()) {
            double areaMidY = Math.floor(areaY+areaH/2) + .5;
            double areaMaxX = areaX + areaW;
            aPntr.drawLine(areaX,areaMidY-1, areaMaxX,areaMidY-1);
            aPntr.drawLine(areaX,areaMidY+1, areaMaxX,areaMidY+1);
            aPntr.setPaint(Color.LIGHTGRAY);
            aPntr.drawLine(areaX, areaMidY, areaMaxX, areaMidY);
        }

        // Paint vertical
        else {
            double areaMidX = Math.floor(areaX+areaW/2) + .5;
            double areaMaxY = areaY + areaH;
            aPntr.drawLine(areaMidX-1, areaY,areaMidX-1, areaMaxY);
            aPntr.drawLine(areaMidX+1, areaY,areaMidX+1, areaMaxY);
            aPntr.setPaint(Color.LIGHTGRAY);
            aPntr.drawLine(areaMidX, areaY, areaMidX, areaMaxY);
        }
    }

    /**
     * Override to return default preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return isVertical()? 3 : 0;
    }

    /**
     * Override to return default preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return isHorizontal()? 3 : 0;
    }
}