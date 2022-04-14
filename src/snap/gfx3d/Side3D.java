/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;

/**
 * An enum to represent a side of rectanglular area: Left, right, top, bottom.
 */
public enum Side3D {

    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    /**
     * Returns whether side is FRONT or BACK.
     */
    public boolean isFrontOrBack()  { return this == FRONT || this == BACK; }

    /**
     * Returns whether side is LEFT or RIGHT.
     */
    public boolean isLeftOrRight()  { return this == LEFT || this == RIGHT; }

    /**
     * Returns whether side is TOP or BOTTOM.
     */
    public boolean isTopOrBottom()  { return this == TOP || this == BOTTOM; }

    // Constants for side normals
    private static final Vector3D FRONT_NORMAL = new Vector3D(0, 0, -1);
    private static final Vector3D BACK_NORMAL = new Vector3D(0, 0, 1);
    private static final Vector3D LEFT_NORMAL = new Vector3D(1, 0, 0);
    private static final Vector3D RIGHT_NORMAL = new Vector3D(-1, 0, 0);
    private static final Vector3D TOP_NORMAL = new Vector3D(0, -1, 0);
    private static final Vector3D BOTTOM_NORMAL = new Vector3D(0, 1, 0);

    /**
     * Returns the side normal for a given side.
     */
    public Vector3D getNormalInward()
    {
        switch (this) {
            case FRONT: return FRONT_NORMAL;
            case BACK: return BACK_NORMAL;
            case LEFT: return LEFT_NORMAL;
            case RIGHT: return RIGHT_NORMAL;
            case TOP: return TOP_NORMAL;
            case BOTTOM: return BOTTOM_NORMAL;
            default: throw new RuntimeException("Side3D.getNormalInward: Unknown side: " + this);
        }
    }
}
