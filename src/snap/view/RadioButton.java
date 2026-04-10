/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropSet;

/**
 * A ToggleButton subclass for RadioButton.
 */
public class RadioButton extends ToggleButton {

    // The placeholder view for actual checkbox bounds
    private Label _radio;

    // The RadioButton rect
    private static final RoundRect RADIO_BUTTON_RECT = new RoundRect(0, 0, 16, 16, 8);
    private static final Ellipse RADIO_INTERIOR_SHAPE = new Ellipse(3, 3, 10, 10);

    // Constants for overridden defaults
    private static final boolean DEFAULT_RADIO_BUTTON_SHOW_AREA = false;

    /**
     * Constructor.
     */
    public RadioButton()
    {
        super();
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
        paintRadioButton(aPntr);
        aPntr.translate(-buttonX, -buttonY);
    }

    /**
     * Paints radio button.
     */
    private void paintRadioButton(Painter aPntr)
    {
        // Paint outside fill and stroke
        Color fillColor = getFillColor();
        if (fillColor != null)
            aPntr.fillWithPaint(RADIO_BUTTON_RECT, fillColor);
        Border border = getBorder();
        Color strokeColor = border != null ? border.getColor() : null;
        if (strokeColor != null)
            aPntr.drawWithPaint(RADIO_BUTTON_RECT, strokeColor);

        // Paint selected
        if (isSelected())
            aPntr.fillWithPaint(RADIO_INTERIOR_SHAPE, Color.DARKGRAY);
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
     * Override to return row layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new RowViewLayout(this, false); }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override ShowArea
        aPropSet.getPropForName(ShowArea_Prop).setDefaultValue(DEFAULT_RADIO_BUTTON_SHOW_AREA);
    }
}