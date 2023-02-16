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
    private static Border CLOSE_BOX_BORDER1 = Border.createLineBorder(Color.LIGHTGRAY, .5);
    private static Border CLOSE_BOX_BORDER2 = Border.createLineBorder(Color.BLACK, 1);
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
        setFill(Color.WHITE);
        setBorder(CLOSE_BOX_BORDER1);
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
            closeBox.setFill(Color.CRIMSON);
            closeBox.setBorder(CLOSE_BOX_BORDER2);
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit()) {
            closeBox.setFill(Color.WHITE);
            closeBox.setBorder(CLOSE_BOX_BORDER1);
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())
            ViewUtils.fireActionEvent(closeBox, anEvent);

        anEvent.consume();
    }
}
