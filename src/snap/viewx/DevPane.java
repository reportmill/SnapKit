package snap.viewx;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.ArrayUtils;
import snap.util.ListUtils;
import snap.view.*;

import java.util.List;

/**
 * A view to allow inspection of View hierarchy.
 */
public class DevPane extends ViewController {

    // The RootView
    private RootView  _rootView;

    // The RootView original content
    private View  _content;

    // The SplitView
    protected SplitView  _splitView;

    // The TabView
    private TabView  _tabView;

    // The Files inspector
    private DevPaneFiles _filesInsp;

    // The ViewControllers inspector
    private DevPaneViewOwners _viewControllersInsp;

    // The ViewTree inspector
    private DevPaneViews _viewsInsp;

    // The Graphics inspector
    private DevPaneGraphics _graphicsInsp;

    // The Console inspector
    private DevPaneConsole _consoleInsp;

    // The Exception inspector
    private DevPaneExceptions  _exceptionInsp;

    // The array of all panes
    private ViewController[] _allPanes;

    // Constants
    private static int DEFAULT_HEIGHT = 300;
    protected static Stroke HIGHLIGHT_BORDER_STROKE = Stroke.getStrokeRound(3);
    protected static Color HIGHLIGHT_BORDER_COLOR = Color.RED.blend(Color.CLEARWHITE, .34);

    /**
     * Constructor.
     */
    public DevPane(View aView)
    {
        super();

        _rootView = aView.getRootView();
        _content = _rootView.getContent();

        // Set DevPanes
        _filesInsp = new DevPaneFiles();
        _viewControllersInsp = new DevPaneViewOwners(this);
        _viewsInsp = new DevPaneViews(this);
        _graphicsInsp = new DevPaneGraphics(this);
        _consoleInsp = new DevPaneConsole();
        _exceptionInsp = new DevPaneExceptions();

        // Set All Panes array
        _allPanes = new ViewController[] { _filesInsp, _viewControllersInsp, _viewsInsp, _graphicsInsp, _consoleInsp, _exceptionInsp };
    }

    /**
     * Returns the real content.
     */
    public View getContent()  { return _content; }

    /**
     * Shows the tab for given class.
     */
    public void showTabForClass(Class<?> aClass)
    {
        getUI();
        List<Tab> tabs = _tabView.getTabBar().getTabs();
        int selIndex = ListUtils.findMatchIndex(tabs, tab -> aClass.isInstance(tab.getContentController()));
        _tabView.setSelIndex(selIndex);

        if (!isShowing())
            runLater(() -> setDevPaneShowing(_rootView, true));
    }

    /**
     * Returns the pane for given class.
     */
    public <T extends ViewController> T getPaneForClass(Class<T> aClass)
    {
        return (T) ArrayUtils.findMatch(_allPanes, pane -> aClass.isInstance(pane));
    }

    /**
     * Install in rootView
     */
    public void installInWindow()
    {
        // Set split view as window content
        View ui = getUI();
        _rootView.setContent(ui);

        // Set original window content as first split view item and make sure it can grow
        _splitView.addItem(_content);
        _content.setGrowHeight(true);
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
        _tabView.getTabBar().setTabMinWidth(80);

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
        // Add tabs
        Tab.Builder tabBuilder = new Tab.Builder(_tabView.getTabBar());
        tabBuilder.title("Files").contentController(_filesInsp).add();
        tabBuilder.title("View Controllers").contentController(_viewControllersInsp).add();
        tabBuilder.title("Views").contentController(_viewsInsp).add();
        tabBuilder.title("Graphics").contentController(_graphicsInsp).add();
        tabBuilder.title("Console").contentController(_consoleInsp).add();
        tabBuilder.title("Exceptions").contentController(_exceptionInsp).add();

        // Create CloseBox for TabView.TabBar
        CloseBox closeBox = new CloseBox();
        closeBox.setMargin(0, 15, 0, 0);
        closeBox.setLean(Pos.CENTER_RIGHT);
        closeBox.setManaged(false);
        closeBox.addEventHandler(e -> removeFromWindow(), View.Action);

        // Add CloseBox to TabView.TabBar
        TabBar tabBar = _tabView.getTabBar();
        ViewUtils.addChild(tabBar, closeBox);
    }

    /**
     * RespondUI.
     */
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
        ViewController owner = rootView.getContent().getController();
        return owner instanceof DevPane ? (DevPane) owner : null;
    }

    /**
     * Returns whether DevPane is showing for view.
     */
    public static boolean isDevPaneShowing(View aView)
    {
        DevPane devPane = getDevPane(aView);
        return devPane != null;
    }

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
     * Toggles DevPane showing.
     */
    public static void toggleDevPaneShowing(View aView)
    {
        setDevPaneShowing(aView, !isDevPaneShowing(aView));
    }

    /**
     * Sets a DevPane visible for view.
     */
    public static void showException(Throwable anException)
    {
        DevPaneExceptions.showException(anException);
    }

    /**
     * Returns the current default dev pane view.
     */
    public static View getDefaultDevPaneView()
    {
        WindowView[] windows = WindowView.getOpenWindows();
        return windows.length > 0 ? windows[0].getRootView() : null;
    }

    /**
     * Special SplitView to paint selected view border.
     */
    private class DPSplitView extends SplitView {

        @Override
        protected void paintAbove(Painter aPntr)
        {
            if (_viewControllersInsp.isShowing())
                _viewControllersInsp.paintViewSelection(aPntr, _splitView);
            else if (_viewsInsp.isShowing())
                _viewsInsp.paintViewSelection(aPntr, _splitView);
        }
    }
}
