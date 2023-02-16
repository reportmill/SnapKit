/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Polygon;
import snap.gfx.Border;
import snap.gfx.Color;

/**
 * A simple view to provide a close box.
 */
public class CloseBox extends ShapeView {

    // Constant for close shape, Borders
    private static final Polygon CLOSE_SHAPE = new Polygon(0, 2, 2, 0, 5, 3, 8, 0, 10, 2, 7, 5, 10, 8, 8, 10, 5, 7, 2, 10, 0, 8, 3, 5);
    private static final Color DEFAULT_COLOR = null;
    private static final Color HOVER_COLOR = Color.CRIMSON;
    private static Border DEFAULT_BORDER = Border.createLineBorder(Color.BLACK, .5);
    private static Border HOVER_BORDER = Border.createLineBorder(Color.BLACK, 1);
    private static EventListener  _handleCloseBoxMouseEvent = e -> handleCloseBoxMouseEvent(e);

    /**
     * Constructor.
     */
    public CloseBox()
    {
        super();
        setShape(CLOSE_SHAPE);
        sizeToShape();

        setPrefSize(11, 11);
        setFillSize(true);
        setFill(DEFAULT_COLOR);
        setBorder(DEFAULT_BORDER);
        addEventFilter(_handleCloseBoxMouseEvent, MouseEnter, MouseExit, MouseRelease);
    }

    /**
     * Called for events on bookmark close button.
     */
    private static void handleCloseBoxMouseEvent(ViewEvent anEvent)
    {
        // Get CloseBox
        View closeBox = anEvent.getView();

        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            closeBox.setFill(HOVER_COLOR);
            closeBox.setBorder(HOVER_BORDER);
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit()) {
            closeBox.setFill(DEFAULT_COLOR);
            closeBox.setBorder(DEFAULT_BORDER);
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())
            ViewUtils.fireActionEvent(closeBox, anEvent);

        anEvent.consume();
    }
}
