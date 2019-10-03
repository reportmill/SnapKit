/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A ToggleButton subclass for RadioButton.
 */
public class RadioButton extends ToggleButton {

    // The view to render the actual Radio button
    RadioArea        _radio;
    
    // Constants
    private static final int SPACING = 5;

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
public RadioButton(String aStr)  { this(); setText(aStr); }

/**
 * Override to suppress normal version.
 */
public void paintFront(Painter aPntr)  { }

/**
 * Override to situate Radio view.
 */
public void setPosition(Pos aPos)
{
    // If already set, just return
    if(aPos==getPosition()) return;
    
    // Set new position and make sure label is loaded
    super.setPosition(aPos);
    getLabel();
    
    // If CENTER_RIGHT, put Radio after label, otherwise put first
    removeChild(_radio);
    if(aPos==Pos.CENTER_RIGHT) addChild(_radio);
    else addChild(_radio, 0);
}

/**
 * Returns the default alignment for button.
 */
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the default padding for RadioButton.
 */
public Insets getDefaultPadding()  { return _def; } static Insets _def = new Insets(2);

/**
 * Returns whether button border is painted by default.
 */
protected boolean getDefaultShowBorder()  { return false; }

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
 * The View to render the Radio button.
 */
protected class RadioArea extends View {
    
    /** Create RadioArea. */
    public RadioArea()  { setPrefSize(16, 16); }
    
    /** Paint RadioArea. */
    public void paintFront(Painter aPntr)
    {
        // Get button state
        int state = isPressed()? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
        
        // Draw button background
        aPntr.drawButton2(0, 0, 16, 16, state, 8);
        
        // If selected, draw inner circle
        if(isSelected()) {
            aPntr.setPaint(Color.DARKGRAY);
            aPntr.fill(new Ellipse(3, 3, 10, 10));
        }
    }
}

}