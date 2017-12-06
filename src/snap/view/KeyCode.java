/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;

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
    public static final int COMMAND = 0x9D;  //KeyEvent.VK_META;
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
    
    // Constant for char
    public static final char CHAR_UNDEFINED = 0xFFFF; //KeyEvent.CHAR_UNDEFINED;

/**
 * Returns a Snap keycode for JFX key code.
 */
public static int get(String aName)
{
    aName = aName.toUpperCase();
    Integer val = _keyCodes.get(aName); if(val!=null) return val;
    System.out.println("KeyCode.get: Undefined: " + aName);
    return snap.view.KeyCode.CHAR_UNDEFINED;
}

/**
 * Returns a Snap keycode for JFX key code.
 */
public static String getName(int aVal)
{
    String val = _keyNames.get(aVal); if(val!=null) return val;
    System.out.println("KeyCode.getName: Undefined: " + aVal);
    return "";
}

static Map <String, Integer> _keyCodes = getKeyCodes();
static Map <Integer, String> _keyNames = getKeyNames();
private static Map <String, Integer> getKeyCodes()
{
    Map m = new HashMap();
    m.put("A", A); m.put("B", B); m.put("C", C); m.put("D", D); m.put("E", E); m.put("F", F); m.put("G", G);
    m.put("H", H); m.put("I", I); m.put("J", J); m.put("K", K); m.put("L", L); m.put("M", M); m.put("N", N);
    m.put("O", O); m.put("P", P); m.put("Q", Q); m.put("R", R); m.put("S", S); m.put("T", T); m.put("U", U);
    m.put("V", V); m.put("W", W); m.put("X", X); m.put("Y", Y); m.put("Z", Z);
    m.put("0", DIGIT0); m.put("1", DIGIT1); m.put("2", DIGIT2); m.put("3", DIGIT3); m.put("4", DIGIT4);
    m.put("5", DIGIT5); m.put("6", DIGIT6); m.put("7", DIGIT7); m.put("8", DIGIT8); m.put("9", DIGIT9);
    m.put("NUMPAD0", NUMPAD0); m.put("NUMPAD1", NUMPAD1); m.put("NUMPAD2", NUMPAD2); m.put("NUMPAD3", NUMPAD3);
    m.put("NUMPAD4", NUMPAD4); m.put("NUMPAD5", NUMPAD5); m.put("NUMPAD6", NUMPAD6); m.put("NUMPAD7", NUMPAD7);
    m.put("NUMPAD8", NUMPAD8); m.put("NUMPAD9", NUMPAD9);
    m.put("LEFT", LEFT); m.put("UP", UP); m.put("RIGHT", RIGHT); m.put("DOWN", DOWN);
    m.put("ALT", ALT); m.put("SHIFT", SHIFT); m.put("COMMAND", COMMAND); m.put("CONTROL", CONTROL);
    m.put("OPEN_BRACKET", OPEN_BRACKET); m.put("CLOSE_BRACKET", CLOSE_BRACKET);
    m.put("[", OPEN_BRACKET); m.put("]", CLOSE_BRACKET);
    m.put("BRACELEFT", BRACELEFT); m.put("BRACERIGHT", BRACERIGHT); m.put("{", BRACELEFT); m.put("}", BRACERIGHT);
    m.put("SLASH", SLASH); m.put("BACK_SLASH", BACK_SLASH); m.put("BACK_SPACE", BACK_SPACE); m.put("COMMA", COMMA);
    m.put("DELETE", DELETE); m.put("END", END); m.put("ENTER", ENTER); m.put("EQUALS", EQUALS);
    m.put("ESCAPE", ESCAPE); m.put("HOME", HOME); m.put("MINUS", MINUS); m.put("PERIOD", PERIOD);
    m.put("PLUS", PLUS); m.put("SEMICOLON", SEMICOLON); m.put("SPACE", SPACE); m.put("TAB", TAB);
    m.put(";", SEMICOLON); m.put("/", SLASH); m.put("\\", BACK_SLASH); m.put("ESC", ESCAPE);
    return m;
}
private static Map <Integer,String> getKeyNames()
{
    Map m = new HashMap();
    for(Map.Entry <String,Integer> entry : _keyCodes.entrySet()) m.put(entry.getValue(), entry.getKey());
    return m;
}

}