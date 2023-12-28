/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropObject;
import java.io.*;
import java.util.Objects;

/**
 * Interface for tracking the progress of arbitrary tasks.
 */
public class TaskMonitor extends PropObject {

    // The total number of tasks
    private int _tasksTotal;

    // The number of tasks completed
    private int _tasksDone;

    // The total number of task work units
    private int _taskTotal;

    // The number of task work units completed
    private int _taskDone;

    // The current task title
    private String _taskTitle = "First Task";

    // Whether monitor has been cancelled
    private boolean _cancelled;

    // Whether monitor has finished
    private boolean _finished;

    // An optional writer to output progress
    private Writer _writer;

    // Constants for properties
    public static final String TasksTotal_Prop = "TasksTotal";
    public static final String TasksDone_Prop = "TasksDone";
    public static final String TaskTotal_Prop = "TaskTotal";
    public static final String TaskDone_Prop = "TaskDone";
    public static final String TaskTitle_Prop = "TaskTitle";
    public static final String Cancelled_Prop = "Cancelled";
    public static final String Finished_Prop = "Finished";

    /**
     * Constructor.
     */
    public TaskMonitor()
    {
        super();
    }

    /**
     * Constructor for given writer.
     */
    public TaskMonitor(PrintStream aPrintStream)
    {
        super();
        _writer = new PrintWriter(aPrintStream);
    }

    /**
     * Returns the total number of tasks.
     */
    public int getTasksTotal()  { return _tasksTotal; }

    /**
     * Sets the total number of tasks.
     */
    protected void setTasksTotal(int aValue)
    {
        if (aValue == _tasksTotal) return;
        firePropChange(TasksTotal_Prop, _tasksTotal, _tasksTotal = aValue);
    }

    /**
     * Returns the number of tasks done.
     */
    public int getTasksDone()  { return _tasksDone; }

    /**
     * Sets the number of tasks done.
     */
    protected void setTasksDone(int aValue)
    {
        if (aValue == _tasksDone) return;
        firePropChange(TasksDone_Prop, _tasksDone, _tasksDone = aValue);
    }

    /**
     * Returns the total number of task work units.
     */
    public int getTaskTotal()  { return _taskTotal; }

    /**
     * Sets the total number of task work units.
     */
    protected void setTaskTotal(int aValue)
    {
        if (aValue == _taskTotal) return;
        firePropChange(TaskTotal_Prop, _taskTotal, _taskTotal = aValue);
    }

    /**
     * Returns the number of task work units completed.
     */
    public int getTaskDone()  { return _taskDone; }

    /**
     * Sets the number of task work units completed.
     */
    protected void setTaskDone(int aValue)
    {
        if (aValue == _taskDone) return;
        firePropChange(TaskDone_Prop, _taskDone, _taskDone = aValue);
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
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    public void startTasks(int aTaskCount)
    {
        setTasksTotal(aTaskCount);
        if (_writer != null)
            println("StartTasks: " + aTaskCount);
    }
    
    /**
     * Begin processing a single task.
     */
    public void beginTask(String aTitle, int theTotalWork)
    {
        setTaskTitle(aTitle);
        setTaskTotal(theTotalWork);
        setTaskDone(0);
        if (_writer != null) {
            String msg = String.format("Begin task %d of %d: %s (%d parts)", _tasksDone + 1, _tasksTotal, aTitle, theTotalWork);
            println(msg);
        }
    }
    
    /**
     * Denote that some work units have been completed.
     */
    public void updateTask(int theWorkDone)
    {
        setTaskDone(_taskDone + theWorkDone);
        if (_writer != null)
            println("UpdateTask " + (_tasksDone + 1) + ": " + theWorkDone);
    }
    
    /**
     * Finish the current task, so the next can begin.
     */
    public void endTask()
    {
        setTasksDone(_tasksDone + 1);
        setTaskDone(_taskTotal);
        if (_tasksDone >= _taskTotal)
            setFinished(true);
        if (_writer != null)
            println("EndTask " + _tasksDone);
    }
    
    /**
     * Returns whether the user asked the process to stop working.
     */
    public boolean isCancelled()  { return _cancelled; }

    /**
     * Sets whether the user asked the process to stop working.
     */
    public void setCancelled(boolean aValue)
    {
        if (aValue == _cancelled) return;
        firePropChange(Cancelled_Prop, _cancelled, _cancelled = aValue);
    }

    /**
     * Returns whether monitor has finished.
     */
    public boolean isFinished()  { return _finished; }

    /**
     * Sets whether monitor has finished.
     */
    public void setFinished(boolean aValue)
    {
        if (aValue == _finished) return;
        firePropChange(Finished_Prop, _finished, _finished = aValue);
    }

    /**
     * Returns the task progress.
     */
    public double getTaskProgress()  { return _taskDone / (double) _taskTotal; }

    /**
     * Print string to output.
     */
    private void println(String aStr)
    {
        try { _writer.write(aStr); _writer.write('\n'); _writer.flush(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}