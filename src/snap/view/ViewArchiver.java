/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;

import snap.gfx.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * This class handles Snap View archival.
 */
public class ViewArchiver extends XMLArchiver {

    // Whether to use real class
    static boolean  _useRealClass = true;

    /**
     * Creates a new ViewArchiver.
     */
    public ViewArchiver()
    {
        setIgnoreCase(true);
    }

    /**
     * Returns a View for source.
     */
    public View getViewForSource(Object aSource)
    {
        return getViewForSourceAndOwner(aSource, null);
    }

    /**
     * Returns a View for source.
     */
    public View getViewForSourceAndOwner(Object aSource, Object anOwner)
    {
        setOwner(anOwner);
        return (View) readFromXMLSource(aSource);
    }

    /**
     * Returns a View for source.
     */
    public View getViewForBytes(byte[] theBytes)
    {
        return (View) readFromXMLSource(theBytes);
    }

    /**
     * Returns the class for a given element.
     */
    @Override
    protected Class<?> getClassForXML(XMLElement anElement)
    {
        Class<?> classForXML = super.getClassForXML(anElement);
        String className = anElement.getAttributeValue("class");

        if (className != null && isUseRealClass()) {
            Class<?> cls = getClassForName(className);
            if (cls != null && View.class.isAssignableFrom(cls)) {
                classForXML = cls;
            }
        }

        // Return
        return classForXML;
    }

    /**
     * Returns a class for name.
     */
    private static Class<?> getClassForName(String aClassName)
    {
        ClassLoader classLoader = ViewArchiver.class.getClassLoader();
        try {
            return Class.forName(aClassName, false, classLoader);
        }
        catch (ClassNotFoundException e) { return null; }
        catch (NoClassDefFoundError t) { System.err.println("ViewArchiver.getClassForName: " + t); return null; }
        catch (Throwable t) { System.err.println("ViewArchiver.getClassForName: " + t); return null; }
    }

    /**
     * Creates the class map.
     */
    @Override
    protected Map<String, Class> createClassMap()
    {
        // Create class map and add classes
        Map<String,Class> cmap = new HashMap<>();

        // View classes
        cmap.put("ArcView", ArcView.class);
        cmap.put("ArrowView", ArrowView.class);
        cmap.put("BorderView", BorderView.class);
        cmap.put("Box", BoxView.class);
        cmap.put("BoxView", BoxView.class);
        cmap.put("BrowserView", BrowserView.class);
        cmap.put("Button", Button.class);
        cmap.put("ColorButton", snap.viewx.ColorButton.class);
        cmap.put("ColorDock", snap.viewx.ColorDock.class);
        cmap.put("ColorWell", snap.viewx.ColorWell.class);
        cmap.put("ColView", ColView.class);
        cmap.put("CheckBox", CheckBox.class);
        cmap.put("CheckBoxMenuItem", CheckBoxMenuItem.class);
        cmap.put("ComboBox", ComboBox.class);
        cmap.put("DocView", DocView.class);
        cmap.put("HBox", RowView.class);
        cmap.put("ImageView", ImageView.class);
        cmap.put("Label", Label.class);
        cmap.put("ListView", ListView.class);
        cmap.put("Menu", Menu.class);
        cmap.put("MenuBar", MenuBar.class);
        cmap.put("MenuButton", MenuButton.class);
        cmap.put("MenuItem", MenuItem.class);
        cmap.put("PageView", PageView.class);
        cmap.put("PathView", PathView.class);
        cmap.put("ProgressBar", ProgressBar.class);
        cmap.put("RadioButton", RadioButton.class);
        cmap.put("RectView", RectView.class);
        cmap.put("RowView", RowView.class);
        cmap.put("ScaleBox", ScaleBox.class);
        cmap.put("ScrollView", ScrollView.class);
        cmap.put("Separator", Separator.class);
        cmap.put("Slider", Slider.class);
        cmap.put("SnapActor", snap.viewx.SnapActor.class);
        cmap.put("SnapScene", snap.viewx.SnapScene.class);
        cmap.put("Spinner", Spinner.class);
        cmap.put("SplitView", SplitView.class);
        cmap.put("SpringView", SpringView.class);
        cmap.put("StackView", StackView.class);
        cmap.put("StringView", StringView.class);
        cmap.put("SwitchView", SwitchView.class);
        cmap.put("TableView", TableView.class);
        cmap.put("TableCol", TableCol.class);
        cmap.put("TabView", TabView.class);
        cmap.put("TextArea", TextArea.class);
        cmap.put("TextView", TextView.class);
        cmap.put("TextField", TextField.class);
        cmap.put("ThumbWheel", ThumbWheel.class);
        cmap.put("ToggleButton", ToggleButton.class);
        cmap.put("TreeView", TreeView.class);
        cmap.put("TitleView", TitleView.class);
        cmap.put("VBox", ColView.class);
        cmap.put("View", View.class);

        // Graphics
        cmap.put("Fill", Color.class);
        cmap.put("Color", Color.class);
        cmap.put("Font", Font.class);
        cmap.put("BevelBorder", Borders.BevelBorder.class);
        cmap.put("EtchBorder", Borders.EtchBorder.class);
        cmap.put("LineBorder", Borders.LineBorder.class);
        cmap.put("GradientPaint", GradientPaint.class);
        cmap.put("ImagePaint", ImagePaint.class);
        cmap.put("BlurEffect", BlurEffect.class);
        cmap.put("ShadowEffect", ShadowEffect.class);
        cmap.put("ReflectEffect", ReflectEffect.class);
        cmap.put("EmbossEffect", EmbossEffect.class);

        // Add aliases and return cmap
        addAliases(cmap);
        return cmap;
    }

    /**
     * Adds aliases.
     */
    protected void addAliases(Map<String,Class> cmap)
    {
        // Shape classes
        cmap.put("document", DocView.class); // RMDocument.class
        cmap.put("image-shape", ImageView.class); //RMImageShape.class
        cmap.put("page", PageView.class); // RMPage.class
        cmap.put("polygon", PathView.class); //RMPolygonShape.class
        cmap.put("rect", RectView.class); //RMRectShape.class
        cmap.put("spring-shape", SpringView.class); //RMSpringShape.class
        cmap.put("text", TextView.class); //RMTextShape.class
        cmap.put("TextViewBase", TextView.class);

        // Graphics
        cmap.put("color", Color.class);
        cmap.put("font", Font.class);
        cmap.put("fill", Color.class); //RMFill.class
        cmap.put("gradient-fill", GradientPaint.class); //RMGradientFill.class
        cmap.put("image-fill", ImagePaint.class); //RMImageFill.class
        cmap.put("blur-effect", BlurEffect.class);
        cmap.put("shadow-effect", ShadowEffect.class);
        cmap.put("reflection-effect", ReflectEffect.class);
        cmap.put("emboss-effect", EmbossEffect.class);
    }

    /**
     * Returns an image for given name/path.
     */
    public Image getImage(String aPath)
    {
        // If there is an Archiver.Owner, look for image as class resource
        Class<?> ownerClass = getOwnerClass();
        for (Class<?> cls = ownerClass; cls != null && cls != ViewOwner.class; cls = cls.getSuperclass()) {
            Image image = Image.get(cls, aPath);
            if (image != null)
                return image;
        }

        // Otherwise, try to find image name as path relative to Archiver.SourceURL
        WebURL url = getSourceURL();
        return Image.get(url, aPath);
    }

    /**
     * Returns whether to use real classes.
     */
    public static boolean isUseRealClass()
    {
        return _useRealClass;
    }

    /**
     * Sets whether to use real classes.
     */
    public static void setUseRealClass(boolean aFlag)
    {
        _useRealClass = aFlag;
    }

    /**
     * Returns an image for given name/path.
     */
    public static Image getImage(XMLArchiver anArchiver, String aPath)
    {
        if (anArchiver instanceof ViewArchiver)
            return ((ViewArchiver) anArchiver).getImage(aPath);
        return null;
    }
}