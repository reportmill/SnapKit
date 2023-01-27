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

    // The placeholder view for actual checkbox bounds
    private Label  _radio;

    // Constants for overridden defaults
    private static final boolean DEFAULT_RADIO_BUTTON_SHOW_AREA = false;
    private static final Pos DEFAULT_RADIO_BUTTON_ALIGN = Pos.CENTER_LEFT;
    private static final Insets DEFAULT_RADIO_BUTTON_PADDING = new Insets(2);
    private static final int DEFAULT_RADIO_BUTTON_SPACING = 5;

    /**
     * Creates RadioButton.
     */
    public RadioButton()
    {
        // Create/add radio
        _radio = new Label();
        _radio.setPrefSize(16, 16);
        addChild(_radio);
    }

    /**
     * Creates RadioButton with given text.
     */
    public RadioButton(String aStr)
    {
        this();
        setText(aStr);
    }

    /**
     * Override to suppress normal version.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        double buttonX = _radio.getX();
        double buttonY = _radio.getY();
        aPntr.translate(buttonX, buttonY);
        ButtonPainter buttonPainter = ViewTheme.get().getButtonPainter();
        buttonPainter.paintButton(aPntr, this);
        aPntr.translate(-buttonX, -buttonY);
    }

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
    public Pos getDefaultAlign()  { return DEFAULT_RADIO_BUTTON_ALIGN; }

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
     * Override to customize.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // ShowArea
            case ShowArea_Prop: return DEFAULT_RADIO_BUTTON_SHOW_AREA;

            // Align, Padding, Spacing
            case Align_Prop: return DEFAULT_RADIO_BUTTON_ALIGN;
            case Padding_Prop: return DEFAULT_RADIO_BUTTON_PADDING;
            case Spacing_Prop: return DEFAULT_RADIO_BUTTON_SPACING;

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }
}