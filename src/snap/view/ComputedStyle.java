package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class resolves the style properties for a view.
 */
public class ComputedStyle {

    // The View
    private View _view;

    // The style state
    private PseudoClass _viewState;

    // The computed values
    private Map<String,Object> _computedValues = new HashMap<>();

    // A placeholder for a null fill and border
    public static final Color NULL_FILL = new Color(.92);
    public static final Border NULL_BORDER = Border.createLineBorder(Color.PINK, 1);

    /**
     * Constructor.
     */
    public ComputedStyle(View aView)
    {
        _view = aView;
    }

    /**
     * Returns the align.
     */
    public Pos getAlign()  { return getComputedValue(View.Align_Prop, Pos.class); }

    /**
     * Returns the margin.
     */
    public Insets getMargin()  { return getComputedValue(View.Margin_Prop, Insets.class); }

    /**
     * Returns the padding.
     */
    public Insets getPadding()  { return getComputedValue(View.Padding_Prop, Insets.class); }

    /**
     * Returns the spacing.
     */
    public double getSpacing()
    {
        Double spacing = getComputedValue(View.Spacing_Prop, Double.class);
        return spacing != null ? spacing : 0;
    }

    /**
     * Returns the fill.
     */
    public Paint getFill()
    {
        Paint fill = getComputedValue(View.Fill_Prop, Paint.class);
        return fill == NULL_FILL ? null : fill;
    }

    /**
     * Returns the border.
     */
    public Border getBorder()
    {
        Border border = getComputedValue(View.Border_Prop, Border.class);
        return border == NULL_BORDER ? null : border;
    }

    /**
     * Returns the border radius.
     */
    public double getBorderRadius()
    {
        Double borderRadius = getComputedValue(View.BorderRadius_Prop, Double.class);
        return borderRadius != null ? borderRadius : 0;
    }

    /**
     * Returns the font.
     */
    public Font getFont()  { return getComputedValue(View.Font_Prop, Font.class); }

    /**
     * Returns the text color.
     */
    public Color getTextColor()  { return getComputedValue(View.TextColor_Prop, Color.class); }

    /**
     * Returns the hover style.
     */
    public ComputedStyle getHoverStyle()  { return getStyleForState(PseudoClass.Hover); }

    /**
     * Returns the active style.
     */
    public ComputedStyle getActiveStyle()  { return getStyleForState(PseudoClass.Active); }

    /**
     * Returns the computed style for given state.
     */
    public ComputedStyle getStyleForState(PseudoClass viewSate)
    {
        ComputedStyle styleForState = new ComputedStyle(_view);
        styleForState._viewState = viewSate;
        return styleForState;
    }

    /**
     * Resets all properties.
     */
    public void resetAll()
    {
        _computedValues.clear();
    }

    /**
     * Called to mark given prop dirty.
     */
    public void resetStyleProp(String propName)
    {
        _computedValues.remove(propName);
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

        // Get view style - get state style if state provided
        ViewStyle viewStyle = _view.getStyle();
        if (_viewState != null)
            viewStyle = viewStyle.getStyleForState(_viewState);

        // Get computed value for prop name (just return if null)
        T computedValue = viewStyle.getComputedValue(propName, valueClass);
        if (computedValue == null)
            return null;

        // Add to cache and return
        _computedValues.put(propName, computedValue);
        return computedValue;
    }
}
