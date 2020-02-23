/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;

/**
 * A class to represent a cursor.
 */
public class Cursor {
    
    // The cursor name
    String          _name;

    // Predefined cursor types
    public static final Cursor DEFAULT = new Cursor("DEFAULT");
    public static final Cursor CROSSHAIR = new Cursor("CROSSHAIR");
    public static final Cursor HAND = new Cursor("HAND");
    public static final Cursor MOVE = new Cursor("MOVE");
    public static final Cursor TEXT = new Cursor("TEXT");
    public static final Cursor NONE = new Cursor("NONE");
    public static final Cursor N_RESIZE = new Cursor("N_RESIZE");
    public static final Cursor S_RESIZE = new Cursor("S_RESIZE");
    public static final Cursor E_RESIZE = new Cursor("E_RESIZE");
    public static final Cursor W_RESIZE = new Cursor("W_RESIZE");
    public static final Cursor NE_RESIZE = new Cursor("NE_RESIZE");
    public static final Cursor NW_RESIZE = new Cursor("NW_RESIZE");
    public static final Cursor SE_RESIZE = new Cursor("SE_RESIZE");
    public static final Cursor SW_RESIZE = new Cursor("SW_RESIZE");

/**
 * Creates a new Cursor.
 */
protected Cursor(String aName)  { _name = aName; }

/**
 * Returns the cursor name.
 */
public String getName()  { return _name; }

/**
 * Returns the cursor for given position.
 */
public static Cursor get(Pos aHandle)
{
    switch(aHandle) {
        case TOP_CENTER: return Cursor.N_RESIZE;
        case BOTTOM_CENTER: return Cursor.S_RESIZE;
        case CENTER_RIGHT: return Cursor.E_RESIZE;
        case CENTER_LEFT: return Cursor.W_RESIZE;
        case TOP_LEFT: return Cursor.NW_RESIZE;
        case TOP_RIGHT: return Cursor.NE_RESIZE;
        case BOTTOM_LEFT: return Cursor.SW_RESIZE;
        case BOTTOM_RIGHT: return Cursor.SE_RESIZE;
        default: return null;
    }
}

}