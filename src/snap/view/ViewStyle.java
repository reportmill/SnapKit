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

    // The state of this style
    private PseudoClass _state = PseudoClass.Normal;

    // The normal style
    private ViewStyle _normalStyle;

    // The states available from this style
    private Map<PseudoClass,ViewStyle> _states;

    // Properties
    protected Pos _align;
    protected Insets _margin;
    protected Insets _padding;
    protected Double _spacing;
    protected Paint _fill;
    protected Border _border;
    protected Double _borderRadius;
    protected Font _font;
    protected Color _textColor;

    /**
     * Constructor.
     */
    public ViewStyle()
    {
        _viewClass = View.class;
        _normalStyle = this;
        _align = Pos.TOP_LEFT;
        _margin = Insets.EMPTY;
        _padding = Insets.EMPTY;
        _spacing = 0d;
        _fill = null;
        _border = null;
        _borderRadius = 0d;
        _font = null;
        _textColor = Color.BLACK;
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
     * Returns the font.
     */
    public Font getFont()  { return _font; }

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
            case View.Spacing_Prop -> _spacing = Convert.getDouble(aValue);
            case View.Fill_Prop -> _fill = Paint.of(aValue);
            case View.Border_Prop -> _border = Border.of(aValue);
            case View.BorderRadius_Prop -> _borderRadius = Convert.getDouble(aValue);
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
        // Get class style for state
        ViewStyle classStyle = this;
        if (_view != null)
            classStyle = _view.getClassStyleForState(newState);

        // Copy style, set state and return
        ViewStyle newStyle = classStyle.clone();
        newStyle._state = newState;
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
}
