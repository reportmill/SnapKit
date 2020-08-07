package snap.util;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a selection for a table.
 */
public class TableSel implements Cloneable {

    // The Anchor X/Y
    private int _anchX, _anchY;

    // The Lead X/Y
    private int _leadX, _leadY;

    // Whether this is blacklist rect
    private boolean  _isBlacklist;

    // The next rect
    private TableSel _next;

    // Shared empty selection
    public static final TableSel EMPTY = new TableSel(-1, -1, -1, -1);

    // Shared empty indexes
    private static final int[] EMPTY_INDEXES = new int[0];

    /**
     * Constructor.
     */
    public TableSel(int anchX, int anchY, int leadX, int leadY)
    {
        _anchX = anchX; _anchY = anchY;
        _leadX = leadX; _leadY = leadY;
    }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isEmpty()  { return _anchX<0 || _anchY<0 || _leadX<0 || _leadY<0; }

    /**
     * Returns the anchor X.
     */
    public int getAnchorX()  { return _anchX; }

    /**
     * Returns the anchor Y.
     */
    public int getAnchorY()  { return _anchY; }

    /**
     * Returns the lead X.
     */
    public int getLeadX()  { return _leadX; }

    /**
     * Returns the lead Y.
     */
    public int getLeadY()  { return _leadY; }

    /**
     * Return the min X.
     */
    public int getMinX()  { return Math.min(_anchX, _leadX); }

    /**
     * Returns the min Y.
     */
    public int getMinY()  { return Math.min(_anchY, _leadY); }

    /**
     * Return the max X.
     */
    public int getMaxX()  { return Math.max(_anchX, _leadX); }

    /**
     * Returns the max Y.
     */
    public int getMaxY()  { return Math.max(_anchY, _leadY); }

    /**
     * Returns whether given X/Y are selected.
     */
    public boolean isSel(int aX, int aY)
    {
        // If Next SelRect and X/Y inside any, return it's value
        if (_next!=null && _next.isInsideAny(aX, aY))
            return _next.isSel(aX, aY);

        // If inside and not blacklist
        boolean inside = aX>=getMinX() && aX<=getMaxX() && aY>=getMinY() && aY<=getMaxY();
        return inside && !_isBlacklist;
    }

    /**
     * Returns the selection encompassing all.
     */
    public TableSel getSelAll()
    {
        // If not deep, just return this
        if (_next==null)
            return this;

        int minX = getMinX();
        int minY = getMinY();
        int maxX = getMaxX();
        int maxY = getMaxY();
        TableSel next = _next;
        while (next!=null) {
            if (!next._isBlacklist) {
                minX = Math.min(minX, next.getMinX());
                minY = Math.min(minY, next.getMinY());
                maxX = Math.max(maxX, next.getMaxX());
                maxY = Math.max(maxY, next.getMaxY());
            }
            next = next._next;
        }
        return new TableSel(minX, minY, maxX, maxY);
    }

    /**
     * Returns the tail selection.
     */
    public TableSel getTail()
    {
        TableSel tail = this;
        while(tail._next!=null) tail = tail._next;
        return tail;
    }

    /**
     * Returns the X indexes.
     */
    public int[] getIndexesYForX(int aX)
    {
        TableSel selAll = getSelAll();
        int minY = selAll.getMinY();
        int maxY = selAll.getMaxY();
        int len = maxY - minY + 1;
        int len2 = 0;
        int indexes[] = new int[len];
        for (int i=0; i<len; i++)
            if (isSel(aX, minY + i))
                indexes[len2++] = minY + i;
        return Arrays.copyOf(indexes, len2);
    }

    /**
     * Returns whether given X/Y are inside.
     */
    private boolean isInside(int aX, int aY)
    {
        return aX>=getMinX() && aX<=getMaxX() && aY>=getMinY() && aY<=getMaxY();
    }

    /**
     * Returns whether given X/Y are inside this rect or any child.
     */
    private boolean isInsideAny(int aX, int aY)
    {
        return isInside(aX, aY) || _next!=null && _next.isInsideAny(aX, aY);
    }

    /**
     * Returns whether given X is 'before' Anchor X (on the opposite side of lead).
     */
    private boolean isBeforeAnchorX(int aX)
    {
        return _anchX<=_leadX && aX<_anchX || _anchX>=_leadX && aX>_anchX;
    }

    /**
     * Returns whether given Y is 'before' Anchor Y (on the opposite side of lead).
     */
    private boolean isBeforeAnchorY(int aY)
    {
        return _anchY<=_leadY && aY<_anchY || _anchY>=_leadY && aY>_anchY;
    }

    /**
     * Returns a copy of this SelRect resulting from 'Shift-adding' new selection (shift key is down).
     */
    public TableSel copyForShiftAdd(int anchX, int anchY, int leadX, int leadY)
    {
        // Do basic clone
        TableSel clone = new TableSel(_anchX, _anchY, _leadX, _leadY);

        // If next SelRect, just copy next
        if (_next!=null) {
            clone._next = _next.copyForShiftAdd(anchX, anchY, leadX, leadY);
        }

        // Otherwise, update clone anch/lead x/y
        else {
            clone._leadX = leadX;
            if (isBeforeAnchorX(anchX))
                clone._anchX = _leadX;
            clone._leadY = leadY;
            if (isBeforeAnchorY(anchY))
                clone._anchY = _leadY;
        }

        // Return clone
        return clone;
    }

    /**
     * Returns a copy of this SelRect resulting from 'Meta-adding' new selection (short-cut key is down).
     */
    public TableSel copyForMetaAdd(int anchX, int anchY, int leadX, int leadY)
    {
        // Create new tail for new anchor/lead (set Blacklist if new anchor hits selected cell)
        TableSel newTail = new TableSel(anchX, anchY, leadX, leadY);
        newTail._isBlacklist = isSel(anchX, anchY);

        // Get current tail and add new tail
        TableSel clone = clone();
        TableSel tail = clone; while (tail._next!=null) tail = tail._next;
        tail._next = newTail;
        return clone;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return Objects.hash(_anchX, _anchY, _leadX, _leadY, _isBlacklist, _next);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableSel tableSel = (TableSel) o;
        return _anchX == tableSel._anchX && _anchY == tableSel._anchY &&
                _leadX == tableSel._leadX && _leadY == tableSel._leadY &&
                _isBlacklist == tableSel._isBlacklist &&
                Objects.equals(_next, tableSel._next);
    }

    /**
     * Standard clone implementation.
     */
    protected TableSel clone()
    {
        TableSel clone;
        try { clone = (TableSel)super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        if (_next!=null)
            clone._next = _next.clone();
        return clone;
    }

    /**
     * Standard toString implemetation.
     */
    public String toString()
    {
        return "TableSel {" + "AnchX=" + _anchX + ", AnchY=" + _anchY + ", LeadX=" + _leadX + ", LeadY=" + _leadY +
            ", isBlacklist=" + _isBlacklist + ", Next=" + _next + '}';
    }
}
