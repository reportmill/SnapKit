package snap.view;
import snap.gfx.Color;
import snap.gfx.Paint;

/**
 * This class resolves the style properties for a view.
 */
public class ComputedStyle {

    // The View
    private View _view;

    // The view style
    private ViewStyle _viewStyle;

    // Cached fill
    private Paint _fill;

    // Constants
    private static final Paint NULL_FILL = new Color(0, 0, 0);

    /**
     * Constructor.
     */
    public ComputedStyle(View aView)
    {
        _view = aView;
        _viewStyle = aView.getStyle2();
    }

    /**
     * Returns the fill.
     */
    public Paint getFill()
    {
        if (_fill == null) resetStyle();
        return _fill == NULL_FILL ? null : _fill;
    }

    /**
     * Called to mark given prop dirty.
     */
    public void resetStyleProp(String propName)
    {
        switch (propName) {
            case View.Fill_Prop -> _fill = null;
            case View.Border_Prop -> _fill = null;
            case View.BorderRadius_Prop -> _fill = null;
        }
    }

    /**
     * Resets this style.
     */
    public void resetStyle()
    {
        ViewStyle classStyle = ViewTheme.get().getViewStyleForClass(_view.getClass());
        _fill = _viewStyle.getFill();
        if (_fill == null)
            _fill = classStyle.getFill();
        if (_fill == null)
            _fill = NULL_FILL;
    }
}
