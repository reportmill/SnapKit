/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A View subclass for rects.
 */
public class RectView extends View {

/**
 * Creates a new RectView.
 */
public RectView()  { }

/**
 * Creates a new RectView for given bounds.
 */
public RectView(double aX, double aY, double aW, double aH)  { setBounds(aX, aY, aW, aH); setPrefSize(aW,aH); }

}