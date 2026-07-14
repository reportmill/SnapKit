/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to facilitate working with keyboard key codes.
 */
public class KeyCode {

    // Constants for key codes (letters)
    public static final int A = 'A';
    public static final int B = 'B';
    public static final int C = 'C';
    public static final int D = 'D';
    public static final int E = 'E';
    public static final int F = 'F';
    public static final int G = 'G';
    public static final int H = 'H';
    public static final int I = 'I';
    public static final int J = 'J';
    public static final int K = 'K';
    public static final int L = 'L';
    public static final int M = 'M';
    public static final int N = 'N';
    public static final int O = 'O';
    public static final int P = 'P';
    public static final int Q = 'Q';
    public static final int R = 'R';
    public static final int S = 'S';
    public static final int T = 'T';
    public static final int U = 'U';
    public static final int V = 'V';
    public static final int W = 'W';
    public static final int X = 'X';
    public static final int Y = 'Y';
    public static final int Z = 'Z';
    public static final int DIGIT0 = '0';
    public static final int DIGIT1 = '1';
    public static final int DIGIT2 = '2';
    public static final int DIGIT3 = '3';
    public static final int DIGIT4 = '4';
    public static final int DIGIT5 = '5';
    public static final int DIGIT6 = '6';
    public static final int DIGIT7 = '7';
    public static final int DIGIT8 = '8';
    public static final int DIGIT9 = '9';
    public static final int NUMPAD0 = 0x60;
    public static final int NUMPAD1 = 0x61;
    public static final int NUMPAD2 = 0x62;
    public static final int NUMPAD3 = 0x63;
    public static final int NUMPAD4 = 0x64;
    public static final int NUMPAD5 = 0x65;
    public static final int NUMPAD6 = 0x66;
    public static final int NUMPAD7 = 0x67;
    public static final int NUMPAD8 = 0x68;
    public static final int NUMPAD9 = 0x69;

    // Arrows
    public static final int LEFT = 0x25;    //KeyEvent.VK_LEFT;
    public static final int UP = 0x26;      //KeyEvent.VK_UP;
    public static final int RIGHT = 0x27;   //KeyEvent.VK_RIGHT;
    public static final int DOWN = 0x28;    //KeyEvent.VK_DOWN;

    // Modifiers
    public static final int ALT = 0x12;      //KeyEvent.VK_ALT;
    public static final int SHIFT = 0x10;    //KeyEvent.VK_SHIFT;
    public static final int META = 0x9D;  //KeyEvent.VK_META;
    public static final int CONTROL = 0x11;  //KeyEvent.VK_CONTROL;

    // Miscellaneous
    public static final int OPEN_BRACKET = 0x5B;     //KeyEvent.VK_OPEN_BRACKET;
    public static final int CLOSE_BRACKET = 0x5D;    //KeyEvent.VK_CLOSE_BRACKET;
    public static final int BRACELEFT = 0xA1;        //KeyEvent.VK_BRACELEFT;
    public static final int BRACERIGHT = 0xA2;       //KeyEvent.VK_BRACERIGHT;
    public static final int SLASH = 0x2F;            //KeyEvent.VK_SLASH;
    public static final int BACK_SLASH = 0x5C;       //KeyEvent.VK_BACK_SLASH;
    public static final int BACK_SPACE = '\b';       //KeyEvent.VK_BACK_SPACE;
    public static final int COMMA = 0x2C;            //KeyEvent.VK_COMMA;
    public static final int DELETE = 0x7F;           //KeyEvent.VK_DELETE;
    public static final int END = 0x23;              //KeyEvent.VK_END;
    public static final int ENTER = '\n';            //KeyEvent.VK_ENTER;
    public static final int EQUALS = 0x3D;           //KeyEvent.VK_EQUALS;
    public static final int ESCAPE = 0x1B;           //KeyEvent.VK_ESCAPE;
    public static final int HOME = 0x24;             //KeyEvent.VK_HOME;
    public static final int MINUS = 0x2D;            //KeyEvent.VK_MINUS;
    public static final int PERIOD = 0x2E;           //KeyEvent.VK_PERIOD;
    public static final int PLUS = 0x0209;           //KeyEvent.VK_PLUS;
    public static final int SEMICOLON = 0x3B;        //KeyEvent.VK_SEMICOLON;
    public static final int SPACE = 0x20;            //KeyEvent.VK_SPACE;
    public static final int TAB = '\t';              //KeyEvent.VK_TAB;
    public static final int PAGE_UP = 33;            //KeyEvent.VK_PAGE_UP;
    public static final int PAGE_DOWN = 34;          //KeyEvent.VK_PAGE_DOWN;

    // Constant for char
    public static final char CHAR_UNDEFINED = 0xFFFF; //KeyEvent.CHAR_UNDEFINED;

    /**
     * Returns a keycode for given key name.
     */
    public static int getKeyCodeForName(String keyName)
    {
        String keyNameUC = keyName.toUpperCase();
        Integer keyCode = _keyCodes.get(keyNameUC);
        if (keyCode != null)
            return keyCode;
        System.out.println("KeyCode.getKeyCodeForName: Undefined: " + keyNameUC);
        return snap.view.KeyCode.CHAR_UNDEFINED;
    }

    /**
     * Returns the key name for given key code.
     */
    public static String getNameForKeyCode(int keyCode)
    {
        String keyName = _keyNames.get(keyCode);
        if (keyName != null)
            return keyName;
        System.out.println("KeyCode.getNameForKeyCode: Undefined: " + keyCode);
        return "";
    }

    static Map<String, Integer> _keyCodes = getKeyCodes();
    static Map<Integer, String> _keyNames = getKeyNames();

    private static Map<String, Integer> getKeyCodes()
    {
        Map<String,Integer> keyCodes = new HashMap<>();
        keyCodes.put("A", A);
        keyCodes.put("B", B);
        keyCodes.put("C", C);
        keyCodes.put("D", D);
        keyCodes.put("E", E);
        keyCodes.put("F", F);
        keyCodes.put("G", G);
        keyCodes.put("H", H);
        keyCodes.put("I", I);
        keyCodes.put("J", J);
        keyCodes.put("K", K);
        keyCodes.put("L", L);
        keyCodes.put("M", M);
        keyCodes.put("N", N);
        keyCodes.put("O", O);
        keyCodes.put("P", P);
        keyCodes.put("Q", Q);
        keyCodes.put("R", R);
        keyCodes.put("S", S);
        keyCodes.put("T", T);
        keyCodes.put("U", U);
        keyCodes.put("V", V);
        keyCodes.put("W", W);
        keyCodes.put("X", X);
        keyCodes.put("Y", Y);
        keyCodes.put("Z", Z);
        keyCodes.put("0", DIGIT0);
        keyCodes.put("1", DIGIT1);
        keyCodes.put("2", DIGIT2);
        keyCodes.put("3", DIGIT3);
        keyCodes.put("4", DIGIT4);
        keyCodes.put("5", DIGIT5);
        keyCodes.put("6", DIGIT6);
        keyCodes.put("7", DIGIT7);
        keyCodes.put("8", DIGIT8);
        keyCodes.put("9", DIGIT9);
        keyCodes.put("NUMPAD0", NUMPAD0);
        keyCodes.put("NUMPAD1", NUMPAD1);
        keyCodes.put("NUMPAD2", NUMPAD2);
        keyCodes.put("NUMPAD3", NUMPAD3);
        keyCodes.put("NUMPAD4", NUMPAD4);
        keyCodes.put("NUMPAD5", NUMPAD5);
        keyCodes.put("NUMPAD6", NUMPAD6);
        keyCodes.put("NUMPAD7", NUMPAD7);
        keyCodes.put("NUMPAD8", NUMPAD8);
        keyCodes.put("NUMPAD9", NUMPAD9);
        keyCodes.put("LEFT", LEFT);
        keyCodes.put("UP", UP);
        keyCodes.put("RIGHT", RIGHT);
        keyCodes.put("DOWN", DOWN);
        keyCodes.put("ALT", ALT);
        keyCodes.put("SHIFT", SHIFT);
        keyCodes.put("META", META);
        keyCodes.put("CONTROL", CONTROL);
        keyCodes.put("OPEN_BRACKET", OPEN_BRACKET);
        keyCodes.put("CLOSE_BRACKET", CLOSE_BRACKET);
        keyCodes.put("[", OPEN_BRACKET);
        keyCodes.put("]", CLOSE_BRACKET);
        keyCodes.put("BRACELEFT", BRACELEFT);
        keyCodes.put("BRACERIGHT", BRACERIGHT);
        keyCodes.put("{", BRACELEFT);
        keyCodes.put("}", BRACERIGHT);
        keyCodes.put("SLASH", SLASH);
        keyCodes.put("BACK_SLASH", BACK_SLASH);
        keyCodes.put("BACK_SPACE", BACK_SPACE);
        keyCodes.put("COMMA", COMMA);
        keyCodes.put("DELETE", DELETE);
        keyCodes.put("END", END);
        keyCodes.put("ENTER", ENTER);
        keyCodes.put("EQUALS", EQUALS);
        keyCodes.put("ESCAPE", ESCAPE);
        keyCodes.put("HOME", HOME);
        keyCodes.put("MINUS", MINUS);
        keyCodes.put("PERIOD", PERIOD);
        keyCodes.put("PLUS", PLUS);
        keyCodes.put("SEMICOLON", SEMICOLON);
        keyCodes.put("SPACE", SPACE);
        keyCodes.put("TAB", TAB);
        keyCodes.put(";", SEMICOLON);
        keyCodes.put("/", SLASH);
        keyCodes.put("\\", BACK_SLASH);
        keyCodes.put("ESC", ESCAPE);
        return keyCodes;
    }

    private static Map<Integer, String> getKeyNames()
    {
        Map<Integer,String> keyNames = new HashMap<>();
        for (Map.Entry<String, Integer> entry : _keyCodes.entrySet())
            keyNames.put(entry.getValue(), entry.getKey());
        return keyNames;
    }
}