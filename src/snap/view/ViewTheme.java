package snap.view;
import snap.gfx.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {
    
    // The current theme
    private static ViewTheme     _theme = new LightTheme();

    // The last theme
    private static ViewTheme     _lastTheme = new LightTheme();

    // Color constants
    private static Color BACK_FILL = new Color("#E9E8EA");
    private static Color BACK_DARK_FILL = new Color("#C0C0C0");
    private static Color SEL_FILL = Color.LIGHTGRAY; //new Color("#0032D0");
    private static Color SEL_TEXT_FILL = Color.BLACK; //Color.WHITE;
    private static Color TARG_FILL = new Color("#4080F0");
    private static Color TARG_TEXT_FILL = Color.WHITE;

/**
 * Returns the background fill.
 */
public Paint getBackFill()  { return BACK_FILL; }

/**
 * Returns the background fill.
 */
public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

/**
 * Returns the text fill.
 */
public Paint getTextFill()  { return Color.BLACK; }

/**
 * Returns the selection color.
 */
public Paint getSelectFill()  { return SEL_FILL; }

/**
 * Returns the selection color.
 */
public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

/**
 * Returns the selection color.
 */
public Paint getTargetFill()  { return TARG_FILL; }

/**
 * Returns the selection color.
 */
public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

/**
 * Creates a button area.
 */
public ButtonArea createButtonArea()
{
    return new ButtonArea();
}

/**
 * Returns the current theme.
 */
public static ViewTheme get()  { return _theme; }

/**
 * Returns the last theme.
 */
public static ViewTheme getLast()  { return _lastTheme; }

/**
 * Sets the theme by name.
 */
public static void setThemeForName(String aName)
{
    _lastTheme = _theme;
    // Set new theme
    switch(aName) {
        case "Light": _theme = new LightTheme(); break;
        case "Dark": _theme = new DarkTheme(); break;
        default: _theme = new ViewTheme();
    }
    
    // Update windows
    WindowView wins[] = WindowView.getOpenWindows();
    for(WindowView win : wins) {
        RootView rview = win.getRootView();
        rview.themeChanged();
        rview.setFill(_theme.getBackFill());
        rview.repaint();
    }
}

/**
 * A Theme for plain rendering.
 */
private static class LightTheme extends ViewTheme {
    
    // Color constants
    private static Color BACK_FILL = new Color("#F2F2F2");
    private static Color BACK_DARK_FILL = BACK_FILL.darker().darker();
    private static Color SEL_FILL = Color.LIGHTGRAY;
    private static Color SEL_TEXT_FILL = Color.BLACK;
    private static Color TARG_FILL = new Color("#F8F8F8");
    private static Color TARG_TEXT_FILL = Color.BLACK;

    /** Returns the selection color. */
    public Paint getSelectFill()  { return SEL_FILL; }
    
    /** Returns the selection color. */
    public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }
    
    /** Returns the selection color. */
    public Paint getTargetFill()  { return TARG_FILL; }
    
    /** Returns the selection color. */
    public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }
    
    /** Returns the background fill. */
    public Paint getBackFill()  { return BACK_FILL; }
    
    /** Returns the background fill. */
    public Paint getBackDarkFill()  { return BACK_DARK_FILL; }
    
    /** Creates a button area. */
    public ButtonArea createButtonArea()
    {
        return new PlainButtonArea();
    }

}

/**
 * A ButtonArea for plain buttons.
 */
private static class PlainButtonArea extends ButtonArea {
    
    // Colors
    private static Color BUTTON_RING_COLOR = new Color("#BFBFBF");
    private static Color BUTTON_OVER_COLOR = new Color("#F8F8F8");
    private static Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF");
    private static Color BUTTON_BLUE_COLOR = new Color("#87AFDA");
    
    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void paint(Painter aPntr)
    {
        // Get fill color
        Color fill = Color.WHITE;
        if(_state==BUTTON_OVER) fill = BUTTON_OVER_COLOR;
        else if(_state==BUTTON_PRESSED) fill = BUTTON_PRESSED_COLOR;
        
        // Get shape and paint fill
        RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
        aPntr.setPaint(fill);
        aPntr.fill(rect);
        
        // Get stroke color
        Color stroke = BUTTON_RING_COLOR;
        if(_state==BUTTON_OVER) stroke = BUTTON_BLUE_COLOR;
        else if(_state==BUTTON_PRESSED) stroke = BUTTON_BLUE_COLOR;
        
        // Draw ring
        rect.setRect(_x, _y, _w, _h);
        aPntr.setColor(stroke);
        aPntr.draw(rect);
    }
}

/**
 * A Theme for dark rendering.
 */
private static class DarkTheme extends ViewTheme {
    
    // Color constants
    private static Color BACK_FILL = new Color("#3C3F41");
    private static Color BACK_DARK_FILL = BACK_FILL.darker().darker();
    private static Color SEL_FILL = Color.LIGHTGRAY;
    private static Color SEL_TEXT_FILL = Color.BLACK;
    private static Color TARG_FILL = new Color("#F8F8F8");
    private static Color TARG_TEXT_FILL = Color.BLACK;

    /** Returns the selection color. */
    public Paint getSelectFill()  { return SEL_FILL; }
    
    /** Returns the selection color. */
    public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }
    
    /** Returns the selection color. */
    public Paint getTargetFill()  { return TARG_FILL; }
    
    /** Returns the selection color. */
    public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }
    
    /** Returns the background fill. */
    public Paint getBackFill()  { return BACK_FILL; }
    
    /** Returns the background fill. */
    public Paint getBackDarkFill()  { return BACK_DARK_FILL; }
    
    /** Creates a button area. */
    public ButtonArea createButtonArea()
    {
        return new PlainButtonArea();
    }

}

}