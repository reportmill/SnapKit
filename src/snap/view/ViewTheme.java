package snap.view;
import snap.gfx.*;
import snap.viewx.ColorDock;
import java.util.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {

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
        initViewStyles();
    }

    /**
     * Returns the background fill.
     */
    public Paint getBackFill()
    {
        ViewStyle rootViewStyle = getStyleForClass(RootView.class);
        return rootViewStyle.getFill();
    }

    /**
     * Returns the fill for gutter areas like scrollbar background or tabview button bar background.
     */
    public Paint getGutterFill()
    {
        ViewStyle rootViewAltStyle = getStyleForClassAndState(RootView.class, PseudoClass.Alternate);
        return rootViewAltStyle.getFill();
    }

    /**
     * Returns the color for content like text fields, text areas, list areas.
     */
    public Color getContentColor()
    {
        ViewStyle textViewStyle = getStyleForClass(TextView.class);
        return textViewStyle.getFill().getColor();
    }

    /**
     * Returns the color for alternate content, like list area alternate rows.
     */
    public Color getContentAltColor()
    {
        ViewStyle listViewAltStyle = getStyleForClassAndState(ListView.class, PseudoClass.Alternate);
        return listViewAltStyle.getFill().getColor();
    }

    /**
     * Returns the selection color.
     */
    public Paint getSelectedFill()
    {
        ViewStyle listViewActiveStyle = getStyleForClassAndState(ListView.class, PseudoClass.Active);
        return listViewActiveStyle.getFill();
    }

    /**
     * Returns the targeted color.
     */
    public Paint getTargetedFill()
    {
        ViewStyle listViewHoverStyle = getStyleForClassAndState(ListView.class, PseudoClass.Hover);
        return listViewHoverStyle.getFill();
    }

    /**
     * Initialize ViewStyles.
     */
    protected void initViewStyles()
    {
        // RootView
        setViewStyleString(RootView.class, "Fill: #FA");
        setViewStyleStringForAlternate(RootView.class, "Fill: #F0");

        // Label, ButtonBase
        setViewStyleString(Label.class, "Align: CENTER_LEFT; Spacing: 4");
        setViewStyleString(ButtonBase.class, "Align: CENTER; Padding: 3; BorderRadius: 4");

        // Button
        setViewStyleString(Button.class, "Fill: WHITE; Border: #BF");
        setViewStyleStringForHover(Button.class, "Fill: #F8");
        setViewStyleStringForActive(Button.class, "Fill: #DF; Border: #87AFDA");
        setViewStyleStringForSelected(Button.class, "Fill: #DF; Border: #87AFDA");

        // ToggleButton
        setViewStyleString(ToggleButton.class, "Fill: WHITE; Border: #BF");
        setViewStyleStringForHover(ToggleButton.class, "Fill: #F8");
        setViewStyleStringForActive(ToggleButton.class, "Fill: #DF; Border: #87AFDA");
        setViewStyleStringForSelected(ToggleButton.class, "Fill: #DF; Border: #87AFDA");

        // RadioButton, CheckBox
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
        setViewStyleString(ListView.class, "Fill: WHITE; Border: #C0");
        setViewStyleStringForAlternate(ListView.class, "Fill: #F8");
        setViewStyleStringForActive(ListView.class, "Fill: #DA");
        setViewStyleStringForHover(ListView.class, "Fill: #E6; TextColor: WHITE");

        // MenuBar
        setViewStyleString(MenuBar.class, "Padding: 2, 10, 2, 10; Font: Arial 13");

        // MenuItem
        setViewStyleString(MenuItem.class, "Align: CENTER_LEFT; Padding: 4, 8, 4, 6");
        setViewStyleStringForHover(MenuItem.class, "Fill: #E6; TextColor: WHITE");
        setViewStyleString(Menu.class, "Font: Arial 13");

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

        // BoxView, RowView, ColView, StackView, TitleView, BorderView
        setViewStyleString(BoxView.class, "Align: CENTER");
        setViewStyleString(RowView.class, "Align: CENTER_LEFT");
        setViewStyleString(ColView.class, "Align: TOP_LEFT");
        setViewStyleString(StackView.class, "Align: CENTER");
        setViewStyleString(TitleView.class, "Padding: 2");
        setViewStyleString(BorderView.class, "Align: CENTER");

        // ProgressBar, ThumbWheel, ColorDock
        setViewStyleString(ProgressBar.class, "BorderRadius: 4");
        setViewStyleString(ThumbWheel.class, "Fill: #FA");
        setViewStyleString(ColorDock.class, "Border: bevel");

        // TabBar
        setViewStyleString(TabBar.class, "Padding: 3, 3, 3, 5");

        // DocView, PageView
        setViewStyleString(DocView.class, "Align: CENTER_LEFT");
        setViewStyleString(PageView.class, "Fill: WHITE; Border: BLACK");

        // ArrowView, StringView, WrapView
        setViewStyleString(ArrowView.class, "Align: CENTER");
        setViewStyleString(StringView.class, "Align: CENTER_LEFT");
        setViewStyleString(WrapView.class, "Align: CENTER");
    }

    /**
     * Returns the style for given class.
     */
    public ViewStyle getStyleForClass(Class<? extends View> viewClass)
    {
        // Get style for class, just return if found
        ViewStyle viewStyle = _viewStyles.get(viewClass);
        if (viewStyle != null)
            return viewStyle;

        // Creates style for class, adds to given map and returns it
        return getStyleForClassMapAndClass(_viewStyles, viewClass);
    }

    /**
     * Returns the style for given class and state.
     */
    public ViewStyle getStyleForClassAndState(Class<? extends View> viewClass, PseudoClass viewState)
    {
        ViewStyle viewStyle = getStyleForClass(viewClass);
        return viewStyle.getStyleForState(viewState);
    }

    /**
     * Sets a View style string for given class and style string.
     */
    public void setViewStyleString(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClass(viewClass);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View hover style string for given class and style string.
     */
    public void setViewStyleStringForHover(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, PseudoClass.Hover);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View active style string for given class and style string.
     */
    public void setViewStyleStringForActive(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, PseudoClass.Active);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View active style string for given class and style string.
     */
    public void setViewStyleStringForSelected(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, PseudoClass.Selected);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View active style string for given class and style string.
     */
    public void setViewStyleStringForAlternate(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, PseudoClass.Alternate);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View style string for given class and style string.
     */
    public void setViewStyleStringForClassAndState(Class<? extends View> viewClass, PseudoClass pseudoClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, pseudoClass);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Sets a View property value for given class, property name and value.
     */
    public void setViewStylePropValue(Class<? extends View> viewClass, String propName, Object aValue)
    {
        ViewStyle viewStyle = getStyleForClass(viewClass);
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
        ViewStyle newViewStyle = getStyleForClass(aView.getClass());
        ViewStyle oldViewStyle = oldTheme.getStyleForClass(aView.getClass());
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

    /**
     * Returns the style for given class.
     */
    protected static ViewStyle getStyleForClassMapAndClass(Map<Class<?>, ViewStyle> viewStyles, Class<? extends View> viewClass)
    {
        // Get style from class, just return if found
        ViewStyle viewStyle = viewStyles.get(viewClass);
        if (viewStyle != null)
            return viewStyle;

        // Create style, add to cache and return
        viewStyle = getStyleForClassMapAndClassImpl(viewStyles, viewClass);
        viewStyles.put(viewClass, viewStyle);
        return viewStyle;
    }

    /**
     * Returns the style for given class.
     */
    private static ViewStyle getStyleForClassMapAndClassImpl(Map<Class<?>, ViewStyle> viewStyles, Class<? extends View> viewClass)
    {
        Class<?> superClass = viewClass.getSuperclass();
        if (superClass != null && View.class.isAssignableFrom(superClass)) {
            ViewStyle superClassStyle = getStyleForClassMapAndClass(viewStyles, (Class<? extends View>) superClass);
            return superClassStyle.copyForClass(viewClass);
        }

        return null;
    }
}