/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for ShadowEffect.
 */
public class ShadowEffectTool extends StylerOwner {

    /**
     * Reset UI controls.
     */
    public void resetUI()
    {
        // Get currently selected effect and shadow effect (create new if not available)
        Styler styler = getStyler();
        Effect eff = styler.getEffect();
        ShadowEffect seff = eff instanceof ShadowEffect ? (ShadowEffect) eff : new ShadowEffect();

        // Update ShadowColor
        setViewValue("ShadowColorWell", seff.getColor());

        // Set SoftnessSlider and SoftnessText
        setViewValue("SoftnessSlider", seff.getRadius());
        setViewValue("SoftnessText", seff.getRadius());

        // Update ShadowDXSpinner and ShadowDYSpinner
        setViewValue("ShadowDXSpinner", seff.getDX());
        setViewValue("ShadowDYSpinner", seff.getDY());
    }

    /**
     * Responds to changes from the UI panel controls and updates currently selected shape.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get currently selected effect and shadow effect (create new if not available)
        Styler styler = getStyler();
        Effect eff = styler.getEffect();
        ShadowEffect seff = eff instanceof ShadowEffect ? (ShadowEffect) eff : new ShadowEffect();

        // Handle ShadowColorWell: Create new fill from old shadow fill with new softness
        if (anEvent.equals("ShadowColorWell")) {
            ColorWell cwell = getView("ShadowColorWell", ColorWell.class);
            seff = seff.copyForColor(cwell.getColor());
        }

        // Handle SoftnessText and SoftnessSlider
        if (anEvent.equals("SoftnessText") || anEvent.equals("SoftnessSlider"))
            seff = seff.copyForRadius(anEvent.getIntValue());

        // Handle ShadowDXSpinner, ShadowDYSpinner
        if (anEvent.equals("ShadowDXSpinner"))
            seff = seff.copyForOffset(anEvent.getIntValue(), seff.getDY());
        if (anEvent.equals("ShadowDYSpinner"))
            seff = seff.copyForOffset(seff.getDX(), anEvent.getIntValue());

        // Handle OffsetPanel - get the offset panel and create new fill from old shadow fill with new dx
        if (anEvent.equals("ShadowOffsetPanel")) {
            OffsetPanel op = getView("ShadowOffsetPanel", OffsetPanel.class);
            seff = seff.copyForOffset(seff.getDX() + op.getDX(), seff.getDY() + op.getDY());
        }

        // Set new shadow effect
        styler.setEffect(seff);
    }

    /**
     * Implements a simple control to edit shadow position.
     */
    public static class OffsetPanel extends View {

        // Previous offsets
        int _x1, _y1;

        // Current offsets
        int _x2, _y2;

        /**
         * Creates offset panel.
         */
        public OffsetPanel()
        {
            setActionable(true);
            enableEvents(MousePress, MouseDrag, MouseRelease);
        }

        /**
         * Handle Events.
         */
        protected void processEvent(ViewEvent e)
        {
            // Handle MousePressed
            if (e.isMousePress()) {
                _x1 = _x2 = (int) e.getX();
                _y1 = _y2 = (int) e.getY();
            }

            // Handle MouseDragged, MouseReleased
            else if (e.isMouseDrag() || e.isMouseRelease()) {
                if (!isEnabled()) return;
                _x1 = _x2;
                _y1 = _y2;
                _x2 = (int) e.getX();
                _y2 = (int) e.getY();

                // Send event, repaint
                fireActionEvent(e);
                repaint();
            }
        }

        /**
         * Return offset X.
         */
        public int getDX()  { return _x2 - _x1; }

        /**
         * Return offset Y.
         */
        public int getDY()  { return _y2 - _y1; }

        /**
         * Paint component.
         */
        public void paintFront(Painter aPntr)
        {
            double w = getWidth();
            double h = getHeight();
            aPntr.setColor(Color.WHITE);
            aPntr.fillRect(0, 0, w, h);
            aPntr.setColor(Color.BLACK);
            aPntr.drawRect(0, 0, w, h);
            aPntr.setColor(Color.LIGHTGRAY);
            aPntr.fill3DRect(w / 4, h / 4, w / 2, h / 2, true);
        }
    }
}