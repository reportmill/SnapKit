package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Color;
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
        setPropValue(View.Align_Prop, Pos.TOP_LEFT);
        setPropValue(View.Margin_Prop, Insets.EMPTY);
        setPropValue(View.Padding_Prop, Insets.EMPTY);
        setPropValue(View.Spacing_Prop, 0d);
        setPropValue(View.BorderRadius_Prop, 0d);
        setPropValue(View.TextColor_Prop, Color.BLACK);
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
}
