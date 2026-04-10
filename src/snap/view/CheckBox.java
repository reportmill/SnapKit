/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.geom.RectBase;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.props.PropSet;

/**
 * A View subclass for CheckBox.
 */
public class CheckBox extends ToggleButton {
    
    // The placeholder view for actual checkbox bounds
    private Label  _check;

    // Constant for CheckBox rect
    private static final RoundRect CHECK_BOX_RECT = new RoundRect(0, 0, 16, 16, 3);
    
    // Constants for overridden defaults
    private static final boolean DEFAULT_CHECK_BOX_SHOW_AREA = false;

    /**
     * Constructor.
     */
    public CheckBox()
    {
        super();
        _showArea = DEFAULT_CHECK_BOX_SHOW_AREA;

        // Create/add check
        _check = new Label();
        _check.setPrefSize(16, 16);
        addChild(_check);
    }

    /**
     * Constructor for given label text.
     */
    public CheckBox(String aStr)  { this(); setText(aStr); }

    /**
     * Override to suppress normal painting.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        double buttonX = _check.getX();
        double buttonY = _check.getY();
        aPntr.translate(buttonX, buttonY);
        paintCheckBoxButton(aPntr, this);
        aPntr.translate(-buttonX, -buttonY);
    }

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
        if (aPos == Pos.CENTER_RIGHT)
            addChild(_check);
        else addChild(_check, 0);
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
        aPropSet.getPropForName(ShowArea_Prop).setDefaultValue(DEFAULT_CHECK_BOX_SHOW_AREA);
    }

    /**
     * Draws checkbox for given checkbox or checkbox menuitem.
     */
    public static void paintCheckBoxButton(Painter aPntr, ButtonBase aButton)
    {
        // Get fill color and paint fill
        Color fillColor = aButton.getFillColor();
        if (fillColor != null)
            aPntr.fillWithPaint(CHECK_BOX_RECT, fillColor);

        // Get stroke color and paint stroke
        Border border = aButton.getBorder();
        Color strokeColor = border != null ? border.getColor() : null;
        if (strokeColor != null)
            aPntr.drawWithPaint(CHECK_BOX_RECT, strokeColor);

        // Paint selected
        if (aButton.isSelected()) {
            Stroke oldStroke = aPntr.getStroke();
            int OFFSET = 5, SIZE = 11;
            aPntr.setStroke(Stroke.Stroke2);
            aPntr.drawLineWithPaint(OFFSET, OFFSET, SIZE, SIZE, Color.BLACK);
            aPntr.drawLine(SIZE, OFFSET, OFFSET, SIZE);
            aPntr.setStroke(oldStroke);
        }
    }
}