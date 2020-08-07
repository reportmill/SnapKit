package snap.util;
import java.util.List;

/**
 * An interface for views that are selectable.
 */
public interface Selectable<T> {

    /**
     * Returns the items for a given name or UI view.
     */
    default List<T> getItems() { return null; }

    /**
     * Sets the items for a given name or UI view.
     */
    default void setItems(List<T> theItems)  { }

    /**
     * Sets the items for a given name or UI view.
     */
    default void setItems(T... theItems)  { }

    /**
     * Returns the selected index for given name or UI view.
     */
    int getSelIndex();

    /**
     * Sets the selected index for given name or UI view.
     */
    void setSelIndex(int aValue);

    /**
     * Returns the selected item for given name or UI view.
     */
    default T getSelItem()
    {
        int ind = getSelIndex();
        List<T> items = getItems();
        return ind>=0 && items!=null ? items.get(ind) : null;
    }

    /**
     * Sets the selected item for given name or UI view.
     */
    default void setSelItem(T anItem)
    {
        List<T> items = getItems(); if (items==null) return;
        int ind = items.indexOf(anItem);
        setSelIndex(ind);
    }

    // Constants for properties
    String Items_Prop = "Items";
    String SelIndex_Prop = "SelIndex";
    String SelItem_Prop = "SelItem";
}
