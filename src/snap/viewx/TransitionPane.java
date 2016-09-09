package snap.viewx;
import snap.gfx.Insets;
import snap.view.*;

/**
 * A panel that lets you set a view and have the old one transition out.
 */
public class TransitionPane extends ParentView {

    // The current content node
    View                 _content;// = new Label("No content");

    // The last content node
    View                 _lastContent;
    
    // The Transition
    Transition           _transition = MoveDown;

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
    
    // Remove LastContent
    if(_lastContent!=null) removeChild(_lastContent);
    
    // Set LastContent and Content
    _lastContent = _content; _content = aView;
    
    // Add view
    if(_content!=null) addChild(aView,0);
    else { removeChildren(); _lastContent = null; return; }
    
    // Configure transition
    _transition.configure(this);
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
protected void layoutChildren()
{
    if(_content==null) return;
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    setClip(getBoundsInside());
    _content.setBounds(x, y, w, h);
}

/**
 * A class to perform transitions.
 */
public static class Transition {

    /** Configure. */
    public void configure(TransitionPane aTP)  { }
    
    /** Removes OldNode from TransitionPane. */
    public void finish(TransitionPane aTP, View oldNode)
    {
        if(oldNode==aTP._lastContent) {
            aTP.removeChild(oldNode); aTP._lastContent = null; aTP._transition = MoveDown; }
        oldNode.setTransX(0); oldNode.setTransY(0);
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
    public void configure(TransitionPane aTP)
    {
        View nview = aTP._content, oview = aTP._lastContent;
        nview.setTransY(aTP.getHeight()); nview.getAnim(0).clear().getAnim(500).setTransY(0).play();
        if(oview==null) return;
        oview.setTransY(0);
        oview.getAnim(0).clear().getAnim(500).setTransY(-aTP.getHeight()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveDown = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP)
    {
        View nview = aTP._content, oview = aTP._lastContent;
        nview.setTransY(-aTP.getHeight()); nview.getAnim(0).clear().getAnim(500).setTransY(0).play();
        if(oview==null) return;
        oview.setTransY(0);
        oview.getAnim(0).clear().getAnim(500).setTransY(aTP.getHeight()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveLeft = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP)
    {
        View nview = aTP._content, oview = aTP._lastContent;
        nview.setTransX(-aTP.getWidth()); nview.getAnim(0).clear().getAnim(500).setTransX(0).play();
        if(oview==null) return;
        oview.setTransX(0);
        oview.getAnim(0).clear().getAnim(500).setTransX(aTP.getWidth()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition MoveRight = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP)
    {
        View nview = aTP._content, oview = aTP._lastContent; TransitionPane tp = aTP;
        nview.setTransX(aTP.getWidth()); nview.getAnim(0).clear().getAnim(500).setTransX(0).play();
        if(oview==null) return;
        oview.setTransX(0);
        oview.getAnim(0).clear().getAnim(500).setTransX(-aTP.getWidth()).setOnFinish(a -> finish(aTP, oview)).play();
    }
};

/**
 * A class to perform transitions.
 */
public static Transition Instant = new Transition() {

    /** Configure. */
    public void configure(TransitionPane aTP)
    {
        if(aTP._lastContent==null) return;
        aTP.removeChild(aTP._lastContent);
        aTP._transition = MoveDown; 
    }
};

}