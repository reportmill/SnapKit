package snap.styler;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.Convert;
import snap.view.ComboBox;
import snap.view.TitleView;
import snap.view.ViewEvent;
import snap.viewx.ColorButton;

/**
 * Class to provide UI editing for Styler font/text attributes.
 */
public class FontTool extends StylerOwner {

    // The TitleView
    private TitleView _titleView;

    // The font face ComboBox
    private ComboBox <String> _fontFaceComboBox;

    // The font size ComboBox
    private ComboBox <Number> _fontSizeComboBox;

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get/configure FontFaceComboBox
        _fontFaceComboBox = getView("FontFaceComboBox", ComboBox.class);
        _fontFaceComboBox.getPopupList().setMaxRowCount(20);
        _fontFaceComboBox.setItems(Font.getFamilyNames());

        // Get/configure FontSizeComboBox
        _fontSizeComboBox = getView("FontSizeComboBox", ComboBox.class);
        Number sizes[] = { 6, 8, 9, 10, 11, 12, 14, 16, 18, 22, 24, 36, 48, 64, 72, 96, 128, 144 };
        _fontSizeComboBox.setItems(sizes);
        _fontSizeComboBox.setItemTextFunction(i -> Convert.stringValue(i) + " pt");

        _titleView = getView("TextTitleView", TitleView.class);
        //textTitleView.setContent(_fontPanel.getUI());
    }

    /**
     * Called to reset UI controls.
     */
    protected void resetUI()
    {
        // Get styler
        Styler styler = getStyler();
        Font font = styler.getFont();

        // Reset FontFaceComboBox, FontSizeComboBox
        _fontFaceComboBox.setSelItem(font.getFamily());
        String fstext = _fontSizeComboBox.getText(font.getSize());
        _fontSizeComboBox.setText(fstext);

        // Reset TextColorButton
        Color textColor = styler.getTextColor();
        setViewValue("TextColorButton", textColor != null ? textColor : Color.BLACK);

        // Reset BoldButton, ItalicButton, UnderlineButton, OutlineButton
        setViewValue("BoldButton", font.isBold());
        setViewEnabled("BoldButton", font.getBold() != null);
        setViewValue("ItalicButton", font.isItalic());
        setViewEnabled("ItalicButton", font.getItalic() != null);
        setViewValue("UnderlineButton", styler.isUnderlined());
        setViewValue("OutlineButton", styler.getTextBorder() != null);
    }

    /**
     * Called to respond to UI controls
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle FontFaceComboBox
        if (anEvent.equals("FontFaceComboBox")) {
            String fname = anEvent.getText();
            styler.setFontFamily(fname);
        }

        // Handle FontSizeComboBox
        if (anEvent.equals("FontSizeComboBox")) {
            styler.setFontSize(anEvent.getFloatValue(), false);
        }

        // Handle TextColorButton
        if (anEvent.equals("TextColorButton")) {
            Color color = anEvent.getView(ColorButton.class).getColor();
            styler.setTextColor(color);
        }

        // Handle FontSizeUpButton, FontSizeDownButton
        if (anEvent.equals("FontSizeUpButton")) {
            Font font = styler.getFont();
            double size = font.getSize() < 16 ? 1 : 2;
            styler.setFontSize(size, true);
        }
        if (anEvent.equals("FontSizeDownButton")) {
            Font font = styler.getFont();
            double size = font.getSize() < 16 ? -1 : -2;
            styler.setFontSize(size, true);
        }

        // Handle BoldButton, ItalicButton, UnderlineButton, OutlineButton
        if (anEvent.equals("BoldButton"))
            styler.setFontBold(anEvent.getBoolValue());
        if (anEvent.equals("ItalicButton"))
            styler.setFontItalic(anEvent.getBoolValue());
        if (anEvent.equals("UnderlineButton"))
            styler.setUnderlined(anEvent.getBoolValue());
        if (anEvent.equals("OutlineButton")) {
            Border tbdr = styler.getTextBorder();
            Border tbdr2 = tbdr==null ? Border.blackBorder() : null;
            Color tclr2 = tbdr==null ? Color.WHITE : Color.BLACK;
            styler.setTextBorder(tbdr2);
            styler.setTextColor(tclr2);
        }

        // Handle FontPickerButton
        if (anEvent.equals("FontPickerButton")) {
            Font ofont = styler.getFont();
            Font font = new FontPicker().showPicker(styler.getClientView(), ofont);
            if(font != null)
                styler.setFontFamily(font.getFamily());
        }
    }
}