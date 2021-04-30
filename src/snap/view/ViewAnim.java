/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.geom.Pos;
import snap.util.*;

/**
 * A class to animate View attributes.
 */
public class ViewAnim implements XMLArchiver.Archivable {
    
    // The View
    private View  _view;
    
    // The parent anim
    private ViewAnim  _parent;

    // The start/end times
    private int  _start, _end;
    
    // The anim keys
    private List <String>  _keys = new ArrayList<>();
    
    // The end values
    private Map <String,Object>  _endVals = new HashMap<>();
    
    // The nested anims
    private List <ViewAnim>  _anims = new ArrayList<>();
    
    // The loop count
    private int  _loopCount;
    
    // The Interpolator
    private Interpolator  _interp = Interpolator.EASE_BOTH;
    
    // A runnable to be called on each anim frame
    private Runnable  _onFrame;
    
    // A runnable to be called when anim is finished
    private Runnable  _onFinish;
    
    // Whether the anim needs to finished when cleared
    private boolean  _needsFinish;
    
    // The start time, current time
    private int  _time;
    
    // The max time
    private int  _maxTime = -1;
    
    // Whether this anim was suspended because it's not visible
    private boolean  _suspended;
    
    // The ViewUpdater currently playing this anim
    private ViewUpdater  _updater;
    
    // A convenience for ViewUpdater
    protected int  _startTime;
    
    /**
     * Creates a new ViewAnim.
     */
    public ViewAnim(View aView)  { _view = aView; }

    /**
     * Creates a new ViewAnim.
     */
    protected ViewAnim(View aView, int aStart, int anEnd)
    {
        this(aView);
        _start = aStart; _end = anEnd;
    }

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
    public ViewAnim getRoot()  { return _parent!=null? _parent : this; }

    /**
     * Returns the root ViewAnim.
     */
    public ViewAnim getRoot(int aTime)  { return getRoot().getAnim(aTime); }

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
     * Returns whether anim is empty/cleared.
     */
    public boolean isEmpty()
    {
        if (_endVals.size()>0)
            return false;
        for (ViewAnim child : _anims)
            if (!child.isEmpty())
                return false;
        return true;
    }

    /**
     * Returns the keys.
     */
    public List <String> getKeys()  { return _keys; }

    /**
     * Returns the start value for given key.
     */
    public Object getStartVal(String aKey)  { return _parent!=null? _parent.getEndVal(aKey) : getEndVal(aKey); }

    /**
     * Sets the start value for given key.
     */
    public ViewAnim setStartVal(String aKey, Object aVal)
    {
        if (_parent!=null)
            _parent.setValue(aKey, aVal);
        return this;
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
        if (aTime<=getEnd()) return this;
        for (ViewAnim anim : _anims) {
            if (aTime==anim.getEnd()) return anim; }
        ViewAnim anim = new ViewAnim(_view, getEnd(), aTime);
        _anims.add(anim); anim._parent = this;

        // Clear MaxTimes and return
        for (ViewAnim p=this;p!=null;p=p.getParent()) p._maxTime = -1;
        return anim;
    }

    /**
     * Returns the max time.
     */
    public int getMaxTime()
    {
        if (_maxTime>=0) return _maxTime;
        int max = getEnd();
        for (ViewAnim anim : _anims) max = Math.max(max, anim.getMaxTime());
        return _maxTime = max;
    }

    /**
     * Returns the key frame times.
     */
    public Integer[] getKeyFrameTimes()
    {
        Set <Integer> timesSet = new HashSet<>();
        timesSet.add(getStart()); timesSet.add(getEnd());
        for (ViewAnim anim : _anims) Collections.addAll(timesSet, anim.getKeyFrameTimes());
        Integer times[] = timesSet.toArray(new Integer[0]);
        Arrays.sort(times);
        return times;
    }

    /**
     * Returns whether anim is playing.
     */
    public boolean isPlaying()  { return _parent!=null? _parent.isPlaying() : (_updater!=null); }

    /**
     * Returns whether anim should be playing, but didn't have access to ViewUpdater (probably wasn't showing).
     */
    public boolean isSuspended()  { return _parent!=null? _parent.isSuspended() : _suspended; }

    /**
     * Play the anim.
     */
    public void play()
    {
        // If Parent, forward to it. If already playing, just return
        if (_parent!=null) { _parent.play(); return; }
        if (isPlaying()) return;

        // Get ViewUpdater and startAnim() (or mark suspended if updater not available)
        _updater = _view.getUpdater();
        if (_updater!=null) { _updater.startAnim(this); _suspended = false; }
        else _suspended = true;
    }

    /**
     * Stop the anim.
     */
    public void stop()
    {
        // If Parent, forward to it. If already stopped, just return
        if (_parent!=null) { _parent.stop(); return; }
        if (!isPlaying()) return;

        // If ViewUpdater set, stopAnim() and clear Updater/Suspended
        if (_updater!=null) _updater.stopAnim(this);
        _updater = null; _suspended = false;
    }

    /**
     * Suspends the anim. Called when anim was playing but view no longer showing. Should restart when conditions are right.
     */
    protected void suspend()
    {
        if (_parent!=null) { _parent.suspend(); return; }
        if (_updater!=null) { _updater.stopAnim(this); _updater = null; _suspended = true; }
    }

    /**
     * Returns the current time.
     */
    public int getTime()  { return _time; }

    /**
     * Sets the current time.
     */
    public void setTime(int aTime)
    {
        // Get newTime and oldTime. If MaxTime is zero, make sure there's a change
        int newTime = aTime, oldTime = _time, maxTime = getMaxTime(); if (maxTime==0) oldTime = -1;

        // If looping, adjust newTime
        if (_loopCount>0) {
            int start = getStart(), loopCount = _loopCount -1;
            int loopLen = maxTime - start;
            int loopTime = (newTime - start) - loopCount*loopLen;
            if (loopTime<0) loopTime = (newTime - start)%loopLen;
            newTime = start + loopTime;
        }

        // If time already set, just return
        if (newTime==oldTime) return;
        _time = newTime;

        // If new values need to be set, set them
        boolean needsUpdate = !(oldTime<=_start && newTime<=_start || oldTime>=_end && newTime>=_end);
        if (needsUpdate)
            updateValues();

        // Forward on to child anims (if within range)
        for (ViewAnim child : _anims)
            if (_time>child.getStart())
                child.setTime(_time);

        // If on frame set, call it
        if (_onFrame!=null)
            _onFrame.run();

        // If completed and root anim, stop playing
        boolean completed = _time >= maxTime;
        if (completed && isRoot())
            stop();

        // If completed and there is an OnFinish, trigger it
        if (completed && oldTime<maxTime && _onFinish!=null)
            _onFinish.run();
    }

    /**
     * Updates values for current time.
     */
    protected void updateValues()
    {
        for (String key : getKeys()) {
            Object val = getValue(key, _time);
            _view.setPropValue(key, val);
        }
    }

    /**
     * Returns the interpolated value.
     */
    public Object interpolate(Object aVal1, Object aVal2)
    {
        double time = getTime(), start = getStart(), end = getEnd();
        if (time<=start) return aVal1;
        if (time>=end) return aVal2;
        return interpolate(aVal1, aVal2, (time-start)/(end - start));
    }

    /**
     * Returns the interpolated value.
     */
    public Object interpolate(Object aVal1, Object aVal2, double aRatio)
    {
        // Interpolate numbers
        if (aVal1 instanceof Number && aVal2 instanceof Number) {
            double val1 = ((Number)aVal1).doubleValue(), val2 = ((Number)aVal2).doubleValue();
            return _interp.getValue(aRatio, val1, val2);
        }

        // Interpolate colors
        if (aVal1 instanceof Color || aVal2 instanceof Color) {
            Color c1 = aVal1 instanceof Color? (Color)aVal1 : Color.CLEAR;
            Color c2 = aVal2 instanceof Color? (Color)aVal2 : Color.CLEAR;
            double ratio = _interp.getValue(aRatio, 0, 1);
            return c1.blend(c2, ratio);
        }

        return null;
    }

    /**
     * Returns the value for given key at current time.
     */
    public Object getValue(String aKey)  { return getValue(aKey, _time); }

    /**
     * Returns the value for given key and time.
     */
    public Object getValue(String aKey, int aTime)
    {
        // Get start/end values (just return if they are equal)
        Object fromVal = getStartVal(aKey);
        Object toVal = getEndVal(aKey);
        if (SnapUtils.equals(fromVal, toVal)) return fromVal;

        // Return value based on time
        Object val;
        if (aTime<=getStart()) val = fromVal;
        else if (aTime>=getEnd()) val = toVal;
        else {
            double ratio = (aTime-getStart())/(double)getLen();
            val = interpolate(fromVal, toVal, ratio);
        }
        return val;
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
     * Sets the PrefWidth value.
     */
    public ViewAnim setPrefWidth(double aVal)  { return setValue(View.PrefWidth_Prop, aVal); }

    /**
     * Sets the PrefHeight value.
     */
    public ViewAnim setPrefHeight(double aVal)  { return setValue(View.PrefHeight_Prop, aVal); }

    /**
     * Sets preferred size values.
     */
    public ViewAnim setPrefSize(double aW, double aH)  { setPrefWidth(aW); return setPrefHeight(aH); }

    /**
     * Sets the TransX value.
     */
    public ViewAnim setTransX(double aVal)  { return setValue(View.TransX_Prop, aVal); }

    /**
     * Sets the TransY value.
     */
    public ViewAnim setTransY(double aVal)  { return setValue(View.TransY_Prop, aVal); }

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
     * Sets the Fill value.
     */
    public ViewAnim setFill(Paint aVal)  { return setValue(View.Fill_Prop, aVal); }

    /**
     * Sets the Opacity value.
     */
    public ViewAnim setOpacity(double aVal)  { return setValue(View.Opacity_Prop, aVal); }

    /**
     * Returns the end value for given key.
     */
    public ViewAnim setValue(String aKey, Object aValue)  { return setValue(aKey, null, aValue); }

    /**
     * Returns the end value for given key.
     */
    public ViewAnim setValue(String aKey, Object aVal0, Object aVal1)
    {
        // Add key and EndVal
        ListUtils.addUnique(_keys, aKey);
        _endVals.put(aKey, aVal1);

        // If Start value provided, set it
        if (aVal0!=null)
            setStartVal(aKey, aVal0);

        // If Start value missing, set it from any parent or view
        else if (_parent!=null && getStartVal(aKey)==null)
            _parent.setValue(aKey, findStartValue(aKey, aVal1));

        // Return
        return this;
    }

    /**
     * Returns the start value for given end value by searching parents or asking view.
     */
    protected Object findStartValue(String aKey, Object aVal)
    {
        // Iterate up parents to see if start val has been set in previous key frame
        Object sval = null;
        for (ViewAnim par=_parent; par!=null && sval==null;par=par._parent)
            sval = par.getEndVal(aKey);

        // If not found, get from current view
        if (sval==null) {
            sval = _view.getPropValue(aKey);
            if (sval==null) {
                if (aVal instanceof Integer) sval = 0;
                else if (aVal instanceof Double) sval = 0d;
                else if (aVal instanceof Color) sval = Color.CLEAR;
                else System.err.println("ViewAnim.findStartValue: No default start value for type " + aVal.getClass());
            }
        }

        // Return start value
        return sval;
    }

    /**
     * Sets the loop count.
     */
    public ViewAnim setLoops()  { return setLoopCount(Short.MAX_VALUE); }

    /**
     * Returns the LoopCount.
     */
    public int getLoopCount()  { return _loopCount; }

    /**
     * Sets the loop count.
     */
    public ViewAnim setLoopCount(int aValue)  { _loopCount = aValue; return this; }

    /**
     * Returns the consumer to be called on each frame.
     */
    public Runnable getOnFrame()  { return _parent!=null? _parent.getOnFrame() : _onFrame; }

    /**
     * Sets the consumer to be called on each frame.
     */
    public ViewAnim setOnFrame(Runnable aRun)
    {
        if (_parent!=null) _parent.setOnFrame(aRun);
        else _onFrame = aRun;
        return this;
    }

    /**
     * Sets the consumer to be called on each frame.
     */
    public ViewAnim setOnFrame(Consumer <ViewAnim> aCall)  { return setOnFrame(() -> aCall.accept(this)); }

    /**
     * Returns the function to be called when anim is finished.
     */
    public Runnable getOnFinish()  { return _parent!=null? _parent.getOnFinish(): _onFinish; }

    /**
     * Sets a function to be called when anim is finished.
     */
    public ViewAnim setOnFinish(Runnable aRun)
    {
        if (_parent!=null) _parent.setOnFinish(aRun);
        else _onFinish = aRun;
        return this;
    }

    /**
     * Sets a function to be called when anim is finished.
     */
    public ViewAnim setOnFinish(Consumer <ViewAnim> aFinish)  { return setOnFinish(() -> aFinish.accept(this)); }

    /**
     * Sets anim to finish when cleared.
     */
    public ViewAnim needsFinish()
    {
        if (_parent!=null) _parent.needsFinish();
        return this;
    }

    /**
     * Sets whether animation should start fast.
     */
    public ViewAnim startFast()  { setInterpolator(Interpolator.EASE_OUT); return this; }

    /**
     * Sets animation to run with linear interpolator.
     */
    public ViewAnim setLinear()  { setInterpolator(Interpolator.LINEAR); return this; }

    /**
     * Sets whether to ease animation in
     */
    public ViewAnim setInterpolator(Interpolator anInterp)  { _interp = anInterp; return this; }

    /**
     * Clears the anim.
     */
    public ViewAnim clear()
    {
        // If parent, have it do clear instead
        if (_parent!=null) return getRoot().clear().getAnim(_end);

        // If needs finish, do finish
        if (_needsFinish) {
            finish(); _needsFinish = false;
        }

        // Do clear
        stop(); _loopCount = 0; _onFinish = null; _interp = Interpolator.EASE_BOTH;
        _time = 0; _maxTime = -1; _onFrame = _onFinish = null;
        _keys.clear(); _endVals.clear(); _anims.clear();
        return this;
    }

    /**
     * Finishes the anim.
     */
    public ViewAnim finish()
    {
        // If parent, have it do finish instead
        if (_parent!=null) { getRoot().finish(); return this; }

        // Do finish
        int maxTime = getMaxTime()*(getLoopCount() + 1);
        setTime(maxTime);
        return this;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        StringBuffer sb = StringUtils.toString(this, "Start", "End");
        String keys = ListUtils.joinStrings(getKeys(), ",");
        if (keys.length()>0) StringUtils.toStringAdd(sb, "Keys", keys);
        if (_loopCount==Short.MAX_VALUE) StringUtils.toStringAdd(sb, "Loops", "true");
        else if (_loopCount>0) StringUtils.toStringAdd(sb, "LoopCount", _loopCount);
        if (isRoot()) sb.append(" for ").append(_view.getClass().getSimpleName());
        if (isRoot() && _view.getName()!=null) sb.append(' ').append(_view.getName());
        for (ViewAnim va : _anims) sb.append("\n    " + va.toString().replace("\n", "\n    "));
        return sb.toString();
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Create element
        XMLElement e = new XMLElement("Anim");

        // Unarchive Loops, LoopCount
        if (getLoopCount()>0)
            e.add("LoopCount", getLoopCount());

        // Archive KeyValues
        toXMLAnim(this, e);
        return e;
    }

    /**
     * XML archival.
     */
    protected static void toXMLAnim(ViewAnim theAnim, XMLElement aXML)
    {
        // Iterate over values
        for (String key : theAnim.getKeys()) {
            XMLElement kvxml = new XMLElement("KeyValue");
            kvxml.add("Time", theAnim.getEnd());
            kvxml.add("Key", key);
            Object val = theAnim.getEndVal(key);
            if (val instanceof Color) val = '#' + ((Color)val).toHexString();
            kvxml.add("Value", val);
            aXML.add(kvxml);
        }

        // Iterate over children
        for (ViewAnim child : theAnim.getAnims())
            toXMLAnim(child, aXML);
    }

    /**
     * XML unarchival.
     */
    public ViewAnim fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Legacy
        if (!anElement.getName().equals("Anim")) { fromXMLLegacy(anElement); return this; }

        // Unarchive LoopCount
        if (anElement.hasAttribute("LoopCount"))
            setLoopCount(anElement.getAttributeIntValue("LoopCount"));

        // Unarchive KeyValue records
        ViewAnim anim = this;
        for (int i=anElement.indexOf("KeyValue");i>=0;i=anElement.indexOf("KeyValue",i+1)) {
            XMLElement keyVal = anElement.get(i);

            // Get time and make sure we have right anim
            int time = keyVal.getAttributeIntValue("Time");
            anim = anim.getAnim(time);

            // Get key and value
            String key = keyVal.getAttributeValue("Key");
            String valStr = keyVal.getAttributeValue("Value"); Object val = null;
            if (valStr.startsWith("#")) val = new Color(valStr);
            else if (valStr.equalsIgnoreCase("true")) val = Boolean.TRUE;
            else if (valStr.equalsIgnoreCase("false")) val = Boolean.FALSE;
            else val = SnapUtils.doubleValue(valStr);
            anim.setValue(key, val);
        }

        // Return this anim
        return this;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLLegacy(XMLElement anElement)
    {
        ViewAnim anim = this;
        for (int i=anElement.indexOf("KeyFrame");i>=0;i=anElement.indexOf("KeyFrame",i+1)) {
            XMLElement kframe = anElement.get(i); int time = kframe.getAttributeIntValue("time");
            anim = anim.getAnim(time);
            for (int j=kframe.indexOf("KeyValue");j>=0;j=kframe.indexOf("KeyValue",j+1)) { XMLElement kval = kframe.get(j);
                String key = kval.getAttributeValue("key"); double val = kval.getAttributeFloatValue("value");
                anim.setValue(key, val);
            }
            if (kframe.getAttributeBoolValue("Loops", false)) setLoops();
            if (kframe.hasAttribute("LoopCount")) setLoopCount(kframe.getAttributeIntValue("LoopCount"));
        }
    }

    /**
     * Play animations deep.
     */
    public static void playDeep(View aView)
    {
        // If anim, play
        ViewAnim anim = aView.getAnim(-1);
        if (anim!=null) anim.play();

        // If parent, forward for children
        if (aView instanceof ParentView) { ParentView par = (ParentView)aView;
            for (View child : par.getChildren())
                playDeep(child);
        }
    }

    /**
     * Stop animations deep.
     */
    public static void stopDeep(View aView)
    {
        // If anim, stop
        ViewAnim anim = aView.getAnim(-1);
        if (anim!=null) anim.stop();

        // If parent, forward for children
        if (aView instanceof ParentView) { ParentView par = (ParentView)aView;
            for (View child : par.getChildren())
                stopDeep(child);
        }
    }

    /**
     * Returns the anim time.
     */
    public static int getTimeDeep(View aView)
    {
        // If anim, return time
        ViewAnim anim = aView.getAnim(-1);
        if (anim!=null)
            return anim.getTime();

        // If parent, forward for children
        if (aView instanceof ParentView) {
            ParentView par = (ParentView) aView;
            for (View child : par.getChildren())
                if (child.getAnim(-1) != null)
                    return child.getAnim(-1).getTime();
        }

        // Return 0. Should probably getAnimDeepAny().getTime()
        return 0;
    }

    /**
     * Sets the anim time deep.
     */
    public static void setTimeDeep(View aView, int aValue)
    {
        // If anim, setTime
        ViewAnim anim = aView.getAnim(-1);
        if (anim!=null) anim.setTime(aValue);

        // If parent, forward for children
        if (aView instanceof ParentView) { ParentView par = (ParentView)aView;
            for (View child : par.getChildren())
                setTimeDeep(child, aValue);
        }
    }

    /**
     * Sets the alignment.
     */
    public static void setAlign(View aView, Pos aPos, int aTime)
    {
        ViewAnimUtils.setAlign(aView, aPos, aTime);
    }
}