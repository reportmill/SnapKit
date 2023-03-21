package snap.styler;
import snap.geom.Insets;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to collapse any view.
 */
public class Collapser {

    // The View
    private View _view;

    // The label
    private Label _label;

    // The first focus view
    private View _firstFocus;

    // Indicator view for collapse
    private View _collapseGraphic;

    // Whether Title view is expanded
    private boolean _expanded = true;

    // Whether view is GrowHeight
    private boolean _growHeight;

    // A Collapse group
    private CollapseGroup _group;

    // Known Collapse groups by name
    private static Map<String, CollapseGroup> _groups = new HashMap<>();

    // Constants
    public static Font LABEL_FONT = Font.Arial14;//.getBold();
    public static Color LABEL_FILL = new Color("#F4F4F8"); //"#e0e6f0"
    public static Color LABEL_TEXT_FILL = Color.DARKGRAY;
    public static Border LABEL_BORDER = Border.createLineBorder(LABEL_FILL.darker(), 1).copyForInsets(Insets.EMPTY);

    /**
     * Creates a Collapser for given View and Label.
     */
    public Collapser(View aView, Label aLabel)
    {
        setView(aView);
        setLabel(aLabel);
    }

    /**
     * Returns the view.
     */
    public View getView()  { return _view; }

    /**
     * Sets the View.
     */
    public void setView(View aView)
    {
        if (aView == _view) return;
        _view = aView;
        _growHeight = aView.isGrowHeight();
    }

    /**
     * Returns the label.
     */
    public Label getLabel()  { return _label; }

    /**
     * Sets the label.
     */
    public void setLabel(Label aLabel)
    {
        // If already set, just return
        if (aLabel == _label) return;

        // Set label
        _label = aLabel;

        // Listen for Label MousePress to trigger expand
        _label.addEventHandler(e -> labelWasPressed(e), View.MousePress);

        // Set CollapseGraphic
        View graphic = getCollapseGraphic();
        _label.setGraphic(graphic);
        graphic.setRotate(isExpanded() ? 90 : 0);
    }

    /**
     * Sets the first focus component.
     */
    public void setFirstFocus(View aView)
    {
        _firstFocus = aView;
    }

    /**
     * Returns whether view is collapsed.
     */
    public boolean isCollapsed()  { return !_expanded; }

    /**
     * Sets whether view is collapsed.
     */
    public void setCollapsed(boolean aValue)
    {
        setExpanded(!aValue);
    }

    /**
     * Sets collapsed animated.
     */
    public void setCollapsedAnimated(boolean aValue)
    {
        setExpandedAnimated(!aValue);
    }

    /**
     * Returns whether title view is expanded.
     */
    public boolean isExpanded()  { return _expanded; }

    /**
     * Sets whether title view is expanded.
     */
    public void setExpanded(boolean aValue)
    {
        // If value already set, just return
        if (aValue == _expanded) return;

        // Set value
        _expanded = aValue;

        // If expanding
        if (aValue) {
            _view.setPrefHeight(-1);
            _view.setGrowHeight(_growHeight);
            if (_group != null)
                _group.collapserDidExpand(this);
        }

        // If callapsing
        else {
            _view.setVisible(false);
            _view.setManaged(false);
            _view.setPrefHeight(0);
            _view.setGrowHeight(false);
            if (_group != null)
                _group.collapserDidCollapse(this);
        }

        // Update graphic
        View graphic = _label.getGraphic();
        if (graphic == null) return;
        graphic.setRotate(aValue ? 90 : 0);
    }

    /**
     * Sets the expanded animated.
     */
    public void setExpandedAnimated(boolean aValue)
    {
        // If already set, just return
        if (aValue == _expanded) return;

        // Cache current size and set new Expanded value
        double viewH = _view.getHeight();
        setExpanded(aValue);

        // Reset/get new PrefSize
        _view.setVisible(true);
        _view.setManaged(true);
        _view.setPrefHeight(-1);
        double prefH = aValue ? _view.getPrefHeight() : 0;

        // Set pref size to current size and expanded to true (for duration of anim)
        _view.setPrefHeight(viewH);

        // Clip View to bounds? (was TitleView.Content)
        _view.setClipToBounds(true);

        // Configure anim to new size
        ViewAnim anim = _view.getAnim(0).clear();
        anim.getAnim(500).setPrefHeight(prefH).setOnFinish(() -> setExpandedAnimDone(aValue)).needsFinish().play();

        // Get graphic and set initial anim rotate
        View graphic = _label.getGraphic();
        if (graphic == null)
            return;
        graphic.setRotate(aValue ? 0 : 90);

        // Configure anim for graphic
        anim = graphic.getAnim(0).clear();
        if (aValue)
            anim.getAnim(500).setRotate(90).play();
        else anim.getAnim(500).setRotate(0).play();
    }

    /**
     * Called when setExpanded animation is done.
     */
    private void setExpandedAnimDone(boolean aValue)
    {
        // If Showing, restore full pref size
        if (aValue) {
            _view.setPrefHeight(-1);
            if (_firstFocus != null)
                _firstFocus.requestFocus();
        }

        // If Hiding, make really hidden
        else {
            _view.setVisible(false);
            _view.setManaged(false);
        }
    }

    /**
     * Called when Label receives a MousePress.
     */
    protected void toggleExpandedAnimated(ViewEvent anEvent)
    {
        //ViewUtils.fireActionEvent(_view, anEvent);
        setExpandedAnimated(!isExpanded());
    }

    /**
     * Sets a collapse group by name.
     */
    public void setGroupForName(String aName)
    {
        _group = getCollapseGroupForName(aName);
        _group.addCollapser(this);
    }

    /**
     * Sets a collapse group by name.
     */
    public CollapseGroup getCollapseGroupForName(String aName)
    {
        CollapseGroup collapseGroup = _groups.get(aName);
        if (collapseGroup == null)
            _groups.put(aName, collapseGroup = new CollapseGroup());
        return collapseGroup;
    }

    /**
     * Called when Label is pressed.
     */
    protected void labelWasPressed(ViewEvent anEvent)
    {
        toggleExpandedAnimated(anEvent);
    }

    /**
     * Returns an image of a down arrow.
     */
    public View getCollapseGraphic()
    {
        // If already set, just return
        if (_collapseGraphic != null) return _collapseGraphic;

        // Create down arrow icon
        Polygon poly = new Polygon(2.5, .5, 2.5, 8.5, 8.5, 4.5);
        ShapeView shapeView = new ShapeView(poly);
        shapeView.setPrefSize(9, 9);
        shapeView.setFill(Color.GRAY);
        shapeView.setBorder(Color.GRAY, 1);

        // Set/return
        return _collapseGraphic = shapeView;
    }

    /**
     * Creates a Collapser for given view, including a label with given name.
     */
    public static Collapser createCollapserAndLabel(View aView, String aLabelTitle)
    {
        // Create Label
        Label label = createLabel(aLabelTitle);

        // Handle TitleView
        if (aView instanceof TitleView) {
            TitleView titleView = (TitleView) aView;
            View content = titleView.getContent();
            BoxView boxView = new BoxView(content, true, true);
            boxView.setPadding(titleView.getPadding());
            ColView colView = new ColView();
            colView.addChild(boxView);
            ViewUtils.replaceView(titleView, colView);
            aView = colView;
        }

        // Add above given view
        ViewHost host = aView.getHost();
        int index = aView.indexInHost();
        host.addGuest(label, index);

        // Create/return collapser
        return new Collapser(aView, label);
    }

    /**
     * Creates a label.
     */
    public static Label createLabel(String aTitle)
    {
        Label label = new Label(aTitle);
        label.setName(aTitle + "Label");
        label.setFont(LABEL_FONT);
        label.setFill(LABEL_FILL);
        label.setTextFill(LABEL_TEXT_FILL);
        label.setBorder(LABEL_BORDER);
        label.getStringView().setGrowWidth(true);
        label.setAlign(Pos.CENTER);
        label.setPadding(4, 4, 4, 10);
        label.setMargin(4, 8, 4, 8);
        label.setBorderRadius(5);
        return label;
    }

    /**
     * A class that tracks multiple collapsers, making sure only one is visible at a time.
     */
    public static class CollapseGroup {

        // The list of collapsers
        private List<Collapser> _collapsers = new ArrayList<>();

        // Whether doing group work
        private boolean _groupWork;

        /**
         * Adds a collapser.
         */
        public void addCollapser(Collapser aCollapser)
        {
            _collapsers.add(aCollapser);
        }

        /**
         * Called when a collapser collapses.
         */
        protected void collapserDidExpand(Collapser aCollapser)
        {
            if (_groupWork) return;
            _groupWork = true;

            for (Collapser c : _collapsers)
                if (c != aCollapser && c.isExpanded())
                    c.setExpandedAnimated(false);

            _groupWork = false;
        }

        /**
         * Called when a collapser collapses.
         */
        protected void collapserDidCollapse(Collapser aCollapser)
        {
            if (_groupWork) return;
            _groupWork = true;

            for (Collapser c : _collapsers)
                if (c != aCollapser && !c.isExpanded()) {
                    c.setExpandedAnimated(true);
                    break;
                }

            _groupWork = false;
        }
    }
}
