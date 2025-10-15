/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.*;
import snap.view.View;
import java.io.*;
import java.util.Objects;

/**
 * Interface for tracking the progress of arbitrary tasks.
 */
public class TaskMonitor extends PropObject {

    // A string to describe task
    private String _title;

    // The total number of tasks
    private int _taskCount;

    // The current of task index
    private int _taskIndex;

    // The total number of task work units
    private int _taskWorkUnitCount;

    // The current task work unit index
    private int _taskWorkUnitIndex;

    // The current task title
    private String _taskTitle = "First Task";

    // Whether task count and run time is indeterminate
    private boolean _indeterminate;

    // Whether monitor has been cancelled
    private boolean _cancelled;

    // Whether monitor has finished
    private boolean _finished;

    // An optional writer to output progress
    private Writer _writer;

    // Another monitor if this monitor is linked
    private TaskMonitor _monitor;

    // A prop change listener
    private PropChangeListener _monitorLsnr;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String TaskCount_Prop = "TaskCount";
    public static final String TaskIndex_Prop = "TaskIndex";
    public static final String TaskTitle_Prop = "TaskTitle";
    public static final String TaskWorkUnitCount_Prop = "TaskWorkUnitCount";
    public static final String TaskWorkUnitIndex_Prop = "TaskWorkUnitIndex";
    public static final String Indeterminate_Prop = "Indeterminate";
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
     * Constructor for given title.
     */
    public TaskMonitor(String aTitle)
    {
        super();
        setTitle(aTitle);
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
     * Returns a string to describe task.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets a string to describe task.
     */
    public void setTitle(String aValue)
    {
        if (Objects.equals(aValue, _taskTitle)) return;
        firePropChange(Title_Prop, _title, _title = aValue);
    }

    /**
     * Returns the total number of tasks.
     */
    public int getTaskCount()  { return _taskCount; }

    /**
     * Sets the total number of tasks.
     */
    protected void setTaskCount(int aValue)
    {
        if (aValue == _taskCount) return;
        firePropChange(TaskCount_Prop, _taskCount, _taskCount = aValue);
    }

    /**
     * Returns the current task index.
     */
    public int getTaskIndex()  { return _taskIndex; }

    /**
     * Sets the current task index.
     */
    protected void setTaskIndex(int aValue)
    {
        if (aValue == _taskIndex) return;
        _taskWorkUnitCount = 0;
        firePropChange(TaskIndex_Prop, _taskIndex, _taskIndex = aValue);
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
     * Finish the current task, so the next can begin.
     */
    public void endTask()
    {
        _taskWorkUnitIndex = 0;
        setTaskIndex(_taskIndex + 1);
        if (_taskIndex >= _taskCount)
            setFinished(true);
        if (_writer != null)
            println("EndTask " + _taskIndex);
    }

    /**
     * Returns whether task timing is indeterminate.
     */
    public boolean isIndeterminate()  { return _indeterminate; }

    /**
     * Sets whether task timing is indeterminate.
     */
    public void setIndeterminate(boolean aValue)
    {
        if (aValue == isIndeterminate()) return;
        firePropChange(Indeterminate_Prop, _indeterminate, _indeterminate = aValue);
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
     * Returns the next monitor in chain (if this monitor is linked to another).
     */
    public TaskMonitor getMonitor()  { return _monitor; }

    /**
     * Sets the next monitor in chain (if this monitor is linked to another).
     */
    public void setMonitor(TaskMonitor sourceMonitor)
    {
        // If already set, just return
        if (sourceMonitor == _monitor) return;

        // Stop listening to old monitor
        if (_monitor != null)
            _monitor.removePropChangeListener(_monitorLsnr);

        // Set
        _monitor = sourceMonitor;

        // Start listening
        if (_monitor != null) {
            _monitor.addPropChangeListener(_monitorLsnr = this::handleMonitorPropChange);

            // Copy all props from other
            Prop[] allProps = getPropSet().getProps();
            for (Prop prop : allProps) {
                Object value = _monitor.getPropValue(prop.getName());
                setPropValue(prop.getName(), value);
            }
        }
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
    public TaskMonitorPanel showProgressPanel(View aView)
    {
        TaskMonitorPanel taskMonitorPanel = new TaskMonitorPanel(aView, null);
        taskMonitorPanel.setMonitor(this);
        return taskMonitorPanel;
    }

    /**
     * Called when source monitor changes.
     */
    protected void handleMonitorPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        Object value = _monitor.getPropValue(propName);
        setPropValue(propName, value);
    }

    /**
     * Print string to output.
     */
    private void println(String aStr)
    {
        try { _writer.write(aStr); _writer.write('\n'); _writer.flush(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // Title, TasksTotal, TasksDone, TaskTotal, TaskDone, TaskTitle, Cancelled, Finished
        aPropSet.addPropNamed(Title_Prop, String.class, null);
        aPropSet.addPropNamed(TaskCount_Prop, int.class, 0);
        aPropSet.addPropNamed(TaskIndex_Prop, int.class, 0);
        aPropSet.addPropNamed(TaskWorkUnitCount_Prop, int.class, 0);
        aPropSet.addPropNamed(TaskWorkUnitIndex_Prop, int.class, 0);
        aPropSet.addPropNamed(TaskTitle_Prop, String.class, null);
        aPropSet.addPropNamed(Cancelled_Prop, boolean.class, false);
        aPropSet.addPropNamed(Finished_Prop, boolean.class, false);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Title, TasksTotal, TasksDone, TaskTotal, TaskDone, TaskTitle, Cancelled, Finished
            case Title_Prop: return getTitle();
            case TaskCount_Prop: return getTaskCount();
            case TaskIndex_Prop: return getTaskIndex();
            case TaskWorkUnitCount_Prop: return getTaskWorkUnitCount();
            case TaskWorkUnitIndex_Prop: return getTaskWorkUnitIndex();
            case TaskTitle_Prop: return getTaskTitle();
            case Cancelled_Prop: return isCancelled();
            case Finished_Prop: return isFinished();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Title, TasksTotal, TasksDone, TaskTotal, TaskDone, TaskTitle, Cancelled, Finished
            case Title_Prop: setTitle(Convert.stringValue(aValue)); break;
            case TaskCount_Prop: setTaskCount(Convert.intValue(aValue)); break;
            case TaskIndex_Prop: setTaskIndex(Convert.intValue(aValue)); break;
            case TaskWorkUnitCount_Prop: setTaskWorkUnitCount(Convert.intValue(aValue)); break;
            case TaskWorkUnitIndex_Prop: setTaskWorkUnitIndex(Convert.intValue(aValue)); break;
            case TaskTitle_Prop: setTaskTitle(Convert.stringValue(aValue)); break;
            case Cancelled_Prop: setCancelled(Convert.boolValue(aValue)); break;
            case Finished_Prop: setFinished(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}