/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.PropSet;

/**
 * A ToggleButton subclass for RadioButton.
 */
public class RadioButton extends ToggleButton {

    // The placeholder view for actual checkbox bounds
    private Label  _radio;

    // Constants for overridden defaults
    private static final Pos DEFAULT_RADIO_BUTTON_ALIGN = Pos.CENTER_LEFT;
    private static final Insets DEFAULT_RADIO_BUTTON_PADDING = new Insets(2);
    private static final int DEFAULT_RADIO_BUTTON_SPACING = 5;
    private static final boolean DEFAULT_RADIO_BUTTON_SHOW_AREA = false;

    /**
     * Constructor.
     */
    public RadioButton()
    {
        super();
        _align = DEFAULT_RADIO_BUTTON_ALIGN;
        _padding = DEFAULT_RADIO_BUTTON_PADDING;
        _spacing = DEFAULT_RADIO_BUTTON_SPACING;
        _showArea = DEFAULT_RADIO_BUTTON_SHOW_AREA;

        // Create/add radio
        _radio = new Label();
        _radio.setPrefSize(16, 16);
        addChild(_radio);
    }

    /**
     * Constructor for given label text.
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
        if (aPos == getPosition()) return;

        // Set new position and make sure label is loaded
        super.setPosition(aPos);
        getLabel();

        // If CENTER_RIGHT, put Radio after label, otherwise put first
        removeChild(_radio);
        if (aPos == Pos.CENTER_RIGHT)
            addChild(_radio);
        else addChild(_radio, 0);
    }

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
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override Align, Padding, Spacing, ShowArea
        aPropSet.getPropForName(Align_Prop).setDefaultValue(DEFAULT_RADIO_BUTTON_ALIGN);
        aPropSet.getPropForName(Padding_Prop).setDefaultValue(DEFAULT_RADIO_BUTTON_PADDING);
        aPropSet.getPropForName(Spacing_Prop).setDefaultValue(DEFAULT_RADIO_BUTTON_SPACING);
        aPropSet.getPropForName(ShowArea_Prop).setDefaultValue(DEFAULT_RADIO_BUTTON_SHOW_AREA);
    }
}