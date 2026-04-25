package snap.view;
import snap.gfx.*;
import snap.util.StyleSheet;
import snap.util.StyleSheetParser;
import java.util.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {

    // Whether theme is initializing
    private boolean _initializing;

    // Map of class to style
    private Map<Class<?>, ViewStyle> _viewStyles = new HashMap<>();

    // A sample text view style for standard text styles
    private ViewStyle _sampleTextViewStyle;

    // A sample list view style for standard selection/hover styles
    private ViewStyle _sampleListViewStyle;

    // A sample root view style for standard root styles
    private ViewStyle _sampleRootViewStyle;

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
        _initializing = true;
        initViewStyles();
        _initializing = false;

        // Set sample styles
        ViewTheme oldTheme = _theme; _theme = this;
        _sampleTextViewStyle = new TextView().getStyle();
        _sampleListViewStyle = new ListView<>().getStyle();
        _sampleRootViewStyle = new RootView().getStyle();
        _theme = oldTheme;
    }

    /**
     * Returns the background fill.
     */
    public Paint getBackFill()  { return _sampleRootViewStyle.getFill(); }

    /**
     * Returns the fill for gutter areas like scrollbar background or tabview button bar background.
     */
    public Paint getGutterFill()
    {
        ViewStyle rootViewAltStyle = _sampleRootViewStyle.getStyleForState(PseudoClass.Alternate);
        return rootViewAltStyle.getFill();
    }

    /**
     * Returns the color for content like text fields, text areas, list areas.
     */
    public Color getContentColor()  { return _sampleTextViewStyle.getFill().getColor(); }

    /**
     * Returns the color for alternate content, like list area alternate rows.
     */
    public Color getContentAltColor()
    {
        if (_sampleListViewStyle == null) return Color.get("#F8");
        ViewStyle listViewAltStyle = _sampleListViewStyle.getStyleForState(PseudoClass.Alternate);
        return listViewAltStyle.getFill().getColor();
    }

    /**
     * Returns the selection color.
     */
    public Paint getSelectedFill()
    {
        ViewStyle listViewActiveStyle = _sampleListViewStyle.getActiveStyle();
        return listViewActiveStyle.getFill();
    }

    /**
     * Returns the targeted color.
     */
    public Paint getTargetedFill()
    {
        ViewStyle listViewHoverStyle = _sampleListViewStyle.getHoverStyle();
        return listViewHoverStyle.getFill();
    }

    /**
     * Returns the standard text color.
     */
    public Color getTextColor()  { return _sampleTextViewStyle.getTextColor(); }

    /**
     * Returns the standard selected text color.
     */
    public Color getSelectedTextColor()  { return _sampleListViewStyle.getActiveStyle().getTextColor(); }

    /**
     * Initialize ViewStyles.
     */
    protected void initViewStyles()
    {
        // View
        String styleSheetStr = """
        
            View { Align: TOP_LEFT; Margin: 0; Padding: 0; Spacing: 0; BorderRadius: 0; TextColor: #00 }
    
            RootView { Fill: #FA}
            RootView:Alternate { Fill: #F0 }
    
            Label { Align: CENTER_LEFT; Spacing: 4 }
    
            ButtonBase { Align: CENTER; Padding: 4; BorderRadius: 4 }
    
            Button { Fill: WHITE; Border: #BF }
            Button:Hover { Fill: #F8 }
            Button:Active { Fill: #DF; Border: #87AFDA }
            Button:Selected { Fill: #DF; Border: #87AFDA }
    
            ToggleButton { Fill: WHITE; Border: #BF }
            ToggleButton:Hover { Fill: #F8 }
            ToggleButton:Active { Fill: #DF; Border: #87AFDA }
            ToggleButton:Selected { Fill: #DF; Border: #87AFDA }
    
            RadioButton { Align: CENTER_LEFT; Padding: 2; Spacing: 5 }
            RadioButton:Hover { Fill: #F8 }
            RadioButton:Active { Fill: #DF; Border: #87AFDA }
            RadioButton:Selected { Fill: #DF; Border: #87AFDA }
    
            CheckBox { Align: CENTER_LEFT; Padding: 2; Spacing: 5 }
            CheckBox:Hover { Fill: #F8 }
            CheckBox:Active { Fill: #DF; Border: #87AFDA }
            CheckBox:Selected { Fill: #DF; Border: #87AFDA }
    
            MenuButton { Fill: WHITE; Border: #BF }
    
            MenuItem { Align: CENTER_LEFT; Padding: 4, 8, 4, 6; Font: Arial 13 }
            MenuItem:Hover { Fill: #E6; TextColor: WHITE }
    
            TextField { Align: CENTER_LEFT; Padding: 3; Fill: WHITE; Border: #C0; BorderRadius: 3 }
    
            TextView { Padding: 4; Fill: WHITE; Border: #C0; BorderRadius: 4 }
            TextView:Hover { TextColor: WHITE }
    
            ComboBox { Align: CENTER_LEFT }
            ImageView { Align: CENTER }
    
            ListView { Fill: WHITE; Border: #C0 }
            ListView:Alternate { Fill: #F8 }
            ListView:Active { Fill: #DA }
            ListView:Hover { Fill: #E6; TextColor: WHITE }
    
            MenuBar { Padding: 2, 10, 2, 10; Font: Arial 13 }
    
            ScrollView { Border: #C0 }
    
            SplitView { Border: #C0 }
    
            TableView { Fill: WHITE; Border: #C0 }
            TableCol { Fill: null; Border: null }
            TableCol:Active { Fill: #DA }
            TableCol:Hover { Fill: #E6; TextColor: WHITE }
    
            TreeView { Fill: WHITE; Border: #C0 }
            TreeCol { Fill: null; Border: null }
            TreeCol:Active { Fill: #DA }
            TreeCol:Hover { Fill: #E6; TextColor: WHITE }
    
            BoxView { Align: CENTER }
            RowView { Align: CENTER_LEFT }
            ColView { Align: TOP_LEFT }
            StackView { Align: CENTER }
            TitleView { Padding: 2 }
            BorderView { Align: CENTER }
    
            ProgressBar { Fill: WHITE; Border: #BF; BorderRadius: 4 }
            ThumbWheel { Fill: #FA }
            ColorDock { Border: bevel }
    
            TabBar { Padding: 3, 3, 3, 5 }
    
            DocView { Align: CENTER_LEFT }
            PageView { Fill: WHITE; Border: BLACK }
    
            ArrowView { Align: CENTER }
            StringView { Align: CENTER_LEFT }
            WrapView { Align: CENTER }
            """;

        // Create style sheet for string and apply
        StyleSheet styleSheet = StyleSheet.createStyleSheetForString(styleSheetStr);
        applyStyleSheet(styleSheet);
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

        // Get view style for super class
        Class<?> superClass = viewClass.getSuperclass();
        ViewStyle superClassStyle = getStyleForClass((Class<? extends View>) superClass);

        // If initializing, copy style for this class
        if (_initializing)
            superClassStyle = superClassStyle.copyForClass(viewClass);

        // Add style to cache map and return
        _viewStyles.put(viewClass, superClassStyle);
        return superClassStyle;
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
    public void setViewStyleStringForAlternate(Class<? extends View> viewClass, String styleString)
    {
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, PseudoClass.Alternate);
        viewStyle.setStyleString(styleString);
    }

    /**
     * Applies rules for given style sheet.
     */
    public void applyStyleSheet(StyleSheet styleSheet)
    {
        List<StyleSheet.Rule> rules = styleSheet.getRules();
        rules.forEach(this::applyStyleRule);
    }

    /**
     * Applies rule for given style sheet rule.
     */
    public void applyStyleRule(StyleSheet.Rule styleRule)
    {
        // Get class for rule
        String className = styleRule.selector().name();
        Class<? extends View> viewClass = getViewClassForName(className);
        if (viewClass == null) {
            System.err.println("ViewTheme.applyStyleRule: Unkown selector class: " + className);
            return;
        }

        // Get view style for class and state
        String pseudoClassStr = styleRule.selector().pseudoClass();
        PseudoClass pseudoClass = pseudoClassStr != null ? PseudoClass.valueOf(pseudoClassStr) : PseudoClass.Normal;
        ViewStyle viewStyle = getStyleForClassAndState(viewClass, pseudoClass);

        // Get declarations and apply to style
        List<StyleSheet.Declaration> declarations = styleRule.declarations();
        declarations.forEach(decl -> viewStyle.setStyleValue(decl.key(), decl.value()));
    }

    /**
     * Returns the current theme.
     */
    public static ViewTheme get()  { return _theme; }

    /**
     * Returns the light theme.
     */
    public static ViewTheme getLight()  { return ViewThemeUtils.getLight(); }

    /**
     * Returns the dark theme.
     */
    public static ViewTheme getDark()  { return ViewThemeUtils.getDark(); }

    /**
     * Sets the theme to theme for given name.
     */
    public static void setThemeForName(String aName)
    {
        ViewTheme theme = ViewThemeUtils.getThemeForName(aName);
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
     * Returns view class for given name.
     */
    private static Class<? extends View> getViewClassForName(String className)
    {
        // Try 'snap.view.' + className
        try { return (Class<? extends View>) Class.forName("snap.view." + className); }
        catch (ClassNotFoundException ignore) { }

        // Try 'snap.viewx.' + className
        try { return (Class<? extends View>) Class.forName("snap.viewx." + className); }
        catch (ClassNotFoundException e) { return null; }

    }
}