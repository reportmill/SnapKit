/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropChangeSupport;
import snap.view.ViewUtils;
import java.util.function.Consumer;

/**
 * A class for running operations in the background.
 */
public abstract class TaskRunner<T> implements TaskMonitor {

    // The name of this runner
    private String _name = getClass().getSimpleName();

    // The TaskMonitor
    private TaskMonitor _monitor = TaskMonitor.NULL;

    // The runner status
    private Status _status = Status.Idle;

    // The runner thread
    private Thread _thread;

    // The runner start time (milliseconds)
    private long _startTime;

    // The runner end time (milliseconds)
    private long _endTime;

    // The result of the run method
    private T _result;

    // The exception thrown if run failed
    private Exception _exception;

    // The success handler
    private Consumer<T> _successHandler;

    // The failed handler
    private Consumer<Exception> _failureHandler;

    // The cancelled handler
    private Runnable _cancelledHandler;

    // The finished handler
    private Runnable _finishedHandler;

    // The PropChangeSupport
    private PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for status
    public enum Status { Idle, Running, Finished, Cancelled, Failed }

    // Constants for Runner PropertyChanges
    public static final String Progress_Prop = "Progress";
    public static final String ActivityText_Prop = "ActivityText";
    public static final String Status_Prop = "Status";

    /**
     * Constructor.
     */
    public TaskRunner()
    {
        super();
    }

    /**
     * Constructor for given monitor.
     */
    public TaskRunner(TaskMonitor aMonitor)
    {
        super();
        setMonitor(aMonitor);
    }

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
    public void startTasks(int aTaskCount)
    {
        _monitor.startTasks(aTaskCount);
    }

    /**
     * Begin processing a single task.
     */
    public void beginTask(String aTitle, int theTotalWork)
    {
        _monitor.beginTask(aTitle, theTotalWork);
        if (_monitor.isCancelled())
            cancel();
    }

    /**
     * Denote that some work units have been completed.
     */
    public void updateTask(int theWorkDone)
    {
        _monitor.updateTask(theWorkDone);
        if (_monitor.isCancelled())
            cancel();
    }

    /**
     * Finish the current task, so the next can begin.
     */
    public void endTask()
    {
        _monitor.endTask();
        if (_monitor.isCancelled())
            cancel();
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
        if (aStatus == _status) return;
        firePropChange(Status_Prop, _status, _status = aStatus);
    }

    /**
     * Returns the thread.
     */
    public Thread getThread()  { return _thread; }

    /**
     * Joins the runner.
     */
    public TaskRunner<T> join()  { return join(0); }

    /**
     * Joins the runner.
     */
    public TaskRunner<T> join(int aTimeout)
    {
        try { _thread.join(aTimeout); }
        catch (Exception e) { }
        return this;
    }

    /**
     * Returns whether thread is still active.
     */
    public boolean isActive()
    {
        return _thread != null && _thread.isAlive();
    }

    /**
     * Whether runner has been cancelled.
     */
    public boolean isCancelled()
    {
        return _monitor.isCancelled() || _status == Status.Cancelled;
    }

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
    public long getElapsedTime()
    {
        long endTime = isActive() ? getSystemTime() : getEndTime();
        long startTime = getStartTime();
        return endTime - startTime;
    }

    /**
     * Returns the system time.
     */
    protected long getSystemTime()  { return System.currentTimeMillis(); }

    /**
     * Starts the runner.
     */
    public TaskRunner<T> start()
    {
        // Create new thread to run this runner's run method then success/failure/finished method with result/exception
        _thread = new Thread(() -> {
            invokeRun();
            ViewUtils.runLater(() -> invokeFinished());
        });

        // Start thread
        if (!SnapUtils.isTeaVM)
            _thread.setName(getName());
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
    public void failure(Exception e)  { }

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
    public Throwable getException()  { return _exception; }

    /**
     * Sets the success handler.
     */
    public void setOnSuccess(Consumer<T> aHandler)  { _successHandler = aHandler; }

    /**
     * Sets the failure handler.
     */
    public void setOnFailure(Consumer<Exception> aHandler)  { _failureHandler = aHandler; }

    /**
     * Sets the cancelled handler.
     */
    public void setOnCancelled(Runnable aRun)  { _cancelledHandler = aRun; }

    /**
     * Sets the finished handler.
     */
    public void setOnFinished(Runnable aRun)  { _finishedHandler = aRun; }

    /**
     * Runs the run method.
     */
    protected void invokeRun()
    {
        // Set start time and run status
        _exception = null;
        _startTime = getSystemTime();
        setStatus(Status.Running);

        // Call run()
        try { _result = run(); }
        catch (Exception e) { _exception = e; }
        catch (Throwable e) { _exception = new RuntimeException(e); }

        // Set end time
        _endTime = getSystemTime();
    }

    /**
     * Runs the success method.
     */
    protected void invokeFinished()
    {
        // Update status
        if (getStatus() != Status.Cancelled)
            setStatus(_exception == null ? Status.Finished : Status.Failed);

        // If cancelled, call cancelled
        if (getStatus() == Status.Cancelled) {
            if (_cancelledHandler != null)
                _cancelledHandler.run();
            cancelled(_exception);
        }

        // Call success/failure
        else if (_exception == null) {
            if (_successHandler != null)
                _successHandler.accept(_result);
            success(_result);
        }

        // Call failure
        else {
            if (_failureHandler != null)
                _failureHandler.accept(_exception);
            failure(_exception);
        }

        // Call finished
        if (_finishedHandler != null)
            _finishedHandler.run();
        finished();
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs == PropChangeSupport.EMPTY)
            _pcs = new PropChangeSupport(this);
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
        if (!_pcs.hasListener(aProp)) return;
        PropChange pc = new PropChange(this, aProp, oldVal, newVal);
        _pcs.firePropChange(pc);
    }
}