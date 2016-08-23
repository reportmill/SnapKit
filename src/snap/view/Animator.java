package snap.view;
import java.util.*;

/**
 * This class manages Anims for a View.
 */
public class Animator {
    
    // The object this animator works for
    View                     _targ;
    
    // The current time
    int                      _time;
    
    // The max time
    int                      _maxTime = -1;
    
    // The number of loops
    int                      _loopCount = -1;
    
    // A map of anim Lists by key
    Map <String,List<Anim>>  _anims = new HashMap();
    
    // The timer
    ViewTimer                _timer = new ViewTimer();

    // An empty list of anims
    static List <Anim>       EMPTY_ANIMS = Collections.EMPTY_LIST;

/**
 * Creates a new Animator for given target.
 */
public Animator(View aTarg)  { _targ = aTarg; }

/**
 * Returns the current time.
 */
public int getTime()  { return _time; }

/**
 * Sets the current time.
 */
public void setTime(int aTime)
{
    // Set time (just return if time already set)
    if(aTime==_time) return;
    _time = aTime;
    
    // Iterate over keys, get anim for key/time and update time
    for(String key : _anims.keySet()) {
        Anim anim = getAnimAt(key, _time);
        anim.setTime(_time);
    }
}

/**
 * Main setTime method called by timer.
 */
protected void setTimeAll()
{
    int time = _timer.getTime();
    if(_maxTime>0 && time>_maxTime) {
        if(_loopCount>0 && time/_maxTime>_loopCount) stop();
        else time = time%_maxTime;
    }
    
    // Set time deep
    setTime(_targ, time);
}

/**
 * Real call to setTime with recurse call for child nodes with animators.
 */
protected void setTime(View aView, int aTime)
{
    Animator animator = aView.getAnimator();
    if(animator!=null) animator.setTime(aTime%5000);
    if(aView instanceof ParentView) { ParentView par = (ParentView)aView;
        for(View child : par.getChildren()) setTime(child, aTime); }
}

/**
 * Returns the max time.
 */
public int getMaxTime()  { return _maxTime; }

/**
 * Sets the max time.
 */
public void setMaxTime(int aTime)  { _maxTime = aTime; }

/**
 * Returns the loop count.
 */
public int getLoopCount()  { return _loopCount; }

/**
 * Sets the loop count.
 */
public void setLoopCount(int aCount)  { _loopCount = aCount; }

/**
 * Returns the list of anims for a key.
 */
public List <Anim> getAnims(String aKey)
{
    List <Anim> anims = _anims.get(aKey);
    return anims!=null? anims : EMPTY_ANIMS;
}

/**
 * Returns the anim for key at given time.
 */
public Anim getAnimAt(String aKey, int aTime)
{
    List <Anim> anims = getAnims(aKey);
    for(Anim anim : anims)
        if(aTime<=anim.getEnd())
            return anim;
    return anims.size()>0? anims.get(anims.size()-1) : null;
}

/**
 * Adds an anim (key frame) at given time.
 */
protected void addAnim(Anim anAnim)
{
    String key = anAnim.getKey();
    List <Anim> anims = getAnims(key); if(anims==EMPTY_ANIMS) _anims.put(key, anims = new ArrayList());
    int index = Collections.binarySearch(anims, anAnim);
    if(index>=0) anims.set(index, anAnim);
    else anims.add(-index-1, anAnim);
}

/**
 * Adds a key frame (anim) for given key, value and time.
 */
public void addKeyFrame(String aKey, Object aVal, int aTime)
{
    // Add anim for key, value and time
    String key = aKey.equals("Roll")? "Rotate" : aKey;
    Anim anim = new Anim(_targ, key, null, aVal, aTime);
    addAnim(anim);
    
    // Iterate over anims and reset start/duration
    List <Anim> anims = getAnims(key); int start = 0; Object fromVal = null;
    for(Anim an : anims) {
        int end = an.getEnd(); an.setStart(start); an.setDuration(end-start); start = end;
        if(fromVal!=null) an.setFromValue(fromVal); else an.getFromValue(); fromVal = an.getToValue();
    }
}

/**
 * Starts the animation.
 */
public void play()
{
    if(_timer.isRunning()) return;
    _timer.setOnFire(t -> setTimeAll()); _timer.setPeriod(40);
    _timer.start();
}

/**
 * Stops the animation.
 */
public void stop()  { _timer.stop(); }

/**
 * Pauses the animation.
 */
public void pause()  { _timer.pause(); }

/**
 * Returns whether animation timer is running.
 */
public boolean isRunning()  { return _timer.isRunning(); }

/**
 * Returns whether animation timer is paused.
 */
public boolean isPaused()  { return _timer.isPaused(); }

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = "Animator { keys=\"";
    for(String s : _anims.keySet()) str += s + ',';  str += "\" }\n";
    for(String key : _anims.keySet()) { List <Anim> anims = _anims.get(key);
        for(Anim anim : anims) str += anim + "\n"; }
    return str;
}

}