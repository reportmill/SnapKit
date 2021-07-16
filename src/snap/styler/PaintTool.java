/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * Provides a tool for editing Paint.
 */
public class PaintTool extends StylerOwner {

    // Map of PaintTool instances by Paint class
    private Map<Class,StylerOwner> _tools = new HashMap<>();
    
    // Array of known fills
    private static Paint  _fills[];

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
        // Get array of known fill names and initialize FillComboBox
        int fcount = getFillCount();
        Object fnames[] = new String[fcount];
        for (int i=0;i<fcount;i++)
            fnames[i] = getFill(i).getName();
        setViewItems("FillComboBox", fnames);
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
        if (fill==null) fill = Color.BLACK;

        // Update FillCheckBox, FillComboBox
        setViewValue("FillCheckBox", styler.getFill()!=null);
        setViewValue("FillComboBox", fill.getName());

        // Get fill tool, install tool UI in fill panel and ResetUI
        StylerOwner ftool = getTool(fill);
        getView("FillPane", BoxView.class).setContent(ftool.getUI());
        ftool.resetLater();
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
            Paint fill = getFill(anEvent.getSelIndex());
            styler.setFill(fill);
        }
    }

    /**
     * Returns the number of known fills.
     */
    public int getFillCount()  { return getFills().length; }

    /**
     * Returns an individual fill at given index.
     */
    public Paint getFill(int anIndex)  { return getFills()[anIndex]; }

    /**
     * Returns the fills.
     */
    private Paint[] getFills()
    {
        // If already set, just return
        if (_fills!=null) return _fills;

        // Create default fills array and return
        Paint f0 = Color.BLACK, f1 = new GradientPaint();
        return _fills = new Paint[] { f0, f1, ImagePaintTool.getDefault() };
    }

    /**
     * Returns the specific tool for a given fill.
     */
    public StylerOwner getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        StylerOwner tool = _tools.get(cls);
        if (tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given fill.
     */
    private static StylerOwner getToolImpl(Class aClass)
    {
        if (aClass==Color.class) return new ColorPaintTool();
        if (aClass==GradientPaint.class) return new GradientPaintTool();
        if (aClass==ImagePaint.class) return new ImagePaintTool();
        throw new RuntimeException("PaintTool.getToolImp: No tool class for " + aClass);
    }
}