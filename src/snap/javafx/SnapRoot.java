package snap.javafx;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SnapRoot extends Pane {

    // The canvas
    Canvas         _canvas;
    
    // The content
    View           _content;
    
/**
 * Creates a new SnapRoot.
 */
public SnapRoot() { _canvas = new Canvas(); _canvas.setManaged(false); _canvas.setMouseTransparent(true); }

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)
{
    if(aView==_content) return;
    _content = aView;
    if(_content==null) getChildren().clear();
    else getChildren().setAll(_canvas, _content.getNative(javafx.scene.Node.class));
}

/**
 * Sets the content of a Scene to a given node.
 */
public static void setContent(Scene aScene, View aView)  { get(aScene).setContent(aView); }

/**
 * Sets the content of a given Window to a given node.
 */
public static void setContent(Window aWin, View aView)  { get(aWin).setContent(aView); }

/**
 * Returns the SnapRoot for a Scene (installing a new one if needed).
 */
public static SnapRoot get(Scene aScene)
{
    if(aScene==null) return null;
    Parent root = aScene.getRoot();
    SnapRoot sroot = root instanceof SnapRoot? (SnapRoot)root : null;
    if(sroot==null) aScene.setRoot(sroot=new SnapRoot());
    return sroot;
}

/**
 * Returns the SnapRoot for a Window (installing a new one if needed).
 */
public static SnapRoot get(Window aWin)
{
    Scene scene = aWin.getScene();
    if(scene==null) ((Stage)aWin).setScene(scene=new Scene(new SnapRoot()));
    return get(scene);
}

/**
 * Paint nodes.
 */
protected void paintRoot(Rect aRect)
{
    RootView rpane = (RootView)_content;
    Painter pntr = new JFXPainter(_canvas);
    rpane.paintViews(pntr, aRect);
}

/** Returns the preferred width. */
protected double computePrefWidth(double aH)  { return _content!=null? _content.getPrefWidth() : 0; }

/** Returns the preferred height. */
protected double computePrefHeight(double aW)  { return _content!=null? _content.getPrefHeight() : 0; }

/** Layout children. */
protected void layoutChildren()
{
    _canvas.setWidth(getWidth()); _canvas.setHeight(getHeight());
    if(_content!=null) _content.setSize(getWidth(),getHeight());
}

}