/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.TaskMonitor;
import snap.view.*;

/**
 * A TaskRunner implementation that runs success/failed/finished on application thread.
 */
public class TaskMonitorPanel extends ViewOwner implements TaskMonitor {

    // The view for progress pane to center on
    View           _view;
    
    // The title for progress pane window to show
    String         _title;
    
    // The delay before task progress panel appears in milliseconds
    int            _delay = 200;
    
    // The task start time in milliseconds
    long           _startTime;
    
    // The number of tasks completed and the total number of tasks
    int            _tasksDone, _tasksTotal;
    
    // The number of task work units completed and the total number of task work units
    int            _taskDone, _taskTotal;
    
    // The current task title
    String         _taskTitle = "First Task";
    
    // Whether monitor has been cancelled
    boolean        _cancelled;
    
    // The activity label
    Label          _alabel;
    
    // The Progress bar
    ProgressBar    _pbar;
    
    // The dialog box
    DialogBox      _dbox;
    
    // Whether monitor should print to standard out
    boolean        _print;

/**
 * Creates a new TaskMonitorPanel for given monitor.
 */
public TaskMonitorPanel(View aView, String aTitle)  { _view = aView; _title = aTitle; }

/**
 * Advise the monitor of the total number of subtasks (invoke only once).
 */
public void startTasks(int aTaskCount)
{
    _tasksTotal = aTaskCount;
    _startTime = System.currentTimeMillis();
    if(_print) System.out.println("StartTasks: " + aTaskCount);
}

/**
 * Begin processing a single task.
 */
public void beginTask(String aTitle, int theTotalWork)
{
    _taskTitle = aTitle; _taskTotal = theTotalWork; _taskDone = 0;
    resetLater();
    if(_print) System.out.println("BeginTask " + (_tasksDone+1) + " of " + _tasksTotal + ": " + aTitle +
        " (" + theTotalWork + " parts)");
}

/**
 * Denote that some work units have been completed.
 */
public void updateTask(int theWorkDone)
{
    _taskDone += theWorkDone;
    resetLater();
    if(_print) System.out.println("UpdateTask " + (_tasksDone+1) + ": " + theWorkDone);
}

/**
 * Finish the current task, so the next can begin.
 */
public void endTask()
{
    _tasksDone++; _taskDone = _taskTotal;
    if(_tasksDone>=_taskTotal && _dbox!=null)
        runLater(() -> hide());
    if(_print) System.out.println("EndTask " + _tasksDone);
}

/**
 * Check for user task cancellation.
 */
public boolean isCancelled()  { return _cancelled; }

/**
 * Create UI.
 */
protected View createUI()
{
    Label tlabel = new Label(); tlabel.setText(_title);
    _alabel = new Label();
    _pbar = new ProgressBar(); _pbar.setPrefWidth(360);
    ColView vbox = new ColView(); vbox.setSpacing(8);
    vbox.addChild(tlabel); vbox.addChild(_alabel); vbox.addChild(_pbar);
    return vbox;
}

/**
 * Override
 */
public synchronized void resetLater()
{
    if(_dbox==null && System.currentTimeMillis()-_startTime>_delay)
        show();
    else if(_dbox!=null)
        super.resetLater();
}

/**
 * Reset UI controls.
 */
protected void resetUI()
{
    String countStr = String.format(" (%d of %d)", _tasksDone+1, _tasksTotal);
    setViewValue(_alabel, _taskTitle + countStr);
    _pbar.setProgress(_taskDone/(double)_taskTotal);
}

/**
 * Show ProgressPane.
 */
protected void show()
{
    _dbox = new DialogBox();
    _dbox.setTitle(_title); _dbox.setContent(getUI());
    _dbox.setOptions(new String[] { "Cancel" });
    
    runLater(() -> _cancelled = _dbox.showConfirmDialog(_view));
}

/**
 * Hide ProgressPane.
 */
protected void hide()  { if(_dbox!=null) _dbox.cancel(); }

}