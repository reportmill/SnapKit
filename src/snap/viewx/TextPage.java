package snap.viewx;
import snap.gfx.*;
import snap.view.*;
import snap.web.WebFile;

/**
 * A WebPage subclass for a TextFile (or any WebFile treated as text).
 */
public class TextPage extends WebPage {

    // The text pane
    TFTextPane           _textPane;
    
    // The text
    String               _text;

/**
 * Returns the text.
 */
public String getText()  { return _text!=null? _text : (_text=getFile().getText()); }

/**
 * Sets the text.
 */
public void setText(String aString)  { _text = aString; }

/**
 * Returns the TextPane.
 */
public TextPane getTextPane()  { getUI(); return _textPane; }

/**
 * Returns the TextArea.
 */
public TextViewBase getTextArea()  { getUI(); return _textPane.getTextView(); }

/**
 * Create UI panel.
 */
protected View createUI()
{
    _textPane = new TFTextPane();
    _textPane.getTextView().setFont(getDefaultFont());
    _textPane.getTextView().setText(getText());
    return _textPane.getUI();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    Button btn = new Button("Reload"); btn.setName("ReloadButton"); btn.setPrefSize(80,22);
    btn.setLeanX(HPos.RIGHT); btn.addEventHandler(e -> getBrowser().reloadPage(), Action);
    _textPane.getToolBarPane().addChild(btn);
}

/**
 * Returns the default font.
 */
private Font getDefaultFont()
{
    String names[] = { "Monaco", "Consolas", "Courier" };
    for(int i=0; i<names.length; i++) {
        Font font = new Font(names[i], 12);
        if(font.getFamily().startsWith(names[i]))
            return font;
    }
    return new Font("Arial", 12);
}

/**
 * A TextPane subclass.
 */
private class TFTextPane extends TextPane implements WebFile.Updater {

    /** Save file. */
    protected void saveChangesImpl()
    {
        try { getFile().save(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    
    /** Override to update Page.Modified. */
    public void setTextModified(boolean aFlag)
    {
        super.setTextModified(aFlag);
        if(getFile()!=null) getFile().setUpdater(aFlag? this : null);
    }
    
    /** WebFile.Updater method. */
    public void updateFile(WebFile aFile)  { getFile().setText(getTextView().getText()); }
}

}