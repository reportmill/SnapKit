package snap.styler;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A panel to help users pick a font by preview.
 */
public class FontPicker extends ViewController {
    
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