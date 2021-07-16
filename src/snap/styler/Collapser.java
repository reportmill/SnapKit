package snap.styler;

import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;

/**
 * A class to collapse any view.
 */
public class Collapser {

    // The View
    private View  _view;

    // The label
    private Label  _label;

    // Indicator view for collapse
    private View  _clpView;

    // Whether Title view is expanded
    private boolean  _expanded = true;

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
        if (aView==_view) return;
        _view = aView;
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
        if (aLabel==_label) return;

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
        if(aValue==_expanded) return;

        // Set value
        _expanded = aValue;

        // If expanding
        if (aValue) {
            _view.setPrefHeight(-1);
        }

        // If callapsing
        else {
            _view.setPrefHeight(0);
        }

        // Update graphic
        View graphic = _label.getGraphic(); if (graphic==null) return;
        graphic.setRotate(aValue? 90 : 0);
    }

    /**
     * Sets the expanded animated.
     */
    public void setExpandedAnimated(boolean aValue)
    {
        // If already set, just return
        if(aValue==_expanded) return;

        // Cache current size and set new Expanded value
        double h = _view.getHeight();
        setExpanded(aValue);

        // Reset/get new PrefSize
        _view.setPrefHeight(-1);
        double ph = aValue ? _view.getPrefHeight() : 0;

        // Set pref size to current size and expanded to true (for duration of anim)
        _view.setPrefHeight(h);

        // Clip View to bounds? (was TitleView.Content)
        _view.setClipToBounds(true);

        // Configure anim to new size
        ViewAnim anim = _view.getAnim(0).clear();
        anim.getAnim(500).setPrefHeight(ph).setOnFinish(() -> setExpandedAnimDone(aValue)).needsFinish().play();

        // Get graphic and set initial anim rotate
        View graphic = _label.getGraphic(); if (graphic==null) return;
        graphic.setRotate(aValue? 0 : 90);

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
        if (aValue)
            _view.setPrefHeight(-1);
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
        // If down arrow icon hasn't been created, create it
        if (_clpView!=null) return _clpView;
        Polygon poly = new Polygon(2.5, .5, 2.5, 8.5, 8.5, 4.5);
        ShapeView sview = new ShapeView(poly); sview.setPrefSize(9,9);
        sview.setFill(Color.GRAY); sview.setBorder(Color.GRAY, 1);
        return _clpView = sview;
    }

    /**
     * Creates a Collapser for given view, including a label with given name.
     */
    public static Collapser createCollapserAndLabel(View aView, String aLabelTitle)
    {
        // Create Label
        Label label = createLabel(aLabelTitle);

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
    private static Label createLabel(String aTitle)
    {
        Label label = new Label(aTitle);
        label.setName(aTitle + "Label");
        label.setFill(new Color("#e0e0e4"));
        label.setFont(Font.Arial14);
        label.getStringView().setGrowWidth(true);
        label.setTextFill(Color.GRAY);
        label.setAlign(Pos.CENTER);
        label.setPadding(4,4,4,10);
        label.setMargin(2,8,2,8);
        label.setRadius(10);
        return label;
    }
}
