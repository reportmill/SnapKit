/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.view.*;
import snap.viewx.FilesBrowser;

/**
 * This FileBrowser subclass browses DropBox files.
 */
public class DropBoxPane extends FilesBrowser {

    // UserEmail Text field
    private TextField  _userEmailText;

    /**
     * Constructor.
     */
    public DropBoxPane()
    {
        super();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Do normal version
        super.initUI();

        // Get RowView for normal DirComboBox
        RowView rowView = _dirComboBox.getParent(RowView.class);
        View[] children = rowView.getChildren();
        for (int i = 1; i < children.length; i++)
            children[i].setVisible(false);

        // Reset label
        Label label = (Label) children[0];
        label.setText("User Email:");

        // Add UserEmail
        _userEmailText = new TextField();
        _userEmailText.setPrefWidth(250);
        rowView.addChild(_userEmailText, 1);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Do normal version
        super.resetUI();

        // Update UserEmailText
        DropBoxSite dropBoxSite = (DropBoxSite) getSite();
        String userEmail = dropBoxSite.getEmail();
        _userEmailText.setText(userEmail);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle UserEmailText
        if (anEvent.getView() == _userEmailText) {
            String userEmail = anEvent.getStringValue();
            DropBoxSite.setDefaultEmail(userEmail);
            DropBoxSite newSite = DropBoxSite.getSiteForEmail(userEmail);
            setSite(newSite);
        }

        // Do normal version
        else super.respondUI(anEvent);
    }
}
