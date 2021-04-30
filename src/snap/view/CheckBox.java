/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;

/**
 * A View subclass for CheckBox.
 */
public class CheckBox extends ToggleButton {
    
    // The view to render the actual check box
    private CheckArea  _check;
    
    // Constants for overridden defaults
    private static final Pos DEFAULT_ALIGN = Pos.CENTER_LEFT;
    private static final Insets DEFAULT_PADDING = new Insets(2);
    private static final boolean DEFAULT_SHOW_AREA = false;
    private static final int DEFAULT_SPACING = 5;

    /**
     * Creates CheckBox.
     */
    public CheckBox()
    {
        // Create/add check
        _check = new CheckArea();
        addChild(_check);
    }

    /**
     * Creates CheckBox with given text.
     */
    public CheckBox(String aStr)  { this(); setText(aStr); }

    /**
     * Override to suppress normal painting.
     */
    @Override
    protected void paintButton(Painter aPntr)  { }

    /**
     * Override to return some space between button and label.
     */
    public double getSpacing()  { return DEFAULT_SPACING; }

    /**
     * Override to situate Check view.
     */
    public void setPosition(Pos aPos)
    {
        // If already set, just return
        if (aPos == getPosition()) return;

        // Set new position and make sure label is loaded
        super.setPosition(aPos);
        getLabel();

        // If CENTER_RIGHT, put Check after label, otherwise put Check first
        removeChild(_check);
        if (aPos==Pos.CENTER_RIGHT)
            addChild(_check);
        else addChild(_check, 0);
    }

    /**
     * Returns the default alignment for CheckBox.
     */
    public Pos getDefaultAlign()  { return DEFAULT_ALIGN; }

    /**
     * Returns the default padding for CheckBox.
     */
    public Insets getDefaultPadding()  { return DEFAULT_PADDING; }

    /**
     * Returns whether button displays standard background by default.
     */
    protected boolean getDefaultShowArea()  { return DEFAULT_SHOW_AREA; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return RowView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()
    {
        RowView.layout(this, false);
    }

    /**
     * The View to render the check.
     */
    protected class CheckArea extends View {

        /** Create CheckArea. */
        public CheckArea()
        {
            setPrefSize(16, 16);
        }

        /** Paint CheckArea. */
        public void paintFront(Painter aPntr)
        {
            _btnArea.paint(aPntr);
        }
    }
}