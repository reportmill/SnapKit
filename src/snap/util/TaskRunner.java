/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A class for running operations in the background.
 */
public abstract class TaskRunner <T> implements TaskMonitor {

    // The name of this runner
    String             _name = "TaskRunner Thread";
    
    // The TaskMonitor
    TaskMonitor        _monitor = TaskMonitor.NULL;
    
    // The runner status
    Status             _status = Status.Idle;

    // The runner thread
    Thread             _thread;
    
    // The runner start time (milliseconds)
    long               _startTime;
    
    // The runner end time (milliseconds)
    long               _endTime;
    
    // The result of the run method
    T                  _result;
    
    // The exception thrown if run failed
    Exception          _exception;
    
    // The PropChangeSupport
    PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // Constants for status
    public enum Status { Idle, Running, Finished, Cancelled, Failed }
    
    // Constants for Runner PropertyChanges
    public static final String Progress_Prop = "Progress";
    public static final String ActivityText_Prop = "ActivityText";
    public static final String Status_Prop = "Status";

/**
 * Creates a new TaskRunner.
 */
public TaskRunner()  { }

/**
 * Creates a new TaskRunner for given monitor.
 */
public TaskRunner(TaskMonitor aMonitor)  { setMonitor(aMonitor); }

/**
 * Returns the name of runner (and thread).
 */
public String getName()  { return _name; }

/**
 * Sets the name of runner (and thread).
 */
public void setName(String aName)  { _name = aName; }

/**
 * Returns the monitor.
 */
public TaskMonitor getMonitor()  { return _monitor; }

/**
 * Sets the monitor.
 */
public void setMonitor(TaskMonitor aMonitor)  { _monitor = aMonitor; }

/**
 * Advise the monitor of the total number of subtasks (invoke only once).
 */
public void startTasks(int aTaskCount)  { _monitor.startTasks(aTaskCount); }

/**
 * Begin processing a single task.
 */
public void beginTask(String aTitle, int theTotalWork)
{
    _monitor.beginTask(aTitle, theTotalWork);
    if(_monitor.isCancelled()) cancel();
}

/**
 * Denote that some work units have been completed.
 */
public void updateTask(int theWorkDone)
{
    _monitor.updateTask(theWorkDone);
    if(_monitor.isCancelled()) cancel();
}

/**
 * Finish the current task, so the next can begin.
 */
public void endTask()
{
    _monitor.endTask();
    if(_monitor.isCancelled()) cancel();
}

/**
 * Returns the status.
 */
public Status getStatus()  { return _status; }

/**
 * Sets the status.
 */
protected void setStatus(Status aStatus)
{
    if(aStatus==getStatus()) return;
    firePropChange(Status_Prop, _status, _status = aStatus);
}

/**
 * Returns the thread.
 */
public Thread getThread()  { return _thread; }

/**
 * Joins the runner.
 */
public TaskRunner <T> join()  { try { _thread.join(); } catch(Exception e) { } return this; }

/**
 * Joins the runner.
 */
public TaskRunner <T> join(int aTimeout)  { try { _thread.join(aTimeout); } catch(Exception e) { } return this; }

/**
 * Returns whether thread is still active.
 */
public boolean isActive()  { return _thread!=null && _thread.isAlive(); }

/**
 * Whether runner has been cancelled.
 */
public boolean isCancelled()  { return _monitor.isCancelled() || _status==Status.Cancelled; }

/**
 * Returns the start time.
 */
public long getStartTime()  { return _startTime; }

/**
 * Returns the end time.
 */
public long getEndTime()  { return _endTime; }

/**
 * Returns the elapsed time.
 */
public long getElapsedTime()  { return (isActive()? getSystemTime() : getEndTime()) - getStartTime(); }

/**
 * Returns the system time.
 */
protected long getSystemTime()  { return System.currentTimeMillis(); }

/**
 * Starts the runner.
 */
public TaskRunner <T> start()
{
    // Create new thread to run this runner's run method then success/failure/finished method with result/exception
    _thread = new Thread(() -> { invokeRun(); invokeFinished(); });
    
    // Start thread
    //_thread.setName(getName()); TeaVM doesn't like this
    _thread.start();
    
    // Return this runner
    return this;
}

/**
 * Cancels the runner.
 */
public void cancel()
{
    // Set Status to Cancelled and interrupt
    setStatus(Status.Cancelled);
    getThread().interrupt();
}

/**
 * The method to run.
 */
public abstract T run() throws Exception;

/**
 * The method run on success.
 */
public void success(T aResult)  { }

/**
 * The method to run when cancelled.
 */
public void cancelled(Exception e)  { }

/**
 * The method to run on failure.
 */
public void failure(Exception e)  { e.printStackTrace(); }

/**
 * The method to run when finished (after success()/failure() call).
 */
public void finished()  { }

/**
 * Returns the result.
 */
public T getResult()  { return _result; }

/**
 * Returns the exception.
 */
public Throwable getExeption()  { return _exception; }

/**
 * Runs the run method.
 */
protected void invokeRun()
{
    // Set start time and run status
    _exception = null;
    _startTime = getSystemTime();
    setStatus(Status.Running);

    // Run run
    try { _result = run(); }
    catch(Exception e) { _exception = e; }
    catch(Throwable e) { _exception = new RuntimeException(e); }
    
    // Set end time
    _endTime = getSystemTime();
}

/**
 * Runs the success method.
 */
protected void invokeFinished()
{
    // Update status
    setStatus(_exception==null? Status.Finished : Status.Failed);
    
    // Call success/failure
    if(_exception==null)
        success(_result);
    else if(getStatus()==Status.Cancelled)
        cancelled(_exception);
    else failure(_exception);
    finished();
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aLsnr)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aLsnr);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aLsnr)
{
    _pcs.removePropChangeListener(aLsnr);
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    PropChange pc = new PropChange(this, aProp, oldVal, newVal);
    _pcs.firePropChange(pc);
}

}