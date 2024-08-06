/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public class TextDoc extends TextBlock {

    // The URL of the file that provided the text
    private WebURL  _sourceURL;

    /**
     * Constructor.
     */
    public TextDoc()
    {
        super();
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _sourceURL; }

    /**
     * Sets the Source URL.
     */
    public void setSourceURL(WebURL aURL)
    {
        _sourceURL = aURL;
    }

    /**
     * Returns the source file.
     */
    public WebFile getSourceFile()
    {
        WebURL sourceURL = getSourceURL();
        return sourceURL != null ? sourceURL.getFile() : null;
    }

    /**
     * Load TextDoc from source URL.
     */
    public void readFromSourceURL(WebURL aURL)
    {
        // Set Doc Source URL
        setSourceURL(aURL);

        // Get URL text and set in doc
        String text = aURL.getText();
        setString(text);
    }

    /**
     * Write TextDoc text to source file.
     */
    public void writeToSourceFile()
    {
        // Get SourceFile
        WebURL sourceURL = getSourceURL();
        WebFile sourceFile = sourceURL.getFile();
        if (sourceFile == null)
            sourceFile = sourceURL.createFile(false);

        // Get TextDoc string and set in file
        String fileText = getString();
        sourceFile.setText(fileText);

        // Save file
        sourceFile.save();
    }
}
