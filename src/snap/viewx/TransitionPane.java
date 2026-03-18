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
    private View _content;
    
    // The last content view
    private View _contentOld;

    // The Transition
    private Transition _transition = MoveDown;

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
        if (aView == _content) return;

        // Make sure animations are finished (preserving transition)
        Transition transition = _transition;
        if (_content != null)
            _content.getAnim(0).finish().clear();
        if (_contentOld != null)
            _contentOld.getAnim(0).finish().clear();
        _transition = transition;

        // Get last content (remove any previous content that might be transitioning out)
        _contentOld = _content;

        // Set new Content (if null, remove children and return)
        _content = aView;
        if (_content == null) {
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
    public static abstract class Transition {

        /** Configure. */
        public abstract void configure(TransitionPane aTP, View nview, View oview);

        /** Removes OldNode from TransitionPane. */
        public void finish(TransitionPane transitionPane, View oldView)
        {
            if (oldView.getParent()!=null)
                transitionPane._transition = MoveDown;
            transitionPane.removeChild(oldView);
            oldView.setTransX(0);
            oldView.setTransY(0);
            oldView.setOpacity(1);
            transitionPane._contentOld = null;
        }
    }

    /**
     * A class to perform transitions.
     */
    public static Transition MoveUp = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            newView.setTransY(transitionPane.getHeight());
            newView.getAnim(500).setTransY(0).play();
            if (oldView == null) return;
            oldView.setTransY(0);
            oldView.getAnim(500).setTransY(-transitionPane.getHeight()).setOnFinish(() -> finish(transitionPane, oldView)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveDown = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            newView.setTransY(-transitionPane.getHeight());
            newView.getAnim(500).setTransY(0).play();
            if (oldView == null) return;
            oldView.setTransY(0);
            oldView.getAnim(500).setTransY(transitionPane.getHeight()).setOnFinish(() -> finish(transitionPane, oldView)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveLeft = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            newView.setTransX(-transitionPane.getWidth());
            newView.getAnim(500).setTransX(0).play();
            if (oldView == null) return;
            oldView.setTransX(0);
            oldView.getAnim(500).setTransX(transitionPane.getWidth()).setOnFinish(() -> finish(transitionPane, oldView)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition MoveRight = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            newView.setTransX(transitionPane.getWidth());
            newView.getAnim(500).setTransX(0).play();
            if (oldView==null) return;
            oldView.setTransX(0);
            oldView.getAnim(500).setTransX(-transitionPane.getWidth()).setOnFinish(() -> finish(transitionPane, oldView)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition FadeIn = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            newView.setOpacity(0);
            newView.getAnim(800).setOpacity(1).play();
            if (oldView==null) return;
            oldView.getAnim(800).setOnFinish(() -> finish(transitionPane, oldView)).play();
        }
    };

    /**
     * A class to perform transitions.
     */
    public static Transition Instant = new Transition() {

        /** Configure. */
        public void configure(TransitionPane transitionPane, View newView, View oldView)
        {
            transitionPane.removeChild(oldView);
            transitionPane._transition = MoveDown;
        }
    };
}