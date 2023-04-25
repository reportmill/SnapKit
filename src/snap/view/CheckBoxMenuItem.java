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

    // Constants
    private static final int DEFAULT_BUTTON_PAD = 2;
    private static final int CHECKBOX_GRAPHIC_WIDTH = 16;
    private static final int CHECKBOX_GRAPHIC_SPACING = 6;
    private static final int CHECKBOX_GRAPHIC_INSET_ALL = DEFAULT_BUTTON_PAD + CHECKBOX_GRAPHIC_WIDTH + CHECKBOX_GRAPHIC_SPACING;

    /**
     * Constructor.
     */
    public CheckBoxMenuItem()
    {
        super();
    }

    /**
     * Override to toggle Selected state.
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        // Toggle Selected property
        setSelected(!isSelected());

        // Do normal version
        super.fireActionEvent(anEvent);
    }

    /**
     * Paint Button.
     */
    @Override
    protected void paintButton(Painter aPntr)
    {
        ButtonPainter buttonPainter = ViewTheme.get().getButtonPainter();
        buttonPainter.paintButton(aPntr, this);
    }

    /**
     * Override to customize left padding for checkbox graphic.
     */
    @Override
    protected BoxViewProxy<?> getViewProxy()
    {
        BoxViewProxy<?> viewProxy = super.getViewProxy();
        viewProxy.setPadding(Insets.add(getPadding(), 0, 0, 0, CHECKBOX_GRAPHIC_INSET_ALL));
        return viewProxy;
    }
}