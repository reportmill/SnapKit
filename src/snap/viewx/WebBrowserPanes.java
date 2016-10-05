/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.*;
import snap.view.*;

/**
 * Holds some weird subclasses of WebBrowserPane.
 */
public class WebBrowserPanes {

/**
 * A version with iPhone background.
 */
public static class Labeled extends WebBrowserPane {

    /** Creates Labeled with text. */
    public Labeled(String aStr)  { _text = aStr; } String _text;
    
    /** Create UI with iPhone image in background. */
    protected View createUI()
    {
        // Load normal UI
        View superUI = super.createUI();
        
        // Create pretty label for top
        Label plabel = new Label(); plabel.setPrefHeight(42); plabel.setMaxWidth(Double.MAX_VALUE);
        plabel.setText(_text);
        plabel.setTextFill(Color.WHITE); plabel.setFont(new Font("Times", 32));
        
        // Attach a LinearGradient style
        Color c1 = Color.get("#85B5E6"), c2 = Color.get("#426FA3");
        GradientPaint lg = new GradientPaint(0, GradientPaint.getStops(0, c1, .5, c2, 1, c1));
        plabel.setFill(lg); plabel.setBorder(Color.GRAY, 1);
        
        // Create BorderView with label and normal UI and return
        BorderView bpane = new BorderView(); bpane.setPrefSize(640, 480);
        bpane.setTop(plabel); bpane.setCenter(superUI);
        return bpane;
    }
    
    /** Configure browser. */
    protected void initUI()
    {
        super.initUI();
        WebBrowser browser = getBrowser(); browser.setFill(snap.gfx.Color.WHITE);
        browser.setGrowHeight(true);
    }
}

/**
 * A version with iPhone background.
 */
public static class iPhone extends WebBrowserPane {

    /** Create UI with iPhone image in background. */
    protected View createUI()
    {
        // Load normal UI and size
        double sc = .75;
        View superUI = super.createUI(); superUI.setPrefSize(1024*sc, 768*sc); superUI.setMaxSize(1024*sc, 768*sc);
        
        // Get background image
        Image image = Image.get(getClass(), "IPadMini.png");
        ImageView iview = new ImageView(image); //iview.setFitWidth(1289*scale); iview.setFitHeight(870*scale);
        
        // Install in StackView
        StackView sview = new StackView(); sview.setFill(Color.GRAY);
        sview.setChildren(iview, superUI);
        return sview;
    }
}

}