package snap.view;
import snap.gfx.*;

/**
 * A JComponentShape for JCheckBoxMenuItem.
 */
public class CheckBoxMenuItem extends MenuItem {

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    int state = isPressed()? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
    Insets ins = getInsetsAll();
    double x = ins.left - 16 - 6, y = ins.top + 2 + Math.round((getHeight() - ins.top - 2 - 16 - 2 - ins.bottom)/2);
    aPntr.drawButton2(x,y,16,16, state);
    if(isSelected()) {
        aPntr.setPaint(Color.BLACK); Stroke str = aPntr.getStroke(); aPntr.setStroke(new Stroke(2));
        aPntr.drawLine(x+5,y+5,x+11,y+11); aPntr.drawLine(x+11,y+5,x+5,y+11); aPntr.setStroke(str);
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

}