/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Insets;
import snap.view.*;

/**
 * A panel that lets you set a view and have the old one transition out.
 */
public class TransitionPane extends ParentView {

    // The current content node
    View                 _content;

    // The Transition
    Transition           _transition = MoveDown;

/**
 * Creates a TransitionPane.
 */
public TransitionPane()  { setClipToBounds(true); }

/**
 * Returns the content node.
 */
public View getContent()  { return _content; }

/**
 * Sets a new content node.
 */
public void setContent(View aView)
{
    // If view already set, just return
    if(aView==_content) return;
    
    // Get last view
    View oldView = getChildCount()>0? getChildLast() : null;
    while(getChildCount()>1) removeChild(0);
    
    // Set LastContent and Content
    _content = aView;
    
    // Add view
    if(_content!=null) addChild(aView,0);
    else { removeChildren(); return; }
    
    // Configure transition
    _transition.configure(this, _content, oldView);
}

/**
 * Returns the current transition.
 */
public Transition getTransition()  { return _transition; }

/**
 * Sets the current transition.
 */
public void setTransition(Transition aTrans)  { _transition = aTrans; }

/**
 * Override to return preferred width of content.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    View c = getContent(); double cw = c!=null? c.getPrefWidth() : 0;
    return ins.left + cw + ins.right;
}

/**
 * Override to return preferred height of content.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    View c = getContent(); double ch = c!=null? c.getPrefHeight() : 0;
    return ins.top + ch + ins.bottom;
}

/**
 * Override to layout content.
 */
protected void layoutImpl()
{
    if(_content==null) return;
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    _content.setBounds(x, y, w, h);
}

/**
 * A class to perform transitions.
 */
public static class Transition {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)  { }
    
    /** Removes OldNode from TransitionPane. */
    public void finish(TransitionPane aTP, View oldView)
    {
        if(oldView.getParent()!=null) aTP._transition = MoveDown;
        aTP.removeChild(oldView);
        oldView.setTransX(0); oldView.setTransY(0);
    }
}


/**
 * A Transition.
 */

/**
 * A class to perform transitions.
 */
public static Transition MoveUp = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)
    {
        nview.setTransY(aTP.getHeight()); nview.getAnimCleared(500).setTransY(0).play();
        if(oview==null) return;
        oview.setTransY(0);
        oview.getAnimCleared(500).setTransY(-aTP.getHeight()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveDown = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)
    {
        nview.setTransY(-aTP.getHeight()); nview.getAnimCleared(500).setTransY(0).play();
        if(oview==null) return;
        oview.setTransY(0);
        oview.getAnimCleared(500).setTransY(aTP.getHeight()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveLeft = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)
    {
        nview.setTransX(-aTP.getWidth()); nview.getAnimCleared(500).setTransX(0).play();
        if(oview==null) return;
        oview.setTransX(0);
        oview.getAnimCleared(500).setTransX(aTP.getWidth()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveRight = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)
    {
        nview.setTransX(aTP.getWidth()); nview.getAnimCleared(500).setTransX(0).play();
        if(oview==null) return;
        oview.setTransX(0);
        oview.getAnimCleared(500).setTransX(-aTP.getWidth()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition Instant = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP, View nview, View oview)
    {
        aTP.removeChild(oview);
        aTP._transition = MoveDown; 
    }
};

}