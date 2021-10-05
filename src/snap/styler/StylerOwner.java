package snap.styler;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.Label;
import snap.view.View;
import snap.view.ViewHost;
import snap.view.ViewOwner;

/**
 * A simple subclass of ViewOwner to work with a Styler.
 */
public class StylerOwner extends ViewOwner {

    // The Styler
    private Styler _styler;

    // The Label
    private Label  _label;

    // The Collapser
    protected Collapser  _collapser;

    // Whether inspector is selected
    private boolean  _selected;

    // Constants
    public static Font LABEL_FONT = Font.Arial14;
    public static Font LABEL_FONT_SEL = Font.Arial14;//.getBold();
    public static Color LABEL_FILL = new Color("#e0e0e4");
    public static Color LABEL_FILL_SEL = new Color("#ececf0"); //"#e0e6f0"
    public static Color LABEL_TEXT_FILL = Color.GRAY;
    public static Color LABEL_TEXT_FILL_SEL = Color.DARKGRAY;
    public static Border LABEL_BORDER_SEL = Border.createLineBorder(LABEL_FILL_SEL.darker(), 1).copyForInsets(Insets.EMPTY);

    /**
     * Returns the styler.
     */
    public Styler getStyler()  { return _styler; }

    /**
     * Sets the styler.
     */
    public void setStyler(Styler aStyler)
    {
        _styler = aStyler;
    }

    /**
     * Returns the Collapser.
     */
    public Collapser getCollapser()
    {
        if (_collapser!=null) return _collapser;

        // Get/add label
        Label label = getLabel();
        View view = getUI();
        ViewHost host = view.getHost();
        int index = view.indexInHost();
        host.addGuest(label, index);

        // Add collaper and label
        //_collapser = Collapser.createCollapserAndLabel(getUI(), getName());
        _collapser = new Collapser(getUI(), label);
        return _collapser;
    }

    /**
     * Returns whether inspector is selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether inspector is selected.
     */
    public void setSelected(boolean aValue)
    {
        if (aValue==isSelected()) return;
        _selected = aValue;

        Collapser collapser = getCollapser();
        if (aValue && !collapser.isExpanded())
            collapser.setExpandedAnimated(true);
        if (!aValue && collapser.isExpanded())
            collapser.setCollapsedAnimated(true);

        getLabel().setFont(aValue ? LABEL_FONT_SEL : LABEL_FONT);
        getLabel().setFill(aValue ? LABEL_FILL_SEL : LABEL_FILL);
        getLabel().setTextFill(aValue ? LABEL_TEXT_FILL_SEL : LABEL_TEXT_FILL);
        getLabel().setBorder(aValue ? LABEL_BORDER_SEL : null);
    }

    /**
     * Returns the label.
     */
    public Label getLabel()
    {
        if (_label!=null) return _label;

        String text = ""; // getName();
        Label label = new Label(text);
        label.setName(text + "Label");
        label.setFont(LABEL_FONT);
        label.setFill(LABEL_FILL);
        label.setTextFill(LABEL_TEXT_FILL);
        label.getStringView().setGrowWidth(true);
        label.setAlign(Pos.CENTER);
        label.setPadding(4,4,4,10);
        label.setMargin(4,8,4,8);
        label.setRadius(5);
        return _label = label;
    }
}
