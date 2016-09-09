/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.Interpolator;
import snap.util.Key;
import snap.util.ListUtils;

/**
 * A class to animate View attributes.
 */
public class ViewAnim {
    
    // The View
    View                 _view;
    
    // The parent anim
    ViewAnim             _parent;

    // The start/end times
    int                  _start, _end;
    
    // The anim keys
    List <String>        _keys = new ArrayList();
    
    // The start values
    Map <String,Object>  _startVals = new HashMap();
    
    // The end values
    Map <String,Object>  _endVals = new HashMap();
    
    // The nested anims
    List <ViewAnim>      _anims = new ArrayList();
    
    // The start time, current time
    int                  _startTime = -1, _time;
    
    // Whether this anim was suspended because it's not visible
    boolean              _suspended;
    
/**
 * Creates a new ViewAnim.
 */
public ViewAnim(View aView, int aStart, int anEnd)  { _view = aView; _start = aStart; _end = anEnd; }

/**
 * Returns the View.
 */
public View getView()  { return _view; }

/**
 * Returns whether this ViewAnim is the root one.
 */
public boolean isRoot()  { return _parent==null; } 

/**
 * Returns the root ViewAnim.
 */
public ViewAnim getRoot(int aTime)  { return _parent!=null? _parent.getRoot(aTime) : getAnim(aTime); } 

/**
 * Returns the parent ViewAnim.
 */
public ViewAnim getParent()  { return _parent; } 

/**
 * Returns the start value.
 */
public int getStart()  { return _start; }

/**
 * Returns the end value.
 */
public int getEnd()  { return _end; }

/**
 * Returns the len value.
 */
public int getLen()  { return _end - _start; }

/**
 * Returns the keys.
 */
public List <String> getKeys()  { return _keys; }

/**
 * Returns the start value for given key.
 */
public Object getStartVal(String aKey)
{
    Object val = _startVals.get(aKey);
    if(val==null) {
        val = Key.getValue(_view, aKey);
        _startVals.put(aKey, val);
    }
    return val;
}

/**
 * Returns the end value for given key.
 */
public Object getEndVal(String aKey)  { return _endVals.get(aKey); }

/**
 * Returns the list of child anims.
 */
public List <ViewAnim> getAnims()  { return _anims; }

/**
 * Returns the anim for the given start/end.
 */
public ViewAnim getAnim(int aTime)
{
    if(aTime<=getEnd()) return this;
    for(ViewAnim anim : _anims) {
        if(aTime==anim.getEnd()) return anim; }
    ViewAnim anim = new ViewAnim(_view, getEnd(), aTime);
    _anims.add(anim); anim._parent = this;
    return anim;
}

/**
 * Whether anim is playing.
 */
public boolean isPlaying()  { return _startTime>=0; }

/**
 * Returns whether anim is suspended.
 */
public boolean isSuspended()  { return _suspended; }

/**
 * Sets whether anim is suspended.
 */
public void setSuspended(boolean aValue)  { _suspended = aValue; }

/**
 * Returns the start time.
 */
public int getStartTime()  { return _startTime; }

/**
 * Sets the start time.
 */
protected void setStartTime(int aTime)
{
    _startTime = aTime;
    for(ViewAnim va : _anims) va.setStartTime(aTime);
}

/**
 * Sets the time, returns whether anim has completed.
 */
public boolean setTime(int aTime)
{
    // Set new time
    int oldTime = _time; _time = aTime - _startTime; if(aTime==oldTime) return false;
    
    // Get whether this anim is waiting or completed or needs update
    boolean waiting = oldTime<=_start && _time<=_start;
    boolean completed = oldTime>=_end && _time >=_end;
    boolean needsUpdate = !(waiting || completed);
    
    // If new values need to be set, set them
    if(needsUpdate) for(String key : getKeys())
        setTime(_time, key);
        
    // Forward on to anims
    completed = _time >=_end;
    for(ViewAnim a : _anims) {
        if(_time>a.getStart())
            completed &= a.setTime(aTime);
        else completed = false;
    }
    
    // If completed and root anim, stop
    if(completed && isRoot())
        stop();
    
    // Return whether completed
    return completed;
}

/**
 * Sets the time.
 */
public void setTime(long aTime, String aKey)
{
    // From/to values
    Object fromVal = getStartVal(aKey), toVal = getEndVal(aKey), val = null;
    
    // Get value for current time
    if(aTime<=getStart()) val = fromVal;
    else if(aTime>=getEnd()) val = toVal;
    else val = interpolate(fromVal, toVal, (aTime-getStart())/(double)getLen());

    try { Key.setValue(_view, aKey, val); }
    catch(Exception e) { System.err.println(e); }
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
 * Sets the X value.
 */
public ViewAnim setX(double aVal)  { return setValue(View.X_Prop, aVal); }

/**
 * Sets the Y value.
 */
public ViewAnim setY(double aVal)  { return setValue(View.Y_Prop, aVal); }

/**
 * Sets the Width value.
 */
public ViewAnim setWidth(double aVal)  { return setValue(View.Width_Prop, aVal); }

/**
 * Sets the Height value.
 */
public ViewAnim setHeight(double aVal)  { return setValue(View.Height_Prop, aVal); }

/**
 * Sets the Rotate value.
 */
public ViewAnim setRotate(double aVal)  { return setValue(View.Rotate_Prop, aVal); }

/**
 * Sets the Scale X/Y values.
 */
public ViewAnim setScale(double aVal)  { return setScaleX(aVal).setScaleY(aVal); }

/**
 * Sets the ScaleX value.
 */
public ViewAnim setScaleX(double aVal)  { return setValue(View.ScaleX_Prop, aVal); }

/**
 * Sets the ScaleY value.
 */
public ViewAnim setScaleY(double aVal)  { return setValue(View.ScaleY_Prop, aVal); }

/**
 * Returns the end value for given key.
 */
public ViewAnim setValue(String aKey, Object aValue)
{
    ListUtils.addUnique(_keys, aKey);
    _endVals.put(aKey, aValue);
    return this;
}

/**
 * Play the anim.
 */
public void play()
{
    if(_parent!=null) { _parent.play(); return; }
    RootView rview = _view.getRootView();
    if(rview!=null) { rview.playAnim(_view); _suspended = false; }
    else _suspended = true;
}

/**
 * Stop the anim.
 */
public void stop()
{
    if(_parent!=null) { _parent.stop(); return; }
    RootView rview = _view.getRootView();
    if(rview!=null) rview.stopAnim(_view);
    _suspended = false; _startTime = -1;
}

/**
 * Suspend the anim (will autostart when conditions are right).
 */
public void suspend()
{
    _suspended = true;
    if(_parent!=null) { _parent.suspend(); return; }
    RootView rview = _view.getRootView();
    if(rview!=null) rview.stopAnim(_view);
}

/**
 * Clears the anim.
 */
public ViewAnim clear()
{
    stop();
    _keys.clear(); _startVals.clear(); _endVals.clear(); _anims.clear(); return this;
}

}