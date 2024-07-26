package snap.viewx;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.GFXEnv;
import snap.gfx.Painter;
import snap.util.ArrayUtils;
import snap.util.Convert;
import snap.util.StringUtils;
import snap.view.*;

import java.util.Collections;

/**
 * A DevPane tab for inspecting the view tree.
 */
public class DevPaneViews extends ViewOwner {

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

    // The Property TableView
    private TableView<PropValuePair>  _propTable;

    // The TextView
    private TextView  _textView;

    // The array of PropValuePairs for PropTable
    private PropValuePair[]  _propPairs;

    // The targeting handler
    private EventListener  _targLsnr = e -> devPaneMouseEvent(e);

    // Constant for visible property names
    private static final String MaxX_Prop = "MaxX";
    private static final String MaxY_Prop = "MaxY";
    private static String[]  PROP_NAMES = {
            View.X_Prop, View.Y_Prop,
            View.Width_Prop, View.Height_Prop,
            MaxX_Prop, MaxY_Prop,
            View.PrefWidth_Prop, View.PrefHeight_Prop,
            View.Align_Prop, View.Margin_Prop, View.Padding_Prop, View.Spacing_Prop,
            View.GrowWidth_Prop, View.GrowHeight_Prop,
            View.LeanX_Prop, View.LeanY_Prop,
            View.Font_Prop, View.Fill_Prop, View.Border_Prop
    };

    /**
     * Constructor.
     */
    public DevPaneViews(DevPane aDevPane)
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
        _propPairs = null;
        _propTable.setItems(getPropValuePairs());
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
     * Returns the list of properties.
     */
    private PropValuePair[] getPropValuePairs()
    {
        // If already set, just return
        if (_propPairs != null) return _propPairs;

        // If no SelView, return empty list
        if (getSelView() == null)
            return _propPairs = new PropValuePair[0];

        // Create new array of PropValuePairs to clear values
        PropValuePair[] propPairs = new PropValuePair[PROP_NAMES.length];
        for (int i=0; i<PROP_NAMES.length; i++)
            propPairs[i] = new PropValuePair(PROP_NAMES[i]);
        return _propPairs = propPairs;
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
        if (par instanceof Label || par instanceof ButtonBase || par instanceof TextField || par instanceof ComboBox) {
            view = par; par = par.getParent();
            if (par instanceof ButtonBase || par instanceof TextField || par instanceof ComboBox)
                view = par;
        }
        return view;
    }

    /**
     * Called to paint SelView.
     */
    public void paintViewSelection(Painter aPntr, View aHostView)
    {
        View selView = getTargView();
        if (selView == null)
            selView = getSelView();
        if (selView==null) return;
        if (selView.getRootView()==null) return;

        Rect rect = selView.getBoundsLocal().getInsetRect(-1);
        Shape rect2 = new RoundRect(rect.x, rect.y, rect.width, rect.height, 4);
        Shape rect3 = selView.localToParent(rect2, aHostView);
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
        _browserView.setResolver(new ViewTreeResolver());
        _browserView.setItemsList(Collections.singletonList(_devPane.getContent()));

        // Configure PropTableView
        _propTable = getView("PropTableView", TableView.class);
        _propTable.setCellConfigure(this :: configurePropTableCell);
        _propTable.setCellPadding(new Insets(4));

        // Get TextView
        _textView = getView("TextView", TextView.class);

        // Handle Escape
        addKeyActionHandler("EscapeAction", "ESCAPE");
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update TargetingButton
        setViewValue("TargetingButton", isTargeting());

        // Update PropTable pairs
        _propTable.setItems(getPropValuePairs());

        // Update PropNameText, PropValueText
        PropValuePair selPair = _propTable.getSelItem();
        setViewValue("PropNameText", selPair != null ? selPair.getPropName() : "");
        if (!getView("PropValueText").isFocused()) // Lame - but Fill/Border break when changed
            setViewValue("PropValueText", selPair != null ? selPair.getValueString() : "");

        // Handle RelayoutViewButton, RepaintViewButton
        setViewEnabled("RelayoutViewButton", getSelView() != null);
        setViewEnabled("RepaintViewButton", getSelView() != null);

        // Update XML
        if (_updateXML)
            resetXmlTextView();
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle BrowserView
            case "BrowserView":
                _devPane.getUI().repaint();
                setTargeting(false);
                _updateXML = true;
                _propPairs = null;
                break;

            // Handle TargetingButton
            case "TargetingButton": setTargeting(true); break;

            // Handle PropNameText, PropValueText
            case "PropNameText": handlePropNameTextAction(anEvent); break;
            case "PropValueText": handlePropValueTextAction(anEvent); break;

            // Handle RelayoutViewButton, RepaintViewButton
            case "RelayoutViewButton": getSelView().relayout(); break;
            case "RepaintViewButton": getSelView().repaint(); break;

            // Handle ShowSourceButton, ShowJavaDocButton
            case "ShowSourceButton": showSource(); break;
            case "ShowJavaDocButton": showJavaDoc(); break;

            // Handle SetPropStringText
            case "SetPropStringText": handleSetPropStringTextAction(anEvent); break;

            // Handle EscapeAction
            case "EscapeAction":
                if (isTargeting()) {
                    setTargeting(false);
                    anEvent.consume();
                }
                break;
        }
    }

    /**
     * Resets the XML TextView.
     */
    private void resetXmlTextView()
    {
        // Get XML for selected view
        View selView = getSelView();
        String viewXML = selView != null && !isTargeting() ? new ViewArchiver().writeToXML(selView).getString() : "";

        // Strip header line
        int xmlHeader = viewXML.indexOf('\n');
        if (xmlHeader > 0)
            viewXML = viewXML.substring(xmlHeader+1);

        // Set xml in TextView
        _textView.setText(viewXML);

        // Reset font to fit
        double fontSize = viewXML.length() > 800 ? 11 : viewXML.length() > 400 ? 12 : viewXML.length() > 200 ? 13 : 14;
        if (_textView.getFont().getSize() != fontSize)
            _textView.setFont(_textView.getFont().copyForSize(fontSize));
        _updateXML = false;
    }

    /**
     * Called when PropNameText gets Action event.
     */
    private void handlePropNameTextAction(ViewEvent anEvent)
    {
        String prefix = anEvent.getStringValue();
        PropValuePair selPair = ArrayUtils.findMatch(_propPairs, propPair -> StringUtils.startsWithIC(propPair.getPropName(), prefix));
        if (selPair != null)
            _propTable.setSelItem(selPair);
    }

    /**
     * Called when PropValueText gets Action event.
     */
    private void handlePropValueTextAction(ViewEvent anEvent)
    {
        View selView = getSelView(); if (selView==null) return;
        PropValuePair selPair = _propTable.getSelItem(); if (selPair == null) return;
        String propName = selPair.getPropName();
        String propValue = anEvent.getStringValue();
        selView.setPropValue(propName, propValue);
        _devPane._splitView.repaint();
    }

    /**
     * Called when SetPropStringText gets Action event.
     */
    private void handleSetPropStringTextAction(ViewEvent anEvent)
    {
        View selView = getSelView(); if (selView==null) return;
        String propsStr = anEvent.getStringValue();
        selView.setPropsString(propsStr);
        _devPane._splitView.repaint();
    }

    /**
     * Shows the source for the current selection.
     */
    private void showSource()
    {
        String urlStr = getSourceUrlForSelView();
        if (urlStr==null) { ViewUtils.beep(); return; }
        GFXEnv.getEnv().openURL(urlStr);
    }

    /**
     * Shows the JavaDoc for the current selection.
     */
    private void showJavaDoc()
    {
        String urlStr = getSourceUrlForSelView();
        if (urlStr == null) { ViewUtils.beep(); return; }
        GFXEnv.getEnv().openURL(urlStr);
    }

    /**
     * Returns the Source url for currently selected type.
     */
    private String getSourceUrlForSelView()
    {
        // Get class name for selected view
        View selView = getSelView();
        Class<?> selViewClass = selView != null ? selView.getClass() : null; if (selViewClass == null) return null;
        if (selViewClass.isArray())
            selViewClass = selViewClass.getComponentType();

        // Iterate up through class parents until URL found or null
        while (selViewClass != null) {
            String url = getSourceUrlForViewClass(selViewClass); if (url != null) return url;
            Class<?> viewSuperclass = selViewClass.getSuperclass();
            selViewClass = viewSuperclass != null && viewSuperclass != Object.class ? viewSuperclass : null;
        }
        return null;
    }

    /**
     * Returns the JavaDoc url for given view class.
     */
    private String getSourceUrlForViewClass(Class<?> viewClass)
    {
        // Get class name for selected JNode (if inner class, strip that out)
        String className = viewClass.getName();
        if (className.contains("$")) {
            int ind = className.indexOf("$");
            className = className.substring(0, ind);
        }

        // Handle snap/snapcharts classes
        String url = null;
        if (className.startsWith("snap."))
            url = "https://github.com/reportmill/SnapKit/blob/master/src/" + className.replace('.', '/') + ".java";
        else if (className.startsWith("snapcharts."))
            url = "https://github.com/reportmill/SnapCharts/blob/master/src/" + className.replace('.', '/') + ".java";

        // Return url
        return url;
    }

    /**
     * Configures a PropTable cell.
     */
    private void configurePropTableCell(ListCell<PropValuePair> aCell)
    {
        // Get PropValuePair for Cell (just return if empty cell)
        PropValuePair propPair = aCell.getItem();
        if (propPair == null)
            return;

        // Configure Col 0, Col 1
        int col = aCell.getCol();
        if (col == 0)
            aCell.setText(propPair.getPropName());
        else aCell.setText(propPair.getValueString());
    }

    /**
     * A class to represent a Prop/Prop-Value pair.
     */
    private class PropValuePair {

        // The property name
        private String  _propName;

        // The property value
        private Object  _propValue;

        // The property value string
        private String  _propValueStr;

        /**
         * Constructor.
         */
        public PropValuePair(String aPropName)
        {
            _propName = aPropName;
        }

        /**
         * Returns the property name.
         */
        public String getPropName()  { return _propName; }

        /**
         * Returns the value.
         */
        public Object getValue()
        {
            if (_propValue != null) return _propValue;
            return _propValue = getValueImpl();
        }

        /**
         * Returns the value.
         */
        private Object getValueImpl()
        {
            View selView = getSelView();
            switch (_propName) {
                case MaxX_Prop: return selView.getX() + selView.getWidth();
                case MaxY_Prop: return selView.getY() + selView.getHeight();
                case View.PrefWidth_Prop: return selView.getPrefWidth();
                case View.PrefHeight_Prop: return selView.getPrefHeight();
                default: return getSelView().getPropValue(_propName);
            }
        }

        /**
         * Returns the value string.
         */
        public String getValueString()
        {
            if (_propValueStr != null) return _propValueStr;
            return _propValueStr = getValueStringImpl();
        }

        /**
         * Returns the value string.
         */
        private String getValueStringImpl()
        {
            Object propValue = getValue();
            if (propValue instanceof Insets)
                return ((Insets) propValue).getString();
            return Convert.stringValue(propValue);
        }
    }

    /**
     * A resolver for Views.
     */
    public class ViewTreeResolver extends TreeResolver<View> {

        /** Returns the parent of given item. */
        public View getParent(View anItem)
        {
            return anItem!=_devPane.getContent() ? anItem.getParent() : null;
        }

        /** Whether given object is a parent (has children). */
        public boolean isParent(View anItem)
        {
            if (!(anItem instanceof ParentView)) return false;
            if (anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner ||
                    anItem instanceof ArrowView || anItem instanceof TextField) return false;
            if (anItem instanceof ComboBox || anItem instanceof ListView) return false;
            return ((ParentView)anItem).getChildCount()>0;
        }

        /** Returns the children. */
        public View[] getChildren(View aParent)
        {
            ParentView par = (ParentView) aParent;
            //if (par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
            //    return sp.getContent()!=null ? new View[] { sp.getContent() } : new View[0]; }
            return par.getChildren();
        }

        /** Returns the text to be used for given item. */
        public String getText(View anItem)
        {
            String str = anItem.getClass().getSimpleName();
            String name = anItem.getName();
            if (name!=null) str = name; //name.contains(str) ? name : (str + " - " + name);
            //String text = anItem.getText(); if (text!=null) str += " \"" + text + "\" ";
            return str;
        }

        /** Return the image to be used for given item. */
        public View getGraphic(View anItem)  { return null; }
    }
}
