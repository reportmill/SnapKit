/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A collection of array utility methods.
 */
public class ArrayUtils {

    /**
     * Returns the length of a given object array (or zero, if null).
     */
    public static int length(Object[] anArray)
    {
        return anArray != null ? anArray.length : 0;
    }

    /**
     * Adds a value to an array of objects.
     */
    public static <T> T[] add(T[] anArray, T anObj)
    {
        return add(anArray, anObj, length(anArray));
    }

    /**
     * Returns a new array by adding a value to an array of objects at the given index.
     */
    public static <T> T[] add(T[] anArray, T anObj, int anIndex)
    {
        // Get array length (throw exception if index out of bounds)
        int length = anArray.length;
        if (anIndex < 0 || anIndex > length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array,
        T[] newArray = Arrays.copyOf(anArray, length + 1);

        // Copy elements prior to index, add value at index, and copy elements beyond index
        newArray[anIndex] = anObj;
        if (length > anIndex)
            System.arraycopy(anArray, anIndex, newArray, anIndex + 1, length - anIndex);

        // Return
        return newArray;
    }

    /**
     * Returns an array by adding to the given array the given objects (array).
     */
    public static <T> T[] addAll(T[] anArray, T ... theObjs)
    {
        if (anArray == null) return theObjs;
        if (theObjs.length == 0) return anArray;

        T[] newArray = Arrays.copyOf(anArray, anArray.length + theObjs.length);
        System.arraycopy(theObjs, 0, newArray, anArray.length, theObjs.length);
        return newArray;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static <T> T[] remove(T[] anArray, int anIndex)
    {
        // Get array length (just return new single object array if empty)
        int length = anArray.length;
        if (anIndex < 0 || anIndex >= length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array
        T[] newArray = (T[]) Array.newInstance(anArray.getClass().getComponentType(), length - 1);

        // Copy elements prior to index and copy elements beyond index
        System.arraycopy(anArray, 0, newArray, 0, anIndex);
        if (length > anIndex + 1)
            System.arraycopy(anArray, anIndex + 1, newArray, anIndex, length - anIndex - 1);

        // Return
        return newArray;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static <T> T[] remove(T[] anArray, T anObj)
    {
        int index = indexOf(anArray, anObj);
        return index >= 0 ? remove(anArray, index) : anArray;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static int[] remove(int[] anArray, int anIndex)
    {
        // Get array length (just return new single object array if empty)
        int length = anArray.length;
        if (anIndex < 0 || anIndex >= length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array and copy elements beyond index
        int[] newArray = Arrays.copyOf(anArray, length - 1);
        if (length > anIndex + 1)
            System.arraycopy(anArray, anIndex + 1, newArray, anIndex, length - anIndex - 1);

        // Return
        return newArray;
    }

    /**
     * Replaces a range of values in an int array with values from another int array.
     */
    public static <T> T[] replace(T[] anArray, int start, int end, T ... swapInArray)
    {
        // Get lengths
        int swapOutLength = end - start;
        int swapInLength = swapInArray.length;
        int newLength = anArray.length - swapOutLength + swapInLength;

        // Create new array
        T[] newArray = Arrays.copyOf(anArray, newLength);

        // Copy elements from swap array and from tail of original array
        System.arraycopy(swapInArray, 0, newArray, start, swapInLength);
        System.arraycopy(anArray, end, newArray, start + swapInLength, anArray.length - end);

        // Return
        return newArray;
    }

    /**
     * Adds a value to an array of objects if list doesn't already contain it.
     */
    public static <T> T[] addId(T[] anArray, T anObj)
    {
        return addId(anArray, anObj, anArray.length);
    }

    /**
     * Adds a value to an array of objects if list doesn't already contain it.
     */
    public static <T> T[] addId(T[] anArray, T anObj, int anIndex)
    {
        if (containsId(anArray, anObj))
            return anArray;
        return add(anArray, anObj, anIndex);
    }

    /**
     * Removes a value from an array of objects if list doesn't already contain it.
     */
    public static <T> T[] removeId(T[] anArray, T anObj)
    {
        int index = indexOfId(anArray, anObj);
        if (index >= 0)
            return remove(anArray, index);
        return anArray;
    }

    /**
     * Returns new double array by adding given value to given array.
     */
    public static double[] add(double[] anArray, double anObj)
    {
        return add(anArray, anObj, anArray.length);
    }

    /**
     * Returns new double array by adding given value to given array at given index.
     */
    public static double[] add(double[] anArray, double anObj, int anIndex)
    {
        // Get array length (throw exception if index out of bounds)
        int length = anArray.length;
        if (anIndex < 0 || anIndex > length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Copy array with extra space
        double[] newArray = Arrays.copyOf(anArray, length + 1);

        // Copy elements prior to index, add value at index, and copy elements beyond index
        newArray[anIndex] = anObj;
        if (anIndex < length)
            System.arraycopy(anArray, anIndex, newArray, anIndex + 1, length - anIndex);

        // Return new array
        return newArray;
    }

    /**
     * Returns the number of object in array of a given class.
     */
    public static int getCount(Object[] anArray, Class<?> aClass)
    {
        // Declare local variable for count
        int count = 0;

        // Iterate over all listeners and increment for count
        for (int i = 0, iMax = length(anArray); i < iMax; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                count++;

        // Return count
        return count;
    }

    /**
     * Returns the individual object of a given class at given index (from all objects of given class).
     */
    public static <T> T get(Object[] anArray, Class<T> aClass, int anIndex)
    {
        // Iterate over all until index of listener of given class is found
        for (int i = 0, iMax = anArray.length, j = 0; i < iMax; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                if (j++ == anIndex)
                    return (T) anArray[i];

        // throw array out of bounds
        throw new ArrayIndexOutOfBoundsException(anIndex);
    }

    /**
     * Returns the individual object of a given class at given index (from all objects of given class).
     */
    public static <T> T[] get(Object[] anArray, Class<T> aClass)
    {
        // Get size and create array
        int size = getCount(anArray, aClass);
        T[] newArray = (T[]) Array.newInstance(aClass, size);

        // Iterate over all until index of listener of given class is found
        for (int i = 0, iMax = anArray.length, j = 0; i < iMax && j < size; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                newArray[j++] = (T) anArray[i];

        // Return
        return newArray;
    }

    /**
     * Returns whether two byte arrays are equal.
     */
    public static boolean equals(byte[] array1, byte[] array2)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length)
            return false;
        return equals(array1, array2, array1.length);
    }

    /**
     * Returns whether two byte arrays are equal to the given length.
     */
    public static boolean equals(byte[] array1, byte[] array2, int length)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length < length || array2.length < length)
            return false;
        for (int i = 0; i < length; i++)
            if (array1[i] != array2[i])
                return false;
        return true;
    }

    /**
     * Returns whether two float arrays are equal.
     */
    public static boolean equals(float[] array1, float[] array2)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length)
            return false;
        return equals(array1, array2, array1.length);
    }

    /**
     * Returns whether two float arrays are equal to the given length.
     */
    public static boolean equals(float[] array1, float[] array2, int length)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length < length || array2.length < length)
            return false;
        for (int i = 0; i < length; i++)
            if (array1[i] != array2[i])
                return false;
        return true;
    }

    /**
     * Returns whether two double arrays are equal.
     */
    public static boolean equals(double[] array1, double[] array2)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length)
            return false;
        return equals(array1, array2, array1.length);
    }

    /**
     * Returns whether two float arrays are equal to the given length.
     */
    public static boolean equals(double[] array1, double[] array2, int length)
    {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length < length || array2.length < length)
            return false;
        for (int i = 0; i < length; i++)
            if (array1[i] != array2[i])
                return false;
        return true;
    }

    /**
     * Returns the index of the given object in the given array.
     */
    public static int indexOf(Object[] anArray, Object anObj)
    {
        for (int i = 0, iMax = anArray.length; i < iMax; i++)
            if (anArray[i].equals(anObj))
                return i;
        return -1;
    }

    /**
     * Returns whether the given array contains the given object.
     */
    public static boolean contains(Object[] anArray, Object anObj)
    {
        return indexOf(anArray, anObj) >= 0;
    }

    /**
     * Returns the index of the given object in the given array using "==" instead of equals.
     */
    public static <T> int indexOfId(T[] anArray, T aValue)
    {
        for (int i = 0, iMax = length(anArray); i < iMax; i++)
            if (anArray[i] == aValue)
                return i;
        return -1;
    }

    /**
     * Returns whether the given array contains the identical given object.
     */
    public static <T> boolean containsId(T[] anArray, T aValue)
    {
        return indexOfId(anArray, aValue) >= 0;
    }

    /**
     * Reverse and array.
     */
    public static void reverse(Object[] anArray)
    {
        for (int i = 0, j = anArray.length - 1; i < j; i++, j--) {
            Object temp = anArray[i];
            anArray[i] = anArray[j];
            anArray[j] = temp;
        }
    }

    /**
     * Returns a filtered array for given original and Predicate.
     */
    public static <T> T[] filter(T[] anArray, Predicate<? super T> aPredicate)
    {
        Stream<T> filteredStream = Stream.of(anArray).filter(aPredicate);
        Class<? extends T> compClass = (Class<? extends T>) anArray.getClass().getComponentType();
        return filteredStream.toArray(size -> (T[]) Array.newInstance(compClass, size));
    }

    /**
     * Returns a mapped array for given original and Function.
     */
    public static <T,R> R[] map(T[] anArray, Function<? super T, ? extends R> aFunction, Class<R> aClass)
    {
        Stream<R> filteredStream = Stream.of(anArray).map(aFunction);
        return filteredStream.toArray(size -> (R[]) Array.newInstance(aClass, size));
    }

    /**
     * Returns a mapped array for given original and Function.
     */
    public static <T,R> R[] mapNonNull(T[] anArray, Function<? super T, ? extends R> aFunction, Class<R> aClass)
    {
        Stream<R> filteredStream = Stream.of(anArray).map(aFunction);
        Stream<R> filteredNonNull = filteredStream.filter(item -> item != null);
        return filteredNonNull.toArray(size -> (R[]) Array.newInstance(aClass, size));
    }

    /**
     * Returns the first item in array that matches given predicate (or null).
     */
    public static <T> T findMatch(T[] anArray, Predicate<? super T> aPred)
    {
        for (T item : anArray)
            if (aPred.test(item))
                return item;
        return null;
    }

    /**
     * Returns the index of first item in array that matches given predicate (or -1).
     */
    public static <T> int findMatchIndex(T[] anArray, Predicate<? super T> aPred)
    {
        for (int i = 0; i < anArray.length; i++)
            if (aPred.test(anArray[i]))
                return i;
        return -1;
    }

    /**
     * Joins an array of items by given delimiter using given function to get item strings.
     */
    public static <T> String joinString(T[] anArray, String aDelim, Function<T,String> aFunc)
    {
        // Get vars
        if (anArray.length == 0) return "";
        String str0 = aFunc.apply(anArray[0]);
        StringBuilder sb = new StringBuilder(str0);

        // Iterate over remaining items and add delim + func(item) for each
        for (int i = 1; i < anArray.length; i++) {
            String itemStr = aFunc.apply(anArray[i]);
            sb.append(aDelim).append(itemStr);
        }

        // Return
        return sb.toString();
    }
}