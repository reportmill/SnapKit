package snap.view;
import snap.gfx.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {

    // Color constants
    protected Color BACK_FILL = new Color("#E9E8EA");
    protected Color BACK_DARK_FILL = new Color("#C0"); // Color.LIGHTGRAY
    protected Color SEL_FILL = new Color("#C0"); // Color.LIGHTGRAY
    protected Color TARG_FILL = new Color("#D0");
    protected Color TEXT_FILL = Color.BLACK;
    protected Color SEL_TEXT_FILL = Color.BLACK;
    protected Color TARG_TEXT_FILL = Color.WHITE;

    // Button colors
    protected Color BUTTON_COLOR = Color.WHITE;
    protected Color BUTTON_OVER_COLOR = new Color("#F8F8F8");
    protected Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF");
    protected Color BUTTON_BORDER_COLOR = new Color("#BFBFBF");
    protected Color BUTTON_BORDER_PRESSED_COLOR = new Color("#87AFDA");

    // The ButtonPainter
    private ButtonPainter  _buttonPainter;

    // The current theme
    private static ViewTheme  _theme = getClassic();

    /**
     * Constructor.
     */
    public ViewTheme()
    {
        super();
        _buttonPainter = createButtonPainter();
    }

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
    public Paint getTextFill()  { return TEXT_FILL; }

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
     * Returns the button color.
     */
    public Color getButtonColor()  { return BUTTON_COLOR; }

    /**
     * Returns the button over color.
     */
    public Color getButtonOverColor()  { return BUTTON_OVER_COLOR; }

    /**
     * Returns the button pressed color.
     */
    public Color getButtonPressedColor()  { return BUTTON_PRESSED_COLOR; }

    /**
     * Returns the button border color.
     */
    public Color getButtonBorderColor()  { return BUTTON_BORDER_COLOR; }

    /**
     * Returns the button border pressed color.
     */
    public Color getButtonBorderPressedColor()  { return BUTTON_BORDER_PRESSED_COLOR; }

    /**
     * Returns the button painter.
     */
    public ButtonPainter getButtonPainter()  { return _buttonPainter; }

    /**
     * Creates the button painter.
     */
    protected ButtonPainter createButtonPainter()  { return new ButtonPainter.Flat(this); }

    /**
     * Returns the current theme.
     */
    public static ViewTheme get()  { return _theme; }

    /**
     * Returns the classic theme.
     */
    public static ViewTheme getClassic()  { return ViewThemes.getClassic(); }

    /**
     * Sets the theme by name.
     */
    public static void setThemeForName(String aName)
    {
        // Set new theme
        _theme = ViewThemes.getThemeForName(aName);

        // Update windows
        WindowView[] openWindows = WindowView.getOpenWindows();
        for (WindowView openWindow : openWindows) {
            RootView rootView = openWindow.getRootView();
            rootView.themeChanged();
            rootView.setFill(_theme.getBackFill());
            rootView.repaint();
        }
    }
}