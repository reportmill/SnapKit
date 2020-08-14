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
    public static int size(List aList)  { return aList==null ? 0 : aList.size(); }

    /**
     * Returns the object at the given index (returns null object for null list or invalid index).
     */
    public static <T> T get(List <T> aList, int anIndex)
    {
        return aList==null || anIndex<0 || anIndex>=aList.size() ? null : aList.get(anIndex);
    }

    /**
     * Returns the last object in the given list.
     */
    public static <T> T getLast(List <T> aList)  { return get(aList, size(aList)-1); }

    /**
     * Returns whether list contains given object (accepts null list).
     */
    public static boolean contains(List aList, Object anObj)  { return aList!=null && aList.contains(anObj); }

    /**
     * Returns whether list contains identical given object (accepts null list).
     */
    public static boolean containsId(List aList, Object anObj)  { return indexOfId(aList, anObj)>=0; }

    /**
     * Returns index of identical given object in given list.
     */
    public static int indexOfId(List aList, Object anObj)
    {
        // Iterate over list objects and return index if identical exists
        for (int i=0, iMax=size(aList); i<iMax; i++)
            if (anObj==aList.get(i))
                return i;

        // Return -1 if identical doesn't exist
        return -1;
    }

    /**
     * Returns an array of indexes for given list and given objects in list.
     */
    public static int[] getIndexesId(List aList, List aSubList)
    {
        // Get list of unique indexes for given list and sublist
        List <Integer> indexes = new ArrayList();
        for (Object obj : aSubList) {
            int index = indexOfId(aList, obj);
            if (!indexes.contains(index))
                indexes.add(index);
        }

        // Get indexes as int array and return
        int indxs[] = new int[indexes.size()];
        for (int i=0, iMax=indexes.size(); i<iMax; i++) indxs[i] = indexes.get(i);
        return indxs;
    }

    /**
     * Adds an object to the given list and returns list (creates list if missing).
     */
    public static <T> List <T> add(List <T> aList, T anObj)
    {
        // If list is null, create list
        if (aList==null) aList = new Vector();

        // Add object
        aList.add(anObj);

        // Return list
        return aList;
    }

    /**
     * Adds an object to the given list if object is absent (creates list if missing).
     */
    public static <T> List <T> addUnique(List <T> aList, T anObj)
    {
        return contains(aList, anObj) ? aList : add(aList, anObj);
    }

    /**
     * Adds an object to the given list if identical object is missing (creates list if missing).
     */
    public static <T> List <T> addUniqueId(List <T> aList, T anObj)
    {
        return containsId(aList, anObj) ? aList : add(aList, anObj);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List <T> addAllUnique(List <T> aList, List <T> theObjects)
    {
        // If list is null, create it
        if (aList==null) aList = new ArrayList();

        // Add objects unique
        for (T object : theObjects)
            if (!aList.contains(object))
                aList.add(object);

        // Return list
        return aList;
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List <T> addAllUniqueId(List <T> aList, List <T> theObjects)
    {
        // If list is null, create it
        if (aList==null) aList = new ArrayList();

        // Add objects unique
        for (T object : theObjects)
            if (!containsId(aList, object))
                aList.add(object);

        // Return list
        return aList;
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List <T> addAllUnique(List <T> aList, T ... theObjects)
    {
        return addAllUnique(aList, aList!=null ? aList.size() : 0, theObjects);
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List <T> addAllUnique(List <T> aList, int anIndex, T ... theObjects)
    {
        // If list is null, create it
        if (aList==null) aList = new ArrayList();

        // Add objects unique
        for (int i=0; i<theObjects.length; i++) { T object = theObjects[i];
            if (!aList.contains(object))
                aList.add(anIndex++, object); }

        // Return list
        return aList;
    }

    /**
     * Adds all object from second list to first list (creates first list if missing).
     */
    public static <T> List <T> addAllUniqueId(List <T> aList, T ... theObjects)
    {
        // If list is null, create it
        if (aList==null) aList = new ArrayList();

        // Add objects unique
        for (T object : theObjects)
            if (!containsId(aList, object))
                aList.add(object);

        // Return list
        return aList;
    }

    /**
     * Removes given object from given list (accepts null list).
     */
    public static boolean remove(List aList, Object anObj)
    {
        return aList!=null && aList.remove(anObj);
    }

    /**
     * Removes range of objects from given list (from start to end, not including end).
     */
    public static void remove(List aList, int start, int end)
    {
        for (int i=end-1; i>=start; i--)
            aList.remove(i);
    }

    /**
     * Removes the last object from given list.
     */
    public static <T> T removeLast(List <T> aList)  { return aList.remove(aList.size() - 1); }

    /**
     * Removes the object identical to the given object from list.
     */
    public static int removeId(List aList, Object anObj)
    {
        int index = indexOfId(aList, anObj);
        if (index>=0) aList.remove(index);
        return index;
    }

    /**
     * Returns the result of binary search, but always returns insert index.
     */
    public static <T> int binarySearch(List <? extends Comparable<? super T>> aList, T aKey)
    {
        //int index = 0; while (index<getFileCount() && aFile.compareTo(getFile(index))>=0) index++;
        int index = aList!=null ? Collections.binarySearch(aList, aKey) : 0;
        return index>=0 ? index : -index - 1;
    }

    /**
     * Moves the object at index 1 to index 2.
     */
    public static void move(List aList, int anIndex1, int anIndex2)
    {
        // If either index is invalid, return
        if (anIndex1<0 || anIndex1>=aList.size() || anIndex2<0 || anIndex2>=aList.size())
            return;

        // Remove object and re-insert and desired index
        Object obj = aList.remove(anIndex1);
        aList.add(anIndex2, obj);
    }

    /**
     * Reverses the items in the given list.
     */
    public static List reverse(List aList)
    {
        for (int i=0, max=aList.size(), iMax= max/2; i<iMax; i++) {
            int oppositeIndex = max - i - 1;
            Object original = aList.set(i, aList.get(oppositeIndex));
            aList.set(oppositeIndex, original);
        }

        return aList;
    }

    /**
     * Returns the first non-list object in the given list hierarchy, recursing if a list is found.
     */
    public static Object getFirstLeaf(List aList)
    {
        for (int i=0, iMax=aList.size(); i<iMax; i++) {
            Object obj = aList.get(i);
            if (obj instanceof List)
                obj = getFirstLeaf((List)obj);
            if (obj!=null)
                return obj;
        }

        return null;
    }

    /**
     * Returns the objects at a given level in the given list hierarchy.
     */
    public static List objectsAtLevel(List aList, int aLevel)
    {
        if (aLevel>0) {
            List array = new ArrayList();
            for (int i=0, iMax=aList.size(); i<iMax; i++) {
                Object obj = aList.get(i);
                array.addAll(objectsAtLevel((List)obj, aLevel-1));
            }
            return array;
        }

        return aList;
    }

    /**
     * Returns the number of objects at a given level in the given list hierarchy.
     */
    public static int countAtLevel(List aList, int aLevel)
    {
        if (aLevel<0)
            return 1;

        if (aLevel>0) {
            int count = 0;
            for (int i=0, iMax=aList.size(); i<iMax; i++)
                count += countAtLevel((List)aList.get(i), aLevel-1);
            return count;
        }

        return aList.size();
    }

    /**
     * Returns a string by concatenating strings in given list separated by given string.
     */
    public static String joinStrings(List aList, String aString)
    {
        StringBuffer sb = new StringBuffer();
        for (int i=0, iMax=aList.size(); i<iMax; i++)
            sb.append(i==0 ? "" : aString).append(aList.get(i));
        return sb.toString();
    }

    /**
     * Adds object from list 2 to list 1, unless they are already present (then removes them).
     */
    public static void xor(List l1, List l2)
    {
        int size = l1.size();

        // Add objects from l2 that aren't in l
        for (int i=0, iMax=l2.size(); i<iMax; i++)
            if (!l1.contains(l2.get(i)))
                l1.add(l2.get(i));

        // Remove elements in l that are in l2
        for (int i=size-1; i>=0; i--)
            if (l2.contains(l1.get(i)))
                l1.remove(i);
    }

    /**
     * Returns a filtered list (copy with given key chain string.
     */
    public static List getFilteredList(List aList, String aKeyChain)
    {
        // If key is null or zero length, just return copy
        if (aKeyChain==null || aKeyChain.length()==0) return new ArrayList(aList);

        // Get key chain
        KeyChain keyChain = KeyChain.getKeyChain(aKeyChain);

        // Allocate new list with same capacity as original
        List list = new ArrayList(aList.size());

        // Iterate over list objects and add to new list if they satisfy key chain
        for (Object object : aList)
            if (KeyChain.getBoolValue(object, keyChain))
                list.add(object);

        // Return list
        return list;
    }

    /**
     * Returns whether objects in list all have same class.
     */
    public static boolean objectsHaveSameClass(List l)
    {
        if (size(l)==0) return false;
        Class firstClass = l.get(0).getClass();
        for (int i=1, iMax=l.size(); i<iMax; i++)
            if (l.get(i).getClass() != firstClass)
                return false;
        return true;
    }

    /**
     * Moves the object at the given index to the front of the list.
     */
    public static void moveToFront(List aList, int anIndex)  { if (anIndex>0) moveToFront(aList, aList.get(anIndex)); }

    /**
     * Move the given object to the front of the list.
     */
    public static void moveToFront(List aList, Object anObj)
    {
        if (anObj==null) return;
        aList.remove(anObj);
        aList.add(0, anObj);
    }

    /**
     * Returns whether lists have same objects in them.
     */
    public static boolean equalsId(List aList1, List aList2)
    {
        // If lists have different sizes, return false
        int size1 = aList1!=null ? aList1.size() : 0;
        int size2 = aList2!=null ? aList2.size() : 0;

        // Check list sizes
        if (size1!=size2) return false;

        // Iterate over objects
        for (int i=0; i<size1; i++)
            if (aList1.get(i)!=aList2.get(i))
                return false;

        // Return true since checks passed
        return true;
    }

    /**
     * Returns a copy of the given list.
     */
    public static <T> List <T> clone(List <T> aList)
    {
        if (aList instanceof ArrayList) return (List)((ArrayList)aList).clone();
        if (aList instanceof Vector) return (List)((Vector)aList).clone();
        return aList!=null ? new ArrayList(aList) : null;
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
    public static <T,R> List<R> getFilteredForClass(Collection<T> aList, Class<R> aClass)
    {
        return (List<R>) aList.stream().filter(item -> aClass.isInstance(item)).collect(Collectors.toList());
    }
}