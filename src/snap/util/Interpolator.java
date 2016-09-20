/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * This class provides functionality to interpolate between two numbers given a ratio between 0-1.
 */
public class Interpolator {

    // The direction for the interpolation
    Direction         _dir = Direction.In;
    
    // Constants for direction
    public enum Direction { In, Out, Both };
    
/**
 * Creates a new interpolator with given direction.
 */
protected Interpolator(Direction aDir)  { _dir = aDir; }

/**
 * Returns the name of this interpolator.
 */
public String getName()  { return "Linear"; }

/**
 * Returns a value given a ratio and start/end values.
 */
public double getValue(double aRatio, double aStart, double anEnd)
{
    switch(_dir) {
        case In: return interp(aRatio, aStart, anEnd);
        case Out: return interpOut(aRatio, aStart, anEnd);
        case Both: return interpBoth(aRatio, aStart, anEnd);
        default: throw new RuntimeException("Unsupported Direction " + _dir);
    }
}

/**
 * Returns a new ratio given normal ratio.
 */
protected double getRatio(double aRatio)  { return aRatio; }

/**
 * Direction In interpolation.
 */
protected double interp(double aRatio, double aStart, double aEnd) { return aStart + (aEnd-aStart)*getRatio(aRatio); }

/**
 * Direction Out interpolation.
 */
protected double interpOut(double aRatio, double aStart, double aEnd)  { return interp(1-aRatio, aEnd, aStart); }

/**
 * Direction Both interpolation.
 */
protected double interpBoth(double aRatio, double aStart, double anEnd)
{
    double midp = (aStart+anEnd)/2;                         // Get mid point of values
    if(aRatio<=.5) return interp(aRatio*2, aStart, midp);   // If beyond midpoint of ratio, interpolate in
    return interpOut((aRatio-.5)*2, midp, anEnd);           // Otherwise, interpolate out
}

/**
 * Returns a string representation of this interpolator.
 */
public String toString()  { return "RMInterpolator: " + getName(); }

/**
 * Linear interpolator.
 */
public static Interpolator LINEAR = new Interpolator(Direction.In);

/**
 * Ease In interpolator.
 */
public static Interpolator EASE_IN = new Interpolator(Direction.In) {
    public String getName() { return "Ease In"; }
    protected double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
};

/**
 * Ease Out interpolator.
 */
public static Interpolator EASE_OUT = new Interpolator(Direction.Out) {
    public String getName() { return "Ease Out"; }
    protected double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
};

/**
 * Ease Both interpolator.
 */
public static Interpolator EASE_BOTH = new Interpolator(Direction.Both) {
    public String getName() { return "Ease Both"; }
    protected double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
};

// Some common named interpolators
static Interpolator _interps[] = { LINEAR, EASE_IN, EASE_OUT, EASE_BOTH };

/**
 * Returns number of shared common interpolators.
 */
public static int getInterpolatorCount()  { return _interps.length; }

/**
 * Returns the individual common interpolator at given index.
 */
public static Interpolator getInterpolator(int anIndex)  { return _interps[anIndex]; }

/**
 * Returns a shared interpolator for given name.
 */
public static Interpolator getInterpolator(String aName)
{
    for(int i=0, iMax=getInterpolatorCount(); i<iMax; i++)
        if(getInterpolator(i).getName().equalsIgnoreCase(aName))
            return getInterpolator(i);
    System.err.println("Interpolator: interpolator not found for name " + aName);
    return getInterpolator(0);
}

}