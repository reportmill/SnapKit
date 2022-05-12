/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Pos;
import snap.util.ArrayUtils;

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

    // An array of corners for this side
    private Corner[]  _corners;

    // An array of edges for this side
    private Edge[]  _edges;

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

    /**
     * Returns the corners.
     */
    public Corner[] getCorners()
    {
        if (_corners != null) return _corners;
        return _corners = getCornersImpl();
    }

    /**
     * Returns the corners.
     */
    private Corner[] getCornersImpl()
    {
        switch (this) {
            case FRONT: return new Corner[] { Corner.FRONT_BOTTOM_LEFT, Corner.FRONT_BOTTOM_RIGHT, Corner.FRONT_TOP_RIGHT, Corner.FRONT_TOP_LEFT };
            case BACK: return new Corner[] { Corner.BACK_BOTTOM_RIGHT, Corner.BACK_BOTTOM_LEFT, Corner.BACK_TOP_LEFT, Corner.BACK_TOP_RIGHT };
            case LEFT: return new Corner[] { Corner.BACK_BOTTOM_LEFT, Corner.FRONT_BOTTOM_LEFT, Corner.FRONT_TOP_LEFT, Corner.BACK_TOP_LEFT };
            case RIGHT: return new Corner[] { Corner.FRONT_BOTTOM_RIGHT, Corner.BACK_BOTTOM_RIGHT, Corner.BACK_TOP_RIGHT, Corner.FRONT_TOP_RIGHT };
            case TOP: return new Corner[] { Corner.FRONT_TOP_LEFT, Corner.FRONT_TOP_RIGHT, Corner.BACK_TOP_RIGHT, Corner.BACK_TOP_LEFT };
            case BOTTOM: return new Corner[] { Corner.BACK_BOTTOM_LEFT, Corner.BACK_BOTTOM_RIGHT, Corner.FRONT_BOTTOM_RIGHT, Corner.FRONT_BOTTOM_LEFT };
            default: throw new RuntimeException("Side3D.getCornersImpl: Unknown side: " + this);
        }
    }

    // Constant for Side corner order.
    private static final Pos[]  CORNER_POS_ORDER = { Pos.BOTTOM_LEFT, Pos.BOTTOM_RIGHT, Pos.TOP_RIGHT, Pos.TOP_LEFT };

    /**
     * Returns the Corner for given Pos.
     */
    public Corner getCornerForPos(Pos aPos)
    {
        int index = ArrayUtils.indexOfId(CORNER_POS_ORDER, aPos);
        Corner[] corners = getCorners();
        return index >= 0 ? corners[index] : null;
    }

    /**
     * Returns the Corner for given Pos.
     */
    public Pos getPosForCorner(Corner aCorner)
    {
        Corner[] corners = getCorners();
        int index = ArrayUtils.indexOfId(corners, aCorner);
        return index >= 0 ? CORNER_POS_ORDER[index] : null;
    }

    /**
     * Returns the edges.
     */
    public Edge[] getEdges()
    {
        if (_edges != null) return _edges;
        return _edges = getEdgesImpl();
    }

    /**
     * Returns the edges.
     */
    private Edge[] getEdgesImpl()
    {
        switch (this) {
            case FRONT: return new Edge[] { Edge.FRONT_BOTTOM, Edge.FRONT_RIGHT, Edge.FRONT_TOP, Edge.FRONT_LEFT };
            case BACK: return new Edge[] { Edge.BACK_BOTTOM, Edge.BACK_LEFT, Edge.BACK_TOP, Edge.BACK_RIGHT };
            case LEFT: return new Edge[] { Edge.LEFT_BOTTOM, Edge.FRONT_LEFT, Edge.LEFT_TOP, Edge.BACK_LEFT };
            case RIGHT: return new Edge[] { Edge.RIGHT_BOTTOM, Edge.BACK_RIGHT, Edge.RIGHT_TOP, Edge.FRONT_RIGHT };
            case TOP: return new Edge[] { Edge.FRONT_TOP, Edge.RIGHT_TOP, Edge.BACK_TOP, Edge.LEFT_TOP };
            case BOTTOM: return new Edge[] { Edge.BACK_BOTTOM, Edge.RIGHT_BOTTOM, Edge.FRONT_BOTTOM, Edge.LEFT_BOTTOM };
            default: throw new RuntimeException("Side3D.getCornersImpl: Unknown side: " + this);
        }
    }

    // Constant for Side edge order.
    private static final Pos[]  EDGE_POS_ORDER = { Pos.BOTTOM_CENTER, Pos.CENTER_RIGHT, Pos.TOP_CENTER, Pos.CENTER_LEFT };

    /**
     * Returns the Edge for given Pos.
     */
    public Edge getEdgeForPos(Pos aPos)
    {
        int index = ArrayUtils.indexOfId(EDGE_POS_ORDER, aPos);
        Edge[] edges = getEdges();
        return index >= 0 ? edges[index] : null;
    }

    /**
     * Returns the Edge for given Pos.
     */
    public Pos getPosForEdge(Edge anEdge)
    {
        Edge[] edges = getEdges();
        int index = ArrayUtils.indexOfId(edges, anEdge);
        return index >= 0 ? EDGE_POS_ORDER[index] : null;
    }

    /**
     * This class describes the corners of a standard cube.
     */
    public enum Corner {

        FRONT_TOP_LEFT(FRONT, TOP, LEFT),

        FRONT_TOP_RIGHT(FRONT, TOP, RIGHT),

        FRONT_BOTTOM_LEFT(FRONT, BOTTOM, LEFT),

        FRONT_BOTTOM_RIGHT(FRONT, BOTTOM, RIGHT),

        BACK_TOP_LEFT(BACK, TOP, LEFT),

        BACK_TOP_RIGHT(BACK, TOP, RIGHT),

        BACK_BOTTOM_LEFT(BACK, BOTTOM, LEFT),

        BACK_BOTTOM_RIGHT(BACK, BOTTOM, RIGHT);

        // The sides in this corner
        private Side3D[]  _sides;

        /**
         * Constructor.
         */
        private Corner(Side3D ... sides)
        {
            _sides = sides;
        }

        /**
         * Returns the sides associated with this corner.
         */
        public Side3D[] getSides()  { return _sides; }
    }

    /**
     * This class describes the edges of the standard cube.
     */
    public enum Edge {

        FRONT_TOP(FRONT, TOP),

        FRONT_RIGHT(FRONT, RIGHT),

        FRONT_BOTTOM(FRONT, BOTTOM),

        FRONT_LEFT(FRONT, LEFT),

        BACK_TOP(BACK, TOP),

        BACK_RIGHT(BACK, RIGHT),

        BACK_BOTTOM(BACK, BOTTOM),

        BACK_LEFT(BACK, LEFT),

        LEFT_TOP(LEFT, TOP),

        LEFT_BOTTOM(LEFT, BOTTOM),

        RIGHT_TOP(RIGHT, TOP),

        RIGHT_BOTTOM(RIGHT, BOTTOM);

        // The sides in this corner
        private Side3D[]  _sides;

        /**
         * Constructor.
         */
        private Edge(Side3D ... sides)
        {
            _sides = sides;
        }

        /**
         * Returns the sides associated with this edge.
         */
        public Side3D[] getSides()  { return _sides; }
    }
}
