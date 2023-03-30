/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.gfx.*;
import snap.view.ViewEvent;
import snap.viewx.ColorWell;

/**
 * Provides a tool for editing Color Paint.
 */
public class ColorPaintTool extends StylerOwner {

    /**
     * Creates PaintTool.
     */
    public ColorPaintTool()
    {
        super();
    }

    /**
     * Called to reset UI controls.
     */
    protected void resetUI()
    {
        // Get currently selected color
        Styler styler = getStyler();
        Color color = styler.getFillColor();

        // Update FillColorWell
        setViewValue("FillColorWell", color);
    }

    /**
     * Called to respond to UI controls
     */
    protected void respondUI(ViewEvent anEvent)
    {
        Styler styler = getStyler();

        // Handle FillColorWell: Get color from ColorWell and set with styler
        if (anEvent.equals("FillColorWell")) {

            // Get color
            ColorWell fillColorWell = getView("FillColorWell", ColorWell.class);
            Color color = fillColorWell.getColor();

            // If command-click, set gradient fill
            if (anEvent.isMetaDown()) {
                Color color1 = styler.getFill() != null ? styler.getFillColor() : Color.CLEARWHITE;
                GradientPaint gradientPaint = new GradientPaint(color1, color, 0);
                styler.setFill(gradientPaint);
            }

            // Set color
            else styler.setFillColor(color);
        }
    }
}