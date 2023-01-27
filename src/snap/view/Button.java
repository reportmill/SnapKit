/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A View subclass that represents a simple Button.
 */
public class Button extends ButtonBase {

    // Whether this button is the window default button
    private boolean  _default;
    
    // Whether this button is the window cancel button
    private boolean  _cancel;
    
    // Constants for properties
    public static final String DefaultButton_Prop = "DefaultButton";

    /**
     * Constructor.
     */
    public Button()
    {
        super();
    }

    /**
     * Constructor with given text.
     */
    public Button(String aStr)
    {
        super();
        setText(aStr);
    }

    /**
     * Returns whether button is window default button.
     */
    public boolean isDefaultButton()  { return _default; }

    /**
     * Sets whether button is window default button.
     */
    public void setDefaultButton(boolean aValue)
    {
        if (aValue == _default) return;
        firePropChange(DefaultButton_Prop, _default, _default=aValue);
    }

    /**
     * Paint Button.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        // Handle ShowArea + DefaultButton
        if (isShowArea() && isDefaultButton()) {
            ButtonPainter buttonPainter = ViewTheme.get().getButtonPainter();
            buttonPainter.paintDefaultButton(aPntr, this);
        }

        // Otherwise, do normal version
        else super.paintButton(aPntr);
    }
}