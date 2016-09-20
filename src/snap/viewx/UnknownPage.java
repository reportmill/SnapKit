/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.view.*;

/**
 * A page for unknown responses.
 */
public class UnknownPage extends WebPage {

/**
 * Creates a file pane for the given file in the requested mode.
 */
protected View createUI()
{
    StackView pane = new StackView();
    pane.addChild(new Label("Unkown File Type: " + getFile().getType()));
    return pane;
}

}