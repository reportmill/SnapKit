/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.gfx.GFXEnv;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility methods for Class.
 */
public class ClassUtils {

    /**
     * Returns the given object as instance of given class, if it is.
     */
    public static <T> T getInstance(Object anObj, Class<T> aClass)
    {
        return aClass.isInstance(anObj) ? (T) anObj : null;
    }

    /**
     * Returns a new instance of a given class.
     */
    public static <T> T newInstance(Class<T> aClass)
    {
        try {
            //return aClass.getConstructor().newInstance(); // Causes TeaVM size to grow
            return aClass.newInstance();
        }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a clone of given Cloneable object using reflection.
     */
    public static Object cloneCloneable(Cloneable anObj)
    {
        // Get clone method from Obj.Class
        Class<?> cls = anObj.getClass();
        Method meth;
        try { meth = cls.getMethod("clone"); }
        catch(NoSuchMethodException e) { throw new RuntimeException(e); }

        // Invoke clone and return result
        try { return meth.invoke(anObj); }
        catch(Throwable e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the package name of given class.
     */
    public static String getPackageName(Class<?> aClass)
    {
        Package pkg = aClass.getPackage();
        return pkg != null ? pkg.getName() : "";
    }

    /**
     * Returns the class for an object.
     */
    public static Class<?> getClass(Object anObj)
    {
        if (anObj instanceof Class)
            return (Class<?>) anObj;
        return anObj != null ? anObj.getClass() : null;
    }

    /**
     * Class.getMethod wrapper to isolate call to one place.
     */
    public static Method getMethod(Class<?> aClass, String aName, Class<?> ... theClasses)
    {
        // Forward to getMethodOrThrow
        try { return getMethodOrThrow(aClass, aName, theClasses); }

        // Return null for exceptions (suppress)
        catch(NoSuchMethodException e) { return null; }
        catch(SecurityException e) { return null; }
    }

    /**
     * Class.getMethod wrapper to isolate call to one place.
     */
    public static Method getMethodOrThrow(Class<?> aClass, String aName, Class<?> ... theClasses) throws NoSuchMethodException
    {
        GFXEnv env = GFXEnv.getEnv();
        return env.getMethod(aClass, aName, theClasses);
    }

    /**
     * Returns primitive type for non-primitive.
     */
    public static Class<?> toPrimitive(Class<?> aClass)
    {
        for (int i = 0; i < _primitives.length; i++)
            if (aClass == _primMappings[i])
                return _primitives[i];
        return aClass;
    }

    /**
     * Returns non-primitive type for primitive.
     */
    public static Class<?> fromPrimitive(Class<?> aClass)
    {
        for (int i = 0; i < _primitives.length; i++)
            if (aClass == _primitives[i])
                return _primMappings[i];
        return aClass;
    }

    /**
     * Returns the common ancestor class for a list of objects.
     */
    public static Class<?> getCommonClass(List<?> aList)
    {
        // Get class for first object, iterate over remaining classes and return common class
        Class<?> commonClass = aList.size() > 0 ? getClass(aList.get(0)) : null;
        for (int i = 1, iMax = aList.size(); i < iMax; i++)
            commonClass = getCommonClass(commonClass, aList.get(i));
        return commonClass;
    }

    /**
     * Returns the common ancestor class for two objects.
     */
    public static Class<?> getCommonClass(Object anObj1, Object anObj2)
    {
        // Bail if either object is null
        if (anObj1 == null || anObj2 == null)
            return null;

        // Get the classes for the objects
        Class<?> class1 = getClass(anObj1);
        Class<?> class2 = getClass(anObj2);

        // If either is assignable from the other, return that class
        if (class1.isAssignableFrom(class2))
            return class1;
        if (class2.isAssignableFrom(class1))
            return class2;

        // Recurse by swapping args and using superclass of second
        return getCommonClass(class2.getSuperclass(), class1);
    }

    // An array of primitive type classes and sister array of it's non-primitive matches
    private static Class<?>[]  _primitives = {
            boolean.class, char.class, byte.class,
            short.class, int.class, long.class,
            float.class, double.class, void.class
    };

    // An array of primitive non-primitive matches
    private static Class<?>[]  _primMappings = {
            Boolean.class, Character.class, Byte.class,
            Short.class, Integer.class, Long.class,
            Float.class, Double.class, Void.class
    };
}