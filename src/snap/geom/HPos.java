/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * A class to represent a horizontal position (LEFT, CENTER, RIGHT).
 */
public enum HPos {

    LEFT,
    CENTER,
    RIGHT;

    /**
     * Returns the pos as a double from 0 to 1.
     */
    public double doubleValue()  { return this==LEFT ? 0 : this==CENTER ? .5 : 1; }

    /**
     * Returns the HPos.
     */
    public static HPos get(String aStr)
    {
        try { HPos.valueOf(aStr); }
        catch(Exception e) { }
        for (HPos p : HPos.values())
            if (p.toString().equalsIgnoreCase(aStr))
                return p;
        return LEFT;
    }
}