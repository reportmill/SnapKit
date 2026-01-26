/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.PropSet;

/**
 * A View subclass for CheckBox.
 */
public class CheckBox extends ToggleButton {
    
    // The placeholder view for actual checkbox bounds
    private Label  _check;
    
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
        ButtonPainter buttonPainter = ViewTheme.get().getButtonPainter();
        buttonPainter.paintButton(aPntr, this);
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
}