package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.viewx.ColorDock;
import java.util.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {

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
    private static ViewTheme  _theme = getLight();

    /**
     * Constructor.
     */
    public ViewTheme()
    {
        super();

        // Initialize ViewStyles
        ViewStyle viewStyle = new ViewStyle();
        _viewStyles.put(View.class, viewStyle);

        // Initialize styles
        initColors();
        initViewStyles();

        // Create ButtonPainter
        _buttonPainter = new ButtonPainter(this);
    }

    /**
     * Initialize colors.
     */
    protected void initColors()  { }

    /**
     * Returns the background fill.
     */
    public Paint getBackFill()
    {
        ViewStyle rootViewStyle = getViewStyleForClass(RootView.class);
        return rootViewStyle.getFill();
    }

    /**
     * Returns the fill for gutter areas like scrollbar background or tabview button bar background.
     */
    public Paint getGutterFill()
    {
        ViewStyle rootViewAltStyle = getViewStyleForClassAndState(RootView.class, ViewStyle.State.Alternate);
        return rootViewAltStyle.getFill();
    }

    /**
     * Returns the color for content like text fields, text areas, list areas.
     */
    public Color getContentColor()
    {
        ViewStyle textViewStyle = getViewStyleForClass(TextView.class);
        return textViewStyle.getFill().getColor();
    }

    /**
     * Returns the color for alternate content, like list area alternate rows.
     */
    public Color getContentAltColor()
    {
        ViewStyle listViewAltStyle = getViewStyleForClassAndState(ListView.class, ViewStyle.State.Alternate);
        return listViewAltStyle.getFill().getColor();
    }

    /**
     * Returns the selection color.
     */
    public Paint getSelectedFill()
    {
        ViewStyle listViewActiveStyle = getViewStyleForClassAndState(ListView.class, ViewStyle.State.Active);
        return listViewActiveStyle.getFill();
    }

    /**
     * Returns the targeted color.
     */
    public Paint getTargetedFill()
    {
        ViewStyle listViewHoverStyle = getViewStyleForClassAndState(ListView.class, ViewStyle.State.Hover);
        return listViewHoverStyle.getFill();
    }

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
     * Initialize ViewStyles.
     */
    protected void initViewStyles()
    {
        // RootView
        setViewStyleString(RootView.class, "Fill: #FA");
        setViewStyleStringForAlternate(RootView.class, "Fill: #F0");

        // Label, ButtonBase, RadioButton, CheckBox
        setViewStyleString(Label.class, "Align: CENTER_LEFT; Spacing: 4");
        setViewStyleString(ButtonBase.class, "Align: CENTER; Padding: 3; BorderRadius: 4");
        setViewStyleString(RadioButton.class, "Align: CENTER_LEFT; Padding: 2; Spacing: 5");
        setViewStyleString(CheckBox.class, "Align: CENTER_LEFT; Padding: 2; Spacing: 5");

        // TextField
        setViewStyleString(TextField.class, "Align: CENTER_LEFT; Padding: 3; Fill: WHITE; Border: #C0; BorderRadius: 3");

        // TextView
        setViewStyleString(TextView.class, "Padding: 4; Fill: WHITE; Border: #C0; BorderRadius: 4");
        setViewStyleStringForHover(TextView.class, "TextColor: WHITE");

        // ComboBox, ImageView
        setViewStyleString(ComboBox.class, "Align: CENTER_LEFT");
        setViewStyleString(ImageView.class, "Align: CENTER");

        // ListView
        setViewStyleString(ListView.class, "Fill: WHITE; Border: #CO");
        setViewStyleStringForAlternate(ListView.class, "Fill: #F8");
        setViewStyleStringForActive(ListView.class, "Fill: #DA");
        setViewStyleStringForHover(ListView.class, "Fill: #E6; TextColor: WHITE");

        // MenuBar
        setViewStyleString(MenuBar.class, "Padding: 2, 10, 2, 10");

        // MenuItem
        setViewStyleString(MenuItem.class, "Align: CENTER_LEFT; Padding: 4, 8, 4, 6");
        setViewStyleStringForHover(MenuItem.class, "Fill: #E6; TextColor: WHITE");

        // ScrollView
        setViewStyleString(ScrollView.class, "Border: #C0");

        // SplitView
        setViewStyleString(SplitView.class, "Border: #C0");

        // TableView
        setViewStyleString(TableView.class, "Fill: WHITE; Border: #C0");
        setViewStyleString(TableCol.class, "Fill: null; Border: null");

        // TreeView
        setViewStyleString(TreeView.class, "Fill: WHITE; Border: #C0");
        setViewStyleString(TreeCol.class, "Fill: null; Border: null");

        // Label
        //ViewStyle labelStyle = getViewStyleForClass(Label.class);
        //labelStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        //labelStyle.setPropValue(View.Spacing_Prop, 4);

        // ButtonBase
        //ViewStyle buttonStyle = getViewStyleForClass(ButtonBase.class);
        //buttonStyle.setPropValue(View.Align_Prop, Pos.CENTER);
        //buttonStyle.setPropValue(View.Padding_Prop, new Insets(3));
        //buttonStyle.setPropValue(View.BorderRadius_Prop, 4);

        // RadioButton
        //ViewStyle radioButtonStyle = getViewStyleForClass(RadioButton.class);
        //radioButtonStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        //radioButtonStyle.setPropValue(View.Padding_Prop, new Insets(2));
        //radioButtonStyle.setPropValue(View.Spacing_Prop, 5);

        // CheckBox
        //ViewStyle checkBoxStyle = getViewStyleForClass(CheckBox.class);
        //checkBoxStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        //checkBoxStyle.setPropValue(View.Padding_Prop, new Insets(2));
        //checkBoxStyle.setPropValue(View.Spacing_Prop, 5);

        // TextField
        //ViewStyle textFieldStyle = getViewStyleForClass(TextField.class);
        //textFieldStyle.setPropValue(View.Align_Prop, Pos.CENTER_LEFT);
        //textFieldStyle.setPropValue(View.Padding_Prop, new Insets(3));
        //textFieldStyle.setPropValue(View.Fill_Prop, getContentColor());
        //textFieldStyle.setPropValue(View.Border_Prop, getContentBorder());
        //textFieldStyle.setPropValue(View.BorderRadius_Prop, 3);

        // TextView
        //setViewStylePropValue(TextView.class, View.Padding_Prop, new Insets(4));
        //setViewStylePropValue(TextView.class, View.Fill_Prop, getContentColor());
        //setViewStylePropValue(TextView.class, View.Border_Prop, getContentBorder());
        //setViewStylePropValue(TextView.class, View.BorderRadius_Prop, 4);

        // ComboBox, ImageView
        //setViewStylePropValue(ComboBox.class, View.Align_Prop, Pos.CENTER_LEFT);
        //setViewStylePropValue(ImageView.class, View.Align_Prop, Pos.CENTER);

        // BoxView, RowView, ColView, StackView, TitleView
        setViewStylePropValue(BoxView.class, View.Align_Prop, Pos.CENTER);
        setViewStylePropValue(RowView.class, View.Align_Prop, Pos.CENTER_LEFT);
        setViewStylePropValue(ColView.class, View.Align_Prop, Pos.TOP_LEFT);
        setViewStylePropValue(StackView.class, View.Align_Prop, Pos.CENTER);
        setViewStylePropValue(TitleView.class, View.Padding_Prop, new Insets(2));

        // ScrollView, SplitView
        //setViewStylePropValue(ScrollView.class, View.Border_Prop, getContentBorder());
        //setViewStylePropValue(SplitView.class, View.Border_Prop, getContentBorder());

        // ListView
        //setViewStylePropValue(ListView.class, View.Fill_Prop, getContentColor());
        //setViewStylePropValue(ListView.class, View.Border_Prop, getContentBorder());

        // TableView
        //setViewStylePropValue(TableView.class, View.Fill_Prop, getContentColor());
        //setViewStylePropValue(TableView.class, View.Border_Prop, getContentBorder());
        //setViewStylePropValue(TableCol.class, View.Fill_Prop, null);
        //setViewStylePropValue(TableCol.class, View.Border_Prop, null);

        // TreeView
        //setViewStylePropValue(TreeView.class, View.Fill_Prop, getContentColor());
        //setViewStylePropValue(TreeView.class, View.Border_Prop, getContentBorder());
        //setViewStylePropValue(TreeCol.class, View.Fill_Prop, null);
        //setViewStylePropValue(TreeCol.class, View.Border_Prop, null);

        // MenuBar, MenuItem
        //setViewStylePropValue(MenuBar.class, View.Padding_Prop, new Insets(2, 10, 2, 10));
        //setViewStylePropValue(MenuItem.class, View.Align_Prop, Pos.CENTER_LEFT);
        //setViewStylePropValue(MenuItem.class, View.Padding_Prop, new Insets(4, 8, 4, 6));

        // ProgressBar, ThumbWheel, ColorDock
        setViewStylePropValue(ProgressBar.class, View.BorderRadius_Prop, 4);
        setViewStylePropValue(ThumbWheel.class, View.Fill_Prop, getBackFill());
        setViewStylePropValue(ColorDock.class, View.Border_Prop, Border.createLoweredBevelBorder());

        // Define Style
//        String style = """
//                Label { Align: CENTER_LEFT; Spacing: 4 }
//                ButtonBase { Align: CENTER; Padding: 3; BorderRadius: 4 }
//                RadioButton { Align: CENTER_LEFT; Padding 2; Spacing 5 }
//                CheckBox { Align: CENTER_LEFT; Padding: 2; Spacing: 5 }
//                TextField { Align: CENTER_LEFT; Padding: 3; Fill: ContentColor; Border: ContentBorder; BorderRadius: 3 }
//                TextView { Padding: 4; Fill: ContentColor; Border: ContentBorder; BorderRadius: 4 }
//                ComboBox { Align: CENTER_LEFT }
//                ImageView { Align: Align: CENTER }
//                BoxView { Align: CENTER }
//                RowView { Align: CENTER_LEFT }
//                ColView { Align: TOP_LEFT }
//                StackView { Align: CENTER }
//                TitleView { Padding: 2 }
//                ScrollView { Border: ContentBorder }
//                SplitView { Border: ContentBorder }
//                ListView { Fill: ContentColor; Border: ContentBorder }
//                TableView { Fill: ContentColor; Border: ContentBorder }
//                TableCol { Fill: null; Border: null }
//                TreeView { Fill: ContentColor; Border: ContentBorder }
//                TreeCol { Fill: null; Border: null }
//                MenuBar { Padding: 2, 10, 2, 10 }
//                MenuItem { Align: CENTER_LEFT; Padding: 4, 8, 4, 6 }
//                ProgressBar { BorderRadius: 4 }
//                ThumbWheel { Fill: BackFill }
//                ColorDock { Border: LoweredBevelBorder }
//                """;
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
     * Returns the ViewStyle for given class and state.
     */
    public ViewStyle getViewStyleForClassAndState(Class<? extends View> viewClass, ViewStyle.State state)
    {
        return getViewStyleForClass(viewClass).getStyleForState(state);
    }

    /**
     * Sets a View style string for given class and style string.
     */
    public void setViewStyleString(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getViewStyleForClass(viewClass);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View hover style string for given class and style string.
     */
    public void setViewStyleStringForHover(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getViewStyleForClassAndState(viewClass, ViewStyle.State.Hover);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View active style string for given class and style string.
     */
    public void setViewStyleStringForActive(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getViewStyleForClassAndState(viewClass, ViewStyle.State.Active);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View active style string for given class and style string.
     */
    public void setViewStyleStringForAlternate(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getViewStyleForClassAndState(viewClass, ViewStyle.State.Alternate);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View style string for given class and style string.
     */
    public void setViewStyleStringForClassAndState(Class<? extends View> viewClass, ViewStyle.State state, String styleString)
    {
        ViewStyle viewStyle = getViewStyleForClassAndState(viewClass, state);
        viewStyle.setStyleString(styleString);
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
     * Returns the dark theme.
     */
    public static ViewTheme getDark()  { return ViewThemes.getDark(); }

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

            // Notify all views
            RootView rootView = openWindow.getRootView();
            rootView.handleThemeChange(oldTheme, newTheme);
            rootView.setFill(_theme.getBackFill());

            // Notify all controllers
            Set<ViewController> windowControllers = new LinkedHashSet<>();
            findViewControllersForView(openWindow, windowControllers);
            windowControllers.forEach(viewCon -> viewCon.handleThemeChange(oldTheme, newTheme));

            // Repaint
            rootView.repaint();
        }
    }

    /**
     * Finds all window controllers.
     */
    private static void findViewControllersForView(View aView, Set<ViewController> viewControllers)
    {
        ViewController viewController = aView.getController();
        if (viewController != null)
            viewControllers.add(viewController);
        if (aView instanceof ParentView parentView)
            parentView.getChildren().forEach(childView -> findViewControllersForView(childView, viewControllers));
    }
}