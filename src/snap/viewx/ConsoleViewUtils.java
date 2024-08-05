/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.text.TextBlock;
import snap.util.Convert;
import snap.view.*;
import java.lang.reflect.Array;

/**
 * This View subclass shows snippets.
 */
class ConsoleViewUtils {

    // A helper
    private static Console.Helper _helper;

    // Constants
    public static final Font DEFAULT_FONT = Font.Arial14;
    private static final Color DEFAULT_TEXTAREA_FILL = new Color(.95);
    private static final Color DEFAULT_TEXTAREA_TEXTFILL = Color.GRAY2;

    /**
     * Override to support custom content views for response values.
     */
    public static ConsoleItemBox createBoxViewForValue(Object value)
    {
        View contentView = createContentViewForValue(value);
        return new ConsoleItemBox(contentView);
    }

    /**
     * Override to support custom content views for response values.
     */
    public static View createContentViewForValue(Object value)
    {
        // Handle View
        if (value instanceof View)
            return (View) value;

        // Handle ViewOwner
        if (value instanceof ViewOwner)
            return createContentViewForViewOwner((ViewOwner) value);

        // If snapcharts class, try to install snapcharts helper
        if (_helper == null && value != null && value.getClass().getName().startsWith("snapcharts"))
            installSnapChartsHelper();

        // Try SnapCharts helper for Chart, DataSet
        if (_helper != null) {
            View view = _helper.createViewForObject(value);
            if (view != null)
                return view;
        }

        // Handle Image
        if (value instanceof Image)
            return createContentViewForImage((Image) value);

        // Handle TextBlock
        if (value instanceof TextBlock)
            return createContentViewForTextBlock((TextBlock) value);

        // Do normal version
        String responseText = getStringForValue(value);
        TextBlock textBlock = createTextBlockForString(responseText);
        return createContentViewForTextBlock(textBlock);
    }

    /**
     * Creates TextBlock for String.
     */
    private static TextBlock createTextBlockForString(String aString)
    {
        // Create TextBlock and configure Style
        TextBlock textBlock = new TextBlock();
        textBlock.setDefaultFont(DEFAULT_FONT);
        textBlock.setDefaultTextStyle(textBlock.getDefaultTextStyle().copyFor(DEFAULT_TEXTAREA_TEXTFILL));

        // Set string
        textBlock.setString(aString);

        // Return
        return textBlock;
    }

    /**
     * Creates content view for TextBlock.
     */
    private static View createContentViewForTextBlock(TextBlock textBlock)
    {
        // Create and configure TextArea
        TextArea textArea = new TextArea(textBlock);
        textArea.setBorderRadius(4);
        textArea.setFill(DEFAULT_TEXTAREA_FILL);
        textArea.setEditable(true);

        // Configure TextArea Sizing
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 20);
        textArea.setPadding(5, 5, 5, 5);

        // If large text, wrap in ScrollView
        if (textArea.getPrefHeight() > 300) {
            ScrollView scrollView = new ScrollView(textArea);
            scrollView.setBorderRadius(4);
            scrollView.setMaxHeight(300);
            scrollView.setGrowWidth(true);
            scrollView.setGrowHeight(true);
            return scrollView;
        }

        // Return
        return textArea;
    }

    /**
     * Creates content view for ViewOwner.
     */
    private static View createContentViewForViewOwner(ViewOwner aViewOwner)
    {
        View view = aViewOwner.getUI();
        return view;
    }

    /**
     * Creates content view for Image.
     */
    private static View createContentViewForImage(Image anImage)
    {
        // Create imageView for image
        ImageView imageView = new ImageView(anImage);
        imageView.setMaxHeight(350);

        // Return
        return imageView;
    }

    /**
     * Returns the value as a string.
     */
    private static String getStringForValue(Object aValue)
    {
        // Handle null
        if (aValue == null)
            return "null";

        // Handle String
        if (aValue instanceof String)
            return (String) aValue;

        // Handle double[]
        if (aValue instanceof double[])
            return "double[] " + Convert.doubleArrayToString((double[]) aValue);

        // Try SnapCharts helper for Chart, DataSet
        if (_helper != null) {
            String str = _helper.createStringForObject(aValue);
            if (str != null)
                return str;
        }

        // Handle exception
        if (aValue instanceof Exception) {
            Exception exception = (Exception) aValue;
            Throwable rootCause = exception;
            while (rootCause.getCause() != null) rootCause = rootCause.getCause();
            return rootCause.toString();
        }

        // Handle Boolean, Number
        if (aValue instanceof Number || aValue instanceof Boolean || aValue instanceof Character)
            return aValue.toString();

        // Handle Class
        if (aValue instanceof Class<?>)
            return "Class: " + ((Class<?>) aValue).getName();

        // Handle Array
        Class<?> valueClass = aValue.getClass();
        if (valueClass.isArray()) {
            StringBuilder sb = new StringBuilder();
            int length = Array.getLength(aValue);
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(", ");
                Object val = Array.get(aValue, i);
                String str = getStringForValue(val);
                sb.append(str);
            }
            return sb.toString();
        }

        // Handle anything
        String valueStr = String.valueOf(aValue);
        String className = aValue.getClass().getSimpleName();
        if (!valueStr.startsWith(className))
            valueStr = className + ": " + aValue;
        return valueStr;
    }

    /**
     * This method tries to install snapcharts helper.
     */
    private static void installSnapChartsHelper()
    {
        try {
            Class<Console.Helper> helperClass = (Class<Console.Helper>) Class.forName("snapcharts.charts.SnapChartsUtils");
            _helper = helperClass.getConstructor().newInstance();
        }
        catch (Exception ignore) { }
    }
}
