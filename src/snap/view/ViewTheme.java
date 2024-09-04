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
    protected Color GUTTER_FILL = new Color("#E0"); // Color.LIGHTGRAY

    // Color for content like text fields, text areas, list areas
    protected Color CONTENT_COLOR = Color.WHITE;

    // Color for content like text fields, text areas, list areas
    protected Color CONTENT_ALT_COLOR = Color.get("#F8");

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

    // Border for distinct content areas like Scrollview, Splitview, TableView, TreeView
    protected Border CONTENT_BORDER = Border.createLineBorder(Color.get("#C0"),1);

    // The ButtonPainter
    private ButtonPainter  _buttonPainter;

    // Map of class to style
    private Map<Class<?>, ViewStyle> _viewStyles = new HashMap<>();

    // The current theme
    private static ViewTheme  _theme = getLight();

    /**
     * Constructor.
     */
    public ViewTheme()
    {
        super();

        // Initialize colors
        initColors();

        // Create and initialize ViewStyles
        _viewStyles = new HashMap<>();
        ViewStyle viewStyle = new ViewStyle(this);
        _viewStyles.put(View.class, viewStyle);
        initViewStyles();

        // Create ButtonPainter
        _buttonPainter = createButtonPainter();
    }

    /**
     * Initialize colors.
     */
    protected void initColors()  { }

    /**
     * Returns the background fill.
     */
    public Paint getBackFill()  { return BACK_FILL; }

    /**
     * Returns the fill for gutter areas like scrollbar background or tabview button bar background.
     */
    public Paint getGutterFill()  { return GUTTER_FILL; }

    /**
     * Returns the color for content like text fields, text areas, list areas.
     */
    public Color getContentColor()  { return CONTENT_COLOR; }

    /**
     * Returns the color for alternate content, like list area alternate rows.
     */
    public Color getContentAltColor()  { return CONTENT_ALT_COLOR; }

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
     * Returns the border for distinct content areas like Scrollview, Splitview, TableView, TreeView.
     */
    public Border getContentBorder()  { return CONTENT_BORDER; }

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
        labelStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        labelStyle.setPropValue(View.Spacing_Prop, 4);

        // Button
        ViewStyle buttonStyle = getViewStyleForClass(ButtonBase.class);
        buttonStyle.setPropValue(View.Align_Prop, Pos.CENTER);
        buttonStyle.setPropValue(View.BorderRadius_Prop, 4);

        // RadioButton
        ViewStyle radioButtonStyle = getViewStyleForClass(RadioButton.class);
        radioButtonStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        radioButtonStyle.setPropValue(View.Padding_Prop, new Insets(2));
        radioButtonStyle.setPropValue(View.Spacing_Prop, 5);

        // CheckBox
        ViewStyle checkBoxStyle = getViewStyleForClass(CheckBox.class);
        checkBoxStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        checkBoxStyle.setPropValue(View.Padding_Prop, new Insets(2));
        checkBoxStyle.setPropValue(View.Spacing_Prop, 5);

        // TextField
        ViewStyle textFieldStyle = getViewStyleForClass(TextField.class);
        textFieldStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        textFieldStyle.setPropValue(View.Padding_Prop, new Insets(2, 2, 2, 5));
        textFieldStyle.setPropValue(View.Fill_Prop, getContentColor());
        textFieldStyle.setPropValue(View.Border_Prop, getContentBorder().copyForInsets(Insets.EMPTY));
        textFieldStyle.setPropValue(View.BorderRadius_Prop, 3);

        // TextArea
        setViewStylePropValue(TextArea.class, View.Padding_Prop, new Insets(2));

        // ListArea
        setViewStylePropValue(ListArea.class, View.Fill_Prop, getContentColor());

        // ScrollView, SplitView, ListView, TableView, TreeView
        setViewStylePropValue(ScrollView.class, View.Border_Prop, getContentBorder());
        setViewStylePropValue(SplitView.class, View.Border_Prop, getContentBorder());
        setViewStylePropValue(ListView.class, View.Border_Prop, getContentBorder());
        setViewStylePropValue(TableView.class, View.Border_Prop, getContentBorder());
        setViewStylePropValue(TreeView.class, View.Border_Prop, getContentBorder());
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
     * Sets a View property value for given class, property name and value.
     */
    public void setViewStylePropValue(Class<? extends View> viewClass, String propName, Object aValue)
    {
        ViewStyle viewStyle = getViewStyleForClass(viewClass);
        viewStyle.setPropValue(propName, aValue);
    }

    /**
     * Returns the current theme.
     */
    public static ViewTheme get()  { return _theme; }

    /**
     * Returns the light theme.
     */
    public static ViewTheme getLight()  { return ViewThemes.getLight(); }

    /**
     * Returns the classic theme.
     */
    public static ViewTheme getClassic()  { return ViewThemes.getClassic(); }

    /**
     * Sets the style property values for given view if they were previously set to default of given old theme.
     */
    protected void setThemeStyleDefaultsForViewAndOldTheme(View aView, ViewTheme oldTheme)
    {
        ViewStyle newViewStyle = getViewStyleForClass(aView.getClass());
        ViewStyle oldViewStyle = oldTheme.getViewStyleForClass(aView.getClass());
        newViewStyle.setStyleDefaultsForViewAndOldStyle(aView, oldViewStyle);
    }

    /**
     * Sets the theme to theme for given name.
     */
    public static void setThemeForName(String aName)
    {
        ViewTheme theme = ViewThemes.getThemeForName(aName);
        if (theme != null)
            setTheme(theme);
        else System.err.println("ViewTheme.setThemeForName: Not theme for name: " + aName);
    }

    /**
     * Sets the theme.
     */
    public static void setTheme(ViewTheme newTheme)
    {
        // Set new theme
        ViewTheme oldTheme = _theme;
        _theme = newTheme;

        // Update windows
        WindowView[] openWindows = WindowView.getOpenWindows();
        for (WindowView openWindow : openWindows) {
            RootView rootView = openWindow.getRootView();
            rootView.themeChanged(oldTheme, newTheme);
            rootView.setFill(_theme.getBackFill());
            rootView.repaint();
        }
    }
}