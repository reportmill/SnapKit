/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.math.*;
import java.util.*;

/**
 * Utility methods for common math operations.
 */
public class MathUtils {
    
    // Random
    private static Random _random = new Random();

    /**
     * Returns whether two real numbers are equal within a small tolerance.
     */
    public static boolean equals(double a, double b)
    {
        return Math.abs(a - b) < 0.0001;
    }

    /**
     * Returns whether two real numbers are equal within given tolerance (e.g., 1e-12)
     */
    public static boolean equals(double a, double b, double aTolerance)
    {
        return Math.abs(a - b) < aTolerance;
    }

    /**
     * Returns whether a real number is practically zero.
     */
    public static boolean equalsZero(double a)
    {
        return equals(a, 0);
    }

    /**
     * Returns whether a real number is practically greater than another.
     */
    public static boolean gt(double a, double b)
    {
        return (a>b) && !equals(a,b);
    }

    /**
     * Returns whether a real number is practically greater than or equal another.
     */
    public static boolean gte(double a, double b)
    {
        return (a>b) || equals(a,b);
    }

    /**
     * Returns whether a real number is practically less than another.
     */
    public static boolean lt(double a, double b)
    {
        return (a<b) && !equals(a,b);
    }

    /**
     * Returns whether a real number is practically less than or equal another.
     */
    public static boolean lte(double a, double b)
    {
        return (a<b) || equals(a,b);
    }

    /**
     * Returns whether a real number is between two other numbers.
     */
    public static boolean between(double a, double x, double y)  { return ((a>=x) && (a<=y)); }

    /**
     * Returns the sign of a given number (as -1 or 1).
     */
    public static int sign(double f)  { return f<0 ? -1 : 1; }

    /**
     * Returns the given number modulo the second number (mod for floats).
     */
    public static double mod(double x, double y)  { return x - y*Math.floor(x/y); }

    /**
     * Returns the given number rounded to the second number (rounding to arbitrary floating values).
     */
    public static float round(float x, float y)  { return y*(int)((x + sign(x)*y/2)/y); }

    /**
     * Returns the given number rounded to the second number (rounding to arbitrary double values).
     */
    public static double round(double x, double y)  { return y*(int)((x + sign(x)*y/2)/y); }

    /**
     * Truncate x down to the nearest y.
     */
    public static double trunc(double x, double y)
    {
        return y*Math.floor((x+.00001)/y);
    }

    /**
     * Truncate x down to the nearest y.
     */
    public static double floor(double x, double y)
    {
        return y*Math.floor((x+.00001)/y);
    }

    /**
     * Truncate x up to the nearest y.
     */
    public static double ceil(double x, double y)
    {
        return y*Math.ceil((x+.00001)/y);
    }

    /**
     * Returns the given float clamped to 1/1000th precision.
     */
    public static double clamp(double f)
    {
        return (f>-1e-3) && (f<1e-3) ? 0 : (f>1e5 ? 1e5f : (f<-1e5 ? -1e5f : f));
    }

    /**
     * Returns the given in clamped between the two values.
     */
    public static int clamp(int i, int min, int max)
    {
        return Math.min(Math.max(i, min), max);
    }

    /**
     * Returns the given double clamped between the two values.
     */
    public static double clamp(double f, double min, double max)
    {
        return Math.min(Math.max(f,min),max);
    }

    /**
     * Returns the given double clamped between the two values (wraps around if out of range).
     */
    public static int clamp_wrap(int a, int x, int y)
    {
        return a<x ? y - (x-a)%(y-x) : a>y ? x + (a-y)%(y-x) : a;
    }

    /**
     * Returns the given double clamped between the two values (wraps around if out of range).
     */
    public static double clamp_wrap(double a, double x, double y)
    {
        return a<x ? y - Math.IEEEremainder(x-a,y-x) : a>y ? x + Math.IEEEremainder(a-y,y-x) : a;
    }

    /**
     * Returns the given double clamped between the two values.
     */
    public static double clamp_doubleback(double a, double x, double y)
    {
        double newA = Math.abs(Math.IEEEremainder(a,2*(y-x)));
        if (newA>y)
            newA = 2*y-newA;
        return newA;
    }

    /**
     * Returns the negative of the given Number.
     */
    public static Number negate(Number aNumber)
    {
        // If BigDecimal, have it negate
        if (aNumber instanceof BigDecimal)
            return ((BigDecimal)aNumber).negate();

        // Return big decimal of negative double value
        return new BigDecimal(-SnapUtils.doubleValue(aNumber));
    }

    /**
     * Returns the sum of the two given Numbers.
     */
    public static Number add(Number n1, Number n2)
    {
        // Try subtracting as BigDecimal (can fail if either are NaN or neg/pos infinity), otherwise add as doubles
        try { return SnapUtils.getBigDecimal(n1).add(SnapUtils.getBigDecimal(n2)); }
        catch(Exception e) { return SnapUtils.doubleValue(n1) + SnapUtils.doubleValue(n2); }
    }

    /**
     * Returns the difference of the two given Numbers.
     */
    public static Number subtract(Number n1, Number n2)
    {
        // Try subtracting as BigDecimal (can fail if either are NaN or neg/pos infinity), otherwise subtract as doubles
        try { return SnapUtils.getBigDecimal(n1).subtract(SnapUtils.getBigDecimal(n2)); }
        catch(Exception e) { return SnapUtils.doubleValue(n1) - SnapUtils.doubleValue(n2); }
    }

    /**
     * Returns the product of the two given Numbers.
     */
    public static Number multiply(Number n1, Number n2)
    {
        // Try multiplying as BigDecimals (can fail if either are NaN or neg/pos infinity), otherwise, multiply as doubles
        try { return SnapUtils.getBigDecimal(n1).multiply(SnapUtils.getBigDecimal(n2)); }
        catch(Exception e) { return SnapUtils.doubleValue(n1)*SnapUtils.doubleValue(n2); }
    }

    /**
     * Returns the result of dividing n1 by n2.
     */
    public static Number divide(Number n1, Number n2)
    {
        // Try dividing as BigDecimals (can fail if either are NaN or neg/pos infinity)
        try {
            BigDecimal dec1 = SnapUtils.getBigDecimal(n1);
            BigDecimal dec2 = SnapUtils.getBigDecimal(n2);
            return dec1.divide(dec2, 16, BigDecimal.ROUND_HALF_DOWN);
        }

        // Otherwise, divide as doubles
        catch(Exception e) {
            double d1 = SnapUtils.doubleValue(n1);
            double d2 = SnapUtils.doubleValue(n2);
            if (d2==0)
                return d1>=0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            return d1/d2;
        }
    }

    /**
     * Returns the sign of the given angle in degrees.
     */
    public static double sin(double anAngle)
    {
        return Math.sin(Math.toRadians(anAngle));
    }

    /**
     * Returns the cos of the given angle in degrees.
     */
    public static double cos(double anAngle)
    {
        return Math.cos(Math.toRadians(anAngle));
    }

    /**
     * Returns a random int.
     */
    public static int randomInt()
    {
        return _random.nextInt();
    }

    /**
     * Returns a random float up to given value.
     */
    public static float randomFloat(float aVal)
    {
        return Math.abs(_random.nextInt()) / (float) Integer.MAX_VALUE * aVal;
    }
}