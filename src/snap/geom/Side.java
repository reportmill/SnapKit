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
        switch (this)
        {
            case LEFT: return Pos.CENTER_LEFT;
            case RIGHT: return Pos.CENTER_RIGHT;
            case TOP: return Pos.TOP_CENTER;
            case BOTTOM: return Pos.BOTTOM_CENTER;
            default: throw new RuntimeException("Side: Unknown side: " + this);
        }
    }

    /**
     * Returns whether side is horizontal (LEFT or RIGHT).
     */
    public boolean isHorizontal()  { return this==LEFT || this==RIGHT; }

    /**
     * Returns whether side is vertical (TOP or BOTTOM).
     */
    public boolean isVertical()  { return this==TOP || this==BOTTOM; }
}
