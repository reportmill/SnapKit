/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Polygon;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * A class to manage a WebBrowser with a standard toolbar.
 */
public class WebBrowserPane extends ViewOwner {

    // The browser
    WebBrowser                     _browser;

/**
 * Returns the WebBrowser.
 */
public WebBrowser getBrowser()  { getUI(); return _browser; }

/**
 * Override to reinstall in BorderView.
 */
protected View createUI()
{
    ParentView superUI = (ParentView)super.createUI();
    View top = superUI.getChild(0), center = superUI.getChild(1), btm = superUI.getChild(2);
    BorderView bpane = new BorderView(); bpane.setTop(top); bpane.setCenter(center); bpane.setBottom(btm);
    return bpane;
}

/**
 * Override to init UI.
 */
protected void initUI()
{
    // Get Browser
    _browser = getView("Browser", WebBrowser.class);
    _browser.addPropChangeListener(pc -> resetLater());
    
    // Get ToolBar
    View toolBar = getView("ToolBar"); toolBar.setPrefSize(toolBar.getWidth(), toolBar.getHeight());
    
    // Get StatusBar
    View statusBar = getView("StatusBar"); statusBar.setPrefSize(statusBar.getWidth(), statusBar.getHeight());
    
    // Set left arrow in BackButton
    ShapeView s2 = new ShapeView(new Polygon(13, 3, 5, 10, 13, 17)); s2.setFill(Color.BLACK);
    getView("BackButton", Button.class).setGraphic(s2);
    
    // Set right arrow in NextButton
    ShapeView s1 = new ShapeView(new Polygon(6, 3, 14, 10, 6, 17)); s1.setFill(Color.BLACK);
    getView("NextButton", Button.class).setGraphic(s1);
    
    // Add key binding to select address bar
    addKeyActionHandler("AddressTextAction", "meta O");
}

/**
 * Reset the UI.
 */
public void resetUI()
{
    // Get URL
    WebBrowser browser = getBrowser();
    WebURL url = browser.getLoader().getURL(); if(url==null) url = browser.getURL();
    
    // Set address text
    setViewText("AddressText", url!=null? url.getString() : "");
    
    // Set ActivityText, StatusText
    setViewText("ActivityText", browser.getActivity());
    setViewText("StatusText", browser.getStatus());
    
    // Update ProgressBar
    ProgressBar pb = getView("ProgressBar", ProgressBar.class); boolean loading = browser.isLoading();
    if(loading && !pb.isVisible()) { pb.setVisible(true); pb.setProgress(-1); }
    else if(!loading && pb.isVisible()) { pb.setProgress(0); pb.setVisible(false); }
}

/**
 * Respond to UI changes.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle BackButton, NextButton, ReloadButton
    if(anEvent.equals("BackButton")) getBrowser().trackBack();
    if(anEvent.equals("NextButton")) getBrowser().trackForward();
    if(anEvent.equals("ReloadButton")) getBrowser().reloadPage();
    
    // Handle AddressText
    if(anEvent.equals("AddressText"))
        getBrowser().setURLString(anEvent.getStringValue());
    
    // Handle AddressTextAction
    if(anEvent.equals("AddressTextAction")) {
        requestFocus("AddressText");
        getView("AddressText", TextField.class).selectAll();
    }
    
    // Handle InfoButton
    if(anEvent.equals("InfoButton")) {
        String msg = "Snap Browser\nA browser for viewing Snap files, pages and applications\n" +
            "Build Date: " + SnapUtils.getBuildInfo() + "\nJVM: " + System.getProperty("java.version");
        DialogBox dbox = new DialogBox("Browser Info"); dbox.setMessage(msg);
        dbox.showMessageDialog(getUI());
    }
}

}