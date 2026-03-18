/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.view.*;

/**
 * A panel that lets you set a view and have the old one transition out.
 */
public class TransitionPane extends ParentView {

    // The current content view
    private View  _content;
    
    // The last content view
    private View  _contentOld;

    // The Transition
    private Transition  _transition = MoveDown;

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
        if (aView==_content) return;

        // Make sure animations are finished
        Transition transition = _transition;
        if (_content!=null)
            _content.getAnim(0).finish().clear();
        if (_contentOld!=null)
            _contentOld.getAnim(0).finish().clear();
        _transition = transition;

        // Get last content (remove any previous content that might be transitioning out)
        _contentOld = _content;

        // Set new Content (if null, remove children and return)
        _content = aView;
        if (_content==null) {
            removeChildren();
            return;
        }

        // Add view
        addChild(aView,0);

        // Make sure new content has no residual animation/transform
        _content.getAnimCleared(0);
        _content.setTransX(0); _content.setTransY(0);

        // If both old/new content, animate size change
        getAnim(0).clear();
        if (_contentOld != null && _content != null) {
            double oldBestW = _contentOld.getBestWidth(-1);
            double oldBestH = _contentOld.getBestHeight(-1);
            double newBestW = _content.getBestWidth(-1);
            double newBestH = _content.getBestHeight(-1);
            if (oldBestW != newBestW || oldBestH != newBestH) {
                setPrefSize(oldBestW, oldBestH);
                getAnim(500).setPrefSize(newBestW, newBestH).setOnFinish(() -> setPrefSize(-1, -1)).play();
                _contentOld.setSize(oldBestW, oldBestH);
                _contentOld.getAnim(500).setWidth(newBestW).setHeight(newBestH).play();
            }
        }

        // Configure transition
        _transition.configure(this, _content, _contentOld);
    }

    /**
     * Returns the current transition.
     */
    public Transition getTransition()  { return _transition; }

    /**
     * Sets the current transition.
     */
    public void setTransition(Transition aTrans)
    {
        _transition = aTrans;
    }

    /**
     * Override to return box layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()
    {
        return new BoxViewLayout(this, getContent(), true, true);
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
            if (oldView.getParent()!=null)
                aTP._transition = MoveDown;
            aTP.removeChild(oldView);
            oldView.setTransX(0);
            oldView.setTransY(0);
            oldView.setOpacity(1);
            aTP._contentOld = null;
        }
    }

    /**
     * A class to perform transitions.
     */
    public static Transition MoveUp = new Transition() {

        /** Configure. */
        public void configure(TransitionPane aTP, View nview, View oview)
        {
            nview.setTransY(aTP.getHeight());
            nview.getAnim(500).setTransY(0).play();
            if (oview==null) return;
            oview.setTransY(0);
            oview.getAnim(500).setTransY(-aTP.getHeight()).setOnFinish(() -> finish(aTP, oview)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveDown = new Transition() {

        /** Configure. */
        public void configure(TransitionPane aTP, View nview, View oview)
        {
            nview.setTransY(-aTP.getHeight());
            nview.getAnim(500).setTransY(0).play();
            if (oview==null) return;
            oview.setTransY(0);
            oview.getAnim(500).setTransY(aTP.getHeight()).setOnFinish(() -> finish(aTP, oview)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveLeft = new Transition() {

        /** Configure. */
        public void configure(TransitionPane aTP, View nview, View oview)
        {
            nview.setTransX(-aTP.getWidth());
            nview.getAnim(500).setTransX(0).play();
            if (oview==null) return;
            oview.setTransX(0);
            oview.getAnim(500).setTransX(aTP.getWidth()).setOnFinish(() -> finish(aTP, oview)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveRight = new Transition() {

        /** Configure. */
        public void configure(TransitionPane aTP, View nview, View oview)
        {
            nview.setTransX(aTP.getWidth());
            nview.getAnim(500).setTransX(0).play();
            if (oview==null) return;
            oview.setTransX(0);
            oview.getAnim(500).setTransX(-aTP.getWidth()).setOnFinish(() -> finish(aTP, oview)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition FadeIn = new Transition() {

        /** Configure. */
        public void configure(TransitionPane aTP, View nview, View oview)
        {
            nview.setOpacity(0);
            nview.getAnim(800).setOpacity(1).play();
            if (oview==null) return;
            oview.getAnim(800).setOnFinish(() -> finish(aTP, oview)).play();
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