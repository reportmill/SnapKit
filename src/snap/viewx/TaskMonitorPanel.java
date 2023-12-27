/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.TaskMonitor;
import snap.view.*;

/**
 * A TaskMonitor implementation that shows task updates in a panel.
 */
public class TaskMonitorPanel extends ViewOwner implements TaskMonitor {

    // The view for progress pane to center on
    protected View  _view;

    // The title for progress pane window to show
    private String  _title;

    // The delay before task progress panel appears in milliseconds
    private int  _delay = 200;

    // The task start time in milliseconds
    private long  _startTime;

    // The number of tasks completed and the total number of tasks
    private int  _tasksDone, _tasksTotal;

    // The number of task work units completed and the total number of task work units
    private int  _taskDone, _taskTotal;

    // The current task title
    private String  _taskTitle = "First Task";

    // Whether monitor has been cancelled
    private boolean  _cancelled;

    // The activity label
    private Label  _activityLabel;

    // The Progress bar
    private ProgressBar  _progressBar;

    // The dialog box
    private DialogBox  _dialogBox;

    // Whether monitor should print to standard out
    private boolean  DEBUG_MODE = false;

    /**
     * Constructor for view and title.
     */
    public TaskMonitorPanel(View aView, String aTitle)
    {
        _view = aView;
        _title = aTitle;
    }

    /**
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    public void startTasks(int aTaskCount)
    {
        _tasksTotal = aTaskCount;
        _startTime = System.currentTimeMillis();
        if (DEBUG_MODE)
            System.out.println("StartTasks: " + aTaskCount);
    }

    /**
     * Begin processing a single task.
     */
    public void beginTask(String aTitle, int theTotalWork)
    {
        _taskTitle = aTitle;
        _taskTotal = theTotalWork;
        _taskDone = 0;
        resetLater();
        if (DEBUG_MODE)
            System.out.println("BeginTask " + (_tasksDone + 1) + " of " + _tasksTotal + ": " + aTitle +
                " (" + theTotalWork + " parts)");
    }

    /**
     * Denote that some work units have been completed.
     */
    public void updateTask(int theWorkDone)
    {
        _taskDone += theWorkDone;
        resetLater();
        if (DEBUG_MODE)
            System.out.println("UpdateTask " + (_tasksDone + 1) + ": " + theWorkDone);
    }

    /**
     * Finish the current task, so the next can begin.
     */
    public void endTask()
    {
        _tasksDone++;
        _taskDone = _taskTotal;
        if (_tasksDone >= _taskTotal && _dialogBox != null)
            runLater(() -> hide());
        if (DEBUG_MODE)
            System.out.println("EndTask " + _tasksDone);
    }

    /**
     * Check for user task cancellation.
     */
    public boolean isCancelled()  { return _cancelled; }

    /**
     * Show panel.
     */
    protected void show()
    {
        _dialogBox = new DialogBox();
        _dialogBox.setTitle(_title);
        _dialogBox.setContent(getUI());
        _dialogBox.setOptions("Cancel");

        runLater(() -> _cancelled = _dialogBox.showConfirmDialog(_view));
    }

    /**
     * Hide panel.
     */
    public void hide()
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
     * Override
     */
    @Override
    public synchronized void resetLater()
    {
        if (_dialogBox == null && System.currentTimeMillis() - _startTime > _delay)
            show();
        else if (_dialogBox != null)
            super.resetLater();
    }

    /**
     * Reset UI controls.
     */
    @Override
    protected void resetUI()
    {
        String countStr = String.format(" (%d of %d)", _tasksDone + 1, _tasksTotal);
        setViewValue(_activityLabel, _taskTitle + countStr);
        _progressBar.setProgress(_taskDone / (double) _taskTotal);
    }
}