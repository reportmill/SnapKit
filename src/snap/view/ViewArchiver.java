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
    static boolean          _useRealCls = true;
    
/**
 * Returns a parent shape for source.
 */
public ParentView getParentView(Object aSource)  { return (ParentView)getView(aSource, null); }

/**
 * Creates a view.
 */
public View getView(Object aSource, Archivable aRootObj)
{
    // If source is a document, just return it
    if(aSource instanceof View) return (View)aSource;
    
    // Get URL and/or bytes (complain if not found)
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    byte bytes[] = url!=null? (url.getFile()!=null? url.getFile().getBytes() : null) : SnapUtils.getBytes(aSource);
    if(bytes==null)
        throw new RuntimeException("RMArchiver.getShape: Cannot read source: " + (url!=null? url : aSource));
    
    // Create archiver, read, set source and return
    setRootObject(aRootObj);
    View shape = (View)readObject(url!=null? url : bytes);
    return shape;
}

/**
 * Returns the class for a given element.
 */
protected Class getClass(XMLElement anElement)
{
    Class cls = super.getClass(anElement);
    String cname = anElement.getAttributeValue("class");
    if(cname!=null && getUseRealClass()) {
        Class c = ClassUtils.getClass(cname);
        if(c!=null && View.class.isAssignableFrom(c)) {
            cls = c; }
    }
    return cls;
}
/**
 * Returns the class map.
 */
public Map <String, Class> getClassMap()  { return _rmCM!=null? _rmCM : (_rmCM=createClassMap()); }
static Map <String, Class> _rmCM;

/**
 * Creates the class map.
 */
protected Map <String, Class> createClassMap()
{
    // Create class map and add classes
    Map cmap = new HashMap();
    
    // Shape classes
    cmap.put("DocView", DocView.class);
    cmap.put("ImageView", ImageView.class);
    cmap.put("PageView", PageView.class);
    cmap.put("PathView", PathView.class);
    cmap.put("RectView", RectView.class);
    cmap.put("StringView", StringView.class);
    
    // View classes
    cmap.put("BorderView", BorderView.class);
    cmap.put("Box", Box.class);
    cmap.put("BrowserView", BrowserView.class);
    cmap.put("Button", Button.class);
    cmap.put("ColorButton", snap.viewx.ColorButton.class);
    cmap.put("ColorDock", snap.viewx.ColorDock.class);
    cmap.put("ColorWell", snap.viewx.ColorWell.class);
    cmap.put("CheckBox", CheckBox.class);
    cmap.put("CheckBoxMenuItem", CheckBoxMenuItem.class);
    cmap.put("ComboBox", ComboBox.class);
    cmap.put("HBox", HBox.class);
    cmap.put("Label", Label.class);
    cmap.put("ListView", ListView.class);
    cmap.put("Menu", Menu.class);
    cmap.put("MenuBar", MenuBar.class);
    cmap.put("MenuButton", MenuButton.class);
    cmap.put("MenuItem", MenuItem.class);
    cmap.put("ProgressBar", ProgressBar.class);
    cmap.put("RadioButton", RadioButton.class);
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
    cmap.put("TextView", TextView.class);
    cmap.put("TextField", TextField.class);
    cmap.put("ThumbWheel", ThumbWheel.class);
    cmap.put("ToggleButton", ToggleButton.class);
    cmap.put("TreeView", TreeView.class);
    cmap.put("TitleView", TitleView.class);
    cmap.put("VBox", VBox.class);
    cmap.put("View", ParentView.class);
    
    // Graphics
    cmap.put("color", Color.class);
    cmap.put("font", Font.class);
    cmap.put("stroke", Border.class);
    cmap.put("fill", Color.class); //RMFill.class
    cmap.put("gradient-fill", GradientPaint.class); //RMGradientFill.class
    cmap.put("image-fill", ImagePaint.class); //RMImageFill.class
    cmap.put("blur-effect", BlurEffect.class);
    cmap.put("shadow-effect", ShadowEffect.class);
    cmap.put("reflection-effect", ReflectEffect.class);
    cmap.put("emboss-effect", EmbossEffect.class);
    
    // Add aliases and return cmap
    addAliases(cmap);
    return cmap;
}

/**
 * Adds aliases.
 */
protected void addAliases(Map cmap)
{
    // Shape classes
    cmap.put("document", DocView.class); // RMDocument.class
    cmap.put("image-shape", ImageView.class); //RMImageShape.class
    cmap.put("page", PageView.class); // RMPage.class
    cmap.put("polygon", PathView.class); //RMPolygonShape.class
    cmap.put("rect", RectView.class); //RMRectShape.class
    cmap.put("spring-shape", SpringView.class); //RMSpringShape.class
    cmap.put("text", TextViewBase.class); //RMTextShape.class
    
    // Swing Component shapes
    cmap.put("panel", SpringView.class);
    cmap.put("jbutton", Button.class);
    cmap.put("jcheckbox", CheckBox.class);
    cmap.put("jcheckboxmenuitem", CheckBoxMenuItem.class);
    cmap.put("jcombobox", ComboBox.class);
    cmap.put("jlabel", Label.class);
    cmap.put("jlist", ListView.class);
    cmap.put("jmenu", Menu.class);
    cmap.put("jmenuitem", MenuItem.class);
    cmap.put("jprogressbar", ProgressBar.class);
    cmap.put("jradiobutton", RadioButton.class);
    cmap.put("jscrollpane", ScrollView.class);
    cmap.put("jseparator", Separator.class);
    cmap.put("jslider", Slider.class);
    cmap.put("jspinner", Spinner.class);
    cmap.put("jsplitpane", SplitView.class);
    cmap.put("jtable", TableView.class);
    cmap.put("JTableColumn", TableCol.class);
    cmap.put("jtabbedpane", TabView.class);
    cmap.put("jtextarea", TextView.class);
    cmap.put("jtextfield", TextField.class);
    cmap.put("jtogglebutton", ToggleButton.class);
    cmap.put("jtree", TreeView.class);
    cmap.put("TitlePane", TitleView.class);
    
    // Miscellaneous component shapes 
    cmap.put("customview", ParentView.class);
    cmap.put("menubutton", MenuButton.class);
    cmap.put("switchpane", SwitchView.class);
    cmap.put("thumbwheel", ThumbWheel.class);
}

/**
 * Returns whether to use real classes.
 */
public static boolean getUseRealClass()  { return _useRealCls; }

/**
 * Sets whether to use real classes.
 */
public static void setUseRealClass(boolean aFlag)  { _useRealCls = aFlag; }

}