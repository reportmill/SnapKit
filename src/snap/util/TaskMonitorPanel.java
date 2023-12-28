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

    // The title for progress pane window to show
    private String _title;

    // The delay before task progress panel appears in milliseconds
    private int _delay = 200;

    // The ViewOwner for UI
    private TaskMonitorPanelViewOwner _viewOwner;

    /**
     * Constructor for view and title.
     */
    public TaskMonitorPanel(View aView, String aTitle)
    {
        super();
        _viewOwner = new TaskMonitorPanelViewOwner();
        _view = aView;
        _title = aTitle;
    }

    /**
     * Show panel.
     */
    protected void showPanel()  { _viewOwner.showDialogBox(); }

    /**
     * Hide panel.
     */
    protected void hide()  { _viewOwner.hideDialogBox(); }

    /**
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    public void startTasks(int aTaskCount)
    {
        // Do normal version
        super.startTasks(aTaskCount);

        // Trigger showPanel after delay
        ViewUtils.runDelayed(() -> {
            if (!isFinished())
                showPanel();
        }, _delay);
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
            _dialogBox.setTitle(_title);
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
            titleLabel.setText(_title);
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