package snap.view;

/**
 * A class to represents physical attributes of a view for the purpose of physics simulation.
 */
public class ViewPhysics <T> {

    // Whether view is subject to forces in simulation
    private boolean  _dynamic;
    
    // Whether view is joint
    private boolean  _joint;
    
    // The density
    private double  _density = 1;
    
    // A group index
    private int  _groupIndex;
    
    // An ivar to hold a native object for simulation library (Body for Box2D)
    private T  _native;

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
     * Returns the group index.
     */
    public int getGroupIndex()  { return _groupIndex; }

    /**
     * Sets the group index.
     */
    public void setGroupIndex(int aValue)  { _groupIndex = aValue; }

    /**
     * Returns the native object.
     */
    public T getNative()  { return _native; }

    /**
     * Sets the native object.
     */
    public void setNative(T anObj)  { _native = anObj; }
}