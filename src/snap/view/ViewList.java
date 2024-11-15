package snap.view;
import java.lang.reflect.Array;
import java.util.*;

import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.util.ArrayUtils;

/**
 * A class to manage a list of views.
 */
public class ViewList {

    // The views array
    private View[] _views = EMPTY_VIEWS_ARRAY;
    
    // The array of managed views (usually just the same as above)
    protected View[] _managed = EMPTY_VIEWS_ARRAY;
    
    // Shared empty view array
    private static View[] EMPTY_VIEWS_ARRAY = new View[0];

    /**
     * Constructor.
     */
    public ViewList()
    {
        super();
    }

    /**
     * Returns the number of views in this list.
     */
    public int size()  { return _views.length; }

    /**
     * Returns the view at the given index.
     */
    public View get(int anIndex)  { return _views[anIndex]; }

    /**
     * Returns the contained views as an array.
     */
    public View[] getAll()  { return _views; }

    /**
     * Adds the given view to end of this list.
     */
    protected void add(View aView)
    {
        add(aView, size());
    }

    /**
     * Adds the given view to this list at given index.
     */
    protected void add(View aView, int anIndex)
    {
        _views = ArrayUtils.add(_views, aView, anIndex);
        _managed = null;
    }

    /**
     * Remove's the view at the given index from this list.
     */
    protected View remove(int anIndex)
    {
        View child = _views[anIndex];
        _views = ArrayUtils.remove(_views, anIndex);
        _managed = null;
        return child;
    }

    /**
     * Removes the given view from this list.
     */
    protected int remove(View aView)
    {
        int index = indexOf(aView);
        if (index >= 0)
            remove(index);
        return index;
    }

    /**
     * Returns the index of the given child in this node's children list.
     */
    public int indexOf(View aView)  { return ArrayUtils.indexOfId(_views, aView); }

    /**
     * Returns the last view of this list.
     */
    public View getFirst()  { return size() > 0 ? get(0) : null; }

    /**
     * Returns the last view of this list.
     */
    public View getLast()  { return size() > 0 ? get(size()-1) : null; }

    /**
     * Returns the view at given point X/Y.
     */
    public View getViewAtXY(double aX, double aY)
    {
        // Get children
        View[] children = getAll();

        // Iterate over Children
        for (int i = children.length-1; i >= 0; i--) {
            View child = children[i];
            if (!child.isPickableVisible())
                continue;
            Point pointInChild = child.parentToLocal(aX, aY);
            if (child.contains(pointInChild.x,pointInChild.y))
                return child;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the first view of given class (optional) hit by given shape, excluding given view (optional).
     */
    public <T extends View> T getViewIntersectingShape(Shape aShape, Class <T> aClass, View aView)
    {
        // Get Children
        View[] children = getAll();

        // Iterate over children
        for (int i = children.length-1; i >= 0; i--) {
            View child = children[i];
            if (child == aView || !child.isPickableVisible())
                continue;
            if (aClass != null && !aClass.isInstance(child))
                continue;
            Shape shapeInChild = child.parentToLocal(aShape);
            if (child.intersectsShape(shapeInChild))
                return (T) child;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the views of given class (optional) hit by given shape.
     */
    public <T extends View> T[] getViewsIntersectingShape(Shape aShape, Class <T> aClass)
    {
        // Get Children
        View[] children = getAll();
        List <T> hit = Collections.EMPTY_LIST;

        // Iterate over children
        for (int i = children.length-1; i >= 0; i--) {
            View child = children[i];
            if (!child.isPickableVisible())
                continue;
            if (aClass != null && !aClass.isInstance(child))
                continue;
            Shape shapeInChild = child.parentToLocal(aShape);
            if (child.intersectsShape(shapeInChild)) {
                if (hit == Collections.EMPTY_LIST) hit = new ArrayList<>();
                hit.add((T)child);
            }
        }

        // Return array
        T[] array = (T[]) Array.newInstance(aClass, hit.size());
        return hit.toArray(array);
    }

    /**
     * Returns the first view of given class (optional) intersecting given view.
     */
    public <T extends View> T getHitView(View aView, Class <T> aClass, double anInset)
    {
        Rect bounds = aView.getBoundsLocal();
        if (anInset != 0)
            bounds.inset(anInset);
        Shape boundsInParent = aView.localToParent(bounds);
        return getViewIntersectingShape(boundsInParent, aClass, aView);
    }

    /**
     * Returns the managed children.
     */
    public View[] getManaged()
    {
        // If already set, just return
        if (_managed != null) return _managed;

        // Get ChildCount, ManagedCount
        int childCount = size();
        int managedCount = 0;
        for (View child : getAll())
            if (child.isManagedVisible())
                managedCount++;

        // If same, just return children
        if (managedCount == childCount)
            return _managed = _views;

        // Get managed array
        View[] managed = new View[managedCount];
        for (int i = 0, j = 0; i < childCount; i++) {
            View child = get(i);
            if (child.isManagedVisible())
                managed[j++] = child;
        }

        // Set, return
        return _managed = managed;
    }
}