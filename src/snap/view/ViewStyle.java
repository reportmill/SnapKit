package snap.view;
import snap.geom.*;
import snap.gfx.*;
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

    // The state of this style
    private PseudoClass _state = PseudoClass.Normal;

    // The style values
    private Map<String,Object> _values = new HashMap<>();

    // The parent style
    private ViewStyle _parent;

    // The normal style
    private ViewStyle _normalStyle;

    // The states available from this style
    private Map<PseudoClass,ViewStyle> _states;

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
     * Returns the parent style.
     */
    public ViewStyle getParent()  { return _parent; }

    /**
     * Returns whether style has given prop name set.
     */
    public boolean isPropSet(String propName)  { return _values.containsKey(propName); }

    /**
     * Returns value for given property name.
     */
    public Object getPropValue(String propName)  { return _values.get(propName); }

    /**
     * Sets value for given property name.
     */
    public void setPropValue(String propName, Object aValue)
    {
        _values.put(propName, aValue);

        if (_view != null)
            _view.getComputedStyle().resetStyleProp(propName);
    }

    /**
     * Returns value for given property name.
     */
    protected Object getPropValueDeep(String propName)
    {
        // Get prop value and return if set
        Object value = getPropValue(propName);
        if (value != null)
            return value;

        // Try view style parent
        ViewStyle parentStyle = getParent();
        if (parentStyle != null)
            return parentStyle.getPropValueDeep(propName);

        // Return not found
        return  null;
    }

    /**
     * Returns value for given property name.
     */
    public Object getComputedValue(String propName)
    {
        Class<?> valueClass = switch (propName) {
            case View.Align_Prop -> Pos.class;
            case View.Margin_Prop, View.Padding_Prop -> Insets.class;
            case View.Spacing_Prop, View.BorderRadius_Prop -> Double.class;
            case View.Fill_Prop -> Paint.class;
            case View.Border_Prop -> Border.class;
            case View.Font_Prop -> Font.class;
            case View.TextColor_Prop -> Color.class;
            default -> throw new RuntimeException("ViewStyle.getComputedValue: Unknown prop name: " + propName);
        };
        return getComputedValue(propName, valueClass);
    }

    /**
     * Returns value for given property name.
     */
    public <T> T getComputedValue(String propName, Class<T> valueClass)
    {
        // If computed value already set, just return
        //Object value = _computedValues.get(propName);
        //if (value != null) return (T) value;

        // Get raw style value
        Object styleValue = computeValueForPropName(propName);
        if (styleValue == null)
            return null;

        // Convert to class, add to cache and return
        T computedValue = convertStyleValueToClass(styleValue, valueClass);
        //_computedValues.put(propName, computedValue);
        return computedValue;
    }

    /**
     * Returns computed value.
     */
    private Object computeValueForPropName(String propName)
    {
        // Get prop value and return if set
        Object value = getPropValueDeep(propName);
        if (value != null)
            return value;

        // If this is class style, return not found
        if (_view == null)
            return  null;

        // Get class style (or class state style if state provided)
        ViewStyle classStyle = _view.getClassStyle();
        if (_state != PseudoClass.Normal)
            classStyle = classStyle.getStyleForState(_state);

        // Return value for class style
        return classStyle.getPropValueDeep(propName);
    }

    /**
     * Returns the hover view style.
     */
    public ViewStyle getHoverStyle()  { return getStyleForState(PseudoClass.Hover); }

    /**
     * Returns the active view style.
     */
    public ViewStyle getActiveStyle()  { return getStyleForState(PseudoClass.Active); }

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
        newStyle._parent = this;
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
        if (valueClass == Paint.class)
            return (T) Paint.of(styleValue);
        if (valueClass == Border.class)
            return (T) Border.of(styleValue);
        if (valueClass == Font.class)
            return (T) Font.of(styleValue);
        if (valueClass == Color.class)
            return (T) Color.get(styleValue);
        if (valueClass == Double.class)
            return (T) Convert.getDouble(styleValue);
        if (styleValue == null)
            return null;

        // If not known class, complain
        System.out.println("ComputedStyle.convertStyleValueToClass: Unknown conversion for class: " + styleValue);
        return valueClass.isInstance(styleValue) ? valueClass.cast(styleValue) : null;
    }
}
