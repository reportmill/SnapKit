package snap.view;

/**
 * A class to represents physical attributes of a view for the purpose of physics simulation.
 */
public class ViewPhysics <T> {

    // Whether view is subject to forces in simulation
    boolean        _dynamic;
    
    // An ivar to hold a native object for simulation library (Body for Box2D)
    T              _ntv;

/**
 * Returns whether view is subject to forces in simulation.
 */
public boolean isDynamic()  { return _dynamic; }

/**
 * Sets whether view is subject to forces in simulation.
 */
public void setDynamic(boolean aValue)  { _dynamic = aValue; }

/**
 * Returns the native object.
 */
public T getNative()  { return _ntv; }

/**
 * Sets the native object.
 */
public void setNative(T anObj)  { _ntv = anObj; }

}