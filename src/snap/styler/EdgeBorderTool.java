/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Borders;
import snap.gfx.Color;
import snap.view.ViewEvent;
import snap.viewx.ColorWell;

/**
 * BorderTool subclass for editing EdgeBorder.
 */
public class EdgeBorderTool extends StylerOwner {

    /**
     * Reset UI controls.
     */
    public void resetUI()
    {
        // Get currently selected border/stroke
        Styler styler = getStyler();
        Border border = styler.getBorder(); if (border==null) border = Border.blackBorder();
        Borders.EdgeBorder bstroke = border instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)border : new Borders.EdgeBorder();

        // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb
        setViewValue("StrokeColorWell", border.getColor());
        setViewValue("StrokeWidthText", border.getWidth());
        setViewValue("StrokeWidthThumb", border.getWidth());

        // Update TopCheckBox, RightCheckBox, BottomCheckBox, LeftCheckBox
        setViewValue("TopCheckBox", bstroke.isShowTop());
        setViewValue("RightCheckBox", bstroke.isShowRight());
        setViewValue("BottomCheckBox", bstroke.isShowBottom());
        setViewValue("LeftCheckBox", bstroke.isShowLeft());
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

        // Handle TopCheckBox, RightCheckBox, BottomCheckBox, LeftCheckBox
        if (anEvent.equals("TopCheckBox"))
            styler.setBorderShowEdge(Pos.TOP_CENTER, anEvent.getBoolValue());
        if (anEvent.equals("RightCheckBox"))
            styler.setBorderShowEdge(Pos.CENTER_RIGHT, anEvent.getBoolValue());
        if (anEvent.equals("BottomCheckBox"))
            styler.setBorderShowEdge(Pos.BOTTOM_CENTER, anEvent.getBoolValue());
        if (anEvent.equals("LeftCheckBox"))
            styler.setBorderShowEdge(Pos.CENTER_LEFT, anEvent.getBoolValue());
    }
}
