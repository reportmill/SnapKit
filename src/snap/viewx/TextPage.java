/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.gfx.Font;
import snap.text.TextDoc;
import snap.view.Button;
import snap.view.TextArea;
import snap.view.View;
import snap.web.WebFile;

/**
 * A WebPage subclass for a TextFile (or any WebFile treated as text).
 */
public class TextPage extends WebPage {

    // The text pane
    private TFTextPane<?>  _textPane = new TFTextPane<>();

    // The text
    private String  _text;

    /**
     * Returns the text.
     */
    public String getText()
    {
        if (_text != null) return _text;
        return _text = getDefaultText();
    }

    /**
     * Sets the text.
     */
    public void setText(String aString)
    {
        _text = aString;
        if (isUISet())
            getTextArea().setText(aString);
    }

    /**
     * Returns the default text.
     */
    protected String getDefaultText()
    {
        return getFile().getText();
    }

    /**
     * Returns the TextPane.
     */
    public TextPane<?> getTextPane()
    {
        getUI();
        return _textPane;
    }

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()
    {
        getUI();
        return _textPane.getTextArea();
    }

    /**
     * Create UI panel.
     */
    protected View createUI()
    {
        return _textPane.getUI();
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Configure TextPane
        _textPane.getTextArea().setFont(getDefaultFont());
        _textPane.getTextArea().setText(getText());

        Button btn = new Button("Reload");
        btn.setName("ReloadButton");
        btn.setPrefSize(80, 22);
        btn.setLeanX(HPos.RIGHT);
        btn.addEventHandler(e -> getBrowser().reloadPage(), Action);
        _textPane.getToolBarPane().addChild(btn);
        setFirstFocus(getTextArea());
    }

    /**
     * Returns the default font.
     */
    private Font getDefaultFont()
    {
        String[] names = { "Monaco", "Consolas", "Courier" };
        for (String name : names) {
            Font font = new Font(name, 12);
            if (font.getFamily().startsWith(name))
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
    private class TFTextPane<T extends TextDoc> extends TextPane<T> implements WebFile.Updater {

        /**
         * Save file.
         */
        protected void saveChangesImpl()
        {
            try { getFile().save(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }

        /**
         * Override to update Page.Modified.
         */
        public void setTextModified(boolean aFlag)
        {
            super.setTextModified(aFlag);
            WebFile file = getFile();
            if (file != null)
                file.setUpdater(aFlag ? this : null);
        }

        /**
         * WebFile.Updater method.
         */
        public void updateFile(WebFile aFile)
        {
            WebFile file = getFile();
            file.setText(getTextArea().getText());
        }
    }
}