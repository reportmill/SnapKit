/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.view.View;
import java.io.*;
import java.util.Objects;

/**
 * Interface for tracking the progress of arbitrary tasks.
 */
public class ActivityMonitor extends Activity {

    // The current task title
    private String _taskTitle = "First Task";

    // The total number of task work units
    private int _taskWorkUnitCount;

    // The current task work unit index
    private int _taskWorkUnitIndex;

    // An optional writer to output progress
    private Writer _writer;

    // Constants for properties
    public static final String TaskTitle_Prop = "TaskTitle";
    public static final String TaskWorkUnitCount_Prop = "TaskWorkUnitCount";
    public static final String TaskWorkUnitIndex_Prop = "TaskWorkUnitIndex";

    /**
     * Constructor.
     */
    public ActivityMonitor()
    {
        super();
    }

    /**
     * Constructor for given title.
     */
    public ActivityMonitor(String aTitle)
    {
        super();
        setTitle(aTitle);
    }

    /**
     * Constructor for given writer.
     */
    public ActivityMonitor(PrintStream aPrintStream)
    {
        super();
        _writer = new PrintWriter(aPrintStream);
    }

    /**
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    public void startForTaskCount(int taskCount)
    {
        setTaskCount(taskCount);
        if (_writer != null)
            println("StartTasks: " + taskCount);
    }

    /**
     * Begin processing a single task.
     */
    public void beginTask(String taskTitle, int workUnitCount)
    {
        setIndeterminate(false);
        setTaskTitle(taskTitle);
        setTaskWorkUnitCount(workUnitCount);
        setTaskWorkUnitIndex(0);
        if (_writer != null) {
            String msg = String.format("Begin task %d of %d: %s (%d parts)", _taskIndex + 1, _taskCount, taskTitle, workUnitCount);
            println(msg);
        }
    }

    /**
     * Denote that some work units have been completed.
     */
    public void updateTask(int workUnitsDone)
    {
        setTaskWorkUnitIndex(_taskWorkUnitIndex + workUnitsDone);
        if (_writer != null)
            println("UpdateTask " + (_taskIndex + 1) + ": " + workUnitsDone);
    }

    /**
     * Returns the current task title.
     */
    public String getTaskTitle()  { return _taskTitle; }

    /**
     * Sets the current task title.
     */
    protected void setTaskTitle(String aString)
    {
        if (Objects.equals(aString, _taskTitle)) return;
        firePropChange(TaskTitle_Prop, _taskTitle, _taskTitle = aString);
    }

    /**
     * Returns the total number of task work units.
     */
    public int getTaskWorkUnitCount()  { return _taskWorkUnitCount; }

    /**
     * Sets the total number of task work units.
     */
    protected void setTaskWorkUnitCount(int aValue)
    {
        if (aValue == _taskWorkUnitCount) return;
        firePropChange(TaskWorkUnitCount_Prop, _taskWorkUnitCount, _taskWorkUnitCount = aValue);
    }

    /**
     * Returns the number of task work units completed.
     */
    public int getTaskWorkUnitIndex()  { return _taskWorkUnitIndex; }

    /**
     * Sets the number of task work units completed.
     */
    protected void setTaskWorkUnitIndex(int aValue)
    {
        if (aValue == _taskWorkUnitIndex) return;
        firePropChange(TaskWorkUnitIndex_Prop, _taskWorkUnitIndex, _taskWorkUnitIndex = aValue);
    }

    /**
     * Finish the current task, so the next can begin.
     */
    public void endTask()
    {
        _taskWorkUnitCount = 0;
        _taskWorkUnitIndex = 0;
        setTaskIndex(_taskIndex + 1);
        if (_taskIndex >= _taskCount)
            setFinished(true);
        if (_writer != null)
            println("EndTask " + _taskIndex);
    }

    /**
     * Returns the task progress.
     */
    public double getTaskProgress()
    {
        if (getTaskCount() <= 0)
            return -1;

        // Get number of tasks done (including fraction of current task done)
        double tasksDone = getTaskIndex();
        if (_taskWorkUnitIndex > 0 && _taskWorkUnitCount > 0)
            tasksDone += _taskWorkUnitIndex / (double) _taskWorkUnitCount;

        // Return fraction of tasks done
        double progress = tasksDone / getTaskCount();
        return Math.max(0, Math.min(progress, 1));
    }

    /**
     * Creates a monitor panel to show progress.
     */
    public void showProgressPanel(View aView)
    {
        new ActivityMonitorPanel(aView, this);
    }

    /**
     * Print string to output.
     */
    private void println(String aStr)
    {
        try { _writer.write(aStr); _writer.write('\n'); _writer.flush(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}