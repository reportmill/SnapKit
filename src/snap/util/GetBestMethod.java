package snap.util;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This class provides functionality to find the best method for given arg classes.
 */
public class GetBestMethod {

    /**
     * Returns the method for given class and name that best matches given parameter types.
     */
    public static Method getBestMethod(Class<?> aClass, String aName, Class<?> ... theClasses)
    {
        // Get methods with compatible name/args (just return if null, no args or singleton)
        Method[] methods = getAllMethods(aClass, aName, theClasses, null);
        if (methods == null)
            return null;
        if (theClasses.length == 0 || methods.length == 1)
            return methods[0];

        // Rate compatible methods and return the most compatible
        Method method = null;
        int rating = 0;
        for (Method meth : methods) {
            int rtg = getRating(meth.getParameterTypes(), theClasses, meth.isVarArgs());
            if (rtg > rating) {
                method = meth;
                rating = rtg;
            }
        }
        return method;
    }

    /**
     * Returns the method for given class, name and parameter types.
     */
    private static Method[] getAllMethods(Class<?> aClass, String aName, Class<?>[] theClasses, Method[] theResult)
    {
        // Make sure class is non-primitive
        Class<?> cls = aClass.isPrimitive() ? ClassUtils.fromPrimitive(aClass) : aClass;

        // Check declared methods
        Method[] methods = getDeclaredMethods(cls, aName, theClasses, theResult);

        // If interface, check extended interfaces
        if (cls.isInterface()) {
            for (Class<?> cl : cls.getInterfaces())
                methods = getAllMethods(cl, aName, theClasses, methods);
        }

        // Otherwise, check superclass
        else if ((cls = cls.getSuperclass()) != null)
            methods = getAllMethods(cls, aName, theClasses, methods);

        // Return methods
        return methods;
    }

    /**
     * Returns compatible declared methods for a given class, name and parameter types array.
     */
    private static Method[] getDeclaredMethods(Class<?> aClass, String aName, Class<?>[] theClasses, Method[] theResult)
    {
        // Get class methods and intern name
        Method[] methods = aClass.getDeclaredMethods();
        String name = aName.intern();

        // Iterate over methods and if compatible, add to results
        for (Method meth : methods) {
            if (meth.isSynthetic()) continue;
            if (meth.getName() == name && isCompatible(meth.getParameterTypes(), theClasses, meth.isVarArgs())) {
                theResult = theResult != null ? Arrays.copyOf(theResult, theResult.length + 1) : new Method[1];
                theResult[theResult.length - 1] = meth;
            }
        }
        return theResult;
    }

    /**
     * Returns whether arg classes are compatible.
     */
    private static boolean isCompatible(Class<?>[] params, Class<?>[] theClasses, boolean isVarArgs)
    {
        // Handle Var args
        if (isVarArgs) {

            // If standard args don't match return false
            if (theClasses.length < params.length - 1 || !isAssignable(params, theClasses, params.length - 1))
                return false;

            // Get VarArgClass
            Class<?> varArgArrayClass = params[params.length - 1];
            Class<?> varArgClass = varArgArrayClass.getComponentType();

            // If only one arg and it is of array class, return true
            Class<?> argClass = theClasses.length == params.length ? theClasses[params.length - 1] : null;
            if (argClass != null && argClass.isArray() && varArgArrayClass.isAssignableFrom(argClass))
                return true;

            // If any var args don't match, return false
            for (int i = params.length - 1; i < theClasses.length; i++)
                if (theClasses[i] != null && !isAssignable(varArgClass, theClasses[i]))
                    return false;
            return true;
        }

        // Handle normal method
        return params.length == theClasses.length && isAssignable(params, theClasses, params.length);
    }

    /**
     * Returns a rating of a method for given possible arg classes.
     * This is a punt - need to groc the docs on this: https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html
     */
    private static int getRating(Class<?>[] theParamTypes, Class<?>[] theClasses, boolean isVarArgs)
    {
        // Get count
        Class<?>[] classes = theParamTypes;
        int classesCount = classes.length;
        if (isVarArgs)
            classesCount--;

        // Iterate over classes and add score based on matching classes
        int rating = 0;
        for (int i = 0; i < classesCount; i++) {
            Class<?> cls1 = classes[i];
            Class<?> cls2 = theClasses[i];
            if (cls1 == cls2) rating += 1000;
            else if (cls2 != null && cls1.isAssignableFrom(cls2))
                rating += 100;
            else if (isAssignable(cls1, cls2))
                rating += 10;
        }

        // If varargs, check remaining args
        if (isVarArgs) {

            // Get VarArgClass
            Class<?> varArgArrayClass = theParamTypes[classesCount];
            Class<?> varArgClass = varArgArrayClass.getComponentType();

            // If only one arg and it is of array class, return true
            Class<?> argClass = theClasses.length == theParamTypes.length ? theClasses[theParamTypes.length - 1] : null;
            if (argClass != null && argClass.isArray() && varArgArrayClass.isAssignableFrom(argClass))
                rating += 1000;

                // If any var args don't match, return false
            else for (int i = classesCount; i < theClasses.length; i++)
                if (theClasses[i] != null && !isAssignable(varArgClass, theClasses[i]))
                    rating += 1000;
        }

        // Return rating
        return rating;
    }

    /**
     * Returns whether a given class could be assigned a value from the second given class (accounting for auto-boxing).
     */
    private static boolean isAssignable(Class<?> aClass1, Class<?> aClass2)
    {
        // Handle null
        if (aClass2 == null)
            return !aClass1.isPrimitive();

        // Handle primitive
        if (aClass1.isPrimitive() || aClass2.isPrimitive())
            return isAssignablePrimitive(aClass1, aClass2);

        // Do normal version
        return aClass1.isAssignableFrom(aClass2);
    }

    /**
     * Returns whether a given primitive class could be assigned a value from the second given class.
     */
    private static boolean isAssignablePrimitive(Class<?> aClass1, Class<?> aClass2)
    {
        // Get primitives
        Class<?> c1 = ClassUtils.toPrimitive(aClass1);
        Class<?> c2 = ClassUtils.toPrimitive(aClass2);
        if (c1 == Object.class)
            return true;

        // Handle float, double, Number
        if (c1 == float.class || c1 == double.class || c1 == Number.class)
            return c2 == c1 || c2 == byte.class || c2 == char.class || c2 == short.class || c2 == int.class || c2 == long.class || c2 == float.class;

        // Handle byte, char, short, int long
        if (c1 == byte.class || c1 == char.class || c1 == short.class || c1 == int.class || c1 == long.class)
            return c2 == c1 || c2 == byte.class || c2 == char.class || c2 == short.class || c2 == int.class;

        // Do normal version
        return c1.isAssignableFrom(c2);
    }

    /**
     * Returns whether second batch of classes is assignable to first batch of classes (accounting for auto-boxing).
     */
    private static boolean isAssignable(Class<?>[] theClasses1, Class<?>[] theClasses2, int aCount)
    {
        if (theClasses1 == null)
            return theClasses2 == null || theClasses2.length == 0;
        if (theClasses2 == null)
            return theClasses1.length == 0;
        for (int i = 0; i < aCount; i++)
            if (theClasses2[i] != null && !isAssignable(theClasses1[i], theClasses2[i]))
                return false;
        return true;
    }
}
