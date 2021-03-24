package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;

/**
 * A view to allow inspection of View hierarchy.
 */
public class DevPane extends ViewOwner {

    // The RootView
    private RootView  _rootView;

    // The RootView original content
    private View  _content;

    // The SplitView
    protected SplitView  _splitView;

    // The TabView
    private TabView  _tabView;

    // The ViewOwners inspector
    private DevPaneViewOwners  _viewOwners = new DevPaneViewOwners(this);

    // The ViewTree inspector
    private DevPaneViews _viewTree = new DevPaneViews(this);

    // The Graphics inspector
    private DevPaneGraphics  _gfxInsp = new DevPaneGraphics(this);

    // Constants
    private static int DEFAULT_HEIGHT = 300;
    protected static Stroke HIGHLIGHT_BORDER_STROKE = new Stroke(3);
    protected static Color HIGHLIGHT_BORDER_COLOR = Color.RED.blend(Color.CLEARWHITE, .34);

    /**
     * Constructor.
     */
    public DevPane(View aView)
    {
        super();

        _rootView = aView.getRootView();
        _content = _rootView.getContent();
    }

    /**
     * Returns the real content.
     */
    public View getContent()  { return _content; }

    /**
     * Install in rootView
     */
    public void installInWindow()
    {
        // Get UI
        View ui = getUI();

        // Set in RootView
        _rootView.setContent(ui);

        // Set Content back
        _splitView.addItem(_content);
    }

    /**
     * Remove from window.
     */
    public void removeFromWindow()
    {
        // Make sure content is Pickable (DevPaneViewTree.Targeting turns this off)
        getContent().setPickable(true);

        // Remove item animated
        _splitView.removeItemWithAnim(_tabView);
        _splitView.getDivider(0).getAnim(0).setOnFinish(() -> removeFromWindowAnimDone());
    }

    /**
     * When anim is done, remove.
     */
    private void removeFromWindowAnimDone()
    {
        _rootView.setContent(_content);
    }

    /**
     * CreateUI.
     */
    @Override
    protected View createUI()
    {
        // Create TabView
        _tabView = new TabView();

        // Create SplitView
        _splitView = new DPSplitView();
        _splitView.setVertical(true);
        _splitView.setBorder(null);
        return _splitView;
    }

    /**
     * Init UI.
     */
    @Override
    protected void initUI()
    {
        _tabView.addTab("View Owners", _viewOwners.getUI());
        _tabView.addTab("Views", _viewTree.getUI());
        _tabView.addTab("Graphics", _gfxInsp.getUI());
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        _splitView.repaint();
    }

    /**
     * When showing, kick off showTabView().
     */
    @Override
    protected void initShowing()
    {
        runLater(() -> showTabView());
    }

    /**
     * Shows the TabView.
     */
    private void showTabView()
    {
        _splitView.addItemWithAnim(_tabView, DEFAULT_HEIGHT);
    }

    /**
     * Returns the DevPane for a given view (really, its RootView).
     */
    public static DevPane getDevPane(View aView)
    {
        RootView rootView = aView.getRootView();
        ViewOwner owner = rootView.getContent().getOwner();
        return owner instanceof DevPane ? (DevPane) owner : null;
    }

    /**
     * Returns whether DevPane is showing for view.
     */
    public static boolean isDevPaneShowing(View aView)  { return getDevPane(aView)!=null; }

    /**
     * Sets a DevPane visible for view.
     */
    public static void setDevPaneShowing(View aView, boolean aValue)
    {
        // DevPane for given view
        DevPane devPane = getDevPane(aView);

        // If requested and not yet installed, create and install
        if (aValue && devPane == null) {
            DevPane devPane2 = new DevPane(aView);
            devPane2.runLater(() -> devPane2.installInWindow());
        }

        // If dismissed (and is indeed installed), remove
        else if (!aValue && devPane != null) {
            devPane.removeFromWindow();
        }
    }

    /**
     * Special SplitView to paint selected view border.
     */
    private class DPSplitView extends SplitView {

        @Override
        protected void paintAbove(Painter aPntr)
        {
            if (_viewOwners.isShowing())
                _viewOwners.paintViewSelection(aPntr, _splitView);
            else if (_viewTree.isShowing())
                _viewTree.paintViewSelection(aPntr, _splitView);
        }
    }
}
