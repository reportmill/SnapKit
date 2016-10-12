/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.*;
import snap.view.*;
import snap.web.WebFile;

/**
 * A WebPage subclass for SnapKit files.
 */
public class SnapPage extends WebPage {

/**
 * Returns the SnapKit file root view.
 */
protected View createUI()
{
    ViewArchiver.setUseRealClass(false);
    View superUI = super.createUI();
    ViewArchiver.setUseRealClass(true);
    if(!(superUI instanceof DocView)) {
        superUI.setFill(ViewUtils.getBackFill());
        superUI.setBorder(Color.BLACK, 1);
        superUI.setEffect(new ShadowEffect());
        BorderView bpane = new BorderView(); bpane.setFillCenter(false); bpane.setCenter(superUI);
        bpane.setFill(ViewUtils.getBackDarkFill());
        superUI = bpane;
    }
    return new ScrollView(superUI);
}
    
/**
 * Override to return UI file.
 */
public Object getUISource()  { return getFile(); }

/** Sets the node value for the given binding from the key value. */
protected void setBindingViewValue(Binding aBinding) { }

/** Override to suppress bindings. */
protected void setBindingModelValue(Binding aBinding)  { }

/**
 * Creates a new file for use with showNewFilePanel method.
 */
protected WebFile createNewFile(String aPath)
{
    // Create file
    WebFile file = super.createNewFile(aPath);
    
    // Create text
    StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<SpringView width=\"400\" height=\"400\" PrefWidth=\"400\" PrefHeight=\"400\">\n");
    //sb.append("\t<Label x=\"100\" y=\"100\" width=\"100\" height=\"25\" text=\"Hello World\" />\n");
    sb.append("</SpringView>\n");
    file.setText(sb.toString());
    return file;
}

}