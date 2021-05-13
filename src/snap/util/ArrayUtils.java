/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A collection of array utility methods.
 */
public class ArrayUtils {

    /**
     * Returns the length of a given object array (or zero, if null).
     */
    public static int length(Object anArray[])  { return anArray!=null ? anArray.length : 0; }

    /**
     * Adds a value to an array of objects.
     */
    public static <T> T[] add(T anArray[], T anObj)  { return add(anArray, anObj, length(anArray)); }

    /**
     * Returns a new array by adding a value to an array of objects at the given index.
     */
    public static <T> T[] add(T anArray[], T anObj, int anIndex)
    {
        // Get array length (throw exception if index out of bounds)
        int length = anArray.length;
        if (anIndex<0 || anIndex>length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array,
        T newArray[] = (T[])Array.newInstance(anArray.getClass().getComponentType(), length+1);

        // Copy elements prior to index, add value at index, and copy elements beyond index
        System.arraycopy(anArray, 0, newArray, 0, anIndex);
        newArray[anIndex] = anObj;
        if (length>anIndex)
            System.arraycopy(anArray, anIndex, newArray, anIndex+1, length - anIndex);

        // Return new array
        return newArray;
    }

    /**
     * Returns an array by adding to the given array the given objects (array).
     */
    public static <T> T[] addAll(T anArray[], T ... theObjs)
    {
        if (anArray==null) return theObjs;
        T array[] = (T[])Array.newInstance(anArray.getClass().getComponentType(), anArray.length + theObjs.length);
        System.arraycopy(anArray, 0, array, 0, anArray.length);
        System.arraycopy(theObjs, 0, array, anArray.length, theObjs.length);
        return array;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static <T> T[] remove(T anArray[], int anIndex)
    {
        // Get array length (just return new single object array if empty)
        int length = anArray.length;
        if (anIndex<0 || anIndex>=length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array
        T newArray[] = (T[])Array.newInstance(anArray.getClass().getComponentType(), length-1);

        // Copy elements prior to index and copy elements beyond index
        System.arraycopy(anArray, 0, newArray, 0, anIndex);
        if (length>anIndex+1)
            System.arraycopy(anArray, anIndex+1, newArray, anIndex, length - anIndex - 1);

        // Return new array
        return newArray;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static <T> T[] remove(T anArray[], T anObj)
    {
        int index = indexOf(anArray, anObj);
        return index>=0 ? remove(anArray, index) : anArray;
    }

    /**
     * Returns a new array by removing a value from an array of objects at the given index.
     */
    public static int[] remove(int anArray[], int anIndex)
    {
        // Get array length (just return new single object array if empty)
        int length = anArray.length;
        if (anIndex<0 || anIndex>=length)
            throw new ArrayIndexOutOfBoundsException(anIndex);

        // Create new array and copy elements beyond index
        int newArray[] = Arrays.copyOf(anArray, length-1);
        if (length>anIndex+1)
            System.arraycopy(anArray, anIndex+1, newArray, anIndex, length - anIndex - 1);

        // Return new array
        return newArray;
    }

    /**
     * Replaces a range of values in an int array with values from another int array.
     */
    public static <T> T[] replace(T anArray[], int start, int end, T ... swapInArray)
    {
        // Get lengths
        int swapOutLength = end - start;
        int swapInLength = swapInArray==null ? 0 : swapInArray.length;
        int newLength = anArray.length - swapOutLength + swapInLength;

        // Create new array
        T newArray[] = (T[])Array.newInstance(anArray.getClass().getComponentType(), newLength);

        // Copy elements prior to index
        System.arraycopy(anArray, 0, newArray, 0, start);
        System.arraycopy(swapInArray, 0, newArray, start, swapInLength);
        System.arraycopy(anArray, end, newArray, start + swapInLength, anArray.length - end);

        // Return new array
        return newArray;
    }

    /**
     * Adds a value to an array of objects if list doesn't already contain it.
     */
    public static <T> T[] addId(T anArray[], T anObj)
    {
        return containsId(anArray, anObj) ? anArray : add(anArray, anObj);
    }

    /**
     * Removes a value from an array of objects if list doesn't already contain it.
     */
    public static <T> T[] removeId(T anArray[], T anObj)
    {
        int index = indexOfId(anArray, anObj);
        return index>=0 ? remove(anArray, index) : anArray;
    }

    /**
     * Returns the number of object in array of a given class.
     */
    public static int getCount(Object anArray[], Class aClass)
    {
        // Declare local variable for count
        int count = 0;

        // Iterate over all listeners and increment for count
        for (int i=0, iMax=length(anArray); i<iMax; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                count++;

        // Return count
        return count;
    }

    /**
     * Returns the individual object of a given class at given index (from all objects of given class).
     */
    public static <T> T get(Object anArray[], Class<T> aClass, int anIndex)
    {
        // Iterate over all until index of listener of given class is found
        for (int i=0, iMax=anArray.length, j=0; i<iMax; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                if (j++==anIndex)
                    return (T)anArray[i];

        // throw array out of bounds
        throw new ArrayIndexOutOfBoundsException(anIndex);
    }

    /**
     * Returns the individual object of a given class at given index (from all objects of given class).
     */
    public static <T> T[] get(Object anArray[], Class<T> aClass)
    {
        // Get size and create array
        int size = getCount(anArray, aClass);
        T array[] = (T[])Array.newInstance(aClass, size);

        // Iterate over all until index of listener of given class is found
        for (int i=0, iMax=anArray.length, j=0; i<iMax && j<size; i++)
            if (aClass.isAssignableFrom(anArray[i].getClass()))
                array[j++] = (T)anArray[i];

        // Return array
        return array;
    }

    /**
     * Returns an array as an ArrayList.
     */
    public static <T> ArrayList <T> asArrayList(T ... theItems)
    {
        ArrayList <T> list = new ArrayList();
        for (T item : theItems) list.add(item);
        return list;
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
        for (int i=0; i<length; i++)
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
        for (int i=0; i<length; i++)
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
        for (int i=0; i<length; i++)
            if (array1[i] != array2[i])
                return false;
        return true;
    }

    /**
     * Returns a double array for given floats.
     */
    public static double[] doubleArray(float[] theFloats)
    {
        if (theFloats == null) return null;
        double[] doubleArray = new double[theFloats.length];
        for (int i=0; i<theFloats.length; i++) doubleArray[i] = theFloats[i];
        return doubleArray;
    }

    /**
     * Returns a float array for given doubles.
     */
    public static float[] floatArray(double[] theDoubles)
    {
        if (theDoubles==null) return null;
        float[] floatArray = new float[theDoubles.length];
        for (int i=0; i<theDoubles.length; i++) floatArray[i] = (float) theDoubles[i];
        return floatArray;
    }

    /**
     * Returns the index of the given object in the given array.
     */
    public static int indexOf(Object[] anArray, Object anObj)
    {
        for (int i=0, iMax=anArray.length; i<iMax; i++)
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
        for (int i=0, iMax=length(anArray); i<iMax; i++)
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
     * Returns a copy of given range of given array (this method is in Java 6 Arrays class).
     */
    public static int[] copyOfRange(int[] anArray, int from, int to)
    {
        int newLength = to - from;
        if (newLength < 0) throw new IllegalArgumentException(from + " > " + to);
        int[] copy = new int[newLength];
        System.arraycopy(anArray, from, copy, 0, Math.min(anArray.length - from, newLength));
        return copy;
    }

    /**
     * Reverse and array.
     */
    public static void reverse(Object[] anArray)
    {
        for (int i=0, j=anArray.length-1; i<j; i++, j--) {
            Object temp = anArray[i];
            anArray[i] = anArray[j];
            anArray[j] = temp;
        }
    }
}