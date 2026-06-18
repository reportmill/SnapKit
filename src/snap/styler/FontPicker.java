package snap.styler;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A panel to help users pick a font by preview.
 */
public class FontPicker extends SNPViewController {
    
    // The DialogBox when running
    private DialogBox _dialogBox;

    // The currently selected font
    private Font _font;
    
    // The currently selected FontSampleView
    private FontSampleView _selectedSampleView;

    // Constants
    private static Border FONT_SAMPLE_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);

    /**
     * Constructor.
     */
    public FontPicker()
    {
        super();
    }

    /**
     * Shows the FontPicker.
     */
    public Font showPicker(View aView, Font aFont)
    {
        _font = aFont;
        _dialogBox = new DialogBox("Font Picker"); _dialogBox.setContent(getUI());
        if (!_dialogBox.showConfirmDialog(aView)) return null;
        return _font;
    }

    /**
     * Sets the selected FontSampleView.
     */
    public void setSelectedSampleView(FontSampleView sampleView)
    {
        if (_selectedSampleView != null) {
            _selectedSampleView.setFill(_selectedSampleView._color);
            Color textColor = ViewTheme.get().getTextColor();
            _selectedSampleView._label.setTextColor(textColor);
            _selectedSampleView._sampleLowerCase.setTextColor(textColor);
            _selectedSampleView._sampleUpperCase.setTextColor(textColor);
        }

        _selectedSampleView = sampleView;

        // Set sample text color
        _selectedSampleView.setFill(ViewTheme.get().getSelectedFill());
        Color textColor = ViewTheme.get().getSelectedTextColor();
        _selectedSampleView._label.setTextColor(textColor);
        _selectedSampleView._sampleLowerCase.setTextColor(textColor);
        _selectedSampleView._sampleUpperCase.setTextColor(textColor);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Create FontSamplesView
        View fontSamplesView = createFontSamplesView();

        // Create ScrollView for Font samples view
        ScrollView scrollView = new ScrollView(fontSamplesView);
        scrollView.setPrefSize(720,540);
        scrollView.setGrowHeight(true);
        return scrollView;
    }

    /**
     * Initializes the UI panel. This method provides the ability to alter any settings or components of the View that
     * were not set by {@link #createUI()}.
     * <br><br>
     * This method is called automatically by SnapKit after the view has been initialized, and does not need to be
     * called inside of an implementation.
     */
    @Override
    protected void initUI() {

    }

    /**
     * Called automatically by SnapKit after a user reacts with a UI component, this method allows the resetting of
     * the UI. It will not cause accidental {@code respondUI(ViewEvent)} calls. It allows the user to reset or change
     * aspects of the UI after an interaction, such as might be required for an animation or image draw.
     * <br> <br>
     * This method is overridable with no default implementation.
     */
    @Override
    protected void resetUI() {

    }

    /**
     * Called automatically by SnapKit when it detects a ViewEvent. This method should be overridden to respond to UI
     * controls, and provide feedback to user interactions.
     * <br>
     * If you are coming from a Swing environment, this class serves the same purposes as the action listeners attached
     * to each individual component. In this case, all of the events are funnelled into the same method, making it
     * easier to keep track of interactions. Everything is managed from the same location.
     *
     * @param anEvent
     */
    @Override
    protected void respondUI(ViewEvent anEvent) {

    }

    /**
     * Creates the font samples view.
     */
    private View createFontSamplesView()
    {
        // Create FontSamplesView
        ColView fontSamplesView = new ColView();
        fontSamplesView.setFillWidth(true);

        // Get font names
        String[] familyNames = Font.getFamilyNames();
        Color BACKGROUND_COLOR_1 = Color.WHITE;
        Color BACKGROUND_COLOR_2 = new Color("#F8F8F8");
        Color backgroundColor = BACKGROUND_COLOR_1;

        // Iterate over  font family names
        for (String familyName : familyNames) {
            Font font = Font.getFont(familyName, 18); if (font == null) continue;
            FontSampleView fontSampleView = new FontSampleView(font, backgroundColor);
            backgroundColor = backgroundColor == BACKGROUND_COLOR_1 ? BACKGROUND_COLOR_2 : BACKGROUND_COLOR_1;
            fontSampleView.addEventHandler(this::handleFontSampleViewMousePress, MousePress);
            fontSamplesView.addChild(fontSampleView);
            if (familyName.equals(_font.getFamily()))
                setSelectedSampleView(fontSampleView);
        }

        // Return
        return fontSamplesView;
    }

    /**
     * Respond UI.
     */
    protected void handleFontSampleViewMousePress(ViewEvent anEvent)
    {
        FontSampleView fontSampleView = anEvent.getView(FontSampleView.class);
        _font = fontSampleView._font;
        setSelectedSampleView(fontSampleView);

        if (anEvent.getClickCount() == 2)
            _dialogBox.confirm();
    }

    /**
     * A class to display a Font sample.
     */
    public static class FontSampleView extends RowView {

        // The font and the UI elements
        private Font _font;
        private Color _color;
        private Label _label, _sampleLowerCase, _sampleUpperCase;

        /** Creates a new FontSampleView. */
        public FontSampleView(Font aFont, Color aColor)
        {
            _font = aFont; setFill(_color = aColor);
            _label = new Label(aFont.getFamily() + ":");
            _label.setPrefWidth(160);
            _sampleLowerCase = new Label("The quick brown fox jumped over the lazy dog?");
            _sampleLowerCase.setFont(aFont);
            _sampleUpperCase = new Label("THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG!");
            _sampleUpperCase.setFont(aFont);
            ColView colView = new ColView();
            colView.setChildren(_sampleLowerCase, _sampleUpperCase);
            colView.setGrowWidth(true);
            colView.setFillWidth(true);
            setChildren(_label, colView);
            setPadding(5,5,5,5);
            setBorder(FONT_SAMPLE_VIEW_BORDER);
        }
    }
}