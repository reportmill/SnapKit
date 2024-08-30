package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Font;
import snap.gfx.Paint;
import java.util.*;

/**
 * This class provides values for view style properties.
 */
public class ViewStyle {

    // Properties
    protected Pos _align;
    protected Insets _margin;
    protected Insets _padding;
    protected double _spacing;
    protected Paint _fill;
    protected Border _border;
    protected double _borderRadius;
    protected Font _font;

    // Map of class to style
    private static Map<Class<?>, ViewStyle> _classStyles = new HashMap<>();

    /**
     * Constructor.
     */
    public ViewStyle()
    {
        _align = Pos.TOP_LEFT;
        _margin = Insets.EMPTY;
        _padding = Insets.EMPTY;
        _spacing = 0;
        _fill = null;
        _border = null;
        _borderRadius = 0;
        _font = Font.Arial11;
    }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()  { return _align; }

    /**
     * Returns the ViewStyle for given class.
     */
    public static ViewStyle getViewStyleForClass(Class<? extends View> viewClass)
    {
        // Get style from class, just return if found
        ViewStyle style = _classStyles.get(viewClass);
        if (style != null)
            return style;

        // Create style, add to cache and return
        style = getViewStyleForClassImpl(viewClass);
        _classStyles.put(viewClass, style);
        return style;
    }

    /**
     * Returns the ViewStyle for given class.
     */
    private static ViewStyle getViewStyleForClassImpl(Class<? extends View> viewClass)
    {
        Class<?> superClass = viewClass.getSuperclass();
        if (superClass != null && superClass.isAssignableFrom(View.class))
            return getViewStyleForClass((Class<? extends View>) superClass);
        return null;
    }
}
