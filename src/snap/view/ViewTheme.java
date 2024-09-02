package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import java.util.HashMap;
import java.util.Map;

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

    // Map of class to style
    private Map<Class<?>, ViewStyle> _viewStyles = new HashMap<>();

    // The current theme
    private static ViewTheme  _theme = getClassic();

    /**
     * Constructor.
     */
    public ViewTheme()
    {
        super();
        _buttonPainter = createButtonPainter();

        // Create and initialize ViewStyles
        _viewStyles = new HashMap<>();
        ViewStyle viewStyle = new ViewStyle();
        _viewStyles.put(View.class, viewStyle);
        initViewStyles();
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
    public Color getTextTargetedColor()  { return TEXT_TARG_COLOR; }

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
     * Initialize ViewStyles.
     */
    protected void initViewStyles()
    {
        // Label
        ViewStyle labelStyle = getViewStyleForClass(Label.class);
        labelStyle._align = Pos.CENTER_LEFT;
        labelStyle._spacing = 4;

        // Button
        ViewStyle buttonStyle = getViewStyleForClass(ButtonBase.class);
        buttonStyle._align = Pos.CENTER;
        buttonStyle._borderRadius = 4;

        // RadioButton
        ViewStyle radioButtonStyle = getViewStyleForClass(RadioButton.class);
        radioButtonStyle._align = Pos.CENTER_LEFT;
        radioButtonStyle._padding = new Insets(2);
        radioButtonStyle._spacing = 5;

        // CheckBox
        ViewStyle checkBoxStyle = getViewStyleForClass(CheckBox.class);
        checkBoxStyle._align = Pos.CENTER_LEFT;
        checkBoxStyle._padding = new Insets(2);
        checkBoxStyle._spacing = 5;

        // TextField
        ViewStyle textFieldStyle = getViewStyleForClass(TextField.class);
        textFieldStyle._align = Pos.CENTER_LEFT;
        textFieldStyle._padding = new Insets(2, 2, 2, 5);
        textFieldStyle._fill = getContentColor();
        textFieldStyle._border = Border.createLineBorder(Color.LIGHTGRAY, 1);
        textFieldStyle._borderRadius = 3;
    }

    /**
     * Returns the ViewStyle for given class.
     */
    public ViewStyle getViewStyleForClass(Class<? extends View> viewClass)
    {
        // Get style for class, just return if found
        ViewStyle viewStyle = _viewStyles.get(viewClass);
        if (viewStyle != null)
            return viewStyle;

        // Creates style for class, adds to given map and returns it
        return ViewStyle.getViewStyleForClassMapAndClass(_viewStyles, viewClass);
    }

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