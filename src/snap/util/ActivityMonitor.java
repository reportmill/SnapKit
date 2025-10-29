/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.view.View;
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

    // Constants for properties
    public static final String TaskTitle_Prop = "TaskTitle";
    public static final String Progress_Prop = "Progress";

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
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    public void startForTaskCount(int taskCount)
    {
        setTaskCount(taskCount);
    }

    /**
     * Begin processing a single task.
     */
    public void beginTask(String taskTitle, int workUnitCount)
    {
        setIndeterminate(false);
        setTaskTitle(taskTitle);
        _taskWorkUnitCount = workUnitCount;
        _taskWorkUnitIndex = 0;
    }

    /**
     * Denote that some work units have been completed.
     */
    public void updateTask(int workUnitsDone)
    {
        _taskWorkUnitIndex = _taskWorkUnitIndex + workUnitsDone;
        firePropChange(Progress_Prop, _taskWorkUnitIndex, _taskWorkUnitIndex);
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
     * Returns an ActivityMonitor that prints to system out.
     */
    public static ActivityMonitor getSystemOutActivityMonitor()
    {
        return new SystemOutActivityMonitor();
    }

    /**
     * A System.out activity monitor.
     */
    private static class SystemOutActivityMonitor extends ActivityMonitor {

        @Override
        public void startForTaskCount(int taskCount)
        {
            super.startForTaskCount(taskCount);
            System.out.println("StartTasks: " + taskCount);
        }

        @Override
        public void beginTask(String taskTitle, int workUnitCount)
        {
            super.beginTask(taskTitle, workUnitCount);
            System.out.printf("Begin task %d of %d: %s (%d parts)", _taskIndex + 1, _taskCount, taskTitle, workUnitCount);
        }

        @Override
        public void updateTask(int workUnitsDone)
        {
            super.updateTask(workUnitsDone);
            System.out.println("UpdateTask " + (_taskIndex + 1) + ": " + workUnitsDone);
        }

        @Override
        public void endTask()
        {
            super.endTask();
            System.out.println("EndTask " + _taskIndex);
        }
    }
}