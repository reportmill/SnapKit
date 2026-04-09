package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.props.Prop;
import snap.util.Convert;
import java.util.*;

/**
 * This class provides values for view style properties.
 */
public class ViewStyle implements Cloneable {

    // The view that owns this style
    private View _view;

    // The View class for this style
    private Class<? extends View> _viewClass;

    // Properties
    protected Pos _align;
    protected Insets _margin;
    protected Insets _padding;
    protected double _spacing;
    protected Paint _fill;
    protected Border _border;
    protected double _borderRadius;
    protected Font _font;
    protected Color _textColor;

    // The states available from this style
    private Map<String,ViewStyle> _states;

    // States
    public enum State { Hover, Active, Alternate, Link, Visited, Focus }

    /**
     * Constructor.
     */
    public ViewStyle()
    {
        _viewClass = View.class;
        _align = Pos.TOP_LEFT;
        _margin = Insets.EMPTY;
        _padding = Insets.EMPTY;
        _spacing = 0;
        _fill = null;
        _border = null;
        _borderRadius = 0;
        _font = null;
        _textColor = Color.BLACK;
    }

    /**
     * Constructor.
     */
    public ViewStyle(View aView)
    {
        _view = aView;
    }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()  { return _align; }

    /**
     * Returns the margin.
     */
    public Insets getMargin()  { return _margin; }

    /**
     * Returns the padding.
     */
    public Insets getPadding()  { return _padding; }

    /**
     * Returns the spacing.
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Returns fill paint.
     */
    public Paint getFill()  { return _fill; }

    /**
     * Returns the border.
     */
    public Border getBorder()  { return _border; }

    /**
     * Returns the radius for border rounded corners.
     */
    public double getBorderRadius()  { return _borderRadius; }

    /**
     * Returns the font.
     */
    public Font getFont()  { return _font; }

    /**
     * Returns the text color.
     */
    public Color getTextColor()  { return _textColor; }

    /**
     * Returns fill color.
     */
    public Color getFillColor()  { return _fill != null ? _fill.getColor() : null; }

    /**
     * Returns the border color.
     */
    public Color getBorderColor()  { return _border != null? _border.getColor() : null; }

    /**
     * Returns the view style for given state.
     */
    public ViewStyle getStyleForState(State state)  { return getStyleForStateName(state.toString()); }

    /**
     * Returns the hover view style.
     */
    public ViewStyle getHoverStyle()  { return getStyleForStateName(State.Hover.toString()); }

    /**
     * Returns the active view style.
     */
    public ViewStyle getActiveStyle()  { return getStyleForStateName(State.Active.toString()); }

    /**
     * Returns the view style for given state name.
     */
    public ViewStyle getStyleForStateName(String stateName)
    {
        if (_states == null) _states = new HashMap<>();
        ViewStyle style = _states.get(stateName);
        if (style != null)
            return style;
        _states.put(stateName, style = clone());
        return style;
    }

    /**
     * Returns value for given property name.
     */
    public Object getPropValue(String propName)
    {
        return switch (propName) {
            case View.Align_Prop -> _align;
            case View.Margin_Prop -> _margin;
            case View.Padding_Prop -> _padding;
            case View.Spacing_Prop -> _spacing;
            case View.Fill_Prop -> _fill;
            case View.Border_Prop -> _border;
            case View.BorderRadius_Prop -> _borderRadius;
            case View.Font_Prop -> _font;
            case View.TextColor_Prop -> _textColor;
            default -> { System.out.println("ViewStyle.getPropValue: Unknown property name: " + propName); yield null; }
        };
    }

    /**
     * Sets value for given property name.
     */
    public void setPropValue(String propName, Object aValue)
    {
        switch (propName) {
            case View.Align_Prop -> _align = Pos.of(aValue);
            case View.Margin_Prop -> _margin = Insets.of(aValue);
            case View.Padding_Prop -> _padding = Insets.of(aValue);
            case View.Spacing_Prop -> _spacing = Convert.doubleValue(aValue);
            case View.Fill_Prop -> _fill = Paint.of(aValue);
            case View.Border_Prop -> _border = Border.of(aValue);
            case View.BorderRadius_Prop -> _borderRadius = Convert.doubleValue(aValue);
            case View.Font_Prop -> _font = Font.of(aValue);
            case View.TextColor_Prop -> _textColor = Color.get(aValue);
            default -> System.out.println("ViewStyle.setPropValue: Unknown property name: " + propName);
        }

        if (_view != null)
            _view.getComputedStyle().resetStyleProp(propName);
    }

    /**
     * Returns the default prop value for view.
     */
    public Object getPropDefaultForView(View aView, String propName)
    {
        switch (propName) {
            case View.Align_Prop: return _align;
            case View.Margin_Prop: return _margin;
            case View.Padding_Prop: return _padding;
            case View.Spacing_Prop: return _spacing;
            case View.Fill_Prop: return _fill;
            case View.Border_Prop: return _border;
            case View.BorderRadius_Prop: return _borderRadius;
            case View.Font_Prop: return _font;
            case View.TextColor_Prop: return _textColor;
        }

        // Get prop and return DefaultValue
        Prop prop = aView.getPropForName(propName);
        if (prop != null)
            return prop.getDefaultValue();

        // Complain and return null
        System.err.println("ViewStyle.getPropDefaultForView: No default found for: " + propName);
        return null;
    }

    /**
     * Sets style values for JSON/CSS style string, e.g.: "Fill: White; Margin: 4; Font: Arial 24;"
     */
    public void setStyleString(String styleString)
    {
        // Get individual prop/value strings (separated by semi-colons)
        String[] propStrings = styleString.split("\\s*;\\s*");

        // Iterate over prop strings and add each
        for (String propString : propStrings) {

            // Get "name:value" string parts
            String[] nameValueStrings = propString.split("\\s*:\\s*");

            // If both prop/value parts found, get prop name and set value
            if (nameValueStrings.length == 2) {
                String propName = nameValueStrings[0].trim();
                setPropValue(propName, nameValueStrings[1]);
            }

            // If "name:value" parts not found, complain
            else System.err.println("ViewStyle.setStyleString: Invalid prop string: " + propString);
        }
    }

    /**
     * Sets the style property values for given view if they were previously set to default of given old style.
     */
    protected void setStyleDefaultsForViewAndOldStyle(View aView, ViewStyle oldViewStyle)
    {
        // Handle general style props
        String[] styleProps = { View.Align_Prop, View.Margin_Prop, View.Padding_Prop, View.Spacing_Prop,
                View.Fill_Prop, View.Border_Prop, View.BorderRadius_Prop };
        for (String propName : styleProps)
            setPropDefaultForView(aView, propName, oldViewStyle);

        // Handle TextColor
        if (aView.getPropForName(View.TextColor_Prop) != null)
            setPropDefaultForView(aView, View.TextColor_Prop, oldViewStyle);
    }

    /**
     * Sets the view prop value to this style default if current value matches old style default.
     */
    private void setPropDefaultForView(View aView, String propName, ViewStyle oldViewStyle)
    {
        Object viewPropValue = aView.getPropValue(propName);
        Object oldDefaultPropValue = oldViewStyle.getPropDefaultForView(aView, propName);
        if (Objects.equals(viewPropValue, oldDefaultPropValue)) {
            Object newDefaultPropValue = getPropDefaultForView(aView, propName);
            aView.setPropValue(propName, newDefaultPropValue);
        }
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public ViewStyle clone()
    {
        try { return (ViewStyle) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return _viewClass.getSimpleName() + " Style";
    }

    /**
     * Returns the ViewStyle for given class.
     */
    protected static ViewStyle getViewStyleForClassMapAndClass(Map<Class<?>, ViewStyle> viewStyles, Class<? extends View> viewClass)
    {
        // Get style from class, just return if found
        ViewStyle viewStyle = viewStyles.get(viewClass);
        if (viewStyle != null)
            return viewStyle;

        // Create style, add to cache and return
        viewStyle = getViewStyleForClassMapAndClassImpl(viewStyles, viewClass);
        viewStyles.put(viewClass, viewStyle);
        return viewStyle;
    }

    /**
     * Returns the ViewStyle for given class.
     */
    private static ViewStyle getViewStyleForClassMapAndClassImpl(Map<Class<?>, ViewStyle> viewStyles, Class<? extends View> viewClass)
    {
        Class<?> superClass = viewClass.getSuperclass();
        if (superClass != null && View.class.isAssignableFrom(superClass)) {
            ViewStyle superClassStyle = getViewStyleForClassMapAndClass(viewStyles, (Class<? extends View>) superClass);
            ViewStyle viewClassStyle = superClassStyle.clone();
            viewClassStyle._viewClass = viewClass;
            return viewClassStyle;
        }

        return null;
    }
}
