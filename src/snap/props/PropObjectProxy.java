/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class is meant to stand in for archival objects that aren't PropObject.
 */
public abstract class PropObjectProxy<T> extends PropObject {

    // The Real class
    protected T  _real;

    /**
     * Returns the real object.
     */
    public T getReal()
    {
        if (_real != null) return _real;
        return _real = getRealImpl();
    }

    /**
     * Returns the real object.
     */
    protected T getRealImpl()  { return null; }

    /**
     * Sets the real object.
     */
    public void setReal(T aReal)
    {
        _real = aReal;
    }
}
