/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.SnapEnv;

/**
 * A class to model a key stroke, including the key code and modifiers (shift, alt, control, meta).
 */
public class KeyCombo {

    // Key code
    private int _keyCode;

    // Whether shift key is down
    private boolean _shiftDown;

    // Whether control key is down
    private boolean _controlDown;

    // Whether alt key is down
    private boolean _altDown;

    // Whether meta key is down ('command' key on Mac, 'windows' key on Windows/Linux)
    private boolean _metaDown;

    // Whether shortcut key is down
    private boolean _shortcut;

    /**
     * Constructor for key code and modifiers.
     */
    protected KeyCombo(int keyCode, boolean shiftDown, boolean controlDown, boolean altDown, boolean metaDown)
    {
        _keyCode = keyCode;
        _shiftDown = shiftDown;
        _controlDown = controlDown;
        _altDown = altDown;
        _metaDown = metaDown;
        _shortcut = SnapEnv.isShortcutControlKey ? controlDown : metaDown;
    }

    /**
     * Creates a new KeyCombo for given key combo string.
     */
    public static KeyCombo get(String keyComboString)
    {
        int keyCode = 0;
        boolean shiftDown = false;
        boolean controlDown = false;
        boolean altDown = false;
        boolean metaDown = false;

        // Get key combo string parts and process to find modifiers and key code
        String[] keyComboStrParts = keyComboString.split("[\\s+]");
        for (String partName : keyComboStrParts) {
            String partNameNormalized = getPartNameNormalized(partName);
            int partKeyCode = KeyCode.getKeyCodeForName(partNameNormalized);
            switch (partKeyCode) {
                case KeyCode.SHIFT -> shiftDown = true;
                case KeyCode.CONTROL -> controlDown = true;
                case KeyCode.ALT -> altDown = true;
                case KeyCode.META -> metaDown = true;
                default -> keyCode = partKeyCode;
            }
        }

        // Return
        return new KeyCombo(keyCode, shiftDown, controlDown, altDown, metaDown);
    }

    /**
     * Returns the KeyCode.
     */
    public int getKeyCode()  { return _keyCode; }

    /**
     * Returns the name.
     */
    public String getName()
    {
        String modStr = (_altDown ? "Alt " : "") + (_shortcut ? "Shortcut " : "") + (_shiftDown ? "Shift " : "");
        return modStr + KeyCode.getNameForKeyCode(_keyCode);
    }

    /**
     * Returns whether shift key is down.
     */
    public boolean isShiftDown()  { return _shiftDown; }

    /**
     * Returns whether control key is down.
     */
    public boolean isControlDown()  { return _controlDown; }

    /**
     * Returns whether alt key is down.
     */
    public boolean isAltDown()  { return _altDown; }

    /**
     * Returns whether meta key is down ('command' key on Mac, 'windows' key on Windows/Linux).
     */
    public boolean isMetaDown()  { return _metaDown; }

    /**
     * Returns whether shortcut key is down.
     */
    public boolean isShortcutDown()  { return _shortcut; }

    /**
     * Standard hashCode implementation.
     */
    @Override
    public int hashCode()
    {
        return _keyCode + (_shiftDown ? 1024 : 0) + (_controlDown ? 2048 : 0) + (_altDown ? 4096 : 0) + (_metaDown ? 8192 : 0);
    }

    /**
     * Standard equals implementation.
     */
    @Override
    public boolean equals(Object anObj)
    {
        return anObj instanceof KeyCombo other && other._keyCode == _keyCode && other._shiftDown == _shiftDown &&
            other._controlDown == _controlDown && other._altDown == _altDown && other._metaDown == _metaDown;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String mod = (_altDown ? "Alt " : "") + (_shortcut ? "Shortcut " : "") + (_shiftDown ? "Shift " : "");
        return "KeyCombo " + mod + _keyCode;
    }

    /**
     * Returns the normalized name for given key combo part name.
     */
    private static String getPartNameNormalized(String partName)
    {
        return switch (partName) {
            case "Shortcut" -> SnapEnv.isShortcutControlKey ? "CONTROL" : "META";
            case "meta" -> "META";
            case "shift" -> "SHIFT";
            default -> partName;
        };
    }
}