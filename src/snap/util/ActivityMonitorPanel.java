/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropChange;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * This class shows activity monitor activity updates in a panel after a short delay.
 */
class ActivityMonitorPanel {

    // The view for progress pane to center on
    protected View _view;

    // The activity monitor
    private ActivityMonitor _activityMonitor;

    // The delay before task progress panel appears in milliseconds
    private int _delay = 200;

    // Whether panel has already shown
    private boolean _didShow;

    // The ViewOwner for UI
    private ActivityMonitorPanelViewOwner _viewOwner;

    /**
     * Constructor for view and title.
     */
    public ActivityMonitorPanel(View aView, ActivityMonitor activityMonitor)
    {
        super();
        _view = aView;
        _activityMonitor = activityMonitor;
        _viewOwner = new ActivityMonitorPanelViewOwner();
        activityMonitor.addPropChangeListener(this::handleMonitorPropChange);
        ViewUtils.runDelayed(this::checkForShowPanel, _delay);
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
        if (!_activityMonitor.isFinished() && !_activityMonitor.isCancelled())
            showPanel();
    }

    /**
     * Check for whether panel should hide.
     */
    private void checkForHidePanel()
    {
        if (_activityMonitor.isFinished() || _activityMonitor.isCancelled())
            hide();
    }

    /**
     * Called when monitor has prop change.
     */
    protected void handleMonitorPropChange(PropChange propChange)
    {
        switch (propChange.getPropName()) {
            case Activity.Finished_Prop -> ViewUtils.runDelayed(this::hide, 500);
            case Activity.Cancelled_Prop -> ViewUtils.runLater(this::hide);
        }
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
            _dialogBox.setTitle(_activityMonitor.getTitle());
            _dialogBox.setContent(getUI());
            _dialogBox.setOptions("Cancel");

            // Show dialog box
            boolean confirmed = _dialogBox.showConfirmDialog(_view);
            if (!confirmed)
                _activityMonitor.setCancelled(true);
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
            titleLabel.setText(_activityMonitor.getTitle());
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
            String taskTitle = _activityMonitor.getTaskTitle();
            int taskNumber = _activityMonitor.getTaskIndex() + 1;
            int taskCount = _activityMonitor.getTaskCount();
            String activityText = String.format("%s (%d of %d)", taskTitle, taskNumber, taskCount);
            setViewValue(_activityLabel, activityText);

            // Update ProgressBar
            double taskProgress = _activityMonitor.getTaskProgress();
            if (taskProgress > 0)
                _progressBar.getAnimCleared(500).setValue(ProgressBar.Progress_Prop, taskProgress).play();
            else _progressBar.setProgress(0);
        }
    }
}