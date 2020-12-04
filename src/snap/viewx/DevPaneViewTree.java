package snap.viewx;

import snap.view.*;

/**
 * A DevPane tab for inspecting the view tree.
 */
public class DevPaneViewTree extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    // The BrowserView
    private BrowserView<View>  _browserView;

    /**
     * Constructor.
     */
    public DevPaneViewTree(DevPane aDevPane)
    {
        super();
        _devPane = aDevPane;
    }

    /**
     * Returns the selected view.
     */
    public View getSelView()  { return _browserView.getSelItem(); }

    /**
     * InitUI.
     */
    @Override
    protected void initUI()
    {
        // Configure BrowserView
        _browserView = getView("BrowserView", BrowserView.class);
        _browserView.setResolver(new ViewTreeResolver());
        _browserView.setItems(_devPane.getContent());
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        if (anEvent.equals(_browserView))
            _devPane.getUI().repaint();
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
            ParentView par = (ParentView)aParent;
            if (par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
                return sp.getContent()!=null ? new View[] { sp.getContent() } : new View[0]; }
            return par.getChildren();
        }

        /** Returns the text to be used for given item. */
        public String getText(View anItem)
        {
            String str = anItem.getClass().getSimpleName();
            //String name = anItem.getName(); if (name!=null) str += " - " + name;
            //String text = anItem.getText(); if (text!=null) str += " \"" + text + "\" ";
            return str;
        }

        /** Return the image to be used for given item. */
        public View getGraphic(View anItem)  { return null; }
    }
}
