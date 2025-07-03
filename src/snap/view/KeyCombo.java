/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.SnapEnv;

/**
 * A class to model specific key strokes, including the key code and modifiers (shift, alt, etc.).
 */
public class KeyCombo {

    // Key code
    private int _kcode;

    // Modifiers
    private boolean _shift, _control, _alt, _command, _shortcut;

    /**
     * Creates a new KeyCombo.
     */
    protected KeyCombo()
    {
    }

    /**
     * Creates a new KeyCombo for key code and modifiers.
     */
    protected KeyCombo(int aKC, boolean isShift, boolean isCntr, boolean isAlt, boolean isCmd)
    {
        _kcode = aKC;
        _shift = isShift;
        _control = isCntr;
        _alt = isAlt;
        _command = isCmd;
        _shortcut = SnapEnv.isWindows || SnapEnv.isWebVM_Windows ? isCntr : isCmd;
    }

    /**
     * Creates a new KeyCombo for given string.
     */
    public static KeyCombo get(String aStr)
    {
        String[] parts = aStr.replace("+", " ").split("\\s");
        KeyCombo keyCombo = new KeyCombo();

        for (String part : parts) {
            part = getPart(part);
            int kcode = KeyCode.get(part);
            switch (kcode) {
                case KeyCode.ALT: keyCombo._alt = true; break;
                case KeyCode.COMMAND: keyCombo._command = true; break;
                case KeyCode.CONTROL: keyCombo._control = true; break;
                case KeyCode.SHIFT: keyCombo._shift = true; break;
                default: keyCombo._kcode = kcode;
            }
        }

        // Set Shortcut
        if (keyCombo.isCommandDown() && SnapEnv.isMac)
            keyCombo._shortcut = true;
        if (keyCombo.isControlDown() && SnapEnv.isWindows)
            keyCombo._shortcut = true;
        if (aStr.contains("Shortcut"))
            keyCombo._shortcut = true;

        // Return
        return keyCombo;
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
        String mod = (_alt ? "Alt " : "") + (_shortcut ? "Shortcut " : "") + (_shift ? "Shift " : "");
        return mod + str;
    }

    /**
     * Returns whether alt is down.
     */
    public boolean isShiftDown()  { return _shift; }

    /**
     * Returns whether control is down.
     */
    public boolean isControlDown()  { return _control; }

    /**
     * Returns whether alt is down.
     */
    public boolean isAltDown()  { return _alt; }

    /**
     * Returns whether command is down.
     */
    public boolean isCommandDown()  { return _command; }

    /**
     * Returns whether shortcut is down.
     */
    public boolean isShortcutDown()  { return _shortcut; }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return _kcode + (_alt ? 9001 : 0) + (_shift ? 9002 : 0) + (_shortcut ? 9003 : 0);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        KeyCombo other = anObj instanceof KeyCombo ? (KeyCombo) anObj : null;
        if (other == null) return false;
        return other._kcode == _kcode && other._shift == _shift &&
                other._alt == _alt && (other._command == _command && other._control == _control || other._shortcut == _shortcut);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String mod = (_alt ? "Alt " : "") + (_shortcut ? "Shortcut " : "") + (_shift ? "Shift " : "");
        return "KeyCombo " + mod + _kcode;
    }

    /**
     * Returns mapping for part.
     */
    private static String getPart(String part)
    {
        if (part.equals("Shortcut")) return SnapEnv.isWindows ? "CONTROL" : "COMMAND";
        if (part.equals("meta")) return SnapEnv.isWindows ? "CONTROL" : "COMMAND";
        if (part.equals("shift")) return "SHIFT";
        return part;
    }
}