/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A MenuItem subclass for Menu-item with CheckBox.
 */
public class CheckBoxMenuItem extends MenuItem {

    // The ButtonArea to paint actual button part
    ButtonArea  _btnArea;

/**
 * Creates CheckBoxMenuItem.
 */
public CheckBoxMenuItem()
{
    themeChanged();
}

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    // Get state and update ButtonArea.State
    int state = isPressed()? BUTTON_PRESSED : _targeted? BUTTON_OVER : BUTTON_NORMAL;
    _btnArea.setState(state);
    
    // Get insets to get button X/Y
    Insets ins = getInsetsAll();
    double x = ins.left - 16 - 6;
    double y = ins.top + 2 + Math.round((getHeight() - ins.getHeight() - 2 - 16 - 2)/2);
    
    // Update ButtonArea.X/Y and paint
    _btnArea.setXY(x, y);
    _btnArea.paint(aPntr);
    
    // If Selected paint X
    if(isSelected()) {
        Stroke str = aPntr.getStroke();
        aPntr.setStroke(Stroke.Stroke2);
        aPntr.drawLineWithPaint(x+5, y+5, x+11, y+11, Color.BLACK);
        aPntr.drawLine(x+11, y+5, x+5, y+11);
        aPntr.setStroke(str);
    }
}

/**
 * Returns the insets for checkbox.
 */
public Insets getInsetsAll()
{
    Insets ins = super.getInsetsAll();
    return new Insets(ins.top, ins.right, ins.bottom, ins.left + 2 + 16 + 6);
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    return Math.max(super.getPrefHeightImpl(aW), ins.top + 2 + 16 + 2 + ins.bottom);
}

    
/**
 * Override to set/reset ButtonArea.
 */
protected void themeChanged()
{
    super.themeChanged();
    _btnArea = ViewTheme.get().createButtonArea();
    _btnArea.setBounds(0, 0, 16, 16);
    _btnArea.setRadius(3);
}
    
}