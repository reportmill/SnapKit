package snap.util;
import java.util.Collections;
import java.util.List;

/**
 * An interface for views that are selectable.
 */
public interface Selectable<T> {

    // Constants for properties
    String Items_Prop = "Items";
    String SelIndex_Prop = "SelIndex";
    String SelItem_Prop = "SelItem";

    /**
     * Sets the items for a given name or UI view.
     */
    default void setItems(T[] theItems)  { }

    /**
     * Returns the items for a given name or UI view.
     */
    default List<T> getItemsList() { return null; }

    /**
     * Sets the items for a given name or UI view.
     */
    default void setItemsList(List<T> theItems)  { }

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
        List<T> items = getItemsList();
        return ind>=0 && items!=null ? items.get(ind) : null;
    }

    /**
     * Sets the selected item for given name or UI view.
     */
    default void setSelItem(T anItem)
    {
        List<T> items = getItemsList(); if (items==null) return;
        int ind = items.indexOf(anItem);
        setSelIndex(ind);
    }

    /**
     * Sets items in given selectable
     */
    static void setItems(Selectable<?> aSelectable, Object theItems)
    {
        // Handle null
        if (theItems == null)
            aSelectable.setItemsList(Collections.emptyList());

        // Handle List
        else if (theItems instanceof List) {
            Selectable<Object> selectable = (Selectable<Object>) aSelectable;
            List<Object> itemsList = (List<Object>) theItems;
            selectable.setItemsList(itemsList);
        }

        // Handle Array
        else if (theItems.getClass().isArray()) {
            Selectable<Object> selectable = (Selectable<Object>) aSelectable;
            Object[] items = (Object[]) theItems;
            selectable.setItems(items);
        }
    }
}
