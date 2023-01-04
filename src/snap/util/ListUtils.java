/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods for use with Java.util.List.
 */
public class ListUtils {

    /**
     * Returns the size of a list (accepts null list).
     */
    public static int size(List<?> aList)
    {
        return aList == null ? 0 : aList.size();
    }

    /**
     * Returns the object at the given index (returns null object for null list or invalid index).
     */
    public static <T> T get(List<T> aList, int anIndex)
    {
        return aList == null || anIndex < 0 || anIndex >= aList.size() ? null : aList.get(anIndex);
    }

    /**
     * Returns the last object in the given list.
     */
    public static <T> T getLast(List<T> aList)
    {
        return get(aList, size(aList) - 1);
    }

    /**
     * Returns whether list contains given object (accepts null list).
     */
    public static boolean contains(List<?> aList, Object anObj)
    {
        return aList != null && aList.contains(anObj);
    }

    /**
     * Returns whether list contains identical given object (accepts null list).
     */
    public static boolean containsId(List<?> aList, Object anObj)
    {
        return indexOfId(aList, anObj) >= 0;
    }

    /**
     * Returns index of identical given object in given list.
     */
    public static int indexOfId(List<?> aList, Object anObj)
    {
        // Iterate over list objects and return index if identical exists
        for (int i = 0, iMax = size(aList); i < iMax; i++)
            if (anObj == aList.get(i))
                return i;

        // Return -1 if identical doesn't exist
        return -1;
    }

    /**
     * Adds an object to the given list and returns list (creates list if missing).
     */
    public static <T> List<T> add(List<T> aList, T anObj)
    {
        // If list is null, create list
        if (aList == null) aList = new Vector<>();

        // Add object and return
        aList.add(anObj);
        return aList;
    }

    /**
     * Adds an object to the given list if object is absent (creates list if missing).
     */
    public static <T> List<T> addUnique(List<T> aList, T anObj)
    {
        return contains(aList, anObj) ? aList : add(aList, anObj);
    }

    /**
     * Adds an object to the given list if identical object is missing (creates list if missing).
     */
    public static <T> List<T> addUniqueId(List<T> aList, T anObj)
    {
        return containsId(aList, anObj) ? aList : add(aList, anObj);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List<T> addAllUnique(List<T> aList, T... theObjects)
    {
        return addAllUnique(aList, aList != null ? aList.size() : 0, theObjects);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List<T> addAllUnique(List<T> aList, int anIndex, T... theObjects)
    {
        // If list is null, create it
        if (aList == null) aList = new ArrayList<>();

        // Add objects unique
        for (T object : theObjects) {
            if (!aList.contains(object))
                aList.add(anIndex++, object);
        }

        // Return list
        return aList;
    }

    /**
     * Removes the last object from given list.
     */
    public static <T> T removeLast(List<T> aList)
    {
        return aList.remove(aList.size() - 1);
    }

    /**
     * Removes the object identical to the given object from list.
     */
    public static int removeId(List<?> aList, Object anObj)
    {
        int index = indexOfId(aList, anObj);
        if (index >= 0)
            aList.remove(index);
        return index;
    }

    /**
     * Reverses the items in the given list.
     */
    public static <T> List<T> reverse(List<T> aList)
    {
        for (int i = 0, max = aList.size(), iMax = max / 2; i < iMax; i++) {
            int oppositeIndex = max - i - 1;
            T original = aList.set(i, aList.get(oppositeIndex));
            aList.set(oppositeIndex, original);
        }

        return aList;
    }

    /**
     * Returns a string by concatenating strings in given list separated by given string.
     */
    public static String joinStrings(List aList, String aString)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, iMax = aList.size(); i < iMax; i++)
            sb.append(i == 0 ? "" : aString).append(aList.get(i));
        return sb.toString();
    }

    /**
     * Adds object from list 2 to list 1, unless they are already present (then removes them).
     */
    public static void xor(List list1, List list2)
    {
        int size = list1.size();

        // Add objects from l2 that aren't in l
        for (Object item : list2)
            if (!list1.contains(item))
                list1.add(item);

        // Remove elements in l that are in l2
        for (int i = size - 1; i >= 0; i--)
            if (list2.contains(list1.get(i)))
                list1.remove(i);
    }

    /**
     * Moves the object at the given index to the front of the list.
     */
    public static void moveToFront(List<?> aList, int anIndex)
    {
        if (anIndex > 0)
            moveToFront(aList, aList.get(anIndex));
    }

    /**
     * Move the given object to the front of the list.
     */
    public static void moveToFront(List aList, Object anObj)
    {
        if (anObj == null) return;
        aList.remove(anObj);
        aList.add(0, anObj);
    }

    /**
     * Returns whether lists have same objects in them.
     */
    public static boolean equalsId(List<?> aList1, List<?> aList2)
    {
        // If lists have different sizes, return false
        int size1 = aList1 != null ? aList1.size() : 0;
        int size2 = aList2 != null ? aList2.size() : 0;

        // Check list sizes
        if (size1 != size2) return false;

        // Iterate over objects
        for (int i = 0; i < size1; i++)
            if (aList1.get(i) != aList2.get(i))
                return false;

        // Return true since checks passed
        return true;
    }

    /**
     * Returns a copy of the given list.
     */
    public static <T> List<T> clone(List<T> aList)
    {
        if (aList instanceof ArrayList)
            return (List<T>) ((ArrayList<T>) aList).clone();
        if (aList instanceof Vector)
            return (List<T>) ((Vector<T>) aList).clone();
        return aList != null ? new ArrayList<>(aList) : null;
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    public static <T> List<T> getFiltered(Collection<T> aList, Predicate<? super T> pred)
    {
        return aList.stream().filter(pred).collect(Collectors.toList());
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    public static <T, R> List<R> getFilteredForClass(Collection<T> aList, Class<R> aClass)
    {
        return (List<R>) aList.stream().filter(item -> aClass.isInstance(item)).collect(Collectors.toList());
    }

    /**
     * Returns the first item in collection that matches given predicate (or null).
     */
    public static <T> T findMatch(Collection<T> aList, Predicate<? super T> aPred)
    {
        for (T item : aList)
            if (aPred.test(item))
                return item;
        return null;
    }
}