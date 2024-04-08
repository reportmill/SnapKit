/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A Timer to fire on node event thread.
 */
public class ViewTimer {

    // The run to be called on each frame
    private Runnable _run;

    // The delay
    private int  _period = 40;
    
    // The timer elapsed time
    private int  _time = -1;
    
    // The timer start time
    private long  _startTime;
    
    // The last time the timer was paused
    private long  _pauseTime;
    
    // The number of times the timer has fired
    private int  _count;
    
    // The run that fires over intervals
    private Runnable _timerRun;
    
    // The environment
    private ViewEnv  _env = ViewEnv.getEnv();
    
    /**
     * Constructor for runnable and period.
     */
    public ViewTimer(Runnable onFire, int aPeriod)
    {
        _period = aPeriod;
        _run = onFire;
    }

    /**
     * Returns the run to be called.
     */
    public Runnable getRun()  { return _run; }

    /**
     * Returns the time in milliseconds between firings.
     */
    public int getPeriod()  { return _period; }

    /**
     * Sets the time in milliseconds between firings.
     */
    public void setPeriod(int aPeriod)
    {
        if (aPeriod == _period) return;
        _period = aPeriod;
        if (isRunning()) {
            stop();
            start();
        }
    }

    /**
     * Returns whether timer is running.
     */
    public boolean isRunning()  { return _timerRun != null; }

    /**
     * Returns whether timer is running.
     */
    public boolean isPaused()  { return _pauseTime > 0; }

    /**
     * Returns the elapsed time.
     */
    public int getTime()
    {
        if (_time > 0)
            return _time;
        return (int) (System.currentTimeMillis() - _startTime);
    }

    /**
     * Returns the number of times the timer has fired.
     */
    public int getCount()  { return _count; }

    /**
     * Start timer.
     */
    public synchronized void start()
    {
        // If task already present, return
        if (_timerRun != null) return;

        // Create task and schedule
        _timerRun = this::sendEvent;

        // Initialize times and Schedule task
        _startTime = System.currentTimeMillis() - (_pauseTime>0 ? (_pauseTime - _startTime) : 0);
        _pauseTime = 0;
        _env.runIntervals(_timerRun, getPeriod());
    }

    /**
     * Start timer.
     */
    public void start(int aDelay)
    {
        if (aDelay > 0)
            _env.runDelayed(this::start, aDelay);
        else start();
    }

    /**
     * Stop timer.
     */
    public synchronized void stop()
    {
        if (_timerRun != null)
            _env.stopIntervals(_timerRun);
        _timerRun = null;
        _pauseTime = 0;
    }

    /**
     * Pauses the timer (next play will start from previous time).
     */
    public void pause()
    {
        stop();
        _pauseTime = System.currentTimeMillis();
    }

    /**
     * Sends the event.
     */
    protected void sendEvent()
    {
        // If no run, just return. Can happen because final interval fires?
        if (_timerRun == null) return; //System.out.println("ViewTimer.sendEvent: Run is null - shouldn't happen");

        // Set time and fire
        _time = (int) (System.currentTimeMillis() - _startTime);
        _run.run();
        _count++;
        _time = -1;
    }
}