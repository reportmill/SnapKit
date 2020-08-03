package snap.util;
import java.util.Arrays;

/**
 * Represents a selection for a List.
 */
public class ListSel implements Cloneable {

    // The Anchor
    private int  _anch;

    // The Lead
    private int  _lead;

    // Whether this is blacklist rect
    private boolean  _isBlacklist;

    // The next rect
    private ListSel  _next;

    // Cached array of selected indexes
    private int  _indexes[];

    // Cached ListSel representing entire range
    private ListSel  _selAll;

    // Shared empty selection
    public static final ListSel EMPTY = new ListSel(-1, -1);

    // Shared empty indexes
    private static final int[] EMPTY_INDEXES = new int[0];

    /**
     * Constructor.
     */
    public ListSel(int anch, int lead)
    {
        _anch = anch;
        _lead = lead;
    }

    /**
     * Returns the anchor.
     */
    public int getAnchor()  { return _anch; }

    /**
     * Returns the lead.
     */
    public int getLead()  { return _lead; }

    /**
     * Return the min.
     */
    public int getMin()  { return Math.min(_anch, _lead); }

    /**
     * Return the max.
     */
    public int getMax()  { return Math.max(_anch, _lead); }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isEmpty()  { return _anch<0 || _lead<0; }

    /**
     * Returns whether given index is selected.
     */
    public boolean isSel(int anIndex)
    {
        // If Next SelRect and X/Y inside any, return it's value
        if (_next!=null && _next.isInsideAny(anIndex))
            return _next.isSel(anIndex);

        // If inside and not blacklist
        boolean inside = anIndex>=getMin() && anIndex<=getMax();
        return inside && !_isBlacklist;
    }

    /**
     * Returns the selection encompassing all.
     */
    public ListSel getSelAll()
    {
        // If already set, just return
        if (_selAll!=null) return _selAll;

        // If not deep, just return this
        if (_next==null)
            return _selAll = this;

        int min = getMin();
        int max = getMax();
        ListSel next = _next;
        while (next!=null) {
            if (!next._isBlacklist) {
                min = Math.min(min, next.getMin());
                max = Math.max(max, next.getMax());
            }
            next = next._next;
        }
        return _selAll = new ListSel(min, max);
    }

    /**
     * Returns the tail selection.
     */
    public ListSel getTail()
    {
        ListSel tail = this;
        while(tail._next!=null) tail = tail._next;
        return tail;
    }

    /**
     * Returns the selected indexes.
     */
    public int[] getIndexes()
    {
        // If already set, just return
        if (_indexes!=null) return _indexes;

        // If empty, just return no indexes
        if (isEmpty())
            return _indexes = new int[0];

        // Get array of indexes
        ListSel selAll = getSelAll();
        int min = selAll.getMin();
        int max = selAll.getMax();
        int len = max - min + 1;
        int len2 = 0;
        int indexes[] = new int[len];
        for (int i=0; i<len; i++)
            if (isSel(min + i))
                indexes[len2++] = min + i;
        if (len2!=len)
            indexes = Arrays.copyOf(indexes, len2);

        // Set/return indexes
        return _indexes = indexes;
    }

    /**
     * Returns whether given X/Y are inside.
     */
    private boolean isInside(int anIndex)
    {
        return anIndex>=getMin() && anIndex<=getMax();
    }

    /**
     * Returns whether given X/Y are inside this rect or any child.
     */
    private boolean isInsideAny(int anIndex)
    {
        return isInside(anIndex) || _next!=null && _next.isInsideAny(anIndex);
    }

    /**
     * Returns whether given index is 'before' Anchor (on the opposite side of lead).
     */
    private boolean isBeforeAnchor(int anIndex)
    {
        return _anch<=_lead && anIndex<_anch || _anch>=_lead && anIndex>_anch;
    }

    /**
     * Returns a copy of this ListSel resulting from 'Shift-adding' new selection (shift key is down).
     */
    public ListSel copyForShiftAdd(int anch, int lead)
    {
        // Handle empty case
        if (isEmpty())
            return new ListSel(anch, lead);

        // Do basic clone
        ListSel clone = new ListSel(_anch, _lead);

        // If next SelRect, just copy next
        if (_next!=null) {
            clone._next = _next.copyForShiftAdd(anch, lead);
        }

        // Otherwise, update clone anch/lead x/y
        else {
            clone._lead = lead;
            if (isBeforeAnchor(anch))
                clone._anch = _lead;
        }

        // Return clone
        return clone;
    }

    /**
     * Returns a copy of this ListSel resulting from 'Meta-adding' new selection (short-cut key is down).
     */
    public ListSel copyForMetaAdd(int anch, int lead)
    {
        // Handle empty case
        if (isEmpty())
            return new ListSel(anch, lead);

        // Create new tail for new anchor/lead (set Blacklist if new anchor hits selected cell)
        ListSel newTail = new ListSel(anch, lead);
        newTail._isBlacklist = isSel(anch);

        // Get current tail and add new tail
        ListSel clone = clone();
        ListSel tail = clone; while (tail._next!=null) tail = tail._next;
        tail._next = newTail;

        // Clear indexes and return
        clone._indexes = null;
        clone._selAll = null;
        return clone;
    }

    /**
     * Returns a ListSel with anything above max trimmed.
     */
    public ListSel copyForMaxSize(int aSize)
    {
        int max = getSelAll().getMax();
        if (max<aSize)
            return this;

        System.out.println("ListSel.copyForMaxSize: Trimming to size " + aSize);
        if (aSize==0)
            return EMPTY;
        return copyForMetaAdd(max, aSize-1);
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public ListSel clone()
    {
        ListSel clone;
        try { clone = (ListSel)super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        if (_next!=null)
            clone._next = _next.clone();
        return clone;
    }

    /**
     * Returns a ListSel for array of indexes.
     */
    public static ListSel getSelForIndexArray(int theIndexes[])
    {
        // Get indexes sorted
        int indexes[] = Arrays.copyOf(theIndexes, theIndexes.length);
        Arrays.sort(indexes);

        // Iterate over intervals
        int anch = indexes.length>0 ? indexes[0] : -1;
        int lead = anch;
        ListSel sel = ListSel.EMPTY;
        for (int i=1; i<indexes.length; i++) {
            int ind = indexes[i]; if (ind<0) continue;
            if (ind!=lead+1) {
                sel = sel.copyForMetaAdd(anch, lead);
                anch = lead = ind;
            }
            else lead = ind;
        }

        // Add final interval
        sel = sel.copyForMetaAdd(anch, lead);
        return sel;
    }

    /**
     * Returns the changed indexes for two ListSels.
     */
    public static int[] getChangedIndexes(ListSel aSel1, ListSel aSel2)
    {
        if (aSel1==null || aSel1.isEmpty())
            return aSel2!=null ? aSel2.getIndexes() : EMPTY_INDEXES;
        if (aSel2==null || aSel2.isEmpty())
            return aSel1!=null ? aSel1.getIndexes() : EMPTY_INDEXES;

        int ind1[] = aSel1.getIndexes();
        int ind2[] = aSel2.getIndexes();
        int len1 = ind1.length;
        int len2 = ind2.length;
        int changed[] = new int[len1+len2];
        int len3 = 0;

        for (int i : ind1)
            if (Arrays.binarySearch(ind2, i)<0)
                changed[len3++] = i;
        for (int i : ind2)
            if (Arrays.binarySearch(ind1, i)<0 && Arrays.binarySearch(changed, i)<0)
                changed[len3++] = i;
        return Arrays.copyOf(changed, len3);
    }
}
