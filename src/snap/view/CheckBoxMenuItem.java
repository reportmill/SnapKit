/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.gfx.*;

/**
 * A MenuItem subclass for Menu-item with CheckBox.
 */
public class CheckBoxMenuItem extends MenuItem {

    /**
     * Paint Button.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        _btnArea.paint(aPntr);
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