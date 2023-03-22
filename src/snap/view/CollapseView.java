package snap.view;
import snap.geom.Insets;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This view subclass holds a header label and content view with support to collapse.
 */
public class CollapseView extends ParentView implements ViewHost {

    // The header label
    private Label _label;

    // The content view
    private View _content;

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
     * Constructor.
     */
    public CollapseView()
    {
        super();
        setMargin(4, 0, 4, 0);
        setSpacing(4);

        // Create/add label
        _label = createLabel();
        addChild(_label);
    }

    /**
     * Constructor for given View.
     */
    public CollapseView(String aTitle, View aView)
    {
        this();

        // Set title and name
        if (aTitle != null) {
            setTitle(aTitle);
            String name = aTitle.replace(" ", "") + "CollapseView";
            setName(name);
        }
        setContent(aView);
    }

    /**
     * Returns the title.
     */
    public String getTitle()  { return _label.getText(); }

    /**
     * Sets the title.
     */
    public void setTitle(String aValue)
    {
        _label.setText(aValue);
    }

    /**
     * Returns the label.
     */
    public Label getLabel()  { return _label; }

    /**
     * Creates a label.
     */
    protected Label createLabel()
    {
        // Create label
        Label label = new Label();
        label.setFont(LABEL_FONT);
        label.setFill(LABEL_FILL);
        label.setTextFill(LABEL_TEXT_FILL);
        label.setBorder(LABEL_BORDER);
        label.getStringView().setGrowWidth(true);
        label.setAlign(Pos.CENTER);
        label.setPadding(4, 4, 4, 10);
        label.setMargin(0, 8, 0, 8);
        label.setBorderRadius(5);

        // Listen for Label MousePress to trigger expand
        label.addEventHandler(e -> labelWasPressed(e), View.MousePress);

        // Set CollapseGraphic
        View graphic = getCollapseGraphic();
        label.setGraphic(graphic);
        graphic.setRotate(isExpanded() ? 90 : 0);

        // Return
        return label;
    }

    /**
     * Returns the content view.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content view.
     */
    public void setContent(View aView)
    {
        // If already set, just return
        if (aView == _content) return;

        // Remove old
        if (_content != null)
            removeChild(_content);

        // Set
        _content = aView;

        // Add new
        if (_content != null) {
            addChild(_content);
            _growHeight = aView.isGrowHeight();
            setGrowHeight(aView.isGrowHeight());
        }
    }

    /**
     * Returns whether view is collapsed.
     */
    public boolean isCollapsed()  { return !_expanded; }

    /**
     * Sets whether view is collapsed.
     */
    public void setCollapsed(boolean aValue)  { setExpanded(!aValue); }

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
            setGrowHeight(_growHeight);
            _content.setPrefHeight(-1);
            if (_group != null)
                _group.collapseViewDidExpand(this);
        }

        // If collapsing
        else {
            setGrowHeight(false);
            _content.setVisible(false);
            _content.setManaged(false);
            _content.setPrefHeight(0);
            if (_group != null)
                _group.collapseViewDidCollapse(this);
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
        double viewH = _content.getHeight();
        setExpanded(aValue);

        // Reset/get new PrefSize
        _content.setVisible(true);
        _content.setManaged(true);
        _content.setPrefHeight(-1);
        double prefH = aValue ? _content.getPrefHeight() : 0;

        // Set pref size to current size and expanded to true (for duration of anim)
        _content.setPrefHeight(viewH);

        // Clip View to bounds? (was TitleView.Content)
        _content.setClipToBounds(true);

        // Configure anim to new size
        ViewAnim anim = _content.getAnim(0).clear();
        anim.getAnim(500).setPrefHeight(prefH);
        anim.setOnFinish(() -> setExpandedAnimDone(aValue)).needsFinish();
        anim.play();

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
            _content.setPrefHeight(-1);
            if (_firstFocus != null)
                _firstFocus.requestFocus();
        }

        // If Hiding, make really hidden
        else {
            _content.setVisible(false);
            _content.setManaged(false);
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
     * Sets the first focus component.
     */
    public void setFirstFocus(View aView)
    {
        _firstFocus = aView;
    }

    /**
     * Sets a collapse group by name.
     */
    public void setGroupForName(String aName)
    {
        _group = getCollapseGroupForName(aName);
        _group.addCollapseView(this);
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
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return ColView.getPrefWidth(this, -1);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return ColView.getPrefHeight(this, -1);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        ColView.layout(this, true);
    }

    /**
     * ViewHost method: Override to return 1 if content is present.
     */
    public int getGuestCount()  { return getContent() != null ? 1 : 0; }

    /**
     * ViewHost method: Override to return content (and complain if index beyond 0).
     */
    public View getGuest(int anIndex)
    {
        if (anIndex > 0)
            throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        return getContent();
    }

    /**
     * ViewHost method: Override to set content.
     */
    public void addGuest(View aChild, int anIndex)
    {
        if (anIndex > 0)
            System.err.println("CollapseView: Attempt to addGuest beyond 0");
        setContent(aChild);
    }

    /**
     * ViewHost method: Override to clear content (and complain if index beyond 0).
     */
    public View removeGuest(int anIndex)
    {
        if (anIndex > 0)
            throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        View content = getContent();
        setContent(null);
        return content;
    }

    /**
     * Override to forward to label.
     */
    @Override
    public String getText()  { return getLabel().getText(); }

    /**
     * Override to forward to label.
     */
    @Override
    public void setText(String aStr)  { getLabel().setText(aStr); }

    /**
     * Replaces given view with CollapseView with given title.
     */
    public static CollapseView replaceViewWithCollapseView(View aView, String aTitle)
    {
        ParentView parentView = aView.getParent();
        int indexInParent = aView.indexInParent();
        CollapseView collapseView = new CollapseView(aTitle, aView);
        ViewUtils.addChild(parentView, collapseView, indexInParent);
        return collapseView;
    }

    /**
     * A class that tracks multiple collapsers, making sure only one is visible at a time.
     */
    public static class CollapseGroup {

        // The list of collapsers
        private List<CollapseView> _collapseViews = new ArrayList<>();

        // Whether doing group work
        private boolean _groupWork;

        /**
         * Adds a collapser.
         */
        public void addCollapseView(CollapseView aCollapser)
        {
            _collapseViews.add(aCollapser);
        }

        /**
         * Called when a CollapseView collapses.
         */
        protected void collapseViewDidExpand(CollapseView aCollapseView)
        {
            if (_groupWork) return;
            _groupWork = true;

            for (CollapseView collapseView : _collapseViews)
                if (collapseView != aCollapseView && collapseView.isExpanded())
                    collapseView.setExpandedAnimated(false);

            _groupWork = false;
        }

        /**
         * Called when a CollapseView collapses.
         */
        protected void collapseViewDidCollapse(CollapseView aCollapseView)
        {
            if (_groupWork) return;
            _groupWork = true;

            for (CollapseView collapseView : _collapseViews)
                if (collapseView != aCollapseView && !collapseView.isExpanded()) {
                    collapseView.setExpandedAnimated(true);
                    break;
                }

            _groupWork = false;
        }
    }
}
