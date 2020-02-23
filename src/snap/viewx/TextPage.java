/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.gfx.*;
import snap.view.*;
import snap.web.WebFile;

/**
 * A WebPage subclass for a TextFile (or any WebFile treated as text).
 */
public class TextPage extends WebPage {

    // The text pane
    TFTextPane           _textPane = new TFTextPane();
    
    // The text
    String               _text;

/**
 * Returns the text.
 */
public String getText()  { return _text!=null? _text : (_text=getDefaultText()); }

/**
 * Sets the text.
 */
public void setText(String aString)
{
    _text = aString;
    if(isUISet())
        getTextArea().setText(aString);
}

/**
 * Returns the default text.
 */
protected String getDefaultText()  { return getFile().getText(); }

/**
 * Returns the TextPane.
 */
public TextPane getTextPane()  { getUI(); return _textPane; }

/**
 * Returns the TextArea.
 */
public TextArea getTextArea()  { getUI(); return _textPane.getTextArea(); }

/**
 * Create UI panel.
 */
protected View createUI()  { return _textPane.getUI(); }

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure TextPane
    _textPane.getTextArea().setFont(getDefaultFont());
    _textPane.getTextArea().setText(getText());

    Button btn = new Button("Reload"); btn.setName("ReloadButton"); btn.setPrefSize(80,22);
    btn.setLeanX(HPos.RIGHT); btn.addEventHandler(e -> getBrowser().reloadPage(), Action);
    _textPane.getToolBarPane().addChild(btn);
    setFirstFocus(getTextArea());
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
 * Override to reload text.
 */
public void reload()
{
    super.reload();
    _text = null;
    _textPane.getTextArea().setText(getText());
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
    public void updateFile(WebFile aFile)  { getFile().setText(getTextArea().getText()); }
}

}