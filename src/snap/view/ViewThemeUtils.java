package snap.view;
import java.util.Objects;

/**
 * Utility methods for ViewTheme.
 */
public class ViewThemeUtils {

    /**
     * Sets the style property values for given view if they were previously set to default of given old theme.
     */
    public static void setThemeStyleDefaultsForViewAndOldTheme(View aView, ViewTheme oldTheme, ViewTheme newTheme)
    {
        ViewStyle newClassStyle = newTheme.getStyleForClass(aView.getClass());
        ViewStyle oldClassStyle = oldTheme.getStyleForClass(aView.getClass());
        setStyleDefaultsForViewAndOldStyle(aView, oldClassStyle, newClassStyle);
    }

    /**
     * Sets the style property values for given view if they were previously set to default of given old class style.
     */
    private static void setStyleDefaultsForViewAndOldStyle(View aView, ViewStyle oldClassStyle, ViewStyle newClassStyle)
    {
        // Handle general style props
        String[] styleProps = { View.Align_Prop, View.Margin_Prop, View.Padding_Prop, View.Spacing_Prop,
                View.Fill_Prop, View.Border_Prop, View.BorderRadius_Prop };
        for (String propName : styleProps)
            setPropDefaultForView(aView, propName, oldClassStyle, newClassStyle);

        // Handle TextColor
        if (aView.getPropForName(View.TextColor_Prop) != null)
            setPropDefaultForView(aView, View.TextColor_Prop, oldClassStyle, newClassStyle);
    }

    /**
     * Sets the view prop value to this style default if current value matches old style default.
     */
    private static void setPropDefaultForView(View aView, String propName, ViewStyle oldClassStyle, ViewStyle newClassStyle)
    {
        Object viewPropValue = aView.getPropValue(propName);
        Object oldDefaultPropValue = oldClassStyle.getStyleValue(propName);
        if (Objects.equals(viewPropValue, oldDefaultPropValue)) {
            Object newDefaultPropValue = newClassStyle.getStyleValue(propName);
            aView.setPropValue(propName, newDefaultPropValue);
        }
    }
}
