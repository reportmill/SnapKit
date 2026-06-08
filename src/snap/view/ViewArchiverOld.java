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
public class ViewArchiverOld extends XMLArchiver {

    /**
     * Creates a new ViewArchiver.
     */
    public ViewArchiverOld()
    {
        setIgnoreCase(true);
    }

    /**
     * Returns a View for source.
     */
    public View getViewForBytes(byte[] theBytes)
    {
        return (View) readXmlFromBytes(theBytes);
    }

    /**
     * Returns the class for a given element.
     */
    @Override
    protected Class<?> getClassForXML(XMLElement anElement)
    {
        Class<?> classForXML = super.getClassForXML(anElement);

        // If real class name is present, try to evaluate it
        String realClassName = anElement.getAttributeValue("Class");
        if (realClassName != null && isUseRealClass()) {
            Class<?> realClass = getRealClassForName(realClassName);
            if (realClass != null && View.class.isAssignableFrom(realClass))
                classForXML = realClass;
        }

        // Return
        return classForXML;
    }

    /**
     * Returns a class for name.
     */
    private Class<?> getRealClassForName(String aClassName)
    {
        Class<?> ownerClass = getOwnerClass();
        ClassLoader classLoader = ownerClass != null ? ownerClass.getClassLoader() : View.class.getClassLoader();
        try { return Class.forName(aClassName, false, classLoader); }
        catch (ClassNotFoundException e) { return null; }
        catch (NoClassDefFoundError t) { System.err.println("ViewArchiver.getClassForName: " + t); return null; }
    }

    /**
     * Creates the class map.
     */
    @Override
    protected Map<String, Class<?>> createClassMap()
    {
        // Create class map and add classes
        Map<String,Class<?>> cmap = new HashMap<>();

        // View classes
        cmap.put("ActorView", snap.games.ActorView.class);
        cmap.put("BorderView", BorderView.class);
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
        cmap.put("StageView", snap.games.StageView.class);
        cmap.put("ImageView", ImageView.class);
        cmap.put("Label", Label.class);
        cmap.put("ListView", ListView.class);
        cmap.put("Menu", Menu.class);
        cmap.put("MenuBar", MenuBar.class);
        cmap.put("MenuButton", MenuButton.class);
        cmap.put("MenuItem", MenuItem.class);
        cmap.put("PathView", PathView.class);
        cmap.put("PenActor", snap.games.PenActor.class);
        cmap.put("ProgressBar", ProgressBar.class);
        cmap.put("RadioButton", RadioButton.class);
        cmap.put("RectView", RectView.class);
        cmap.put("RowView", RowView.class);
        cmap.put("ScaleBox", ScaleBox.class);
        cmap.put("ScrollView", ScrollView.class);
        cmap.put("Separator", Separator.class);
        cmap.put("Slider", Slider.class);
        cmap.put("Spinner", Spinner.class);
        cmap.put("SplitView", SplitView.class);
        cmap.put("SpringView", SpringView.class);
        cmap.put("StackView", StackView.class);
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
        cmap.put("View", View.class);

        // Return
        return cmap;
    }

    /**
     * Returns an image for given name/path.
     */
    public Image getImage(String aPath)
    {
        // If there is an Archiver.Owner, look for image as class resource
        Class<?> ownerClass = getOwnerClass();
        for (Class<?> cls = ownerClass; cls != null && cls != ViewController.class; cls = cls.getSuperclass()) {
            Image image = Image.getImageForClassResource(cls, aPath);
            if (image != null)
                return image;
        }

        // Otherwise, try to find image name as path relative to Archiver.SourceURL
        WebURL url = getSourceURL();
        return Image.getImageForUrlResource(url, aPath);
    }

    /**
     * Returns whether to use real classes.
     */
    public static boolean isUseRealClass()  { return ViewArchiver.isUseRealClassDefault(); }
}