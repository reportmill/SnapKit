/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.function.Consumer;

/**
 * A Timer to fire on node event thread.
 */
public class ViewTimer {

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
    
    // To be called on each frame
    private Consumer<ViewTimer> _onFire;
    
    // The run that fires over intervals
    private Runnable  _run;
    
    // The environment
    private ViewEnv  _env = ViewEnv.getEnv();
    
    /**
     * Creates a new ViewTimer.
     */
    public ViewTimer() { }

    /**
     * Creates a new ViewTimer for period and action.
     */
    public ViewTimer(int aPeriod, Consumer<ViewTimer> onFire)  { _period = aPeriod; _onFire = onFire; }

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
     * Returns the on frame.
     */
    public Consumer<ViewTimer> getOnFire()  { return _onFire; }

    /**
     * Sets the on frame.
     */
    public void setOnFire(Consumer <ViewTimer> onFire)  { _onFire = onFire; }

    /**
     * Returns whether timer is running.
     */
    public boolean isRunning()  { return _run!=null; }

    /**
     * Returns whether timer is running.
     */
    public boolean isPaused()  { return _pauseTime>0; }

    /**
     * Returns the elapsed time.
     */
    public int getTime()
    {
        if (_time > 0) return _time;
        return (int) (System.currentTimeMillis() - _startTime);
    }

    /**
     * Returns the number of times the timer has fired.
     */
    public int getCount()  { return _count; }

    /**
     * Start timer.
     */
    public void start()
    {
        start(_period);
    }

    /**
     * Start timer.
     */
    public synchronized void start(int aDelay)
    {
        // If task already present, return
        if (_run != null) return;

        // Create task and schedule
        _run = () -> sendEvent();

        // Initialize times and Schedule task
        _startTime = System.currentTimeMillis() - (_pauseTime>0 ? (_pauseTime - _startTime) : 0);
        _pauseTime = 0;
        _env.runIntervals(_run, getPeriod(), aDelay, false, true);
    }

    /**
     * Stop timer.
     */
    public synchronized void stop()
    {
        if (_run != null)
            ViewEnv.getEnv().stopIntervals(_run);
        _run = null;
        _pauseTime = 0;
    }

    /**
     * Pauses the timer (next play will start from previous time).
     */
    public void pause()  { stop(); _pauseTime = System.currentTimeMillis(); }

    /**
     * Sends the event.
     */
    protected void sendEvent()
    {
        // If no run, just return. Can happen because final interval fires?
        if (_run == null) return; //System.out.println("ViewTimer.sendEvent: Run is null - shouldn't happen");

        // Set time and fire
        _time = (int) (System.currentTimeMillis() - _startTime);
        _onFire.accept(this);
        _count++;
        _time = -1;
    }
}