/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropChange;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A ActivityMonitor implementation that shows activity updates in a panel after a short delay.
 */
public class ActivityMonitorPanel extends ActivityMonitor {

    // The view for progress pane to center on
    protected View _view;

    // The delay before task progress panel appears in milliseconds
    private int _delay = 200;

    // Whether panel has already shown
    private boolean _didShow;

    // The ViewOwner for UI
    private ActivityMonitorPanelViewOwner _viewOwner;

    /**
     * Constructor.
     */
    public ActivityMonitorPanel()
    {
        super();
        _viewOwner = new ActivityMonitorPanelViewOwner();
    }

    /**
     * Constructor for view and title.
     */
    public ActivityMonitorPanel(View aView, String aTitle)
    {
        this();
        _view = aView;
        setTitle(aTitle);
    }

    /**
     * Show panel.
     */
    protected void showPanel()
    {
        if (_didShow) return;
        _didShow = true;
        ViewUtils.runLater(this::checkForHidePanel);
        _viewOwner.showDialogBox();
    }

    /**
     * Hide panel.
     */
    protected void hide()  { _viewOwner.hideDialogBox(); }

    /**
     * Check for whether panel should show.
     */
    protected void checkForShowPanel()
    {
        if (!isFinished() && !isCancelled())
            showPanel();
    }

    /**
     * Check for whether panel should hide.
     */
    private void checkForHidePanel()
    {
        if (isFinished() || isCancelled())
            hide();
    }

    /**
     * Override to register for showPanel check.
     */
    @Override
    public void setMonitor(ActivityMonitor sourceMonitor)
    {
        super.setMonitor(sourceMonitor);
        ViewUtils.runDelayed(this::checkForShowPanel, _delay);
    }

    /**
     * Override to register for showPanel check.
     */
    @Override
    protected void setTaskCount(int aValue)
    {
        // If going from zero to non-zero, trigger showPanel after delay
        if (getTaskCount() == 0 && aValue != 0)
            ViewUtils.runDelayed(this::checkForShowPanel, _delay);

        // Do normal version
        super.setTaskCount(aValue);
    }

    /**
     * Override to hide panel.
     */
    @Override
    public void setCancelled(boolean aValue)
    {
        super.setCancelled(aValue);
        if (aValue)
            ViewUtils.runLater(this::hide);
    }

    /**
     * Override to hide panel.
     */
    @Override
    public void setFinished(boolean aValue)
    {
        super.setFinished(aValue);
        if (aValue)
            ViewUtils.runDelayed(this::hide, 500);
    }

    /**
     * Override to reset UI if needed.
     */
    @Override
    protected void handleMonitorPropChange(PropChange aPC)
    {
        super.handleMonitorPropChange(aPC);
        _viewOwner.resetLater();
    }

    /**
     * This ViewOwner class shows UI for ActivityMonitorPanel.
     */
    private class ActivityMonitorPanelViewOwner extends ViewOwner {

        // The dialog box
        private DialogBox _dialogBox;

        // The activity label
        private Label _activityLabel;

        // The Progress bar
        private ProgressBar _progressBar;

        /**
         * Constructor.
         */
        public ActivityMonitorPanelViewOwner()
        {
            super();
        }

        /**
         * Show panel.
         */
        protected void showDialogBox()
        {
            _dialogBox = new DialogBox();
            _dialogBox.setTitle(getTitle());
            _dialogBox.setContent(getUI());
            _dialogBox.setOptions("Cancel");

            // Show dialog box
            boolean confirmed = _dialogBox.showConfirmDialog(_view);
            if (!confirmed)
                setCancelled(true);
        }

        /**
         * Hide panel.
         */
        public void hideDialogBox()
        {
            if (_dialogBox != null)
                _dialogBox.cancel();
        }

        /**
         * Create UI.
         */
        @Override
        protected View createUI()
        {
            // Create UI
            Label titleLabel = new Label();
            titleLabel.setText(getTitle());
            _activityLabel = new Label();
            _progressBar = new ProgressBar();
            _progressBar.setPrefSize(360, 16);

            // Create main col view with UI
            ColView colView = new ColView();
            colView.setSpacing(8);
            colView.addChild(titleLabel);
            colView.addChild(_activityLabel);
            colView.addChild(_progressBar);
            return colView;
        }

        /**
         * Reset UI controls.
         */
        @Override
        protected void resetUI()
        {
            // Update ActivityLabel
            String taskTitle = getTaskTitle();
            int taskNumber = getTaskIndex() + 1;
            int taskCount = getTaskCount();
            String activityText = String.format("%s (%d of %d)", taskTitle, taskNumber, taskCount);
            setViewValue(_activityLabel, activityText);

            // Update ProgressBar
            double taskProgress = getTaskProgress();
            if (taskProgress > 0)
                _progressBar.getAnimCleared(500).setValue(ProgressBar.Progress_Prop, taskProgress).play();
            else _progressBar.setProgress(0);
        }
    }
}