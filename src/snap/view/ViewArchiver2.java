package snap.view;
import snap.gfx.Image;
import snap.props.PropArchiverXML;
import snap.props.PropObject;
import snap.util.XMLElement;
import snap.viewx.ColorButton;
import snap.viewx.ColorDock;
import snap.viewx.ColorWell;
import snap.web.WebURL;
import java.util.List;

/**
 * This prop archiver subclass archives views and friends to .snp file.
 */
public class ViewArchiver2 extends PropArchiverXML {

    /**
     * Constructor.
     */
    public ViewArchiver2()
    {
        VIEW_CLASSES.forEach(this::addClassMapClass);
    }

    /**
     * Override to move children inline.
     */
    @Override
    public XMLElement writePropObjectToXml(PropObject aPropObject)
    {
        XMLElement xml = super.writePropObjectToXml(aPropObject);
        inlineViewChildren(xml);
        return xml;
    }

    @Override
    public PropObject readPropObjectFromXml(XMLElement anElement)
    {
        groupViewChildren(anElement);
        return super.readPropObjectFromXml(anElement);
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
     * An array of all View Classes
     */
    private static List<Class<? extends PropObject>> VIEW_CLASSES = List.of(
            View.class,
            BoxView.class,
            Button.class,
            BrowserView.class,
            CheckBox.class,
            CheckBoxMenuItem.class,
            ColorButton.class,
            ColorDock.class,
            ColorWell.class,
            ColView.class,
            ComboBox.class,
            ImageView.class,
            Label.class,
            ListView.class,
            Menu.class, MenuBar.class, MenuButton.class, MenuItem.class,
            ProgressBar.class,
            RadioButton.class,
            RectView.class,
            RowView.class,
            ScaleBox.class,
            Scroller.class,
            ScrollView.class,
            Separator.class,
            Slider.class,
            Spinner.class,
            SplitView.class,
            SpringView.class,
            StackView.class,
            SwitchView.class,
            TableCol.class,
            TableView.class,
            TabView.class,
            ThumbWheel.class,
            TitleView.class,
            ToggleButton.class,
            TreeView.class,
            TextField.class, TextArea.class, TextView.class,
            PathView.class
    );

    /**
     * Recurses into XML element and moves children inline.
     */
    private static void inlineViewChildren(XMLElement xml)
    {
        String childrenPropName = getChildrenPropNameForXml(xml);
        XMLElement childrenXML = xml.getElement(childrenPropName);
        if (childrenXML != null) {
            xml.removeElement(childrenXML);
            childrenXML.getElements().forEach(xml::add);
            childrenXML.getElements().forEach(ViewArchiver2::inlineViewChildren);
        }
        if (xml.hasAttribute(View.RuntimeClassName_Prop))
            xml.getAttribute(View.RuntimeClassName_Prop).setFullName("Class");
    }

    /**
     * Recurses into XML element and moves child elements into Children element.
     */
    private static void groupViewChildren(XMLElement xml)
    {
        if (xml.getElementCount() > 0) {
            List<XMLElement> childXMLs = List.copyOf(xml.getElements());
            childXMLs.forEach(xml::removeElement);
            String childrenPropName = getChildrenPropNameForXml(xml);
            XMLElement childrenXML = new XMLElement(childrenPropName);
            childXMLs.forEach(childrenXML::add);
            xml.add(childrenXML);
            childXMLs.forEach(ViewArchiver2::groupViewChildren);
        }
        if (xml.hasAttribute("Class"))
            xml.getAttribute("Class").setFullName(View.RuntimeClassName_Prop);
    }

    /**
     * Returns the children property name for the given XML element.
     */
    private static String getChildrenPropNameForXml(XMLElement xml)
    {
        return switch (xml.getName()) {
            case "MenuButton", "Menu" -> Menu.MenuItems_Prop;
            case "MenuBar" -> MenuBar.Menus_Prop;
            case "TableView" -> TableView.TableCols_Prop;
            default -> ParentView.Children_Prop;
        };
    }
}
