package snap.styler;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A panel to help users pick a font by preview.
 */
public class FontPicker extends ViewOwner {
    
    // The DialogBox when running
    private DialogBox  _dbox;
    
    // A box to hold labels
    private ColView  _vbox;
    
    // The currently selected font
    private Font  _font;
    
    // The currently selected FontSampleView
    private FontSampleView  _sel;

    /**
     * Shows the FontPicker.
     */
    public Font showPicker(View aView, Font aFont)
    {
        _font = aFont;
        _dbox = new DialogBox("Font Picker"); _dbox.setContent(getUI());
        if (!_dbox.showConfirmDialog(aView)) return null;
        return _font;
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        _vbox = new ColView();
        _vbox.setFillWidth(true);
        ScrollView scroll = new ScrollView(_vbox);
        scroll.setPrefSize(720,540);
        scroll.setGrowHeight(true);
        return scroll;
    }

    /**
     * Initialze UI.
     */
    protected void initUI()
    {
        String[] familyNames = Font.getFamilyNames();
        Color c1 = Color.WHITE, c2 = new Color("#F8F8F8"), c3 = c1;
        Border border = Border.createLineBorder(Color.LIGHTGRAY,1);
        for (String familyName : familyNames) {
            Font font = Font.getFont(familyName, 18);
            FontSampleView fontSampleView = new FontSampleView(font, c3, border);
            c3 = c3==c1? c2 : c1;
            fontSampleView.addEventHandler(this::handleFontSampleViewMousePress, MousePress);
            _vbox.addChild(fontSampleView);
            if (familyName.equals(_font.getFamily()))
                setSelected(fontSampleView);
        }
    }

    /**
     * Respond UI.
     */
    protected void handleFontSampleViewMousePress(ViewEvent anEvent)
    {
        FontSampleView fview = anEvent.getView(FontSampleView.class);
        _font = fview._font;
        setSelected(fview);

        if (anEvent.getClickCount()==2)
            _dbox.confirm();
    }

    /**
     * Sets the selected FontSampleView.
     */
    public void setSelected(FontSampleView aFSV)
    {
        if (_sel!=null) {
            _sel.setFill(_sel._color);
            _sel._label.setTextColor(Color.BLACK);
            _sel._sampleLC.setTextColor(Color.BLACK);
            _sel._sampleUC.setTextColor(Color.BLACK);
        }
        _sel = aFSV;
        _sel.setFill(ViewUtils.getSelectFill());
        _sel._label.setTextColor(ViewUtils.getTextSelectedColor());
        _sel._sampleLC.setTextColor(ViewUtils.getTextSelectedColor());
        _sel._sampleUC.setTextColor(ViewUtils.getTextSelectedColor());
    }

    /**
     * A class to display a Font sample.
     */
    public static class FontSampleView extends RowView {

        // The font and the UI elements
        Font   _font; Color _color;
        Label _label, _sampleLC, _sampleUC;

        /** Creates a new FontSampleView. */
        public FontSampleView(Font aFont, Color aColor, Border aBorder)
        {
            _font = aFont; setFill(_color = aColor);
            _label = new Label(aFont.getFamily() + ":"); _label.setPrefWidth(160);
            _sampleLC = new Label("The quick brown fox jumped over the lazy dog?"); _sampleLC.setFont(aFont);
            _sampleUC = new Label("THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG!"); _sampleUC.setFont(aFont);
            ColView vbox = new ColView(); vbox.setChildren(_sampleLC, _sampleUC);
            vbox.setGrowWidth(true); vbox.setFillWidth(true);
            setChildren(_label, vbox); setPadding(5,5,5,5); setBorder(aBorder);
        }
    }
}