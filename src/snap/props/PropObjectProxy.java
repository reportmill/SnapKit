/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class is meant to stand in for archival objects that aren't PropObject.
 */
public class PropObjectProxy extends PropObject {

    // The real object
    protected Object  _real;

    /**
     * Constructor.
     */
    public PropObjectProxy(Object aRealObject)
    {
        _real = aRealObject;
    }

    /**
     * Returns the real object.
     */
    public Object getReal()  { return _real; }
}
