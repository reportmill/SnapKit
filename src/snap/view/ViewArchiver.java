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
    Map classMap = new HashMap();
    
    // Shape classes
    //classMap.put("arrow-head", RMLineShape.ArrowHead.class);
    //classMap.put("cell-table", RMCrossTab.class);
    //classMap.put("cell-table-frame", RMCrossTabFrame.class);
    classMap.put("document", DocView.class); // RMDocument.class
    //classMap.put("flow-shape", RMFlowShape.class);
    //classMap.put("graph", RMGraph.class);
    //classMap.put("graph-legend", RMGraphLegend.class);
    classMap.put("image-shape", ImageView.class); //RMImageShape.class
    //classMap.put("label", RMLabel.class);
    //classMap.put("labels", RMLabels.class);
    //classMap.put("line", RMLineShape.class);
    //classMap.put("oval", RMOvalShape.class);
    classMap.put("page", PageView.class); // RMPage.class
    classMap.put("polygon", PathView.class); //RMPolygonShape.class
    classMap.put("rect", RectView.class); //RMRectShape.class
    //classMap.put("shape", RMParentShape.class);
    classMap.put("spring-shape", SpringView.class); //RMSpringShape.class
    //classMap.put("subreport", RMSubreport.class);
    //classMap.put("switchshape", RMSwitchShape.class);
    //classMap.put("table", RMTable.class);
    //classMap.put("table-group", RMTableGroup.class);
    //classMap.put("tablerow", RMTableRow.class);
    classMap.put("text", TextViewBase.class); //RMTextShape.class
    classMap.put("TextView", TextView.class); //RMTextShape.class
    //classMap.put("linked-text", RMLinkedText.class);
    //classMap.put("scene3d", RMScene3D.class);
    
    // Graphics
    classMap.put("color", Color.class);
    classMap.put("font", Font.class);
    //classMap.put("pgraph", RMParagraph.class);
    //classMap.put("xstring", RMXString.class);
    
    // Strokes
    //classMap.put("stroke", RMStroke.class); classMap.put("double-stroke", RMStroke.class);
    //classMap.put("border-stroke", RMBorderStroke.class);
    
    // Fills
    classMap.put("fill", Color.class); //RMFill.class
    classMap.put("gradient-fill", GradientPaint.class); //RMGradientFill.class
    //classMap.put("radial-fill", RMRadialGradientFill.class);
    classMap.put("image-fill", ImagePaint.class); //RMImageFill.class
    
    // Effects
    classMap.put("blur-effect", BlurEffect.class);
    classMap.put("shadow-effect", ShadowEffect.class);
    classMap.put("reflection-effect", ReflectEffect.class);
    classMap.put("emboss-effect", EmbossEffect.class);

    // Swing Component shapes
    classMap.put("BorderView", BorderView.class);
    classMap.put("Box", Box.class);
    classMap.put("BrowserView", BrowserView.class);
    classMap.put("ColorButton", snap.viewx.ColorButton.class);
    classMap.put("ColorDock", snap.viewx.ColorDock.class);
    classMap.put("ColorWell", snap.viewx.ColorWell.class);
    classMap.put("panel", SpringView.class);
    classMap.put("HBox", HBox.class);
    classMap.put("jbutton", Button.class);
    classMap.put("jcheckbox", CheckBox.class);
    classMap.put("jcheckboxmenuitem", CheckBoxMenuItem.class);
    classMap.put("jcombobox", ComboBox.class);
    classMap.put("jlabel", Label.class);
    classMap.put("jlist", ListView.class);
    classMap.put("jmenu", Menu.class);
    classMap.put("MenuBar", MenuBar.class);
    classMap.put("jmenuitem", MenuItem.class);
    classMap.put("jprogressbar", ProgressBar.class);
    classMap.put("jradiobutton", RadioButton.class);
    classMap.put("jscrollpane", ScrollView.class);
    classMap.put("jseparator", Separator.class);
    classMap.put("jslider", Slider.class);
    classMap.put("jspinner", Spinner.class);
    classMap.put("jsplitpane", SplitView.class);
    classMap.put("jtable", TableView.class);
    classMap.put("JTableColumn", TableCol.class);
    classMap.put("jtabbedpane", TabView.class);
    classMap.put("jtextarea", TextView.class);
    classMap.put("jtextfield", TextField.class);
    classMap.put("jtogglebutton", ToggleButton.class);
    classMap.put("jtree", TreeView.class);
    classMap.put("StackView", StackView.class);
    classMap.put("TitlePane", TitleView.class);
    classMap.put("VBox", VBox.class);
    
    // Miscellaneous component shapes 
    classMap.put("View", ParentView.class);
    classMap.put("customview", ParentView.class);
    classMap.put("menubutton", MenuButton.class);
    classMap.put("switchpane", SwitchView.class);
    classMap.put("thumbwheel", ThumbWheel.class);
    
    // Return classmap
    return classMap;
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