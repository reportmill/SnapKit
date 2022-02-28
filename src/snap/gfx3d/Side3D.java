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
}
