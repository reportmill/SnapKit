package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.Convert;
import snap.util.FormatUtils;
import java.util.*;

/**
 * This class provides values for view style properties.
 */
public class ViewStyle implements Cloneable {

    // The view that owns this style
    private View _view;

    // The View class for this style
    private Class<? extends View> _viewClass;

    // The state of this style
    private PseudoClass _state = PseudoClass.Normal;

    // The style values
    private Map<String,String> _values = new HashMap<>();

    // The normal style
    private ViewStyle _normalStyle;

    // The states available from this style
    private Map<PseudoClass,ViewStyle> _states;

    // The computed values
    private Map<String,Object> _computedValues = new HashMap<>();

    // A placeholder for a null fill and border
    public static final Color NULL_FILL = new Color(.92);
    public static final Border NULL_BORDER = Border.createLineBorder(Color.PINK, 1);

    /**
     * Constructor.
     */
    public ViewStyle()
    {
        _viewClass = View.class;
        _normalStyle = this;
    }

    /**
     * Constructor.
     */
    public ViewStyle(View aView)
    {
        _view = aView;
        _viewClass = aView.getClass();
        _normalStyle = this;
    }

    /**
     * Returns whether this style is normal style (not pseudo style).
     */
    public boolean isNormalStyle()  { return _normalStyle == this; }

    /**
     * Returns the normal style (if this style is for pseudo class).
     */
    public ViewStyle getNormalStyle()  { return _normalStyle; }

    /**
     * Returns whether style has given prop name set.
     */
    public boolean isPropSet(String propName)  { return _values.containsKey(propName); }

    /**
     * Returns value for given property name.
     */
    public String getStyleValue(String propName)  { return _values.get(propName); }

    /**
     * Sets value for given property name.
     */
    public void setStyleValue(String propName, Object aValue)
    {
        if (aValue instanceof String valueStr) {
            _values.put(propName, valueStr);
            _computedValues.remove(propName);
        }
        else if (getClassForPropName(propName).isInstance(aValue))
            setComputedValue(propName, aValue);
        else if (aValue == null) {
            _values.remove(propName);
            _computedValues.remove(propName);
        }
    }

    /**
     * Returns the style value for given property name, forwarding to style hierarchy.
     */
    private String getStyleValueDeep(String propName)
    {
        // Get prop value and return if set
        String value = getStyleValue(propName);
        if (value != null)
            return value;

        // Handle view + state style: try resolving with class style for state, then normal view style
        if (_view != null && _state != PseudoClass.Normal) {

            // Try resolving with class style for state
            ViewStyle classStyle = _view.getClassStyle();
            ViewStyle classStyleForState = classStyle.getStyleForState(_state);
            value = classStyleForState.getStyleValue(propName);
            if (value != null)
                return value;

            // Forward to view normal style
            ViewStyle viewNormalStyle = getNormalStyle();
            return viewNormalStyle.getStyleValueDeep(propName);
        }

        // Handle view normal style: try resolving with class style
        if (_view != null) {
            ViewStyle classStyle = _view.getClassStyle();
            value = classStyle.getStyleValue(propName);
            if (value != null)
                return value;
        }

        // Handle class + state style: Try class normal style
        if (!isNormalStyle()) {
            ViewStyle normalStyle = getNormalStyle();
            return normalStyle.getStyleValueDeep(propName);
        }

        // Return not found
        return  null;
    }

    /**
     * Returns value for given property name.
     */
    public Object getComputedValue(String propName)
    {
        Class<?> valueClass = getClassForPropName(propName);
        return getComputedValue(propName, valueClass);
    }

    /**
     * Returns value for given property name.
     */
    public <T> T getComputedValue(String propName, Class<T> valueClass)
    {
        // If computed value already set, just return
        Object value = _computedValues.get(propName);
        if (value != null)
            return (T) value;

        // Get generic style value
        String styleValue = getStyleValueDeep(propName);
        if (styleValue == null)
            return null;

        // Convert to class, add to cache and return
        T computedValue = convertStyleValueToClass(styleValue, valueClass);
        _computedValues.put(propName, computedValue);
        return computedValue;
    }

    /**
     * Sets a computed value for given property name.
     */
    public void setComputedValue(String propName, Object value)
    {
        if (value != null) {
            _computedValues.put(propName, value);
            String styleString = convertComputedValueToString(value);
            _values.put(propName, styleString);
        }

        else {
            _computedValues.remove(propName);
            _values.remove(propName);
        }
    }

    /**
     * Returns the view style for given state.
     */
    public ViewStyle getStyleForState(PseudoClass viewState)
    {
        // If normal state requested, just return it
        if (viewState == PseudoClass.Normal)
            return _normalStyle;

        // If this isn't normal state, forward to normal state
        if (this != _normalStyle)
            return _normalStyle.getStyleForState(viewState);

        // If state is in cache just return
        if (_states == null) _states = new HashMap<>();
        ViewStyle style = _states.get(viewState);
        if (style != null)
            return style;

        // Create state, add to cache map and return
        ViewStyle newStyle = copyForState(viewState);
        _states.put(viewState, newStyle);
        return newStyle;
    }

    /**
     * Convenience method to return hover view style.
     */
    public ViewStyle getHoverStyle()  { return getStyleForState(PseudoClass.Hover); }

    /**
     * Convenience method to return active view style.
     */
    public ViewStyle getActiveStyle()  { return getStyleForState(PseudoClass.Active); }

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
                setStyleValue(propName, nameValueStrings[1]);
            }

            // If "name:value" parts not found, complain
            else System.err.println("ViewStyle.setStyleString: Invalid prop string: " + propString);
        }
    }

    /**
     * Convenience method to return computed align.
     */
    public Pos getAlign()  { return getComputedValue(View.Align_Prop, Pos.class); }

    /**
     * Convenience method to return computed margin.
     */
    public Insets getMargin()  { return getComputedValue(View.Margin_Prop, Insets.class); }

    /**
     * Convenience method to return computed padding.
     */
    public Insets getPadding()  { return getComputedValue(View.Padding_Prop, Insets.class); }

    /**
     * Convenience method to return computed spacing.
     */
    public double getSpacing()
    {
        Double spacing = getComputedValue(View.Spacing_Prop, Double.class);
        return spacing != null ? spacing : 0;
    }

    /**
     * Convenience method to return computed fill.
     */
    public Paint getFill()
    {
        Paint fill = getComputedValue(View.Fill_Prop, Paint.class);
        return fill == NULL_FILL ? null : fill;
    }

    /**
     * Convenience method to return computed border.
     */
    public Border getBorder()
    {
        Border border = getComputedValue(View.Border_Prop, Border.class);
        return border == NULL_BORDER ? null : border;
    }

    /**
     * Convenience method to return computed border radius.
     */
    public double getBorderRadius()
    {
        Double borderRadius = getComputedValue(View.BorderRadius_Prop, Double.class);
        return borderRadius != null ? borderRadius : 0;
    }

    /**
     * Convenience method to return computed font.
     */
    public Font getFont()  { return getComputedValue(View.Font_Prop, Font.class); }

    /**
     * Convenience method to return computed text color.
     */
    public Color getTextColor()  { return getComputedValue(View.TextColor_Prop, Color.class); }

    /**
     * Returns value class for given property name.
     */
    public Class<?> getClassForPropName(String propName)
    {
        return switch (propName) {
            case View.Align_Prop -> Pos.class;
            case View.Margin_Prop, View.Padding_Prop -> Insets.class;
            case View.Spacing_Prop, View.BorderRadius_Prop -> Double.class;
            case View.Fill_Prop -> Paint.class;
            case View.Border_Prop -> Border.class;
            case View.Font_Prop -> Font.class;
            case View.TextColor_Prop -> Color.class;
            default -> throw new RuntimeException("ViewStyle.getComputedValue: Unknown prop name: " + propName);
        };
    }

    /**
     * Returns a copy of this style for given class.
     */
    protected ViewStyle copyForClass(Class<? extends View> viewClass)
    {
        ViewStyle newStyle = clone();
        newStyle._viewClass = viewClass;
        newStyle._normalStyle = newStyle;
        return newStyle;
    }

    /**
     * Returns a copy of this style for given state.
     */
    private ViewStyle copyForState(PseudoClass newState)
    {
        // Copy style, set state and return
        ViewStyle newStyle = new ViewStyle();
        newStyle._view = _view;
        newStyle._viewClass = _viewClass;
        newStyle._state = newState;
        newStyle._normalStyle = _normalStyle;
        return newStyle;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public ViewStyle clone()
    {
        ViewStyle clone;
        try { clone = (ViewStyle) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone._values = new HashMap<>(_values);
        clone._states = null;
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        if (_state == PseudoClass.Normal)
            return _viewClass.getSimpleName() + " Style";
        return _viewClass.getSimpleName() + ':' + _state + " Style";
    }

    /**
     * Converts value to class.
     */
    private static <T> T convertStyleValueToClass(Object styleValue, Class<T> valueClass)
    {
        if (valueClass == Pos.class)
            return (T) Pos.of(styleValue);
        if (valueClass == Insets.class)
            return (T) Insets.of(styleValue);
        if (valueClass == Color.class)
            return (T) Color.get(styleValue);
        if (valueClass == Paint.class) {
            Paint paint = Paint.of(styleValue);
            if (paint == null && "null".equals(styleValue))
                paint = NULL_FILL;
            return (T) paint;
        }
        if (valueClass == Border.class) {
            Border border = Border.of(styleValue);
            if (border == null && "null".equals(styleValue))
                border = NULL_BORDER;
            return (T) border;
        }
        if (valueClass == Font.class)
            return (T) Font.of(styleValue);
        if (valueClass == Double.class)
            return (T) Convert.getDouble(styleValue);
        if (styleValue == null)
            return null;

        // If not known class, complain
        System.out.println("ComputedStyle.convertStyleValueToClass: Unknown conversion for class: " + styleValue);
        return valueClass.isInstance(styleValue) ? valueClass.cast(styleValue) : null;
    }

    /**
     * Converts a style value to string.
     */
    private static String convertComputedValueToString(Object styleValue)
    {
        if (styleValue instanceof Pos)
            return styleValue.toString();
        if (styleValue instanceof Insets insets)
            return insets.getString();
        if (styleValue instanceof Paint paint)
            return paint == NULL_FILL ? "null" : paint.codeString();
        if (styleValue instanceof Border border)
            return border == NULL_BORDER ? "null" : border.codeString();
        if (styleValue instanceof Font font)
            return font.codeString();
        if (styleValue instanceof Double doubleValue)
            return FormatUtils.formatNum(doubleValue);

        // If not known class, complain
        System.out.println("ComputedStyle.convertComputedValueToString: Unknown conversion for class: " + styleValue.getClass());
        return styleValue.toString();
    }
}
