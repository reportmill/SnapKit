/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;
import snap.view.ViewEvent;

/**
 * A class to rotate/zoom camera for mouse events.
 */
public class MouseHandler {

    // The Camera
    private Camera  _camera;

    // Mouse drag variable - mouse drag last point
    private Point  _pointLast;

    // used for shift-drag to indicate which axis to constrain rotation to
    private int  _dragConstraint;

    // Constants for mouse drag constraints
    public final int CONSTRAIN_NONE = 0;
    public final int CONSTRAIN_PITCH = 1;
    public final int CONSTRAIN_YAW = 2;

    /**
     * Constructor.
     */
    public MouseHandler(Camera aCamera)
    {
        _camera = aCamera;
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        // Seems unlikely - do we need this?
        if (anEvent.isConsumed()) { System.err.println("MouseHandler(gfx3d): processEvent: Event consumed???"); return; }

        // Handle MousePressed: Set last point to event location in scene coords and _dragConstraint
        if (anEvent.isMousePress())
            mousePressed(anEvent);

        // Handle MouseDragged
        else if (anEvent.isMouseDrag())
            mouseDragged(anEvent);

        // Handle Scroll
        else if (anEvent.isScroll())
            scroll(anEvent);

        // Consume event
        anEvent.consume();
    }

    /**
     * Handle MousePress.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        _pointLast = anEvent.getPoint();
        _dragConstraint = CONSTRAIN_NONE;
    }

    /**
     * Handle MouseDrag.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        // Get event location in this scene shape coords
        Point point = anEvent.getPoint();

        // If right-mouse, muck with perspective
        if (anEvent.isShortcutDown()) {
            double gimbalRad = _camera.getGimbalRadius();
            _camera.setPrefGimbalRadius(gimbalRad + _pointLast.y - point.y);
        }

        // Otherwise, just do pitch and roll
        else {

            // Shift-drag constrains to just one axis at a time
            if (anEvent.isShiftDown()) {

                // If no constraint
                if (_dragConstraint == CONSTRAIN_NONE) {
                    if (Math.abs(point.y - _pointLast.y) > Math.abs(point.x - _pointLast.x))
                        _dragConstraint = CONSTRAIN_PITCH;
                    else _dragConstraint = CONSTRAIN_YAW;
                }

                // If Pitch constrained
                if (_dragConstraint == CONSTRAIN_PITCH)
                    point.x = _pointLast.x;

                    // If Yaw constrained
                else point.y = _pointLast.y;
            }

            // Set pitch & yaw
            _camera.setPitch(_camera.getPitch() + (point.y - _pointLast.y)/1.5f);
            _camera.setYaw(_camera.getYaw() + (point.x - _pointLast.x)/1.5f);
        }

        // Set last point
        _pointLast = point;
    }

    /**
     * Handle Scroll.
     */
    public void scroll(ViewEvent anEvent)
    {
        // Assume + 1x per 60 points (1 inches)
        int SCROLL_SCALE = 10;
        double scroll = anEvent.getScrollY();
        double distZ = scroll * SCROLL_SCALE;
        double focalLen = _camera.getFocalLength();
        double gimbalRad = _camera.getGimbalRadius();
        double gimbalRad2 = Math.max(gimbalRad + distZ, -focalLen + 100);
        _camera.setPrefGimbalRadius(gimbalRad2);
    }
}
