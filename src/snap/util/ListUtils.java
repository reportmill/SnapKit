/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static <T> void addUnique(List<T> aList, T anObj)
    {
        if (!contains(aList, anObj))
            add(aList, anObj);
    }

    /**
     * Adds an object to the given list if identical object is missing (creates list if missing).
     */
    public static <T> void addUniqueId(List<T> aList, T anObj)
    {
        if (!containsId(aList, anObj))
            add(aList, anObj);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> void addAllUnique(List<T> aList, T... theObjects)
    {
        addAllUnique(aList, aList != null ? aList.size() : 0, theObjects);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> void addAllUnique(List<T> aList, int anIndex, T... theObjects)
    {
        // If list is null, create it
        if (aList == null) aList = new ArrayList<>();

        // Add objects unique
        for (T object : theObjects) {
            if (!aList.contains(object))
                aList.add(anIndex++, object);
        }
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
    public static String joinStrings(List<?> aList, String aString)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, iMax = aList.size(); i < iMax; i++)
            sb.append(i == 0 ? "" : aString).append(aList.get(i));
        return sb.toString();
    }

    /**
     * Adds object from list 2 to list 1, unless they are already present (then removes them).
     */
    public static <T> void xor(List<T> list1, List<T> list2)
    {
        int size = list1.size();

        // Add objects from l2 that aren't in l
        for (T item : list2)
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
    public static <T> void moveToFront(List<T> aList, int anIndex)
    {
        if (anIndex > 0)
            moveToFront(aList, aList.get(anIndex));
    }

    /**
     * Move the given object to the front of the list.
     */
    public static <T> void moveToFront(List<T> aList, T anObj)
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
     * Returns a filtered array for given original and Predicate.
     */
    public static <T> List<T> filter(Collection<T> aList, Predicate<? super T> pred)
    {
        return aList.stream().filter(pred).collect(Collectors.toList());
    }

    /**
     * Returns a mapped array for given original and Function.
     */
    public static <T,R> List<R> map(Collection<T> aList, Function<? super T, ? extends R> aFunction)
    {
        Stream<R> filteredStream = aList.stream().map(aFunction);
        return filteredStream.collect(Collectors.toList());
    }

    /**
     * Returns a mapped array for given original and Function.
     */
    public static <T,R> R[] mapToArray(Collection<T> aList, Function<? super T, ? extends R> aFunction, Class<R> aClass)
    {
        Stream<R> filteredStream = aList.stream().map(aFunction);
        return filteredStream.toArray(size -> (R[]) Array.newInstance(aClass, size));
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

    /**
     * Returns the index of first item in list that matches given predicate (or -1).
     */
    public static <T> int findMatchIndex(List<T> aList, Predicate<? super T> aPred)
    {
        for (int i = 0, iMax = aList.size(); i < iMax; i++)
            if (aPred.test(aList.get(i)))
                return i;
        return -1;
    }

    /**
     * Joins a list of items by given delimiter using given function to get item strings.
     */
    public static <T> String joinString(List<T> aList, String aDelim, Function<T,String> aFunc)
    {
        // Get vars
        int size = aList.size();
        if (size == 0) return "";
        String str0 = aFunc.apply(aList.get(0));
        StringBuilder sb = new StringBuilder(str0);

        // Iterate over remaining items and add delim + func(item) for each
        for (int i = 1; i < size; i++) {
            T item = aList.get(i);
            String itemStr = aFunc.apply(item);
            sb.append(aDelim).append(itemStr);
        }

        // Return
        return sb.toString();
    }
}