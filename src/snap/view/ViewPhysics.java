package snap.view;

/**
 * A class to represents physical attributes of a view for the purpose of physics simulation.
 */
public class ViewPhysics {

    // Whether view is subject to forces in simulation
    private boolean  _dynamic;
    
    // Whether view is joint
    private boolean  _joint;
    
    // The density
    private double  _density = 1;

    // The friction
    private double  _friction = .3;

    // The restitution
    private double  _restitution = .5;

    // Whether view is draggable
    private boolean _draggable;
    
    // A group index
    private int  _groupIndex;
    
    // An ivar to hold a native object for simulation library (Body for Box2D)
    private Object _native;

    /**
     * Returns whether view is subject to forces in simulation.
     */
    public boolean isDynamic()  { return _dynamic; }

    /**
     * Sets whether view is subject to forces in simulation.
     */
    public void setDynamic(boolean aValue)  { _dynamic = aValue; }

    /**
     * Returns whether view is a joint.
     */
    public boolean isJoint()  { return _joint; }

    /**
     * Sets whether view is a joint.
     */
    public void setJoint(boolean aValue)  { _joint = aValue; }

    /**
     * Returns the body density.
     */
    public double getDensity()  { return _density; }

    /**
     * Sets the body density.
     */
    public void setDensity(double aValue)  { _density = aValue; }

    /**
     * Returns the body friction.
     */
    public double getFriction()  { return _friction; }

    /**
     * Sets the body friction.
     */
    public void setFriction(double aValue)  { _friction = aValue; }

    /**
     * Returns the body restitution.
     */
    public double getRestitution()  { return _restitution; }

    /**
     * Sets the body restitution.
     */
    public void setRestitution(double aValue)  { _restitution = aValue; }

    /**
     * Returns the group index.
     */
    public int getGroupIndex()  { return _groupIndex; }

    /**
     * Sets the group index.
     */
    public void setGroupIndex(int aValue)  { _groupIndex = aValue; }

    /**
     * Returns whether view is draggable.
     */
    public boolean isDraggable()  { return _draggable; }

    /**
     * Sets whether view is draggable.
     */
    public void setDraggable(boolean aValue)  { _draggable = aValue; }

    /**
     * Returns the native object.
     */
    public Object getNative()  { return _native; }

    /**
     * Sets the native object.
     */
    public void setNative(Object anObj)  { _native = anObj; }
}