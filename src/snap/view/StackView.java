package snap.view;
import snap.gfx.Pos;

/**
 * A ChildView subclass to show overlapping children.
 */
public class StackView extends ChildView {

    // The layout
    ViewLayout.StackLayout _layout = new ViewLayout.StackLayout(this);

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

}