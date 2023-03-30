/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import java.util.*;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * Provides a tool for editing Paint.
 */
public class PaintTool extends StylerOwner {

    // Map of PaintTool instances by Paint class
    private Map<Class<?>,StylerOwner> _tools = new HashMap<>();
    
    // Array of known fills
    private static Paint[]  _fills;

    /**
     * Creates PaintTool.
     */
    public PaintTool()
    {
        super();
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Configure FillComboBox
        Paint[] fills = getFills();
        String[] fillNames = ArrayUtils.map(fills, fill -> fill.getName(), String.class);
        setViewItems("FillComboBox", fillNames);
    }

    /**
     * Called to reset UI controls.
     */
    protected void resetUI()
    {
        // Get styler
        Styler styler = getStyler();

        // Get current fill (or default, if not available)
        Paint fill = styler.getFill();
        if (fill == null)
            fill = Color.BLACK;

        // Update FillCheckBox, FillComboBox
        setViewValue("FillCheckBox", styler.getFill() != null);
        setViewValue("FillComboBox", fill.getName());

        // Get fill tool, install tool UI in fill panel and ResetUI
        StylerOwner fillTool = getTool(fill);
        getView("FillPane", BoxView.class).setContent(fillTool.getUI());
        fillTool.resetLater();
    }

    /**
     * Called to respond to UI controls
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle FillCheckBox: Iterate over shapes and add fill if not there or remove if there
        if (anEvent.equals("FillCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Paint fill = selected ? Color.BLACK : null;
            styler.setFill(fill);
        }

        // Handle FillComboBox: Get selected fill instance and iterate over shapes and add fill if not there
        if (anEvent.equals("FillComboBox")) {
            Paint[] fills = getFills();
            Paint fill = fills[anEvent.getSelIndex()];
            styler.setFill(fill);
        }
    }

    /**
     * Returns the fills.
     */
    private Paint[] getFills()
    {
        // If already set, just return
        if (_fills != null) return _fills;

        // Create default fills array and return
        Paint fill0 = Color.BLACK;
        Paint fill1 = new GradientPaint();
        return _fills = new Paint[] { fill0, fill1, ImagePaintTool.getDefault() };
    }

    /**
     * Returns the specific tool for a given fill.
     */
    public StylerOwner getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class<?> toolClass = anObj instanceof Class ? (Class<?>) anObj : anObj.getClass();
        StylerOwner tool = _tools.get(toolClass);
        if (tool == null) {
            tool = getToolImpl(toolClass);
            _tools.put(toolClass, tool);
            tool.setStyler(getStyler());
        }

        // Return
        return tool;
    }

    /**
     * Returns the specific tool for a given fill.
     */
    private static StylerOwner getToolImpl(Class<?> aClass)
    {
        if (Color.class.isAssignableFrom(aClass))
            return new ColorPaintTool();
        if (GradientPaint.class.isAssignableFrom(aClass))
            return new GradientPaintTool();
        if (ImagePaint.class.isAssignableFrom(aClass))
            return new ImagePaintTool();
        throw new RuntimeException("PaintTool.getToolImp: No tool class for " + aClass);
    }
}