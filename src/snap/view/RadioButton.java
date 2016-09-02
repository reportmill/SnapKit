package snap.view;
import snap.gfx.*;

/**
 * A ToggleButton subclass for RadioButton.
 */
public class RadioButton extends ToggleButton {

/**
 * Creates a new RadioButton.
 */
public RadioButton()  { }

/**
 * Creates a new RadioButton with given text.
 */
public RadioButton(String aStr)  { setText(aStr); }

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    int state = isPressed()? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
    Insets ins = getInsetsAll();
    double x = ins.left - 16 - 6, y = ins.top + 2 + Math.round((getHeight() - ins.top - 2 - 16 - 2 - ins.bottom)/2);
    aPntr.clearRect(0,0,getWidth(),getHeight());
    aPntr.drawButton2(x,y,16,16,state,8);
    if(isSelected()) {
        aPntr.setPaint(Color.DARKGRAY); aPntr.fill(new Ellipse(x+3,y+3,10,10)); }
}

/**
 * Returns the default alignment for button.
 */
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

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

}