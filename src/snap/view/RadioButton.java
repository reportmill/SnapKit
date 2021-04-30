/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;

/**
 * A ToggleButton subclass for RadioButton.
 */
public class RadioButton extends ToggleButton {

    // The view to render the actual Radio button
    private RadioArea  _radio;

    // Constants for overridden defaults
    private static final Pos DEFAULT_ALIGN = Pos.CENTER_LEFT;
    private static final Insets DEFAULT_PADDING = new Insets(2);
    private static final boolean DEFAULT_SHOW_AREA = false;
    private static final int DEFAULT_SPACING = 5;

    /**
     * Creates RadioButton.
     */
    public RadioButton()
    {
        // Create/add radio
        _radio = new RadioArea();
        addChild(_radio);
    }

    /**
     * Creates RadioButton with given text.
     */
    public RadioButton(String aStr)
    {
        this(); setText(aStr);
    }

    /**
     * Override to suppress normal version.
     */
    @Override
    protected void paintButton(Painter aPntr)  { }

    /**
     * Override to return some space between button and label.
     */
    public double getSpacing()  { return DEFAULT_SPACING; }

    /**
     * Override to situate Radio view.
     */
    public void setPosition(Pos aPos)
    {
        // If already set, just return
        if (aPos==getPosition()) return;

        // Set new position and make sure label is loaded
        super.setPosition(aPos);
        getLabel();

        // If CENTER_RIGHT, put Radio after label, otherwise put first
        removeChild(_radio);
        if (aPos==Pos.CENTER_RIGHT)
            addChild(_radio);
        else addChild(_radio, 0);
    }

    /**
     * Returns the default alignment for button.
     */
    public Pos getDefaultAlign()  { return DEFAULT_ALIGN; }

    /**
     * Returns the default padding for RadioButton.
     */
    public Insets getDefaultPadding()  { return DEFAULT_PADDING; }

    /**
     * Returns whether button displays standard background by default.
     */
    protected boolean getDefaultShowArea()  { return DEFAULT_SHOW_AREA; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, aW); }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()  { RowView.layout(this, false); }

    /**
     * The View to render the Radio button.
     */
    protected class RadioArea extends View {

        /** Create RadioArea. */
        public RadioArea()
        {
            setPrefSize(16, 16);
        }

        /** Paint RadioArea. */
        public void paintFront(Painter aPntr)
        {
            _btnArea.paint(aPntr);
        }
    }
}