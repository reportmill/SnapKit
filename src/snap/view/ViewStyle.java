package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;
import java.util.*;

/**
 * This class provides values for view style properties.
 */
public class ViewStyle implements Cloneable {

    // THe View class for this style
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

    /**
     * Constructor.
     */
    public ViewStyle(ViewTheme viewTheme)
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
        _textColor = viewTheme.getTextColor();
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
