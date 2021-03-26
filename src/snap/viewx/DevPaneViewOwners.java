package snap.viewx;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.GFXEnv;
import snap.gfx.Painter;
import snap.util.FileUtils;
import snap.util.SnapUtils;
import snap.view.*;
import snap.view.EventListener;
import snap.web.WebURL;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A DevPane tab for inspecting the view tree.
 */
public class DevPaneViewOwners extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    // Whether in target mode (mouse over updates target)
    private boolean  _targeting;

    // The view under the mouse when targeting
    private View  _targView;

    // Whether View XML needs to be updated
    private boolean _updateXML;

    // The BrowserView
    private BrowserView<View>  _browserView;

    // The TextView
    private TextView  _textView;

    // The targeting handler
    private EventListener  _targLsnr = e -> devPaneMouseEvent(e);

    // Constants
    private static final String SNAPBUILDER_URL = "https://reportmill.com/snaptea/SnapBuilder/classes.js";
    private static final String SNAPBUILDER_URL_LOCAL = "http://localhost:8080/classes.js";

    /**
     * Constructor.
     */
    public DevPaneViewOwners(DevPane aDevPane)
    {
        super();
        _devPane = aDevPane;
    }

    /**
     * Returns whether pane targeting mode is on (mouse over highlights possible selection).
     */
    public boolean isTargeting()  { return _targeting; }

    /**
     * Sets whether pane targeting mode is on (mouse over highlights possible selection).
     */
    public void setTargeting(boolean aValue)
    {
        if (aValue==isTargeting()) return;
        _targeting = aValue;

        // If turn on
        if (aValue) {
            _devPane.getContent().setPickable(false);
            _devPane._splitView.addEventFilter(_targLsnr, MouseMove, MouseExit, MousePress);
        }

        // If turning off
        else {
            _devPane.getContent().setPickable(true);
            _devPane._splitView.removeEventFilter(_targLsnr);
        }
        _updateXML = true;
    }

    /**
     * Returns the selected view.
     */
    public View getSelView()  { return _browserView.getSelItem(); }

    /**
     * Returns the selected view.
     */
    public void setSelView(View aView)
    {
        if (aView==getSelView()) return;
        _browserView.setSelItem(aView);
    }

    /**
     * Returns the targeted view.
     */
    public View getTargView()  { return _targView; }

    /**
     * Sets the targeted view.
     */
    public void setTargView(View aView)
    {
        if (aView==_targView) return;
        _targView = aView;
        _devPane._splitView.repaint();
    }

    /**
     * Returns the selected view owner.
     */
    public ViewOwner getSelViewOwner()
    {
        View selView = getSelView();
        return selView != null ? selView.getOwner() : null;
    }

    /**
     * Returns the XML for selection.
     */
    public String getSelXML()
    {
        // If targeting, return empty?
        if (isTargeting())
            return "";

        // Get selected ViewOwner
        ViewOwner owner = getSelViewOwner(); if (owner == null) return "";

        // If owner has UI file, get text
        WebURL url = ViewEnv.getEnv().getUISource(owner.getClass());
        if (url != null) {
            String str = url.getText();
            return str;
        }

        // Otherwise, just get XML from SelView
        View selView = getSelView(); if (selView == null) return "";
        String xml = new ViewArchiver().writeToXML(selView).getString();
        return xml;
    }

    /**
     * Called when the DevPane.SplitView gets a mouse event.
     */
    private void devPaneMouseEvent(ViewEvent anEvent)
    {
        // Handle MouseMove
        if (anEvent.isMouseMove()) {
            View view = getViewAtPoint(anEvent.getX(), anEvent.getY());
            setSelView(view);
            setTargView(view);
        }

        // Handle MouseExit
        if (anEvent.isMouseExit()) {
            setSelView(null);
            setTargView(null);
        }

        // handle MousePress
        if (anEvent.isMousePress()) {
            setTargeting(false);
            View view = getViewAtPoint(anEvent.getX(), anEvent.getY());
            setTargView(null);
            setSelView(view);
            resetLater();
        }
    }

    /**
     * Returns the view at given point.
     */
    private View getViewAtPoint(double aX, double aY)
    {
        View view = ViewUtils.getDeepestViewAt(_devPane.getContent(), aX, aY);
        View par = view!=null ? view.getParent() : null;
        while (par != null && (par.getOwner() == view.getOwner() || view.getOwner() == null)) {
            view = par;
            par = view.getParent();
        }
        return view;
    }

    /**
     * Called to paint SelView.
     */
    public void paintViewSelection(Painter aPntr, View aHostView)
    {
        // Get SelView (use TargView if targeting)
        View selView = getTargView();
        if (selView == null)
            selView = getSelView();
        if (selView == null || selView.getRootView() == null) return;

        // Get SelView visiible bounds
        Rect rect = selView.getVisRect(); if (rect.isEmpty()) return;
        rect = rect.getInsetRect(-1);

        // Get SelView bounds as round rect in HostView coords
        Shape rect2 = new RoundRect(rect.x, rect.y, rect.width, rect.height, 4);
        Shape rect3 = selView.localToParent(rect2, aHostView);

        // Draw rect
        aPntr.setStroke(DevPane.HIGHLIGHT_BORDER_STROKE);
        aPntr.setColor(DevPane.HIGHLIGHT_BORDER_COLOR);
        aPntr.draw(rect3);
    }

    /**
     * InitUI.
     */
    @Override
    protected void initUI()
    {
        // Configure BrowserView
        _browserView = getView("BrowserView", BrowserView.class);
        _browserView.setResolver(new ViewOwnersTreeResolver());
        _browserView.setItems(_devPane.getContent());
        _browserView.setPrefColWidth(120);

        // Get TextView
        _textView = getView("XMLText", TextView.class);

        // Handle Escape
        addKeyActionHandler("EscapeAction", "ESCAPE");
    }

    @Override
    protected void resetUI()
    {
        // Update TargetingButton
        setViewValue("TargetingButton", isTargeting());

        // Update XMLTextLabel
        ViewOwner selOwner = getSelViewOwner();
        String selOwnerName = selOwner != null ? selOwner.getClass().getSimpleName() + ".snp" : "ViewOwner XML";
        setViewValue("XMLTextLabel", selOwnerName);

        // Update XML
        if (_updateXML) {
            String xml = getSelXML();
            int header = xml.indexOf('\n');
            if (header>0) xml = xml.substring(header+1);
            _textView.setText(xml);
            double size = xml.length() > 800 ? 11 : xml.length() > 400 ? 12 : xml.length() > 200 ? 13 : 14;
            if (_textView.getFont().getSize()!=size)
                _textView.setFont(_textView.getFont().deriveFont(size));
            _updateXML = false;
        }
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle BrowserView
        if (anEvent.equals(_browserView)) {
            _devPane.getUI().repaint();
            setTargeting(false);
            _updateXML = true;
        }

        // Handle TargetingButton
        if (anEvent.equals("TargetingButton"))
            setTargeting(true);

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction") && isTargeting()) {
            setTargeting(false);
            anEvent.consume();
        }

        // Handle CloseButton
        if (anEvent.equals("CloseButton"))
            runLater(() -> DevPane.setDevPaneShowing(_devPane.getContent(), false));

        // Handle ShowUIButton
        if (anEvent.equals("ShowUIButton"))
            showInSnapBuilder(anEvent.isAltDown());

        // Handle ShowSourceButton
        if (anEvent.equals("ShowSourceButton"))
            showOwnerSource();
    }

    /**
     * Shows the UI current selection.
     */
    private void showInSnapBuilder(boolean isLocal)
    {
        // Get filename
        ViewOwner selOwner = getSelViewOwner(); if (selOwner == null) return;
        String filename = selOwner.getClass().getSimpleName() + ".html";

        // Get HTML String and bytes
        String htmlStr = getHTMLString(isLocal);
        if (htmlStr==null) { ViewUtils.beep(); return; }
        byte[] htmlBytes = htmlStr.getBytes();

        // Open filename + bytes
        openFilenameBytes(filename, htmlBytes);
    }

    /**
     * Opens the given filename + bytes as a file.
     */
    private void openFilenameBytes(String aFilename, byte[] theBytes)
    {
        // Get file
        File file = SnapUtils.isTeaVM ? new File('/' + aFilename) :
            FileUtils.getTempFile(aFilename);

        // TeaVM seems to sometimes use remnants of old file. This has been fixed
        if (SnapUtils.isTeaVM)
            try { file.delete(); }
            catch (Exception e) { System.err.println("DevPaneViewOwners.showInSnapBuilder: Error deleting file"); }

        // Write HTML string to temp HTML file
        try { FileUtils.writeBytes(file, theBytes); }
        catch (IOException e)
        {
            System.err.println("openFilenameBytes write error: " + e);
            return;
        }

        // Open temp HTML file
        if (SnapUtils.isTeaVM)
            GFXEnv.getEnv().openFile(file);
        else GFXEnv.getEnv().openURL(file);
    }

    /**
     * Returns the HTML to open UI in SnapBuilder web.
     */
    private String getHTMLString(boolean isLocal)
    {
        // Get URL string for SnapBuilder script
        String urls = SNAPBUILDER_URL;
        if (isLocal)
            urls = SNAPBUILDER_URL_LOCAL;

        // Get SelOwner and XML string
        ViewOwner selOwner = getSelViewOwner();
        Class selClass = selOwner.getClass();
        String xmlFilename = selClass.getSimpleName() + ".snp";
        String xmlPath = selClass.getName().replace('.', '/') + ".snp";
        String xmlStr = getSelXML();

        // Create StringBuffer and add header
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");

        // Add header/title
        String title = "SnapBuilder: " + xmlPath;
        sb.append("<head>\n");
        sb.append("<title>").append(title).append("</title>\n");
        sb.append("</head>\n");

        // Add body
        sb.append("<body>\n");
        sb.append("</body>\n");

        // Add SnapBuilder Script
        sb.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + urls + "\"></script>\n");

        // Add script with inline XMLString and main() entry point
        sb.append("<script>\n\n");
        sb.append("const xmlFilename = '").append(xmlFilename).append("';\n");
        sb.append("const xmlString = `\n");
        sb.append(xmlStr);
        sb.append("\n`;\n\n");
        sb.append("main(['XMLFilename', xmlFilename, 'XMLString', xmlString]);\n\n");
        sb.append("</script>\n\n");

        // Close HTML and return string
        sb.append("</html>\n");
        return sb.toString();
    }

    /**
     * Shows the source for the owner of current selection.
     */
    private void showOwnerSource()
    {
        String urlStr = getOwnerSourceURL();
        if (urlStr==null) { ViewUtils.beep(); return; }
        GFXEnv.getEnv().openURL(urlStr);
    }

    /**
     * Returns the Owner source url for currently selected type.
     */
    public String getOwnerSourceURL()
    {
        // Get class name for selected view
        View selView = getSelView();
        ViewOwner owner = selView!=null ? selView.getOwner() : null;
        Class cls = owner!=null ? owner.getClass() : ViewOwner.class; if (cls==null) return null;

        // Iterate up through class parents until URL found or null
        while (cls!=null) {
            String url = getSourceURL(cls); if (url!=null) return url;
            Class scls = cls.getSuperclass(); cls = scls!=null && scls!=Object.class ? scls : null;
        }
        return null;
    }

    /**
     * Returns the JavaDoc url for currently selected type.
     */
    public String getSourceURL(Class aClass)
    {
        // Get class name for selected JNode (if inner class, strip that out)
        String cname = aClass.getName();
        if (cname.contains("$")) {
            int ind = cname.indexOf("$");
            cname = cname.substring(0, ind);
        }

        // Handle snap/snapcharts classes
        String url = null;
        if (cname.startsWith("snap."))
            url = "https://github.com/reportmill/SnapKit/blob/master/src/" + cname.replace('.', '/') + ".java";
        else if (cname.startsWith("snapcharts."))
            url = "https://github.com/reportmill/SnapCharts/blob/master/src/" + cname.replace('.', '/') + ".java";

        // Return url
        return url;
    }

    /**
     * Returns all ViewOwners for given View and its children.
     */
    private static List<ViewOwner> getChildViewOwnersForView(View aView)
    {
        List<ViewOwner> list = new ArrayList<>();
        getChildViewOwnersForView(aView, list);
        ViewOwner owner = aView.getOwner();
        if (owner != null)
            list.remove(owner);
        return list;
    }

    /**
     * Returns all ViewOwners for given View and its children.
     */
    private static void getChildViewOwnersForView(View aView, List<ViewOwner> aList)
    {
        ViewOwner owner = aView.getOwner();
        if (owner != null && !aList.contains(owner))
            aList.add(owner);
        if (aView instanceof ParentView) {
            ParentView par = (ParentView) aView;
            for (View child : par.getChildren())
                getChildViewOwnersForView(child, aList);
        }
    }

    /**
     * A resolver for Views.
     */
    public class ViewOwnersTreeResolver extends TreeResolver<View> {

        /** Returns the parent of given item. */
        public View getParent(View anItem)
        {
            return anItem!=_devPane.getContent() ? anItem.getParent() : null;
        }

        /** Whether given object is a parent (has children). */
        public boolean isParent(View anItem)
        {
            List<ViewOwner> list = getChildViewOwnersForView(anItem);
            return list.size() > 0;
        }

        /** Returns the children. */
        public View[] getChildren(View aParent)
        {
            List<ViewOwner> list = getChildViewOwnersForView(aParent);
            ViewOwner[] owners = list.toArray(new ViewOwner[0]);
            View[] views = new View[owners.length];
            for (int i=0; i<owners.length; i++)
                views[i] = owners[i].getUI();
            return views;
        }

        /** Returns the text to be used for given item. */
        public String getText(View anItem)
        {
            ViewOwner owner = anItem.getOwner();
            String str = owner != null ? owner.getClass().getSimpleName() : "No Owner";
            return str;
        }

        /** Return the image to be used for given item. */
        public View getGraphic(View anItem)  { return null; }
    }
}
