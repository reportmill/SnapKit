/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for Borders.
 */
public class LineBorderTool extends StylerOwner {

    /**
     * Reset UI controls.
     */
    public void resetUI()
    {
        // Get currently selected border/stroke
        Styler styler = getStyler();
        Border border = styler.getBorder();
        if (border==null) border = Border.createLineBorder(Color.BLACK, 1);
        Stroke stroke = border.getStroke();

        // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb, DashArrayText, DashPhaseSpinner
        setViewValue("StrokeColorWell", border.getColor());
        setViewValue("StrokeWidthText", border.getWidth());
        setViewValue("StrokeWidthThumb", border.getWidth());
        setViewValue("DashArrayText", Stroke.getDashArrayString(stroke));
        setViewValue("DashPhaseSpinner", stroke.getDashOffset());
    }

    /**
     * Respond to UI changes
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get editor selected shapes and selected shape
        Styler styler = getStyler();

        // Handle StrokeColorWell - get color and set in selected shapes
        if (anEvent.equals("StrokeColorWell")) {
            ColorWell cwell = getView("StrokeColorWell", ColorWell.class);
            Color color = cwell.getColor();
            styler.setBorderStrokeColor(color);
        }

        // Handle StrokeWidthText, StrokeWidthThumb
        if (anEvent.equals("StrokeWidthText") || anEvent.equals("StrokeWidthThumb")) {
            double width = anEvent.getFloatValue();
            styler.setBorderStrokeWidth(width);
        }

        // Handle DashArrayText
        if (anEvent.equals("DashArrayText")) {
            double darray[] = Stroke.getDashArray(anEvent.getStringValue());
            styler.setBorderStrokeDashArray(darray);
        }

        // Handle DashPhaseSpinner
        if (anEvent.equals("DashPhaseSpinner")) {
            double dphase = anEvent.getFloatValue();
            styler.setBorderStrokeDashPhase(dphase);
        }
    }
}