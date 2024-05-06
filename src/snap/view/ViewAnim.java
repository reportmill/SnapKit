/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.props.Prop;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.*;
import java.util.*;

/**
 * A class to animate View attributes.
 */
public class ViewAnim implements XMLArchiver.Archivable {

    // The View
    private View _view;

    // The parent anim
    private ViewAnim _parent;

    // The start/end times
    private int _start, _end;

    // The anim keys
    private List<String> _keys = new ArrayList<>();

    // The end values
    private Map<String, Object> _endVals = new HashMap<>();

    // The nested anims
    private List<ViewAnim> _anims = new ArrayList<>();

    // The loop count
    private int _loopCount;

    // The Interpolator
    private Interpolator _interp = Interpolator.EASE_BOTH;

    // A runnable to be called on each anim frame
    private Runnable _onFrame;

    // A runnable to be called when anim is finished
    private Runnable _onFinish;

    // Whether the anim needs to finished when cleared
    private boolean _needsFinish;

    // The start time, current time
    private int _time;

    // The max time
    private int _maxTime = -1;

    // Whether this anim was suspended because it's not visible
    private boolean _suspended;

    // The ViewUpdater currently playing this anim
    private ViewUpdater _updater;

    // A convenience for ViewUpdater
    protected int _startTime;

    /**
     * Constructor.
     */
    public ViewAnim(View aView)
    {
        _view = aView;
    }

    /**
     * Constructor.
     */
    protected ViewAnim(View aView, int aStart, int anEnd)
    {
        this(aView);
        _start = aStart;
        _end = anEnd;
    }

    /**
     * Returns the View.
     */
    public View getView()  { return _view; }

    /**
     * Returns whether this ViewAnim is the root one.
     */
    public boolean isRoot()  { return _parent == null; }

    /**
     * Returns the root ViewAnim.
     */
    public ViewAnim getRoot()  { return _parent != null ? _parent : this; }

    /**
     * Returns the root ViewAnim.
     */
    public ViewAnim getRoot(int aTime)
    {
        return getRoot().getAnim(aTime);
    }

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
     * Returns the length of the frame in milliseconds.
     */
    public int getFrameLength()  { return _end - _start; }

    /**
     * Returns whether anim is empty/cleared.
     */
    public boolean isEmpty()
    {
        if (_endVals.size() > 0)
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
    public Object getStartValue(String aKey)
    {
        if (_parent != null)
            return _parent.getEndVal(aKey);
        return getEndVal(aKey);
    }

    /**
     * Sets the start value for given key.
     */
    public void setStartValue(String aKey, Object aVal)
    {
        if (_parent != null)
            _parent.setValue(aKey, aVal);
    }

    /**
     * Returns the end value for given key.
     */
    public Object getEndVal(String aKey)
    {
        return _endVals.get(aKey);
    }

    /**
     * Returns the list of child anims.
     */
    public List <ViewAnim> getAnims()  { return _anims; }

    /**
     * Returns the anim for the given start/end.
     */
    public ViewAnim getAnim(int aTime)
    {
        // If this anim covers time, just return
        if (aTime <= getEnd()) return this;

        // Get anim for time and return if found
        for (ViewAnim anim : _anims) {
            if (aTime == anim.getEnd())
                return anim;
        }

        // Create anim
        ViewAnim anim = new ViewAnim(_view, getEnd(), aTime);
        _anims.add(anim);
        anim._parent = this;

        // Clear MaxTimes
        for (ViewAnim parentAnim = this; parentAnim != null; parentAnim = parentAnim.getParent())
            parentAnim._maxTime = -1;

        // Return
        return anim;
    }

    /**
     * Returns the max time.
     */
    public int getMaxTime()
    {
        // If already set, just return
        if (_maxTime >= 0) return _maxTime;

        // Calculate
        int max = getEnd();
        for (ViewAnim anim : _anims)
            max = Math.max(max, anim.getMaxTime());

        // Set and return
        return _maxTime = max;
    }

    /**
     * Returns whether anim is playing.
     */
    public boolean isPlaying()
    {
        return _parent != null ? _parent.isPlaying() : (_updater != null);
    }

    /**
     * Returns whether anim should be playing, but didn't have access to ViewUpdater (probably wasn't showing).
     */
    public boolean isSuspended()
    {
        return _parent != null ? _parent.isSuspended() : _suspended;
    }

    /**
     * Play the anim.
     */
    public void play()
    {
        // If Parent, forward to it. If already playing, just return
        if (_parent != null) {
            _parent.play();
            return;
        }

        // If already playing, just return
        if (isPlaying()) return;

        // Get ViewUpdater and startAnim() (or mark suspended if updater not available)
        _updater = _view.getUpdater();
        if (_updater != null) {
            _updater.startAnim(this);
            _suspended = false;
        }

        else _suspended = true;
    }

    /**
     * Stop the anim.
     */
    public void stop()
    {
        // If Parent, forward to it. If already stopped, just return
        if (_parent != null) {
            _parent.stop();
            return;
        }

        // If already stopped, just return
        if (!isPlaying()) return;

        // If ViewUpdater set, stopAnim() and clear Updater/Suspended
        if (_updater != null)
            _updater.stopAnim(this);
        _updater = null;
        _suspended = false;
    }

    /**
     * Suspends the anim. Called when anim was playing but view no longer showing. Should restart when conditions are right.
     */
    protected void suspend()
    {
        if (_parent != null) {
            _parent.suspend();
            return;
        }

        if (_updater != null) {
            _updater.stopAnim(this);
            _updater = null;
            _suspended = true;
        }
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
        int newTime = aTime;
        int oldTime = _time;
        int maxTime = getMaxTime();
        if (maxTime == 0)
            return; // Was: oldTime = -1;

        // If time already set, just return
        if (newTime == oldTime) return;
        _time = newTime;

        // Determine whether new time requires this frame to do update
        int oldTimeLocation = oldTime < _start ? -1 : oldTime >= _end ? 1 : 0;
        int newTimeLocation = newTime < _start ? -1 : newTime >= _end ? 1 : 0;
        boolean needsUpdate = oldTimeLocation * newTimeLocation != 1;

        // Get child time - if looping, shift newTime inside start/end range
        int childTime = newTime;
        if (_loopCount > 0) {
            int loopLen = maxTime - _start;
            if (childTime > maxTime)
                childTime = (newTime - _start) % loopLen + _start;
            maxTime *= _loopCount;
        }

        // If NeedsUpdate and running forward, update values before children
        if (needsUpdate && newTime > oldTime)
            updateValues();

        // Forward to children
        for (ViewAnim child : _anims)
            child.setTime(childTime);

        // If NeedsUpdate and running backward, update values after children
        if (needsUpdate && newTime < oldTime)
            updateValues();

        // Root anim stuff: Trigger OnFrame/OnFinish and stop if completed
        if (isRoot()) {

            // If on frame set, call it
            if (_onFrame != null)
                _onFrame.run();

            // If completed, stop playing and trigger onFinish
            boolean completed = _time >= maxTime;
            if (completed) {
                stop();
                if (oldTime < maxTime && _onFinish != null)
                    _onFinish.run();
            }
        }
    }

    /**
     * Updates values for current time.
     */
    protected void updateValues()
    {
        for (String key : getKeys()) {
            Object val = getValueForKeyAndTime(key, _time);
            _view.setPropValue(key, val);
        }
    }

    /**
     * Returns the value for given key and time.
     */
    protected Object getValueForKeyAndTime(String aKey, int aTime)
    {
        // Get end values
        Object fromVal = getStartValue(aKey);
        Object toVal = getEndVal(aKey);

        // Calculate ratio
        int frameStart = getStart();
        double frameLength = getFrameLength();
        double ratio = (aTime - frameStart) / frameLength;

        // Return interpolated value
        return interpolateValuesForRatio(fromVal, toVal, ratio);
    }

    /**
     * Returns the interpolated value.
     */
    public Object interpolateValuesForRatio(Object aVal1, Object aVal2, double aRatio)
    {
        // If ratio is not inside 0 - 1 range or values equal, return end value
        if (aRatio <= 0)
            return aVal1;
        if (aRatio >= 1)
            return aVal2;
        if (Objects.equals(aVal1, aVal2))
            return aVal1;

        // Interpolate numbers
        if (aVal1 instanceof Number && aVal2 instanceof Number) {
            double val1 = ((Number) aVal1).doubleValue();
            double val2 = ((Number) aVal2).doubleValue();
            return _interp.getValue(aRatio, val1, val2);
        }

        // Interpolate colors
        if (aVal1 instanceof Color || aVal2 instanceof Color) {
            Color c1 = aVal1 instanceof Color ? (Color) aVal1 : Color.CLEAR;
            Color c2 = aVal2 instanceof Color ? (Color) aVal2 : Color.CLEAR;
            double ratio = _interp.getValue(aRatio, 0, 1);
            return c1.blend(c2, ratio);
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
    public ViewAnim setPrefSize(double aW, double aH)
    {
        setPrefWidth(aW);
        return setPrefHeight(aH);
    }

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
    public ViewAnim setValue(String aKey, Object aValue)
    {
        // Handle Scale special
        if (aKey.equals("Scale"))
            return setScale(Convert.doubleValue(aValue));

        return setValue(aKey, null, aValue);
    }

    /**
     * Returns the end value for given key.
     */
    public ViewAnim setValue(String aKey, Object aVal0, Object aVal1)
    {
        // Add key and EndVal
        ListUtils.addUnique(_keys, aKey);
        _endVals.put(aKey, aVal1);

        // If Start value provided, set it
        if (aVal0 != null)
            setStartValue(aKey, aVal0);

            // If Start value missing, set it from any parent or view
        else if (_parent != null && getStartValue(aKey) == null)
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
        Object startVal = null;
        for (ViewAnim par = _parent; par != null && startVal == null; par = par._parent)
            startVal = par.getEndVal(aKey);

        // If not found, get from current view
        if (startVal == null) {
            startVal = _view.getPropValue(aKey);
            if (startVal == null) {
                if (aVal instanceof Integer)
                    startVal = 0;
                else if (aVal instanceof Double)
                    startVal = 0d;
                else if (aVal instanceof Color)
                    startVal = Color.CLEAR;
                else System.err.println("ViewAnim.findStartValue: No default start value for type " + aVal.getClass());
            }
        }

        // Return
        return startVal;
    }

    /**
     * Sets the loop count.
     */
    public ViewAnim setLoops()
    {
        return setLoopCount(Short.MAX_VALUE);
    }

    /**
     * Returns the LoopCount.
     */
    public int getLoopCount()  { return _loopCount; }

    /**
     * Sets the loop count.
     */
    public ViewAnim setLoopCount(int aValue)
    {
        _loopCount = aValue;
        return this;
    }

    /**
     * Returns the runnable to be called on each frame.
     */
    public Runnable getOnFrame()
    {
        return _parent != null ? _parent.getOnFrame() : _onFrame;
    }

    /**
     * Sets the runnable to be called on each frame.
     */
    public ViewAnim setOnFrame(Runnable aRun)
    {
        if (_parent != null)
            _parent.setOnFrame(aRun);
        else _onFrame = aRun;
        return this;
    }

    /**
     * Returns the function to be called when anim is finished.
     */
    public Runnable getOnFinish()
    {
        return _parent != null ? _parent.getOnFinish() : _onFinish;
    }

    /**
     * Sets a function to be called when anim is finished.
     */
    public ViewAnim setOnFinish(Runnable aRun)
    {
        if (_parent != null)
            _parent.setOnFinish(aRun);
        else _onFinish = aRun;
        return this;
    }

    /**
     * Sets anim to finish when cleared.
     */
    public ViewAnim needsFinish()
    {
        if (_parent != null)
            return _parent.needsFinish();
        _needsFinish = true;
        return this;
    }

    /**
     * Sets whether animation should start fast.
     */
    public ViewAnim startFast()
    {
        setInterpolator(Interpolator.EASE_OUT);
        return this;
    }

    /**
     * Sets animation to run with linear interpolator.
     */
    public ViewAnim setLinear()
    {
        setInterpolator(Interpolator.LINEAR);
        return this;
    }

    /**
     * Sets whether to ease animation in
     */
    public ViewAnim setInterpolator(Interpolator anInterp)
    {
        _interp = anInterp;
        return this;
    }

    /**
     * Clears the anim.
     */
    public ViewAnim clear()
    {
        // If parent, have it do clear instead
        if (_parent != null)
            return getRoot().clear().getAnim(_end);

        // If needs finish, do finish
        if (_needsFinish) {
            finish();
            _needsFinish = false;
        }

        // Do clear
        stop();
        _loopCount = 0;
        _interp = Interpolator.EASE_BOTH;
        _time = 0;
        _maxTime = -1;
        _onFrame = _onFinish = null;
        _keys.clear();
        _endVals.clear();
        _anims.clear();
        return this;
    }

    /**
     * Finishes the anim.
     */
    public ViewAnim finish()
    {
        // If parent set, have it do finish instead
        if (_parent != null) {
            getRoot().finish();
            return this;
        }

        // Stop and set time to end
        stop();
        int maxTime = getMaxTime();
        setTime(maxTime);

        // Return
        return this;
    }

    /**
     * Watches given View for changes and registers animation.
     */
    public void startAutoRegisterChanges(String... theProps)
    {
        _autoRegisterChangesListener = pc -> autoRegisterPropChange(pc);
        _autoRegisterChanges = new ArrayList<>();
        View view = getView();
        view.addPropChangeListener(_autoRegisterChangesListener, theProps);
    }

    /**
     * Stops watching and register changes.
     */
    public void stopAutoRegisterChanges()
    {
        // Remove/clear AutoRegisterChangeListener
        View view = getView();
        view.removePropChangeListener(_autoRegisterChangesListener);
        _autoRegisterChangesListener = null;

        // Reset old values for changes and clear changes
        for (PropChange propChange : _autoRegisterChanges) {
            String propName = propChange.getPropName();
            Object oldValue = propChange.getOldValue();
            view.setPropValue(propName, oldValue);
        }
        _autoRegisterChanges = null;
    }

    /**
     * Register anim for changes.
     */
    public void autoRegisterPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        setValue(propName, aPC.getOldValue(), aPC.getNewValue());
        _autoRegisterChanges.add(aPC);
    }

    // AutoRegisterChanges PropChangeListener
    private PropChangeListener _autoRegisterChangesListener;

    // List of changes encountered by AutoRegisterChanges PropChangeListener
    private List<PropChange> _autoRegisterChanges;

    /**
     * Returns the key frame times.
     */
    public Integer[] getKeyFrameTimes()
    {
        Set<Integer> timesSet = new HashSet<>();
        timesSet.add(getStart());
        timesSet.add(getEnd());
        for (ViewAnim anim : _anims)
            Collections.addAll(timesSet, anim.getKeyFrameTimes());
        Integer[] times = timesSet.toArray(new Integer[0]);
        Arrays.sort(times);
        return times;
    }

    /**
     * Configures this anim from given JSON/CSS style string, e.g.: "time: 300; scale: 2; time: 600; scale: 1; time: 1200; rotate: 360"
     */
    public ViewAnim setAnimString(String animString)
    {
        // Get individual prop/value strings (separated by semi-colons)
        String[] propStrings = animString.split(";");
        ViewAnim anim = this;

        // Iterate over prop strings and add each
        for (String propString : propStrings) {

            // Get "name:value" string parts
            String[] nameValueStrings = propString.split(":");

            // If both prop/value parts found, get prop name and set value
            if (nameValueStrings.length == 2) {
                String propName = nameValueStrings[0].trim();

                // Handle "time"
                if (propName.equalsIgnoreCase("time")) {
                    int newTime = Convert.intValue(nameValueStrings[1]);
                    anim = anim.getAnim(newTime);
                }

                // Handle "scale"
                else if (propName.equalsIgnoreCase("scale")) {
                    double scale = Convert.doubleValue(nameValueStrings[1]);
                    anim.setScale(scale);
                }

                // Handle "loopcount"
                else if (propName.equalsIgnoreCase("loopcount")) {
                    int loopCount = Convert.intValue(nameValueStrings[1]);
                    anim = anim.setLoopCount(loopCount);
                }

                // Handle prop
                else {
                    Prop prop = _view.getPropForName(propName);
                    if (prop != null) {
                        Object value = nameValueStrings[1];
                        Class<?> propClass = prop.getPropClass();
                        if (propClass == double.class)
                            value = Convert.doubleValue(value);
                        else if (propClass == Paint.class || propClass == Color.class)
                            value = Color.get(value);
                        anim.setValue(prop.getName(), value);
                    }

                    // If prop not found for name, complain
                    else System.err.println("PropObject.setPropsString: Unknown prop name: " + propName);
                }
            }

            // If "name:value" parts not found, complain
            else System.err.println("PropObject.setPropsString: Invalid prop string: " + propString);
        }

        // Return
        return anim;
    }

    /**
     * Sets props from items which can be times, keys, values.
     */
    public ViewAnim setProps(Object ... propItems)
    {
        for (int i = 0; i < propItems.length; i++) {
            Object propItem = propItems[i];

            // Handle Number: get anim for time and forward
            if (propItem instanceof Number) {
                int time = ((Number) propItem).intValue();
                ViewAnim timeAnim = getAnim(time);
                Object[] remainder = Arrays.copyOfRange(propItems, i + 1, propItems.length);
                timeAnim.setProps(remainder);
                return this;
            }

            // Handle String: get next item as value and set
            if (propItem instanceof String) {
                String propName = (String) propItem;
                Object propVal2 = propItems[++i];
                setValue(propName, propVal2);
            }
        }

        // Return
        return this;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = StringUtils.toString(this, "Start", "End");
        String keys = ListUtils.joinStrings(getKeys(), ",");
        if (keys.length() > 0)
            StringUtils.toStringAdd(sb, "Keys", keys);
        if (_loopCount == Short.MAX_VALUE)
            StringUtils.toStringAdd(sb, "Loops", "true");
        else if (_loopCount > 0)
            StringUtils.toStringAdd(sb, "LoopCount", _loopCount);
        if (isRoot())
            sb.append(" for ").append(_view.getClass().getSimpleName());
        if (isRoot() && _view.getName() != null)
            sb.append(' ').append(_view.getName());
        for (ViewAnim va : _anims)
            sb.append("\n    " + va.toString().replace("\n", "\n    "));
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
        if (getLoopCount() > 0)
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
            if (val instanceof Color) val = '#' + ((Color) val).toHexString();
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
        if (!anElement.getName().equals("Anim")) {
            fromXMLLegacy(anElement);
            return this;
        }

        // Unarchive LoopCount
        if (anElement.hasAttribute("LoopCount"))
            setLoopCount(anElement.getAttributeIntValue("LoopCount"));

        // Unarchive KeyValue records
        ViewAnim anim = this;
        for (int i = anElement.indexOf("KeyValue"); i >= 0; i = anElement.indexOf("KeyValue", i + 1)) {
            XMLElement keyVal = anElement.get(i);

            // Get time and make sure we have right anim
            int time = keyVal.getAttributeIntValue("Time");
            anim = anim.getAnim(time);

            // Get key and value
            String key = keyVal.getAttributeValue("Key");
            String valStr = keyVal.getAttributeValue("Value");
            Object val = null;
            if (valStr.startsWith("#")) val = new Color(valStr);
            else if (valStr.equalsIgnoreCase("true")) val = Boolean.TRUE;
            else if (valStr.equalsIgnoreCase("false")) val = Boolean.FALSE;
            else val = Convert.doubleValue(valStr);
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
        for (int i = anElement.indexOf("KeyFrame"); i >= 0; i = anElement.indexOf("KeyFrame", i + 1)) {
            XMLElement kframe = anElement.get(i);
            int time = kframe.getAttributeIntValue("time");
            anim = anim.getAnim(time);
            for (int j = kframe.indexOf("KeyValue"); j >= 0; j = kframe.indexOf("KeyValue", j + 1)) {
                XMLElement kval = kframe.get(j);
                String key = kval.getAttributeValue("key");
                double val = kval.getAttributeFloatValue("value");
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
        if (anim != null) anim.play();

        // If view is ParentView, forward for children
        if (aView instanceof ParentView) {
            ParentView par = (ParentView) aView;
            for (View child : par.getChildren())
                playDeep(child);
        }
    }

    /**
     * Stop animations deep.
     */
    public static void stopDeep(View aView)
    {
        // If anim set, stop
        ViewAnim anim = aView.getAnim(-1);
        if (anim != null)
            anim.stop();

        // If view is ParentView, forward for children
        if (aView instanceof ParentView) {
            ParentView par = (ParentView) aView;
            for (View child : par.getChildren())
                stopDeep(child);
        }
    }

    /**
     * Returns the anim time.
     */
    public static int getTimeDeep(View aView)
    {
        // If anim set, return time
        ViewAnim anim = aView.getAnim(-1);
        if (anim != null)
            return anim.getTime();

        // If view is ParentView, forward for children
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
        // If anim set, setTime
        ViewAnim anim = aView.getAnim(-1);
        if (anim != null)
            anim.setTime(aValue);

        // If view is ParentView, forward for children
        if (aView instanceof ParentView) {
            ParentView par = (ParentView) aView;
            for (View child : par.getChildren())
                setTimeDeep(child, aValue);
        }
    }
}