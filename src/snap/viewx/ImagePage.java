/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Image;
import snap.view.*;

/**
 * A page for images.
 */
public class ImagePage extends WebPage {

/**
 * Creates a file pane for the given file in the requested mode.
 */
protected View createUI()
{
    StackView pane = new StackView();
    pane.addChild(new ImageView(Image.get(getFile().getInputStream())));
    return pane;
}

}