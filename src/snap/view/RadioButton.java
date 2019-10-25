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
 * The View to render the Radio button.
 */
protected class RadioArea extends View {
    
    // The ButtonArea to paint actual button part
    ButtonArea _btnArea;
    
    // The shape to paint fill button part
    Shape      _centerShape = new Ellipse(3, 3, 10, 10);
    
    /** Create RadioArea. */
    public RadioArea()
    {
        setPrefSize(16, 16);
        themeChanged();
    }
    
    /** Paint RadioArea. */
    public void paintFront(Painter aPntr)
    {
        // Update ButtonArea.State and ButtonArea.Selected
        int state = isPressed()? BUTTON_PRESSED : _targeted? BUTTON_OVER : BUTTON_NORMAL;
        _btnArea.setState(state);
        _btnArea.setSelected(isSelected());
        
        // Paint actual button part
        _btnArea.paint(aPntr);
        
        // If selected, draw inner circle
        if(isSelected())
            aPntr.fillWithPaint(_centerShape, Color.DARKGRAY);
    }
    
    /** Override to set/reset ButtonArea. */
    protected void themeChanged()
    {
        super.themeChanged();
        _btnArea = ViewTheme.get().createButtonArea();
        _btnArea.setBounds(0, 0, 16, 16);
        _btnArea.setRadius(8);
    }
}

}