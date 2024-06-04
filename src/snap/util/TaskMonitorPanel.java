/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A TaskMonitor implementation that shows task updates in a panel after a short delay.
 */
public class TaskMonitorPanel extends TaskMonitor {

    // The view for progress pane to center on
    protected View _view;

    // The delay before task progress panel appears in milliseconds
    private int _delay = 200;

    // Whether panel has already shown
    private boolean _didShow;

    // The ViewOwner for UI
    private TaskMonitorPanelViewOwner _viewOwner;

    /**
     * Constructor.
     */
    public TaskMonitorPanel()
    {
        super();
        _viewOwner = new TaskMonitorPanelViewOwner();
    }

    /**
     * Constructor for view and title.
     */
    public TaskMonitorPanel(View aView, String aTitle)
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
     * Override to register for showPanel check.
     */
    @Override
    public void setMonitor(TaskMonitor sourceMonitor)
    {
        super.setMonitor(sourceMonitor);
        ViewUtils.runDelayed(this::checkForShowPanel, _delay);
    }

    /**
     * Override to register for showPanel check.
     */
    @Override
    protected void setTasksTotal(int aValue)
    {
        // If going from zero to non-zero, trigger showPanel after delay
        if (getTasksTotal() == 0 && aValue != 0)
            ViewUtils.runDelayed(this::checkForShowPanel, _delay);

        // Do normal version
        super.setTasksTotal(aValue);
    }

    /**
     * Override to hide panel.
     */
    @Override
    public void setCancelled(boolean aValue)
    {
        super.setCancelled(aValue);
        if (aValue)
            hide();
    }

    /**
     * Override to hide panel.
     */
    @Override
    public void setFinished(boolean aValue)
    {
        super.setFinished(aValue);
        if (aValue)
            hide();
    }

    /**
     * This ViewOwner class shows UI for TaskMonitorPanel.
     */
    private class TaskMonitorPanelViewOwner extends ViewOwner {

        // The dialog box
        private DialogBox _dialogBox;

        // The activity label
        private Label _activityLabel;

        // The Progress bar
        private ProgressBar _progressBar;

        /**
         * Constructor.
         */
        public TaskMonitorPanelViewOwner()
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
            _progressBar.setPrefWidth(360);

            // Create main col view with UI
            ColView colView = new ColView();
            colView.setSpacing(8);
            colView.addChild(titleLabel);
            colView.addChild(_activityLabel);
            colView.addChild(_progressBar);
            return colView;
        }

        /**
         * Initialize when showing.
         */
        @Override
        protected void initShowing()
        {
            addPropChangeListener(pc -> resetLater());
        }

        /**
         * Reset UI controls.
         */
        @Override
        protected void resetUI()
        {
            // Update ActivityLabel
            String taskTitle = getTaskTitle();
            int tasksDone = getTasksDone() + 1;
            int tasksTotal = getTasksTotal();
            String activityText = String.format("%s (%d of %d)", taskTitle, tasksDone + 1, tasksTotal);
            setViewValue(_activityLabel, activityText);

            // Update ProgressBar
            double taskProgress = getTaskProgress();
            _progressBar.setProgress(taskProgress);
        }
    }
}