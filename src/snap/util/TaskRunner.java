/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropObject;
import snap.view.ViewUtils;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * This class runs a task on a separate thread.
 */
public class TaskRunner<T> extends PropObject {

    // The name of this runner
    private String _name = getClass().getSimpleName();

    // The function for this task runner
    private Callable<T> _taskFunction;

    // The runner status
    private Status _status = Status.Idle;

    // The ActivityMonitor
    private ActivityMonitor _monitor;

    // The runner thread
    private Thread _thread;

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

    // Constants for status
    public enum Status { Idle, Running, Finished, Cancelled, Failed }

    // Constants for properties
    public static final String Status_Prop = "Status";

    /**
     * Constructor.
     */
    public TaskRunner()
    {
        super();
    }

    /**
     * Constructor for given task function.
     */
    public TaskRunner(Callable<T> aSupplier)
    {
        super();
        _taskFunction = aSupplier;
    }

    /**
     * Constructor for given name.
     */
    public TaskRunner(String aName)
    {
        super();
        setName(aName);
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
     * Returns the task supplier function.
     */
    public Callable<T> getTaskFunction()  { return _taskFunction; }

    /**
     * Sets the task callable function.
     */
    public void setTaskFunction(Callable<T> aSupplier)  { _taskFunction = aSupplier; }

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
     * Returns the monitor.
     */
    public synchronized ActivityMonitor getMonitor()
    {
        if (_monitor != null) return _monitor;
        return _monitor = new ActivityMonitor(getName());
    }

    /**
     * Sets the monitor.
     */
    public void setMonitor(ActivityMonitor aMonitor)  { _monitor = aMonitor; }

    /**
     * Returns the thread.
     */
    public Thread getThread()  { return _thread; }

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
    public boolean isCancelled()  { return _status == Status.Cancelled; }

    /**
     * Starts the runner.
     */
    public void start()
    {
        // Create new thread to run task
        _thread = new Thread(this::runTask);

        // Start thread
        if (!SnapEnv.isTeaVM)
            _thread.setName(getName());
        _thread.start();
    }

    /**
     * Cancels the runner.
     */
    public void cancel()
    {
        setStatus(Status.Cancelled);
    }

    /**
     * Terminates this runner (clients should try cancel first).
     */
    public void terminate()
    {
        cancel();
        getThread().interrupt();
    }

    /**
     * Returns the result.
     */
    public T getResult()  { return _result; }

    /**
     * Waits for this task runner to finish and return the result.
     */
    public T awaitResult()
    {
        assert (getStatus() != Status.Idle);
        join();
        return getResult();
    }

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
        catch (Exception e) { throw new RuntimeException(e); }
        return this;
    }

    /**
     * Returns the exception.
     */
    public Throwable getException()  { return _exception; }

    /**
     * Returns the success handler.
     */
    public Consumer<T> getOnSuccess()  { return _successHandler; }

    /**
     * Sets the success handler.
     */
    public synchronized void setOnSuccess(Consumer<T> aHandler)
    {
        if (aHandler == getOnSuccess()) return;
        _successHandler = aHandler;

        // If already finished, call handler
        if (_successHandler != null && _status == Status.Finished)
            ViewUtils.runLater(() -> _successHandler.accept(_result));
    }

    /**
     * Returns the failure handler.
     */
    public Consumer<Exception> getOnFailure()  { return _failureHandler; }

    /**
     * Sets the failure handler.
     */
    public synchronized void setOnFailure(Consumer<Exception> aHandler)
    {
        if (aHandler == getOnFailure()) return;
        _failureHandler = aHandler;

        // If already finished, call handler
        if (_failureHandler != null && _status == Status.Failed)
            ViewUtils.runLater(() -> _failureHandler.accept(_exception));
    }

    /**
     * Sets the cancelled handler.
     */
    public synchronized void setOnCancelled(Runnable aRun)
    {
        _cancelledHandler = aRun;

        // If already finished, call handler
        if (_cancelledHandler != null && _status == Status.Cancelled)
            ViewUtils.runLater(_cancelledHandler);
    }

    /**
     * Sets the finished handler.
     */
    public synchronized void setOnFinished(Runnable aRun)
    {
        _finishedHandler = aRun;

        // If already finished, call handler
        if (_finishedHandler != null && (_status == Status.Finished || _status == Status.Failed || _status == Status.Cancelled))
            ViewUtils.runLater(_finishedHandler);
    }

    /**
     * Runs the run method.
     */
    protected void runTask()
    {
        // Set run status
        setStatus(Status.Running);

        // Run task
        try { _result = _taskFunction.call(); }
        catch (Exception e) { _exception = e; }
        catch (Throwable e) { _exception = new RuntimeException(e); }

        // Update status
        if (getStatus() != Status.Cancelled)
            setStatus(_exception == null ? Status.Finished : Status.Failed);

        // Handle task finished
        ViewUtils.runLater(this::handleTaskFinished);
    }

    /**
     * Called when task has completed.
     */
    protected synchronized void handleTaskFinished()
    {
        // If cancelled, call cancelled
        if (getStatus() == Status.Cancelled) {
            if (_monitor != null)
                _monitor.setCancelled(true);
            if (_cancelledHandler != null)
                _cancelledHandler.run();
        }

        // Call success/failure
        else if (_exception == null) {
            if (_successHandler != null)
                _successHandler.accept(_result);
        }

        // Call failure
        else {
            if (_monitor != null)
                _monitor.setCancelled(true);
            if (_failureHandler != null)
                _failureHandler.accept(_exception);
            else _exception.printStackTrace();
        }

        // Call finished
        if (_monitor != null)
            _monitor.setFinished(true);
        if (_finishedHandler != null)
            _finishedHandler.run();
    }
}