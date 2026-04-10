package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.Convert;
import java.util.List;

/**
 * This class resolves the style properties for a view.
 */
public class ComputedStyle {

    // The View
    private View _view;

    // Cached align
    private Pos _align;

    // Cached margin
    private Insets _margin;

    // Cached padding
    private Insets _padding;

    // Cached spacing
    private double _spacing;

    // Cached fill
    private Paint _fill;

    // Cached border
    private Border _border;

    // Cached border radius
    private double _borderRadius;

    // Cached font
    private Font _font;

    // Cached text color
    private Color _textColor;

    // A placeholder for a null fill and border
    public static final Color NULL_FILL = new Color(.92);
    public static final Border NULL_BORDER = Border.createLineBorder(Color.PINK, 1);

    // All properties
    private static List<String> ALL_PROPS = List.of(View.Align_Prop, View.Margin_Prop, View.Padding_Prop,
            View.Spacing_Prop, View.Fill_Prop, View.Border_Prop, View.BorderRadius_Prop, View.Font_Prop, View.TextColor_Prop);

    /**
     * Constructor.
     */
    public ComputedStyle(View aView)
    {
        _view = aView;
        resetAll();
    }

    /**
     * Returns the align.
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
     * Returns the fill.
     */
    public Paint getFill()  { return _fill == NULL_FILL ? null : _fill; }

    /**
     * Returns the border.
     */
    public Border getBorder()  { return _border == NULL_BORDER ? null : _border; }

    /**
     * Returns the border radius.
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
     * Resets all properties.
     */
    public void resetAll()
    {
        ALL_PROPS.forEach(this::resetStyleProp);
    }

    /**
     * Called to mark given prop dirty.
     */
    public void resetStyleProp(String propName)
    {
        switch (propName) {
            case View.Align_Prop -> _align = (Pos) computeValueForPropName(propName);
            case View.Margin_Prop -> _margin = (Insets) computeValueForPropName(propName);
            case View.Padding_Prop -> _padding = (Insets) computeValueForPropName(propName);
            case View.Spacing_Prop -> _spacing = Convert.doubleValue(computeValueForPropName(propName));
            case View.Fill_Prop -> _fill = (Paint) computeValueForPropName(propName);
            case View.Border_Prop -> _border = (Border) computeValueForPropName(propName);
            case View.BorderRadius_Prop -> _borderRadius = Convert.doubleValue(computeValueForPropName(propName));
            case View.Font_Prop -> _font = (Font) computeValueForPropName(propName);
            case View.TextColor_Prop -> _textColor = (Color) computeValueForPropName(propName);
        }
    }

    /**
     * Returns computed value.
     */
    private Object computeValueForPropName(String propName)
    {
        ViewStyle viewStyle = _view.getStyle();
        Object value = viewStyle.getPropValue(propName);
        if (value == null) {
            ViewStyle classStyle = ViewTheme.get().getViewStyleForClassAndState(_view.getClass(), _view.getStyleState());
            value = classStyle.getPropValue(propName);
        }
        return value;
    }
}
