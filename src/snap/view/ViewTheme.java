package snap.view;
import snap.gfx.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {

    // Background fill
    protected Color BACK_FILL = new Color("#E9E8EA");

    // Far background fill, like gutters of scroll bar or tabview buttons
    protected Color GUTTER_FILL = new Color("#C0"); // Color.LIGHTGRAY

    // Color for content like text fields, text areas, list areas
    protected Color CONTENT_COLOR = Color.WHITE;

    // Selection fill, like list items
    protected Color SEL_FILL = new Color("#C0"); // Color.LIGHTGRAY

    // Targeted fill, like mouse over list items
    protected Color TARG_FILL = new Color("#D0");

    // Text color
    protected Color TEXT_COLOR = Color.BLACK;

    // Text color for text in selected list item
    protected Color TEXT_SEL_COLOR = Color.BLACK;

    // Text color for text in targeted list item
    protected Color TEXT_TARG_COLOR = Color.WHITE;

    // Main button color
    protected Color BUTTON_COLOR = Color.WHITE;

    // Button color when mouse over
    protected Color BUTTON_OVER_COLOR = new Color("#F8F8F8");

    // Button color when pressed
    protected Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF");

    // Button border color
    protected Color BUTTON_BORDER_COLOR = new Color("#BFBFBF");

    // Button border pressed color
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
     * Returns the fill for gutter areas like scrollbar background or tabview button bar background.
     */
    public Paint getGutterFill()  { return GUTTER_FILL; }

    /**
     * Returns the Color for content like text fields, text areas, list areas.
     */
    public Color getContentColor()  { return CONTENT_COLOR; }

    /**
     * Returns the selection color.
     */
    public Paint getSelectedFill()  { return SEL_FILL; }

    /**
     * Returns the targeted color.
     */
    public Paint getTargetedFill()  { return TARG_FILL; }

    /**
     * Returns the text color.
     */
    public Color getTextColor()  { return TEXT_COLOR; }

    /**
     * Returns the text color when in selected item.
     */
    public Color getTextSelectedColor()  { return TEXT_SEL_COLOR; }

    /**
     * Returns the text color when in targeted item.
     */
    public Paint getTextTargetedColor()  { return TEXT_TARG_COLOR; }

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