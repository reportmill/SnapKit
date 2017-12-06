/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.SnapUtils;

/**
 * A class to model specific key strokes, including the key code and modifiers (shift, alt, etc.).
 */
public class KeyCombo {

    // Key code
    int         _kcode;
    
    // Modifiers
    boolean     _alt, _command, _control, _shift, _shortcut;

/**
 * Creates a new KeyCombo.
 */
protected KeyCombo()  { }

/**
 * Creates a new KeyCombo for key code and modifiers.
 */
protected KeyCombo(int aKC, boolean isAlt, boolean isCmd, boolean isCntr, boolean isShift)
{
    _kcode = aKC; _alt = isAltDown(); _command = isCmd; _control = isCntr; _shift = isShift;
    if(isCommandDown() && SnapUtils.isMac) _shortcut = true;
    if(isControlDown() && SnapUtils.isWindows) _shortcut = true;
}

/**
 * Creates a new KeyCombo for given string.
 */
public static KeyCombo get(String aStr)
{
    String parts[] = aStr.replace("+"," ").split("\\s");
    KeyCombo kcombo = new KeyCombo();
    
    for(String part : parts) {
        part = getPart(part);
        int kcode = KeyCode.get(part);
        switch(kcode) {
            case KeyCode.ALT: kcombo._alt = true; break;
            case KeyCode.COMMAND: kcombo._command = true; break;
            case KeyCode.CONTROL: kcombo._control = true; break;
            case KeyCode.SHIFT: kcombo._shift = true; break;
            default: kcombo._kcode = kcode;
        }
    }
    
    if(kcombo.isCommandDown() && SnapUtils.isMac) kcombo._shortcut = true;
    if(kcombo.isControlDown() && SnapUtils.isWindows) kcombo._shortcut = true;
    return kcombo;   
}

/**
 * Returns the KeyCode.
 */
public int getKeyCode()  { return _kcode; }

/**
 * Returns the name.
 */
public String getName()
{
    String str = KeyCode.getName(_kcode);
    String mod = (_alt? "Alt " : "") + (_shortcut? "Shortcut " : "") + (_shift? "Shift " : "");
    return mod + str;
}

/**
 * Returns whether alt is down.
 */
public boolean isAltDown()  { return _alt; }

/**
 * Returns whether command is down.
 */
public boolean isCommandDown()  { return _command; }

/**
 * Returns whether control is down.
 */
public boolean isControlDown()  { return _control; }

/**
 * Returns whether alt is down.
 */
public boolean isShiftDown()  { return _shift; }

/**
 * Returns whether shortcut is down.
 */
public boolean isShortcutDown()  { return _shortcut; }

/**
 * Standard hashCode implementation.
 */
public int hashCode()
{
    return _kcode + (_alt? 9001 : 0) + (_shift? 9002 : 0) + (_shortcut? 9003 : 0);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    KeyCombo other = anObj instanceof KeyCombo? (KeyCombo)anObj : null; if(other==null) return false;
    return other._kcode==_kcode && other._shift==_shift &&
        other._alt==_alt && (other._command==_command && other._control==_control || other._shortcut==_shortcut);
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String mod = (_alt? "Alt " : "") + (_shortcut? "Shortcut " : "") + (_shift? "Shift " : "");
    return "KeyCombo " + mod + _kcode;
}

/** Returns mapping for part. */
private static String getPart(String part)
{
    if(part.equals("Shortcut")) return SnapUtils.isWindows? "CONTROL" : "COMMAND";
    if(part.equals("meta")) return SnapUtils.isWindows? "CONTROL" : "COMMAND";
    if(part.equals("shift")) return "SHIFT";
    return part;
}

}