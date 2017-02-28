/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * This class provides a basic description for comparison and sorting with a simple key and order (ascending or
 * descending). You can create a new order like this:
 * <p><blockquote><pre>
 *   Sort titleFirst = new Sort("Title");
 *   Sort bestRevenueFirst = new Sort("Revenue", ORDER_DESCEND);
 * </pre></blockquote><p>
 * This class also provides useful static methods for comparison and sorting:
 * <p><blockquote><pre>
 *   Sort.sort(myList, "Title"); // Sort myList by its contents' getTitle method (alphabetically)
 *   Sort.sort(myList, bestRevenueFirst); // Sort myList by its contents' getRevenue method (largest first)
 *   List mySortList = new ArrayList();
 *   mySortList.add(bestRevenueFirst);
 *   mySortList.add(titleFirst);
 *   Sort.sort(myList, mySortList); // Sort myList by revenue and title
 * </blockquote></pre>
 */
public class Sort implements Comparator, Cloneable, JSONArchiver.GetKeys {

    // The key that is evaluated on objects to be sorted
    String   _key;
    
    // The sort order: ascending or descending
    int      _order = ORDER_ASCEND;

    // Constants for sort order
    public static final byte ORDER_SAME = 0;
    public static final byte ORDER_ASCEND = -1;
    public static final byte ORDER_DESCEND = 1;
    
    // A default Comparator
    public static Comparator COMPARATOR = new Comparator() {
        public int compare(Object obj1, Object obj2) { return Sort.Compare(obj1, obj2); }};

/**
 * Creates a plain sort with no key. Used for unarchival, shouldn't be called directly.
 */
public Sort() { }

/**
 * Creates a sort with the given key and ORDER_ASCEND.
 */
public Sort(String aKey)  { _key = aKey; }

/**
 * Creates a sort with the given key and order.
 */
public Sort(String aKey, int anOrder)  { _key = aKey; _order = anOrder; }

/**
 * Returns the key for this sort.
 */
public String getKey()  { return _key; }

/**
 * Sets the key for this sort.
 */
public void setKey(String aKey)  { _key = aKey; }

/**
 * Returns the order for this sort.
 */
public int getOrder()  { return _order; }

/**
 * Sets the order for this sort.
 */
public void setOrder(int anOrder)  { _order = anOrder; }

/**
 * Toggles the order for this sort between ORDER_ASCEND<->ORDER_DESCEND.
 */
public void toggleOrder()
{
    _order = _order==ORDER_ASCEND? ORDER_DESCEND : _order==ORDER_DESCEND? ORDER_ASCEND : ORDER_SAME;
}

/**
 * Returns how the two given objects compare with this sort.
 */
public int compare(Object obj1, Object obj2)
{
    // Get values for objects and sort key
    Object val1 = KeyChain.getValue(obj1, getKey());
    Object val2 = KeyChain.getValue(obj2, getKey());
    
    // Get standard compare result, if Sort.Order is descending, flip, return
    int compare = Sort.Compare(val1, val2);
    if(getOrder()==Sort.ORDER_DESCEND) compare = -compare;
    return compare;
}

/**
 * Compare two value objects (assumed to be String, Number, Boolean, Date, Comparable, etc.).
 */
public static int Compare(Object anObj1, Object anObj2)
{
    // Convert keychains to strings
    if(anObj1 instanceof KeyChain) anObj1 = anObj1.toString();
    if(anObj2 instanceof KeyChain) anObj2 = anObj2.toString();
    
    // Handle identity
    if(anObj1==anObj2) return ORDER_SAME;
    
    // Handle either null: make any existing object come first (opposite if other is String)
    if(anObj1==null) return anObj2 instanceof String? ORDER_DESCEND : ORDER_ASCEND;
    if(anObj2==null) return anObj1 instanceof String? ORDER_ASCEND : ORDER_DESCEND;
    
    // Handle String (ignore case)
    if(anObj1 instanceof String && anObj2 instanceof String) {
        int order = ((String)anObj1).compareToIgnoreCase((String)anObj2);
        return order<0? ORDER_ASCEND : order>0? ORDER_DESCEND : ORDER_SAME;
    }
    
    // Handle Number
    else if(anObj1 instanceof Number && anObj2 instanceof Number) {
        double value1 = ((Number)anObj1).doubleValue();
        double value2 = ((Number)anObj2).doubleValue();
        if(value1 < value2) return ORDER_ASCEND;
        else if(value1 > value2) return ORDER_DESCEND;
    }

    // Handle Date
    else if(anObj1 instanceof Date && anObj2 instanceof Date) {
        if(((Date)anObj1).before((Date)anObj2)) return ORDER_ASCEND;
        else if(((Date)anObj1).after((Date)anObj2)) return ORDER_DESCEND;
    }
    
    // Handle Boolean
    else if(anObj1 instanceof Boolean && anObj2 instanceof Boolean) {
        if(!(((Boolean)anObj1).booleanValue()) && (((Boolean)anObj2).booleanValue())) return ORDER_ASCEND;
        else if((((Boolean)anObj1).booleanValue()) && !(((Boolean)anObj2).booleanValue())) return ORDER_DESCEND;
    }
    
    // Handle Comparable objects
    else if(anObj1 instanceof Comparable)
        return ((Comparable)anObj1).compareTo(anObj2);
    
    // Handle everything else
    else if(!anObj1.equals(anObj2))
        return ORDER_ASCEND;

    // If nothing above applies, just return order same
    return ORDER_SAME;
}

/**
 * Returns the given list sorted by the given key.
 */
public static void sort(List aList, String aKey)  { Collections.sort(aList, new Sort(aKey)); }

/**
 * Returns the given list sorted by the given sort.
 */
public static void sort(List aList, Sort aSort)  { Collections.sort(aList, aSort); }

/**
 * Returns the given list sorted by the given key.
 */
public static void sort(List aList, String ... theKeys)
{
    List <Sort> sorts = new ArrayList(theKeys.length);
    for(String key : theKeys) sorts.add(new Sort(key));
    sort(aList, sorts);
}

/**
 * Returns the given list sorted by the given sort.
 */
public static void sort(List aList, Sort ... theSorts)  { sort(aList, Arrays.asList(theSorts)); }

/**
 * Returns the given list sorted by the given list of sorts.
 */
public static void sort(List aList, List aSortList)  { Collections.sort(aList, new SortsComparator(aSortList)); }

/**
 * Returns a new sorted list from given collection.
 */
public static <T extends Comparable<? super T>> List <T> sortedList(Collection <T> aCollection)
{
    List <T> list = new ArrayList(aCollection);
    Collections.sort(list);
    return list;
}

/**
 * Returns a new list from the given list sorted by the given key.
 */
public static <T> List <T> sortedList(Collection <T> aCollection, String aKey)
{
    List <T> list = new ArrayList(aCollection);
    sort(list, aKey);
    return list;
}

/**
 * Returns a new list from the given list sorted by the given key.
 */
public static <T> List <T> sortedList(Collection <T> aCollection, String ... theKeys)
{
    List <T> list = new ArrayList(aCollection);
    sort(list, theKeys);
    return list;
}

/**
 * A comparator that compares with a given list of sorts.
 */
private static class SortsComparator implements Comparator {

    // The list of sorts
    List <Sort>  _sorts;

    // Creates a new SortsComparator
    public SortsComparator(List aSortList)  { _sorts = aSortList; }

    // Compares two objects with sorts list
    public int compare(Object obj1, Object obj2)
    {
        // Iterate over sorts, compare and return first non-zero
        for(Sort sort : _sorts) {
            int compare = sort.compare(obj1, obj2);
            if(compare!=0)
                return compare;
        }
        return 0;
    }
}

/**
 * Standard clone implementation.
 */
public Object clone()
{
    try { return super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
}

/**
 * RMJSONArchiver GetKeys method.
 */
public List <String> getJSONKeys()  { return Arrays.asList("Key", "Order"); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other sort
    if(anObj==this) return true;
    if(!(anObj instanceof Sort)) return false;
    Sort other = (Sort)anObj;
    
    // Check Key, Order
    if(!SnapUtils.equals(other._key, _key)) return false;
    if(other._order!=_order) return false;
    return true; // Return true since all checks passed
}

/**
 * Returns a string representation of sort (just the sort key).
 */
public String toString()  { return _key; }

}