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
        Color color = getStyler().getFillColor();

        // Update FillColorWell
        setViewValue("FillColorWell", color);
    }

    /**
     * Called to respond to UI controls
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle FillColorWell: Get color from ColorWell and set with styler
        if (anEvent.equals("FillColorWell")) {
            ColorWell cwell = getView("FillColorWell", ColorWell.class);
            Color color = cwell.getColor();
            getStyler().setFillColor(color);
        }
    }
}