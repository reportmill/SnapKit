package snap.view;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import snap.util.*;

/**
 * This class represents an animation record for one node, key, to-value and duration.
 */
public class Anim implements Comparable<Anim> {
    
    // The target
    Object               _target;
    
    // The key
    String               _key;
    
    // The to/from values
    Object               _fromVal, _toVal, _val;
    
    // The start of this anim in milliseconds
    int                  _start;
    
    // The duration of the animation in milliseconds
    int                  _duration;
    
    // A runnable to be called when timer is finished
    Consumer <Anim>      _onFrame;
    
    // A runnable to be called when timer is finished
    Consumer <Anim>      _onFinish;
    
    // The current time for this anim
    int                  _time;
    
    // The time between frames in milliseconds
    int                  _period = 40;
    
    // The timer
    ViewTimer            _timer = new ViewTimer();

/**
 * Creates a new animator.
 */
public Anim()  { }

/**
 * Creates a new animator.
 */
public Anim(Object aTarget, String aKey, Object fromVal, Object toVal, int theMillis)
{
    _target = aTarget; _key = aKey; _fromVal = fromVal; _toVal = toVal; _duration = theMillis;
}

/**
 * Returns the target.
 */
public Object getTarget()  { return _target; }

/**
 * Returns the key.
 */
public String getKey()  { return _key; }

/**
 * Returns the from value.
 */
public Object getFromValue()
{
    if(_fromVal!=null) return _fromVal;
    return _fromVal = Key.getValue(getTarget(), getKey());
}

/**
 * Returns the from value.
 */
public void setFromValue(Object aValue)  { _fromVal = aValue; }

/**
 * Returns the to value.
 */
public Object getToValue()  { return _toVal; }

/**
 * Returns the start of this anim in milliseconds.
 */
public int getStart()  { return _start; }

/**
 * Sets the start of this anim in milliseconds.
 */
public void setStart(int aTime)  { _start = aTime; }

/**
 * Returns the duration of this anim in milliseconds.
 */
public int getDuration()  { return _duration; }

/**
 * Sets the duration of this anim in milliseconds.
 */
public void setDuration(int theMillis)  { _duration = theMillis; }

/**
 * Returns the end of this anim in milliseconds.
 */
public int getEnd()  { return _start + _duration; }

/**
 * Returns the time between frames in milliseconds.
 */
public int getPeriod()  { return _period; }

/**
 * Returns the time between frames in milliseconds.
 */
public void setPeriod(int theMillis)  { _period = theMillis; }

/**
 * Returns the on frame.
 */
public Consumer <Anim> getOnFrame()  { return _onFrame; }

/**
 * Sets the on frame.
 */
public void setOnFrame(Consumer <Anim> aFrame)  { _onFrame = aFrame; }
    
/**
 * Returns the on finished.
 */
public Consumer <Anim> getOnFinish()  { return _onFinish; }

/**
 * Sets the on finished.
 */
public void setOnFinish(Consumer <Anim> aFinish)  { _onFinish = aFinish; }
    
/**
 * Starts the animation.
 */
public void play()
{
    if(_timer.isRunning()) return;
    _timer.setOnFire(t -> setTime(t.getTime())); _timer.setPeriod(_period);
    _timer.start();
}

/**
 * Stops the animation.
 */
public void stop()  { _timer.stop(); }

/**
 * Returns the current time for this anim.
 */
public int getTime()  { return _time; }

/**
 * Sets the current time for this anim.
 */
public void setTime(int aTime)
{
    // Set new time
    if(aTime==_time) return;
    _time = aTime;
    
    // If outside time range, stop and set to end value
    if(_time<=getStart() || _time>=getEnd()) {
        stop();
        _val = _time>=getEnd()? getToValue() : getFromValue();
        fireFrameImpl();
        if(_onFinish!=null)
            _onFinish.accept(this);
    }
    
    // Otherwise, interpolate and set value
    else {
        _val = interpolate(getFromValue(), getToValue(), (_time-getStart())/(double)_duration);
        fireFrameImpl();
    }
}

/**
 * Returns the elapsed time.
 */
public int getElapsedTime()  { return _time; }

/**
 * Returns the current value.
 */
public Object getValue()  { return _val; }

/**
 * Called on every frame.
 */
protected void fireFrameImpl()
{
    if(_onFrame!=null) _onFrame.accept(this);
    else {
        try { Key.setValue(getTarget(), getKey(), _val); }
        catch(Exception e) { System.err.println(e); }
    }
}

/**
 * Returns the interpolated value.
 */
public Object interpolate(Object aVal1, Object aVal2, double aRatio)
{
    if(aVal1 instanceof Number && aVal2 instanceof Number) {
        double val1 = ((Number)aVal1).doubleValue(), val2 = ((Number)aVal2).doubleValue();
        return Interpolator.EASE_BOTH.getValue(aRatio, val1, val2);
    }
    return null;
}

/**
 * Standard compareTo implementation.
 */
public int compareTo(Anim anAnim)
{
    if(!SnapUtils.equals(_key, anAnim._key)) return _key.compareTo(anAnim._key);
    if(getEnd()!=anAnim.getEnd()) return getEnd()<anAnim.getEnd()? -1 : 1;
    return 0;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return String.format("Anim { key=%s, from=%s, to=%s, end=%d, val=%s, time=%d }",
        getKey(), fmt(getFromValue()), fmt(getToValue()), getEnd(), fmt(getValue()), getTime());
}

// Format value
private Object fmt(Object anObj)  { return anObj instanceof Double? _fmt.format(anObj) : anObj; }
DecimalFormat _fmt = new DecimalFormat("0.##");

}