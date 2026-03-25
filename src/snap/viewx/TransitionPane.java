/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.view.*;

/**
 * A view that lets you set a content view and have the old one transition out.
 */
public class TransitionPane extends ParentView {

    // The current content view
    private View _content;
    
    // The last content view
    private View _contentOld;

    // The Transition
    private Transition _transition = MoveDown;

    /**
     * Constructor.
     */
    public TransitionPane()  { setClipToBounds(true); }

    /**
     * Returns the content view.
     */
    public View getContent()  { return _content; }

    /**
     * Sets a new content view.
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

        // Make sure this view and new content has no residual animation/transform
        getAnim(0).clear();
        _content.getAnimCleared(0);
        _content.setTransX(0); _content.setTransY(0);

        // Try to animate size change for new content view
        configureAnimateSizeChange();

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
    public void setTransition(Transition aTrans)  { _transition = aTrans; }

    /**
     * Configures animation for old/new content size change.
     */
    private void configureAnimateSizeChange()
    {
        // If old content is not set, just return
        if (_contentOld == null) return;

        // If new content is at same size as old, just return
        double newBestW = _content.getBestWidth(-1);
        double newBestH = _content.getBestHeight(-1);
        if (newBestW == getWidth() && newBestH == getHeight())
            return;

        // Configure animation for this view to new content pref size
        setPrefSize(getWidth(), getHeight());
        getAnim(500).setPrefSize(newBestW, newBestH).setOnFinish(() -> setPrefSize(-1, -1)).play();
    }

    /**
     * Override to return box layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new BoxViewLayout(this, getContent(), true, true); }

    /**
     * Override to sync old content view size to new content view.
     */
    @Override
    protected void layoutImpl()
    {
        super.layoutImpl();
        if (_contentOld != null && _content != null)
            _contentOld.setSize(_content.getWidth(), _content.getHeight());
    }

    /**
     * This is called when a transition is finished.
     */
    protected void handleTransitionFinished()
    {
        if (_contentOld.getParent() != null)
            _transition = MoveDown;
        removeChild(_contentOld);
        _contentOld.setTransX(0);
        _contentOld.setTransY(0);
        _contentOld.setOpacity(1);
        _contentOld = null;
    }

    /**
     * An interface to configure transitions.
     */
    public interface Transition {
        void configure(TransitionPane transView, View newView, View oldView);
    }

    /**
     * This transition slides new content view in from bottom.
     */
    public static Transition MoveUp = (TransitionPane transView, View newView, View oldView) -> {
        newView.setTransY(transView.getHeight());
        newView.getAnim(500).setTransY(0).play();
        if (oldView == null) return;
        oldView.setTransY(0);
        oldView.getAnim(500).setTransY(-transView.getHeight()).setOnFinish(transView::handleTransitionFinished).play();
    };

    /**
     * This transition slides new content view in from top.
     */
    public static Transition MoveDown = (TransitionPane transView, View newView, View oldView) -> {
        newView.setTransY(-transView.getHeight());
        newView.getAnim(500).setTransY(0).play();
        if (oldView == null) return;
        oldView.setTransY(0);
        oldView.getAnim(500).setTransY(transView.getHeight()).setOnFinish(transView::handleTransitionFinished).play();
    };

    /**
     * This transition slides new content view in from left.
     */
    public static Transition MoveLeft = (TransitionPane transView, View newView, View oldView) -> {
        newView.setTransX(-transView.getWidth());
        newView.getAnim(500).setTransX(0).play();
        if (oldView == null) return;
        oldView.setTransX(0);
        oldView.getAnim(500).setTransX(transView.getWidth()).setOnFinish(transView::handleTransitionFinished).play();
    };

    /**
     * This transition slides new content view in from right.
     */
    public static Transition MoveRight = (TransitionPane transView, View newView, View oldView) -> {
        newView.setTransX(transView.getWidth());
        newView.getAnim(500).setTransX(0).play();
        if (oldView == null) return;
        oldView.setTransX(0);
        oldView.getAnim(500).setTransX(-transView.getWidth()).setOnFinish(transView::handleTransitionFinished).play();
    };

    /**
     * This transition fades new content view in.
     */
    public static Transition FadeIn = (TransitionPane transView, View newView, View oldView) -> {
        newView.setOpacity(0);
        newView.getAnim(800).setOpacity(1).play();
        if (oldView == null) return;
        oldView.getAnim(800).setOnFinish(transView::handleTransitionFinished).play();
    };

    /**
     * This transition sets new content view in immediately (no transition).
     */
    public static Transition Instant = (TransitionPane transView, View newView, View oldView) -> {
        transView.removeChild(oldView);
        transView._transition = MoveDown;
    };
}