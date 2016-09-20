/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;

/**
 * Interface for tracking the progress of arbitrary tasks.
 */
public interface TaskMonitor {

    /**
     * Advise the monitor of the total number of subtasks (invoke only once).
     */
    void startTasks(int aTaskCount);
    
    /**
     * Begin processing a single task.
     */
    void beginTask(String aTitle, int theTotalWork);
    
    /**
     * Denote that some work units have been completed.
     */
    void updateTask(int theWorkDone);
    
    /**
     * Finish the current task, so the next can begin.
     */
    void endTask();
    
    /**
     * Check for user task cancellation.
     * @return true if the user asked the process to stop working.
     */
    boolean isCancelled();

/**
 * A simple progress reporter printing on a stream.
 */
public static class Text implements TaskMonitor {
    int _tasksDone, _tasksTotal;
    int _taskDone, _taskTotal;
    Writer _out;

    /** Initialize a new progress monitor. */
    public Text(PrintStream aPS)  { this(new PrintWriter(aPS)); }

    /** Initialize a new progress monitor. */
    public Text(Writer out)  { _out = out; }

    /** Called to start monitor with number of tasks. */
    public void startTasks(int aTaskCount)  { _tasksTotal = aTaskCount; }
    
    /** Called to start task with total work. */
    public void beginTask(String aTitle, int theTotalWork)
    {
        _taskTotal = theTotalWork; _taskDone = 0;
        print(aTitle + "    ...    ");
    }
    
    /** Called to update task work count. */
    public void updateTask(int theWorkDone)  { _taskTotal += theWorkDone; }

    /** Called to end task work count. */
    public void endTask()  { print("Completed\n"); _taskDone = _taskTotal; _tasksDone++; }
    
    /** Check for user task cancellation. */
    public boolean isCancelled()  { return false; }

    /** Print string to output. */
    private void print(String aStr)
    {
        try { _out.write(aStr); _out.flush(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}

/**
 * An TaskMonitor that ignores everything.
 */
public static TaskMonitor NULL = new TaskMonitor() {
    public void startTasks(int aTaskCount)  { }
    public void beginTask(String aTitle, int theTotalWork)  { }
    public void updateTask(int theWorkDone)  { }
    public void endTask()  { }
    public boolean isCancelled()  { return false; }
};

}