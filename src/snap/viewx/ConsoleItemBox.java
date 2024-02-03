/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.BoxView;
import snap.view.Cursor;
import snap.view.View;
import snap.view.ViewEvent;

/**
 * This View subclass displays a console item.
 */
class ConsoleItemBox extends BoxView {

    // Vars for box resize
    private static double  _mx, _my, _boxW, _boxH;

    // Constants
    private static final ShadowEffect DEFAULT_SHADOW = new ShadowEffect(5, Color.GRAY3, 0, 0);

    /**
     * Constructor.
     */
    public ConsoleItemBox(View aView)
    {
        // Create BoxView wrapper
        super(aView, false, false);
        setFill(Color.WHITE);
        setBorderRadius(4);
        setEffect(DEFAULT_SHADOW);

        // Sizing
        setMargin(6, 10, 6, 10);

        // Register box for resize events
        if (getPrefWidth() > 100 && getPrefHeight() > 40)
            addEventHandler(e -> boxViewDidMouseEvent(e), View.MouseEvents);
    }

    /**
     * Handle Box mouse events to resize.
     */
    private static void boxViewDidMouseEvent(ViewEvent anEvent)
    {
        // Get event info
        View view = anEvent.getView();
        double mx = anEvent.getX();
        double my = anEvent.getY();

        // Handle MouseMove
        if (anEvent.isMouseMove()) {
            if (mx > view.getWidth() - 10 && my > view.getHeight() - 10)
                view.setCursor(Cursor.SE_RESIZE);
            else view.setCursor(null);
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit())
            view.setCursor(null);

            // Handle MousePress
        else if (anEvent.isMousePress()) {
            if (mx > view.getWidth() - 10 && my > view.getHeight() - 10) {
                _mx = mx; _my = my;
                _boxW = view.getWidth();
                _boxH = view.getHeight();
                anEvent.consume();
            }
        }

        // Handle MouseDrag/MouseRelease
        else if (_mx > 0) {

            if (anEvent.isMouseDrag()) {
                double dw = mx - _mx;
                double dh = my - _my;
                double boxW = Math.max(_boxW + dw, ((int) _boxW) / 2);
                double boxH = Math.max(_boxH + dh, ((int) _boxH) / 2);
                view.setPrefSize(boxW, boxH);
                View content = ((BoxView) view).getContent();
                content.setGrowWidth(true);
                content.setGrowHeight(true);
            }

            else if (anEvent.isMouseRelease())
                _mx = _my = 0;
            anEvent.consume();
        }
    }
}
