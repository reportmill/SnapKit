package snap.geom;

/**
 * An enum to represent a side of rectanglular area: Left, right, top, bottom.
 */
public enum Side {

    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    /**
     * Returns the Pos for given Side.
     */
    public Pos getPos()
    {
        return switch (this) {
            case LEFT -> Pos.CENTER_LEFT;
            case RIGHT -> Pos.CENTER_RIGHT;
            case TOP -> Pos.TOP_CENTER;
            case BOTTOM -> Pos.BOTTOM_CENTER;
        };
    }

    /**
     * Returns whether side is LEFT or RIGHT.
     */
    public boolean isLeftOrRight()  { return this == LEFT || this == RIGHT; }

    /**
     * Returns whether side is TOP or BOTTOM.
     */
    public boolean isTopOrBottom()  { return this == TOP || this == BOTTOM; }
}
