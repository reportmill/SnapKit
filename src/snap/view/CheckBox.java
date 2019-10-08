/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A View subclass for CheckBox.
 */
public class CheckBox extends ToggleButton {
    
    // The view to render the actual check box
    CheckArea        _check;
    
    // Constants
    private static final int SPACING = 5;

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
public void paintFront(Painter aPntr)  { }

/**
 * Override to situate Check view.
 */
public void setPosition(Pos aPos)
{
    // If already set, just return
    if(aPos==getPosition()) return;
    
    // Set new position and make sure label is loaded
    super.setPosition(aPos);
    getLabel();
    
    // If CENTER_RIGHT, put Check after label, otherwise put Check first
    removeChild(_check);
    if(aPos==Pos.CENTER_RIGHT) addChild(_check);
    else addChild(_check, 0);
}

/**
 * Returns the default alignment for CheckBox.
 */
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the default padding for CheckBox.
 */
public Insets getDefaultPadding()  { return _def; } static Insets _def = new Insets(2);

/**
 * Returns whether button displays standard background by default.
 */
protected boolean getDefaultShowArea()  { return false; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, null, SPACING, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, null, aW); }

/**
 * Override to layout children.
 */
protected void layoutImpl()  { RowView.layout(this, null, null, false, SPACING); }

/**
 * The View to render the check.
 */
protected class CheckArea extends View {
    
    /** Create CheckArea. */
    public CheckArea()  { setPrefSize(16, 16); }
    
    /** Paint CheckArea. */
    public void paintFront(Painter aPntr)
    {
        // Get button state
        int state = isPressed()? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
        
        // Draw button background
        aPntr.drawButton2(0, 0, 16, 16, state);
        
        // If selected, draw X
        if(isSelected()) {
            Stroke str = aPntr.getStroke();
            aPntr.setPaint(Color.BLACK);
            aPntr.setStroke(new Stroke(2));
            aPntr.drawLine(5, 5, 11, 11);
            aPntr.drawLine(11, 5, 5, 11);
            aPntr.setStroke(str);
        }
    }
}

}